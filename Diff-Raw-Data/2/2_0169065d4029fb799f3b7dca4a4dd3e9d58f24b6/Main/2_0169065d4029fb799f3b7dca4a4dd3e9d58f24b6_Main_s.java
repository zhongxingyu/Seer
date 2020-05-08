 package server;
 
 import javax.swing.*;
 import java.awt.*;
 import org.eclipse.swt.widgets.Display;
 import java.rmi.registry.*;
 
 import database.Configure;
 
 @SuppressWarnings("serial")
 public class Main extends JFrame {
 	private final static int LINE = 14, WIDTH = 29;
 	private static final int PORT = 1099;
 	private Registry registry;
 	private JLabel statusLabel;
 	private String[] statusList = new String[LINE];
 	private boolean[] statusNormal = new boolean[LINE];
 	private int listLength = 0;
 
 	Main() {
 		super("HyberCube Server");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(500, 280);
 		setLocationRelativeTo(null);
 		setResizable(false);
		setBackground(Color.black);
 		getContentPane().setLayout(null);
 
 		JPanel animated = new JPanel() {
 			@Override
 			public void paint(Graphics g) {
 				try {
 					g.drawImage(new ImageIcon("res/cube.gif").getImage(), 0, 0,
 							this);
 				} catch (Exception e) {
 				}
 			}
 		};
 		animated.setBounds(0, 0, 256, 256);
 		getContentPane().add(animated);
 
 		statusLabel = new JLabel();
 		statusLabel.setVerticalAlignment(SwingConstants.TOP);
 		statusLabel.setBounds(260, 6, 234, 246);
 		getContentPane().add(statusLabel);
 		add("Initializing server...", true);
 		add("", true);
 
 		try {
 			Configure.read();
 			Interface server = new Server(this);
 			registry = LocateRegistry.createRegistry(PORT);
 			registry.rebind("hybercube", server);
 			add("Server is up and running.", true);
 			add("Listening on " + PORT + ".", true);
 			add("", true);
 		} catch (Exception e) {
 			add("Error starting server!", false);
 		}
 
 		setVisible(true);
 	}
 
 	void add(String entry, boolean normal) {
 		while (entry.length() > WIDTH) {
 			add(entry.substring(0, WIDTH), normal);
 			entry = entry.substring(WIDTH);
 		}
 		if (listLength == LINE) {
 			for (int i = 0; i < LINE - 1; i++) {
 				statusList[i] = statusList[i + 1];
 				statusNormal[i] = statusNormal[i + 1];
 			}
 			statusList[LINE - 1] = entry;
 			statusNormal[LINE - 1] = normal;
 		} else {
 			statusList[listLength] = entry;
 			statusNormal[listLength] = normal;
 			listLength++;
 		}
 		updateLabel();
 	}
 
 	private void updateLabel() {
 		String html = "<html>";
 		for (int i = 0; i < listLength; i++)
 			if (statusNormal[i])
 				html += "<font face=\"Courier\" color=\"white\">"
 						+ statusList[i] + "</font><br>";
 			else
 				html += "<font face=\"Courier\" color=\"red\">" + statusList[i]
 						+ "</font><br>";
 		statusLabel.setText(html + "</html>");
 	}
 
 	public static void main(String[] args) {
 		Display.setAppName("HyberCube");
 		Display display = Display.getDefault();
 
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				new Main();
 			}
 		});
 
 		while (true) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 	}
 }
