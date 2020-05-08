 package org.zeroturnaround.jenkins;
 
 import hudson.Extension;
 import hudson.model.Describable;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.zeroturnaround.liverebel.plugins.ServersUtil;
 
 import java.util.List;
 import java.util.UUID;
 
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverCheckBoxToServer;
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverToServerCheckBox;
 
 public class DeployOrUpdate implements Describable<DeployOrUpdate> {
 
   public final String contextPath;
   private String contextPathWithEnvVarReplaced;
 
   public final UpdateStrategiesImpl updateStrategies;
   public final List<ServerCheckbox> servers;
 
   @DataBoundConstructor
   public DeployOrUpdate(String contextPath, UpdateStrategiesImpl updateStrategies, List<ServerCheckbox> servers) {
     this.contextPath = StringUtils.trimToNull(contextPath);
     this.updateStrategies = updateStrategies;
     this.servers = servers;
   }
 

   public String getContextPath() {
    return contextPath;
  }
  public String getContextPathWithEnv() {
     if (contextPathWithEnvVarReplaced == null) {
       return contextPath;
     }
     else {
       return contextPathWithEnvVarReplaced;
     }
   }
 
   public void setContextPathWithEnvVarReplaced(String contextPathWithEnvVarReplaced) {
     this.contextPathWithEnvVarReplaced = contextPathWithEnvVarReplaced;
   }
 
   @Override
   public String toString() {
     return "{ contextPath=" + contextPath + "; updateStrategies=" + updateStrategies + "; servers=" + servers + " }";
   }
 
   public List<ServerCheckbox> getServers() {
     return serverToServerCheckBox(new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), serverCheckBoxToServer(servers)).getServers());
   }
 
   public DescriptorImpl getDescriptor() {
     return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
   }
 
   @Extension
   public static class DescriptorImpl extends Descriptor<DeployOrUpdate> {
 
     @Override
     public String getDisplayName() {
       return "Deploy or distribute";
     }
 
     public List<ServerCheckbox> getDefaultServers() {
       return serverToServerCheckBox(new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), null).getDefaultServers());
     }
 
     public String getUniqueId() {
       return UUID.randomUUID().toString();
     }
 
   }
 
 }
