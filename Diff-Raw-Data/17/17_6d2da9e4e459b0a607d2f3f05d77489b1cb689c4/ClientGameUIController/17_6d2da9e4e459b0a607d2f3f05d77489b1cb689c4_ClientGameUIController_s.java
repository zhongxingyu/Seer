 package client.ui;
 
 /**
  * Created by:
  * User: Lars Trey
  * Date: 08.10.13
  * Time: 16:35
  */
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.scene.Scene;
 import javafx.scene.chart.CategoryAxis;
 import javafx.scene.chart.LineChart;
 import javafx.scene.chart.NumberAxis;
 import javafx.scene.chart.PieChart;
 import javafx.scene.chart.StackedBarChart;
 import javafx.scene.chart.XYChart;
 import javafx.scene.control.Button;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.ContentDisplay;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListView;
 import javafx.scene.control.ProgressBar;
 import javafx.scene.control.SelectionMode;
 import javafx.scene.control.Slider;
 import javafx.scene.control.TableCell;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.TitledPane;
 import javafx.scene.control.Tooltip;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.AnchorPane;
 import javafx.stage.Modality;
 import javafx.stage.Stage;
 import javafx.stage.StageStyle;
 import javafx.util.Callback;
 import javafx.util.StringConverter;
 import message.GameDataMessageFromClient;
 import message.GameDataMessageToClient;
 import message.GameDataMessageToClient.StorageToClient.StorageElementToClient;
 import client.ui.ClientGameUIModel.Benefit;
 import client.ui.ClientGameUIModel.BenefitBooking;
 import client.ui.ClientGameUIModel.Offer;
 import client.ui.ClientGameUIModel.ProductionOrder;
 import client.ui.ClientGameUIModel.Request;
 import client.ui.ClientGameUIModel.StoragePosition;
 import client.ui.ClientGameUIModel.SupplierOffer;
 
 /**
  * Dies ist die Controller-Klasse der Game-Stage. Hier wird alles implementiert,
  * was das Game-UI manipulieren soll.
  * 
  * @author Lars Trey
  */
 public class ClientGameUIController implements Initializable {
 
 	private ClientGameUIModel model;
 	private static ClientGameUIController clientGameUIController;
 	private Stage dialogStage;
 
 	/**
 	 * Hier werden alle Felder des UIs mit Variablen verknpft.
 	 */
 	@FXML
 	private ListView<String> eventListView;
 	private ObservableList<String> events = FXCollections.observableArrayList();
 	@FXML
 	private Button endRoundButton;
 	@FXML
 	private Label roundLabel;
 	@FXML
 	private ProgressBar roundProgressBar;
 	// Purchase
 	@FXML
 	private TitledPane newPurchaseRequestTitledPane;
 	@FXML
 	private Button newPurchaseRequestButton;
 	@FXML
 	private Button newPurchaseRequestSaveButton;
 	@FXML
 	private ChoiceBox<String> newPurchaseRequestArticleNameChoiceBox;
 	@FXML
 	private Slider newPurchaseRequestArticleQualitySlider;
 	@FXML
 	private TextField newPurchaseRequestArticleQualityTextField;
 	@FXML
 	private TableView<Request> purchaseRequestsTableView;
 	@FXML
 	private TableColumn<Request, String> purchaseRequestArticleTableColumn;
 	@FXML
 	private TableColumn<Request, Integer> purchaseRequestIdTableColumn;
 	@FXML
 	private TableColumn<Request, String> purchaseRequestQualityTableColumn;
 	@FXML
 	private TableColumn<Request, String> purchaseRequestStatusTableColumn;
 	@FXML
 	private TableView<SupplierOffer> purchaseOffersTableView;
 	@FXML
 	private TableColumn<SupplierOffer, Integer> purchaseOffersIdTableColumn;
 	@FXML
 	private TableColumn<SupplierOffer, String> purchaseOffersArticleTableColumn;
 	@FXML
 	private TableColumn<SupplierOffer, String> purchaseOffersQualityTableColumn;
 	@FXML
 	private TableColumn<SupplierOffer, String> purchaseOffersQuantityTableColumn;
 	@FXML
 	private TableColumn<SupplierOffer, String> purchaseOffersPriceTableColumn;
 	// Production
 	@FXML
 	private TitledPane newProductionOrderTitledPane;
 	@FXML
 	private Button newProductionOrderButton;
 	@FXML
 	private Button newProductionOrderSaveButton;
 	@FXML
 	private ChoiceBox<StoragePosition> newProductionOrderWaferChoiceBox;
 	@FXML
 	private Slider newProductionOrderOutputQuantitySlider;
 	@FXML
 	private TextField newProductionOrderOutputQuantityTextField;
 	@FXML
 	private ChoiceBox<StoragePosition> newProductionOrderCaseChoiceBox;
 	@FXML
 	private TextField newProductionOrderCostsTextField;
 	@FXML
 	private TableView<ProductionOrder> productionOrdersTableView;
 	@FXML
 	private TableColumn<ProductionOrder, Integer> productionOrderIdTableColumn;
 	@FXML
 	private TableColumn<ProductionOrder, String> productionOrderQualityWaferTableColumn;
 	@FXML
 	private TableColumn<ProductionOrder, String> productionOrderQualityCaseTableColumn;
 	@FXML
 	private TableColumn<ProductionOrder, String> productionOrderTargetQuantityTableColumn;
 	@FXML
 	private TableColumn<ProductionOrder, String> productionOrderQualityPanelTableColumn;
 	@FXML
 	private TableColumn<ProductionOrder, String> productionOrderActualQuantityTableColumn;
 	@FXML
 	private TableColumn<ProductionOrder, String> productionOrderCostsPerUnitTableColumn;
 	@FXML
 	private TextField machineryLevelTextField;
 	@FXML
 	private TextField machineryMaximumCapacityTextField;
 	@FXML
 	private TextField machineryPlannedCapacityTextField;
 	@FXML
 	private ProgressBar machineryWorkloadProgressBar;
 	@FXML
 	private CheckBox machineryIncreaseLevelCheckBox;
 	// Storage
 	private ObservableList<StoragePosition> resourcesInStorage = FXCollections
 			.observableArrayList();
 	private ObservableList<StoragePosition> waferInStorage = FXCollections
 			.observableArrayList();
 	private ObservableList<StoragePosition> casesInStorage = FXCollections
 			.observableArrayList();
 	@FXML
 	private TextField storageCostsWaferTextField;
 	@FXML
 	private TextField storageCostsCasesTextField;
 	@FXML
 	private TextField storageCostsPanelsTextField;
 	@FXML
 	private TableView<StoragePosition> storagePositionsTableView;
 	@FXML
 	private TableColumn<StoragePosition, Integer> storagePositionIdTableColumn;
 	@FXML
 	private TableColumn<StoragePosition, String> storagePositionRessourceTableColumn;
 	@FXML
 	private TableColumn<StoragePosition, String> storagePositionQualityTableColumn;
 	@FXML
 	private TableColumn<StoragePosition, String> storagePositionQuantityTableColumn;
 	@FXML
 	private TableColumn<StoragePosition, String> storagePositionCostsTableColumn;
 	// Sales
 	@FXML
 	private Button newSaleOfferButton;
 	@FXML
 	private Button newSaleOfferSaveButton;
 	@FXML
 	private ChoiceBox<StoragePosition> newSaleOfferArticleChoiceBox;
 	@FXML
 	private Slider newSaleOfferArticleQuantitySlider;
 	@FXML
 	private TextField newSaleOfferArticleQuantityTextField;
 	@FXML
 	private TextField newSaleOfferArticlePriceTextField;
 	@FXML
 	private TextField newSaleOfferCostsTextField;
 	@FXML
 	private TextField newSaleOfferDistributionCostsTextField;
 	@FXML
 	private TextField newSaleOfferMaximumProfitTextField;
 	@FXML
 	private TitledPane newSaleOfferTitledPane;
 	@FXML
 	private TableView<Offer> salesTableView;
 	@FXML
 	private TableColumn<Offer, Integer> salesIdTableColumn;
 	@FXML
 	private TableColumn<Offer, String> salesProductTableColumn;
 	@FXML
 	private TableColumn<Offer, String> salesQualityTableColumn;
 	@FXML
 	private TableColumn<Offer, String> salesPriceTableColumn;
 	@FXML
 	private TableColumn<Offer, String> salesQuantityTableColumn;
 	@FXML
 	private TableColumn<Offer, String> salesSoldQuantityTableColumn;
 	@FXML
 	private TableColumn<Offer, String> salesProfitTableColumn;
 	@FXML
 	private TableColumn<Offer, String> salesCostsTableColumn;
 	// HumanResources
 	@FXML
 	private TextField hrWagesPerHourTextField;
 	@FXML
 	private TextField hrAverageWagesTextField;
 	@FXML
 	private TextField hrCountEmployeesTextField;
 	@FXML
 	private TextField hrWageCostsTextField;
 	@FXML
 	private ChoiceBox<Benefit> benefitsChoiceBox;
 	private ObservableList<Benefit> possibleBenefits = FXCollections
 			.observableArrayList();
 	@FXML
 	private Button bookBenefitButton;
 	@FXML
 	private TextField bookBenefitDurationTextField;
 	@FXML
 	private TextField bookBenefitCostsPerRoundTextField;
 	@FXML
 	private TextField benefitsTotalCostsTextField;
 	@FXML
 	private TableView<BenefitBooking> bookedBeneftisTableView;
 	@FXML
 	private LineChart<String, Double> hrMotivationLineChart;
 	@FXML
 	private CategoryAxis hrMotivationLineChartXAxis;
 	@FXML
 	private NumberAxis hrMotivationLineChartYAxis;
 	// Marketing
 	@FXML
 	private TextField marketResearchAverageWagesLastRoundTextField;
 	@FXML
 	private TextField marketResearchPeakAMarketTextField;
 	@FXML
 	private TextField marketResearchPeakCMarketTextField;
 	@FXML
 	private CheckBox marketResearchBuyMarketResearchCheckBox;
 	@FXML
 	private LineChart<String, Double> marketingWaferPriceChart;
 	@FXML
 	private CategoryAxis marketingWaferPriceChartXAxis;
 	@FXML
 	private NumberAxis marketingWaferPriceChartYAxis;
 	@FXML
 	private LineChart<String, Double> marketingCasePriceChart;
 	@FXML
 	private CategoryAxis marketingCasePriceChartXAxis;
 	@FXML
 	private NumberAxis marketingCasePriceChartYAxis;
 	@FXML
 	private PieChart marketingMarketSharePieChart;
 	// Reporting
 	@FXML
 	private TextField reportingSalesFixCostsTextField;
 	@FXML
 	private TextField reportingProductionFixCostsTextField;
 	@FXML
 	private TextField reportingStorageFixCostsTextField;
 	@FXML
 	private TextField reportingPurchaseFixCostsTextField;
 	@FXML
 	private TextField reportingHRFixCostsTextField;
 	@FXML
 	private TextField reportingMarketingFixCostsTextField;
 	@FXML
 	private TextField reportingMachineryLevelTextField;
 	@FXML
 	private TextField reportingMachineryMaxCapacityTextField;
 	@FXML
 	private ProgressBar reportingMachineryAvgWorkloadProgressBar;
 	@FXML
 	private ProgressBar reportingMachineryLastRoundWorkloadProgressBar;
 	@FXML
 	private StackedBarChart<String, Double> reportingSalesBarChart;
 	@FXML
 	private CategoryAxis reportingSalesBarChatXAxis;
 	@FXML
 	private NumberAxis reportingSalesBarChatYAxis;
 	@FXML
 	private LineChart reportingCompanyValueLineChart;
 
 	public static ClientGameUIController getClientGameUIController() {
 		return clientGameUIController;
 	}
 
 	public void setModel(ClientGameUIModel model) {
 		this.model = model;
 	}
 
 	private void loadLineChart(String title, MouseEvent event,
 			ArrayList<HashMap<String, Double>> data, String[] cat,
 			boolean symbols) {
 		try {
 			// Load the fxml file and create a new stage for the popup
 			FXMLLoader loader = new FXMLLoader(
 					ClientUIStart.class.getResource("PopupLineChart.fxml"));
 			AnchorPane page = (AnchorPane) loader.load();
 			Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
 			dialogStage.setTitle(title);
 			dialogStage.initModality(Modality.NONE);
 			Scene scene = new Scene(page);
 			dialogStage.setScene(scene);
 			dialogStage.setX(event.getScreenX() + 75);
 			dialogStage.setY(event.getScreenY() + 30 - dialogStage.getHeight());
 
 			PopupLineChartController controller = (PopupLineChartController) loader
 					.getController();
 			controller.setDialogStage(dialogStage);
 			controller.setGraphData(data, cat, symbols);
 			this.dialogStage = dialogStage;
 			dialogStage.show();
 
 		} catch (IOException e) {
 			// Exception gets thrown if the fxml file could not be loaded
 			e.printStackTrace();
 		}
 	}
 
 	@FXML
 	public void onMouseEnteredSales(MouseEvent event) {
 		try {
 			// Load the fxml file and create a new stage for the popup
 			FXMLLoader loader = new FXMLLoader(
 					ClientUIStart.class
 							.getResource("PopUpStackedBarChart.fxml"));
 			AnchorPane page = (AnchorPane) loader.load();
 			Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
 			dialogStage.setTitle("Sales");
 			dialogStage.initModality(Modality.NONE);
 			Scene scene = new Scene(page);
 			dialogStage.setScene(scene);
 			dialogStage.setX(event.getScreenX() + 75);
 			dialogStage.setY(event.getScreenY() + 30 - dialogStage.getHeight());
 
 			PopupStackedBarChartController controller = (PopupStackedBarChartController) loader
 					.getController();
 			controller.setDialogStage(dialogStage);
 
 			ArrayList<HashMap<String, Double>> data = this.model
 					.getSalesChartData();
 
 			String[] cat = new String[5];
 
 			int index = 0;
 			for (int i = ClientGameUIModel.getRound() - 5; i < ClientGameUIModel
 					.getRound(); i++) {
 				cat[index++] = "Runde " + i;
 			}
 
 			controller.setGraphData(data, cat);
 
 			// Show the dialog and wait until the user closes it
 			dialogStage.show();
 			this.dialogStage = dialogStage;
 
 		} catch (IOException e) {
 			// Exception gets thrown if the fxml file could not be loaded
 			e.printStackTrace();
 		}
 
 	}
 
 	@FXML
 	public void onMouseEnteredWafer(MouseEvent event) {
 
 		ArrayList<HashMap<String, Double>> data = this.model
 				.getWaferPriceListChartData();
 
 		String[] cat = new String[100];
 		for (int i = 0; i < 100; i++) {
 			cat[i] = (i + 1) + "";
 		}
 		loadLineChart("Wafer", event, data, cat, false);
 		
 	}
 
 	@FXML
 	public void onMouseEnteredCase(MouseEvent event) {
 
 		ArrayList<HashMap<String, Double>> data = this.model
 				.getCasePriceListChartData();
 
 		String[] cat = new String[100];
 		for (int i = 0; i < 100; i++) {
 			cat[i] = (i + 1) + "";
 		}
 		loadLineChart("Wafer", event, data, cat, false);
 	}
 
 	@FXML
 	public void onMouseEnteredMotivation(MouseEvent event) {
 
 		ArrayList<HashMap<String, Double>> data = this.model
 				.getMotivationChartData();
 
 		String[] catMot = new String[ClientGameUIModel.getRound()];
 		for (int i = 0; i < catMot.length; i++) {
 			catMot[i] = (i + 1) + "";
 		}
 		loadLineChart("Wafer", event, data, catMot, true);
 
 	}
 
 	@FXML
 	public void onMouseExited(MouseEvent event) {
 		dialogStage.close();
 	}
 
 	@FXML
 	public void onMouseMoved(MouseEvent event) {
 		dialogStage.setX(event.getScreenX() + 75);
 		dialogStage.setY(event.getScreenY() + 30 - dialogStage.getHeight());
 	}
 
 	/**
 	 * Hier werden alle Felder des UIs initialisiert, die initial beim Aufrufen
 	 * des UIs gefuellt sein sollen.
 	 */
 	@Override
 	public void initialize(URL url, ResourceBundle rb) {
 		
 		model = new ClientGameUIModel();
 		model.setIn((GameDataMessageToClient) ClientUIStart.getLoginModel().client
 				.readMessage());
 		model.parseAnswerFromServer();
 		model.setupMessageCreator();
 
 		initGeneral();
 		initStorage();
 		initPurchase();
 		initProduction();
 		initSales();
 		initHumanResources();
 		initMarketing();
 		initReporting();
 		
 		//START BUILDING SALES CHART
 		 String[] cat = new String[5];
 		 int index=0;
 		 for(int i=ClientGameUIModel.getRound()-5; i < ClientGameUIModel.getRound(); i++) {
 			 cat[index++] = "Runde " + i;
 		 }
 		
 		 buildXYChart(this.model.getSalesChartData(), cat,
 		 reportingSalesBarChatXAxis, reportingSalesBarChatYAxis,
 		 reportingSalesBarChart);
 		
 		 // START BULDING MOTIVATION CHART
 		
 		 String[] catMot = new String[ClientGameUIModel.getRound()];
 		 for( int i=0; i<catMot.length; i++) {
 			 catMot[i] = (i+1)+"";
 		 }
 		
 		 buildXYChart(this.model.getMotivationChartData(), catMot,
 		 hrMotivationLineChartXAxis, hrMotivationLineChartYAxis,
 		 hrMotivationLineChart);
 		
 		 // START BUILDING WAFER PRICE CHART
 		 String[] catWafer = new String[100];
 		 for( int i=0; i<catWafer.length; i++) {
 			 catWafer[i] = (i+1) + "";
 		 }
 		
 		 buildXYChart(this.model.getWaferPriceListChartData(), catWafer,
 		 marketingWaferPriceChartXAxis,
 		 marketingWaferPriceChartYAxis,marketingWaferPriceChart );
 		 marketingWaferPriceChart.setCreateSymbols(false);
 		
 		 // START BUILDING Case PRICE CHART
 		 String[] catCase = new String[100];
 		 for( int i=0; i<catCase.length; i++) {
 			 catCase[i] = (i+1) + "";
 		 }
 		
 		 buildXYChart(this.model.getCasePriceListChartData(), catCase,
 		 marketingCasePriceChartXAxis,
 		 marketingCasePriceChartYAxis,marketingCasePriceChart );
 		 marketingCasePriceChart.setCreateSymbols(false);
 		
 		 // START BUILDING Market Shares
 		
 		 buildPieChart(this.model.getMarketShareChartData(),
 		 marketingMarketSharePieChart);
 		
 		 //END WORK
 		 
 	}
 
 	private int deformatCurrency(String oldCurrencyString) {
 
 		String currencyString = "";
 		currencyString = oldCurrencyString.replace(",", "");
 		// System.out.println(currencyString);
 		currencyString = currencyString.replace(".", "");
 		// System.out.println(currencyString);
 		currencyString = currencyString.replace(" ", "");
 		// System.out.println(currencyString);
 		currencyString = currencyString.replace("", "");
 		// System.out.println(currencyString);
 		int currencyValue = Integer.parseInt(currencyString);
 
 		return currencyValue;
 	}
 
 	private int deformatQuantity(String oldQuantityString) {
 
 		String quantityString = "";
 		quantityString = oldQuantityString.replace(".", "");
 		int quantityValue = Integer.parseInt(quantityString);
 
 		return quantityValue;
 	}
 
 	private void updateEventListView(String text) {
 		events.add("Runde " + ClientGameUIModel.getRound() + ":   " + text);
 	}
 
 	private void initGeneral() {
 
 		processRoundProgressBar(ClientGameUIModel.getRound());
 		updateEventListView(model.getnFormatterCurrency().format(
 				model.getIn().cash)
 				+ " Barvermgen");
 		eventListView.setItems(events);
 
 		/**
 		 * Tabbergreifende Einstellungen
 		 */
 
 		/**
 		 * Hier werden alle Slider bidirektional mit der dazugehrigen Textbox
 		 * verknpft. Der Wert wird dabei durch den StringConverter auf einen
 		 * Integer ohne NKST gesetzt.
 		 */
 
 		StringConverter<Number> stringConverterForSliders = new StringConverter<Number>() {
 
 			@Override
 			public String toString(Number t) {
 				Integer number = t.intValue();
 				return number.toString();
 			}
 
 			@Override
 			public Number fromString(String string) {
 				return Double.parseDouble(string);
 			}
 
 		};
 
 		newPurchaseRequestArticleQualityTextField.textProperty()
 				.bindBidirectional(
 						newPurchaseRequestArticleQualitySlider.valueProperty(),
 						stringConverterForSliders);
 		newProductionOrderOutputQuantityTextField.textProperty()
 				.bindBidirectional(
 						newProductionOrderOutputQuantitySlider.valueProperty(),
 						stringConverterForSliders);
 		newSaleOfferArticleQuantityTextField.textProperty().bindBidirectional(
 				newSaleOfferArticleQuantitySlider.valueProperty(),
 				stringConverterForSliders);
 
 		/**
 		 * ActionListener
 		 */
 
 		endRoundButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent actionEvent) {
 
 //				for (Request req : purchaseRequestsTableView.getItems()) {
 //
 //					if (req.getStatus().equals("Offen")) {
 //
 //						for (SupplierOffer offer : req.getOffer()) {
 //							if (!offer.getQuantity().isEmpty()) {
 //								model.getMessCreator().addAccepted(
 //										offer.getName(),
 //										Integer.parseInt(offer.getQuality()),
 //										Integer.parseInt(offer.getQuantity()));
 //								break;
 //							}
 //						}
 //					}
 //				}
 
 				if (machineryIncreaseLevelCheckBox.selectedProperty().get() == true) {
 					model.getMessCreator().setMachine(true);
 				} else if (machineryIncreaseLevelCheckBox.selectedProperty()
 						.get() == false) {
 					model.getMessCreator().setMachine(false);
 				}
 
 				if (marketResearchBuyMarketResearchCheckBox.selectedProperty()
 						.get() == true) {
 					model.getMessCreator().setMarketResearch(true);
 				} else if (machineryIncreaseLevelCheckBox.selectedProperty()
 						.get() == false) {
 					model.getMessCreator().setMarketResearch(false);
 				}
 				
 				//model.getMessCreator().setWage(deformatCurrency(hrWagesPerHourTextField.getText())*100);
 				model.getMessCreator().setWage(deformatCurrency(hrWagesPerHourTextField.getText()));
 
 				ClientUIStart.getLoginModel().client
 						.writeMessage((GameDataMessageFromClient) model
 								.getMessCreator().getSendMessage());
 
 				model.setIn((GameDataMessageToClient) ClientUIStart
 						.getLoginModel().client.readMessage());
 				model.parseAnswerFromServer();
 				model.setupMessageCreator();
 
 				initGeneral();
 				initStorage();
 				initPurchase();
 				initProduction();
 				initSales();
 				initHumanResources();
 				initMarketing();
 				initReporting();
 
 			}
 		});
 
 	}
 
 	class EditingCell extends TableCell<Request, String> {
 
 		private TextField textField;
 		boolean alreadyFilledCell;
 
 		public EditingCell() {
 		}
 		
 		
 
 		@Override
 		public void startEdit() {
 
 			for (int i = 0; i < 3; i++) {
 				if (this.getTableColumn().getCellData(i).isEmpty()) {
 					alreadyFilledCell = false;
 				} else {
 					alreadyFilledCell = true;
 					break;
 				}
 			}
 
 			if (purchaseRequestsTableView.getSelectionModel().getSelectedItem()
 					.getStatus().equals("Offen")
 					&& alreadyFilledCell == false) {
 
 				super.startEdit();
 
 				if (textField == null) {
 					createTextField();
 				}
 
 				setGraphic(textField);
 				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
 				textField.selectAll();
 			
 //			} else if (purchaseRequestsTableView.getSelectionModel()
 //					.getSelectedItem().getStatus().equals("Offen")
 //					&& alreadyFilledCell == true) {
 //
 //				if (!this.getItem().toString().isEmpty()) {
 //
 //					super.startEdit();
 //
 //					if (textField == null) {
 //						createTextField();
 //					}
 //
 //					setGraphic(textField);
 //					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
 //					textField.selectAll();
 //
 //				} else {
 //					cancelEdit();
 //				}
 
 			} else {
 				cancelEdit();
 			}
 
 		}
 
 		@Override
 		public void cancelEdit() {
 			super.cancelEdit();
 
 			setText(String.valueOf(getItem()));
 			setContentDisplay(ContentDisplay.TEXT_ONLY);
 		}
 
 		@Override
 		public void updateItem(String item, boolean empty) {
 			super.updateItem(item, empty);
 
 			if (empty) {
 				setText(null);
 				setGraphic(null);
 			} else {
 				if (isEditing()) {
 					if (textField != null) {
 						textField.setText(getString());
 					}
 					setGraphic(textField);
 					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
 				} else {
 					setText(getString());
 					setContentDisplay(ContentDisplay.TEXT_ONLY);
 				}
 			}
 		}
 
 		private void createTextField() {
 			textField = new TextField(getString());
 			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()
 					* 2);
 			textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
 
 				@Override
 				public void handle(KeyEvent t) {
 					if (t.getCode() == KeyCode.ENTER) {
 						commitEdit(textField.getText());
 					} else if (t.getCode() == KeyCode.ESCAPE) {
 						cancelEdit();
 					}
 				}
 			});
 		}
 
 		private String getString() {
 			return getItem() == null ? "" : getItem().toString();
 		}
 
 	}
 
 	private void initPurchase() {
 
 		/**
 		 * purchaseRequestTable: CellFactory
 		 */
 
 		purchaseRequestArticleTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Request, String>(
 						"name"));
 
 		purchaseRequestIdTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Request, Integer>(
 						"id"));
 
 		purchaseRequestStatusTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Request, String>(
 						"status"));
 
 		purchaseRequestQualityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Request, String>(
 						"quality"));
 
 		purchaseRequestsTableView.setItems(model.getPurchaseRequestTableData());
 
 		/**
 		 * purchaseRequestTable: Misc
 		 */
 		
 		final ChangeListener<Request> purchaseRequestsTableViewListener = new ChangeListener<Request>() {
 			public void changed(ObservableValue<? extends Request> observable, Request oldValue, Request newValue) {
 				model.getPurchaseOffersTableData().clear();
 				for (SupplierOffer o : newValue.getOffer()) {
 					model.getPurchaseOffersTableData().add(o);
 					if (o.getRound() == ClientGameUIModel.getRound() - 1) {
 						purchaseOffersTableView.setEditable(true);
 						purchaseOffersQuantityTableColumn.setEditable(true);
 					}
 				}
 			}
 		};
 		
 //		purchaseRequestsTableView.getSelectionModel().selectedItemProperty().addListener(purchaseRequestsTableViewListener);
 //		purchaseRequestsTableView.getSelectionModel().selectedItemProperty().removeListener(purchaseRequestsTableViewListener);
 
 		// Verhindert, dass man eine neue Spalte durch schieben hinzufgen kann
 		purchaseRequestsTableView
 				.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
 		// purchaseRequestIdTableColumn.setSortType(TableColumn.SortType.DESCENDING);
 		purchaseRequestsTableView.getSelectionModel().setSelectionMode(
 				SelectionMode.SINGLE);
 
 		purchaseRequestsTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Request>() {
 			public void changed(ObservableValue<? extends Request> observable, Request oldValue, Request newValue) {
 				model.getPurchaseOffersTableData().clear();
				for (SupplierOffer o : newValue.getOffer()) {
					model.getPurchaseOffersTableData().add(o);
					if (o.getRound() == ClientGameUIModel.getRound() - 1) {
						purchaseOffersTableView.setEditable(true);
						purchaseOffersQuantityTableColumn.setEditable(true);
 					}
 				}
 			}
 		});
 		
 
 		/**
 		 * purchaseOffersTable: CellFactory
 		 */
 
 		Callback<TableColumn<SupplierOffer, String>, TableCell<SupplierOffer, String>> cellFactory = new Callback<TableColumn<SupplierOffer, String>, TableCell<SupplierOffer, String>>() {
 			public TableCell call(TableColumn p) {
 				return new EditingCell();
 			}
 		};
 
 		purchaseOffersArticleTableColumn
 				.setCellValueFactory(new PropertyValueFactory<SupplierOffer, String>(
 						"name"));
 		purchaseOffersIdTableColumn
 				.setCellValueFactory(new PropertyValueFactory<SupplierOffer, Integer>(
 						"id"));
 		purchaseOffersQualityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<SupplierOffer, String>(
 						"quality"));
 		purchaseOffersQuantityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<SupplierOffer, String>(
 						"quantity"));
 		purchaseOffersQuantityTableColumn.setCellFactory(cellFactory);
 		purchaseOffersQuantityTableColumn
 				.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<SupplierOffer, String>>() {
 					@Override
 					public void handle(
 							TableColumn.CellEditEvent<SupplierOffer, String> t) {
 								((SupplierOffer) t.getTableView().getItems().get(t.getTablePosition().getRow())).setQuantity(t.getNewValue());
 								model.getMessCreator().addAccepted(
 									purchaseOffersTableView.getSelectionModel().getSelectedItem().getName(),
									Integer.parseInt(t.getNewValue()),
									Integer.parseInt(purchaseOffersTableView.getSelectionModel().getSelectedItem().getQuality())						
 								);
 								System.out.println(purchaseOffersTableView.getSelectionModel().getSelectedItem().getName()+""+
 									Integer.parseInt(t.getNewValue())+""+
 									Integer.parseInt(purchaseOffersTableView.getSelectionModel().getSelectedItem().getQuality()));
 							}
 						});
 		purchaseOffersPriceTableColumn
 				.setCellValueFactory(new PropertyValueFactory<SupplierOffer, String>(
 						"price"));
 
 		purchaseOffersTableView.setItems(model.getPurchaseOffersTableData());
 
 		/**
 		 * purchaseOffersTable: Misc
 		 */
 
 		purchaseOffersTableView
 				.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
 		purchaseOffersTableView.getSelectionModel().setSelectionMode(
 				SelectionMode.SINGLE);
 
 		/**
 		 * ActionListener
 		 */
 
 		newPurchaseRequestButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent actionEvent) {
 				// Felder resetten
 				newPurchaseRequestTitledPane.setDisable(false);
 				newPurchaseRequestSaveButton.setDisable(false);
 				newPurchaseRequestArticleNameChoiceBox.getSelectionModel()
 						.clearSelection();
 				newPurchaseRequestArticleQualitySlider.adjustValue(1.0);
 				
 			}
 		});
 
 		newPurchaseRequestSaveButton
 				.setOnAction(new EventHandler<ActionEvent>() {
 					@Override
 					public void handle(ActionEvent actionEvent) {
 
 						if (!newPurchaseRequestArticleNameChoiceBox.getValue()
 								.equals("")) {
 
 							model.getPurchaseRequestTableData().add(
 									new Request(
 											newPurchaseRequestArticleNameChoiceBox
 													.getValue().toString(),
 											newPurchaseRequestArticleQualityTextField
 													.getText()));
 
 							model.getMessCreator()
 									.addRequest(
 											newPurchaseRequestArticleNameChoiceBox
 													.getValue().toString(),
 											Integer.parseInt(newPurchaseRequestArticleQualityTextField
 													.getText()));
 
 						}
 
 						newPurchaseRequestArticleNameChoiceBox
 								.getSelectionModel().clearSelection();
 						newPurchaseRequestArticleQualitySlider.adjustValue(1.0);
 						newPurchaseRequestTitledPane.setDisable(true);
 						newPurchaseRequestSaveButton.setDisable(true);
 
 					}
 				});
 
 	}
 
 	private void getResourcesInStorage() {
 
 		resourcesInStorage.clear();
 		waferInStorage.clear();
 		casesInStorage.clear();
 
 		// for (StorageElementToClient elem :
 		// model.getIn().storage.storageElements) {
 		//
 		// System.out.println(elem.type);
 		//
 		// if (elem.type.equals("Wafer")) {
 		// waferInStorage.add(elem);
 		// } else if (elem.type.equals("Gehuse")) {
 		// casesInStorage.add(elem);
 		// } else {
 		// resourcesInStorage.add(elem);
 		// }
 		//
 		// }
 
 		for (StoragePosition elem : model.getStoragePositionsTableData()) {
 
 			// System.out.println(elem.type);
 
 			if (elem.getRessource().equals("Wafer")) {
 				waferInStorage.add(elem);
 			} else if (elem.getRessource().equals("Gehuse")) {
 				casesInStorage.add(elem);
 			} else {
 				resourcesInStorage.add(elem);
 			}
 
 		}
 
 	}
 
 	private boolean calcMaximumProduction() {
 
 		int qWaferInStorage = deformatQuantity(newProductionOrderWaferChoiceBox
 				.getValue().getQuantity());
 		int qCasesInStorage = deformatQuantity(newProductionOrderCaseChoiceBox
 				.getValue().getQuantity());
 		int qCasesNeededforMaxPanels = qWaferInStorage / 54;
 		int remainingMachineCapacity = model.getIn().reporting.machinery.maxCapacity
 				- Integer.parseInt(machineryPlannedCapacityTextField.getText());
 
 		// System.out.println(qCasesNeededforMaxPanels);
 
 		if (qCasesNeededforMaxPanels > qCasesInStorage
 				&& qCasesInStorage <= remainingMachineCapacity) {
 			newProductionOrderOutputQuantitySlider.setMax(qCasesInStorage);
 			newProductionOrderOutputQuantitySlider.setValue(qCasesInStorage);
 		} else if (qCasesNeededforMaxPanels <= qCasesInStorage
 				&& qCasesNeededforMaxPanels <= remainingMachineCapacity) {
 			newProductionOrderOutputQuantitySlider
 					.setMax(qCasesNeededforMaxPanels);
 			newProductionOrderOutputQuantitySlider
 					.setValue(qCasesNeededforMaxPanels);
 		} else if (qCasesInStorage > remainingMachineCapacity
 				|| qCasesNeededforMaxPanels > remainingMachineCapacity) {
 			System.out.println(remainingMachineCapacity);
 			newProductionOrderOutputQuantitySlider
 					.setMax(remainingMachineCapacity);
 			newProductionOrderOutputQuantitySlider
 					.setValue(remainingMachineCapacity);
 		} else if (qCasesInStorage > model.getIn().reporting.machinery.maxCapacity
 				|| qCasesNeededforMaxPanels > model.getIn().reporting.machinery.maxCapacity) {
 			return false;
 		}
 
 		return true;
 
 	}
 
 	private boolean calcAndSetMachinery(int productionOrder) {
 
 		int maxCapacity = model.getIn().reporting.machinery.maxCapacity;
 		int cumulativeWorkloadBefore = 0;
 		int cumulativeWorkloadAfter = 0;
 		double percentWorkloadAfter;
 
 		final Tooltip tooltip = new Tooltip();
 
 		for (ProductionOrder x : model.getProductionOrdersTableData()) {
 			int targetQuantity = deformatQuantity(x.getTargetQuantity());
 
 			if (x.getQualityPanel().equals("")) {
 				cumulativeWorkloadBefore += targetQuantity;
 			}
 		}
 
 		cumulativeWorkloadAfter = cumulativeWorkloadBefore + productionOrder;
 		percentWorkloadAfter = (double) cumulativeWorkloadAfter
 				/ (double) maxCapacity;
 
 		tooltip.setText((percentWorkloadAfter * 100) + " %" + " - "
 				+ cumulativeWorkloadAfter + " Panels");
 
 		if (cumulativeWorkloadAfter > maxCapacity) {
 			// machineryWorkloadProgressBar.setProgress(percentWorkloadAfter);
 			machineryWorkloadProgressBar.getStyleClass().add("red-bar");
 			machineryWorkloadProgressBar.setTooltip(tooltip);
 			return false;
 		} else if (cumulativeWorkloadAfter <= maxCapacity
 				&& percentWorkloadAfter >= 70.0) {
 			machineryWorkloadProgressBar.setProgress(percentWorkloadAfter);
 			machineryWorkloadProgressBar.getStyleClass().add("yellow-bar");
 			machineryWorkloadProgressBar.setTooltip(tooltip);
 			machineryPlannedCapacityTextField.setText(cumulativeWorkloadAfter
 					+ "");
 		} else if (cumulativeWorkloadAfter <= maxCapacity
 				&& percentWorkloadAfter < 70.0) {
 			machineryWorkloadProgressBar.setProgress(percentWorkloadAfter);
 			machineryWorkloadProgressBar.getStyleClass().add("yellow-bar");
 			machineryWorkloadProgressBar.setTooltip(tooltip);
 			machineryPlannedCapacityTextField.setText(cumulativeWorkloadAfter
 					+ "");
 		}
 
 		return true;
 
 	}
 
 	private void initProduction() {
 
 		/**
 		 * productionOrdersTable: CellFactory
 		 */
 
 		productionOrderIdTableColumn
 				.setCellValueFactory(new PropertyValueFactory<ProductionOrder, Integer>(
 						"id"));
 
 		productionOrderQualityWaferTableColumn
 				.setCellValueFactory(new PropertyValueFactory<ProductionOrder, String>(
 						"qualityWafer"));
 
 		productionOrderQualityCaseTableColumn
 				.setCellValueFactory(new PropertyValueFactory<ProductionOrder, String>(
 						"qualityCase"));
 
 		productionOrderTargetQuantityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<ProductionOrder, String>(
 						"targetQuantity"));
 
 		productionOrderQualityPanelTableColumn
 				.setCellValueFactory(new PropertyValueFactory<ProductionOrder, String>(
 						"qualityPanel"));
 
 		productionOrderActualQuantityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<ProductionOrder, String>(
 						"actualQuantity"));
 
 		productionOrderCostsPerUnitTableColumn
 				.setCellValueFactory(new PropertyValueFactory<ProductionOrder, String>(
 						"costsPerUnit"));
 
 		// productionOrderCostsPerUnitTableColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
 		productionOrdersTableView
 				.setItems(model.getProductionOrdersTableData());
 
 		productionOrdersTableView
 				.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
 		// productionOrderIdTableColumn.setSortType(TableColumn.SortType.DESCENDING);
 		productionOrdersTableView.getSelectionModel().setSelectionMode(
 				SelectionMode.SINGLE);
 
 		/**
 		 * Misc
 		 */
 
 		machineryLevelTextField.setText(model.getIn().reporting.machinery.level
 				+ "");
 		machineryMaximumCapacityTextField
 				.setText(model.getIn().reporting.machinery.maxCapacity + "");
 		machineryPlannedCapacityTextField.setText("0");
 		// boolean isPossibleMachinery;
 		// boolean isPossibleProduction;
 
 		/**
 		 * ActionListener
 		 */
 
 		final ChangeListener<StoragePosition> newProductionOrderWaferChoiceBoxListener = new ChangeListener<StoragePosition>() {
 			public void changed(
 					ObservableValue<? extends StoragePosition> observable,
 					StoragePosition oldValue, StoragePosition newValue) {
 
 				if (newProductionOrderCaseChoiceBox.getValue() != null) {
 					calcMaximumProduction();
 				}
 
 			}
 		};
 
 		final ChangeListener<StoragePosition> newProductionOrderCaseChoiceBoxListener = new ChangeListener<StoragePosition>() {
 			public void changed(
 					ObservableValue<? extends StoragePosition> observable,
 					StoragePosition oldValue, StoragePosition newValue) {
 
 				if (newProductionOrderWaferChoiceBox.getValue() != null) {
 					calcMaximumProduction();
 				}
 
 			}
 		};
 
 		final ChangeListener<Number> newProductionOrderOutputQuantitySliderListener = new ChangeListener<Number>() {
 			public void changed(ObservableValue<? extends Number> observable,
 					Number oldValue, Number newValue) {
 
 				int costsWafer = (deformatCurrency(newProductionOrderWaferChoiceBox
 						.getValue().getCosts())
 						* Integer
 								.parseInt(newProductionOrderOutputQuantityTextField
 										.getText()) * 54) / 100;
 				int costsCases = (deformatCurrency(newProductionOrderCaseChoiceBox
 						.getValue().getCosts()) * Integer
 						.parseInt(newProductionOrderOutputQuantityTextField
 								.getText())) / 100;
 				int costsProductionOrder = costsWafer + costsCases;
 				newProductionOrderCostsTextField.setText(model
 						.getnFormatterCurrency().format(costsProductionOrder));
 
 			}
 		};
 
 		newProductionOrderButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent actionEvent) {
 				getResourcesInStorage();
 				newProductionOrderTitledPane.setDisable(false);
 				newProductionOrderSaveButton.setDisable(false);
 				newProductionOrderWaferChoiceBox.valueProperty()
 						.removeListener(
 								newProductionOrderWaferChoiceBoxListener);
 				newProductionOrderCaseChoiceBox.valueProperty().removeListener(
 						newProductionOrderCaseChoiceBoxListener);
 				newProductionOrderOutputQuantitySlider.valueProperty()
 						.removeListener(
 								newProductionOrderOutputQuantitySliderListener);
 				newProductionOrderWaferChoiceBox.getSelectionModel()
 						.clearSelection();
 				newProductionOrderWaferChoiceBox.getItems().clear();
 				newProductionOrderCaseChoiceBox.getSelectionModel()
 						.clearSelection();
 				newProductionOrderCaseChoiceBox.getItems().clear();
 				newProductionOrderCostsTextField.clear();
 				newProductionOrderOutputQuantitySlider.adjustValue(0.0);
 				newProductionOrderWaferChoiceBox.getItems().setAll(
 						waferInStorage);
 				newProductionOrderCaseChoiceBox.getItems().setAll(
 						casesInStorage);
 				newProductionOrderWaferChoiceBox.valueProperty().addListener(
 						newProductionOrderWaferChoiceBoxListener);
 				newProductionOrderCaseChoiceBox.valueProperty().addListener(
 						newProductionOrderCaseChoiceBoxListener);
 				newProductionOrderOutputQuantitySlider.valueProperty()
 						.addListener(
 								newProductionOrderOutputQuantitySliderListener);
 			}
 		});
 
 		newProductionOrderSaveButton
 				.setOnAction(new EventHandler<ActionEvent>() {
 					@Override
 					public void handle(ActionEvent actionEvent) {
 
 						if (newProductionOrderCaseChoiceBox.getValue() != null
 								&& newProductionOrderWaferChoiceBox.getValue() != null) {
 							boolean isPossibleMachinery = calcAndSetMachinery(Integer
 									.parseInt(newProductionOrderOutputQuantityTextField
 											.getText()));
 
 							if (isPossibleMachinery == true
 									&& !newProductionOrderOutputQuantityTextField
 											.getText().equals("0")) {
 								model.getProductionOrdersTableData().add(
 										new ProductionOrder(
 												newProductionOrderWaferChoiceBox
 														.getValue()
 														.getQuality(),
 												newProductionOrderCaseChoiceBox
 														.getValue()
 														.getQuality(),
 												newProductionOrderOutputQuantityTextField
 														.getText()));
 
 								model.getMessCreator()
 										.addProductionOrder(
 												Integer.parseInt(newProductionOrderWaferChoiceBox
 														.getValue()
 														.getQuality()),
 												Integer.parseInt(newProductionOrderCaseChoiceBox
 														.getValue()
 														.getQuality()),
 												Integer.parseInt(newProductionOrderOutputQuantityTextField
 														.getText()));
 
 							}
 
 						}
 
 						newProductionOrderWaferChoiceBox
 								.valueProperty()
 								.removeListener(
 										newProductionOrderWaferChoiceBoxListener);
 						newProductionOrderCaseChoiceBox
 								.valueProperty()
 								.removeListener(
 										newProductionOrderCaseChoiceBoxListener);
 						newProductionOrderOutputQuantitySlider
 								.valueProperty()
 								.removeListener(
 										newProductionOrderOutputQuantitySliderListener);
 						newProductionOrderWaferChoiceBox.getSelectionModel()
 								.clearSelection();
 						newProductionOrderWaferChoiceBox.getItems().clear();
 						newProductionOrderCaseChoiceBox.getSelectionModel()
 								.clearSelection();
 						newProductionOrderCaseChoiceBox.getItems().clear();
 						newProductionOrderCostsTextField.clear();
 						newProductionOrderOutputQuantitySlider.adjustValue(0.0);
 						newProductionOrderTitledPane.setDisable(true);
 
 					}
 				});
 
 	}
 
 	private void calcStorageCosts() {
 
 		int costsWafer = 0;
 		int costsCases = 0;
 		int costsPanels = 0;
 
 		int storageCostsWafer = model.getIn().storage.storageCostsWafer;
 		int storageCostsCases = model.getIn().storage.storageCostsCase;
 		int storageCostsPanels = model.getIn().storage.storageCostsPanel;
 
 		if (model.getIn().storage.storageElements != null) {
 
 			for (StorageElementToClient elem : model.getIn().storage.storageElements) {
 
 				if (elem.type.equals("Wafer")) {
 					costsWafer += elem.quantity * storageCostsWafer;
 				} else if (elem.type.equals("Gehuse")) {
 					costsCases += elem.quantity * storageCostsCases;
 				} else if (elem.type.equals("Panel")) {
 					costsPanels += elem.quantity * storageCostsPanels;
 				}
 
 			}
 
 		}
 
 		storageCostsWaferTextField.setText(model.getnFormatterCurrency()
 				.format(costsWafer / 100.0));
 		storageCostsCasesTextField.setText(model.getnFormatterCurrency()
 				.format(costsCases / 100.0));
 		storageCostsPanelsTextField.setText(model.getnFormatterCurrency()
 				.format(costsPanels / 100.0));
 
 	}
 
 	private void initStorage() {
 
 		/**
 		 * storagePositionsTable: CellFactory
 		 */
 
 		storagePositionIdTableColumn
 				.setCellValueFactory(new PropertyValueFactory<StoragePosition, Integer>(
 						"id"));
 
 		storagePositionRessourceTableColumn
 				.setCellValueFactory(new PropertyValueFactory<StoragePosition, String>(
 						"ressource"));
 
 		storagePositionQualityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<StoragePosition, String>(
 						"quality"));
 
 		storagePositionQuantityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<StoragePosition, String>(
 						"quantity"));
 
 		storagePositionCostsTableColumn
 				.setCellValueFactory(new PropertyValueFactory<StoragePosition, String>(
 						"costs"));
 
 		// storagePositionCostsTableColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
 		storagePositionsTableView
 				.setItems(model.getStoragePositionsTableData());
 
 		storagePositionsTableView
 				.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
 		// storagePositionIdTableColumn.setSortType(TableColumn.SortType.DESCENDING);
 		storagePositionsTableView.getSelectionModel().setSelectionMode(
 				SelectionMode.SINGLE);
 
 		/**
 		 * Misc
 		 */
 
 		calcStorageCosts();
 
 	}
 
 	private void initSales() {
 
 		/**
 		 * salesTableView: CellFactory
 		 */
 
 		salesIdTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Offer, Integer>(
 						"id"));
 		salesProductTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Offer, String>(
 						"product"));
 		salesQualityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Offer, String>(
 						"quality"));
 		salesPriceTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Offer, String>(
 						"price"));
 		salesQuantityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Offer, String>(
 						"quantity"));
 		salesSoldQuantityTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Offer, String>(
 						"soldQuantity"));
 		salesProfitTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Offer, String>(
 						"profit"));
 		salesCostsTableColumn
 				.setCellValueFactory(new PropertyValueFactory<Offer, String>(
 						"costs"));
 
 		salesTableView.setItems(model.getOfferTableData());
 
 		salesTableView
 				.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
 		// salesIdTableColumn.setSortType(TableColumn.SortType.DESCENDING);
 		salesTableView.getSelectionModel().setSelectionMode(
 				SelectionMode.SINGLE);
 
 		/**
 		 * ActionListener
 		 */
 
 		final ChangeListener<StoragePosition> newSaleOfferArticleChoiceBoxListener = new ChangeListener<StoragePosition>() {
 			public void changed(
 					ObservableValue<? extends StoragePosition> observable,
 					StoragePosition oldValue, StoragePosition newValue) {
 				newSaleOfferArticleQuantitySlider.setMax(Integer
 						.parseInt(newSaleOfferArticleChoiceBox.getValue()
 								.getQuantity()));
 				newSaleOfferArticleQuantitySlider.setValue(Integer
 						.parseInt(newSaleOfferArticleChoiceBox.getValue()
 								.getQuantity()));
 			}
 		};
 
 		final ChangeListener<String> newSaleOfferArticlePriceTextFieldListener = new ChangeListener<String>() {
 			public void changed(ObservableValue<? extends String> observable,
 					String oldValue, String newValue) {
 
 				int distributionCosts = model.getIn().distribution.costsPerOffer / 100;
 				// System.out.println(newSaleOfferArticleChoiceBox.getValue().getCosts());
 				int cost = deformatCurrency(newSaleOfferArticleChoiceBox
 						.getValue().getCosts()) / 100;
 				// System.out.println(cost);
 				int quantity = Integer
 						.parseInt(newSaleOfferArticleQuantityTextField
 								.getText());
 				int price = Integer.parseInt(newSaleOfferArticlePriceTextField
 						.getText());
 				int cumulCosts = (quantity * cost) + distributionCosts;
 				int maxProfit = (quantity * price) - cumulCosts;
 
 				newSaleOfferDistributionCostsTextField.setText(model
 						.getnFormatterCurrency().format(distributionCosts));
 				newSaleOfferCostsTextField.setText(model
 						.getnFormatterCurrency().format(cost));
 				newSaleOfferMaximumProfitTextField.setText(model
 						.getnFormatterCurrency().format(maxProfit));
 
 			}
 		};
 
 		newSaleOfferButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent actionEvent) {
 				getResourcesInStorage();
 				newSaleOfferArticlePriceTextField.textProperty()
 						.removeListener(
 								newSaleOfferArticlePriceTextFieldListener);
 				newSaleOfferArticleChoiceBox.valueProperty().removeListener(
 						newSaleOfferArticleChoiceBoxListener);
 				newSaleOfferArticleChoiceBox.getSelectionModel()
 						.clearSelection();
 				newSaleOfferArticlePriceTextField.clear();
 				newSaleOfferArticleQuantitySlider.adjustValue(0.0);
 				newSaleOfferArticleQuantityTextField.clear();
 				newSaleOfferCostsTextField.clear();
 				newSaleOfferDistributionCostsTextField.clear();
 				newSaleOfferCostsTextField.clear();
 				newSaleOfferMaximumProfitTextField.clear();
 				newSaleOfferArticleChoiceBox.getItems().setAll(
 						resourcesInStorage);
 				newSaleOfferArticleChoiceBox.valueProperty().addListener(
 						newSaleOfferArticleChoiceBoxListener);
 				newSaleOfferArticlePriceTextField.textProperty().addListener(
 						newSaleOfferArticlePriceTextFieldListener);
 				newSaleOfferTitledPane.setDisable(false);
 				newSaleOfferSaveButton.setDisable(false);
 			}
 		});
 
 		newSaleOfferSaveButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent actionEvent) {
 
 				model.getOfferTableData()
 						.add(new Offer(
 								newSaleOfferArticleChoiceBox.getValue()
 										.getQuality(),
 								newSaleOfferArticleQuantityTextField.getText(),
 								Integer.parseInt(newSaleOfferArticlePriceTextField
 										.getText())
 										* 100 + ""));
 
 				model.getMessCreator().addOffer(
 						Integer.parseInt(newSaleOfferArticleChoiceBox
 								.getValue().getQuality()),
 						Integer.parseInt(newSaleOfferArticleQuantityTextField
 								.getText()),
 						Integer.parseInt(newSaleOfferArticlePriceTextField
 								.getText()) * 100);
 
 				newSaleOfferArticlePriceTextField.textProperty()
 						.removeListener(
 								newSaleOfferArticlePriceTextFieldListener);
 				newSaleOfferArticleChoiceBox.valueProperty().removeListener(
 						newSaleOfferArticleChoiceBoxListener);
 				newSaleOfferArticleChoiceBox.getSelectionModel()
 						.clearSelection();
 				newSaleOfferArticlePriceTextField.clear();
 				newSaleOfferArticleQuantitySlider.adjustValue(0.0);
 				newSaleOfferArticleQuantityTextField.clear();
 				newSaleOfferCostsTextField.clear();
 				newSaleOfferDistributionCostsTextField.clear();
 				newSaleOfferCostsTextField.clear();
 				newSaleOfferMaximumProfitTextField.clear();
 				newSaleOfferTitledPane.setDisable(true);
 
 			}
 		});
 
 	}
 
 	private void initReporting() {
 
 		reportingSalesFixCostsTextField.setText(model.getnFormatterCurrency()
 				.format(model.getIn().reporting.fixCosts.get(0).costs / 100));
 		reportingHRFixCostsTextField.setText(model.getnFormatterCurrency()
 				.format(model.getIn().reporting.fixCosts.get(1).costs / 100));
 		reportingMarketingFixCostsTextField.setText(model
 				.getnFormatterCurrency().format(
 						model.getIn().reporting.fixCosts.get(2).costs / 100));
 		reportingProductionFixCostsTextField.setText(model
 				.getnFormatterCurrency().format(
 						model.getIn().reporting.fixCosts.get(3).costs / 100));
 		reportingPurchaseFixCostsTextField.setText(model
 				.getnFormatterCurrency().format(
 						model.getIn().reporting.fixCosts.get(4).costs / 100));
 		reportingStorageFixCostsTextField.setText(model.getnFormatterCurrency()
 				.format(model.getIn().reporting.fixCosts.get(5).costs / 100));
 		reportingMachineryLevelTextField
 				.setText(model.getIn().reporting.machinery.level + "");
 		reportingMachineryMaxCapacityTextField
 				.setText(model.getIn().reporting.machinery.maxCapacity + "");
 		reportingMachineryAvgWorkloadProgressBar
 				.setProgress(model.getIn().reporting.machinery.averageUsage);
 		// System.out.println(model.getIn().reporting.machinery.averageUsage);
 		reportingMachineryLastRoundWorkloadProgressBar.setProgress(model
 				.getIn().reporting.machinery.usageLastRound);
 		// System.out.println(model.getIn().reporting.machinery.usageLastRound);
 
 	}
 
 	private void initMarketing() {
 
 		/**
 		 * Misc
 		 */
 
 		// System.out.println(model.getIn().humanResources.averageWage);
 
 		if (model.getIn().marketing.isBooked == true) {
 			marketResearchAverageWagesLastRoundTextField
 					.setText(model.getIn().humanResources.averageWage + "");
 			marketResearchPeakAMarketTextField
 					.setText(model.getIn().marketing.peakAMarket + "");
 			marketResearchPeakCMarketTextField
 					.setText(model.getIn().marketing.peakCMarket + "");
 		} else {
 			marketResearchAverageWagesLastRoundTextField.setText("n.A.");
 			marketResearchPeakAMarketTextField.setText("n.A.");
 			marketResearchPeakCMarketTextField.setText("n.A.");
 		}
 
 	}
 
 	private void initHumanResources() {
 
 		/**
 		 * Misc
 		 */
 
 		// System.out.println(model.getIn().humanResources.myWage);
 		// System.out.println(model.getIn().humanResources.averageWage);
 		// System.out.println(model.getIn().humanResources.wageCosts);
 
 		hrWagesPerHourTextField.setText(model.getnFormatterCurrency().format(
 				model.getIn().humanResources.myWage/100));
 		hrAverageWagesTextField.setText(model.getnFormatterCurrency().format(
 				model.getIn().humanResources.averageWage/100));
 		hrCountEmployeesTextField.setText(model.getnFormatter().format(
 				model.getIn().humanResources.countEmployees));
 		hrWageCostsTextField.setText(model.getnFormatterCurrency().format(
 				model.getIn().humanResources.wageCosts/100));
 
 		benefitsChoiceBox.setItems(model.getBenefitBoxData());
 		bookedBeneftisTableView.setItems(model.getBenefitBookingTableData());
 
 	}
 
 	/**
 	 * Berechnet den Runden-Fortschrittsbalken neu und setzt den neuen Wert.
 	 * Ausserdem wird das Runden-Label aktualisiert.
 	 * 
 	 * @param round
 	 */
 
 	public void processRoundProgressBar(int round) {
 
 		Integer roundInt = new Integer(round);
 		Integer maxRoundsInt = new Integer(model.getMaxRounds());
 		double newProgress = (roundInt.doubleValue() / maxRoundsInt
 				.doubleValue());
 		roundProgressBar.setProgress(newProgress);
 		roundLabel.setText("Runde: " + roundInt.toString());
 
 	}
 
 	/**
 	 * Diese Methode befuellt Kategorie-Orientierte Daten in ein BarChart
 	 * 
 	 * @param data
 	 *            Eine Liste der Daten, die zu jeder Kategorie gehoeren.
 	 * @param categories
 	 *            Bezeichner der Kategorie (gleich lang wie data!)
 	 * @param xAxisBar
 	 *            Die XAchse des Diagramms
 	 * @param yAxisBar
 	 *            Die YAchse des Diagramms
 	 * @param barChart
 	 *            Das Diagramm in dem die Daten erscheinen sollen
 	 */
 	public static <T> void buildXYChart(ArrayList<HashMap<String, T>> data,
 			String[] categories, CategoryAxis xAxisBar, NumberAxis yAxisBar,
 			XYChart<String, T> barChart) {
 
 		HashMap<String, XYChart.Series<String, T>> seriesMaps = new HashMap<String, XYChart.Series<String, T>>();
 
 		for (int i = 0; i < data.size(); i++) {
 			HashMap<String, T> roundMap = data.get(i);
 			for (Map.Entry<String, T> entry : roundMap.entrySet()) {
 				XYChart.Series<String, T> roundSeries = seriesMaps.get(entry
 						.getKey());
 				if (roundSeries == null) {
 					XYChart.Series<String, T> newSerie = new XYChart.Series<>();
 					newSerie.setName(entry.getKey());
 					newSerie.getData().add(
 							new XYChart.Data<String, T>(categories[i], entry
 									.getValue()));
 					seriesMaps.put(entry.getKey(), newSerie);
 					barChart.getData().add(newSerie);
 				} else {
 					roundSeries.getData().add(
 							new XYChart.Data<String, T>(categories[i], entry
 									.getValue()));
 				}
 			}
 		}
 
 		xAxisBar.setCategories(FXCollections.observableArrayList(categories));
 	}
 
 	public static void buildPieChart(HashMap<String, Double> data,
 			PieChart chart) {
 		for (Map.Entry<String, Double> entry : data.entrySet()) {
 			chart.getData().add(
 					new PieChart.Data(entry.getKey(), entry.getValue()));
 		}
 	}
 
 }
