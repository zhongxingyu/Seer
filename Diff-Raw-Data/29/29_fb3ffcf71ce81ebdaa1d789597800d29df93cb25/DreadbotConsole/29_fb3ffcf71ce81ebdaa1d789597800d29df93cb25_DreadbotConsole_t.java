 package scott.dreadbot;
 
 import gnu.io.CommPortIdentifier;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Locale;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
 import javax.swing.Timer;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import scott.dreadbot.components.GamePadController;
 import scott.dreadbot.components.ServoStatusPanel;
 
 public class DreadbotConsole {
 
 	private static final int DELAY = 40;
 	private static ApplicationContext context;
 	private static GamePadController controller;
 	private static JFrame frame;
 	private static Timer pollTimer;
 
 	// private static JMenuBar menuBar;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		try {
			controller = new GamePadController();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, e.getMessage(),
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				createAndShowGUI();
 			}
 		});
 
 	}
 
 	private static void createAndShowGUI() {
 
 		frame = new JFrame(getSimpleMessage("frame.title"));
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				getLogger().debug(getSimpleMessage("app.timer.exiting"));
 				pollTimer.stop();
 				getLogger().debug(getSimpleMessage("app.exiting"));
 				System.exit(0);
 			}
 		});
 		JMenuBar menuBar = new JMenuBar();
 		// build menus
 		JMenu fileMenu = new JMenu(getSimpleMessage("menu.file"));
 		JMenuItem exitItem = new JMenuItem(
 				getSimpleMessage("menu.file.item.exit"));
 		exitItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				getLogger().debug(getSimpleMessage("app.timer.exiting"));
 				pollTimer.stop();
 				getLogger().debug(getSimpleMessage("app.exiting"));
 				System.exit(0);
 			}
 		});
 		fileMenu.addSeparator();
 		fileMenu.add(exitItem);
 		menuBar.add(fileMenu);
 		JMenu toolMenu = new JMenu(getSimpleMessage("menu.tools"));
 		JMenu serialMenu = new JMenu(getSimpleMessage("menu.tools.menu.serial"));
 		ArrayList<String> ports = getCOMPorts();
 		for (Iterator<String> portsIter = ports.iterator(); portsIter.hasNext();) {
 			JCheckBoxMenuItem mItem = new JCheckBoxMenuItem(portsIter.next());
 			serialMenu.add(mItem);
 		}
 		toolMenu.add(serialMenu);
 		menuBar.add(toolMenu);
 		// Build panel components
 		ServoStatusPanel sPanel1 = new ServoStatusPanel(context.getMessage(
 				"servo.panel.title", new Object[] { 1 }, Locale.getDefault()));
 		// Add components to the frame in proper order
 		frame.setJMenuBar(menuBar);
 		frame.add(sPanel1);
 
 		// initialize and start the Timer
 		pollTimer = new Timer(DELAY, new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		pollTimer.start();
 
 		// Display the window.
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	private static Logger getLogger() {
 		return Logger.getLogger(DreadbotConsole.class);
 	}
 
 	private static String getSimpleMessage(String message) {
 		return context.getMessage(message, null, Locale.getDefault());
 	}
 
 	@SuppressWarnings("unchecked")
 	private static ArrayList<String> getCOMPorts() {
 		ArrayList<String> ports = new ArrayList<String>();
 		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier
 				.getPortIdentifiers();
 		while (portEnum.hasMoreElements()) {
 			CommPortIdentifier portId = portEnum.nextElement();
 			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
 				getLogger().debug("Found serial port: " + portId.getName());
 				ports.add(portId.getName());
 			}
 		}
 		return ports;
 	}
 
 }
