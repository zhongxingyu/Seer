 import javax.swing.*;
 import java.awt.event.*;
 
 /** Creates a CourseWeightingEditor that disables the given JFrame
  * @author Qasim Ali
  */
 public class CourseInfoEditController implements ActionListener {
   private Course course;  // Main model
   private MainWindow mainWindow;  // Window to disable
   
   /** Create this handler for the given Course
    * @param course    the Course to edit
    * @param mw        the MainWindow to disable 
    */
   public CourseInfoEditController(Course course, MainWindow mw) {
     this.course = course;
     this.mainWindow = mw;
   }
   
   /** Open a CourseWeightingEditor
    * @param e    the triggered event
    */
  public void actionPerformed(ActionEvent e) {
     CourseWeightingEditor editor = new CourseWeightingEditor(this.course,
             this.mw);
   }
 }
