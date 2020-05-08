 package no.ebakke.studycaster.applications;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.logging.Level;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 import no.ebakke.studycaster.api.StudyCaster;
 import no.ebakke.studycaster.api.StudyCasterException;
 import no.ebakke.studycaster.ui.StudyCasterUI;
 import no.ebakke.studycaster.ui.StudyCasterUI.UIAction;
 import no.ebakke.studycaster.util.Util;
 import no.ebakke.studycaster.screencasting.ScreenCensor;
 import no.ebakke.studycaster.util.MyFileNameExtensionFilter;
 
 public class StudyLauncher {
   private static enum StudyType {RECRUITING, MAIN_EXCEL, MAIN_RS};
   private static StudyType studyType;
 
   private static final String instructions =
     "<html><b>Instructions:</b><ol>" +
     "<li>Edit the spreadsheet that opened in another window<br>" +
     "(according to the instructions in the HIT)." +
     "<li>Save and close the spreadsheet.<br>" +
     "<li>Click the button below." +
     "<li>Paste the confirmation code into the HIT." +
     "</ol><p align=\"right\">Thanks!<br><a href=\"mailto:ebakke@mit.edu\">ebakke@mit.edu</a></p></html>";
 
   public static void main(String args[]) {
     StudyCaster.log.info("Entering initial log message to promote fail-fast behavior of potential ConsoleTee bugs.");
 
     if (args.length != 1) {
       System.err.println("Usage: StudyLauncher <experiment-code>");
       StudyCaster.log.severe("No experiment code provided");
       System.exit(-1);
     }
     if        (args[0].equals("5782")) {
       studyType = StudyType.RECRUITING;
     } else if (args[0].equals("6728")) {
       studyType = StudyType.MAIN_EXCEL;
     } else if (args[0].equals("1093")) {
       studyType = StudyType.MAIN_RS;
     } else {
       StudyCaster.log.severe("Invalid experiment code provided");
       System.exit(-1);
     }
 
     StudyCasterUI scui;
 
     try {
       FileFilter filter;
       if (studyType == StudyType.MAIN_RS) {
         filter = new MyFileNameExtensionFilter(Arrays.asList(new String[] {"rzw"}), "Spreadsheet Study Application files");
       } else {
         filter = new MyFileNameExtensionFilter(Arrays.asList(new String[] {"xls", "xml", "xlsx", "xlsm", "xlsb"}), "Microsoft Excel Workbook");
       }
       scui = new StudyCasterUI(instructions, filter);
     } catch (StudyCasterException e) {
       StudyCaster.log.log(Level.SEVERE, "Unexpected exception during UI initialization", e);
       System.exit(0);
       return;
     }
 
     if (scui.wasClosed()) {
       try {
         StudyCaster sc = new StudyCaster();
         sc.enterRemoteLogRecord("User declined at consent dialog");
         sc.concludeStudy();
       } catch (StudyCasterException e) {
         StudyCaster.log.warning("Failed to send consent-declined log message");
       }
       System.exit(0);
     }
     StudyCaster sc = null;
     long lastModified1, lastModified2;
     boolean download = true;
     File openedFile;
     try {
       scui.getProgressBarUI().setProgress(25);
       scui.getProgressBarUI().setTaskAppearance("Initializing user study app...", false);
       sc = new StudyCaster();
       sc.startRecording(new ScreenCensor(
           Arrays.asList(new String[] {"User Study Console", "Excel", "Calc", "Numbers", "Gnumeric", "KSpread", "Quattro", "Mesa", "Spreadsheet Study Application"}),
           Arrays.asList(new String[] {"Firefox", "Internet Explorer", "Outlook", "Chrome", "Safari", "Upload and Retrieve Confirmation Code",
           "Open Sample Document"}),
           true));
       scui.getProgressBarUI().setProgress(50);
       scui.getProgressBarUI().setTaskAppearance("Downloading sample document...", false);
       String studyFileNameLocal, studyFileNameRemote;
       switch (studyType) {
         case        RECRUITING:
           studyFileNameLocal  = "userstudyHIT_currencies.xls";
           studyFileNameRemote = "currencies.xls";
         break; case MAIN_EXCEL:
           studyFileNameLocal  = "userstudyHIT_coursecatalog.xls";
           studyFileNameRemote = "exflat.xls";
         break; case MAIN_RS:
           studyFileNameLocal  = "userstudyHIT_coursecatalog.rzw";
           studyFileNameRemote = "rsnest.rzw";
         break; default:
           throw new AssertionError("Unexpected study type");
       }
 
       openedFile = new File(new File(System.getProperty("java.io.tmpdir")), studyFileNameLocal);
       if (openedFile.exists()) {
         StudyCaster.log.info("File to be downloaded already exists in temp directory");
 
         while (true) {
           download = scui.showDownloadOrExistingDialog(Util.getPathString(openedFile));
           StudyCaster.log.info("User chose to " + (download ? "download new file" : "keep existing file"));
           // TODO: Move this mess out of here.
           if (download) {
             String path = openedFile.getPath();
             int dot = path.lastIndexOf(".");
             String basename = (dot < 0) ? path : path.substring(0, dot);
             String extension = (dot < 0) ? "" : path.substring(dot);
             File newName;
             int index = 1;
             do {
               newName = new File(basename + " (" + index + ")" + extension);
               index++;
             } while (newName.exists());
             if (!openedFile.renameTo(newName)) {
               scui.showMessageDialog("Open Sample Document",
                   "<html>Failed to rename<br>" + Util.getPathString(openedFile) + "<br><br>" +
                   "If the file is still open, please close it and try again.</html>",
                   JOptionPane.WARNING_MESSAGE);
             } else {
               scui.showMessageDialog("Open Sample Document",
                 "<html>The old file<br>" + Util.getPathString(openedFile) + "<br>was renamed to<br>" +
                 Util.getPathString(newName) + "</html>",
                 JOptionPane.INFORMATION_MESSAGE);
               break;
             }
           } else {
             break;
           }
         }
       }
       if (download)
         sc.downloadFile(studyFileNameRemote, openedFile);
       scui.setDefaultFile(openedFile);
       lastModified1 = openedFile.lastModified();
       scui.getProgressBarUI().setProgress(75);
       scui.getProgressBarUI().setTaskAppearance("Opening sample document...", false);
       if (Util.fileAvailableExclusive(openedFile)) {
         if (studyType == StudyType.MAIN_RS) {
           try {
             StudyCaster.log.info("Now starting Relational Spreadsheet");
             no.ebakke.hier.view.Main.main(new String[] {Util.getPathString(openedFile)});
             StudyCaster.log.info("Relational Spreadsheet started");
           } catch (Exception e) {
             StudyCaster.log.log(Level.SEVERE, "Caught exception from Relational Spreadsheet main method", e);
           }
         } else {
           Util.desktopOpenFile(openedFile, "Excel or a compatible spreadsheet application");
         }
       }
       lastModified2 = openedFile.lastModified();
       scui.getProgressBarUI().setProgress(100);
       scui.setUploadEnabled(true);
       scui.getProgressBarUI().setTaskAppearance("", false);
       scui.getProgressBarUI().setProgress(0);
     } catch (StudyCasterException e) {
       StudyCaster.log.log(Level.SEVERE, "Can't Load User Study", e);
       if (sc != null)
         sc.concludeStudy();
       sc.enterRemoteLogRecord("Failed to load user study");
       scui.showMessageDialog("Can't Load User Study", e.getLocalizedMessage(), JOptionPane.WARNING_MESSAGE);
       scui.disposeUI();
       return;
     }
     sc.enterRemoteLogRecord("User study loaded OK; experiment " + studyType);
 
     UIAction action;
    boolean warnedAboutUnchanged = false;
     do {
       //scui.setMonitorStreamProgress(sc.getRecordingStream(), true);
       action = scui.waitForUserAction();
       //scui.setMonitorStreamProgress(sc.getRecordingStream(), false);
       if (action == UIAction.UPLOAD) {
         File selectedFile = scui.getSelectedFile();
         long nowLastModified = selectedFile.lastModified();
         boolean isSameFile = false;
         try {
           isSameFile = selectedFile.getCanonicalPath().equals(openedFile.getCanonicalPath());
         } catch (IOException e) {
           StudyCaster.log.log(Level.WARNING, "Failed to check for filename equality", e);
         }
        if (download && isSameFile && (nowLastModified == lastModified1 || nowLastModified == lastModified2) && !warnedAboutUnchanged) {
          warnedAboutUnchanged = true;
           StudyCaster.log.info("Got upload on unchanged document; exclusive=" + Util.fileAvailableExclusive(selectedFile));
 
           scui.showMessageDialog("Upload",
                   "<html>Please edit, save, and close the spreadsheet document, then try again.<br><br>" +
                   "(The document should have opened in a new window, possibly in the background.)<br>" +
                   "</html>"
                   , JOptionPane.WARNING_MESSAGE);
         } else if (!Util.fileAvailableExclusive(selectedFile)) {
           StudyCaster.log.info("Got upload on changed but still open document");
           scui.showMessageDialog("Upload",
                 "<html>Please close the spreadsheet document, then try again.</html>"
                 , JOptionPane.WARNING_MESSAGE);
         } else {
           sc.enterRemoteLogRecord("Now starting upload");
           StudyCaster.log.info("Now starting upload");
           try {
             scui.getProgressBarUI().setTaskAppearance("Uploading document...", true);
             StudyCaster.log.info("Uploading file " + selectedFile.getName());
             sc.uploadFile(selectedFile, "uploaded_" + Util.sanitizeFileNameComponent(selectedFile.getName()));
             scui.getProgressBarUI().setTaskAppearance("Finishing screencast upload...", true);
             sc.stopRecording();
             scui.setMonitorStreamProgress(sc.getRecordingStream(), true);
             sc.waitForScreenCastUpload();
             scui.setMonitorStreamProgress(sc.getRecordingStream(), false);
             scui.getProgressBarUI().setTaskAppearance("Uploading log...", true);
             sc.enterRemoteLogRecord("Concluding after successful upload");
             sc.concludeStudy();
             scui.getProgressBarUI().setTaskAppearance("", false);
             scui.showConfirmationCodeDialog(sc.getServerContext().getTicketCC().toString(), true);
             scui.disposeUI();
           } catch (StudyCasterException e) {
             StudyCaster.log.log(Level.SEVERE, "Failed to upload", e);
             scui.getProgressBarUI().setTaskAppearance("", false);
             scui.getProgressBarUI().setProgress(0);
             scui.showMessageDialog("Failed to Upload File", e.getLocalizedMessage(), JOptionPane.WARNING_MESSAGE);
           }
         }
       } else if (action == UIAction.CLOSE) {
         sc.enterRemoteLogRecord("Got a close action");
       }
       scui.setUploadEnabled(true);
     } while (action != UIAction.CLOSE);
     sc.concludeStudy();
     // Shut down the force-shutdown thread in StudyCasterUI.
     System.exit(0);
   }
 }
