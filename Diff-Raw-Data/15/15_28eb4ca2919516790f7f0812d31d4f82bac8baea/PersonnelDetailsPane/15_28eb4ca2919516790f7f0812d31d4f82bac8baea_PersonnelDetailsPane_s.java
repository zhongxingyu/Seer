 package com.newdawn.gui.personnel;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 
 import javafx.beans.binding.Bindings;
 import javafx.beans.binding.StringExpression;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.TextField;
 
 import com.newdawn.model.personnel.Official;
 import com.sun.javafx.binding.SelectBinding;
 
 /**
  * 
  * @author Pierrick Puimean-Chieze
  */
 public class PersonnelDetailsPane implements Initializable {
 
 	@FXML
 	// fx:id="assignementTextField"
 	private TextField assignementTextField; // Value injected by FXMLLoader
 	@FXML
 	// fx:id="designationTextField"
 	private TextField designationTextField; // Value injected by FXMLLoader
 	@FXML
 	// fx:id="locationTestField"
 	private TextField locationTestField; // Value injected by FXMLLoader
 	private ObjectProperty<Official> officialProperty;
 
 	public ObjectProperty<Official> officialProperty() {
 		if (officialProperty == null) {
 			officialProperty = new SimpleObjectProperty<>(this, "official");
 		}
 		return officialProperty;
 	}
 
 	/**
 	 * Get the value of official
 	 * 
 	 * @return the value of official
 	 */
 	public Official getOfficial() {
 		return officialProperty().getValue();
 	}
 
 	/**
 	 * Set the value of official
 	 * 
 	 * @param official
 	 *            new value of official
 	 */
 	public void setOfficial(Official official) {
 		this.officialProperty().setValue(official);
 		
 	}
 
 	@Override
 	// This method is called by the FXMLLoader when initialization is complete
 	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
 		assert assignementTextField != null : "fx:id=\"assignementTextField\" was not injected: check your FXML file 'PersonnelDetailsPane.fxml'.";
 		assert designationTextField != null : "fx:id=\"designationTextField\" was not injected: check your FXML file 'PersonnelDetailsPane.fxml'.";
 		assert locationTestField != null : "fx:id=\"locationTestField\" was not injected: check your FXML file 'PersonnelDetailsPane.fxml'.";
 		// initialize your logic here: all @FXML variables will have been
 		// injected
 		final StringExpression officialNameExpression = Bindings
 				.selectString(officialProperty(), "rank", "designation")
 				.concat(" ")
 				.concat(Bindings.selectString(officialProperty(), "name"));
 		
//		designationTextField.textProperty().bind(officialNameExpression);
//		assignementTextField.textProperty().bind(
//				Bindings.selectString(officialProperty(), "assignment",
//						"visualName"));
 	}
 }
