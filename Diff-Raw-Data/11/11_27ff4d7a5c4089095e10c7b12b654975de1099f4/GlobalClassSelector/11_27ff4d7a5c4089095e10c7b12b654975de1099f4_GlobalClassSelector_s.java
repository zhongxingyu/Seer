 package Control;
 
 import java.util.ArrayList;
 
 import sshaclient.Constants;
 import sshaclient.SocketClient;
 
 import Model.Player;
 import Model.Arenas.Arena;
 import Model.Classes.ClassHunter;
 import Model.Classes.ClassWarrior;
 import Model.Classes.ClassWizard;
 
 public class GlobalClassSelector {
 
 	private static final int nbrOfPlayers = 4;
 	
 	private boolean isMultiplayer = false;
 	private static GlobalClassSelector myControl = null;
 	private Player[] players = new Player[nbrOfPlayers];
 	private PlayerControl[] playerControllers = new PlayerControl[nbrOfPlayers];
 	private Thread[] controllerThreads = new Thread[nbrOfPlayers];
 	private SocketClient socketClient;
 	private Thread socketThread;
 	private boolean changedPlayer = false;
 	private int activePlayer = 0;
 	private Arena mapSelected;
 	private int difficultySelected;
	private String playerName = "UnclePlayful";
 	
 	
 	//Singleton
 	public static GlobalClassSelector getController() {
 	if (myControl == null) {
 	   myControl = new GlobalClassSelector();
 	   // prepare myController here or use setter() methods or a parameterized constructor
 	   }
 	   return myControl;
 	}
 
 	// make constructor private so no one except the getController() can call it
 	private GlobalClassSelector() {
 		players[0] = new ClassHunter(getActivePlayerName(), "player", 120, 100, 0);
 		
 		socketClient = new SocketClient(Constants.hostName, Constants.defaultPort, players[0]);
 		socketThread = new Thread(socketClient);
 		socketThread.start();
 		
 	}
 	
 	public synchronized void addPlayer(Player player, int index){
 		players[index] = player;
 	}
 	public synchronized void removePlayer(int index) {
 		players[index] = null;
 	}
 	public synchronized void resetPlayers(){
 		players = new Player[nbrOfPlayers];
 	}
 	public synchronized Player[] getPlayers(){
 		return players;
 	}
 	public synchronized Player getPlayer(int index) {
 		return players[index];
 	}
 	public synchronized void addPlayerController(PlayerControl pc, int index) {
 		playerControllers[index] = pc;
 	}
 	public synchronized void removePlayerController(int index) {
 		playerControllers[index] = null;
 	}
 	public synchronized void resetPlayerControllers() {
 		playerControllers = new PlayerControl[nbrOfPlayers];
 	}
 	public synchronized PlayerControl[] getPlayerControllers() {
 		return playerControllers;
 	}
 	public synchronized PlayerControl getPlayerControl(int index) {
 		return playerControllers[index];
 	}
 	public synchronized void addControllerThread(Thread thread, int index) {
 		controllerThreads[index] = thread;
 	}
 	public synchronized void removeControllerThread(int index) {
 		controllerThreads[index] = null;
 	}
 	public synchronized void resetControllerThreads() {
 		controllerThreads = new Thread[nbrOfPlayers];
 	}
 	public synchronized Thread[] getControllerThreads() {
 		return controllerThreads;
 	}
 	public synchronized Thread getControllerThread( int index) {
 		return controllerThreads[index];
 	}
 	
 	public String getActivePlayerName(){
 		return playerName;
 	}
 	public void setActivePlayerName(String name){
 		playerName = name;
 	}
 	public int getActivePlayerIndex(){
 		return activePlayer;
 	}
 	public void setActivePlayerIndex(int index){
 		activePlayer = index;
 	}
 	public boolean getSingleOrMulti(){
 		return isMultiplayer;
 	}
 	public void setSingleOrMulti(boolean singleOrMulti){
 		isMultiplayer = singleOrMulti;
 	}
 	public SocketClient getSocketClient() {
 		return socketClient;
 	}
 	public Arena getMapSelected(){
 		return mapSelected;
 	}
 	public void setMapSelected(Arena map){
 		mapSelected = map;
 	}
 	public int getDiffcultySelected(){
 		return difficultySelected;
 	}
 	public void setDifficultySelected(int dif){
 		difficultySelected= dif;
 	}
 }
