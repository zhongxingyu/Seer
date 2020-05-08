 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.controls.file;
 
 import java.util.Arrays;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.tcf.te.ui.controls.BaseDialogSelectionControl;
 import org.eclipse.tcf.te.ui.controls.nls.Messages;
 import org.eclipse.tcf.te.ui.controls.validator.FileNameValidator;
 import org.eclipse.tcf.te.ui.controls.validator.Validator;
 import org.osgi.framework.Bundle;
 
 
 /**
  * Base implementation of a simple file selection control.
  * <p>
  * The control supports direct editing by the user or browsing for the file. By
  * default, the control has a history of recently selected files.
  */
 public class FileSelectionControl extends BaseDialogSelectionControl {
 	private String[] filterExtensions;
 	private String[] filterNames;
 
 	private String last_filter_path = null;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param parentPage The parent dialog page this control is embedded in.
 	 *                   Might be <code>null</code> if the control is not associated with a page.
 	 */
 	public FileSelectionControl(IDialogPage parentPage) {
 		super(parentPage);
 		setDialogTitle(Messages.FileSelectionControl_title_open);
 		setGroupLabel(Messages.FileSelectionControl_group_label);
 		setEditFieldLabel(Messages.FileSelectionControl_editfield_label);
 	}
 
 	/**
 	 * Set the filter extensions string array used by the standard file dialog. If set
 	 * to <code>null</code>, not filter extensions will be given to the file dialog.
 	 *
 	 * @param filterExtensions The filter extensions string array to use or <code>null</code>.
 	 */
 	public void setFilterExtensions(String[] filterExtensions) {
 		this.filterExtensions = filterExtensions != null ? Arrays.copyOf(filterExtensions, filterExtensions.length) : null;
 		if (getEditFieldValidator() != null && filterExtensions != null) {
 			((FileNameValidator)getEditFieldValidator()).setFileExtensions(filterExtensions);
 		}
 	}
 
 	/**
 	 * Returns the filter extensions string array used by the standard file dialog.
 	 *
 	 * @return The filter extensions string array.
 	 */
 	public String[] getFilterExtensions() {
 		return filterExtensions != null ? Arrays.copyOf(filterExtensions, filterExtensions.length) : new String[0];
 	}
 
 
 	/**
 	 * Set the filter names string array used by the standard file dialog. If set
 	 * to <code>null</code>, not filter names will be given to the file dialog.
 	 *
 	 * @param filterExtensions The filter names string array to use or <code>null</code>.
 	 */
 	public void setFilterNames(String[] filterNames) {
 		this.filterNames = filterNames != null ? Arrays.copyOf(filterNames, filterNames.length) : null;
 	}
 
 	/**
 	 * Returns the filter names string array used by the standard file dialog.
 	 *
 	 * @return The filter names string array or <code>null</code>.
 	 */
 	public String[] getFilterNames() {
 		return filterNames != null ? Arrays.copyOf(filterNames, filterNames.length) : new String[0];
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseDialogSelectionControl#doCreateDialogControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected Dialog doCreateDialogControl(Composite parent) {
 		Assert.isNotNull(parent);
 
 		// create a standard file dialog
 		FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN);
 		return dialog;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseDialogSelectionControl#configureDialogControl(org.eclipse.swt.widgets.Dialog)
 	 */
 	@Override
 	protected void configureDialogControl(Dialog dialog) {
 		super.configureDialogControl(dialog);
 
 		// we do expect a FileDialog here
 		if (dialog instanceof FileDialog) {
 			FileDialog fileDialog = (FileDialog)dialog;
 
 			// set the file dialog filter extensions if available.
 			if (getFilterExtensions().length > 0) {
 				fileDialog.setFilterExtensions(getFilterExtensions());
 			}
 
 			// set the file dialog filter names if available.
 			if (getFilterNames().length > 0) {
 				fileDialog.setFilterNames(getFilterNames());
 			}
 
 			// the dialog should open within the directory of the currently selected
 			// file. If no file has been currently selected, it should open within the
 			// last browsed directory.
 			String selectedFile = doGetSelectedFile();
 			if (selectedFile != null && selectedFile.trim().length() > 0) {
 				IPath filePath = new Path(selectedFile);
 				// If the selected file points to an directory, use the directory as is
 				IPath filterPath = filePath.toFile().isDirectory() ? filePath : filePath.removeLastSegments(1);
				while (filterPath != null && filterPath.segmentCount() > 1 && !filterPath.toFile().exists()) {
					filterPath = filterPath.removeLastSegments(1);
				}
 				String filterFileName = filePath.toFile().isDirectory() || !filePath.toFile().exists() ? null : filePath.lastSegment();
 
 				if (!filterPath.isEmpty()) fileDialog.setFilterPath(filterPath.toString());
 				if (filterFileName != null) fileDialog.setFileName(filterFileName);
 			} else {
 				String filterPath = last_filter_path != null ? last_filter_path : doGetDefaultFilterPath();
 				fileDialog.setFilterPath(filterPath);
 			}
 		}
 	}
 
 	/**
 	 * Returns the file from which to set the initial directory. This method
 	 * is called from {@link #configureDialogControl(Dialog)} in case the dialog
 	 * is a {@link FileDialog}.
 	 * <p>
 	 * <b>Note:</b> The method may return a directory to use as initial directory
 	 *              in case the selected file cannot be determined.
 	 *
 	 * @return The file to set the initial directory to the file dialog or <code>null</code> if none.
 	 */
 	protected String doGetSelectedFile() {
 		return getEditFieldControlText();
 	}
 
 	/**
 	 * Returns the default filter path to use if no file has been selected so far <b>and</b>
 	 * the dialog opens for the first time.
 	 * <p>
 	 * <b>Note:</b> The default implementation returns the current workspace location if the
 	 * resources plug-in is installed.
 	 *
 	 * @return The default filter path to use or <code>null</code>.
 	 */
 	protected String doGetDefaultFilterPath() {
 		if (Platform.getBundle("org.eclipse.core.resources") != null //$NON-NLS-1$
 						&& Platform.getBundle("org.eclipse.core.resources").getState() == Bundle.ACTIVE) { //$NON-NLS-1$
 			return org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#doCreateEditFieldValidator()
 	 */
 	@Override
 	protected Validator doCreateEditFieldValidator() {
 		return new FileNameValidator(Validator.ATTR_MANDATORY |
 		                               FileNameValidator.ATTR_MUST_EXIST |
 		                               FileNameValidator.ATTR_CAN_READ |
 		                               FileNameValidator.ATTR_CAN_WRITE);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#configureEditFieldValidator(org.eclipse.tcf.te.ui.controls.validator.Validator)
 	 */
 	@Override
 	protected void configureEditFieldValidator(Validator validator) {
 		if (validator instanceof FileNameValidator) {
 			if (getFilterExtensions() != null) {
 				((FileNameValidator)validator).setFileExtensions(getFilterExtensions());
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseDialogSelectionControl#doOpenDialogControl(org.eclipse.swt.widgets.Dialog)
 	 */
 	@Override
 	protected String doOpenDialogControl(Dialog dialog) {
 		Assert.isNotNull(dialog);
 
 		// We do expect a file dialog here.
 		if (dialog instanceof FileDialog) {
 			FileDialog fileDialog = (FileDialog)dialog;
 			String selected = fileDialog.open();
 			if (selected != null) last_filter_path = fileDialog.getFilterPath();
 			return selected;
 		}
 
 		return null;
 	}
 }
