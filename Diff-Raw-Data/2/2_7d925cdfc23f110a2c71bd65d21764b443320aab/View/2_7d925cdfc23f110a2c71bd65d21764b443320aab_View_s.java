 package view;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.ResourceBundle;
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import model.IModel;
 
 /**
  * The View for this simulation. Contains a Canvas to draw sprites on,
  * information about positions and heading of the turtle, a command window with
  * history, a command line to type in commands, and options to load and save.
  * 
  * @author Zhen Gou
  * @author David Winegar
  * 
  */
 public class View extends JFrame {
 	private static final long serialVersionUID = 401L;
 	private static final String DEFAULT_RESOURCE_PACKAGE = "view.resources.";
 	private static final String USER_DIR = "user.dir";
 	private static final JFileChooser FILE_CHOOSER = new JFileChooser(System
 			.getProperties().getProperty(USER_DIR));
 
 	private JTabbedPane myTabbedPane;
 	private ResourceBundle myResources;
 	private IModel myModel;
 	private int numberOfWorkspaces = 0;
 	private String myLanguage;
 	private Dimension myCanvasBounds;
 
 	private ArrayList<WorkspaceInView> myWorkspaces = new ArrayList<WorkspaceInView>();
 
 	/**
 	 * Creates the view window.
 	 * 
 	 * @param title
 	 *            title of window
 	 * @param language
 	 *            localization language for configuration file
 	 * @param model
 	 *            IModel used to communicate with model
 	 * @param canvasBounds
 	 *            bounds of the canvas
 	 */
 	public View(String title, String language, IModel model,
 			Dimension canvasBounds) {
 		setTitle(title);
 		myCanvasBounds = canvasBounds;
 		myLanguage = language;
 		myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE
 				+ myLanguage);
 		myModel = model;
 
 		myTabbedPane = new JTabbedPane();
 		getContentPane().add(myTabbedPane);
 		createNewWorkspace();
 
 		setJMenuBar(makeMenuBar());
 		setComponentListener();
 		setTabChangeListener();
 
 		pack();
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	}
 
 	/**
 	 * creates a new workspace, update no. of workspaces, my workspace arraylist
 	 */
 	public void createNewWorkspace() {
 		numberOfWorkspaces++;
 		WorkspaceInView workspace = new WorkspaceInView(myModel,
 				myCanvasBounds, myLanguage, numberOfWorkspaces);
 		myTabbedPane.addTab("Workspace " + numberOfWorkspaces, workspace);
 		myWorkspaces.add(workspace);
 
 	}
 
 	/**
 	 * Creates the menu bar.
 	 * 
 	 * @return JMenuBar representing the menu bar
 	 */
 	private JMenuBar makeMenuBar() {
 		JMenuBar result = new JMenuBar();
 		result.add(makeFileMenu());
 		return result;
 
 	}
 
 	/**
 	 * Creates a menu with 3 options: Save, Load, and Exit.
 	 * 
 	 * @return JMenu representing the menu.
 	 */
 	@SuppressWarnings("serial")
 	protected JMenu makeFileMenu() {
 		JMenu result = new JMenu(myResources.getString("File"));
 		result.add(new AbstractAction(myResources.getString("new_Workspace")) {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				createNewWorkspace();
 				getCurrentWorkspace().updateAndSuppressOutput(); //deal with turtle disappearance
 			}
 		});
 		result.add(new AbstractAction(myResources.getString("LoadCommand")) {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int response = FILE_CHOOSER.showOpenDialog(null);
 				if (response == JFileChooser.APPROVE_OPTION) {
 					File file = FILE_CHOOSER.getSelectedFile();
 
 					myModel.loadFunctionsAndVariables(file);
 					showMessage(myResources.getString("FileLoaded")
 							+ file.getName());
 
 				}
 			}
 		});
 		result.add(new AbstractAction(myResources.getString("SaveCommand")) {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				int response = FILE_CHOOSER.showSaveDialog(null);
 				if (response == JFileChooser.APPROVE_OPTION) {
 					File file = FILE_CHOOSER.getSelectedFile();
 
 					myModel.saveFunctionsAndVariables(file);
 					showMessage(myResources.getString("FileSaved")
 							+ file.getName());
 
 				}
 			}
 		});
 		result.add(new AbstractAction(myResources.getString("Quit")) {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// clean up any open resources, then end program
 				System.exit(0);
 			}
 		});
 		return result;
 	}
 
 	/**
 	 * Writes the message to the command window.
 	 * 
 	 * @param message
 	 */
 	private void showMessage(String message) {
 		getCurrentWorkspace().showMessage(message);
 	}
 
 	private WorkspaceInView getCurrentWorkspace() {
 		int currentWorkspace = myTabbedPane.getSelectedIndex();
 		return myWorkspaces.get(currentWorkspace);
 
 	}
 
 	/**
 	 * Add component listener to the GUI so that when window size changed the
 	 * canvas will update, repainting objects;
 	 */
 
 	public void setComponentListener() {
 		addComponentListener(new ComponentListener() {
 			@Override
 			public void componentResized(ComponentEvent evt) {
 				getCurrentWorkspace().updateAndSuppressOutput();
 
 			}
 
 			@Override
 			public void componentHidden(ComponentEvent arg0) {
 
 			}
 
 			@Override
 			public void componentMoved(ComponentEvent arg0) {
 
 			}
 
 			@Override
 			public void componentShown(ComponentEvent arg0) {
 
 			}
 		});
 	}
 
 	/**
 	 * monitor tab change; if changed update, to ensure repainting
 	 */
 	public void setTabChangeListener() {
 		myTabbedPane.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				WorkspaceInView workspace = myWorkspaces.get(myTabbedPane
 						.getSelectedIndex());
 				myModel.switchToWorkspace(workspace.getID());
				getCurrentWorkspace().update();
 
 			}
 		});
 	}
 
 }
