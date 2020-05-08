 /*
  *
  *
  */
 package adg.red.controllers;
 
 import adg.red.models.Course;
 import adg.red.models.Section;
 import adg.red.models.User;
 import adg.red.session.Context;
 import adg.red.utils.ViewLoader;
 import adg.red.locale.LocaleManager;
 import adg.red.models.skeleton.ILocalizable;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextField;
 
 /**
  * FXML Controller class for SearchBox.fxml
  * <p/>
  * @author harsimran.maan
  */
 public class SearchBoxController implements Initializable, ILocalizable
 {
 
     @FXML
     private Button btnGo;
     @FXML
     private Label lblSearch;
     @FXML
     private TextField txtSearch;
 
     public void localize()
     {
         lblSearch.setText(LocaleManager.get(25) + ":");
         btnGo.setText(LocaleManager.get(26));
     }
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb)
     {
         localize();
     }
 
     /**
      * Search the input keywords. Can only search by department, course number
      * and instructor name.
      * <p/>
      * @param event
      */
     @FXML
     private void search(ActionEvent event) throws Exception
     {
         Context.getInstance().setSearching(true);
        Context.getInstance().setSearchResultList(new ArrayList());
         String keywords[] = txtSearch.getText().trim().split(" ");
         int courseNumber = 0;
         String deptId = "";
 
         for (int i = 0; i < keywords.length; i++)
         {
             try
             {
                 courseNumber = Integer.parseInt(keywords[i]);
             }
             catch (NumberFormatException e)
             {
                 deptId = keywords[i];
             }
         }
 
         // search both course number and deptId
         if (courseNumber != 0 && !deptId.equalsIgnoreCase(""))
         {
             Context.getInstance().setSearchResultList(Course.getByDepartmentIdAndCourseNumber(deptId, courseNumber));
         }
         // search only depId
         else if (courseNumber == 0 && !deptId.equalsIgnoreCase(""))
         {
             if (deptId.length() <= 4)
             {
                 Context.getInstance().setSearchResultList(Course.getByDepartmentId(deptId));
             }
             else
             {
                 List<User> users = null;
                 try
                 {
                     users = User.getUserByFirstName(deptId);
                 }
                 catch (Exception ex)
                 {
                     try
                     {
                         users = User.getUserByLastName(deptId);
                     }
                     catch (Exception ex1)
                     {
                     }
                 }
 
                 if (users != null)
                 {
                     try
                     {
                         ArrayList<Section> sections = new ArrayList();
                         for (User user : users)
                         {
                             sections.addAll(Section.getByFacultyMemberId(user.getFacultyMember()));
                         }
 
                         if (!sections.isEmpty())
                         {
                             ArrayList<Course> courseList = new ArrayList();
                             for (Section sec : sections)
                             {
                                 if (!courseList.contains(sec.getCourse()))
                                 {
                                     courseList.add(sec.getCourse());
                                 }
                             }
                             Context.getInstance().setSearchResultList(courseList);
                         }
                     }
                     catch (Exception ex)
                     {
                        Logger.getLogger(SearchBoxController.class.getName()).log(Level.WARNING, null, ex);
                     }
                 }
             }
         }
         // search only course number
         else if (courseNumber != 0 && deptId.equalsIgnoreCase(""))
         {
             Context.getInstance().setSearchResultList(Course.getCourseByCourseNumer(courseNumber));
         }
 
         ViewLoader view = new ViewLoader(Context.getInstance().getDisplayView());
         view.loadView("student/CourseListView");
     }
 }
