 package presentation.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.KeyStroke;
 
 import presentation.model.MainWindowModel;
 import presentation.model.ModelController;
 import domain.Library;
 
 /**
  * Represents the overall main window of the library application. Initializes
  * its properties and elements with their controllers.
  */
 public class LibraryMainWindow extends JFrame implements Observer {
 
 	private static final long serialVersionUID = 1L;
 	private MainWindowModel model;
 	private LibraryMenuBar menubar;
 	private ActiveUserPanel userpanel;
 	private LibraryTabbedPane tabbedpane;
 	private ModelController controller;
 	public FindAsYouTypeGlassPane findAsYouTypeGlassPane;
 
 	public LibraryMainWindow(Library library) {
 		controller = new ModelController(library);
 
 		model = controller.main_model;
 		model.addObserver(this);
 
 		controller.library = library;
 
 		setTitle(model.getWindowTitle());
 		setPreferredSize(new Dimension(800, 400));
 		setMinimumSize(new Dimension(460, 355));
 		setSize(750, 450);
 
 		initGUI();
 	}
 
 	public MainWindowModel getModel() {
 		return model;
 	}
 
 	private void initGUI() {
 		initMenubar();
 		findAsYouTypeGlassPane = new FindAsYouTypeGlassPane(this);
 		initActiveUserPanel();
 		initTabbedPane();
 		requestFocusOnSearchField();
 		addGlobalKeyListener();
 		setGlassPaneClosesOnEscape();
 	}
 
 	private void initTabbedPane() {
 		tabbedpane = new LibraryTabbedPane(controller);
 		add(tabbedpane, BorderLayout.CENTER);
 	}
 
 	private void initActiveUserPanel() {
 		userpanel = new ActiveUserPanel(controller);
 		add(userpanel, BorderLayout.NORTH);
 	}
 
 	private void initMenubar() {
 		menubar = new LibraryMenuBar(controller);
 		setJMenuBar(menubar);
 	}
 
 	private void requestFocusOnSearchField() {
 		addWindowListener(new WindowAdapter() {
 			public void windowOpened(WindowEvent e) {
 				controller.searchtab_model.setRequestFocus();
 			}
 		});
 	}
 
 	private void setGlassPaneClosesOnEscape() {
 		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
 		getRootPane().registerKeyboardAction(new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				getGlassPane().setVisible(false);
 			}
 		}, escapeKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
 	}
 
 	private void addGlobalKeyListener() {
 		Toolkit.getDefaultToolkit().getSystemEventQueue().push(
 				new GlobalKeyListenerEventQueue(controller, this));
 	}
 
 	public void update(Observable o, Object arg) {
 		controller.tabbed_model.setActiveTab(controller.tabbed_model
 				.getActiveTab());
 		setTitle(controller.tabbed_model.getTabbedTitle());
 
 		// update set active user
 		// update status bar
 	}
 
 }
