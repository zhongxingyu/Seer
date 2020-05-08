 package com.madalla.webapp.email;
 
 import java.text.MessageFormat;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Component;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.protocol.http.ClientProperties;
 import org.apache.wicket.protocol.http.WebRequestCycle;
 import org.apache.wicket.protocol.http.request.WebClientInfo;
 import org.apache.wicket.util.value.ValueMap;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.IValidationError;
 import org.apache.wicket.validation.ValidationError;
 import org.apache.wicket.validation.validator.AbstractValidator;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 
 import com.madalla.service.captcha.CaptchaUtils;
 import com.madalla.service.email.IEmailSender;
 import com.madalla.service.email.IEmailServiceProvider;
 import com.madalla.wicket.ValidationStyleBehaviour;
 
 public class EmailFormPanel extends Panel {
     private static final long serialVersionUID = -1643728343421366820L;
     private Log log = LogFactory.getLog(this.getClass());
     //private String imagePass = CaptchaUtils.randomString(4, 6);
     private Integer first = CaptchaUtils.randomInteger(1, 20);
     private Integer second = CaptchaUtils.randomInteger(1, 12);
     
     private String subject;
     
     public EmailFormPanel(String id, String subject) {
         super(id);
         this.subject = subject;
         Form emailForm = new EmailForm("emailForm", this);
         //AjaxFormValidatingBehavior.addToAllFormComponents(emailForm,"onblur");
         add(emailForm);
     }
 
     public class EmailForm extends Form {
         private static final long serialVersionUID = -2684823497770522924L;
         private final ValueMap properties = new ValueMap();
         
         //private final CaptchaImageResource captchaImageResource;
         
         public EmailForm(String id, Component panel) {
             super(id);
             
             //captchaImageResource = new CaptchaImageResource(imagePass);
             
             final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
             feedbackPanel.setOutputMarkupId(true);
             add(feedbackPanel);
             
            TextField name = new RequiredTextField("name",new PropertyModel(properties,"Name"));
             name.add(new ValidationStyleBehaviour());
             name.setLabel(new Model(panel.getString("label.name")));
             name.add(new AjaxFormComponentUpdatingBehavior("onblur"){
 				private static final long serialVersionUID = -1336792123145056842L;
 				protected void onUpdate(AjaxRequestTarget target) {
 					target.addComponent(getFormComponent());
 				}
             });
             add(name);
             
             RequiredTextField email = new RequiredTextField("email",new PropertyModel(properties,"email"));
             email.add(EmailAddressValidator.getInstance());
             email.add(new ValidationStyleBehaviour());
             email.add(new AjaxFormComponentUpdatingBehavior("onblur"){
 				private static final long serialVersionUID = -6327997848765302354L;
 				protected void onUpdate(AjaxRequestTarget target) {
 					target.addComponent(getFormComponent());
 				}
             });
             add(email);
             
             TextArea comment = new TextArea("comment",new PropertyModel(properties,"comment"));
             add(comment);
             
             add(new Label("captchaString", first+" + "+second+" = "));
             RequiredTextField password = new RequiredTextField("password", new PropertyModel(properties, "password")){
 				private static final long serialVersionUID = -108228073455105029L;
 				protected final void onComponentTag(final ComponentTag tag) {
                         super.onComponentTag(tag);
                         // clear the field after each render
                         //tag.put("value", "");
                 }
             };
             password.add(new AbstractValidator(){
 				private static final long serialVersionUID = 2572094991300700912L;
 				protected void onValidate(IValidatable validatable) {
                     String password = (String)validatable.getValue();
                     try {
                         int answer = Integer.parseInt(password);
                         if (answer != first.intValue() + second.intValue()){
                         	log.debug("onValidate - entered:"+password+" should be:"+first+"+"+second);
                             IValidationError error = new ValidationError().addMessageKey("message.captcha");
                             validatable.error(error);
                         }
                     } catch (Exception e){
                     	
                     }
 //                    if (!imagePass.equals(password)) {
 //                    	log.debug("onValidate - entered:"+password+" should be:"+imagePass);
 //                        IValidationError error = new ValidationError().addMessageKey("message.captcha");
 //                        validatable.error(error);
 //                        //"You entered '" + password + "' You should have entered '" + imagePass + "'"
 //                    }
 //                    captchaImageResource.invalidate();
                 }
             });
             password.add(new ValidationStyleBehaviour());
             password.add(new AjaxFormComponentUpdatingBehavior("onblur"){
 				private static final long serialVersionUID = 1072604570134494000L;
 
 				protected void onUpdate(AjaxRequestTarget target) {
 					target.addComponent(getFormComponent());
 				}
             });
             add(password);
             
             
         }
 
         protected void onSubmit() {
             IEmailSender email = getEmailSender();
             String body = getEmailBody(properties.getString("name"),properties.getString("email"),properties.getString("comment"));
             boolean result = email.sendEmail(subject, body);
             if (result){
                 info("Email sent successfully");
             } else {
                 error("Failed to send email!");
             }
         }
         
         private String getEmailBody(String from, String email, String comment){
             Object[] args = {from,email,comment};
             String body = MessageFormat.format(getEmailtemplate(),args);
             RequestCycle requestCycle = getRequestCycle();
             if (requestCycle instanceof WebRequestCycle){
                 WebClientInfo clientInfo = (WebClientInfo)((WebRequestCycle) requestCycle).getClientInfo();
                 ClientProperties cp = clientInfo.getProperties();
 
                 StringBuffer sb = new StringBuffer(body).append(System.getProperty("line.separator"));
                 sb.append("NavigatorAppName : ").append(cp.getNavigatorAppName()).append(System.getProperty("line.separator"));
                 sb.append("NavigatorAppCodeName : ").append(cp.getNavigatorAppCodeName()).append(System.getProperty("line.separator"));
                 sb.append("NavigatorAppVersion : ").append(cp.getNavigatorAppVersion()).append(System.getProperty("line.separator"));
                 sb.append("BrowserVersionMajor : ").append(cp.getBrowserVersionMajor()).append(System.getProperty("line.separator"));
                 sb.append("BrowserVersionMinor : ").append(cp.getBrowserVersionMinor()).append(System.getProperty("line.separator"));
                 body = sb.toString();
             }
             return body;
         }
         
         private String getEmailtemplate(){
             StringBuffer sb = new StringBuffer("Email sent from emalan.com website...").append(System.getProperty("line.separator"));
             sb.append("From: {0} ({1})").append(System.getProperty("line.separator"));
             sb.append("Comment: {2}").append(System.getProperty("line.separator"));
             return sb.toString();
         }
 
     }
     protected IEmailSender getEmailSender(){
     	return ((IEmailServiceProvider)getApplication()).getEmailSender();
     }
     
 }
