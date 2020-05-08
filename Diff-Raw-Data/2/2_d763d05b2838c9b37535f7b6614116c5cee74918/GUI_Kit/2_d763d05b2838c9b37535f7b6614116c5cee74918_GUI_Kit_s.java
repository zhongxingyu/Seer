 package gui;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 import agents.Kit;
 
 public class GUI_Kit implements GUI_Component {
 	ImageIcon background; // image of kit
 	boolean good; // is the kit good or not
 	int x, y; // coordinates
 	ArrayList<GUI_Part> parts; // list of parts
 	Kit kit;
 	
 	// Main function
 	public static void main(String[] args) {
 		// Set window and panel
 		JFrame j = new JFrame();
 		JPanel jp = new JPanel();
 
 		// Create kit and gui kit
 		Map<String, Integer> config = new TreeMap<String, Integer>();
 		config.put("Test part", 5);
 		Kit kit = new Kit(config, 1);
 		GUI_Kit guiKit = new GUI_Kit(kit, 0 ,0);
 		//guiKit.repaint();
 		// Set the jframe
 		j.setContentPane(jp);
 		j.setSize(new Dimension(60,160));
 		j.setVisible(true);
 	}
 	
 	// Constructor
 	public GUI_Kit(Kit k, int x, int y){
 		this.parts = new ArrayList<GUI_Part>();
 		this.good = false;
		this.background = new ImageIcon("gfx/nest.png");
 		this.x = x;
 		this.y = y;
 		kit = k;
 		
 		// add parts 
 		//this.parts.add(new GUI_Part());
 	}
 	
 	// Getters
 	public boolean isGood(){
 		return this.good;
 	}
 	public int getKitId(){
 		return this.kit.getKitId();
 	}
 	// Setters
 	public void setGood(boolean g){
 		this.good = g;
 	}
 	public void setX(int x){
 		this.x = x;
 	}
 	public void setY(int y){
 		this.y = y;
 	}
 	
 	// Add parts into kit
 	public void addPart(GUI_Part p){
 		this.parts.add(p);
 	}
 	
 	// Remove last part from kit
 	public GUI_Part popPart(){
 		// If there is a part left, pop the last element
 		if (parts.size() > 0){
 			return this.parts.remove(parts.size() - 1);
 		}
 		return null;
 	}
 
 	// Update the position of of the kit and parts
     public void updateGraphics() {
     	int partX = this.x;
 		int partY = this.y;
 		
 		// Set the coordinates for each part
     	for (int i = 0; i < parts.size(); i++){
     		parts.get(i).setCoordinates(partX, partY);
     		
     		partX += 20;
     	
     		// Reset the x coordinate and increment the y if its the 4th or 7th part
     		if (i == 4 || i == 7){
     			partX = this.x;
         		partY += 20;
     		}
     	}
     }
 	
 	// Paint kit background and parts
 	@Override
 	public void paintComponent(JPanel j, Graphics2D g2) {
 		this.updateGraphics();
 
 		background.paintIcon(j, g2, x, y);
 		
 		// Paint individual fishes
     	for (int i = 0; i < parts.size(); i++){
     		parts.get(i).paintComponent(j, g2);
     	}
 	}
 
 }
