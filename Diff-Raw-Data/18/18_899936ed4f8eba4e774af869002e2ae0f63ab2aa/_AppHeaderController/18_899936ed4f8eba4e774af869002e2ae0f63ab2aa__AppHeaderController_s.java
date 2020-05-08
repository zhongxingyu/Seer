 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.sohail.online_quizzing_app.controller;
 
 import com.sohail.online_quizzing_app.AllScenesInterface;
 import com.sohail.online_quizzing_app.OnlineQuizzingApp;
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.text.Text;
 
 /**
  * FXML Controller class
  *
  * @author sohail.alam
  */
 public class _AppHeaderController implements Initializable {
 
     private double xOffset = 0;
     private double yOffset = 0;
     @FXML
     private ImageView imageView_appLogo;
     @FXML
     private Text txt_headerTitle;
     @FXML
     private Button btn_appToggleFullScreen;
     @FXML
     private Button btn_appHelp;
     @FXML
     private Button btn_appAbout;
     @FXML
     private Button btn_appExit;
     @FXML
     private Button btn_appLogout;
     @FXML
     private Text txt_headerInfo;
     @FXML
     private Text txt_headerUserLoggedInName;
 
     //*********** All Events Common To Entire Application
     /**
      *
      * @param event
      */
     synchronized public void handleAppAboutButtonEvent(ActionEvent event) {
         //
     }
 
     /**
      *
      * @param event
      */
     synchronized public void handleAppHelpButtonEvent(ActionEvent event) {
         //
     }
 
     /**
      *
      * @param event
      */
     synchronized public void handleAppExitButtonAction(ActionEvent event) {
         System.exit(0);
     }
 
     /**
      *
      * @param event
      */
     synchronized public void handleAppLogoutButtonAction(ActionEvent event) {
         //
     }
 
     /**
      * Click and Hold to make window draggable
      *
      * @param event
      */
     synchronized public void handleAppMouseClick_Hold(MouseEvent event) {
         xOffset = event.getSceneX();
         yOffset = event.getSceneY();
     }
 
     /**
      * Drag to drag window
      *
      * @param event
      */
     synchronized public void handleAppMouseDrag(MouseEvent event) {
         if (!OnlineQuizzingApp.getInstance().getStage().isFullScreen()) {
             OnlineQuizzingApp.getInstance().getStage().setX(event.getScreenX() - xOffset);
             OnlineQuizzingApp.getInstance().getStage().setY(event.getScreenY() - yOffset);
         }
     }
 
     /**
      * Toggle Full Screen Behavior
      *
      * @param event
      */
     synchronized public void handleAppToggleFullScreenButtonAction(ActionEvent event) {
         if (OnlineQuizzingApp.getInstance().getStage().isFullScreen()) {
             OnlineQuizzingApp.getInstance().getStage().setFullScreen(false);
         } else {
             OnlineQuizzingApp.getInstance().getStage().setFullScreen(true);
         }
     }
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         // TODO
         String stageTitle = OnlineQuizzingApp.getInstance().getStage().getTitle();
         try {
 
             if (stageTitle.equals(AllScenesInterface.ONLINE_QUIZZING_APP_FXML)) {
                 txt_headerInfo.setVisible(false);
                 txt_headerUserLoggedInName.setVisible(false);
                 btn_appLogout.setVisible(false);
             } else {
                 txt_headerInfo.setVisible(true);
                 txt_headerUserLoggedInName.setVisible(true);
                 btn_appLogout.setVisible(true);
             }
 
             switch (stageTitle) {
                 case AllScenesInterface.ADMIN_ADD_NEW_QUIZ_FXML:
                     txt_headerTitle.setText("ADMIN: Add New Quiz");
                     break;
                  case AllScenesInterface.ADMIN_ADD_QUESTION_FXML:
                     txt_headerTitle.setText("ADMIN: Add New Question");
                     break;
                 case AllScenesInterface.ADMIN_HOME_FXML:
                     txt_headerTitle.setText("ADMIN: Home");
                     break;
                 case AllScenesInterface.ADMIN_MANAGE_QUIZ_FXML:
                     txt_headerTitle.setText("ADMIN: Manage Quiz");
                     break;
                 case AllScenesInterface.ADMIN_MANAGE_USERS_FXML:
                     txt_headerTitle.setText("ADMIN: Manage Users");
                     break;
                 case AllScenesInterface.ADMIN_VIEW_QUIZ_FXML:
                     txt_headerTitle.setText("ADMIN: View Quiz");
                     break;
                 case AllScenesInterface.ADMIN_VIEW_SCORES_FXML:
                     txt_headerTitle.setText("ADMIN: View Scores");
                     break;
                 case AllScenesInterface.ONLINE_QUIZZING_APP_FXML:
                     txt_headerTitle.setText("Welcome To Online Quizzing App!!");
                     break;
                 default:
                     throw new AssertionError();
             }
         } catch (Exception e) {
         }
     }
 }
