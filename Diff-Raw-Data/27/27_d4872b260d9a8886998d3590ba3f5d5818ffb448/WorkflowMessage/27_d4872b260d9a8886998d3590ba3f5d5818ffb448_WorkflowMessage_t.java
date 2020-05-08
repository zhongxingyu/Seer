 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import org.restlet.routing.Template;
import org.springframework.util.StringUtils;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public class WorkflowMessage {
     public static final String MESSAGE_TEMPLATE_VARIABLE_ACTION = "action";
 
     private WorkflowStep step;
     private Map<String, String> uriTemplateValues, messageTemplateValues;
     private String applicationMountPoint;
     private boolean mayPerform;
 
     public WorkflowMessage(
         WorkflowStep step, String applicationMountPoint, boolean mayPerform
     ) {
         this.step = step;
         this.applicationMountPoint = applicationMountPoint;
         this.mayPerform = mayPerform;
 
         uriTemplateValues = new HashMap<String, String>();
         messageTemplateValues = new HashMap<String, String>();
     }
 
     public boolean getMayPerform() {
         return mayPerform;
     }
 
     public String getHtml() {
         String actionHtml = getMayPerform() ? getActionLink() : step.getActionPhrase();
         Map<String, String> messageVariableValues = new HashMap<String, String>(messageTemplateValues);
         messageVariableValues.put(MESSAGE_TEMPLATE_VARIABLE_ACTION, actionHtml);
 
         StringBuilder html = new StringBuilder().append(stepMessage(messageVariableValues));
         if (!getMayPerform()) {
             html.append("  A <em>").append(step.getNecessaryRole().getDisplayName()).append("</em> can do this.");
         }
         return html.toString();
     }
 
     public String getText() {
         Map<String, String> messageVariableValues = new HashMap<String, String>(messageTemplateValues);
         messageVariableValues.put(MESSAGE_TEMPLATE_VARIABLE_ACTION, step.getActionPhrase());
         StringBuilder text = new StringBuilder().append(stepMessage(messageVariableValues));
         if (getMayPerform()) {
             text.append("  You can do this.");
         } else {
             text.append("  A ").append(step.getNecessaryRole().getDisplayName()).append(" can do this.");
         }
         return text.toString();
     }
 
     public WorkflowMessage setUriVariable(String name, String value) {
         uriTemplateValues.put(name, value);
         return this;
     }
 
     public WorkflowMessage setMessageVariable(String name, String value) {
         messageTemplateValues.put(name, value);
         return this;
     }
 
     public String getActionLink() {
         if (step.getUriTemplate() == null) {
             return step.getActionPhrase();
         } else {
             return String.format("<a href=\"%s\" class=\"control\">%s</a>",
                 getUri(), step.getActionPhrase());
         }
     }
 
     public String getUri() {
         verifyTemplateVariablesAvailable(step.getUriTemplate(), uriTemplateValues);
 
         StringBuilder uri = new StringBuilder();
        if (StringUtils.hasText(applicationMountPoint)) {
             uri.append(applicationMountPoint);
             if (uri.charAt(uri.length() - 1) == '/') {
                 uri.deleteCharAt(uri.length() - 1);
             }
         }
         return uri.append(step.getUriTemplate().format(uriTemplateValues)).toString();
     }
 
     private String stepMessage(Map<String, String> messageVariableValues) {
         verifyTemplateVariablesAvailable(step.getMessageTemplate(), messageVariableValues);
         return step.getMessageTemplate().format(messageVariableValues);
     }
 
     private void verifyTemplateVariablesAvailable(Template template, Map<String, String> values) {
         for (String v : template.getVariableNames()) {
             if (!values.keySet().contains(v)) {
                 throw new StudyCalendarSystemException(
                     "Missing %s variable value for workflow message", v);
             }
         }
     }
 
     public WorkflowStep getStep() {
         return step;
     }
 
     ////// OBJECT METHODS
 
     @Override
     public String toString() {
         return new StringBuilder(getClass().getSimpleName()).
             append("[step=").append(getStep()).
             append("; mayPerform=").append(getMayPerform()).
             append("]").toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof WorkflowMessage)) return false;
 
         WorkflowMessage that = (WorkflowMessage) o;
 
         if (step != null ? !step.equals(that.step) : that.step != null) return false;
         if (applicationMountPoint != null ? !applicationMountPoint.equals(that.applicationMountPoint) : that.applicationMountPoint != null) return false;
         if (mayPerform != that.mayPerform) return false;
 
         if (uriTemplateValues != null ? !uriTemplateValues.equals(that.uriTemplateValues) : that.uriTemplateValues != null) return false;
         if (messageTemplateValues != null ? !messageTemplateValues .equals(that.messageTemplateValues ) : that.messageTemplateValues != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = step != null ? step.hashCode() : 0;
         result = 31 * result + (applicationMountPoint != null ? applicationMountPoint.hashCode() : 0);
         result = 31 * result + (mayPerform ? 1 : 0);
 
         result = 31 * result + (uriTemplateValues  != null ? uriTemplateValues .hashCode() : 0);
         result = 31 * result + (messageTemplateValues  != null ? messageTemplateValues .hashCode() : 0);
         return result;
     }
 }
