 package com.madalla.webapp.user;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.StringAutoCompleteRenderer;
 import org.apache.wicket.markup.html.JavascriptPackageResource;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.util.MapModel;
 import org.apache.wicket.util.string.Strings;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.validator.AbstractValidator;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 import org.springframework.beans.BeanUtils;
 
 import com.madalla.bo.SiteData;
 import com.madalla.bo.security.UserData;
 import com.madalla.bo.security.UserSiteData;
 import com.madalla.email.IEmailSender;
 import com.madalla.email.IEmailServiceProvider;
 import com.madalla.service.IDataService;
 import com.madalla.util.security.SecurityUtils;
 import com.madalla.webapp.css.Css;
 import com.madalla.webapp.panel.CmsPanel;
 import com.madalla.webapp.scripts.scriptaculous.Scriptaculous;
 import com.madalla.webapp.security.IAuthenticator;
 import com.madalla.wicket.form.AjaxValidationRequiredTextField;
 import com.madalla.wicket.form.AjaxValidationSubmitButton;
 import com.madalla.wicket.form.ValidationStyleBehaviour;
 
 public class UserAdminPanel extends CmsPanel {
 
 	private static final long serialVersionUID = 9027719184960390850L;
 	private static final Log log = LogFactory.getLog(UserAdminPanel.class);
 
 	//private UserDataView user = new UserDataView() ;
 	private boolean lockUsername = false;
 	private TextField<String> usernameField;
 	private List<SiteData> sites = new ArrayList<SiteData>() ;
 	private List<SiteData> sitesChoices ;
 
 	public class NewUserForm extends Form<UserDataView> {
 		private static final long serialVersionUID = 9033980585192727266L;
 
 		public NewUserForm(String id, IModel<UserDataView> model) {
 			super(id, model);
 			
 			//User Name Field
 			usernameField = new RequiredTextField<String>("name") {
 
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				protected void onBeforeRender() {
 					if (lockUsername) {
 						setEnabled(false);
 					} else {
 						setEnabled(true);
 					}
 					super.onBeforeRender();
 				}
 
 			};
 			usernameField.setOutputMarkupId(true);
 			IAutoCompleteRenderer<String> autoCompleteRenderer = StringAutoCompleteRenderer.instance();
 			usernameField.add(new AutoCompleteBehavior<String>(autoCompleteRenderer) {
 
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				protected Iterator<String> getChoices(String input) {
 					if (Strings.isEmpty(input)) {
 						List<String> s = Collections.emptyList();
 						return s.iterator();
 					}
 					List<String> choices = new ArrayList<String>(10);
 
 					List<UserData> list = getRepositoryService().getUsers();
 					for(UserData data : list){
 						if (data.getName().toLowerCase().startsWith(input.toLowerCase())){
 							choices.add(data.getName());
 							if (choices.size() == 10) {
 								break;
 							}
 						}
 					}
 					return choices.iterator();
 				}
 
 			});
 			
 			//Validation
 			usernameField.add(new AbstractValidator<String>(){
 				private static final long serialVersionUID = 1L;
 				@Override
 				protected void onValidate(IValidatable<String> validatable) {
 					String value = (String) validatable.getValue();
 					if (StringUtils.isEmpty(value)){
 						error(validatable,"error.required");
 					}else if (!StringUtils.isAlphanumeric(value)){
 						error(validatable,"error.alphanumeric");
 					} else {
 						if (value.length() <= 4){
 							error(validatable, "error.length");
 						}
 					}
 				}
 				
 			});
 			
 			add(usernameField);
 		}
         
 
         
 	}
 	
 	
 
 	public class ProfileForm extends Form<UserDataView> {
 		private static final long serialVersionUID = -2684823497770522924L;
 
 		public ProfileForm(String id, IModel<UserDataView> model) {
 			super(id, model);
 
 			FeedbackPanel emailFeedback = new FeedbackPanel("emailFeedback");
 			add(emailFeedback);
 			TextField<String> email = new AjaxValidationRequiredTextField("email", emailFeedback);
 			email.add(new ValidationStyleBehaviour());
 			email.add(EmailAddressValidator.getInstance());
 			add(email);
 
			add(new TextField<String>("firstName"));
			add(new TextField<String>("lastName"));
			add(new CheckBox("admin"));
			add(new CheckBox("requiresAuth"));
 			sitesChoices = getRepositoryService().getSiteEntries();
 			add(new CheckBoxMultipleChoice<SiteData>("site", getSitesModel() ,
					sitesChoices, new ChoiceRenderer<SiteData>("name")));
 		}
 
 	}
 
 	public UserAdminPanel(String id) {
 
 		super(id);
 
 		add(JavascriptPackageResource.getHeaderContribution(Scriptaculous.PROTOTYPE));
 		add(Css.CSS_FORM);
 
 		final UserDataView user = new UserDataView();
 		
 		final Form<UserDataView> userForm = new NewUserForm("userForm",new CompoundPropertyModel<UserDataView>(user) );
 		add(userForm);
 
 		final FeedbackPanel userFeedback = new ComponentFeedbackPanel("formFeedback", userForm);
 		userFeedback.setOutputMarkupId(true);
 		userForm.add(userFeedback);
 
 		// User edit form
 		final Form<UserDataView> profileForm = new ProfileForm("profileForm", new CompoundPropertyModel<UserDataView>(user));
 		profileForm.setOutputMarkupId(true);
 		profileForm.add(new SimpleAttributeModifier("class", "formHide"));
 		add(profileForm);
 
 		//Select or Create New User
 		AjaxButton newUserSubmit = new AjaxValidationSubmitButton(
 				"userSubmit", userForm) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				super.onSubmit(target, form);
 				target.addComponent(userFeedback);
 
 				populateUserData(user.getName(), (UserDataView)form.getModelObject());
 				log.debug("onSubmit - "+user);
 				profileForm.add(new SimpleAttributeModifier("class","formShow"));
 				target.addComponent(profileForm);
 				lockUsername = true;
 				target.addComponent(userForm);
 			}
 
 			@Override
 			protected void onError(final AjaxRequestTarget target, Form<?> form) {
 				super.onError(target, form);
 				target.addComponent(userFeedback);
 			}
 
 			@Override
 			protected final void onBeforeRender() {
 				if (lockUsername) {
 					setEnabled(false);
 				} else {
 					setEnabled(true);
 				}
 				super.onBeforeRender();
 			}
 		};
 		userForm.add(newUserSubmit);
 		
 		usernameField.add(new AttributeModifier("onchange", true, new Model<String>(
 				"document.getElementById('"+ newUserSubmit.getMarkupId()
 				+ "').onclick();return false;")));
 		
 
 		//Welcome Email
 		final Label welcomeFeedback = new Label("welcomeFeedback", new Model<String>(""));
 		welcomeFeedback.setOutputMarkupId(true);
 		profileForm.add(welcomeFeedback);
 		
 		final AjaxLink<String> welcomeLink = new IndicatingAjaxLink<String>("welcomeLink"){
 			private static final long serialVersionUID = 1L;
 
             @Override
             public void onClick(AjaxRequestTarget target) {
             	String message = formatUserMessage("message.new", user);
           		if(sendEmail("Welcome Email", message, user)){
                		welcomeFeedback.setDefaultModelObject(getString("welcome.success"));
            		} else {
            			welcomeFeedback.setDefaultModelObject(getString("welcome.fail"));
            		}
          		target.addComponent(welcomeFeedback);
             }
 
             @Override
 			protected void onBeforeRender() {
 				if(StringUtils.isEmpty(user.getEmail())){
 					setEnabled(false);
 				} else {
 					setEnabled(true);
 				}
 				super.onBeforeRender();
 			}
 
 		};
 		profileForm.add(welcomeLink);
 
 		//Reset Password 
 		final Label resetFeedback = new Label("resetFeedback", new Model<String>(""));
 		resetFeedback.setOutputMarkupId(true);
 		profileForm.add(resetFeedback);
 		
         final AjaxLink<String> resetLink = new IndicatingAjaxLink<String>("resetLink"){
             private static final long serialVersionUID = 1L;
 
             @Override
             public void onClick(AjaxRequestTarget target) {
             	String message = formatUserMessage("message.reset", user);
                 if (sendEmail("Reset Password", message, user)){
                 	resetFeedback.setDefaultModelObject(getString("reset.success"));
                 } else {
                 	resetFeedback.setDefaultModelObject(getString("reset.fail"));
                 }
                 target.addComponent(resetFeedback);
             }
             
             @Override
 			protected void onBeforeRender() {
 				if(StringUtils.isEmpty(user.getEmail())){
 					setEnabled(false);
 				} else {
 					setEnabled(true);
 				}
 				super.onBeforeRender();
 			}
 
         };
         profileForm.add(resetLink);
 
 		final FeedbackPanel profileFeedback = new ComponentFeedbackPanel(
 				"profileFeedback", profileForm);
 		profileFeedback.setOutputMarkupId(true);
 		profileForm.add(profileFeedback);
 
 		//Submit Button for just Saving
 		final AjaxButton submitButton = new AjaxValidationSubmitButton(
 				"profileSubmit", profileForm) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				super.onSubmit(target, form);
 				target.addComponent(profileFeedback);
 				saveUserData(user, user.getRequiresAuth());
 				form.info(getString("message.success"));
 				target.addComponent(welcomeLink);
 				target.addComponent(resetLink);
 			}
 
 			@Override
 			protected void onError(final AjaxRequestTarget target, Form<?> form) {
 				super.onError(target, form);
 				target.addComponent(profileFeedback);
 
 			}
 		};
 		profileForm.add(submitButton);
 
 		//Submit for saving and resetting for starting another
 		AjaxButton submitNewButton = new AjaxValidationSubmitButton(
 				"newSubmit", profileForm) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				super.onSubmit(target, form);
 
 				target.addComponent(profileFeedback);
 				saveUserData(user, user.getRequiresAuth());
 				form.info(getString("message.success"));
 
 				// clear User, hide Profile and enable Username
 				profileForm.add(new SimpleAttributeModifier("class", "formHide"));
 				lockUsername = false;
 				target.addComponent(userForm);
 				target.addComponent(profileForm);
 				resetUserData(user);
 
 				// Clear and set focus on User Name Text Field
                 target.appendJavascript("$('" + usernameField.getMarkupId() + "').clear();" 
                         + "$('" + usernameField.getMarkupId() + "').focus();");
 			}
 
 			@Override
 			protected void onError(final AjaxRequestTarget target, Form<?> form) {
 				super.onError(target, form);
 				target.addComponent(profileFeedback);
 
 			}
 		};
 		profileForm.add(submitNewButton);
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	private IModel<Collection<SiteData>> getSitesModel(){
 		return new Model((Serializable) sites);
 	}
 	
 	private boolean sendEmail(String subject, String message, UserDataView user){
         return getEmailSender().sendUserEmail(subject, message, user.getEmail(), user.getFirstName());
 	}
 	
 	private String resetPassword(UserDataView user){
         String password = SecurityUtils.getGeneratedPassword();
         log.debug("resetPassword - username="+user.getName() + "password="+ password);
         user.setPassword(SecurityUtils.encrypt(password));
         saveUserData(user, user.getRequiresAuth());
         return password;
 	}
 	
 	private void populateUserData(String username, UserDataView user){
 		UserData src = getRepositoryService().getUser(username);
 		BeanUtils.copyProperties(src, user);
 		List<UserSiteData> userSites = getRepositoryService().getUserSiteEntries(src);
 		for(UserSiteData userSite : userSites){
 			Boolean requiresAuth = userSite.getRequiresAuthentication();
 			if (requiresAuth != null && requiresAuth.booleanValue()) {
 				user.setRequiresAuth(true);
 			}
 			for(SiteData site : sitesChoices){
 				if (site.getName().equals(userSite.getName())){
 					sites.add(site);
 				}
 			}
 		}
 	}
 	
 	private void saveUserData(UserData userData, Boolean auth){
 		UserData dest = getRepositoryService().getUser(userData.getName());
 		BeanUtils.copyProperties(userData, dest);
 		log.debug("saveUserData - " + dest);
 		saveData(dest);
 		log.debug("saveUserData - save Site Entries");
 		getRepositoryService().saveUserSiteEntries(dest, sites, auth == null? false : auth.booleanValue());
 	}
 	
 	private void resetUserData(UserDataView user){
 	    user.setName("");
 	    sites.clear();
 	}
 	
 	private String formatUserMessage(String key, UserDataView user) {
 		IDataService service = getRepositoryService();
 		SiteData site = service.getSiteData();
     	UserData userData = service.getUser(user.getName());
     	
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("firstName", StringUtils.defaultString(user.getFirstName()));
 		map.put("lastName", StringUtils.defaultString(user.getLastName()));
 		map.put("name", user.getName());
 		map.put("password", resetPassword(user));
 		String url = StringUtils.defaultString(site.getUrl());
 		map.put("url", url );
 		map.put("description", StringUtils.defaultString(site.getMetaDescription()));
 
 		MapModel<String, String> model = new MapModel<String,String>(map);
 		String message = getString(key, model);
 		
     	if (service.isUserSite(userData) && StringUtils.isNotEmpty(url)){
     		IAuthenticator authenticator = getRepositoryService().getUserAuthenticator();
     		if (site.getSecurityCertificate() && authenticator.requiresSecureAuthentication(userData.getName())){
     			map.put("passwordChangePage", "securePassword");
     		} else {
     			map.put("passwordChangePage", "password");
     		}
     		message = message + getString("message.password", model);
     	}
     	List<UserSiteData> sites = service.getUserSiteEntries(userData);
     	if (sites.size() > 0){
     		message = message + getString("message.sites");
     	}
 		for (UserSiteData siteData : sites){
 			SiteData accessSite = service.getSite(siteData.getName());
 			message = message + getString("message.site", new Model<SiteData>(accessSite));
 		}
 		message = message + getString("message.closing");
     	log.debug("formatMessage - " + message);
 		return message;
 	}
 	
 	protected IEmailSender getEmailSender() {
 		return ((IEmailServiceProvider) getApplication()).getEmailSender();
 	}
 }
