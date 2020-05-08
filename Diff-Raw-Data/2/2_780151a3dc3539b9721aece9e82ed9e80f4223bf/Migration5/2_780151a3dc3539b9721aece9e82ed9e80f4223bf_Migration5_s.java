 package de.deepamehta.plugins.eduzen.migrations;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.TopicType;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.ResultSet;
 import de.deepamehta.core.model.AssociationModel;
 import de.deepamehta.core.model.TopicRoleModel;
 import de.deepamehta.core.model.SimpleValue;
 import de.deepamehta.core.service.Migration;
 import de.deepamehta.core.service.Directives;
 
 
 
 
 public class Migration5 extends Migration {
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
     @Override
     public void run() {
 
         // rename default Workspace
         Topic defaultWorkspace = dms.getTopic(9676, false, null);
         // setChildTopicvalue just works if there is a "one" relation present
         defaultWorkspace.setChildTopicValue("dm4.workspaces.name", new SimpleValue("EduZEN Editors"));
         // defaultWorkspace.setUri("de.workspaces.deepamehta");
 
         // update all associatons to this workspace according to new workspace-assignment in 4.0.12
         ResultSet<RelatedTopic> topics = defaultWorkspace.getRelatedTopics("dm4.workspaces.workspace_context",
             "dm4.core.default", null, null, false, false, 0, null);
         for (RelatedTopic topic : topics) {
             topic.getAssociation().update(new AssociationModel("dm4.core.aggregation",
                 new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.part"),
                 new TopicRoleModel(topic.getId(), "dm4.core.whole")
             ), null, new Directives());
         }
 
         // delete deprecated association type and two deprecated topic instances
         dms.getAssociationType("dm4.workspaces.workspace_context", null).delete(new Directives());
         dms.getTopic("uri", new SimpleValue("dm4.workspaces.workspace_topic"), false, null).delete(new Directives());
         dms.getTopic("uri", new SimpleValue("dm4.workspaces.workspace_type"), false, null).delete(new Directives());
 
         // fetch and relate username "admin" to at least one workspace (we use here "9676" the "EduZEN Editors")
        Topic admin = dms.getTopic("uri", new SimpleValue("dm4.accesscontrol.username"), false, null);
         if (admin == null) throw new RuntimeException("could not fetch admin by uri \"dm4.accesscontrol.username\"");
         //
         dms.createAssociation(new AssociationModel("dm4.core.aggregation",
             new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.part"),
             new TopicRoleModel(admin.getId(), "dm4.core.whole")
         ), null);
     }
 
 }
