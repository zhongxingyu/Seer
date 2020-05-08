 import javax.swing.*;
 import java.awt.*;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.Vector;
 
 
 public class Lgs extends TransferHandler implements ActionListener {
    private static final String versionString = "buildnumber";
 
     //TODO: make this configurable
     String[] ext = {"jpg", "xmp"};
 
     public static void main(String[] args) {
         // Schedule a job for the event-dispatching thread:
         // creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
                     Lgs lgsGui = new Lgs();
                     lgsGui.createAndShowGUI();
                 } catch (RuntimeException e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     private JFrame frame;
     private JTextField masterDirectory;
     private JComboBox masterAlbum;
     private JRadioButton dirRadioButton;
     private JTextField targetDirectory;
     private JTextField slaveDirectory;
     private JTextArea outputArea;
 
     private void createAndShowGUI() {
         String setLafResult = this.setLAF();
         this.outputArea = new JTextArea();
         this.outputArea.append(setLafResult);
 
         this.frame = new JFrame("lgs v"+versionString);
         this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.frame.setPreferredSize(new Dimension(600, 500));
         Container contentPane = this.frame.getContentPane();
         GridBagLayout gbl = new GridBagLayout();
         contentPane.setLayout(gbl);
         double lWeight = 0.1;
         GridBagConstraints gc = new GridBagConstraints();
         gc.fill = GridBagConstraints.BOTH;
         gc.anchor = GridBagConstraints.WEST;
         gc.insets = new Insets(1, 3, 1, 3);
         gc.gridx = 0;
         gc.gridy = 0;
         gc.weightx = lWeight;
         contentPane.add(new JLabel("master"), gc);
         gc.gridx++;
         this.dirRadioButton = new JRadioButton("verzeichnis", true);
         this.dirRadioButton.setActionCommand("dir");
         this.dirRadioButton.addActionListener(this);
         contentPane.add(this.dirRadioButton, gc);
         gc.gridx++;
         gc.gridwidth = 5;
         gc.weightx = 1;
         this.masterDirectory = new JTextField();
         this.masterDirectory.setName("master");
         this.masterDirectory.setTransferHandler(this);
         contentPane.add(this.masterDirectory, gc);
         gc.gridwidth = 1;
         gc.gridy++;
         gc.gridx = 1;
         gc.weightx = lWeight;
         JRadioButton dbRadioButton = new JRadioButton("db-album");
         dbRadioButton.setActionCommand("db");
         dbRadioButton.addActionListener(this);
         contentPane.add(dbRadioButton, gc);
         gc.gridx++;
         gc.weightx = 1;
         gc.gridwidth = 5;
         this.masterAlbum = new JComboBox(this.getAlbums());
         this.masterAlbum.setActionCommand("dbalbums");
         this.masterAlbum.addActionListener(this);
         this.masterAlbum.setEnabled(false);
         contentPane.add(this.masterAlbum, gc);
 
         ButtonGroup bgroup = new ButtonGroup();
         bgroup.add(this.dirRadioButton);
         bgroup.add(dbRadioButton);
 
         gc.gridx = 0;
         gc.gridy++;
         gc.gridwidth = 1;
         gc.weightx = lWeight;
         contentPane.add(new JLabel("slave"), gc);
         gc.gridx++;
         gc.weightx = 1;
         gc.gridwidth = 6;
         this.slaveDirectory = new JTextField();
         this.slaveDirectory.setName("slave");
         this.slaveDirectory.setTransferHandler(this);
         contentPane.add(this.slaveDirectory, gc);
 
         gc.gridy++;
         gc.gridx = 0;
         gc.gridwidth = 1;
         gc.weightx = lWeight;
         contentPane.add(new JLabel("ziel"), gc);
         gc.gridx++;
         gc.weightx = 1;
         gc.gridwidth = 6;
         this.targetDirectory = new JTextField();
         this.targetDirectory.setName("ziel");
         this.targetDirectory.setTransferHandler(this);
         contentPane.add(this.targetDirectory, gc);
 
         gc.gridy++;
         gc.gridx = 3;
         gc.gridwidth = 2;
         gc.weightx = lWeight;
         JButton startButton = new JButton("start");
         startButton.setActionCommand("start");
         startButton.addActionListener(this);
         contentPane.add(startButton, gc);
 
         gc.gridx += 2;
         JButton endButton = new JButton("ende");
         endButton.setActionCommand("end");
         endButton.addActionListener(this);
         contentPane.add(endButton, gc);
 
         gc.gridy++;
         gc.gridx = 0;
         gc.weightx = 1;
         gc.weighty = 1;
         gc.gridwidth = 7;
         gc.gridheight = 8;
         JScrollPane jsp = new JScrollPane(this.outputArea);
         contentPane.add(jsp, gc);
 
         this.frame.pack();
         this.frame.setVisible(true);
     }
 
     private String setLAF() {
         String result = "";
         Toolkit.getDefaultToolkit().setDynamicLayout(true);
         System.setProperty("sun.awt.noerasebackground", "true");
         JFrame.setDefaultLookAndFeelDecorated(true);
         JDialog.setDefaultLookAndFeelDecorated(true);
         try {
             UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
             result = "black theme geladen\n";
         } catch (ClassNotFoundException e) {
             result = "black theme nicht gefunden\n";
             e.printStackTrace();
         } catch (InstantiationException e) {
             e.printStackTrace();
             this.outputArea.append("black theme nicht gefunden\n");
         } catch (IllegalAccessException e) {
             e.printStackTrace();
             this.outputArea.append("black theme nicht gefunden\n");
         } catch (UnsupportedLookAndFeelException e) {
             e.printStackTrace();
             this.outputArea.append("black theme nicht gefunden\n");
         }
         return result;
     }
 
     private Vector<String> getAlbums() {
         Vector<String> ret = new Vector<String>();
         ret.add("");
         String err;
         try {
             // get last version info from internet
             URL updateURL = new URL("http://www.lichtographie.de/lgsFuncs.php");
             BufferedReader in = new BufferedReader(new InputStreamReader(updateURL.openStream()));
             String inputLine;
             while ((inputLine = in.readLine()) != null) {
                 ret.add(inputLine);
             }
             this.outputArea.append(ret.size() + " alben gefunden\n");
             // close stream
             in.close();
         } catch (FileNotFoundException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // e.printStackTrace();
         } catch (UnknownHostException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // e.printStackTrace();
         } catch (ConnectException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // e.printStackTrace();
         } catch (MalformedURLException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // MyLog.exceptionError(e);
             e.printStackTrace();
         } catch (IOException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // MyLog.exceptionError(e);
             e.printStackTrace();
         }
         return ret;
     }
 
     private Vector<String> getAlbumInfo(String albumId) {
         Vector<String> ret = new Vector<String>();
         String err;
         try {
             // get last version info from internet
             URL updateURL = new URL("http://www.lichtographie.de/lgsFuncs.php?album=" + albumId);
             BufferedReader in = new BufferedReader(new InputStreamReader(updateURL.openStream()));
             String inputLine;
             while ((inputLine = in.readLine()) != null) {
                 ret.add(inputLine);
             }
             // close stream
             in.close();
         } catch (FileNotFoundException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // e.printStackTrace();
         } catch (UnknownHostException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // e.printStackTrace();
         } catch (ConnectException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // e.printStackTrace();
         } catch (MalformedURLException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // MyLog.exceptionError(e);
             e.printStackTrace();
         } catch (IOException e) {
             err = "Kein Zugang zum Internet gefunden.";
             // MyLog.exceptionError(e);
             e.printStackTrace();
         }
         return ret;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getActionCommand().equals("start")) {
             this.startLGS();
         } else if (e.getActionCommand().equals("end")) {
             this.frame.dispose();
         } else if (e.getActionCommand().equals("db")) {
             this.masterAlbum.setEnabled(true);
             this.masterDirectory.setEnabled(false);
         } else if (e.getActionCommand().equals("dir")) {
             this.masterAlbum.setEnabled(false);
             this.masterDirectory.setEnabled(true);
         } else if (e.getActionCommand().equals("dbalbums")) {
             String album = (String) this.masterAlbum.getSelectedItem();
             String albumName = album.substring(album.indexOf(";") + 1).trim();
             String albumID = album.substring(0, album.indexOf(";")).trim();
             Vector<String> pics = getAlbumInfo(albumID);
             this.outputArea.append(+pics.size() + " bilder in album " + albumName + " gefunden\n");
         }
     }
 
     @Override
     public boolean importData(JComponent comp, Transferable t) {
         // Make sure we have the right starting points
         if (!(comp instanceof JTextField)) {
             return false;
         }
         if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
             return false;
         }
 
         // Grab the tree, its model and the root node
         JTextField textField = (JTextField) comp;
         try {
             List data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
             for (Object object : data) {
                 File f = (File) object;
                 textField.setText(f.getAbsolutePath());
                 Vector<FileInfo> fis = FileSyncer.GetFileInfoItems(f.getAbsolutePath(), this.ext);
                 this.outputArea.append(fis.size() + " dateien in " + textField.getName() + " gefunden\n");
             }
             return true;
         } catch (UnsupportedFlavorException ufe) {
             System.err.println("Ack! we should not be here.\nBad Flavor.");
         } catch (IOException ioe) {
             System.out.println("Something failed during import:\n" + ioe);
         }
         return false;
     }
 
     @Override
     public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
         if (comp instanceof JTextField) {
             for (DataFlavor transferFlavor : transferFlavors) {
                 if (transferFlavor.equals(DataFlavor.javaFileListFlavor)) {
                     return true;
                 }
             }
             return false;
         }
         return false;
     }
 
     private void startLGS() {
         if (this.dirRadioButton.isSelected()) {
             // use supplied directory
             String masterDir = this.masterDirectory.getText();
             String slaveDir = this.slaveDirectory.getText();
             String targetDir = this.targetDirectory.getText();
             FileSyncer.SyncItemsFS(FileSyncer.GetFileInfoItems(masterDir, this.ext), slaveDir, targetDir, this.outputArea);
         } else {
             // use album from db
             String slaveDir = this.slaveDirectory.getText();
             String targetDir = this.targetDirectory.getText();
             String albumInfo = (String) this.masterAlbum.getSelectedItem();
             String[] parts = albumInfo.split(";");
             String albumId = parts[0].trim();
             Vector<String> foddos = this.getAlbumInfo(albumId);
             FileSyncer.SyncItemsDB(foddos, slaveDir, targetDir, this.outputArea);
         }
 
     }
 }
