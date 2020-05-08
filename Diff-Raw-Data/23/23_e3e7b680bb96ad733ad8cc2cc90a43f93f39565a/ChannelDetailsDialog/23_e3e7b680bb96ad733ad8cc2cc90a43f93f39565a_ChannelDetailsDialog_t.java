 /**
  * 
  */
 package org.cotrix.web.importwizard.client.step.channel;
 
 import java.util.List;
 
 import org.cotrix.web.importwizard.shared.AssetDetails;
 import org.cotrix.web.importwizard.shared.Property;
 import org.cotrix.web.importwizard.shared.RepositoryDetails;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class ChannelDetailsDialog extends DialogBox {
 
 	private static ChannelDetailsDialogUiBinder uiBinder = GWT
 			.create(ChannelDetailsDialogUiBinder.class);
 
 	interface ChannelDetailsDialogUiBinder extends
 	UiBinder<Widget, ChannelDetailsDialog> {
 	}
 
 	interface Style extends CssResource {
 		String headerlabel();
 		String valuelabel();
 		String flexTable();
 		String flexTableHeader();
 		String grid();
 		String cell();
 		String metadata();
 		String metadataLabel();
 
 	}
 
 	@UiField Label assetId;
 	@UiField Label assetName;
 	@UiField Label assetType;
 	@UiField FlexTable assetDetails;	
 
 	@UiField Label repositoryName;
 	@UiField Label repositoryPublishedTypes;
 	@UiField Label repositoryReturnedTypes;
 	@UiField FlexTable repositoryDetails;
 
 	@UiField Style style;
 
 	public ChannelDetailsDialog() {
 		setModal(true);
 		setGlassEnabled(true);
 		setAnimationEnabled(true);
 		setAutoHideEnabled(true);
		setWidth("500px");
 
 		/*Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 			public void execute() {
 				int left = Window.getScrollLeft()+ ((Window.getClientWidth( ) - box.getOffsetWidth( )) / 2); 
 				int top = Window.getScrollTop()+((Window.getClientHeight( ) - box.getOffsetHeight( )) / 4);
 				box.setPopupPosition(left, top);
 			}
 		});*/
 		setWidget(uiBinder.createAndBindUi(this));
 
 		setupEmpty(assetDetails);
 		setupEmpty(repositoryDetails);
 		
 	}
 
 	public void setAsset(AssetDetails asset)
 	{
 		assetId.setText(asset.getId());
 		assetName.setText(asset.getName());
 		assetType.setText(asset.getType());
 		addAssetProperties(asset.getProperties());
 
 		setRepository(asset.getRepository());
 	}
 
 	protected void setRepository(RepositoryDetails repository)
 	{
 		repositoryName.setText(repository.getName());
 		repositoryPublishedTypes.setText(repository.getPublishedTypes());
 		repositoryReturnedTypes.setText(repository.getReturnedTypes());
 
 		addRepositoryProperties(repository.getProperties());
 	}
 
 	public void addAssetProperties(List<Property> properties)
 	{
		assetDetails.removeAllRows();
 		if (!properties.isEmpty()) {
 			setupHeaders(assetDetails);
 			for (Property property:properties) addRow(assetDetails, property.getName(), property.getValue(), property.getDescription());
 		} else setupEmpty(assetDetails);
 	}
 
 	public void addRepositoryProperties(List<Property> properties)
 	{
		repositoryDetails.removeAllRows();
 		if (!properties.isEmpty()) {
 			setupHeaders(repositoryDetails);
 			for (Property property:properties) addRow(repositoryDetails, property.getName(), property.getValue(), property.getDescription());
 		} else setupEmpty(repositoryDetails);
 	}
 
 	protected void setupEmpty(FlexTable table)
 	{
 		table.setWidget(0, 0, getHeaderLabel("No properties"));
 		table.getFlexCellFormatter().setColSpan(0, 0, 3);
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
 		headerLabel.setStyleName(style.headerlabel());
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
