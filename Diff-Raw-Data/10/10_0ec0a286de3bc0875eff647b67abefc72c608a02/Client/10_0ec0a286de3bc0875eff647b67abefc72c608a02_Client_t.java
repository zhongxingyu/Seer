 package menu.networking;
 
 import java.awt.Color;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import linewars.gamestate.MapConfiguration;
 import linewars.gamestate.Race;
 import linewars.init.Game;
 import linewars.init.PlayerData;
 import menu.ContentProvider;
 import menu.panels.CreateGamePanel;
 
 public class Client implements Runnable
 {
 	private Socket socket;
 	private ObjectOutputStream out;
 	private ObjectInputStream in;
 	private int playerIndex;
 	private CreateGamePanel gamePanel;
 	private boolean running;
 	
 	public Client(int port, String serverIp, CreateGamePanel gamePanel) throws SocketException
 	{
 		this.gamePanel = gamePanel;
 		try {
 			socket = new Socket(serverIp, port);
 			out = new ObjectOutputStream(socket.getOutputStream());
 			in = new ObjectInputStream(socket.getInputStream());
 		} catch (Exception e) {
 			throw new SocketException();
 		}
 		
 		playerIndex = (Integer) NetworkUtil.readObject(in);
 		PlayerBean[] pbs = (PlayerBean[]) NetworkUtil.readObject(in);
 		
 		boolean isReplay = (Boolean) NetworkUtil.readObject(in);
 		gamePanel.setReplay(isReplay);
 		gamePanel.setSelection(NetworkUtil.readObject(in));
 		
 		for (int i = 0; i < pbs.length; ++i)
 		{
 			gamePanel.addPlayer((i == playerIndex && !isReplay));
 			gamePanel.updatePlayerPanel(i, pbs[i]);
 		}
 	}
 	
 	public int getPlayerIndex()
 	{
 		return playerIndex;
 	}
 	
 	public void start()
 	{
 		// starts the client in its own thread
 		Thread th = new Thread(this);
 		th.setDaemon(true);
 		th.setName("Client");
 		th.start();
 	}
 	
 	public void sendMessage(MessageType type, Object ... obj)
 	{
 		NetworkUtil.writeObject(out, type);
 		for (Object o : obj)
 		{
 			NetworkUtil.writeObject(out, o);
 		}
 	}
 
 	@Override
 	public void run()
 	{
 		running = true;
 		while (running)
 		{
 			MessageType type = (MessageType) NetworkUtil.readObject(in);
 			if (type != null)
 				handleMessage(type);
 		}
 	}
 	
 	private void handleMessage(MessageType type)
 	{
 		int playerId = (Integer) NetworkUtil.readObject(in);
 		switch (type)
 		{
 		case name:
 			gamePanel.setPlayerName(playerId, (String) NetworkUtil.readObject(in));
 			break;
 		case slot:
 			gamePanel.setPlayerSlot(playerId, (Integer) NetworkUtil.readObject(in));
 			break;
 		case race:
 			gamePanel.setPlayerRace(playerId, (Integer) NetworkUtil.readObject(in));
 			break;
 		case color:
 			gamePanel.setPlayerColor(playerId, (Color) NetworkUtil.readObject(in));
 			break;	
 		case chat:
 			gamePanel.updateChat((String) NetworkUtil.readObject(in));
 			break;
 		case isReplay:
 			gamePanel.setReplay((Boolean) NetworkUtil.readObject(in));
 			break;
 		case selection:
 			gamePanel.setSelection(NetworkUtil.readObject(in));
 			gamePanel.repaint();
 			break;
 		case playerJoin:
 			if (playerIndex != playerId) {
 				gamePanel.addPlayer(false);
 				gamePanel.updatePlayerPanel(playerId, (PlayerBean) NetworkUtil.readObject(in));
 			}
 			break;
 		case clientCancelGame:
 			if (playerId == playerIndex)
 				running = false;
 			else
 				gamePanel.removePlayer(playerId);
 			
 			if (playerId < playerIndex) playerIndex--;
 			break;
 		case serverCancelGame:
 			running = false;
 			if (socket.getInetAddress().getHostAddress().toString().equals("127.0.0.1") == false)
 				JOptionPane.showMessageDialog(gamePanel, "The host left the game.");
 			
 			gamePanel.goBackToTitleMenu();
 			break;
 		case startGame:
 			System.out.println("The game is starting!");
 			
 			boolean isReplay = (Boolean) NetworkUtil.readObject(in);
 			Object selection = NetworkUtil.readObject(in);
 			PlayerBean[] players = (PlayerBean[]) NetworkUtil.readObject(in);
 			
 			String[] ipAddresses = (String[]) NetworkUtil.readObject(in);
 			boolean isObserver = false;  // TODO implement observing
 			
 			List<PlayerData> playerList = convertToPlayerData(players);
 			List<String> clientList = new ArrayList<String>();
 			for (int i = 0; i < ipAddresses.length; ++i) clientList.add(ipAddresses[i]);
 			
 			if (isReplay)
 			{
 				// TODO implement
 			}
 			else
 			{
 				MapConfiguration map = (MapConfiguration) selection;
 				String serverIp = socket.getInetAddress().getHostAddress();
 				
 				Game g = new Game(map, playerList);
 				if (playerId == 0) 
 					g.initializeServer(clientList);
 				g.initializeClient(serverIp, playerId, isObserver);
 				g.run();
 			}
 			
 			// TODO close the lobby system
 			
 			break;
 		}
 	}
 	
 	private List<PlayerData> convertToPlayerData(PlayerBean[] players)
 	{
 		List<PlayerData> playerList = new ArrayList<PlayerData>();
 		Race[] races = ContentProvider.getAvailableRaces();
 		for (int i = 0; i < players.length; ++i)
 		{
 			PlayerData pd = new PlayerData();
 			pd.setColor(players[i].getColor());
 			pd.setName(players[i].getName());
 			pd.setRace(races[players[i].getRaceIndex()]);
 			pd.setStartingSlot(players[i].getSlot());
 			playerList.add(pd);
 		}
 		return playerList;
 	}
 }
