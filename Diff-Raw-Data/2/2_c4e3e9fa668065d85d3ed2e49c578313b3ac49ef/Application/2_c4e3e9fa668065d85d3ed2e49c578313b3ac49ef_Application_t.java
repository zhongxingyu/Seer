 package spreadsheet;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import spreadsheet.NoSuchSpreadsheetException;
 import spreadsheet.Spreadsheet;
 
 /** A singleton class representing a spreadsheet application.
  *
  * The instance is initialized on first mention of the class.
  */
 public final class Application {
 
   private ArrayList<Spreadsheet> spreadsheets;
   private Spreadsheet worksheet;
   public static ArrayList<String> saveVariables;
 
   public static final Application instance = new Application();
 
   private Application() {
     this.worksheet = new Spreadsheet();
     this.spreadsheets = new ArrayList<Spreadsheet>();
     this.spreadsheets.add(this.worksheet);
    Application.saveVariables = new ArrayList<String>();
   }
 
   public void exit() {
     System.exit(0);
   }
 
   public Spreadsheet getWorksheet() {
     return worksheet;
   }
   
   public void newSpreadsheet() {
     this.spreadsheets.add(new Spreadsheet());
   }
   
   public void changeWorksheet(final String name)
     throws NoSuchSpreadsheetException{
     Iterator<Spreadsheet> it = spreadsheets.iterator();
     Spreadsheet spreadsheet = it.next();
     Boolean found = false;
     while (it.hasNext() && !found) {
       if (spreadsheet.getName().equals(name)) {
         this.worksheet = spreadsheet;
         found = true;
       }
       else {
         spreadsheet = it.next();
       }
     }
     if (!spreadsheet.getName().equals(name) && !found) {
       throw new NoSuchSpreadsheetException(name);
     }
     else {
       this.worksheet = spreadsheet;
     }
   }
   
   public Spreadsheet getSpreadsheet(final String name)
     throws NoSuchSpreadsheetException{
     Iterator<Spreadsheet> it = spreadsheets.iterator();
     Spreadsheet spreadsheet = it.next();
     while (it.hasNext()) {
       if (spreadsheet.getName().equals(name)) {
         return spreadsheet;
       }
       else {
         spreadsheet = it.next();
       }
     }
     throw new NoSuchSpreadsheetException(name);
   }
   
   public Iterable<String> listSpreadsheets() {
 	 
 	ArrayList<String> nameArray = new ArrayList<String>();
 	
 	for (Spreadsheet s : spreadsheets) {
 		
 		nameArray.add(s.getName());
 	}
 	
 	Iterable<String> names = nameArray;
 	
     return names;
   }
 }
