 package nl.codecentric.jenkins.appd.util;
 
 import java.util.ResourceBundle;
 
 /**
  * Class provides access to the Messages {@link ResourceBundle} and hides initialization of the
  * properties file.
  */
 public enum LocalMessages {
 
   PROJECTACTION_DISPLAYNAME("AppDynamicsProjectAction.DisplayName"),
   BUILDACTION_DISPLAYNAME("AppDynamicsBuildAction.DisplayName"),
   PUBLISHER_DISPLAYNAME("AppDynamicsResultsPublisher.DisplayName"),
   REPORT_DISPLAYNAME("AppDynamicsReport.DisplayName");
 
  private final static ResourceBundle MESSAGES = ResourceBundle.getBundle("nl.codecentric.jenkins.appd.Messages");
   private final String msgRef;
 
 
   private LocalMessages(final String msgReference) {
     msgRef = msgReference;
   }
 
   @Override
   public String toString() {
     return MESSAGES.getString(msgRef);
   }
 }
