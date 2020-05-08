 package warborn.main;
 
 import java.awt.DisplayMode;
 import java.awt.EventQueue;
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
 		/*EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Launcher window = new Launcher();
 					DisplayMode[] modes = window.model.getDisplayModes();
 					warborn.model.ScreenManager screen = window.model.getScreenManager();
 					DisplayMode compatibleMode = screen.findFirstCompatibleMode(modes);
 					System.out.println(compatibleMode.getBitDepth() + "");
 					screen.setFullScreen(compatibleMode);
 					Thread.sleep(5000);
 					screen.restoreScreen();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});*/
 	}
 	public void run() {
 		ScreenManager screen = model.getScreenManager();
 		try {
 			DisplayMode[] modes = model.getDisplayModes();
 			DisplayMode compatibleMode = screen.getFirstCompatibleDisplayMode(modes);
 			screen.setFullScreen(compatibleMode, frame);
 			Thread.sleep(5000);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}finally{
 			screen.restoreScreen();
 		}
 	}
 	/**
 	 * Create the application.
 	 */
 	public Launcher() {
 		initialize();
 		model = new Model();
		model.nextState();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new MainFrame();
 		//frame.setBounds(100, 100, 450, 300);
 		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.add(new GothenburgMapView());
 	}
 
 	
 	public void createGame(IMap map){
 		frame.add((JPanel)map);
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(model.getState() == 1){
 			model.deleteObserver(this);
 			createGame(model.getMap());
 		}
 	}
 
 }
