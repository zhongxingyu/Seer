 package de.deepamehta.plugins.workspaces;
 
 import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.plugin.DeepaMehtaPlugin;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 public class WorkspacesPlugin extends DeepaMehtaPlugin {
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     @Override
    public void preCreateHook(Topic topic, Map<String, String> clientContext) {
     }
 
     @Override
    public void postCreateHook(Topic topic, Map<String, String> clientContext) {
        String workspaceId = clientContext.get("workspace_id");
        if (workspaceId != null) {
            dms.createRelation("RELATION", topic.id, Long.parseLong(workspaceId), null);
        } else {
            logger.warning("Topic " + topic + " can't be related to a workspace (current workspace is unknown)");
        }
     }
 
     // ---
 
     @Override
     public String getClientPlugin() {
         return "dm3_workspaces.js";
     }
 
     @Override
     public int requiredDBModelVersion() {
         return 1;
     }
 }
