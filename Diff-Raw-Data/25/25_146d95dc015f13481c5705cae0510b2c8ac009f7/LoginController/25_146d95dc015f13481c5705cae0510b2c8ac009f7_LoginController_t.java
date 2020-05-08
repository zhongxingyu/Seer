 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.controllers;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.application.Platform;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.scene.Node;
 import javafx.scene.control.Button;
 import javafx.scene.control.MenuItem;
 import adg.red.BootStrap;
 import adg.red.models.User;
 import adg.red.models.UserType;
 import adg.red.models.Locale;
 import adg.red.models.ResourceDictionary;
 import adg.red.models.ResourceDictionaryPK;
 import adg.red.utils.LocaleManager;
 import adg.red.utils.RedEntityManager;
 import javafx.scene.layout.AnchorPane;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.scene.control.Label;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.TextField;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 /**
  *
  * @author Witt
  */
 public class LoginController implements Initializable
 {
 
     @FXML //  fx:id="close"
     private MenuItem close; // Value injected by FXMLLoader
     @FXML //  fx:id="loginBtn"
     private Button loginBtn; // Value injected by FXMLLoader
     @FXML //  fx:id="viewArea"
     private AnchorPane viewArea; // Value injected by FXMLLoader
     @FXML //  fx:id="loginErrLbl"
     private Label loginErrLbl; // Value injected by FXMLLoader
     @FXML //  fx:id="forgotPassBtn"
     private Button forgotPassBtn; // Value injected by FXMLLoader
     @FXML
     private TextField usernameTxt; // created by J. Yu
     @FXML
     private PasswordField passwordTxt; // created by J. Yu
     @FXML //  fx:id="passLbl"
     private Label passLbl; // Value injected by FXMLLoader
     @FXML //  fx:id="userLbl"
     private Label userLbl; // Value injected by FXMLLoader
 
     /**
      * Initialize all the action events.
      * <p/>
      * @param url
      * @param rb
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
 
         // bind resourceDictionary
         loginBtn.setText(LocaleManager.get(1));
         forgotPassBtn.setText(LocaleManager.get(2));
         userLbl.setText(LocaleManager.get(3));
         passLbl.setText(LocaleManager.get(4));
        loginErrLbl.setText(LocaleManager.get(5));
        
         // setOnAction when close menuitem is selected
         close.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 Platform.exit();
             }
         });
 
         // setOnAction when login button is pressed
         loginBtn.setOnAction(new EventHandler<ActionEvent>()
         {
             @Override
             public void handle(ActionEvent event)
             {
                 // get userid and password input from gui by J. Yu
                 String uid = usernameTxt.getText().toString();
 
                 String pwd = passwordTxt.getText().toString();
                 System.out.println(uid);
                 System.out.println(pwd);
 
                 //LOGIN
                 try
                 {
                     User user = User.login(uid, pwd);
                     View view = new View(viewArea);
                     view.loadView("HomeView");
                 }
                 catch (Exception ex)
                 {
                     loginErrLbl.setVisible(true);
                     loginErrLbl.setText(ex.getMessage());
                 }
             }
         });
     }
 }
