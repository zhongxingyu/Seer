 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.controllers;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Hyperlink;
 import javafx.scene.layout.AnchorPane;
 
 /**
  * FXML Controller class
  *
  * @author Witt
  */
 public class HomeViewController implements Initializable {
 
     @FXML //  fx:id="brwCourseBtn"
     private Button brwCourseBtn; // Value injected by FXMLLoader    
     @FXML //  fx:id="disBrwCourseArea"
     private AnchorPane disBrwCourseArea; // Value injected by FXMLLoader    
     @FXML //  fx:id="homeLk"
     private Hyperlink homeLk; // Value injected by FXMLLoader    
     @FXML //  fx:id="browseCourseLk"
     private Hyperlink browseCourseLk; // Value injected by FXMLLoader
     @FXML //  fx:id="homeView"
     private AnchorPane homeView; // Value injected by FXMLLoader
     @FXML //  fx:id="deptLk"
     private static Hyperlink deptLk; // Value injected by FXMLLoader
     @FXML //  fx:id="courseLk"
     private static Hyperlink courseLk; // Value injected by FXMLLoader
 
     public static Hyperlink getCourseLk() {
         return courseLk;
     }
 
     public static void setCourseLk(Hyperlink courseLk) {
         HomeViewController.courseLk = courseLk;
     }
     
     public static Hyperlink getDeptLk() {
         return deptLk;
     }
 
     public static void setDeptLk(Hyperlink deptLk) {
         HomeViewController.deptLk = deptLk;
     }
     
     @FXML
     private Button logOut;
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         // TODO
         
          // setOnAction when 
         brwCourseBtn.setOnAction(new EventHandler<ActionEvent>()  {
                         
             @Override
             public void handle(ActionEvent event) {                   
                 try {                    
                     View view = new View(disBrwCourseArea);
                     view.loadView("BrowseCourse");     
                     browseCourseLk.setVisible(true);
                 } 
                 catch (Exception ex) {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }
                  
             }
         });
         
         // setOnAction when home link is clicked
         homeLk.setOnAction(new EventHandler<ActionEvent>()  {
                         
             @Override
             public void handle(ActionEvent event) {                   
                 try {                    
                     View view = new View(homeView);
                     view.loadView("HomeView");     
                 } 
                 catch (Exception ex) {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }                 
             }
         });
         
         // setOnAction when browse course link is clicked
         browseCourseLk.setOnAction(new EventHandler<ActionEvent>()  {
                         
             @Override
             public void handle(ActionEvent event) {                   
                 try {                    
                     View view = new View(disBrwCourseArea);
                     view.loadView("BrowseCourse");   
                     deptLk.setVisible(false);
                    courseLk.setVisible(false);
                    browseCourseLk.setVisited(false);
                    deptLk.setVisited(false);
                    courseLk.setVisited(false);
                 } 
                 catch (Exception ex) {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }                 
             }
         });
         
         // setOnAction when dept link is clicked
         deptLk.setOnAction(new EventHandler<ActionEvent>()  {
                         
             @Override
             public void handle(ActionEvent event) {                   
                 try {                    
                     View view = new View(disBrwCourseArea);
                     view.loadView("CourseListView"); 
                     courseLk.setVisible(false);
                    deptLk.setVisited(false);
                 } 
                 catch (Exception ex) {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }                 
             }
         });
         
         // setOnAction when course link is clicked
         courseLk.setOnAction(new EventHandler<ActionEvent>()  {
                         
             @Override
             public void handle(ActionEvent event) {                   
                 try {                    
                     View view = new View(disBrwCourseArea);
                    view.loadView("CourseView"); 
                    courseLk.setVisited(false);
                 } 
                 catch (Exception ex) {
                     Logger.getLogger(HomeViewController.class.getName()).log(Level.SEVERE, null, ex);
                 }                 
             }
         });
     }    
 
     @FXML
     private void logOut(ActionEvent event) {
     }
 }
