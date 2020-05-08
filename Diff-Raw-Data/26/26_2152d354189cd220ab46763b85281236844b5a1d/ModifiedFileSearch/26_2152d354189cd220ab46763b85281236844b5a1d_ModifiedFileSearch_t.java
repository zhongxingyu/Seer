 package modifiedfilesearch;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Queue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadFactory;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import modifiedfilesearch.data.DisplayStringImpl;
 import modifiedfilesearch.data.DisplayString;
 import modifiedfilesearch.data.FileInfo;
 import modifiedfilesearch.ui.DisplayStringLayerUI;
 import modifiedfilesearch.ui.DirectoryEntryLayerUI;
 import modifiedfilesearch.utils.NamedThreadFactory;
 
 /**
  * A graphical application that searches directories for modified files and
  * displays results to the user. The intention of this application is to 
  * demonstrate a handful of new features in Java 7. 
  *
  * @author Brendan Cashman
  * @since 1.7
  */
 class ModifiedFileSearch implements ActionListener {
 
     /**
      * 
      */
     private ModifiedFileSearch() {
         searchStatusQueue = new ConcurrentLinkedQueue<DisplayString>();
         infoQueue = new ConcurrentLinkedQueue<DisplayString>();
         fileInfoQueue = new ConcurrentLinkedQueue<FileInfo>();
         fileWalkThreadFactory = new NamedThreadFactory("file-walk");
         translateThreadFactory = new NamedThreadFactory("translate-file-info");
     }
 
     /**
      * Creates and configures the GUI components for this application.
      *
      */
     private void createUI() {
 
         // Make sure the button will activate if it has focus and the
         // user hits the enter key.
         UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
 
         JLabel directoryLabel = new JLabel("Directory to Search:");
         directoryLabel.setPreferredSize(new Dimension(140, 20));
 
         dirEntryLayerUI = new DirectoryEntryLayerUI();
         String defaultPath = System.getProperty("user.home");
         pathField = new JTextField();
         pathField.setColumns(30);
         pathField.setText(defaultPath);
 
         fileChooserButton = new JButton("...");
         final JFileChooser chooser = new JFileChooser(pathField.getText());
         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         fileChooserButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (dirEntryLayerUI.isEntryValid()) {
                     chooser.setCurrentDirectory(new File(pathField.getText()));
                 }
                 if (chooser.showOpenDialog(pathField) == JFileChooser.OPEN_DIALOG) {
                     String newPath = chooser.getSelectedFile().getPath();
                     pathField.setText(newPath);
                 }
             }
         });
 
         JPanel pathPanel = new JPanel();
         pathPanel.add(new JLayer<JTextField>(pathField, dirEntryLayerUI));
         pathPanel.add(fileChooserButton);
 
         JLabel fileTypeLabel = new JLabel("Files Extension(s) to Search:");
         fileTypeLabel.setPreferredSize(new Dimension(180, 20));
 
         fileTypeTF = new JTextField("*.java *.xml *.conf *.script");
         fileTypeTF.setPreferredSize(new Dimension(120, 20));
         fileTypeTF.setToolTipText("Enter '*.*' to search all files");
         JLabel modParameterLabel = new JLabel("Search for files modified since:");
 
         everButton = new JRadioButton("Ever");
         everButton.setSelected(true);
         thirtyDayButton = new JRadioButton("30 Days");
         weekButton = new JRadioButton("Week");
         hourButton = new JRadioButton("Hour");
 
         ButtonGroup group = new ButtonGroup();
         group.add(everButton);
         group.add(thirtyDayButton);
         group.add(weekButton);
         group.add(hourButton);
         JPanel modifiedPanel = new JPanel();
         modifiedPanel.add(everButton);
         modifiedPanel.add(thirtyDayButton);
         modifiedPanel.add(weekButton);
         modifiedPanel.add(hourButton);
 
         modifiedButton = new JButton("Search for Modifications");
         modifiedButton.setActionCommand(SEARCH_COMMAND);
         modifiedButton.addActionListener(this);
 
         final Box outerBox = Box.createVerticalBox();
         outerBox.add(Box.createVerticalStrut(15));
         final Box dirBox = Box.createHorizontalBox();
         dirBox.add(Box.createHorizontalStrut(5));
         dirBox.add(directoryLabel);
         dirBox.add(Box.createHorizontalGlue());
         outerBox.add(dirBox);
         outerBox.add(Box.createVerticalStrut(5));
         outerBox.add(pathPanel);
         outerBox.add(Box.createVerticalStrut(15));
         final Box typeBox = Box.createHorizontalBox();
         typeBox.add(Box.createHorizontalStrut(5));
         typeBox.add(fileTypeLabel);
         typeBox.add(Box.createHorizontalStrut(5));
         typeBox.add(fileTypeTF);
         outerBox.add(typeBox);
         outerBox.add(Box.createVerticalStrut(20));
         final Box entryBox = Box.createHorizontalBox();
         entryBox.add(Box.createHorizontalStrut(5));
         entryBox.add(modParameterLabel);
         entryBox.add(Box.createHorizontalGlue());
         outerBox.add(entryBox);
         outerBox.add(Box.createVerticalStrut(5));
         outerBox.add(modifiedPanel);
         outerBox.add(Box.createVerticalStrut(25));
         final Box buttonBox = Box.createHorizontalBox();
         buttonBox.add(Box.createHorizontalGlue());
         buttonBox.add(modifiedButton);
         buttonBox.add(Box.createHorizontalGlue());
         outerBox.add(buttonBox);
         JPanel panel = new JPanel();
         panel.add(outerBox);
 
         searchStatusLayerUI = new DisplayStringLayerUI(searchStatusQueue);
         footerPanel = new JPanel();
         final JButton pauseButton = new JButton(ButtonState.Pause.toString());
                      
         pauseButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                // The pause/restart functionality is intended to effect only the
                 // display of the search. The inspected of files will continue, and 
                 // possibly finish. Once the application is restarted, the status will
                // be displayed as if the file walk was still occurring.
                 ButtonState currentState = ButtonState.valueOf(pauseButton.getText());
                 switch (currentState) {
                     case Pause:
                         pauseButton.setText(ButtonState.Restart.toString());
                         searchStatusLayerUI.pauseScroll();
                         break;
                     case Restart:
                         pauseButton.setText(ButtonState.Pause.toString());
                         searchStatusLayerUI.resumeScroll();
                         break;
                 }
             }
         });
 
         infoLayerUI = new DisplayStringLayerUI(infoQueue);
 
         final JButton resetButton = new JButton("Reset");
         resetButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 // cancel the current search and return GUI to original state.
                 isWalking = false;
                 fileWalkTaskFuture.cancel(true);
                 translateTaskFuture.cancel(true);
                 fileInfoQueue.clear();
                 searchStatusQueue.clear();
                 infoQueue.clear();
                 
                 searchStatusLayerUI.stop();
                 infoLayerUI.stop();                
                 footerPanel.setVisible(false);
                 modifiedButton.setEnabled(true);
                 fileChooserButton.setEnabled(true);
                 pauseButton.setText(ButtonState.Pause.toString());
             }
         });
 
         fpsSlider = new JSlider(JSlider.HORIZONTAL,
                 0, 100, 10);
         fpsSlider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 JSlider source = (JSlider) e.getSource();
                 if (!source.getValueIsAdjusting()) {
                     int fps = source.getValue();
                     searchStatusLayerUI.setFPS(fps);
                 }
             }
         });
 
         //Turn on labels at major tick marks.
         fpsSlider.setMajorTickSpacing(20);
         fpsSlider.setMinorTickSpacing(0);
         fpsSlider.setPaintLabels(true);
 
         JPanel sliderPanel = new JPanel();
         sliderPanel.add(new JLabel("Adjust Display Speed:"));
         sliderPanel.add(fpsSlider);
         sliderPanel.setBorder(BorderFactory.createEtchedBorder());
         footerPanel.add(sliderPanel);
         footerPanel.add(Box.createRigidArea(new Dimension(40, 0)));
         footerPanel.add(pauseButton);
         footerPanel.add(Box.createHorizontalStrut(5));
         footerPanel.add(resetButton);
         footerPanel.setVisible(false);
 
         JTextPane editorPane = new JTextPane();
         editorPane.setEditable(false);
         java.net.URL helpURL = ModifiedFileSearch.class.getResource(
                 "Modified File Search.htm");
         if (helpURL != null) {
             try {
                 editorPane.setPage(helpURL);
             } catch (IOException e) {
                 System.err.println("Attempted to read a bad URL: " + helpURL);
             }
         } else {
             System.err.println("Couldn't find file: ModifiedFilesFinder.html");
         }
 
         JScrollPane editorScrollPane =
                new JScrollPane(new JLayer(editorPane, infoLayerUI));
         editorScrollPane.setVerticalScrollBarPolicy(
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         editorScrollPane.setHorizontalScrollBarPolicy(
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         editorScrollPane.setPreferredSize(new Dimension(400, 250));
         editorScrollPane.setMinimumSize(new Dimension(10, 10));
 
         JFrame frame = new JFrame("Modified File Search");
         frame.getContentPane().setLayout(new BorderLayout());
 
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                editorScrollPane, new JLayer(panel, searchStatusLayerUI));
         frame.getContentPane().add(splitPane, BorderLayout.CENTER);
         frame.getContentPane().add(footerPanel, BorderLayout.SOUTH);
 
         frame.pack();
         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
     }
 
     @Override
    public void actionPerformed(final ActionEvent e) {
         // This actionPerformed is intended to only handle an event that starts the search.
         if (e.getActionCommand() == null
                 || !e.getActionCommand().equals(SEARCH_COMMAND)) {
             return;
         }
 
         // Parse and validate the user entered formats.
         String enteredTypes = fileTypeTF.getText();
         if (enteredTypes == null
                 || enteredTypes.isEmpty() ) {
             JOptionPane.showMessageDialog(fileTypeTF,
                     "Please enter a file type to search.", "Input Required",
                     JOptionPane.INFORMATION_MESSAGE);
             return;
         } else if ( !dirEntryLayerUI.isEntryValid() ) {
             JOptionPane.showMessageDialog(fileTypeTF,
                     "Please enter a valid directory to search.", "Input Required",
                     JOptionPane.INFORMATION_MESSAGE);
             return;
         }
 
         modifiedButton.setEnabled(false);
         fileChooserButton.setEnabled(false);
         fpsSlider.setValue(10);
         footerPanel.setVisible(true);
 
         searchStatusLayerUI.start();
         infoLayerUI.start();
         // show some basic information over the text area
         String dirInfo = "Directories in white have been searched.";
         String modInfo = "Files in red have been modified.";
         String noModInfo = "Files in green have not been modified";
 
         infoQueue.add(new DisplayStringImpl(dirInfo, Color.WHITE));
         infoQueue.add(new DisplayStringImpl(modInfo, Color.RED));
         infoQueue.add(new DisplayStringImpl(noModInfo, Color.GREEN));
 
         // Determine the modified time to search for
         if (everButton.isSelected()) {
             modifiedTimeWindow = 0;
         } else if (thirtyDayButton.isSelected()) {
             modifiedTimeWindow = (MS_IN_HOUR * 720);
         } else if (weekButton.isSelected()) {
             modifiedTimeWindow = (MS_IN_HOUR * 168);
         } else if (hourButton.isSelected()) {
             modifiedTimeWindow = MS_IN_HOUR;
         }
 
         final Path path = Paths.get(pathField.getText());
         // regexp will remove '.' and ',', returning just file types
         String[] strsToAdd = enteredTypes.split("\\s*(,|\\s|\"|\')\\s*");
         boolean first = true;
        // Using the "glob" syntax defined here:
         // http://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)
         StringBuilder pttrBuilder = new StringBuilder("glob:*.{");
         for (String str : strsToAdd) {
             if (!str.isEmpty()) {
                 if (str.startsWith("."))
                     str = str.substring(1);
                 else if (str.startsWith("*."))
                     str = str.substring(2);
                 if (!first) {
                     pttrBuilder.append(",");
                 } else {
                     first = false;
                 }
                 pttrBuilder.append(str);
             }
         }
         pttrBuilder.append("}");
         final String pattern = pttrBuilder.toString();
 
         isWalking = true;
        // Start 2 threads, one to execute the file walk and one to translate
        // the output of the file walk to a format for the status components.
         
         TranslateFileInfoTask translateTask = new TranslateFileInfoTask();
         translateTaskFuture =
                 Executors.newSingleThreadExecutor(translateThreadFactory).submit(translateTask);
 
         FileWalkTask walkTask = new FileWalkTask(path, pattern);        
         fileWalkTaskFuture =
                 Executors.newSingleThreadExecutor(fileWalkThreadFactory).submit(walkTask);
     }
 
     private enum ButtonState {
         Pause, Restart
     }
 
     private static final String SEARCH_COMMAND = "StartModifiedSearch";
     private static final long MS_IN_HOUR = 3600000;
     private boolean isWalking = false;
     private DisplayStringLayerUI searchStatusLayerUI;
     private DisplayStringLayerUI infoLayerUI;
     private DirectoryEntryLayerUI dirEntryLayerUI;
     private Future fileWalkTaskFuture;
     private Future translateTaskFuture;
     private long modifiedTimeWindow;
     private final Queue<DisplayString> infoQueue;
     private final Queue<DisplayString> searchStatusQueue;
     private final Queue<FileInfo> fileInfoQueue;
     private final ThreadFactory fileWalkThreadFactory;
     private final ThreadFactory translateThreadFactory;
     private JButton fileChooserButton;
     private JButton modifiedButton;
     private JPanel footerPanel;
     private JRadioButton everButton;
     private JRadioButton thirtyDayButton;
     private JRadioButton weekButton;
     private JRadioButton hourButton;
     private JSlider fpsSlider;
     private JTextField fileTypeTF;
     private JTextField pathField;
 
     public static void main(String[] args) {
         final ModifiedFileSearch finder = new ModifiedFileSearch();
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 finder.createUI();
             }
         });
     }
 
     /**
      * This task will translate the FileInfo objects provided by the
      * SpecifiedFileVisitor to DisplayStrings that can be rendered by the
      * DisplayStringLayerUI.
      */
     private class TranslateFileInfoTask implements Callable {
 
         @Override
         public Object call() {
 
             while (isWalking || fileInfoQueue.size() > 0) {
                 FileInfo info = fileInfoQueue.poll();
                 if (info != null) {
                     String str = info.getPath().toString();
                     Color color;
                     if (info.getBasicFileAttributes() != null) {
                         if (info.getBasicFileAttributes().isDirectory()) {
                             color = Color.WHITE;
                         } else {
                             // if the window is set to 0, used creation time
                             long threshold = info.getBasicFileAttributes().creationTime().toMillis();
                             if (modifiedTimeWindow != 0) {
                                 threshold = System.currentTimeMillis() - modifiedTimeWindow;
                             }
                             if (info.getBasicFileAttributes().lastModifiedTime().toMillis()
                                     > threshold) {
                                 color = Color.RED;
                             } else {
                                 color = Color.GREEN;
                             }
                         }
                         DisplayStringImpl dsi = new DisplayStringImpl(str, color);
                         searchStatusQueue.add(dsi);
                     } else {
                         str = "Could not access " + str;
                         color = Color.ORANGE;
                         DisplayStringImpl dsi = new DisplayStringImpl(str, color);
                         infoQueue.add(dsi);
                     }
                 }
             }
             searchStatusQueue.add(new DisplayStringImpl("Finished", Color.GREEN));
             return true;
         }
     }
 
     /**
      * This task will execute the FileWalk of the path specified, visiting files
      * matching the pattern specified.
      */
     private class FileWalkTask implements Callable {
 
         /**
          * Constructs a task to execute the "walk" of the directory specified.
          * If the walk produces a IOException, an appropriate DisplayString will
          * be added to the infoQueue.
          *
          * @param pathToWalk - Path to the directory to be walked.
          * @param patternToMatch - Pattern to match files to while walking.
          */
         private FileWalkTask(Path pathToWalk, String patternToMatch) {
             this.pathToWalk = pathToWalk;
             this.patternToMatch = patternToMatch;
         }
 
         @Override
         public Object call() throws Exception {
             try {
                 SpecifiedFileVisitor fileVisitor =
                         new SpecifiedFileVisitor(fileInfoQueue, patternToMatch);
                 Files.walkFileTree(pathToWalk, fileVisitor);
                 isWalking = false;
             } catch (IOException ioe) {
                 String str = ioe.getMessage();
                 DisplayString dspStr = new DisplayStringImpl(str, Color.ORANGE);
                 infoQueue.add(dspStr);
             }
             return true;
         }
         final private Path pathToWalk;
         final private String patternToMatch;
     }
 }
