 /*
  * The MIT License
  * 
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Brian Westrich, Martin Eigenbrodt
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package hudson.plugins.downstream_ext;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.matrix.MatrixAggregatable;
 import hudson.matrix.MatrixAggregator;
 import hudson.matrix.MatrixBuild;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.DependecyDeclarer;
 import hudson.model.DependencyGraph;
 import hudson.model.Hudson;
 import hudson.model.Item;
 import hudson.model.Items;
 import hudson.model.Job;
 import hudson.model.Project;
 import hudson.model.Result;
 import hudson.model.Run;
 import hudson.model.Cause.UpstreamCause;
 import hudson.model.listeners.ItemListener;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.BuildTrigger;
 import hudson.tasks.Messages;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Triggers builds of other projects.
  *
  * Note that this class is in large parts copied & pasted from {@link BuildTrigger} (rev. 21890)
  */
 @SuppressWarnings("unchecked")
 public class DownstreamTrigger extends Recorder implements DependecyDeclarer, MatrixAggregatable {
 
     /**
      * Comma-separated list of other projects to be scheduled.
      */
     private String childProjects;
 
     /**
      * Threshold status to trigger other builds.
      */
    private Result threshold = Result.SUCCESS;
     
     private final boolean onlyIfSCMChanges;
 
     @DataBoundConstructor
    public DownstreamTrigger(String childProjects, String threshold, boolean onlyIfSCMChanges) {
        this(childProjects, resultFromString(threshold), onlyIfSCMChanges);
     }
 
     public DownstreamTrigger(String childProjects, Result threshold, boolean onlyIfSCMChanges) {
         if(childProjects==null)
             throw new IllegalArgumentException();
         this.childProjects = childProjects;
         this.threshold = threshold;
         this.onlyIfSCMChanges = onlyIfSCMChanges;
     }
    
    private static Result resultFromString(String s) {
    	Result result = Result.fromString(s);
    	// fromString returns FAILURE for unknown strings instead of
    	// IllegalArgumentException. Don't know why the author thought that this
    	// is useful ...
    	if (!result.toString().equals(s)) {
    		throw new IllegalArgumentException("Unknown result type '" + s + "'");
    	}
    	return result;
    }
 
     public String getChildProjectsValue() {
         return childProjects;
     }
 
     public Result getThreshold() {
         if(threshold==null)
             return Result.SUCCESS;
         else
             return threshold;
     }
     
     public boolean isOnlyIfSCMChanges() {
     	return onlyIfSCMChanges;
     }
 
     public List<AbstractProject> getChildProjects() {
         return Items.fromNameList(childProjects,AbstractProject.class);
     }
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.NONE;
     }
     
     @Override
 	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
         if(build.getResult().isBetterOrEqualTo(getThreshold())) {
             PrintStream logger = listener.getLogger();
             //Trigger downstream projects of the project defined by this trigger
             List <AbstractProject> downstreamProjects = getChildProjects();
                 
             for (AbstractProject p : downstreamProjects) {
                 if(p.isDisabled()) {
                     logger.println(Messages.BuildTrigger_Disabled(p.getName()));
                     continue;
                 }
                 
                 if(isOnlyIfSCMChanges() && !p.pollSCMChanges(listener)) {
                 	logger.println(hudson.plugins.downstream_ext.Messages.DownstreamTrigger_NoSCMChanges(p.getName()));
                 	continue;
                 }
                 // this is not completely accurate, as a new build might be triggered
                 // between these calls
                 String name = p.getName()+" #"+p.getNextBuildNumber();
                 if(p.scheduleBuild(new UpstreamCause((Run)build))) {
                     logger.println(Messages.BuildTrigger_Triggering(name));
                 } else {
                     logger.println(Messages.BuildTrigger_InQueue(name));
                 }
             }
         }
 
         return true;
     }
 
     public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
     	// Must not do this, otherwise BuildTrigger would recognize the downstream
     	// dep and start the downstream build, no matter what we do ourself.
     	
         //graph.addDependency(owner,getChildProjects());
     }
 
     @Override
     public boolean needsToRunAfterFinalized() {
         return true;
     }
 
     @Override
     public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
         return new MatrixAggregator(build, launcher, listener) {
             @Override
             public boolean endBuild() throws InterruptedException, IOException {
                 return perform(build, launcher, listener);
             }
         };
     }
 
     /**
      * Called from {@link Job#renameTo(String)} when a job is renamed.
      *
      * @return true
      *      if this {@link DownstreamTrigger} is changed and needs to be saved.
      */
     public boolean onJobRenamed(String oldName, String newName) {
         // quick test
         if(!childProjects.contains(oldName))
             return false;
 
         boolean changed = false;
 
         // we need to do this per string, since old Project object is already gone.
         String[] projects = childProjects.split(",");
         for( int i=0; i<projects.length; i++ ) {
             if(projects[i].trim().equals(oldName)) {
                 projects[i] = newName;
                 changed = true;
             }
         }
 
         if(changed) {
             StringBuilder b = new StringBuilder();
             for (String p : projects) {
                 if(b.length()>0)    b.append(',');
                 b.append(p);
             }
             childProjects = b.toString();
         }
 
         return changed;
     }
 
     private Object readResolve() {
         if(childProjects==null)
             return childProjects="";
         return this;
     }
 
     @Extension
     public static class DescriptorImpl extends BuildTrigger.DescriptorImpl {
    	
    	public static final String[] THRESHOLD_VALUES = {
    		Result.SUCCESS.toString(), Result.UNSTABLE.toString(), Result.FAILURE.toString()
    	};
    	
         @Override
 		public String getDisplayName() {
             return hudson.plugins.downstream_ext.Messages.DownstreamTrigger_DisplayName();
         }
 
         @Override
         public String getHelpFile() {
             return "/help/project-config/downstream.html";
         }
 
         @Override
         public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return new DownstreamTrigger(
                 formData.getString("childProjects"),
                formData.getString("threshold"),
                 formData.has("onlyIfSCMChanges") && formData.getBoolean("onlyIfSCMChanges"));
         }
 
         @Extension
         public static class ItemListenerImpl extends ItemListener {
             @Override
             public void onRenamed(Item item, String oldName, String newName) {
                 // update DownstreamPublisher of other projects that point to this object.
                 // can't we generalize this?
                 for( Project<?,?> p : Hudson.getInstance().getProjects() ) {
                     DownstreamTrigger t = p.getPublishersList().get(DownstreamTrigger.class);
                     if(t!=null) {
                         if(t.onJobRenamed(oldName,newName)) {
                             try {
                                 p.save();
                             } catch (IOException e) {
                                 LOGGER.log(Level.WARNING, "Failed to persist project setting during rename from "+oldName+" to "+newName,e);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private static final Logger LOGGER = Logger.getLogger(DownstreamTrigger.class.getName());
 }
