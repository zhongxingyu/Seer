 /**
  * 
  */
 package org.cotrix.web.importwizard.client.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.Constants;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface ImportConstants extends Constants {
 	
 	public static ImportConstants INSTANCE = GWT.create(ImportConstants.class);
 	
 	@DefaultStringArrayValue({"en","fr"})
 	String[] languages();
 	
 	@DefaultIntValue(30)
 	int fileNameMaxSize();
	
	String[] csvMimeTypes();
	String[] xmlMimeTypes();
 
 }
