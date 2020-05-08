 package final_project.view;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.util.*;
 
 import net.java.balloontip.BalloonTip;
 
 import final_project.control.*;
 import final_project.input.*;
 import final_project.model.store.IDataStore;
 import final_project.view.PoolObserverPanel.Status;
 
 public class MainWindow {
 
 	private JFrame frmFencingManager;
 	private CheckInPanel checkInPanel;
 	private TournamentController tournamentController;
 	private IDataStore dataStore;
 	private JSplitPane splitPane;
 	private SubscriberAdminPanel subscriberAdminPanel;
 	private Collection<BalloonTip> balloons;
 	private JPanel panel_1;
 
 	/**
 	 * Create the application.
 	 */
 
 	public MainWindow(ITournamentInfo info, String username, String password) {
 		tournamentController = new TournamentController(username,password,info, this);
 		dataStore = info.getDataStore();
 		initialize();
 		frmFencingManager.setVisible(true);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		//initialize balloon collection
 		balloons = new LinkedList<BalloonTip>();
 		//Override key bindings to make enter press buttons
 		InputMap im = (InputMap)UIManager.get("Button.focusInputMap");
 		im.put(KeyStroke.getKeyStroke( "ENTER" ), "pressed");
 		im.put(KeyStroke.getKeyStroke( "released ENTER" ), "released");
 
 		frmFencingManager = new JFrame();
 
 		frmFencingManager.setTitle("Fencing Manager");
 		frmFencingManager.setBounds(100, 100, 900, 600);
 		frmFencingManager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{200, 200, 200, 0};
 		gridBagLayout.rowHeights = new int[]{258, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		frmFencingManager.getContentPane().setLayout(gridBagLayout);
 
 		splitPane = new JSplitPane();
 		splitPane.setOneTouchExpandable(true);
 
 		GridBagConstraints gbc_splitPane = new GridBagConstraints();
 		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
 		gbc_splitPane.gridwidth = 3;
 		gbc_splitPane.fill = GridBagConstraints.BOTH;
 		gbc_splitPane.gridx = 0;
 		gbc_splitPane.gridy = 0;
 		frmFencingManager.getContentPane().add(splitPane, gbc_splitPane);
 
 		subscriberAdminPanel = new SubscriberAdminPanel(tournamentController);
 		subscriberAdminPanel.setOpaque(true);
 		subscriberAdminPanel.setSize(new Dimension(273, 58));
 		subscriberAdminPanel.setMinimumSize(new Dimension(0, 58));
 		splitPane.setLeftComponent(subscriberAdminPanel);
 
 		checkInPanel = new CheckInPanel(tournamentController);
 		splitPane.setRightComponent(checkInPanel);
 
 		//PoolSizeInfoPanel poolSizeInfoPanel = new PoolSizeInfoPanel();
 		//tabbedPane.addTab("New tab", null, poolSizeInfoPanel, null);
 		splitPane.setDividerLocation(300);
 		
 		panel_1 = new JPanel();
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.gridwidth = 2;
 		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
 		gbc_panel_1.fill = GridBagConstraints.BOTH;
 		gbc_panel_1.gridx = 1;
 		gbc_panel_1.gridy = 1;
 		frmFencingManager.getContentPane().add(panel_1, gbc_panel_1);
 	}
 	
 	public void loadRightPanel(JPanel panel) {
 		splitPane.setRightComponent(panel);
 	}
 	
 	public void registerBalloon(BalloonTip balloon) {
 		balloons.add(balloon);
 	}
 	
     public void setBalloonsOpacity(float opacity) {
         for (BalloonTip balloon : balloons) {
             balloon.setOpacity(opacity);
         }
     }
 	
 	public void hideAllBalloons() {
 		for (BalloonTip b : balloons)
 			b.setVisible(false);
 	}
 	
 	public void updateSubscriberGUI() {
 		subscriberAdminPanel.updateSubscriberTable();
 	}
 
 	public void setStatusLabel(Status medical, int id) {
		if(splitPane.getRightComponent() instanceof PoolRoundObserverPanel) {
			PoolRoundObserverPanel p = (PoolRoundObserverPanel) splitPane.getRightComponent();
		}
 	}
 }
