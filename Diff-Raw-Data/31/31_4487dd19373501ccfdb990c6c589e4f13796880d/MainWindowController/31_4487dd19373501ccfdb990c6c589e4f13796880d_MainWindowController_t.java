 package innuinfocomm;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.MenuBar;
 import javafx.scene.control.Tab;
 import javafx.scene.control.TabPane;
 import javafx.scene.control.TitledPane;
 import javafx.scene.layout.AnchorPane;
 
 
 public class MainWindowController {
 
    @FXML
     private ResourceBundle resources;
 
     @FXML
     private URL location;
 
     @FXML
     private Button AddItemBtn;
 
     @FXML
     private TitledPane VoucherMgmt;
 
     @FXML
     private Button addLedgerButton;
 
     @FXML
     private Button addNewLedgerType;
 
     @FXML
     private Button createNewPayableBtn;
 
     @FXML
     private Button createNewPaymentVoucherResult;
 
     @FXML
     private Button createNewPurchaseBtn;
 
     @FXML
     private Button createNewRecieptVoucherBtn;
 
     @FXML
     private Button createNewRecievableBtn;
 
     @FXML
     private Button createNewSaleBillBtn;
 
     @FXML
     private Button importInventoryBtn;
 
     @FXML
     private TitledPane ledgerMgmt;
 
     @FXML
     private AnchorPane leftMenuHolder;
 
     @FXML
     private AnchorPane mainContentHolder;
 
     @FXML
     private TabPane mainContentTabPane;
 
     @FXML
     private AnchorPane rootPane;
 
     @FXML
     private MenuBar topMenuBar;
 
     @FXML
     private Button viewAllItemsBtn;
 
     @FXML
     private Button viewAllLedgerButton;
 
     @FXML
     private TitledPane x1;
 
     @FXML
     private TitledPane x2;
 
 
     @FXML
     void handleAddLedgerButton(ActionEvent event) throws IOException {
         
         Parent rootNew = FXMLLoader.load(getClass().getResource("AddNewCustomer.fxml"));
         Tab tab = new Tab("Add New Customer");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);
        mainContentTabPane.getSelectionModel().select(tab);
         
         
     }
 
     @FXML
     void handleAddNewLedgerType(ActionEvent event) {
     }
 
     @FXML
     void handleViewAllLedgerButton(ActionEvent event) throws IOException {
         Parent rootNew = FXMLLoader.load(getClass().getResource("ViewAllLedger.fxml"));
         Tab tab = new Tab("View All Ledger");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);
        mainContentTabPane.getSelectionModel().select(tab);
         
     }
     
     @FXML 
     void handleAddItemBtn() throws IOException{
         Parent rootNew = FXMLLoader.load(getClass().getResource("AddNewPharmaItem.fxml"));
         Tab tab = new Tab("Add an Item");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);
        mainContentTabPane.getSelectionModel().select(tab);
     
     }
     
     @FXML
     void handleViewAllItemsBtn() throws IOException{
     
         Parent rootNew = FXMLLoader.load(getClass().getResource("ViewAllItems.fxml"));
         Tab tab = new Tab("View All Items");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);
        mainContentTabPane.getSelectionModel().select(tab);
     }
     
     @FXML
     void handleCreateNewSaleBillBtn() throws IOException{
         Parent rootNew = FXMLLoader.load(getClass().getResource("SalebillPharma.fxml"));
         Tab tab = new Tab("Create New Sale Bill");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);
        mainContentTabPane.getSelectionModel().select(tab);
     
     }
     
     @FXML 
     void handleCreateNewPayableBtn()
     {
         
     
     }
     
     @FXML 
     void handleCreateNewRecievableBtn(){
     
     }
     
     @FXML
     void handleCreateNewPurchaseBill() throws IOException{
         Parent rootNew = FXMLLoader.load(getClass().getResource("PurchaseVoucher.fxml"));
         Tab tab = new Tab("Create New Purchase Bill");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);                
        mainContentTabPane.getSelectionModel().select(tab);
     }
     
     @FXML 
     void handleCreateNewRecieptVoucherBtn() throws IOException{
         Parent rootNew = FXMLLoader.load(getClass().getResource("RecieptVoucher.fxml"));
         Tab tab = new Tab("Create New Reciept Voucher");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);                
        mainContentTabPane.getSelectionModel().select(tab);
     }
     
     @FXML
     void handleCreateNewPaymentVoucher() throws IOException{
         Parent rootNew = FXMLLoader.load(getClass().getResource("PaymentVoucher.fxml"));
         Tab tab = new Tab("Create New Purchase Voucher");
         tab.setContent(rootNew);
        mainContentTabPane.getTabs().add(tab);                  
        mainContentTabPane.getSelectionModel().select(tab);
     }
     
     @FXML
     void handleCreateNewContraVoucher() throws IOException{
         Parent rootNew = FXMLLoader.load(getClass().getResource("ContraVoucher.fxml"));
         Tab tab = new Tab("Create New Contra Voucher");
         tab.setContent(rootNew);
        mainContentTabPane.getTabs().add(tab);                  
        mainContentTabPane.getSelectionModel().select(tab);
     
     }
     
     @FXML
     void handleCreateNewJournalVoucher() throws IOException{
         Parent rootNew = FXMLLoader.load(getClass().getResource("Journalvoucher.fxml"));
         Tab tab = new Tab("Create New Journal Voucher");
         tab.setContent(rootNew);
        mainContentTabPane.getTabs().add(tab);                  
        mainContentTabPane.getSelectionModel().select(tab);
     
     
     }
 
     @FXML
     void initialize() {
         assert VoucherMgmt != null : "fx:id=\"VoucherMgmt\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert addLedgerButton != null : "fx:id=\"addLedgerButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert addNewLedgerType != null : "fx:id=\"addNewLedgerType\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert ledgerMgmt != null : "fx:id=\"ledgerMgmt\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert leftMenuHolder != null : "fx:id=\"leftMenuHolder\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert mainContentHolder != null : "fx:id=\"mainContentHolder\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert mainContentTabPane != null : "fx:id=\"mainContentTabPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert topMenuBar != null : "fx:id=\"topMenuBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert viewAllLedgerButton != null : "fx:id=\"viewAllLedgerButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert AddItemBtn != null : "fx:id=\"viewAllLedgerButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert x1 != null : "fx:id=\"x1\" was not injected: check your FXML file 'MainWindow.fxml'.";
         assert createNewSaleBillBtn != null : "fx:id=\"createNewSaleBillBtn\" was not injected: check your FXML file 'MainWindow.fxml'.";
 
 
     }
     
     @FXML
     public void handleImportInventoryBtn() throws IOException{
     
      Parent rootNew = FXMLLoader.load(getClass().getResource("ImportInventory.fxml"));
         Tab tab = new Tab("Import Inventory");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);
        mainContentTabPane.getSelectionModel().select(tab);
     }
     
     @FXML 
     public void handleViewSaleBillBtn() throws IOException{
             
      Parent rootNew = FXMLLoader.load(getClass().getResource("ViewSaleBillSearch.fxml"));
         Tab tab = new Tab("View Sale Bills");
         tab.setContent(rootNew);
         mainContentTabPane.getTabs().add(tab);
        mainContentTabPane.getSelectionModel().select(tab);
     }
 
 }
