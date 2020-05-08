 package GUI;
 
 import javax.swing.*;
 import suggest.SuggestGUI;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 
 /**
  * @author bverch
  *
  *
  *	This is the frame that the user will see upon starting the program.
  *
  *	The user will click on the file menu and select one of the following options:
  *
  *		Create a Project: This will create a new whiteboard panel that will initialize as a host.
  *
  *		Join a Project: This will create a new whiteboard panel that will connect to a host, which will then
  *send information about the brainstormsession entered to the whiteboard panel's backend.
  *
  *		Load Project: This will create a new whiteboard panel that will load its backend from an xml(?)
  *
  *		Save Project: This will save the whiteboard panel's backend to an xml(?)
  *
  *		Close Project: This will close a project, prompting the user to save first.
  *
  *		Exit Brainstorm: This will exit the program, prompting the user to save first.
  *
  *
  */
 
 public class MainFrame extends JFrame {
     /**
      * 
      */
     private static final long serialVersionUID = 1601613810507399098L;
     protected static final int SCROLL_INCREMENT = 16;
     public JMenuItem _newProject, _save, _close, _exit, _load, _undo, _redo, _join;
     private JMenu _file, _edit;
     private JMenuBar _menuBar;
     private InterfacePanel _interfacePane;
     private SuggestGUI _suggestPanel;
     private WhiteboardPanel _whiteboard;
     private JFileChooser fc;
     private JScrollPane _scrollPane;
     private MainFrame _frame;
 
     /*
      * Mainframe()
      * 
      * Initializes the menus and list of whiteboardpanels.
      * 
      * 
      */
 
 
     public MainFrame(String projectName){
         super(projectName);
         this.setVisible(true);
         setJMenuBar(initMenu());
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         setBounds(0,0,screenSize.width,screenSize.height);
 
      
         fc = new JFileChooser();
 
         //whiteboard.newProject
         this.setTitle(projectName);
         _whiteboard = new WhiteboardPanel(projectName, this);
         
         _interfacePane = new InterfacePanel(_whiteboard);
         Dimension interfaceSize = new Dimension(350, screenSize.height);
         _interfacePane.setPreferredSize(interfaceSize);
         _interfacePane.setSize(interfaceSize);
         //_interfacePane.setLayout(new GridLayout(10,0));
         _interfacePane.setLayout(new FlowLayout());
         _interfacePane.setVisible(true);
 
         _suggestPanel = new SuggestGUI(interfaceSize, this);
 
         _interfacePane.add(_suggestPanel);
         _frame = this;
         this.addComponentListener(new ComponentListener() {
 			
 			@Override
 			public void componentShown(ComponentEvent e) {
 				
 			}
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				_suggestPanel.textResize(_frame.getSize().getHeight());
				System.out.println(_frame.getSize());
 				
 			}
 			
 			@Override
 			public void componentMoved(ComponentEvent e) {
 				
 			}
 			
 			@Override
 			public void componentHidden(ComponentEvent e) {
 				
 			}
 		});
 
         add(_interfacePane, BorderLayout.WEST);
         fc = new JFileChooser();
 
         //whiteboard.newProject
         this.setTitle(projectName);
         _whiteboard = new WhiteboardPanel(projectName, this);
         _suggestPanel.setBackend(_whiteboard.getBackend());
         int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
         int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
         _scrollPane = new JScrollPane(_whiteboard,v,h);
         //interesting stuff
         _scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
         _scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
         ViewportDragScrollListener l = new ViewportDragScrollListener(_whiteboard);
         _whiteboard._mouseListener = l;
         _whiteboard.getBackend()._mouseListener = l;
         JViewport vp = _scrollPane.getViewport();
         vp.addMouseMotionListener(l);
         vp.addMouseListener(l);
         vp.addHierarchyListener(l);
         add(_scrollPane, BorderLayout.CENTER);
         if(!_suggestPanel.networkingSet()) {
             _suggestPanel.setNetworking(_whiteboard.getBackend().getNetworking());
         }
         //JOptionPane.showMessageDialog(null, "You clicked the New Project menu, and added: " + projectName);
 
         validate();
         repaint();
     }
     /*
      * Method initMenu()
      * 
      * Arguments: none
      * 
      * Returns: the completed JMenu, actionlisteners and all
      * 
      * This method is essentially used just to clean up the constructor of MainFrame a bit.
      * 
      */
     public JMenuBar initMenu(){
         _menuBar = new JMenuBar();
         _menuBar.setVisible(true);
 
         //file menu
         _file = new JMenu("File");
         _file.setMnemonic(KeyEvent.VK_A);
         _file.getAccessibleContext().setAccessibleDescription("Save, load, etc.");
         _menuBar.add(_file);
 
         //new project MenuItem
         _newProject = new JMenuItem("Create a Project", KeyEvent.VK_N);
         _newProject.setMnemonic('N');
         _newProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
         _newProject.getAccessibleContext().setAccessibleDescription("Creates a new project");
         _newProject.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.err.println("NOT IMPLEMENTED");
                 try{
                     Runtime.getRuntime().exec("java brainStormProject");
                 }
                 catch(IOException exception){
 
                 }
             }
         });
         _file.add(_newProject);
 
         //join project MenuItem
         _join = new JMenuItem("Join Project", KeyEvent.VK_L);
         _join.setMnemonic('J');
         _join.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
         _join.getAccessibleContext().setAccessibleDescription("Joins an existing project");
         _join.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 _suggestPanel.tabbedPane.setSelectedIndex(1);
                 _suggestPanel._usernameField.grabFocus();
             }
         });
         _file.add(_join);
         
         //load project MenuItem
         _load = new JMenuItem("Load Project", KeyEvent.VK_L);
         _load.setMnemonic('L');
         _load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
         _load.getAccessibleContext().setAccessibleDescription("Loads an existing project");
         _load.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //whiteboard.loadProject
                 if (!_whiteboard.getBackend().pastActions.empty()) {
                     int ret = JOptionPane.showConfirmDialog(_whiteboard, "You have made changes to the current brainStorm would you like to save?");
                     if (ret == JOptionPane.YES_OPTION) {
                         /* Call save */
                         _save.getActionListeners()[0].actionPerformed(null);
                     } else if (ret == JOptionPane.CANCEL_OPTION || ret == JOptionPane.CLOSED_OPTION) {
                         return;
                     }
                 }
                 int ret = fc.showOpenDialog(_whiteboard);
                 if (ret == JFileChooser.APPROVE_OPTION) {
                     /* Make new whiteboard */
                     /*
                     removeAll();
                     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                     //setBounds(0,0,screenSize.width,screenSize.height);
 
                     _interfacePane = new InterfacePanel(_whiteboard);
                     Dimension interfaceSize = new Dimension(350, screenSize.height);
                     _interfacePane.setPreferredSize(interfaceSize);
                     _interfacePane.setSize(interfaceSize);
                     //_interfacePane.setLayout(new GridLayout(10,0));
                     _interfacePane.setLayout(new FlowLayout());
                     _interfacePane.setVisible(true);
 
                     _suggestPanel = new SuggestGUI(interfaceSize);
 
                     _interfacePane.add(_suggestPanel);
 
                     add(_interfacePane, BorderLayout.WEST);
                     fc = new JFileChooser();
 
                     //whiteboard.newProject
                     //this.setTitle(projectName);
                     _whiteboard = new WhiteboardPanel();
                     _suggestPanel.setBackend(_whiteboard.getBackend());
                     int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
                     int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
                     _scrollPane = new JScrollPane(_whiteboard,v,h);
                     //interesting stuff
                     _scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
                     _scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
                     ViewportDragScrollListener l = new ViewportDragScrollListener(_whiteboard);
                     _whiteboard._mouseListener = l;
                     _whiteboard.getBackend()._mouseListener = l;
                     JViewport vp = _scrollPane.getViewport();
                     vp.addMouseMotionListener(l);
                     vp.addMouseListener(l);
                     vp.addHierarchyListener(l);
                     add(_scrollPane, BorderLayout.CENTER);
                     if(!_suggestPanel.networkingSet()) {
                         _suggestPanel.setNetworking(_whiteboard.getBackend().getNetworking());
                     }
                     //JOptionPane.showMessageDialog(null, "You clicked the New Project menu, and added: " + projectName);
 
                     validate();
                     repaint();
                    */
                     /*
                     //this.setTitle(projectName);
                     removeAll();
                     _whiteboard = new WhiteboardPanel();
                     _suggestPanel.setBackend(_whiteboard.getBackend());
                     /* NEED TO DELETE OLD SCROLL
                     int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
                     int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
                     _scrollPane = new JScrollPane(_whiteboard,v,h);
                     //interesting stuff
                     _scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
                     _scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
                     ViewportDragScrollListener l = new ViewportDragScrollListener(_whiteboard);
                     _whiteboard._mouseListener = l;
                     _whiteboard.getBackend()._mouseListener = l;
                     JViewport vp = _scrollPane.getViewport();
                     vp.addMouseMotionListener(l);
                     vp.addMouseListener(l);
                     vp.addHierarchyListener(l);
                     add(_interfacePane, BorderLayout.WEST);
                     add(_scrollPane, BorderLayout.CENTER);
                     if(!_suggestPanel.networkingSet()) {
                         _suggestPanel.setNetworking(_whiteboard.getBackend().getNetworking());
                     }
                     _scrollPane.revalidate();
                     validate();
                     repaint();
                     */
                     /*
                     //this.setTitle(projectName);
                     _scrollPane.removeAll();
                     //_scrollPane.remove(_whiteboard);
                     _whiteboard = new WhiteboardPanel();
                     //int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
                     //int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
                     _scrollPane.add(_whiteboard);
                     //_whiteboard.e
                     _suggestPanel.setBackend(_whiteboard.getBackend());
                     //interesting stuff
                     ViewportDragScrollListener l = new ViewportDragScrollListener(_whiteboard);
                     _whiteboard._mouseListener = l;
                     _whiteboard.getBackend()._mouseListener = l;
                     if(!_suggestPanel.networkingSet()) {
                         _suggestPanel.setNetworking(_whiteboard.getBackend().getNetworking());
                     }
                     repaint();
                     */
                     /* Load in saved file */
                     _whiteboard.getBackend().load(fc.getSelectedFile());
                 }
             }
         });
         _file.add(_load);
 
         //save project MenuItem
         _save = new JMenuItem("Save Project", KeyEvent.VK_S);
         _save.setMnemonic('S');
         _save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
         _save.getAccessibleContext().setAccessibleDescription("Saves an existing project");
         _save.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int ret = fc.showSaveDialog(_whiteboard);
                 if (ret == JFileChooser.APPROVE_OPTION) {
                     _whiteboard.getBackend().save(fc.getSelectedFile());
                 }
             }
         });
         _file.add(_save);
 
         //quit program MenuItem
         _exit = new JMenuItem("Exit Brainstorm");
         _exit.getAccessibleContext().setAccessibleDescription("Exits the entire program");
         _exit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //something in here saying "are you sure you want to quit?"
                 System.exit(1);
             }
         });
         _file.add(_exit);
 
         _edit = new JMenu("Edit");
         _edit.setMnemonic(KeyEvent.VK_A);
         _edit.getAccessibleContext().setAccessibleDescription("Edit things");
         _menuBar.add(_edit);
 
         //undo action MenuItem
         _undo = new JMenuItem("Undo", KeyEvent.VK_Z);
         _undo.setMnemonic('Z');
         _undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
         _undo.getAccessibleContext().setAccessibleDescription("Undoes an action");
         _undo.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //CLICKING UNDO METHOD
                 if(_whiteboard==null) {
                     System.out.println("no pane is selected!");
                     return;
                 }				
                 _whiteboard.undo();
                 //JOptionPane.showMessageDialog(null, "You clicked the Undo menu");
             }
         });
         _edit.add(_undo);
 
 
         //redo action MenuItem
         _redo = new JMenuItem("Redo", KeyEvent.VK_Y);
         _redo.setMnemonic('Y');
         _redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
         _redo.getAccessibleContext().setAccessibleDescription("Redoes an action");
         _redo.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //CLICKING REDO METHOD
                 if(_whiteboard==null) {
                     System.out.println("no pane is selected!");
                     return;
                 }				
                 _whiteboard.redo();
             }
         });
         _edit.add(_redo);
 
         return _menuBar;
     }
 	public static void main(String[] args) {
 		String projectName = JOptionPane.showInputDialog(null, "Project Name","New Project",JOptionPane.PLAIN_MESSAGE);
 		if(projectName==null) {
 			System.exit(1);
 			return;
 		}
 		if(projectName.length()>0){
 			new MainFrame(projectName);
 		}
 		else{
 			new MainFrame("(untitled)");
 		}
 	}
 	public void initHelpBox(){
 		JFrame helpBox = new JFrame();
 		helpBox.setVisible(true);
 		helpBox.setSize(new Dimension(500,200));
 		JTextArea helpInfo = new JTextArea("Welcome to Brainstorm!\n\nBegin by right-clicking anywhere in the gray area.\n" +
 				"You can change internal settings of each node by right-clicking within them.\nYou can also look up information via the suggest box.\n\n" +
 				"Finally, start brainstorming with your friends!\nHost a project by typing in a username and selecting the 'host' option.\nJoin a project by typing in a username and the address of the host.");
 		helpInfo.setBackground(Color.ORANGE);
 		helpBox.add(helpInfo);
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		helpInfo.setEditable(false);
 		helpBox.setResizable(false);
 		helpBox.setLocation(new Point((screenSize.width/2) - (helpBox.getWidth()/2),(screenSize.height/2) - (helpBox.getHeight()/2)));
 		helpInfo.revalidate();
 		helpBox.repaint();
 	}
 }
