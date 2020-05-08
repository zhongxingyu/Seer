 package src.ui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 
 import src.net.NetworkGame;
 import src.net.NetworkGameController;
 import src.ui.controller.GameController;
 import src.ui.controller.MultiplayerController;
 import src.ui.creepside.CreepSideBar;
 import src.ui.side.Sidebar;
 
 /**
  * Handles setup of multiplayer display components. 
  */
 public class MultiplayerGamePanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	private MultiplayerController controller; 
 	
 	private MapComponent opponentMap;
 	private MapComponent localMap;
 	private Sidebar sidebar;
 	
 	private JPanel gamePanel;
 	
 	public MultiplayerGamePanel(GameController localController, 
 								NetworkGameController networkController,
 								NetworkGame game,
 								MultiplayerController multiController) {
 		
 		super(new BorderLayout());
 		gamePanel = new JPanel();
 		opponentMap = new MapComponent(true);
 		opponentMap.setGridOn(true);
 		opponentMap.setSize(375, 375);
 		
 		localMap = new MapComponent(false);
 		localMap.setGridOn(true);
 		localMap.setSize(375, 375);
 		
 		localController.setGame(game);
		localController.setMultiplayerController(multiController);
 		networkController.setGame(game);
 		
 		sidebar = new Sidebar(localController, multiController);
 		localController.setSidebar(sidebar);
 		
 		opponentMap.setGameController(networkController);
 		opponentMap.setMap(game.getMap());
 		localMap.setGameController(localController);
 		localMap.setMap(game.getMap());
 		
 		gamePanel.add(opponentMap);
 		gamePanel.add(localMap);
 		gamePanel.add(sidebar);
 		
 		add(gamePanel, BorderLayout.CENTER);
 		
 		// setup side bar
 		CreepSideBar cs = new CreepSideBar(localController);
 		add(cs, BorderLayout.SOUTH);
 	}
 }
