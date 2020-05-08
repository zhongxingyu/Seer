 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Vlak;
 
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.Timer;
 
 
 public class Vlak extends JFrame implements ActionListener {
         GameState status;
         public Timer refresh;
                 
         JTextField infoBar;
         GameFrame gameFrame;
         JPanel panel;
         GridBagLayout layout;
         JButton startb;
         JButton lmapb;
         JButton cmapb;
         //JButton settingb;
         JButton exitb;
         
     public Vlak() {
         super();
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         this.refresh = new Timer(1000, this);
         status = new GameState();
         infoBar = new JTextField("-- Level 0 --");
         //gameFrame = new GameFrame();
         panel = new JPanel();
         layout = new GridBagLayout();
         startb = new JButton("- Start Game -");
         lmapb = new JButton("- Load map -");
         cmapb = new JButton("- Create map -");
         //settingb = new JButton("- Settings -");
         exitb = new JButton("-- Exit --");
         
         startb.addActionListener(new ButtonInterface(1,this));
         lmapb.addActionListener(new ButtonInterface(2,this));
         cmapb.addActionListener(new ButtonInterface(3,this));
         exitb.addActionListener(new ButtonInterface(4,this));
         
         infoBar.setEditable(false);
         infoBar.setFont(new Font("TimesRoman", Font.BOLD, 16));
         infoBar.setForeground(Color.BLUE);
        
         panel.add(infoBar);
         panel.add(startb);
         panel.add(lmapb);
         panel.add(cmapb);
         panel.add(exitb);
         
         this.add(panel);
         
         this.setSize(220, 200);
         setLocationRelativeTo(null);
         setTitle("TheTrain");
         
         status.gameType = 1;
         
         setResizable(false);
         setVisible(true);
     }
 
     public void reloadStrings() {
        if (this.status.gameType>1) { this.infoBar.setText(String.format("map: %s",this.status.mapName)); }
        else { this.infoBar.setText(String.format("-- Level %d --",this.status.level)); }
        FontMetrics metr = infoBar.getFontMetrics(infoBar.getFont());
        infoBar.setBounds(10, 10,metr.stringWidth(infoBar.getText()+20),22);
        this.repaint();
     }
     
     public int runGame() {
         status.gameState = 2;
         this.refresh.start();
         gameFrame = new GameFrame(new Board(this),1024,705,1);
         this.setVisible(false);
         return 0;
     }
     
     public int runCreator(String mapPathName) {
         status.gameState = 2;
         status.gameType = 1;
         this.refresh.start();
         gameFrame = new GameFrame(new Creator(this),1024,705,2);
         this.setVisible(false);
         return 0;  
     }
     
     public static void main(String[] args) {
         new Vlak();
     }   
 
     public void close() {
     WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
     Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
     }
     
     @Override
     public void actionPerformed(ActionEvent ae) {
         if (this.status.gameState==4) {
             this.gameFrame.close();
             System.out.print("frame cosed/game ended\n");
             this.gameFrame.dispose();
             this.status.gameState = 1;
             if (this.status.won==true) {
                 if (this.status.gameType==1) {
                     this.status.level+=1;
                     this.status.mapName = String.format("lvl_%d.map", this.status.level);
                     this.reloadStrings(); }
                 this.status.won=false; 
             }
             this.setVisible(true); }
     }
}
