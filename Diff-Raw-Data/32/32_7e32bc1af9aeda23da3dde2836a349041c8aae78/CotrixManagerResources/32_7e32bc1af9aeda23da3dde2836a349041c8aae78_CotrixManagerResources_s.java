 package org.cotrix.web.manage.client.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.inject.Singleton;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public interface CotrixManagerResources extends ClientBundle {
 	
 	public static final CotrixManagerResources INSTANCE = GWT.create(CotrixManagerResources.class);
 
 	@Source("style.css")
 	public CotrixManagerStyle css();
 	
 	@Source("propertyGrid.css")
 	public PropertyGridStyle propertyGrid();
 	
 	@Source("attributeRow.css")
 	public AttributeRowStyle attributeRow();
 	
 	public ImageResource table();
 	
 	public ImageResource tableDisabled();
 	
 	public ImageResource close();
 	public ImageResource closeSmall();
 	
 	public ImageResource allColumns();
 	
 	public ImageResource allNormals();
 	
 	public ImageResource versionItem();
 	
 	public ImageResource bullet();
 	
 	public ImageResource lock();
 	public ImageResource lockDisabled();
 	
 	public ImageResource unlock();
 	public ImageResource unlockDisabled();
 	
 	public ImageResource seal();
 	public ImageResource sealDisabled();
 	
 	public ImageResource attributesSelected();
 	public ImageResource attributesUnselected();
 	public ImageResource attributesDisabled();
 	
 	public ImageResource filter();
 	public ImageResource filterDisabled();
 	
 	public ImageResource linksSelected();
 	public ImageResource linksUnselected();
 	public ImageResource linksDisabled();
 	
 	@Source("thumb_vertical.png")
 	public ImageResource thumbVertical();
 	
 	@Source("thumb_horz.png")
 	public ImageResource thumbHorizontal();
 	
 	public ImageResource newVersion();
 	public ImageResource newVersionDisabled();
 	
 	
 	public ImageResource save();
 	public ImageResource edit();
 	public ImageResource cancel();
 	
 	public ImageResource addButton();
 	public ImageResource addButtonHover();
 	
 	interface CotrixManagerStyle extends CssResource {
 		String editor();
 		String systemProperty();
 		String addButton();
 		String headerCode();
 		String noItemsBackground();
 	}
 	
 	public interface PropertyGridStyle extends CssResource {
 		String emptyTableWidget();
 
 		String header();
 
 		String value();
 		
 		String valueBoxLeft();
 		String valueBoxCenter();
 		String valueBoxRight();
 
 		String textValue();
 	}
 	
 	public interface AttributeRowStyle extends CssResource {
 		String buttonCell();
 		String button();
 	}
 
 }
