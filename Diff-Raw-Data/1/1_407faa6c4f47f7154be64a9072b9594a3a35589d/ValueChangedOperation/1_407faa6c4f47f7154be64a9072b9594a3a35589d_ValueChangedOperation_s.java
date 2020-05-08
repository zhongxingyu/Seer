 package gx.realtime.operation;
 
 import gx.realtime.serialize.ValueChangedOperationDeserializer;
 
 import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
 
 /**
  * Internal event for adding a CollabortiveObject to the model
  * 
  * @author E.S. van der Veen
  */
 @JsonDeserialize(using = ValueChangedOperationDeserializer.class)
 public class ValueChangedOperation extends Operation {
 
     private String objectId;
     private String key;
     private ObjectType valueType;
     private String value;
     
     public ValueChangedOperation(String id, String key, ObjectType type, String value)
     {
         this.type = Type.VALUE_CHANGED;
         this.objectId = id;
         this.valueType = type;
         this.value = value;
     }
     
     public ValueChangedOperation(String id, String key, int type, String value)
     {
         this(id, key, ObjectType.map(type), value);
     }
 
     public enum ObjectType
     {
         EDITABLE_STRING,
         COLLABORATIVE_OBJECT,
         JSON;
         
         public static ObjectType map(int type)
         {
             switch(type)
             {
             case 1:
             return EDITABLE_STRING;
             case 2:
             return COLLABORATIVE_OBJECT;
             case 21:
             return JSON;
             }
             return null;
         } 
     }
 }
