 package core;
 
 import javax.swing.JPanel;
 
 import Objects.DrawObject;
 import Objects.DrawObjectLayerCompare;
 
 
 import java.awt.*;
 import java.util.ArrayDeque;
 import java.util.PriorityQueue;
 
 public class Screen extends JPanel {
 	private int temp; // temporary struct for rendering data
 	
 	public PriorityQueue<DrawObject> draw_objs;
 	
 	public Screen(){
 		setDoubleBuffered(true);
 		draw_objs  = new PriorityQueue<DrawObject>(1, new DrawObjectLayerCompare());
 	}
 
    public void paint(Graphics g) {
 	
 	   while (draw_objs.size() != 0){
 		   DrawObject d = draw_objs.poll();
 		  // System.out.println(d.getLayer()+" "+d.getPos());
 		   d.draw(g);
 	   }
 
 	   
 	  //super.paint(g);
 
 	   
 	   //Paint from whatever
    }
    public void getState(int a){
 	   temp = a;
 	   return;
    }
    
    public void addDrawObject(DrawObject object){
 	   if (!draw_objs.contains(object)){
 			//System.out.println(object.getLayer());
 
 		   draw_objs.add(object);
 	   }
 	}
    
    public void setDrawObjects(PriorityQueue<DrawObject> objects){
 		draw_objs = objects;
 	}
     
 }
