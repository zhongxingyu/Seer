 package org.cotrix.web.importwizard.client.step.summary;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.cotrix.web.importwizard.client.util.ProgressDialog;
 import org.cotrix.web.importwizard.shared.AttributeDefinition;
 import org.cotrix.web.importwizard.shared.AttributeMapping;
 import org.cotrix.web.importwizard.shared.ImportMetadata;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SummaryStepViewImpl extends Composite implements SummaryStepView {
 
 	protected static final int FILE_FIELD_ROW = 0;
 	protected static final int PROPERTIES_FIELD_ROW = 2;
 	protected static final int MAPPING_MODE_FIELD_ROW = 3;
 
 	@UiTemplate("SummaryStep.ui.xml")
 	interface SummaryStepUiBinder extends UiBinder<Widget, SummaryStepViewImpl> {}
 	private static SummaryStepUiBinder uiBinder = GWT.create(SummaryStepUiBinder.class);
 
 	@UiField Grid panel;
 
 	@UiField Label fileField;
 	@UiField Label codelistField;
 	@UiField FlexTable propertiesTable;
 	@UiField Label mappingModeField;
 	@UiField FlexTable customTable;
 	@UiField Style style;
 
 	interface Style extends CssResource {
 		String headerlabel();
		String valuelabel();
		String summaryTable();
		String summaryTableHeader();
		String grid();
		String cell();
		String metadata();
		String metadataLabel();
 	}
 
 	private ProgressDialog progressDialog;
 
 	public SummaryStepViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
		panel.getColumnFormatter().setWidth(0, "150px");
		panel.getRowFormatter().setVerticalAlign(4, HasVerticalAlignment.ALIGN_TOP);
 	}
 
 	public void setMapping(List<AttributeMapping> mappings)
 	{
 		customTable.removeAllRows();
 		int row = 0;
 		for (AttributeMapping mapping:mappings) {
 			StringBuilder mappingDescription = new StringBuilder();
 			String originalName = mapping.getField().getLabel();
 			
 			if (mapping.isMapped()) {
 				mappingDescription.append("import <b>").append(originalName).append("</b>");
 				AttributeDefinition definition = mapping.getAttributeDefinition();
 				if (!originalName.equals(definition.getName())) mappingDescription.append(" as ").append(definition.getName());
 				if (definition.getType() !=null) {
 					mappingDescription.append(" is ").append(definition.getType().toString());
 					if (definition.getLanguage()!=null) mappingDescription.append(" in ").append(definition.getLanguage());
 				}
 			} else mappingDescription.append("ignore <b>").append(originalName).append("</b>");
 
 			HTML mappingLabel = new HTML(mappingDescription.toString());
 			customTable.setWidget(row, 0, mappingLabel);
 			row++;
 		}
 	}
 
 	public void setMetadata(ImportMetadata metadata) {
 
 		codelistField.setText(metadata.getName());
 
 		propertiesTable.removeAllRows();
 
 		if (metadata.getAttributes().size() == 0) {
 			panel.getRowFormatter().setVisible(PROPERTIES_FIELD_ROW, false);
 			//propertiesTable.setVisible(false);
 		} else {
 			panel.getRowFormatter().setVisible(PROPERTIES_FIELD_ROW, true);
 			//propertiesTable.setVisible(true);
 			propertiesTable.setText(0, 0, "Name");
 			propertiesTable.setText(0, 1, "Value");
 			propertiesTable.getCellFormatter().setStyleName(0, 0, style.headerlabel());
 			propertiesTable.getCellFormatter().setStyleName(0, 1, style.headerlabel());
 			int row = 1;
 			for (Entry<String, String> attribute:metadata.getAttributes().entrySet()) {
 				propertiesTable.setText(row, 0, attribute.getKey());
 				propertiesTable.setText(row, 1, attribute.getValue());
 				row++;
 			}
 		}
 	}
 
 	@Override
 	public void showProgress() {
 		if(progressDialog == null){
 			progressDialog = new ProgressDialog();
 		}
 		progressDialog.center();
 	}
 
 	@Override
 	public void hideProgress() {
 		if(progressDialog != null) progressDialog.hide();
 	}
 
 	public void setFileName(String fileName)
 	{
 		fileField.setText(fileName);
 	}
 
 	public void setFileNameVisible(boolean visible)
 	{
 		panel.getRowFormatter().setVisible(FILE_FIELD_ROW, visible);
 	}
 
 	public void setMappingMode(String mappingMode)
 	{
 		mappingModeField.setText(mappingMode);
 	}
 
 	public void setMappingModeVisible(boolean visible)
 	{
 		panel.getRowFormatter().setVisible(MAPPING_MODE_FIELD_ROW, visible);
 	}
 }
