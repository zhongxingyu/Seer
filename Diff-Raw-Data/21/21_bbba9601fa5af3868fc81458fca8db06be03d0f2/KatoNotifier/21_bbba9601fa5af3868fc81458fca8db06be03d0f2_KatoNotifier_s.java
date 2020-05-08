 package jenkins.plugins.kato;
 
 import hudson.Util;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.export.Exported;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.logging.Logger;
 
 @SuppressWarnings({"unchecked"})
 public class KatoNotifier extends Notifier {
 
     private static final Logger logger = Logger.getLogger(KatoNotifier.class.getName());
 
     private String buildServerUrl;
     private String room;
     private String sendAs;
 
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

     public String getRoom() {
        return room;
     }
 
     public String getBuildServerUrl() {
         return buildServerUrl;
     }
 
     public String getSendAs() {
        return sendAs;
     }
 
     public void setBuildServerUrl(final String buildServerUrl) {
         this.buildServerUrl = buildServerUrl;
     }
 
     public void setRoom(final String room) {
         this.room = room;
     }
 
     public void setSendAs(final String sendAs) {
         this.sendAs = sendAs;
     }
 
     @DataBoundConstructor
     public KatoNotifier(final String room, String buildServerUrl, final String sendAs) {
         super();
         this.buildServerUrl = buildServerUrl;
         this.room = room;
         this.sendAs = sendAs;
     }
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.BUILD;
     }
 
     public KatoService newKatoService(final String room) {
         String sendAs = Util.fixEmpty(getSendAs());
         return new StandardKatoService(room == null ? getRoom() : room, sendAs == null ? "Build Server" : sendAs);
     }
 
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
         return true;
     }
 
     @Extension
     public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
         private String room;
         private String buildServerUrl;
         private String sendAs;
 
         public DescriptorImpl() {
             load();
         }
 
         public String getRoom() {
             return room;
         }
 
         public String getBuildServerUrl() {
             return buildServerUrl;
         }
 
         public String getSendAs() {
             return sendAs;
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             return true;
         }
 
         @Override
         public KatoNotifier newInstance(StaplerRequest sr) {
             if (buildServerUrl == null) buildServerUrl = sr.getParameter("katoBuildServerUrl");
             if (room == null) room = sr.getParameter("katoRoom");
             if (sendAs == null) sendAs = sr.getParameter("katoSendAs");
             return new KatoNotifier(room, buildServerUrl, sendAs);
         }
 
         @Override
         public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
             room = sr.getParameter("katoRoom");
             buildServerUrl = sr.getParameter("katoBuildServerUrl");
             sendAs = sr.getParameter("katoSendAs");
             if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
                 buildServerUrl = buildServerUrl + "/";
             }
             try {
                 new KatoNotifier(room, buildServerUrl, sendAs);
             } catch (Exception e) {
                 throw new FormException("Failed to initialize notifier - check your global notifier configuration settings", e, "");
             }
             save();
             return super.configure(sr, formData);
         }
 
         @Override
         public String getDisplayName() {
             return "Kato Notifications";
         }
     }
 
     public static class KatoJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {
         private String room;
         private boolean startNotification;
         private boolean notifySuccess;
         private boolean notifyAborted;
         private boolean notifyNotBuilt;
         private boolean notifyUnstable;
         private boolean notifyFailure;
 
 
         @DataBoundConstructor
         public KatoJobProperty(String room, boolean startNotification, boolean notifyAborted, boolean notifyFailure, boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable) {
             this.room = room;
             this.startNotification = startNotification;
             this.notifyAborted = notifyAborted;
             this.notifyFailure = notifyFailure;
             this.notifyNotBuilt = notifyNotBuilt;
             this.notifySuccess = notifySuccess;
             this.notifyUnstable = notifyUnstable;
         }
 
         @Exported
         public String getRoom() {
             return room;
         }
 
         @Exported
         public boolean getStartNotification() {
             return startNotification;
         }
 
         @Exported
         public boolean getNotifySuccess() {
             return notifySuccess;
         }
 
         @Override
         public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
             if (startNotification) {
                 Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
                 for (Publisher publisher : map.values()) {
                     if (publisher instanceof KatoNotifier) {
                         logger.info("Invoking Started...");
                         new ActiveNotifier((KatoNotifier) publisher).started(build);
                     }
                 }
             }
             return super.prebuild(build, listener);
         }
 
         @Exported
         public boolean getNotifyAborted() {
             return notifyAborted;
         }
 
         @Exported
         public boolean getNotifyFailure() {
             return notifyFailure;
         }
 
         @Exported
         public boolean getNotifyNotBuilt() {
             return notifyNotBuilt;
         }
 
         @Exported
         public boolean getNotifyUnstable() {
             return notifyUnstable;
         }
 
         @Extension
         public static final class DescriptorImpl extends JobPropertyDescriptor {
             public String getDisplayName() {
                 return "Kato Notifications";
             }
 
             @Override
             public boolean isApplicable(Class<? extends Job> jobType) {
                 return true;
             }
 
             @Override
             public KatoJobProperty newInstance(StaplerRequest sr, JSONObject formData) throws hudson.model.Descriptor.FormException {
                 return new KatoJobProperty(sr.getParameter("katoProjectRoom"),
                         sr.getParameter("katoStartNotification") != null,
                         sr.getParameter("katoNotifyAborted") != null,
                         sr.getParameter("katoNotifyFailure") != null,
                         sr.getParameter("katoNotifyNotBuilt") != null,
                         sr.getParameter("katoNotifySuccess") != null,
                         sr.getParameter("katoNotifyUnstable") != null);
             }
         }
     }
 }
