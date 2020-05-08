 package warborn.main;
 
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import warborn.map.GothenburgMapView;
 import warborn.map.IMap;
 import warborn.model.Model;
 
 public class Launcher implements Observer{
 
 	private JFrame frame;
 	private Model model;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		Launcher l = new Launcher();
 		l.run();
 	}
 	
 	public void run() {
 	/*	ScreenManager screen = model.getScreenManager();
 		try {
 			DisplayMode[] modes = model.getDisplayModes();
 			DisplayMode compatibleMode = screen.getFirstCompatibleDisplayMode(modes);
 			screen.setFullScreen(compatibleMode, frame);
 		//	Thread.sleep(5000);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}finally{
 		//	screen.restoreScreen();
 		}*/
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * Create the application.
 	 */
 	public Launcher() {
 		model = new Model();
 		initialize();
 		model.addObserver(this);
 		model.nextState();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new MainFrame();
 		frame.setBounds(0, 0, model.getWidth(), model.getHeight());
 		frame.setUndecorated(true);
 		frame.setResizable(false);
 		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(GothenburgMapView.getMapPanel());
 	}
 
 	
 	public void createGame(IMap map){
 		frame.add((JPanel)map);
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(model.getState() == 1){
 			model.deleteObserver(this);
 			createGame(model.getMap());
 			System.out.println("Hej");
 		}
 	}
 
 }
