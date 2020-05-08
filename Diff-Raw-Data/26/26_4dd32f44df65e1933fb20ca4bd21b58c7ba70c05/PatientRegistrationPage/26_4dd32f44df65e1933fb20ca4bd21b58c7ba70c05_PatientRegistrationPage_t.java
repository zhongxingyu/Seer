 package com.solidstategroup.radar.web.pages;
 
 import com.solidstategroup.radar.model.exception.RegistrationException;
 import com.solidstategroup.radar.model.user.PatientUser;
 import com.solidstategroup.radar.service.UserManager;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public class PatientRegistrationPage extends BasePage {
 
     @SpringBean
     private UserManager userManager;
 
     public PatientRegistrationPage() {
 
         // Construct our model and the form
         CompoundPropertyModel<PatientUser> model = new CompoundPropertyModel<PatientUser>(new PatientUser());
         Form<PatientUser> form = new Form<PatientUser>("form", model) {
             @Override
             protected void onSubmit() {
                 try {
                     userManager.registerPatient(getModelObject());
                 } catch (RegistrationException e) {
                     error("Could not register - please check the radar number and date of birth are correct");
                 }
             }
         };
        add(form);
 
         // Construct feedback panel
         final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
         feedbackPanel.setOutputMarkupPlaceholderTag(true);
         form.add(feedbackPanel);
 
         // Add fields
         form.add(new RequiredTextField("radarNumber"));
        form.add(new RequiredTextField("username"));
         form.add(new DateTextField("dateOfBirth", "dd-MM-yyyy"));
 
         // Ajax submit link
         form.add(new AjaxSubmitLink("submit") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 target.add(feedbackPanel);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(feedbackPanel);
             }
         });
     }
 }
