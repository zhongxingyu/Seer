 import javax.swing.*;
 
 /** The 'Assignments' tab of the MainView.  Provides an interface for the user
  * to change the Course's assignments, as well as list them.
  * @author Qasim Ali 
  * Created June 1, 2013
  */
 public class AssignmentTab extends JPanel {
   private JTable table;  // Displays assignment information 
   private JScrollPane tableScroll;  // Allows user to scroll through
                                     // AssignmentTable 
   private Course course;  // Model to fetch data from
 
   // Buttons 
   private JButton editButton;
   private JButton addButton;
   private JButton removeButton;
 
   private AssignmentTableModel tableModel; // Table model that assignmentTable
                                            // uses to get data from 
   // Controllers for buttons
   private AssignmentAddController addController;
   private AssignmentEditController editController;
   private AssignmentRemoveController removeController;
 
   /** Creates an AssignmentTab with the given Course model 
    * @param course    the Course model to read data from 
    */
   public AssignmentTab(Course course) {
     super();
     this.course = course;
     this.initUI(); 
     this.registerControllers();
     this.update();
   }
 
   /** Initializes UI components 
    */
   private void initUI() {
     this.tableModel = new AssignmentTableModel(this.course);
     this.table = new JTable(this.tableModel);
 
     this.tableScroll = new JScrollPane(this.table);
     this.table.setFillsViewportHeight(true);  // Makes the table fill its 
                                               // parent container tableScroll
     this.editButton = new JButton("Edit assignment...");
     this.addButton = new JButton("Add assignment...");
     this.removeButton = new JButton("Delete assignment");
 
     JPanel buttonPanel = new JPanel();  // Contains buttons to add/remove/edit 
                                         // assignments, all aligned on one 
                                         // row
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
     buttonPanel.add(this.editButton);
     buttonPanel.add(this.removeButton);
     buttonPanel.add(this.addButton);
 
     this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
     this.add(this.tableScroll);
     this.add(buttonPanel);    
   }
 
   /** Creates and registers controllers with assignment add/edit/remove buttons 
    */
   private void registerControllers() {
     // Initialize controllers
     this.removeController = new RemoveController(this.course, this.table);
     this.addController = new AddController(this.course);
    this.editController = new EditController(this.course, this.table);
     
     // Link controllers
     this.addButton.addActionListener(this.addController);
     this.editButton.addActionListener(this.editController);
     this.removeButton.addActionListener(this.removeController);
   }
 
   /** Updates components to reflect the Course's data 
    */
   public void update() {
     this.tableModel.update();
   }
 } 
