 package tribes2d.maingame;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 import tribes2d.map.LoadMap;
 import tribes2d.player.PlayerDefault;
import java.awt.image.BufferedImage;
 
 public class GameWindow extends JPanel implements ActionListener {
 
     private Timer timer;
     private PlayerDefault player;
     private boolean mapNotLoaded = true;
 
     public GameWindow() {
         addKeyListener(new TAdapter());
         setFocusable(true);
         setBackground(Color.BLUE);
         setDoubleBuffered(true);
 
         player = new PlayerDefault();
 
         timer = new Timer(5, this);
         timer.start();
     }
 
     public void initGamePanel(JFrame parent) {
     }
 
     public void paint(Graphics g) {
         super.paint(g);
 
         Graphics2D g2d = (Graphics2D) g;
         if (mapNotLoaded) {
             try {
                 new LoadMap(g, null, this);
                mapNotLoaded=false;
             } catch (IOException ex) {
                 Logger.getLogger(GameWindow.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
        else
            {
                LoadMap.paintComponent(g,LoadMap.level);
            }
         g2d.drawImage(player.getImage(), player.getX(), player.getY(), this);
         Toolkit.getDefaultToolkit().sync();
         g.dispose();
     }
 
     public void actionPerformed(ActionEvent e) {
         player.move();
         repaint();
     }
 
     private class TAdapter extends KeyAdapter {
 
         public void keyReleased(KeyEvent e) {
             player.keyReleased(e);
         }
 
         public void keyPressed(KeyEvent e) {
             player.keyPressed(e);
         }
     }
 }
