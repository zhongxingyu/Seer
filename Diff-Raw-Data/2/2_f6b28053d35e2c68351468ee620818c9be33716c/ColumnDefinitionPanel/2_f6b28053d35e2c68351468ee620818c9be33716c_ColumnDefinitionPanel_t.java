 package org.cotrix.web.importwizard.client.step.mapping;
 
 import java.util.Arrays;
 
 import org.cotrix.web.importwizard.client.resources.ImportConstants;
 import org.cotrix.web.importwizard.shared.ColumnType;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class ColumnDefinitionPanel extends Composite {
 	
 	protected static final String NO_TYPE_VALUE = "NONE";
 	protected static final String NO_TYPE_LABEL = "------ Select Type ------";
 
 	private static ColumnDefinitionPanelUiBinder uiBinder = GWT.create(ColumnDefinitionPanelUiBinder.class);
 
 	interface ColumnDefinitionPanelUiBinder extends UiBinder<Widget, ColumnDefinitionPanel> {
 	}
 
 	@UiField ListBox typeList;
 	@UiField Label inLabel;
 	@UiField ListBox languageList;
 
 	@UiField
 	Style style;
 
 	interface Style extends CssResource {
 		public String textPadding();
 		String listBoxError();
 		String listBox();
 	}
 
 	public ColumnDefinitionPanel() {
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		typeList.addChangeHandler(new ChangeHandler() {
 			
 			@Override
 			public void onChange(ChangeEvent event) {
 				updateLanguageList();
 			}
 		});
 		
 		setupTypeList();
 		setupLanguageList();
 	}
 	
 	protected void setupTypeList()
 	{
 		typeList.addItem(NO_TYPE_LABEL, NO_TYPE_VALUE);
 		for (ColumnType columnType:ColumnType.values()) typeList.addItem(columnType.getLabel(), columnType.toString());
 		setType(NO_TYPE_VALUE);
 	}
 	
 	protected void setupLanguageList()
 	{
 		String[] languages = ImportConstants.INSTANCE.languages();
 		Arrays.sort(languages);
 		for (String language:languages) languageList.addItem(language);
 	}
 	
 	protected void updateLanguageList()
 	{
 		ColumnType columnType = getColumnType();
 		if (columnType == null) setLanguageVisibile(false);
 		else {
 			switch (columnType) {
 				case CODE: setLanguageVisibile(false); break;
 				case DESCRIPTION: setLanguageVisibile(true); break;
 			}
 		}
 	}
 	
 	protected void setLanguageVisibile(boolean visible)
 	{
 		languageList.setVisible(visible);
 		inLabel.setVisible(visible);
 	}
 	
 	public void setColumnType(ColumnType columnType)
 	{
 		if (columnType == null) setType(NO_TYPE_VALUE);
 		else setType(columnType.toString());
 		updateLanguageList();
 	}
 	
 	protected void setType(String value)
 	{
 		for (int i = 0; i < typeList.getItemCount(); i++) {
 			if (typeList.getValue(i).equals(value)) {
 				typeList.setSelectedIndex(i);
 				return;
 			}
 		}
 	}
 	
 	public void setLanguage(String language)
 	{
 		if (language == null) return;
 		for (int i = 0; i < languageList.getItemCount(); i++) {
 			if (languageList.getItemText(i).equals(language)) languageList.setSelectedIndex(i);
 		}
 	}
 	
 	public ColumnType getColumnType()
 	{
 		int selectedIndex = typeList.getSelectedIndex();
 		if (selectedIndex<0) return null;
 		String typeValue = typeList.getValue(selectedIndex);
 		if (NO_TYPE_VALUE.equals(typeValue)) return null;
 		return ColumnType.valueOf(typeValue);
 	}
 	
 	public String getLanguage()
 	{
		if (!languageList.isVisible() || languageList.getSelectedIndex()<0) return null;
 		return languageList.getValue(languageList.getSelectedIndex());
 	}
 	
 	public void setErrorStyle(){
 		typeList.setStyleName(style.listBoxError());
 	}
 	
 	public void setNormalStyle(){
 		typeList.setStyleName(style.listBox());
 	}
 }
