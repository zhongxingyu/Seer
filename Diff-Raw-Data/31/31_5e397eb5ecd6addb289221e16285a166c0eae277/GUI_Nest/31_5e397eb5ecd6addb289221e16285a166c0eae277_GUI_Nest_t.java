 package gui;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.io.File;
 import java.util.*;
 import javax.swing.*;
 
public class GUI_Nest implements GUI_Component {
 	ArrayList<GUI_Part> partList;
 	int nestNumber;
 	
 	ImageIcon image;
 	
 	public GUI_Nest(int nestNum, ImageIcon img) {
 		partList = new ArrayList<GUI_Part>();
 		nestNumber = nestNum;
 		image = img;
 	}
 	
	public void paintComponent(JPanel j, Graphics2D g) {
 		image.paintIcon(j, g, 0, nestNumber*200 + 22);
 		
 		for(int i=0; i<partList.size(); i++) {
 			GUI_Part part = partList.get(i);
 			int x = (i / 4) * 25 + 12;
 			
 			//I don't know why, but an offset of 22 is required to put the image @ 0,0 (at least on my mac) -- Alex
 			int y = (i % 4) * 25 + 200*nestNumber + 12 + 22;
 			
 			part.setCoordinates(x, y);
			part.paintComponent(j, g);
 		}
	}
 
 	public void updateGraphics() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public static void main(String[] args) {		
 		final GUI_Nest nest = new GUI_Nest(0, new ImageIcon("team06/gfx/nest.png"));
 		
 		for(int i=0; i<14; i++)
 			nest.partList.add(new GUI_Part());
 		
		JFrame f = new JFrame();
		JPanel p = new JPanel() {
 			public void paint(Graphics g) {
				nest.paintComponent(this, (Graphics2D)g);
 			}
 		};
 		
		f.setContentPane(p);
		
 		f.pack();
 		f.setVisible(true);
 	}
 }
