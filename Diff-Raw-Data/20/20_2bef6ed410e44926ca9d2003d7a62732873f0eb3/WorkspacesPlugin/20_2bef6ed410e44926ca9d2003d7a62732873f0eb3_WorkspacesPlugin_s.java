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
    public void preCreateHook(Topic topic, HashMap clientContext) {
     }
 
     @Override
    public void postCreateHook(Topic topic, HashMap clientContext) {
        long workspaceId = (Long) clientContext.get("workspace_id");
        dms.createRelation("RELATION", topic.id, workspaceId, null);
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
