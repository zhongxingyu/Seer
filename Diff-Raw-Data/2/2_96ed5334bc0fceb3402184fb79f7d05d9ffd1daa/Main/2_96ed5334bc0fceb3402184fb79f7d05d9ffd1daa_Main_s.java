 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 import java.util.Stack;
 public class Main {
 
 
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		Scanner in = new Scanner(System.in);
 		BufferedReader br = null;
 
 		String filepath;
 		int count = 0; //counter
 		int option; //Specifies the option that the user selects from the menu
 		String x; //general purpose variable for user input/loading
 		String classType;
 
 		Stack<Team> teams = new Stack<Team>();
 		Stack<Player> players;
 		Stack<Coach> coaches;
 
 		System.out.println("Welcome to H.P.A. - The Hockey Performance Analyzer.");
 		System.out.println("Press ENTER to continue.");
 		in.nextLine();
 
 		boolean menuRunning;
 		menuRunning = true;
 		System.out.println("Stage 1: Input Information");
 		System.out.println("Note: More than one team may be inputted.");
 		do {
 			//resets players and coaches
 			players = new Stack<Player>();
 			coaches = new Stack<Coach>(); 
 			System.out.println("1 - Manually enter a team's statistics.");
 			System.out.println("2 - Load a team's statistics from a text file.");
 			System.out.println("3 - Continue to Stage 2: Operations"); //continues to next step once more than 1 team is loaded
 			System.out.println("Select an option: ");
 			option = in.nextInt();
 			if (option == 1) {
 				boolean continuePrompt;
 				int type;
 				//Prompts for team stats
 				System.out.println("Step 1: Team Information");
 				teams.push(new Team());
 
 				//Prompts for player stats
 				System.out.println("Step 2: Player Information");
 				do {
 					do {
 						System.out.println("1 - Forward");
 						System.out.println("2 - Defense");
 						System.out.println("3 - Goalie");
 						System.out.println("Select player type: ");
 						type = in.nextInt();
 					} while (type<1||type>3);
 					if (type == 1) 
 						players.push(new Forward());
 					else if (type == 2)
 						players.push(new Defense());
 					else
 						players.push(new Goalie());
 
 					System.out.println("Add another player? (Y/N");
 					x = in.next();
 					if (x.equalsIgnoreCase("Y"))
 						continuePrompt = true;
 					else
 						continuePrompt = false;
 				} while (continuePrompt);
 				players.copyInto(teams.get(count).getPlayers()); //copies stack into player array
 
 				//prompts for coach stats
 				System.out.println("Step 3: Coach Information");
 				do {
 					do {
 						System.out.println("1 - Head");
 						System.out.println("2 - Assistant");
 						System.out.println("3 - Goaltender");
 						System.out.println("4 - Trainer");
 						System.out.println("Select player type: ");
 						type = in.nextInt();
 					} while (type<1||type>4);
 					if (type == 1) 
 						coaches.push(new head());
 					else if (type == 2)
 						coaches.push(new assistant());
 					else if (type == 3)
 						coaches.push(new goaltender());
 					else
 						coaches.push(new trainer());
 
 					System.out.println("Add another coach? (Y/N");
 					x = in.next();
 					if (x.equalsIgnoreCase("Y"))
 						continuePrompt = true;
 					else
 						continuePrompt = false;
 				} while (continuePrompt);
 				coaches.copyInto(teams.get(count).getCoachingstaff()); //copies stack into coach array
 				teams.get(count).updateconf();
 				teams.get(count).updatediv();
 				count++;
 			}
 			else if (option == 2) {
 				//loads from team from text file
 				boolean fileFound;
 				in = new Scanner(System.in); //or else catch behaves weirdly
 				do {
 					System.out.println("Enter location of the text file you want to load from: ");
 					filepath = in.nextLine();
 					try {
 						br = new BufferedReader(new FileReader(filepath));
 						fileFound = true;
 					}
 					catch (FileNotFoundException e) {
 						fileFound = false;
 						System.out.println("File not found.");
 					}
 				} while (fileFound == false);
 				teams.push(new Team(br));
 				//loads players from text file
 				br.readLine(); //skips empty line
 				do {
 					x = br.readLine();
 					classType = x.substring(x.indexOf(": ")+2,x.length());
 					if (classType.equals("forward"))
 						players.push(new Forward(br));
 					else if (classType.equals("defense")) {
 						players.push(new Defense(br));
 					}
 					else 
 						players.push(new Goalie(br));
 					br.readLine();//skips the space between each player
 					br.mark(1000); //stores this location in the memory so program can revisit this part of the stream later
 					x = br.readLine(); //reads next object
 					classType = x.substring(x.indexOf(": ")+2,x.length());
 					br.reset();//moves cursor back to where stream was marked
 				} while (classType.equals("forward")||classType.equals("defense")||classType.equals("goalie"));
 				teams.get(count).putplayersize(players.size());
 				players.copyInto(teams.get(count).getPlayers()); //copies stack into player array
 
 				//loads coaches from text file
 				do {
 					x = br.readLine();
 					classType = x.substring(x.indexOf(": ")+2,x.length());
 					if (classType.equals("head"))
 						coaches.push(new head(br));
 					else if (classType.equals("assistant"))
 						coaches.push(new assistant(br));
 					else if (classType.equals("goaltender"))
 						coaches.push(new goaltender(br));
 					else 
 						coaches.push(new trainer(br));
 					x = br.readLine();//skips the space between each coach
 					br.mark(1000); //stores this location in the memory so program can revisit this part of the stream later
 					x = br.readLine(); //checks if next line in the text file is end of file or not
 					br.reset();
 				} while (x != null && !x.equals(""));
 				teams.get(count).putcoachingstaffsize(coaches.size());
 				coaches.copyInto(teams.get(count).getCoachingstaff()); //copies stack into coach array
 				count++;
 			}
 			else if (option == 3) {
 				if (count<1)
 					System.out.println("Error - Information is required for at least 1 team.");
 				else
 					menuRunning = false;
 			}
 			else
 				System.out.println("Invalid option.");
 		} while (menuRunning);
 		br.close(); 
 
 		Person[] jumboArray; //temporary array
 		Stack<Person> jumbo; //temporary stack (to make life easier)
 		PrintWriter pw = null; //initializes printwriter
 		int option1; //temporarily holds option input for menu inside menu
 		int option2; //temporarily holds option input for menu inside menu inside menu
 		System.out.println("Stage 2: Operations");
 		menuRunning = true;
 		do {
 			do {
 				System.out.println("1 - Save teams onto a text file.");
 				System.out.println("2 - Sort a team."); //by stats that users specify
 				System.out.println("3 - Rank the teams.");
 				System.out.println("4 - Rank the players."); //make gigantic array of players and bubble sort according to rating
 				System.out
 				.println("5 - Use the Cost Efficiency System (C.E.S.) to rank players. ");
 				System.out
 				.println("6 - List all players within a certain age threshold.");
 				System.out.println("7 - List all right-handed players.");
 				System.out.println("8 - List all left-handed players.");
 				System.out.println("9 - List all the rookies.");
 				System.out.println("12 - Terminate the program.");
 				System.out.println("Select an operation: ");
 				option = in.nextInt();
 			} while (option < 1 || option > 12);
 			String folder;
 			File someFolder;
 			if (option == 1) {
 				do {
 					in = new Scanner(System.in);
 					System.out.println("Enter location to save text files: ");
 					folder = in.nextLine();
 					someFolder = new File(folder); 
 					if (!someFolder.exists()||!someFolder.isDirectory()||!folder.substring(folder.length()-1).equals("\\"))
 						System.out.println("Location not found.");
 				} while (!someFolder.exists()||!someFolder.isDirectory()||!folder.substring(folder.length()-1).equals("\\"));
 
 				for (int i = 0; i < teams.size(); i++) {
 					filepath =  folder + teams.get(i).getName() + ".txt";
 					pw = new PrintWriter(new FileWriter(filepath));
 					pw.println("League: NHL");
 					pw.println("");
 					teams.get(i).save(pw); //already skips line in methods
 					pw.close();
 				}
 			}
 			else if (option == 2) {
 				do {
 					System.out.println("Player fields: ");
 					System.out
 					.println("1. First Name\n2. Last Name\n3. Age\n4. Height\n5. Weight");
 					System.out
 					.println("6. Salary per year\n7. Years remaining on contract\n8. Total salary\n9. Rating\n10. Games played\n11. Penalty Minutes\n12. Jersey number\n13. Total number of minutes on ice");
 					System.out
 					.println("14. Plus/Minus\n15. Goals per game\n16. Shots taken per game\n17. Assists per game\n18. Number of shifts per game\n19. Number of game-winning goals\n20. Number of power play goals\n21. Number of overtime goals\n22. Points\n23. Faceoff win percentage\n24. Shooting percentage\n25. Total Goals per Season\n26. Total Shots per Season\n27. Total Assists per Season");
 					System.out
					.println("28. Number of wins\n29. Numer of losses\n30. Number of wins during playoffs\n31. Number of losses during playoffs\n32. Number of losses during overtime\n33. Number of games started by goalie\n34. Number of goals scored against goalie\n35. Total number of shots faced by goalie\n36. Number of saves by goalie\n37. Save Percentage\n38. Shutout Saves\n39. Empty Net Goals");
 					System.out.println("Select an option: ");
 					option1 = in.nextInt();
 				} while (option1<1 || option1>40);
 
 				if (option1 >=1 && option1 <=5) {
 					//create big array of coaches and players for all teams
 					jumbo = new Stack<Person>();
 					for (int i = 0; i<teams.size();i++) {
 						for (int j = 0; j<teams.get(i).getCoachingstaff().length;j++)
 							jumbo.push(teams.get(i).getCoach(j));
 						for (int j = 0; j<teams.get(i).getPlayers().length;j++) 
 							jumbo.push(teams.get(i).getPlayer(j));
 					}
 					jumboArray = new Person[jumbo.size()];
 					jumbo.copyInto(jumboArray);
 
 					if (option1 == 1) {
 						System.out.println("1 - Ascending order (Z-A).");
 						System.out.println("2 - Descending order (A-Z).");
 						System.out.println("Select an option: ");
 						option2 = in.nextInt();
 						if (option2 == 1)
 							sortingMethods.sortFNameA(jumboArray);
 						else
 							sortingMethods.sortFNameD(jumboArray);
 						for (int i=0;i<jumboArray.length;i++)
 							System.out.println(jumboArray[i].getFName());
 					}
 
 					else if (option1 == 2) {
 						System.out.println("1 - Ascending order (Z-A).");
 						System.out.println("2 - Descending order (A-Z).");
 						System.out.println("Select an option: ");
 						option2 = in.nextInt();
 						if (option2 == 1)
 							sortingMethods.sortLNameA(jumboArray);
 						else
 							sortingMethods.sortLNameD(jumboArray);
 						for (int i=0;i<jumboArray.length;i++)
 							System.out.println(jumboArray[i].getLName());
 					}
 
 					else if (option1 == 3) {
 						System.out.println("1 - Ascending order.");
 						System.out.println("2 - Descending order.");
 						System.out.println("Select an option: ");
 						option2 = in.nextInt();
 						if (option2 == 1)
 							sortingMethods.sortAgeA(jumboArray);
 						else
 							sortingMethods.sortAgeD(jumboArray);
 						for (int i=0;i<jumboArray.length;i++)
 							System.out.println(jumboArray[i].getFName()+" "+jumboArray[i].getLName()+", age "+jumboArray[i].getAge());
 					}
 
 					else if (option1 == 4) {
 						System.out.println("1 - Ascending order.");
 						System.out.println("2 - Descending order.");
 						System.out.println("Select an option: ");
 						option2 = in.nextInt();
 						if (option2 == 1)
 							sortingMethods.sortHeightA(jumboArray);
 						else
 							sortingMethods.sortHeightD(jumboArray);
 						for (int i=0;i<jumboArray.length;i++)
 							System.out.println(jumboArray[i].getFName()+" "+jumboArray[i].getLName()+", height: "+jumboArray[i].getHeight());
 					}
 
 					else {
 						System.out.println("1 - Ascending order.");
 						System.out.println("2 - Descending order.");
 						System.out.println("Select an option: ");
 						option2 = in.nextInt();
 						if (option2 == 1)
 							sortingMethods.sortWeightA(jumboArray);
 						else
 							sortingMethods.sortWeightD(jumboArray);
 						for (int i=0;i<jumboArray.length;i++)
 							System.out.println(jumboArray[i].getFName()+" "+jumboArray[i].getLName()+", height: "+jumboArray[i].getHeight());
 					}
 				}
 				else if (option1 >=6 && option1 <=13) {
 					//create big array of players for all teams
 					jumbo = new Stack<Person>();
 					for (int i = 0; i<teams.size();i++) {
 						for (int j = 0; j<teams.get(i).getPlayers().length;j++) 
 							jumbo.push(teams.get(i).getPlayer(j));
 					}
 					jumboArray = new Person[jumbo.size()];
 					jumbo.copyInto(jumboArray);
 					
 					if (option1 == 6) {
 						System.out.println("1 - Ascending order.");
 						System.out.println("2 - Descending order.");
 						System.out.println("Select an option: ");
 						option2 = in.nextInt();
 						if (option2 == 1)
 							sortingMethods.sortSalaryPYA(jumboArray);
 						else
 							sortingMethods.sortSalaryPYD(jumboArray);
 						for (int i=0;i<jumboArray.length;i++)
 							System.out.println(jumboArray[i].getFName()+", salary per year: $"+((Player)jumboArray[i]).getSalaryPY());
 					}
 				}
 
 
 
 			}
 
 
 
 			else if (option == 12) {
 				System.out.println("Terminating program.");
 				System.out.println("Thank you for using H.P.A.");
 				menuRunning = false;
 				in.close();
 				pw.close();
 			}
 			else
 				System.out.println("Invalid option.");
 		} while (menuRunning);
 	}
 }
