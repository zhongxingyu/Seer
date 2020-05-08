 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 package org.generationcp.ibpworkbench.model.formfieldfactory;
 
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.validator.PersonNameValidator;
 import org.generationcp.ibpworkbench.validator.UserPasswordValidator;
 import org.generationcp.ibpworkbench.validator.UsernameValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.validator.EmailValidator;
 import com.vaadin.data.validator.StringLengthValidator;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.DefaultFieldFactory;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.PasswordField;
 import com.vaadin.ui.TextField;
 
 
 /**
  * <b>Description</b>: Field factory for generating Users and Persons fields for UserAccountForm class.
  * 
  * <br>
  * <br>
  * 
  * <b>Author</b>: Michael Blancaflor
  * <br>
  * <b>File Created</b>: Jul 16, 2012
  */
 @Configurable
 public class UserAccountFormFieldFactory extends DefaultFieldFactory{
 
     private static final long serialVersionUID = 3560059243526106791L;
     
     private Field firstName;
     private Field lastName;
     
     private PasswordField password;
     private PasswordField passwordConfirmation;
     
     private ComboBox securityQuestion;
     
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
 
     public UserAccountFormFieldFactory() {
         initFields();
     }
     
     private void initFields() {
         firstName = new TextField();
         firstName.setRequired(true);
         firstName.setRequiredError("Please enter a First Name.");
         firstName.addValidator(new StringLengthValidator("First Name must be 1-20 characters.", 1, 20, false));
         
         lastName = new TextField();
         lastName.setRequired(true);
         lastName.setRequiredError("Please enter a Last Name.");
         lastName.addValidator(new StringLengthValidator("Last Name must be 1-50 characters.", 1, 50, false));
         
         firstName.addValidator(new PersonNameValidator(firstName, lastName));
         
         password = new PasswordField();
         password.setRequired(true);
         password.setRequiredError("Please enter a Password.");
         password.addValidator(new StringLengthValidator("Password must be 1-30 characters.", 1, 30, false));
         
         passwordConfirmation = new PasswordField();
         passwordConfirmation.setRequired(true);
         passwordConfirmation.setRequiredError("Please re-type your password for confirmation.");
         passwordConfirmation.addValidator(new StringLengthValidator("Password confirmation must be 1-30 characters.", 1, 30, false));
         
         password.addValidator(new UserPasswordValidator(password, passwordConfirmation));
         
         securityQuestion = new ComboBox();
     }
 
     @Override
     public Field createField(Item item, Object propertyId, Component uiContext) {
          
         Field field = super.createField(item, propertyId, uiContext);
         
         /*if("positionTitle".equals(propertyId)) {
             messageSource.setCaption(field, Message.USER_ACC_POS_TITLE);
             field.setRequired(false);
             field.setRequiredError("Please enter a Position Title.");
             field.addValidator(new StringLengthValidator("Position Title must be 2-25 characters.", 2, 25, false));
         } else */
         if ("firstName".equals(propertyId)) {
             messageSource.setCaption(firstName, Message.USER_ACC_FNAME);
            firstName.setRequired(true);
            lastName.setRequiredError("Please enter First Name");
             return firstName;
         } else if ("lastName".equals(propertyId)) {
             messageSource.setCaption(lastName, Message.USER_ACC_LNAME);
            lastName.setRequired(true);
            lastName.setRequiredError("Please enter Last Name");
             return lastName;
         } else if ("middleName".equals(propertyId)) {
             messageSource.setCaption(field, Message.USER_ACC_MIDNAME);
             field.setRequired(false);
             //field.setRequiredError("Please enter a Middle Initial.");
             field.addValidator(new StringLengthValidator("Middle Initial must be 1-15 characters.", 1, 15, false));
         } else if ("email".equals(propertyId)) {
             messageSource.setCaption(field, Message.USER_ACC_EMAIL);
             field.setRequired(true);
             field.setRequiredError("Please enter an Email Address.");
             field.addValidator(new StringLengthValidator("Email Address must be 5-40 characters.", 5, 40, false));
             field.addValidator(new EmailValidator("Please enter a valid Email Address."));
         } else if ("username".equals(propertyId)) {
             messageSource.setCaption(field, Message.USER_ACC_USERNAME);
             field.setRequired(true);
             field.setRequiredError("Please enter a Username.");
             field.addValidator(new StringLengthValidator("Username must be 1-30 characters.", 1, 30, false));
             field.addValidator(new UsernameValidator());
         } else if ("password".equals(propertyId)) {
             messageSource.setCaption(password, Message.USER_ACC_PASSWORD);
             return password;
         } else if ("passwordConfirmation".equals(propertyId)) {
             messageSource.setCaption(passwordConfirmation, Message.USER_ACC_PASSWORD_CONFIRM);
             return passwordConfirmation;
         } else if ("securityQuestion".equals(propertyId)) {
             messageSource.setCaption(securityQuestion, Message.SECURITY_QUESTION);
             securityQuestion.setWidth("100%");
             securityQuestion.setNullSelectionAllowed(false);
             securityQuestion.setRequired(true);
             securityQuestion.setRequiredError("Please specify a security question that only you can answer.");
             
             securityQuestion.setTextInputAllowed(false);
             securityQuestion.addItem("What is your first pet's name?");
             securityQuestion.addItem("What is your mother's maiden name?");
             securityQuestion.addItem("What is your town of birth?");
             
             return securityQuestion;
         } else if ("securityAnswer".equals(propertyId)) {
             messageSource.setCaption(field, Message.SECURITY_ANSWER);
             field.setWidth("100%");
             field.setRequired(true);
             field.setRequiredError("Please enter the answer to your security question.");
             field.addValidator(new StringLengthValidator("Security Answer must be 1-255 characters.", 1, 255, false));
         } 
         
         return field;
     }
 }
