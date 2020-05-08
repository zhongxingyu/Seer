 package devhood.im.sim;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.GenericXmlApplicationContext;
 
 import devhood.im.sim.ui.MainFrame;
 
 /**
  * Main Class.
  * 
  * Possible commandline arguments:
  * 
  * -n nickname -f databasefile
  * 
  * 
  * @author flo
  * 
  */
 public class SimMain {
 
 	private static Logger log = Logger.getLogger(SimMain.class.toString());
 
 	/**
 	 * Main method, starts the program. <br />
 	 * 
 	 * @param args
 	 *            arguments
 	 */
 	public static void main(String[] args) {
 		String username = System.getProperty("user.name");
 		if (args.length > 0) {
 			for (int i = 0; i < args.length; i++) {
 				if ("-n".equals(args[i]) && i + 1 < args.length) {
 					username = args[i + 1];
 				}
 				if ("-f".equals(args[i]) && i + 1 < args.length) {
 					System.setProperty("sim.jdbcDbPath", args[i + 1]);
 				}
 			}
 
 		}
 
 		System.setProperty("sim.username", username);
 
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 
 			SimMain main = new SimMain();
 			main.startup();
 		} catch (Exception e) {
 			log.log(Level.WARNING, "Applikation konnte nicht gestartet werden",
 					e);
 			JOptionPane.showMessageDialog(
 					null,
 					"SIM konnte nicht gestartet werden! Exception: "
 							+ e.getMessage(), "SIM S Instant Messenger",
 					JOptionPane.ERROR_MESSAGE);
 		} finally {
			System.exit(0);
 		}
 	}
 
 	/**
 	 * Startet den {@link ApplicationContext} und zeigt das {@link MainFrame}
 	 * an.
 	 */
 	public void startup() {
 		GenericXmlApplicationContext context = new GenericXmlApplicationContext();
 
 		context.setValidating(false);
 		context.load("classpath:/devhood/im/sim/applicationContext.xml");
 		context.refresh();
 		MainFrame mainFrame = (MainFrame) context.getBean("mainFrame");
 		mainFrame.initMainFrame();
 	}
 }
