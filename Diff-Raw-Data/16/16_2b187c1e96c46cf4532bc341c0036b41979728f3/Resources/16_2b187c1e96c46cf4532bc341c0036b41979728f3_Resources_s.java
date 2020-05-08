 package org.cotrix.web.importwizard.client.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.ImageResource;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface Resources extends ClientBundle {
 	public static final Resources INSTANCE = GWT.create(Resources.class);
 	
 	@Source("style.css")
 	public ImportCss css();
 
 	@Source("loader.gif")
 	public ImageResource loader();
 
 	@Source("cloud.png")
 	public ImageResource cloud();
 
 	@Source("computer.png")
 	public ImageResource computer();
 	
 	@Source("close.png")
 	public ImageResource close();
 	
 	@Source("refresh.png")
 	public ImageResource refresh();
 	
 	@Source("trash.png")
 	public ImageResource trash();
 	
 	@Source("trash_tick.png")
	public ImageResource trasTick();
 	
 	@Source("download.png")
 	public ImageResource download();
 
 }
