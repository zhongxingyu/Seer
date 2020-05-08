 package newOne;
 
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.PointerInfo;
 import java.awt.event.*;
 
 public class Events implements MouseMotionListener,MouseListener{
 	private PMgnt pm;
 	private int pos_x;
 	private int pos_y;
 	private int area_x = 200;
 	private int area_y = 200;
 	private int rad_distance = 10;
 	private int particleCounter = 200;
 	private PharmacyPanel panel;
	private int orad = 0;
	private Particle particle;
 	
 	
 	public Events(PMgnt pm) {
 		this.pm = pm;
 		panel = new PharmacyPanel(pm);
		orad = pm.particlesystem[0].OUTER_RAD;
		
 	}
 
 	//Events to forward to PMgmt and PE
 	
 	//bewegen
 	//TODO
 	public void move(int x, int y) {
 		boolean check = false;
 		int resultVector_y = 0;
 		int resultVector_x = 0;
 		int distance = 0;
 		while (!check) {
 			for (int i = 0; i < particleCounter; i++) {
 				resultVector_x = pos_x - pm.particlesystem[i].getLocation(0);
 				resultVector_y = pos_y - pm.particlesystem[i].getLocation(1);
 				distance = calculateDistance(resultVector_x, resultVector_y);
 				if (distance <= rad_distance) {
 					pm.particlesystem[i].setLocation(0, x);
 					pm.particlesystem[i].setLocation(1, y);
 					check = true;
 					break;
 				}
 			}
 			pos_x = (int) ((Math.random() + 1) * area_x);
 			pos_y = (int) ((Math.random() + 1) * area_y);
 		}
 	}
 	
 	public void openPharmacyPanel(){
 		panel.setVisible(true);
 	}
 	
 	private int calculateDistance(int resultVector_x, int resultVector_y) {
 		int distance = (int) Math.sqrt((resultVector_y * resultVector_x)
 				+ (resultVector_y * resultVector_y));
 		return distance;
 	}
 
 	
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		int mouse_x,mouse_y;
 		mouse_x = e.getX();
 		mouse_y = e.getY();
 		
 		//used for Hammer(optional!) and poke
 		
 		poke(mouse_x,mouse_y);
 		
 	}
 
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void poke(int mouse_x, int mouse_y) {
 		// TODO Auto-generated method stub
 		//(1)partikel ansprechen mit abgefragten koordinaten
 		//(2)diesen partikel aendern
		int xPartLocation = 0;
		int yPartLocation = 0;
		int distance = 0;

		for (Particle p : pm.particlesystem) {
			xPartLocation = pos_x - p.getLocation(0);
			yPartLocation = pos_y - p.getLocation(1);
			distance = calculateDistance(xPartLocation, yPartLocation);
			if(distance <= orad){
				particle = p;
				break;
			}
		}
		System.out.println(particle.getSpeed(0));
		System.out.println(particle.getSpeed(1));
		particle.setSpeed(20, 20);
 		
 	}
 	
 	
 }
 	
