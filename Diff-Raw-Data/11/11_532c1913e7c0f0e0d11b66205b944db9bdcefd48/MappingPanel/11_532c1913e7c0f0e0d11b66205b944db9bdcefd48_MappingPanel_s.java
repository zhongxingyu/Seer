 /**
  * 
  */
 package org.cotrix.web.importwizard.client.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cotrix.web.importwizard.client.resources.Resources;
 import org.cotrix.web.importwizard.shared.AttributeDefinition;
 import org.cotrix.web.importwizard.shared.AttributeMapping;
 import org.cotrix.web.importwizard.shared.AttributeType;
 import org.cotrix.web.importwizard.shared.Field;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimpleCheckBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class MappingPanel extends Composite {
 
 	protected static int IGNORE_COLUMN = 0;
 	protected static int NAME_COLUMN = 1;
 	protected static int LABEL_COLUMN = 2;
 	protected static int DEFINITION_COLUMN = 3;
 
 	protected SimplePanel container;
 	protected FlexTable columnsTable;
 	protected FlexTable loadingContainter;
 
 	protected boolean typeDefinition;
 
 	protected List<SimpleCheckBox> includeCheckBoxes = new ArrayList<SimpleCheckBox>();
 	protected List<TextBox> nameFields = new ArrayList<TextBox>();
 	protected List<AttributeDefinitionPanel> definitionsPanels = new ArrayList<AttributeDefinitionPanel>();
 	protected List<AttributeDefinition> definitions = new ArrayList<AttributeDefinition>();
 	protected List<Field> fields = new ArrayList<Field>();
 
 	public MappingPanel(boolean typeDefinition)
 	{
 		this.typeDefinition = typeDefinition;
 		container = new SimplePanel();
 		columnsTable = new FlexTable();
 		setupLoadingContainer();
 		
 		container.setWidget(columnsTable);
 		initWidget(container);
 
 		Resources.INSTANCE.css().ensureInjected();
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
 		nameFields.clear();
 		fields.clear();
 		definitions.clear();
 
 		FlexCellFormatter cellFormatter = columnsTable.getFlexCellFormatter();
 
 		for (AttributeMapping attributeMapping:mapping) {
 
 			final int row = columnsTable.getRowCount();
 			AttributeDefinition attributeDefinition = attributeMapping.getAttributeDefinition();
 			definitions.add(attributeDefinition);
 
 			final SimpleCheckBox checkBox = new SimpleCheckBox();
 			checkBox.setStyleName(Resources.INSTANCE.css().simpleCheckbox());
 			checkBox.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					Log.trace("Include? "+checkBox.getValue());
 					setInclude(row, checkBox.getValue());
 				}
 			});
 
 			checkBox.setValue(attributeDefinition != null);
 			columnsTable.setWidget(row, IGNORE_COLUMN, checkBox);
 			includeCheckBoxes.add(checkBox);
 
 			Field field = attributeMapping.getField();
 			fields.add(field);
 
 			TextBox nameField = new TextBox();
 			nameField.setStyleName(Resources.INSTANCE.css().textBox());
 			nameField.setValue(field.getLabel());
 			columnsTable.setWidget(row, NAME_COLUMN, nameField);
 			cellFormatter.setStyleName(row, NAME_COLUMN, Resources.INSTANCE.css().mappingCell());
 			nameFields.add(nameField);
 
 			if (typeDefinition) {
 				columnsTable.setWidget(row, LABEL_COLUMN, new Label("is a"));
 				cellFormatter.setStyleName(row, LABEL_COLUMN, Resources.INSTANCE.css().paddedText());
 
 				AttributeDefinitionPanel definitionPanel = new AttributeDefinitionPanel(AttributeDefinitionPanel.CSVTypeLabelProvider);
 				if (attributeDefinition != null) {
 					definitionPanel.setType(attributeDefinition.getType());
 					definitionPanel.setLanguage(attributeDefinition.getLanguage());
 				}
 				definitionsPanels.add(definitionPanel);
 				columnsTable.setWidget(row, DEFINITION_COLUMN, definitionPanel);
 				cellFormatter.setStyleName(row, DEFINITION_COLUMN, Resources.INSTANCE.css().mappingCell());
 			}
 		}
 	}
 
 	protected void setInclude(int row, boolean include)
 	{
 		((TextBox)columnsTable.getWidget(row, NAME_COLUMN)).setEnabled(include);
 		if (typeDefinition) {
 			((Label)columnsTable.getWidget(row, LABEL_COLUMN)).setStyleName(Resources.INSTANCE.css().paddedTextDisabled(), !include);
 			((AttributeDefinitionPanel)columnsTable.getWidget(row, DEFINITION_COLUMN)).setEnabled(include);
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
 
 		AttributeDefinition attributeDefinition = definitions.get(index);
 
 		if (typeDefinition) {
 			AttributeDefinitionPanel panel = definitionsPanels.get(index);
 			AttributeType type = panel.getType();
 			String language = panel.getLanguage();
 			attributeDefinition.setType(type);
 			attributeDefinition.setLanguage(language);
 		}
 
 		String name = nameFields.get(index).getValue();
 		attributeDefinition.setName(name);
 
 		return attributeDefinition;
 	}
 
 }
