 package com.nonagon.modular.client.strings;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 public class LocalizedString extends JavaScriptObject {
 
 	public static LocalizedString create() {
 		
 		LocalizedString instance = ((LocalizedString)JavaScriptObject.createObject());
 		instance.init();
 		
 		return instance;
 	}
 	
 	protected final native void init()/*-{
 		
 		this.__type = "Nonagon.Modular.Strings.LocalizedString, Nonagon.Modular";
 	}-*/;
 
 	protected LocalizedString() {}
 	
	public final native String get(String culture)/*-{

		if(culture.indexOf('-') <= 0 && culture.indexOf('_') <= 0) {
			
			culture = culture.concat('_', culture);
		}
		
		culture = culture.replace('_', '-');
		
		return this[culture];
		 
	}-*/;
	
	public final native void set(String culture, String value)/*-{ 

		culture = culture.replace('_', '-');
		
		this[culture] = value; 
	
	}-*/;
 }
