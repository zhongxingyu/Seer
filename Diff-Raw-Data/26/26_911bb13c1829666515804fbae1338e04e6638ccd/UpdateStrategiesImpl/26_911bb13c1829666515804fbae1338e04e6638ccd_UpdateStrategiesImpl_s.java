 package org.zeroturnaround.jenkins;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.zeroturnaround.jenkins.updateModes.FailBuild;
 import org.zeroturnaround.jenkins.updateModes.FullRestart;
 import org.zeroturnaround.jenkins.updateModes.Hotpatch;
 import org.zeroturnaround.jenkins.updateModes.LiveRebelDefault;
 import org.zeroturnaround.jenkins.updateModes.RollingRestarts;
 import org.zeroturnaround.liverebel.plugins.PluginUtil;
 import org.zeroturnaround.liverebel.plugins.UpdateMode;
 import org.zeroturnaround.liverebel.plugins.UpdateStrategies;
 
 public class UpdateStrategiesImpl implements Describable<UpdateStrategiesImpl>, UpdateStrategies {
 
   public final UpdateMode primary;
   public UpdateMode fallback;
   public boolean updateWithWarnings;
   public int sessionDrainTimeout;
   public int requestPauseTimeout;
   public final org.zeroturnaround.jenkins.updateModes.UpdateMode updateMode;
 
   @DataBoundConstructor
   public UpdateStrategiesImpl(org.zeroturnaround.jenkins.updateModes.UpdateMode updateMode) {
     this.updateMode = updateMode;
     if (updateMode instanceof RollingRestarts) {
       primary = UpdateMode.ROLLING_RESTARTS;
       sessionDrainTimeout = ((RollingRestarts) updateMode).sessionDrain;
     } else if (updateMode instanceof Hotpatch) {
       primary = UpdateMode.HOTPATCH;
       fallback = getFallback(((Hotpatch) updateMode).fallback);
       updateWithWarnings = ((Hotpatch) updateMode).updateWithWarnings;
       requestPauseTimeout = ((Hotpatch) updateMode).requestPause;
     }  else if (updateMode instanceof FullRestart) {
       primary = UpdateMode.OFFLINE;
       requestPauseTimeout = ((FullRestart) updateMode).connectionPause;
     } else {
       primary = UpdateMode.LIVEREBEL_DEFAULT;
     }
     if (requestPauseTimeout == 0) requestPauseTimeout = PluginUtil.DEFAULT_REQUEST_PAUSE;
     if (sessionDrainTimeout == 0) sessionDrainTimeout = PluginUtil.DEFAULT_SESSION_DRAIN;
   }
 
   private UpdateMode getFallback(org.zeroturnaround.jenkins.updateModes.UpdateMode updateMode) {
     if (updateMode instanceof RollingRestarts) {
       this.sessionDrainTimeout = ((RollingRestarts) updateMode).sessionDrain;
       return UpdateMode.ROLLING_RESTARTS;
     }
     else if (updateMode instanceof FullRestart)
       return UpdateMode.OFFLINE;
     else if (updateMode instanceof FailBuild)
       return UpdateMode.FAIL_BUILD;
     else if (updateMode instanceof LiveRebelDefault)
       return UpdateMode.LIVEREBEL_DEFAULT;
     return UpdateMode.LIVEREBEL_DEFAULT;
   }
 
   public UpdateMode getPrimaryUpdateStrategy() {
     return primary;
   }
 
   public UpdateMode getFallbackUpdateStrategy() {
     return fallback;
   }
 
   public boolean updateWithWarnings() {
     return updateWithWarnings;
   }
 
   public int getSessionDrainTimeout() {
     return sessionDrainTimeout;
   }
 
   public int getRequestPauseTimeout() {
     return requestPauseTimeout;
   }
 
   public DescriptorImpl getDescriptor() {
     return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
   }
 
   @Extension
   public static class DescriptorImpl extends Descriptor<UpdateStrategiesImpl> {
 
     @Override
     public String getDisplayName() {
       return "Update Strategies info";
     }
     public Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> getDefaultPrimaryUpdate() {
       DescriptorExtensionList<org.zeroturnaround.jenkins.updateModes.UpdateMode, Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> allDescriptors = Hudson.getInstance().getDescriptorList(org.zeroturnaround.jenkins.updateModes.UpdateMode.class);
       Iterator<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> it = allDescriptors.iterator();
       while (it.hasNext()) {
         Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> next = it.next();
         if (next.clazz == LiveRebelDefault.class) {
           return next;
         }
       }
       return null;
     }
 
     public List<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> getPrimaryUpdateModes() {
       DescriptorExtensionList<org.zeroturnaround.jenkins.updateModes.UpdateMode, Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> allDescriptors = Hudson.getInstance().getDescriptorList(org.zeroturnaround.jenkins.updateModes.UpdateMode.class);
       List<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> primaryUpdateModes = new ArrayList<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>>();
 
       Iterator<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> it = allDescriptors.iterator();
       while (it.hasNext()) {
         Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> next = it.next();
         if (next.clazz != FailBuild.class) {
           primaryUpdateModes.add(next);
         }
       }
 
      return primaryUpdateModes;
     }
   }
 }
