 /*******************************************************************************
  * Copyright (c) 2010, 2011 Technical University of Denmark.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors:
  *    Patrick Koenemann, DTU Informatics - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.mpatch.emfdiff2mpatch.wizards;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.mpatch.MPatchModel;
 import org.eclipse.emf.compare.mpatch.common.util.MPatchConstants;
 import org.eclipse.emf.compare.mpatch.emfdiff2mpatch.Emfdiff2mpatchActivator;
 import org.eclipse.emf.compare.mpatch.emfdiff2mpatch.util.AbstractEmfdiffExportWizard;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IWorkbench;
 
 /**
  * The default wizard for transforming an emfdiff into an {@link MPatchModel}.
  * 
  * @author Patrick Koenemann (pk@imm.dtu.dk)
  * 
  */
 public class EmfdiffExportWizard extends AbstractEmfdiffExportWizard {
 
 	/** Icon for export wizard. */
 	private static final String MPATCH_ICON = "icons/mpatch.gif";
 
 	/** References the page displayed by this wizard. */
 	private EmfdiffExportWizardPage page;
 	
 	/** Proposed IFile for storing MPatch file. */
 	private IFile file;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#initializeDefaultPageImageDescriptor()
 	 */
 	@Override
 	protected void initializeDefaultPageImageDescriptor() {
 		final URL imageURL = Emfdiff2mpatchActivator.getDefault().getBundle().getEntry(MPATCH_ICON);
 		setDefaultPageImageDescriptor(ImageDescriptor.createFromURL(imageURL));
 	}
 
 	/**
 	 * Call super.performFinish and then try to save result in the selected file.
 	 */
 	@Override
 	public boolean performFinish() {
 		if (!super.performFinish())
 			return false;
 
 		// save transformation results
 		final Resource output = new XMIResourceImpl(URI.createURI(page.createNewFile().getFullPath().toString()));
 		output.getContents().add(getMPatch());
 		try {
 			output.save(null);
 		} catch (final IOException e) {
 			Emfdiff2mpatchActivator.getDefault()
 					.logError("Could not save " + MPatchConstants.MPATCH_SHORT_NAME + " file!", e);
 			MessageDialog.openError(getShell(), "Failed to save file",
 					"The File could not be saved! Please see the error log for details.\nError message: "
 							+ e.getMessage());
 		}
 
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.wizard.Wizard#addPages()
 	 */
 	@Override
 	public void addPages() {
 		final String fileName = file == null ? "result." + MPatchConstants.FILE_EXTENSION_MPATCH : file.getName();
 		
		page = new EmfdiffExportWizardPage(MPatchConstants.MPATCH_LONG_NAME + " export", new StructuredSelection(file));
 		page.setTitle(MPatchConstants.MPATCH_LONG_NAME);
 		page.setDescription("Transforms emfdiff into " + MPatchConstants.MPATCH_LONG_NAME + ".");
 		page.setFileName(fileName);
 		addPage(page);
 
 		super.addPages();
 	}
 	
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		super.init(workbench, selection);
 		
 		// filter mpatch file :-D
 		for (Object obj : selection.toArray()) {
 			if (obj instanceof IFile) {
 				final IFile file = (IFile) obj;
 				if (MPatchConstants.FILE_EXTENSION_MPATCH.equals(((IFile)obj).getFileExtension())) {
 					this.file = file;
 				}
 			}
 		}
 	}
 }
