 package org.zeroturnaround.jenkins;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.zeroturnaround.jenkins.util.ArtifactAndMetadataDescriptor;
 import org.zeroturnaround.liverebel.plugins.Server;
 import org.zeroturnaround.liverebel.plugins.ServersUtil;
 
 import com.zeroturnaround.liverebel.api.Forbidden;
 import com.zeroturnaround.liverebel.api.SchemaInfo;
 
 import hudson.Extension;
 import hudson.model.Hudson;
 import hudson.util.FormValidation;
 import static org.apache.commons.lang.StringUtils.trimToNull;
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverCheckBoxToServer;
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverToServerCheckBox;
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.filterFileServers;
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.filterProxyServers;
 
 public class DeployOrUpdate extends LiveRebelDeployBuilder.ActionWrapper {
 
   public final String contextPath;
   public final String artifact;
   public final String app;
   public final String ver;
   public final String trace;
   private String contextPathWithEnvVarReplaced;
 
   public final UpdateStrategiesImpl updateStrategies;
   public final List<ServerCheckbox> servers;
   public final List<ServerCheckbox> staticServers;
   public final String virtualHost;
   public final boolean isOverride;
   public final boolean hasStaticContent;
   public final boolean hasDatabaseMigrations;
   public final String filePath;
   public final String selectedSchema;
   public final String targetProxy;
   public final String destinationFileName;
 
 
   @DataBoundConstructor
   public DeployOrUpdate(String contextPath, String artifact, String trace, UpdateStrategiesImpl updateStrategies, List<ServerCheckbox> servers, OverrideForm overrideForm,
                         String destinationFileName,
                         boolean destinationFileNameForm,
                         String virtualHost,
                         boolean virtualHostForm,
                         StaticContentForm staticContentForm,
                         SchemaSelectionForm schemaSelectionForm) {
     this.artifact = trimToNull(artifact);
     this.trace = trimToNull(trace);
     this.contextPath = trimToNull(contextPath);
     this.updateStrategies = updateStrategies;
     this.servers = servers == null ? new ArrayList<ServerCheckbox>() : servers;
 
     if (overrideForm != null) {
       this.app = trimToNull(overrideForm.getApp());
       this.ver = trimToNull(overrideForm.getVer());
       this.isOverride = true;
     } else {
       this.app = null;
       this.ver = null;
       this.isOverride = false;
     }
     if (staticContentForm != null) {
       this.staticServers = staticContentForm.getStaticServers();
       this.filePath = trimToNull(staticContentForm.getFilePath());
       this.hasStaticContent = true;
     }
     else {
       this.staticServers = null;
       this.filePath = null;
       this.hasStaticContent = false;
     }
 
     if (schemaSelectionForm != null) {
       this.selectedSchema = schemaSelectionForm.getSelectedSchema();
       this.targetProxy = schemaSelectionForm.getTargetProxy();
       this.hasDatabaseMigrations = true;
     }
     else {
       this.selectedSchema = null;
       this.targetProxy = null;
      this.hasDatabaseMigrations = false;
     }
 
     if (virtualHostForm) {
       this.virtualHost = trimToNull(virtualHost);
     }
     else {
       this.virtualHost = null;
     }
 
     if (destinationFileNameForm) {
       this.destinationFileName = trimToNull(destinationFileName);
     }
     else {
       this.destinationFileName = null;
     }
   }
 
   public String getContextPath() {
     return contextPath;
   }
 
   public String getFilePath() {
     return filePath;
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
     return "{ contextPath=" + contextPath + "; updateStrategies=" + updateStrategies + "; servers=" +
         servers + "; virtualHost=" + virtualHost + "; staticServers=" + staticServers + "; filePath=" + filePath + "; }";
   }
 
   public List<ServerCheckbox> getServers() {
     return serverToServerCheckBox(new ServersUtil(
         LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(),
         serverCheckBoxToServer(servers == null ? new ArrayList<ServerCheckbox>() : servers)).getServers());
   }
 
   public List<ServerCheckbox> getStaticServers() {
     return serverToServerCheckBox(filterFileServers(serverCheckBoxToServer(this.getServers()), this.staticServers));
   }
 
   public boolean isDefaultHostSelected() {
     return this.virtualHost == null || this.virtualHost.equalsIgnoreCase("default host");
   }
 
   public boolean isDestinationFileNameSelected() {
     return trimToNull(this.destinationFileName) == null;
   }
 
   public DescriptorImpl getDescriptor() {
     return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
   }
 
   @Extension
   public static class DescriptorImpl extends ArtifactAndMetadataDescriptor<LiveRebelDeployBuilder.ActionWrapper> {
     private List<Server> descriptorServers;
    private List<SchemaInfo> schemas = null;
     private String tempUniqueId;
 
     @Override
     public String getDisplayName() {
       return "Deploy or update";
     }
 
     public List<ServerCheckbox> getDefaultServers() {
       descriptorServers = new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), null).getDefaultServers();
       return serverToServerCheckBox(descriptorServers);
     }
 
     public List<ServerCheckbox> getDefaultStaticServers() {
       return serverToServerCheckBox(filterFileServers(descriptorServers, null));
     }
 
     public List<SchemaInfo> getDefaultSchemas() {
      schemas = new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), null).getSchemas();
       return schemas;
     }
 
     public List<SchemaInfo> getDefaultSchemasByServer(String serverId) {
       if (serverId == null || serverId.length() == 0)
         return new ArrayList<SchemaInfo>();
      schemas = new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), null).getSchemas();
       List<SchemaInfo> schemasByServer = new ArrayList<SchemaInfo>();
       for (SchemaInfo schemaInfo : schemas) {
         if (schemaInfo.getServerId().equalsIgnoreCase(serverId))
           schemasByServer.add(schemaInfo);
       }
       return schemasByServer;
     }
 
     public List<ServerCheckbox> getDefaultProxyServers() {
       return serverToServerCheckBox(filterProxyServers(descriptorServers));
     }
 
     public String getCurrentUniqueId() {
       return tempUniqueId;
     }
 
     public String getUniqueId() {
       tempUniqueId = "a" + UUID.randomUUID().toString();
       return tempUniqueId;
     }
 
     public FormValidation doCheckTestServers() throws IOException, ServletException {
       try {
         List<ServerCheckbox> availableServers = getDefaultServers();
         if (availableServers.isEmpty())
           return FormValidation.error("No connected servers!");
         boolean anyOnline = false;
         for (ServerCheckbox serverCheckbox : availableServers) {
           if (serverCheckbox.isConnected())
             anyOnline = true;
         }
         if (!anyOnline)
           return FormValidation.warning("No online servers!");
       }
       catch (Forbidden e) {
         if (e.getMessage().contains("MANAGE_GROUPS")) {
           return FormValidation.error("User whose authentication token is used must have MANAGE_GROUPS permission!");
         }
         else
           throw e;
       }
       return FormValidation.ok();
     }
   }
 
 }
