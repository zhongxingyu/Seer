 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.*;
 import java.awt.font.*;
 import java.awt.image.BufferedImage;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.*;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.text.DefaultCaret;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 
 import java.util.concurrent.locks.*;
 import java.util.ArrayList;
 
 public class Renderer {
 
     private JFrame frame;
     private boolean waiting;
     private String[] states; // keeps track of the state of the GUI
     private ReentrantLock lock;
 
     public Renderer() {
 
         frame = new JFrame("M.U.L.E.");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(false);
         frame.setMinimumSize(new Dimension(950, 600));
         frame.setVisible(true);
         waiting = true;
         lock = new ReentrantLock();
     }
 
     public String[] drawIntroScreen() {
         
         // declare initial variables
         String action = "";
         states = new String[1];
         states[0] = "new";
         ImagePanel panel = new ImagePanel("/media/startscreen.png");
         panel.setLayout(null);
         changePanel(frame, panel);
 
         // add buttons
         JButton quitButton = addButtonToPanel(panel, 11, 520, 171, 40, 0, "quit");
         JButton loadButton = addButtonToPanel(panel, 590, 520, 171, 40, 0, "load");
         JButton newButton = addButtonToPanel(panel, 771, 520, 171, 40, 0, "new");
 
         blockForInput();
         exitSafely();
         return states;
     }
 
     public String[] drawSetupScreen() {
 
         // declare initial variables
         String action = "";
         states = new String[3];
         states[0] = "okay";
         states[1] = "medium";
         states[2] = "1";
         ImagePanel panel = new ImagePanel("/media/gamesetup.png");
         panel.setLayout(null);
         changePanel(frame, panel);
 
         // add buttons
         JButton backButton = addButtonToPanel(panel, 11, 520, 170, 40, 0, "back");
         JButton okayButton = addButtonToPanel(panel, 771, 520, 170, 40, 0, "okay");
         JButton easyButton = addButtonToPanel(panel, 160, 163, 173, 28, 1, "easy");
         JButton mediumButton = addButtonToPanel(panel, 407, 163, 173, 28, 1, "medium");
         JButton hardButton = addButtonToPanel(panel, 716, 163, 173, 28, 1, "hard");
         JButton onePlayer = addButtonToPanel(panel, 191, 394, 14, 40, 2, "1");
         JButton twoPlayer = addButtonToPanel(panel, 329, 394, 14, 40, 2, "2");
         JButton threePlayer = addButtonToPanel(panel, 468, 394, 14, 40, 2, "3");
         JButton fourPlayer = addButtonToPanel(panel, 608, 394, 14, 40, 2, "4");
         JButton fivePlayer = addButtonToPanel(panel, 749, 394, 14, 40, 2, "5");
 
         blockForInput();
         exitSafely();
         return states;
     }
 
     public String[] drawMapScreen() {
 
         // declare initial variables
         String action = "";
         states = new String[2];
         states[0] = "okay";
         states[1] = "1";
         ImagePanel panel = new ImagePanel("/media/mapselection.png");
         panel.setLayout(null);
         changePanel(frame, panel);
 
         // add buttons
         JButton backButton = addButtonToPanel(panel, 11, 520, 170, 40, 0, "back");
         JButton okayButton = addButtonToPanel(panel, 770, 520, 170, 40, 0, "okay");
         JButton map1Button = addButtonToPanel(panel, 110, 157, 225, 122, 1, "1");
         JButton map2Button = addButtonToPanel(panel, 365, 157, 225, 122, 1, "2");
         JButton map3Button = addButtonToPanel(panel, 615, 157, 225, 122, 1, "3");
         JButton map4Button = addButtonToPanel(panel, 235, 307, 225, 122, 1, "4");
         JButton map5Button = addButtonToPanel(panel, 488, 307, 225, 122, 1, "5");
 
         blockForInput();
         exitSafely();
         return states;
     }
 
     public String[] drawCharacterScreen() {
 
         // declare initial variables
         String action = "";
         states = new String[4];
         states[0] = "okay";
         states[1] = "human";
         states[2] = "default";
         states[3] = "red";
         ImagePanel panel = new ImagePanel("/media/playerselection.png");
         panel.setLayout(null);
         changePanel(frame, panel);
 
         // add buttons
         JButton backButton = addButtonToPanel(panel, 11, 520, 170, 40, 0, "back");
         JButton okayButton = addButtonToPanel(panel, 771, 520, 170, 40, 0, "okay");
         JButton humanButton = addButtonToPanel(panel, 75, 78, 133, 115, 1, "human");
         JButton elephantButton = addButtonToPanel(panel, 232, 78, 133, 115, 1, "elephant");
         JButton squirrelButton = addButtonToPanel(panel, 413, 78, 133, 115, 1, "squirrel");
         JButton frogButton = addButtonToPanel(panel, 593, 78, 133, 115, 1, "frog");
         JButton catButton = addButtonToPanel(panel, 763, 78, 133, 115, 1, "cat");
         JButton redButton = addButtonToPanel(panel, 70, 240, 130, 200, 3, "red");
         JButton blueButton = addButtonToPanel(panel, 230, 240, 130, 200, 3, "blue");
         JButton pinkButton = addButtonToPanel(panel, 390, 240, 130, 200, 3, "pink");
         JButton greenButton = addButtonToPanel(panel, 550, 240, 130, 200, 3, "green");
         JButton orangeButton = addButtonToPanel(panel, 710, 240, 130, 200, 3, "orange");
 
         JTextField nameBox = addTextToPanel(panel, 470, 522, 225, 38);
 
         blockForInputCharacter(panel);
         exitSafely();
         states[2] = nameBox.getText();
         return states;
     }
 
     public void drawLoadScreen(LoadScreenModel model) {
         return;
     }
 
 
     // helper methods
 
     private void changePanel(JFrame frame, JPanel panel) {
         //frame.removeAll();
         frame.setContentPane(panel);
         frame.pack();
         frame.repaint();
     }
 
     private void blockForInput() {
         // wait for a button to be clicked
         boolean waitingSafe = true; // used to avoid race condition
         while (waitingSafe) {
             try {
                 lock.lock();
                 waitingSafe = waiting;
             }
             finally {
                 lock.unlock();
             }
         }
     }
 
     private void blockForInputCharacter(JPanel panel) {
         // wait for a button to be clicked
         JLabel colors = addLabelToPanel(panel, 70, 240, 804, 200, "/media/" + states[1] + ".png");
        panel.repaint();
         String oldState = states[1];
         boolean waitingSafe = true; // used to avoid race condition
         while (waitingSafe) {
             if (!oldState.equals(states[1])) {
                 panel.remove(colors);
                 colors = addLabelToPanel(panel, 70, 240, 804, 200, "/media/" + states[1] + ".png");
                 panel.repaint();
                 oldState = states[1];
             }
 
             try {
                 lock.lock();
                 waitingSafe = waiting;
             }
             finally {
                 lock.unlock();
             }
         }
     }
 
     private void exitSafely() {
         try {
             lock.lock();
             waiting = true;
         }
         finally {
             lock.unlock();
         }
     }
 
 
     private JButton addButtonToPanel(JPanel panel, int x, int y, int width, int height, 
         final int stateNum, final String stateText) {
         JButton button = new JButton();
         button.setBounds(x, y, width, height);
         panel.add(button);
 
         button.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 states[stateNum] = stateText; // set the new state
 
                 // safely set the value of waiting
                 if (stateNum == 0) {
                     try {
                         lock.lock();
                         waiting = false;
                     }
                     finally {
                         lock.unlock();
                     }
                 }
             }
         });
 
         button.setOpaque(false);
         button.setContentAreaFilled(false);
         button.setBorderPainted(false);
         return button;
     }
 
     private JTextField addTextToPanel(JPanel panel, int x, int y, int width, int height) {
         JTextField text = new JTextField("default");
         text.setBounds(x, y, width, height);
         DefaultCaret c = (DefaultCaret)text.getCaret();
         c.setVisible(true);
         c.setDot(0);
         text.setFont(new Font("Candara", Font.PLAIN, 30));
         text.setHorizontalAlignment(JTextField.LEFT);
         text.setForeground(Color.WHITE);
         text.setBackground(new Color(87, 51, 4));
         text.setOpaque(false);
         text.setCaretColor(Color.WHITE);
         text.setBorder(javax.swing.BorderFactory.createEmptyBorder());
         panel.add(text);
         return text;
     }
 
     private JLabel addLabelToPanel(JPanel panel, int x, int y, int width, int height, String image) {
         BufferedImage img;
         try {
             img = ImageIO.read(getClass().getResourceAsStream(image));
         }
         catch (Exception e) {
             System.out.println("Caught: " + e);
             return null;
         }
 
         ImageIcon icon = new ImageIcon(img);
         JLabel label = new JLabel();
         label.setIcon(icon);
         label.setBounds(x, y, width, height);
         panel.add(label);
         return label;
     }
 }
