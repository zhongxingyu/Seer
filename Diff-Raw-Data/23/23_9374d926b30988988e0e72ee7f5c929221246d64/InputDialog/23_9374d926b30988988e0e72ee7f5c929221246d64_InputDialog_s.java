 package bioGUI;
 
 import java.awt.Component;
 import java.awt.ComponentOrientation;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.FlowLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JOptionPane;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JTextArea;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.BoxLayout;
 import javax.swing.filechooser.FileFilter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import javax.swing.ButtonGroup;
 import javax.swing.JRadioButton;
 
 @SuppressWarnings("serial")
 public class InputDialog extends JDialog {
     /*
      * CONSTANTS
      */
 
     private final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 500;
 
     /*
      * GUI Components
      */
     private Container mPane;
     private JTextField mFileFASTA, mFileGFF, mLow, mHigh, mSpSeq, mRnaStart, mShiftIncr;
     private JTextArea mDisplayArea;
     private JCheckBox mUseSlidingWindow;
 
     public InputDialog() {
         super();
 
         setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
         setResizable(false);
         setLocationRelativeTo(null);
 
         mPane = this.getContentPane();
         mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
         mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
 
         mFileFASTA = new JTextField(20);
         mFileGFF = new JTextField(20);
         mLow = new JTextField(5);
         mHigh = new JTextField(5);
         mSpSeq = new JTextField(20);
         mRnaStart = new JTextField(20);
 
         mUseSlidingWindow = new JCheckBox("Use Sliding Window", false);
 
         mDisplayArea = new JTextArea();
 
         mShiftIncr = new JTextField(20);
     }
 
     public static void main(String[] args) {
         InputDialog dialog = new InputDialog();
 
         dialog.init();
         dialog.setVisible(true);
     }
 
     /*
      * Adds all of the components to this dialog.
      */
     public void init() {
         JPanel fastaFileField = prepareFileField(mFileFASTA);
         JPanel gffFileField = prepareGFFField(mFileGFF);
         JPanel posField = prepareParamControls(mLow, mHigh, mSpSeq,
                 mShiftIncr, mRnaStart);
 
         mPane.add(fastaFileField);
         mPane.add(gffFileField);
         mPane.add(posField);
 
         //mPane.add(mDisplayArea);
         JScrollPane scrollDisplay = new JScrollPane(mDisplayArea);
         scrollDisplay.setPreferredSize(new Dimension(200, 300));
         mPane.add(scrollDisplay);
 
         mPane.add(initControls());
 
         mPane.validate();
     }
 
     private JPanel prepareGFFField(JTextField mFileGFF2) {
         JPanel gffFileField = new JPanel();
 
         gffFileField.setLayout(new FlowLayout(FlowLayout.LEADING));
 
         gffFileField.add(new JLabel("Select GFF File:"));
         gffFileField.add(mFileGFF2);
         gffFileField.add(prepareBrowseButton(mFileGFF2));
 
         return gffFileField;
     }
 
     /*
      * Creates and returns a JPanel containing sub components that make up the
      * input file selection section
      */
     // need to figure out multiple select
     private JPanel prepareFileField(JTextField fileField) {
         JPanel fastaFileField = new JPanel();
 
         fastaFileField.setLayout(new FlowLayout(FlowLayout.LEADING));
 
         fastaFileField.add(new JLabel("Select FASTA File:"));
         fastaFileField.add(fileField);
         fastaFileField.add(prepareBrowseButton(fileField));
 
         return fastaFileField;
     }
 
     /*
      * Creates and returns a JButton that can be used to browse for any given
      * file. The input JTextField is associated with the returned button such
      * that when the browse button is used to select a file, the full file name
      * is written to the input JTextField
      */
     private JButton prepareBrowseButton(final JTextField fileField) {
         JButton fileBrowse = new JButton("Browse");
 
         fileBrowse.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 JFileChooser chooser = new JFileChooser();
                 chooser.setMultiSelectionEnabled(true);
                 chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                 TreeSet<String> ts = new TreeSet<String>();
 
                 int returnVal = chooser.showOpenDialog(chooser);
 
                 if (returnVal == JFileChooser.CANCEL_OPTION) {
                     System.out.println("cancelled");
                 } else if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File[] fastaFiles = chooser.getSelectedFiles();
 
                     String fileList = "";
                     for (int i = 0; i < fastaFiles.length; i++) {
 
                         if (fastaFiles[i].isDirectory()) {
                             File[] filelist = fastaFiles[i].listFiles();
                             for (File f : filelist) {
                                 ts.add(f.getAbsolutePath());
                             }
 
                             for (String s : ts) {
                                 fileList += s + " , ";
                             }
                         } else {
                             fileList += fastaFiles[i].getAbsolutePath() + " , ";
                         }
                     }
 
                     fileField.setText(fileList);
                 } else {
                     System.out.println("Encountered Unknown Error");
                     System.exit(0);
                 }
             }
         });
 
         return fileBrowse;
     }
 
     /*
      * Creates and returns a JPanel containing all of the parameter JTextFields.
      * Additionally adds an ItemListener to the JCheckBox to hide/show the
      * JTextFields for the sliding window parameters.
      */
     @SuppressWarnings("unused")
     private JPanel prepareParamControls(JTextField low, JTextField high,
             JTextField spseq,
             JTextField folddiff,
             JTextField mrnastart) {
         JPanel controlField = new JPanel(), startField = new JPanel(),
                 endField = new JPanel(), checkBoxField = new JPanel(),
                 windowField = new JPanel(), shiftField = new JPanel();
         final JPanel slideFields = new JPanel();
 
         ButtonGroup bg = new ButtonGroup();
 
 
         JRadioButton jrb = new JRadioButton("Option 1");
         bg.add(jrb);
         startField.setLayout(new FlowLayout(FlowLayout.LEADING));
         startField.add(jrb);
         startField.add(new JLabel("Find all repeats range: Low:"));
         startField.add(low);
         low.setVisible(true);
         startField.add(new JLabel("High:"));
         startField.add(high);
 
         JRadioButton jrb2 = new JRadioButton("Option 2");
         bg.add(jrb2);
         endField.setLayout(new FlowLayout(FlowLayout.LEADING));
         endField.add(jrb2);
         endField.add(new JLabel("Find specific sequence:"));
         endField.add(spseq);
 
 
         jrb.setSelected(true);
 
 
         checkBoxField.setLayout(new FlowLayout(FlowLayout.LEADING));
         checkBoxField.add(new JLabel("Fold Difference: "));
         checkBoxField.add(folddiff);
 
 
         slideFields.setLayout(new FlowLayout(FlowLayout.LEADING));
        slideFields.add(new JLabel("NE upstream of mRNA start:"));
         slideFields.add(mrnastart);
 
         /*
          * Putting all parameter inputs together
          */
         controlField.setLayout(new BoxLayout(controlField, BoxLayout.Y_AXIS));
         controlField.add(startField);
         controlField.add(endField);
         controlField.add(checkBoxField);
         controlField.add(slideFields);
 
         return controlField;
     }
 
     /*
      * Creates and returns a JPanel containing all of the controls available on
      * this dialog window. This includes the "Run," "Save," and "Quit" buttons.
      */
     public JPanel initControls() {
         JPanel dialogControls = new JPanel();
 
         dialogControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 
         dialogControls.add(createRunButton());
         dialogControls.add(createSaveButton());
         dialogControls.add(createQuitButton());
 
         dialogControls.setAlignmentX(Component.CENTER_ALIGNMENT);
 
         return dialogControls;
     }
 
     /*
      * Creates and returns a JButton that executes the appropriate code. The
      * code right now is extremely basic (it doesn't run anything) and output is
      * written to mDisplayArea, a JTextArea in the middle (ish) of this dialog
      * window
      */
     private JButton createRunButton() {
 
         JButton runButton = new JButton("Run");
 
         runButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
 //				int start, end, winSize, winShift;
                 HashMap<String, HashMap<String, String>> outputStrs;
                 ArrayList<String> gffStrs = new ArrayList<String>();
                 ArrayList<String> fastaStrs = new ArrayList<String>();
 //				Boolean useSlidingWindow;
 
                 if (mFileFASTA.getText().equals("") || mFileGFF.getText().equals("")) {
                     JOptionPane.showMessageDialog(null,
                             "No FASTA or GFF file was selected",
                             "Invalid File", JOptionPane.ERROR_MESSAGE);
                 } else {
 
                     outputStrs = new HashMap<String, HashMap<String, String>>();
 
                     String fastaStrsRaw = mFileFASTA.getText();
                     int fastaStrsPtr = 0;
                     String gffStrsRaw = mFileGFF.getText();
                     int GffStrsPtr = 0;
 
 //					System.out.println("fastaStrsRaw:"+fastaStrsRaw);
 //					System.out.println("gffStrsRaw:"+gffStrsRaw);
 
                     fastaStrs.addAll(Arrays.asList(fastaStrsRaw.split(" , ")));
                     gffStrs.addAll(Arrays.asList(gffStrsRaw.split(" , ")));
 //					System.out.println(fastaStrs);
 //					System.out.println(gffStrs);
 /*
                     while (fastaStrsRaw != ""){
                     if (fastaStrsRaw.indexOf(',') != -1){
                     fastaStrsPtr = fastaStrsRaw.indexOf(',');
                     fastaStrs.add(fastaStrsRaw.substring(0, fastaStrsPtr));
                     fastaStrsRaw = fastaStrsRaw.substring(fastaStrsPtr+2, fastaStrsRaw.length());
                     }
                     else{
                     fastaStrs.add(fastaStrsRaw.substring(0, fastaStrsRaw.length()));
                     fastaStrsRaw = "";
                     }
                     }
 
                     while (gffStrsRaw != ""){
                     if (gffStrsRaw.indexOf(',') != -1){
                     GffStrsPtr = gffStrsRaw.indexOf(',');
                     gffStrs.add(gffStrsRaw.substring(0, GffStrsPtr));
                     gffStrsRaw = gffStrsRaw.substring(GffStrsPtr+2, gffStrsRaw.length());
                     }
                     else{
                     gffStrs.add(gffStrsRaw.substring(0, gffStrsRaw.length()));
                     }
                     }
                      */
                     //fastaStrs.add(mFileFASTA.getText());
                     //gffStrs.add();
 
                     try {
                         //may need to mess around with string output here,
                         //	since each fasta/gff combination output is going to be on the same row,
                         //	we may need to convert data into column format, or we could just talk
                         //	to bio people and see if they will accept files x data vs. data x files
                         outputStrs = GeneAnalytics.work(fastaStrs, gffStrs);
                     } catch (Exception e2) {
                         JOptionPane.showMessageDialog(null,
                                 "Error received from work method. File is invalid ",
                                 "Work Method Error", JOptionPane.ERROR_MESSAGE);
                         e2.printStackTrace();
                     }
                     String output = "	";
                     for (String key : outputStrs.keySet()) {
                         output += key + "	";
                     }
                     output += "\navgGeneSpan	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("avgGeneSpan") + "	";
                     }
                     output += "\navgCDSLength	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("avgCDSLength") + "	";
                     }
                     output += "\navgExonSize	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("avgExonSize") + "	";
                     }
                     output += "\navgIntronSize	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("avgIntronSize") + "	";
                     }
                     output += "\navgIntergenicSize	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("avgIntergenicSize") + "	";
                     }
                     output += "\navgNucDensitySpan	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("avgNucDensitySpan") + "	";
                     }
                     output += "\navgNucDensityRegion	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("avgNucDensityRegion") + "	";
                     }
                     output += "\ncdsNucProportion	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("cdsNucProportion") + "	";
                     }
                     output += "\ngenesPer10KB	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("genesPerKB") + "	";
                     }
                     output += "\nkbPerGene	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("kbPerGene") + "	";
                     }
                     output += "\npredictProtein	";
                     for (String key : outputStrs.keySet()) {
                         output += outputStrs.get(key).get("predictProtein") + "	";
                     }
 
                     mDisplayArea.setText(output);
                 }
             }
         });
 
         return runButton;
     }
 
     /*
      * Creates and returns a JButton that allows the user to select a file for
      * the contents of the JTextArea to be written to.
      */
     private JButton createSaveButton() {
         JButton saveButton = new JButton("Save");
 
         saveButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (mDisplayArea.getText().equals("")) {
                     JOptionPane.showMessageDialog(null,
                             "No output to save", "Empty output",
                             JOptionPane.ERROR_MESSAGE);
                 } else {
                     JFileChooser chooser = new JFileChooser();
                     //Set file type that should be display by set JFileChooser's file filter
                     chooser.setFileFilter(new TypeOfFile());
                     int returnVal = chooser.showSaveDialog(mPane);
 
                     if (returnVal == JFileChooser.APPROVE_OPTION) {
                         try {
                             FileWriter writer = new FileWriter(chooser.getSelectedFile() + ".csv");
                             writer.write(mDisplayArea.getText());
                             writer.close();
                         } catch (java.io.IOException ioErr) {
                             JOptionPane.showMessageDialog(null,
                                     "Encountered unknown error when saving output",
                                     "Unable to save output", JOptionPane.ERROR_MESSAGE);
                         }
                     } else if (returnVal == JFileChooser.ERROR_OPTION) {
                         JOptionPane.showMessageDialog(null,
                                 "Encountered unknown error when saving output",
                                 "Unable to save output", JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
         });
 
         return saveButton;
     }
 
     /*
      * Self explanatory
      */
     private JButton createQuitButton() {
         JButton quitButton = new JButton("Quit");
 
         quitButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 dispose(); //closes the dialog window
                 return;
             }
         });
 
         return quitButton;
     }
 
     //Class TypeOfFile
     class TypeOfFile extends FileFilter {
         //Type of file that should be display in JFileChooser will be set here
         //We choose to display only directory and text file
 
         public boolean accept(File f) {
             return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
         }
 
         //Set description for the type of file that should be display
         public String getDescription() {
             return ".csv files";
         }
     }
 }
