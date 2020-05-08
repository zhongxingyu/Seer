 //This class is the standard game thing type object, known as entities. This class
 //operates every function that is not affected by other entities. It is also used
 //to define initial constants for each entity via each different init function
 //antoerh test comment
 import java.util.Random;
 
 public class Entity extends GameMechanics{
 	
 	double x;//position
 	double y;//--------
 	double preX = 0;
 	double preY = 0;
 	double vx;//velocity
 	double vy;//--------
 	double ax;//acceleration
 	double ay;//------------
 	double linFriction; //linear friction
 	double maxlinSpeed;//maximum linear speed
 	
 	double walkForce;
 	
 	double bearing;//radial position
 	double radv; //radial velocity
 	double rada; //radial acceleration
 	double radFriction;//radial friction
 	double maxRadSpeed;//maximum radial speed
 	
 	double mass;
 	double radInertia; //rotational inertia
 	
 	double scaleOffset = 1;
 	
 	double size;
 	
 	int type;//playertype
 	int walkDirection;
 	
 	boolean walking = false;
 	
 	Random generator;
 	
 	Entity(){
 		generator = new Random();
 	}
 
 	//ship and structure initialization
 	public void playerInit(){
 		this.linFriction = 100;
 		this.radFriction = 100;
 		this.size = 400;
 		this.type = 0;
 		this.maxRadSpeed = 100;
 		this.maxlinSpeed = 1000;
 		this.mass = 5;
 		this.radInertia = 5;
 		this.walkForce = 5000;
 	}
 	
 	public void ballInit(){
 		this.linFriction = 100;
 		this.radFriction = 0;
 		this.size = 41;
 		this.maxRadSpeed = 300;
 		this.maxlinSpeed = 125;
 		this.mass = 5;
 		this.radInertia = 5;
 	}
 	
 	public boolean move(double p){
 		period = p;
 		
 		preX = this.x;
 		preY = this.y;
 		
 		this.bearing += this.radv*period; //add radial velocity to bearing
 		
 		if(this.bearing >= 360){
 			this.bearing -= 360;
 		}else if(this.bearing < 0){
 			this.bearing += 360;
 		}
 		
 		//forward motion
 		if(this.walking && sqr(this.vx) + sqr(this.vy) < sqr(this.maxlinSpeed)){
 			this.ax += this.applyForceX(this.walkDirection*this.walkForce*period, this.bearing);
 			this.ay += this.applyForceY(this.walkDirection*this.walkForce*period, this.bearing);
 		}
 		
 		double theta = Math.toDegrees(Math.atan2(this.vy,this.vx));
 		
 		if ((this.vx != 0 || this.vy != 0) && this.linFriction != 0){ //friction
 			
 			//X friction
 			if(sign(this.vx - this.linFriction*cos((int)theta)*period) == sign(this.vx) || this.ax != 0){
 				this.ax -= this.linFriction*cos((int)theta)*period;
 			}else{
 				this.vx = 0;
 			}
 			
 			//Y friction
 			if(sign(this.vy - this.linFriction*sin((int)theta)*period) == sign(this.vy) || this.ay != 0){
 				this.ay -= this.linFriction*sin((int)theta)*period;
 			}else{
 				this.vy = 0;
 			}
 		}
 		
 		this.vx += this.ax;
 		this.vy += this.ay;
 		
 		theta = Math.toDegrees(Math.atan2(this.vy,this.vx));
 		
 		this.ax = 0;
 		this.ay = 0;
 		
 		
 		double finalx = this.x + this.vx*period;
 		double finaly = this.y + this.vy*period;
 		this.x = finalx;
 		this.y = finaly;
 		
 
 		//map bound handlers
		int leftBound = -1000;
		int topBound = 5720;
		int bottomBound = 280;
		int rightBound = 7000;
		
 		if(this.type == 0 || this.type == 1){
			if(this.x < leftBound + this.size){ //left-right map bound stopper
				this.x = leftBound + this.size;
			}else if(this.x > rightBound - this.size){
				this.x = rightBound - this.size;
 			}
 			
			if(this.y > topBound - this.size){ //top-bottom map bound stopper
				this.y = topBound - this.size;
			}else if(this.y < bottomBound + this.size){
				this.y = bottomBound + this.size;
 			}
 		}
 		
 		return false;
 	}//end move void
 	
 	public double applyForceX(double force, double angle){
 		
 		return (force*cos((int)angle))/this.mass;
 		
 	}
 	
 	public double applyForceY(double force, double angle){
 		
 		return (force*sin((int)angle))/this.mass;
 		
 	}
 	
 	//custom sign method
 	public double sign(double x){
 		if (x > 0){
 			return 1;
 		}else if (x < 0){
 			return -1;
 		}else{
 			return 0;
 		}
 	}
 }
 
