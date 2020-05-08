 package de.deepamehta.plugins.workspaces;
 
 import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.plugin.DeepaMehtaPlugin;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 public class WorkspacesPlugin extends DeepaMehtaPlugin {
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // Note: we must use the postCreateHook to create the relation because at pre_create the document has no ID yet.
     @Override
     public void postCreateHook(Topic topic, Map<String, String> clientContext) {
         // Note 1: we do not relate search results to a workspace. Otherwise the search result would appear
        // as relation when displaying the workspace. That's because an "Auxiliray" relation is not be
         // created if there is another relation already.
         // Note 2: we do not relate workspaces to a workspace. This would be contra-intuitive.
        if (!topic.typeId.equals("Serch Result") && !topic.typeId.equals("Workspace")) {
             String workspaceId = clientContext.get("workspace_id");
             if (workspaceId != null) {
                 dms.createRelation("RELATION", topic.id, Long.parseLong(workspaceId), null);
             } else {
                logger.warning("Topic " + topic + " can't be related to a workspace (current workspace is unknown)");
             }
         } else {
            logger.info("### " + topic + " is not assigned to a workspace");
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
