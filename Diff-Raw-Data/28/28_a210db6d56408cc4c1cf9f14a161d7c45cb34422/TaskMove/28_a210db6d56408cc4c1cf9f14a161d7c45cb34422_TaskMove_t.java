 package nitrogene.npc;
 
 import org.newdawn.slick.geom.Line;
 
 public class TaskMove extends Task {
 	private float desx, desy, cx, cy;
 	private Line line1;
 	private Line line2;
 	public int taskID = 1;
 	public TaskMove(NPCship s, float desx, float desy){
 		super(s);
 		this.desx = desx;
 		this.desy = desy;
 		findlines();
 	}
 	
 	//plot course to destination coordinates
 	private void findlines(){
 		//Line line1 = new Line(cx, desx);
 		//Line line2 = new Line(cy, desy);
 	}
 	
 	private void findobstruction(){
 		
 	}
 
 	@Override
 	public void activate(int delta, float camX, float camY) {
		if(this.ship.getX() != desx && this.ship.getY() != desy) {
			if(desx > this.ship.getX()) {
				this.ship.setX(this.ship.getX() + 3);
			} else {
				this.ship.setX(this.ship.getX() - 3);
			}
			if(desy > this.ship.getY()) {
				this.ship.setY(this.ship.getY() + 3);
			} else {
				this.ship.setY(this.ship.getY() - 3);
			}
		} else if(this.ship.getX() != desx) {
 			if(desx > this.ship.getX()) {
 				this.ship.setX(this.ship.getX() + 5);
 			} else {
 				this.ship.setX(this.ship.getX() - 5);
 			}
 		} else if(this.ship.getY() != desy) {
 			if(desy > this.ship.getY()) {
 				this.ship.setY(this.ship.getY() + 5);
 			} else {
 				this.ship.setY(this.ship.getY() - 5);
 			}
 		} else {
 			this.close(delta);
			ship.removeTask(this); 
 		}
 	}
 
 	@Override
 	public void close(int delta) {
 		
 	}
 	@Override
 	public int getTaskID() {
 		// TODO Auto-generated method stub
 		return taskID;
 	}
 	
 	//create path from lines
 	
 	
 	//find time between paths
 	
 	
 	//calculate acceleration and toggles
 	
 	
 	//execute with timer
 	
 }
