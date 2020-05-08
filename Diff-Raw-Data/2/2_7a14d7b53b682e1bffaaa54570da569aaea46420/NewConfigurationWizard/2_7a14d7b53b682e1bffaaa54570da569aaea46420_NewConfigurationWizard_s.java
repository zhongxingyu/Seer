 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.xmof.configuration.ui.wizards;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.modelexecution.xmof.configuration.ConfigurationGenerator;
 
 public class NewConfigurationWizard extends Wizard implements INewWizard {
 
 	private SelectTargetFilePage selectTargetFilePage;
 	private SelectEcoreModelFilePage selectEcoreModelFilePage;
 	private ISelection selection;
 	private ResourceSet resourceSet = new ResourceSetImpl();
 
 	public NewConfigurationWizard() {
 		super();
 		setNeedsProgressMonitor(true);
 	}
 
 	public void addPages() {
 		selectEcoreModelFilePage = new SelectEcoreModelFilePage(selection,
 				resourceSet);
 		selectTargetFilePage = new SelectTargetFilePage(selection);
 		addPage(selectEcoreModelFilePage);
 		addPage(selectTargetFilePage);
 	}
 
 	public boolean performFinish() {
 
 		final Resource metamodelResource = selectEcoreModelFilePage
 				.getMetamodelResource();
 		final Collection<EClass> mainClasses = selectEcoreModelFilePage
 				.getMainClasses();
 		final IFile xmofFile = selectTargetFilePage.getModelFile();
 		IRunnableWithProgress op = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor)
 					throws InvocationTargetException {
 				try {
 					doFinish(xmofFile, metamodelResource, mainClasses, monitor);
 				} catch (CoreException e) {
 					throw new InvocationTargetException(e);
 				} finally {
 					monitor.done();
 				}
 			}
 		};
 		try {
 			getContainer().run(true, false, op);
 		} catch (InterruptedException e) {
 			return false;
 		} catch (InvocationTargetException e) {
 			Throwable realException = e.getTargetException();
 			MessageDialog.openError(getShell(), "Error",
 					realException.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	private void doFinish(final IFile xmofFile, Resource metamodelResource,
 			Collection<EClass> mainClasses, IProgressMonitor monitor)
 			throws CoreException {
 		if (metamodelResource == null) {
 			throwCoreException("No ecore file selected");
 		}
 		if (xmofFile == null) {
 			throwCoreException("No xmof file location selected");
 		}
 
 		monitor.beginTask("Creating " + xmofFile.getName(), 2);
 
 		ConfigurationGenerator generator = new ConfigurationGenerator(
 				getEPackages(metamodelResource), mainClasses);
 		Collection<EPackage> xmofPackages = generator
 				.generateConfigurationPackages();
 
 		Resource resource = resourceSet.createResource(URI.createURI(xmofFile
 				.getFullPath().toPortableString()));
 		resource.getContents().addAll(xmofPackages);
 		try {
 			resource.save(null);
 		} catch (IOException e1) {
 			throwCoreException(e1.getMessage());
 		}
 
 		monitor.worked(1);
 		monitor.setTaskName("Opening xMOF file for editing...");
 		getShell().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				IWorkbenchPage page = PlatformUI.getWorkbench()
 						.getActiveWorkbenchWindow().getActivePage();
 				try {
 					IDE.openEditor(page, xmofFile, true);
 				} catch (PartInitException e) {
 				}
 			}
 		});
 		monitor.worked(1);
 	}
 
 	private Collection<EPackage> getEPackages(Resource metaModelResource) {
 		Collection<EPackage> packages = new ArrayList<EPackage>();
 		for (EObject eObject : metaModelResource.getContents()) {
 			if (eObject instanceof EPackage) {
 				packages.add((EPackage) eObject);
 			}
 		}
 		return packages;
 	}
 
 	private void throwCoreException(String message) throws CoreException {
 		IStatus status = new Status(IStatus.ERROR,
				"org.modelexecution.xmof.confgenerator", IStatus.OK, message,
 				null);
 		throw new CoreException(status);
 	}
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.selection = selection;
 	}
 }
