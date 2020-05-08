 import java.io.BufferedReader;
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
 
 		boolean menuRunning;
 		int error = 0;
 		menuRunning = true;
 		do {
 			//resets players and coaches
 			players = new Stack<Player>();
 			coaches = new Stack<Coach>(); 
 			System.out.println("Step 1: Enter team information");
 			System.out.println("1 - Manually enter Team");
 			System.out.println("2 - Load Team from text file");
 			System.out.println("3 - Continue"); //continues to next step once more than 1 team is loaded
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
				coaches.copyInto(teams.get(count).getCoachingstaff()); //copies stack into player array
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
 				in.close();
 				//loads players from text file
 				br.readLine(); //skips empty line
 				do {
 					x = br.readLine();
 					classType = x.substring(x.indexOf(": ")+2,x.length());
 					error++;
 					System.out.println(error);
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
 					x = x.substring(x.indexOf(": ")+2,x.length()); //checks if next line in the text file is end of file or not ERROR ERROR ERROR
 					br.reset();
 				} while (x != null);
 				coaches.copyInto(teams.get(count).getCoachingstaff()); //copies stack into player array
 				teams.get(count).updateconf();
 				teams.get(count).updatediv();
 				count++;
 			}
 			else if (option == 3) {//to avoid printing invalid
 				if (count<1)
 					System.out.println("Error - Information is required for at least 1 team.");
 				else
 					menuRunning = false;
 			}
 			else
 				System.out.println("Invalid option.");
 		} while (menuRunning);
 
 
 		System.out.println("Enter location to save text file: ");
 		filepath = in.next();
 		PrintWriter pw = new PrintWriter(new FileWriter(filepath));
 		System.out.println("League: NHL\n");
 		for (int i = 0; i<teams.size();i++) {
 			teams.get(i).save(pw);
 			teams.get(i).writePlayer(pw);
 			teams.get(i).writeCoach(pw);
 			pw.println(""); //skips an additional line between each team
 		}
 		in.close();
 		br.close();
 		pw.close();
 
 	}
 }
