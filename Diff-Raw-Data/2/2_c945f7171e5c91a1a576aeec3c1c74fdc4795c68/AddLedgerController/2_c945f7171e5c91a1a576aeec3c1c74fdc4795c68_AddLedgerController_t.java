 /**
  * Sample Skeleton for "AddLedger.fxml" Controller Class
  * You can copy and paste this code into your favorite IDE
  **/
 
 package innuinfocomm;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ResourceBundle;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.control.Button;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextArea;
 import javafx.scene.control.TextField;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import pojos.Ledger;
 import pojos.LedgerGroup;
 import utils.EntityManagerHelper;
 import utils.LedgerView;
 
 public class AddLedgerController {
 
     @FXML // ResourceBundle that was given to the FXMLLoader
     private ResourceBundle resources;
 
     @FXML // URL location of the FXML file that was given to the FXMLLoader
     private URL location;
 
     @FXML // fx:id="cancelButton"
     private Button cancelButton; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_address"
     private TextArea led_address; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_contact_no"
     private TextField led_contact_no; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_contact_person"
     private TextField led_contact_person; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_cst_tin"
     private TextField led_cst_tin; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_email"
     private TextField led_email; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_name"
     private TextField led_name; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_open_bal"
     private TextField led_open_bal; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_open_bal_type"
     private ComboBox<String> led_open_bal_type; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_type"
     private ComboBox<String> led_type; // Value injected by FXMLLoader
 
     @FXML // fx:id="led_vat_tin"
     private TextField led_vat_tin; // Value injected by FXMLLoader
 
     @FXML // fx:id="resetButton"
     private Button resetButton; // Value injected by FXMLLoader
 
     @FXML // fx:id="saveButton"
     private Button saveButton; // Value injected by FXMLLoader
     
     @FXML //fx:id = "errorLabel"
     private Label errorLabel;
     
     @FXML //fx:id = "successLabel"
     private Label successLabel;
 
 
 
     
     
     // Handler for Button[fx:id="cancelButton"] onAction
     @FXML
     void habdleCancelButton(ActionEvent event) {
         // handle the event here
     }
 
     // Handler for Button[fx:id="resetButton"] onAction
     @FXML
     void handleResetButton(ActionEvent event) {
         // handle the event here
         led_address.setText(null);
         led_contact_no.setText(null);
         led_contact_person.setText(null);
         led_cst_tin.setText(null);
         led_email.setText(null);
         led_name.setText(null);
         led_open_bal.setText(null);
         led_vat_tin.setText(null);
         errorLabel.setVisible(false);
         successLabel.setVisible(false);
                 
         
     }
 
     // Handler for Button[fx:id="saveButton"] onAction
     @FXML
     void handleSaveButton(ActionEvent event) {
         // get all the data and save to Database
         
         //write code here to check if the dabase is working or not
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("InnuInfocommPU");
         EntityManager em  = emf.createEntityManager();
         
                
         //check the name field
         String name = led_name.getText();
         if((name == null) || (name.trim().equals(""))){
             successLabel.setVisible(false);
             errorLabel.setText("Name cannot be Empty");
             errorLabel.setVisible(true);
             return;       
         }
             
         
         
         //check opening balance
         float open_bal =0.0f;
         try{
         open_bal = Float.parseFloat(led_open_bal.getText());
         }catch(Exception e){
             
            errorLabel.setText("Please Enter open balance in digits");
             errorLabel.setVisible(true);
             return;        
         }
         
         //take other data
         
         String conPerson = led_contact_person.getText();
         String conNum = led_contact_no.getText();
         String email = led_email.getText();
         String vat = led_vat_tin.getText();
         String cst = led_cst_tin.getText();
         String add = led_address.getText();
         int type  = led_type.getSelectionModel().getSelectedIndex()+1;
         int op_type = led_open_bal_type.getSelectionModel().getSelectedIndex();
         
         Ledger l = new Ledger();
         l.setLedgerId(null);
         l.setLedgerName(name);
         l.setLedgerPersonName(conPerson);
         l.setLedgerAddress(add);
         l.setLedgerContactEmail(email);
         l.setLedgerContactNo(conNum);
         l.setLedgerCstTin(cst);
         l.setLedgerModificationDate(new Date());
         l.setLedgerCreateDate(new Date());
         l.setLedgerOpenBal(open_bal);
         l.setLedgerOpenBalType(op_type==0);
         l.setLedgerType(type);
         l.setLedgerVatTin(vat);
         l.setLedgerPresentBal(open_bal);
         l.setLedgerPresentBalType(op_type==0);
         l.setLedgerType(led_type.getSelectionModel().getSelectedIndex()+1);
         
         try{
         em.getTransaction().begin();
         em.persist(l);
         em.getTransaction().commit();
         }catch(Exception ex){
             errorLabel.setVisible(true);
         }
         
          
         handleResetButton(event);
         successLabel.setVisible(true);
         
         
         
                 
         
         
         
     }
 
     @FXML // This method is called by the FXMLLoader when initialization is complete
     void initialize() {
         assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_address != null : "fx:id=\"led_address\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_contact_no != null : "fx:id=\"led_contact_no\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_contact_person != null : "fx:id=\"led_contact_person\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_cst_tin != null : "fx:id=\"led_cst_tin\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_email != null : "fx:id=\"led_email\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_name != null : "fx:id=\"led_name\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_open_bal != null : "fx:id=\"led_open_bal\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_open_bal_type != null : "fx:id=\"led_open_bal_type\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_type != null : "fx:id=\"led_type\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert led_vat_tin != null : "fx:id=\"led_vat_tin\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert resetButton != null : "fx:id=\"resetButton\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert errorLabel != null : "fx:id=\"errorLabel\" was not injected: check your FXML file 'AddLedger.fxml'.";
         assert successLabel != null : "fx:id=\"successLabel\" was not injected: check your FXML file 'AddLedger.fxml'.";
 
         // Initialize your logic here: all @FXML variables will have been injected
         
         
         led_open_bal_type.setValue("Credit (Cr)");
         errorLabel.setVisible(false);
         successLabel.setVisible(false);
         EntityManager em = EntityManagerHelper.getInstance().getEm();
          Query q = em.createNamedQuery("LedgerGroup.findAll");
         List<LedgerGroup> lgroups =  q.getResultList();
         
         for(LedgerGroup lg : lgroups){
             led_type.getItems().add(lg.getId()-1, lg.getGroupName());           
         }
 
     }
 
 }
