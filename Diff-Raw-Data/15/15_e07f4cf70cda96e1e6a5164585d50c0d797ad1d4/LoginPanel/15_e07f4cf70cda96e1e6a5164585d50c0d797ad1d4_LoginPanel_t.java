 /**
  * Loginpanel voor de quiz
  * 
  * @author De Meersman Vincent
  */
 package javaFXpanels.Login;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 
 import javax.annotation.processing.Messager;
 
 import javaFXpanels.MessageProvider.MessageProvider;
 
 import Protocol.exceptions.IdRangeException;
 import Protocol.requests.CreateUserRequest;
 import Protocol.requests.LoginRequest;
 import Protocol.responses.LoginResponse.UserType;
 
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.TextField;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.Pane;
 
 public class LoginPanel implements Initializable {
 	@FXML
 	private AnchorPane centerAnchor;
 	@FXML
 	private AnchorPane loginAnchor;
 	@FXML
 	private Pane registerPane;
 	@FXML
 	private Label labelGebruiker;
 	@FXML
 	private Label labelWachtwoord;
 	@FXML
 	private Label labelGeenGebruiker;
 	@FXML
 	private Label labelRegNaam;
 	@FXML
 	private Label labelRegVoornaam;
 	@FXML
 	private Label labelRegGebruikersnaam;
 	@FXML
 	private Label labelRegEmail;
 	@FXML
 	private Label labelRegWachtwoord;
 	@FXML
 	private Label labelRegWachtwoordOpnieuw;
 	@FXML
 	private Label labelRegType;
 	@FXML
 	private TextField fieldGebruiker;
 	@FXML
 	private PasswordField fieldWachtwoord;
 	@FXML
 	private TextField fieldRegNaam;
 	@FXML
 	private TextField fieldRegVoornaam;
 	@FXML
 	private TextField fieldRegGebruikersnaam;
 	@FXML
 	private TextField fieldRegEmail;
 	@FXML
 	private PasswordField fieldRegWachtwoord;
 	@FXML
 	private PasswordField fieldRegWachtwoordOpnieuw;
 	@FXML
 	private Button buttonGo;
 	@FXML
 	private Button buttonRegistreer;
 	@FXML
 	private Button buttonRegConfirm;
 	@FXML
 	private Button buttonRegAnnuleer;
 	@FXML
 	private RadioButton radioKwisser;
 	@FXML
 	private RadioButton radioJury;
 	
 	private MessageProvider messageProvider;
 	
 	@Override
 	public void initialize(URL arg0, ResourceBundle arg1) {
 		
		messageProvider = new MessageProvider(loginAnchor);
 			
 	}
 	@FXML
 	private void buttonGoClicked(){
		if (fieldGebruiker.getText().isEmpty() || fieldWachtwoord.getText().isEmpty()) {
			messageProvider.showWarning("Geen gebruikersnaam / wachtwoord ingevuld.");
		} else {
			try {
				new LoginRequest(fieldGebruiker.getText(),
						fieldWachtwoord.getText()).send();
			} catch (IdRangeException e) {
 				messageProvider.showError("Fout bij het aanmelden.");
			}
 		}
 	}
 	@FXML	
 	private void buttonRegisterClicked(){
 		centerAnchor.setVisible(false);
 		centerAnchor.setDisable(true);
 		registerPane.setVisible(true);
 		registerPane.setDisable(false);
 	}
 	@FXML
 	private void buttonRegGoClicked(){
 		
 		
 		if (fieldRegGebruikersnaam.getText().isEmpty() || fieldRegWachtwoord.getText().isEmpty()
 				|| fieldRegVoornaam.getText().isEmpty() || fieldRegNaam.getText().isEmpty() ||
 				fieldRegEmail.getText().isEmpty() || fieldRegWachtwoordOpnieuw.getText().isEmpty()) {
 			
 			messageProvider.showWarning("Gelieve alle velden in te vullen.");
 			
 		} else if (!(fieldRegWachtwoord.getText().equals(fieldRegWachtwoordOpnieuw.getText()))) {
 			
 			messageProvider.showWarning("Wachtwoorden komen niet overeen.");
 			
 		} else {
 	
 			UserType t = null;
 			if (radioJury.isSelected()) {
 				t = UserType.JURRY;
 			} else if (radioKwisser.isSelected()) {
 				t = UserType.PLAYER;
 			}
 			try {
 				new CreateUserRequest(fieldRegGebruikersnaam.getText(),
 						fieldRegWachtwoord.getText(),
 						fieldRegVoornaam.getText(), fieldRegNaam.getText(),
 						fieldRegEmail.getText(), t).send();
 			} catch (IdRangeException e) {
 				messageProvider
 						.showError("Fout bij het aanmaken van nieuwe gebruiker.");
 			}
 		}
 	}
 	@FXML
 	private void buttonRegAnnuleerClicked(){
 		centerAnchor.setVisible(true);
 		centerAnchor.setDisable(false);
 		registerPane.setVisible(false);
 		registerPane.setDisable(true);
 	}
 		
 }
