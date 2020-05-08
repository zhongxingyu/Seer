 package Kode;
 
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.ButtonGroup;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import javax.swing.SpringLayout;
 import javax.swing.JLabel;
 
 public class GUI extends JFrame {
 
     //Alle komponenter
     private BrettRute bakgrunn = new BrettRute("src/Kode/Bilder/ramme.png");
     private JMenuBar menuBar = new JMenuBar();
     private Sjakk sjakk = new Sjakk();
     private Timer timerS;
     private Timer timerH;
     private JTextArea textarea = new JTextArea(10, 12);
     private JTextArea textarea2 = new JTextArea(10, 12);
     private JScrollPane scrollpane = new JScrollPane(textarea);
     private JScrollPane scrollpane2 = new JScrollPane(textarea2);
     private Container contentPane = getContentPane();
     private SpringLayout layout = new SpringLayout();
     SjakkListener sjakkL = new SjakkListener() {
 
         @Override
         public void sjakkReceived(SjakkEvent event) {
             if (event.lag() == 1) {
                 timerS.resume();
                 timerH.pause();
                 textarea.setText(sjakk.getHvitLogg());
             } else if (event.lag() == 2) {
                 timerS.pause();
                 timerH.resume();
                 textarea2.setText(sjakk.getSvartLogg());
             }
         }
     };
 
     public GUI(String tittel) {
         //Ramme
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setVisible(true);
         setTitle(tittel);
         contentPane.setLayout(layout);
         setJMenuBar(menuBar);
         sjakk = new Sjakk();
         sjakk.addSjakkListener(sjakkL);
         timerS = new Timer();
         timerH = new Timer();
         scrollpane = new JScrollPane(textarea);
         scrollpane2 = new JScrollPane(textarea2);
         bakgrunn.setPreferredSize(new Dimension(920, 650));
         add(scrollpane);
         add(sjakk);
         add(scrollpane2);
         add(timerS);
         add(timerH);
         //add(bakgrunn);
         layout.putConstraint(SpringLayout.WEST, scrollpane, 23, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, scrollpane, 20, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.NORTH, scrollpane2, 20, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.WEST, scrollpane2, 920, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.EAST, sjakk, 840, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, sjakk, 30, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.NORTH, timerS, 190, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.WEST, timerS, 64, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, timerH, 190, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.WEST, timerH, 970, SpringLayout.WEST, contentPane);
         
         
         
 
         //Menybar
         JMenu file = new JMenu("Fil");
         JMenu settings = new JMenu("Innstillinger");
         JMenu credits = new JMenu("Credits");
         JMenu help = new JMenu("Hjelp");
         menuBar.add(file);
         menuBar.add(settings);
         menuBar.add(credits);
         menuBar.add(help);
         menuBar.setBorder(null);
 
         //Knapper til menybar
         JMenuItem Nyttspill = new JMenuItem("Nytt Spill", new ImageIcon("src/Kode/Bilder/nyttspill1.png"));
         Nyttspill.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.SHIFT_MASK));
         JMenuItem Avslutt = new JMenuItem("Avslutt", new ImageIcon("src/Kode/Bilder/avslutt.png"));
         JMenuItem Save = new JMenuItem("Lagre spill", new ImageIcon("src/Kode/Bilder/mac.png"));
         JMenuItem Load = new JMenuItem("Åpne spill", new ImageIcon("src/Kode/Bilder/Load Icon.png"));
         Save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
         Load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
         JRadioButtonMenuItem Meme = new JRadioButtonMenuItem("Meme-sjakk");
         JRadioButtonMenuItem Vanlig = new JRadioButtonMenuItem("Vanlig sjakk");
         Meme.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.SHIFT_MASK));
         Vanlig.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.SHIFT_MASK));
         Avslutt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.SHIFT_MASK));
         JMenuItem Utviklere = new JMenuItem("Utviklere");
         ButtonGroup bg = new ButtonGroup();
 
         bg.add(Meme);
         bg.add(Vanlig);
         file.add(Nyttspill);
         file.add(Save);
         file.add(Load);
         file.add(Avslutt);
         settings.add(Meme);
         settings.add(Vanlig);
         credits.add(Utviklere);
 
         //Logg
         textarea.setEditable(false);
         textarea2.setEditable(false);
         //textarea.setOpaque(false);
         //scrollpane.setOpaque(false);
         //scrollpane.getViewport().setOpaque(false);
         scrollpane.setBorder(null);
         //textarea2.setOpaque(false);
         //scrollpane2.setOpaque(false);
         //scrollpane2.getViewport().setOpaque(false);
         scrollpane2.setBorder(null);
 
 
         //Lyttere
         Nyttspill.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 reset();
             }
         });
         Avslutt.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         Utviklere.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Copyright © 2003–2011 Andreas Kalstad, Henrik Reitan, Michael Olsen, Lars Kristoffer Sagmo. \nAll rights reserved.", "MemeChess",3, new ImageIcon("src/Kode/Bilder/trollfaceW.png"));
             }
         });
          Save.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 sjakk.tilTabell();
             }
         });
          Load.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 sjakk.fraTabell();
                 sjakk.refresh();
             }
         });
          Meme.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 sjakk.endreUI(1);
             }
         });
          Vanlig.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 sjakk.endreUI(2);
             }
         });
 
 
 
         pack();
         setLocationRelativeTo(null);
         setVisible(true);
     }
     public void reset(){
         remove(sjakk);
         remove(scrollpane);
         remove(scrollpane2);
         textarea.setText("");
         textarea2.setText("");
         repaint();
         menuBar.remove(timerS);
         menuBar.remove(timerH);
         sjakk = new Sjakk();
         timerS = new Timer();
         timerH = new Timer();
         scrollpane = new JScrollPane(textarea);
         scrollpane2 = new JScrollPane(textarea2);
         add(sjakk);
         sjakk.addSjakkListener(sjakkL);
         add(scrollpane, SpringLayout.WEST);
         add(scrollpane2, SpringLayout.EAST);
         layout.putConstraint(SpringLayout.WEST, scrollpane, 0, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.WEST, sjakk, 152, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.WEST, scrollpane2, 755, SpringLayout.WEST, contentPane);
         setVisible(true);
     }
 }
