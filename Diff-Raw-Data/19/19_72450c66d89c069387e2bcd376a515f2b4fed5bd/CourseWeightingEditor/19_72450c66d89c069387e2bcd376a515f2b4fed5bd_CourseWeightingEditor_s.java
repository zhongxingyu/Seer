 import javax.swing.*;
 import java.awt.*;
 import javax.swing.border.*;
 
 /** A dialog that allows the user to edit a course's weightings for each
  * category
  * @author Qasim Ali
  */
 public class CourseWeightingEditor extends JDialog {
   private Course course;
   private JPanel mainPanel;
   private JPanel contentPane;
   private JLabel[] categoryLabels;  // Contains labels for category fields
   private JTextField[] categoryFields;  // Contains category fields
   private JButton okButton;
 
   /** Creates an editor for the specified course 
    * @param course    the course to edit 
    */
   public CourseWeightingEditor(Course course, MainWindow mw) {
    super(mw, "Editing " + this.course.getCode(), true);
     this.course = course;
     
     this.initUI();
     this.layoutUI();
     this.registerControllers();
     this.pack();
     this.setVisible(true);
   }
 
   /** Initializes UI components
    */
   private void initUI() {
     this.mainPanel = new JPanel();
     this.contentPane = new JPanel();
     this.setContentPane(this.contentPane);
     
     this.categoryLabels = new JLabel[4];
     this.categoryFields = new JTextField[4];
     
     // Initialize labels
     this.categoryLabels[Course.C] = new JLabel("C/O:");
     this.categoryLabels[Course.T] = new JLabel("T/I:");
     this.categoryLabels[Course.A] = new JLabel("AP:");
     this.categoryLabels[Course.K] = new JLabel("K/U:");
     
     // Initialize fields with values from the course
    this.categoryFields[Course.C] = new JTextField(Integer.toString(
             this.course.getCourseWeightings(Course.C)));
     this.categoryFields[Course.T] = new JTextField(Integer.toString(
             this.course.getCourseWeightings(Course.T)));
     this.categoryFields[Course.A] = new JTextField(Integer.toString(
             this.course.getCourseWeightings(Course.A)));
     this.categoryFields[Course.K] = new JTextField(Integer.toString(
            this.course.getCourseWeightings(Course.K)));
             
     this.okButton = new JButton("OK");
   }
   
   private void layoutUI() {
     this.mainPanel.setLayout(new GridLayout(4, 2));  // content pane is 4x2
     
     // Add elements row by row - row 1
     this.mainPanel.add(this.categoryLabels[Course.C]);
     this.mainPanel.add(this.categoryFields[Course.C]);
     // Row 2
     this.mainPanel.add(this.categoryLabels[Course.T]);
     this.mainPanel.add(this.categoryFields[Course.T]);
     // Row 3
     this.mainPanel.add(this.categoryLabels[Course.A]);
     this.mainPanel.add(this.categoryFields[Course.A]);
     // Row 4
     this.mainPanel.add(this.categoryLabels[Course.K]);
     this.mainPanel.add(this.categoryFields[Course.K]);
     
     this.contentPane.setLayout(new BoxLayout(this.contentPane, 
             BoxLayout.Y_AXIS));
     
     this.contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));  //10px padding
     this.contentPane.add(this.mainPanel);
     this.contentPane.add(this.okButton);
   }
   
   private void registerControllers() {
     CourseWeightingAcceptController okController = 
             new CourseWeightingAcceptController(this.course,
                    this.categoryFields);
     
     this.okButton.addActionListener(okController);
   }
 } 
