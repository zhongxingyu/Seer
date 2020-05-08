 package uk.ac.ebi.fgpt.webapp;
 
 import org.mged.magetab.error.ErrorCode;
 import org.mged.magetab.error.ErrorItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.Accessioner;
 import uk.ac.ebi.fgpt.sampletab.SampleTabcronBulk;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
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
             InputStream is = SampleTabcronBulk.class.getResourceAsStream("/mysql.properties");
             mysqlProperties.load(is);
         } catch (IOException e) {
             log.error("Unable to read resource mysql.properties");
             e.printStackTrace();
             return;
         }
         this.host = mysqlProperties.getProperty("hostname");
         this.port = new Integer(mysqlProperties.getProperty("port"));
         this.database = mysqlProperties.getProperty("database");
         this.username = mysqlProperties.getProperty("username");
         this.password = mysqlProperties.getProperty("password");
     }
     
     public void respondSimpleError(HttpServletResponse response, String message){
         //write error to log
         log.error(message);
         //write error to response
         //TODO add prettyfication to EBI standards
         Writer out = null;
         try {
             out = response.getWriter();
             out.write(message);
         } catch (IOException e) {
             log.error("Unable to generate a simple error for user");
             e.printStackTrace();
         } finally {
             if (out!= null){
                 try {
                     out.close();
                 } catch (IOException e) {
                     //do nothing
                 }
             }
             try {
                 response.flushBuffer();
             } catch (IOException e) {
                 //do nothing
             }
         }
     }
     
     @RequestMapping(value = "/accession", method = RequestMethod.POST)
     public void doAccession(@RequestParam("file")MultipartFile file, HttpServletResponse response) {
         
         //convert input into a sample data object
         InputStream is;
         try {
             is = file.getInputStream();
         } catch (IOException e) {
             e.printStackTrace();
             respondSimpleError(response, "Unable to recieve that SampleTab file. Contact administrator for more information.");
             //TODO output nice webpage of error
             return;
             //note: maximum upload filesize specified in sampletab-accessioner-config.xml
         }
         
         //parse the input
         SampleTabParser<SampleData> parser = new SampleTabParser<SampleData>();
         final List<ErrorItem> errorItems;
         errorItems = new ArrayList<ErrorItem>();
         parser.addErrorItemListener(new ErrorItemListener() {
             public void errorOccurred(ErrorItem item) {
                 errorItems.add(item);
             }
         });
         
         SampleData st = null;
         try{
             //TODO error listener
             st = parser.parse(is);
         } catch (ParseException e) {
             e.printStackTrace();
             respondSimpleError(response, "Unable to parse that SampleTab file. Contact administrator for more information.");
             //TODO output nice webpage of error
             return;
         } 
         
         //see if parsing threw errors
         if (!errorItems.isEmpty()) {
             // there are error items, print them and fail
             StringBuilder sb = new StringBuilder();
             for (ErrorItem item : errorItems) {
                 //look up the error code by ID to get human-readable string
                 ErrorCode code = null;
                 for (ErrorCode ec : ErrorCode.values()) {
                     if (item.getErrorCode() == ec.getIntegerValue()) {
                         code = ec;
                         break;
                     }
                 }
 
                 if (code != null) {
                     sb.append("Listener reported error...").append("\n");
                     sb.append("\tError Code: ").append(item.getErrorCode()).append(" [").append(code.getErrorMessage())
                             .append("]").append("\n");
                     sb.append("\tType: ").append(item.getErrorType()).append("\n");
                 } else {
                     sb.append("Listener reported error...");
                     sb.append("\tError Code: ").append(item.getErrorCode()).append("\n");
                 }
                 sb.append("\tLine: ").append(item.getLine() != -1 ? item.getLine() : "n/a").append("\n");
                 sb.append("\tColumn: ").append(item.getCol() != -1 ? item.getCol() : "n/a").append("\n");
                 sb.append("\tAdditional comment: ").append(item.getComment()).append("\n");
                 sb.append("\n");
                 
                respondSimpleError(response, sb.toString());
             }
             return;
         }
         
         
         //assign accessions
         Accessioner a;
         try {
             a = getAccessioner();
             st = a.convert(st);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             respondSimpleError(response, "Unable to connect to accession database. Contact administrator for more information.");
             //TODO output nice webpage of error
             return;
         } catch (SQLException e) {
             e.printStackTrace();
             respondSimpleError(response, "Unable to connect to accession database. Contact administrator for more information.");
             //TODO output nice webpage of error
             return;
         } catch (ParseException e) {
             e.printStackTrace();
             respondSimpleError(response, "Unable to assign accessions. Contact administrator for more information.");
             //TODO output nice webpage of error
             return;
         }
 
         //set it to be marked as a download file
         response.setContentType("application/octet-stream");
         //set the filename to download it as
         response.addHeader("Content-Disposition","attachment; filename=sampletab.txt");
         //writer to the output stream
         try {
             Writer out = new OutputStreamWriter(response.getOutputStream());
             SampleTabWriter sampletabwriter = new SampleTabWriter(out);
             sampletabwriter.write(st);
             sampletabwriter.close();
             response.flushBuffer();
         } catch (IOException e) {
             e.printStackTrace();
             respondSimpleError(response, "Unable to output SampleTab. Contact administrator for more information.");
             return;
         }
         
     }
     
     private Accessioner getAccessioner() throws ClassNotFoundException, SQLException{
         if (accessioner == null){
             accessioner = new Accessioner(host, port, database, username, password);
         }
         return accessioner;
     }
     
     
 }
