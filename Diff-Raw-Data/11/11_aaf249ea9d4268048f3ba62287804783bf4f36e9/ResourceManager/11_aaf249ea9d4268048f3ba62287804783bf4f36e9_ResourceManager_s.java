 package program;
 
 import java.awt.Desktop;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Map;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import utilities.EmailHandler;
 import utilities.FileHelper;
 import utilities.PdfHandler;
 import utilities.RosterParser;
 import utilities.xml.XmlParser;
 //-----------------------------------------------------------------------------
 /**
  * The <code>ResourceManager</code> class manages the programs resources.
  * Upon creation, it performs a series of checks to determine what system 
  * resources are currently available to the program. The program prefers
  * to be as integrated as possible with the Desktop and will use the host
  * system's default browser, email client, pdf reader, etc to open and
  * manipulate files if the desktop is supported. If the Desktop API is not
  * available, alternative modules are loaded to compensate.
  * <code>ResourceManager</code> also checks to make sure that the proper files 
  * and directories necessary to the program's operation are found and creates 
  * any necessary directories that were not found. 
  */
 public class ResourceManager {
 	private Desktop desktop;
     private Desktop.Action action = Desktop.Action.OPEN;
     
     JFrame parent;
     PdfHandler pdfHandler;
     EmailHandler emailHandler;
 //-----------------------------------------------------------------------------
     protected ResourceManager(JFrame parent){
     	this.parent = parent;
     	
     	//Set up the classes controlling the program's actions
     	if (Desktop.isDesktopSupported()) {
             desktop = Desktop.getDesktop();
             // now enable buttons for actions that are supported.
             verifyAllActions();
         } else {
         	//load backup modules
         	
         }
     	
     	//Load the properties from the progProperties.xml file
     	loadProperties();
     	
     }
 //-----------------------------------------------------------------------------
 	/**
      * Check if each of the actions are enabled on this host. For any
      * actions not enabled load the backup modules to support them.
      * The actions: open browser, open email client, open, edit, 
      * and print files using their associated default application.
      * 
      */
     private void verifyAllActions() {
         if (!desktop.isSupported(Desktop.Action.BROWSE)) {
         	//load backup browser
         }
         if (!desktop.isSupported(Desktop.Action.MAIL)) {
             //load email handler
         }
         if (!desktop.isSupported(Desktop.Action.OPEN)) {
             //load backup pdf reader
         }
         if (!desktop.isSupported(Desktop.Action.EDIT)) {
         	//load backup pdf reader
         }
         if (!desktop.isSupported(Desktop.Action.PRINT)) {
             //load backup print mechanisms
         }
     }
 //-----------------------------------------------------------------------------
     /**
 	* Launch the default application associated with a specific
 	* filename using the preset Desktop.Action.
 	*
 	*/
 	public void launchDefaultApplication(ActionEvent evt, String fileName) {
 		File file = new File(fileName);
 		
 		try{
 			switch(action){
 			case OPEN:
 				desktop.open(file);
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 			
 		}
 		
     }
 //-----------------------------------------------------------------------------
     /**
 	* Launch the default application associated with a specific
 	* filename using the preset Desktop.Action.
 	*
 	*/
 	public void launchDefaultApplication(String fileName) {
 		File file = new File(fileName);
 		
 		try{
 			switch(action){
 			case OPEN:
 				desktop.open(file);
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 			
 		}
 		
     }
 //-----------------------------------------------------------------------------
 	/**
      * Launch the default email client using the "mailto"
      * protocol and the text supplied by the user.
      * @param destEmailAddr - the destination's email address
      */
     public void launchMail(ActionEvent evt, String destEmailAddr) {
         URI uriMailTo = null;
         try {
             if (destEmailAddr.length() > 0) {
                 uriMailTo = new URI("mailto", destEmailAddr, null);
                 desktop.mail(uriMailTo);
             } else {
                 desktop.mail();
             }
         } catch(IOException ioe) {
             ioe.printStackTrace();
         } catch(URISyntaxException use) {
             use.printStackTrace();
         }
     }
 //-----------------------------------------------------------------------------  
 	/**
 	 * Show the user an error dialog.
 	 * The error dialog's body will contain the specified message and it's title
 	 * will be the specified errorType. 
 	 * @param errorType - the type of error that occurred (i.e. I/O error)
 	 * @param errorMessage - the message accompanying the associated error
 	 * communicating to the user what went wrong and how (if at all) they 
 	 * can fix it
 	 */
 	public void showErrorDialog(String errorType, String errorMessage){
 		
 		JOptionPane.showMessageDialog(parent, errorMessage, errorType, 
 				JOptionPane.ERROR_MESSAGE);
 		
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Gets the parent frame and returns it to the calling method. 
 	 * Used mainly to create dialogs.
 	 * @return the program's top level parent JFrame
 	 */
 	public JFrame getGuiParent(){
 		return parent;
 	}
 //-----------------------------------------------------------------------------	 
 	/**
 	 * Load the additional Properties from the progProperties.xml file.
 	 */
 	public void loadProperties(){
 		String p = FileHelper.getPropertiesFile();
 	
 		//load the properties from the xml file
 		XmlParser.loadProperties(p);
 
 //DEBUG: print syst env
 		//printEnv();
 		
 	}
 //-----------------------------------------------------------------------------
 	public static void setLatestReportName(String reportFileName){
 		XmlParser.setSystemProperty("UMPD.latestReport", reportFileName);
 		System.setProperty("UMPD.latestReport", "reportFileName");
 	}
 	
 //-----------------------------------------------------------------------------	
 	public static ArrayList<String> getRollCall() {
 		int shiftTime;
 		ArrayList<String> Employees = new ArrayList();
 			
 		shiftTime = getShiftTime();
 		if (shiftTime == -1) {
 			System.out.println("Couldn't get shift time");
 			System.exit(0);
 		}
 			
 		RosterParser parser = new RosterParser();
 		Employees = parser.getEmployeesOnShift(shiftTime);
 		return Employees;
 	}
 //-----------------------------------------------------------------------------	
 	private static int getShiftTime() {
 		int currentHour, currentMin, shiftTime;
 		Calendar cal;
 		
 		/*
 		 * determine the time of the nearest shift, if greater than 
 		 * half an hour past a shift, go to the next one
 		 */
 		shiftTime = -1;
 		cal = Calendar.getInstance();
		currentHour = cal.get(Calendar.HOUR);
 		currentMin = cal.get(Calendar.MINUTE);
 			
 		if (currentHour < 6) 
 			shiftTime = 6;
 		else if (currentHour == 6) {
 			if (currentMin <= 30)
 				shiftTime = 6;
 			else
 				shiftTime = 10;
 		}
 		else if (currentHour < 10)
 			shiftTime = 10;
 		else if (currentHour == 10) {
 			if (currentMin <= 30)
 				shiftTime = 10;
 			else 
 				shiftTime = 18;
 		}
 		else if (currentHour < 18)
 			shiftTime = 18;
 		else if (currentHour == 18) {
 			if (currentMin <= 30)
 				shiftTime = 18;
 			else 
 				shiftTime = 22;
 		}
 		else if (currentHour < 22)
 			shiftTime = 22;
 		else if (currentHour == 22) 
 				shiftTime = 22;
 		else 
 			shiftTime = 22;
 		return shiftTime;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * DEBUGGER: Print the system environment 
 	 */
 	public void printEnv(){
 		//get the syst env
 		Map<String, String> env = System.getenv();
 		//go thru the properties and print each key/value pair
 	    for (String envName : env.keySet()) {
 	        System.out.format("%s=%s%n",
 	                          envName,
 	                          env.get(envName));
 	    }
 	
 	}
 //-----------------------------------------------------------------------------
 }
