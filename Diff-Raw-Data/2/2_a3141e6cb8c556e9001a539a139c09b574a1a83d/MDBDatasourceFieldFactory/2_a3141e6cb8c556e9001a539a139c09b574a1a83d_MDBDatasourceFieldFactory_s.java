 package com.pace.settings.ui;
 
 import javax.swing.Icon;
 
 
 
 
 import com.pace.settings.PaceSettingsConstants;
 import com.vaadin.data.Item;
 
 
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.TextField;
 
 /**
  * MDB Datasource Field Factory
  * 
  * @author JMilliron
  *
  */
 public class MDBDatasourceFieldFactory extends PaceSettingsDefaultFieldFactory {
 
 	
 	private static final String CONNECTION_STRING_INPUT_PROMPT = "EDSDomain=Essbase;EDSUrl=http://localhost:13080/aps/JAPI;Server=?;User=?;Password=?;Application=?;Database=?";
 
 	private static final long serialVersionUID = 2885762876258153110L;
 	
 	public static final String NAME = "name";
 	public static final String CONNECTION_STRING = "connectionString";
 	public static final String META_DATA_SERVICE_PROVIDER = "metaDataServiceProvider";
 	public static final String DATA_SERVICE_PROVIDER = "dataServiceProvider";
 	public static final String TOOL_TIP = "connectionToolTip";
 		
 	private ComboBox metaDataServiceProviderComboBox = new ComboBox();
 	
 	private ComboBox dataServiceProviderComboBox = new ComboBox();
 	
 	
 	
 	
 	private String connectionStringTooltip = null;
 	
 	public String getConnectionStringTooltip() {
 		return connectionStringTooltip;
 	}
 
 	public MDBDatasourceFieldFactory() {
 		
 		formOrderList.add(NAME);
 		formOrderList.add(TOOL_TIP);
 		formOrderList.add(CONNECTION_STRING);
 		
 		formOrderList.add(META_DATA_SERVICE_PROVIDER);
 		formOrderList.add(DATA_SERVICE_PROVIDER);		
 			
 		requiredFieldSet.add(NAME);
 		requiredFieldSet.add(META_DATA_SERVICE_PROVIDER);
	requiredFieldSet.add(TOOL_TIP);
 		requiredFieldSet.add(DATA_SERVICE_PROVIDER);
 		requiredFieldSet.add(CONNECTION_STRING);
 		
 		captionMap.put(NAME, "Name");
 		captionMap.put(META_DATA_SERVICE_PROVIDER, "Meta Data Service Provider");
 		captionMap.put(TOOL_TIP, "");
 		captionMap.put(DATA_SERVICE_PROVIDER, "Data Service Provider");
 		captionMap.put(CONNECTION_STRING, "Connection String");
 		
 			
 		metaDataServiceProviderComboBox.setNewItemsAllowed(false);
 		metaDataServiceProviderComboBox.setNullSelectionAllowed(false);
 		metaDataServiceProviderComboBox.addItem("com.pace.mdb.essbase.EsbMetaData");
 		
 		dataServiceProviderComboBox.setNewItemsAllowed(false);
 		dataServiceProviderComboBox.setNullSelectionAllowed(false);
 		dataServiceProviderComboBox.addItem("com.pace.mdb.essbase.EsbData");
 		
 	
 		String tab = "&nbsp;&nbsp;&nbsp;&nbsp;";
 		
 		StringBuilder connectionStringTooltipStringBuilder = new StringBuilder("=================================<p>");
 		connectionStringTooltipStringBuilder.append("EDS/APS/HPS connection url examples<p>");
 		connectionStringTooltipStringBuilder.append("=================================<p><p>");
 		connectionStringTooltipStringBuilder.append("EDS 7.x, APS 9.0.x to 9.2.x:<p>");
 		connectionStringTooltipStringBuilder.append(tab + "EDSUrl=tcpip://pchiadg1:5001<p>");
 		connectionStringTooltipStringBuilder.append(tab + "EDSUrl=http://localhost:11080/eds/EssbaseEnterprise<p><p>");
 		connectionStringTooltipStringBuilder.append("APS 9.3.x:<p>");
 		connectionStringTooltipStringBuilder.append(tab + "EDSUrl=http://localhost:13080/aps/JAPI<p><p>");
 		connectionStringTooltipStringBuilder.append("HPS 11.1.x and above:<p>");
 		connectionStringTooltipStringBuilder.append(tab + "EDSUrl=Embedded<p>");
 		connectionStringTooltipStringBuilder.append("EDSUrl=http://localhost:13080/aps/JAPI<p><p>");
 		connectionStringTooltipStringBuilder.append("Connection String Example:<p>");
 		connectionStringTooltipStringBuilder.append(tab + "EDSDomain=Essbase;EDSUrl=http://localhost:13080/aps/JAPI;Server=localhost;User=admin;Password=password;Application=Titan;Database=Titan<p>");
 		
 		connectionStringTooltip = connectionStringTooltipStringBuilder.toString();
 		
 		
 		
 		
 		
 	}
 	
 	@Override
 	public Field createField(Item item, Object propertyId, Component uiContext) {
 		
 		Field field = super.createField(item, propertyId, uiContext);
 	
 		if ( propertyId.equals(CONNECTION_STRING)) {
 			
 			field.setHeight("3em");
 			field.setWidth("95%");
 			
 			TextField tf = (TextField) field;
 			
 			tf.setInputPrompt(CONNECTION_STRING_INPUT_PROMPT);
 		
 		
 			
 		} else if ( propertyId.equals(META_DATA_SERVICE_PROVIDER) ) {
 			
 			metaDataServiceProviderComboBox.setCaption(field.getCaption());
 			metaDataServiceProviderComboBox.setRequired(requiredFieldSet.contains(propertyId));
 			metaDataServiceProviderComboBox.setWidth(PaceSettingsConstants.COMMON_FIELD_WIDTH_25_EM);
 			
 			return metaDataServiceProviderComboBox;
 			
 		} else if ( propertyId.equals(DATA_SERVICE_PROVIDER) ) {
 			
 			dataServiceProviderComboBox.setCaption(field.getCaption());
 			dataServiceProviderComboBox.setRequired(requiredFieldSet.contains(propertyId));
 			dataServiceProviderComboBox.setWidth(PaceSettingsConstants.COMMON_FIELD_WIDTH_25_EM);
 			
 			return dataServiceProviderComboBox;
 			
 		} 
 	else if(propertyId.equals(TOOL_TIP))
 		{
 
 		
 		field.setReadOnly(true);
 		field.setIcon(new ThemeResource("icons/32/questionmark1.png"));
 	
 	
 		field.setDescription(getConnectionStringTooltip());
 		TextField lf = (TextField)field;
 		
 		}
 		
 		
 		return field;
 	}
 
 }
