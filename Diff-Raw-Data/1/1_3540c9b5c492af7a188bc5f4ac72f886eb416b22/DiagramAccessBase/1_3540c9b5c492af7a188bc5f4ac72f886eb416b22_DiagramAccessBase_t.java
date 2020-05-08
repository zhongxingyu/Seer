 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.common;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.eclipse.etrice.core.room.RoomModel;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 
 /**
  * @author hrentz
  *
  */
 public abstract class DiagramAccessBase {
 
 	private static final String DIAGRAMS_FOLDER_NAME = "diagrams";
 
 	/**
 	 * 
 	 */
 	public DiagramAccessBase() {
 		super();
 	}
 
 	public Diagram getDiagram(StructureClass sc) {
 		Resource resource = sc.eResource();
 		if (resource==null)
 			return null;
 		
 		URI uri = resource.getURI();
 		
 		// TODOHRR: put common diagram access code into ui.common
 		// make abstract methods get fileEtension() and populateDiagram()
 		
 		String modelName = ((RoomModel) sc.eContainer()).getName();
 		
 		URI diagURI = null;
 		boolean exists = false;
 		if (uri.isPlatformResource()) {
 			uri = uri.trimSegments(1);
 			IFolder parentFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(uri.toPlatformString(true)));
 	
 			IFolder diagramFolder = parentFolder.getFolder(DIAGRAMS_FOLDER_NAME);
 			
 			IFile diagramFile = diagramFolder.getFile(modelName+"."+sc.getName()+getFileExtension());
 			diagURI = URI.createPlatformResourceURI(diagramFile.getFullPath().toString(), true);
 			exists = diagramFile.exists();
 		}
 		else {
 			File diagramFile = new File(uri.toFileString());
 			diagramFile = new File(diagramFile.getParent()
 					+File.separator+DIAGRAMS_FOLDER_NAME
 					+File.separator+modelName+"."+sc.getName()+getFileExtension());
 			diagURI = URI.createFileURI(diagramFile.getPath());
 			exists = diagramFile.exists();
 		}
 		
 		ResourceSet rs = new ResourceSetImpl();
 		if (exists) {
 			Resource diagRes = rs.getResource(diagURI, true);
 			if (diagRes.getContents().isEmpty())
 				return null;
 			if (diagRes.getContents().get(0) instanceof Diagram)
 				return (Diagram) diagRes.getContents().get(0);
 		}
 		else {
 			Resource diagRes = rs.createResource(diagURI);
 			
 			Diagram diagram = Graphiti.getPeCreateService().createDiagram(getDiagramTypeId(), getDiagramName(sc), true);
 			diagRes.getContents().add(diagram);
 			
 			populatediagram(sc, diagram);
 			
 			try {
 				diagRes.save(Collections.EMPTY_MAP);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return diagram;
 		}
 		
 		return null;
 	}
 
 	private void populatediagram(StructureClass ac, Diagram diagram) {
 		ResourceSet rs = diagram.eResource().getResourceSet();
 		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(rs);
 		if (editingDomain == null) {
 			// Not yet existing, create one
 			editingDomain = TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain(rs);
 		}
 	
 		// IMPORTANT STEP: this resolves the object and creates a new resource in the resource set
 		URI boUri = EcoreUtil.getURI(ac);
 		ac = (StructureClass) editingDomain.getResourceSet().getEObject(boUri, true);
 		
 		editingDomain.getCommandStack().execute(getInitialCommand(ac, diagram, editingDomain));
 		editingDomain.dispose();
 	}
 
 	public void openDiagramEditor(StructureClass sc) {
 		Diagram diagram = getDiagram(sc);
 		
 		String platformString = diagram.eResource().getURI().toPlatformString(true);
 		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformString));
 		IFileEditorInput input = new FileEditorInput(file);
 	
 		try {
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, getEditorId());
 		} catch (PartInitException e) {
 			String error = "Error while opening diagram editor";
 			System.err.println(error);
 		}
 	}
 
 	abstract protected String getDiagramName(StructureClass sc);
 	abstract protected String getDiagramTypeId();
 	abstract protected String getEditorId();
 	abstract protected String getFileExtension();
 	abstract protected Command getInitialCommand(StructureClass ac, Diagram diagram, TransactionalEditingDomain editingDomain);
 
 }
