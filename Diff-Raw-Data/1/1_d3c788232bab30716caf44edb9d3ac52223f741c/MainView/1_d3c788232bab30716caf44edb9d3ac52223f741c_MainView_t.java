 package org.esgi.java.grabbergui.view.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JToolBar;
 
 import org.esgi.java.grabbergui.controller.ProjectDestructor;
 import org.esgi.java.grabbergui.models.ProjectGrabber;
 import org.esgi.java.grabbergui.view.gui.lang.TR;
 import org.esgi.java.grabbergui.view.update.IViewRefresh;
 import org.esgi.java.grabbergui.view.update.ViewRefresh;
 
 
 /**
  * MainView is the main window of the project. This class controls all
  * events of its Components, and links them.
  * <p>
  * This window is separate in three parts :
  * 	<ul>
  * 		<li>
  * 			<b>The ControlerPanelView :</b> Control the project display.
  * 			It is situated on the left of the main window. 
  * 		<li>
  * 		<li>
  * 			<b>The ProjectPanelView :</b> Display the projects.
  * 			It is situated on the top right of the main window.
  * 		</li>
  * 		<li>
  * 			<b>The StatusPanelView :</b> Display the informations of a
  * 			selected project. It is situated on the bottom left of main
  * 			window.
  * 		</li>
  * 	</ul>
  * </p>
  * 
  * @see ControlerPanelView
  * @see ProjectPanelView
  * @see StatusPanelView
  * 
  * @author Sebastien Manicon : SManicon@free.fr
  * @author Keny SIPOKPEY
  */
 public class MainView extends JFrame implements ActionListener, IViewRefresh {
 	private static final long serialVersionUID = 4841699388035446461L;
 	
 	//---------------------------------------------------------------------------------------------
 	// Private variables
 	//---------------------------------------------------------------------------------------------
 	private JPanel mainPanel;
 	private ProjectPanelView projectPanel;
 	private ControlerPanelView controlerPanel;
 	private StatusPanelView statusPanel;
 	
 	private JMenuItem menuNewProject;
 	private JMenuItem menuImportProject;
 	private JMenuItem menuLeave;
 	
 	private JButton toolStart;
 	private JButton toolPause;
 	private JButton toolStop;
 	private JButton toolLeave;
 	private JButton toolAddProject;
 	private JButton toolDeleteProject;
 
 	
 	//---------------------------------------------------------------------------------------------
 	// Constructor
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Constructs the MainView object 
 	 */
 	public MainView() {
 		super("Grabber");
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		this.mainPanel = new JPanel(new BorderLayout());
 		this.add(this.mainPanel);
 		
 		TR.INIT();
 		
 		// Init all the window item
 		this.projectPanel  = new ProjectPanelView();
 		this.controlerPanel = new ControlerPanelView();
 		this.statusPanel   = new StatusPanelView();
 		
 		// Init the window display
 		this._createWindowSplit();
 		this._createMenu();
 		this._createToolbar();
 		this._addListeners();
 		
 		//launch the view refresh
 		ViewRefresh refresh = new ViewRefresh();
 		refresh.addViewToRefresh(this);
 		refresh.start();
 	}
 	
 	//---------------------------------------------------------------------------------------------
 	// Override methods
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Receive and handle the action events.
 	 * if the source of the event is unknown, then
 	 * this method do nothing. Else, the action will
 	 * be dispatched in an other section of this class.
 	 * 
 	 * @see ActionEvent
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		//If the action come from the ControlerPanel
 		if(e.getSource() == this.controlerPanel) 
 			this._onControlerEvent(e);
 		
 		//If the action come from the Menu
 		else if(e.getSource() instanceof JMenuItem) 
 			this._onMenuEvent(e);
 		
 		else if ((e.getSource() instanceof JButton) && 
 				(((JButton) e.getSource()).getParent() instanceof JToolBar))
 			this._onToolbarEvent(e);
 			
 	}	
 	
 	/**
 	 * Called by the {@link ViewRefresh} for refresh item.
 	 */
 	@Override
 	public void execRefresh() {
 		this.projectPanel.refresh();
 	}
 	
 	//---------------------------------------------------------------------------------------------
 	// Private methods
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Create the split windows.
 	 * The window will be split in three parts :
 	 * <ul>
 	 * 	<li>the controller</li>
 	 * 	<li>the project list</li>
 	 * 	<li>the project informations</li>
 	 * </ul>
 	 * these parts will be resizable.
 	 * 
 	 * @see JSplitPane
 	 */
 	private void _createWindowSplit() {
 		// Split the window with the project panel and the status panel
 		JSplitPane projectSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
 				this.projectPanel, this.statusPanel);
 		projectSplitPane.setPreferredSize(new Dimension(400,400));
 		projectSplitPane.setDividerLocation(200);
 
 		// Split the window with the projects panel and the last split
 		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
 				this.controlerPanel, projectSplitPane);
 		mainSplitPane.setPreferredSize(new Dimension(600,400));
 		mainSplitPane.setDividerLocation(200);
 		
 		// Add the splits to the main window
 		this.mainPanel.add(mainSplitPane,BorderLayout.CENTER);
 	}
 	
 	/**
 	 * Create the menu bar and add it to the main window.
 	 * this menu bar contains action for create new project, leave the
 	 * software or get some help.
 	 * 
 	 * @see JMenuBar
 	 * @see JMenu
 	 * @see JMenuItem
 	 */
 	private void _createMenu() {
 		// Create menu items
 		this.menuNewProject    = new JMenuItem(TR.toString("$MV_MENU_NEW"));
 		this.menuImportProject = new JMenuItem(TR.toString("$MV_MENU_IMPORT"));
 		this.menuLeave         = new JMenuItem(TR.toString("$MV_MENU_LEAVE"));
 		
 		// Create file menu
 		JMenu fileMenu = new JMenu(TR.toString("$MV_MENU_FILE"));
 		fileMenu.add(this.menuNewProject);
 		fileMenu.addSeparator();
 		fileMenu.add(this.menuImportProject);
 		fileMenu.addSeparator();
 		fileMenu.add(this.menuLeave);
 		
 		// Create menu bar
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(fileMenu);
 		
 		// Add the menu bar to the window
 		this.setJMenuBar(menuBar);
 	}
 	
 	/**
 	 * Create the toolbar.
 	 * With it, you can controle the application.
 	 * 
 	 * @see JButton
 	 * @see JToolBar
 	 */
 	private void _createToolbar()
 	{
 		String resourcePath = this._projectPath() + "resources/";
 		
 		this.toolStart = new JButton(new ImageIcon(resourcePath + "toolStart.png"));
 		this.toolStop  = new JButton(new ImageIcon(resourcePath + "toolStop.png"));
 		this.toolPause = new JButton(new ImageIcon(resourcePath + "toolPause.png"));
 		this.toolLeave = new JButton(new ImageIcon(resourcePath + "toolLeave.png"));
 		this.toolAddProject = new JButton(new ImageIcon(resourcePath + "toolAdd.png")); 
 		this.toolDeleteProject = new JButton(new ImageIcon(resourcePath + "toolDelete.png"));
 		
 		this.toolStart.addActionListener(this);
 		this.toolStop.addActionListener(this);
 		this.toolPause.addActionListener(this);
 		this.toolLeave.addActionListener(this);
 		this.toolAddProject.addActionListener(this);
 		this.toolDeleteProject.addActionListener(this);
 		
 		// Create toolBar Processing
 		JToolBar toolBar = new JToolBar();
 		toolBar.setFloatable(false);
 		
 		// Project control
 		toolBar.add(this.toolAddProject);
 		toolBar.add(this.toolDeleteProject);
 		toolBar.add(this.toolLeave);
 		
 		toolBar.addSeparator();
 		
 		// Project Processing
 		toolBar.add(this.toolStart);
 		toolBar.add(this.toolPause);
 		toolBar.add(this.toolStop);
 		
 		this.mainPanel.add(toolBar, BorderLayout.PAGE_START);
 	}
 	
 
 	/**
 	 * Initialize all event listeners of this class components.
 	 */
 	private void _addListeners() {
 		//listener for the controler
 		this.controlerPanel.addActionListner(this);
 		
 		//listener for the menu
 		this.menuNewProject.addActionListener(this);
 		this.menuLeave.addActionListener(this);
 		this.menuImportProject.addActionListener(this);
 		
 		//listener for the toolbar
 		this.toolAddProject.addActionListener(this);
 		this.toolDeleteProject.addActionListener(this);
 		this.toolLeave.addActionListener(this);
 		this.toolPause.addActionListener(this);
 		this.toolStart.addActionListener(this);
 		this.toolStop.addActionListener(this);
 	}
 
 	/**
 	 * Show the dialog for create a new project.
 	 * 
 	 * @see CreateProjectDialog
 	 */
 	private void _createNewProject() 
 	{
 		CreateProjectDialog projectDialog = new CreateProjectDialog(this);
 		projectDialog.setVisible(true);
 		
 		if(projectDialog.isSubmit())
 		{
 			ProjectGrabber pgrabber = new ProjectGrabber(
 			projectDialog.getName(),
 			projectDialog.getURL(),
 			ProjectGrabber.PROJECT_RUN
 			);
 			this.projectPanel.addProject(pgrabber);
 			
 		}
 		
 	}
 	
 	/**
 	 * Launch the opendialog for import a new project.
 	 */
 	private void _importProject()
 	{
 		JFileChooser fc = new JFileChooser();
 		//TODO : Set the filter
 		int result = fc.showOpenDialog(this);
 		if (result == JFileChooser.APPROVE_OPTION) 
 		{
 			//TODO : load project.
 		}
 	}
 	
 	/**
 	 * Return the absolute path of the project
 	 */
 	private String _projectPath() {
 		String classPath = ControlerPanelView.class.getResource("").getPath();
 		return classPath.substring(0, 
 				classPath.length() - ControlerPanelView.class.getPackage().getName().length() - 1);
 	}
 	
 	//---------------------------------------------------------------------------------------------
 	// Events
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * Called when a button of the ControlerPanelview was clicked.
 	 * 
 	 * @see MainView#actionPerformed(ActionEvent)
 	 * @see ControlPanelEvent
 	 */
 	private void _onControlerEvent(ActionEvent e) {
 		// Display all
 		if (e.getActionCommand().equalsIgnoreCase(ControlerPanelView.DISPLAY_ALL))
 			this.projectPanel.showAllProjects();
 		// Display Running
 		else if (e.getActionCommand().equalsIgnoreCase(ControlerPanelView.DISPLAY_RUN))
 			this.projectPanel.showRunProjects();
 		// Display Paused
 		else if (e.getActionCommand().equalsIgnoreCase(ControlerPanelView.DISPLAY_PAUSE))
 			this.projectPanel.showPauseProjects();
 		// Display End
 		else if (e.getActionCommand().equalsIgnoreCase(ControlerPanelView.DISPLAY_END))
 			this.projectPanel.showEndProjects();
 	}
 	
 	/**
 	 * Called when a menu item was clicked.
 	 * 
 	 * @see MainView#actionPerformed(ActionEvent)
 	 */
 	private void _onMenuEvent(ActionEvent e) {
 		//Create a new project
 		if (e.getSource() == this.menuNewProject) 
 			this._createNewProject();
 		
 		//Import New Project
 		else if (e.getSource() == this.menuImportProject)
 			this._importProject();
 		
 		//Leave the application
 		else if(e.getSource() == this.menuLeave) 
 			this.dispose(); 
 	}
 	
 	/**
 	 * Called when a button on the toolbar was clicked.
 	 * 
 	 * @see MainView#actionPerformed(ActionEvent)
 	 */
 	private void _onToolbarEvent(ActionEvent e)
 	{
 		//Create a new project
 		if(e.getSource() == this.toolAddProject)
 			this._createNewProject();
 		
 		//Delete the selectionned project
 		else if(e.getSource() == this.toolDeleteProject)
 			for (ProjectGrabber pgrabber : this.projectPanel.getSelectedprojects()) {
 				this.projectPanel.removeProject(pgrabber);
 				new ProjectDestructor(pgrabber);
 			}
 		//Quit the programe
 		else if(e.getSource() == this.toolLeave)
 			this.dispose();
 		
 		//Put in pause the selectionned project
 		else if(e.getSource() == this.toolPause)
 			for (ProjectGrabber pgrabber : this.projectPanel.getSelectedprojects())
 				pgrabber.pause();	
 		
 		//Start the selectionned project
 		else if(e.getSource() == this.toolStart)
 			for (ProjectGrabber pgrabber : this.projectPanel.getSelectedprojects())
 				pgrabber.start();
 
 		//Stop the selectionned project
 		else if(e.getSource() == this.toolStop)
 			for (ProjectGrabber pgrabber : this.projectPanel.getSelectedprojects())
 				pgrabber.stop();
 	}
 
 }
