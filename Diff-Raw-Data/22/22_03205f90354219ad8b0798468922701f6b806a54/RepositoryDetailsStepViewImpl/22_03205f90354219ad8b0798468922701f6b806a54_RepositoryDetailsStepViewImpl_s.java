 /**
  * 
  */
 package org.cotrix.web.publish.client.wizard.step.repositorydetails;
 
 import java.util.List;
 
 import org.cotrix.web.common.client.resources.CommonResources;
import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.common.shared.codelist.Property;
 import org.cotrix.web.common.shared.codelist.RepositoryDetails;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Singleton;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class RepositoryDetailsStepViewImpl extends Composite implements RepositoryDetailsStepView {
 	
 	protected static final String NOTHING = "nothing";
 	protected static final int ASSET_PROPERTIES_ROW = 3;
 	protected static final int REPOSITORY_PROPERTIES_ROW = 3;
 
 	private static RepositoryDetailsUiBinder uiBinder = GWT.create(RepositoryDetailsUiBinder.class);
 	
 	@UiTemplate("RepositoryDetailsStep.ui.xml")
 	interface RepositoryDetailsUiBinder extends UiBinder<Widget, RepositoryDetailsStepViewImpl> {
 	}
 
 	@UiField Grid repositoryDetails;
 	@UiField Label repositoryName;
 	@UiField Label repositoryPublishedTypes;
 	@UiField Label repositoryReturnedTypes;
 	@UiField FlexTable repositoryProperties;
 
 	public RepositoryDetailsStepViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 		CommonResources.INSTANCE.css().ensureInjected();
 	}
 
 	public void setRepository(RepositoryDetails repository)
 	{
 		repositoryName.setText(repository.getName().toHtml());
 		repositoryPublishedTypes.setText(repository.getPublishedTypes().isEmpty()?NOTHING:repository.getPublishedTypes());
 		repositoryPublishedTypes.setStyleName(CommonResources.INSTANCE.css().missingValueText(), repository.getPublishedTypes().isEmpty());
 		
 		repositoryReturnedTypes.setText(repository.getReturnedTypes().isEmpty()?NOTHING:repository.getReturnedTypes());
 		repositoryReturnedTypes.setStyleName(CommonResources.INSTANCE.css().missingValueText(), repository.getReturnedTypes().isEmpty());
 		
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
 		headerLabel.setStyleName(CommonResources.INSTANCE.css().propertiesTableHeader());
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
