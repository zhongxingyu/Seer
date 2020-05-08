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
 		
	@Source("browse.png")
 	public ImageResource browse();
 	
 	@Source("browse-hover.png")
 	public ImageResource browseHover();
 
	@Source("close.png")
 	public ImageResource close();
 	
	@Source("trash.png")
 	public ImageResource trash();
 	
 	@Source("trash_tick.png")
 	public ImageResource trashTick();
 	
	@Source("back.png")
 	public ImageResource back();
 	
	@Source("reload.png")
 	public ImageResource reload();
 	
	
 }
