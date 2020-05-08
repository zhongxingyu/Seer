 /**
  * 
  */
 package org.cotrix.web.common.client.resources;
 
 import com.google.gwt.resources.client.CssResource;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface CommonCss extends CssResource {
 
 	@ClassName("gwt-PopupPanelGlass")
 	String glassPanel();
 
 	String simpleCheckbox();
 	
 	String paddedText();
 	String paddedTextDisabled();
 	
 	String linkText();	
 	
 	String listBox();
 	
 	String imageButton();
 	
 	String textBox();
 	
 	String verticalHeadersSingleItem();
 	String verticalHeadersMultipleItem();
 	String verticalHeadersFieldItem();
 	
 	
 	String wizardPanel();
 	
 	String blueButton();
 	String grayButton();
 	
 	String propertiesTable();
 	String propertiesTableHeader();
 
 	String propertyLabel();
 	String propertyValue();
 		
 	String buttonManage();
 	String buttonRefresh();
	String buttonPreview();
 	String buttonCloud();
 	String buttonComputer();
 	String buttonDownload();
 	String buttonImport();
 	
 	String mappingTable();
 	String mappingTableNameHeader();	
 	String mappingCell();
 	String mappingAttributeHeaderCell();
 	String mappingAttributeHeader();
 	
 	String missingValueText();
 	
 	
 	String errorPanel();
 	String errorMessageContainer();
 	String errorMessage();
 	
 	String searchBox();
 	String searchBackground();
 	
 	String dialogBox();
 	String dialogTitle();
 	String dialogSubtitle();
 	String dialogTextfield();
 	String dialogTextbox();
 	String dialogTextboxInvalid();
 	String dialogLink();
 	String dialogSubmit();
 	
 	String sugestionListBoxPanel();
 	String sugestionListBoxTextBox();
 	String sugestionListBoxButton();
 }
