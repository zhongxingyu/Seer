 package de.deepamehta.plugins.workspaces.migrations;
 
 import de.deepamehta.core.model.DataField;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.service.Migration;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 
 public class Migration1 extends Migration {
 
     public void run() {
         addWorkspacesFieldToAllTypes();
         createWorkspaceTopicType();
     }
 
     // ---
 
     private void addWorkspacesFieldToAllTypes() {
         DataField workspacesField = new DataField("Workspaces");
         workspacesField.setDataType("relation");
         workspacesField.setRelatedTypeId("Workspace");
         workspacesField.setEditor("checkboxes");
         // workspacesField.setIndexingMode("FULLTEXT_KEY");
         //
         for (String typeId : dms.getTopicTypeIds()) {
             dms.addDataField(typeId, workspacesField);
         }
     }
 
     private void createWorkspaceTopicType() {
         DataField nameField = new DataField("Name");
         nameField.setDataType("text");
         nameField.setEditor("single line");
         // nameField.setIndexingMode("FULLTEXT_KEY");
         //
         DataField descriptionField = new DataField("Description");
         descriptionField.setDataType("html");
         descriptionField.setEditor("multi line");
         // nameField.setIndexingMode("FULLTEXT_KEY");
         //
         List dataFields = new ArrayList();
         dataFields.add(nameField);
         dataFields.add(descriptionField);
         //
         Map properties = new HashMap();
         properties.put("type_id", "Workspace");
        properties.put("icon_src", "images/star.png");
         properties.put("implementation", "PlainDocument");
         //
         dms.createTopicType(properties, dataFields);
     }
 }
