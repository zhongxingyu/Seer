 package uk.ac.ebi.fgpt.webapp;
 
 import org.mged.magetab.error.ErrorItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
 import uk.ac.ebi.arrayexpress2.magetab.validator.Validator;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.arrayexpress2.sampletab.validator.SampleTabValidator;
 import uk.ac.ebi.fgpt.sampletab.Accessioner;
 import uk.ac.ebi.fgpt.sampletab.AccessionerENA;
 import uk.ac.ebi.fgpt.sampletab.Corrector;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * A spring controller that returns an accessioned version of a POSTed SampleTab
  *
  * @author Adam Faulconbridge
  * @date 02/05/12
  */
 @Controller
 @RequestMapping
 public class SubmissionController {
         
     private Logger log = LoggerFactory.getLogger(getClass());
                 
     private final File path;
     private final Pattern pattern = Pattern.compile("^GSB-\\(([0-9]++)\\)$");
     private AccessionerENA accessioner;
     
     private Corrector corrector;
     
     public SubmissionController(){
         Properties properties = new Properties();
         try {
             InputStream is = SubmissionController.class.getResourceAsStream("/sampletab.properties");
             properties.load(is);
         } catch (IOException e) {
             log.error("Unable to read resource properties", e);
             path = null;
             return;
         }
         path = new File(properties.getProperty("submissionpath"));
         if (!path.exists()){
             //TODO throw error
             log.error("Submission path "+path+" does not exist");
         }
         
         properties = new Properties();
         try {
             InputStream is = AccessionerController.class.getResourceAsStream("/oracle.properties");
             properties.load(is);
         } catch (IOException e) {
             log.error("Unable to read resource properties", e);
             return;
         }
         
         String host = properties.getProperty("hostname");
         Integer port = new Integer(properties.getProperty("port"));
         String database = properties.getProperty("database");
         String username = properties.getProperty("username");
         String password = properties.getProperty("password");
         try {
             accessioner = new AccessionerENA(host, port, database, username, password);
         } catch (ClassNotFoundException e) {
             log.error("Unable to create accessioner", e);
             accessioner = null;
         } catch (SQLException e) {
             log.error("Unable to create accessioner", e);
             accessioner = null;
         }
         
         corrector = new Corrector();
     }
     
     
     private synchronized int getNewSubID() throws IOException{
         int maxSubID = 0;
        for (File subdir : path.listFiles()){
             if (!subdir.isDirectory()){
                 continue;
             } else {
                 Matcher match = pattern.matcher(subdir.getName());
                 if (match != null){
                     Integer subid = new Integer(match.group(1));
                     if (subid > maxSubID){
                         maxSubID = subid;
                     }
                 }
             }
         }
         maxSubID++;
         File subDir = SampleTabUtils.getSubmissionDirFile("GSB-"+maxSubID);
         if (!subDir.mkdirs()){
             throw new IOException("Unable to create submission directory");
         }
         return maxSubID;
         
     }
     
     private Outcome getErrorOutcome(String message, String comment) {
         Outcome o = new Outcome();
         List<Map<String,String>> errorList = new ArrayList<Map<String,String>>();
         Map<String, String> errorMap = new HashMap<String, String>();
         //errorMap.put("type", errorItem.getErrorType());
         //errorMap.put("code", new Integer(errorItem.getErrorCode()).toString());
         //errorMap.put("line", new Integer(errorItem.getLine()).toString());
         //errorMap.put("col", new Integer(errorItem.getCol()).toString());
         errorMap.put("message", message);
         errorMap.put("comment", comment);
         errorList.add(errorMap);
         o.setErrors(errorList);
         return o;
         
     }
     
     
     @RequestMapping(value = "/v1/json/sb", method = RequestMethod.POST)
     public @ResponseBody Outcome doSubmission(@RequestBody SampleTabRequest sampletab, String apikey) {
         boolean isSRA = false;
         boolean isCGAP = false;
         //test API key
         if (apikey != null && apikey.equals("NZ80KZ7G13NHYDM3")) {
             //SRA
             isSRA = true;
         } else if (apikey != null && apikey.equals("XWURYU77KWT663IQ")) {
             //CGAP
             isCGAP = true;
         } else if (apikey != null && apikey.equals("FZJ5VRBEZEJ5ZDP8")) {
             //BBMRI.eu
             isCGAP = true;
         } else {
             //invalid API key, return errors
             return getErrorOutcome("Invalid API key ("+apikey+")", "Contact biosamples@ebi.ac.uk for assistance");
         }
         
         // setup an overall try/catch to catch and report all errors
         try {
             //setup parser to listen for errors
             SampleTabParser<SampleData> parser = new SampleTabParser<SampleData>();
             
             final List<ErrorItem> errorItems;
             errorItems = new ArrayList<ErrorItem>();
             parser.addErrorItemListener(new ErrorItemListener() {
                 public void errorOccurred(ErrorItem item) {
                     errorItems.add(item);
                 }
             });
             SampleData sampledata = null;
             //convert json object to string
             String singleString = sampletab.asSingleString();
             
             //setup the string as an input stream
             InputStream is = new ByteArrayInputStream(singleString.getBytes("UTF-8"));
              
             try {
                 //parse the input into sampletab
                 //will also validate
                 sampledata = parser.parse(is);
             } catch (ParseException e) {
                 //catch parsing errors for malformed submissions
                 log.error("parsing error", e);
                 return new Outcome(null, e.getErrorItems());
             } 
             
             //look at submission id
             if (isSRA) {
                 //extra validation for SRA
                 if (!sampledata.msi.submissionIdentifier.matches("^GEN-[ERD]R[AP][0-9]+$")) {
                     return getErrorOutcome("Submission identifier invalid", "SRA submission identifier must match regular expression ^GEN-[SED]R[AP][0-9]+$");
                 }               
             } else {
                 //must be a GSB submission ID
                 if (sampledata.msi.submissionIdentifier == null || sampledata.msi.submissionIdentifier.length() == 0) {
                     sampledata.msi.submissionIdentifier = "GSB-"+getNewSubID();
                 } else if (!sampledata.msi.submissionIdentifier.matches("^GSB-[1-9][0-9]*$")) {
                     return getErrorOutcome("Submission identifier invalid", "Submission identifier must match regular expression ^GSB-[1-9][0-9]*$");
                 }
             }
             File subdir = SampleTabUtils.getSubmissionDirFile(sampledata.msi.submissionIdentifier);
             subdir = new File(path.toString(), subdir.toString());
             File outFile = new File(subdir, "sampletab.pre.txt");
 
             
             //assign accessions to sampletab object
             synchronized(accessioner) {
                 sampledata = accessioner.convert(sampledata);
             }
             
             //correct errors
             synchronized(corrector) {
                 corrector.correct(sampledata);
             }
             
             SampleTabWriter writer = null;
             try {
                 writer = new SampleTabWriter(new BufferedWriter(new FileWriter(outFile)));
                 writer.write(sampledata);
             } catch (IOException e) {
                 log.error("Problem writing to "+outFile, e);
                 return getErrorOutcome("Unable to store submission", "Retry later or contact biosamples@ebi.ac.uk if this error persists");
             } finally {
                 if (writer != null) {
                     try {
                         writer.close();
                     } catch (IOException e) {
                         //do nothing
                     }
                 }
             }
             
             //return the submitted file, and any generated errors            
             return new Outcome(sampledata, errorItems);
             
         } catch (Exception e) {
             //general catch all for other errors, e.g SQL, IO
             log.error("Unrecognized error", e);
             return getErrorOutcome("Unknown error", "Contact biosamples@ebi.ac.uk for assistance");
         } 
     }
     
     
 }
