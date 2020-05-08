 /**
  * @(#)MissileObj.java
  *
  *
  * @author 
  * @version 1.00 2012/6/27
  */
  
 import java.util.*;
 import java.net.*;
 
public class MissileObj extends GameObj{
 	
 	GameObj target=null;
 	double maxAngVel = 0.5;
	double maxVelocity = 2.5;
     double velocity = 2.5;
    double accel = 1;
     int damage = 0;
     double timeLeft = 0; //how long the missle flies before it dies
     
    
    public void Init(GameObj targetObj, int dmg, double turnRate, double speed, double maxSpeed, double accelaration, double time){
     	Global.state.activeObjs.add(this);
     	if(CameraCanSee()){
     		Global.view.addDrawObject(this);
     	}
     	target = targetObj;
     	timeLeft = time;
     	damage = dmg;
     	maxAngVel = turnRate;
     	velocity = speed;
    	maxVelocity = maxSpeed;
    	accel = accelaration;
     }
     
     public void Step(){
     	if (timeLeft <= 0){
     		//if missle is out of time, delete it here
     	}
     	
     	if (target!=null) {
     		//Calculate targetAng from missle to target
     		//Ant: make getAngle in Obj!!
     		double targetAng=0.0;
     	
     	
     		//Then rotate missle towards target
     		double deltaAng = (targetAng - angle);
     		if(deltaAng < -180) deltaAng += 360;
     		if(deltaAng > 180) deltaAng -= 360;
     		deltaAng = Math.min(maxAngVel,Math.max(-maxAngVel,deltaAng));
     	
     		rotate(deltaAng);
     	}
     	
     	int frame;
     	frame = (int) Math.round(angle/5)+1;
     	sprite.setFrame(frame);
     	
     	velX = velocity * Math.cos(angle/180*Math.PI);
     	velY = velocity * Math.sin(angle/180*Math.PI);
     	
     	move(velX,velY);
     	
     	Global.player.cx += velX;
     	Global.player.cy += velY;
     	
     }
     
     
    public MissileObj(String image) {
     	super(image);
     }
     
    public MissileObj(String image, URL spritecontext) {
     	super(image, spritecontext);
     }
     
     
 }
