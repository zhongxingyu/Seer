 /**
  *
  * Copyright (c) 2013 Berner Fachhochschule, Switzerland. Bern University of
  * Applied Sciences, Engineering and Information Technology, Research Institute
  * for Security in the Information Society, E-Voting Group, Biel, Switzerland.
  *
  * Project independent VoteVerifier.
  *
  */
 package ch.bfh.univoteverifier.common;
 
 import ch.bfh.univote.common.Candidate;
 import ch.bfh.univote.common.Choice;
 import ch.bfh.univote.common.PoliticalList;
 import ch.bfh.univoteverifier.gui.ElectionReceipt;
 import ch.bfh.univoteverifier.gui.GUIconstants;
 import ch.bfh.univoteverifier.listener.VerificationEvent;
 import ch.bfh.univoteverifier.listener.VerificationListener;
 import ch.bfh.univoteverifier.listener.VerificationMessage;
 import ch.bfh.univoteverifier.table.ResultDescriber;
 import ch.bfh.univoteverifier.verification.VerificationResult;
 import ch.bfh.univoteverifier.verification.VerificationThread;
 import java.io.File;
 import java.util.Formatter;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Runs the program without in the terminal as the text only version in which
  * only one verification can run at a time.
  *
  * @author Justin Springer
  */
 public class ConsoleRunner {
 
     private static final Logger LOGGER = VrfLogger.getLoggerForClass(ConsoleRunner.class.getName());
     private final ResourceBundle rb;
     private Messenger msgr;
 
     /**
      * Create an instance of this class which checks the parameters passed and
      * starts the verification type the user entered.
      *
      * @param msgr Messenger to show messages in the console.
      * @param args the arguments to pass to the program.
      */
     public ConsoleRunner() {
         VerificationListener vl = new StatusUpdate();
         msgr = new Messenger("default");
         msgr.getStatusSubject().addListener(vl);
         rb = ResourceBundle.getBundle("error", GUIconstants.getLocale());
     }
 
     /**
      * Begin the program with the provided parameters.
      *
      * @param args The parameters passed to the program from the terminal.
      */
     public void begin(String[] args) {
         boolean hasUorI = args[0].equals("-u") || args[0].equals("-i");
         boolean has2args = args.length == 2;
         if (!hasUorI || !has2args) {
             LOGGER.log(Level.SEVERE, GUIconstants.USAGE);
             System.exit(0);
         } else if (args[0].equals("-u")) {
             runUniVrfConsole(msgr, args[1]);
         } else if (args[0].equals("-i")) {
             runIndVrfConsole(msgr, args[1]);
 
         }
     }
 
     /**
      * Show the verification information in the terminal. This method is called
      * if the program is being run from the terminal.
      *
      * @param ve VerificationResult helper class containing verification
      * information.
      */
     public void showResultInTerminal(VerificationEvent ve) {
 
         VerificationResult vr = ve.getVr();
         int code = vr.getVerificationType().getID();
         ResultDescriber rd = new ResultDescriber();
         String vrfType = rd.getTextFromVrfCode(code);
         formatText(vrfType, vr.getEntityName());
         String result = getTextResultName(vr);
         String outputText = vrfType + " ............. " + result;
         LOGGER.log(Level.INFO, outputText);
        Report report = ve.getVr().getReport();

        if (report != null) {
            String outputText2 = "";
            if (report.getAdditionalInformation() != null) {
                outputText2 += report.getAdditionalInformation();
            }
            if (report.getException() != null) {
                outputText2 += report.getException();
            }
            if (report.getFailureCode() != null) {
                outputText2 += report.getFailureCode();
            }

            LOGGER.log(Level.INFO, outputText2);

        }
     }
 
     /**
      * Show the verification information in the terminal. This method is called
      * if the program is being run from the terminal.
      *
      * @param ve VerificationResult helper class containing verification
      * information.
      */
     public void showElectionResultInTerminal(VerificationEvent ve) {
         Map<Choice, Integer> eResults = ve.getElectionResults();
 
         for (Entry<Choice, Integer> e : eResults.entrySet()) {
             Choice c = e.getKey();
             Integer count = e.getValue();
             String textOutput = "";
             if (c instanceof PoliticalList) {
                 PoliticalList pl = (PoliticalList) c;
                 String number = pl.getNumber();
                 String partyName = pl.getPartyName().get(0).getText();
                 textOutput = number + " " + "Political list: " + partyName;
             } else if (c instanceof Candidate) {
                 Candidate can = (Candidate) c;
                 String number = can.getNumber();
                 String firstName = can.getFirstName();
                 String lastName = can.getLastName();
                 textOutput = "\t" + number + " " + firstName + " " + lastName;
             }
             textOutput += "...................." + count;
             LOGGER.log(Level.INFO, textOutput);
         }
     }
 
     /**
      * Run a universal verification process in the console.
      *
      * @param msgr A messenger for this verification process.
      * @param eID An election ID to run a verification process for.
      */
     public void runUniVrfConsole(Messenger msgr, String eID) {
         String msg = rb.getString("beginningVrfFor") + " " + rb.getString("forElectionId") + " " + eID;
         msgr.sendSetupError(msg);
         VerificationThread vt = new VerificationThread(msgr, eID);
         vt.start();
     }
 
     /**
      * Run a individual verification process in the console.
      *
      * @param msgr A messenger for this verification process.
      * @param path A path to a file containing a QR Code of and election
      * receipt.
      */
     public void runIndVrfConsole(Messenger msgr, String path) {
         File file = new File(path);
         QRCode qr = new QRCode(msgr);
         ElectionReceipt er = qr.decodeReceipt(file);
         if (er == null) {
             String errorText = rb.getString("fileReadError");
             LOGGER.log(Level.INFO, errorText);
             System.exit(0);
         } else {
             String msg = rb.getString("beginningVrfFor") + " " + rb.getString("ballotProvided");
             msgr.sendSetupError(msg);
             VerificationThread vt = new VerificationThread(msgr, er);
             vt.setName(er.getElectionID());
             vt.start();
         }
     }
 
     /**
      * Get the appropriate text to be displayed for this result.
      *
      * @param vr The result set for which to find text form of the result.
      * @return String the result as text.
      */
     public String getTextResultName(VerificationResult vr) {
 
         String resultText = "";
 
         if (vr.getReport() != null && vr.getReport().getException() != null) {
             //If there is an exception
             resultText = "warning: ERROR";
         } else if (!vr.isImplemented() && vr.getReport() != null && vr.getReport().getFailureCode() != null) {
             //if not implemented and there is a failure code
             resultText = "not implemented";
         } else if (vr.getResult() && vr.isImplemented()) {
             //if result true and it is implemented
             resultText = "passed";
         } else if (!vr.getResult() && vr.isImplemented()) {
             //if result false and it is implemented
             resultText = "failed";
         }
         return resultText;
     }
 
     /**
      * Format the text with an appropriate amount of dots. I.e.
      * Text...............
      *
      * @return The formatted text.
      */
     public String formatText(String text, String entityName) {
         StringBuilder sb = new StringBuilder();
         Formatter formatter = new Formatter(sb, GUIconstants.getLocale());
         formatter.format(text, entityName);
         String outText = formatter.toString();
         return outText;


     }
 
     /**
      * This inner class represents the implementation of the observer pattern
      * for the status messages for the console and verification parts of the
      * GUI.
      *
      * @author Justin Springer
      */
     class StatusUpdate implements VerificationListener {
 
         @Override
         public void updateStatus(VerificationEvent ve) {
 
 
             if (ve.getVm() == VerificationMessage.SETUP_ERROR) {
                 String text = "\n" + ve.getMsg();
                 LOGGER.log(Level.INFO, text);
             } else if (ve.getVm() == VerificationMessage.ELECTION_SPECIFIC_ERROR) {
             } else if (ve.getVm() == VerificationMessage.SETUP_ERROR) {
                 String text = "\n" + ve.getMsg();
                 LOGGER.log(Level.INFO, text);
             } else if (ve.getVm() == VerificationMessage.VRF_FINISHED) {
                 System.exit(0);
             } else if (ve.getVm() == VerificationMessage.ELECTION_RESULTS) {
                 showElectionResultInTerminal(ve);
             } else if (ve.getVm() == VerificationMessage.RESULT) {
                 showResultInTerminal(ve);
             }
 
         }
     }
 }
