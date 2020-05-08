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
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Polygon;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 public class Relationship extends DraggableObject implements MouseListener{
 	private int width=0;
 	private int height=0;
 	private ERScriptRelationship mLink;
 	private MouseMotionListener moveListener;
         private String text;
         public Entity firstEntity;
         public Entity secondEntity;
 
     public Relationship(JViewport parent, ERScriptRelationship link, Entity e1, Entity e2, String text) {
         super();
         this.mLink = link;
         this.text=text;
         this.parent = parent;
         this.firstEntity=e1;
         this.secondEntity=e2;
         this.firstEntity.relationships.add(this);
         this.secondEntity.relationships.add(this);
         this.attributes=new ArrayList<Attribute>();
         
         parent.addDraggable(this);
         setLocation(10,10);
         setSize(100,50);
         ERToolView.currentFocus=mLink;
         ERToolView.PropertyField.setText(text);
         ERToolView.relationships.add(this);
       // ERToolView.addObject(this);
          final DraggableObject thisObject = this;
         MouseListener ml = new MouseListener(){
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
                             if (e.getButton()==e.BUTTON1){
 				if(isPicked(e)) {
                                 
                                     if (thisObject.conflictsWithOther(e)){
                                         if (thisObject.isFrontMost(thisObject.getConflicts(e))) dragMe();
                                         else return;
                                     }
                                     else {
                                         thisObject.moveToFront();
                                         thisObject.parent.setDraggables(ERToolView.objects);
                                         dragMe();
                                     }
 				}
                         }
                             else if (e.getButton() == e.BUTTON3){
                                 if (isPicked(e)){
                                     ERToolView.currentFocus = mLink;
                                    ObjectEditMenu menu = new ObjectEditMenu(e);
                                 }
                             }
                             
                                 
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				dontDragMe();
 			}
         };
         parent.addMouseListener(ml);
         this.mouseListener = ml;
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
 		
              g.setColor(Color.BLACK);
              
               if (this.attributes.size()!=0){
                    
                     for (Attribute a: this.attributes){
                         g.drawLine(x+width/2, y+height/2, a.x+width/2, a.y+height/2);
                     }
                     
                }
                 
                 if (this.firstEntity.isWeak || this.secondEntity.isWeak){
                     int[] xPointsW1 = {x-9, x+(width/2), x+width+9, x+(width/2)};
                  int[] yPointsW1 = {y+height/2,y-9,y+height/2,y+height+9};
                  g.fillPolygon(xPointsW1, yPointsW1,4); 
                  
                  g.setColor(Color.GRAY);
                  int[] xPointsW2 = {x-6, x+(width/2), x+width+6, x+(width/2)};
                  int[] yPointsW2 = {y+height/2,y-6,y+height/2,y+height+6};
                  g.fillPolygon(xPointsW2, yPointsW2,4); 
                 }
                   g.setColor(Color.BLACK);
                // draw background shape for border effect
                  int[] xPointsB = {x-3, x+(width/2), x+width+3, x+(width/2)};
                  int[] yPointsB = {y+height/2,y-3,y+height/2,y+height+3};
                  g.fillPolygon(xPointsB, yPointsB,4); 
                   
                 
                  
                  //then draw actual relationship
                 g.setColor(Color.WHITE);
                  int[] xPoints = {x, x+(width/2), x+width, x+(width/2)};
                  int[] yPoints = {y+height/2,y,y+height/2,y+height};
                 g.fillPolygon(new Polygon(xPoints,yPoints, 4));
 	
                  g.setColor(Color.BLACK);
                 
                  // Center text horizontally and vertically within object
                 FontMetrics fm = g.getFontMetrics();
                 int textx = (x+this.width/2) - fm.stringWidth(this.text)/2;
                 int texty = y+this.height / 2+ fm.getAscent()/2;
                 g.drawString(this.text, textx, texty);
            
                
 		super.draw(g);
 	}
 
 	private void dragMe(){
 		final Relationship self = this;
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
 	
 	
         public void setText(String s){
             this.text=s;
             parent.repaint();
         }
         
          protected boolean isPicked(MouseEvent e) {
 		if(e.getX() >= x && e.getX() <= x + width && e.getY() >= y && e.getY() <= y + height) return true;
 		return false;
 	}
         public String getText(){
             return this.text;
         }
         
          @Override
         public void delete(){
             ERToolView.relationships.remove(this);
             
             ERToolView.objects.remove(this);
              this.parent.setDraggables(ERToolView.objects);
             
             this.firstEntity.relationships.remove(this);
             
             
             this.secondEntity.relationships.remove(this);
             
            
             this.parent.repaint();
             
             this.removeMouseListener();
             ERToolView.currentFocus=null;
             ERToolView.PropertyField.setText("");
             
             
             
         }
 }
 
