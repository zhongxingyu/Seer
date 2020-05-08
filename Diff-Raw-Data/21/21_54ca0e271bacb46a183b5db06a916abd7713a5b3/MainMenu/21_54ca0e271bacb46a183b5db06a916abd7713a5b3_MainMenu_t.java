 package gui;
 
 
 import fap_java.Game;
 
 import fap_java.Params;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import fap_java.Tools;
 
 import java.awt.BorderLayout;
 
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class MainMenu extends FAPanel implements AnimPanel{
     private JButton btnAdventure = new JButton();
     private JButton btnVersus = new JButton();
     private JButton btnQuickPlay = new JButton();
     private JButton btnOptions = new JButton();
     private JButton btnCredits = new JButton();
     private JButton btnQuit = new JButton();
     
     private ThreadGUI theThread;
     
     private JPanel nextPanel;
 
     public MainMenu(TheFrame fr, boolean animBack) {
         super(fr, null);
         nextPanel = null;
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
         btnOptions.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 btnOption_actionPerformed();
             }
             });
         btnVersus.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 btnVersus_actionPerformed();
             }
             });
         btnAdventure.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 btnAdv_actionPerformed();
             }
             });
 
         //-------------- Images
 
         // init location
         this.cloudsX = maxxC;
         this.swordX = maxxS;
         
         // Adding buttons
         if (!animBack) {
             addButtons();
         }
         else{
             this.startSliding(false);
         }
 
         this.validate();
         this.repaint();
         
         //this.startSliding(false);
     }
     
     private void btnAdv_actionPerformed() {
         nextPanel = new HardCodePanel(parent, this);
         this.startSliding(true);
     }
     
     private void btnOption_actionPerformed() {
         nextPanel = new OptionMenu(parent, this);
         this.startSliding(true);
     }
     
     private void btnVersus_actionPerformed() {
         nextPanel = new PlayerSelection(parent, this);
         this.startSliding(true);
     }
 
     private void startQuickPlay() {
         // init map
         int[] possibleMaps = { 5, 6, 8, 10, 11 };
         int nmap = possibleMaps[Tools.randRange(0, possibleMaps.length - 1)];
        nmap = 5;
         // init skill
         int pcP = Tools.randRange(1, 9, Params.excludedChars);
         int pcF = Tools.randRange(1, 9, Params.excludedChars);
 
         Game game = new Game("" + pcP + "," + pcF, "0,1", "0,1","0,1", false, nmap,Params.defaultVictoryScore,0,0);
         
         nextPanel = new LoadingScreen(parent,game,this,nmap);
         this.startSliding(true);
     }
 
     private void addButtons() {
         // Adding
         this.add(btnAdventure);
         this.add(btnVersus);
         this.add(btnQuickPlay);
         this.add(btnOptions);
         this.add(btnCredits);
         this.add(btnQuit);
     }
 
     private void removeButtons() {
         this.remove(btnAdventure);
         this.remove(btnVersus);
         this.remove(btnQuickPlay);
         this.remove(btnOptions);
         this.remove(btnCredits);
         this.remove(btnQuit);
     }
 
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
     }
 
     public void executeAnim() {
         swordX+=speed;
         cloudsX+=speed*1.35;
         if((speed < 0 && swordX <= minxS) || (speed > 0 && swordX >= maxxS)){
             endAnim();
         }
         repaint();
     }
     
     public void startSliding(boolean toTheLeft){
         this.removeButtons();
         speed = 25;
         if(toTheLeft){
             speed*=-1;
             swordX = maxxS;
             cloudsX = maxxC;
         }
         else{
             swordX = minxS;
             cloudsX = minxC;
         }
         
         theThread = new ThreadGUI(this);
         theThread.setRunning(false);
         new Thread(this.theThread).start();
         theThread.setRunning(true);
     }
     
     public void endAnim(){
         theThread.setRunning(false);
         if(nextPanel != null){
             parent.changePanel(nextPanel, BorderLayout.CENTER);
         }
         else{
             addButtons();
         }
     }
 
     public void goBack() {} // Not needed
 
     public void setNextPanel(JPanel nextPanel) {
         this.nextPanel = nextPanel;
     }
 
     public JPanel getNextPanel() {
         return nextPanel;
     }
 }
