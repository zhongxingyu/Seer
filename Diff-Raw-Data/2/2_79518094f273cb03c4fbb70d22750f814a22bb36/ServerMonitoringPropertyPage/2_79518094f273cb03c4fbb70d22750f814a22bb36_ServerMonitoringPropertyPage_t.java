 /*******************************************************************************
  * Copyright (c) 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.php.zendserver.monitor.ui.preferences;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchPropertyPage;
 import org.eclipse.ui.dialogs.PropertyPage;
 import org.osgi.service.prefs.BackingStoreException;
 import org.zend.php.zendserver.deployment.core.targets.TargetsManagerService;
 import org.zend.php.zendserver.monitor.core.MonitorManager;
 import org.zend.php.zendserver.monitor.internal.ui.Activator;
 import org.zend.php.zendserver.monitor.internal.ui.Messages;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.connection.data.values.IssueSeverity;
 
 /**
  * Property page for Zend Server monitoring customization.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class ServerMonitoringPropertyPage extends PropertyPage implements
 		IWorkbenchPropertyPage {
 
 	private static final int DELAY_DEFAULT = 10;
 
 	private Button monitoringEnabled;
 	private Button[] severityButtons;
 	private Button hideButton;
 	private Text delayText;
 
 	private IEclipsePreferences prefs;
 
 	public ServerMonitoringPropertyPage() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
 	 */
 	public boolean performOk() {
 		boolean isDirty = false;
 		try {
 			for (Button button : severityButtons) {
 				boolean selection = button.getSelection();
 				String nodeName = getNodeName(button);
 				Boolean current = getCurrentValue(nodeName);
 				if ((current != null && current != selection)
 						|| (current == null && selection)) {
 					prefs.putBoolean(getNodeName(button), selection);
 					isDirty = true;
 				}
 			}
 			Boolean hideValue = getCurrentValue(MonitorManager.HIDE_KEY);
 			boolean hideSelection = hideButton.getSelection();
 			if ((hideValue != null && hideValue != hideSelection)
 					|| (hideValue == null && hideSelection)) {
 				prefs.putBoolean(MonitorManager.HIDE_KEY, hideSelection);
 				isDirty = true;
 			}
 			int val = Integer.valueOf(delayText.getText());
 			if (val != DELAY_DEFAULT) {
 				prefs.putInt(MonitorManager.HIDE_TIME_KEY, val);
 				isDirty = true;
 			}
 			if (isDirty) {
 				prefs.flush();
 			}
 			if (getEnabledTargets().size() == 0
 					&& monitoringEnabled.getSelection()) {
 				Job createMonitors = new Job(
 						Messages.ServerMonitoringPropertyPage_EnableJobTitle) {
 
 					@Override
 					protected IStatus run(IProgressMonitor monitor) {
 						IProject p = getProject();
 						MonitorManager.setEnabled(p.getName(), true);
 						MonitorManager.create(p.getName());
 						return Status.OK_STATUS;
 					}
 				};
 				createMonitors.setSystem(true);
 				createMonitors.schedule();
 			} else if (getEnabledTargets().size() != 0
 					&& !monitoringEnabled.getSelection()) {
 				MonitorManager.removeProject(getProject().getName());
 			}
 		} catch (BackingStoreException e) {
 			Activator.log(e);
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
 	 * .swt.widgets.Composite)
 	 */
 	protected Control createContents(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		composite.setLayout(new GridLayout(1, true));
 		monitoringEnabled = new Button(composite, SWT.CHECK);
 		monitoringEnabled.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				updateEnablement(monitoringEnabled.getSelection());
 			}
 		});
 		monitoringEnabled
 				.setText(Messages.ServerMonitoringPropertyPage_EnableCheckboxLabel);
 		createSeveritySection(composite);
 		createDelaySection(composite);
 		init();
 		return composite;
 	}
 
 	protected void updateEnablement(boolean selection) {
 		for (Button button : severityButtons) {
 			button.setEnabled(selection);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
 	 */
 	protected void performDefaults() {
 		IProject project = getProject();
 		for (Button button : severityButtons) {
 			IssueSeverity sev = IssueSeverity.byName(button.getText());
 			if (sev == IssueSeverity.CRITICAL || sev == IssueSeverity.WARNING) {
 				button.setSelection(true);
 			} else {
 				button.setSelection(false);
 			}
 		}
 		hideButton.setSelection(false);
 		delayText.setText(String.valueOf(DELAY_DEFAULT));
 		delayText.setEnabled(false);
 		if (MonitorManager.isDeployed(project.getName())) {
 			if (getEnabledTargets().size() > 0) {
 				updateEnablement(true);
 				monitoringEnabled.setSelection(true);
 			} else {
 				updateEnablement(false);
 				monitoringEnabled.setSelection(false);
 			}
 			monitoringEnabled.setEnabled(true);
 		} else {
 			updateEnablement(false);
 			monitoringEnabled.setSelection(false);
 			monitoringEnabled.setEnabled(false);
 		}
 	}
 
 	private Boolean getCurrentValue(String nodeName) {
 		String val = prefs.get(nodeName, (String) null);
 		return val != null ? Boolean.valueOf(val) : null;
 	}
 
 	private void init() {
 		IProject project = getProject();
 		if (project != null) {
 			prefs = new ProjectScope(project)
 					.getNode(org.zend.php.zendserver.monitor.core.Activator.PLUGIN_ID);
 		}
 		for (Button button : severityButtons) {
 			String nodeName = getNodeName(button);
 			button.setEnabled(isEnabled());
 			button.setSelection(prefs.getBoolean(nodeName, false));
 		}
 
 		boolean hideValue = prefs.getBoolean(MonitorManager.HIDE_KEY, false);
 		hideButton.setSelection(hideValue);
 		delayText.setEnabled(hideValue);
 		delayText.setText(String.valueOf(prefs.getInt(
 				MonitorManager.HIDE_TIME_KEY, 10)));
 
 		if (MonitorManager.isDeployed(project.getName())) {
 			if (getEnabledTargets().size() > 0) {
 				updateEnablement(true);
 				monitoringEnabled.setSelection(true);
 			}
 		} else {
 			monitoringEnabled.setEnabled(false);
 		}
 	}
 
 	private List<IZendTarget> getEnabledTargets() {
 		List<IZendTarget> result = new ArrayList<IZendTarget>();
 		IZendTarget[] targets = TargetsManagerService.INSTANCE
 				.getTargetManager().getTargets();
 		for (IZendTarget target : targets) {
 			String value = prefs.get(MonitorManager.ENABLED + target.getId(),
 					null);
 			if (value != null && Boolean.valueOf(value)) {
 				result.add(target);
 			}
 		}
 		return result;
 	}
 
 	private String getNodeName(Button button) {
 		return button.getText().toLowerCase();
 	}
 
 	private void createSeveritySection(Composite composite) {
 		Label label = new Label(composite, SWT.WRAP);
 		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		label.setText(Messages.ServerMonitoringPropertyPage_Description);
 		Group group = new Group(composite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		group.setLayout(new GridLayout(1, false));
 		IssueSeverity[] severityValues = IssueSeverity.values();
 		severityButtons = new Button[severityValues.length];
 		for (int i = 0; i < severityValues.length; i++) {
 			severityButtons[i] = createCheckBox(group, severityValues[i]);
 		}
 	}
 
 	private void createDelaySection(Composite composite) {
 		hideButton = new Button(composite, SWT.CHECK);
 		hideButton.setText(Messages.ServerMonitoringPropertyPage_HideLabel);
 		hideButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				delayText.setEnabled(hideButton.getSelection());
 			}
 		});
 		hideButton.setLayoutData(new GridData(SWT.TOP, SWT.FILL, false, false));
 		Group group = new Group(composite, SWT.NONE);
 		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
 		group.setLayout(new GridLayout(1, false));
 		Composite labelComposite = new Composite(group, SWT.NONE);
 		labelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
 				false));
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		labelComposite.setLayout(layout);
 		Label label = new Label(labelComposite, SWT.NONE);
 		label.setText(Messages.ServerMonitoringPropertyPage_DelayLabel);
 		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, true);
 		gd.minimumWidth = 100;
 		label.setLayoutData(gd);
 		delayText = new Text(labelComposite, SWT.BORDER | SWT.SINGLE);
 		delayText.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				try {
 					String val = delayText.getText();
 					Integer.valueOf(val);
 				} catch (NumberFormatException ex) {
 					setErrorMessage(Messages.ServerMonitoringPropertyPage_InvalidDelayMessage);
 					setValid(false);
 					return;
 				}
 				setErrorMessage(null);
 				setValid(true);
 			}
 		});
 		delayText.setText(String.valueOf(DELAY_DEFAULT));
 		delayText
 				.setToolTipText(Messages.ServerMonitoringPropertyPage_DelayTooltip);
 		GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
 		data.minimumWidth = 100;
 		delayText.setLayoutData(data);
 	}
 
 	private boolean isEnabled() {
 		TargetsManager manager = new TargetsManager();
 		IZendTarget[] targets = manager.getTargets();
 		for (IZendTarget target : targets) {
 			if (prefs
 					.getBoolean(MonitorManager.ENABLED + target.getId(), false)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private Button createCheckBox(Composite parent, IssueSeverity severity) {
 		Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
 		button.setText(severity.getName());
 		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		return button;
 	}
 
 	private IProject getProject() {
 		final IAdaptable adaptable = getElement();
 		Object project = Platform.getAdapterManager().getAdapter(adaptable,
 				IProject.class);
 		if (project instanceof IProject) {
 			return (IProject) project;
 		}
 		return null;
 	}
 
 }
