 package tazam;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.*;
 
 /**
  * The first frame opened when user loads the application. Allows for opening
  * multiple audio files - each time one is opened, a new frame is created.
  *
  */
 public class StartFrame extends JFrame {
 
     /**
      * Name of the current audio clip selected
      */
     private static String name;
     /**
      * Default width
      */
     private static final int width = 700;
     /**
      * Default height
      */
     private static final int height = 500;
     /**
      * Menu item for opening an audio file
      */
     private JMenuItem open;
     /**
      * Menu item for indexing an audio file
      */
     private JMenuItem index;
     /**
      * Menu item for indexing an entire folder
      */
     private JMenuItem indexFolder;
     /**
      * Displays the index in a JFrame
      */
     private JMenuItem displayIndex;
     /**
      * Menu item to exit the main application
      */
     private JMenuItem exit;
     /**
      * The underlying track index of the files user has indexed.
      */
     private TrackIndex trackIndex;
     /**
      * Self-reference
      */
     private StartFrame startFrame;
     /**
      * A button to index a selected track
      */
     private JButton indexTrackButton = new JButton("Indexar pista");
     /**
      * A button to index an entire folder
      */
     private JButton indexFolderButton = new JButton("Indexar carpeta");
    
     private JButton btMicro = new JButton("Gravar");
     /**
      * The table in the frame that displays the contents of the index
      */
 //    private JTable table;
     /**
      * A text area to put results
      */
     private static JTextArea textArea = new JTextArea();
    
     private static JScrollPane scrollPane = new JScrollPane(textArea);
 
     /**
      * Creates the first frame the user sees at startup.
      */
     public StartFrame() {
         super("Tazam - UIB 2012");
         startFrame = this;
         setLayout(new BorderLayout());
         textArea.setText("");
         textArea.setEditable(false);
         textArea.setMargin(new Insets(2, 2, 2, 2));
 
         JPanel indexPanel = new JPanel();
         add(indexPanel, BorderLayout.NORTH);
         indexPanel.add(indexTrackButton, BorderLayout.WEST);
         indexTrackButton.addActionListener(new IndexTrackListener());
         indexPanel.add(indexFolderButton, BorderLayout.EAST);
         indexFolderButton.addActionListener(new IndexFolderListener());
         indexPanel.add(btMicro, BorderLayout.EAST);
         btMicro.addActionListener(new MicroListener());
         add(scrollPane, BorderLayout.CENTER);
 
         setPreferredSize(new Dimension(width, height));
         setMenu();
         setResizable(false);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocation(300, 100);
         initLookAndFeel();
         SwingUtilities.updateComponentTreeUI(this);
         pack();
         setVisible(true);
     }
 
     private void initLookAndFeel() {
         String lookAndFeel;
         String osname = System.getProperty("os.name").toLowerCase();
 
         if (osname.equals("linux")) {
             lookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
         } else if (osname.startsWith("windows")) {
             lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
         } else if (osname.startsWith("mac")) {
             lookAndFeel = UIManager.getSystemLookAndFeelClassName();
 //            lookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
         } else {
             lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
         }
 
         try {
             UIManager.setLookAndFeel(lookAndFeel);
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
             JOptionPane.showMessageDialog(null, "Error: " + e);
         }
     }
 
     /**
      * Sets up the main window's menu.
      */
     private void setMenu() {
         JMenuBar menuBar = new JMenuBar();
         setJMenuBar(menuBar);
         JMenu file = new JMenu("Arxiu");
         menuBar.add(file);
 
         //OPEN A CLIP/TRACK
         open = new JMenuItem("Obrir Arxiu/Pista");
         file.add(open);
         open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
         open.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         try {
                             AudioClip clip = StartFrame.loadClip();
                             Signal signal = new Signal(clip, name);
                             ClipFrame clipFrame = new ClipFrame(signal, startFrame);
                         } catch (Exception x) {
                             //do nothing
                         }
                     }
                 });
 
         //INDEX a track.
         index = new JMenuItem("Indexar Pista");
         file.add(index);
         index.addActionListener(new IndexTrackListener());
 
         //INDEX an entire folder
         indexFolder = new JMenuItem("Indexar Carpeta");
         file.add(indexFolder);
         indexFolder.addActionListener(new IndexFolderListener());
 
         //DISPLAY THE INDEX
         displayIndex = new JMenuItem("Mostrar Índex");
         file.add(displayIndex);
         displayIndex.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         displayIndex();
                     }
                 });
 
         exit = new JMenuItem("Sortir");
         file.add(exit);
         exit.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         System.exit(0);
                     }
                 });
 
     }
 
     /**
      * Open an audio file and create a window to display it.
      */
     public static AudioClip loadClip() throws NullPointerException {
         JFileChooser d = new JFileChooser();
         d.setDialogTitle("Selecciona arxiu audio");
         d.setDialogType(JFileChooser.OPEN_DIALOG);
         d.setFileFilter(new javax.swing.filechooser.FileFilter() {
 
             private AudioFileFormat.Type[] types =
                     AudioSystem.getAudioFileTypes();
 
             @Override
             public boolean accept(File f) {
                 String name = f.getName();
                 return (f.isDirectory() || supportedType(name));
             }
 
             private boolean supportedType(String name) {
                 for (int i = 0; i < types.length; i++) {
                     if (name.endsWith(types[i].getExtension())) {
                         return true;
                     }
                 }
                 return false;
             }
 
             @Override
             public String getDescription() {
                 return "fitxers d'audio suportats (wav)";
             }
         });
         int ret = d.showOpenDialog(null);
         if (ret == JFileChooser.APPROVE_OPTION) {
             try {
                 File f = d.getSelectedFile();
                 name = f.getName();//get the name of the selection
                 AudioClip clip; // CANVIAT
                 try (AudioInputStream ain = AudioSystem.getAudioInputStream(f)) {
                     clip = AudioClip.fromStream(ain, f.getName());
                 }
                 return clip;
             } catch (IOException x) {
                 JOptionPane.showMessageDialog(null, "IOException: " + x.getMessage());
             } catch (UnsupportedAudioFileException x) {
                 JOptionPane.showMessageDialog(null, "Format de fitxer no suportat.");
             } catch (Throwable x) {
                 JOptionPane.showMessageDialog(null, "Error: " + x);
             }
         }
         return null;
     }
 
     /**
      * Displays the contents of the index, if it is not null.
      */
     public void displayIndex() {
         JDialog indexFrame = new JDialog(this);
         indexFrame.setTitle("Contingut índex");
         indexFrame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
         indexFrame.setLocationRelativeTo(this);
         JTextArea _textArea = new JTextArea();
         _textArea.setEditable(false);
         JScrollPane _scrollPane = new JScrollPane(_textArea);
         indexFrame.getContentPane().add(_scrollPane);
         if (trackIndex == null) {
             JOptionPane.showMessageDialog(this, "L'índex es buit");
         } else {
             Iterator<TrackID> it = trackIndex.getTrackIDIterator();
             while (it.hasNext()) {
                 TrackID id = it.next();
                 TrackInfo info = trackIndex.getTrackInfo(id);
                 String s = ("TrackID: " + id.getIntID() + " " + info.toString() + "\n");
                 _textArea.append(s);
             }
             indexFrame.pack();
             indexFrame.setVisible(true);
         }
 
 
     }
 
     /**
      * Gets the underlying track index
      *
      * @return The track index.
      */
     public TrackIndex getTrackIndex() {
         return trackIndex;
     }
 
     /**
      * Starts the main application.
      */
     public static void main(String[] args) {
 
         StartFrame f = new StartFrame();
         if (f.getTrackIndex() != null) {
         }
     }
 
     public static JTextArea getArea() {
         return textArea;
     }
 
     public static JScrollPane getScroll() {
         return scrollPane;
     }
 
     /**
      * Listener inside the class to load a specific index.
      *
      */
     private class IndexTrackListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             JFileChooser f = new JFileChooser();
             f.setDialogTitle("Selecciona un arxiu per indexar");
             f.setDialogType(JFileChooser.OPEN_DIALOG);
             f.setFileFilter(new javax.swing.filechooser.FileFilter() {
 
                 private AudioFileFormat.Type[] types =
                         AudioSystem.getAudioFileTypes();
 
                 @Override
                 public boolean accept(File f) {
                     String name = f.getName();
                     return (f.isDirectory() || supportedType(name));
                 }
 
                 private boolean supportedType(String name) {
                     for (int i = 0; i < types.length; i++) {
                         if (name.endsWith(types[i].getExtension())) {
                             return true;
                         }
                     }
                     return false;
                 }
 
                 @Override
                 public String getDescription() {
                     return "fitxers d'audio suportats (wav)";
                 }
             });
             int ret = f.showOpenDialog(null);
             if (ret == JFileChooser.APPROVE_OPTION) {
                 try {
                     File file = f.getSelectedFile();
                     if (trackIndex == null) {
                         trackIndex = new TrackIndex(file);
                     } else {
                         trackIndex.addTrack(file);
                     }
                 } catch (Exception x) {
                     JOptionPane.showMessageDialog(null, "Error durant l'indexat.");
                 }
             }
         }
     }
 
     private class IndexFolderListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             JFileChooser f = new JFileChooser();
             f.setDialogTitle("Selecciona una carpeta per indexar");
             f.setDialogType(JFileChooser.OPEN_DIALOG);
             f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             int ret = f.showOpenDialog(null);
             if (ret == JFileChooser.APPROVE_OPTION) {
                 textArea.append("\nIndexant (pot tardar un momentet)...");
                 textArea.repaint();
                 try {
                     File fileDir = f.getSelectedFile();
                     if (trackIndex == null) {
                         trackIndex = new TrackIndex(fileDir);
                     } else {
                         trackIndex.addFolder(fileDir);
                         //JOptionPane.showMessageDialog(startFrame, "The folder -" + 
                         //		fileDir.getName() + "-\nwas successfully indexed.");
                     }
                 } catch (Exception x) {
                     JOptionPane.showMessageDialog(null, "Error durant l'indexat");
                 }
             }
         }
     }
     
         
     private class MicroListener implements ActionListener {
 
 //        public void JDialogAviso() {
 //            JDialog aviso = new JDialog();
 //            aviso.setTitle("Contingut índex");
 //            aviso.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 //            aviso.setLocationRelativeTo(null);
 //            JTextArea textArea = new JTextArea();
 //            textArea.setEditable(false);
 //            JScrollPane scrollPane = new JScrollPane(textArea);
 //            aviso.getContentPane().add(scrollPane);
 //
 //            aviso.pack();
 //            aviso.setVisible(true);
 //        }
 
         @Override
         public void actionPerformed(ActionEvent e) { 
             Microphone mic = new Microphone();
             switch (btMicro.getText()) {
                 case "Gravar":
                     btMicro.setText("Aturar");
                     try {
                         if (!mic.saveFileRecorded()) {
                             btMicro.setText("Gravar");        
                         }
                     } catch (FileNotFoundException ex) {
                         Logger.getLogger(StartFrame.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (IOException ex) {
                         Logger.getLogger(StartFrame.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     break;
                 case "Aturar":
                     btMicro.setText("Gravar");
                     mic.stopRunning();
                     break;
             }
         }
     }
 }
