 package ca.ubc.cs310.golddiggers.propertytaxwatcher.client.page;
 
 import java.util.ArrayList;
 
 import ca.ubc.cs310.golddiggers.propertytaxwatcher.client.PropertyTaxWatcher;
 import ca.ubc.cs310.golddiggers.propertytaxwatcher.client.service.PropertyTaxSearchService;
 import ca.ubc.cs310.golddiggers.propertytaxwatcher.client.service.PropertyTaxSearchServiceAsync;
 import ca.ubc.cs310.golddiggers.propertytaxwatcher.client.service.TweeterService;
 import ca.ubc.cs310.golddiggers.propertytaxwatcher.client.service.TweeterServiceAsync;
 import ca.ubc.cs310.golddiggers.propertytaxwatcher.client.widget.DataTablePanel;
 import ca.ubc.cs310.golddiggers.propertytaxwatcher.server.PropertyTax;
 import ca.ubc.cs310.golddiggers.propertytaxwatcher.server.SearchParameter;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.visualization.client.AbstractDataTable;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.DataView;
 import com.google.gwt.visualization.client.VisualizationUtils;
 import com.google.gwt.visualization.client.visualizations.MapVisualization;
 import com.google.gwt.visualization.client.visualizations.Table;
 
 /**
  * SearchPage class. This class represents the search page on the Property Tax
  * Watch web app.
  * 
  * @author Hubert Ngu
  */
 public class SearchPage extends Page
 {
 	// Search fields.
 	private VerticalPanel searchPanel = new VerticalPanel();
 	private TextBox minCurrentValTextBox = new TextBox();
 	private TextBox maxCurrentValTextBox = new TextBox();
 	private TextBox postalCodeTextBox = new TextBox();
 	private Button searchButton = new Button("Search");
 	private Button detailResultButton = new Button("Detail/Simple View");
 	private Button compareButton = new Button("Compare");
 	private Button clearButton = new Button("Clear");
	private Label searchLabel = new Label("Search Result.");
 	private VerticalPanel resultPanel = new VerticalPanel();
 	private HorizontalPanel resultSubPanel = new HorizontalPanel();
 	private final TabPanel resultTabPanel = new TabPanel();
 	private final TabPanel compareTabPanel = new TabPanel();
 	private final int[] resultSimpleColumn = new int[] { 0, 11, 12, 19, 20 };
 	private final int[] resultDetailColumn = new int[26];
	private boolean hasSearch;
	private boolean hasCompare;
	private boolean isDetail;
	private DataTable dataTable;
 
 	// Server services
 	private static final TweeterServiceAsync tweeterService = GWT
 			.create(TweeterService.class);
 	private static final PropertyTaxSearchServiceAsync propertyTaxSearchService = GWT
 			.create(PropertyTaxSearchService.class);
 
 	public SearchPage()
 	{
 		super("Search");
 	}
 
 	@Override
 	public void loadPage()
 	{
 		super.loadPage();
 
		initializeFields();
 		this.runVisualizations();
 	}
 
 	private void runVisualizations()
 	{
 		// Load the search.
 		VisualizationUtils.loadVisualizationApi(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				PropertyTaxWatcher.initializePropertyTaxes();
 				createSearchWidget();
 			}
 		}, Table.PACKAGE);
 	}
 
 	private void createSearchWidget()
 	{
 
 		searchPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 
 		searchPanel.setStylePrimaryName("container-narrow");
 		searchPanel.addStyleName("row");
 		searchButton.setStylePrimaryName("btn btn-success");
 		detailResultButton.setStylePrimaryName("btn btn-success");
 
 		RootPanel.get().add(searchPanel);
 
 		searchPanel.add(minCurrentValTextBox);
 		searchPanel.add(maxCurrentValTextBox);
 		searchPanel.add(postalCodeTextBox);
 		searchPanel.add(searchButton);
 		searchPanel.add(clearButton);
 
 		minCurrentValTextBox.setText("Minimum Land Value");
 		maxCurrentValTextBox.setText("Maximum Land Value");
 		postalCodeTextBox.setText("Postal Code");
 
 		RootPanel.get().add(searchPanel);
 
		resultPanel.add(searchLabel);
 		resultPanel.add(detailResultButton);
 		resultPanel.add(compareButton);
 		resultPanel.add(resultSubPanel);
 		resultSubPanel.add(resultTabPanel);
 		resultSubPanel.add(compareTabPanel);
 		resultPanel.setVisible(hasSearch);
 		compareTabPanel.setVisible(hasCompare);
 		
 		resultTabPanel.setSize("600px", "400px");
 		compareTabPanel.setSize("600px", "400px");
 
 		RootPanel.get().add(resultPanel);
 		
 		createSearchButton();
 		createDetailResultButton();
 		createCompareButton();
 		createClearButton();
 	}
 
 	private void createClearButton(){
 		clearButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
				initializeFields();
 				
 				resultTabPanel.clear();
 				compareTabPanel.clear();
 				resultPanel.setVisible(hasSearch);
 				compareTabPanel.setVisible(hasCompare);
 
 				minCurrentValTextBox.setText("Minimum Land Value");
 				maxCurrentValTextBox.setText("Maximum Land Value");
 				postalCodeTextBox.setText("Postal Code");
 				
 				minCurrentValTextBox.selectAll();
 			}
 		});
 		return;
 	}
 	
 	private void createDetailResultButton()
 	{
 		detailResultButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				resultTabPanel.remove(0);
 				DataView resultView = DataView.create(dataTable);
 
 				if (isDetail)
 				{
 					resultView.setColumns(resultSimpleColumn);
 					isDetail = false;
 				} else
 				{
 					resultView.setColumns(resultDetailColumn);
 					isDetail = true;
 				}
 				resultTabPanel.add(new DataTablePanel(
 						(AbstractDataTable) resultView), "Result Table");
 				resultTabPanel.selectTab(0);
 			}
 		});
 		return;
 	}
 
 	private void createCompareButton()
 	{
 		compareButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				DataView resultView = DataView.create(dataTable);
 				// resultTabPanel.clear();
 				compareTabPanel.clear();
 				if (isDetail)
 				{
 					resultView.setColumns(resultDetailColumn);
 				} else
 				{
 					resultView.setColumns(resultSimpleColumn);
 				}
 				compareTabPanel.add(new DataTablePanel(
 						(AbstractDataTable) resultView), "Pervious Result");
 				hasCompare = true;
 				compareTabPanel.setVisible(hasCompare);
 				compareTabPanel.selectTab(0);
 			}
 		});
 		return;
 	}
 
 	private void createSearchButton()
 	{
 		searchButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				System.out.println("clicked!");
 				SearchParameter para = new SearchParameter();
 
 				String message = PropertyTaxWatcher.loginInfo.getNickname()
 						+ " searched for property tax values with";
 
 				String minMessage = minCurrentValTextBox.getText();
 				if (!minMessage.isEmpty()
 						&& !minMessage
 								.matches("Minimum Land Value"))
 				{
 					if (!minMessage.matches("^[0-9\\.]{1,}$"))
 					{
 						Window.alert("'" + minMessage
 								+ "' is not a valid value.");
 						minCurrentValTextBox.selectAll();
 						return;
 					} else
 					{
 						para.setMinCurrentLandValue(Double
 								.parseDouble(minMessage));
 						message += "lower limit = " + minMessage + " ";
 					}
 				}
 
 				String maxMessage = maxCurrentValTextBox.getText();
 				if (!maxMessage.isEmpty()
 						&& !maxMessage
 								.matches("Maximum Land Value"))
 				{
 					if (!maxMessage.matches("^[0-9\\.]{1,}$"))
 					{
 						Window.alert("'" + maxMessage
 								+ "' is not a valid value.");
 						maxCurrentValTextBox.selectAll();
 						return;
 					} else
 					{
 						para.setMaxCurrentLandValue(Double
 								.parseDouble(maxMessage));
 						message += "upper limit = " + maxMessage + " ";
 					}
 				}
 
 				if (para.hasMinCurrentLandValue()
 						&& para.hasMaxCurrentLandValue()
 						&& para.getMinCurrentLandValue() > para
 								.getMaxCurrentLandValue())
 				{
 					Window.alert("'" + minMessage + "' is larger than '"
 							+ maxMessage + ", not a valid bound.");
 					minCurrentValTextBox.selectAll();
 					maxCurrentValTextBox.selectAll();
 					return;
 				}
 
 				String postal = postalCodeTextBox.getText().toUpperCase()
 						.trim().replaceAll("\\s", "");
 				if (!postalCodeTextBox.getText().matches(
 						"Postal Code")
 						&& !postal.isEmpty())
 				{
 					if (!postal.matches("[A-Z][0-9][A-Z][0-9][A-Z][0-9]"))
 					{
 						Window.alert("'" + postal
 								+ "' is not a valid postal code.");
 						postalCodeTextBox.selectAll();
 						return;
 					} else
 					{
 						para.setPostalCode(postal.substring(0, 3) + " "
 								+ postal.substring(3));
 						message += "and postal code " + postal + " ";
 					}
 				}
 
 				if (!para.hasMaxCurrentLandValue() && !para.hasMinCurrentLandValue()
 						&& !para.hasPostalCode()){
 					Window.alert("Please type in search parameter.");
 					minCurrentValTextBox.selectAll();
 					return;
 				}
 				message += ".";
 
 				System.out.println("Go to Server!");
 				propertyTaxSearchService.searchProperty(para,
 						new AsyncCallback<ArrayList<PropertyTax>>()
 						{
 							public void onFailure(Throwable error)
 							{
 								GWT.log(error.getMessage());
 							}
 
 							public void onSuccess(ArrayList<PropertyTax> result)
 							{
 								hasSearch = true;
 								displayResult(result);
 								GWT.log("Search Succeed!");
 								System.out.println("Result back!");
 							}
 						});
 
 				tweet(message);
 			}
 		});
 		return;
 	}
 
 	private void displayResult(ArrayList<PropertyTax> result)
 	{
 		System.out.println("Displaying!");
 		DataTable resultDataTable = DataTable.create();
 		resultDataTable = PropertyTaxWatcher
 				.initializeDataTable(resultDataTable);
 		resultDataTable = PropertyTaxWatcher.populateDataTable(resultDataTable,
 				result);
 		dataTable = resultDataTable;
 		DataView resultView = DataView.create(resultDataTable);
 		DataView mapView = DataView.create(resultDataTable);
 
 		if (resultDataTable.getNumberOfRows() == 0)
 		{
 			Window.alert("No result found.");
 			return;
 		}
 
 		isDetail = false;
 		resultView.setColumns(resultSimpleColumn);
 		mapView.setColumns(new int[] { 12, 19 });
 
 		resultPanel.setVisible(hasSearch);
 		resultTabPanel.clear();
 		//resultTabPanel.add(createMapVisualization(mapView), "Map");
 		resultTabPanel.add(new DataTablePanel(
 				(AbstractDataTable) resultView), "Result Table");
 		System.out.println("Data table created!");
 		resultTabPanel.selectTab(0);
 	}
 
 	/**
 	 * Tweets a message on the golddigger310's Twitter account.
 	 * 
 	 * @param message
 	 *            The message to tweet.
 	 */
 	private void tweet(String message)
 	{
 		tweeterService.updateStatus(message, new AsyncCallback<Void>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{
 				GWT.log("Failed to tweet!");
 			}
 
 			@Override
 			public void onSuccess(Void result)
 			{
 				GWT.log("Status update successful!");
 			}
 
 		});
 	}
 
 	private Widget createMapVisualization(DataView dataView)
 	{
 		MapVisualization map = new MapVisualization("400", "400");
 		map.draw(dataView);
 		return map.asWidget();
 	}
 
	private void initializeFields()
 	{
 		for (int i = 0; i < resultDetailColumn.length; i++)
 			resultDetailColumn[i] = i;
		
		hasSearch = false;
		hasCompare = false;
		isDetail = false;
		dataTable = null;
 	}
 }
