 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.mgn.collabdesktop.menu.frames.about;
 
 import cz.mgn.collabdesktop.menu.MenuFrame;
 import cz.mgn.collabdesktop.utils.CConstans;
 import cz.mgn.collabdesktop.utils.ImageUtil;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.image.BufferedImage;
 import javax.swing.*;
 
 /**
  *
  * @author indy
  */
 public class About extends MenuFrame implements ActionListener {
 
     protected JButton licence;
     protected JButton authors;
     protected JButton close;
 
     public About() {
         super();
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         centerWindow();
         setVisible(true);
     }
 
     @Override
     protected String getSectionName() {
         return "about";
     }
 
     @Override
     protected void initComponents() {
        setMinimumSize(new Dimension(400, 400));
         setPreferredSize(new Dimension(400, 450));
         setSize(getPreferredSize());
         setLayout(new BorderLayout(0, 10));
 
         UpPanel upPanel = new UpPanel();
         add(upPanel, BorderLayout.NORTH);
 
         MiddlePanel middlePanel = new MiddlePanel();
         add(middlePanel, BorderLayout.CENTER);
 
         JPanel buttons = new JPanel();
         buttons.setLayout(new GridLayout(1, 3, 5, 0));
         add(buttons, BorderLayout.SOUTH);
 
         licence = new JButton("Licence");
         licence.addActionListener(this);
         buttons.add(licence);
         authors = new JButton("Authors");
         authors.addActionListener(this);
         buttons.add(authors);
         close = new JButton("Close");
         close.addActionListener(this);
         buttons.add(close);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == close) {
             dispose();
         } else if (e.getSource() == licence) {
             goTo(new Licence(), true);
         } else if (e.getSource() == authors) {
             goTo(new Authors(), true);
         }
     }
 
     protected class MiddlePanel extends JPanel {
 
         public MiddlePanel() {
             init();
         }
 
         protected void init() {
             setLayout(new BorderLayout());
             JTextArea info = new JTextArea("Collab is tool for shared drawing over the internet with possibility connection of many users in real-time. Licenced as GNU GPLv3. Copyright 2012 Moon Games.");
             info.setLineWrap(true);
             info.setWrapStyleWord(true);
             info.setEditable(false);
             JScrollPane infoScrollPane = new JScrollPane(info);
             add(infoScrollPane);
         }
     }
 
     protected class UpPanel extends JPanel implements ComponentListener {
 
         protected BufferedImage logo = null;
         protected JTextField name = null;
 
         public UpPanel() {
             loadLogo();
             init();
         }
 
         protected void init() {
             setLayout(null);
             setPreferredSize(new Dimension(200, 215));
 
             name = new JTextField(CConstans.NAME + " " + CConstans.VERSION);
             name.setHorizontalAlignment(JTextField.CENTER);
             name.setBorder(null);
             name.setOpaque(false);
             name.setEditable(false);
             int y = logo.getHeight();
             name.setLocation(0, y);
             //TODO: better font
             name.setFont(name.getFont().deriveFont(Font.BOLD, 25));
             add(name);
             
             addComponentListener(this);
         }
 
         protected void loadLogo() {
             logo = ImageUtil.loadImageFromResources("/resources/images/logo-180.png");
         }
 
         protected void paintLogo(Graphics2D g) {
             int x = (getWidth() - logo.getWidth()) / 2;
             int y = 0;
             g.drawImage(logo, x, y, null);
         }
 
         @Override
         public void paintComponent(Graphics g2) {
             Graphics2D g = (Graphics2D) g2;
             g.setColor(getBackground());
             g.fillRect(0, 0, getWidth(), getHeight());
             g.setComposite(AlphaComposite.SrcOver);
             paintLogo(g);
         }
         
         protected void recountNamePosition() {
             name.setSize(getWidth(), 35);
         }
 
         @Override
         public void componentResized(ComponentEvent e) {
            recountNamePosition();
         }
 
         @Override
         public void componentMoved(ComponentEvent e) {
         }
 
         @Override
         public void componentShown(ComponentEvent e) {
         }
 
         @Override
         public void componentHidden(ComponentEvent e) {
         }
     }
 }
