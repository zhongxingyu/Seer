 package gui;
 
 
 import fap_java.Game;
 
 import java.awt.Color;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import fap_java.Tools;
 import fap_java.Graph;
 
 import java.awt.BorderLayout;
 
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class MainMenu extends JPanel {
     private JButton btnAdventure = new JButton();
     private JButton btnVersus = new JButton();
     private JButton btnQuickPlay = new JButton();
     private JButton btnOptions = new JButton();
     private JButton btnCredits = new JButton();
     private JButton btnQuit = new JButton();
 
     private TheFrame parent;
 
     public MainMenu(TheFrame fr, boolean animBack) {
         //super();
         parent = fr;
         try {
             jbInit(animBack);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void jbInit(boolean animBack) throws Exception {
         this.setLayout(null);
         this.setSize(Constants.frameDimension);
 
         //------ Buttons
         // Texts
         btnAdventure.setText("Aventure");
         btnVersus.setText("Versus");
         btnQuickPlay.setText("Partie Rapide");
         btnOptions.setText("Options");
         btnCredits.setText("Credits");
         btnQuit.setText("Quitter");
         // Sizes
         btnAdventure.setSize(120, 40);
         btnVersus.setSize(120, 40);
         btnQuickPlay.setSize(120, 40);
         btnOptions.setSize(120, 40);
         btnCredits.setSize(120, 40);
         btnQuit.setSize(120, 40);
         // Locations
         int origX = 460;
         int origY = 150;
         btnAdventure.setLocation(origX + 32, origY);
         btnVersus.setLocation(origX + 70, origY + 50);
         btnQuickPlay.setLocation(origX + 38, origY + 100);
         btnOptions.setLocation(origX + 0, origY + 150);
         btnCredits.setLocation(origX + 68, origY + 200);
         btnQuit.setLocation(origX + 50, origY + 250);
         // Adding
         if(!animBack){
             addButtons();
         }
         // Effects
         btnQuickPlay.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 startQuickPlay();
             }
         });
         btnQuit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
    
     }
 
     private void startQuickPlay() {
         // init map
         int[] possibleMaps = {5,6,8,10,11};
         int nmap = possibleMaps[Tools.randRange(0, possibleMaps.length-1)];
         // init skill
         int pcP = 0;
         do {
             pcP = Tools.randRange(1, 9);
         } while (pcP == 2 || pcP == 7);
         int pcF = 0;
         do {
             pcF = Tools.randRange(1, 9);
         } while (pcF == 2 || pcF == 7);
 
         Game game = new Game("" + pcP + "," + pcF, "0,1", "0,1", true, nmap);
         parent.changePanel(game, BorderLayout.CENTER);
     }
     
     private void addButtons(){
         // Adding
         this.add(btnAdventure);
         this.add(btnVersus);
         this.add(btnQuickPlay);
         this.add(btnOptions);
         this.add(btnCredits);
         this.add(btnQuit);
     }
     private void removeButtons(){
         this.remove(btnAdventure);
         this.remove(btnVersus);
         this.remove(btnQuickPlay);
         this.remove(btnOptions);
         this.remove(btnCredits);
         this.remove(btnQuit);
     }
     
     public void paintComponent(Graphics g){
         super.paintComponent(g);
         Graphics2D g2d = (Graphics2D)g;
         // Background
         int w = this.getWidth( );
         int h = this.getHeight( );
          
         // Paint a gradient from top to bottom
         GradientPaint gp = new GradientPaint(
             0, 0, new Color(50,118,249),
             0, h, new Color(114,228,255) );
 
         g2d.setPaint( gp );
         g2d.fillRect( 0, 0, w, h );
         
         // Sword
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(Graph.guimg.get("MM_sword"), -80, 70, 2153, 762, null);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
     }
 }
