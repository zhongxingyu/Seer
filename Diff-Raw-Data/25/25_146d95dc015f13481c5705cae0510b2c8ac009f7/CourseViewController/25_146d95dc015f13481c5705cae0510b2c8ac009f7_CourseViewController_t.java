 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adg.red.controllers;
 
 import adg.red.models.Course;
 import adg.red.models.Department;
 import java.net.URL;
 import java.util.List;
 import java.util.ResourceBundle;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableView;
 import javafx.scene.control.cell.PropertyValueFactory;
 
 /**
  * FXML Controller class
  * <p/>
  * @author Witt
  */
 public class CourseViewController implements Initializable
 {
 
     @FXML //  fx:id="courseIdColmn"
    private TableColumn<Course, String> courseNumberColmn; // Value injected by FXMLLoader
     @FXML //  fx:id="courseNameColmn"
     private TableColumn<Course, String> courseNameColmn; // Value injected by FXMLLoader
     @FXML //  fx:id="disTable"
     private TableView<Course> disTable; // Value injected by FXMLLoader
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
         // TODO
         populateCourseTable(Context.getInstance().getUserSelect());
     }
 
     public void populateCourseTable(String departmentId)
     {
 
         List<Course> courses = Course.getByDepartment(Department.getDepartmentById(departmentId));
         populate(courses);
 
     }
 
     private void populate(List<Course> courses)
     {
        courseNumberColmn.setCellValueFactory(new PropertyValueFactory<Course, String>("departmentIdAndCourseNumber"));
        courseNameColmn.setCellValueFactory(new PropertyValueFactory<Course, String>("name"));        
         disTable.getItems().setAll(courses);
     }
 }
