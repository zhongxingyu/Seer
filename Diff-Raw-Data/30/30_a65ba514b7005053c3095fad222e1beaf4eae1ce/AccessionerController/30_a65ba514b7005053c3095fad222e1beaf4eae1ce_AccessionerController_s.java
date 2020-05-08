 package uk.ac.ebi.fgpt.webapp;
 
 import org.mged.magetab.error.ErrorItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.fgpt.sampletab.Accessioner;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * A spring controller that returns an accessioned version of a POSTed SampleTab
  *
  * @author Adam Faulconbridge
  * @date 02/05/12
  */
 @Controller
 @RequestMapping
 public class AccessionerController {
     
     private String host;
     private int port;
     private String database;
     private String username;
     private String password;
     private Accessioner accessioner;
     
     private Logger log = LoggerFactory.getLogger(getClass());
     
     public AccessionerController(){
 
         Properties mysqlProperties = new Properties();
         try {
             InputStream is = AccessionerController.class.getResourceAsStream("/mysql.properties");
             mysqlProperties.load(is);
         } catch (IOException e) {
             log.error("Unable to read resource mysql.properties", e);
             return;
         }
         this.host = mysqlProperties.getProperty("hostname");
         this.port = new Integer(mysqlProperties.getProperty("port"));
         this.database = mysqlProperties.getProperty("database");
         this.username = mysqlProperties.getProperty("username");
         this.password = mysqlProperties.getProperty("password");
     }
     
     /*
      * Echoing function. Used for triggering download of javascript
      * processed sampletab files. No way to download a javascript string
      * directly from memory, so it is bounced back off the server through
      * this method.
      */
     @RequestMapping(value = "/echo", method = RequestMethod.POST)
     public void echo(String input, HttpServletResponse response) throws IOException {
 
         log.debug("Recieved echo: "+input);
         
         
         //set it to be marked as a download file
         response.setContentType("application/octet-stream");
         //set the filename to download it as
         response.addHeader("Content-Disposition","attachment; filename=sampletab.txt");
 
         //writer to the output stream
         //let springs default error handling take over and redirect on error.
         Writer out = null; 
         try {
             out = new OutputStreamWriter(response.getOutputStream());
             out.write(input);
         } finally {
             if (out != null){
                 try {
                     out.close();
                     response.flushBuffer();
                 } catch (IOException e) {
                     //do nothing
                 }
             }
         }
         
     }
         
     @RequestMapping(value = "/jsac", method = RequestMethod.POST)
     public @ResponseBody Outcome doAccession(@RequestBody SampleTabRequest sampletab) {
         //setup parser to listen for errors
         SampleTabParser<SampleData> parser = new SampleTabParser<SampleData>();
         
         final List<ErrorItem> errorItems;
         errorItems = new ArrayList<ErrorItem>();
         parser.addErrorItemListener(new ErrorItemListener() {
             public void errorOccurred(ErrorItem item) {
                 errorItems.add(item);
             }
         });
          
         try {
             //convert json object to string
             String singleString = sampletab.asSingleString();
             
             //setup the string as an input stream
             InputStream is = new ByteArrayInputStream(singleString.getBytes("UTF-8"));
             
             //parse the input into sampletab
             SampleData sampledata = parser.parse(is);
             
             //assign accessions to sampletab object
             Accessioner accessioner = getAccessioner();
             sampledata = accessioner.convert(sampledata);
             
             //return the accessioned file, and any generated errors            
             return new Outcome(sampledata, errorItems);
            
         } catch (ParseException e) {
             //catch parsing errors for malformed submissions
            log.error(e.getMessage());
            return new Outcome(null, e.getErrorItems());
         } catch (Exception e) {
             //general catch all for other errors, e.g SQL
            log.error(e.getMessage());
            return new Outcome();
         } 
     }
     
     
     private Accessioner getAccessioner() throws ClassNotFoundException, SQLException{
         if (accessioner == null){
             accessioner = new Accessioner(host, port, database, username, password);
         }
         return accessioner;
     }
     
     
     
 }
