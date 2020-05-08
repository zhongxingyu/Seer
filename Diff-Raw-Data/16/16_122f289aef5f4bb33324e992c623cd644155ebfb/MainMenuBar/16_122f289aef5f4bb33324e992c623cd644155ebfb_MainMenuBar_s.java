 package oleksii.filonov.gui;
 
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ResourceBundle;
 
 public class MainMenuBar extends JMenuBar {
 
     private static ResourceBundle resourceBundle = ResourceBundle.getBundle("localization/bundle");
 
 
     public MainMenuBar() {
        JMenu settingsMenu = new JMenu(resourceBundle.getString("menu.title.settings"));
         add(settingsMenu);
        settingsMenu.
         settingsMenu.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JDialog dialog = new JDialog();
                 dialog.setVisible(true);
             }
         });
     }
 }
