 /**
  * 
  */
 package org.cotrix.web.importwizard.client.step.repositorydetails;
 
 import java.util.List;
 
 import org.cotrix.web.importwizard.client.resources.Resources;
 import org.cotrix.web.importwizard.shared.Property;
 import org.cotrix.web.importwizard.shared.RepositoryDetails;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class RepositoryDetailsStepViewImpl extends Composite implements RepositoryDetailsStepView {
 	
 	protected static final int ASSET_PROPERTIES_ROW = 3;
 	protected static final int REPOSITORY_PROPERTIES_ROW = 3;
 
 	private static repositoryDetailsUiBinder uiBinder = GWT.create(repositoryDetailsUiBinder.class);
 	
 	@UiTemplate("RepositoryDetailsStep.ui.xml")
 	interface repositoryDetailsUiBinder extends UiBinder<Widget, RepositoryDetailsStepViewImpl> {
 	}
 
 	@UiField Grid repositoryDetails;
 	@UiField Label repositoryName;
 	@UiField Label repositoryPublishedTypes;
 	@UiField Label repositoryReturnedTypes;
 	@UiField FlexTable repositoryProperties;
 
 	public RepositoryDetailsStepViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	public void setRepository(RepositoryDetails repository)
 	{
 		repositoryName.setText(repository.getName());
		repositoryPublishedTypes.setText(repository.getPublishedTypes());
		repositoryReturnedTypes.setText(repository.getReturnedTypes());

 		addRepositoryProperties(repository.getProperties());
 	}
 
 	public void addRepositoryProperties(List<Property> properties)
 	{
 		repositoryProperties.removeAllRows();
 		repositoryDetails.getRowFormatter().setVisible(REPOSITORY_PROPERTIES_ROW, !properties.isEmpty());
 		
 		if (!properties.isEmpty()) {
 			setupHeaders(repositoryProperties);
 			for (Property property:properties) addRow(repositoryProperties, property.getName(), property.getValue(), property.getDescription());
 		}
 	}
 
 	protected void setupHeaders(FlexTable table)
 	{
 		table.getFlexCellFormatter().setColSpan(0, 0, 1);
 		table.setWidget(0, 0, getHeaderLabel("Name"));
 		table.setWidget(0, 1, getHeaderLabel("Value"));
 		table.setWidget(0, 2, getHeaderLabel("Description"));
 	}
 
 	protected Label getHeaderLabel(String text)
 	{
 		Label headerLabel = new Label(text);
 		headerLabel.setStyleName(Resources.INSTANCE.css().propertiesTableHeader());
 		return headerLabel;
 	}
 
 	protected void addRow(FlexTable table, String name, String value, String description)
 	{
 		int numRows = table.getRowCount();
 		table.setWidget(numRows, 0, new Label(name));
 		table.setWidget(numRows, 1, new Label(value));
 		table.setWidget(numRows, 2, new Label(description));
 	}
 }
