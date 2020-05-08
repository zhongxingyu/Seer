 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 public class Team {
 	private String name; //Name of team
 	/*
       1. New Jersey Devils
       2. New York Islanders
       3. New York Rangers
       4. Philadelphia Flyers
       5. Pittsburgh Penguins
       6. Boston Bruins
       7. Buffalo Sabres
       8. Montreal Canadiens
       9. Ottawa Senators
       10. Toronto Maple Leafs
       11. Carolina Hurricanes
       12. Florida Panthers
       13. Tampa Bay Lightning
       14. Washington Capitals
       15. Winnipeg Jets
       16. Chicago Blackhawks
       17. Columbus Blue Jackets
       18. Detroit Red Wings
       19. Nashville Predators
       20. St. Louis Blues
       21. Calgary Flames
       22. Edmonton Oilers
       23. Colorado Avalanche
       24. Minnesota Wild
       25. Vancouver Canucks
       26. Anaheim Ducks
       27. Dallas Stars
       28. Los Angeles Kings
       29. Phoenix Coyotes
       30. San Jose Sharks
 	 */
 	private String division; // that the division belongs to
 	private String conference; // that the conference belongs to
 	private Coach [] coachingstaff;  // coaches, assistants, trainers, etc.
 	private Player [] players;  // a structure that will contain the maximum number of roster spots allowed for a team
 	private double payroll;   //how much the team is paying its players currently for the season (in millions)
 	private final double salarycap = 64.3;   // put by the league. It is the same for all teams (in millions)
 	//Team stats
 	private int gp; //games played
 	private int wins; //games won
 	private int losses; //games lost
 	private int goalsfor; //total goals scored (including power-play, short-handed and empty-net goals)
 	private int goalsagainst; //total goals scored against (including power-play, short-handed and empty-net goals)
 	private int ppo; //number of power play opportunities
 	private int tsh; //number of times short handed
 	private int ppgoals; //total number of powerplay goals
 	private int shgoals; //total number of shorthanded goals
 	private int ppgoalsA; //total number of powerplay goals against
 	private int shgoalsA; //total number of shorthanded goals against
 	private int shotsfor; //total shots
 	private int shotsagainst; //total shots against
 	private int saves; //total saves
 	private double avgGPG; //average goals per game scored, calculated
 	private double avgGAPG; //average goals per game against, calculated
 	private double avgSPG; //average shots per game, calculated
 	private double avgSAPG; //average shots per game against, calculated
 	private double shPercent; //The total goals scored divided by the total number of shots taken, calculated
 	private double savePercent; //The total saves divided by the total shots faced, calculated
 	private double ppPercent; //Power play percent: Power-play goals divided by power-play opportunites or power-play goals allowed divided by times short-handed, calculated
 
 	public Team(String name, double payroll, int gpT,
 			int wins, int losses, int goalsfor, int goalsagainst, int ppo,
 			int tsh, int ppgoals, int shgoals, int ppgoalsA, int shgoalsA,
 			int shotsfor, int shotsagainst, int saves) {
 		//super(); what's this for?
 		try{
 			putName (Integer.parseInt(name));
 		}catch(NumberFormatException e){
 			this.name = name;
 		}
 		division = getDivision();
 		conference = getConference();
 		this.payroll = payroll;
 		this.gp = gpT;
 		this.wins = wins;
 		this.losses = losses;
 		this.goalsfor = goalsfor;
 		this.goalsagainst = goalsagainst;
 		this.ppo = ppo;
 		this.tsh = tsh;
 		this.ppgoals = ppgoals;
 		this.shgoals = shgoals;
 		this.ppgoalsA = ppgoalsA;
 		this.shgoalsA = shgoalsA;
 		this.shotsfor = shotsfor;
 		this.shotsagainst = shotsagainst;
 		this.saves = saves;
 	}
 
 	public Team(BufferedReader br) throws IOException {
 		load(br);
 	}
 	
 	public Team() {
 		prompt();
 	}
 	/**
 	 * Loads the team data from a text file to this object
 	 * @author MK, AV, CH, PJ
 	 * @throws IOException 
 	 */
 	public void load(BufferedReader br) throws IOException {
 		String x;
 
 		x = br.readLine();//skips first line of text file
 		x = br.readLine();//skips second line of text file
 
 		x = br.readLine();
 		name = x.substring(x.indexOf(": ")+2,x.length());
 
 		x = br.readLine();
 		division = x.substring(x.indexOf(": ")+2,x.length());
 
 		x = br.readLine();
 		conference = x.substring(x.indexOf(": ")+2,x.length());
 
 		x = br.readLine();
 		payroll = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		gp = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		wins = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		losses = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		goalsfor = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		goalsagainst = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		ppo = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		tsh  = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		ppgoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		shgoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		ppgoalsA = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		shgoalsA = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		shotsfor = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		shotsagainst = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 		x = br.readLine();
 		saves = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 
 	}
 
 	/**
 	 * Loads the team data through prompting the user for data
 	 * @author MK, AV, CH, PJ
 	 */
 	public void prompt() {
 		Scanner in = new Scanner(System.in);
 		System.out.print("1. New Jersey Devils\n2. New York Islanders\n"+
 				"3. New York Rangers\n4. Philadelphia Flyers\n5. Pittsburgh Penguins\n"+
 				"6. Boston Bruins\n7. Buffalo Sabres\n8. Montréal Canadiens\n"+
 				"9. Ottawa Senators\n10. Toronto Maple Leafs\n11. Carolina Hurricanes\n"+
 				"12. Florida Panthers\n13. Tampa Bay Lightning\n14. Washington Capitals\n" +
 				"15. Winnipeg Jets\n16. Chicago Blackhawks\n17. Columbus Blue Jackets\n"+
 				"18. Detroit Red Wings\n19. Nashville Predators\n20. St. Louis Blues\n"+
 				"21. Calgary Flames\n22. Edmonton Oilers\n23. Colorado Avalanche\n"+
 				"24. Minnesota Wild\n25. Vancouver Canucks\n26. Anaheim Ducks\n"+
 				"27. Dallas Stars\n28. Los Angeles Kings\n29. Phoenix Coyotes\n"+
 				"30. San Jose Sharks\nEnter (1-30)");
 		name=in.nextLine();
 		do{
 			System.out.print("Enter "+name+"'s payroll for players: ");
 			payroll=in.nextDouble();
 		}while (payroll<0); //Ensures the payroll for the players isn't negative
 
 		do{
 			System.out.print("Enter the number of games the "+name+" played: ");
 			gp=in.nextInt();
 		}while(gp<0); //Ensures the number of games played is above zero (valid)
 
 		do{
 			System.out.print("Enter the number of games the "+name+" won: ");
 			wins=in.nextInt();
 		}while(wins<0); //Ensures the number of games won is above zero (valid)
 
 		do{
 			System.out.print("Enter the number of games the "+name+" lost: ");
 			losses=in.nextInt();
 		}while(losses<0); //Ensures the number of games lost is above zero (valid)
 
 		do{
 			System.out.print("Enter the total number of goals the "+name+" scored (including power-play, short-handed and empty-net goals): ");
 			goalsfor=in.nextInt();
 		}while(goalsfor<0); //Ensures the total goals scored is above zero (valid)
 
 		do{
 			System.out.print("Enter the total goals scored against the "+name+" (including power-play, short-handed and empty-net goals): ");
 			goalsagainst=in.nextInt();
 		}while(goalsagainst<0); //Ensures the total goals scored against is above zero (valid)
 
 		do{
 			System.out.print("Enter the number of power-play opportunities that the "+name+" have: ");
 			ppo=in.nextInt();
 		}while(ppo<0); //Ensures the number of power-play opportunities is above zero (valid)
 
 		do{
 			System.out.print("Enter the number of times the "+name+" were short-handed: ");
 			tsh=in.nextInt();
 		}while(tsh<0); //Ensures the number of times short-handed is above zero (valid)
 
 		do{
 			System.out.print("Enter the total number of power-play goals the "+name+" have: ");
 			ppgoals=in.nextInt();
 		}while(ppgoals<0); //Ensures the total number of power-play goals is above zero (valid)
 
 		do{
 			System.out.print("Enter the total number of short-handed goals the "+name+" have: ");
 			shgoals=in.nextInt();
 		}while(shgoals<0); //Ensures the total number of short-handed goals is above zero (valid)
 
 		do{
 			System.out.print("Enter the total number of power-play goals aganist the "+name+": ");
 			ppgoalsA=in.nextInt();
 		}while(ppgoalsA<0); //Ensures the total number of power-play goals against is above zero (valid)
 
 		do{
 			System.out.print("Enter the total number of short-handed goals against the "+name+": ");
 			shgoalsA=in.nextInt();
 		}while(shgoalsA<0); //Ensures the total number of short-handed goals against is above zero (valid)
 
 		do{
 			System.out.print("Enter the total number of shots on the "+name+"'s goal: "); //More info needed
 			shotsfor=in.nextInt();
 		}while(shotsfor<0); //Ensures the total shots on goal is above zero (valid)
 
 		do{
 			System.out.print("Enter the total number of shots against the "+name+": ");
 			shotsagainst=in.nextInt();
 		}while(shotsagainst<0); //Ensures the total shots against is above zero (valid)
 
 		do{
 			System.out.print("Enter the total number of saves for the "+name+": ");
 			saves=in.nextInt();
 		}while(saves<0); //Ensures the total saves is above zero (valid)
 	}
 
 	/**
 	 * Saves all statistics of team onto a text file
 	 * @param pw of type PrintWriter
 	 */
 	public void save(PrintWriter pw){
 		pw.println("TEAM NAME: "+name);
 		pw.println("Divisison: "+division);
 		pw.println("Conference: "+conference);
 		pw.println("Payroll: "+payroll);
 		pw.println("Games Played: "+gp);
 		pw.println("Games Won: "+wins);
 		pw.println("Games Lost:"+losses);
 		pw.println("Total Goals Scored: "+goalsfor);
 		pw.println("Total Goals Scored Against: "+goalsagainst);
 		pw.println("Number of Powerplay Opportunities: "+ppo);
 		pw.println("Number of Short Times Handed: "+tsh);
 		pw.println("Total Number of Powerplay Goals: "+ppgoals);
 		pw.println("Total Number of Shorthanded Goals: "+shgoals);
 		pw.println("Total Number of Powerplay Goals Against: "+ppgoalsA);
 		pw.println("Total number of Shorthanded Goals Against: "+shgoalsA);
 		pw.println("Total Shots: "+shotsfor);
 		pw.println("Total Shots Against: "+shotsagainst);
 		pw.println("Total Saves: "+saves);
 
 	}
 
 	/**
 	 * Returns name of team
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns Division team belongs to
 	 * @return the division
 	 */
 	public String getDivision() {
 		updatediv();
 		return division;
 	}
 
 	/**
 	 * Returns Conference team belongs to
 	 * @return the conference
 	 */
 	public String getConference() {
 		updateconf();
 		return conference;
 	}
 
 	/**
 	 * Returns coaching staff
 	 * @return the coachingstaff
 	 */
 	public Coach [] getCoachingstaff() {
 		return coachingstaff;
 	}
 
 	/**
 	 * Returns the players in the team
 	 * @return the players
 	 */
 	public Player[] getPlayers() {
 		return players;
 	}
 
 	/**
 	 * Returns the total payroll of the team
 	 * @return the payroll
 	 */
 	public double getPayroll() {
 		return payroll;
 	}
 
 	/**
 	 * Returns the salary cap for the team
 	 * @return the salarycap
 	 */
 	public double getSalarycap() {
 		return salarycap;
 	}
 
 	/**
 	 * Returns total number of games played
 	 * @return the gp
 	 */
 	public int getGP() {
 		return gp;
 	}
 
 	/**
 	 * Returns total number of games won
 	 * @return the wins
 	 */
 	public int getWins() {
 		return wins;
 	}
 
 	/**
 	 * Returns total number of games lost
 	 * @return the losses
 	 */
 	public int getLosses() {
 		return losses;
 	}
 
 	/**
 	 * Returns total goals scored by the team 
 	 * @return the goalsfor
 	 */
 	public int getGoalsfor() {
 		return goalsfor;
 	}
 
 	/**
 	 * Return total goals scored against team
 	 * @return the goalsagainst
 	 */
 	public int getGoalsagainst() {
 		return goalsagainst;
 	}
 
 	/**
 	 * Return total Power play opportunities
 	 * @return the ppo
 	 */
 	public int getPpo() {
 		return ppo;
 	}
 
 	/**
 	 * Return times shorthanded
 	 * @return the tsh
 	 */
 	public int getTsh() {
 		return tsh;
 	}
 
 	/**
 	 * Returns total powerplay goals scored
 	 * @return the ppgoals
 	 */
 	public int getPpgoals() {
 		return ppgoals;
 	}
 
 	/**
 	 * Returns total short handed goals scored
 	 * @return the shgoals
 	 */
 	public int getShgoals() {
 		return shgoals;
 	}
 
 	/**
 	 * Return total goals scored against during powerplay
 	 * @return the ppgoalsA
 	 */
 	public int getPpgoalsA() {
 		return ppgoalsA;
 	}
 
 	/**
 	 * Returns total goals scored against during shorthanded play
 	 * @return the shgoalsA
 	 */
 	public int getShgoalsA() {
 		return shgoalsA;
 	}
 
 	/**
 	 * Returns total shots 
 	 * @return the shotsfor
 	 */
 	public int getShotsfor() {
 		return shotsfor;
 	}
 
 	/**
 	 * Return total shots against team
 	 * @return the shotsagainst
 	 */
 	public int getShotsagainst() {
 		return shotsagainst;
 	}
 
 	/**
 	 * Returns total saves
 	 * @return the saves
 	 */
 	public int getSaves() {
 		return saves;
 	}
 
 	/**
 	 * Returns average goals scored per game
 	 * @return the avgGPG
 	 */
 	public double getAvgGPG() {
 		updateAvgGPG();
 		return avgGPG;
 	}
 
 	/**
 	 * Returns average goals scored against per game
 	 * @return the avgGAPG
 	 */
 	public double getAvgGAPG() {
 		updateAvgGAPG();
 		return avgGAPG;
 	}
 
 	/**
 	 * Returns average shots per game
 	 * @return the avgSPG
 	 */
 	public double getAvgSPG() {
 		updateAvgSPG();
 		return avgSPG;
 	}
 
 	/**
 	 * Returns average shots against per game
 	 * @return the avgSAPG
 	 */
 	public double getAvgSAPG() {
 		updateAvgSAPG();
 		return avgSAPG;
 	}
 
 	/**
 	 * Returns the shooting percentage
 	 * @return the shPercent
 	 */
 	public double getShPercent() {
 		updateShPercent();
 		return shPercent;
 	}
 
 	/**
 	 * @return the savePercent
 	 */
 	public double getSavePercent() {
 		updateSavePercent();
 		return savePercent;
 	}
 
 	/**
 	 * @return the power play Percent
 	 */
 	public double getPpPercent() {
 		updatePpPercent();
 		return ppPercent;
 	}
 
 	//Mutators
 
 	/**
 	 * Assigns field name with a string, updates division and conference
 	 * @param number that denotes the team
 	 */
 	public void putName(int x) {
 		if (x<=15){
 			conference = "Eastern";
 			if (x<=5){
 				division = "Atlantic Division";
 				if (x == 1)
 					name = "New Jersey Devils";
 				else if (x == 2)
 					name = "New York Islanders";
 				else if (x == 3)
 					name = "New York Rangers";
 				else if (x == 4)
 					name = "Philadelphia Flyers";
 				else
 					name = "Pittsburgh Penguins";
 			}
 			else if (x<=10){
 				division = "Northeast Division";
 				if (x == 6)
 					name = "Boston Bruins";
 				else if (x == 7)
 					name = "Buffalo Sabres";
 				else if (x == 8)
 					name = "Montréal Canadiens";
 				else if (x == 9)
 					name = "Ottawa Senators";
 				else
 					name = "Toronto Maple Leafs";
 			}
 			else{
 				division = "Southeast Division";
 				if (x == 11)
 					name = "Carolina Hurricanes";
 				else if (x == 12)
 					name = "Florida Panthers";
 				else if (x == 13)
 					name = "Tampa Bay Lightning";
 				else if (x == 14)
 					name = "Washington Capitals";
 				else
 					name = "Winnipeg Jets";
 			}
 		}
 		if (x<=30){
 			conference = "Western";
 			if (x<=20){
 				division = "Central Division";
 				if (x == 16)
 					name = "Chicago Blackhawks";
 				else if (x == 17)
 					name = "Columbus Blue Jackets";
 				else if (x == 18)
 					name = "Detroit Red Wings";
 				else if (x == 19)
 					name = "Nashville Predators";
 				else
 					name = "St. Louis Blues";
 			}
 			else if (x<=25){
 				division = "Northwest Division";
 				if (x == 21)
 					name = "Calgary Flames";
 				else if (x == 22)
 					name = "Edmonton Oilers";
 				else if (x == 23)
 					name = "Colorado Avalanche";
 				else if (x == 24)
 					name = "Minnesota Wild";
 				else
 					name = "Vancouver Canucks";
 			}
 			else{
 				division = "Pacific Division";
 				if (x == 26)
 					name = "Anaheim Ducks";
 				else if (x == 27)
 					name = "Dallas Stars";
 				else if (x == 28)
 					name = "Los Angeles Kings";
 				else if (x == 29)
 					name = "Phoenix Coyotes";
 				else
 					name = "San Jose Sharks";		
 
 			}
 		}
 	}
 
 	
 	/**
 	 * Assigns the parameter to coachingstaff
 	 * @param Coach [] x an array of coaches
 	 */
 	public void putCoachingstaff(Coach[] x) {
 		coachingstaff = x;
 	}
 
 	/**
 	 * Assigns the parameter to players
 	 * @param Player [] x an array of players
 	 */
 	public void putPlayers(Player[] x) {
 		players = x;
 	}
 
 	/**
 	 * Assigns the parameter to payroll
 	 * @param double x the payroll of the team
 	 */
 	public void putPayroll(double x) {
 		payroll = x;
 	}
 
 	/**
 	 * Assigns the parameter to gp
 	 * @param int x the total games played
 	 */
 	public void putGp(int x) {
 		gp = x;
 	}
 
 	/**
 	 * Assigns the parameter to wins
 	 * @param int x the total games won
 	 */
 	public void putWins(int x) {
 		wins = x;
 	}
 
 	/**
 	 * Assigns the parameter to losses
 	 * @param int x the total games lost
 	 */
 	public void putLosses(int x) {
 		losses = x;
 	}
 	/**
 	 * Assigns the parameter to goalsfor
 	 * @param int x representing total goals scored
 	 */
 	public void putGoalsfor(int x) {
 		goalsfor = x;
 	}
 
 	/**
 	 * Assigns the parameter to goalsagainst
 	 * @param int x representing total goals scored against
 	 */
 	public void putGoalsagainst(int x) {
 		goalsagainst = x;
 	}
 
 	/**
 	 * Assigns the parameter to ppo
 	 * @param int x representing power play opportunities
 	 */
 	public void putPpo(int x) {
 		ppo = x;
 	}
 
 	/**
 	 * Assigns the parameter to tsh
 	 * @param int x representing times short handed
 	 */
 	public void putTsh(int x) {
 		tsh = x;
 	}
 
 	/**
 	 * Assigns the parameter to ppgoals
 	 * @param int x representing power play goals scored
 	 * 
 	 */
 	public void putPpgoals(int x) {
 		ppgoals = x;
 	}
 
 	/**
 	 * Assigns the parameter to shgoals
 	 * @param int x representing short handed goals scored
 	 */
 	public void putShgoals(int x) {
 		shgoals = x;
 	}
 
 	/**
 	 * Assigns the parameter to ppgoalsA
 	 * @param int x representing goals scored against during power play
 	 */
 	public void putPpgoalsA(int x) {
 		ppgoalsA = x;
 	}
 
 	/**
 	 * Assigns the parameter to shgoalsA
 	 * @param int x representing short handed goals scored against
 	 */
 	public void putShgoalsA(int x) {
 		shgoalsA = x;
 	}
 
 	/**
 	 * Assigns the parameter to shotsfor
 	 * @param int x representing total shots
 	 */
 	public void putShotsfor(int x) {
 		shotsfor = x;
 	}
 
 
 	/**
 	 * Assigns the parameter to shotsagainst
 	 * @param int x representing total shots against
 	 */
 	public void putShotsagainst(int x) {
 		shotsagainst = x;
 	}
 
 	/**
 	 * Assigns parameter to saves
 	 * @param int x representing total saves
 	 */
 	public void putSaves(int x) {
 		saves = x;
 	}
 
 	/**
 	 * Updates avgGPG
 	 */
 	public void updateAvgGPG() {
 		avgGPG = goalsfor/gp;
 	}
 
 	/**
 	 * Updates avgGAPG
 	 */
 	public void updateAvgGAPG() {
 		avgGAPG = goalsagainst/gp;
 	}
 
 	/**
 	 * Updates avgSPG
 	 */
 	public void updateAvgSPG() {
 		avgSPG = shotsfor/gp;
 	}
 
 	/**
 	 * Updates avgSAPG
 	 */
 	public void updateAvgSAPG() {
 		avgSAPG = shotsagainst/gp;
 	}
 
 	/**
 	 * Updates ShPercent
 	 */
 	public void updateShPercent() {
 		shPercent = (goalsfor/shotsfor)*100;
 	}
 
 	/**
 	 * Updates savePercent
 	 */
 	public void updateSavePercent() {
 		savePercent = (saves/shotsagainst)*100;
 	}
 
 	/**
 	 * Updates ppPercent
 	 */
 	public void updatePpPercent() {
 		ppPercent = (ppgoals/ppo)*100;
 	}
 	
 	/**
 	 * Updates division
 	 */
 	public void updatediv() {
 		if (name.equals("New Jersey Devils")||name.equals("New York Islanders")||name.equals("New York Rangers")||name.equals("Philadelphia Flyers")||name.equals("Pittsburgh Penguins")){
 			division = "Atlantic Division";
 		}
 		else if (name.equals("Boston Bruins")||name.equals("Buffalo Sabres")||name.equals("Montréal Canadiens")||name.equals("Ottawa Senators")||name.equals("Toronto Maple Leafs")){
 			division = "Northeast Division";
 		}
 		else if (name.equals("Carolina Hurricanes")||name.equals("Florida Panthers")||name.equals("Tampa Bay Lightning")||name.equals("Washington Capitals")||name.equals("Winnipeg Jets")){
 			division = "Southeast Division";
 		}
 		else if (name.equals("Chicago Blackhawks")||name.equals("Columbus Blue Jackets")||name.equals("Detroit Red Wings")||name.equals("Nashville Predators")||name.equals("St. Louis Blues")){
 			division = "Central Division";
 		}
 		else if (name.equals("Calgary Flames")||name.equals("Edmonton Oilers")||name.equals("Colorado Avalanche")||name.equals("Minnesota Wild")||name.equals("Vancouver Canucks")){
 			division = "Northwest Division";
 		}
 		else {
 			division = "Pacific Division";
 		}
 	}
 	
 	/**
 	 * Updates Conference
 	 */
 	public void updateconf() {
 		int conf = confNum();
 		if (conf == 0){
 			conference = "Eastern";
 		}else{
 			conference = "Western";
 		}
 	}
 
 	/**
 	 * Writes players to text file
 	 * @param pw the print writer keeping track of cursor in the file
 	 */
 	public void writePlayer(PrintWriter pw){
 		for (int i = 0; i < players.length; i++){
 			players[i].save(pw);
 		}
 	}
 	
 	/**
 	 * Writes coaches to text file
 	 * @param pw the print writer keeping track of cursor in the file
 	 */
 	public void writeCoach(PrintWriter pw){
		for (int i = 0; i < coachingstaff.length; i++){
 			coachingstaff[i].save(pw);
 		}
 	}
 
 	/**
 	 * Determines the appropriate division the team belongs to
 	 * @return an integer representing the division the team belongs to
 	 */
 	public int divNum(){
 		if (name.equals("New Jersey Devils")||name.equals("New York Islanders")||name.equals("New York Rangers")||name.equals("Philadelphia Flyers")||name.equals("Pittsburgh Penguins")){
 			return 0;
 		}
 		else if (name.equals("Boston Bruins")||name.equals("Buffalo Sabres")||name.equals("Montr�al Canadiens")||name.equals("Ottawa Senators")||name.equals("Toronto Maple Leafs")){
 			return 1;
 		}
 		else if (name.equals("Carolina Hurricanes")||name.equals("Florida Panthers")||name.equals("Tampa Bay Lightning")||name.equals("Washington Capitals")||name.equals("Winnipeg Jets")){
 			return 2;
 		}
 		else if (name.equals("Chicago Blackhawks")||name.equals("Columbus Blue Jackets")||name.equals("Detroit Red Wings")||name.equals("Nashville Predators")||name.equals("St. Louis Blues")){
 			return 3;
 		}
 		else if (name.equals("Calgary Flames")||name.equals("Edmonton Oilers")||name.equals("Colorado Avalanche")||name.equals("Minnesota Wild")||name.equals("Vancouver Canucks")){
 			return 4;
 		}
 		else {
 			return 5;
 		}
 	}
 	/**
 	 * Determines the appropriate conference the team belongs to
 	 * @return an integer representing the conference the team belongs to
 	 */
 	public int confNum(){
 		int x = divNum();
 		if (x<=2)
 			return 0;
 		else
 			return 1;
 	}
 }
