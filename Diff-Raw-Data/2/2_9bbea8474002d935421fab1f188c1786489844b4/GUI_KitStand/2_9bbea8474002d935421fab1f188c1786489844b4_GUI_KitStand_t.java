 package gui;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 import agents.Kit;
 
 public class GUI_KitStand implements GUI_Component {
 
 	GUI_Stand[] stands;
 
 	// This is the constructor. It creates the array( size 3) of stands and sets
 	// each stands. Then it sets stands[0] and stand[1] to normals stand objs
 	// and stands[2] to inspection area obj.
 	public GUI_KitStand(int x, int y) {
 		stands = new GUI_Stand[3];
 
 		for (int i = 0; i < 3; i++)
			stands[i] = new GUI_Stand(x, i*150 + y);
 	}
 
 	public void DoAddKit(GUI_Kit k) {
 		for (GUI_Stand s : stands)
 			if (s.kit == null)
 				s.kit = k;
 	}
 
 	public void DoRemoveKit(GUI_Kit k) {
 		for (GUI_Stand s : stands)
 			if (s.kit == k)
 				s.removeKit();
 	}
 
 	// The paint function calls the drawing of each of the stands in the array.
 	public void paintComponent(JPanel j, Graphics2D g) {
 		for (GUI_Stand s : stands)
 			s.paintComponent(j, g);
 	}
 
 	public void updateGraphics() {
 		for (GUI_Stand s : stands)
 			s.updateGraphics();
 	}
 
 	public int getX(int i) {
 		return stands[i].x;
 	}
 
 	public int getY(int i) {
 		return stands[i].y;
 	}
 
 	public void addPart(int number, GUI_Part p) {
 		stands[number].kit.addPart(p);
 	}
 
 	public boolean positionOpen(int i) {
 		return (stands[i].kit == null);
 	}
 
 	// the check status function will see if a GUIKit is done with its part
 	// annimation and is ready to be inspected or if it being inspected will
 	// make sure it gets picked up.
 	// NOT NEEDED THIS VERSION!
 	/*
 	 * void checkStatus(){
 	 * 
 	 * }
 	 */
 
 }
