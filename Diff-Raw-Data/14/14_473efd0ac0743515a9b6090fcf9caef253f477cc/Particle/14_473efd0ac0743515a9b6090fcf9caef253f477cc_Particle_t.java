 package newOne;
 
 public class Particle {
 	public final int INNER_RAD = 5;//Lieber beim erstellen im PMgnt festlegen?
	public final int OUTER_RAD = 20;//Lieber beim erstellen im PMgnt festlegen?
 	private int[] location = new int[2];
 	private float[] speed = {0.0f, 0.0f};
 
 	Particle(int pos_x, int pos_y) {
 		this.location[0] = pos_x;
 		this.location[1] = pos_y;
 	}
 
 	public void setLocation(int x_loc, int y_loc) {
 		this.location[0] = x_loc;
 		this.location[1] = y_loc;
 	}
 
 	public int getLocation(int direction) {
 		return location[direction];
 	}
 
 	public float getSpeed(int direction) {
 		return speed[direction];
 	}
 
 	public void setSpeed(float x_speed, float y_speed) {
		float x_speed_abs = Math.abs(x_speed);
		float y_speed_abs = Math.abs(y_speed);
		
		if((x_speed_abs > OUTER_RAD && y_speed_abs > OUTER_RAD)){
 			this.speed[0] = 10;
 			this.speed[1] = 10;
 		}else{
			if(x_speed_abs > OUTER_RAD && y_speed_abs < OUTER_RAD){
 				this.speed[0] = 10;
 				this.speed[1] = y_speed;
 			}else{
				if(x_speed_abs < OUTER_RAD && y_speed_abs > OUTER_RAD){
 					this.speed[0] = x_speed;
 					this.speed[1] = 10;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Calculates distance between two particles
 	 * @param p1 Particle
 	 * @param p2 Particle
 	 * @return array of integer containing 0=Vector_x, 1=Vector_y 2=Distance
 	 */
 	public int[] getDistance(Particle p){
 		int[] result = new int[3];
 		result[0]=(this.getLocation(0)-p.getLocation(0));
 		result[1]=(this.getLocation(1)-p.getLocation(1));
 		result[2]=(int) Math.sqrt((result[0]*result[0])+(result[1]*result[1]));
 		return result;
 	}
 
 
 }
