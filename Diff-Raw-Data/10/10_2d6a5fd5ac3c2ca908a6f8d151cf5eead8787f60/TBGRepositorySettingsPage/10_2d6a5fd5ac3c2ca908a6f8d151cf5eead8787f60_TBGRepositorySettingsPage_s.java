 package com.undebugged.mylyn.tbg.ui.wizard;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.preference.StringFieldEditor;
 import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 import com.undebugged.mylyn.tbg.core.TBGCorePlugin;
 
 public class TBGRepositorySettingsPage extends AbstractRepositorySettingsPage {
 
 	protected static final String LABEL_SECURITYKEY = "Security Key";
 	protected StringFieldEditor repositorySecurityKeyEditor;
 	
 	public TBGRepositorySettingsPage(TaskRepository taskRepository) {
 		super("The Bug Genie Repository Settings", "Settings for The Bug Genie Repository", taskRepository);
 		setNeedsAnonymousLogin(false);
 		setNeedsEncoding(false);
 		setNeedsTimeZone(false);
 		setNeedsAdvanced(true);
 		setNeedsCertAuth(false);
 		setNeedsProxy(false);
 		setNeedsHttpAuth(false);
 		setNeedsValidation(false);
 		setNeedsValidateOnFinish(false);
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		super.createControl(parent);
 	}
 	
 	@Override
 	public String getConnectorKind() {
 		return TBGCorePlugin.CONNECTOR_KIND;
 	}
 
 	
 	/**
 	 * Returns true, if credentials are incomplete. Clients may override this method.
 	 * 
 	 * @since 3.4
 	 */
 	protected boolean isMissingCredentials() {
 		return repositoryUserNameEditor.getStringValue().trim().equals("") //$NON-NLS-1$
				|| repositorySecurityKeyEditor.getStringValue().trim().equals("")
 				|| (getSavePassword() && repositoryPasswordEditor.getStringValue().trim().equals("")); //$NON-NLS-1$
 	}
 	
 	
 	
 	@Override
 	protected Validator getValidator(TaskRepository repository) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected boolean isValidUrl(String url) {
 		if (url == null || url.trim().length() < 8) return false;
 		return super.isValidUrl(url);
 	}
 	
 	public String getSecurityKey() {
 		return repositorySecurityKeyEditor.getStringValue();
 	}
 	public void setSecurityKey(String key) {
 		repositorySecurityKeyEditor.setStringValue(key);
 	}
 	
 	
 	@Override
 	protected void repositoryTemplateSelected(RepositoryTemplate template) {
 		repositoryLabelEditor.setStringValue(template.label);
 		setUrl(template.repositoryUrl);
 		setUserId("username");
 		setPassword("password");
 		setSecurityKey("security-key");
 		getContainer().updateButtons();
 	}
 
 	@Override
 	public void applyTo(TaskRepository repository) {
 		super.applyTo(repository);
 		repository.setProperty(TBGCorePlugin.PROPERTY_SECURITYKEY, getSecurityKey());
 	}
 	
 	@Override
 	protected void createAdditionalControls(Composite parent) {
 		repositorySecurityKeyEditor = new StringFieldEditor("", LABEL_SECURITYKEY,  //$NON-NLS-1$
 				StringFieldEditor.UNLIMITED, parent) {
 
 			@Override
 			protected boolean doCheckState() {
 				return true;
 			}
 
 			@Override
 			protected void valueChanged() {
 				super.valueChanged();
 				isPageComplete();
 				if (getWizard() != null) {
 					getWizard().getContainer().updateButtons();
 				}
 			}
 
 			@Override
 			public int getNumberOfControls() {
 				return 2;
 			}
 		};
 		repositorySecurityKeyEditor.setTextLimit(40);
 		Label dummyLabel = new Label(compositeContainer, SWT.NONE); // dummy control to fill 3rd column when no anonymous login
 		GridDataFactory.fillDefaults().applyTo(dummyLabel); // not really necessary, but to be on the safe side
 
 		addRepositoryTemplatesToServerUrlCombo();
 	}
 }
