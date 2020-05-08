 import java.util.Scanner;
 
 /*
 Note: this class is incomplete.
 */
 
 
 public class leMain {
   public static void main(String []args) {
 		//Person[] person = new Person[46]; //one roster is 23 players
 		int c = 0; //tracks the index of the person in being added into the array
 		int option; //specifies the option that the user selects from the menu
 		Scanner in = new Scanner(System.in);
 		String personType;
 		String playerType;
 
 		//temporary fields for person
		String fname; //Name
		String lname;
 		int age; //Age
 		String tempGender;
 		boolean gender; //True if male 
 		double height; //Height in centimetres
 		double weight; //Weight in kg
 		int birthMonth; //Month of birth 
 		int birthDay; //Day of birth
 		int birthYear; //Year of birth
 		String birthPlace; //City, Province/State of birth; Province/State in short notation
 		String martialStatus; //Single, Married, Divorced, Widowed etc.
 
 		//temporary fields for player
 		double salaryPY; //Salary per year
 		int contractR; //# of years remaining of contract 
 		double tSalary; //Total salary over entire contract length
 		int rating; //Temp for something awesome
 		int gp; //Games played
 		double penaltyT; //Time spent in penalties
 		int penaltyN; //Number of penalties
 		boolean arm; //Shooting arm: True if right arm
 		String tempArm;
 		int number; //Jersey number
 
 		//temporary fields for coach
 		int winS; //Regular season wins
 		int loseS; //Regular season loses
 		int gameCS; //Regular season games coached
 		double pointPS; //Regular season Points percentage (number of points team earned divided by total possible points)
 		int winP; //Playoff wins
 		int loseP; //Playoff loses
 		int gameCP; //Playoff games coached
 		int stanleyN; //Number of Stanley Cups
 		int coachType; //The type of coach (head, assistant or goaltender)
 
 		//temporary fields for goalie
 
 		//temporary fields for defense
 
 		//temporary fields for forward
 		int gwGoals; //Number of game-winning goals
 		int ppGoals; //Number of power play goals
 		int soGoals; //Number of shoot-out goals
 		do {
 			do {
 				//outputs menu and prompts for an option
 				System.out.println("1 - Add a person.");
 				System.out.println("2 - Save data."); //saves a full object
 				System.out.println("3 - Load data."); //loads an empty object with data
 				System.out.println("4 - Terminate the program.");
 				System.out.println("Select an option: ");
 				option = in.nextInt();
 			} while (option < 0 || option > 4);
 			if (option == 1) {
 				//prompt for all person fields
 				do {
					System.out.println("Enter first name: ");
					fname = in.nextLine();
 					in.nextLine();
					System.out.println("Enter last name: ");
					lname = in.nextLine();
					in.nextLine();
				} while (fname.equals("")&&lname.equals("")); //ensures that something is entered
 
 				do {
 					System.out.println("Enter age: ");
 					age = in.nextInt();
 				} while (age <= 0); //ensures age is greater than 0
 
 				do {
 					System.out.println("Enter gender (m/f): ");
 					tempGender = in.next();
 				} while (!tempGender.equals('m') && !tempGender.equals('f'));
 				if (tempGender.equals('m'))
 					gender = true;
 				else
 					gender = false;
 
 				do {
 					System.out.println("Enter height: ");
 					height = in.nextDouble();
 				} while (height <= 0); //ensures height is greater than 0
 
 				do {
 					System.out.println("Enter weight: ");
 					weight = in.nextDouble();
 				} while (weight <= 0); //ensures weight is greater than 0
 				
 				do {
 					System.out.println("Enter birth month: ");
 					birthMonth = in.nextInt();
 				} while (birthMonth <= 0 || birthMonth >12); //ensures birth month is between January and Decemeber
 				
 				do {
 					System.out.println("Enter birth day: ");
 					birthDay = in.nextInt();
 				} while (birthDay <= 0 || birthDay >31); //ensures birth day is between 1 and 31
 				
 				do {
 					System.out.println("Enter birth year: ");
 					birthYear = in.nextInt();
 				} while (birthYear <= 0); //ensures birth month is greater than 0
 				
 				do {
 					System.out.println("Enter birth place based on list: "); //Show list
 					birthPlace = in.next();
 				} while (Integer.parseInt(birthPlace) <= 0 || Integer.parseInt(birthPlace) > 21); //ensures birth place selection is in the list
 				
 				do {
 					System.out.println("Enter martial status based on list: "); //Show list
 					martialStatus = in.next();
 				} while (Integer.parseInt(martialStatus) <= 0 || Integer.parseInt(martialStatus) > 21); //ensures martial status selection is in the list
 				
 				do {
 					//prompt for coach/player
 					do {
 						System.out.println("Select whether person is a coach or player (c/p): ");
 						personType = in.next();
 						if (personType.equals("p")) {
 							//prompt for player stuff
 							do{
 								System.out.println("Enter the player's salary per year: ");
 								salaryPY=in.nextDouble();
 							}while (salaryPY<0); //Ensures the player does have a salary per year
 							
 							do{
 								System.out.println("Enter the player's number of years remaining in the contract: ");
 								contractR=in.nextInt();
 							}while (contractR<0); //Ensures the player has number of years in the contract
 							
 							do{
 								System.out.println("Enter the player's total salary over the entire contract: ");
 								tSalary=in.nextDouble();
 							}while (tSalary<0); //Ensures the player has number of years in the contract
 							
 							do{
 								System.out.println("Enter the number of games the player has played: ");
 								gp=in.nextInt();
 							}while (gp<0); //Ensures the player has at least player 0 games
 							
 							do{
 								System.out.println("Enter the time spent in penalty box for the player: ");
 								penaltyT=in.nextDouble();
 							}while (penaltyT<0); //Ensures the player has at least spent 0 seconds in the penalty box
 							
 							do{
 								System.out.println("Enter the number times spent in the penalty box: ");
 								penaltyN=in.nextInt();
 							}while (penaltyN<0); //Ensures the player has at least been in the penalty box once
 							
 							do {
 								System.out.println("Enter player's shooting arm (R/L): ");
 								tempArm = in.next();
 							} while (!tempArm.equals('R') && !tempArm.equals('L'));
 							if (tempArm.equals("R"))
 								arm=true;
 							else
 								arm=false;
 							
 							do{
 								System.out.println("Enter the player's jersey number: ");
 								number=in.nextInt();
 							}while (number<0); //Ensures the player doesn't have a negative jersey number
 							
 							System.out.println("Select whether player is forward, defence or goalie: ");
 							playerType = in.next();
 							if (playerType.equals("forward")){
 								do{
 									System.out.println("Enter the number of game-winning goals");
 									gwGoals=in.nextInt();
 								}while (gwGoals<0); //Ensure the player doesn't have negative game-winning goals
 								
 								do{
 									System.out.println("Enter the number of power-play goals");
 									ppGoals=in.nextInt();
 								}while (ppGoals<0); //Ensure the player doesn't have negative power-play goals
 								
 								do{
 									System.out.println("Enter the number of shootout goals");
 									soGoals=in.nextInt();
 								}while (soGoals<0); //Ensure the player doesn't have negative shootout goals
 								
								Forward forward=new Forward(fname, lname, age, gender, height, weight, birthMonth, birthDay, birthYear, birthPlace, martialStatus);
 								forward.getAge();
 								forward.putSalaryPY(salaryPY);
 							}
 						}
 						
 						else if (personType.equals("c")) {
 							//person[c] = (Coach)person[c];
 							//prompt for coach fields
 							do{
 								System.out.println("Enter the coach's wins for the regular season: ");
 								winS=in.nextInt();
 							}while (winS<0); //Ensures the the coach doesn't have negative wins
 							
 							do{
 								System.out.println("Enter the coach's loses for the regular season: ");
 								loseS=in.nextInt();
 							}while (loseS<0); //Ensures the the coach doesn't have negative loses
 							
 							do{
 								System.out.println("Enter the coach's games coached for the regular season: ");
 								gameCS=in.nextInt();
 							}while (gameCS<0); //Ensures the the coach doesn't have negative games coached
 							
 							do{
 								System.out.println("Enter the coach's wins for the playoffs: ");
 								winP=in.nextInt();
 							}while (winP<0); //Ensures the the coach doesn't have negative wins
 							
 							do{
 								System.out.println("Enter the coach's loses for the playoffs: ");
 								loseP=in.nextInt();
 							}while (loseP<0); //Ensures the the coach doesn't have negative loses
 							
 							do{
 								System.out.println("Enter the coach's games coached for the playoffs: ");
 								gameCP=in.nextInt();
 							}while (gameCP<0); //Ensures the the coach doesn't have negative games coached
 							
 							do{
 								System.out.println("Enter the number of times the coach has won Stanley Cups: ");
 								stanleyN=in.nextInt();
 							}while (gameCP<0); //Ensures the the coach doesn't have negative number of times the coach wins Stanley Cups
 							
 							do {
 								System.out.println("Enter the type of coach the coach is: "); //Show list
 								coachType = in.nextInt();
 							} while (coachType <= 0 || coachType > 3); //ensures coach type selection is in the list
 						} 
 					} while (!personType.equals("p") && !personType.equals("c"));
 				} while (true);
 			}
 			else if (option == 2) {
 
 			}
 			else if (option == 3) {
 
 			}
 			else {
 
 			}
 		} while (option != 4);
 
 	}
 }
