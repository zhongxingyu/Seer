 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ertool;
 
 /**
  *
  * @author Kyle
  */
 
 import javax.swing.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.font.TextAttribute;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.swing.*;
 
 public class Attribute extends DraggableObject implements MouseListener{
 	private int width=0;
 	private int height=0;
 	public ERScriptAttribute mLink;
 	private MouseMotionListener moveListener;
          private String text;
          public boolean isKey;
          public DraggableObject belongsTo; //Entity or Relationship to which the attribute belongs
          
 
     public Attribute(JViewport parent, ERScriptAttribute link, String text, Boolean isKey, DraggableObject belongsTo) {
         super();
         this.mLink = link;
         this.text=text;
         this.isKey=isKey;
         this.belongsTo = belongsTo;
         this.belongsTo.attributes.add(this);
         
         this.parent = parent;
         this.parent.addDraggable(this);
         setLocation(10,10);
         setSize(100,50);
         ERToolView.currentFocus=mLink;
         ERToolView.PropertyField.setText(text);
        //ERToolView.addObject(this);
         final DraggableObject thisObject = this;
         parent.addMouseListener(new MouseListener(){
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if(isPicked(e)) {
                                 
                                     if (thisObject.conflictsWithOther(e)){
                                         if (thisObject.isFrontMost(thisObject.getConflicts(e))) dragMe();
                                         else return;
                                     }
                                     else{ 
                                         thisObject.moveToFront();
                                         thisObject.parent.setDraggables(ERToolView.objects);
                                         dragMe();
                                     }
 				}
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				dontDragMe();
 			}
         });
     }
     
     public void setLocation(int x, int y){
     	this.x = x;
     	this.y = y;
     	parent.repaint();
     }
     
     public void setSize(int width, int height){
     	this.width = width;
     	this.height = height;
     	parent.repaint();
     }
     
 	@Override
 	public void draw(Graphics g) {
 		
                 //first draw larger oval for border effect
                 g.setColor(this.belongsTo.getColor());
                 g.fillOval(x-3, y-3, width+6, height+6) ;
             
                 //then draw actual oval
                 g.setColor(Color.WHITE);
                 g.fillOval(x, y, width, height);
                 
                 
                 g.setColor(Color.BLACK);
                 Font prevFont=g.getFont();
                 
                 // Center text horizontally and vertically within object
                 if (this.isKey){
                         Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
                         map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                         g.setFont(new Font(map));
                 }
                 FontMetrics fm = g.getFontMetrics();
                 int textx = (x+this.width/2) - fm.stringWidth(this.text)/2;
                 int texty = y+this.height / 2+ fm.getAscent()/2;
                 
                 g.drawString(this.text, textx, texty);
                 g.setFont(prevFont);
            
 		super.draw(g);
                 
                 
 	}
 
 	private void dragMe(){
 		final Attribute self = this;
                 ERToolView.currentFocus=mLink;
                 ERToolView.PropertyField.setText(self.getText());
 		moveListener = new MouseMotionListener() {
 			@Override
 			public void mouseDragged(MouseEvent event) {
 				self.setLocation(event.getX() - width/2, event.getY() - height/2);
 			}
 			@Override
 			public void mouseMoved(MouseEvent event) {
 				
 			}
 		};
 		parent.addMouseMotionListener(moveListener);
     }
 	
 	private void dontDragMe(){
 		parent.removeMouseMotionListener(moveListener);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		System.out.println("Clicked at: " +e.getX() + ", " + e.getY());
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		dontDragMe();
 	}
         
          protected boolean isPicked(MouseEvent e) {
 		if(e.getX() >= x && e.getX() <= x + width && e.getY() >= y && e.getY() <= y + height) return true;
 		return false;
 	}
 	
 	
         
         public void setText(String s){
             this.text=s;
             parent.repaint();
         }
         
         public String getText(){
             return this.text;
         }
 }
