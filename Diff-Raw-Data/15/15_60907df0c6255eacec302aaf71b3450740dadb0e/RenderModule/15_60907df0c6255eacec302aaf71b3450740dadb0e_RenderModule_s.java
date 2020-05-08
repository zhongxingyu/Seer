 package core;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import Objects.DrawObject;
 
 public class RenderModule extends JFrame{
 
 public Screen sc;
 
 private ArrayList<Rectangle> redrawAreas;
 
 public RenderModule(){
 	 sc = new Screen();
 	 add(sc);
      setTitle("GameScreen");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setSize(1000, 1000);
      setLocationRelativeTo(null);
      setVisible(true);
      setResizable(false);
      redrawAreas = new ArrayList<Rectangle>();
 }
 
 public void update(int a){
 	checkUpdatedAreas();
 	sc.getState(a);
 	sc.repaint();
 }
 
 public void addUpdatedArea(Rectangle rect){
 	redrawAreas.add(rect);
 }
 
 public void checkUpdatedAreas(){
 	for (DrawObject i:DrawObject.getList()){
 		for(Rectangle rect:redrawAreas){
 			if (rect.intersects(i.getRect())){
 				addDrawObject(i);
 				break;
 			}
 		}
 	}
 	redrawAreas.clear();
 }
 
 public void checkAreaAboveLayer(Rectangle rect, int layer){
 	for (DrawObject i:DrawObject.getList()){
 		if (i.getLayer() > layer && rect.intersects(i.getRect())){
 			addDrawObject(i);
 		}
 	}
 	redrawAreas.clear();
 }
 
 public void addDrawObject(DrawObject object){
 	sc.addDrawObject(object);
 }
 
 public void setDrawObjects(PriorityQueue<DrawObject> objects){
 	sc.setDrawObjects(objects);
 }
 
 
 }
