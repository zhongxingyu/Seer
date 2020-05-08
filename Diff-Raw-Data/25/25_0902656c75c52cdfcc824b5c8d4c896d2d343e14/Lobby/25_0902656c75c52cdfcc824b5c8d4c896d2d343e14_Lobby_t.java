 package pongLobby;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Vector;
 import java.util.Scanner;
 
 public class Lobby {
 
 	public static void main(String[] args) {
 		boolean addressGiven = false;
 		Lobby lobby=null;
 		InetAddress serverAddress= null;
 		int serverPort=0, localPort=0;
 		int nbParameter=0;
 		for(int i=0;i<args.length;i+=2)
 		{
 			if(args[i].equals("-h")){
 				try{
 					serverAddress = InetAddress.getByName(args[i+1]);
 				}
 				catch(UnknownHostException ex)
 				{
 					System.out.println("Addresse du serveur inconnue: " + ex);
 					System.exit(1);
 				}
 				addressGiven=true;
 			}
 			else if(args[i].equals("-lp")){
 				localPort=Integer.parseInt(args[i+1]);
 				nbParameter+=1;
 			}
 			else if(args[i].equals("-sp")){
 				serverPort=Integer.parseInt(args[i+1]);
 				nbParameter+=1;
 			}
 			else
 			{
 				System.out.println("Argument " + args[i] + " inconnu.");
 			}
 			
 		}
 		
 		if(!addressGiven || (nbParameter==1))
 		
 		{
 			System.out.println("Merci d'utiliser le logiciel comme suit:");
 			System.out.println("-h pour spécifier l'addresse ou le nom du serveur de jeu");
 			System.out.println("-lp pour spécifier le port du lobby");
 			System.out.println("-sp pour spécifier le port du serveur de jeu");			
 			System.out.println("merci de spécifier au moins l'addresse du serveur de jeu");
 		}
 		else{
 			if(nbParameter==2)
 				lobby = new Lobby(serverAddress, localPort, localPort-1, serverPort);
 			else
 				lobby = new Lobby(serverAddress);
 			lobby.startMulticastNetworkThread();
 		}
 			
 	}
 	
 	Lobby(InetAddress serverAddress){
 		set_state(LobbyState.INITIALISING);
 		set_playersVector(new Vector<Player>());
 		set_multicastPort(6665);
 		set_unicastPort(6664);
 		//_serverPort = 6666;
 		//_serverAddress= serverAddress;
 	}
 	
 	Lobby(InetAddress serverAddress, int unicastPort, int multicastPort, int serverPort)
 	{
 		set_state(LobbyState.INITIALISING);
 		set_playersVector(new Vector<Player>());
 		set_unicastPort(unicastPort);
 		set_multicastPort(multicastPort);
 		//_serverPort = serverPort;
 		//_serverAddress= serverAddress;
 	}
 	
 	public void startMulticastNetworkThread(){
		_multicastCommunicator = new MulticastCommunicator(this);
		_unicastListener = new UnicastListener(this);
		_multicastCommunicator.start();
		_unicastListener.start();
 		set_state(LobbyState.WAITING);
 		_displayMenu();
 	}
 	
 	public String displayPlayers(){
 		String string;
 		string = "Joueurs connectés à la partie:\n";
 		for(int i=0;i<get_playersVector().size();++i)
 			string += "Joueur n° " + i + " id: " + get_playersVector().elementAt(i).get_id() + "\n";
 		return string;
 	}
 	
 	public Player searchPlayer(long id)
 	{
 		for(int i=0; i<_playersVector.size(); i++)
 			if(_playersVector.elementAt(i).get_id() == id)
 				return _playersVector.elementAt(i);
 		return null;
 	}
 	
 	public Player searchPlayer(InetAddress ip)
 	{
 		for(int i=0; i<_playersVector.size(); i++)
 			if(_playersVector.elementAt(i).get_ipAddress().equals(ip))
 				return _playersVector.elementAt(i);
 		return null;
 	}
 	
 	public boolean isPlayerInVector(long id)
 	{
 		for(int i=0; i<_playersVector.size();i++)
 			if(_playersVector.elementAt(i).get_id()==id)
 				return true;
 		return false;
 	}
 
 	
 	//Methodes privées
 	/*******************/
 	private void _displayMenu()
 	{
 		Scanner sc = new Scanner(System.in);
 		String str = "";
 		while(get_state().equals(LobbyState.WAITING))
 		{
 			System.out.println("En écoute, " + get_playersVector().size() + " joueurs connectés.");
 			System.out.println("a: actualiser cette ligne.");
 			System.out.println("l: afficher la liste des joueurs.");
 			System.out.println("q: quitter sans lancer la partie.");
 			if(get_playersVector().size()>=2)
 				System.out.println("s: lancer la partie.");
 			str=sc.nextLine();
 			if(str.toLowerCase().equals("l"))
 				System.out.println(displayPlayers());
 			else if(str.toLowerCase().equals("q"))
 				set_state(LobbyState.STARTED);
 			else if(str.toLowerCase().equals("s") && get_playersVector().size()>=2)
 				set_state(LobbyState.READY_TO_START);
 		}
 		sc.close();
 		
 		while(get_state().equals(LobbyState.READY_TO_START))
 		{
 			System.out.println("Lancement du jeu.");
 		}
 		if(get_state().equals(LobbyState.STARTING))
 			System.out.println("Jeu lancé, fermeture du serveur du lobby.");
 		while(get_state().equals(LobbyState.STARTING))
 		{
 
 		}
 		if(get_state().equals(LobbyState.STARTED))
 		{
 			System.out.println("Lobby fermé.");
 			System.exit(0);
 		}
 	}
 	
 	//getters/setters
 	/******************/
 	public LobbyState get_state() {
 		return _state;
 	}
 
 	public void set_state(LobbyState _state) {
 		this._state = _state;
 	}
 
 
 	public Vector<Player> get_playersVector() {
 		return _playersVector;
 	}
 
 	public void set_playersVector(Vector<Player> _playersVector) {
 		this._playersVector = _playersVector;
 	}
 
 
 	public int get_multicastPort() {
 		return _multicastPort;
 	}
 
 	public void set_multicastPort(int _multicastPort) {
 		this._multicastPort = _multicastPort;
 	}
 
 
 	public int get_unicastPort() {
 		return _unicastPort;
 	}
 
 	public void set_unicastPort(int _unicastPort) {
 		this._unicastPort = _unicastPort;
 	}
 
 
 	private Vector<Player> _playersVector;
 	private LobbyState _state;
	private MulticastCommunicator _multicastCommunicator;
	private UnicastListener _unicastListener;
 	private int _multicastPort;
 	private int _unicastPort;
 	//private int _serverPort;
 	//private InetAddress _serverAddress;
 
 }
