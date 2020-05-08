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
 
  private JLabel totalLabel;    // Heading for totals section 
  private JLabel weightingLabel;  // Heading for weightings section
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

    this.mainPanel = new JPanel();

    // Initialize text fields 
    for(int i = 0; i < 3; i++) {
      this.weightings[i] = new JTextField();
      this.totals[i] = new JTextField();
    }

    // Initialize labels 
    this.categories[Course.C] = new JLabel("C/O:");
    this.categories[Course.A] = new JLabel("AP:");
    this.categories[Course.K] = new JLabel("K/U:");
    this.categories[Course.T] = new JLabel("T/I:");
    this.totalLabel = new JLabel("Totals");
    this.weightingLabel = new JLabel("Weightings");

    // Initialize buttons 
    this.okButton = new JButton("OK");
    this.cancelButton = new JButton("Cancel");
  }

  /** Sets up the layout for UI components 
   */
  private void layoutUI() {
   this.contentPane.setLayout(new BoxLayout(this.contentPane,
          BoxLayout.Y_AXIS));
   this.mainPanel.setLayout(new GridLayout(3, 5));
   }
 } 
