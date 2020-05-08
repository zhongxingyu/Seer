 package org.dialogix.util;
 
 import org.dialogix.loader.InstrumentExcelLoader;
 import org.dianexus.triceps.parser.*;
 import java.io.*;
 import java.util.*;
 import java.util.logging.*;
 import org.dianexus.triceps.Datum;
 import org.dianexus.triceps.Triceps;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 
 
 /**
 Unit testing program.  Passed one or more equw ations; returns the results as Strings; 
 logs errors; and maintains history of parsed equations for insertion into a 5 column HTML table.
  */
 public class DialogixParserTool implements java.io.Serializable {
 
     private String filenameList;
     private Logger logger = Logger.getLogger("org.dianexus.triceps.DialogixParserTool");
     private DialogixParser parser = new DialogixParser(new StringReader(""));
     private StringBuffer queryHistory = new StringBuffer();
     private int numQueries = 0;
 
     public void DialogixParserTool() {
     }
 
     public void ConnectDatabase() {
     }
 
     public Connection createConnection() {
         Connection con = null;
         try {
             Class.forName("com.mysql.jdbc.Driver").newInstance();
             con = DriverManager.getConnection("jdbc:mysql://localhost:3306/dialogix_j2ee?useUnicode=yes&characterEncoding=UTF-8", 
                     "dialogix_j2ee", "dialogix_j2ee_pass");           
         } catch (Exception e) {
             logger.log(Level.SEVERE, e.getMessage(), e);
         }
         return con;
     }
 
     /**
     Main function for parsing an equation.  
     InputFormat:
     <equation> <tab> <expected result>
     Separates the input into multiple lines; splits on TAB to get equation and expected result; 
     Parses each one and logs the results, answer, whether matches expected answer, errors, and dependencies
     @param eqn  The string (can be multi-lined) of equations to parse
     @return The final answer
      */
     public String parse(String eqn, String request) {
         Connection con = createConnection();
 	Statement stmt = null;
         String output = null;
         String result = "*EMPTY*";
         if (logger.isLoggable(Level.FINER)) {
             logger.log(Level.FINER, "Parsing: " + eqn);
         }
         if (eqn == null) {
             return result;
         }
         /* First separate into multiple lines */
         String[] eqns = eqn.split("\n|\r");
         for (int x = 0; x < eqns.length; ++x) {
             String line = eqns[x];
             if (line.matches("^\\s*$")) {
                 continue;  // don't process equations missing any contents
 
             }
             if (line.matches("^#")) {
                 continue;	// don't parse lines starting with a comment
 
             }
             /* Next parse on tabs.  First column is equation, 2nd is correct answer (if present) */
             String[] cols = line.split("\t");
             String testEquation = cols[0];
             String expectedAnswer = null;
             if (cols.length > 1) {
                 expectedAnswer = cols[1];
             }
             try {
//                Triceps triceps = new Triceps();    
                
                 Datum datum;
                 if (logger.isLoggable(Level.FINER)) {
                     logger.log(Level.FINER, "Parsing: " + testEquation);
                 }
                 parser.ReInit(new StringReader(testEquation));
                datum = parser.parse(null); // FIXME does tihs really need the triceps object? triceps);
                 result = datum.stringVal();
                 ++numQueries;
                 logQueries(testEquation, result, expectedAnswer);
 
                 StringBuffer sb = new StringBuffer("INSERT INTO parser_test (equation, answer, expected, correct) values (");
                 sb.append("'").append(quoteSQL(testEquation)).append("',");
                 sb.append("'").append(quoteSQL(result)).append("',");
                 sb.append("'").append(quoteSQL(expectedAnswer)).append("',");
                 sb.append(result.equals(expectedAnswer) ? 1 : 0).append(");");
                 output = sb.toString();  
                 stmt = con.createStatement();
                 stmt.execute(sb.toString());
                 
 
             } catch (Throwable e) {
                 // FIXME:  Is it risky to catch an arbitrary Exception here?
                 logger.log(Level.WARNING, "Error: "+e+"Query "+output);
                 result = "*INVALID*";
                 logQueries(testEquation, result, expectedAnswer);
             }
         }
         return result;
     }
 
     /**
     Creates an 5 columh HTML table of Equation, Results, Expected, Errors, and Dependencies.
     @param eqn  The equation which was parsed
     @param result The result of parsing that equation
      */
     private void logQueries(String eqn,
             String result,
             String expectedAnswer) {
         StringBuffer sb = new StringBuffer();
         if (logger.isLoggable(Level.FINER)) {
             logger.log(Level.FINER, "Result of <<" + eqn + ">> is <<" + result + ">>");
         }
         sb.append("<TR><TD>");
         sb.append((new XMLAttrEncoder()).encode(eqn));
         sb.append("&nbsp;</TD><TD>");
         sb.append(result);
         sb.append("&nbsp;</TD>");
         if (expectedAnswer != null) {
             if (expectedAnswer.equals(result)) {
                 sb.append("<TD BGCOLOR='green'>PASS");
             } else {
                 sb.append("<TD BGCOLOR='red'>");
                 sb.append(expectedAnswer);
             }
         } else {
             sb.append("<TD>");
         }
         sb.append("&nbsp;</TD><TD>");
         if (parser.numErrors() > 0) {
             Iterator it = parser.getErrors().iterator();
             while (it.hasNext()) {
                 sb.append((new XMLAttrEncoder()).encode(it.next().toString()));
                 sb.append("<BR/>");
             }
         }
         sb.append("&nbsp;</TD><TD>");
         if (parser.numDependencies() > 0) {
             Iterator it = parser.getDependencies().iterator();
             while (it.hasNext()) {
                 sb.append(it.next());
                 sb.append("<BR/>");
             }
         }
         sb.append("&nbsp;</TD><TR>");
 
         queryHistory = sb.append(queryHistory);
     }
 
     /**
     Shows the number of non-null equations that have been processed.
     @return The number of non-null equations.
      */
     public int getNumQueries() {
         return numQueries;
     }
 
     /**
     Gets this history of queries as 4 column HTML
     @return A String of the queries in 4 column HTML format
     @see #logQueries
      */
     public String getQueryHistory() {
         return queryHistory.toString();
     }
 
     String quoteSQL(String src) {
         if (src == null) {
             return "";
         }
         return src.replace("'", "\\'").replace("\"", "\\\"");
     }
 
     public void setFilesToLoad(String filenameList) {
         this.filenameList = filenameList;
     }
 
     public String getLoadResults() {
         StringBuffer sb = new StringBuffer();
 
         if (filenameList == null || "".equals(filenameList.trim())) {
             return "No Filename Specified";
         }
 
         /* First separate into multiple lines */
         String[] filenames = filenameList.split("\n|\r");
         for (int x = 0; x < filenames.length; ++x) {
             String filename = filenames[x].trim();
             if (filename.equals("")) {
                 continue;
             }
             InstrumentExcelLoader instrumentExcelLoader = new InstrumentExcelLoader();
 
             sb.append("Loading Excel file " + filename + " ...<br>");
 
             instrumentExcelLoader.loadInstrument(filename);
             if (instrumentExcelLoader.getDatabaseStatus() == true) {
                 sb.append("... Successfully created databases<br>");
             } else {
                 sb.append("... Error creating databases<br>");
             }
 
             if (instrumentExcelLoader.hasInstrumentLoadErrors()) {
                 sb.append(instrumentExcelLoader.getLoadErrorsAsHtmlTable());
             }
 
             if (instrumentExcelLoader.getVersionFileStatus() == true) {
                 sb.append("... Successfully loaded instrument to target directory.  Launch it <a href='" + instrumentExcelLoader.getLaunchCommand() + "'>here</a>");
             } else {
                 sb.append("... Unable to load it to the target directory");
             }
             sb.append("<br>");
         }
         logger.log(Level.FINE, sb.toString());
         return sb.toString();
     }
     Runtime rt = Runtime.getRuntime();
 
     /**
     @return the bytes of free memory
      */
     public long getFreeMemory() {
         return rt.freeMemory();
     }
 
     /**
     @return the bytes of Maximum available memory requestable
      */
     public long getMaxMemory() {
         return rt.maxMemory();
     }
 
     /**
     @return the total currently available memory
      */
     public long getTotalMemory() {
         return rt.totalMemory();
     }
 
     /**
     Manually run the garbage collection
      **/
     public void garbageCollect() {
         rt.gc();
     }
 
     /**
     @return memory used, in megabytes
      */
     public String getMemoryUsed() {
         long used = (getTotalMemory() - getFreeMemory());
         double kb = Math.floor(used / 1000);
         double mb = kb / 1000;
         return (Double.toString(mb) + "MB");
     }
 }
