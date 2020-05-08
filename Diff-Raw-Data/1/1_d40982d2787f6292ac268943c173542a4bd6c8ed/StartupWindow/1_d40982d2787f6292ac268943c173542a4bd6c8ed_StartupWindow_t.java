 import javax.swing.*;
 import java.awt.*;
 import javax.swing.border.*;
 
 /** Window that appears before the main window. Allows the user to either create
  * a new course or load an existing one.
  * @author Qasim Ali
  */
  public class StartupWindow extends JFrame {
    private JPanel newCoursePanel; // Contains components for creating new course
    private JPanel loadCoursePanel; // Contains components for loading course
    private JPanel contentPane;
    private JLabel welcomeLabel;  // Provides instructions for the user
    private JLabel codeLabel;  // Instructs the user to enter course code
    
    // Radio buttons used to select between new and loading courses
    private JRadioButton loadButton;
    private JRadioButton newButton;
    private ButtonGroup group;  // Used to prevent multiple buttons from being
                                // selected
    private JTextField fileName;  // Displays the chosen course's filename
    private JTextField courseCode;  // Allows the user to enter a new course's 
                                    // code
    private JButton okButton;  // Allows the user to confirm and close the window
    private JButton fileButton;  // Allows the user to select a file
    
    private JPanel mainPanel;  // Contains all components except for ok button
    
    public StartupWindow() {
      super("GRADEschool 2K13");
      this.initUI();
      this.layoutUI();
      this.registerControllers();
      this.pack();
      // This window will only be closed when the user clicks the 'ok' button
      this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      
      // Assume that the user wants to load a course
      this.disableNew();
      
      this.setVisible(true);
    }
    
    /** Initialize UI components
     */
     private void initUI() {
       // Initialize panels
       this.contentPane = new JPanel();
       this.setContentPane(this.contentPane);
       this.mainPanel = new JPanel();
       this.newCoursePanel = new JPanel();
       this.loadCoursePanel = new JPanel();
       
       // Initialize buttons
       this.loadButton = new JRadioButton("Load course", true); // Selected by 
                                                                // default
       this.newButton = new JRadioButton("New course");
       this.group = new ButtonGroup();
       this.group.add(loadButton);
       this.group.add(newButton);
       this.fileButton = new JButton("Select file...");
       this.okButton = new JButton("OK");
       
       // Initialize text fields
       this.fileName = new JTextField();
       this.fileName.setEditable(false);  // User can only change filename by
                                          // opening file chooser dialog
       this.courseCode = new JTextField();
       
       // Initialize labels
       this.codeLabel = new JLabel("Course code:");
       this.welcomeLabel = new JLabel("<html>Welcome to GRADEschool 2K13. Please "
               + "load an existing course or create a new one.</html>");
     }
     
     /** Layout UI components
      */
     private void layoutUI() {
       GridLayout mainLayout = new GridLayout(4, 2);
       mainLayout.setHgap(5); // Add horizontal space between components
       
       this.mainPanel.setLayout(mainLayout);
       this.contentPane.setLayout(new BoxLayout(this.contentPane, 
               BoxLayout.Y_AXIS));
               
       this.mainPanel.add(this.loadButton);
       this.mainPanel.add(new JPanel()); // Empty panel used for spacing
       this.mainPanel.add(this.fileButton);
       this.mainPanel.add(this.fileName);
       this.mainPanel.add(this.newButton);
       this.mainPanel.add(new JPanel()); // Empty panel
       this.mainPanel.add(this.codeLabel);
       this.mainPanel.add(this.courseCode);
       
       // Ensure the components are in the center of contentPane
       this.okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
       this.welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
       
       this.contentPane.setBorder(new EmptyBorder(10,10,10,10)); // 10px padding
       this.mainPanel.setBorder(new EmptyBorder(0,0,10,0)); // Add 10px gap on bottom
       this.contentPane.add(this.welcomeLabel);
       this.contentPane.add(this.mainPanel);
       this.contentPane.add(this.okButton);
     }
     
     /** Register controllers for radio buttons, ok button, and file chooser 
      * button
      */
     private void registerControllers() {
       this.loadButton.addActionListener(new StartupButtonHandler(this.loadButton, this));
       this.newButton.addActionListener(new StartupButtonHandler(this.newButton, this));
      this.fileButton.addActionListener(new StartupFileButtonHandler(this.fileName, this));
     }
     
     /** Disable all components within the loadCoursePanel and enable 
      * components in newCoursePanel
      */
     public void disableLoad() {
       this.fileButton.setEnabled(false);
       this.courseCode.setEnabled(true);
       this.codeLabel.setEnabled(true);
     }
     
     /** Disable all components within the newCoursePanel ande enable components
      * in loadCoursePanel
      */
     public void disableNew() {
       this.courseCode.setEnabled(false);
       this.codeLabel.setEnabled(false);
       this.fileButton.setEnabled(true);
     }
  }
