 package net.sozinsoft.tokenlab.ui;
 
 import net.sozinsoft.tokenlab.*;
 import net.sozinsoft.tokenlab.Character;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.prefs.Preferences;
 
 import static net.sozinsoft.tokenlab.ui.IconCreator.createImageIcon;
 
 public class ConfigureCharacterDialog extends JDialog {
     public static final String IMAGE_DIR = "IMAGE_DIR";
     public static final String TOKEN_DIR = "TOKEN_DIR";
     private JPanel contentPane;
     private JButton buttonOK;
     private JButton buttonCancel;
     private JButton setPortraitImageButton;
     private JButton setPogImageButton;
     private JButton setTokenLocationButton;
     private Config.ConfigEntry  configEntry;
     private final Preferences prefs;
     private JFileChooser imageChooser;
     private JFileChooser tokenOutputChooser;
     private JList targetList;
 
 
     private void setImageFileChooserFilters(JFileChooser chooser) {
         chooser.setFileFilter(new FileFilter() {
             @Override
             public boolean accept(File f) {
                 if (f.isDirectory()) {
                     return true;
                 } else {
                     String name = f.getName();
                     return name.endsWith("tiff") || name.endsWith("tif") || name.endsWith("gif") ||
                             name.endsWith("bmp") || name.endsWith("jpg") || name.endsWith("jpeg") ||
                             name.endsWith("png");
                 }
             }
 
             @Override
             public String getDescription() {
                 return "Image files";
             }
         });
     }
 
     private void setTokenFileChooserFilters(JFileChooser chooser ) {
        chooser.setFileFilter(new FileFilter() {
             @Override
             public boolean accept(File f) {
                 if (f.isDirectory()) {
                     return true;
                 } else {
                     String name = f.getName();
                     return name.endsWith("tok") || name.endsWith("pog");
                 }
             }
 
             @Override
             public String getDescription() {
                 return "Token files";
             }
         });
     }
 
     private void checkToEnableOkButton() {
         if ( setPogImageButton.isEnabled() == false &&
              setPortraitImageButton.isEnabled() == false &&
              setTokenLocationButton.isEnabled() == false ) {
             buttonOK.setEnabled(true);
         }
     }
 
 
     public ConfigureCharacterDialog( JList targetList, Config.ConfigEntry config, final Preferences prefs ) {
         this.configEntry = config;
         this.targetList = targetList;
         this.prefs  = prefs;
         imageChooser       = new JFileChooser( this.prefs.get(IMAGE_DIR, "" ) );
         tokenOutputChooser = new JFileChooser( this.prefs.get(TOKEN_DIR, "" ) );
         setImageFileChooserFilters(imageChooser);
         setTokenFileChooserFilters(tokenOutputChooser);
         setContentPane(contentPane);
         setModal(true);
         getRootPane().setDefaultButton(buttonOK);
         buttonOK.setEnabled(false);
 
         buttonOK.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onOK();
             }
         });
 
         buttonCancel.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() when cross is clicked
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() on ESCAPE
         contentPane.registerKeyboardAction(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         setPortraitImageButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                  int returnVal = imageChooser.showOpenDialog(null);
                  if (returnVal == JFileChooser.APPROVE_OPTION) {
                      File imageFile = imageChooser.getSelectedFile();
                     prefs.put(IMAGE_DIR, imageFile.getAbsolutePath());
                      configEntry.setPortraitFilePath(imageFile.getAbsolutePath());
                      setPortraitImageButton.setEnabled(false);
                      checkToEnableOkButton();
                  }
             }
         });
         setPogImageButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int returnVal = imageChooser.showOpenDialog(null);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                      File imageFile = imageChooser.getSelectedFile();
                     prefs.put(IMAGE_DIR, imageFile.getAbsolutePath());
                      configEntry.setImageFilePath(imageFile.getAbsolutePath());
                      setPogImageButton.setEnabled(false);
                      checkToEnableOkButton();
                  }
             }
         });
         setTokenLocationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                 int returnVal = tokenOutputChooser.showOpenDialog(null);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                      File tokenFile = tokenOutputChooser.getSelectedFile();
                     prefs.put(TOKEN_DIR, tokenFile.getAbsolutePath());
                      configEntry.setOutputTokenTo(tokenFile.getAbsolutePath());
                      setTokenLocationButton.setEnabled(false);
                      checkToEnableOkButton();
                  }
             }
         });
     }
 
     private void onOK() {
         targetList.repaint();
         dispose();
     }
 
     private void onCancel() {
 // add your code here if necessary
         dispose();
     }
 
     public static void main(String[] args) {
         ConfigureCharacterDialog dialog = new ConfigureCharacterDialog( null, null, null);
         dialog.pack();
         dialog.setVisible(true);
         System.exit(0);
     }
 }
