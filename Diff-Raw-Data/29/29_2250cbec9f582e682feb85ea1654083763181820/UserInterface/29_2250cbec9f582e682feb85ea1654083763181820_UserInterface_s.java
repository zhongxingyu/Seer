 import java.io.PrintStream;
 
 /*
  * This class is declared abstract because the getInputString and getInputInt methods
  * need to be overridden, depending on what type of UI you're using.
  * 
  * Additionally, the PrintStream object is not initialized here.
  * It is up to a subclass to initialize the PrintStream to the appropriate instance.
  */
 
 public interface UserInterface {
 
 	public String getInputString();
 	
 	public int getInputInt();
 			
 	public void start();
 	
 	public void showPrompt();
 
 	public void searchByYear();
 
 	public String askUserForWinLossOrAll();
 	
 	public String askUserForTeamName();

<<<<<<< HEAD
	public void searchByTeam();
=======
		if (which.equalsIgnoreCase("W"))
			println(_dataProcessor.showDataForTeam(team, true, false));
		else if (which.equalsIgnoreCase("L"))
			println(_dataProcessor.showDataForTeam(team, false, true));
		else if (which.equalsIgnoreCase("A"))
			println(_dataProcessor.showDataForTeam(team, true, true));
		else
			println(which + " is not a valid selection.");
	}
>>>>>>> a0f6ba6de8f41d236b796b4d3f249b45fce7922e
 	
 	public void searchByRange();
 	
 	public void showTeamsYears();
 	
 	public void print(String data);
 
 	public void println(int data);
 
 	public void println(String data);
 	
 	public PrintStream getPrintstream();
 }
