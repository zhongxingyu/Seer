 package org.cotrix.web.ingest.client.util;
 
 import java.util.Arrays;
 
 import org.cotrix.web.common.client.resources.CommonResources;
 import org.cotrix.web.common.client.widgets.EnumListBox;
 import org.cotrix.web.common.client.widgets.EnumListBox.LabelProvider;
 import org.cotrix.web.ingest.client.resources.ImportConstants;
 import org.cotrix.web.ingest.shared.AttributeType;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AttributeDefinitionPanel extends Composite {
 
 	private static AttributeDefinitionPanelUiBinder uiBinder = GWT.create(AttributeDefinitionPanelUiBinder.class);
 
 	interface AttributeDefinitionPanelUiBinder extends UiBinder<Widget, AttributeDefinitionPanel> {
 	}
 
 	public static final LabelProvider<AttributeType> SDMXTypeLabelProvider  = new LabelProvider<AttributeType>() {
 
 		@Override
 		public String getLabel(AttributeType type) {
 			switch (type) {
 				case CODE: return "Code";
 				case OTHER_CODE: return "Other code";
 				case DESCRIPTION: return "Description";
 				case ANNOTATION: return "Annotation";
 				case NAME: return "Name";
 				case OTHER: return "Other";
 				default: throw new IllegalArgumentException("No label mapping found for attribute type "+type);
 			}
 		}
 	};
 
 	public static final LabelProvider<AttributeType> CSVTypeLabelProvider  = new LabelProvider<AttributeType>() {
 
 		@Override
 		public String getLabel(AttributeType type) {
 			switch (type) {
 				case CODE: return "Primary code";
 				case OTHER_CODE: return "Other code";
 				case DESCRIPTION: return "Description";
 				case ANNOTATION: return "Annotation";
 				case NAME: return "Name";
 				case OTHER: return "Other";
 				default: throw new IllegalArgumentException("No label mapping found for attribute type "+type);
 			}
 		}
 	};
 
 	@UiField(provided=true) EnumListBox<AttributeType> typeList;
 	@UiField TextBox customType;
 	@UiField Label inLabel;
 	@UiField ListBox languageList;
 
 	@UiField Style style;
 
 	interface Style extends CssResource {
 		String listBoxError();
 	}
 	
 	public AttributeDefinitionPanel(LabelProvider<AttributeType> typeLabelProvider) {
 		
 		typeList = new EnumListBox<AttributeType>(AttributeType.class, typeLabelProvider);
 		
 		initWidget(uiBinder.createAndBindUi(this));
 
 		typeList.addChangeHandler(new ChangeHandler() {
 
 			@Override
 			public void onChange(ChangeEvent event) {
 				updateVisibilities();
 			}
 		});
 
 		setupLanguageList();
 	}
 
 	@SuppressWarnings("deprecation")
 	protected void setupLanguageList()
 	{
 		String[] languages = ImportConstants.INSTANCE.languages();
 		Arrays.sort(languages);
		languageList.addItem(" ");
 		for (String language:languages) languageList.addItem(language);
 	}
 
 	public AttributeType getType()
 	{
 		return typeList.getSelectedValue();
 	}
 	
 	public String getCustomType() {
 		return customType.getText();
 	}
 
 	public void setType(AttributeType type, String customType)
 	{
 		typeList.setSelectedValue(type);
 		this.customType.setText(customType);
 		updateVisibilities();
 	}
 
 	public String getLanguage()
 	{
 		if (!languageList.isVisible() || languageList.getSelectedIndex()<0) return null;
		return languageList.getValue(languageList.getSelectedIndex());
 	}	
 
 	public void setLanguage(String language)
 	{
 		if (language == null) return;
 		for (int i = 0; i < languageList.getItemCount(); i++) {
 			if (languageList.getItemText(i).equals(language)) languageList.setSelectedIndex(i);
 		}
 	}
 
 	protected void updateVisibilities()
 	{
 		AttributeType type = getType();
 		setLanguagePanelVisibile(type != null && type != AttributeType.CODE && type != AttributeType.OTHER_CODE);
 		customType.setVisible(typeList.getSelectedValue() == AttributeType.OTHER);
 	}
 
 	protected void setLanguagePanelVisibile(boolean visible)
 	{
 		inLabel.setVisible(visible);
 		languageList.setVisible(visible);
 	}	
 
 	public void setErrorStyle(){
 		typeList.setStyleName(style.listBoxError());
 	}
 
 	public void setNormalStyle(){
 		typeList.setStyleName(CommonResources.INSTANCE.css().listBox());
 	}
 
 	public void setEnabled(boolean enabled)
 	{
 		typeList.setEnabled(enabled);
 		inLabel.setStyleName(CommonResources.INSTANCE.css().paddedTextDisabled(), !enabled);
 		languageList.setEnabled(enabled);
 	}
 
 }
