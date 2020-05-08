 package net.freifunk.dm3plugins.geomap;
 
 import de.deepamehta.plugins.accesscontrol.AccessControlPlugin;
import de.deepamehta.plugins.accesscontrol.AccessControlPlugin.Permission;
 import de.deepamehta.plugins.accesscontrol.AccessControlPlugin.Role;
import de.deepamehta.plugins.accesscontrol.model.Permissions;
 
 import de.deepamehta.core.model.DataField;
 import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.service.Plugin;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 public class FreifunkGeomapPlugin extends Plugin {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
    private AccessControlPlugin accessControl;
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // ************************
     // *** Overriding Hooks ***
     // ************************
 
 
 
     @Override
     public void postInstallPluginHook() {
         TopicType freikarteType = dms.getTopicType("net/freifunk/topictype/freikarte", null);
        Permissions permissions = new Permissions();
        permissions.add(Permission.WRITE, false);
        permissions.add(Permission.CREATE, true);
        // Note: the Freifunk plugin must be installed *after* the Access Control plugin.
        // FIXME: remove that constraint.
        accessControl = (AccessControlPlugin) getService().getPlugin("de.deepamehta.3-accesscontrol");
        accessControl.createACLEntry(freikarteType.id, Role.EVERYONE, permissions);
     }
 
     @Override
     public void providePropertiesHook(Topic topic) {
         if (topic.typeUri.equals("net/freifunk/topictype/freikarte")) {
             topic.setProperty("de/deepamehta/core/property/longitude",
                 dms.getTopicProperty(topic.id, "de/deepamehta/core/property/longitude"));
             topic.setProperty("de/deepamehta/core/property/latitude",
                 dms.getTopicProperty(topic.id, "de/deepamehta/core/property/latitude"));
         }
     }
 }
