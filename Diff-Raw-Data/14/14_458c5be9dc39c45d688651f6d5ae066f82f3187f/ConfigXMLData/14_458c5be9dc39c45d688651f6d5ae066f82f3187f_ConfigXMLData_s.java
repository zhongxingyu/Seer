 package com.redhat.automationportalui.client.data;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 
 public class ConfigXMLData extends JavaScriptObject
 {
 	public static native ConfigXMLData createConfigXMLData()
 	/*-{
 		return eval('({"fromDate"=new Date(),"path":"","entry":""})');
 	}-*/;
 	
 	public static native JsArray<ConfigXMLData> createArrayConfigXMLData()
 	/*-{
 		return eval('([])');
 	}-*/;
 	
 	protected ConfigXMLData()
 	{
 
 	}
 
 	public native final Date getFromDate()
 	/*-{
 		return this.fromDate;
 	}-*/;
 
	public native void setFromDate(final Date fromDate)
 	/*-{
 		this.fromDate = fromDate;
 	}-*/;
 
	public native String getPath()
 	/*-{
 		return this.path;
 	}-*/;
 
	public native void setPath(final String path)
 	/*-{
 		this.path = path;
 	}-*/;
 
	public native String getEntry()
 	/*-{
 		return this.entry;
 	}-*/;
 
	public native void setEntry(final String entry)
 	/*-{
 		this.entry = entry;
 	}-*/;
 }
