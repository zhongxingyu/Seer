 /**
  * Pylon object serving as equipment slots and turrets with angles and shit.
  * 
  * STILL UNDECIDED HOW TO PROCESS WEAPON GROUPS!
  */
 
 import java.util.ArrayList;
 import java.awt.geom.Point2D;
 import java.io.*;
 
 public class Pylon {
 
 	ShipObj source;
 	
 	GameObj setTarget; //player-set target
 	GameObj autoTarget; //default target if no player-set target available
 	
 	ItemObj equipped;
 	
 	/* //Possibly deprecated... Up to you.
 	 * //This stuff is for pylon clusters and weapon stacks...
 	 *
 	 * int capacity = 1;
 	 * int clusterIndex = 0;
 	 * ArrayList<WeaponObj> cluster = new ArrayList(0);
 	 * ArrayList<Point2D> offsets = new(ArrayList(0);
 	*/
 	
 	int size; //Module size. 1=tiny;2=small;3=medium;4=large;5=huge
 	String allowedType;//allowed item types for this pylon
 	
 	String tag;//My thought is you assign a tag to a pylon and then a tag to the associated hitCircles
 	
 	double activateTimer;
 	//The delay should depend upon the equipped module
 	
 	boolean activated=false; //Is the pylon's ability activated? (weapon firing, shield boost, engine boost, etc??)
 	int activateParams; //For some reason this seemed appropriate. I don't know how else to pass the 
 	//	Player's keypress to a sheild activation...
 	
 	
 	double currAngle; //This is angle relative to world coordinates
 	double selfAngle; //This is angle relative to the ship direction
 	double maxAngVel; //Degrees per second
 	//double angThrust; //angThrust = Mass*AngVel. Use this formula to calibrate. Set to 0 for static turrets.
 	
 	//All angles must be in degrees from 0 to 360
 	
 	double centerAngle; //This is the "centered" or "neutral" orientation of the pylon
 	double arcAngle; //This is 1/2 the angular range of the pylon in degrees
 	
 	//This should be relative to the top left corner of the displayed sprite...
 	double screenX; 
 	double screenY;
 	String gui; //For gui
 	
 	//Polar coordinates relative to the ship in world coordinates. 
 	//Makes it convenient to concatenate ship rotation.
 	double polarRadius; //This is in pixels
 	double polarAngle; //This is in degrees
 	
 	//baseHealth and realHealth we already discussed.
 	double baseHealth;
 	double realHealth;
 	
 	//Flat armor!
 	double flatArmor;
 	
 	//is this pylon set to autoAttack?
 	boolean autoAttack = true;
 
 	
 
     public Pylon(ShipObj source, String t, String allowedType, double health, double radius, double angle, double centerAngle, double arcAngle, double screenX, double screenY, int size, String image) {
     	//for testing purposes
     	//baseHealth = 50;
     	//type = "Weapon";
     	//realHealth = 100;
     	//WeaponObj testGun = new WeaponObj("Resources/Sprites/PlasmaSmall.png", "Resources/Sprites/explode_2.png", 10 , 300.0, 30000.0, 0.0, 2.0, 180); //fake guns for testing
     	//public WeaponObj(String img, String hitImg, int life, double maxSpeed, double accel, double turnSpeed, double spread) {
     	//this.equipped = (ItemObj)testGun; 
     	//
     	this.baseHealth = health;
     	this.allowedType = allowedType; 
     	this.tag=t;
     	this.polarRadius = radius;
     	this.polarAngle = angle;
     	this.centerAngle = centerAngle;
     	this.arcAngle = arcAngle;
     	this.screenX=screenX;
     	this.screenY=screenY;
     	this.gui=image;
     	this.size = size;
     	
     	this.selfAngle = centerAngle;
     		
     	if (source!=null){
     		source.pylons.add(this);
     		this.source = source;
     	}
     }
     
     public void addSource(GameObj s){
     	source = (ShipObj)s;
     }
     
     public boolean canEquip(ItemObj O){
    	if(allowedType==null) return false; //null pylon??
    	if(O.size>this.size) return false; //item too big!
    		
     	switch(O.type){
    		case "Weapon":		if(allowedType.indexOf("w")>=0){return true;}
    		case "Shield":		if(allowedType.indexOf("s")>=0){return true;}
    		case "PowerCore":	if(allowedType.indexOf("c")>=0){return true;}
    		case "Engine":		if(allowedType.indexOf("e")>=0){return true;}
    		case "Support":		if(allowedType.indexOf("p")>=0){return true;}
     	}
     	
     	
     	//System.out.println("cant equip "+O.name+" for some reason");
     	return false;
     }
     
     public void equipItem(ItemObj item){
     	if(!canEquip(item)) return;
     	this.equipped = item;
     	this.realHealth = baseHealth + item.baseHealth; 
     	
     	if(item instanceof EngineObj){
     		this.source.maxVelocity=((EngineObj)item).maxVelocity;
     		this.source.maxAngVel=((EngineObj)item).maxAngVelocity;
     	}
     }
     
     public void Step(){//This should be called by the ShipObj during ShipObj.Step()
     	
     	//realHealth = baseHealth/2*(Math.sin(Global.state.time/10)+1);
     	//System.out.println(realHealth);
     	/*
     	if (this.source == Global.state.playerObj){
     		System.out.println("pylonHealth: "+this.realHealth+", playerShip health: "+this.source.currCoreHealth);
     	}
     	*/
     	
     	if(equipped == null) return; //There is no equipped module!
     	
     	if(!equipped.isActive) return; //Equipped is a passive module
     	
     	if(realHealth <= 0) return; //Pylon's been knocked out and will not function.
     	
     	//check if weapon has a setTarget of if its on autoAttack
     	if(this.equipped.type.equals("Weapon")){
     		if(this.autoAttack==true){activated=true;}
     		else if((this.setTarget!=null)&&(this.setTarget.currCoreHealth>0)){activated=true;}
     	}
     	
     	updateCurrentAngle();
     	
     	
     	if(activated){
 	    	if(this.equipped.type.compareTo("Weapon")==0){//Since "string"=="string" is weird...
 	    		
 	    		WeaponObj weapon = (WeaponObj) equipped;
 	    	//	System.out.println("pylon check");
 	    		//First check if this weapon can fire based upon the context (ship power and ammunition)
 	    		if(!weapon.canFire()) return;
 	    		
 	    		if((setTarget!=null)&&(this.canFireOn(setTarget))){
 	    			//There is a player-set target and we can fire on it
 	    			this.shootAt(setTarget);
 	    		}else if(autoAttack){
 	    			//Check if we can fire on current autoTarget
 	    			if((autoTarget!=null)&&(this.canFireOn(autoTarget))){
 	    				this.shootAt(autoTarget);
 	    			}else{
 	    				this.autoTarget=null;
 	    				//Must find new autoTarget
 	    				for(int x=0; x<Global.state.activeObjs.size();x++){
 	    					//Look at things we can shoot at only; Which is only shipObj for now
 	    					Obj currObj = Global.state.activeObjs.get(x);
 	    					if(currObj instanceof ShipObj){
 	    						if(this.canFireOn((ShipObj) currObj)){
 	    							this.autoTarget = (ShipObj)currObj;
 	    						}
 	    					}
 	    				}
 	    			}
 	    			
 	    			//And then fire on it if there is a new autoTarget
 	    			if (this.autoTarget!=null){
 	    				this.shootAt(autoTarget);
 	    			}
 	    		}
 	    	}else if(this.equipped.type.compareTo("Shield")==0){
 	    		//First check the ship power and if the direction determined 
 	    		//	by activateParam is full health or not
 	    		
 	    		//Activate the shield; activateParam correlates with shield direction
 	    		
 	    	}else if(this.equipped.type.compareTo("Support")==0){
 	    		//check if this support item can fire based upon the context (ship power and ammunition)
 	    		
 	    		//Activate the support module.
 	    		
 	    	}
     	}else{
     		//In the event of autoattack, find a target within the angular range and etc...
     		
     		//Then set activated = true
     		
     		
     		//Realign the pylon to the center orientation.
     		UpdateAngVel();
     		AlignTo(centerAngle);
     	}
     	
     }
     
     public Boolean canFireOn(GameObj target){
     	PointS P = this.getCoords();
     	PointS T = new PointS(target.x,target.y);
     	//Check if target is alive; TODO: implement ship health?
     	
     	if(!source.isHostile(target)){
     		return false;
     	}
     	
     	if (target.currCoreHealth<=0){
     		return false;
     	}
     	//Check if target is within range (distance < missileLife*maxSpeed)
     	WeaponObj weapon = (WeaponObj) equipped;
 
     	double distance = P.distance(T);
     	//System.out.println("distance to target: "+distance+" maxMissileSpeed: "+ weapon.missileMaxSpeed+" missileLife: "+weapon.missileLife);
     	if (distance > weapon.missileMaxSpeed*weapon.missileLife){
     		return false;
     	}
     	//Check if target is within fire arc
     	Double targetAng = this.getAngle(target);
     	Double diffAng = targetAng - this.centerAngle;
     	
     	
     	if(diffAng < -180) diffAng += 360;
     	if(diffAng >= 180) diffAng -= 360;
     	
     	
     	if(Math.abs(diffAng)>this.arcAngle){
     		return false;
     	}
 
     	return true;
     }
     
     public void shootAt(GameObj target){
     	if(Global.state.time < activateTimer) return;
     	
     	activateTimer = Global.state.time+equipped.activateDelay;
     	
     	WeaponObj weapon = (WeaponObj) equipped;
     	
     	//figure out where the missile's origin is
 		PointS P = this.getCoords();
 		double nx = P.x;
 		double ny = P.y;
 		
 		//Align the weapon towards target and if the pylon pointing at target, fire.
 	    double targetAng=0;
 	    if(weapon.missileHoming){
 	    	targetAng = this.getAngle(target);
 	    }else{
 	    	targetAng = findTargetAng(target,weapon.missileMaxSpeed);
 	    }
 	    			
 	    this.AlignTo(targetAng);
 		//System.out.println("centerAngle :"+ this.centerAngle+" currAngle "+this.currAngle +" selfAngle: "+this.selfAngle +" with targetAng: "+targetAng);
 	    if(Math.abs(targetAng-selfAngle)<=weapon.angleSpread || (weapon.missileHoming)){
 	    	//fire
 	    	weapon.Fire(nx,ny,this.currAngle, this.source, target);
 	    }
 	    
 	    this.activated = false;
     }
     
     //public void takeDmg(){
     public void takeDamageFrom(MissileObj O){  
        
         double shields = source.getShields(O.lastx,O.lasty);
         shields = Math.max(0,shields-O.damageToShield);
        
         double damageHull;
         if(shields > 0) damageHull = O.damageThruShield;
         else damageHull = O.damageToHull;
      
         damageHull -= Math.max(0,flatArmor - O.damageArmorPiercing);
      
         source.setShields(shields,O.lastx,O.lasty);
         setLife(realHealth-damageHull);
     }
     
     public void setLife(double life){
     	realHealth = life;
     	
     	if(this.realHealth<=0){
     		this.activated=false;
     	}else{
     		this.activated = true;
     	}
     }
     
     public void UpdateAngVel(){ 
     	//This is used to compute the maximum angular velocity based upon equipped itemObj
     	maxAngVel = 180;
     	if(equipped.type.compareTo("Weapon")==0 && equipped != null){
     		//simplify this down so maxAngVel is hardcoded in shipFile. Eliminate excess fields!
     		//System.out.println(""+this.angThrust+", "+equipped.mass);
     		WeaponObj weapon = (WeaponObj) equipped;
     		maxAngVel = weapon.turnSpeed;
     	}
     }
     
     public void initHealth(){ 
     	//Initializes realHealth to the maximum health.
     	realHealth = baseHealth;
     	if(equipped != null) realHealth += equipped.baseHealth;
     }
     
     public void updateCurrentAngle(){
     	currAngle = source.currAngle + selfAngle;
     	
     	if(currAngle >= 360) currAngle -= 360;
     	if(currAngle < 0) currAngle += 360;
     }
     
     public boolean AlignTo(double targetAng){
     	//first update the maximum angular speed
     	double deltaAng = findDeltaAng(targetAng);
     	
     	//System.out.println("DELTA ANG: "+(targetAng-selfAngle)+", "+deltaAng+", "+centerAngle+": "+selfAngle);
     	
     	selfAngle += deltaAng;
     	double checkAngle = selfAngle - centerAngle;
     	if(checkAngle < -180) checkAngle += 360;
     	if(checkAngle >= 180) checkAngle -= 360;
     	
     	if(checkAngle < -arcAngle) selfAngle = centerAngle - arcAngle;
     	if(checkAngle > arcAngle) selfAngle = centerAngle + arcAngle;
     	
     	if(selfAngle < -180) selfAngle += 360;
     	if(selfAngle >= 180) selfAngle -= 360;   
     	
     	updateCurrentAngle();
     	
     	return (selfAngle == targetAng);
     }
     
     public double findDeltaAng(double destAngle){
     	UpdateAngVel();
     	
     	double deltaAng = (destAngle - selfAngle);
     	double maxDeltaAng = maxAngVel * Global.state.dtt;
     	
     	
     	if(deltaAng < -180) deltaAng += 360;
     	if(deltaAng >= 180) deltaAng -= 360;
     	
     	deltaAng = Math.min(maxDeltaAng,Math.max(-maxDeltaAng,deltaAng));
     	return deltaAng;
     }
     
     
     public double findTargetAng(GameObj target, double missileSpeed){
     	
     	if( (target==null)||(target==source) ) return currAngle; //Ship can't fire on itself
     	
     	PointS point = getCoords();
     	
     	//Compute the firing vector
     	double a,b,c,d,distance,t0,t1,t;
     	Vector2D V = new Vector2D(target.velX,target.velY);
     	Vector2D PO = new Vector2D(target.x-point.x,target.y-point.y);
     	Vector2D P = null;
     	
     	distance = PO.length;
     	
     	a = (V.dot(V))-(missileSpeed*missileSpeed);
     	b = PO.dot(V.multiply(2));
     	c = PO.dot(PO);
     	d = b*b - 4*a*c; //Doesn't this look familiar?
     	
     	if(d < 0) {
     		return currAngle; //No solution.
     	//d < 0 means that any bullet travelling at missileSpeed will never hit the target
     	
     	}
     	
     	t0 = (-b - Math.sqrt(d))/(2*a);
     	t1 = (-b + Math.sqrt(d))/(2*a);
     	
     	if(t0 < 0){ t = t1;
     	}else{
     		if(t1 < 0){ t = t0;
     		}else{
     			t = Math.min(t0,t1);
     		}
     	}
     	
     	P = PO.add(V.multiply(t)); //Vector to the target interception point relative to the source in world pixels.
     	
     	
     	
     	//Convert the firing vector to angle in degrees
     	double returnAngle = P.toAngle()-source.currAngle;
     	if(returnAngle < -180) returnAngle += 360;
     	if(returnAngle > 180) returnAngle -= 360;
     	
     	//System.out.println(returnAngle);
     	
     	return returnAngle;
     }
     
     public double getAngle(GameObj O){//This reutrns an angle relative to the source object's angle.
     	if(source == null || O == null) return 0;
     	
     	double sx, sy;
     	PointS P = getCoords();
     	sx = O.x - P.x; sy = O.y - P.y;
     	
     	double angle = Math.atan2(sy,sx);
     	angle -= source.currAngle;
     	
     	while(angle < 0) angle += 360;
     	while(angle >=360) angle -= 360;
     	
     	return angle;
     }
     
     public PointS getCoords(){
 		double nx = source.x+polarRadius*Math.cos(Math.toRadians(polarAngle+source.currAngle));
 		double ny = source.y+polarRadius*Math.sin(Math.toRadians(polarAngle+source.currAngle));
     	return new PointS(nx,ny);
     }
 }
