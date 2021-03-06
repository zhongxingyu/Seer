 package org.jenkinsci.plugins.xunit;
 
 import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
 import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
 import hudson.DescriptorExtensionList;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 import hudson.tasks.junit.JUnitResultArchiver;
 import hudson.tasks.test.TestResultProjectAction;
 import org.jenkinsci.lib.dryrun.DryRun;
 import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
 import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
 import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
 import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 /**
  * Class that converting custom reports to Junit reports and records them
  *
  * @author Gregory Boissinot
  */
 @SuppressWarnings({"unchecked", "unused"})
 public class XUnitPublisher extends Recorder implements DryRun, Serializable {
 
     private TestType[] types;
     private XUnitThreshold[] thresholds;
     private int thresholdMode;
 
    /**
     * Computed
     */
    private XUnitProcessor xUnitProcessor;

     public XUnitPublisher(TestType[] types, XUnitThreshold[] thresholds) {
         this.types = types;
         this.thresholds = thresholds;
        xUnitProcessor = new XUnitProcessor(types, thresholds, thresholdMode);
     }
 
     @DataBoundConstructor
     public XUnitPublisher(TestType[] tools, XUnitThreshold[] thresholds, int thresholdMode) {
         this.types = tools;
         this.thresholds = thresholds;
         this.thresholdMode = thresholdMode;
        xUnitProcessor = new XUnitProcessor(types, thresholds, thresholdMode);
     }
 
     public TestType[] getTypes() {
         return types;
     }
 
     public XUnitThreshold[] getThresholds() {
         return thresholds;
     }
 
     public int getThresholdMode() {
         return thresholdMode;
     }
 
     @Override
     public Action getProjectAction(AbstractProject<?, ?> project) {
         JUnitResultArchiver jUnitResultArchiver = project.getPublishersList().get(JUnitResultArchiver.class);
         if (jUnitResultArchiver == null) {
             return new TestResultProjectAction(project);
         }
         return null;
     }
 
     @Override
     public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
             throws InterruptedException, IOException {
         return xUnitProcessor.performXUnit(false, build, listener);
     }
 
     public boolean performDryRun(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
             throws InterruptedException, IOException {
         try {
             xUnitProcessor.performXUnit(true, build, listener);
         } catch (Throwable t) {
             listener.getLogger().println("[ERROR] - There is an error: " + t.getCause().getMessage());
         }
         //Always exit on success (returned code and status)
         build.setResult(Result.SUCCESS);
         return true;
     }
 
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.NONE;
     }
 
     @Extension
     @SuppressWarnings("unused")
     public static final class XUnitDescriptorPublisher extends BuildStepDescriptor<Publisher> {
 
         public XUnitDescriptorPublisher() {
             super(XUnitPublisher.class);
             load();
         }
 
         @Override
         public String getDisplayName() {
             return Messages.xUnit_PublisherName();
         }
 
         @Override
         public boolean isApplicable(Class type) {
             return true;
         }
 
         @Override
         public String getHelpFile() {
             return "/plugin/xunit/help.html";
         }
 
         public DescriptorExtensionList<TestType, TestTypeDescriptor<?>> getListXUnitTypeDescriptors() {
             return TestTypeDescriptor.all();
         }
 
         public DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> getListXUnitThresholdDescriptors() {
             return XUnitThresholdDescriptor.all();
         }
 
         public XUnitThreshold[] getListXUnitThresholdInstance() {
             return new XUnitThreshold[]{
                     new FailedThreshold(),
                     new SkippedThreshold()
             };
         }
 
     }
 
 }
