 package innuinfocomm;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.ResourceBundle;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.TableColumn;
 import javafx.event.EventHandler;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.cell.PropertyValueFactory;
 
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.control.TableCell;
 import javafx.scene.control.ContentDisplay;
 import javafx.scene.control.TableColumn.CellEditEvent;
 import javafx.util.Callback;
 import org.javafxdata.control.*;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import pojos.ItemGroup;
 import pojos.Items;
 import pojos.SaleBill;
 import pojos.SalebillItem;
 import utils.EntityManagerHelper;
 
 
 
 public class SaleBillController implements Initializable {
 
     @FXML
     private ResourceBundle resources;
 
     @FXML
     private URL location;
 
     SaleBill s = new SaleBill();
     @FXML
     private TextField billNoTextbox;
 
     @FXML
     private RadioButton challanRadioBtn;
 
     @FXML
     private RadioButton challanRateRadioBtn;
 
     @FXML
     private TextField companyTextBox;
 
     @FXML
     private ComboBox<String> customerComboBox;
 
     @FXML
     private TextField dateTextBox;
 
     @FXML
     private TextField discountTextBox;
 
     @FXML
     private TextField frieghtTextBox;
 
     @FXML
     private RadioButton invoiceRadioBtn;
 
     @FXML
     private TableColumn<SalebillItem, String> itemNameTableCol;
 
     @FXML
     private Button printBtn;
 
 
 
     @FXML
     private TableColumn<SalebillItem, Double> quantityTableCol;
 
     @FXML
     private TableColumn<SalebillItem, Double> rateTableCol;
 
     @FXML
     private TableColumn<SalebillItem, String> remarkTableCol;
     
     @FXML
     private TableView<SalebillItem> SaleItemTableView;
 
     @FXML
     private TextField remarksTextBox;
 
     @FXML
     private Button saveBtn;
 
     @FXML
     private ComboBox<String> siteComboBox;
 
     @FXML
     private TableColumn<SalebillItem, Double> totalTableCol;
 
     @FXML
     private TextField totalTextBox;
 
     @FXML
     private TableColumn<SalebillItem, String> unitTableCol;
 
     @FXML
     private TableColumn<SalebillItem,Double> vatPercTableCol;
 
     @FXML
     private TableColumn<SalebillItem, Double> vatRsTableCol;
 
     @FXML
     private TextField vatTextBox;
     
     @FXML
     private ComboBox<Items> itemsComboBox;
     
     @FXML 
     private ComboBox<ItemGroup> groupComboBox;
     
     @FXML
     private ComboBox<ItemGroup> subGroupComboBox;
     
 
     @FXML
     void handlePrintBtn(ActionEvent event) {
     }
 
     @FXML
     void handleSaveBtn(ActionEvent event) {
     }
     
     private  ObservableList<SalebillItem> data = FXCollections.observableArrayList();
     private ArrayList<SalebillItem> saleItemList = new ArrayList<SalebillItem>();
 
 
     @FXML
     public void initialize(URL location, ResourceBundle r) {
         assert SaleItemTableView != null : "fx:id=\"SaleItemTableView\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert billNoTextbox != null : "fx:id=\"billNoTextbox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert challanRadioBtn != null : "fx:id=\"challanRadioBtn\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert challanRateRadioBtn != null : "fx:id=\"challanRateRadioBtn\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert companyTextBox != null : "fx:id=\"companyTextBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert customerComboBox != null : "fx:id=\"customerComboBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert dateTextBox != null : "fx:id=\"dateTextBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert discountTextBox != null : "fx:id=\"discountTextBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert frieghtTextBox != null : "fx:id=\"frieghtTextBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert groupComboBox != null : "fx:id=\"groupComboBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert invoiceRadioBtn != null : "fx:id=\"invoiceRadioBtn\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert itemNameTableCol != null : "fx:id=\"itemNameTableCol\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert itemsComboBox != null : "fx:id=\"itemsCombobox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert printBtn != null : "fx:id=\"printBtn\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert quantityTableCol != null : "fx:id=\"quantityTableCol\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert rateTableCol != null : "fx:id=\"rateTableCol\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert remarkTableCol != null : "fx:id=\"remarkTableCol\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert remarksTextBox != null : "fx:id=\"remarksTextBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert saveBtn != null : "fx:id=\"saveBtn\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert siteComboBox != null : "fx:id=\"siteComboBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert subGroupComboBox != null : "fx:id=\"subGroupComboBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert totalTableCol != null : "fx:id=\"totalTableCol\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert totalTextBox != null : "fx:id=\"totalTextBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert unitTableCol != null : "fx:id=\"unitTableCol\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert vatPercTableCol != null : "fx:id=\"vatPercTableCol\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert vatRsTableCol != null : "fx:id=\"vatRsTableCol\" was not injected: check your FXML file 'SaleBill.fxml'.";
         assert vatTextBox != null : "fx:id=\"vatTextBox\" was not injected: check your FXML file 'SaleBill.fxml'.";
         
         itemNameTableCol.setCellValueFactory(new PropertyValueFactory<SalebillItem,String>("itemName"));
         quantityTableCol.setCellValueFactory(new PropertyValueFactory<SalebillItem,Double>("itemQnty"));
         rateTableCol.setCellValueFactory(new PropertyValueFactory<SalebillItem,Double>("itemRate"));
         unitTableCol.setCellValueFactory(new PropertyValueFactory<SalebillItem,String>("itemUnitName"));
         remarkTableCol.setCellValueFactory(new PropertyValueFactory<SalebillItem,String>("remark"));
         totalTableCol.setCellValueFactory(new PropertyValueFactory<SalebillItem,Double>("total"));
         vatPercTableCol.setCellValueFactory(new PropertyValueFactory<SalebillItem,Double>("itemVatPerc"));
         vatRsTableCol.setCellValueFactory(new PropertyValueFactory<SalebillItem,Double>("itemVatRs"));
         
         SaleItemTableView.setEditable(true);
         
         quantityTableCol.setEditable(true);
         //quantityTableCol.setCellFactory(TextFieldTableCell.);
         Callback<TableColumn<SalebillItem,Double>,TableCell<SalebillItem,Double>> cellFactory = 
                 new Callback<TableColumn<SalebillItem,Double>,TableCell<SalebillItem,Double>>(){
                      public TableCell<SalebillItem,Double> call(TableColumn<SalebillItem,Double> p) {
                       return new EditingQntyCell();
                   }
                 };
         quantityTableCol.setCellFactory(cellFactory);
         
         quantityTableCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<SalebillItem, Double>>(){
 
                     @Override
                     public void handle(TableColumn.CellEditEvent<SalebillItem, Double> t) {
                         System.out.println("on edit commit called " + t.getNewValue());
                         SalebillItem edit = (SalebillItem) t.getTableView().getItems().get(t.getTablePosition().getRow());
                         edit.setItemQnty(t.getNewValue());
                         
                         setTotalTextBox();
                         t.getTableView().getColumns().get(0).setVisible(false);
                         t.getTableView().getColumns().get(0).setVisible(true);
                         //t.getTableView().getItems()
                         //t.getTableView().getSelectionModel().clearSelection();
                         //t.getTableView().getSelectionModel().clearSelection();
                     }               
         });
         
 
         initializeSaleBill();
         //itemsComboBox = new ComboBox<>();
         groupComboBox.getItems().clear();
         subGroupComboBox.getItems().clear();        
         itemsComboBox.getItems().clear();
                 
         System.out.println("\n in initialize func");
         fillGroupComboBox();
         
         groupComboBox.valueProperty().addListener(new ChangeListener<ItemGroup>(){
 
             @Override
             public void changed(ObservableValue<? extends ItemGroup> ov, ItemGroup t, ItemGroup t1) {
                 //now set the subgroup items here 
                    System.out.println("group combox box value hanged");
                    System.out.println("old value = "+t.getItemGroupName());
                    System.out.println("new value = "+t1.getItemGroupName());
                    fillSubGroupComboBox();
            
             }
         
         
         });
         
         subGroupComboBox.valueProperty().addListener(new ChangeListener<ItemGroup>(){
 
             @Override
             public void changed(ObservableValue<? extends ItemGroup> ov, ItemGroup t, ItemGroup t1) {
                 if((t1!=null))
                         {
                             System.out.println("subgroup value changed to "+t1.getItemGroupName());
                             fillItemsComboBox();
                         }
             }
         
         
         });
         
         itemsComboBox.valueProperty().addListener(new ChangeListener<Items>(){
 
             @Override
             public void changed(ObservableValue<? extends Items> ov, Items t, Items t1) {
                 if((t!=null) &&(t1!=null)){
                     SalebillItem item = new SalebillItem();
                     item.setItemId(t1.getItemId());
                     item.setItemName(t1.getItemName());
                     item.setItemUnitName(t1.getItemFirstUnit().getUnitName());
                     item.setItemVatPerc(t1.getItemVatPerc());
                     item.setItemRate(t1.getItemRate());
                    item.setItemQnty(1.0);
                    
                     item.setRemark("");
                    //item.setItemVatRs(0.0);
                     item.setSaleBillNo(s);
                    System.out.println(item.getTotal());
                    //item.setTotal(0.0);
                     //saleItemList.add(item);
                     data.add(item);
                     //data = FXCollections.observableArrayList(saleItemList);
                     SaleItemTableView.setItems(data);
                     SaleItemTableView.getSelectionModel().clearSelection();
                 }
             }
         
             
         });
         
         
    }
     
     
     
     
     
     private void initializeTableColumns(){
     
         itemNameTableCol.setEditable(true);
         
     
     }
     
     private void fillGroupComboBox(){
     
         System.out.println("fillGroupComboBox");
         //get all the item group with parent id as 0;
         EntityManager em = EntityManagerHelper.getInstance().getEm();
         Query q  = em.createNamedQuery("ItemGroup.findByItemGroupParent");
         q.setParameter("itemGroupParent", 0);
         ArrayList<ItemGroup> groups = new ArrayList<ItemGroup>(q.getResultList());        
         groupComboBox.getItems().clear();
         groupComboBox.getItems().addAll(groups);
         groupComboBox.getSelectionModel().clearSelection();
         groupComboBox.getSelectionModel().selectFirst();
         
     
     }
     
     private void fillSubGroupComboBox(){
         //get the selected value in the group Combo Box
         System.out.println("fill subgroup combo box");
         ItemGroup group = groupComboBox.getSelectionModel().getSelectedItem();
         
         EntityManager em = EntityManagerHelper.getInstance().getEm();
         Query q  = em.createNamedQuery("ItemGroup.findByItemGroupParent");
         q.setParameter("itemGroupParent", group.getItemGroupId());
         em.getTransaction().begin();
         ArrayList<ItemGroup> subGroup = new ArrayList<ItemGroup>(q.getResultList());
         em.getTransaction().commit();
         
         if(subGroup.size()<1){  //this means that no subgroup exist
             System.out.println("inside the nosubgroup condition");
             subGroupComboBox.getItems().clear();
             fillItemsComboBox();
             return ;
         }
         System.out.println("group has subgroups" + subGroup.size());
         subGroupComboBox.getItems().clear();
         subGroupComboBox.getItems().addAll(subGroup);
         subGroupComboBox.getSelectionModel().clearSelection();
         subGroupComboBox.getSelectionModel().selectFirst();
     
     }
     
     private void fillItemsComboBox(){
         System.out.println("Inside fill items");
         //itemsComboBox.getItems().removeAll(itemsComboBox.getItems());
        
         //check if the number of items in subgorup. 
         if(subGroupComboBox.getItems().size() < 1){
             System.out.println("No sub groups exist");
             //the get the items according to the selected group
             ItemGroup group = groupComboBox.getSelectionModel().getSelectedItem();
             ArrayList<Items> items1 = new ArrayList<Items>(group.getItemsCollection1());
             System.out.println(group.getItemGroupName()+"\n" + group.getItemsCollection().size()+" "+group.getItemsCollection1().size()+"\n"+items1.size());
             itemsComboBox.getItems().clear();
             itemsComboBox.getItems().addAll(items1);
             itemsComboBox.getSelectionModel().clearSelection();
             itemsComboBox.getSelectionModel().selectFirst();
             
         
         }else{
             //get the select subgroup and list items from that 
             //EntityManager em = EntityManagerHelper.getInstance().getEm();
             //em.getTransaction().begin();
             ItemGroup group = subGroupComboBox.getSelectionModel().getSelectedItem();
             
             System.out.println("group has subgroup and subgroup has items");
             System.out.println("subgroup = " +group.getItemGroupName());
             //System.out.println(group.getItemGroupName() + "\n"+group.getItemsCollection().size()+"\n "+group.getItemsCollection1().size());
             ArrayList<Items> items1 = new ArrayList<Items>(group.getItemsCollection());
             System.out.println(items1.size() + "num items ");
             itemsComboBox.getItems().clear();
             itemsComboBox.getItems().addAll(items1);
             itemsComboBox.getSelectionModel().clearSelection();                    
             itemsComboBox.getSelectionModel().selectFirst();
             
             
         
         }
                
     
     }
 
     private void initializeSaleBill() {
         //first thing is get a saleBillNumber;
         
         
         EntityManager em = EntityManagerHelper.getInstance().getEm();
         
         Integer nextValue = (Integer)em.createQuery("select max(s.saleBillNo) from SaleBill s").getSingleResult();
         if(nextValue!=null)
             billNoTextbox.setText(nextValue+"");
         else
             billNoTextbox.setText(1+"");
         
         s.setSaleBillDate(new Date());          
         
         
         s.setSaleBillDate(new Date());
         
         Calendar c = Calendar.getInstance();
         int month = c.get(Calendar.MONTH)+1;
         int day = c.get(Calendar.DAY_OF_MONTH);
         int year = c.get(Calendar.YEAR);
         
         dateTextBox.setText(day+"/"+month+"/"+year);
         dateTextBox.setEditable(false);
         
     }
 
     //qnty cell editing tablecell impl
    
     private void setTotalTextBox(){
         
         
         double total = 0.0;
         double vat = 0.0;
         for(int i = 0 ;i <data.size();i++){
             vat += data.get(i).getItemVatRs();
             total += data.get(i).getTotal() + data.get(i).getItemVatRs();
             
         }
         
         
         if(discountTextBox.getText().equals("")){
             totalTextBox.setText(String.format("%.2f", total));
         }
         else{
             double discount = Double.parseDouble(discountTextBox.getText());
             total -= total*discount/100;
             totalTextBox.setText(String.format("%.2f", total));             
          }
         vatTextBox.setText(String.format("%.2f", vat));
     }
            
 }
 
 
  class EditingQntyCell extends TableCell<SalebillItem,Double>{
            private TextField textField;
            
            public EditingQntyCell(){}
            
            @Override
            public void startEdit(){
                super.startEdit();
                if(textField == null)
                    createTextField();
                
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                textField.selectAll();
            }
            
             @Override
           public void cancelEdit() {
             super.cancelEdit();
 
             setText(String.valueOf(getItem()));
             setContentDisplay(ContentDisplay.TEXT_ONLY);
         }
            
            //update Item
               @Override
               public void updateItem(Double item, boolean empty) {
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
            
            //textfield creation and settings
            private void createTextField(){
                 textField = new TextField(getString());
                 textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
                 textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
 
                     @Override
                     public void handle(KeyEvent t) {
                         System.out.println("inside key handle event");
                         System.out.println(t.getCode());
                         if (t.getCode() == KeyCode.ENTER) {
                             commitEdit(Double.parseDouble(textField.getText()));
                             System.out.println("enter key pressed");
                         } else if (t.getCode() == KeyCode.ESCAPE) {
                             cancelEdit();
                         }
                     }
                 });
            
            
           }//end create textfield
            
             
            private String getString() {
                     return getItem() == null ? "" : getItem().toString();
            }
 
     }
 
 
