 package com.solidstategroup.radar.web.pages.regisration;
 
 
 import com.solidstategroup.radar.model.exception.InvalidSecurityQuestionAnswer;
 import com.solidstategroup.radar.model.exception.RegistrationException;
 import com.solidstategroup.radar.model.exception.UserEmailAlreadyExists;
 import com.solidstategroup.radar.model.user.ProfessionalUser;
 import com.solidstategroup.radar.service.UserManager;
 import com.solidstategroup.radar.service.UtilityManager;
 import com.solidstategroup.radar.web.components.RadarRequiredDropdownChoice;
 import com.solidstategroup.radar.web.components.RadarRequiredTextField;
 import com.solidstategroup.radar.web.components.RadarTextFieldWithValidation;
 import com.solidstategroup.radar.web.pages.BasePage;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
 import org.apache.wicket.feedback.FeedbackMessage;
 import org.apache.wicket.feedback.IFeedbackMessageFilter;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.HiddenField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 public class ProfessionalRegistrationPage extends BasePage {
 
     public static final String AREA1 = "GB and Ireland";
     public static final String AREA2 = "Outside GB and Ireland";
     public static final String ERROR_MESSAGE = "An unexpected error has occurred";
     private IModel<String> securityQuestionAnswModel = new Model<String>();
     @SpringBean
     UtilityManager utilityManager;
     @SpringBean
     UserManager userManager;
 
     public ProfessionalRegistrationPage() {
 
         final List<Component> componentsToUpdate = new ArrayList<Component>();
 
         Random randomGenerator = new Random();
         int firstNumber = randomGenerator.nextInt(10);
         int secondNumber = randomGenerator.nextInt(10);
 
         final Form<ProfessionalUser> form = new Form<ProfessionalUser>("form",
                 new CompoundPropertyModel<ProfessionalUser>(new ProfessionalUser())) {
             @Override
             protected void onSubmit() {
                 try {
                     ProfessionalUser professionalUser = getModelObject();
                     professionalUser.setSecurityQuestionAnsw(securityQuestionAnswModel.getObject());
                     userManager.registerProfessional(professionalUser);
                 } catch (UserEmailAlreadyExists professionalUserEmailAlreadyExists) {
                     get("emailContainer").get("email").error("This email address has already been taken");
                 } catch (InvalidSecurityQuestionAnswer invalidSecurityQuestionAnswer) {
                     get("securityQuestionContainer").get("securityQuestion")
                             .error("Wrong answer to the anti-spam question, please try again.");
                 } catch (RegistrationException e) {
                     error(ERROR_MESSAGE);
                 }
             }
         };
 
         final IModel<String> areaModel = new Model<String>();
 
         form.add(new RadarRequiredTextField("surname", form, componentsToUpdate));
         form.add(new RadarRequiredTextField("forename", form, componentsToUpdate));
         form.add(new RadarRequiredDropdownChoice("title", Arrays.asList("Dr", "Professor", "Mr", "Mrs", "Miss"),
                 new ChoiceRenderer(), form, componentsToUpdate));
 
         DropDownChoice areaDropDown = new RadarRequiredDropdownChoice("areaDropDown", areaModel,
                 Arrays.asList(AREA1, AREA2),
                 new ChoiceRenderer(), form, componentsToUpdate);
 
         form.add(areaDropDown);
         areaDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 target.add(componentsToUpdate.toArray(new Component[componentsToUpdate.size()]));
             }
         });
 
         WebMarkupContainer gmcContainer = new WebMarkupContainer("gmcContainer") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
             }
 
             @Override
             public boolean isVisible() {
                 return (AREA1).equals(areaModel.getObject());
             }
         };
 
         RadarRequiredTextField gmc = new RadarRequiredTextField("gmc", gmcContainer, componentsToUpdate);
         gmcContainer.add(gmc);
         form.add(gmcContainer);
         componentsToUpdate.add(gmcContainer);
 
         WebMarkupContainer roleContainer = new WebMarkupContainer("roleContainer") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
             }
 
             @Override
             public boolean isVisible() {
                 return areaModel.getObject() != null;
             }
         };
 
         RadarRequiredTextField role = new RadarRequiredTextField("role", roleContainer, componentsToUpdate);
         roleContainer.add(role);
         form.add(roleContainer);
         componentsToUpdate.add(roleContainer);
 
         WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
             }
 
             @Override
             public boolean isVisible() {
                 return areaModel.getObject() != null;
             }
         };
 
         RadarTextFieldWithValidation email = new RadarTextFieldWithValidation("email",
                 EmailAddressValidator.getInstance(), true, emailContainer, componentsToUpdate);
 
         //RadarRequiredTextField email = new RadarRequiredTextField("email", emailContainer, componentsToUpdate);
         emailContainer.add(email);
         form.add(emailContainer);
         componentsToUpdate.add(emailContainer);
 
         WebMarkupContainer phoneContainer = new WebMarkupContainer("phoneContainer") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
             }
 
             @Override
             public boolean isVisible() {
                 return areaModel.getObject() != null;
             }
         };
 
         RadarRequiredTextField phone = new RadarRequiredTextField("phone", phoneContainer, componentsToUpdate);
         phoneContainer.add(phone);
         form.add(phoneContainer);
         componentsToUpdate.add(phoneContainer);
 
         WebMarkupContainer centreContainer = new WebMarkupContainer("centreContainer") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
             }
 
             @Override
             public boolean isVisible() {
                 return areaModel.getObject() != null;
             }
         };
 
         RadarRequiredDropdownChoice centre = new RadarRequiredDropdownChoice("centre", utilityManager.getCentres(),
                 new ChoiceRenderer("name", "id"), centreContainer, componentsToUpdate);
         centreContainer.add(centre);
         form.add(centreContainer);
         componentsToUpdate.add(centreContainer);
 
         WebMarkupContainer securityQuestionContainer = new WebMarkupContainer("securityQuestionContainer") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
             }
 
             @Override
             public boolean isVisible() {
                 return areaModel.getObject() != null;
             }
         };
 
        WebMarkupContainer securityQuestionMsgContainer = new WebMarkupContainer("securityQuestionMsgContainer") {
            {
                setOutputMarkupId(true);
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            public boolean isVisible() {
                return areaModel.getObject() != null;
            }
        };

         RadarRequiredTextField securityQuestion = new RadarRequiredTextField("securityQuestion",
                 securityQuestionContainer, componentsToUpdate);
         securityQuestionContainer.add(securityQuestion);
         form.add(securityQuestionContainer);
         componentsToUpdate.add(securityQuestionContainer);
 
         securityQuestionAnswModel.setObject(String.valueOf(firstNumber + secondNumber));
         HiddenField securityQuestionAnsw = new HiddenField("securityQuestionAnsw", securityQuestionAnswModel);
         securityQuestionContainer.add(securityQuestionAnsw);
         form.add(securityQuestionContainer);
         componentsToUpdate.add(securityQuestionContainer);
 
 
         Label successMessage = new Label("successMessage", "Thank you. Your registration has been successful and will" +
                 " be confirmed by email shortly") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
                 form.add(this);
                 componentsToUpdate.add(this);
             }
 
             @Override
             public boolean isVisible() {
                 return form.isSubmitted() && !form.hasError();
             }
         };
 
         Label errorMessage = new Label("errorMessage", "Please fix all errors") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
                 form.add(this);
                 componentsToUpdate.add(this);
             }
 
             @Override
             public boolean isVisible() {
                 return form.isSubmitted() && form.hasError();
             }
         };
 
         Label securityQuestionMessage = new Label("securityQuestionMessage",
                 firstNumber + " + " + secondNumber + " = ? ") {
             {
                 setOutputMarkupId(true);
                 setOutputMarkupPlaceholderTag(true);
                 form.add(this);
                 componentsToUpdate.add(this);
             }
 
             @Override
             public boolean isVisible() {
                 return areaModel.getObject() != null;
             }
         };
        securityQuestionMsgContainer.add(securityQuestionMessage);
        form.add(securityQuestionMsgContainer);
        componentsToUpdate.add(securityQuestionMsgContainer);
 
         // Construct feedback panel
         // for errors not specific to a particular component
         final FeedbackPanel feedbackPanel = new FeedbackPanel("errorFeedback", new IFeedbackMessageFilter() {
             public boolean accept(FeedbackMessage feedbackMessage) {
                 String message = feedbackMessage.getMessage().toString();
                 return message.contains(ERROR_MESSAGE);
             }
         });
 
         form.add(feedbackPanel);
 
 
         AjaxButton submit = new IndicatingAjaxButton("submit") {
             {
                 componentsToUpdate.add(this);
             }
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 target.add(componentsToUpdate.toArray(new Component[componentsToUpdate.size()]));
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(componentsToUpdate.toArray(new Component[componentsToUpdate.size()]));
             }
 
             @Override
             public boolean isVisible() {
                 if (form.isSubmitted() && !form.hasError()) {
                     return false;
                 }
                 return super.isVisible();
             }
         };
         form.add(submit);
         submit.add(new Label("submitLabel", new AbstractReadOnlyModel<Object>() {
             @Override
             public Object getObject() {
                 return areaModel.getObject() == null ? "Continue" : "Register";
             }
         }));
 
         submit.setOutputMarkupId(true);
         submit.setOutputMarkupPlaceholderTag(true);
         componentsToUpdate.add(submit);
 
         add(form);
 
     }
 }
