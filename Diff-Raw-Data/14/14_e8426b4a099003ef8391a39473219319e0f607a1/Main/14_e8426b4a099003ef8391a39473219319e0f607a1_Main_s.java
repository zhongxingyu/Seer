 import java.sql.SQLException;
 
 import pizzaProgram.database.DatabaseConnection;
 import pizzaProgram.events.EventDispatcher;
 import pizzaProgram.gui.ProgramWindow;
 
 /**
  * The Main class acts as the root of the system. Its main task is to hold references and initialize various parts/modules
  * @author Bart
  *
  */
 public class Main {
 	/**
 	 * A reference to the main event dispatcher, which represents the communication backbone of the program
 	 */
 	public EventDispatcher eventDispatcher;
 	
 	/**
 	 * A reference to the class that managed the program's main window
 	 */
 	private ProgramWindow programWindow;
 	
 	
 	private DatabaseConnection databaseConnection;
 	
 	/**
 	 * creates every part of the program, and sets it up correctly
 	 */
 	public void initialize()
 	{
 		this.eventDispatcher = new EventDispatcher();
 		this.connectToDatabase();
 		this.createMainWindow();
		
 	}
 	
 	private void createMainWindow()
 	{
 		this.programWindow = new ProgramWindow(this.eventDispatcher);
 		this.programWindow.createMenuBar();
 		this.programWindow.createMainWindow(640, 480);
 		this.programWindow.showOrder(databaseConnection);
 	}
 	
 	private void connectToDatabase()
 	{
 		try {
 			this.databaseConnection = new DatabaseConnection();
 			this.databaseConnection.connect();
 			this.databaseConnection.buildContents();
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 }
