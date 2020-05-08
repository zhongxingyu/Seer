 package org.cotrix.web.ingest.client.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.inject.Singleton;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public interface Resources extends ClientBundle {
 	
 	public static final Resources INSTANCE = GWT.create(Resources.class);
 	
 	@Source("style.css")
 	public ImportCss css();
 		
 	public ImageResource browse();
 	
 	@Source("browse-hover.png")
 	public ImageResource browseHover();
 
 	public ImageResource close();
 	
 	public ImageResource trash();
 	
 	@Source("trash_tick.png")
 	public ImageResource trashTick();
 	
 	public ImageResource back();
 	
 	public ImageResource reload();
 	
	public ImageResource reloadHover();
 }
