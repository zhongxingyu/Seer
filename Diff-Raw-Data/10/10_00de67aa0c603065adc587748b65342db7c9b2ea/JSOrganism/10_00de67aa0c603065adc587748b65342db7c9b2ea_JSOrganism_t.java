 package org.concord.geniverse.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
final public class JSOrganism extends JavaScriptObject {
 	
	protected JSOrganism(){
		
	}
 
 	public static native JSOrganism getJSOrganismFromJSONString(String jsonString) /*-{
 		return eval('('+jsonString+')');
 	}-*/;
 
 	public native String getAlleles() /*-{
 		return this.alleles;
 	}-*/;
 
 	public native String getImageURL() /*-{
 		return this.imageURL;
 	}-*/;
 
 	public native String getName() /*-{
 		return this.name_0;
 	}-*/;
 
 	public native int getSex() /*-{
 		return this.sex;
 	}-*/;
 
 //	// Not implemented yet
 //	public native ArrayList<String> getCharacteristics() /*-{
 //		return null;
 //	}-*/;
 //
 //	// Not implemented yet
 //	public native HashMap<String, Object> getMetaInfo() /*-{
 //		return null;
 //	}-*/;
 
 }
