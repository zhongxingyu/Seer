 package btlshp.turns;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Serializable;
 
 import btlshp.entities.Base;
 import btlshp.entities.ConstructBlock;
 import btlshp.entities.Location;
 import btlshp.enums.Direction;
 import btlshp.entities.Ship;
 import btlshp.entities.Map;
 import java.io.*;
 public interface Turn {
 	/**
 	 * @returns true if the move object represents a successful move, false otherwise.
 	 */
 	boolean wasSuccessful();
 
 	/**
 	 * Executes a given move object representing a move from the other player.
 	 * @throws IllegalStateException If the turn was not successful.
 	 */
 	void executeTurn();
 }
 
 class Pass implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3359745261868508357L;
 	@Override
 	public void executeTurn() {//Does no work
 	}
 	@Override
 	public boolean wasSuccessful() {
 		//Always returns true because a pass turn cannot fail
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "pass";
 	}
 }
 class RequestPostponeGame implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2537600636201002718L;
 	@Override
 	/**
 	 * Opponent would like to PostPone Game
 	 */
 	public void executeTurn() {
 		// TODO Implementation UI dependent
 		//Generate a dialog box with player and allow player to accept or reject
 	}
 	@Override
 	public boolean wasSuccessful() {
 		// TODO Implementation UI dependent
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "requestPostponeGame";
 	}
 }
 class ConfirmPostponeGame implements Turn, Serializable{
 
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7601913526703183133L;
 	@Override
 	/**
 	 * Opponent accepted postponing game
 	 */
 	public void executeTurn() {
 		// TODO Implementation UI dependent
 		
 	}
 
 
 	@Override
 	public boolean wasSuccessful() {
 		// TODO Implementation UI dependent
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "confirmPostponeGame";
 	}
 }
 class LoadGameState implements Turn, Serializable{
 
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4870988534077315987L;
 	private String filePath;
 	/**
 	 * 
 	 * @param f location of saved game file
 	 */
 	public LoadGameState(String f){
 		filePath = f;
 	}
 	@Override
 	public void executeTurn() {
 			FileInputStream fileIn = null;
 			ObjectInputStream objIn = null;
 			try{
 				fileIn = new FileInputStream(filePath);
 				objIn = new ObjectInputStream(fileIn);
 				fileIn.close();
 			}catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 			
 	}
 
 
 	@Override
 	public boolean wasSuccessful() {
 		// TODO Implementation IO dependent
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "loadGameState";
 	}
 }
 class SaveGameState implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2354631655310067958L;
 	private Map saveGame;
 	
 	public SaveGameState(Map map)
 	{
 		saveGame = map;
 	}
 	@Override
 	public void executeTurn()  {
 		// TODO Auto-generated method stub
 		ObjectOutputStream objOut = null;
 		FileOutputStream fileOut = null;
 		try {
			fileOut = new FileOutputStream("..Dropbox/Btlshp/game.dat");
 			objOut = new ObjectOutputStream(fileOut);
 			
 			// uncertain what object is needed to save
 			objOut.writeObject(saveGame);
 			objOut.close();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public boolean wasSuccessful() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "saveGameState";
 	}
 	
 }
 class RequestSurrender implements Turn, Serializable{
 
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4395558489216020334L;
 	@Override
 	/**
 	 * Opponent requests to quit(surrender) game
 	 */
 	public void executeTurn() {
 		// TODO Implementation UI dependent
 		//Generate dialog for the player to accept or reject surrender
 		
 	}
 
 
 	@Override
 	public boolean wasSuccessful() {
 		// TODO Implementation UI dependent
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "requestSurrender";
 	}
 }
 class AcceptSurrender implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8163598235191879797L;
 	@Override
 	/**
 	 * Opponent has accepted request to surrender
 	 */
 	public void executeTurn() {
 		// TODO Implementation UI dependent
 		//Generate dialog informing player opponent has accepted surrender
 		
 	}
 	@Override
 	public boolean wasSuccessful() {
 		// TODO Implementation UI dependent
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "acceptSurrender";
 	}
 }
 class MoveShip implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4599021282070269467L;
 	private Ship s;
 	private Direction dir;
 	private int distance;
 	private Map m;
 	private boolean success = false;
 
 	public MoveShip(Map m, Ship s, Direction dir, int distance) {
 		this.m = m;
 		this.s = s;
 		this.dir = dir;
 		this.distance = distance;
 	}
 
 	@Override
 	public void executeTurn() {
 		try{
 			m.move(s, dir, distance);
 			success = true;
 		}catch(IllegalStateException e){
 			success = false;
 		}
 		
 	}
 
 	@Override
 	public boolean wasSuccessful() {
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "moveShip";
 	}
 }
 class PlaceMine implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4336837927576289067L;
 	private Map m;
 	private Location loc;
 	private Ship s;
 	private boolean success = false;
 	public PlaceMine(Map m, Ship s, Location loc) {
 		this.m = m;
 		this.loc = loc;
 		this.s = s;
 	}
 
 	@Override
 	public void executeTurn() {
 		try{
 			m.placeMine(s, loc);
 			success = true;
 		}catch(IllegalStateException e){
 			success = false;
 		}
 	}
 
 
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	@Override
 	public String toString(){
 		return "placeMine";
 	}
 }
 class TakeMine implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1991458862311470623L;
 	private Location loc;
 	private Ship s;
 	private Map m;
 	private boolean success = false;
 
 	public TakeMine(Map m, Ship s, Location loc) {
 		this.s = s;
 		this.loc = loc;
 		this.m = m;
 	}
 
 	@Override
 	public void executeTurn() {
 		try{
 			m.pickupMine(s, loc);
 			success = true;
 		}catch(IllegalStateException e){
 			success = false;
 		}
 	}
 
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	@Override
 	public String toString(){
 		return "takeMine";
 	}
 }
 class LaunchTorpedo implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1790493199271040630L;
 	private Map m;
 	private Ship s;
 	private boolean success = false;
 
 
 	LaunchTorpedo(Map m2, Ship s2) {
 		this.m = m2;
 		this.s = s2;
 	}
 
 	@Override
 	public void executeTurn() {
 		try{
 			m.fireTorpedo(s);
 			success = true;
 		}catch(IllegalStateException e){
 			success = false;
 		}
 	}
 
 
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	@Override
 	public String toString(){
 		return "launchTorpedo";
 	}
 }
 
 class Shoot implements Turn, Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -605750640559980738L;
 	private Map m;
 	private Ship s;
 	private Location loc;
 	private boolean success = false;
 
 	public Shoot(Map m, Ship s, Location loc) {
 		this.m = m;
 		this.s = s;
 		this.loc = loc;
 	}
 
 	@Override
 	public void executeTurn() {
 		try{
 			m.fireGuns(s, loc);
 			success = true;
 		}catch(IllegalStateException e){
 			success = false;
 		}
 	}
 
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	@Override
 	public String toString(){
 		return "shoot";
 	}
 }
 class RepairBase implements Turn, Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3486672126675014858L;
 	private ConstructBlock repairBlock;
 	private Base b;
 	private boolean success = false;
 
 	RepairBase(Base b, ConstructBlock repairBlock) {
 		this.b = b;
 		this.repairBlock = repairBlock;
 	}
 
 	@Override
 	public void executeTurn() {
 		b.AssesRepair(repairBlock);
 		success = true;
 	}
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	@Override
 	public String toString(){
 		return "repairBase";
 	}
 }
 class RepairShip implements Turn, Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 89817140305258661L;
 	private Ship s;
 	private ConstructBlock repairBlock;
 	private boolean success = false;
 
 	RepairShip(Ship s, ConstructBlock repairBlock) {
 		this.s = s;
 		this.repairBlock = repairBlock;
 	}
 
 	@Override
 	public void executeTurn() {
 		s.AssesRepair(repairBlock);
 		success = true;
 	}
 
 	
 
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	
 	@Override
 	public String toString(){
 		return "repairShip";
 	}
 }
