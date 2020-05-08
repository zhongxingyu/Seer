 package shell;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 
 import listeners.commandListeners.RemoveSelectedObjects;
 import listeners.commandListeners.SelectAllObjects;
 import listeners.commandListeners.algoritm.AlgoritmRun;
 import listeners.commandListeners.algoritm.AlgoritmStep;
 import listeners.commandListeners.algoritm.AlgoritmStop;
 import listeners.commandListeners.main.Close;
 import listeners.commandListeners.main.Create;
 import listeners.commandListeners.main.Exit;
 import listeners.commandListeners.main.Open;
 import listeners.commandListeners.main.Save;
 import listeners.commandListeners.mode.EnabledEdgeMode;
 import listeners.commandListeners.mode.EnabledEditLabelMode;
 import listeners.commandListeners.mode.EnabledVertexMode;
 import listeners.eventListeners.WindowEvents;
 import main.Controller;
 
 public class MainFrame extends JFrame {
 	private static final int DEFAUT_WIDTH = 800;
 	private static final int DEFAUT_HEIGHT = 600;
 
 	private Controller controller;
 	private Map<String, ActionListener> listeners;
 
 	private JTabbedPane tabbedPane;
 
 	public MainFrame(Controller controller) {
 		initialize();
 		this.controller = controller;
 
 		addWindowListener(new WindowEvents());
 		MenuPanel menuPanel = new MenuPanel(listeners);
 		ButtonPanel buttonPanel = new ButtonPanel(listeners);
 
 		this.tabbedPane = new JTabbedPane();
 		JScrollPane scrollPane = new JScrollPane(tabbedPane);
 		scrollPane
 				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scrollPane
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setAutoscrolls(true);
 
 		Container contentPane = getContentPane();
 		contentPane.add(menuPanel, BorderLayout.NORTH);
 		contentPane.add(buttonPanel, BorderLayout.WEST);
 		contentPane.add(tabbedPane, BorderLayout.CENTER);
 		contentPane.add(scrollPane, BorderLayout.CENTER);
 
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	private void initialize() {
 		setTitle("Editor Graph");
 		setSize(DEFAUT_WIDTH, DEFAUT_HEIGHT);
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		int idGraph = getCurrentIdGraph();
 
 		listeners = new HashMap<String, ActionListener>();
 		listeners.put("VERTEX_MODE", new EnabledVertexMode(this));
 		listeners.put("EDGE_MODE", new EnabledEdgeMode(this));
 		listeners.put("EDIT_MODE", new EnabledEditLabelMode(this));
 		listeners.put("CREATE", new Create(controller));
 		listeners.put("OPEN", new Open(controller));
 		listeners.put("CLOSE", new Close(controller));
 		listeners.put("SAVE", new Save(controller));
 		listeners.put("EXIT", new Exit(controller));
 		listeners.put("SELECT_ALL", new SelectAllObjects(controller));
 		listeners.put("REMOVE", new RemoveSelectedObjects(controller));
 //		listeners.put("RUN_ALGO", new AlgoritmRun(controller));
 //		listeners.put("STEP_ALGO", new AlgoritmStep(controller));
 //		listeners.put("STOP_ALGO", new AlgoritmStop(controller));
 	}
 
 	public int getCurrentIdGraph() {
 		return getCurrentPanel().getId();
 	}
 
 	EditionPanel getCurrentPanel() {
 		return (EditionPanel) tabbedPane.getComponent(tabbedPane
 				.getSelectedIndex());
 	}
 
 	public void paint() {
 		tabbedPane.repaint();
 	}
 
 	public void enableVertexMode() {
 		getCurrentPanel().enableVertexMode();
 	}
 
 	public void enableEdgeMode() {
 		getCurrentPanel().enableEdgeMode();
 	}
 
 	public void enableEditLabelMode() {
 		getCurrentPanel().enableEditMode();
 	}
 
 	public void repaintPanel() {
 		getCurrentPanel().repaint();
 	}
 
 	public void addTab(int id, String header) {
 		tabbedPane.addTab(header, new EditionPanel(id, controller));
 		tabbedPane.setSelectedIndex(tabbedPane.getComponentCount());
 	}
 
 	public void removeTab() {
 		tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
 	}
 }
