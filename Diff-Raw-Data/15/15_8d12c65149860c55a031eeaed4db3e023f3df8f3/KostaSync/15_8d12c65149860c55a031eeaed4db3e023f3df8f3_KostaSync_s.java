 package org.kostausa.sync;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.google.gdata.client.spreadsheet.SpreadsheetService;
 import com.google.gdata.data.spreadsheet.CustomElementCollection;
 import com.google.gdata.data.spreadsheet.ListEntry;
 import com.google.gdata.data.spreadsheet.ListFeed;
 import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
 import com.google.gdata.data.spreadsheet.WorksheetEntry;
 import com.google.gdata.data.spreadsheet.WorksheetFeed;
 import com.google.gdata.util.ServiceException;
 
 /**
  * Sync Service between KOSTA Database and Google
  * 
  * @author eungyu
  *
  */
 public class KostaSync 
 { 
   private final static Logger LOG = Logger.getLogger(KostaSync.class);
   
   private final URL                _targetUrl;
   private final SpreadsheetService _service;
   private final Map<String, String> _keymap;
   
   /**
    * Constructor
    * 
    * @param targetUrl target spreadsheet feed to connect to 
    * @throws ServiceException
    * @throws IOException 
    * @throws FileNotFoundException 
    */
   public KostaSync(URL targetUrl)
     throws ServiceException
   {
     // the dirty work of authenticating is hidden to a factory
     _service = GoogleServiceFactory.createAuthenticatedService();
     
     _keymap = new HashMap<String, String>();
     _keymap.put(Kostan.NAME_COLUMN, "성명한글");
     _keymap.put(Kostan.GENDER_COLUMN, "성별");
     _keymap.put(Kostan.EMAIL_COLUMN, "emailaddress");
     
     _targetUrl = targetUrl;
   }
 
   /**
    * Main loop of instantiating Kostan 
    * and syncing them with MySQL database
    * 
    * @throws IOException
    * @throws ServiceException
    * @throws SQLException 
    */
   public void run() throws IOException, ServiceException, SQLException
   {
     SpreadsheetEntry spreadsheet = _service.getEntry(_targetUrl, SpreadsheetEntry.class);
     
     WorksheetFeed feed = _service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
     List<WorksheetEntry> worksheets = feed.getEntries();
     
     WorksheetEntry worksheet = worksheets.get(0); // get the first
     ListFeed records = _service.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
     
     UserStore userStore = new UserStore(Conference.INDIANAPOLIS);
     
     int count = 0;
     for (ListEntry entry : records.getEntries())
     {
       Kostan person = null;
       try
       {
         String nameKey = _keymap.get(Kostan.NAME_COLUMN);
         String emailKey = _keymap.get(Kostan.EMAIL_COLUMN);
         String genderKey = _keymap.get(Kostan.GENDER_COLUMN);
 
         CustomElementCollection columns = entry.getCustomElements();
         
         person = new Kostan(Conference.INDIANAPOLIS, 
                             columns.getValue(nameKey),
                            columns.getValue(emailKey),
                            columns.getValue(genderKey));
       }
       
       catch (IncompleteRecordException e)
       {
        LOG.warn("Skipping user");
       }
       
       if (userStore.exists(person))
       {
         continue;
       }
           
       try
       {
         userStore.save(person);
       }
       catch (SQLException e)
       {
         LOG.warn("Failed to insert record " + person.toString());
         continue;
       }
       
       count++;
       LOG.info("Saved " + person.toString());
     }    
     
     LOG.info("Added " + count + " new records");
   }
 
   /**
    * Main entry point
    * 
    * @param args
    * @throws IOException
    * @throws ServiceException
    */
   public static void main(String[] args) throws IOException, ServiceException
   {
     BasicConfigurator.configure();
     LOG.setLevel(Level.INFO);
 
     LOG.info("=============");
     LOG.info("Starting Sync");
     
     URL indyUrl = 
         new URL("https://spreadsheets.google.com/feeds/spreadsheets/tyeT6YZgHyssi7AA9AAsQEw");
     
     KostaSync sync = null;
     try
     {
       sync = new KostaSync(indyUrl);
     }
     catch (ServiceException e)
     {
       LOG.warn("Failed to initialize service");
       return;
     }
   
     try
     {
       sync.run();
     }
     catch (Exception e)
     {
       LOG.error("Sync process stopped abnormally");
       LOG.error(e.getMessage());
       return;
     }    
     
   }
 }
