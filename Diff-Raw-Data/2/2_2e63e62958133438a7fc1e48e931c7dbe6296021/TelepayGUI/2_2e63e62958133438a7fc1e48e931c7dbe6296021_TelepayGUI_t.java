 package no.hild1.bank;
 
import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import javax.swing.*;
 
 import no.hild1.bank.telepay.*;
 import no.hild1.bank.utils.FileDrop;
 import no.hild1.bank.utils.MessageConsole;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class TelepayGUI extends JFrame {
     private static Log log = LogFactory.getLog(TelepayGUI.class);
     private JEditorPane logPane;
     JButton selectFile, closeButton, viewAllRecordsButton, copyLog, fakeFile;
     JButton doubleCheck;
     final JFileChooser fc = new JFileChooser();
     LocalActionListener localActionListener = new LocalActionListener();
     JFrame application;
     TelepayParser telepayParser;
     JTabbedPane tpane;
     JPanel recordsView;
 	public TelepayGUI() {
         super("Telepay 2v1 Viewer");
         application = this;
 
         logPane = new JEditorPane();
         fc.setCurrentDirectory(new File(System.getProperty("user.home")));
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         JScrollPane scrollPane = new JScrollPane(logPane);
         MessageConsole mc = new MessageConsole(logPane);
         mc.redirectOut();
         mc.redirectErr(Color.RED, null);
         //mc.setMessageLines(100);
         tpane = new JTabbedPane();
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
 
         tpane.add("Logg", scrollPane);
 
         getContentPane().add(tpane, BorderLayout.CENTER);
 
         getContentPane().add(makeButtonPanel(), BorderLayout.SOUTH);
 
         javax.swing.border.TitledBorder dragBorder = new javax.swing.border.TitledBorder( "Slipp fil for å sette den som aktiv fil" );
 
         new FileDrop( tpane, dragBorder, true, new LocalFileDropListener());
 
         setSize();
 	    setVisible(true);
 	}
 
     public JPanel makeButtonPanel() {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
 
         selectFile = new JButton("Åpne fil");
         selectFile.addActionListener(localActionListener);
         panel.add(selectFile);
 
         panel.add(Box.createHorizontalGlue());
         viewAllRecordsButton = new JButton("Vis alle records");
         viewAllRecordsButton.setEnabled(false);
         viewAllRecordsButton.addActionListener(localActionListener);
         panel.add(viewAllRecordsButton);
 
         panel.add(Box.createHorizontalGlue());
         doubleCheck = new JButton("Sjekk for doble betalinger");
         doubleCheck.setEnabled(false);
         doubleCheck.addActionListener(localActionListener);
         panel.add(doubleCheck);
 
         panel.add(Box.createHorizontalGlue());
         fakeFile = new JButton("Sjekk for doble betalinger");
         fakeFile.setEnabled(false);
         fakeFile.addActionListener(localActionListener);
         panel.add(fakeFile);
 
         panel.add(Box.createHorizontalGlue());
         copyLog = new JButton("Kopier logg");
         copyLog.addActionListener(localActionListener);
         panel.add(copyLog);
 
         panel.add(Box.createHorizontalGlue());
         closeButton = new JButton("Lukk");
         closeButton.addActionListener(localActionListener);
         panel.add(closeButton);
 
         return panel;
     }
     public void setSize() {
         Dimension screenSize = getToolkit().getScreenSize();
         //int width = screenSize.width * 4 / 10;
         int width = 850;
         int height = 500;
         logPane.setPreferredSize(new Dimension(width, height));
         pack();
     }
     public void displayError(String msg) { showMessageDialog(msg, JOptionPane.ERROR_MESSAGE);}
     public void displayMessage(String msg) { showMessageDialog(msg, JOptionPane.INFORMATION_MESSAGE); }
     public void showMessageDialog(String msg, int messagetype) {
         log.error(msg);
         JTextArea textArea = new JTextArea(msg);
         textArea.setColumns(50);
         textArea.setLineWrap( true );
         textArea.setWrapStyleWord( true );
         textArea.setSize(textArea.getPreferredSize().width, 1);
         JOptionPane.showMessageDialog(
                 application, textArea, "Melding", messagetype);
     }
 
     private void resetApp() {
         telepayParser = null;
         viewAllRecordsButton.setEnabled(false);
         fakeFile.setEnabled(false);
     }
 
     class LocalFileDropListener implements no.hild1.bank.utils.FileDrop.Listener {
         public void filesDropped(File[] files) {
             {
                     log.info("Parsing dropped file: " + files[0].getName());
                     resetApp();
                     parseFile(files[0]);
             }
         }
     }
 
     private void parseFile(File file) {
         resetApp();
         telepayParser = new TelepayParser(file);
             try {
                 telepayParser.basicCheck();
                 telepayParser.parseAllRecords();
              /*   for (Betfor b : telepayParser.records) {
                     log.info("\n" + b.getRecord(false));
                 }
                */
                 showRecords();
                 fakeFile.setEnabled(true);
                 viewAllRecordsButton.setEnabled(true);
                 doubleCheck.setEnabled(true);
             } catch (TelepayParserException e1) {
                 resetApp();
                 displayError(e1.toString());
             }
     }
     private void showRecords() {
         if (recordsView != null) {
             tpane.remove(recordsView);
         }
         recordsView = new DisplayRecords(telepayParser.records);
         //JScrollPane scrollPanel = new JScrollPane();
 
         tpane.add(recordsView, BorderLayout.CENTER, 0);
 
         //scrollPanel.setViewportView(recordsView);
 
         tpane.setTitleAt(0, "Records");
         tpane.setSelectedComponent(recordsView);
     }
 
     class LocalActionListener implements java.awt.event.ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             if (e.getSource() == selectFile) {
                 int returnVal = fc.showOpenDialog(TelepayGUI.this);
 
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File file = fc.getSelectedFile();
                     log.debug("Åpner : " + file.getName() + ".");
                     parseFile(file);
                 } else {
                     resetApp();
                     log.debug("Åpne-kommand avbrutt av bruker.");
                 }
             } else if (e.getSource() == closeButton) {
                     application.dispose();
             } else if (e.getSource() == viewAllRecordsButton)  {
                 showRecords();
             } else if (e.getSource() == copyLog) {
                 logPane.selectAll();
                 logPane.copy();
             } else if (e.getSource() == fakeFile) {
 
                 DateFormat df = new SimpleDateFormat("yyyy-mm-dd-HH-mm");
                 Date today = Calendar.getInstance().getTime();
                 String reportDate = df.format(today);
                 String filename = telepayParser.sourceFile.getAbsolutePath() + "."+reportDate+".telepay";
                 if (JOptionPane.showConfirmDialog(
                         application, "Den nye filen vil bli lagret som \n" + filename,
                         "Filnavn", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {return; }
 
                 String orgNR = "";
                 boolean first = true;
                 do {
                     orgNR = (String)JOptionPane.showInputDialog(
                             application,
                             "Tast inn nytt Org.Nr" + ((first) ? "" : "\nOrg.Nr må være 9 siffer"),
                             "Tast inn nytt Org.Nr",
                             JOptionPane.PLAIN_MESSAGE,
                             null,
                             null,
                             "");
                     if (orgNR == null) { return; }
                     first = false;
                 } while (!orgNR.matches("[0-9]{9}"));
 
                 String konto = "";
                 first = true;
                 do {
                     konto = (String)JOptionPane.showInputDialog(
                             application,
                             "Tast inn nytt kontonummer" + ((first) ? "" : "\nKontonummer må være 11 siffer"),
                             "Tast inn nytt kontonummer",
                             JOptionPane.PLAIN_MESSAGE,
                             null,
                             null,
                             "");
                     if (konto == null) { return; }
                     first = false;
                 } while (!konto.matches("[0-9]{11}"));
 
                 if (JOptionPane.showConfirmDialog(
                         application, "Nytt Org.Nr: " + orgNR + "\nNytt kontonummer: " + konto+"\n\nEr dette riktig?",
                         "Bekreft nye data", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {return; }
 
                 String newRecords = "";
                 for (Betfor record : telepayParser.records) {
                     newRecords += record.getRecord(false, orgNR, konto);// + "\n";
                 }
                 log.info("\n" + newRecords);
                 File tmpFile = new File(filename);
 
                 try {
                     FileUtils.writeStringToFile(tmpFile, newRecords, "ISO-8859-1");
                 } catch (Exception writeException) {
                     displayError(writeException.toString());
                 }
                /* java.util.List<String> lines = Arrays.asList(newRecords.split("\n"));
                 try {
                     FileUtils.writeLines(tmpFile, lines);
                 } catch (Exception writeException) {
                     displayError(writeException.toString());
                 }*/
 
 
             } else if (e.getSource() == doubleCheck) {
                 Set<String> acckid = new HashSet<String>();
                 boolean foundDouble = false;
                 for (Betfor record: telepayParser.records) {
 
                     if (record instanceof Betfor23){
                         String KID = ((Betfor23)record).get(Betfor23.Element.KID);
                         String ACCOUNTNUMBER = ((Betfor23)record).get(Betfor23.Element.ACCOUNTNUMBER);
                         String INVOICEAMOUNT = ((Betfor23)record).get(Betfor23.Element.INVOICEAMOUNT);
                         String pattern = "^[ ]{27}$";
                         String key;
                         if (KID.matches(pattern)) {
                             log.info("Ikke KID: Record #" + record.header.getRecordNum());
                             key =  "ACC+AMOUNT:" + ACCOUNTNUMBER + ":"+ INVOICEAMOUNT;
                         } else {
                             log.info("KID: Record #" + record.header.getRecordNum());
                             key = "ACC+KID:" + ACCOUNTNUMBER +":"+ KID;
                         }
 
                         if (acckid.contains(key)) {
                             displayError("Fant nøkkel "+ key + " mer enn en gang. Sist sett i record #" + record.header.getRecordNum());
                             foundDouble = true;
                         } else {
                             log.info("Adding " + key);
                             acckid.add(key);
                         }
                     }
                 }
                 if (!foundDouble) {
                     displayMessage("Fant ingen KID-kontonummer eller beløp-kontonummer-par.");
                 }
             }
         }
     }
 
 	public static void main(String[] args) throws Exception {
 
         try {
             UIManager.setLookAndFeel(
                     UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception e) {
             UIManager.setLookAndFeel(
                     UIManager.getCrossPlatformLookAndFeelClassName());
         }
 
 
             SwingUtilities.invokeLater(new Runnable()
             { public void run()  { new TelepayGUI(); } });
 	}
 }
