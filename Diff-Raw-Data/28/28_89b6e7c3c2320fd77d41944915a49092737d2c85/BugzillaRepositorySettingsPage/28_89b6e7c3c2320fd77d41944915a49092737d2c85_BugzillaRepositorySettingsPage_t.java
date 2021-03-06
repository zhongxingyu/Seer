 /*******************************************************************************
  * Copyright (c) 2004, 2010 Tasktop Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tasktop Technologies - initial API and implementation
  *     Red Hat Inc. - fixes for bug 259291
  *******************************************************************************/
 
 package org.eclipse.mylyn.internal.bugzilla.ui.tasklist;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
 import org.eclipse.mylyn.commons.net.AuthenticationType;
 import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
 import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClientFactory;
 import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
 import org.eclipse.mylyn.internal.bugzilla.core.BugzillaLanguageSettings;
 import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
 import org.eclipse.mylyn.internal.bugzilla.core.BugzillaStatus;
 import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
 import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
 import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
 import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
 import org.eclipse.mylyn.internal.tasks.core.RepositoryTemplateManager;
 import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
 import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
 import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
 import org.eclipse.mylyn.tasks.core.RepositoryStatus;
 import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.ui.TasksUi;
 import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author Mik Kersten
  * @author Rob Elves
  * @author Charley Wang
  */
 public class BugzillaRepositorySettingsPage extends AbstractRepositorySettingsPage {
 
 	private static final String TOOLTIP_AUTODETECTION_ENABLED = Messages.BugzillaRepositorySettingsPage_override_auto_detection_of_platform;
 
 	private static final String TOOLTIP_AUTODETECTION_DISABLED = Messages.BugzillaRepositorySettingsPage_available_once_repository_created;
 
 	private static final String LABEL_SHORT_LOGINS = Messages.BugzillaRepositorySettingsPage_local_users_enabled;
 
 	private static final String LABEL_VERSION_NUMBER = "3.0 - 3.6"; //$NON-NLS-1$
 
 	private static final String TITLE = Messages.BugzillaRepositorySettingsPage_bugzilla_repository_settings;
 
 	private static final String DESCRIPTION = MessageFormat.format(
 			Messages.BugzillaRepositorySettingsPage_supports_bugzilla_X, LABEL_VERSION_NUMBER)
 			+ Messages.BugzillaRepositorySettingsPage_example_do_not_include;
 
 	protected Button autodetectPlatformOS;
 
 	protected Combo defaultPlatformCombo;
 
 	protected Combo defaultOSCombo;
 
 	protected Text descriptorFile;
 
 	private Button cleanQAContact;
 
 	private RepositoryConfiguration repositoryConfiguration = null;
 
 	private String platform = null;
 
 	private String os = null;
 
 	private Combo languageSettingCombo;
 
 	private Button useXMLRPCstatusTransitions;
 
 	private Button useclassification;
 
 	private Button usetargetmilestone;
 
 	private Button useqacontact;
 
 	private Button usestatuswhiteboard;
 
 	private Button usebugaliases;
 
 	private Button use_see_also;
 
 	public BugzillaRepositorySettingsPage(TaskRepository taskRepository) {
 		super(TITLE, DESCRIPTION, taskRepository);
 		setNeedsAnonymousLogin(true);
 		setNeedsEncoding(true);
 		setNeedsTimeZone(false);
 		setNeedsHttpAuth(true);
 	}
 
 	@Override
 	protected void repositoryTemplateSelected(RepositoryTemplate template) {
 		repositoryLabelEditor.setStringValue(template.label);
 		setUrl(template.repositoryUrl);
 		// setAnonymous(info.anonymous);
 		if (template.characterEncoding != null) {
 			setEncoding(template.characterEncoding);
 		}
 		boolean value = Boolean.parseBoolean(template.getAttribute("useclassification")); //$NON-NLS-1$
 		useclassification.setSelection(value);
 		value = Boolean.parseBoolean(template.getAttribute("usetargetmilestone")); //$NON-NLS-1$
 		usetargetmilestone.setSelection(value);
 		value = Boolean.parseBoolean(template.getAttribute("useqacontact")); //$NON-NLS-1$
 		useqacontact.setSelection(value);
 		value = Boolean.parseBoolean(template.getAttribute("usestatuswhiteboard")); //$NON-NLS-1$
 		usestatuswhiteboard.setSelection(value);
 		value = Boolean.parseBoolean(template.getAttribute("usebugaliases")); //$NON-NLS-1$
 		usebugaliases.setSelection(value);
 		value = Boolean.parseBoolean(template.getAttribute("use_see_also")); //$NON-NLS-1$
 		use_see_also.setSelection(value);
 
 		getContainer().updateButtons();
 
 	}
 
 	@Override
 	protected void createAdditionalControls(Composite parent) {
 		addRepositoryTemplatesToServerUrlCombo();
 		Label shortLoginLabel = new Label(parent, SWT.NONE);
 		shortLoginLabel.setText(LABEL_SHORT_LOGINS);
 		cleanQAContact = new Button(parent, SWT.CHECK | SWT.LEFT);
 		if (repository != null) {
 			boolean shortLogin = Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN));
 			cleanQAContact.setSelection(shortLogin);
 		}
 
 		if (null != repository) {
 			BugzillaRepositoryConnector connector = (BugzillaRepositoryConnector) TasksUi.getRepositoryConnector(repository.getConnectorKind());
 			repositoryConfiguration = connector.getRepositoryConfiguration(repository.getRepositoryUrl());
 			platform = repository.getProperty(IBugzillaConstants.BUGZILLA_DEF_PLATFORM);
 			os = repository.getProperty(IBugzillaConstants.BUGZILLA_DEF_OS);
 		}
 
 		Label defaultPlatformLabel = new Label(parent, SWT.NONE);
 		defaultPlatformLabel.setText(Messages.BugzillaRepositorySettingsPage_AUTOTETECT_PLATFORM_AND_OS);
 		if (null == repository) {
 			defaultPlatformLabel.setToolTipText(TOOLTIP_AUTODETECTION_DISABLED);
 		} else {
 			defaultPlatformLabel.setToolTipText(TOOLTIP_AUTODETECTION_ENABLED);
 		}
 
 		Composite platformOSContainer = new Composite(parent, SWT.NONE);
 		GridLayout gridLayout = new GridLayout(3, false);
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		platformOSContainer.setLayout(gridLayout);
 
 		autodetectPlatformOS = new Button(platformOSContainer, SWT.CHECK);
 		autodetectPlatformOS.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (autodetectPlatformOS.isEnabled() && repositoryConfiguration == null
 						&& !autodetectPlatformOS.getSelection()) {
 					try {
 						getWizard().getContainer().run(true, false, new IRunnableWithProgress() {
 
 							public void run(IProgressMonitor monitor) throws InvocationTargetException,
 									InterruptedException {
 								try {
 									monitor.beginTask(
 											Messages.BugzillaRepositorySettingsPage_Retrieving_repository_configuration,
 											IProgressMonitor.UNKNOWN);
 									BugzillaRepositoryConnector connector = (BugzillaRepositoryConnector) TasksUi.getRepositoryConnector(repository.getConnectorKind());
 									repositoryConfiguration = connector.getRepositoryConfiguration(repository, false,
 											monitor);
 									if (repositoryConfiguration != null) {
 										platform = repository.getProperty(IBugzillaConstants.BUGZILLA_DEF_PLATFORM);
 										os = repository.getProperty(IBugzillaConstants.BUGZILLA_DEF_OS);
 										PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 
 											public void run() {
 												populatePlatformCombo();
 												populateOsCombo();
 											}
 										});
 									}
 								} catch (CoreException e) {
 									throw new InvocationTargetException(e);
 								} finally {
 									monitor.done();
 								}
 
 							}
 
 						});
 					} catch (InvocationTargetException e1) {
 						if (e1.getCause() != null) {
 							setErrorMessage(e1.getCause().getMessage());
 						}
 					} catch (InterruptedException e1) {
 						// ignore
 					}
 				}
 				defaultPlatformCombo.setEnabled(!autodetectPlatformOS.getSelection());
 				defaultOSCombo.setEnabled(!autodetectPlatformOS.getSelection());
 			}
 
 		});
 		autodetectPlatformOS.setEnabled(null != repository);
 		if (null == repository) {
 			autodetectPlatformOS.setToolTipText(TOOLTIP_AUTODETECTION_DISABLED);
 		} else {
 			autodetectPlatformOS.setToolTipText(TOOLTIP_AUTODETECTION_ENABLED);
 		}
 		autodetectPlatformOS.setSelection(null == platform && null == os);
 
 		defaultPlatformCombo = new Combo(platformOSContainer, SWT.READ_ONLY);
 		populatePlatformCombo();
 
 		defaultOSCombo = new Combo(platformOSContainer, SWT.READ_ONLY);
 		populateOsCombo();
 
 		new Label(parent, SWT.NONE).setText(Messages.BugzillaRepositorySettingsPage_Language_);
 		languageSettingCombo = new Combo(parent, SWT.DROP_DOWN);
 
 		Label xmlrpc = new Label(parent, SWT.NONE);
 		xmlrpc.setText(Messages.BugzillaRepositorySettingsPage_AutodetectWorkflow);
 		xmlrpc.setToolTipText(Messages.BugzillaRepositorySettingsPage_RequiresBugzilla3_6);
 		useXMLRPCstatusTransitions = new Button(parent, SWT.CHECK | SWT.LEFT);
 		useXMLRPCstatusTransitions.setText(Messages.BugzillaRepositorySettingsPage_UseXmlRpc);
 
 		Label descriptorLabel = new Label(parent, SWT.NONE);
 		descriptorLabel.setText(Messages.BugzillaRepositorySettingsPage_descriptor_file);
 		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(descriptorLabel);
 
 		Composite descriptorComposite = new Composite(parent, SWT.NONE);
 		gridLayout = new GridLayout(2, false);
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		descriptorComposite.setLayout(gridLayout);
 		GridDataFactory.fillDefaults()
 				.grab(true, false)
 				.align(SWT.FILL, SWT.BEGINNING)
 				.hint(200, SWT.DEFAULT)
 				.applyTo(descriptorComposite);
 
 		descriptorFile = new Text(descriptorComposite, SWT.BORDER);
 		GridDataFactory.fillDefaults()
 				.grab(true, false)
 				.align(SWT.LEFT, SWT.CENTER)
 				.hint(200, SWT.DEFAULT)
 				.applyTo(descriptorFile);
 
 		Button browseDescriptor = new Button(descriptorComposite, SWT.PUSH);
 		browseDescriptor.setText(Messages.BugzillaRepositorySettingsPage_Browse_descriptor);
 		browseDescriptor.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog fd = new FileDialog(new Shell());
 				fd.setText(Messages.BugzillaRepositorySettingsPage_SelectDescriptorFile);
 				String dFile = fd.open();
 				if (dFile != null && dFile.length() > 0) {
 					descriptorFile.setText(dFile);
 					isPageComplete();
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 
 			}
 		});
 		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(browseDescriptor);
 		descriptorFile.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent e) {
 				if (getWizard() != null) {
 					getWizard().getContainer().updateButtons();
 				}
 			}
 		});
 
 		for (BugzillaLanguageSettings bugzillaLanguageSettings : BugzillaRepositoryConnector.getLanguageSettings()) {
 			languageSettingCombo.add(bugzillaLanguageSettings.getLanguageName());
 		}
 		if (repository != null) {
 			//Set language selection
 			String language = repository.getProperty(IBugzillaConstants.BUGZILLA_LANGUAGE_SETTING);
 			if (language != null && !language.equals("") && languageSettingCombo.indexOf(language) >= 0) { //$NON-NLS-1$
 				languageSettingCombo.select(languageSettingCombo.indexOf(language));
 			}
 
 			//Set descriptor file
 			if (descriptorFile != null) {
 				String file = repository.getProperty((IBugzillaConstants.BUGZILLA_DESCRIPTOR_FILE));
 				if (file != null) {
 					descriptorFile.setText(file);
 				}
 			}
 		}
 		if (languageSettingCombo.getSelectionIndex() == -1) {
 			if (languageSettingCombo.indexOf(IBugzillaConstants.DEFAULT_LANG) >= 0) {
 				languageSettingCombo.select(languageSettingCombo.indexOf(IBugzillaConstants.DEFAULT_LANG));
 			}
 		}
 		Group adminGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
 		adminGroup.setLayout(new GridLayout(3, false));
 		adminGroup.setText(Messages.BugzillaRepositorySettingsPage_admin_parameter);
 		GridDataFactory.fillDefaults()
 				.grab(true, false)
 				.align(SWT.BEGINNING, SWT.CENTER)
 				.span(2, 1)
 				.applyTo(adminGroup);
 
 		useclassification = new Button(adminGroup, SWT.CHECK | SWT.LEFT);
 		useclassification.setText(Messages.BugzillaRepositorySettingsPage_useclassification);
 		usetargetmilestone = new Button(adminGroup, SWT.CHECK | SWT.LEFT);
 		usetargetmilestone.setText(Messages.BugzillaRepositorySettingsPage_usetargetmilestone);
 		useqacontact = new Button(adminGroup, SWT.CHECK | SWT.LEFT);
 		useqacontact.setText(Messages.BugzillaRepositorySettingsPage_useqacontact);
 		usestatuswhiteboard = new Button(adminGroup, SWT.CHECK | SWT.LEFT);
 		usestatuswhiteboard.setText(Messages.BugzillaRepositorySettingsPage_usestatuswhiteboard);
 		usebugaliases = new Button(adminGroup, SWT.CHECK | SWT.LEFT);
 		usebugaliases.setText(Messages.BugzillaRepositorySettingsPage_usebugaliases);
 		use_see_also = new Button(adminGroup, SWT.CHECK | SWT.LEFT);
 		use_see_also.setText(Messages.BugzillaRepositorySettingsPage_use_see_also);
 		if (repository != null) {
 			RepositoryTemplate myTemplate = null;
 			if (repository.getProperty(IBugzillaConstants.BUGZILLA_PARAM_USECLASSIFICATION) == null) {
 				final RepositoryTemplateManager templateManager = TasksUiPlugin.getRepositoryTemplateManager();
 				for (RepositoryTemplate template : templateManager.getTemplates(connector.getConnectorKind())) {
 					if (repository.getRepositoryLabel().equals(template.label)) {
 						myTemplate = template;
 						break;
 					}
 				}
 				if (myTemplate != null) {
 					// we have an Template but no values in the Repository so we use the Template values
 					boolean value = Boolean.parseBoolean(myTemplate.getAttribute("useclassification")); //$NON-NLS-1$
 					useclassification.setSelection(!value);
 					value = Boolean.parseBoolean(myTemplate.getAttribute("usetargetmilestone")); //$NON-NLS-1$
 					usetargetmilestone.setSelection(!value);
 					value = Boolean.parseBoolean(myTemplate.getAttribute("useqacontact")); //$NON-NLS-1$
 					useqacontact.setSelection(!value);
 					value = Boolean.parseBoolean(myTemplate.getAttribute("usestatuswhiteboard")); //$NON-NLS-1$
 					usestatuswhiteboard.setSelection(!value);
 					value = Boolean.parseBoolean(myTemplate.getAttribute("usebugaliases")); //$NON-NLS-1$
 					usebugaliases.setSelection(!value);
 					value = Boolean.parseBoolean(myTemplate.getAttribute("use_see_also")); //$NON-NLS-1$
 					use_see_also.setSelection(!value);
 
 					value = Boolean.parseBoolean(myTemplate.getAttribute("useXMLRPC")); //$NON-NLS-1$
 					useXMLRPCstatusTransitions.setSelection(value);
 				} else {
 					useclassification.setSelection(true);
 					usetargetmilestone.setSelection(false);
 					useqacontact.setSelection(false);
 					usestatuswhiteboard.setSelection(false);
 					usebugaliases.setSelection(true);
 					use_see_also.setSelection(true);
 					useXMLRPCstatusTransitions.setSelection(false);
 				}
 			} else {
 				// we use the repository values
 				boolean value = Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.BUGZILLA_PARAM_USECLASSIFICATION));
 				useclassification.setSelection(!value);
 				value = Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.BUGZILLA_PARAM_USETARGETMILESTONE));
 				usetargetmilestone.setSelection(!value);
 				value = Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.BUGZILLA_PARAM_USEQACONTACT));
 				useqacontact.setSelection(!value);
 				value = Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.BUGZILLA_PARAM_USESTATUSWHITEBOARD));
 				usestatuswhiteboard.setSelection(!value);
 				value = Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.BUGZILLA_PARAM_USEBUGALIASES));
 				usebugaliases.setSelection(!value);
 				value = Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.BUGZILLA_PARAM_USE_SEE_ALSO));
 				use_see_also.setSelection(!value);
 
 				value = Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.BUGZILLA_USE_XMLRPC));
 				useXMLRPCstatusTransitions.setSelection(value);
 			}
 		}
 	}
 
 	private void populateOsCombo() {
 		if (null != repositoryConfiguration && defaultOSCombo != null) {
 			defaultOSCombo.removeAll();
 			List<String> optionValues = repositoryConfiguration.getOSs();
 			for (String option : optionValues) {
 				defaultOSCombo.add(option.toString());
 			}
 			if (null != os && defaultOSCombo.indexOf(os) >= 0) {
 				defaultOSCombo.select(defaultOSCombo.indexOf(os));
 			} else {
 				// remove value if no longer exists and set to All!
 				repository.removeProperty(IBugzillaConstants.BUGZILLA_DEF_OS);
 				defaultOSCombo.select(0);
 			}
 		} else {
 			defaultOSCombo.add(Messages.BugzillaRepositorySettingsPage_All);
 			defaultOSCombo.select(0);
 		}
 		defaultOSCombo.getParent().pack(true);
 		defaultOSCombo.setEnabled(!autodetectPlatformOS.getSelection());
 	}
 
 	private void populatePlatformCombo() {
 		if (null != repositoryConfiguration && defaultPlatformCombo != null) {
 			defaultPlatformCombo.removeAll();
 			List<String> optionValues = repositoryConfiguration.getPlatforms();
 			for (String option : optionValues) {
 				defaultPlatformCombo.add(option.toString());
 			}
 			if (null != platform && defaultPlatformCombo.indexOf(platform) >= 0) {
 				defaultPlatformCombo.select(defaultPlatformCombo.indexOf(platform));
 			} else {
 				// remove value if no longer exists and set to All!
 				repository.removeProperty(IBugzillaConstants.BUGZILLA_DEF_PLATFORM);
 				defaultPlatformCombo.select(0);
 			}
 		} else {
 			defaultPlatformCombo.add(Messages.BugzillaRepositorySettingsPage_All);
 			defaultPlatformCombo.select(0);
 		}
 		defaultPlatformCombo.getParent().pack(true);
 		defaultPlatformCombo.setEnabled(!autodetectPlatformOS.getSelection());
 	}
 
 	@SuppressWarnings({ "restriction" })
 	@Override
 	public void applyTo(final TaskRepository repository) {
 		AuthenticationCredentials repositoryAuth = repository.getCredentials(AuthenticationType.REPOSITORY);
 		AuthenticationCredentials httpAuth = repository.getCredentials(AuthenticationType.HTTP);
 		AuthenticationCredentials proxyAuth = repository.getCredentials(AuthenticationType.PROXY);
 		boolean changed = repository.getCharacterEncoding() != getCharacterEncoding();
 		changed = changed || repository.getSavePassword(AuthenticationType.REPOSITORY) != getSavePassword();
 		changed = changed || repositoryAuth.getUserName().compareTo(getUserName()) != 0;
 		changed = changed || repositoryAuth.getPassword().compareTo(getPassword()) != 0;
 		changed = changed
 				|| Boolean.parseBoolean(repository.getProperty(IBugzillaConstants.BUGZILLA_USE_XMLRPC)) != useXMLRPCstatusTransitions.getSelection();
		changed = changed
				|| !equals(repository.getProperty(IBugzillaConstants.BUGZILLA_DESCRIPTOR_FILE),
						descriptorFile.getText());
 		if (httpAuth != null) {
 			changed = changed || httpAuth.getUserName().compareTo(getHttpAuthUserId()) != 0
 					|| httpAuth.getPassword().compareTo(getHttpAuthPassword()) != 0
					|| !equals(repository.getProperty(TaskRepository.PROXY_HOSTNAME), getProxyHostname())
					|| !equals(repository.getProperty(TaskRepository.PROXY_PORT), getProxyPort());
 		}
 		if (proxyAuth != null) {
 			changed = changed || proxyAuth.getUserName().compareTo(getProxyUserName()) != 0
 					|| proxyAuth.getPassword().compareTo(getProxyPassword()) != 0;
 		}
 		applyToInternal(repository);
 		if (changed) {
 			final String jobName = MessageFormat.format(
 					Messages.BugzillaRepositorySettingsPage_Updating_repository_configuration_for_X,
 					repository.getRepositoryUrl());
 			Job updateJob = new Job(jobName) {
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					monitor.beginTask(jobName, IProgressMonitor.UNKNOWN);
 					try {
 						performUpdate(repository, connector, monitor);
 					} finally {
 						monitor.done();
 					}
 					return Status.OK_STATUS;
 				}
 			};
 			// show the progress in the system task bar if this is a user job (i.e. forced)
 			updateJob.setProperty(WorkbenchUtil.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
 			updateJob.setUser(true);
 			updateJob.schedule();
 
 		}
 	}
 
	/**
	 * Treats null as equal to the empty string
	 */
	private boolean equals(String s1, String s2) {
		if (s1 == null) {
			s1 = "";
		}
		if (s2 == null) {
			s2 = "";
		}
		return s1.equals(s2);
	}

 	public void performUpdate(final TaskRepository repository, final AbstractRepositoryConnector connector,
 			IProgressMonitor monitor) {
 		try {
 			connector.updateRepositoryConfiguration(repository, monitor);
 		} catch (final CoreException e) {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					TasksUiInternal.displayStatus(
 							Messages.BugzillaRepositorySettingsPage_Error_updating_repository_configuration,
 							e.getStatus());
 				}
 			});
 		}
 	}
 
 	@Override
 	protected boolean isValidUrl(String url) {
 		return BugzillaClient.isValidUrl(url);
 	}
 
 	@Override
 	protected Validator getValidator(TaskRepository repository) {
 		return new BugzillaValidator(repository);
 	}
 
 	public class BugzillaValidator extends Validator {
 
 		final TaskRepository repository;
 
 		public BugzillaValidator(TaskRepository repository) {
 			this.repository = repository;
 		}
 
 		@Override
 		public void run(IProgressMonitor monitor) throws CoreException {
 			try {
 				validate(monitor);
 			} catch (OperationCanceledException e) {
 				throw e;
 			} catch (Exception e) {
 				displayError(repository.getRepositoryUrl(), e);
 			}
 		}
 
 		private void displayError(final String serverUrl, Throwable e) {
 			IStatus status;
 			if (e instanceof MalformedURLException) {
 				status = new BugzillaStatus(IStatus.WARNING, BugzillaCorePlugin.ID_PLUGIN,
 						RepositoryStatus.ERROR_NETWORK, Messages.BugzillaRepositorySettingsPage_Server_URL_is_invalid);
 			} else if (e instanceof CoreException) {
 				status = ((CoreException) e).getStatus();
 			} else if (e instanceof IOException) {
 				status = new BugzillaStatus(IStatus.WARNING, BugzillaCorePlugin.ID_PLUGIN, RepositoryStatus.ERROR_IO,
 						serverUrl, e.getMessage());
 			} else {
 				status = new BugzillaStatus(IStatus.WARNING, BugzillaCorePlugin.ID_PLUGIN,
 						RepositoryStatus.ERROR_NETWORK, serverUrl, e.getMessage());
 			}
 			setStatus(status);
 		}
 
 		public void validate(IProgressMonitor monitor) throws IOException, CoreException {
 
 			if (monitor == null) {
 				monitor = new NullProgressMonitor();
 			}
 			try {
 				monitor.beginTask(Messages.BugzillaRepositorySettingsPage_Validating_server_settings,
 						IProgressMonitor.UNKNOWN);
 				BugzillaClient client = null;
 
 				BugzillaRepositoryConnector connector = (BugzillaRepositoryConnector) TasksUi.getRepositoryConnector(repository.getConnectorKind());
 				client = BugzillaClientFactory.createClient(repository, connector);
 				client.validate(monitor);
 			} finally {
 				monitor.done();
 			}
 		}
 
 	}
 
 	@Override
 	public String getConnectorKind() {
 		return BugzillaCorePlugin.CONNECTOR_KIND;
 	}
 
 	@Override
 	public boolean canValidate() {
 		// need to invoke isPageComplete() to trigger message update
 		return isPageComplete() && (getMessage() == null || getMessageType() != IMessageProvider.ERROR);
 	}
 
 	@Override
 	public boolean isPageComplete() {
 		if (descriptorFile != null) {
 			String descriptorFilePath = descriptorFile.getText();
 			if (descriptorFilePath != null && !descriptorFilePath.equals("")) { //$NON-NLS-1$
 				File testFile = new File(descriptorFilePath);
 				if (!testFile.exists()) {
 					setMessage(Messages.BugzillaRepositorySettingsPage_DescriptorFileNotExists, IMessageProvider.ERROR);
 					return false;
 				}
 			}
 		}
 		return super.isPageComplete();
 	}
 
 	private void applyToInternal(final TaskRepository repository) {
 		super.applyTo(repository);
 		repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY, IRepositoryConstants.CATEGORY_BUGS);
 		repository.setProperty(IBugzillaConstants.REPOSITORY_SETTING_SHORT_LOGIN,
 				String.valueOf(cleanQAContact.getSelection()));
 		repository.setProperty(IBugzillaConstants.BUGZILLA_LANGUAGE_SETTING, languageSettingCombo.getText());
 		repository.setProperty(IBugzillaConstants.BUGZILLA_USE_XMLRPC,
 				Boolean.toString(useXMLRPCstatusTransitions.getSelection()));
 		repository.setProperty(IBugzillaConstants.BUGZILLA_DESCRIPTOR_FILE, descriptorFile.getText());
 		if (!autodetectPlatformOS.getSelection()) {
 			repository.setProperty(IBugzillaConstants.BUGZILLA_DEF_PLATFORM,
 					String.valueOf(defaultPlatformCombo.getItem(defaultPlatformCombo.getSelectionIndex())));
 			repository.setProperty(IBugzillaConstants.BUGZILLA_DEF_OS,
 					String.valueOf(defaultOSCombo.getItem(defaultOSCombo.getSelectionIndex())));
 		} else {
 			repository.removeProperty(IBugzillaConstants.BUGZILLA_DEF_PLATFORM);
 			repository.removeProperty(IBugzillaConstants.BUGZILLA_DEF_OS);
 		}
 		repository.setProperty(IBugzillaConstants.BUGZILLA_PARAM_USECLASSIFICATION,
 				Boolean.toString(!useclassification.getSelection()));
 		repository.setProperty(IBugzillaConstants.BUGZILLA_PARAM_USETARGETMILESTONE,
 				Boolean.toString(!usetargetmilestone.getSelection()));
 		repository.setProperty(IBugzillaConstants.BUGZILLA_PARAM_USEQACONTACT,
 				Boolean.toString(!useqacontact.getSelection()));
 		repository.setProperty(IBugzillaConstants.BUGZILLA_PARAM_USESTATUSWHITEBOARD,
 				Boolean.toString(!usestatuswhiteboard.getSelection()));
 		repository.setProperty(IBugzillaConstants.BUGZILLA_PARAM_USEBUGALIASES,
 				Boolean.toString(!usebugaliases.getSelection()));
 		repository.setProperty(IBugzillaConstants.BUGZILLA_PARAM_USE_SEE_ALSO,
 				Boolean.toString(!use_see_also.getSelection()));
 	}
 
 	@Override
 	public TaskRepository createTaskRepository() {
 		TaskRepository repository = new TaskRepository(connector.getConnectorKind(), getRepositoryUrl());
 		applyToInternal(repository);
 		return repository;
 	}
 
 }
