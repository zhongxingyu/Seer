 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.ui.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.ui.wizard.IWizardHandle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.events.*;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMInstallType;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.preference.IPreferenceNode;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.preference.PreferenceManager;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jst.server.core.internal.GenericRuntime;
 import org.eclipse.jst.server.core.internal.IGenericRuntimeWorkingCopy;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.ui.PlatformUI;
 /**
  * Wizard page to set the server install directory.
  */
 public class GenericRuntimeComposite extends Composite {
 	protected static final String INSTALLED_JRE_PREFERENCE_PAGE_ID = "org.eclipse.jdt.debug.ui.preferences.VMPreferencePage";
 	protected IRuntimeWorkingCopy runtimeWC;
 	protected IGenericRuntimeWorkingCopy runtime;
 	
 	protected IWizardHandle wizard;
 	
 	protected Text name;
 	protected Text installDir;
 	protected Combo combo;
 	protected List installedJREs;
 	protected String[] jreNames;
 
 	/**
 	 * GenericRuntimeComposite constructor comment.
 	 */
 	protected GenericRuntimeComposite(Composite parent, IWizardHandle wizard) {
 		super(parent, SWT.NONE);
 		this.wizard = wizard;
 		
 		wizard.setTitle(Messages.runtimeTypeTitle);
 		wizard.setDescription(Messages.runtimeTypeDescription);
 		wizard.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_WIZ_RUNTIME_TYPE));
 		
 		createControl();
 	}
 
 	protected void setRuntime(IRuntimeWorkingCopy newRuntime) {
 		if (newRuntime == null) {
 			runtimeWC = null;
 			runtime = null;
 		} else {
 			runtimeWC = newRuntime;
			runtime = (IGenericRuntimeWorkingCopy) newRuntime.getAdapter(IGenericRuntimeWorkingCopy.class);
 		}
 		
 		init();
 		validate();
 	}
 
 	/**
 	 * Provide a wizard page to change the root directory.
 	 */
 	protected void createControl() {
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		setLayout(layout);
 		setLayoutData(new GridData(GridData.FILL_BOTH));
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ContextIds.RUNTIME_COMPOSITE);
 		
 		Label label = new Label(this, SWT.NONE);
 		label.setText(Messages.runtimeTypeName);
 		GridData data = new GridData();
 		data.horizontalSpan = 2;
 		label.setLayoutData(data);
 		
 		name = new Text(this, SWT.BORDER);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		name.setLayoutData(data);
 		name.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				runtimeWC.setName(name.getText());
 				validate();
 			}
 		});
 	
 		label = new Label(this, SWT.NONE);
 		label.setText(Messages.runtimeTypeLocation);
 		data = new GridData();
 		data.horizontalSpan = 2;
 		label.setLayoutData(data);
 	
 		installDir = new Text(this, SWT.BORDER);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		installDir.setLayoutData(data);
 		installDir.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				runtimeWC.setLocation(new Path(installDir.getText()));
 				validate();
 			}
 		});
 	
 		Button browse = SWTUtil.createButton(this, Messages.browse);
 		browse.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent se) {
 				DirectoryDialog dialog = new DirectoryDialog(GenericRuntimeComposite.this.getShell());
 				dialog.setMessage(Messages.runtimeTypeSelectLocation);
 				dialog.setFilterPath(installDir.getText());
 				String selectedDirectory = dialog.open();
 				if (selectedDirectory != null)
 					installDir.setText(selectedDirectory);
 			}
 		});
 		
 		updateJREs();
 		
 		// JDK location
 		label = new Label(this, SWT.NONE);
 		label.setText(Messages.runtimeTypeJRE);
 		data = new GridData();
 		data.horizontalSpan = 2;
 		label.setLayoutData(data);
 		
 		combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
 		combo.setItems(jreNames);
 		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		combo.setLayoutData(data);
 		
 		combo.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				int sel = combo.getSelectionIndex();
 				IVMInstall vmInstall = null;
 				if (sel > 0)
 					vmInstall = (IVMInstall) installedJREs.get(sel - 1);
 				
 				runtime.setVMInstall(vmInstall);
 				validate();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		
 		Button button = SWTUtil.createButton(this, Messages.runtimeTypeInstalledJREs);
 		button.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				String currentVM = combo.getText();
 				if (showPreferencePage()) {
 					updateJREs();
 					combo.setItems(jreNames);
 					combo.setText(currentVM);
 					if (combo.getSelectionIndex() == -1)
 						combo.select(0);
 				}
 			}
 		});
 		
 		init();
 		validate();
 
 		Dialog.applyDialogFont(this);
 
 		name.forceFocus();
 	}
 	
 	protected void updateJREs() {
 		// get all installed JVMs
 		installedJREs = new ArrayList();
 		IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
 		int size = vmInstallTypes.length;
 		for (int i = 0; i < size; i++) {
 			IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
 			int size2 = vmInstalls.length;
 			for (int j = 0; j < size2; j++) {
 				installedJREs.add(vmInstalls[j]);
 			}
 		}
 		
 		// get names
 		size = installedJREs.size();
 		jreNames = new String[size+1];
 		jreNames[0] = Messages.runtimeTypeDefaultJRE;
 		for (int i = 0; i < size; i++) {
 			IVMInstall vmInstall = (IVMInstall) installedJREs.get(i);
 			jreNames[i+1] = vmInstall.getName();
 		}
 	}
 
 	protected boolean showPreferencePage() {
 		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();
 		IPreferenceNode node = manager.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage").findSubNode("org.eclipse.jdt.debug.ui.preferences.VMPreferencePage");
 		PreferenceManager manager2 = new PreferenceManager();
 		manager2.addToRoot(node);
 		final PreferenceDialog dialog = new PreferenceDialog(getShell(), manager2);
 		final boolean[] result = new boolean[] { false };
 		BusyIndicator.showWhile(getDisplay(), new Runnable() {
 			public void run() {
 				dialog.create();
 				if (dialog.open() == Window.OK)
 					result[0] = true;
 			}
 		});
 		return result[0];
 	}
 	
 	protected void init() {
 		if (installDir == null || runtime == null)
 			return;
 		
 		name.setText(runtimeWC.getName());
 		
 		if (runtimeWC.getLocation() != null)
 			installDir.setText(runtimeWC.getLocation().toOSString());
 		else
 			installDir.setText("");
 		
 		// set selection
 		if (((GenericRuntime)runtime).isUsingDefaultJRE())
 			combo.select(0);
 		else {
 			boolean found = false;
 			int size = installedJREs.size();
 			for (int i = 0; i < size; i++) {
 				IVMInstall vmInstall = (IVMInstall) installedJREs.get(i);
 				if (vmInstall.equals(runtime.getVMInstall())) {
 					combo.select(i + 1);
 					found = true;
 				}
 			}
 			if (!found)
 				combo.select(0);
 		}
 	}
 
 	protected void validate() {
 		if (runtime == null) {
 			wizard.setMessage("", IMessageProvider.ERROR);
 			return;
 		}
 
 		IStatus status = runtimeWC.validate(null);
 		if (status == null || status.isOK())
 			wizard.setMessage(null, IMessageProvider.NONE);
 		else
 			wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
 	}
 }
