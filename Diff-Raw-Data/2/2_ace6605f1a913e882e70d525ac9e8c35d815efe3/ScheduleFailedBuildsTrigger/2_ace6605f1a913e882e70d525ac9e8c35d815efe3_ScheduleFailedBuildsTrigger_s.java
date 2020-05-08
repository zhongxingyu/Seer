 package com.progress.hudson;
 
 import static hudson.Util.fixNull;
 import hudson.model.BuildableItem;
 import hudson.model.Item;
 import hudson.triggers.Trigger;
 import hudson.triggers.TriggerDescriptor;
 import hudson.util.FormFieldValidator;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import antlr.ANTLRException;
 
 /** Triggers a build when a project has failed previously and is scheduled for a retry. 
  * 
  * @author Stefan Fritz <sfritz@progress.com>
  * */
 public class ScheduleFailedBuildsTrigger extends Trigger<BuildableItem> {
 
     
     public ScheduleFailedBuildsTrigger(String crontab) throws ANTLRException{
       super(crontab); //run was never called when using the default constructor!? 
     }
   
     
     @Override
     public void run() {      
         if(FailedBuildsQueue.needsBuild(job)){          
                job.scheduleBuild();
         }
     }  
     
     @Override
     public TriggerDescriptor getDescriptor() {
         return DESCRIPTOR;
     }
     
     /**
      * Descriptor should be singleton.
      */
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
     
     public static final class DescriptorImpl extends TriggerDescriptor {
 
         public DescriptorImpl() {
             super(ScheduleFailedBuildsTrigger.class);
         }
 
         @Override
         public boolean isApplicable(Item item) {
             return true;
         }
 
         @Override
         public String getDisplayName() {
             return "Schedule failed Builds regularly (see Retry interval @ScheduleFailedBuildsPublisher)";
         }
         
        
   
         
         @Override
         public ScheduleFailedBuildsTrigger newInstance(StaplerRequest req) throws FormException {
                      
                   try {
                     return new ScheduleFailedBuildsTrigger("* * * * *"); 
                   } catch (ANTLRException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                     return null;
                   }
                
         }
 
     }
 
   
 
 }
