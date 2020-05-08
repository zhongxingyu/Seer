 import javax.swing.*;
 import java.awt.*;
 
 /** A modal dialog that provides the user methods to edit an assignment's 
  * totals and weightings
  */
 public class AssignmentEditor extends JDialog {
   private Course course;
   private String name;  // Assignment name
 
   private JPanel contentPane;   // This window's content pane 
   private JPanel mainPanel;     // Panel that contains labels and fields for 
                                 // data entry
   private JLabel[] categories;  // Labels used to identify each category 
   private JTextField[] weightings;  // Fields for user to enter weightings 
   private JTextField[] totals;   // Fields for user to enter category totals 
 
   private JPanel buttonPanel;   // Container for buttons 
   private JButton okButton;
   private JButton cancelButton;
 
   /** Shows the editor window for the assignment with the given name
    * @param course    the Course model to use
    * @param name      the name of the assignment to edit 
    * @param mainWindow    the application's main window 
    */
   public AssignmentEditor(Course course, String name, JFrame mainWindow) {
     super(mainWindow, "Editing assignment \'" + name + "\'", true);
     this.course = course;
     this.name = name;
 
     this.initUI();
     this.layoutUI();
     this.registerControllers();
   }
 
   /** Initialize UI components
    */
   private void initUI() {
     this.contentPane = new JPanel();
     this.setContentPane(this.contentPane);
   }
 } 
