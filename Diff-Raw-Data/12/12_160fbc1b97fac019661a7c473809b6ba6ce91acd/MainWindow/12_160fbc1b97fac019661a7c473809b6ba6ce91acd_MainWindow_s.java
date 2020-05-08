 package com.nigorojr.typebest;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.HashMap;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 /**
  * This class adds the necessary components and creates the window. All the
  * other stuff such as reading/writing data etc. are left to MainTypePanel
  * class.
  * 
  * @author Naoki Mizuno
  */
 
 @SuppressWarnings("serial")
 public class MainWindow extends JFrame {
     private TypePanel typePanel = new TypePanel();
     private ClickResponder clickResponder = new ClickResponder();
     private JButton restartButton = new JButton("Restart");
     private TimerPanel timeElapsed;
     private HashMap<String, JMenuItem> menuItem = new HashMap<String, JMenuItem>();
 
     private boolean restartFlag = false;
 
     /**
      * Creates a new window with a panel that you type in, and a menu bar.
      */
     public MainWindow() {
         super();
 
         setTitle("TypeBest");
 
         SpringLayout springLayout = new SpringLayout();
 
         JPanel mainPanel = new JPanel();
         mainPanel.setLayout(springLayout);
 
         // Add items to menu
         menuItem.put("ch_user", new JMenuItem("Change User"));
         menuItem.put("ch_mode", new JMenuItem("Change Practice Mode"));
         menuItem.put("ch_font", new JMenuItem("Change Font"));
         menuItem.put("ch_layout", new JMenuItem("Change Keyboard Layout"));
         menuItem.put("ch_color", new JMenuItem("Change Color"));
         menuItem.put("ch_noShuffle", new JCheckBoxMenuItem(
                 "Don't Shuffle Words"));
         menuItem.put("ch_fun", new JCheckBoxMenuItem("Fun"));
         // NOTE: "Save Settings" will be added separately
 
         // Add the menu bar
         menuBar();
 
         // Box that shows how much time has elapsed
         timeElapsed = new TimerPanel();
         springLayout.putConstraint(SpringLayout.SOUTH, timeElapsed, -5,
                 SpringLayout.NORTH, typePanel);
         springLayout.putConstraint(SpringLayout.EAST, timeElapsed, -15,
                 SpringLayout.EAST, typePanel);
 
         // JLabel that shows the current keyboard layout
         JLabel currentKeyboardLayout = typePanel.getKeyboardLayoutLabel();
         springLayout.putConstraint(SpringLayout.SOUTH, currentKeyboardLayout,
                 -5, SpringLayout.NORTH, typePanel);
         springLayout.putConstraint(SpringLayout.WEST, currentKeyboardLayout,
                 15, SpringLayout.WEST, typePanel);
 
         // Window to type in
         springLayout.putConstraint(SpringLayout.NORTH, typePanel, 50,
                 SpringLayout.NORTH, mainPanel);
         springLayout.putConstraint(SpringLayout.SOUTH, typePanel, -40,
                 SpringLayout.SOUTH, mainPanel);
         springLayout.putConstraint(SpringLayout.WEST, typePanel, 5,
                 SpringLayout.WEST, mainPanel);
         springLayout.putConstraint(SpringLayout.EAST, typePanel, -5,
                 SpringLayout.EAST, mainPanel);
 
         addKeyListener(new TypingResponder());
 
         springLayout.putConstraint(SpringLayout.EAST, restartButton, -8,
                 SpringLayout.EAST, mainPanel);
         springLayout.putConstraint(SpringLayout.SOUTH, restartButton, -8,
                 SpringLayout.SOUTH, mainPanel);
 
         restartButton.addActionListener(clickResponder);
         // When this is true, all the typing gets redirected to the button
         restartButton.setFocusable(false);
 
         // Add things to the main panel
         mainPanel.add(currentKeyboardLayout);
         mainPanel.add(timeElapsed);
         mainPanel.add(restartButton);
         mainPanel.add(typePanel);
 
         // Make the MainTypePanel scrollable (experimental)
         // JScrollPane scrollPane = new JScrollPane();
         // scrollPane.setViewportView(typePanel);
 
         // Add to the frame
         getContentPane().add(mainPanel);
         // getContentPane().add(scrollPane);
 
         // The size of the main window
         setSize(800, 400);
         setPreferredSize(getSize());
 
         typePanel.loadPreferences();
         typePanel.loadLinesAndAddToPanel();
     }
 
     /**
      * Adds a menu bar to the main panel.
      */
     public void menuBar() {
         JMenuBar menu = new JMenuBar();
         // TODO: Think what kind of menus to add
         JMenu settings = new JMenu("Settings");
         for (String key : menuItem.keySet()) {
             menuItem.get(key).addActionListener(clickResponder);
             settings.add(menuItem.get(key));
         }
         // This item is added separately so that it shows up at the last of the
         // list
         JMenuItem save = new JMenuItem("Save Current Settings");
         save.addActionListener(clickResponder);
         settings.add(save);
 
         menu.add(settings);
         setJMenuBar(menu);
     }
 
     public void restart() {
         timeElapsed.reset();
         typePanel.reset();
     }
 
     /**
      * When a key is pressed, pass it to the method in TypePanel that
      * determines what to do.
      */
     public class TypingResponder implements KeyListener {
         @Override
         public void keyPressed(KeyEvent e) {
         }
 
         @Override
         public void keyReleased(KeyEvent e) {
         }
 
         @Override
         public void keyTyped(KeyEvent e) {
             // Restart when ESCAPE key is pressed twice in a row
             if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                 if (restartFlag) {
                     restartFlag = false;
                     restart();
                 }
                 else
                     restartFlag = true;
             }
            else {
                 if (!typePanel.isRunning()) {
                     typePanel.start();
                     timeElapsed.start();
                 }
                 restartFlag = false;
                 typePanel.processPressedKey(e.getKeyChar());
 
                 // If this was the last key
                 if (!typePanel.isRunning()) {
                     timeElapsed.stop();
                     typePanel.showResultAndAdd();
                 }
             }
         }
     }
 
     /**
      * Determines what to do when a button is pressed.
      */
     public class ClickResponder implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             ChangePreferences cp =
                     new ChangePreferences(TypePanel.getPreferences());
 
             if (e.getSource() == restartButton)
                 restart();
             else if (e.getSource() == menuItem.get("ch_user"))
                 cp.changeUser();
             else if (e.getSource() == menuItem.get("ch_mode"))
                 // TODO: Change practice mode
                 ;
             else if (e.getSource() == menuItem.get("ch_font"))
                 cp.changeFont();
             else if (e.getSource() == menuItem.get("ch_layout"))
                 cp.changeKeyboardLayout();
             else if (e.getSource() == menuItem.get("ch_color"))
                 cp.changeColor();
         }
     }
 
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
         catch (InstantiationException e) {
             e.printStackTrace();
         }
         catch (IllegalAccessException e) {
             e.printStackTrace();
         }
         catch (UnsupportedLookAndFeelException e) {
             e.printStackTrace();
         }
         MainWindow mw = new MainWindow();
         mw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         mw.setLocationRelativeTo(null);
         mw.setVisible(true);
     }
 }
