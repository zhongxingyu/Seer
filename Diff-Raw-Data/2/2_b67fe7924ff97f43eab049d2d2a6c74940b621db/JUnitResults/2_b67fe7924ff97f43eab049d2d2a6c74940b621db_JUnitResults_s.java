 package com.yahoo.dtf.results;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.URI;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import com.yahoo.dtf.DTFProperties;
 import com.yahoo.dtf.actions.Action;
 import com.yahoo.dtf.exception.ResultsException;
 import com.yahoo.dtf.exception.StorageException;
 import com.yahoo.dtf.util.TimeUtil;
 
 public class JUnitResults extends ResultsBase {
 
     private URI _uri = null;
     
     private PrintStream _xml = null;
     
     public JUnitResults(URI uri, boolean savelogs) {
         super(uri, true);
         _uri = uri;
     }
     
     public URI getURI() { return _uri; }
 
     public void start() throws ResultsException {
         try {
             OutputStream os = Action.getStorageFactory().getOutputStream(_uri);
             _xml = new PrintStream(os);
         } catch (StorageException e) {
             throw new ResultsException("Unable to open output file.",e);
         }
     }
     
     public void stop() throws ResultsException {
         _xml.close();
         
         /*
          * now lets beautify the resulting XML. 
          */
     }
   
     private void startTestSuite(Result result, PrintStream ps) throws ResultsException { 
         ps.print("<testsuite");
        ps.print(" name=\"" + result.getName() + "\"");
         ps.print(" classname=\"dtf." + result.getName() + "\"");
         ps.print(" tests=\"" + result.getTotalTests() + "\"");
         
         try { 
             ps.print(" start=\"" + TimeUtil.dateStampToDateStamp(result.getStart()) + "\"");
             ps.print(" stop=\"" + TimeUtil.dateStampToDateStamp(result.getStop()) + "\"");
         } catch (ParseException e) { 
             throw new ResultsException("Error handling date.",e);
         }
          
         ps.print(" time=\"" + result.getDurationInSeconds() + "\"");
         ps.print(" timestamp=\"" + new Date(result.getStart()) + "\"");
         ps.print(" passed=\"" + result.getNumPassed() + "\"");
         ps.print(" failures=\"" + result.getNumFailed() + "\"");
         ps.print(" errors=\"" + result.getNumFailed() + "\"");
         ps.println(" disabled=\"" + result.getNumSkipped() + "\">");
     }
     
     private void printProperties(Result result, PrintStream ps) { 
         Set<Entry<Object,Object>> entries = result.getProperties().entrySet();
       
         for (Entry<Object,Object> entry : entries) { 
             ps.println("<property name=\"" + entry.getKey() + 
                      "\" value=\"" + entry.getValue() + "\" />");
         }
     }
    
     private boolean hasTestSuite = false;
     public void recordResult(Result result) throws ResultsException {
         if ( result.isTestSuite() ) { 
             hasTestSuite = true;
             startTestSuite(result, _xml); 
             printProperties(result, _xml);
             printResultNode(result,_xml);
             
             Iterator results = result.getResults().iterator();
             while (results.hasNext()) { 
                 recordResult((Result)results.next());
             }
             
             String testlogfile = (String)
                      result.getProperties().get(DTFProperties.DTF_TESTCASE_LOG);
             
             File log = null;
             if (testlogfile != null && (log = new File(testlogfile)).exists()) {
                 _xml.println("<system-out><![CDATA[");
                 try {
                     FileInputStream fis = new FileInputStream(log);
                     InputStreamReader isr = new InputStreamReader(fis);
                     BufferedReader br = new BufferedReader(isr);
                     try { 
                         String line = null;
                         while ( (line = br.readLine()) != null ) 
                             _xml.println(line);
                     } finally { 
                         br.close();
                     }
                 } catch (FileNotFoundException e) {
                     throw new ResultsException("Unable to read log file.",e);
                 } catch (IOException e) {
                     throw new ResultsException("Error reading log file.",e);
                 }
                 _xml.println("]]></system-out>");
             } else { 
                 _xml.println("<system-out/>");
             }
             _xml.println("<system-err/>");
             _xml.println("</testsuite>");
         } else if (result.isTestCase()) { 
             if ( !hasTestSuite ) { 
                 hasTestSuite = true;
                 startTestSuite(result, _xml); 
                 _xml.println("</testsuite>");
             } else {
                 /*
                  * Test case output should look like this:
                  * 
                  * <testcase name="testAdd" 
                  *           time="0.018"/>
                  */
                 _xml.print("<testcase name=\"" + result.getName() + "\"");
                 
                 try { 
                     _xml.print(" start=\"" + 
                                TimeUtil.dateStampToDateStamp(result.getStart())
                                + "\"");
                     _xml.print(" stop=\"" + 
                                TimeUtil.dateStampToDateStamp(result.getStop())
                                + "\"");
                 } catch (ParseException e) { 
                     throw new ResultsException("Error handling date.",e);
                 }
                 _xml.println(" time=\"" + result.getDurationInSeconds() + "\">");
 
                 printProperties(result,_xml);
                 printResultNode(result,_xml);
                 
                 _xml.println("</testcase>");
             }
         }
     }
 
     private void printResultNode(Result result, PrintStream ps) {
         if (result.isFailResult()) {
             ps.print("<failure>");
             if (result.getOutput() != null)
                 ps.print(result.getOutput());
             ps.println("</failure>");
         }
     }
 }
