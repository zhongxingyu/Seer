 package de.deepamehta.plugins.workspaces.migrations;
 
 import de.deepamehta.core.model.DataField;
import de.deepamehta.core.model.Relation;
import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.service.Migration;
 
import java.util.HashMap;
import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 public class Migration2 extends Migration {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
     @Override
     public void run() {
         TopicType workspaceType = dms.getTopicType("de/deepamehta/core/topictype/Workspace");
         DataField nameField        = workspaceType.getDataField("de/deepamehta/core/property/Name");
         DataField descriptionField = workspaceType.getDataField("de/deepamehta/core/property/Description");
         nameField.setIndexingMode("FULLTEXT");
         descriptionField.setIndexingMode("FULLTEXT");
     }
 }
