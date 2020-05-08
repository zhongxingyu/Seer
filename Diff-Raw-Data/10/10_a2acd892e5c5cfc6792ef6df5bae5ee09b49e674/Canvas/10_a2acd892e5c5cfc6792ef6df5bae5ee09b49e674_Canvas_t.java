 package frontEnd;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.util.ResourceBundle;
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import backEnd.Turtle;
 import controller.Controller;
 
 
 /**
  * Canvas Class extends JPanel. Contains three areas: CommandPrompt Panel, Command History TextArea,
  * and TurtleView Component. CommandPrompt Panel contains command TextArea.
  * 
  * 
  * @author Danny Goodman, David Le
  * 
  */
 
 @SuppressWarnings("serial")
 public class Canvas extends JPanel {
 
     // default serialization ID
     private static final long serialVersionUID = 1L;
     private static final String FRONTEND_RESOURCE = "resources.FrontEnd";
 
     private JFileChooser myChooser;
     private Controller myController;
     private JTabbedPane myWorkspaces;
     private TurtleView myTurtleView;
     private ResourceBundle myResources;
 
     /**
      * Canvas object created in Main class. Takes in size of JPanel.
      * Initializes JComponents. Instantiates Workspace.
      * 
      * @param size
      */
     public Canvas (Dimension size) {
         // set size (a bit of a pain)
         setPreferredSize(size);
         setSize(size);
         // prepare to receive input
         setFocusable(true);
         requestFocus();
         myResources = ResourceBundle.getBundle(FRONTEND_RESOURCE);
         myWorkspaces = new JTabbedPane();
         add(myWorkspaces);
         myController = new Controller(this);
         int workspaceCount = myWorkspaces.getTabCount() + 1;
         myWorkspaces.add(myResources.getString("WorkspaceTitle") + " " + workspaceCount, makeWorkspace());
         // make file chooser
         myChooser = new JFileChooser(myResources.getString("UserDirectory"));
         // size and display the GUI
         setVisible(true);
     }
     
     /**
      * Creates workspace to be added into the myWorkspaces tab manager
      * 
      * @return workspace to be made
      */
     private WorkspaceView makeWorkspace () {
     	WorkspaceView workspace = new WorkspaceView(this);
     	workspace.makeTurtle();
         return workspace;
     }
     
     /**
      * Returns the workspace which the user is currently working in
      * @return activeWorkspace
      */
     public WorkspaceView getWorkspace () {
     	WorkspaceView activeWorkspace = (WorkspaceView) myWorkspaces.getSelectedComponent();
     	return activeWorkspace;
     }
     
     /**
      * Returns the index of the workspace which the user is currently working
      * in
      * 
      * @return index of active workspace
      */
     public int getWorkspaceIndex () {
     	return myWorkspaces.getSelectedIndex();
     }
 
     /**
      * Returns the controller
      * 
      * @return controller
      */
     public Controller getController() {
     	return myController;
     }
     
     /**
      * Passes a copy of the changedTurtle to the TurtleView. Called by Workspace's Observer method.
      * 
      * @param changedTurtle
      */
     public void updateTurtle (Turtle changedTurtle) {
     	getWorkspace().updateTurtle(changedTurtle);
     }
 
     /**
      * Makes the JMenuBar for the user to interact with
      * 
      * @return menuBar
      */
     public JMenuBar makeMenus () {
         JMenuBar menuBar = new JMenuBar();
         menuBar.add(makeFileMenu());
         menuBar.add(makeViewMenu());
         return menuBar;
     }
 
     /**
      * Makes the "File" menu from which the user can make new workspaces, load commands,
      * or exit the program
      * 
      * @return fileMenu
      */
     private JMenu makeFileMenu () {
         JMenu fileMenu = new JMenu(myResources.getString("FileMenu"));
         fileMenu.add(new AbstractAction(myResources.getString("NewCommand")) {
             @Override
             public void actionPerformed (ActionEvent e) {
             	int workspaceNumber = myWorkspaces.getTabCount() + 1;
             	myWorkspaces.add(myResources.getString("WorkspaceTitle") + " " + workspaceNumber, makeWorkspace());
             }
         });
         fileMenu.add(new AbstractAction(myResources.getString("OpenCommand")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 int response = myChooser.showOpenDialog(null);
                 if (response == JFileChooser.APPROVE_OPTION) {
                     // TODO: process file; throw exception if not working.
                 }
                else {
                	showErrorMsg("Could not open file");
                }
             }
         });
         fileMenu.add(new JSeparator());
         fileMenu.add(new AbstractAction(myResources.getString("ExitCommand")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 // clean up; exit.
                 System.exit(0);
             }
         });
         return fileMenu;
     }
 
     /**
      * Makes the "View" menu from which the user can toggle warping through
      * borders
      * 
      * @return viewMenu
      */
     private JMenu makeViewMenu () {
         JMenu viewMenu = new JMenu(myResources.getString("ViewMenu"));
         viewMenu.add(new AbstractAction(myResources.getString("WarpCommand")) {
             @Override
             public void actionPerformed (ActionEvent e) {
                 myTurtleView.toggleWarp();
             }
 
         });
         return viewMenu;
     }
    
    public void showErrorMsg (String text) {
    	JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
    }
 }
