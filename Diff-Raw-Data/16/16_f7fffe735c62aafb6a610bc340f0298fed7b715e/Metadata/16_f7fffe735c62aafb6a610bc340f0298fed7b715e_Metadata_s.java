 package uk.ac.nott.mrl.homework.client.model;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArrayString;
 
 public class Metadata extends JavaScriptObject
 {
 	public static final native Metadata parse(final String json)
 	/*-{
 		return eval('(' + json + ')');
 	}-*/;
 	
 	protected Metadata()
 	{
 		
 	}
 	
 	public final native JsArrayString getTypes() /*-{ return this.types; }-*/;
 
	public final native JavaScriptStringMap getOwners() /*-{ return this.owners; }-*/;	
 }
