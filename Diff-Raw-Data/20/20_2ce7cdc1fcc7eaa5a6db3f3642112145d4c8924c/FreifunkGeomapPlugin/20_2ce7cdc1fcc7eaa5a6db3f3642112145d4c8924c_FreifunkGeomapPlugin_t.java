 package net.freifunk.dm3plugins.geomap;
 
 import de.deepamehta.plugins.workspaces.service.WorkspacesService;
 
 import de.deepamehta.plugins.accesscontrol.AccessControlPlugin.Permission;
 import de.deepamehta.plugins.accesscontrol.AccessControlPlugin.Role;
 import de.deepamehta.plugins.accesscontrol.model.Permissions;
 import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
 
 import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.service.Plugin;
 import de.deepamehta.core.service.PluginService;
 
 import java.util.logging.Logger;
 
 
 
 public class FreifunkGeomapPlugin extends Plugin {
 
     private static final String FREIFUNK_WORKSPACE_NAME = "Freifunk";
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private WorkspacesService wsService;
     private AccessControlService acService;
 
     // service availability book keeping
     private boolean performWorkspaceInitialization;
     private boolean performACLInitialization;
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // **************************************************
     // *** Hooks (called from DeepaMehta 3 framework) ***
     // **************************************************
 
 
 
     /**
      * Creates the "Freifunk" workspace and setup the ACLs.
      * <p>
      * Note: this relies on the Workspaces service <i>and</i> the Access Control service respectively.
      * If the services are not yet available the respective actions are postponed till the services arrive.
      * See {@link serviceArrived}.
      * <p>
      * TODO: the undeterministic service availability requires the plugin developer to do extra book keeping
      * and furthermore results in doubling the event handler calls. To alleviate this situation the
      * framework should provide a higher-level event mechanism to allow the developer to define "group events":
      * "execute this handler once event A <i>and</i> event B had occured (in whatever order)".
      * <p>
      * A special challenge here is transaction handling.
      * The transaction (see {@link de.deepamehta.core.service.Plugin#initPlugin}) should only committed
      * once <i>all</i> of the combined events are handled.
      */
     @Override
     public void postInstallPluginHook() {
        performWorkspaceInitialization = true;
        performACLInitialization = true;
         if (wsService != null) {
            logger.info("########## Clean install detected AND WorkspacesService already available => create Freifunk workspace");
             createFreifunkWorkspace();
        } else {
            logger.info("########## Clean install detected, WorkspacesService NOT yet available => create Freifunk workspace later on");
         }
         if (acService != null) {
             initACL();
         }
     }
 
     // ---
 
    @Override
     public void serviceArrived(PluginService service) {
        logger.info("########## Service arrived: " + service);
         if (service instanceof WorkspacesService) {
             wsService = (WorkspacesService) service;
             if (performWorkspaceInitialization) {
                logger.info("########## WorkspacesService arrived AND clean install detected => create Freifunk workspace");
                 createFreifunkWorkspace();
            } else {
                logger.info("########## WorkspacesService arrived, clean install NOT yet detected => possibly create Freifunk workspace later on");
             }
         } else if (service instanceof AccessControlService) {
             acService = (AccessControlService) service;
             if (performACLInitialization) {
                 initACL();
             }
         }
     }
 
    @Override
     public void serviceGone(PluginService service) {
         if (service instanceof WorkspacesService) {
             wsService = null;
         } else if (service instanceof AccessControlService) {
             acService = null;
         }
     }
 
     // ---
 
     @Override
     public void providePropertiesHook(Topic topic) {
         if (topic.typeUri.equals("net/freifunk/topictype/freikarte") ||
             topic.typeUri.equals("net/freifunk/topictype/access_point")) {
             //
             topic.setProperty("de/deepamehta/core/property/longitude",
                 dms.getTopicProperty(topic.id, "de/deepamehta/core/property/longitude"));
             topic.setProperty("de/deepamehta/core/property/latitude",
                 dms.getTopicProperty(topic.id, "de/deepamehta/core/property/latitude"));
         }
     }
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
     private void createFreifunkWorkspace() {
         // create workspace
         Topic workspace = wsService.createWorkspace(FREIFUNK_WORKSPACE_NAME);
         // assign "Access Point" type
         TopicType apType = dms.getTopicType("net/freifunk/topictype/access_point", null);     // clientContext=null
         wsService.assignType(workspace.id, apType.id);
         // assign "Freifunk Community" type
         TopicType fcType = dms.getTopicType("net/freifunk/topictype/community", null);        // clientContext=null
         wsService.assignType(workspace.id, fcType.id);
         //
         // book keeping
         performWorkspaceInitialization = false;
     }
 
     private void initACL() {
         Permissions permissions = new Permissions();
         permissions.add(Permission.WRITE, false);
         permissions.add(Permission.CREATE, true);
         //
         // *Everyone* can create a "Freikarte"
         TopicType fkType = dms.getTopicType("net/freifunk/topictype/freikarte", null);
         acService.createACLEntry(fkType.id, Role.EVERYONE, permissions);
         // *Members* of the Freifunk workspace can create an "Access Point".
         TopicType apType = dms.getTopicType("net/freifunk/topictype/access_point", null);
         acService.createACLEntry(apType.id, Role.MEMBER, permissions);
         // *Members* of the Freifunk workspace can create a "Freifunk Community".
         TopicType fcType = dms.getTopicType("net/freifunk/topictype/community", null);
         acService.createACLEntry(fcType.id, Role.MEMBER, permissions);
         //
         // book keeping
         performACLInitialization = false;
     }
 }
