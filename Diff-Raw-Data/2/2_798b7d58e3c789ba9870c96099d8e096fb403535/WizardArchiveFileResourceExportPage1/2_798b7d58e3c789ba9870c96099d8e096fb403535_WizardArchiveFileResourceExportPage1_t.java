 /*******************************************************************************
  * Copyright (c) 2000, 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Red Hat, Inc - added bzip2 format support
  *******************************************************************************/
 package org.eclipse.ui.internal.wizards.datatransfer;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.ui.PlatformUI;
 import  org.eclipse.ui.internal.wizards.datatransfer.TarFileExporter;
 /**
  *	Page 1 of the base resource export-to-archive Wizard.
  *
  *	@since 3.1
  */
 public class WizardArchiveFileResourceExportPage1 extends
 		WizardFileSystemResourceExportPage1 {
 
     // widgets
     protected Button compressContentsCheckbox;
     
     protected Button zipFormatButton;
     protected Button tarFormatButton;
     
 
     protected Button uncompressTarButton;
     protected Button gzipCompressButton;
     protected Button bzip2CompressButton;
 
     // dialog store id constants
     private final static String STORE_DESTINATION_NAMES_ID = "WizardZipFileResourceExportPage1.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$
 
     private final static String STORE_CREATE_STRUCTURE_ID = "WizardZipFileResourceExportPage1.STORE_CREATE_STRUCTURE_ID"; //$NON-NLS-1$
 
     private final static String STORE_COMPRESS_CONTENTS_ID = "WizardZipFileResourceExportPage1.STORE_COMPRESS_CONTENTS_ID"; //$NON-NLS-1$
 
     private final static String STORE_ZIP_FORMAT_ID = "WizardZipFileResourceExportPage1.STORE_ZIP_FORMAT_ID";	//$NON-NLS-1$
     
     private final static String STORE_UNCOMPRESS_TAR_FORMAT_ID = "WizardZipFileResourceExportPage1.STORE_UNCOMPRESS_TAR_FORMAT_ID";	//$NON-NLS-1$
     
     private final static String STORE_GZIP_FORMAT_ID = "WizardZipFileResourceExportPage1.STORE_GZIP_FORMAT_ID"; //$NON-NLS-1$
     
     private final static String STORE_BZIP2_FORMAT_ID = "WizardZipFileResourceExportPage1.STORE_BZIP2_FORMAT_ID"; //$NON-NLS-1$
     /**
      *	Create an instance of this class. 
      *
      *	@param name java.lang.String
      */
     protected WizardArchiveFileResourceExportPage1(String name,
             IStructuredSelection selection) {
         super(name, selection);
     }
 
     /**
      * Create an instance of this class
      * @param selection the selection
      */
     public WizardArchiveFileResourceExportPage1(IStructuredSelection selection) {
         this("zipFileExportPage1", selection); //$NON-NLS-1$
         setTitle(DataTransferMessages.ArchiveExport_exportTitle);
         setDescription(DataTransferMessages.ArchiveExport_description);
     }
 
     /** (non-Javadoc)
      * Method declared on IDialogPage.
      */
     public void createControl(Composite parent) {
         super.createControl(parent);
         PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
                 IDataTransferHelpContextIds.ZIP_FILE_EXPORT_WIZARD_PAGE);
     }
 
     /**
      *	Create the export options specification widgets.
      *
      */
     protected void createOptionsGroupButtons(Group optionsGroup) {
     	Font font = optionsGroup.getFont();
     	optionsGroup.setLayout(new GridLayout(2, true));
     	
     	Composite left = new Composite(optionsGroup, SWT.NONE);
     	left.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
     	left.setLayout(new GridLayout(1, true));
 
         createFileFormatOptions(left, font);
         
         /*// compress... checkbox
         compressContentsCheckbox = new Button(left, SWT.CHECK
                 | SWT.LEFT);
         compressContentsCheckbox.setText(DataTransferMessages.ZipExport_compressContents);
         compressContentsCheckbox.setFont(font);*/
 
         Composite right = new Composite(optionsGroup, SWT.NONE);
         right.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
         right.setLayout(new GridLayout(1, true));
 
         createDirectoryStructureOptions(right, font);
 
         // initial setup
         createDirectoryStructureButton.setSelection(true);
         createSelectionOnlyButton.setSelection(false);
         compressContentsCheckbox.setSelection(true);
 
     }
 
     /**
      * Create the buttons for the group that determine if the entire or
      * selected directory structure should be created.
      * @param optionsGroup
      * @param font
      */
     protected void createFileFormatOptions(Composite optionsGroup, Font font) {
         // create directory structure radios
         zipFormatButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
         zipFormatButton.setText(DataTransferMessages.ArchiveExport_saveInZipFormat);
         zipFormatButton.setSelection(true);
         zipFormatButton.setFont(font);
 		zipFormatButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (((Button)e.widget).getSelection()) {
 					// activate Zip options
 					activateZipOptions();
 					setZipOption();
 					// try setting the correct file extension
 					setDestinationValue(getDestinationValue(true));
 				}
 			}
 		});
         //create an indent
         Button tmp= new Button(optionsGroup, SWT.CHECK);
         int indent = tmp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
         tmp.dispose();
         // compress... checkbox
         compressContentsCheckbox = new Button(optionsGroup, SWT.CHECK
                 | SWT.LEFT);
         compressContentsCheckbox.setText(DataTransferMessages.ZipExport_compressContents);
         compressContentsCheckbox.setFont(font);      
         GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gridData.horizontalSpan = 2;
 		gridData.horizontalIndent = indent;
         compressContentsCheckbox.setLayoutData(gridData);
 
         // create directory structure radios
         tarFormatButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
         tarFormatButton.setText(DataTransferMessages.ArchiveExport_saveInTarFormat);
         tarFormatButton.setSelection(false);
         tarFormatButton.setFont(font);
 		tarFormatButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if(((Button)e.widget).getSelection()) {
 					// activate tar options
 					activateTarOptions();
 					setUncompressedTarOption();
 					// try setting the correct file extension
 					setDestinationValue(getDestinationValue(true));
 				}
 			}
 		});
         
         
         //uncompressed tar format
         uncompressTarButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
         uncompressTarButton.setText(DataTransferMessages.WizardArchiveFileResourceExportPage_UncompressedTarFormat);
         uncompressTarButton.setSelection(false);
         uncompressTarButton.setFont(font);
         uncompressTarButton.setLayoutData(gridData);
         uncompressTarButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent e) {
         		if(((Button)e.widget).getSelection()) {
         			//set uncompressed option
         			setUncompressedTarOption();
         			// try setting the correct file extension
         			setDestinationValue(getDestinationValue(true));
         		}
         	}
         });
         //gzip format tar ball
         gzipCompressButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
         gzipCompressButton.setText(DataTransferMessages.WizardArchiveFileResourceExportPage_GzipCompressedTarFormat);
         gzipCompressButton.setSelection(false);
         gzipCompressButton.setFont(font);
         gzipCompressButton.setLayoutData(gridData);
         gzipCompressButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent e) {
         		if(((Button)e.widget).getSelection()) {
         			//set gzip compress option
         			setGzipTarOption();
         			// try setting the correct file extension
         			setDestinationValue(getDestinationValue(true));
         		}
         	}
         });
         
         //bzip2 format tar ball
         bzip2CompressButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
         bzip2CompressButton.setText(DataTransferMessages.WizardArchiveFileResourceExportPage_Bzip2CompressedTarFormat);
         bzip2CompressButton.setSelection(false);
         bzip2CompressButton.setFont(font);
         bzip2CompressButton.setLayoutData(gridData);
         bzip2CompressButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent e) {
         		if(((Button)e.getSource()).getSelection()) {
         			//set bzip2 compress option
         			setBzipTarOption();
         			// try setting the correct file extension
         			setDestinationValue(getDestinationValue(true));
         		}
         	}
         });
         
         //additional format can be added in a similar fashion as the previous code blocks
     }    
     
     /**
      * Returns a boolean indicating whether the directory portion of the
      * passed pathname is valid and available for use.
      */
     protected boolean ensureTargetDirectoryIsValid(String fullPathname) {
         int separatorIndex = fullPathname.lastIndexOf(File.separator);
 
         if (separatorIndex == -1) {
 			return true;
 		}
 
         return ensureTargetIsValid(new File(fullPathname.substring(0,
                 separatorIndex)));
     }
 
     /**
      * Returns a boolean indicating whether the passed File handle is
      * is valid and available for use.
      */
     protected boolean ensureTargetFileIsValid(File targetFile) {
         if (targetFile.exists() && targetFile.isDirectory()) {
             displayErrorDialog(DataTransferMessages.ZipExport_mustBeFile);
             giveFocusToDestination();
             return false;
         }
 
         if (targetFile.exists()) {
             if (targetFile.canWrite()) {
                 if (!queryYesNoQuestion(DataTransferMessages.ZipExport_alreadyExists)) {
 					return false;
 				}
             } else {
                 displayErrorDialog(DataTransferMessages.ZipExport_alreadyExistsError);
                 giveFocusToDestination();
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Ensures that the target output file and its containing directory are
      * both valid and able to be used.  Answer a boolean indicating validity.
      */
     protected boolean ensureTargetIsValid() {
         String targetPath = getDestinationValue(false);
 
         if (!ensureTargetDirectoryIsValid(targetPath)) {
 			return false;
 		}
 
         if (!ensureTargetFileIsValid(new File(targetPath))) {
 			return false;
 		}
 
         return true;
     }
 
     /**
      *  Export the passed resource and recursively export all of its child resources
      *  (iff it's a container).  Answer a boolean indicating success.
      */
     protected boolean executeExportOperation(ArchiveFileExportOperation op) {
         op.setCreateLeadupStructure(createDirectoryStructureButton
                 .getSelection());
         op.setUseCompression(compressContentsCheckbox.getSelection());
         op.setUseTarFormat(tarFormatButton.getSelection());
 
         try {
             getContainer().run(true, true, op);
         } catch (InterruptedException e) {
             return false;
         } catch (InvocationTargetException e) {
             displayErrorDialog(e.getTargetException());
             return false;
         }
 
         IStatus status = op.getStatus();
         if (!status.isOK()) {
             ErrorDialog.openError(getContainer().getShell(),
                     DataTransferMessages.DataTransfer_exportProblems,
                     null, // no special message
                     status);
             return false;
         }
 
         return true;
     }
 
     /**
      * The Finish button was pressed.  Try to do the required work now and answer
      * a boolean indicating success.  If false is returned then the wizard will
      * not close.
      * @returns boolean
      */
     public boolean finish() {
     	List resourcesToExport = getWhiteCheckedResources();
     	
         if (!ensureTargetIsValid()) {
 			return false;
 		}
 
         //Save dirty editors if possible but do not stop if not all are saved
         saveDirtyEditors();
         // about to invoke the operation so save our state
         saveWidgetValues();
 
 		// determine save format, if zip, use old ArchiveFileExportOperation
 		// constructor
 		// otherwise, tar format and call new ArchiveFileExportOperation
 		// constructor with format code from TarFileExporter
         int tarCode = getTarExportCode();
         ArchiveFileExportOperation operation = null;
         if(tarCode != -1) { //tar is selected
         	try {
         	operation = new ArchiveFileExportOperation(null,
                     resourcesToExport, getDestinationValue(true), tarCode);
         	}catch(IllegalArgumentException iae) {
         		//this should never happen
         	}
         }else { //zip is selected
         	operation = new ArchiveFileExportOperation(null,
                     resourcesToExport, getDestinationValue(true));
         }
         return executeExportOperation(operation);
     }
 
     /**
      *	Answer the string to display in the receiver as the destination type
      */
     protected String getDestinationLabel() {
         return DataTransferMessages.ArchiveExport_destinationLabel;
     }
 
     /**
      *	Answer the contents of self's destination specification widget.  If this
      *	value does not have a suffix then add it first.
      *	@param radioActivated
      */
     protected String getDestinationValue(boolean radioActivated) {
     	String idealSuffix = getOutputSuffix();
         String destinationText = super.getDestinationValue();
 
         // only append a suffix if the destination doesn't already have a . in 
         // its last path segment.  
         // Also prevent the user from selecting a directory.  Allowing this will 
         // create a ".zip" file in the directory
         if (destinationText.length() != 0
                 && !destinationText.endsWith(File.separator)) {
             int dotIndex = destinationText.lastIndexOf('.');
             if (dotIndex != -1) {
                 //since the method is not called by radio button handler, we must determine the extension
             	if(!radioActivated)
             	{
             		int extIndex = getCompressionExtensionIndex(destinationText);
             		if(extIndex != -1)
             			idealSuffix = destinationText.substring(extIndex);
             	}
             	// the last path seperator index
                 int pathSepIndex = destinationText.lastIndexOf(File.separator);
                if (/*pathSepIndex != -1 && */dotIndex > pathSepIndex) {
                     //detect if its one of the supported file extensions, if it is, replace the file extension
                 	//otherwise, append
 					int extIndex = getCompressionExtensionIndex(destinationText);
 					if(extIndex != -1)
 						destinationText = destinationText.substring(0, extIndex) + idealSuffix;
 					else
 						destinationText += idealSuffix;
                 } 
             } else {
                 destinationText += idealSuffix;
             }
         }
 
         return destinationText;
     }
     
     /**
      *  Returns the index of the beginning of the file extension, returns -1 if its not found.
      *  @param in
      *  @return int
      */
     protected int getCompressionExtensionIndex(String in)
     {
     	int index = -1;
     	if ((in.endsWith(".tar"))	 			//$NON-NLS-1$
 				|| (in.endsWith(".zip"))		//$NON-NLS-1$
 				|| (in.endsWith(".tgz"))		//$NON-NLS-1$
 				|| (in.endsWith(".tbz"))		//$NON-NLS-1$
 				|| (in.endsWith(".tbz2"))) {	//$NON-NLS-1$
     		index = in.lastIndexOf('.');
     	} else if(in.endsWith(".tar.gz")) {	//$NON-NLS-1$
 			index = in.lastIndexOf(".tar.gz"); //$NON-NLS-1$
     	} else if(in.endsWith(".tar.bz2")) {	//$NON-NLS-1$
 			index = in.lastIndexOf(".tar.bz2"); //$NON-NLS-1$
 		}
     	//additional format can be added by adding code block in similar fashion as the above if statement blocks
     	return index;
     }
 
     /**
      *	Answer the suffix that files exported from this wizard should have.
      *	If this suffix is a file extension (which is typically the case)
      *	then it must include the leading period character.
      *
      */
 	protected String getOutputSuffix() {
 		if (zipFormatButton.getSelection()) {
 			return ".zip"; //$NON-NLS-1$
 		} else if (gzipCompressButton.getSelection()) {
 			return ".tar.gz"; //$NON-NLS-1$
 		} else if (bzip2CompressButton.getSelection()) {
 			return ".tar.bz2"; //$NON-NLS-1$
 		} else {
 			return ".tar"; //$NON-NLS-1$
 		}
 		//additional format can be added by adding code block in similar fashion as the above if statement blocks
 	}
 
     /**
      *	Open an appropriate destination browser so that the user can specify a source
      *	to import from
      */
     protected void handleDestinationBrowseButtonPressed() {
         FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
         //additional format can be added to the following file filter string array
         dialog.setFilterExtensions(new String[] { "*.zip;*.tar.gz;*.tar;*.tar.bz2", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
         dialog.setText(DataTransferMessages.ArchiveExport_selectDestinationTitle);
         String currentSourceString = getDestinationValue(false);
         int lastSeparatorIndex = currentSourceString
                 .lastIndexOf(File.separator);
         if (lastSeparatorIndex != -1) {
 			dialog.setFilterPath(currentSourceString.substring(0,
                     lastSeparatorIndex));
 		}
         String selectedFileName = dialog.open();
 
         if (selectedFileName != null) {
             setErrorMessage(null);
             setDestinationValue(selectedFileName);
         }
     }
 
     /**
      *	Hook method for saving widget values for restoration by the next instance
      *	of this class.
      */
     protected void internalSaveWidgetValues() {
         // update directory names history
         IDialogSettings settings = getDialogSettings();
         if (settings != null) {
             String[] directoryNames = settings
                     .getArray(STORE_DESTINATION_NAMES_ID);
             if (directoryNames == null) {
 				directoryNames = new String[0];
 			}
 
 			directoryNames = addToHistory(directoryNames,
 					getDestinationValue(false));
 			settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
 
 			settings.put(STORE_CREATE_STRUCTURE_ID,
 					createDirectoryStructureButton.getSelection());
 
 			settings.put(STORE_COMPRESS_CONTENTS_ID,
 					compressContentsCheckbox.getSelection());
 
 			settings.put(STORE_ZIP_FORMAT_ID, zipFormatButton.getSelection());
 
 			settings.put(STORE_UNCOMPRESS_TAR_FORMAT_ID,
 					uncompressTarButton.getSelection());
 
 			settings.put(STORE_GZIP_FORMAT_ID,
 					gzipCompressButton.getSelection());
 
 			settings.put(STORE_BZIP2_FORMAT_ID,
 					bzip2CompressButton.getSelection());
             //additional format can be added here
         }
     }
 
     /**
      *	Hook method for restoring widget values to the values that they held
      *	last time this wizard was used to completion.
      */
     protected void restoreWidgetValues() {
         IDialogSettings settings = getDialogSettings();
         if (settings != null) {
             String[] directoryNames = settings
                     .getArray(STORE_DESTINATION_NAMES_ID);
             if (directoryNames == null || directoryNames.length == 0) {
 				return; // ie.- no settings stored
 			}
 
             // destination
             setDestinationValue(directoryNames[0]);
             for (int i = 0; i < directoryNames.length; i++) {
 				addDestinationItem(directoryNames[i]);
 			}
 
             boolean setStructure = settings
                     .getBoolean(STORE_CREATE_STRUCTURE_ID);
 
             createDirectoryStructureButton.setSelection(setStructure);
             createSelectionOnlyButton.setSelection(!setStructure);
 
             
             
             boolean zipFormat = settings.getBoolean(STORE_ZIP_FORMAT_ID);
             //additional format can be added to the following if statement block
 			if (zipFormat) {
 				activateZipOptions();
 				setZipOption();
 				compressContentsCheckbox.setSelection(settings
 						.getBoolean(STORE_COMPRESS_CONTENTS_ID));
 			} else {
 				activateTarOptions();
 				uncompressTarButton.setSelection(settings
 						.getBoolean(STORE_UNCOMPRESS_TAR_FORMAT_ID));
 				gzipCompressButton.setSelection(settings
 						.getBoolean(STORE_GZIP_FORMAT_ID));
 				bzip2CompressButton.setSelection(settings
 						.getBoolean(STORE_BZIP2_FORMAT_ID));
 			}
         }
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
      */
     protected String destinationEmptyMessage() {
         return DataTransferMessages.ArchiveExport_destinationEmpty;
     }
     
     /**
      *	Answer a boolean indicating whether the receivers destination specification
      *	widgets currently all contain valid values.
      */
     protected boolean validateDestinationGroup() {
     	String destinationValue = getDestinationValue(false);
     	if (destinationValue.endsWith(".tar")) { //$NON-NLS-1$
     		//compressContentsCheckbox.setSelection(false);
     		activateTarOptions();
     		setUncompressedTarOption();
     	} else if (destinationValue.endsWith(".tar.gz") //$NON-NLS-1$
 				|| destinationValue.endsWith(".tgz")) { //$NON-NLS-1$
     		//compressContentsCheckbox.setSelection(true);
     		activateTarOptions();
     		setGzipTarOption();
     	} else if (destinationValue.endsWith(".zip")) { //$NON-NLS-1$
     		activateZipOptions();
     		setZipOption();
     	}else if((destinationValue.endsWith(".tar.bz2"))	//$NON-NLS-1$
     			|| (destinationValue.endsWith(".tbz"))		//$NON-NLS-1$
     			|| (destinationValue.endsWith(".tbz2"))){	//$NON-NLS-1$
     		activateTarOptions();
     		setBzipTarOption();
     	}
     	//additional format can be added by adding code block in similar fashion as the above if statement blocks
     	return super.validateDestinationGroup();
     }
     
 	/**
 	 * Configure the compression format radio buttons when uncompressed tar
 	 * format is selected.
 	 */
 	protected void setUncompressedTarOption() {
 		//radio button event handling, additional format can be added below
 		tarFormatButton.setSelection(true);
 		uncompressTarButton.setSelection(true);
 		gzipCompressButton.setSelection(false);
 		bzip2CompressButton.setSelection(false);
 		zipFormatButton.setSelection(false);
 	}
 
 	/**
 	 * Configure the compression format radio buttons when Gzip compressed tar
 	 * format is selected.
 	 */
 	protected void setGzipTarOption() {
 		//radio button event handling, additional format can be added below
 		tarFormatButton.setSelection(true);
 		uncompressTarButton.setSelection(false);
 		gzipCompressButton.setSelection(true);
 		bzip2CompressButton.setSelection(false);
 		zipFormatButton.setSelection(false);
 	}
 
 	/**
 	 * Configure the compression format radio buttons when Zip format is
 	 * selected.
 	 */
 	protected void setZipOption() {
 		//radio button event handling, additional format can be added below
 		tarFormatButton.setSelection(false);
 		uncompressTarButton.setSelection(false);
 		gzipCompressButton.setSelection(false);
 		bzip2CompressButton.setSelection(false);
 		zipFormatButton.setSelection(true);
 	}
 
 	/**
 	 * Configure the compression format radio buttons when Bzip2 compressed tar
 	 * format is selected.
 	 */
 	protected void setBzipTarOption() {
 		//radio button event handling, additional format can be added below
 		tarFormatButton.setSelection(true);
 		uncompressTarButton.setSelection(false);
 		gzipCompressButton.setSelection(false);
 		bzip2CompressButton.setSelection(true);
 		zipFormatButton.setSelection(false);
 	}
 	
 	/**
 	 * Activate the Tar compression format options.
 	 */
 	protected void activateTarOptions() {
 		// radio button event handling, additional format can be added below
 		zipFormatButton.setSelection(false);
 		compressContentsCheckbox.setEnabled(false);
 		tarFormatButton.setSelection(true);
 		uncompressTarButton.setEnabled(true);
 		gzipCompressButton.setEnabled(true);
 		bzip2CompressButton.setEnabled(true);
 		uncompressTarButton.setSelection(true);
 	}
 
 	/**
 	 * Activate the Zip compression format options.
 	 */
 	protected void activateZipOptions() {
 		// radio button event handling, additional format can be added below
 		zipFormatButton.setSelection(true);
 		compressContentsCheckbox.setEnabled(true);
 		tarFormatButton.setSelection(false);
 		uncompressTarButton.setEnabled(false);
 		gzipCompressButton.setEnabled(false);
 		bzip2CompressButton.setEnabled(false);
 	}
 	
 	/**
 	 * Returns the tar mode code to the caller, based on the radio buttons'
 	 * status.
 	 */
 	protected int getTarExportCode() {
 		int code = -1;
 		if (tarFormatButton.getSelection()) {
 			if (uncompressTarButton.getSelection())
 				code = TarFileExporter.UNCOMPRESSED;
 			else if (gzipCompressButton.getSelection())
 				code = TarFileExporter.GZIP;
 			else if (bzip2CompressButton.getSelection())
 				code = TarFileExporter.BZIP2;
 		}
 		//additional format can be added by adding code block in similar fashion as the above if statement blocks
 		return code;
 	}
 }
