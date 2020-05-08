 import javax.swing.*;
 import java.awt.event.*;
 
 /** Either creates a new course based on the user's chosen course code or gets
  * one from FileManager based on the chosen course file's name when the user
  * clicks the 'ok' button in StartupWindow
  */
 public class StartupAcceptHandler implements ActionListener {
   private JRadioButton loadButton;  // Radio button that user selects to laod
                                     // course
   private JTextField fileField;  // Field that contains the selected course's
                                  // filename
   private JTextField courseField;  // Field that contains the new course's code
   private StartupWindow window;  // Window that created this handler
   
   /** Creates this handler     
    * @param loadButton    button that is selected to enable course loading
    * @param fileField     text field that contains selected file name
    * @param courseField   text field that contains new course code
    * @param window        the StartupWindow that created this object
    */  
   public StartupAcceptHandler(JRadioButton loadButton, JTextField fileField,
           JTextField courseField, StartupWindow window) {
     this.loadButton = loadButton;
     this.fileField = fileField;
     this.courseField = courseField;
     this.window = window;
   }
   
   /** Get or create a Course depending on which option in the StartupWindow is 
    * selected, and set it
    * @param e    the triggered event
    */
   public void actionPerformed(ActionEvent e) {
     // The load button is selected, so load a course
     if(this.loadButton.isSelected()) {
       String filename = this.fileField.getText();
      this.window.setCourse(FileManager.loadCourse(filename);
     }
     // The 'new course' button must be selected, so create a new course
     else {
       Course newCourse = new Course(this.courseField.getText());
       this.window.setCourse(newCourse);
     }
   }
 }
