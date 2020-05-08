 import javax.swing.*;
 import java.awt.*;
 import java.util.ArrayList;
 import javax.swing.border.*;
 
 /** A panel that contains labels that display basic course info, including 
   * means, medians, and student/assignment info
   * @author Qasim Ali
   */
 public class CourseInfoView extends JPanel {
   private Course course; // Model
 
   // General course info 
   private JPanel infoPanel; // Contains general course info
   private JLabel nameLabel; // Displays the Course's name
   private JLabel students;  // Displays number of students
   private JLabel assignments; // Displays number of assignments
   
   // 'Means' section
   private JPanel meanPanel;
   private JLabel courseMean;
   private JLabel[] meanLabels; // Contains labels that display means for each
                                // category
     
   // 'Medians' section
   private JPanel medianPanel;
   private JLabel courseMedian;
   private JLabel[] medianLabels;
   
   // 'Course Weightings' section
   private JPanel weightingPanel;
   private JLabel[] weightingLabels;
   private JButton weightingEditButton;
   
  public CourseInfoView(Course course) { 
     super();
     this.course = course;
     this.initUI();
     this.update();
   }
   
   /** Initializes UI components
     */
   private void initUI() {
     this.initMean();
     this.initMedian();
     this.initInfo();
     this.initWeighting();
     
     this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
     this.setBorder(new EmptyBorder(10, 10, 10, 10)); // 10px padding
     this.add(this.infoPanel);
     this.add(this.weightingPanel);
     this.add(this.meanPanel);
     this.add(this.medianPanel);
   }
 
   /** Initialize and lay out components for weighting section
    */
   private void initWeighting() {
     // Initialize panel
     this.weightingPanel = new JPanel();
     this.weightingPanel.setLayout(new BoxLayout(this.weightingPanel,
             BoxLayout.Y_AXIS));
     // Labels
     this.weightingLabels = new JLabel[4];
     this.weightingLabels[Course.C] = new JLabel("CO: ");
     this.weightingLabels[Course.T] = new JLabel("T/I: ");
     this.weightingLabels[Course.A] = new JLabel("AP: ");
     this.weightingLabels[Course.K] = new JLabel("K/U: ");
     
     this.weightingEditButton = new JButton("Edit...");
     
     // Lay out and add components
     for(int i = 0; i < 4; i++) {
       // Ensure labels are center aligned
       this.weightingLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);
       this.weightingPanel.add(weightingLabels[i]);
     }
     this.weightingEditButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     this.weightingPanel.add(this.weightingEditButton);
     this.weightingPanel.setBorder(BorderFactory.createTitledBorder(
             "Course Weightings"));
     // Prevent title from being cut off by adding empty component
     this.weightingPanel.add(Box.createRigidArea(new Dimension(150, 0)));
   }
   
   /** Initialize and add components for median section 
    */
   private void initMedian() {
     // Initialize labels 
     this.courseMedian = new JLabel("Course: ");
     this.medianLabels = new JLabel[4];
     this.medianLabels[Course.C] = new JLabel("CO: ");
     this.medianLabels[Course.T] = new JLabel("T/I: ");
     this.medianLabels[Course.A] = new JLabel("AP: ");
     this.medianLabels[Course.K] = new JLabel("K/U: ");
 
     this.medianPanel = new JPanel();
     this.medianPanel.setBorder(BorderFactory.createTitledBorder(
             "Course Medians"));
     this.medianPanel.setLayout(new BoxLayout(this.medianPanel,
             BoxLayout.Y_AXIS));
             
     /* Ensure that the labels are in the center of the panel (adding the 
      * RigidArea to meanPanel misaligns them somehow)
      */
     for(int i = 0; i < 4; i++) {
       this.medianLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);
     }
     this.courseMean.setAlignmentX(Component.CENTER_ALIGNMENT);
     
     // Add components
     this.medianPanel.add(this.courseMean);
     for(JLabel label : this.medianLabels) {
       this.medianPanel.add(label);
     }
     // Empty space used to prevent the panel's title from being cut off
     this.medianPanel.add(Box.createRigidArea(new Dimension(125, 0)));
   }
 
   /** Initialize and add components for mean section
    */
   private void initMean() {
     // Initialize labels 
     this.courseMean = new JLabel("Course: ");
     this.meanLabels = new JLabel[4];
     this.meanLabels[Course.C] = new JLabel("CO: ");
     this.meanLabels[Course.T] = new JLabel("T/I: ");
     this.meanLabels[Course.A] = new JLabel("AP: ");
     this.meanLabels[Course.K] = new JLabel("K/U: ");
 
     this.meanPanel = new JPanel();
     this.meanPanel.setBorder(BorderFactory.createTitledBorder("Course Means"));
     this.meanPanel.setLayout(new BoxLayout(this.meanPanel, BoxLayout.Y_AXIS));
     
     /* Ensure that the labels are in the center of the panel (adding the 
      * RigidArea to meanPanel misaligns them somehow)
      */
     for(int i = 0; i < 4; i++) {
       this.meanLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);
     }
     // Add components
     this.meanPanel.add(this.courseMean);
     for(JLabel label : this.meanLabels) {
       this.meanPanel.add(label);
     }
     
     // Empty space used to prevent the panel's title from being cut off
     this.meanPanel.add(Box.createRigidArea(new Dimension(125, 0)));
   }
 
   /** Initializles and adds components for infoPanel
    */
   private void initInfo() {
     this.infoPanel = new JPanel();
     this.nameLabel = new JLabel("Course code: ");
     this.students = new JLabel("Students: ");
     this.assignments = new JLabel("Assignments: ");
 
     this.infoPanel.setLayout(new BoxLayout(this.infoPanel, BoxLayout.Y_AXIS));
     this.infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // 10px padding
     this.infoPanel.add(this.nameLabel);
     this.infoPanel.add(this.students);
     this.infoPanel.add(this.assignments);
   }
   
   /** Updates general course info labels 
    */
   private void updateCourseInfo() {
     this.students.setText("Students: " + this.course.getStudents().size());
     this.assignments.setText("Assignments: "
             + this.course.getAssignmentOutlines().size());
     this.nameLabel.setText("Course code: " + this.course.getCode());
   }
 
   /** Updates course mean info labels 
    */
   private void updateMean() {
     this.courseMean.setText("Course: " + this.course.getMean());
     this.meanLabels[Course.C].setText("CO: " 
             + this.course.getMean(Course.C));
     this.meanLabels[Course.K].setText("K/U: "
             + this.course.getMean(Course.K));
     this.meanLabels[Course.A].setText("AP: "
             + this.course.getMean(Course.A));
     this.meanLabels[Course.T].setText("T/I: "
             + this.course.getMean(Course.T));
   }
 
   /** Updates course median info labels 
    */
   private void updateMedian() {
     this.courseMedian.setText("Course: " + this.course.getMedian());
     this.medianLabels[Course.C].setText("CO: "
             + this.course.getMedian(Course.C));
     this.medianLabels[Course.K].setText("K/U: " 
             + this.course.getMedian(Course.K));
     this.medianLabels[Course.A].setText("AP: " 
             + this.course.getMedian(Course.A));
     this.medianLabels[Course.T].setText("T/I: "
             + this.course.getMedian(Course.T));
   }
 
   /** Update course weighting labels
    */
   public void updateWeightings() {
     this.weightingLabels[Course.C].setText("CO: "
             + this.course.getCourseWeightings(Course.C));
     this.weightingLabels[Course.K].setText("K/U: "
             + this.course.getCourseWeightings(Course.K));
     this.weightingLabels[Course.T].setText("T/I: "
             + this.course.getCourseWeightings(Course.T));
     this.weightingLabels[Course.A].setText("AP: "
             + this.course.getCourseWeightings(Course.A));
   }
   
   /** Update labels to reflect the Course's data
    */
   public void update() {
     this.updateWeightings();
     this.updateCourseInfo();
     this.updateMean();
     this.updateMedian();
   }
 }
