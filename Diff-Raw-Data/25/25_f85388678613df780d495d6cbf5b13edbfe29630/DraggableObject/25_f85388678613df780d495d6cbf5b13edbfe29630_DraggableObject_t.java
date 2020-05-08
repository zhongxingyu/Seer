 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ertool;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 /**
  *
  * @author buetowbp
  */
 public class DraggableObject{
 	protected JViewport parent;
 	protected int x;
 	protected int y;
         protected int width;
         protected int height;
         protected String text;
         protected Color color;
         protected ArrayList<Attribute> attributes;
         protected ArrayList<Relationship> relationships;
         protected MouseListener mouseListener;
         
     public DraggableObject() {
     }
     
     public void draw(Graphics g){
     }
 
     void setText(String text) {
     
     }
     public String getText(){
         return this.text;
         
     }
     public Color getColor(){
         return this.color;
     }
     
     
     
     public boolean isFrontMost(ArrayList<DraggableObject> draggables){
         for (int i = ERToolView.objects.size() - 1; i > -1; i--){
             for (DraggableObject d2: draggables){
                 if (ERToolView.objects.get(i).equals(d2)) return false;
                 
             }
             if (ERToolView.objects.get(i).equals(this)) return true;
         }
         return true; //should never get here
        
     }
     
     public void moveToFront(){
         ArrayList<DraggableObject> temp = new ArrayList<DraggableObject>();
         for (DraggableObject d: ERToolView.objects){
            if (this.toString().equals(d.toString())) continue;
             else temp.add(d);
         }
         temp.add(this);
         ERToolView.setObjects(temp);
         
         
     }
    
     public ArrayList<DraggableObject> getConflicts(MouseEvent e){
         ArrayList<DraggableObject> retList = new ArrayList<DraggableObject>();
         for (DraggableObject d: ERToolView.objects){
             if (d.isPicked(e) && !(this.equals(d))){
                 retList.add(d);
             }
         
             
     }
         return retList;
     }
     public boolean conflictsWithOther(MouseEvent e){
        
         for (DraggableObject d: ERToolView.objects){
             if (d.isPicked(e) && !(this.equals(d))){
                 
                 return true;
             }
          }
         return false;
         
     }
     
      protected boolean isPicked(MouseEvent e) {
 		if(e.getX() >= x && e.getX() <= x + width && e.getY() >= y && e.getY() <= y + height) return true;
 		return false;
 	}
     
      public void removeMouseListener(){
          this.parent.removeMouseListener(this.mouseListener);
      }
      
      public void delete(){
          //should be implemented and overridden in each type (Entity, Attribute, Relationship)
      }
    
 }
