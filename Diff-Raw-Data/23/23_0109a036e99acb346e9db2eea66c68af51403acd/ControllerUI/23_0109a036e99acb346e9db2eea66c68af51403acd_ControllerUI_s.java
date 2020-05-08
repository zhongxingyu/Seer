 package edu.cmu.ui;
 
 import java.awt.Desktop;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import org.slf4j.Logger;
 
 import edu.cmu.logger.EmulatorLogger;
//import edu.cmu.scripting.ScriptLoader;
 import edu.cmu.server.JettyServer;
 
 /**
  * A SWING GUI that gives the user the basic control over the embedded jetty
  * server and displays server messages. 
  * This should be the only entry point of this program. 
  * 
  * @author ziw
  * 
  */
 public class ControllerUI extends JFrame {
 	
 	private static final long serialVersionUID = -583606563274987091L;
 	
 	private Logger logger = EmulatorLogger.getServerLogger();
 	private UIActionHandler handler;
 	private Map<Date,String> messages;
 	
 	private static final int WIDTH = 700;
 	private static final int HEIGHT = 300;
 
 	// button labels
 	private static final String WINDOW_LABEL = "BWT Emulator Control";
 	private static final String QUIT_BUTTON = "Quit";
 	private static final String LAUNCH_BUTTON = "Launch Emulator";
 	private static final String CLEAR_BUTTON = "Clear Messages";
 
 	// buttons
 	private JButton quitButton;
 	private JButton launchButton;
 	private JButton clearButton;
 
 	// other components
 	private JTextPane statusArea;
 	private JScrollPane scroll;
 	
 	//tooltips
 	private static final String QUIT_TOOLTIP = "Stop the server and quit the program.";
 	private static final String LAUNCH_TOOLTIP_ON = "Start the server and launch the emulator.";
 		
 	public ControllerUI() {
 		messages = new HashMap<Date,String>();
 		initUI();
 		try {
 			handler = new UIActionHandler();
 			appendMessage(getServerStatus());
 		} catch (Exception e) {
 			logger.error("Error occured when intializing the server.");
 			EmulatorLogger.logException(logger, e);
 			appendMessage("Error occured when intializing the server.");
 			appendMessage(e.getMessage());
 			appendLoggerURL();
 			showPopupErrorMessage(e.getMessage()+"\n" +"Please check server.log");
 //			System.exit(0);
 			//TODO exit the program if OS not valid. Right now commented out for dev purpose.
 		}
 	}
 	
 
 
 	private void initUI() {
 		JPanel panel = new JPanel();
 		getContentPane().add(panel);
 		panel.setLayout(null);
 
 		initLayout();
 		bindActionListener();
 		
 		panel.add(scroll);
 		panel.add(quitButton);
 		panel.add(clearButton);
 		panel.add(launchButton);
 
 		setTitle(WINDOW_LABEL);
 		setSize(WIDTH, HEIGHT);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 	}
 
 
 	
 	private void initLayout(){
 		quitButton = new JButton(QUIT_BUTTON);
 		launchButton = new JButton(LAUNCH_BUTTON);
 		clearButton = new JButton(CLEAR_BUTTON);
 		
 		
 		launchButton.setBounds(20, 30, 150, 50);
 		clearButton.setBounds(20, 100, 150, 50);
 		quitButton.setBounds(20,170,150,50);
 
 		
 		statusArea = new JTextPane();
 		statusArea.setEditable(false);
 		statusArea.setContentType("text/html");
 		
 		scroll = new JScrollPane(statusArea); 
 		scroll.setBounds(200, 30, 480, 200);
 		
 		
 		launchButton.setEnabled(true);
 		
 		quitButton.setToolTipText(QUIT_TOOLTIP);
 		launchButton.setToolTipText(LAUNCH_TOOLTIP_ON);
 		
 		
 	}
 
 	private void bindActionListener() {
 
 		quitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				//try to stop the server before quit the program.
 				try {
					handler.stopServer();
 				} catch (Exception e1) {
 					logger.error("Error when stopping the server when quitButton is clicked");
 					EmulatorLogger.logException(logger, e1);
 				} finally {
 					System.exit(0);
 				}
 			}
 		});
 
 
 		launchButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {	
 				
 				if(handler == null){
 					showPopupErrorMessage("Emulator only supports Windows/Linux.");
 					appendMessage("Error occurred when starting the server.");
 					return;
 				}
 				
 				try {
 					handler.startServer();
 				} catch (Exception e1) {
 					logger.error("Exception when starting the server.");
 					EmulatorLogger.logException(logger, e1);
 					appendMessage("Error occurred when starting the server.");
 					appendMessage(e1.getMessage());
 					appendLoggerURL();
 				}
 				boolean on = handler.isServerRunning();
 				if(on){
 					String url = "http://localhost:" + handler.getPort()
 							+ "/index.html";
 					try {
 						java.awt.Desktop.getDesktop().browse(
 								java.net.URI.create(url));
 					} catch (IOException e1) {
 						appendMessage("Error occurred when launching the emulator.");
 						logger.error("Error opening the browser.");
 						EmulatorLogger.logException(logger, e1);
 					}
 				}
 				appendMessage(getServerStatus());
 
 			}
 		});
 		
 		clearButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				messages.clear();
 				updateStatusArea();
 			}
 		});
 		
 		statusArea.addHyperlinkListener(new HyperlinkListener() {
 				
 				@Override
 				public void hyperlinkUpdate(HyperlinkEvent e) {
 					if(HyperlinkEvent.EventType.ACTIVATED == e.getEventType()){	
 						File logDirectory = new File("./BWT_LOGS");
 						if(logDirectory.exists() && logDirectory.isDirectory()){
 							try {
 								Desktop.getDesktop().open(logDirectory);
 							} catch (IOException e1) {
 								e1.printStackTrace();
 							}
 						}
 					}
 				}
 		});
 		
 	}
 	
 	//used to show critical error.
 	private void showPopupErrorMessage(String msg){
 		JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
 	}
 	
 
 	private void updateStatusArea() {
 		statusArea.setText(getMessages());
 	}
 	
 	//append a message to the end of status area
 	private void appendMessage(String s){
 		if(s!=null){
 			messages.put(new Date(),s.trim());
 		}
 		updateStatusArea();
 	}
 	
 	private String getMessages(){
 		SimpleDateFormat d = new SimpleDateFormat("HH:mm:ss ");
 		StringBuffer sb = new StringBuffer();
 
 		
 		SortedSet<Date> keySet = new TreeSet<Date>(messages.keySet());
 		for(Date date : keySet){
 			sb.append(d.format(date)+":"+ messages.get(date)+"<br/>");
 		}
 		
 		
 		return sb.toString();
 	}
 
 	private void appendLoggerURL(){
 		String s = ("Please check the server log: <a href='#'>server.log</a> ");
 		File logDirectory = new File("./BWT_LOGS");
 		if(logDirectory.exists() && logDirectory.isDirectory()){
 			s+=( ("("+ logDirectory.getAbsolutePath()).replace("./BWT_LOGS", "")+"BWT_LOGS"+")");
 		}
 		appendMessage(s);
 	}
 	
 	private String getServerStatus() {
 		int status = handler.getServerStatus();
 		int port = handler.getPort();
 		String serverStatus;
 
 		switch (status) {
 			case JettyServer.RUNNING:
 				serverStatus = "Running";
 				break;
 			case JettyServer.OFFLINE:
 				serverStatus = "Offline";
 				break;
 			case JettyServer.ERROR:
 			default:
 				serverStatus = "Error. Unknown status";
 		}
 
 		StringBuffer sb = new StringBuffer();
 		sb.append("Server Status: " + serverStatus + "\n");
 
 		if (status == JettyServer.RUNNING)
 			sb.append("Port:          " + port + "\n");
 		return sb.toString();
 	}
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				ControllerUI sg = new ControllerUI();
 				sg.setVisible(true);
 			}
 		});
	}
 }
