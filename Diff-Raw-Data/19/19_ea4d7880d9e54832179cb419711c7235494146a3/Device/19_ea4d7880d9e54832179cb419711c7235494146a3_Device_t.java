 package nl.sense_os.commonsense.lib.client.model.apiclass;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsonUtils;
 
 /**
  * Overlay for device objects that are sent from CommonSense back end using JSON.
  */
 public class Device extends JavaScriptObject {
 
     protected Device() {
         // empty protected constructor
     }
 
     public final native String getId() /*-{
 		return this.id;
     }-*/;
 
     public final native String getType() /*-{
 		return this.type;
     }-*/;
 
     public final native String getUuid() /*-{
 		return this.uuid;
     }-*/;
 
     public final String serialize() {
         StringBuilder builder = new StringBuilder("{");
        builder.append("\"id\":" + JsonUtils.escapeValue(getId()));
         builder.append(",\"type\":" + JsonUtils.escapeValue(getType()));
         builder.append(",\"uuid\":" + JsonUtils.escapeValue(getUuid()));
         builder.append("}");
         return builder.toString();
     }
 }
