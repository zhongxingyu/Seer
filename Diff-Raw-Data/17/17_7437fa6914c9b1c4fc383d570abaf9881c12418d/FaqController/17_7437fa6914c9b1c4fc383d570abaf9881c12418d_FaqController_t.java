 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.controllers;
 
 import adg.red.models.Faq;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.ListView;
 import javafx.scene.text.Text;
 
 /**
  * FXML Controller class
  *
  * @author Witt
  */
 public class FaqController implements Initializable {
 
     @FXML //  fx:id="faqLv"
     private ListView<Text> faqLv; // Value injected by FXMLLoader
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         // TODO
         populateListView();
     }    
     
     public void populateListView() {
         List<Faq> faqList;
         faqList= Faq.getAllFaq();
         List<Text> stringList = new ArrayList<>();
           
         
         for(Faq faq: faqList) {
             Text textQ = new Text();
            textQ.wrappingWidthProperty().bind(faqLv.widthProperty());
             textQ.setText("Q: " + faq.getQuestion());            
             stringList.add(textQ);
             
            Text textA = new Text();            
            textA.wrappingWidthProperty().bind(faqLv.widthProperty());
             textA.setText("A: " + faq.getAnswer());            
             stringList.add(textA);
         }           
         
         faqLv.getItems().setAll(stringList);
         
     }
 }
