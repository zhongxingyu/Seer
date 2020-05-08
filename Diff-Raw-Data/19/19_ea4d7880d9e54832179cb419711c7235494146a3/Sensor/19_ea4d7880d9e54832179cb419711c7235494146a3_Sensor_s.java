 package nl.sense_os.commonsense.lib.client.model.apiclass;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.core.client.JsonUtils;
 
 /**
  * Overlay for sensor objects that are sent from CommonSense back end using JSON.
  */
 public class Sensor extends JavaScriptObject {
 
    public native static final Sensor create(int id, String name, String description,
             String dataType) /*-{
 		return {
 			'id' : id,
 			'name' : name,
 			'device_type' : description,
 			'data_type' : dataType
 		};
     }-*/;
 
     protected Sensor() {
         // protected empty constructor
     }
 
     public final native String getDataStructure() /*-{
 		return this.data_structure;
     }-*/;
 
     public final native String getDataType() /*-{
 		return this.data_type;
     }-*/;
 
     public final native String getDescription() /*-{
 		return this.device_type;
     }-*/;
 
     public final native Device getDevice() /*-{
 		return this.device;
     }-*/;
 
     public final native String getDisplayName() /*-{
 		if (undefined == this.display_name || this.display_name === '') {
 			return this.name;
 		} else {
 			return this.display_name;
 		}
     }-*/;
 
     public final native Environment getEnvironment() /*-{
 		return this.environment;
     }-*/;
 
     public final native String getId() /*-{
 		return this.id;
     }-*/;
 
     public final native String getName() /*-{
 		return this.name;
     }-*/;
 
     public final native User getOwner() /*-{
 		return this.owner;
     }-*/;
 
     public final native String getPagerType() /*-{
 		return this.pager_type;
     }-*/;
 
     public final native int getType() /*-{
 		return parseInt(this.type);
     }-*/;
 
     public final native JsArray<User> getUsers() /*-{
 		if (undefined != this.users) {
 			return this.users;
 		} else {
 			return [];
 		}
     }-*/;
 
     public final String serialize() {
         StringBuilder builder = new StringBuilder("{");
        builder.append("\"id\":" + getId());
         builder.append(",\"name\":" + JsonUtils.escapeValue(getName()));
         builder.append(",\"device_type\":" + JsonUtils.escapeValue(getDescription()));
         builder.append(",\"data_type\":" + JsonUtils.escapeValue(getDataType()));
         if (null != getDevice()) {
             builder.append(",\"device\":" + getDevice().serialize());
         }
         builder.append("}");
         return builder.toString();
     }
 }
