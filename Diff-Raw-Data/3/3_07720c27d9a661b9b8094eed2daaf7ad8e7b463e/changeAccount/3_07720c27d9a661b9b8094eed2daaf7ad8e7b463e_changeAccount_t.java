 package reevent.web.account;
 
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.validator.AbstractValidator;
 import reevent.domain.User;
 import reevent.service.UserService;
 import reevent.web.ReEventApplication;
 import reevent.web.ReEventSession;
 import reevent.web.Template;
 
 import java.util.List;
 
 import static java.util.Arrays.asList;
 
 @AuthorizeInstantiation("USER")
 public class changeAccount extends Template {
 	
 	Form<User> changeUserForm;
     Label username;
     TextField<String> password;
     TextField<String> passwordVerify;
     TextField<String> firstName;
     TextField<String> lastName;
     
    @SpringBean
     UserService users;
 
 	public changeAccount(){
         CompoundPropertyModel<User> formModel = new CompoundPropertyModel<User>(ReEventSession.get().getModUserSignedIn());
 		
         add(changeUserForm = new Form<User>("changeUserForm", formModel) {
             @Override
             protected void onSubmit() {
                 if (!getModelObject().getId().equals(ReEventSession.get().getUserSignedIn())) {
                     throw new IllegalStateException("Tried to edit details of another user");
                 }
                 users.update(getModelObject(), password.getModelObject());
                 setResponsePage(ReEventApplication.get().getAccount());
             }
         });
 
         changeUserForm.add(username = new Label("username", formModel.<String>bind("username")));
         username.add(new AbstractValidator<String>() {
             @Override
             protected void onValidate(IValidatable<String> field) {
                 if (!users.isAvailable(field.getValue())) {
                     this.error(field, "username.not.available");
                 }
             }
         });
 
         changeUserForm.add(password = new PasswordTextField("password", Model.<String>of()));
 
         changeUserForm.add(passwordVerify = new PasswordTextField("passwordVerify", Model.<String>of()));
         passwordVerify.add(new AbstractValidator<String>() {
             @Override
             protected void onValidate(IValidatable<String> field) {
                 password.processInput();
                 if (!password.isValid()) return;
                 if (!field.getValue().equals(password.getInput())) {
                     error(field, "passwords.do.not.match");
                 }
             }
         });
 
         changeUserForm.add(firstName = new TextField<String>("firstName"));
         
         changeUserForm.add(lastName = new TextField<String>("lastName"));
 
         // required fields
         List<TextField<String>> required = asList(firstName, lastName);
         List<TextField<String>> optional = asList(password, passwordVerify);
         addFormLabels(required);
         addFormLabels(optional);
         for (FormComponent fc : required) {
             fc.setRequired(true);
         }
         for (TextField<String> fc : optional) {
             fc.setRequired(false);
         }
     }
 }
