 package screens;
 
 import controllers.HomeController;
 import screens.controls.MainFrame;
 import screens.controls.ImagePanel;
 import views.HomeView;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Job:- Understands to display the start window of the application.
  */
 
 public class HomeScreen implements HomeView {
     private static final String BG_IMAGE = "images/homepage.jpg";
     MainFrame mainFrame;
     private final HomeController controller;
     JButton startServerButton;
     JButton joinServerButton;
     JButton quitButton;
 
     public HomeScreen(MainFrame mainFrame, HomeController controller) {
         this.mainFrame = mainFrame;
         this.controller = controller;
         ImagePanel panel = mainFrame.createImagePanel(BG_IMAGE);
 
         startServerButton = createButton(100, 300, "StartServer");
         joinServerButton = createButton(100, 400, "JoinServer");
         quitButton = createButton(100, 500, "Quit");
 
         panel.add(startServerButton);
         panel.add(joinServerButton);
         panel.add(quitButton);
         addButtonHandlers();
     }
 
     private void addButtonHandlers() {
         startServerButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 controller.startServer();
             }
         });
 
         joinServerButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 controller.joinServer();
             }
         });
         quitButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int selectedOption = JOptionPane.showConfirmDialog(null, "Do you really want to quit?", "", JOptionPane.YES_NO_OPTION);
                 if (selectedOption == JOptionPane.YES_OPTION) {
                     System.exit(0);
                 }
             }
         });
     }
 
     @Override
     public void display() {
     }
 
     private JButton createButton(int x_axis, int y_axis, String buttonName) {
         JButton button = new JButton(buttonName);
         button.setSize(145, 50);
         button.setLocation(x_axis, y_axis);
         button.setFont(new Font("Verdana", Font.BOLD, 14));
         button.setForeground(Color.ORANGE);
         button.setBackground(Color.BLACK);
         return button;
     }
 
 }
