 package monopoly;
 
 import gui.GameFrame;
 import java.net.Socket;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Scanner;
 
 import multiplayer.PlayerMP;
 import multiplayer.packet.Packet03Username;
 import multiplayer.packet.Packet04Colorcode;
 
 public class Game
 {
 	/* GUI Data */
 	public String myPlayer;
 	public GameFrame gframe;
 	
 	/* Game Data */
 	private int[] dice;
 	public Turn turn;
 	
 	/* MP Data */
	private byte myId = 127;
 	PlayerMP[] players;
 	public byte playerCount = 127;
 	public byte gotPlayers = 1;
 	
 	/* Pass by Reference */
 	private static Game game;
 	public static Game Monopoly() { return game; }
 	
 	public PlayerMP getPlayer() { return players[myId]; }
 	public byte getMyID() { return myId; }
	public void setMyID(byte b) { myId = b; }
 	
 	public static void main(String[] args)
 	{
 		game = new Game();
 		game.StartGame();
 	}
 	
 	private Game()
 	{	
 	}
 	
 	private void StartGame()
 	{
 		Log("OpenMMP, Open Source Monopoly Multiplayer!");
 		Log("OpenMMP V0.1 Alpha");
 		Log("Made by Johnnei");
 		Log("Preparing OpenMMP");
 		init();
 		System.out.print("Username: ");
 		String username = readInput();
 		myPlayer = username;
 		System.out.print("Color Code: 0x");
 		String code = readInput();
 		int colorCode = Integer.valueOf(code, 16);
 		System.out.print("Server IP: ");
 		String ip = readInput();
 		Log("Connecting to Server...");
 		try
 		{
 			Socket pSocket = new Socket(ip, 27960);
 			PlayerMP socket = new PlayerMP(pSocket);
 			socket.setUsername(username);
 			socket.setColor(colorCode);
 			Log("Waiting for ID...");
			while(myId == 127)
 			{
 				Thread.sleep(100);
 			}
			socket.setId(myId);
 			players[socket.getId()] = socket;
 			Log("Sending Player Data...");
 			getPlayer().addPacket(new Packet03Username(username, socket.getId()));
 			getPlayer().addPacket(new Packet04Colorcode(colorCode, socket.getId()));
 			Log("Waiting for Server to start the game!");
 			
 			while(playerCount != gotPlayers)
 			{
 				//Idle to keep connection, just for testing atm
 				Thread.sleep(100);
 			}
 		} catch (Exception e)
 		{
 			Log("Failed!");
 			e.printStackTrace();
 		}
 	}
 	
 	private void init()
 	{
 		players = new PlayerMP[6];
 		dice = new int[] { 1, 1};
 	}
 	
 	private static String readInput()
 	{
 		Scanner input = new Scanner(System.in);
 		return input.nextLine();
 	}
 	
 	public static void Log(String line)
 	{
 		System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "] " + line);
 	}
 	
 	/* Public Getters / Setters */
 
 	public Player[] getPlayers()
 	{
 		return players;
 	}
 	
 	public void registerPlayer(PlayerMP player)
 	{
 		players[player.getId()] = player;
 		++gotPlayers;
 	}
 	
 	public int getDice(int i)
 	{
 		return dice[i];
 	}
 
 	public int diceEyesCount()
 	{
 		return dice[0] + dice[1];
 	}
 }
