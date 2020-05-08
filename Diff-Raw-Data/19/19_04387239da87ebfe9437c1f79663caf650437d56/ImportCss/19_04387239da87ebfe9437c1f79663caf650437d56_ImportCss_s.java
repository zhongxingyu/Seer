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
 	String propertyValue();
 	
 	String propertiesTable();
 	String propertiesTableHeader();
 
 	String listBox();
 
 	String textBox();
 
 	String paddedText();
 	String sectionTitle();
 	String linkText();
 
 	String paddedTextDisabled();
 	
 	String imageButton();
 	
 	String verticalHeadersSingleItem();
 	
 	String verticalHeadersMultipleItem();
 	
 	String verticalHeadersFieldItem();
 	
 	String previewHeader();
 	String preview();
 	String previewCell();
 	
 	String blueButton();
 	String grayButton();
 	
 	String areaHighlight();
 }
