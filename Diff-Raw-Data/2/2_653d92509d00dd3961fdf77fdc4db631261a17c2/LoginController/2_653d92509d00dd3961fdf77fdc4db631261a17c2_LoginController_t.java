 package info.unterstein.todofx.presentation.login;
 
 import info.unterstein.todofx.business.boundary.TodoListService;
 import info.unterstein.todofx.business.entity.User;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.TextField;
 import javafx.scene.input.MouseEvent;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 
 public class LoginController implements Initializable {
 
  private static final String LENGTH_ERROR = "Name and password must be set and at least 4 characters.";
 
   private static final String PASSWORD_MATH_ERROR = "Passwords does not match.";
 
   private static final String PASSWORD_USER_ERROR = "Username or password is wrong.";
 
   private static final String USER_EXISTS = "Username exists.";
 
   @FXML
   private Button login;
 
   @FXML
   private Button register;
 
   @FXML
   private TextField loginUserName;
 
   @FXML
   private PasswordField loginPassword;
 
   @FXML
   private TextField registerUserName;
 
   @FXML
   private PasswordField registerPassword;
 
   @FXML
   private PasswordField registerRePassword;
 
   @FXML
   private Label errors;
 
   @Override
   public void initialize(URL url, ResourceBundle resourceBundle) {
     login.setOnMouseClicked(new EventHandler<MouseEvent>() {
 
       @Override
       public void handle(MouseEvent mouseEvent) {
         String userNameValue = loginUserName.getText();
         String passwordValue = loginPassword.getText();
         // validation
         if (TodoListService.validateInputs(userNameValue, passwordValue) == false) {
           errors.setText(LENGTH_ERROR);
           return;
         }
         // persistence
         errors.setText("");
         User user = TodoListService.instance().login(userNameValue, passwordValue);
         if (user == null) {
           errors.setText(PASSWORD_USER_ERROR);
           return;
         }
         // TODO switch View
       }
     });
     register.setOnMouseClicked(new EventHandler<MouseEvent>() {
 
       @Override
       public void handle(MouseEvent mouseEvent) {
         String userNameValue = registerUserName.getText();
         String passwordValue = registerPassword.getText();
         String rePasswordValue = registerRePassword.getText();
         // validation
         if (TodoListService.validateInputs(userNameValue, passwordValue) == false) {
           errors.setText(LENGTH_ERROR);
           return;
         }
         if (TodoListService.validateEquals(passwordValue, rePasswordValue) == false) {
           errors.setText(PASSWORD_MATH_ERROR);
           return;
         }
         try {
           // persistence
           errors.setText("");
           TodoListService.instance().register(userNameValue, passwordValue, rePasswordValue);
           // TODO switch View
         } catch (IllegalArgumentException e) {
           errors.setText(USER_EXISTS);
         }
       }
 
     });
   }
 
 
 }
