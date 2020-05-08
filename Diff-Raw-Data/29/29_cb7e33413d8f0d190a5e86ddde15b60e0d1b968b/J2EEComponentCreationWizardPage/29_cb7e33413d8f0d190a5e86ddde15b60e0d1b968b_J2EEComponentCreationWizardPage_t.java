 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Nov 10, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jst.j2ee.application.operations.J2EEComponentCreationDataModel;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.servertarget.ServerTargetDataModel;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.common.frameworks.operations.WTPOperationDataModel;
 import org.eclipse.wst.common.frameworks.operations.WTPPropertyDescriptor;
 import org.eclipse.wst.common.frameworks.ui.WTPWizardPage;
 import org.eclipse.wst.common.modulecore.internal.operation.ComponentCreationDataModel;
 
 import org.eclipse.wst.server.ui.ServerUIUtil;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 
 
 public abstract class J2EEComponentCreationWizardPage extends WTPWizardPage {
 
 	private static final boolean isWindows = SWT.getPlatform().toLowerCase().startsWith("win"); //$NON-NLS-1$
 	protected static final String MODULE_VERSION = "Module Version:";
 	protected NewModuleGroup projectNameGroup;
 	protected Composite advancedComposite;
 	protected Button advancedButton;
 	protected boolean showAdvanced = false;
 	protected AdvancedSizeController advancedController;
 	protected boolean advancedControlsBuilt = false;
 	private ServerEarAndStandaloneGroup earGroup;
 
 	/**
 	 *  This type is responsible for setting the Shell size based on the showAdvanced flag. It will
 	 * track the original size of the Shell even if the user resizes it. One problem that we may
 	 * face is that the Shell size could change by the framework prior to the Shell being made
 	 * visible but the page will already get an enter call. This means that we will need to set the
 	 * Shell size based on the showAdvanced flag when the Shell resize event is called and the Shell
 	 * is visible.
 	 */
 	private class AdvancedSizeController implements ControlListener {
 		private int advancedHeight = -1;
 		private Point originalSize;
 		private boolean ignoreShellResize = false;
 
 		private AdvancedSizeController(Shell aShell) {
 			originalSize = aShell.getSize();
 			aShell.addControlListener(this);
 		}
 
 		public void controlMoved(ControlEvent e) {
 			//do nothing
 		}
 
 		public void controlResized(ControlEvent e) {
 			if (!ignoreShellResize) {
 				Control control = (Control) e.getSource();
 				if (control.isVisible()) {
 					originalSize = control.getSize();
 					if (advancedHeight == -1)
 						setShellSizeForAdvanced();
 				}
 			}
 		}
 
 		protected void resetOriginalShellSize() {
 			setShellSize(originalSize.x, originalSize.y);
 		}
 
 		private void setShellSize(int x, int y) {
 			ignoreShellResize = true;
 			try {
 				getShell().setSize(x, y);
 			} finally {
 				ignoreShellResize = false;
 			}
 		}
 
 		protected void setShellSizeForAdvanced() {
 			int height = calculateAdvancedShellHeight();
 			if (height != -1)
 				setShellSize(getShell().getSize().x, height);
 		}
 
 		private int calculateAdvancedShellHeight() {
 			Point advancedCompSize = advancedComposite.getSize();
 			if (advancedCompSize.x == 0)
 				return -1;
 			int height = computeAdvancedHeight();
 			if (!showAdvanced && height != -1)
 				height = height - advancedComposite.getSize().y;
 			return height;
 		}
 
 		/*
 		 * Compute the height with the advanced section showing. @return
 		 */
 		private int computeAdvancedHeight() {
 			if (advancedHeight == -1) {
 				Point controlSize = getControl().getSize();
 				if (controlSize.x != 0) {
 					int minHeight = originalSize.y - controlSize.y;
 					Point pageSize = getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
 					advancedHeight = pageSize.y + minHeight;
 				}
 			}
 			return advancedHeight;
 		}
 	}
 	
 	public J2EEComponentCreationWizardPage(J2EEComponentCreationDataModel dataModel, String pageName) {
 		super(dataModel, pageName);
 	}
 	
 	protected void createProjectNameGroup(Composite parent) {
 		projectNameGroup = new NewModuleGroup(parent, SWT.NULL, (J2EEComponentCreationDataModel)model);
 	}
 
 	protected void addToAdvancedComposite(Composite advanced) {
 		createVersionComposite(advanced);
 		createServerEarAndStandaloneGroup(advanced);
 	}
 
 	protected void createServerEarAndStandaloneGroup(Composite parent) {
 		earGroup = new ServerEarAndStandaloneGroup(parent, getJ2EEModuleCreationDataModel());
 	}
 
 	protected J2EEComponentCreationDataModel getJ2EEModuleCreationDataModel() {
 		return (J2EEComponentCreationDataModel) model;
 	}
 
 	protected void validatePage() {
 		super.validatePage();
 		if (!showAdvanced && !isPageComplete()) {
 			String prop = validateControlsBase();
 			if (null != prop) {
 				String[] advancedProperties = {WTPOperationDataModel.NESTED_MODEL_VALIDATION_HOOK, ComponentCreationDataModel.COMPONENT_VERSION, J2EEComponentCreationDataModel.EAR_MODULE_NAME, J2EEComponentCreationDataModel.ADD_TO_EAR};
 				for (int i = 0; i < advancedProperties.length; i++) {
 					if (prop.equals(advancedProperties[i])) {
 						toggleAdvanced(true);
 						return;
 					}
 				}
 			}
 		}
 	}
 
 	protected String[] getValidationPropertyNames() {
 		return new String[]{ComponentCreationDataModel.PROJECT_NAME, ComponentCreationDataModel.COMPONENT_VERSION, ComponentCreationDataModel.COMPONENT_NAME, J2EEComponentCreationDataModel.EAR_MODULE_NAME, J2EEComponentCreationDataModel.ADD_TO_EAR};
 	}
 
 	protected void createVersionComposite(Composite parent) {
 		createVersionComposite(parent, getVersionLabel(), ComponentCreationDataModel.COMPONENT_VERSION);
 	}
 
 	protected String getVersionLabel() {
 		return MODULE_VERSION;
 	}
 
 	public void dispose() {
 		super.dispose();
 		if (earGroup != null)
 			earGroup.dispose();
 		if (projectNameGroup != null)
 			projectNameGroup.dispose();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#enter()
 	 */
 	protected void enter() {
//		if (advancedControlsBuilt) {
//			if (isFirstTimeToPage)
//				initializeAdvancedController();
//			if (isWindows) {
//				advancedController.setShellSizeForAdvanced();
//			}
//		}
 		super.enter();
 	}
 
 	private void initializeAdvancedController() {
 		advancedController = new AdvancedSizeController(getShell());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#exit()
 	 */
 	protected void exit() {
		if (advancedControlsBuilt && isWindows && advancedController!=null) {
 			advancedController.resetOriginalShellSize();
 		}
 		super.exit();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#storeDefaultSettings()
 	 */
 	public void storeDefaultSettings() {
 		super.storeDefaultSettings();
 		if (advancedControlsBuilt) {
 			IDialogSettings settings = getDialogSettings();
 			if (settings != null)
 				settings.put(getShowAdvancedKey(), showAdvanced);
 		}
 	}
 
 	protected String getShowAdvancedKey() {
 		return getClass().getName() + "_SHOW_ADVANCED"; //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#restoreDefaultSettings()
 	 */
 	protected void restoreDefaultSettings() {
 		super.restoreDefaultSettings();
 		if (advancedControlsBuilt) {
 			IDialogSettings settings = getDialogSettings();
 			if (settings != null)
 				showAdvanced = !settings.getBoolean(getShowAdvancedKey());
 			advancedButton.setSelection(!showAdvanced); //set opposite b/c toggleAdvanced(boolean)
 			// will flip it
 			toggleAdvanced(false);
 		}
 	}
 	
 	protected Composite createTopLevelComposite(Composite parent) {
 		Composite top = new Composite(parent, SWT.NONE);
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, getInfopopID());
 		top.setLayout(new GridLayout());
 		top.setData(new GridData(GridData.FILL_BOTH));
 		Composite composite = new Composite(top, SWT.NONE);
 		GridLayout layout = new GridLayout(3, false);
 		composite.setLayout(layout);
 		createProjectNameGroup(composite);
 		Composite detail = new Composite(top, SWT.NONE);
 		detail.setLayout(new GridLayout());
 		detail.setData(new GridData(GridData.FILL_BOTH));
 		createAdvancedComposite(detail);
 		return top;
 	}
 
 	/**
 	 * @param parent
 	 */
 	protected Composite createAdvancedComposite(Composite parent) {
 		advancedControlsBuilt = true;
 		advancedButton = new Button(parent, SWT.TOGGLE);
 		setAdvancedLabelText();
 		final Cursor hand = new Cursor(advancedButton.getDisplay(), SWT.CURSOR_HAND);
 		advancedButton.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				hand.dispose();
 			}
 		});
 		advancedComposite = new Composite(parent, SWT.NONE);
 		//toggleAdvanced(false);
 		GridLayout layout = new GridLayout(3, false);
 		GridData data = new GridData();
 		advancedComposite.setData(data);
 		advancedComposite.setLayout(layout);
 		advancedButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				toggleAdvanced(true);
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				//do nothing
 			}
 		});
 		advancedButton.addListener(SWT.MouseHover, new Listener() {
 			public void handleEvent(Event event) {
 				if (event.type == SWT.MouseHover)
 					advancedButton.setCursor(hand);
 			}
 		});
 		addToAdvancedComposite(advancedComposite);
 		return advancedComposite;
 	}
 
 	/**
 	 * @param advancedLabel
 	 */
 	private void setAdvancedLabelText() {
 		if (advancedControlsBuilt) {
 			if (showAdvanced)
 				advancedButton.setText(J2EEUIMessages.getResourceString("J2EEProjectCreationPage_UI_0")); //$NON-NLS-1$
 			else
 				advancedButton.setText(J2EEUIMessages.getResourceString("J2EEProjectCreationPage_UI_1")); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * @param advancedLabel
 	 */
 	protected void toggleAdvanced(boolean setSize) {
 		if (advancedControlsBuilt) {
 			showAdvanced = !showAdvanced;
 			advancedComposite.setVisible(showAdvanced);
 			setAdvancedLabelText();
			if (setSize && isWindows) {
				if (advancedControlsBuilt) {
					if (advancedController == null)
						initializeAdvancedController();
					if (isWindows) {
						advancedController.setShellSizeForAdvanced();
					}
				}
 				advancedController.setShellSizeForAdvanced();
			}	
 		}
 	}
 
 	protected void createVersionComposite(Composite parent, String labelText, String versionProp) {
 		Label label = new Label(parent, SWT.NONE);
 		label.setText(labelText);
 		Combo versionCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
 		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.widthHint = 305;
 		versionCombo.setLayoutData(gridData);
 		Control[] deps = new Control[]{label};
 		synchHelper.synchCombo(versionCombo, versionProp, deps);
 		String[] items = versionCombo.getItems();
 		if (items != null && items.length > 0)
 			versionCombo.select(items.length - 1);
 		new Label(parent, SWT.NONE); //pad
 	}
 	
 	public static boolean launchNewRuntimeWizard(Shell shell, ServerTargetDataModel model) {
 		WTPPropertyDescriptor[] preAdditionDescriptors = model.getValidPropertyDescriptors(ServerTargetDataModel.RUNTIME_TARGET_ID);
 		boolean isOK = ServerUIUtil.showNewRuntimeWizard(shell, model.computeTypeId(), model.computeVersionId());
 		if (isOK && model != null) {
 			model.notifyValidValuesChange(ServerTargetDataModel.RUNTIME_TARGET_ID);
 			WTPPropertyDescriptor[] postAdditionDescriptors = model.getValidPropertyDescriptors(ServerTargetDataModel.RUNTIME_TARGET_ID);
 			Object[] preAddition = new Object[preAdditionDescriptors.length];
 			for (int i = 0; i < preAddition.length; i++) {
 				preAddition[i] = preAdditionDescriptors[i].getPropertyValue();
 			}
 			Object[] postAddition = new Object[postAdditionDescriptors.length];
 			for (int i = 0; i < postAddition.length; i++) {
 				postAddition[i] = postAdditionDescriptors[i].getPropertyValue();
 			}
 			Object newAddition = ProjectUtilities.getNewObject(preAddition, postAddition);
 			if (newAddition != null)
 				model.setProperty(ServerTargetDataModel.RUNTIME_TARGET_ID, newAddition);
 		}
 		return isOK;
 	}
 
 }
