 package nl.sense_os.commonsense.lib.client.model.apiclass;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsonUtils;
 
 /**
  * Overlay for user objects that are sent from CommonSense back end using JSON.
  */
 public class User extends JavaScriptObject {
 
     protected User() {
         // empty protected constructor
     }
 
     public final native String getEmail() /*-{
 		if (undefined != this.email) {
 			return this.email;
 		} else {
 			return '';
 		}
     }-*/;
 
     public final native String getId() /*-{
 		return this.id;
     }-*/;
 
     public final native String getMobile() /*-{
 		if (undefined != this.mobile) {
 			return this.mobile;
 		} else {
 			return '';
 		}
     }-*/;
 
     public final native String getName() /*-{
 		if (undefined != this.name) {
 			return this.name;
 		} else {
 			return '';
 		}
     }-*/;
 
     public final native String getSurname() /*-{
 		if (undefined != this.surname) {
 			return this.surname;
 		} else {
 			return '';
 		}
     }-*/;
 
     public final native String getUsername() /*-{
 		if (undefined != this.username) {
 			return this.username;
 		} else {
 			return '';
 		}
     }-*/;
 
     public final native String getUuid() /*-{
 		if (undefined != this.UUID) {
 			return this.UUID;
 		} else {
 			return '';
 		}
     }-*/;
 
     public final String serialize() {
         StringBuilder builder = new StringBuilder("{");
        builder.append("\"id\":" + getId());
         builder.append(",\"username\":" + JsonUtils.escapeValue(getUsername()));
         builder.append(",\"email\":" + JsonUtils.escapeValue(getEmail()));
         builder.append(",\"name\":" + JsonUtils.escapeValue(getName()));
         builder.append(",\"surname\":" + JsonUtils.escapeValue(getSurname()));
         builder.append(",\"mobile\":" + JsonUtils.escapeValue(getMobile()));
         builder.append("}");
         return builder.toString();
     }
 }
