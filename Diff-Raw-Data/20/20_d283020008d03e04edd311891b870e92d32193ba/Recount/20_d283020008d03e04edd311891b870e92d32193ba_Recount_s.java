 package colloid.model;
 
 import java.io.File;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
import java.util.HashMap;
 import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 import colloid.model.combat.Combat;
 import colloid.model.combat.ICombat;
 
 import javafx.scene.control.TextArea;
 
 public class Recount {
 
     private String path;
     private static Recount instance;
     private boolean isRunning;
     private long fileTimeStamp;
 
     protected File[] logs;
     String lastLine = null;
     ArrayList<String> data = new ArrayList<String>();
     TextArea textLog;
     TextArea recountLog;
     ICombat combat;
 
 
     protected Recount() {
         combat = new Combat(this);
     }
     
     public TextArea getRecountLog() {
         return recountLog;
     }
 
     public static Recount getInstance() {
         if (instance == null) {
             instance = new Recount();
         }
 
         return instance;
     }
 
     public void run() {
         isRunning = true;
         new Thread(new Runnable() {
             @Override
             public void run() {
                 while (isRunning) {
                     parse();
                 }
             }
         }).start();
     }
 
     public void stop() {
         isRunning = false;
     }
 
     public boolean isRunning() {
         return isRunning;
     }
 
     public Recount setCombatDirPath(String dirPath) {
         init(dirPath);
         return this;
     }
 
     private void init(String path) {
         this.path = path;
         File dir = new File(path);
         logs = dir.listFiles();
     }
 
     public void parse() {
         if (logs == null || logs.length == 0) {
             return;
         }
         File log = logs[logs.length - 1];
         /**
          * @TODO
          */
         if (log.isFile()) {
             String tailLines = LogUtil.tail(log, 10);
             String[] lines = tailLines.split("\n");
             for (String line : lines) {
                 Date logTime = parseDate(line);
                 if (logTime == null) {
                     continue;
                 }
                 if (lastLine == null || !isLogged(logTime)) {
                     lastLine = line;
                     if (textLog != null && line != null) {
                         countCombat(line);
                         synchronized (textLog) {
                             if (line != null) { 
                                 textLog.insertText(0, line + "\n");
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private boolean isFileModified(File file) {
         if (fileTimeStamp != file.length()) {
             fileTimeStamp = file.length();
 
             return true;
         }
 
         return false;
     }
 
     private Date parseDate(String line) {
         final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.S",
                 Locale.getDefault());
         Date logTime;
         try {
             logTime = timeFormat.parse(line.substring(1, 13));
             return logTime;
         } catch (ParseException e) {
             System.out.println(e.getMessage());
         } catch (StringIndexOutOfBoundsException e) {
             System.out.println(e);
         }
 
         return null;
     }
 
     private boolean isLogged(Date logtime) {
         if (logtime.getTime() > parseDate(lastLine).getTime()) {
             return false;
         }
 
         return true;
     }
 
     public ArrayList<String> getData() {
         return data;
     }
 
     public String getLastLine() {
         return lastLine;
     }
 
     public void setTextLog(final TextArea textLog) {
         this.textLog = textLog;
     }
 
     public void setRecountLog(final TextArea recountLog) {
         this.recountLog = recountLog;
     }
 
     public void countCombat(String event) {
         combat.add(event).recount();
     }
 
     public ICombat getCombat() {
         return combat;
     }
 
     public void setCombat(ICombat combat) {
         this.combat = combat;
     }
 }
