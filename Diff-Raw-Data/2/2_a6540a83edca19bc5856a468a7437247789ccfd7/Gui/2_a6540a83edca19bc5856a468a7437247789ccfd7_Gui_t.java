 package org.spigotmc.interglot;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.WindowConstants;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 public class Gui extends JFrame {
 
     private final Logger logger = Logger.getLogger("Interglot");
     private final JFileChooser chooser = new JFileChooser();
 
     {
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         chooser.setFileFilter(new FileNameExtensionFilter("Plugins (.jar)", "jar"));
         logger.setUseParentHandlers(false);
         logger.addHandler(new Handler() {
             @Override
             public void publish(LogRecord record) {
                 logText.append(record.getMessage() + '\n');
                 logText.setCaretPosition(logText.getText().length());
             }
 
             @Override
             public void flush() {
             }
 
             @Override
             public void close() throws SecurityException {
             }
         });
     }
 
     public Gui() {
         initComponents();
     }
 
     private void initComponents() {//GEN-BEGIN:initComponents
 
         inputFile = new JTextField();
         outputFile = new JTextField();
         inputLabel = new JLabel();
         outputLabel = new JLabel();
         scrollPanel = new JScrollPane();
         logText = new JTextArea();
         transformButton = new JButton();
         copyright = new JLabel();
         versionLabel = new JLabel();
         minecraftVersion = new JTextField();
         libigot = new JLabel();
         jScrollPane1 = new JScrollPane();
         warning = new JTextArea();
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("Interglot Jar Repackager");
         setResizable(false);
 
         inputFile.setEditable(false);
         inputFile.setFocusable(false);
         inputFile.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 inputFileMouseClicked(evt);
             }
         });
 
         outputFile.setEditable(false);
         outputFile.setFocusable(false);
         outputFile.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 outputFileMouseClicked(evt);
             }
         });
 
         inputLabel.setText("Input File:");
 
         outputLabel.setText("Output File:");
 
         logText.setEditable(false);
         logText.setColumns(20);
         logText.setLineWrap(true);
         logText.setRows(5);
         logText.setText("Welcome to Interglot. This program enables package renaming across plugins to quickly enable them to work with later CraftBukkit and Spigot versions. Simply select an input file and an output file Minecraft version, and then transform!\nTHIS SOFTWARE IS PROVIDED AS IS AND COMES WITH NO WARRANTY, EXPLICIT OR IMPLIED.\n\n");
         logText.setWrapStyleWord(true);
         scrollPanel.setViewportView(logText);
 
         transformButton.setText("Transform!");
         transformButton.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 transformButtonMouseClicked(evt);
             }
         });
 
         copyright.setText("Copyright SpigotMC 2013");
 
         versionLabel.setText("Minecraft Version");
 
         minecraftVersion.setText("1.4.6");
 
         libigot.setIcon(new ImageIcon(getClass().getResource("/libigot.png"))); // NOI18N
 
         warning.setEditable(false);
         warning.setColumns(20);
         warning.setFont(new Font("Ubuntu", 0, 11)); // NOI18N
         warning.setLineWrap(true);
         warning.setRows(5);
        warning.setText("This tool is provided to update plugins which might not otherwise work on unmodified\nCraftBukkit builds. Use with caution.\n\nHere is a guy with his foot in his mouth to remind you.");
         warning.setWrapStyleWord(true);
         warning.setFocusable(false);
         jScrollPane1.setViewportView(warning);
 
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(copyright)
                         .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(transformButton))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(Alignment.LEADING)
                             .addComponent(outputLabel)
                             .addComponent(inputLabel, Alignment.TRAILING))
                         .addGap(18, 18, 18)
                         .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                             .addComponent(outputFile, GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                             .addComponent(inputFile)))
                     .addComponent(scrollPanel, GroupLayout.PREFERRED_SIZE, 740, GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                     .addComponent(versionLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(minecraftVersion)
                     .addComponent(libigot, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(inputFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(inputLabel)
                     .addComponent(versionLabel))
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(outputLabel)
                     .addComponent(outputFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(minecraftVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(Alignment.LEADING)
                     .addComponent(scrollPanel, GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(libigot)
                         .addGap(18, 18, 18)
                         .addComponent(jScrollPane1)))
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(copyright)
                     .addComponent(transformButton))
                 .addContainerGap())
         );
 
         pack();
     }//GEN-END:initComponents
 
     private void inputFileMouseClicked(MouseEvent evt) {//GEN-FIRST:event_inputFileMouseClicked
         if (chooser.showOpenDialog(inputFile) == JFileChooser.APPROVE_OPTION) {
             inputFile.setText(chooser.getSelectedFile().getPath());
         }
     }//GEN-LAST:event_inputFileMouseClicked
 
     private void outputFileMouseClicked(MouseEvent evt) {//GEN-FIRST:event_outputFileMouseClicked
         if (chooser.showOpenDialog(outputFile) == JFileChooser.APPROVE_OPTION) {
             outputFile.setText(chooser.getSelectedFile().getPath());
         }
     }//GEN-LAST:event_outputFileMouseClicked
 
     private void transformButtonMouseClicked(MouseEvent evt) {//GEN-FIRST:event_transformButtonMouseClicked
         if (inputFile.getText().isEmpty() || outputFile.getText().isEmpty()) {
             JOptionPane.showMessageDialog(this, "Please ensure both an input file and output file are selected", "No files", JOptionPane.ERROR_MESSAGE);
             return;
         }
         if (!minecraftVersion.getText().matches("[\\d\\\\.]+")) {
             JOptionPane.showMessageDialog(this, "Please specify a valid Minecraft version!", "Invalid Minecraft version", JOptionPane.ERROR_MESSAGE);
             return;
         }
         new SwingWorker() {
             @Override
             protected Object doInBackground() throws Exception {
                 App.process(new File(inputFile.getText()), outputFile.getText(), "v" + minecraftVersion.getText().replace('.', '_'), logger);
                 return null;
             }
         }.execute();
     }//GEN-LAST:event_transformButtonMouseClicked
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JLabel copyright;
     private JTextField inputFile;
     private JLabel inputLabel;
     private JScrollPane jScrollPane1;
     private JLabel libigot;
     private JTextArea logText;
     private JTextField minecraftVersion;
     private JTextField outputFile;
     private JLabel outputLabel;
     private JScrollPane scrollPanel;
     private JButton transformButton;
     private JLabel versionLabel;
     private JTextArea warning;
     // End of variables declaration//GEN-END:variables
 }
