 package pl.rawie.timetrack.interfaces.jsf.utils;
 
 import org.springframework.validation.FieldError;
 import org.springframework.validation.ObjectError;
 import pl.rawie.timetrack.domain.model.DomainError;
 import pl.rawie.timetrack.domain.validator.ValidationError;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
import java.util.Locale;
import java.util.ResourceBundle;
 
 public class Message {
     public static void error(Throwable cause) {
         new MessageBuilder()
                 .withSummary(cause.getClass().getSimpleName())
                 .withDetail(cause.getLocalizedMessage())
                 .addMessage();
     }
 
     public static void validationError(ValidationError cause) {
         MessageBuilder builder = new MessageBuilder();
         for (ObjectError error : cause.getErrors().getAllErrors()) {
             if (error instanceof FieldError) {
                 FieldError fieldError = (FieldError) error;
                 builder
                         .forForm("form")
                         .forField(fieldError.getField())
                         .withSummary(cause.getLocalizedMessage())
                        .withDetail(getResourceMessage(fieldError.getCode()))
                         .addMessage();
             } else {
                 builder
                         .withSummary(cause.getLocalizedMessage())
                         .addMessage();
             }
         }
     }
 
    private static String getResourceMessage(String code) {
        ResourceBundle bundle = ResourceBundle.getBundle("ValidationError");
        return (bundle.containsKey(code)) ? bundle.getString(code) : "Missing message: " + code;
    }

     public static void domainError(DomainError cause) {
         new MessageBuilder()
                 .withSummary(cause.getLocalizedMessage())
                 .withDetail(cause.getCode().name())
                 .addMessage();
     }
 
     public static void info(String message) {
         new MessageBuilder()
                 .withSeverity(FacesMessage.SEVERITY_INFO)
                 .withSummary(message)
                 .addMessage();
     }
 }
 
 class MessageBuilder {
     private String form;
     private String field;
     private FacesMessage.Severity severity;
     private String summary;
     private String detail;
 
     MessageBuilder() {
         reset();
     }
 
     private void reset() {
         form = null;
         field = null;
         severity = FacesMessage.SEVERITY_ERROR;
         summary = "Missing message text!!!";
         detail = null;
     }
 
     public MessageBuilder forForm(String form) {
         this.form = form;
         return this;
     }
 
     public MessageBuilder forField(String field) {
         this.field = field;
         return this;
     }
 
     public MessageBuilder withSeverity(FacesMessage.Severity severity) {
         this.severity = severity;
         return this;
     }
 
     public MessageBuilder withSummary(String summary) {
         this.summary = summary;
         return this;
     }
 
     public MessageBuilder withDetail(String detail) {
         this.detail = detail;
         return this;
     }
 
     public MessageBuilder addMessage() {
         FacesMessage message = new FacesMessage(severity, summary, detail);
         FacesContext
                 .getCurrentInstance()
                 .addMessage(getClientId(), message);
         return this;
     }
 
     private String getClientId() {
         String id = "";
         if (form != null) id += form;
         if (form != null && field != null) id += ':';
         if (field != null) id += field;
         return (id.isEmpty()) ? null : id;
     }
 }
