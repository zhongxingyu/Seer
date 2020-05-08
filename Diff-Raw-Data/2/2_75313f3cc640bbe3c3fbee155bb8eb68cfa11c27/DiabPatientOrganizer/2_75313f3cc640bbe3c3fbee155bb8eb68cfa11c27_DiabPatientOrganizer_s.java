 package com.osdiab.patient_organizer;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.filechooser.FileSystemView;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 /**
  * Author: odiab
  * Date: 6/21/13
  * Time: 9:03 AM
  * Triggers sequences of actions that correspond to actions in the program.
  * Entry point into the program.
  */
 public class DiabPatientOrganizer extends JFrame
         implements ActionListener
 {
     public static final DateFormat DATE_FORMAT;
     static {
         DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy");
         DATE_FORMAT.setTimeZone(TimeZone.getDefault());
     }
 
     private File directory;
     private JButton chooseButton;
     private static FileCache cache = new FileCache();
 
     public static FileCache getCache()
     {
         return cache;
     }
 
     private void initUI()
     {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             // do nothing, it's fine to have default look and feel.
         }
 
         setTitle("Patient Organizer");
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         JLabel introText = new JLabel(
                 "<html><p>Enter a file containing a patient schedule.</p>" +
                         "<p>The data can contain more than one day.</p></html>"
         );
         getContentPane().add(introText, BorderLayout.NORTH);
 
         chooseButton = new JButton("Choose XML");
         chooseButton.addActionListener(this);
         chooseButton.setPreferredSize(new Dimension(300, 150));
         getContentPane().setPreferredSize(new Dimension(300, 150));
 
         getContentPane().add(chooseButton, BorderLayout.CENTER);
         pack();
         setVisible(true);
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == chooseButton) {
             organizeSchedule();
         }
     }
 
     private void organizeSchedule() {
         Schedule schedule = getScheduleFromUser();
         if (schedule == null) return;
         FileOrganizer organizer = new FileOrganizer(directory);
         List<UpdateData> result = null;
         try {
             result = organizer.organizeSchedule(schedule);
             displayFeedback(result);
         } catch (IOException e) {
             displayError(e);
         }
     }
 
     private void displayError(Exception e) {
         String message = "<html>" +
                 "<p>An error occurred that I couldn't deal with. Call Omar!</p>" +
                 "<p>Reason: '" + e.getMessage() + "'</p></html>";
         JOptionPane.showMessageDialog(this, message);
     }
 
     private void displayFeedback(List<UpdateData> result) {
         StringBuilder messageBuilder = new StringBuilder();
         messageBuilder.append("<html><p>Patients were placed in the following folders:</p>");
         messageBuilder.append("<ul>");
         for (UpdateData data : result) {
             messageBuilder.append("<li>");
             messageBuilder.append(data.getFolderUsed().getName());
             messageBuilder.append("</li>");
 
             if (data.getErrors().isEmpty() && data.getNewPatients().isEmpty()) {
                 continue;
             }
 
             messageBuilder.append("<ul>");
             if (data.getNewPatients().size() != 0) {
                 messageBuilder.append("The following new patients were placed in this folder");
                 messageBuilder.append(" (check if they are actually new!):</li>");
                 messageBuilder.append("<ul>");
                 for (File newPatient : data.getNewPatients()) {
                     messageBuilder.append("<li>");
                     messageBuilder.append(newPatient.getName());
                     messageBuilder.append("</li>");
                 }
                 messageBuilder.append("</ul>");
             }
 
             if (data.getErrors().size() != 0) {
                 messageBuilder.append(
                        "The following patients could not have their dates changed properly (ask Omar!):</li>");
                 messageBuilder.append("<ul>");
                 for (Map.Entry<File, Exception> entry : data.getErrors().entrySet()) {
                     File file = entry.getKey();
                     Exception error = entry.getValue();
                     messageBuilder.append("<li>");
                     messageBuilder.append(file.getName());
                     messageBuilder.append(": ");
                     messageBuilder.append(error.getMessage());
                     messageBuilder.append("</li>");
                 }
                 messageBuilder.append("</ul>");
             }
 
             messageBuilder.append("</ul>");
         }
         messageBuilder.append("</ul></html>");
 
         JOptionPane.showMessageDialog(this, messageBuilder.toString());
     }
 
     private Schedule getScheduleFromUser() {
         // get file
         JFileChooser chooser = createFileChooser();
         int returnVal = chooser.showOpenDialog(this);
         if (returnVal != JFileChooser.APPROVE_OPTION) {
             return null;
         }
         File file = chooser.getSelectedFile();
 
         // do computations
         Schedule schedule = new Schedule(file);
         return schedule;
     }
 
     private JFileChooser createFileChooser() {
         JFileChooser chooser = new JFileChooser();
         FileFilter xmlFilter = new FileNameExtensionFilter("XML files (*.xml)", "xml");
         chooser.setDialogTitle("Choose an XML schedule file...");
         chooser.setFileFilter(xmlFilter);
         return chooser;
     }
 
     private void createUI()
     {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 initUI();
             }
         });
     }
 
     /**
      * Default constructor; uses system-dependent default directory as Documents location
      */
     public DiabPatientOrganizer()
     {
         JFileChooser fr = new JFileChooser();
         FileSystemView fw = fr.getFileSystemView();
         directory = fw.getDefaultDirectory();
     }
 
     /**
      * Constructor for passing in a directory to use as Documents
      * @param directory File representing Documents location
      */
     public DiabPatientOrganizer(File directory)
     {
         this.directory = directory;
     }
 
     /**
      * Entrypoint into the application
      * @param args Arguments for program
      */
     public static void main(String[] args)
     {
         DiabPatientOrganizer organizer;
         if (args.length > 0) {
             File dir = new File(args[0]);
             if (!dir.exists() || !dir.isDirectory()) {
                 System.err.println("Selected directory does not exist, or is not a directory. " +
                         "Usage: java DiabPatientOrganizer [/path/to/documents]");
                 System.exit(1);
             }
 
             organizer = new DiabPatientOrganizer(dir);
             organizer.createUI();
         } else {
             organizer = new DiabPatientOrganizer();
             organizer.createUI();
         }
 
         System.out.print("Directory used: ");
         System.out.println(organizer.directory);
     }
 }
