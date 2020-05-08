 package devhood.im.sim;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.UIManager;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
import devhood.im.sim.config.SimConfiguration;
import devhood.im.sim.controller.SimControl;
 import devhood.im.sim.event.EventDispatcher;
 import devhood.im.sim.event.Events;
 import devhood.im.sim.model.Message;
 import devhood.im.sim.model.MessageType;
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
 		} catch (Exception e) {
 			log.log(Level.WARNING,
 					"Konnte System default Look n Feel nicht laden, benutzte Java default.",
 					e);
 		}
 		SimMain main = new SimMain();
 
 		main.startup();
 		// SimMain.startSimulation();
 
 	}
 
 	/**
 	 * Startet den {@link ApplicationContext} und zeigt das {@link MainFrame}
 	 * an.
 	 */
 	public void startup() {
 		try {
			ApplicationContext context = new ClassPathXmlApplicationContext(
					"applicationContext.xml");

 			MainFrame mainFrame = (MainFrame) context.getBean("mainFrame");
 			mainFrame.initMainFrame();
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE,
 					"Applikation konnte nicht gestartet werden: ", e);
 		}
 	}
 
 	/**
 	 * startet messaging simulaton, erzeugt 10 nachrichten.
 	 */
 	public static void startSimulation() {
 		final Timer t = new Timer();
 		TimerTask task = new TimerTask() {
 			volatile int cnt = 0;
 
 			@Override
 			public void run() {
 
 				if (cnt < 10) {
 					Message m = new Message();
 					int userid = (int) Math.floor(Math.random() * 10);
 					m.setSender("User " + userid);
 					List<String> users = new ArrayList<String>();
 					// users.add(Sim.getCurrentUser().getName());
 					m.setReceiver(users);
 					if (cnt % 2 == 0) {
 						m.setMessageType(MessageType.ALL);
 					}
 
 					m.setText("Dies ist eine Nachricht ���� " + Math.random());
 
 					EventDispatcher.fireEvent(Events.MESSAGE_RECEIVED, m);
 
 					cnt++;
 				} else {
 					t.cancel();
 				}
 
 			}
 		};
 
 		t.schedule(task, 1000, 5000);
 
 	}
 }
