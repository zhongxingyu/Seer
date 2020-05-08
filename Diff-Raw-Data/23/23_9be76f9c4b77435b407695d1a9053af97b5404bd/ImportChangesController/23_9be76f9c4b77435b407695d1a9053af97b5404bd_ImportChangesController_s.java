 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.importexport.impl;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.impl.ProjectSpaceBase;
 import org.eclipse.emf.emfstore.internal.client.model.importexport.ExportImportDataUnits;
 import org.eclipse.emf.emfstore.internal.client.model.importexport.IExportImportController;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage;
 
 /**
  * A controller for importing changes which then will be applied upon
  * a given {@link ProjectSpace}.
  * 
  * @author emueller
  * 
  */
 public class ImportChangesController implements IExportImportController {
 
 	private final ProjectSpaceBase projectSpace;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param projectSpace
 	 *            the {@link ProjectSpace} upon which to apply the changes being imported
 	 */
 	public ImportChangesController(ProjectSpaceBase projectSpace) {
 		this.projectSpace = projectSpace;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.controller.importexport.IExportImportController#getLabel()
 	 */
 	public String getLabel() {
 		return "changes";
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.controller.importexport.IExportImportController#getFilteredNames()
 	 */
 	public String[] getFilteredNames() {
 		return new String[] { "EMFStore change package (" + ExportImportDataUnits.Change.getExtension() + ")",
 			"All Files (*.*)" };
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.controller.importexport.IExportImportController#getFilteredExtensions()
 	 */
 	public String[] getFilteredExtensions() {
 		return new String[] { "*" + ExportImportDataUnits.Change.getExtension(), "*.*" };
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.controller.importexport.IExportImportController#getParentFolderPropertyKey()
 	 */
 	public String getParentFolderPropertyKey() {
 		return null;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.controller.importexport.IExportImportController#execute(java.io.File,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void execute(File file, IProgressMonitor progressMonitor) throws IOException {
 
 		ResourceSetImpl resourceSet = new ResourceSetImpl();
 		Resource resource = resourceSet.getResource(URI.createFileURI(file.getAbsolutePath()), true);
 		EList<EObject> directContents = resource.getContents();
 
 		// sanity check
 		if (directContents.size() != 1 && (!(directContents.get(0) instanceof ChangePackage))) {
 			throw new IOException("File is corrupt, does not contain changes.");
 		}
 
 		ChangePackage changePackage = (ChangePackage) directContents.get(0);
 
 		projectSpace.applyOperations(changePackage.getOperations(), true);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.controller.importexport.IExportImportController#getFilename()
 	 */
 	public String getFilename() {
 		return null;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.controller.importexport.IExportImportController#isExport()
 	 */
 	public boolean isExport() {
 		return false;
 	}
 
 }
