 /**
  * 
  */
 package de.findus.cydonia.server;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
import com.sun.org.apache.bcel.internal.util.ClassLoader;

 import de.findus.cydonia.server.GameServer.ServerStateListener;
 
 /**
  * @author Findus
  *
  */
 @SuppressWarnings("serial")
 public class ServerConfigFrame extends JFrame implements ActionListener, ServerStateListener {
 
 	
 	private GameServer server;
 	
 	private JTextField commandInput;
 	private JButton serverButton;
 	
 	JLabel nameLabel;
 	JLabel mapLabel;
 	JLabel stateLabel;
 	
 	private DefaultListModel<String> listmodel;
 	private JList<String> maplist;
 	
 	public ServerConfigFrame(GameServer server) {
 		this.server = server;
 		
 		this.setTitle("Cydonia Server");
 		
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setLocationByPlatform(true);
 		initGUI();
 		loadMapNames();
 		
 		server.registerStateListener(this);
 	}
 	
 	private void initGUI() {
 		this.setPreferredSize(new Dimension(500, 300));
 		
 		serverButton = new JButton("Start Server");
 		serverButton.addActionListener(this);
 		serverButton.setActionCommand("server");
 		this.add(serverButton, BorderLayout.NORTH);
 		
 		listmodel = new DefaultListModel<String>();
 		maplist = new JList<String>(listmodel);
 		this.add(maplist, BorderLayout.CENTER);
 		
 		nameLabel = new JLabel("My Server");
 		mapLabel = new JLabel();
 		stateLabel = new JLabel("Stopped");
 		
 		JPanel infoPanel = new JPanel(new GridLayout(5, 2));
 		infoPanel.add(new JLabel("Servername: "));
 		infoPanel.add(nameLabel);
 		infoPanel.add(new JLabel("Map: "));
 		infoPanel.add(mapLabel);
 		infoPanel.add(new JLabel("State: "));
 		infoPanel.add(stateLabel);
 		this.add(infoPanel, BorderLayout.EAST);
 		
 		
 		commandInput = new JTextField();
 		commandInput.addActionListener(this);
 		commandInput.setActionCommand("sendCommand");
 		commandInput.setPreferredSize(new Dimension(400, 20));
 		this.add(commandInput, BorderLayout.SOUTH);
 	}
 
 	private void loadMapNames() {
		InputStream is = ClassLoader.getSystemResourceAsStream("de/findus/cydonia/level/levels.txt");
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		String line;
 		try {
 			while ((line = br.readLine()) != null) {
 				listmodel.addElement(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if ("server".equals(e.getActionCommand())) {
 			if(server.isRunning()) {
 				server.stop(false);
 				this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			}else {
 				if(maplist.getSelectedValue() != null) {
 					this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 					server.start(maplist.getSelectedValue());
 				}
 			}
 		}else if("sendCommand".equals(e.getActionCommand())) {
 			server.handleCommand(commandInput.getText());
 		}
 	}
 
 	@Override
 	public void stateChanged() {
 		if(server.isRunning()) {
 			serverButton.setText("Stop Server");
 		}else {
 			serverButton.setText("StartServer");
 		}
 		if(server.getWorldController() != null && server.getWorldController().getMap() != null) { 
 			mapLabel.setText(server.getWorldController().getMap().getName());
 		}
 	}
 }
