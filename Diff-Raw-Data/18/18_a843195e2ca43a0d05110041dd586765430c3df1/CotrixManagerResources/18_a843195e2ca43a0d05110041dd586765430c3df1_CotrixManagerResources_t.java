 package org.cotrix.web.codelistmanager.client.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface CotrixManagerResources extends ClientBundle {
 	
 	public static final CotrixManagerResources INSTANCE = GWT.create(CotrixManagerResources.class);
 
 	@Source("style.css")
 	public CotrixManagerStyle css();
 	
 	public ImageResource table();
 	
 	public ImageResource tableDisabled();
 	
 	public ImageResource close();
 	
 	public ImageResource plus();
 	
 	public ImageResource minus();
 	
 	public ImageResource allColumns();
 	
	public ImageResource allNormals();
 	
 	public ImageResource versionItem();
 	
 	public ImageResource lock();
 	
 	public ImageResource lockDisabled();
 	
 	public ImageResource unlock();
 	
 	public ImageResource unlockDisabled();
 	
 	public ImageResource search();
 	
 	public ImageResource attributes();
 	public ImageResource attributesDisabled();
 	
 	public ImageResource metadata();
 	public ImageResource metadataDisabled();
 	
 	public ImageResource userPreferences();
 	public ImageResource userPreferencesDisabled();
 	
 	public ImageResource filter();
 	public ImageResource filterDisabled();
 	
 	interface CotrixManagerStyle extends CssResource {
 		String search();
 	}
 
 }
