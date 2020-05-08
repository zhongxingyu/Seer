 package view;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ResourceBundle;
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JSeparator;
 import slogo.Controller;
 
 
 public class ToolBarArea extends JMenuBar {
 
     private static final String DEFAULT_RESOURCE_PACKAGE = "resources.";
     private static final String USER_DIR = "user.dir";
     private static final Color BACKGROUND_COLOR = Color.GREEN;
     private static final long serialVersionUID = 1L;
     private Controller myController;
     private JFileChooser myChooser;
     private ResourceBundle myResources;
 
     ToolBarArea (Controller control) {
         myController = control;
         myChooser = new JFileChooser(System.getProperties().getProperty(USER_DIR));
         myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE + "English");
 
         setBackground(BACKGROUND_COLOR);
         this.add(makeFileMenu());
     }
 
     private JMenu makeFileMenu () {
         JMenu result = new JMenu(myResources.getString("FileMenu"));
         result.add(new AbstractAction(myResources.getString("OpenCommand")) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed (ActionEvent e) {
                 try {
                     int response = myChooser.showOpenDialog(null);
                     if (response == JFileChooser.APPROVE_OPTION) {
                         echo(new FileReader(myChooser.getSelectedFile()));
                     }
                 }
                 catch (IOException io) {
                     showError(io.toString());
                 }
             }
         });
         result.add(new AbstractAction(myResources.getString("SaveCommand")) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed (ActionEvent e) {
                 try {
                     echo(new FileWriter("demo.out"));
                 }
                 catch (IOException io) {
                     showError(io.toString());
                 }
             }
         });
         result.add(new JSeparator());
         result.add(new AbstractAction(myResources.getString("QuitCommand")) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed (ActionEvent e) {
                 System.exit(0);
             }
         });
         return result;
     }
 
     private void echo (FileWriter fileWriter) {
     }
 
     private void showError (String message) {
         JOptionPane.showMessageDialog(this, message, myResources.getString("ErrorTitle"),
                                       JOptionPane.ERROR_MESSAGE);
     }
 
     private void echo (FileReader fileReader) {
         try {
            String userInput = "";
             BufferedReader input = new BufferedReader(fileReader);
             String line = input.readLine();
            while (line != null) {
                userInput += line + "\n";
                 line = input.readLine();
             }
            myController.processUserInput(0, userInput);
         }
         catch (IOException e) {
             showError(e.toString());
         }
     }
 
 }
