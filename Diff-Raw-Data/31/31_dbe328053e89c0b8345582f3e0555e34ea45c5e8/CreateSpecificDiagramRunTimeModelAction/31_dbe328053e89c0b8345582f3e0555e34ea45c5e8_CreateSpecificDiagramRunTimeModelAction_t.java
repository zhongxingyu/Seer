/*
 * Copyright (c) 2006, 2007 Borland Software Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Borland Software Corporation - initial API and implementation
 */
 package org.eclipse.gmf.internal.codegen.popup.actions;
 
 import java.io.IOException;
 import java.util.Collections;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.gmf.internal.bridge.genmodel.DiagramRunTimeModelTransformer;
 import org.eclipse.gmf.internal.bridge.genmodel.DummyGenModel;
 import org.eclipse.gmf.internal.bridge.genmodel.RuntimeGenModelAccess;
 import org.eclipse.gmf.mappings.Mapping;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 public class CreateSpecificDiagramRunTimeModelAction implements IObjectActionDelegate {
 
 	private IFile myMapFile;
 
 	private IWorkbenchPart myPart;
 
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		myPart = targetPart;
 	}
 
 	public void run(IAction action) {
 		final ResourceSetImpl resSet = new ResourceSetImpl();
 		final Mapping m = (Mapping) resSet.getResource(getMapModelURI(), true).getContents().get(0);
 		final String packName = myMapFile.getLocation().removeFileExtension().lastSegment();
 		DiagramRunTimeModelTransformer t = new DiagramRunTimeModelTransformer(packName, getMapModelURI().appendSegment("diagramrt").toString());
 		t.transform(m);
 		EPackage drtModel = t.getResult();
 		Resource drtEcoreModelRes = resSet.createResource(getDestEcoreURI());
 		URI genModelURI = getDestGenModelURI();
 		Resource drtGenModelRes = Resource.Factory.Registry.INSTANCE.getFactory(genModelURI).createResource(genModelURI);
 		resSet.getResources().add(drtGenModelRes);
 		// Resource drtGenModelRes =
 		// resSet.createResource(getDestGenModelURI());
 		drtEcoreModelRes.getContents().add(drtModel);
 		drtGenModelRes.getContents().add(createGenModel(drtModel));
 		try {
 			drtEcoreModelRes.save(Collections.EMPTY_MAP);
 			drtGenModelRes.save(Collections.EMPTY_MAP);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 
 	}
 
 	private URI getDestEcoreURI() {
 		IPath p = myMapFile.getFullPath();
 		return URI.createPlatformResourceURI(p.removeLastSegments(1).append(p.removeFileExtension().lastSegment() + "-drt").addFileExtension("ecore").toString(), true);
 	}
 
 	private URI getDestGenModelURI() {
 		IPath p = myMapFile.getFullPath();
 		return URI.createPlatformResourceURI(p.removeLastSegments(1).append(p.removeFileExtension().lastSegment() + "-drt").addFileExtension("genmodel").toString(), true);
 	}
 
 	public void selectionChanged(IAction action, ISelection selection) {
 		myMapFile = (IFile) ((IStructuredSelection) selection).getFirstElement();
 	}
 
 	private URI getMapModelURI() {
 		return URI.createPlatformResourceURI(myMapFile.getFullPath().toString(), true);
 	}
 
 	protected Shell getShell() {
 		return myPart.getSite().getShell();
 	}
 
 	@SuppressWarnings("unchecked")
 	private GenModel createGenModel(EPackage model) {
 		GenModel genModel = new DummyGenModel(model, null).create();
 		genModel.getForeignModel().add(model.eResource().getURI().toString());
 		genModel.getUsedGenPackages().add(getDiagramRunTimeGenPackage());
 
 		GenPackage genPackage = genModel.getGenPackages().get(0);
 		genPackage.setPrefix(model.getName() + "DRT");
 		return genModel;
 	}
 
 	private GenPackage getDiagramRunTimeGenPackage() {
 		RuntimeGenModelAccess runtimeAccess = new RuntimeGenModelAccess();
 		runtimeAccess.ensure();
 		return runtimeAccess.genPackage();
 	}
 }
