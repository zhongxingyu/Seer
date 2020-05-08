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
 
 	public void DoAddKit(Kit k) {
 		for (int s = 2; s>=0; s--){
 			if (stands[s].kit == null){
 				stands[s].kit = new GUI_Kit(k,getX(s),getY(s));
 				stands[s].temp = stands[s].kit;
 				break;
 			}
 		}
 	}
 	
 	public void DoAddKitToInspection(Kit k){
 		stands[0].kit = new GUI_Kit(k, getX(0),getY(0));
 		stands[0].temp = stands[0].kit;
 	}
 
 	public void DoRemoveKit(Kit k) {
 		for (int s = 1; s<=2; s++){
 			if (stands[s].kit!= null){
 				stands[s].removeKit();
 				break;
 			}
 		}
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
 		if (stands[i].kit == null){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	public void addkit(GUI_Kit k, int number){
 		/*stands[number].kit = k;
                 k.setX(stands[number].x);
                 k.setY(stands[number].y);*/
 	}
 	
 	public GUI_Kit checkKit(int number){
		return stands[number].temp;
 	}
 	
 	public Kit getTempKit(){
 		int i = 2;
 		for(int j = 0; j<2; j++){
 			if(stands[j].kit!= null){
 				i = j;
 				break;
 			}
 		}
 		return stands[i].kit.kit;
 	}
 
 	// the check status function will see if a GUIKit is done with it�s part
 	// annimation and is ready to be inspected or if it being inspected will
 	// make sure it gets picked up.
 	// NOT NEEDED THIS VERSION!
 	/*
 	 * void checkStatus(){
 	 * 
 	 * }
 	 */
 
 	
 }
