 package aceofspades.frames;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 
 /**
  *
  * @author Player1os <player1os at gmail.com>
  */
 public class DrawEditor extends JFrame {
 
     int _width = 1280;
     int _height = 720;
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     JPanel MainPanel;
     JPanel EditorPanel;
     JPanel SettingsPanel;
     JMenuBar MenuBar;
     JMenu mFile;
     JMenuItem miNew;
     JMenuItem miOpen;
     JMenuItem miSave;
     JMenuItem miSaveAs;
     JMenuItem miExit;
     JMenu mEdit;
     JMenuItem miAddCardSetClass;
     JMenuItem miTemplates;
     JTextArea taGameInit;
     JScrollPane spGameInit;
     JTextArea taGameRules;
     JScrollPane spGameRules;
     JTextArea taGameWinCond;
     JScrollPane spGameWinCond;
 
     public DrawEditor() {
         final JFrame _frame = this;
 
         this.setTitle("Ace of Spades : Game Editor");
         this.setBounds((screenSize.width / 2) - (_width / 2), (screenSize.height / 2) - (_height / 2), _width, _height);
         this.setLocationRelativeTo(null);
         this.setResizable(false);
         this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         this.setBackground(Color.DARK_GRAY);
 
         MainPanel = new JPanel();
         MainPanel.setLayout(null);
         this.add(MainPanel, BorderLayout.CENTER);
 
         MenuBar = new JMenuBar();
 
         mFile = new JMenu("File");
 
         miNew = new JMenuItem("New");
         miNew.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,
                 java.awt.Event.CTRL_MASK));
         miNew.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent event) {
                 openFile(new File("scripts/templates/tmpFile.lua"));
             }
         });
         mFile.add(miNew);
 
         miOpen = new JMenuItem("Open");
         miOpen.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O,
                 java.awt.Event.CTRL_MASK));
         miOpen.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent event) {
                 try {
                     JFileChooser fc = new JFileChooser(".lua");
                     fc.setCurrentDirectory(new File("./scripts"));
 
                     if (fc.showOpenDialog(_frame) == JFileChooser.APPROVE_OPTION) {
                         openFile(fc.getSelectedFile());
                     }
                 } catch (Exception e) {
                     
                 }
 
             }
         });
         mFile.add(miOpen);
 
         /*
          * miSave = new JMenuItem("Save");
          * miSave.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
          * java.awt.Event.CTRL_MASK)); miSave.addActionListener(new
          * ActionListener() {
          *
          * @Override public void actionPerformed(ActionEvent event) { try {
          * JFileChooser fc = new JFileChooser(".lua");
          * fc.setCurrentDirectory(new File("./scripts"));
          *
          * if (fc.showSaveDialog(_frame) == JFileChooser.APPROVE_OPTION) { File
          * file = fc.getSelectedFile(); //Open file and write TEXT EDITOR
          * contents to current FILE } } catch (Exception e) {
          *
          * }
          *
          * }
          * }); mFile.add(miSave);
          */
 
         miSaveAs = new JMenuItem("Save As ...");
         miSaveAs.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D,
                 java.awt.Event.CTRL_MASK));
         miSaveAs.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent event) {
                 try {
                     JFileChooser fc = new JFileChooser(".lua");
                     fc.setCurrentDirectory(new File("./scripts"));
 
                     if (fc.showSaveDialog(_frame) == JFileChooser.APPROVE_OPTION) {
                         saveFile(fc.getSelectedFile());
                     }
                 } catch (Exception e) {
                 }
 
             }
         });
         mFile.add(miSaveAs);
 
         miExit = new JMenuItem("Exit");
         miExit.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X,
                 java.awt.Event.CTRL_MASK));
         miExit.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent event) {
                 _frame.dispose();
             }
         });
         mFile.add(miExit);
 
         MenuBar.add(mFile);
 
         this.add(MenuBar, BorderLayout.NORTH);
 
         EditorPanel = new JPanel();
         EditorPanel.setLayout(null);
         EditorPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
         EditorPanel.setLocation(5, 5);
         EditorPanel.setSize(1265, 665);
 
         taGameInit = new JTextArea();
         taGameInit.setEditable(true);
         spGameInit = new JScrollPane(taGameInit);
         spGameInit.setLocation(5, 5);
         spGameInit.setSize(EditorPanel.getWidth() - 10, 100);
 
         taGameRules = new JTextArea();
         taGameRules.setEditable(true);
         spGameRules = new JScrollPane(taGameRules);
         spGameRules.setLocation(5, 105);
         spGameRules.setSize(EditorPanel.getWidth() - 10, EditorPanel.getHeight() - 210);
 
         taGameWinCond = new JTextArea();
         taGameWinCond.setEditable(true);
         spGameWinCond = new JScrollPane(taGameWinCond);
         spGameWinCond.setLocation(5, EditorPanel.getHeight() - 105);
         spGameWinCond.setSize(EditorPanel.getWidth() - 10, 100);
 
         EditorPanel.add(spGameInit);
         EditorPanel.add(spGameRules);
         EditorPanel.add(spGameWinCond);
 
         MainPanel.add(EditorPanel);
         
         openFile(new File("scripts/templates/tmpFile.lua"));
 
         this.setVisible(true);
     }
 
     private void openFile(File file) {
         Scanner sc = null;
         try {
             sc = new Scanner(file);
             StringBuilder textSB = new StringBuilder();
 
             while (sc.hasNext()) {
                 String text = sc.nextLine();
                 if (text.equals("-- GameRules BEGIN")) {
                     break;
                 }
                 textSB.append(text).append("\n");
             }
 
             taGameInit.setText(textSB.toString());
             textSB = new StringBuilder();
 
             while (sc.hasNext()) {
                 String text = sc.nextLine();
                 if (text.equals("-- GameRules END")) {
                     break;
                 }
                 textSB.append(text).append("\n");
             }
 
             taGameRules.setText(textSB.toString());
             textSB = new StringBuilder();
 
             while (sc.hasNext()) {
                 textSB.append(sc.nextLine()).append("\n");
             }
 
             taGameWinCond.setText(textSB.toString());
         } catch (FileNotFoundException ex) {
             Logger.getLogger(DrawEditor.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             sc.close();
         }
     }
 
     private void saveFile(File file) {
         PrintStream ps = null;
         try {
             ps = new PrintStream(file);
             ps.println(taGameInit.getText());
             ps.println("-- GameRules BEGIN");
             ps.println(taGameRules.getText());
             ps.println("-- GameRules END");
             ps.print(taGameWinCond.getText());                    
         } catch (FileNotFoundException ex) {
             Logger.getLogger(DrawEditor.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             ps.close();
         }
     }
 }
