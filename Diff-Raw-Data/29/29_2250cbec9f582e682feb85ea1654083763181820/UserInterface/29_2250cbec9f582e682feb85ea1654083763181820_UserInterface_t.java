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

	public void searchByTeam();
 	
 	public void searchByRange();
 	
 	public void showTeamsYears();
 	
 	public void print(String data);
 
 	public void println(int data);
 
 	public void println(String data);
 	
 	public PrintStream getPrintstream();
 }
