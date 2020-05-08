 package org.cotrix.web.importwizard.client.step.sdmxmapping;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cotrix.web.importwizard.client.resources.Resources;
 import org.cotrix.web.importwizard.client.util.AlertDialog;
 import org.cotrix.web.importwizard.shared.AttributeDefinition;
 import org.cotrix.web.importwizard.shared.AttributeMapping;
 import org.cotrix.web.importwizard.shared.Field;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SdmxMappingStepViewImpl extends Composite implements SdmxMappingStepView {
 
 	@UiTemplate("SdmxMappingStep.ui.xml")
 	interface HeaderTypeStepUiBinder extends UiBinder<Widget, SdmxMappingStepViewImpl> {}
 	private static HeaderTypeStepUiBinder uiBinder = GWT.create(HeaderTypeStepUiBinder.class);
 
 	protected static int IGNORE_COLUMN = 0;
 	protected static int NAME_COLUMN = 1;
 	protected static int LABEL_COLUMN = 2;
 
 	@UiField FlexTable columnsTable;
 	@UiField TextBox name;
 	@UiField Style style;
 
 	private AlertDialog alertDialog;
 
 	interface Style extends CssResource {
 		String cell();
 	}
 
 	protected List<ToggleButton> excludeButtons = new ArrayList<ToggleButton>();
 	protected List<TextBox> nameFields = new ArrayList<TextBox>();
 	protected List<Field> fields = new ArrayList<Field>();
 	protected List<AttributeDefinition> definitions = new ArrayList<AttributeDefinition>();
 
 	public SdmxMappingStepViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 		Resources.INSTANCE.css().ensureInjected();
 	}
 	
 	@Override
 	public void setCodelistName(String name) {
 		this.name.setValue(name);
 	}
 
 	@Override
 	public String getCodelistName() {
 		return this.name.getValue();
 	}
 
 	public void setAttributes(List<AttributeMapping> mappings)
 	{
 		columnsTable.removeAllRows();
 		excludeButtons.clear();
 		nameFields.clear();
 		fields.clear();
 		definitions.clear();
 
 		FlexCellFormatter cellFormatter = columnsTable.getFlexCellFormatter();
 
 		for (AttributeMapping attributeMapping:mappings) {
 			final int row = columnsTable.getRowCount();
 
			final ToggleButton excludeButton = new ToggleButton(new Image(Resources.INSTANCE.trash()), new Image(Resources.INSTANCE.trasTick()));
 			excludeButton.setStyleName(Resources.INSTANCE.css().imageButton());
 			excludeButton.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					Log.trace("Exclude is Down? "+excludeButton.isDown());
 					setExclude(row, excludeButton.isDown());
 				}
 			});
 			columnsTable.setWidget(row, IGNORE_COLUMN, excludeButton);
 			excludeButtons.add(excludeButton);
 
 			Field field = attributeMapping.getField();
 			fields.add(field);
 
 			TextBox nameField = new TextBox();
 			nameField.setStyleName(Resources.INSTANCE.css().textBox());
 			nameField.setValue(field.getLabel());
 			columnsTable.setWidget(row, NAME_COLUMN, nameField);
 			cellFormatter.setStyleName(row, NAME_COLUMN, style.cell());
 			nameFields.add(nameField);
 			
 			AttributeDefinition attributeDefinition = attributeMapping.getAttributeDefinition();
 			definitions.add(attributeDefinition);
 			if (attributeDefinition == null) {
 				excludeButton.setDown(true);
 			} else {
 				excludeButton.setDown(false);
 			}
 		}
 	}
 
 	protected void setExclude(int row, boolean exclude)
 	{
 		((TextBox)columnsTable.getWidget(row, NAME_COLUMN)).setEnabled(!exclude);
 		((Label)columnsTable.getWidget(row, LABEL_COLUMN)).setStyleName(Resources.INSTANCE.css().paddedTextDisabled(), exclude);
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
 		if (excludeButtons.get(index).isDown()) return null;
 
 		AttributeDefinition attributeDefinition = definitions.get(index);
 
 		String name = nameFields.get(index).getValue();
 		attributeDefinition.setName(name);
 		
 		return attributeDefinition;
 	}
 
 	public void alert(String message) {
 		if(alertDialog == null){
 			alertDialog = new AlertDialog();
 		}
 		alertDialog.setMessage(message);
 		alertDialog.show();
 	}
 
 }
