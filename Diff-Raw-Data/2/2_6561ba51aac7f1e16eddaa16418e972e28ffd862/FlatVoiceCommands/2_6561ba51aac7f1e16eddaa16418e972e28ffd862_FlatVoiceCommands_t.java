 /*
  * Copyright 1999-2004 Carnegie Mellon University.
  * Portions Copyright 2004 Sun Microsystems, Inc.
  * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
  * All Rights Reserved.  Use is subject to license terms.
  *
  * See the file "license.terms" for information on usage and
  * redistribution of this file, and for a DISCLAIMER OF ALL
  * WARRANTIES.
  *
  */
 
package flat10c.flat_voice_commands;
 
 import edu.cmu.sphinx.frontend.util.Microphone;
 import edu.cmu.sphinx.recognizer.Recognizer;
 import edu.cmu.sphinx.result.*;
 import edu.cmu.sphinx.decoder.search.Token;
 import edu.cmu.sphinx.util.props.ConfigurationManager;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.text.DecimalFormat;
 
 /* XML Parsing */
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 import javax.xml.parsers.*;
 import javax.xml.xpath.*;
 
 /**
  * A simple speech application built using Sphinx-4. This application uses the Sphinx-4
  * endpointer, which automatically segments incoming audio into utterances and silences.
  * It recognizes configured voice commands and triggers associated actions.
  */
 public class FlatVoiceCommands {
 
     private static DecimalFormat format = new DecimalFormat("#.#####");
     private static Document config_xml;
     private static HashMap server_auth = new HashMap();
 
     public static void main(String[] args) {
         System.out.println("Loading config ...");
 
         /* Load Sphinx Configuration */
         ConfigurationManager cm = new ConfigurationManager(FlatVoiceCommands.class.getResource("sphinx.config.xml"));
 
         /* Load Server Configuration */
         try {
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             dbFactory.setNamespaceAware(true);
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             config_xml = dBuilder.parse(FlatVoiceCommands.class.getResourceAsStream("auth.config.xml"));
         } catch (IOException ex) {                  ex.printStackTrace();
         } catch (SAXException ex) {                 ex.printStackTrace();
         } catch (ParserConfigurationException ex) { ex.printStackTrace(); }
         XPathFactory factory = XPathFactory.newInstance();
         XPath xpath = factory.newXPath();
         try {
             server_auth.put("xbmc_username", (String)xpath.evaluate("//server_auth/xbmc/username", config_xml, XPathConstants.STRING));
             server_auth.put("xbmc_password", (String)xpath.evaluate("//server_auth/xbmc/password", config_xml, XPathConstants.STRING));
             server_auth.put("lock_username", (String)xpath.evaluate("//server_auth/lock/username", config_xml, XPathConstants.STRING));
             server_auth.put("lock_password", (String)xpath.evaluate("//server_auth/lock/password", config_xml, XPathConstants.STRING));
         } catch (XPathExpressionException ex) {     ex.printStackTrace();  }
 
         // Create lock commands hash.
         HashMap lock_commands = new HashMap() {{
             put("unlock door", (String)"Unlock Door");
             put("open door",   (String)"Unlock Door");
 
             put("turn on light", (String)"Turn Hall Light [ON]");
             put("turn light on", (String)"Turn Hall Light [ON]");
             put("turn off light", (String)"Turn Hall Light [OFF]");
             put("turn light off", (String)"Turn Hall Light [OFF]");
 
             put("turn on fan", (String)"Turn Fan [ON]");
             put("turn fan on", (String)"Turn Fan [ON]");
             put("turn off fan", (String)"Turn Fan [OFF]");
             put("turn fan off", (String)"Turn Fan [OFF]");
         }};
 
 
         System.out.println("Initializing Sphinx ...");
 
         Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
         recognizer.allocate();
 
         System.out.println("Starting Microphone ...");
 
         // start the microphone or exit if this is not possible
         Microphone microphone = (Microphone) cm.lookup("microphone");
         if (!microphone.startRecording()) {
             System.out.println("Cannot start microphone.");
             recognizer.deallocate();
             System.exit(1);
         }
 
         System.out.println("Say a command to the flat.");
         System.out.println("Start speaking. Press Ctrl-C to quit.\n");
 
         // Loop the recognition until the program exits.
         while (true) {
 
             Result result = recognizer.recognize();
 
             if (result != null) {
 
                 ConfidenceScorer cs = (ConfidenceScorer) cm.lookup("confidenceScorer");
                 ConfidenceResult cr = cs.score(result);
                 Path best = cr.getBestHypothesis();
 
                 // Print linear confidence of the best path
                 System.out.println(best.getTranscription());
                 System.out.println
                         ("     (confidence: " +
                                 format.format(best.getLogMath().logToLinear
                                         ((float) best.getConfidence()))
                                 + ')');
                 System.out.println();
 
                 String resultText = result.getBestFinalResultNoFiller().trim();
 
                 // Look up resultText in lock_commands hash
                 String cmd = (String)lock_commands.get(resultText);
 
                 System.out.println("[" + resultText + "]");
 
                 if (cmd != "") {
                     System.out.println("Sending Command: " + cmd + '\n');
                     sendEvoCommand(cmd);
                 } else {
                     System.out.println("I can't hear what you said.\n");
                 }
             }
         }
     }
 
     // Sends an HTTP POST request to lock thin-client
     private static void sendEvoCommand(String cmd) {
         try {
             // Construct data
             String data = URLEncoder.encode("user", "UTF-8") + "=" +
                                 URLEncoder.encode((String)server_auth.get("lock_username"), "UTF-8");
             data += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                                 URLEncoder.encode((String)server_auth.get("lock_password"), "UTF-8");
             data += "&" + URLEncoder.encode("action", "UTF-8") + "=" +
                                 URLEncoder.encode(cmd, "UTF-8");
 
             // Send data
             URL url = new URL("http://lock-10c:80/action");
             URLConnection conn = url.openConnection();
             conn.setDoOutput(true);
             OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(data);
             wr.flush();
 
             // Get the response
             BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             String line;
             while ((line = rd.readLine()) != null) {
                 // Process line...
             }
             wr.close();
             rd.close();
         } catch (Exception e) {
         }
     }
 }
 
