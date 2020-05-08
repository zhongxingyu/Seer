 import java.io.*;
 
 public class testloadsave {
 	 
 
 	/**
 	 * Loads the data from a text file onto an object of type person in the program
 	 * @param String filepath
 	 * @param Object of type person, Note: this object must be initialized already
 	 * @author MK, AV, CH, PJ
 	 */
 	
 	public static void load(String file, Person person) throws IOException{
 		String x;
 		FileReader fr = new FileReader(file);
 		BufferedReader br = new BufferedReader(fr);
 		String classType;
 		
 		//loads person fields into the object
 		x = br.readLine();
 		classType = x.substring(x.indexOf(": ")+2,x.length());
 		
 		if (classType.equals("coach")) {
 			x = br.readLine();
 			((Coach)person).coachType = Coach.convertCoachTypeToInt(x.substring(x.indexOf(": ")+2,x.length()));
 		}
 			
 		x = br.readLine();
 		person.fname = x.substring(x.indexOf(": ")+2,x.length());
 		
 		x = br.readLine();
 		person.lname = x.substring(x.indexOf(": ")+2,x.length());
 		
 		x = br.readLine();
 		person.age = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 		
 		x = br.readLine();
 		x = x.substring(x.indexOf(": ")+2,x.length());
 		if (x.equals("male"))
 			person.gender = true;
 		else
 			person.gender = false;
 		
 		x = br.readLine();
 		person.height = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 		
 		x = br.readLine();
 		person.weight = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 		
 		x = br.readLine();
 		person.birthMonth = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 		
 		x = br.readLine();
 		person.birthDay = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 		
 		x = br.readLine();
 		person.birthYear = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 		
 		x = br.readLine();
 		person.birthPlace = Person.convertBirthPlaceToInt(x.substring(x.indexOf(": ")+2,x.length()));
 		
 		x = br.readLine();
 		person.maritalStatus = Person.convertMaritalStatusToInt(x.substring(x.indexOf(": ")+2,x.length()));
 		
 		if (classType.equals("coach")) {
 			//loads coach fields into the object
 			x = br.readLine();
 			((Coach)person).winS = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Coach)person).loseS = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Coach)person).gameCS = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Coach)person).pointPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Coach)person).winP = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Coach)person).loseP = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Coach)person).gameCP = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Coach)person).stanleyN = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 		}
 		else if (classType.equals("forward")||classType.equals("defense")||classType.equals("goalie")) {
 			//load player fields into object
 			x = br.readLine();
 			((Player)person).salaryPY = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).contractR = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).tSalary = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).rating = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).gp = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).penaltyT = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).penaltyN = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			x = x.substring(x.indexOf(": ")+2,x.length());
 			if (x.equals("right arm"))
 				((Player)person).arm = true;
 			else
 				((Player)person).arm = false;
 			
 			x = br.readLine();
 			((Player)person).number = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).numMin = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			x = x.substring(x.indexOf(": ")+2,x.length());
 			if (x.equals("true")) 
 				((Player)person).rookie = true;
 			else
 				((Player)person).rookie = false;
 			
 			x = br.readLine();
 			((Player)person).penaltyMinor = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).penaltyMajor = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).penaltyMisc = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).penaltyGMisc = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			x = br.readLine();
 			((Player)person).match = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			
 			if (classType.equals("forward")) {
 				//load forward fields into object
 				x = br.readLine();
 				((Forward)person).plusMinus = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).avgGoalsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).avgGoalPercentagePS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).avgShotsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).zsGoalPercentage = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).avgAssistsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).avgPenaltyPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).avgPPGoalsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).avgSOGoalsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).gwGoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).ppGoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).soGoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).avgNShifts = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).otGoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).faceoffPercentage = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).points = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Forward)person).shootPercentage = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 			}
 			else if (classType.equals("defense")) {
 				//load defense fields into object
 				x = br.readLine();
 				((Defense)person).plusMinus = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).avgGoalsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).avgGoalPercentagePS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).avgShotsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).zsGoalPercentage = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).avgAssistsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).avgPenaltyPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).avgPPGoalsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).avgSOGoalsPS = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).gwGoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).ppGoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Defense)person).soGoals = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			}
 			else {
 				//load goalie fields into object
 				x = br.readLine();
 				((Goalie)person).wins = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).loses = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).winsP = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).losesP = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).losesOT = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).gameStart = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).goalA = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).goalAA = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).shotsOG = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).saves = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).savePercent = Double.parseDouble(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).shutouts = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 				
 				x = br.readLine();
 				((Goalie)person).emptyNG = Integer.parseInt(x.substring(x.indexOf(": ")+2,x.length()));
 			}			
 		}
 		br.close();
 		fr.close();
 	}
 	
 	/**
 	 * Saves the data of a Person object onto a text file
 	 * @param String filepath
 	 * @param Object of type person
 	 * @author MK, AV, CH, PJ
 	 */
 	public static void save(String file, Person p) throws IOException {
 		FileWriter fw = new FileWriter(file);    //FileWriter 
 		PrintWriter pw = new PrintWriter (fw);     //PrintWriter
 		
 		if (p instanceof Coach) {
 			pw.println("Class: coach");
 			pw.println("Coach type: "+(Coach.convertCoachTypeToString(((Coach)p).getcoachType())));
 		}
 		
 		else if (p instanceof Forward)
 			pw.println("Class: forward");
 		else if (p instanceof Defense)
 			pw.println("Class: defense");
 		else 
 			pw.println("Class: goalie");
 		
 		//Writes all person fields onto text file
 		pw.println("First name: "+p.getFName());
 		pw.println("Last name: "+p.getLName());
 		pw.println("Age: "+p.getAge());
 		if (p.getGender()==true)
 			pw.println("Gender: male");
 		else
 			pw.println("Gender: female");
 		pw.println("Height: "+p.getHeight());
 		pw.println("Weight: "+p.getWeight());
 		pw.println("Birth month: "+p.getBirthMonth());
 		pw.println("Birth day: "+p.getBirthDay());
 		pw.println("Birth year: "+p.getBirthYear());
 		pw.println("BirthPlace: "+Person.convertBirthPlaceToString(p.getBirthPlace()));
 		pw.println("Marital status: "+Person.convertMaritalStatusToString(p.getMaritalStatus()));
 
 		if (p instanceof Coach){
 			//Writes all the fields that coach has onto text file
 			pw.println("Season wins: "+((Coach) p).getWinS());
 			pw.println("Season losses: "+((Coach) p).getloseS());
 			pw.println("Season games coached: "+((Coach) p).getGameCS());
 			pw.println("Points percentage: "+((Coach) p).getPointPS());
 			pw.println("Playoff wins: "+((Coach) p).getWinP());
 			pw.println("Playoff losses: "+((Coach) p).getLoseP());
 			pw.println("Playoff games coached: "+((Coach) p).getGameCP());
 			pw.println("Number of Stanley Cups won: "+((Coach) p).getStanleyN());
 		}
 		else if ((p instanceof Forward) || (p instanceof Defense) || (p instanceof Goalie)){
 			//Writes all the player fields onto text file
 			pw.println("Salary per year: "+((Player) p).getSalaryPY());
 			pw.println("Number of years remaining in contract: "+((Player) p).getcontractR());
 			pw.println("Total salary over entire contract length: "+((Player) p).getTSalary());
 			pw.println("Rating: "+((Player) p).getRating());
 			pw.println("Games played: "+((Player) p).getGP());
 			pw.println("Time spent in penalties: "+((Player) p).getPenaltyT());
 			pw.println("Number of penalties: "+((Player) p).getPenaltyN());
 			if (((Player)p).getArm()==true){
 				pw.println("Arm: right arm");
 			}
 			else{
 				pw.println("Arm: left arm");
 			}
 			pw.println("Jersey number: "+((Player) p).getNumber());
 			pw.println("Total number of minutes on ice: "+((Player) p).getNumMin());
 			pw.println("Rookie: "+((Player)p).getRookie());
 			pw.println("Number of minor penalties: "+((Player) p).getPenaltyMinor());
 			pw.println("Number of major penalties: "+((Player) p).getPenaltyMajor());
 			pw.println("Number of misconduct penalties: "+((Player) p).getPenaltyMisc());
 			pw.println("Number of game misconduct penalties: "+((Player) p).getPenaltyGMisc());
 			pw.println("Match penalties: "+((Player) p).getMatch());
 			
 			if (p instanceof Forward) { 
 				//writes all forward fields onto text file
 				pw.println("Plus/Minus: "+((Forward) p).getplusMinus());
 				pw.println("Average goals per season: "+((Forward) p).getavgGoalsPS());
 				pw.println("Average goal percentage per season: "+((Forward) p).getavgGoalPercentagePS());
 				pw.println("Average shots taken per season: "+((Forward) p).getavgShotsPS());
 				pw.println("Z-score of goal percentage: "+((Forward) p).getzsGoalPercentage());
 				pw.println("Average assists per season: "+((Forward) p).getavgAssistsPS());
 				pw.println("Average penalty minutes per season: "+((Forward) p).getavgPenaltyPS());
 				pw.println("Average power play goals per season: "+((Forward) p).getavgPPGoalsPS());
 				pw.println("Average shoot-out goals per season: "+((Forward) p).getavgSOGoalsPS());
 				pw.println("Number of game-winning goals: "+((Forward) p).getGWGoals());
 				pw.println("Number of power play goals: "+((Forward) p).getPPGoals());
 				pw.println("Number of shootout goals: "+((Forward) p).getSOGoals());
 				pw.println("Average number of shifts per game: "+((Forward) p).getAvgNShifts());
 				pw.println("Number of overtime goals: "+((Forward) p).getOTGoals());
 				pw.println("Faceoff win percentage: "+((Forward) p).getFaceoffPercentage());
 				pw.println("Points: "+((Forward) p).getPoints());
 				pw.println("Shooting percentage: "+((Forward) p).getShootPercentage());
 			}
 			else if (p instanceof Defense){
 				//Writes all the fields that defense has onto text file	
 				pw.println("Plus/Minus: "+((Defense) p).getplusMinus());
 				pw.println("Average goals per season: "+((Defense) p).getavgGoalsPS());
 				pw.println("Average goal percentage per season: "+((Defense) p).getavgGoalPercentagePS());
 				pw.println("Average shots taken per season: "+((Defense) p).getavgShotsPS());
 				pw.println("Z=score of goal percentage: "+((Defense) p).getzsGoalPercentage());
 				pw.println("Average assists per season: "+((Defense) p).getavgAssistsPS());
 				pw.println("Average penalty minutes per season: "+((Defense) p).getavgPenaltyPS());
 				pw.println("Average power play goals per season: "+((Defense) p).getavgPPGoalsPS());
 				pw.println("Average shoot-out goals per season: "+((Defense) p).getavgSOGoalsPS());
 				pw.println("Number of game-winning goals: "+((Defense) p).getGWGoals());
 				pw.println("Number of power play goals: "+((Defense) p).getPPGoals());
 				pw.println("Number of shootout goals: "+((Defense) p).getSOGoals());
 				pw.println("Average number of shifts per game: "+((Defense) p).getAvgNShifts());
 				pw.println("Number of overtime goals: "+((Defense) p).getOTGoals());
 				pw.println("Faceoff win percentage: "+((Defense) p).getFaceoffPercentage());
 				pw.println("Points: "+((Defense) p).getPoints());
 			}
 			else if (p instanceof Goalie){
 				//Writes all the fields that goalie has onto text file
 				pw.println("Wins: "+((Goalie) p).getWins());
 				pw.println("Losses: "+((Goalie) p).getLoses());
 				pw.println("Number of wins during playoffs: "+((Goalie) p).getWinsP());
 				pw.println("Number of losses during playoffs: "+((Goalie) p).getLosesP());
 				pw.println("Number of losses during overtime: "+((Goalie) p).getLosesOT());
 				pw.println("Bumber of games started: "+((Goalie) p).getGameStart());
 				pw.println("Number of goals scored against: "+((Goalie) p).getGameStart());
 				pw.println("Mean goals scored against goalie: "+((Goalie) p).getGoalA());
 				pw.println("Mean goals per 60 minutes scored on the goalie: "+((Goalie) p).getgoalAA());
 				pw.println("Total number of shots on goal the goalie faced: "+((Goalie) p).getShotsOG());
 				pw.println("Bymber of saves goalie made: "+((Goalie) p).getSaves());
 				pw.println("Percentage of total shots faced the goalie saved: "+((Goalie) p).getSavePercent());
 				pw.println("Number of games where goalie had no goals aginst him/her and only goalie to play in game: "+((Goalie) p).getShutouts());
 				pw.println("Number of goals scored against while off ice for extra attack player: "+((Goalie) p).getEmptyNG());
 			}
 		}	
 		fw.close();
 		pw.close();
 	}
 	
 	public static void main(String[] args) throws IOException {
 		Person p1 = new Coach("clem", null, 0, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
 		save("G:\\Computer Science\\workspace\\Inheritance Assignment\\samplecoach.txt",p1);//save test for coach
 		Person p2 = new Goalie("clement", "hoang", 17, true, 178, 78, 6, 24, 1995, 4, 2, 0, 0, 0, 0, 0, 0, 0, false, 0, 0, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
 		save("G:\\Computer Science\\workspace\\Inheritance Assignment\\samplegoalie.txt",p2);//save test for goalie
 		Person p3 = new Forward("clement", "hoang", 17, true, 178, 78, 6, 24, 1995, 4, 2, 0, 0, 0, 0, 0, 0, 0, false, 0, 0, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
 		save("G:\\Computer Science\\workspace\\Inheritance Assignment\\sampleforward.txt",p3);//save test for forward
		Person p4 = new Defense("clement", "hoang", 17, true, 178, 78, 6, 24, 1995, 4, 2, 0, 0, 0, 0, 0, 0, 0, false, 0, 0, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
 		save("G:\\Computer Science\\workspace\\Inheritance Assignment\\samplegoalie.txt",p4);//save test for defense
 		
 		Person loadguy = new Coach(null, null, 0, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);//temporary coach person
 		load("G:\\Computer Science\\workspace\\Inheritance Assignment\\sample.txt", loadguy);//reads file named "sample" which holds all the data
 		save("G:\\Computer Science\\workspace\\Inheritance Assignment\\saveloadguy.txt",loadguy);//saves the file onto separate file named "saveloadguy"
 	}
 }
