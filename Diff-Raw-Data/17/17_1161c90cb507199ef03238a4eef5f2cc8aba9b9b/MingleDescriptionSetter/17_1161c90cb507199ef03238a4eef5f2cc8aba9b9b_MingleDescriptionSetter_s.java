 package mingleplugin;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
 
 public class MingleDescriptionSetter extends Recorder {
   private static final Logger LOGGER = Logger.getLogger(MingleChangeLogAnnotator.class.getName());
 
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
   BuildListener listener) throws InterruptedException {
     LOGGER.info("Started MingleDescriptionSetter");
 
     AbstractProject<?,?> project = build.getProject();
     MingleRestService service = MingleRestService.get(project);
 
     //TODO: getProjectActions() -> Returns action objects if THIS BUILDSTEP has actions to contribute to a Project. :/
     MingleBuildAction action = (MingleBuildAction) getProjectActions(project);
     List<Integer> cardIds = action.getCardIds();
     Iterator<Integer> myListIterator = cardIds.iterator();
     String newDescription = "__TEST__:</br>\n";
 
     while (myListIterator.hasNext()) {
       int id = (int) myListIterator.next();
       URL cardurl;
       try {
         cardurl = service.getCardUrl(id);
       } catch (MalformedURLException e) {
         listener.getLogger().println("Couldn't get URL for Card " + id);
         // skip the rest
         continue;
       }
       //TODO: Card headline here
       newDescription += "<a href=\""+cardurl.toString()+"\">#"+id+"</a><br/>\\n"; 
     }
     // 	result = build.getEnvironment(listener).expand(result);.
     listener.getLogger().println("Description set: " + newDescription);
     try {
       build.setDescription(newDescription);
     } catch (IOException e) {
       listener.getLogger().println("Couldn't set the build description!");
     }
 
     //if anything goes wrong throw new AbortException()
 
     return true;
   }
 
   public BuildStepMonitor getRequiredMonitorService() {
     return BuildStepMonitor.NONE;
   }
 
   @Extension
   public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 
     public DescriptorImpl() {
       super(MingleDescriptionSetter.class);
     }
 
     @Override
     public String getDisplayName() {
       return "MingleDescriptionSetter";
     }
 
     @Override
     public boolean isApplicable(Class<? extends AbstractProject> jobType) {
       return true;
     }
 
   }
 
   @Override
   public DescriptorImpl getDescriptor() {
     return (DescriptorImpl) super.getDescriptor();
   }
 
 }
