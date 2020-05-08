 import java.io.PrintStream;
 import java.util.Scanner;
 
 
 public class CommandLineUserInterface implements UserInterface {
 	
 	protected Scanner _in;
 	protected PrintStream _out;
 	protected Log _log;
 	protected DataProcessor _dataProcessor;
 	
 	public CommandLineUserInterface(DataProcessor dataProcessor) {
 		_log = Log.getInstance();
 		_in = new Scanner(System.in);
 		_out = getPrintstream();
 		_dataProcessor = dataProcessor;
 	}
 	
 	public void start() {
 		
 		println("Welcome to the World Series database!");
 		_log.log("Starting application");
 
 		String choice = null; // the thing that the user chooses to do
 
 		do {
 			showPrompt();
 			
 			choice = getInputString();
 			_log.log("User input: " + choice);
 			
 			if (choice.equals("1")) searchByYear();	
 			else if (choice.equals("2")) searchByTeam();
 			else if (choice.equals("3")) searchByRange();
 			else if (choice.equals("4")) showTeamsYears();
 			else if (!quit(choice)) {
 				println("That is not a valid selection.");
 			}
 			
 		}
 		while (!quit(choice));
 		println("Good bye");
 	}
 
 	protected static boolean quit(String choice) {
 		return choice.equals("Q") || choice.equals("q");
 	}
 	
 	public void showPrompt() {
 		println("");
 		println("Please make your selection:");
 		println("1: Search for World Series info by year");
 		println("2: Search for World Series info by team");
 		println("3: Show all World Series for a range of years");
 		println("4: Show all teams' World Series wins");
 		println("Q: Quit");
 		print("> ");
 
 	}
 
 	public void searchByYear() {
 		print("Please enter the year: ");
 		try {
 			int year = getInputInt();
 			_log.log("Trying to search for year: " + year);
 			WorldSeriesInstance wsi = _dataProcessor.showDataForYear(year);
 			if (wsi == null) println("No World Series held in " + year);
 			else println(wsi.toString());
 		}
 		catch (Exception e) { 
 			println("That is not a valid year.");
 			_log.log("User entered invalid year");
 		}
 	}
 
 	public String askUserForWinLossOrAll() {
 		print("Do you want to see World Series the team has (W)on, (L)ost, or (A)ll? ");
 		return getInputString();
 	}
 	
 	public String askUserForTeamName() {
 		print("Please enter the team name: ");
 		return getInputString();
 	}
 
 	public void searchByTeam() {
 		String team = askUserForTeamName();
 		String which = askUserForWinLossOrAll();
 		_log.log("Trying to search for: team=" + team + ", which=" + which);
 
 		if (which.equalsIgnoreCase("W"))
			println(_dataProcessor.showDataForTeam(team, true, false));
 		else if (which.equalsIgnoreCase("L"))
			println(_dataProcessor.showDataForTeam(team, false, true));
 		else if (which.equalsIgnoreCase("A"))
			println(_dataProcessor.showDataForTeam(team, true, true));
 		else
 			println(which + " is not a valid selection.");
 	}
 	
 	public void searchByRange() {
 		int startYear, endYear;
 		print("Please enter the starting year: ");
 		try {
 			startYear = getInputInt();
 			_log.log("Trying to search for range starting with: " + startYear);
 		}
 		catch (Exception e) { 
 			println("That is not a valid year.");
 			_log.log("User entered invalid year");
 			return;
 		}
 		print("Please enter the ending year: ");
 		try {
 			endYear = getInputInt();
 			_log.log("Trying to search for range starting with: " + endYear);
 		}
 		catch (Exception e) { 
 			println("That is not a valid year.");
 			_log.log("User entered invalid year");
 			return;
 		}
 		String yearData = _dataProcessor.showDataForRange(startYear, endYear);
 		println(yearData);
 	}
 	
 	public void showTeamsYears(){
 		String teamYearsData = _dataProcessor.showDataTeamsYears();
 		println(teamYearsData);
 	}
 	
 	public void print(String data) {
 		_out.print(data);
 	}
 
 	public void println(int data) {
 		_out.println(data);
 	}
 
 	public void println(String data) {
 		_out.println(data);
 	}
 	
 	public String getInputString() {
 		return _in.nextLine();
 	}
 
 	public int getInputInt() {
 		return _in.nextInt();
 	}
 	
 	public PrintStream getPrintstream() {
 		return System.out;
 	}
 }
