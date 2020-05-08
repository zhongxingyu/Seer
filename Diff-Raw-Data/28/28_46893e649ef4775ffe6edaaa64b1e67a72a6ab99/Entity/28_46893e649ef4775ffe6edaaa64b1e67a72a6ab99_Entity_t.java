 //This class is the standard game thing type object, known as entities. This class
 //operates every function that is not affected by other entities. It is also used
 import java.awt.Polygon;
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
 	double sideWalkForce;
 	
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
 	int sideWalkDirection;
 	
 	final int[] cornerIntercept = {11900,5900,100,-5900};
 	
 	boolean walking = false;
 	boolean sideWalking = false;
 	
 	Random generator;
 	
 	private final double BALL_ELASTICITY=.35;
 	
 	Entity(){
 		generator = new Random();
 	}
 
 	//ship and structure initialization
 	public void playerInit(){
 		this.linFriction = 500;
 		this.radFriction = 100;
 		this.size = 200;
 		this.type = 0;
 		this.maxRadSpeed = 100;
 		this.maxlinSpeed = 1000;
 		this.mass = 5;
 		this.radInertia = 5;
 		this.walkForce = 5000;
 		this.sideWalkForce = 5000;
 	}
 	
 	public void ballInit(){
 		this.linFriction = 500;
 		this.radFriction = 0;
 		this.size = 41;
 		this.type = 1;
 		this.maxRadSpeed = 300;
 		this.maxlinSpeed = 10000;
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
 		
 		if(sqr(this.vx) + sqr(this.vy) < sqr(this.maxlinSpeed)){
 			//forward motion
 			if(this.walking){
 				this.ax += this.applyForceX(this.walkDirection*this.walkForce*period, this.bearing);
 				this.ay += this.applyForceY(this.walkDirection*this.walkForce*period, this.bearing);
 			}
 			
 			//strafing
 			if(this.sideWalking){
 				this.ax += this.applyForceX(this.sideWalkDirection*this.sideWalkForce*period, this.bearing + 90);
 				this.ay += this.applyForceY(this.sideWalkDirection*this.sideWalkForce*period, this.bearing + 90);
 			}
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
 		final int leftBound = -1000;
 		final int topBound = 5720;
 		final int bottomBound = 280;
 		final int rightBound = 7000;
 		final int GOAL_TOP= 3430;
 		final int GOAL_BOTTOM= 2590;
 		final int GOAL_LEFT = -1440;
 		final int GOAL_RIGHT = 7420;
 		
 		if(this.type == 0){//player
 			if(this.x < leftBound + this.size){ //left-right map bound stopper
 				this.x = leftBound + this.size;
 				this.vx = 0;
 			}else if(this.x > rightBound - this.size){
 				this.x = rightBound - this.size;
 				this.vx = 0;
 			}
 			
 			if(this.y > topBound - this.size){ //top-bottom map bound stopper
 				this.y = topBound - this.size;
 				this.vy = 0;
 			}else if(this.y < bottomBound + this.size){
 				this.y = bottomBound + this.size;
 				this.vy = 0;
 			}
 			//CORNER CONDITIONS
 
 			double vMag =  Math.sqrt(this.vx*this.vx+this.vy*this.vy)*.7071;
 			
 			for(int i=0;i<4;i++){
 				if(corner[i].contains(this.x+this.size*.5*cos((i+1)*45), this.y+this.size*.5*sin((i+1)*45))){
 					if(this.type == 3){
 						if(this.vy>=-this.vx)
 						{
 							this.vx= Math.pow(-1,(i+1))*vMag*cos((int) (45-this.bearing));
 							this.vy= Math.pow(-1,(i+1))*vMag*sin((int) (45-this.bearing));
 						}else{
 							this.vx= Math.pow(-1,(i))*vMag*cos((int) (45-this.bearing));
 							this.vy= Math.pow(-1,(i))*vMag*sin((int) (45-this.bearing));
 						}
 					}
 					
 					//double penetration = abs(cornerIntercept[i] -(/*x-coord*/this.y+this.size*.5*sin(45*(i+1)) - /*slope*/0.7071*Math.pow((-1),(i+1))*/*y-coord*/(this.x+this.size*.5*cos(45*(i+1)))))/*divided by sqrt(m^2+1)*//1.2247;
					//double adjustment = (this.size*1.2247+cornerIntercept[i])/(this.y - Math.pow(-1, i+1)*.7071*this.x);
 					
				}
							

					//this.x*=adjustment;
					//this.y*=adjustment;
 					
 					break;
			}
			
 			
 			/*
 			switch(containingCorner){//if the ball intersects this corner, do this
 			case 1 ://top right
 				if(this.vy>=-this.vx)
 				{
 					this.vx= -vMag*Math.cos(Math.toRadians(45-this.bearing));
 					this.vy= -vMag*Math.cos(Math.toRadians(45-this.bearing));
 				}else{
 					this.vx= vMag*Math.cos(Math.toRadians(45-this.bearing));
 					this.vy= vMag*Math.cos(Math.toRadians(45-this.bearing));
 				}
 				break;
 			case 2 ://top left
 				if(this.vy>=-this.vx)
 				{
 					this.vx= -vMag*Math.cos(Math.toRadians(45-this.bearing));
 					this.vy= -vMag*Math.cos(Math.toRadians(45-this.bearing));
 				}else{
 					this.vx= vMag*Math.cos(Math.toRadians(45-this.bearing));
 					this.vy= vMag*Math.cos(Math.toRadians(45-this.bearing));
 				}
 				break;
 			case 3 ://bottom left
 				if(this.vy<=-this.vx)
 				{
 					this.vx= -vMag*Math.cos(Math.toRadians(45-this.bearing));
 					this.vy= -vMag*Math.cos(Math.toRadians(45-this.bearing));
 				}else{
 					this.vx= vMag*Math.cos(Math.toRadians(45-this.bearing));
 					this.vy= vMag*Math.cos(Math.toRadians(45-this.bearing));
 				}
 				break;
 			case 4 ://bottom right
 				if(this.vy>=-this.vx)
 				{
 					this.vx= -vMag*Math.cos(Math.toRadians(45-this.bearing));
 					this.vy= -vMag*Math.cos(Math.toRadians(45-this.bearing));
 				}else{
 					this.vx= vMag*Math.cos(Math.toRadians(45-this.bearing));
 					this.vy= vMag*Math.cos(Math.toRadians(45-this.bearing));
 				}
 				break;
 			default :
 				break;
 			}
 			
 			if(this.y<(-this.x)+100+(.7071*this.size))//Bottom Left
 			{
 				
 				this.x+=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 				this.y+=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 			}
 			if(this.y>-(this.x)+11900+(.7071*this.size))//Top Right
 			{
 				
 			}
 			
 			if(this.y>(this.x)+5900-(.7071*this.size))//Top Left
 			{
 				
 				this.x+=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 				this.y-=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 			}
 			if( this.y<(this.x)-5900+(.7071*this.size))//Bottom Right
 			{
 				
 				this.x-=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 				this.y+=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 			}
 			*/
 		}
 		//3430 2590
 		if(this.type == 1){//ball
 			if(this.y>GOAL_BOTTOM && this.y<GOAL_TOP)
 			{
 				//Top Goal Bound
 				if(this.y+this.size>GOAL_TOP && (this.x < leftBound ||this.x > rightBound)){
 					this.y = GOAL_TOP - this.size;
 					this.vy *= -BALL_ELASTICITY;
 					this.vx *= BALL_ELASTICITY;
 				}else if(this.y-this.size<GOAL_BOTTOM && (this.x < leftBound ||this.x > rightBound)){ //Bottom Goal Bound
 					this.y = GOAL_BOTTOM + this.size;
 					this.vy *= -BALL_ELASTICITY;
 					this.vx *= BALL_ELASTICITY;
 				}else if(this.x-this.size<GOAL_LEFT){ //Left Goal Bound
 					this.x = GOAL_LEFT + this.size;
 					this.vx *= -BALL_ELASTICITY;
 					this.vy *= BALL_ELASTICITY;
 				}else if(this.x+this.size>GOAL_RIGHT){ //Right Goal Bound
 					this.x = GOAL_RIGHT - this.size;
 					this.vx *= -BALL_ELASTICITY;
 					this.vy *= BALL_ELASTICITY;
 				}
 			
 		}else{			
 			if(this.x < leftBound + this.size){ //left-right map bound stopper
 					this.x = leftBound + this.size;
 					this.vx *= -BALL_ELASTICITY;
 					this.vy *= BALL_ELASTICITY;
 			}else if(this.x > rightBound - this.size){ //RIGHT SIDE
 					this.x = rightBound - this.size;
 					this.vx *= -BALL_ELASTICITY;
 					this.vy *= BALL_ELASTICITY;
 			}
 			
 			if(this.y > topBound - this.size){ //top-bottom map bound stopper
 				this.y = topBound - this.size;
 				this.vy *= -BALL_ELASTICITY;
 				this.vx *= BALL_ELASTICITY;
 			}else if(this.y < bottomBound + this.size){ // BOTTOM
 				this.y = bottomBound + this.size;
 				this.vy *= -BALL_ELASTICITY;
 				this.vx *= BALL_ELASTICITY;
 			}
 		}
 			//CORNER CONDITIONS
 		/*	final double CORNER_POSITION_OFFSET_MULTIPLIER = 2*.7071;//NEEDS *.7071 FOR PROPER OPERATION
 			if(this.y<(-this.x)+100+(.7071*this.size))//Bottom Left
 			{
 				double temp= -(this.vx*BALL_ELASTICITY);
 				this.vx=-(this.vy*BALL_ELASTICITY);
 				this.vy=temp;
 				
 				this.x+=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 				this.y+=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 			}
 			if(this.y>-(this.x)+11900+(.7071*this.size))//Top Right
 			{
 				double temp= -(this.vx*BALL_ELASTICITY);
 				this.vx=-(this.vy*BALL_ELASTICITY);
 				this.vy=temp;
 				this.x-=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 				this.y-=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 			}
 			
 			if(this.y>(this.x)+5900+(.7071*this.size))//Top Left
 			{
 				double temp= (this.vx*BALL_ELASTICITY);
 				this.vx=(this.vy*BALL_ELASTICITY);
 				this.vy=temp;
 				this.x+=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 				this.y-=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 			}
 			if( this.y<(this.x)-5900+(.7071*this.size))//Bottom Right
 			{
 				double temp= (this.vx*BALL_ELASTICITY);
 				this.vx=(this.vy*BALL_ELASTICITY);
 				this.vy=temp;
 				this.x-=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 				this.y+=CORNER_POSITION_OFFSET_MULTIPLIER*this.size;
 			}
 			*/
 			
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
 
