 
 package se.nctrl.jenkins.plugin;
 
 import hudson.model.AbstractBuild;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author karl
  */
 public class CTSuiteLogParser extends CTLogParser {
 
     private enum Fields {
 
         CASES,
         USER,
         HOST,
         HOSTS,
         LAN,
         EMULATOR_VSN,
         EMULATOR,
         OTP_RELEASE,
         STARTED,
         CASE,
         LOGFILE,
         ENDED,
         RESULT,
         ELAPSED,
         GROUP_TIME,
         FINISHED,
         FAILED,
         SUCCESSFUL,
         USER_SKIPPED,
         AUTO_SKIPPED,
         GROUP_PROPS,
         NODE_START,
         NODE_STOP
     }
     
     private static Pattern field_pattern = Pattern.compile("^=(\\w+)\\s+(.+)$");
     private static Pattern comment_pattern = Pattern.compile("^===.+$");
     
     private static final Logger logger = Logger.getLogger(CTSuiteLogParser.class.getName());
     
     private BufferedReader br;
     
     private CTResult tr_root = null;
     private CTResult tr_current_child = null;
     private boolean parsing_child = false;
     private String suite_path;
 
     public CTSuiteLogParser(AbstractBuild build) {
         
         super(build);
     }
     
     public CTResult parse(File f) throws FileNotFoundException, IOException {
         
         this.tr_root = new CTResult(this.getBuild());
         //this.suite_path = f.
         
         FileInputStream fs = new FileInputStream(f);
         this.br = new BufferedReader(new InputStreamReader(fs, "UTF-8"));
         
         try {
             while (true) {
                 String l = br.readLine();
 
                 if (l == null) {
                     break;
                 }
 
                 Matcher f_m = field_pattern.matcher(l);
                 
                 if (f_m.matches()) {
                     String fieldname = f_m.group(1);
                     String value = f_m.group(2);
                     parseField(fieldname,value);
                 }
                                
             }
         } finally {
             br.close();
         }
         
         return tr_root;
     }
 
     private void parseField(String fieldname, String value) throws IOException
     {
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
         logger.log(Level.FINE, "parsed : field = {0}, value = {1}", new Object[]{fieldname, value});
 
         Fields f = Fields.valueOf(fieldname.toUpperCase());
         
 
         switch (f) {
 
             case CASES:
                 int cases;
                 if (value.equals("unknown")) { // this is apparently possible
                     cases = 0;
                     logger.log(Level.WARNING, "Cases had value 'unknown'");
                 } else {              
                     cases = Integer.parseInt(value); 
                 }
                 
                 logger.log(Level.FINE, "Set cases to {0}", cases);
                 this.tr_root.setCases(cases);
                 break;
 
             case USER:
                 logger.log(Level.FINE, "Set user to {0}", value);
                 this.tr_root.setUser(value);
                 break;
 
             case HOST:
                 logger.log(Level.FINE, "Set host to {0}", value);
                 this.tr_root.setHost(value);
                 break;
 
             case HOSTS:
                 logger.log(Level.FINE, "Set hosts to {0}", value);
                 this.tr_root.setHosts(value);
                 break;
 
             case LAN:
                 logger.log(Level.FINE, "Set lan to {0}", value);
                 this.tr_root.setLan(value);
                 break;
 
             case EMULATOR_VSN:
                 logger.log(Level.FINE, "Set emulator_vsn to {0}", value);
                 this.tr_root.setEmulator_vsn(value);
                 break;
 
             case EMULATOR:
                 logger.log(Level.FINE, "Set emulator to {0}", value);
                 this.tr_root.setEmulator(value);
                 break;
 
             case OTP_RELEASE:
                 logger.log(Level.FINE, "Set otp_release to {0}", value);
                 this.tr_root.setOtp_release(value);
                 break;
 
             case STARTED:
 
                 Date started_date;
                 try {
                     started_date = df.parse(value);
                     logger.log(Level.FINE, "Set date to {0}", started_date.toString());
                 } catch (ParseException ex) {
                     started_date = null;
                     logger.log(Level.SEVERE, "Error while parsing date.");
                 }
                 if (this.parsing_child && this.tr_current_child != null) {
                     this.tr_current_child.setStarted(started_date);
                 } else if (!this.parsing_child) {
                     this.tr_root.setStarted(started_date);
                 } else {
                     logger.log(Level.SEVERE, "Unexpected date-field.");
                 }
                 break;
             case CASE:
 
                 if (this.parsing_child) {
                     this.tr_root.addChild(this.tr_current_child);
                     
                 }
 
                 logger.log(Level.FINE, "Creating new child = {0}", value);
 
                 this.tr_current_child = new CTResult(this.getBuild());
                 this.tr_current_child.setCase_name(value);
                 this.parsing_child = true;
 
 
                 break;
             case LOGFILE:
                 if (this.parsing_child && this.tr_current_child != null) {
                     logger.log(Level.FINE, "Set logfile to {0}", value);
                     this.tr_current_child.setLog_file(value);
                 } else if (!this.parsing_child) {
                     logger.log(Level.SEVERE, "Unexpected logfile-field.");
 
                 }
                 break;
             case ENDED:
 
                 if (this.parsing_child && this.tr_current_child != null) {
                     Date ended_date;
                     try {
                         ended_date = df.parse(value);
                         logger.log(Level.FINE, "Set date to {0}", ended_date.toString());
                     } catch (ParseException ex) {
                         ended_date = null;
                         logger.log(Level.SEVERE, "Error while parsing date.");
                     }
                     this.tr_current_child.setEnded(ended_date);
                 } else if (!this.parsing_child) {
                     logger.log(Level.SEVERE, "Unexpected ended-field.");
 
                 }
                 break;
 
             case RESULT:
                 int res = -1;
 
                 String value2 = this.readMultiLine();
                 String value3 = value + value2;
 
                 if (this.parsing_child && this.tr_current_child != null) {
 
                     if (value3 != null) {
                         if (value3.substring(0, 2).toUpperCase().equals("OK")) {
                             logger.log(Level.FINE, "Set result to OK");
                             res = 1;
                         } else if (value3.substring(0, 6).toUpperCase().equals("FAILED")) {
                             logger.log(Level.FINE, "Set result to FAILED");
                             res = 0;
                         } else if (value3.substring(0, 7).toUpperCase().equals("SKIPPED")) {
                             logger.log(Level.FINE, "Set result to SKIPPED");
                             res = 2;
                         } else {
                             logger.log(Level.SEVERE, "Unable to parse result-field (Invalid result)");
                         }
                     } else {
                         logger.log(Level.SEVERE, "Unable to parse result-field (Empty result)");
                     }
                     this.tr_current_child.setResult(res);
                     this.tr_current_child.setResult_msg(value3);
                 } else if (!this.parsing_child) {
                     logger.log(Level.SEVERE, "Unexpected result-field.");
                 }
                 break;
 
             case ELAPSED:
                 if (this.parsing_child && this.tr_current_child != null) {
                 float elapsed = Float.parseFloat(value);
                 logger.log(Level.FINE, "Set elapsed to {0}", elapsed);
                 this.tr_root.setElapsed(elapsed); 
                 }  else if (!this.parsing_child) {
                     logger.log(Level.SEVERE, "Unexpected elapsed-field.");
                 }
                 break;
 
             case GROUP_TIME:
                 logger.log(Level.FINE, "Set group time to {0}", value);
                 this.tr_root.setGroup_time(value);
 
                 break;
             case FINISHED:
                 Date finished_date;
                 try {
                     finished_date = df.parse(value);
                     logger.log(Level.FINE, "Set finished date to {0}", finished_date.toString());
                 } catch (ParseException ex) {
                     finished_date = null;
                     logger.log(Level.SEVERE, "Error while parsing date.");
                 }
                this.tr_root.setFinished(finished_date);
                 break;
 
             case SUCCESSFUL:
                 int successful = Integer.parseInt(value);
                 logger.log(Level.FINE, "Set successful to {0}", successful);
                 this.tr_root.setSuccessful(successful);
 
                 break;
             case FAILED:
                 int failed = Integer.parseInt(value);
                 logger.log(Level.FINE, "Set failed to {0}", failed);
                 this.tr_root.setSuccessful(failed);
 
                 break;
             case USER_SKIPPED:
                 int user_skipped = Integer.parseInt(value);
                 logger.log(Level.FINE, "Set user_skipped to {0}", user_skipped);
                 this.tr_root.setUser_skipped(user_skipped);
 
                 break;
             case AUTO_SKIPPED:
                 int auto_skipped = Integer.parseInt(value);
                 logger.log(Level.FINE, "Set auto_skipped to {0}", auto_skipped);
                 this.tr_root.setAuto_skipped(auto_skipped);
 
 
                 break;
             case GROUP_PROPS:
                 String gp_value2 = this.readMultiLine();
                 String gp_value3 = value + gp_value2;
 
                 this.tr_root.setGroup_props(gp_value3);
 
                 break;
                 
             case NODE_START:
                 logger.log(Level.FINE, "Set node start to {0}", value);
                 this.tr_root.setNode_start(value);
                 break;
             
              case NODE_STOP:
                 logger.log(Level.FINE, "Set node stop to {0}", value);
                 this.tr_root.setNode_stop(value);
                 break;    
                 
             default:
         }
 
 
     }
     
     private String readMultiLine() throws IOException {
         StringBuilder sb = new StringBuilder();
         boolean done = false;
 
         while (!done) {
             br.mark(1024); 
             String l = br.readLine();
             if (l != null)
             {
               Matcher f_m = field_pattern.matcher(l); 
               Matcher c_m = comment_pattern.matcher(l);
               if (f_m.matches() || c_m.matches()) {
                   br.reset(); // we've read past the multiline value
                   done = true;
               } else {
                   sb.append(l);                 
               }
             } else { done = true; }
             
         }
         
         return sb.toString();
     }
     
 }
