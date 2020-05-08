 package org.cotrix.web.importwizard.client.step.summary;
 
 import java.util.Date;
 import java.util.List;
 
 import org.cotrix.web.importwizard.client.util.ProgressDialog;
 import org.cotrix.web.importwizard.shared.AttributeDefinition;
 import org.cotrix.web.importwizard.shared.AttributeMapping;
 import org.cotrix.web.importwizard.shared.ImportMetadata;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.shared.DateTimeFormat;
 import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SummaryStepViewImpl extends Composite implements SummaryStepView {
 	
 	protected static final int HEADER_ROW = 0;
 	protected static final int NAME_COLUMN = 0;
 	protected static final int TYPE_COLUMN = 1;
 	protected static final DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
 
 	@UiTemplate("SummaryStep.ui.xml")
 	interface SummaryStepUiBinder extends UiBinder<Widget, SummaryStepViewImpl> {}
 	private static SummaryStepUiBinder uiBinder = GWT.create(SummaryStepUiBinder.class);
 
 
 	@UiField HTMLPanel panel;
 	@UiField FlexTable summaryTable;
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
 		setupHeader();
 	}
 
 	protected void setupHeader() {
 		summaryTable.setWidget(HEADER_ROW, NAME_COLUMN, new HTML("Header"));
 		summaryTable.setWidget(HEADER_ROW, TYPE_COLUMN, new HTML("Type"));
 		summaryTable.setWidget(HEADER_ROW, 2, new HTML("Metadata"));
 		summaryTable.getCellFormatter().setStyleName(HEADER_ROW, NAME_COLUMN, style.summaryTableHeader());
 		summaryTable.getCellFormatter().setStyleName(HEADER_ROW, TYPE_COLUMN, style.summaryTableHeader());
 		summaryTable.getCellFormatter().setStyleName(HEADER_ROW, 2, style.summaryTableHeader());
 	}
 	
 	public void setMapping(List<AttributeMapping> mapping)
 	{
 		int row = 1;
 		for (AttributeMapping attributeMapping:mapping) {
 			HTML header = new HTML(attributeMapping.getField().getLabel());
 			HTML type = getType(attributeMapping.getAttributeDefinition());
 			summaryTable.setWidget(row, NAME_COLUMN, header);
 			summaryTable.setWidget(row, TYPE_COLUMN, type);
 			row++;
 		}
 	}
 	
	protected HTML getType(AttributeDefinition attribute)
 	{
		if (attribute.getType()==null) return new HTML("Not Defined");
		StringBuilder text = new StringBuilder(attribute.getType().getLabel());
		if (attribute.getLanguage()!=null) text.append(" (").append(attribute.getLanguage()).append(')');
 		return new HTML(text.toString());
 	}
 	
 	public void setMetadata(ImportMetadata metadata) {
 		summaryTable.getFlexCellFormatter().setRowSpan(7, 2, summaryTable.getRowCount() - 7);
 		summaryTable.setWidget(1, 2, new HTML("<strong>Name : </strong>"+metadata.getName()));
 		summaryTable.setWidget(2, 2, new HTML("<strong>Owner : </strong>"+metadata.getOwner()));
 		summaryTable.setWidget(3, 2, new HTML("<strong>Description : </strong>"+metadata.getDescription()));
 		summaryTable.setWidget(4, 2, new HTML("<strong>Create Date : </strong>"+format(metadata.getCreateDate())));
 		summaryTable.setWidget(5, 2, new HTML("<strong>Update Date: </strong>"+format(metadata.getUpdateDate())));
 		summaryTable.setWidget(6, 2, new HTML("<strong>Version : </strong>"+metadata.getVersion()));
 	}
 	
 	protected String format(Date date)
 	{
 		if (date == null) return "";
 		else return dateFormat.format(date);
 	}
 	
 	public void alert(String message) {
 
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
 }
