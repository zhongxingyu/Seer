 package configurationslicing.emailTrigger;
 
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.plugins.emailext.EmailType;
 import hudson.plugins.emailext.ExtendedEmailPublisher;
 import hudson.plugins.emailext.plugins.EmailTrigger;
 import hudson.tasks.Publisher;
 import hudson.util.DescribableList;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import configurationslicing.email.AbstractEmailSliceSpec;
 import configurationslicing.email.ProjectHandler;
 
 @SuppressWarnings("unchecked")
 public abstract class AbstractTriggerSliceSpec extends AbstractEmailSliceSpec
         implements ProjectHandler {
 
     protected static final String DISABLED = "(Disabled)";
     protected static final String SEPARATOR = ",";
 
     protected AbstractTriggerSliceSpec(String joinString, String name,
             String url) {
         super(joinString, name, url);
     }
 
 
     public abstract String getTrigger();
 
     public abstract EmailTrigger createTrigger();
 
     protected ProjectHandler getProjectHandler(AbstractProject project) {
         return this;
     }
 
     @Override
     public String getConfiguredValueDescription() {
         return "Recipient List, Committers, Requester, Culprits <i>(e.g. true,true,false,true)</i>"
                 + "<ul><li>Send to Recipient List (true/false)</li>"
                 + "<li>Send to Committers (true/false)</li>"
                 + "<li>Send to Requester (true/false)</li>"
                 + "<li>Send to Culprits (true/false)</li></ul>";
     }
 
     public String getNotActivatedMessage() {
         return "\"Editable Email Notification\" is not activated for these jobs! \n"
                 + "Activate it first under the point \"Editable Email Notification\" in "
                 + "the Configuration Slicing, before adding any triggers!";
     }
 
     public ExtendedEmailPublisher getExtMailer(AbstractProject project) {
         DescribableList<Publisher, Descriptor<Publisher>> publishers = project
                 .getPublishersList();
         Descriptor<Publisher> descriptor = Hudson.getInstance().getDescriptor(
                 ExtendedEmailPublisher.class);
         Publisher emailPublisher = publishers.get(descriptor);
         return (ExtendedEmailPublisher) emailPublisher;
     }
 
     @Override
     public List<String> getValues(AbstractProject<?, ?> project) {
 
         String trigger = null;
         boolean triggerConfigured = false;
         List<String> values = new ArrayList<String>();
         ExtendedEmailPublisher mailer = getExtMailer(project);
         if (mailer != null) {
 
             if (!mailer.isConfigured()) {
                 trigger = DISABLED;
                 values.add(trigger);
             } else {
                 List<EmailTrigger> configuredTriggers = mailer.getConfiguredTriggers();
                 for (EmailTrigger emailTrigger : configuredTriggers) {
 
                     if (emailTrigger.getDescriptor().getTriggerName().equals(getTrigger())) {
                         trigger = emailTrigger.getEmail().getSendToRecipientList() + SEPARATOR
                                 + emailTrigger.getEmail().getSendToDevelopers() + SEPARATOR
                                 + emailTrigger.getEmail().isSendToRequester() + SEPARATOR
                                 + emailTrigger.getEmail().getIncludeCulprits();
                         values.add(trigger);
                         triggerConfigured = true;
                     }
                 }
                 if (!triggerConfigured) {
                     trigger = DISABLED;
                     values.add(trigger);
                 }
             }
         } else {
             trigger = getNotActivatedMessage();
             values.add(trigger);
         }
         return values;
     }
 
     public boolean setValues(AbstractProject<?, ?> project, List<String> set) {
 
         String configLine;
         List<String> values = new ArrayList<String>();
         boolean disabled = false;
         configLine = set.iterator().next();
 
         if (DISABLED.equals(configLine) || StringUtils.isEmpty(configLine)
                 || configLine == null) {
             disabled = true;
         } else {
             final String[] split = configLine.split(SEPARATOR);
             if (split.length == 4) {
                 values.add(split[0]);
                 values.add(split[1]);
                 values.add(split[2]);
                 values.add(split[3]);
             } else {
                 disabled = true;
             }
         }
 
         boolean saved = false;
         boolean oneSaved = false;
         ProjectHandler handler = getProjectHandler(project);
 
         if (disabled) {
             oneSaved = handler.removeTrigger(project);
         } else {
             oneSaved = handler.addTrigger(project, values);
         }
         if (oneSaved) {
             try {
                 project.save();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
             saved = true;
         }
         return saved;
     }
 
     public boolean removeTrigger(AbstractProject project) {
         ExtendedEmailPublisher mailer = getExtMailer(project);
         if (mailer != null) {
             List<EmailTrigger> configuredTriggers = mailer
                     .getConfiguredTriggers();
             for (EmailTrigger emailTrigger : configuredTriggers) {
                 if (emailTrigger.getDescriptor().getTriggerName()
                         .equals(getTrigger())) {
                     configuredTriggers.remove(emailTrigger);
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean addTrigger(AbstractProject project, List<String> values) {
         boolean recipientList = false;
         boolean developers = false;
         boolean requester = false;
         boolean includeCulprits = false;
 
         ExtendedEmailPublisher mailer = getExtMailer(project);
 
         if (mailer != null) {
             recipientList = Boolean.parseBoolean(values.get(0));
             developers = Boolean.parseBoolean(values.get(1));
             requester = Boolean.parseBoolean(values.get(2));
             includeCulprits = Boolean.parseBoolean(values.get(3));
 
             List<EmailTrigger> configuredTriggers = mailer
                     .getConfiguredTriggers();
             for (EmailTrigger emailTrigger : configuredTriggers) {
                 if (emailTrigger.getDescriptor().getTriggerName()
                         .equals(getTrigger())) {
                     emailTrigger.getEmail().setSendToRecipientList(
                             recipientList);
                     emailTrigger.getEmail().setSendToDevelopers(developers);
                     emailTrigger.getEmail().setSendToRequester(requester);
                     emailTrigger.getEmail().setIncludeCulprits(includeCulprits);
                     return true;
                 }
             }
             EmailTrigger trigger = createTrigger();
             EmailType email = new EmailType();
            email.setBody("$DEFAULT_CONTENT");
            email.setSubject("$DEFAULT_SUBJECT");
             email.setSendToRecipientList(recipientList);
             email.setSendToDevelopers(developers);
             email.setSendToRequester(requester);
             email.setIncludeCulprits(includeCulprits);
             trigger.setEmail(email);
             configuredTriggers.add(trigger);
 
             return true;
 
         } else {
             return false;
         }
     }
 
     public String getRecipients(AbstractProject project) {
         return null;
     }
 
     public boolean removeMailer(AbstractProject project) throws IOException {
         return false;
     }
 
     public boolean addMailer(AbstractProject project) throws IOException {
         return false;
     }
 
     public boolean setRecipients(AbstractProject project, String recipients)
             throws IOException {
         return false;
     }
 
 }
