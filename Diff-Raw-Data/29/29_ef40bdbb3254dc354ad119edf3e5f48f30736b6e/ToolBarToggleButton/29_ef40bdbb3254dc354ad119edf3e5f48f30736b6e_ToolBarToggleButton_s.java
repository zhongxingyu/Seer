 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package app.navigps.gui.buttons;
 
import app.navigps.utils.OutputVerboseStream;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * Created on 2009-01-06, 04:01:24
  * @author vara
  */
 public class ToolBarToggleButton extends JToggleButton implements MouseListener,ChangeListener{
 
     private boolean mouseOnButton = false;
     private boolean mousePressedButton = false;
 
     private GradientPaint[] gradientOnButton = new GradientPaint[2];
     private GradientPaint[] gradientPressedButtion = new GradientPaint[2];
     private Rectangle2D [] recForGradient = new Rectangle2D[2];
     private Dimension oldSize = null;
     private RoundRectangle2D borderOnButton;
 
     private float roundCorner = 12.0f;
 
     /**
      *
      * @param a
      * @param i
      */
     public ToolBarToggleButton(Action a,ImageIcon i){
         super(a);
         setIcon(i);
         setText("");
         setFocusPainted(false);
         setBorderPainted(false);
         if(a.isEnabled())
             addMouseListener(this);
 
         addChangeListener(this);
     }
     
     private void updateMyUI(){
         oldSize = getSize();
         gradientOnButton[0] = new GradientPaint(0.0f, (float) getHeight()/4,new Color(250,250,255),
                                                 0.0f, 0.0f, new Color(224,234,255));
         gradientOnButton[1] = new GradientPaint(0.0f, (float) getHeight()/3, new Color(250,250,255),
                                                 0.0f, (float) getHeight(), new Color(174,201,255));
 
         gradientPressedButtion[0] = new GradientPaint(0.0f, (float) getHeight()/4,new Color(255,255,255),
                                                       0.0f, 0.0f, new Color(124,134,155));
         gradientPressedButtion[1] = new GradientPaint(0.0f, (float) getHeight()/3, new Color(255,255,255),
                                                       0.0f, (float) getHeight(), new Color(74,101,155));
         recForGradient[0] = new Rectangle2D.Double(0, 0, getWidth(), getHeight()/3);
         recForGradient[1] = new Rectangle2D.Double(0, getHeight()/3, getWidth(), getHeight());
         borderOnButton    = new RoundRectangle2D.Double(1,2, getWidth()-2, getHeight()-4, roundCorner,roundCorner);
     }
 
     @Override
     public void paintComponent(Graphics g){
         //super.paintComponent(g);
 
         if(isEnabled()){
             final Graphics2D g2 = (Graphics2D) g.create();
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
             g2.setColor(getBackground());
             g2.fillRect(0, 0, getWidth(), getHeight());
 
             int icox= (getWidth()-getIcon().getIconWidth())>>1;
             int icoy= (getHeight()-getIcon().getIconHeight())>>1;
 
             if(oldSize==null || oldSize.getHeight()!=getHeight() || oldSize.getWidth()!=getWidth())
                 updateMyUI();
 
             if(isMouseOnButton() || isSelected()){
                 Paint oldPaint = g2.getPaint();
                 g2.setClip(borderOnButton);
                 GradientPaint [] gradient = null;
                 Color borderColor = null;
 
                 if(mousePressedButton){
                     borderColor = new Color(25, 25, 25);
                     gradient = gradientPressedButtion;
                     icox+=2;
                     icoy+=2;
                 }else{
                     borderColor = new Color(155, 155, 155);
                     gradient = gradientOnButton;
                 }
 
                 for(int i=0;i<gradient.length && i<recForGradient.length;i++){
                     g2.setPaint(gradient[i]);
                     g2.fill(recForGradient[i]);
                 }
                 g2.setPaint(oldPaint);
                 g2.setClip(0, 0,getWidth(),getHeight());
                 g2.setColor(borderColor);
                 g2.draw(borderOnButton);
 
             }
             getIcon().paintIcon(this, g2,icox,icoy);
             g2.dispose();
         }else
             super.paintComponent(g);
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {}
     @Override
     public void mousePressed(MouseEvent e) {
         mousePressedButton = true;
     }
     @Override
     public void mouseReleased(MouseEvent e) {
 
         mousePressedButton = false;
     }
     @Override
     public void mouseEntered(MouseEvent e) {
         setMouseOnButton(true);
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         setMouseOnButton(false);
     }
 
     /**
      *
      * @return
      */
     protected boolean isMouseOnButton() {
         return mouseOnButton;
     }
 
     /**
      *
      * @param mouseOnButton
      */
     protected void setMouseOnButton(boolean mouseOnButton) {
         this.mouseOnButton = mouseOnButton;
     }
 
     @Override
     public void setEnabled(boolean b){
         super.setEnabled(b);
         if(b)
             addMouseListener(this);
         else
             removeMouseListener(this);
         
         //System.out.println(getAction().getValue(Action.NAME)+" isEnabled "+b);
     }
 
     @Override
     public void stateChanged(ChangeEvent e) {
         //System.out.print("ChangeEvent ");
         //System.out.println(((AbstractButton)e.getSource()).getAction().getValue(Action.NAME)+" is selected "+((AbstractButton)e.getSource()).isSelected());
     }
 }
