 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cichlid.seprphase3.GUIInterface;
 
 import cichlid.seprphase3.*;
 import cichlid.seprphase3.GUIInterface.GUIWindow;
 import cichlid.seprphase3.Simulator.GameManager;
 import cichlid.seprphase3.Simulator.PlantController;
 import cichlid.seprphase3.Simulator.PlantStatus;
 import cichlid.seprphase3.Simulator.Simulator;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.io.*;
 import javax.imageio.*;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.text.html.Option;
 
 public class MenuInterface extends JPanel implements MouseListener {
 
     private BufferedImage backgroundImage;
     PlantGUIElement newGameButton;
     PlantGUIElement loadGameButton;
     private GUIWindow parent;
     private Simulator simulator;
     
     private Boolean loading = false;
 
     public MenuInterface(GUIWindow _parent, Simulator _simulator) {
         parent = _parent;
         simulator = _simulator;
 
         backgroundImage = ImageUtils.loadImage("images/menu.png");
 
         BufferedImage buttonImage = ImageUtils.loadImage("images/button.png");
         newGameButton = new PlantGUIElement(buttonImage, 200, 500, 1.0f, 0, 0);
         loadGameButton = new PlantGUIElement(buttonImage, 200, 600, 1.0f, 0, 0);
 
         this.addMouseListener(this);
     }
 
     @Override
     public void paintComponent(Graphics g) {
         g.drawImage(backgroundImage, 0, 0, null);
         
         g.setFont(new Font("Impact", Font.BOLD, 30));
         g.setColor(Color.BLACK);
 
         g.drawImage(newGameButton.image, newGameButton.x(), newGameButton.y(), null);
         g.drawString("New Game", newGameButton.x() + 30, newGameButton.y() + 47);
         g.drawImage(loadGameButton.image, loadGameButton.x(), loadGameButton.y(), null);
         g.drawString("Load Game", loadGameButton.x() + 25, loadGameButton.y() + 50);
         
         if (loading == true) {
             g.setColor(Color.WHITE);
             g.drawString("Loading...", 450, 550);
         }
     }
 
     @Override
     public void mouseClicked(MouseEvent click) {
         if(newGameButton.location.contains(click.getPoint())) {
            boolean cancel = false;
            
                String name = JOptionPane.showInputDialog("Enter your name");
                
            if (name == null) {return;}    

 
             loading = true;
             paint(this.getGraphics());
             simulator.setUsername(name);
             parent.state = GameState.Running;
             parent.setWindow(new PlantInterface(simulator, simulator, simulator));
            }
         
         if(loadGameButton.location.contains(click.getPoint())) {
            
               String name = JOptionPane.showInputDialog("Enter your name");
                
            if (name == null) {return;}    
                
             parent.setWindow(new LoadInterface(parent, simulator, name));
         }
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 }
