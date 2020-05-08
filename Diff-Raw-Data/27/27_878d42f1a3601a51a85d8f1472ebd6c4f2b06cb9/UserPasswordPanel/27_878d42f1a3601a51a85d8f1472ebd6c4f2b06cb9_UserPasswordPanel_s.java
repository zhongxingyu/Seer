 package com.madalla.webapp.user;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.util.value.ValueMap;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.validator.AbstractValidator;
 
 import com.madalla.bo.security.UserData;
 import com.madalla.util.security.ICredentialHolder;
 import com.madalla.util.security.SecureCredentials;
 import com.madalla.util.security.SecurityUtils;
 import com.madalla.webapp.CmsSession;
 import com.madalla.webapp.css.Css;
 import com.madalla.webapp.panel.CmsPanel;
 import com.madalla.wicket.form.AjaxValidationStyleSubmitButton;
 import com.madalla.wicket.form.ValidationStylePasswordField;
 
 public class UserPasswordPanel extends CmsPanel{
 
 	private static final long serialVersionUID = 3207705274626512033L;
 
 	private ICredentialHolder credentials = new SecureCredentials();
 	private UserData user;
 	
     public class PasswordForm extends Form<Object> {
         
         private static final long serialVersionUID = 9033980585192727266L;
         private final ValueMap properties = new ValueMap();
         
         public PasswordForm(String id, String pwd) {
             super(id);
             
             properties.add("existingPassword", pwd);
             
             FeedbackPanel existingFeedback = new FeedbackPanel("existingFeedback");
             add(existingFeedback);
             PasswordTextField existingPassword = new ValidationStylePasswordField("existingPassword", 
             		new PropertyModel<String>(properties,"existingPassword"), existingFeedback);
             add(existingPassword);
             existingPassword.add(new AbstractValidator<String>(){
                 private static final long serialVersionUID = 1L;
 
                 @Override
                 protected void onValidate(IValidatable<String> validatable) {
                     String value = SecurityUtils.encrypt((String)validatable.getValue());
                     if (!user.getPassword().equals(value)){
                         error(validatable,"error.existing");
                     }
                 }
                 
             });
             existingPassword.setResetPassword(false);
             
             
             FeedbackPanel newFeedback = new FeedbackPanel("newFeedback");
             add(newFeedback);
             TextField<String> newPassword = new ValidationStylePasswordField("newPassword",
                     new PropertyModel<String>(credentials,"password"), newFeedback);
             add(newPassword);
             
             FeedbackPanel confirmFeedback = new FeedbackPanel("confirmFeedback");
             add(confirmFeedback);
             TextField<String> confirmPassword = new ValidationStylePasswordField("confirmPassword", 
             		new PropertyModel<String>(properties,"confirmPassword"), confirmFeedback);
             add(confirmPassword);
             
             //Validate that new and confirm are equal
             add(new EqualPasswordInputValidator(newPassword, confirmPassword));
             
         }
     }
     public UserPasswordPanel(String id, String userName){
     	this(id,userName,"");
     }
     
     public UserPasswordPanel(String id, String userName, String existingPassword){
     	super(id);
     	
 		add(Css.CSS_FORM);
 
 		user = getRepositoryService().getUser(userName);
         
     	//****************
         //Password Section
         
         //Form
         Form<Object> passwordForm = new PasswordForm("passwordForm", existingPassword);
         passwordForm.setOutputMarkupId(true);
         
         add(passwordForm);
         
         final FeedbackPanel passwordFeedback = new ComponentFeedbackPanel("passwordFeedback",passwordForm);
         passwordFeedback.setOutputMarkupId(true);
         passwordForm.add(passwordFeedback);
         
         AjaxButton passwordSubmit = new AjaxValidationStyleSubmitButton("passwordSubmit", passwordForm){
         	private static final long serialVersionUID = 1L;
         	
             @Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				super.onSubmit(target, form);
 				
 				//validation passed we can log user in
 				if (((CmsSession)getSession()).login(user.getName(), user.getPassword())){
 					target.addComponent(passwordFeedback);
 	                user.setPassword(credentials.getPassword());
 	                saveData(user);
 	                form.info(getString("message.success"));
 	                setResponsePage(getApplication().getHomePage());
 				}
 			}
 
             @Override
             protected void onError(final AjaxRequestTarget target, Form<?> form) {
             	super.onError(target, form);
             	target.addComponent(passwordFeedback);
             }
         };
         passwordForm.add(passwordSubmit);
         passwordForm.add(new AttributeModifier("onSubmit", true, new Model<String>("document.getElementById('" + passwordSubmit.getMarkupId() + "').onclick();return false;")));
         
     }
 
 }
