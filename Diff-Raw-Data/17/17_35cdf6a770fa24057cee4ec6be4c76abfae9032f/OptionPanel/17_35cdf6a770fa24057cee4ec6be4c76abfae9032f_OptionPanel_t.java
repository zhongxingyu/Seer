 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.honeybadgers.flltutorial.ui.main.content.utilities;
 
 import com.honeybadgers.flltutorial.model.Option;
 import java.awt.AlphaComposite;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ComponentAdapter;
 import java.awt.image.BufferedImage;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 
 
 /**
  *
  * @author chingaman
  */
 public abstract class OptionPanel extends JLayeredPane implements Cloneable
 {
     
     public static enum OptionState
     {
         NORMAL, HIDDEN_OCCUPY, DRAGGED, DROPPED, UNOCCUPIED, CORRECT, INCORRECT, FINISHED, TRANSPARENT
     }
     protected OptionState state;
     protected Option option;
     protected JPanel beaconPanel;
     protected JPanel contentPanel;
     protected BufferedImage panelImage;
     //JLabel description;
     public OptionPanel()
     {
         super();
         this.state = OptionState.UNOCCUPIED;
         
       
         this.initComponents();
     }
     public OptionPanel(Option option)
     {
         super();
         
         this.option = option;
         this.state = OptionState.NORMAL;
         
         this.initComponents();
     }
     private void initComponents()
     {
         this.contentPanel = new JPanel();
         this.contentPanel.setVisible(true);
         this.contentPanel.setOpaque(true);
         
         this.add(this.contentPanel, JLayeredPane.DEFAULT_LAYER);
         
         this.beaconPanel = new JPanel();
         
         this.beaconPanel.setVisible(true);          //might be more efficient to set invisible at times
         this.beaconPanel.setOpaque(false);
         
         this.add(this.beaconPanel, JLayeredPane.DRAG_LAYER);
         
         this.addComponentListener(new ComponentAdapter()
         {
              @Override
              public void componentResized(java.awt.event.ComponentEvent evt) {
                  beaconPanel.setSize(getSize());
                  contentPanel.setSize(getSize());
                  beaconPanel.revalidate();
                  contentPanel.revalidate();
              }
         });
     }
     //TODO: create methods that will draw a button under different STATES
     abstract public OptionPanel copy();
 
     /**
      * This object method transfers in a give option, and stores it. It may not show
      * the option immediately, unless the state of the option panel is NORMAL.
      * 
      * @param option the Option object that will be stored in this OptionPanel
      */
     abstract public void transferOption(Option option);
     /**
      * This object method sets the state of this OptionPanel. It is mainly used for display
      * purposes.
      * 
      * @param state the enumerate type OptionPanel.OptionState which denotes the state of the panel.
      */
     abstract public void setState(OptionState state);
     
   
     public OptionState getState()
     {
         return this.state;
     }
     
     public Option getOption() {
         return option;
     }
     boolean moved = false;
     public void hasMoved(){
         repaint();
         moved = true;
     }
     @Override
     public void paint(Graphics g)
     {
         if(this.state == OptionState.TRANSPARENT)
         {
             if(panelImage == null || panelImage.getWidth() != this.getWidth() || panelImage.getHeight() != this.getHeight())
             {
                 this.panelImage = (BufferedImage)this.createImage(this.getWidth(), this.getHeight());
             }
             Graphics gPanel = this.panelImage.getGraphics();
             gPanel.setClip(g.getClip());
             if(!moved)
             {
                 super.repaint(); 
             }
             
             super.paint(gPanel);
             
             Graphics2D g2 = (Graphics2D)g;
             
             AlphaComposite srcComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
             g2.setComposite(srcComposite);
             g2.drawImage(this.panelImage, 0, 0, null);
         }
         else
         {
             super.paint(g);
         }
     }
     /**
      * Returns an Object that covers the whole panel and can be used to track, and
      * receive events on the given option panel.
      * 
      * @return 
      */
     public Component getBeacon()
     {
         return this.beaconPanel;
     }
     /**
      * Returns the corresponding option panel from the input beacon.
      * 
      * @param object
      * @return 
      */
     public static OptionPanel getOptionPanelFromBeacon(Component object)
     {
         if(object.getParent() instanceof OptionPanel)
         {
             return (OptionPanel)object.getParent();
         }
         return null;
     }
 }
