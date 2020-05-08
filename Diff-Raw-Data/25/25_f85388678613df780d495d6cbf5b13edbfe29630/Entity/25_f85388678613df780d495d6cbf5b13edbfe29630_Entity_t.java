 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ertool;
 import javax.swing.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 import javax.swing.*;
 /**
  *
  * @author buetowbp
  */
 public class Entity extends DraggableObject implements MouseListener{
 	private int width=0;
 	private int height=0;
 	private ERScriptEntity mLink;
 	private MouseMotionListener moveListener;
         private String text;
         private Color color;
 
         public boolean isWeak;
 
     public Entity(JViewport parent, ERScriptEntity link, String text, Boolean isWeak) {
         super();
         this.mLink = link;
         this.parent = parent;
         this.attributes= new ArrayList<Attribute>();
         this.relationships = new ArrayList<Relationship>();
      
         this.color=Constants.colors[ERToolView.entities.size()%Constants.colors.length];
         
         parent.addDraggable(this);
         setLocation(10,10);
        
         setSize(100,50);
          this.text=text;
          this.isWeak=isWeak;
         this.moveToFront();
         ERToolView.currentFocus=mLink;
         ERToolView.PropertyField.setText(text);
         System.out.println(ERToolView.objects.toString()+"OC");
        //ERToolView.addObject(this);///IT IS HAPPENING HERE <----------------------------------
        System.out.println(ERToolView.objects.toString()+"OC");
        
         final DraggableObject thisObject = this;
         ERToolView.entities.add(this);
        
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
                                         if (thisObject.conflictsWithOther(e)){
                                             if (thisObject.isFrontMost(thisObject.getConflicts(e))) {
                                                 ERToolView.currentFocus = mLink;
                                                  ObjectEditMenu menu = new ObjectEditMenu(e);
                                             }
                                 }
                                         else{
                                              ERToolView.currentFocus = mLink;
                                              ObjectEditMenu menu = new ObjectEditMenu(e);
                                         }
                                     
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
 		
             
              g.setColor(this.color);
              //draw lines from entity to each of its attributes
             if (this.attributes.size()!=0){
                    
                     for (Attribute a: this.attributes){
                         g.drawLine(x+width/2, y+height/2, a.x+width/2, a.y+height/2);
                     }
                     
                }
             if (this.relationships.size()!=0){
                 for (Relationship r: this.relationships){
                     g.drawLine(x+width/2, y+height/2, r.x+width/2, r.y+height/2);
                 }
             }
                
             //if entity is a weak entity, draw outermost border
                 if (this.isWeak) {
                      g.setColor(this.color);
                     g.fillRect(x-9,y-9,width+18,height+18);
                     g.setColor(Color.GRAY); // color isn't dynamic; value must be manually changed with canvas color
                     g.fillRect(x-6,y-6,width+12,height+12);
                    
                     
                 }
             
                 //first draw larger rectangle in back to get a border effect
                 g.setColor(this.color);
                 g.fillRect(x-3,y-3,width+6,height+6);
                 //fill front rectangle
                 g.setColor(Color.WHITE);
 		g.fillRect(x, y, width, height);
                
                
                 
                 g.setColor(Color.BLACK);
                 
                 
                 
                 
               // Center text horizontally and vertically within object
                 FontMetrics fm = g.getFontMetrics();
                 int textx = (x+this.width/2) - fm.stringWidth(this.text)/2;
                 int texty = y+this.height / 2+ fm.getAscent()/2;
                 g.drawString(this.text, textx, texty);
                
                 
                
            
 		super.draw(g);
 	}
 
 	private void dragMe(){
 		final Entity self = this;
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
 	
 	public boolean isPicked(MouseEvent e) {
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
         
         public Color getColor(){
             return this.color;
         }
         
         @Override
         public void delete(){
             ERToolView.entities.remove(this);
             
             System.out.println(ERToolView.objects.toString()+"OR");
             ERToolView.objects.remove(this);
             System.out.println(ERToolView.objects.toString()+"OR");
             
             this.parent.setDraggables(ERToolView.objects);
             this.parent.repaint();
             
             this.removeMouseListener();
             ERToolView.currentFocus=null;
             ERToolView.PropertyField.setText("");
             
             //ERToolView.store.removeEntity(mLink);
             /*
             for (Attribute a: this.attributes){
                 
                 a.delete();
                 this.parent.setDraggables(ERToolView.objects);
                 this.parent.repaint();
             }
             for (Relationship r: this.relationships){
                
                 r.delete();
                 this.parent.setDraggables(ERToolView.objects);
                 this.parent.repaint();
             }*/
             
         }
         
         
 }
