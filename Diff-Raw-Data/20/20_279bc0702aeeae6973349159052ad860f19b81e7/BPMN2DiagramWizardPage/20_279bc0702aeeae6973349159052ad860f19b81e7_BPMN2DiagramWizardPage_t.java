 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
  *  All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *
  * @author Innar Made
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.ui.wizards;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 
 public class BPMN2DiagramWizardPage extends WizardPage {
 	private Text containerText;
 
 	private Text fileText;
 
 	private final ISelection selection;
 
 	private IResource diagramContainer;
 
 	/**
 	 * Constructor for SampleNewWizardPage.
 	 * 
 	 * @param pageName
 	 */
 	public BPMN2DiagramWizardPage(ISelection selection) {
 		super("wizardPage");
 		setTitle("New BPMN2 File");
 		setDescription("Create a new BPMN2 file.");
 		this.selection = selection;
 	}
 
 	/**
 	 * @see IDialogPage#createControl(Composite)
 	 */
 	@Override
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 3;
 		layout.verticalSpacing = 9;
 		Label label = new Label(container, SWT.NULL);
 		label.setText("&Location:");
 
 		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		containerText.setLayoutData(gd);
 		containerText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 
 		Button button = new Button(container, SWT.PUSH);
 		button.setText("Browse...");
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleBrowse();
 			}
 		});
 		label = new Label(container, SWT.NULL);
 		label.setText("&File name:");
 
 		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fileText.setLayoutData(gd);
 		fileText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		initialize();
 		dialogChanged();
 		setControl(container);
 	}
 
 	/**
 	 * Tests if the current workbench selection is a suitable diagramContainer to use.
 	 */
 
 	private void initialize() {
		String filename = "process.bpmn";
 		if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
 			IStructuredSelection ssel = (IStructuredSelection) selection;
 			if (ssel.size() > 1) {
 				return;
 			}
 			Object obj = ssel.getFirstElement();
			if (obj instanceof IAdaptable) {
				Object res = ((IAdaptable)obj).getAdapter(IResource.class);
				if (res!=null)
					obj = res;
			}
 			if (obj instanceof IResource) {
 				IContainer container;
 				if (obj instanceof IContainer) {
 					container = (IContainer) obj;
 				} else {
 					container = ((IResource) obj).getParent();
 				}
 				containerText.setText(container.getFullPath().toString());
				for (int i=1; ; ++i) {
					filename = "process_" + i + ".bpmn";
					IResource file = container.findMember(filename);
					if (file==null) {
						break;
					}
				}
 			}
 		}
		fileText.setText(filename);
 	}
 
 	/**
 	 * Uses the standard diagramContainer selection dialog to choose the new value for the diagramContainer field.
 	 */
 
 	private void handleBrowse() {
 		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace()
 				.getRoot(), false, "Select Folder for the diagram");
 		if (dialog.open() == Window.OK) {
 			Object[] result = dialog.getResult();
 			if (result.length == 1) {
 				containerText.setText(((Path) result[0]).toString());
 			}
 		}
 	}
 
 	/**
 	 * Ensures that both text fields are set.
 	 */
 
 	private void dialogChanged() {
 		diagramContainer = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
 		String fileName = getFileName();
 
 		if (getContainerName().length() == 0) {
 			updateStatus("Folder must be specified");
 			return;
 		}
 		if (diagramContainer == null || (diagramContainer.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
 			updateStatus("Folder must exist");
 			return;
 		}
 		if (!diagramContainer.isAccessible()) {
 			updateStatus("Project must be writable");
 			return;
 		}
 		if (fileName.length() == 0) {
 			updateStatus("Name must be specified");
 			return;
 		}
 		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
 			updateStatus("Name must be valid");
 			return;
 		}
 		int dotLoc = fileName.lastIndexOf('.');
 		if (dotLoc != -1) {
 			String ext = fileName.substring(dotLoc + 1);
 			if (ext.equalsIgnoreCase("bpmn") == false && ext.equalsIgnoreCase("bpmn2") == false) {
 				updateStatus("File extension must be \"bpmn\" or \"bpmn2\"");
 				return;
 			}
 		}
 		updateStatus(null);
 	}
 
 	private void updateStatus(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 
 	public String getContainerName() {
 		return containerText.getText();
 	}
 
 	public String getFileName() {
 		return fileText.getText();
 	}
 
 	public IResource getDiagramContainer() {
 		return diagramContainer;
 	}
 }
