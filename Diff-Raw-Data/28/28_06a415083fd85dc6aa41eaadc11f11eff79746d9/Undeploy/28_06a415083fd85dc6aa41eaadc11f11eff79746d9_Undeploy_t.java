 package org.zeroturnaround.jenkins;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.zeroturnaround.liverebel.plugins.Server;
 import org.zeroturnaround.liverebel.plugins.ServersUtil;
 
 import com.zeroturnaround.liverebel.api.Forbidden;
 import com.zeroturnaround.liverebel.api.SchemaInfo;
 
 import hudson.Extension;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.util.FormValidation;
 import static org.apache.commons.lang.StringUtils.trimToNull;
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.filterFileServers;
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverCheckBoxToServer;
 import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverToServerCheckBox;
 
 public class Undeploy extends LiveRebelDeployBuilder.ActionWrapper {
 
   public final List<ServerCheckbox> servers;
   public final String undeployID;
   public final boolean hasDatabaseMigrations;
   public final String selectedSchema;
   public final String targetProxy;
   public final boolean hasStaticContent;
   public final List<ServerCheckbox> staticServers;
 
   @DataBoundConstructor
   public Undeploy(List<ServerCheckbox> servers, String undeployID,
                   SchemaSelectionForm schemaSelectionForm,
                   StaticContentForm staticContentForm) {
     this.servers = servers == null ? new ArrayList<ServerCheckbox>() : servers;
     this.undeployID = trimToNull(undeployID);
     if (schemaSelectionForm != null) {
       this.selectedSchema = schemaSelectionForm.getSelectedSchema();
       this.targetProxy = "";
       this.hasDatabaseMigrations = true;
     }
     else {
       this.selectedSchema = null;
       this.targetProxy = null;
       this.hasDatabaseMigrations = false;
     }
 
     if (staticContentForm != null) {
       this.staticServers = staticContentForm.getStaticServers();
       this.hasStaticContent = true;
     }
     else {
       this.staticServers = null;
       this.hasStaticContent = false;
     }
   }
 
   public List<ServerCheckbox> getServers() {
     return serverToServerCheckBox(new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), serverCheckBoxToServer(servers == null ? new ArrayList<ServerCheckbox>() : servers)).getServers());
   }
 
   public List<ServerCheckbox> getStaticServers() {
     return serverToServerCheckBox(filterFileServers(serverCheckBoxToServer(this.getServers()), this.staticServers));
   }
 
   public DescriptorImpl getDescriptor() {
     return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
   }
 
   @Override
   public String toString() {
     return "{ servers=" + servers + ", hasDatabaseMigrations=" + hasDatabaseMigrations + ", selectedSchema=" + selectedSchema + ",  }";
   }
 
   @Extension
   public static class DescriptorImpl extends Descriptor<LiveRebelDeployBuilder.ActionWrapper> {
 
     private List<Server> descriptorServers;
    private List<SchemaInfo> schemas = null;
     private String tempUniqueId;
 
     @Override
     public String getDisplayName() {
       return "Undeploy";
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
         if (availableServers.isEmpty()) return FormValidation.error("No connected servers!");
         boolean anyOnline = false;
         for (ServerCheckbox serverCheckbox : availableServers) {
           if (serverCheckbox.isConnected()) anyOnline = true;
         }
         if (!anyOnline) return FormValidation.warning("No online servers!");
       } catch (Forbidden e) {
         if (e.getMessage().contains("MANAGE_GROUPS")) {
           return FormValidation.error("User whose authentication token is used must have MANAGE_GROUPS permission!");
         } else throw e;
       }
       return FormValidation.ok();
     }
   }
 }
