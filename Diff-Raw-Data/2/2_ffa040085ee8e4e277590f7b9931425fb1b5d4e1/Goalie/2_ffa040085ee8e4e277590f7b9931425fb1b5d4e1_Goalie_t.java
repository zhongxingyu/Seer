 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 public class Goalie extends Player {
 	protected int wins; //Number of wins
 	protected int loses; //Number of losses
 	protected int winsP; //Number of wins during playoffs
 	protected int losesP; //Number of loses during playoffs
 	protected int losesOT; //Number of loses during overtime
 	protected int gameStart; //The number of games started by goalie
 	protected int goalA; //Number of goals scored against goalie
 	protected int shotsOG; //Total number of shots faced by goalie
 	protected int saves; //Number of saves by goalie
 	protected double savePercent; //Percentage of total shots faced that were saved (dividing the number of saves by the total number of shots on goal), calculated
 	protected int shutouts; //Number of games where all the goals were blocked by the goalie and the goalie was playing for the entire duration of the game
 	protected int emptyNG; //Number of goals scored against while off ice for extra attack player
 	
 	//for our scoring system, we can have the main ones (ex. savepercent, etc. decide on bulk of rating
 	//then things like emptyNG and shutouts that can imply good decision-making/other factors can be like an "AIF"
 	
 	/**
    	* Constructs an object of type Goalie
    	* @param fname, lname of type string and gender of type boolean and height, weight of type double and month, day, year, place, mStatus of type int
    	* salaryP of type double, contractR of type int, tSalary of type double, rating of type int, gp of type int, penaltyT of type double, penaltyN of type int, arm of type boolean, number of type int,
    	* numMin of type double, rookie of type boolean,wins of type int, loses of type int, winsP of type int, losesP of type int, losesOT of type int, gameStart of type int, goalA of type int, shotsOG of type int, saves of type int, shutouts of type int, emptyNG of type int
    	* @throws IOException
    	*/
 	public Goalie(String fname, String lname, boolean gender, double height, double weight, int month, int day, int year, int place, int mStatus,//person fields
 			double salaryPY, int contractR, double tSalary, int rating, int gp, double penaltyT, int penaltyN, boolean arm, int number, //player fields
 			double numMin, boolean rookie, //more player fields
 			int wins, int loses, int winsP, int losesP, int losesOT, int gameStart, int goalA, int shotsOG, int saves, int shutouts, int emptyNG) { //goalie fields
 		
 		super(fname, lname, gender, height, weight, month, day, year, place, mStatus,//person fields
 				salaryPY, contractR, tSalary, gp, penaltyT, penaltyN, arm, number, //player fields
 				numMin, rookie); //more player fields
 		
 		this.wins = wins;
 		this.loses = loses;
 		this.winsP = winsP;
 		this.losesP = losesP;
 		this.losesOT = losesOT;
 		this.gameStart = gameStart;
 		this.numMin = numMin;
 		this.goalA = goalA;
 		this.shotsOG = shotsOG;
 		this.saves = saves;
 		this.shutouts = shutouts;
 		this.emptyNG = emptyNG;
 		
 		updateSavePercent();
 	}
 	
 	/**
 	* Constructs an object of type Goalie by calling the load method
    	* @throws IOException
 	*/
 	public Goalie(BufferedReader br) throws IOException {
 		super(br);
 		loadgoalie(br);
 	}
 	
 	/**
    	* Constructs an object of type Goalie by prompting for each field
 	* @throws IOException
 	*/
 	public Goalie() {
 		super();
 		prompt();
 	}
 	
 	/**
 	 * Loads goalie data from a text file into this object
 	 * @author MK, AV, CH, PJ
 	 * @throws IOException 
 	 * @overrides load in Player
 	 */
 	public void loadgoalie( BufferedReader br) throws IOException {
 		//load goalie fields into object
 		String x;
 		
 		x = br.readLine();
 		wins = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		loses = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		winsP = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		losesP = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		losesOT = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		gameStart = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		goalA = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		shotsOG = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		saves = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		savePercent = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		shutouts = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		emptyNG = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 	}
 	
 	/**
 	 * Saves all statistics of goalie onto a text file
 	 * @param pw of type PrintWriter
 	 * @overrides save in Player
 	 */
 	public void save(PrintWriter pw) {
 		super.save(pw);
 		//Writes all goalie fields onto text file
 		pw.println("Number of wins: "+wins);
 		pw.println("Numer of losses: "+loses);
 		pw.println("Number of wins during playoffs: "+winsP);
 		pw.println("Number of losses during playoffs: "+losesP);
 		pw.println("Number of losses during overtime: "+losesOT);
		pw.println("Number of games started by goalie: "+gameStart);
 		pw.println("Number of goals scored against goalie: "+goalA);
 		pw.println("Total number of shots faced by goalie: "+shotsOG);
 		pw.println("Number of saves by goalie: "+saves);
 		pw.println("Save Percentage: "+savePercent);
 		pw.println("Shutout Saves: "+shutouts);
 		pw.println("Empty Net Goals: "+emptyNG);
 	}
 	
 	/**
 	 * Loads a goalie's data through prompting the user
 	 * @author MK, AV, CH, PJ
 	 * @overrides prompt in Player
 	 */
 	public void prompt() {
 		Scanner in = new Scanner(System.in);
 		do {
 			System.out.print("Enter the number of times "+fname+" has won: ");
 			wins=in.nextInt();
 		} while (wins<0); //Ensures the number of wins is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" has lost: ");
 			loses=in.nextInt();
 		} while(loses<0); //Ensures the number of loses is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" has won during playoffs: ");
 			winsP=in.nextInt();
 		} while(winsP<0); //Ensures the number of wins during playoffs is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" has lost during playoffs: ");
 			losesP=in.nextInt();
 		} while(losesP<0); //Ensures the number of loses during playoffs is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" has lost during overtime: ");
 			losesOT=in.nextInt();
 		} while(losesOT<0); //Ensures the number of loses during overtime is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" has started at the beginning of a game: ");
 			gameStart=in.nextInt();
 		} while(gameStart<0); //Ensures the number of games started is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" got scored on: ");
 			goalA=in.nextInt();
 		} while(goalA<0); //Ensures the number of goals scored against is above zero (valid)
 		
 		do {
 			System.out.print("Enter the total number of times "+fname+" was shot on (shots on goal): ");
 			shotsOG=in.nextInt();
 		} while(shotsOG<0); //Ensures the total number of shots on goal is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" made a save: ");
 			saves=in.nextInt();
 		} while(saves<0); //Ensures the number of saves goalie made is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" was never scored against and played for the entire game: ");
 			shutouts=in.nextInt();
 		} while(shutouts<0); //Ensures the number of shutouts is above zero (valid)
 		
 		do {
 			System.out.print("Enter the number of times "+fname+" was scored on while off the ice to add an extra attack player: ");
 			emptyNG=in.nextInt();
 		} while(emptyNG<0); //Ensures the the number of empty net goals is above zero (valid)
 		in.close();
 	}
 	
 	//Accessor Methods
 	/**
 	 * Returns number of games won
 	 * @return wins
 	 */
 	public int getWins(){
 		return wins;
 	}
 	
 	/**
 	 * Returns number of games losses
 	 * @return loses
 	 */
 	public int getLoses(){
 		return loses;
 	}
 	
 	/**
 	 * Returns number of games won during playoffs
 	 * @return winsP
 	 */
 	public int getWinsP(){
 		return winsP;
 	}
 	
 	/**
 	 * Returns number of games loses during playoffs
 	 * @return losesP
 	 */
 	public int getLosesP(){
 		return losesP;
 	}
 	
 	/**
 	 * Returns number of games loses during overtime
 	 * @return losesOT
 	 */
 	public int getLosesOT(){
 		return losesOT;
 	}
 	
 	/**
 	 * Returns number of games started
 	 * @return gameStart
 	 */
 	public int getGameStart(){
 		return gameStart;
 	}
 	
 	/**
 	 * Returns number of goals scored against the goalie
 	 * @return goalAA
 	 */
 	public int getGoalA(){
 		return goalA;
 	}
 	
 	/**
 	 * Returns total number of shots on goal the goalie faced
 	 * @return shotsOG
 	 */
 	public int getShotsOG(){
 		return shotsOG;
 	}
 	
 	/**
 	 * Returns number of saves goalie has made
 	 * @return saves
 	 */
 	public int getSaves(){
 		return saves;
 	}
 	
 	/**
 	 * Returns percentage of total shots faced the goalie saved
 	 * @return savePercent
 	 */
 	public double getSavePercent(){
 		updateSavePercent();
 		return savePercent;
 	}
 	
 	/**
 	 * Returns number of games where goalie had no goals against him and only goalie to play in game 
 	 * @return shutouts
 	 */
 	public int getShutouts(){
 		return shutouts;
 	}
 	
 	/**
 	 * Returns number of goals scored against while off ice for extra attack player
 	 * @return emptyNG
 	 */
 	public int getEmptyNG(){
 		return emptyNG;
 	}
 	
 	//Mutator Methods
 	/**
 	 * Stores parameter to wins
 	 * @param x of type int
 	 */
 	public void putWins(int x){
 		wins=x;
 	}
 	
 	/**
 	 * Stores parameter to loses
 	 * @param x of type int
 	 */
 	public void putLoses(int x){
 		loses=x;
 	}
 	
 	/**
 	 * Stores parameter to winsP
 	 * @param x of type int
 	 */
 	public void putWinsP(int x){
 		winsP=x;
 	}
 	
 	/**
 	 * Stores parameter to losesP
 	 * @param x of type int
 	 */
 	public void putLosesP(int x){
 		losesP=x;
 	}
 	
 	/**
 	 * Stores parameter to losesOT
 	 * @param x of type int
 	 */
 	public void putLosesOT(int x){
 		losesOT=x;
 	}
 	
 	/**
 	 * Stores parameter to gameStart
 	 * @param x of type int
 	 */
 	public void putGameStart(int x){
 		gameStart=x;
 	}
 	
 	/**
 	 * Stores parameter to goalA
 	 * @param x of type int
 	 */
 	public void putGoalA(int x){
 		goalA=x;
 	}
 	
 	/**
 	 * Stores parameter to shotsOG
 	 * @param x of type int
 	 */
 	public void putShotsOG(int x){
 		shotsOG=x;
 	}
 	
 	/**
 	 * Stores parameter to saves
 	 * @param x of type int
 	 */
 	public void putSaves(int x){
 		saves=x;
 	}
 	
 	/**
 	 * Stores parameter to shutouts
 	 * @param x of type int
 	 */
 	public void putShutouts(int x){
 		shutouts=x;
 	}
 	
 	/**
 	 * Stores parameter to emptyNG
 	 * @param x of type int
 	 */
 	public void putEmptyNG(int x){
 		emptyNG=x;
 	}
 	
 	//Update Methods
 	/**
 	 * Stores parameter to savePercent
 	 * @param x of type double
 	 */
 	public void updateSavePercent(){
 		savePercent=goalA/shotsOG;
 	}
 
 	/**
 	 * calculates the rating of a goalie
 	 */
 	public void calculateRating() {
 		// TODO Auto-generated method stub
 		
 	}
 }
