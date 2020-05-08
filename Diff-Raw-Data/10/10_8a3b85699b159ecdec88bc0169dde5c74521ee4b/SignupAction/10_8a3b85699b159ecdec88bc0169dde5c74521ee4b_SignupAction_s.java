 package org.jrecruiter.web.actions.registration;
 
 import java.util.Map;
 
 import net.tanesha.recaptcha.ReCaptcha;
 import net.tanesha.recaptcha.ReCaptchaResponse;
 
 import org.apache.struts2.ServletActionContext;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.interceptor.SessionAware;
 import org.apache.struts2.interceptor.validation.SkipValidation;
 import org.jasypt.digest.StringDigester;
 import org.jrecruiter.common.ApiKeysHolder;
 import org.jrecruiter.common.CollectionUtils;
 import org.jrecruiter.common.PasswordGenerator;
 import org.jrecruiter.common.Constants.UserAuthenticationType;
 import org.jrecruiter.model.User;
 import org.jrecruiter.service.exceptions.DuplicateUserException;
 import org.jrecruiter.web.actions.BaseAction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.conversion.annotations.Conversion;
 import com.opensymphony.xwork2.validator.annotations.EmailValidator;
 import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
 import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
 import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
 import com.opensymphony.xwork2.validator.annotations.Validations;
 import com.opensymphony.xwork2.validator.annotations.ValidatorType;
 
 /**
  * Responsible for registering potential job posters
  *
  * @author Gunnar Hillert
  * @version $Id:UserService.java 128 2007-07-27 03:55:54Z ghillert $
  */
 @Conversion
@Result(name="success", location="index", type="redirectAction")
 public class SignupAction extends BaseAction implements SessionAware {
 
 
     private Map<String, Object> session = CollectionUtils.getHashMap();
 
     private User user;
     private String password;
     private String password2;
 
     private String recaptcha_challenge_field;
     private String recaptcha_response_field;
 
     private @Autowired ReCaptcha reCaptcha;
     private @Autowired StringDigester stringDigester;
 
     private transient ApiKeysHolder apiKeysHolder;
 
     /** serialVersionUID. */
     private static final long serialVersionUID = -3422780336408883930L;
 
     private final static Logger LOGGER = LoggerFactory.getLogger(SignupAction.class);
 
     @Validations(
             requiredStrings = {
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "password",       trim=true, message = "You must enter a passwordsssss."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.firstName", trim=true, message = "You must enter a first name."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.lastName",  trim=true, message = "You must enter a last name."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.company",   trim=true, message = "You must enter a company.")
                      },
              requiredFields = {
                      @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "user.email",  message = "You must enter an email address.")
                   },
             emails =
                     { @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "user.email", message = "You must enter a valid email address.")},
             stringLengthFields =
                     {
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "120", fieldName = "password",       message = "The password must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.username",  message = "The user name must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.firstName", message = "The first name must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.lastName",  message = "The last name must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.company",   message = "The company name must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.email",     message = "The email address must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "25",  fieldName = "user.phone",     message = "The phone number must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "25",  fieldName = "user.fax",       message = "The fax number must be shorter than ${maxLength} characters.")
                     }
             )
     public String save() {
 
         final ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(ServletActionContext.getRequest().getRemoteHost(), recaptcha_challenge_field, recaptcha_response_field);
 
         if (!reCaptchaResponse.isValid()) {
             addActionError(super.getText("class.SignupAction.error.not.a.good.captcha"));
             return INPUT;
         }
 
         this.user.setPassword(this.stringDigester.digest(this.password));
         user.setUserAuthenticationType(UserAuthenticationType.USERNAME_PASSWORD);
 
         try {
            userService.addUser(user, Boolean.TRUE);
         } catch (DuplicateUserException e) {
 
             LOGGER.warn(e.getMessage());
               addFieldError("username", getText("class._ALL.error.duplicateEmail"));
               return INPUT;
         }
 
         addActionMessage(getText("class.SignupAction.success"));
         return SUCCESS;
 
     }
 
     @Validations(
             requiredStrings = {
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.firstName", trim=true, message = "You must enter a first name."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.lastName",  trim=true, message = "You must enter a last name."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.company",   trim=true, message = "You must enter a company.")
                      },
              requiredFields = {
                      @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "user.email",  message = "You must enter an email address.")
                   },
             emails =
                     { @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "user.email", message = "You must enter a valid email address.")},
             stringLengthFields =
                     {
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.username",  message = "The user name must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.firstName", message = "The first name must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.lastName",  message = "The last name must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.company",   message = "The company name must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50",  fieldName = "user.email",     message = "The email address must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "25",  fieldName = "user.phone",     message = "The phone number must be shorter than ${maxLength} characters."),
                     @StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "25",  fieldName = "user.fax",       message = "The fax number must be shorter than ${maxLength} characters.")
                     }
             )
     public String saveForOpenId() {
 
         if (session.get("OpenIdUserObject") == null) {
             addActionError(getText("class.SignupAction.error.no_openid_token_found"));
         }
 
         final User openIdUser = (User) session.get("OpenIdUserObject");
 
         user.setUsername(openIdUser.getUsername());
         user.setUserAuthenticationType(UserAuthenticationType.OPEN_ID);
 
         final ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(ServletActionContext.getRequest().getRemoteHost(), recaptcha_challenge_field, recaptcha_response_field);
 
         if (!reCaptchaResponse.isValid()) {
             addActionError(super.getText("class.SignupAction.error.not.a.good.captcha"));
             return INPUT;
         }
 
         user.setPassword(stringDigester.digest(PasswordGenerator.generatePassword()));
         try {
            userService.addUser(user, Boolean.TRUE);
         } catch (DuplicateUserException e) {
 
             LOGGER.warn(e.getMessage());
               addFieldError("username", getText("class._ALL.error.duplicateEmail"));
               return INPUT;
         }
 
         addActionMessage(getText("class.SignupAction.success"));
         return SUCCESS;
     }
 
     /**
      *
      */
     public void validateSave() {
 
         if (password != null && password2 != null) {
             if (!password.trim().equals(password2.trim())) {
                 addFieldError("password2", "The passwords do not match.");
             }
         }
 
     }
 
     @SkipValidation
     public String execute() {
 
         if (session.get("OpenIdUserObject") != null) {
 
             final User openIdUser = (User) session.get("OpenIdUserObject");
             this.user = new User();
             this.user.setEmail(openIdUser.getEmail());
             this.user.setFirstName(openIdUser.getFirstName());
             this.user.setLastName(openIdUser.getLastName());
             this.user.setUserAuthenticationType(UserAuthenticationType.OPEN_ID);
 
         } else {
             this.user = new User();
             this.user.setUserAuthenticationType(UserAuthenticationType.USERNAME_PASSWORD);
         }
 
         return INPUT;
     }
 
     public User getUser() {
         return user;
     }
 
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getPassword2() {
         return password2;
     }
 
     public void setPassword2(String password2) {
         this.password2 = password2;
     }
 
     public void setStringDigester(StringDigester stringDigester) {
         this.stringDigester = stringDigester;
     }
 
     public String getRecaptcha_challenge_field() {
         return recaptcha_challenge_field;
     }
 
     public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {
         this.recaptcha_challenge_field = recaptcha_challenge_field;
     }
 
     public String getRecaptcha_response_field() {
         return recaptcha_response_field;
     }
 
     public void setRecaptcha_response_field(String recaptcha_response_field) {
         this.recaptcha_response_field = recaptcha_response_field;
     }
 
     public ApiKeysHolder getApiKeysHolder() {
         return apiKeysHolder;
     }
 
     public void setApiKeysHolder(ApiKeysHolder apiKeysHolder) {
         this.apiKeysHolder = apiKeysHolder;
     }
 
     @Override
     public void setSession(Map<String, Object> session) {
         this.session = session;
     }
 
 }
