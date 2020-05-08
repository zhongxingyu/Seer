 package org.zend.php.zendserver.deployment.debug.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.statushandlers.StatusManager;
 import org.zend.php.zendserver.deployment.core.sdk.EclipseMappingModelLoader;
 import org.zend.php.zendserver.deployment.core.sdk.SdkStatus;
 import org.zend.php.zendserver.deployment.core.sdk.StatusChangeListener;
 import org.zend.php.zendserver.deployment.debug.core.config.DeploymentHelper;
 import org.zend.php.zendserver.deployment.debug.core.config.IDeploymentHelper;
 import org.zend.php.zendserver.deployment.debug.core.jobs.AbstractLaunchJob;
 import org.zend.php.zendserver.deployment.debug.ui.Activator;
 import org.zend.php.zendserver.deployment.debug.ui.Messages;
 import org.zend.php.zendserver.deployment.ui.actions.AddTargetAction;
 import org.zend.php.zendserver.deployment.ui.targets.TargetsCombo;
 import org.zend.sdklib.application.ZendApplication;
 import org.zend.sdklib.library.StatusCode;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 import org.zend.webapi.core.connection.data.ApplicationsList;
 
 public class ConfigurationBlock extends AbstractBlock {
 
 	private TargetsCombo targetsCombo = new TargetsCombo();
 	private Link newTargetLink;
 	private Text baseUrl;
 	private Text userAppName;
 	private Button ignoreFailures;
 	private Button deployButton;
 	private Button updateButton;
 	private Button autoDeployButton;
 	private Button noActionButton;
 
 	private IRunnableContext context;
 	private Combo updateCombo;
 	private Combo autoDeployCombo;
 
 	private ApplicationInfo[] applicationInfos = new ApplicationInfo[0];
 
 	private boolean autoDeploy;
 	private int currentAppId;
 
 	public ConfigurationBlock(IStatusChangeListener listener) {
 		this(listener, null);
 	}
 
 	public ConfigurationBlock(IStatusChangeListener listener, IRunnableContext context) {
 		super(listener);
 		this.context = context;
 		this.autoDeploy = isAutoDeployAvailable();
 	}
 
 	@Override
 	public Composite createContents(final Composite parent) {
 		super.createContents(parent);
 		getContainer().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		createDeployCombo(getContainer());
 		createLocationLink(getContainer());
 		baseUrl = createLabelWithText(Messages.configurationPage_baseURL, "", getContainer()); //$NON-NLS-1$
 		ExpandableComposite expComposite = new ExpandableComposite(getContainer(), SWT.NONE,
 				ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
 		expComposite.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				if (e.getState()) {
 					Shell shell = parent.getShell();
 					Point point = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 					shell.setSize(shell.getSize().x, point.y);
 				}
 			}
 		});
 		expComposite.setText(Messages.advancedSection_Title);
 		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gd.horizontalSpan = 2;
 		expComposite.setLayoutData(gd);
 		Composite advancedSection = new Composite(expComposite, SWT.NONE);
 		advancedSection.setLayout(new GridLayout(2, false));
 		createOperationsSection(advancedSection);
 		userAppName = createLabelWithText(Messages.configurationPage_appUserName,
 				Messages.configurationPage_appUserNameTooltip, advancedSection);
 		ignoreFailures = createLabelWithCheckbox(Messages.configurationPage_ignoreFailures,
 				Messages.configurationPage_ignoreFailuresTooltip, advancedSection);
 		;
 		expComposite.setClient(advancedSection);
 		return getContainer();
 	}
 
 	@Override
 	public void initializeFields(IDeploymentHelper helper) {
 		targetsCombo.select(helper.getTargetId());
 		
 		URL newBaseURL = helper.getBaseURL();
 		if (newBaseURL != null) {
 			if (helper.isDefaultServer()) {
 				String targetHost = getTarget().getDefaultServerURL().toString();
 				baseUrl.setText(targetHost + newBaseURL.getPath());
 			} else {
 				baseUrl.setText(newBaseURL.toString());
 			}
 		}
 		ignoreFailures.setSelection(helper.isIgnoreFailures());
 		userAppName.setText(helper.getAppName());
 		currentAppId = helper.getAppId();
 		initDefaultOperation(helper);
 	}
 
 	public void initDefaultOperation(IDeploymentHelper helper) {
 		switch (helper.getOperationType()) {
 		case IDeploymentHelper.DEPLOY:
 			if (!deployButton.getSelection()) {
 				deployButton.setSelection(true);
 				enableDeploySection();
 			}
 			break;
 		case IDeploymentHelper.UPDATE:
 			if (!updateButton.getSelection()) {
 				updateButton.setSelection(true);
 				enableUpdateSection();
 			}
 			break;
 		case IDeploymentHelper.AUTO_DEPLOY:
 			if (autoDeploy && !autoDeployButton.getSelection()) {
 				autoDeployButton.setSelection(true);
 				enableAutoDeploySection();
 			}
 			break;
 		case IDeploymentHelper.NO_ACTION:
 			if (!noActionButton.getSelection()) {
 				noActionButton.setSelection(true);
 				enableNoActionSection();
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public IStatus validatePage() {
 		if (getTarget() == null) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					Messages.configurationPage_ValidationError_TargetLocation);
 		}
 		if (getBaseURL() == null) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					Messages.configurationPage_ValidationError_BaseUrl);
 		}
 		return new Status(IStatus.OK, Activator.PLUGIN_ID, Messages.configurationPage_Description);
 	}
 
 	@Override
 	public IDeploymentHelper getHelper() {
 		DeploymentHelper helper = new DeploymentHelper();
 		URL url = getBaseURL();
 		if (url != null) {
 			helper.setBaseURL(url.toString());
 		}
 		helper.setTargetId(getTarget().getId());
 		helper.setTargetHost(getTarget().getHost().getHost());
 		if (getOperationType() == IDeploymentHelper.UPDATE) {
 			ApplicationInfo info = getUpdateSelection();
 			if (info != null) {
 				helper.setAppId(info.getId());
 			}
 		}
 		if (getOperationType() == IDeploymentHelper.AUTO_DEPLOY) {
 			ApplicationInfo info = getAutoDeploySelection();
 			if (info != null) {
 				helper.setAppId(info.getId());
 			}
 		}
 		helper.setAppName(userAppName.getText());
 		helper.setIgnoreFailures(ignoreFailures.getSelection());
 		helper.setDefaultServer(isDefaultServer());
 		helper.setOperationType(getOperationType());
 		helper.setInstalledLocation(getInstalledLocation());
 		return helper;
 	}
 
 	public void setBaseURLEnabled(boolean value) {
 		baseUrl.setEnabled(value);
 	}
 
 	public void setDeployComboEnabled(boolean value) {
 		targetsCombo.setEnabled(value);
 		newTargetLink.setEnabled(value);
 	}
 
 	public void setUserAppNameEnabled(boolean value) {
 		userAppName.setEnabled(value);
 	}
 
 	public void setIgnoreFailuresEnabled(boolean value) {
 		ignoreFailures.setEnabled(value);
 	}
 
 	private int getOperationType() {
 		if (deployButton.getSelection()) {
 			return IDeploymentHelper.DEPLOY;
 		}
 		if (updateButton.getSelection()) {
 			return IDeploymentHelper.UPDATE;
 		}
 		if (autoDeployButton.getSelection()) {
 			return IDeploymentHelper.AUTO_DEPLOY;
 		}
 		return IDeploymentHelper.NO_ACTION;
 	}
 
 	private ApplicationInfo getUpdateSelection() {
 		if (context != null) {
 			int idx = updateCombo.getSelectionIndex();
 			if (idx <= -1) {
 				return null;
 			}
 			return applicationInfos[idx];
 		}
 		return null;
 	}
 
 	private ApplicationInfo getAutoDeploySelection() {
 		if (context != null) {
 			int idx = autoDeployCombo.getSelectionIndex();
 			if (idx <= -1) {
 				return null;
 			}
 			return applicationInfos[idx];
 		}
 		return null;
 	}
 
 	private URL getBaseURL() {
 		try {
 			URL result = new URL(baseUrl.getText());
 			return result;
 		} catch (MalformedURLException e) {
 			return null;
 		}
 	}
 
 	private boolean isDefaultServer() {
 		URL baseUrl = getBaseURL();
 		URL targetUrl = getTarget().getHost();
 		if (baseUrl.getHost().equals(targetUrl.getHost())) {
 			return true;
 		}
 		return false;
 	}
 
 	private IZendTarget getTarget() {
 		return targetsCombo.getSelected();
 	}
 
 	private String getInstalledLocation() {
 		if (autoDeploy && getOperationType() == IDeploymentHelper.AUTO_DEPLOY) {
 			int index = autoDeployCombo.getSelectionIndex();
 			if (index != -1) {
 				return applicationInfos[index].getInstalledLocation();
 			}
 		}
 		if (getOperationType() == IDeploymentHelper.UPDATE) {
 			int index = updateCombo.getSelectionIndex();
 			if (index != -1) {
 				return applicationInfos[index].getInstalledLocation();
 			}
 		}
 		return null;
 	}
 
 	private void createLocationLink(Composite container) {
 		newTargetLink = new Link(container, SWT.NONE);
 		String text = "<a>" + Messages.configurationPage_AddTarget + "</a>"; //$NON-NLS-1$//$NON-NLS-2$
 		newTargetLink.setText(text);
 		newTargetLink.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				AddTargetAction addTarget = new AddTargetAction();
 				addTarget.run();
 				IZendTarget newTarget = addTarget.getTarget();
 				if (newTarget != null) {
 					targetsCombo.updateItems();
 					targetsCombo.select(newTarget.getId());
 					validatePage();
 				}
 			}
 		});
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		gd.horizontalSpan = 2;
 		newTargetLink.setLayoutData(gd);
 	}
 
 	private void createDeployCombo(Composite container) {
 		targetsCombo.setLabel(Messages.configurationPage_DeployTo);
 		targetsCombo.setTooltip(Messages.configurationPage_DeployToTooltip);
 		targetsCombo.createControl(container);
 		targetsCombo.getCombo().addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				changeHost(targetsCombo.getSelected());
 				listener.statusChanged(validatePage());
 			}
 		});
 	}
 
 	private void createOperationsSection(Composite parent) {
 		Label operationLabel = new Label(parent, SWT.NONE);
 		operationLabel.setText(Messages.advancedSection_Label);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		operationLabel.setLayoutData(gd);
 
 		noActionButton = createRadioButton(parent, Messages.advancedSection_NoAction);
 		noActionButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				enableNoActionSection();
 				listener.statusChanged(validatePage());
 			}
 		});
 
 		deployButton = createRadioButton(parent, Messages.advancedSection_Deploy);
 		deployButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				enableDeploySection();
 				listener.statusChanged(validatePage());
 			}
 		});
 
 		updateButton = createRadioButton(parent, Messages.advancedSection_Update);
 		updateButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (updateButton.getSelection()) {
 					enableUpdateSection();
 					listener.statusChanged(validatePage());
 				}
 			}
 		});
 		targetsCombo.getCombo().addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (updateButton.getSelection()) {
 					getApplicationsInfo(updateCombo);
 				}
 				if (autoDeploy && autoDeployButton.getSelection()) {
 					getApplicationsInfo(autoDeployCombo);
 				}
 				listener.statusChanged(validatePage());
 			}
 		});
 
 		Composite updateComboComposite = new Composite(parent, SWT.NONE);
 		updateComboComposite.setLayout(new GridLayout(2, true));
 		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
 		gd.horizontalSpan = 2;
 		gd.horizontalIndent = 10;
 		updateComboComposite.setLayoutData(gd);
 		updateCombo = createLabelWithCombo(Messages.advancedSection_updateComboLabel,
 				Messages.advancedSection_updateComboTooltip,
 				updateComboComposite);
 		updateCombo.setEnabled(false);
 		updateCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				fillFieldsByAppInfo(updateCombo);
 				listener.statusChanged(validatePage());
 			}
 		});
 
 		if (autoDeploy) {
 			autoDeployButton = createRadioButton(parent, Messages.advancedSection_AutoDeploy);
 			autoDeployButton.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					if (autoDeployButton.getSelection()) {
 						enableAutoDeploySection();
 						listener.statusChanged(validatePage());
 					}
 				}
 			});
 
 			Composite autoDeployComboComposite = new Composite(parent, SWT.NONE);
 			autoDeployComboComposite.setLayout(new GridLayout(2, true));
 			gd = new GridData(SWT.FILL, SWT.FILL, true, false);
 			gd.horizontalSpan = 2;
 			gd.horizontalIndent = 10;
 			autoDeployComboComposite.setLayoutData(gd);
 			autoDeployCombo = createLabelWithCombo(Messages.advancedSection_autoDeployComboLabel,
 					Messages.advancedSection_autoDeployComboTooltip, autoDeployComboComposite);
 			autoDeployCombo.setEnabled(false);
 			autoDeployCombo.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					fillFieldsByAppInfo(autoDeployCombo);
 					listener.statusChanged(validatePage());
 				}
 			});
 		}
 	}
 
 	private void fillFieldsByAppInfo(Combo combo) {
 		int index = combo.getSelectionIndex();
 		if (index != -1) {
 			ApplicationInfo info = applicationInfos[index];
 			IDeploymentHelper helper = new DeploymentHelper();
 			try {
 				helper.setInstalledLocation(info.getInstalledLocation());
 				helper.setOperationType(getOperationType());
 				helper.setAppId(info.getId());
 				helper.setAppName(info.getUserAppName());
 				URL baseURL = new URL(info.getBaseUrl());
 				if (baseURL.getHost().equals(IDeploymentHelper.DEFAULT_SERVER)) {
 					helper.setDefaultServer(true);
 					IZendTarget target = getTarget();
 					URL updatedURL = new URL(baseURL.getProtocol(), target.getHost().getHost(),
 							baseURL.getPath());
 					helper.setBaseURL(updatedURL.toString());
 				} else {
 					helper.setDefaultServer(false);
 					helper.setBaseURL(baseURL.toString());
 				}
 				if (currentAppId != helper.getAppId()) {
 					initializeFields(helper);
 				}
 				setBaseURLEnabled(false);
 			} catch (MalformedURLException e) {
 				Activator.log(e);
 			}
 		}
 	}
 
 	private Button createRadioButton(Composite container, String label) {
 		Button button = new Button(container, SWT.RADIO);
 		button.setText(label);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		gd.horizontalIndent = 8;
 		button.setLayoutData(gd);
 		return button;
 	}
 
 	private void enableDeploySection() {
 		updateCombo.setEnabled(false);
 		IDeploymentHelper helper = new DeploymentHelper();
 		helper.setOperationType(getOperationType());
 		initializeFields(helper);
 		setBaseURLEnabled(true);
 		setUserAppNameEnabled(true);
 		ignoreFailures.setEnabled(true);
 	}
 
 	private void enableNoActionSection() {
 		setBaseURLEnabled(false);
 		setUserAppNameEnabled(false);
 		autoDeployCombo.setEnabled(false);
 		updateCombo.setEnabled(false);
 		ignoreFailures.setEnabled(false);
 	}
 
 	private void enableUpdateSection() {
 		setBaseURLEnabled(false);
 		setUserAppNameEnabled(false);
 		autoDeployCombo.setEnabled(false);
 		getApplicationsInfo(updateCombo);
 		updateCombo.setEnabled(true);
 		ignoreFailures.setEnabled(true);
 	}
 
 	private void enableAutoDeploySection() {
 		setBaseURLEnabled(false);
 		setUserAppNameEnabled(false);
 		updateCombo.setEnabled(false);
 		getApplicationsInfo(autoDeployCombo);
 		autoDeployCombo.setEnabled(true);
 		ignoreFailures.setEnabled(true);
 	}
 
 	private void getApplicationsInfo(Combo combo) {
 		final IZendTarget selectedTarget = getTarget();
 		if (selectedTarget != null && context != null) {
 			try {
 				context.run(true, false, new IRunnableWithProgress() {
 					public void run(IProgressMonitor monitor) {
 						StatusChangeListener listener = new StatusChangeListener(monitor);
 						ZendApplication app = new ZendApplication(new EclipseMappingModelLoader());
 						app.addStatusChangeListener(listener);
 						applicationInfos = new ApplicationInfo[0];
 						ApplicationsList info = app.getStatus(selectedTarget.getId());
 						org.zend.sdklib.library.IStatus status = listener.getStatus();
 						StatusCode code = status.getCode();
 						if (code == StatusCode.ERROR) {
 							StatusManager.getManager().handle(new SdkStatus(status),
 									StatusManager.SHOW);
 						} else {
 							setApplicationsInfos(info);
 						}
 					}
 				});
 				populateApplicationsList(combo);
 			} catch (InvocationTargetException e) {
 				Activator.log(e);
 			} catch (InterruptedException e) {
 				Activator.log(e);
 			}
 		}
 	}
 
 	private void setApplicationsInfos(ApplicationsList list) {
 		if (list != null && list.getApplicationsInfo() != null) {
 			applicationInfos = list.getApplicationsInfo().toArray(new ApplicationInfo[0]);
 		}
 	}
 
 	private void populateApplicationsList(final Combo combo) {
 		Display.getDefault().syncExec(new Runnable() {
 
 			public void run() {
 				combo.removeAll();
 				if (applicationInfos.length != 0) {
 					for (ApplicationInfo info : applicationInfos) {
 						combo.add(info.getAppName() + " (name: " + info.getUserAppName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 				}
 				if (currentAppId != -1 && combo.getItemCount() > 0) {
 					if (getOperationType() == IDeploymentHelper.UPDATE
 							|| getOperationType() == IDeploymentHelper.AUTO_DEPLOY) {
 						for (int i = 0; i < applicationInfos.length; i++) {
 							if (applicationInfos[i].getId() == currentAppId) {
 								combo.select(i);
 								fillFieldsByAppInfo(combo);
 							}
 						}
 					} else {
 						combo.select(0);
 						fillFieldsByAppInfo(combo);
 					}
 				}
 			}
 		});
 	}
 
 	private void changeHost(IZendTarget target) {
 		URL targetHost = target.getHost();
 		URL oldUrl = getBaseURL();
 		try {
 			URL updatedUrl = new URL(targetHost.getProtocol(), targetHost.getHost(),
 					targetHost.getPort(), oldUrl.getFile());
 			baseUrl.setText(updatedUrl.toString());
 		} catch (MalformedURLException e) {
 			Activator.log(e);
 		}
 	}
 
 	private boolean isAutoDeployAvailable() {
 		IConfigurationElement[] config = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(Activator.AUTO_DEPLOY_EXTENSION_ID);
 		try {
 			for (IConfigurationElement e : config) {
 
 				final Object o = e.createExecutableExtension("class"); //$NON-NLS-1$
 				if (o instanceof AbstractLaunchJob) {
 					return true;
 				}
 			}
 		} catch (CoreException e) {
 			Activator.log(e);
 			return false;
 		}
 		return false;
 	}
 
 }
