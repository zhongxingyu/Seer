 package testParticleSystem;
 
import sleimiPrototype.Particle;


 public class PMgnt {
 	
 	private int system_size = 0;
 	public Particle[] particlesystem; 
 	private int pos_x = 0;
 	private int pos_y = 0;
 	private int area_x = 0;
 	private int area_y = 0;
 	private int rad_distance = 0;
 	private int particleCounter = 0;
 	
 	public PMgnt(int system_size, int area_x, int area_y, int rad_distance){
 		this.system_size = system_size;
 		this.area_x = area_x;
 		this.area_y = area_y;
 		this.rad_distance = rad_distance;
 		particlesystem = new Particle[this.system_size];
 		createParticle();
 	}
 
 	private void createParticle() {
 		boolean done = false;
 		pos_x = (int) ((Math.random()+1)*area_x);
 		pos_y = (int) ((Math.random()+1)*area_y);
 		particlesystem[0] = new Particle(pos_x,pos_y);
 		particleCounter++;
 		for(int i = 1; i < system_size; i++){
 			while (!done) {
 				pos_x = (int) ((Math.random()+1)*area_x);
 				pos_y = (int) ((Math.random()+1)*area_y);
 				 if(isNeighbour()){
 						particlesystem[i] = new Particle(pos_x,pos_y);
 						particleCounter++;
 						done = true;
 				 }
 			}
 			done = false;
 		}
 	}
 
 	private boolean isNeighbour() {
 		boolean check = false;
 		int resultVector_y = 0;
 		int resultVector_x = 0;
 		int distance = 0;
 		while(!check){
 			for (int i = 0; i < particleCounter; i++) {
 				resultVector_x = pos_x - particlesystem[i].getLocation(0);
 				resultVector_y = pos_y - particlesystem[i].getLocation(1);
 				distance = calculateDistance(resultVector_x, resultVector_y);
 				if (distance <= rad_distance) {
 					check = true;
 					break;
 				}
 			}
 			pos_x = (int) ((Math.random()+1)*area_x);
 			pos_y = (int) ((Math.random()+1)*area_y);
 		}
 		return check;
 	}
 
 	public int calculateDistance(int resultVector_x, int resultVector_y) {
 		int distance = (int) Math.sqrt((resultVector_y*resultVector_x)+(resultVector_y*resultVector_y));
 		return distance;
 	}
 
 	public void printParticleLocation() {
 		for(Particle p : particlesystem){
 			System.out.println("X_LOCATION: " + p.getLocation(0));
 			System.out.println("Y_LOCATION: " + p.getLocation(1));
 			System.out.println();
 		}
 	}
 
 	
 	public void move(int x, int y) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
