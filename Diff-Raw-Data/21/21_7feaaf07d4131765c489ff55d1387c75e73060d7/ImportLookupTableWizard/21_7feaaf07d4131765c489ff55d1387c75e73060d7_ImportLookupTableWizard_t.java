 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.lookuptable.editor.importwizard;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collections;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.dialogs.IOverwriteQuery;
 import org.eclipse.ui.ide.undo.MoveResourcesOperation;
 import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
 import org.eclipse.ui.wizards.datatransfer.ImportOperation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ImportLookupTableWizard extends Wizard implements IImportWizard {
 
 	private static final Logger logger = LoggerFactory.getLogger(ImportLookupTableWizard.class);
 	private IStructuredSelection selection;
 	private LookupTableImportPage lookupTableImportPage;
 
 	public ImportLookupTableWizard() {
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.selection = selection;
 		setWindowTitle("Import");
 	}
 
 	@Override
 	public boolean performFinish() {
 		IResource fileToBeReplaced = lookupTableImportPage.getFileToBeReplaced();
 		IPath fileToBeReplacedPath = fileToBeReplaced.getFullPath();
 		IPath newPath = new Path(fileToBeReplacedPath.toString() + "_backup");
		String operation_name = "";
		operation_name = "create backup";
 		MoveResourcesOperation moveResourcesOperation = new MoveResourcesOperation(fileToBeReplaced, newPath,
				operation_name);
 		try {
 			moveResourcesOperation.execute(new NullProgressMonitor(), null);
 
 			String replacingFileFullLocation = lookupTableImportPage.getReplacingFileFullLocation();
 			IContainer parent = lookupTableImportPage.getFileToBeReplaced().getParent();
 			ImportOperation importFileOperation = new ImportOperation(parent.getFullPath(),
 					FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {
 
 						@Override
 						public String queryOverwrite(String pathString) {
 							return ALL;
 						}
 					}, Collections.singletonList(new File(replacingFileFullLocation)));
 			importFileOperation.setCreateContainerStructure(false);
 			importFileOperation.run(new NullProgressMonitor());
 			String lastSegment = new Path(replacingFileFullLocation).lastSegment();
			operation_name = "new file rename";
 			moveResourcesOperation = new MoveResourcesOperation(parent.getFile(new Path(lastSegment)),
					fileToBeReplacedPath, operation_name);
 			moveResourcesOperation.execute(new NullProgressMonitor(), null);
 		} catch (ExecutionException e) {
			logger.error("Unable to perform the following operation on the file " +operation_name, e);
 		} catch (InvocationTargetException e) {
			logger.error("Problem running process to import file", e);
 		} catch (InterruptedException e) {
			logger.error("File import cancelled", e);
 		}
 
 		return true;
 	}
 
 	@Override
 	public void addPages() {
 		lookupTableImportPage = new LookupTableImportPage("Import New Lookup table", selection);
 		addPage(lookupTableImportPage);
 	}
 
 	public static class LookupTableImportPage extends WizardPage implements IWizardPage {
 
 		private IResource fileToBeReplaced;
 		private final IStructuredSelection structuredSelection;
 		private Text txtCopyIntoFolder;
 		private Label lblFileCopyToFolder;
 		private Text txtFileToReplace;
 		private Button btnCopyIntoFolder;
 		private Button btnBrowseFileToReplace;
 		private Label lblFileToReplace;
 		private String replacingFileFullLocation;
 
 		public String getReplacingFileFullLocation() {
 			return replacingFileFullLocation;
 		}
 
 		protected LookupTableImportPage(String pageName, IStructuredSelection structuredSelection) {
 			super(pageName);
 			this.structuredSelection = structuredSelection;
 			setTitle("Lookup Table");
 			setDescription("Import a lookup table into GDA");
 		}
 
 		@Override
 		public void createControl(Composite parent) {
 			Composite root = new Composite(parent, SWT.None);
 
 			root.setLayout(new GridLayout());
 
 			Composite srcComposite = createSourceComposite(root);
 			srcComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 			Composite destComposite = createDestinationComposite(root);
 			destComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 			setControl(root);
 			setPageComplete(false);
 		}
 
 		@Override
 		public boolean isPageComplete() {
 			return true;
 		}
 
 		private Composite createDestinationComposite(Composite root) {
 			Group destComposite = new Group(root, SWT.None);
 			destComposite.setLayout(new GridLayout());
 			destComposite.setText("Destination");
 
 			final Button[] btnOptions = new Button[2];
 
 			btnOptions[0] = new Button(destComposite, SWT.RADIO);
 			GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
 			layoutData.horizontalSpan = 2;
 			btnOptions[0].setLayoutData(layoutData);
 			btnOptions[0].setText("Replace existing Lookup table");
 
 			final Composite cmpReplaceExisting = new Composite(destComposite, SWT.None);
 			cmpReplaceExisting.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			cmpReplaceExisting.setLayout(new GridLayout(3, false));
 
 			lblFileToReplace = new Label(cmpReplaceExisting, SWT.None);
 			lblFileToReplace.setLayoutData(new GridData());
 			lblFileToReplace.setText("File To Replace:");
 
 			txtFileToReplace = new Text(cmpReplaceExisting, SWT.BORDER);
 			txtFileToReplace.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 			btnBrowseFileToReplace = new Button(cmpReplaceExisting, SWT.PUSH);
 			btnBrowseFileToReplace.setText("Browse...");
 			btnBrowseFileToReplace.setLayoutData(new GridData());
 			btnBrowseFileToReplace.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					IFile[] openFileSelection = WorkspaceResourceDialog.openFileSelection(
 							btnBrowseFileToReplace.getShell(), "Select File to replace", false, new Object[] {}, null);
 					if (openFileSelection != null && openFileSelection.length == 1) {
 						fileToBeReplaced = openFileSelection[0];
 						txtFileToReplace.setText(openFileSelection[0].getFullPath().toString());
 					}
 				}
 			});
 
 			btnOptions[1] = new Button(destComposite, SWT.RADIO);
 			layoutData = new GridData(GridData.FILL_HORIZONTAL);
 			layoutData.horizontalSpan = 2;
 			btnOptions[1].setLayoutData(layoutData);
 			btnOptions[1].setText("Copy into existing folder");
 
 			final Composite cmpCopyIntoFolder = new Composite(destComposite, SWT.None);
 			cmpCopyIntoFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			cmpCopyIntoFolder.setLayout(new GridLayout(3, false));
 
 			lblFileCopyToFolder = new Label(cmpCopyIntoFolder, SWT.None);
 			lblFileCopyToFolder.setLayoutData(new GridData());
 			lblFileCopyToFolder.setText("Copy Into Folder");
 
 			txtCopyIntoFolder = new Text(cmpCopyIntoFolder, SWT.BORDER);
 			txtCopyIntoFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 			btnCopyIntoFolder = new Button(cmpCopyIntoFolder, SWT.PUSH);
 			btnCopyIntoFolder.setText("Browse...");
 			btnCopyIntoFolder.setLayoutData(new GridData());
 			btnCopyIntoFolder.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					IContainer[] openFolderSelection = WorkspaceResourceDialog.openFolderSelection(
 							btnCopyIntoFolder.getShell(), "Select File to replace", false, new Object[] {}, null);
 					if (openFolderSelection != null && openFolderSelection.length == 1) {
 						txtCopyIntoFolder.setText(openFolderSelection[0].getFullPath().toString());
 					}
 				}
 			});
 
 			final SelectionAdapter rdSelAdapter = new SelectionAdapter() {
 
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					if (e.getSource().equals(btnOptions[0]) && btnOptions[0].getSelection()) {
 						enableReplaceWidget(true);
 						enableCopyWidget(false);
 					} else if (e.getSource().equals(btnOptions[1]) && btnOptions[1].getSelection()) {
 						enableCopyWidget(true);
 						enableReplaceWidget(false);
 					}
 				}
 			};
 			btnOptions[0].addSelectionListener(rdSelAdapter);
 			btnOptions[1].addSelectionListener(rdSelAdapter);
 
 			if (structuredSelection != null) {
 				Object firstElement = structuredSelection.getFirstElement();
 				if (firstElement instanceof IFile && ((IFile) firstElement).getFileExtension().equals("txt")) {
 					IFile file = (IFile) firstElement;
 					btnOptions[0].setSelection(true);
 					btnOptions[1].setSelection(false);
 					enableReplaceWidget(true);
 					enableCopyWidget(false);
 					txtFileToReplace.setText(file.getFullPath().toString());
 					fileToBeReplaced = file;
 				} else {
 					btnOptions[1].setSelection(true);
 					btnOptions[0].setSelection(false);
 					enableCopyWidget(true);
 					enableReplaceWidget(false);
 					if (firstElement instanceof IContainer) {
 						IContainer iContainer = (IContainer) firstElement;
 						txtCopyIntoFolder.setText(iContainer.getFullPath().toString());
 					}
 				}
 
 			}
 
 			return destComposite;
 		}
 
 		public IResource getFileToBeReplaced() {
 			return fileToBeReplaced;
 		}
 
 		private void enableReplaceWidget(boolean enabled) {
 			txtFileToReplace.setEnabled(enabled);
 			btnBrowseFileToReplace.setEnabled(enabled);
 			lblFileToReplace.setEnabled(enabled);
 		}
 
 		private void enableCopyWidget(boolean enabled) {
 			txtCopyIntoFolder.setEnabled(enabled);
 			btnCopyIntoFolder.setEnabled(enabled);
 			lblFileCopyToFolder.setEnabled(enabled);
 		}
 
 		private Composite createSourceComposite(Composite root) {
 			Composite srcComposite = new Composite(root, SWT.None);
 			srcComposite.setLayout(new GridLayout(3, false));
 
 			Label lblFromDir = new Label(srcComposite, SWT.None);
 			lblFromDir.setText("From directory:");
 			lblFromDir.setLayoutData(new GridData());
 
 			final Text txtFileLocation = new Text(srcComposite, SWT.BORDER);
 			txtFileLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 			final Button btnBrowse = new Button(srcComposite, SWT.PUSH);
 			btnBrowse.setLayoutData(new GridData());
 			btnBrowse.setText("Browse...");
 			btnBrowse.addSelectionListener(new SelectionAdapter() {
 
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					FileDialog dialog = new FileDialog(txtFileLocation.getShell(), SWT.SAVE | SWT.SHEET);
 					dialog.setText("File Selection");
 
 					String selectedFile = dialog.open();
 					if (selectedFile != null) {
 						txtFileLocation.setText(selectedFile);
 						replacingFileFullLocation = selectedFile;
 						setErrorMessage(null);
 					}
 				}
 			});
 
 			return srcComposite;
 		}
 
 	}
 }
