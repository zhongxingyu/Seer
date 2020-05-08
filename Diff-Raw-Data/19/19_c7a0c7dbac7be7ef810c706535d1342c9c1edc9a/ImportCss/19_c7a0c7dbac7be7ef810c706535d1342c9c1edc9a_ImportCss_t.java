 /**
  * 
  */
 package org.cotrix.web.importwizard.client.resources;
 
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.user.client.ui.SimpleCheckBox;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface ImportCss extends CssResource {
 
 
 	@ClassName("gwt-PopupPanelGlass")
 	String glassPanel();
 
 
 	/**
 	 * Style for {@link SimpleCheckBox}
 	 * @return
 	 */
 	String simpleCheckbox();
 
 	/**
 	 * Style for property label
 	 * @return
 	 */
 	String propertyLabel();
 
 	String listBox();
 
 	String textBox();
 
 	String paddedText();
 
 	String paddedTextDisabled();
 	
 	String imageButton();
 	
 	String verticalHeadersSingleItem();
 	
 	String verticalHeadersMultipleItem();
	
	String verticalHeadersFieldItem();
 }
