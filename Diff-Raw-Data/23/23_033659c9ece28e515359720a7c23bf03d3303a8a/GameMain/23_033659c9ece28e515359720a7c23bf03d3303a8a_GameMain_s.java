 package src;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import src.core.Creep;
 import src.core.Game;
 import src.core.Map;
 import src.core.Player;
 import src.core.Tower;
 import src.ui.MapComponent;
 import src.ui.title.TitleScreen;
 import src.ui.side.Sidebar;
 
 /**
  * The main entry point for the entire application.  Displays the main window.
  */
 public class GameMain extends JFrame {
 	
 	private static Thread thread = new Thread();
 	private TitleScreen ts;
 	
 	private static final long serialVersionUID = 1L;
 
 	public GameMain() {
 		// set up window
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(800, 600); // TODO: Figure out resize and/or move this to
 							// another file
 		
 		// initialize the title screen
 		ts = new TitleScreen(this);
 		getContentPane().add(ts);
 	}
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				final GameMain main = new GameMain();
 				main.setVisible(true);
 
 				// refresh the window at about 30 fps
 				Timer t = new Timer(33, new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						main.repaint();
 					}
 				});
 
 				t.start();
 			}
 		});
 	}
 	
 	public void showTitleScreen(){
 		getContentPane().removeAll();
 		getContentPane().add(ts);
 	}
 	
 	public void createGame(){
 		getContentPane().removeAll();
 		getContentPane().setLayout(new BorderLayout());
 		
 		// set up some test data that will be run
 		Map m = Map.demoMap();
 
 		// initialize a game
 		Game g = new Game();
 
 		// single creep
 		Creep c = new Creep();
 		c.setPath(m.getPath());
 		c.setPosition(m.getPath().getPoint(0));
 		
 		ArrayList<Tower> towers = new ArrayList<Tower>();
 
 		// a player
 		Player p = new Player();
 
 		g.setMap(m);
 		g.setTowers(towers);
 		g.setPlayer(p);
 		
 		// set up a gamecontroller to mediate interaction between backend and frontend
 		GameController gc = new GameController();
 		gc.setGame(g);
 
 		// map component to draw everything
 		MapComponent mc = new MapComponent(Map.demoMap());
 		mc.setSize(600, 600);
 		mc.setGridOn(true);
 		mc.setGameController(gc);
 		getContentPane().add(mc, BorderLayout.CENTER);
 
 		// setup sidebar
 		Sidebar s = new Sidebar(gc);
 		getContentPane().add(s, BorderLayout.LINE_END);
 		
 		gc.setSidebar(s);
 
 		Runner r = new Runner();
 		r.setGameController(gc);
 		thread = new Thread(r);
 		thread.start();
 		
		//getContentPane().validate();
		
 	}
 }
