 package de.deepamehta.plugins.time.migrations;
 
 import de.deepamehta.core.model.DataField;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.service.Migration;
 
 
 
 public class Migration1 extends Migration {
 
     @Override
     public void run() {
         // Add "Date Created" and "Date Modified" data fields to all existing topic types.
         // Note: Topic types created after the time plugin is activated get these fields through the preCreateHook().
         // See de.deepamehta.plugins.time.TimePlugin
         //
         // TODO: Avoid this code doubling by providing a "update type definition" facility.
         //
         DataField dateCreatedField = new DataField("Date Created", "number");
         dateCreatedField.setUri("de/deepamehta/core/property/DateCreated");
         dateCreatedField.setIndexingMode("FULLTEXT_KEY");
        dateCreatedField.setRendererClass("TimestampFieldRenderer");
         //
         DataField dateModifiedField = new DataField("Date Modified", "number");
         dateModifiedField.setUri("de/deepamehta/core/property/DateModified");
         dateModifiedField.setIndexingMode("FULLTEXT_KEY");
        dateModifiedField.setRendererClass("TimestampFieldRenderer");
         //
         for (String typeUri : dms.getTopicTypeUris()) {
             dms.addDataField(typeUri, dateCreatedField);
             dms.addDataField(typeUri, dateModifiedField);
         }
     }
 }
