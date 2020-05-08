 package org.cotrix.web.common.client.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.inject.Singleton;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public interface CommonResources extends ClientBundle {
 	public static final CommonResources INSTANCE = GWT.create(CommonResources.class);
 	
	@Source("style.css")
 	public CommonCss css();
 	
 	@Source({"datePicker.css", "definitions.css"})
 	public DatePickerStyle datePicker();
 	
 	public ImageResource loader();
 	
 	public ImageResource circleLoader();
 	
 	@Source("popup-arrow.png")
 	public ImageResource popupArrow();
 	
 	public ImageResource dataLoader();
 
 	public ImageResource manage();
 
 	public ImageResource refresh();
 	
 	public ImageResource undo();
 
 	public ImageResource preview();
 	
 	public ImageResource computer();
 	
 	@Source("computer-hover.png")
 	public ImageResource computerHover();
 
 	public ImageResource cloud();
 	
 	@Source("cloud-hover.png")
 	public ImageResource cloudHover();
 	
 	public ImageResource download();
 	
 	@Source("import.png")
 	public ImageResource importIcon();
 	
 	ImageResource selectArrow();
 	
 	ImageResource user();
 	
 	ImageResource userDisabled();
 	
 	ImageResource userPreferences();
 	
 	ImageResource userPreferencesDisabled();
 	
 	ImageResource search();
 	
 	ImageResource plus();
 	ImageResource plusDisabled();
 	
 	ImageResource minus();
 	ImageResource minusDisabled();
 
 }
