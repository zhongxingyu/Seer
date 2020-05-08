 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dab.gui.gamepanel;
 
 import dab.engine.newsim.AbstractSimulator;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 
 /**
  *
  * @author eduard
  */
 public abstract class GamePanel extends JLayeredPane {
     protected AbstractSimulator simulator;
     protected BufferedImage background;
     protected ArrayList<UIComponent> uiComponents;
     
     public GamePanel(AbstractSimulator simulator) {
         // setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); 
         this.simulator = simulator;
         uiComponents = new ArrayList<>();
         
         // load background and set the size of the pannel according to it
         try {
             background = ImageIO.read(GamePanel.class.getResourceAsStream("gamepanel_bkg.png"));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         //setBackground(Color.WHITE); // default color should be white
         //setOpaque(true);
         
         //add(new JLabel(background), DEFAULT_LAYER);
         Dimension panelSize = new Dimension(background.getWidth(), background.getHeight());
         setPreferredSize(panelSize);
         setMinimumSize(panelSize);
         setMaximumSize(panelSize);
         setSize(panelSize);
         
         setLayout(null);
         
         
         uiComponents.add(new UIPump(this, simulator.getPumps().get(1),   new Point(664, 437),"pumpOff.png" , "pump_broken.png", "pumps.gif"));
         uiComponents.add(new UIPump(this, simulator.getPumps().get(0),   new Point(298, 433), "pumpOff.png" , "pump_broken.png", "pumps.gif"));
         uiComponents.add(new UIComponent(this, simulator.getReactor(),   new Point(67, 220), "reactor.png", "reactor_broken.png"));
         uiComponents.add(new UIComponent(this, simulator.getCondenser(), new Point(480, 348), "condenser.png", "condenser_broken.png"));
         uiComponents.add(new UIComponent(this, simulator.getTurbine(),   new Point(367, 64), "turbine.png", "turbine_broken.png"));
         uiComponents.add(new UIComponent(this, simulator.getCondenser(), new Point(798, 389), "coolant.png", "coolant_broken.png"));
        // uiComponents.add(new UIComponent(this, simulator.getValve(1), new Point(798, 389), "coolant.png", "coolant_broken.png"));
         
         for (UIComponent c : uiComponents) {
             add(c, PALETTE_LAYER);
             c.setVisible(true);
         }
     }
     
     public void updateComponents() {
         for (UIComponent c : uiComponents) {
             c.update();
         }
     }
     
     
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         //paintComponents(g);
         g.drawImage(background, 0, 0, null);
     }
     
 }
