 package net.sourceforge.importscrubber;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.SwingUtilities;
 
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import javax.swing.text.Document;
 
 public class ImportScrubberGUI implements IProgressMonitor, ActionListener
 {
 
     private JTextField sourceFilesField, classFilesField;
     private JFrame mainFrame;
     private JCheckBox recurseCheckbox;
     private JCheckBox classFromSourceCheckbox;
     private JCheckBox sortHighCheckbox;
     private JList fileList = new JList(new UniqueListModel());
     private ImportScrubber scrubber;
     private JComboBox formats;
     private ImportScrubberMenu menu;
     private Settings settings = new Settings("importscrubber");
     private JButton goButton;
     private JButton addFilesButton, clearFilesButton;
     JTextField thresholdField = new JTextField(2);
     JCheckBox thresholdCheckbox;
 
     public ImportScrubberGUI()
     {
         ResourceBundle res = ResourceBundle.getBundle("net.sourceforge.importscrubber.Resources");
 
         mainFrame = new JFrame(res.getString(Resources.VERSION_ID));
         mainFrame.setLocation(200, 150);
         mainFrame.setSize(600,520);
         mainFrame.addWindowListener(new BasicWindowMonitor());
         mainFrame.getContentPane().setLayout(new BorderLayout());
 
         recurseCheckbox = new JCheckBox(res.getString(Resources.RECURSE_LABEL));
         if (settings.get(ImportScrubber.RECURSE)!=null) {
             recurseCheckbox.setSelected(Boolean.valueOf(settings.get(ImportScrubber.RECURSE)).booleanValue());
         }
         sortHighCheckbox = new JCheckBox(res.getString(Resources.SORT_JAVA_LIBS_LABEL));
         if (settings.get(ImportScrubber.SORT_STD_HIGH)!=null) {
             sortHighCheckbox.setSelected(Boolean.valueOf(settings.get(ImportScrubber.SORT_STD_HIGH)).booleanValue());
         }
         classFromSourceCheckbox = new JCheckBox("Class files are in source directory");
         if (settings.get(ImportScrubber.SYNC_CLASS_TO_SOURCE)!=null) {
             classFromSourceCheckbox.setSelected(Boolean.valueOf(settings.get(ImportScrubber.SYNC_CLASS_TO_SOURCE)).booleanValue());
         }
         classFromSourceCheckbox.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         maybeSyncClassToSource();
                     }
                 }
                                                  );
 
         sourceFilesField = new JTextField(43);
         classFilesField = new JTextField(43);
         BrowseButton sourceBrowseButton = new BrowseButton(sourceFilesField, res.getString(Resources.BROWSE_LABEL));
         BrowseButton classBrowseButton = new BrowseButton(classFilesField, res.getString(Resources.BROWSE_LABEL));
         if(settings.get(ImportScrubber.START_DIRECTORY_KEY) != null) {
             sourceFilesField.setText(settings.get(ImportScrubber.START_DIRECTORY_KEY));
         }
         if(settings.get(ImportScrubber.CLASS_DIRECTORY_KEY) != null) {
             classFilesField.setText(settings.get(ImportScrubber.CLASS_DIRECTORY_KEY));
         }
         sourceFilesField.getDocument().addDocumentListener(new DocumentListener() {
                     private void update() {
                         maybeSyncClassToSource();
                     }
                     public void removeUpdate(DocumentEvent e) {
                         update();
                     }
                     public void insertUpdate(DocumentEvent e) {
                         update();
                     }
                     public void changedUpdate(DocumentEvent e) {}
                 }
                                                           );
         maybeSyncClassToSource();
 
         JLabel sourceFilesLabel = new JLabel("Source File/Directory");
         JPanel sourceFilesPanel = new JPanel(new FlowLayout());
         sourceFilesPanel.add(sourceFilesField);
         sourceFilesPanel.add(sourceBrowseButton);
 
         JLabel classFilesLabel = new JLabel("Class Directory");
         JPanel classFilesPanel = new JPanel(new FlowLayout());
         classFilesPanel.add(classFilesField);
         classFilesPanel.add(classBrowseButton);
 
         clearFilesButton = new JButton(res.getString(Resources.CLEAR_FILES_LABEL));
         clearFilesButton.setMnemonic('C');
         clearFilesButton.addActionListener(this);
 
         addFilesButton = new JButton(res.getString(Resources.FIND_FILES_LABEL));
         addFilesButton.setMnemonic('A');
         addFilesButton.addActionListener(this);
 
         goButton = new JButton(res.getString(Resources.GO_LABEL));
         goButton.setMnemonic('G');
         goButton.addActionListener(this);
         goButton.setEnabled(false);
 
         JPanel buttonsPanel = new JPanel(new FlowLayout());
         buttonsPanel.add(addFilesButton);
         buttonsPanel.add(clearFilesButton);
 
         JScrollPane listPane = new JScrollPane(fileList);
         JPanel listPanel = new JPanel(new BorderLayout());
         listPanel.add(listPane,BorderLayout.CENTER);
 
         JPanel filesPanel = new JPanel(new GridBagLayout());
         filesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Files"));
 
         GridBagConstraints c = new GridBagConstraints();
         c.anchor = GridBagConstraints.NORTHWEST;
         c.weightx = 1;
         c.gridx = 0;
         c.gridy = 0;
         c.fill = GridBagConstraints.NONE;
         c.gridwidth = 1;
         filesPanel.add(recurseCheckbox, c);
 
         c.gridy = 1;
         filesPanel.add(classFromSourceCheckbox, c);
 
         c.gridy = 2;
         filesPanel.add(sourceFilesLabel, c);
 
         c.gridy = 3;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridwidth = 2;
         filesPanel.add(sourceFilesPanel, c);
 
         c.gridy = 4;
         c.fill = GridBagConstraints.NONE;
         c.gridwidth = 1;
         filesPanel.add(classFilesLabel, c);
 
         c.gridy = 5;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridwidth = 2;
         filesPanel.add(classFilesPanel, c);
 
         c.gridy = 6;
         c.fill = GridBagConstraints.NONE;
         c.gridwidth = 1;
         filesPanel.add(buttonsPanel, c);
 
         c.gridy = 7;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridwidth = 2;
         filesPanel.add(listPanel, c);
 
         formats = new JComboBox();
         for (Iterator i = StatementFormat.formatIterator(); i.hasNext(); ) {
             formats.addItem(i.next());
         }
         if (settings.get(ImportScrubber.BREAK_STYLE) != null) {
 			formats.setSelectedItem(settings.get(ImportScrubber.BREAK_STYLE));
         }
 
         JLabel thresholdLabel = new JLabel("Import package.* if more than this many classes used: ");
         if (settings.get(ImportScrubber.THRESHOLD) != null) {
 			thresholdField.setText(settings.get(ImportScrubber.THRESHOLD));
 		} else {
 			thresholdField.setText("0");
 		}
         thresholdField.setMaximumSize(thresholdField.getPreferredSize());
         thresholdCheckbox = new JCheckBox("Only apply to standard libraries");
         if (settings.get(ImportScrubber.THRESHOLD_STD_ONLY)!=null) {
             thresholdCheckbox.setSelected(Boolean.valueOf(settings.get(ImportScrubber.THRESHOLD_STD_ONLY)).booleanValue());
         }
         JPanel thresholdPanel = new JPanel();
         thresholdPanel.setLayout(new BoxLayout(thresholdPanel, BoxLayout.X_AXIS));
         thresholdPanel.add(thresholdLabel);
         thresholdPanel.add(thresholdField);
         thresholdPanel.add(thresholdCheckbox);
 
         JPanel optionsPanel = new JPanel();
         optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
         optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));
         thresholdPanel.setAlignmentX(JDialog.LEFT_ALIGNMENT);
         optionsPanel.add(thresholdPanel);
         sortHighCheckbox.setAlignmentX(JDialog.LEFT_ALIGNMENT);
         optionsPanel.add(sortHighCheckbox);
         formats.setAlignmentX(JDialog.LEFT_ALIGNMENT);
         formats.setMaximumSize(formats.getPreferredSize());
         optionsPanel.add(formats);
 
         menu = new ImportScrubberMenu(this);
         mainFrame.setJMenuBar(menu);
         mainFrame.getContentPane().add(filesPanel,BorderLayout.NORTH);
         mainFrame.getContentPane().add(optionsPanel,BorderLayout.CENTER);
         mainFrame.getContentPane().add(goButton, BorderLayout.SOUTH);
 
         scrubber = new ImportScrubber(System.getProperty("file.encoding"));
 
         mainFrame.setVisible(true);
     }
 
     private void maybeSyncClassToSource()
     {
         classFilesField.setEnabled(!classFromSourceCheckbox.isSelected());
         if (classFromSourceCheckbox.isSelected()) {
             classFilesField.setText(ImportScrubber.getDirectory(sourceFilesField.getText()));
         }
     }
 
     public void go()
     {
         if (fileList.getModel().getSize() == 0) {
             return;
         }
 
         setBusy(true);
 
         ResourceBundle res = ResourceBundle.getBundle("net.sourceforge.importscrubber.Resources");
 
         try {
             scrubber.setFormat(new StatementFormat(sortHighCheckbox.isSelected(),
                                                    StatementFormat.valueToKey(formats.getSelectedItem()),
                                                    new Integer(thresholdField.getText()).intValue(),
                                                    thresholdCheckbox.isSelected()));
             int count = scrubber.buildTasks(new Iterator() {
                                                 Enumeration e = ((UniqueListModel)fileList.getModel()).elements();
                                                 public Object next() {
                                                     return e.nextElement();
                                                 }
                                                 public boolean hasNext() {
                                                     return e.hasMoreElements();
                                                 }
                                                 public void remove
                                                 () {}
                                             }
                                            );
             scrubber.runTasks(this);
             setBusy(false);
 
             JOptionPane.showMessageDialog(null, res.getString(Resources.ALL_DONE) + " - processed " + count + " files", res.getString(Resources.APP_NAME), JOptionPane.INFORMATION_MESSAGE);
         } catch (Exception e) {
             setBusy(false);
             JOptionPane.showMessageDialog(null, res.getString(Resources.ERR_UNABLE_TO_FINISH) + e.getMessage(), res.getString(Resources.APP_NAME), JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
         }
     }
 
     public void find()
     {
         setBusy(true);
         try {
             scrubber.setFileRoot(sourceFilesField.getText(), classFilesField.getText(), recurseCheckbox.isSelected());
             UniqueListModel model = (UniqueListModel)fileList.getModel();
             for (Iterator i = scrubber.getFilesIterator(); i.hasNext();) {
                 FilePair fp = (FilePair)i.next();
                 model.addElement(fp);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         setBusy(false);
         // leave it too swing to repaint.  Model will handle repaints for you.
     }
 
     public void destroy()
     {
         settings.put(ImportScrubber.START_DIRECTORY_KEY, sourceFilesField.getText());
         settings.put(ImportScrubber.CLASS_DIRECTORY_KEY, classFilesField.getText());
         settings.put(ImportScrubber.RECURSE, String.valueOf(recurseCheckbox.isSelected()));
         settings.put(ImportScrubber.SORT_STD_HIGH, String.valueOf(sortHighCheckbox.isSelected()));
         settings.put(ImportScrubber.SYNC_CLASS_TO_SOURCE, String.valueOf(classFromSourceCheckbox.isSelected()));
         settings.put(ImportScrubber.THRESHOLD, thresholdField.getText());
         settings.put(ImportScrubber.THRESHOLD_STD_ONLY, String.valueOf(thresholdCheckbox.isSelected()));
         settings.put(ImportScrubber.BREAK_STYLE, (String)formats.getSelectedItem());
         try {
             settings.save();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
 
         mainFrame.setVisible(false);
 
         mainFrame.dispose();
         System.exit(0);
     }
 
     public void actionPerformed(java.awt.event.ActionEvent actionEvent)
     {
         if(actionEvent.getSource() == clearFilesButton) {
             UniqueListModel model = (UniqueListModel)fileList.getModel();
             model.removeAllElements();
             setBusy(false); // disables Go
         } else if(actionEvent.getSource() == addFilesButton) {
             Thread fc = new Thread() {
                             public void run() {
                                 setBusy(true);
                                 find();
                                 setBusy(false);
                             }
                         };
             // Keeps Swing UI Usable
             fc.setPriority(Thread.MIN_PRIORITY);
             fc.start();
         } else if(actionEvent.getSource() == goButton) {
             Thread go = new Thread() {
                             public void run() {
                                 go();
                             }
                         };
             // Keeps Swing UI Usable
             go.setPriority(Thread.MIN_PRIORITY);
             go.start();
         }
     }
 
     public void setBusy(boolean busy)
     {
         javax.swing.SwingUtilities.invokeLater(new SetBusy(busy));
     }
 
     public class SetBusy implements Runnable
     {
         boolean busy;
         public SetBusy(boolean busy)
         {
             this.busy = busy;
         }
         public void run()
         {
             addFilesButton.setEnabled(!busy);
             clearFilesButton.setEnabled(!busy);
             goButton.setEnabled(!busy && fileList.getModel().getSize() > 0);
 
             if(busy) {
                 mainFrame.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
             } else {
                 mainFrame.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
             }
         }
     }
 
     // ITaskListener
     public void taskStarted(ScrubTask task)
     {
         UniqueListModel model = (UniqueListModel)fileList.getModel();
         for (Enumeration e = model.elements(); e.hasMoreElements();) {
             FilePair pair = (FilePair)e.nextElement();
             if (pair.getSourceFile().getAbsolutePath().equals(task.getSourcePath())) {
                 fileList.setSelectedValue(pair, true);
             }
         }
     }
 
     public void taskComplete(ScrubTask task)
     {
         fileList.clearSelection();
     }
     // ITaskListener
 
     public static void main(String[] args)
     {
         new ImportScrubberGUI();
     }
 
     private class BrowseButton extends JButton
     {
         private JTextField _field;
 
         public BrowseButton(final JTextField field, String label)
         {
             super(label);
             _field = field;
             addActionListener(new ActionListener() {
                                   public void actionPerformed(ActionEvent e) {
                                       JFileChooser chooser = new JFileChooser(field.getText());
 
                                       chooser.setDialogTitle(ResourceBundle.getBundle("net.sourceforge.importscrubber.Resources").getString(Resources.FILE_BROWSER_TITLE));
                                       chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                                       chooser.showOpenDialog(mainFrame);
 
                                       if (chooser.getSelectedFile() != null) {
                                           _field.setText(chooser.getSelectedFile().getAbsolutePath());
                                       }
                                   }
                               }
                              );
         }
     }
 
     private class BasicWindowMonitor extends WindowAdapter
     {
         public void windowClosing(WindowEvent we)
         {
             destroy();
         }
     }
 
     private class UniqueListModel extends DefaultListModel
     {
         private Map contents = new HashMap(1000);
         private Boolean t = Boolean.TRUE;
 
         public UniqueListModel()
         {
             super();
         }
 
         @SuppressWarnings("unchecked")
         public void addElement(Object o)
         {
             if (!contents.containsKey(o)) {
                 super.addElement(o);
                 contents.put(o, t);
             }
         }
 
         public void removeAllElements()
         {
             super.removeAllElements();
             contents.clear();
         }
     }
 }
