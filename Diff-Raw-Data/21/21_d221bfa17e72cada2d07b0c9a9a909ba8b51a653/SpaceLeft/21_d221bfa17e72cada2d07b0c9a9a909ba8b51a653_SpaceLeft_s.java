 package hudson.plugins.space_left;
 
 import hudson.FilePath;
 import hudson.model.*;
 import hudson.remoting.VirtualChannel;
 import jenkins.model.Jenkins;
 import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Represents an object that knows, how much space is left to execute a job
  *
  * @author ffromm, Frederik Fromm
  */
 public class SpaceLeft {
 
     /**
      * Returns the free disk space
      *
      * @param slave          slave to get the free space from
      * @param currentProject current project that may have passed custom set space needed
      * @param spaceNeeded    custom set space needed by jop parameter REQUIRED_SPACE
      * @return the free disk space
      * @throws java.io.IOException
      * @throws InterruptedException
      */
     public Long getFreeSpace(Slave slave, AbstractProject<FreeStyleProject, FreeStyleBuild> currentProject, long spaceNeeded) throws IOException, InterruptedException {
         FilePath p = slave.getRootPath();
 
         if (p == null) {
            return null;
         }
 
         Long freeSpaceOnSlave = p.act(new GetUsableSpace());
 
         if (spaceNeeded > 0) {
             freeSpaceOnSlave -= spaceNeeded;
         } else if (currentProject != null) {
             freeSpaceOnSlave -= this.getRequiredSpace(currentProject);
         }
 
         FilePath workspace = p.child("workspace");
 
         if (workspace.exists()) {
             for (FilePath projectDir : workspace.listDirectories()) {
 
                 TopLevelItem topLevelItem = Jenkins.getInstance().getItem(projectDir.getName());
 
                 if (topLevelItem instanceof AbstractProject) {
                     AbstractProject project = (AbstractProject) topLevelItem;
 
                     Long requiredSpace = this.getRequiredSpace(project);
                     freeSpaceOnSlave -= requiredSpace;
 
                     if (freeSpaceOnSlave <= 0) {
                         freeSpaceOnSlave = 0L;
                         break;
                     }
                 }
             }
         }
 
         return freeSpaceOnSlave;
     }
 
     /**
      * Returns the value of the SpaceLeftProperty.getRequiredSpace method. If the property is not set, 0 is returned.
      * @param project the project containing SpaceLeftProperty.getRequiredSpace
      * @return Returns the value of the SpaceLeftProperty.getRequiredSpace method
      */
     public Long getRequiredSpace(AbstractProject project) {
         SpaceLeftProperty spaceLeftProperty = (SpaceLeftProperty) project.getProperty(SpaceLeftProperty.class);
 
         if(spaceLeftProperty != null) {
             return spaceLeftProperty.getRequiredSpace();
         }
         return 0L;
     }
 
     /**
      * inner class that is executed on slave to get free disk space
      */
     protected static final class GetUsableSpace implements FilePath.FileCallable<Long> {
         @IgnoreJRERequirement
         public Long invoke(File f, VirtualChannel channel) throws IOException {
             try {
                 long s = f.getUsableSpace();
 
                 if (s <= 0) {
                     return null;
                 }
 
                 return s;
             } catch (LinkageError e) {
                 // pre-mustang
                 return null;
             }
         }
 
         private static final long serialVersionUID = 1L;
     }
 }
