 /**
  * 
  */
 package org.cotrix.web.ingest.client.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cotrix.web.common.client.resources.CommonResources;
 import org.cotrix.web.common.shared.Language;
 import org.cotrix.web.ingest.shared.AttributeDefinition;
 import org.cotrix.web.ingest.shared.AttributeMapping;
 import org.cotrix.web.ingest.shared.AttributeType;
 import org.cotrix.web.ingest.shared.Field;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimpleCheckBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AttributeMappingPanel extends Composite {
 
 	protected static int IGNORE_COLUMN = 0;
 	protected static int DEFINITION_COLUMN = 1;
 
 	protected SimplePanel container;
 	protected FlexTable columnsTable;
 	protected FlexTable loadingContainter;
 
 	protected boolean showTypeDefinition;
 
 	protected List<SimpleCheckBox> includeCheckBoxes = new ArrayList<SimpleCheckBox>();
 	protected List<AttributeDefinitionPanel> definitionsPanels = new ArrayList<AttributeDefinitionPanel>();
 	protected List<Field> fields = new ArrayList<Field>();
 
 	public AttributeMappingPanel()
 	{
 		container = new SimplePanel();
 		columnsTable = new FlexTable();
 		setupLoadingContainer();
 
 		container.setWidget(columnsTable);
 		initWidget(container);
 	}
 
 	/**
 	 * @param showTypeDefinition the showTypeDefinition to set
 	 */
 	public void setShowTypeDefinition(boolean showTypeDefinition) {
 		this.showTypeDefinition = showTypeDefinition;
 	}
 
 	protected void setupLoadingContainer()
 	{
 		loadingContainter = new FlexTable();
 		loadingContainter.getElement().setAttribute("align", "center");
 		loadingContainter.setWidget(0, 0, new Label("loading..."));
 	}
 
 	public void setLoading()
 	{
 		container.setWidget(loadingContainter);
 	}
 
 	public void unsetLoading()
 	{
 		container.setWidget(columnsTable);
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	public void setMapping(List<AttributeMapping> mapping)
 	{
 		columnsTable.removeAllRows();
 		definitionsPanels.clear();
 		includeCheckBoxes.clear();
 		fields.clear();
 
 		FlexCellFormatter cellFormatter = columnsTable.getFlexCellFormatter();
 
 		for (AttributeMapping attributeMapping:mapping) {
 
 			final int row = columnsTable.getRowCount();
 			AttributeDefinition attributeDefinition = attributeMapping.getAttributeDefinition();
 
 			final SimpleCheckBox checkBox = new SimpleCheckBox();
 			checkBox.setStyleName(CommonResources.INSTANCE.css().simpleCheckbox());
 			checkBox.setTitle("Select to include in the imported codelist.");
 
 			checkBox.setValue(attributeDefinition != null);
 			columnsTable.setWidget(row, IGNORE_COLUMN, checkBox);
 			cellFormatter.setStyleName(row, IGNORE_COLUMN, CommonResources.INSTANCE.css().mappingCell());
 			includeCheckBoxes.add(checkBox);
 
 			Field field = attributeMapping.getField();
 			fields.add(field);
 
			String nameFieldTitle = showTypeDefinition?"Rename the column.":"Rename SDMX element.";
			final AttributeDefinitionPanel definitionPanel = new AttributeDefinitionPanel(AttributeDefinitionPanel.CSVTypeLabelProvider, nameFieldTitle);
 			definitionsPanels.add(definitionPanel);
 			columnsTable.setWidget(row, DEFINITION_COLUMN, definitionPanel);
 			cellFormatter.setStyleName(row, DEFINITION_COLUMN, CommonResources.INSTANCE.css().mappingCell());
 			checkBox.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					definitionPanel.setEnabled(checkBox.getValue());
 				}
 			});
 
 			String name = attributeDefinition != null?attributeDefinition.getName():field.getLabel();
 			definitionPanel.setName(name);
 
 			definitionPanel.setTypeDefinitionVisible(showTypeDefinition);
 			if (attributeDefinition != null) {
 				definitionPanel.setType(attributeDefinition.getType(), attributeDefinition.getCustomType());
 				definitionPanel.setLanguage(attributeDefinition.getLanguage());
 			}
 		}
 	}
 
 	public void setCodeTypeError()
 	{
 		for (AttributeDefinitionPanel definitionPanel:definitionsPanels) {
 			AttributeType attributeType = definitionPanel.getType();
 			if (attributeType!=null && attributeType == AttributeType.CODE) definitionPanel.setErrorStyle();
 		}
 	}
 
 	public void cleanStyle()
 	{
 		for (AttributeDefinitionPanel definitionPanel:definitionsPanels) definitionPanel.setNormalStyle();
 	}
 
 	public List<AttributeMapping> getMappings()
 	{
 		List<AttributeMapping> mappings = new ArrayList<AttributeMapping>();
 		for (int i = 0; i < fields.size(); i++) {
 			Field field = fields.get(i);
 			AttributeDefinition attributeDefinition = getDefinition(i);
 
 			AttributeMapping mapping = new AttributeMapping();
 			mapping.setField(field);
 			mapping.setAttributeDefinition(attributeDefinition);
 			mappings.add(mapping);
 		}
 
 		return mappings;
 	}
 
 	protected AttributeDefinition getDefinition(int index)
 	{
 		if (!includeCheckBoxes.get(index).getValue()) return null;
 
 		AttributeDefinition attributeDefinition = new AttributeDefinition();
 
 		AttributeDefinitionPanel panel = definitionsPanels.get(index);
 
 		String name = panel.getName();
 		attributeDefinition.setName(name);
 
 		AttributeType type = panel.getType();
 		attributeDefinition.setType(type);
 
 		String customType = panel.getCustomType();
 		attributeDefinition.setCustomType(customType);
 
 		Language language = panel.getLanguage();
 		attributeDefinition.setLanguage(language);
 
 		attributeDefinition.setOptional(panel.isOptional());
 
 		return attributeDefinition;
 	}
 
 }
