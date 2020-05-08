 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.wordpress.erenha.arjuna.jauza.controller;
 
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFIndividualProperty;
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFProperty;
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFValue;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.ListView;
 import javafx.stage.Stage;
 import org.openrdf.model.URI;
 import org.openrdf.model.vocabulary.DCTERMS;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.model.vocabulary.SKOS;
 
 /**
  * FXML Controller class
  *
  * @author Hindarwan
  */
 public class AddPropertyDialogController implements Initializable {
 
     private Stage stage;
     private MainController mainController;
     @FXML //  fx:id="propertyListView"
     private ListView<RDFProperty> propertyListView; // Value injected by FXMLLoader
     @FXML //  fx:id="vocabularyComboBox"
     private ComboBox<String> vocabularyComboBox; // Value injected by FXMLLoader
     private ObservableList<RDFProperty> property;
 
     public void setStage(Stage stage) {
         this.stage = stage;
     }
 
     public void setMainController(MainController mainController) {
         this.mainController = mainController;
     }
 
     @Override // This method is called by the FXMLLoader when initialization is complete
     public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
         assert propertyListView != null : "fx:id=\"propertyListView\" was not injected: check your FXML file 'addPropertyDialog.fxml'.";
         assert vocabularyComboBox != null : "fx:id=\"vocabularyComboBox\" was not injected: check your FXML file 'addPropertyDialog.fxml'.";
 
         // initialize your logic here: all @FXML variables will have been injected
         property = FXCollections.observableArrayList();
         propertyListView.setItems(property);
        ObservableList<String> vocab = FXCollections.observableArrayList("DCTERMS: Dublin Core Terms", "OWL: Web Ontology Language", "RDFS: RDF Schema Vocabulary", "SKOS: Simple Knowedge Organization System");
         vocabularyComboBox.setItems(vocab);
         vocabularyComboBox.getSelectionModel().selectFirst();
        propertyFromNS("DCTERMS: Dublin Core Terms");
         vocabularyComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                 propertyFromNS(t1);
             }
         });
 
     }
 
     private void propertyFromNS(String vocab) {
         switch (vocab) {
            case "DCTERMS: Dublin Core Terms":
                 setPropertyToList(DCTERMS.class.getDeclaredFields());
                 break;
            case "OWL: Web Ontology Language":
                 setPropertyToList(OWL.class.getDeclaredFields());
                 break;
            case "RDFS: RDF Schema Vocabulary":
                 setPropertyToList(RDFS.class.getDeclaredFields());
                 break;
            case "SKOS: Simple Knowedge Organization System":
                 setPropertyToList(SKOS.class.getDeclaredFields());
                 break;
         }
     }
 
     private void setPropertyToList(Field[] terms) {
         property.clear();
         for (Field field : terms) {
             if (field.getType().getCanonicalName().equals(URI.class.getCanonicalName())) {
                 try {
                     URI uri = (URI) field.get(null);
                    char a = uri.getLocalName().charAt(0);
                    if (a >= 97 && a <= 122) {
                        property.add(new RDFProperty(uri.stringValue(), uri.getLocalName()));
                    }
                 } catch (IllegalArgumentException | IllegalAccessException ex) {
                     Logger.getLogger(AddPropertyDialogController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     public void matchAction(ActionEvent event) {
         if (!propertyListView.getSelectionModel().isEmpty()) {
             RDFIndividualProperty rdfIndividual = new RDFIndividualProperty(
                     propertyListView.getSelectionModel().getSelectedItem(),
                     new RDFValue("", ""));
             mainController.getAnnotationTabController()
                     .getExtractionPanelController()
                     .getIndividualTable()
                     .getSelectionModel()
                     .getSelectedItem()
                     .getRdfIndividualProperty()
                     .add(rdfIndividual);
             mainController.getAnnotationTabController()
                     .getExtractionPanelController()
                     .getIndividualDetails()
                     .add(rdfIndividual);
             stage.close();
         } else {
         }
     }
 }
