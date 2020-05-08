 package linewars.init;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 
 public class GameLobby extends javax.swing.JFrame {
 	
 	private static final int PORT = 8002;
 	
 	private JLabel mapLabel;
 	private JLabel numberLabel;
 	private JLabel raceLabel;
 	private JLabel nameLabel;
 	private JPanel tabbedPane;
 	private JTextField mapField;
 	
 	private MainWindow parent;
 	private List<PlayerObject> players;
 	private int curIndex;
 	private JButton startButton;
 	private JButton cancelButton;
 	
 	private Object playerLock;
 
 	public GameLobby(MainWindow parent) {
 		super();
 		this.parent = parent;
 		curIndex = 1;
 		players = new ArrayList<PlayerObject>();
 		playerLock = new Object();
 		initGUI();
 	}
 	
 	public void startServer(String hostName, String mapURI) throws IOException
 	{
 		mapField.setText(mapURI);
 		
 		PlayerObject p = new PlayerObject(hostName, "127.0.0.1");
 		players.add(p);
 		addPlayer(p, true);
 		
 		
 		ServerClientListener listener = new ServerClientListener(new ServerSocket(PORT));
 		startButton.addActionListener(listener);
 		cancelButton.addActionListener(listener);
 		players.get(0).raceBox.addActionListener(listener);
 		
 		Thread th = new Thread(listener);
 		th.setDaemon(true);
 		th.start();
 	}
 	
 	public void startClient(String clientName, String serverAddress) throws UnknownHostException, IOException
 	{
 		Socket socket = new Socket(serverAddress, PORT);
 		
 		ServerConnection conn = new ServerConnection(socket, clientName);
 		cancelButton.addActionListener(conn);
 		
 		// sends the server the player name
 		conn.out.println(clientName);
 		
 		// gets the map and clientIp from server
 		mapField.setText((String) conn.readObject());
 		conn.ipAddress = (String) conn.readObject();
 		
 		// starts the reading thread
 		Thread th = new Thread(conn);
 		th.setDaemon(true);
 		th.start();
 	}
 	
 	private void clearPlayers()
 	{
 		synchronized (playerLock)
 		{
 			for (PlayerObject p : players)
 			{
 				tabbedPane.remove(p.playerIndex);
 				tabbedPane.remove(p.nameField);
 				tabbedPane.remove(p.raceBox);
 			}
 			curIndex = 1;
 			players.clear();
 		}
 	}
 	
 	private void addPlayer(PlayerObject toAdd, boolean enabled)
 	{
 		toAdd.raceBox.setEnabled(enabled);
 		
 		GridBagConstraints c = new GridBagConstraints(0, Integer.parseInt(toAdd.playerIndex.getText()), 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
 		tabbedPane.add(toAdd.playerIndex, c);
 		
 		c.gridx += 1;
 		c.anchor = GridBagConstraints.WEST;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		tabbedPane.add(toAdd.nameField, c);
 		
 		c.gridx += 2;
 		tabbedPane.add(toAdd.raceBox, c);
 	}
 	
 	private void showMainMenu()
 	{
 		// TODO implement
 	}
 	
 	private Game initializeGame()
 	{
 		List<String> races = new ArrayList<String>(players.size());
 		List<String> names = new ArrayList<String>(players.size());
 		for (PlayerObject o : players)
 		{
 			races.add((String) o.raceBox.getSelectedItem());
 			names.add(o.nameField.getText());
 		}
 		return new Game(mapField.getText(), players.size(), races, names);
 	}
 	
 	private class PlayerObject
 	{
 		private JLabel playerIndex;
 		private JTextField nameField;
 		private JComboBox raceBox;
 		private String ipAddress;
 		
 		public PlayerObject(String name, String address)
 		{
 			playerIndex = new JLabel(new Integer(curIndex++).toString());
 			nameField = new JTextField(name);
 			nameField.setEnabled(false);
 			raceBox = new JComboBox(parent.getRaces());
 			ipAddress = address;
 		}
 		
 		public PlayerObject(String[] dataStrings)
 		{
 			playerIndex = new JLabel(new Integer(curIndex++).toString());
 			nameField = new JTextField(dataStrings[0]);
 			nameField.setEnabled(false);
 			startButton.setEnabled(false);
 			ipAddress = dataStrings[1];
 			raceBox = new JComboBox(parent.getRaces());
 			raceBox.setSelectedItem(dataStrings[2]);
 		}
 		
 		public String[] getDataStrings()
 		{
 			return new String[]{ nameField.getText(), ipAddress, (String) raceBox.getSelectedItem() };
 		}
 	}
 	
 	private class ServerClientListener implements Runnable, ActionListener
 	{
 		private ServerSocket serverSocket;
 		private List<ClientConnection> clientConnections;
 		private boolean running;
 		
 		public ServerClientListener(ServerSocket socket)
 		{
 			serverSocket = socket;
 			clientConnections = new ArrayList<ClientConnection>();
 		}
 		
 		public void run()
 		{
 			running = true;
 			while (running)
 			{
 				// accepts a new client
 				Socket clientSocket = null;
 				try
 				{
 					clientSocket = serverSocket.accept();
 				}
 				catch (IOException e)
 				{
 					continue;
 				}
 				
 				// creates a player object for this client
 				ClientConnection conn = new ClientConnection(clientSocket, curIndex);
 				final PlayerObject playerObject = new PlayerObject(conn.clientName, conn.clientIpAddress);
 				
 				// sends the client the map and its ip address for identification
 				writeObject(conn, mapField.getText());
 				writeObject(conn, playerObject.ipAddress);
 				
 				// adds it to the list of sockets
 				synchronized (playerLock) {
 					clientConnections.add(conn);
 					players.add(playerObject);
 				}
 				
 				// update the gui
 				SwingUtilities.invokeLater(new Runnable() { public void run() {
 					addPlayer(playerObject, false);
 					validate();
 				}});
 				
 				// send updated list to all players
 				updateAllClients();
 				
 				// starts up a new receiver
 				Thread th = new Thread(conn);
 				th.setDaemon(true);
 				th.start();
 			}
 		}
 		
 		public void actionPerformed(ActionEvent e)
 		{
 			Object src = e.getSource();
 			if (src instanceof JComboBox)
 			{
 				updateAllClients();
 			}
 			else if (src == startButton)
 			{
 				synchronized (playerLock)
 				{
 					// send start messages
 					for (ClientConnection c : clientConnections)
 					{
 						writeObject(c, "start");
 					}
 					
 					List<String> addresses = new ArrayList<String>(players.size());
 					for (PlayerObject o : players)
 					{
 						addresses.add(o.ipAddress);
 					}
 					
 					try {
 						serverSocket.close();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 					running = false;
 					parent.dispose();
 					
 					 Game g = initializeGame();
 					 g.initializeServer(players.size(), addresses);
 					 g.initializeClient(players.size(), "127.0.0.1", 0);
 					 g.run();
 				}
 			}
 			else if (src == cancelButton)
 			{
 				synchronized (playerLock)
 				{
 					for (ClientConnection c : clientConnections)
 					{
 						writeObject(c, "cancel");
 					}
 					
 					running = false;
 					try {
 						serverSocket.close();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 				
 				showMainMenu();
 			}
 		}
 		
 		private void updateAllClients()
 		{
 			String[][] data = null;
 			synchronized (playerLock)
 			{
 				data = new String[players.size()][];
 				for (int i = 0; i < players.size(); ++i)
 				{
 					data[i] = players.get(i).getDataStrings();
 				}
 				
 				for (int i = 0; i < clientConnections.size(); ++i)
 				{
 					ClientConnection conn = clientConnections.get(i);
 					writeObject(conn, data);
 				}
 			}
 		}
 		
 		private void writeObject(ClientConnection conn, Object o)
 		{
 			try {
 				conn.out.writeObject(o);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		private class ClientConnection implements Runnable
 		{
 			private BufferedReader in;
 			private ObjectOutputStream out;
 			
 			private String clientName;
 			private String clientIpAddress;
 			private int clientIndex;
 			
 			public ClientConnection(Socket socket, int index)
 			{
 				clientIndex = index;
 				clientIpAddress = socket.getInetAddress().getHostAddress();
 				
 				try {
 					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 					out = new ObjectOutputStream(socket.getOutputStream());
 				} catch (IOException e) {
 					// problem getting IO for socket
 					e.printStackTrace();
 				}
 				
 				// gets the name of the client using the input reader
 				clientName = readLine();
 			}
 			
 			public void run()
 			{
 				String inputLine = null;
 				while ((inputLine = readLine()) != null)
 				{
 					final String race = inputLine;
 					SwingUtilities.invokeLater(new Runnable() { public void run() {
 						synchronized (playerLock) {
 							players.get(clientIndex - 1).raceBox.setSelectedItem(race);
 						}
 						ServerClientListener.this.updateAllClients();
 					}});
 				}
 			}
 			
 			private String readLine()
 			{
 				try {
 					return in.readLine();
 				} catch (IOException e) {
 					return null;
 				}
 			}
 		}
 	}
 
 	private class ServerConnection implements Runnable, ActionListener
 	{
 		private PrintWriter out;
 		private ObjectInputStream in;
 		private Socket socket;
 		
 		private String ipAddress;
 		private String clientName;
 		
 		private boolean running;
 		
 		public ServerConnection(Socket socket, String clientName)
 		{
 			this.socket = socket;
 			try {
 				in = new ObjectInputStream(socket.getInputStream());
 				out = new PrintWriter(socket.getOutputStream(), true);
 			} catch (IOException e) {
 				// problem getting IO for socket
 				e.printStackTrace();
 			}
 			
 			this.clientName = clientName;
 			// ip address is set externally by the startClient method above
 		}
 		
 		public void run()
 		{
 			running = true;
 			while (running)
 			{
 				Object input = null;
 				while ((input = readObject()) != null)
 				{
 					if (input instanceof String[][])
 					{
 						String[][] data = (String[][]) input;
 						final PlayerObject[] pArr = new PlayerObject[data.length];
 						for (int i = 0; i < data.length; ++i)
 						{
 							pArr[i] = new PlayerObject(data[i]);
 						}
 						updateGUI(pArr);
 					}
 					else if (input.equals("start"))
 					{
 						Game g = initializeGame();
 						synchronized (playerLock)
 						{
 							int i = 0;
 							for (; i < players.size() && !players.get(i).ipAddress.equals(ipAddress); ++i);
 							
							g.initializeClient(players.size(), ipAddress, i);
 						}
 						
 						running = false;
 						parent.dispose();
 						
 						g.run();
 					}
 					else if (input.equals("cancel"))
 					{
 						// TODO display a message
 						
 						running = false;
 						try {
 							socket.close();
 						} catch (IOException e) {}
 						
 						showMainMenu();
 					}
 				}
 			}
 		}
 		
 		public void actionPerformed(ActionEvent e)
 		{
 			Object src = e.getSource();
 			if (src instanceof JComboBox)
 			{
 				JComboBox b = (JComboBox) src;
 				out.println((String) b.getSelectedItem());
 			}
 			else if (src == cancelButton)
 			{
 				// TODO inform the host that this client is leaving the game
 				
 				try {
 					socket.close();
 				} catch (IOException e1) {}
 				
 				showMainMenu();
 			}
 		}
 		
 		private Object readObject()
 		{
 			try {
 				return in.readObject();
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 	
 		private void updateGUI(final PlayerObject[] pArr)
 		{
 			SwingUtilities.invokeLater(new Runnable() { public void run() {
 				synchronized (playerLock) {
 					clearPlayers();
 					for (int i = 0; i < pArr.length; ++i)
 					{
 						boolean enabled = (pArr[i].ipAddress.equals(ipAddress) && pArr[i].nameField.getText().equals(clientName));
 						if (enabled) pArr[i].raceBox.addActionListener(ServerConnection.this);
 						addPlayer(pArr[i], enabled);
 						validate();
 						players.add(pArr[i]);
 					}
 				}
 			}});
 		}
 	}
 	
 	private void initGUI() {
 		try {
 			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			addWindowListener(new CloseAdapter(parent));
 			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
 			getContentPane().setLayout(thisLayout);
 			this.setTitle("Game Lobby");
 			{
 				mapLabel = new JLabel();
 				mapLabel.setText("Map:");
 			}
 			{
 				mapField = new JTextField();
 				mapField.setEnabled(false);
 			}
 			{
 				tabbedPane = new JPanel();
 				GridBagLayout tabbedPaneLayout = new GridBagLayout();
 				tabbedPaneLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
 				tabbedPaneLayout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7};
 				tabbedPaneLayout.columnWeights = new double[] {0.02, 0.1, 0.01, 0.1};
 				tabbedPaneLayout.columnWidths = new int[] {7, 7, 7, 7};
 				tabbedPane.setLayout(tabbedPaneLayout);
 				{
 					numberLabel = new JLabel();
 					tabbedPane.add(numberLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
 					numberLabel.setText("#");
 				}
 				{
 					nameLabel = new JLabel();
 					tabbedPane.add(nameLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
 					nameLabel.setText("Name");
 				}
 				{
 					raceLabel = new JLabel();
 					tabbedPane.add(raceLabel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
 					raceLabel.setText("Race");
 				}
 			}
 			{
 				cancelButton = new JButton();
 				cancelButton.setText("Cancel");
 			}
 			{
 				startButton = new JButton();
 				startButton.setText("Start");
 			}
 			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				    .addComponent(mapField, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 				    .addComponent(mapLabel, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				    .addComponent(cancelButton, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 				    .addComponent(startButton, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
 				.addContainerGap());
 			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(thisLayout.createParallelGroup()
 				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
 				        .addComponent(mapLabel, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
 				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				        .addGroup(thisLayout.createParallelGroup()
 				            .addComponent(mapField, GroupLayout.Alignment.LEADING, 0, 387, Short.MAX_VALUE)
 				            .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
 				                .addGap(0, 198, Short.MAX_VALUE)
 				                .addComponent(startButton, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
 				                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 1, GroupLayout.PREFERRED_SIZE)
 				                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE))))
 				    .addComponent(tabbedPane, GroupLayout.Alignment.LEADING, 0, 443, Short.MAX_VALUE))
 				.addContainerGap());
 			thisLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {startButton, cancelButton});
 			pack();
 			this.setSize(475, 406);
 		} catch (Exception e) {
 		    //add your error handling code here
 			e.printStackTrace();
 		}
 	}
 
 }
