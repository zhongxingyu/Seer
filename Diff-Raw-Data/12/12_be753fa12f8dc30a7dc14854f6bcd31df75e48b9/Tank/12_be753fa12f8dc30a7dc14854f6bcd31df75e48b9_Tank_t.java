 package entities;
 
 import game.Animation;
 import game.ResourceManager;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import states.GameState;
 
 public class Tank {
 
 	private Vector2f pos;		// Position of the Tank
 	private Vector2f vel;		// Velocity of the Tank
 	private Vector2f bPos; 		// Position of the Barrel of the tank
 	private Animation body;			// Image for the body of the Tank
 	private Image barrel;	    // Image for the barrel of the Tank
 	private float bAngle;		// Angle of the barrel relative to horizontal (in degrees)
 	private float bx;			// Position of the barrel in x direction w.r.t body
 	private float by;			// Position of the barrel in y direction w.r.t body
 	private float wepx;			// Position in x direction of location projectiles will fire from (w.r.t barrel position)
 	private float wepy;			// Position in y direction of location projectiles will fire from (w.r.t barrel position)
 	private float launchSpeed;  // Percentage of weapon maximum muzzle velocity
 	private int hitpoints;		// Health of tank remaining
 	private Weapon[] weapons;	// Weapons associated with the tank
 	private int currentWeapon;	// Index of currently selected weapon
 	private float movementSpeed;// Movement speed of tank.
 	private boolean isAlive; 	// Flag to see if tank is alive.
 	private boolean hasShot;	// Flag to check if tank has shot this round
 	private float weight;
 	
 	
 	
 	public Tank(int id, float x, float y){
 		pos = new Vector2f(x,y);
 		vel = new Vector2f(0,0);
 		launchSpeed = 0;
 		bAngle = 0;
 		currentWeapon = 0;
 		isAlive = true;
 		hasShot = false;
 			
 		int[] wepIDs = new int[2]; // ID's of the two (changeable) weapons a tank has
 		loadResources(id, wepIDs); // Using ResourceManager
 		
 		updateWepXY();
 		barrel.setCenterOfRotation(0,barrel.getHeight()/2); // So that it rotates about its end.
 		bPos = new Vector2f(pos.x+bx, pos.y+by);
 		Vector2f wepPos = new Vector2f(bPos.x + wepx, bPos.y + wepy);
 		weapons = new Weapon[] {new Weapon(wepIDs[0],wepPos), new Weapon(wepIDs[1],wepPos)}; // ID of 0 is a placeholder
 	}
 
 	private void loadResources(int id, int[] wepIDs) {
 		// YAY finally loading from resource manager :D
 		body = ResourceManager.getInstance().getAnimation("TANK_" + id);
 		barrel = body.getImage(4); //should be 4
 		
 		String[] info = ResourceManager.getInstance().getText("TANK_" + id + "_INFO").split(",");
 		
 		bx = Float.parseFloat(info[0]);
 		by = Float.parseFloat(info[1]); 
 		hitpoints = Integer.parseInt(info[2]);
 		movementSpeed = Float.parseFloat(info[3]);
 		wepIDs[0] = Integer.parseInt(info[4]);
 		wepIDs[1] = Integer.parseInt(info[5]);
 		weight = Integer.parseInt(info[6]);
 	}
 
 	public void render (GameContainer gc, StateBasedGame game, Graphics g, Camera cam){
 		if (isAlive) {
 			float scale = cam.getMultipliedScale();
 			Vector2f relpos = cam.getRelFocusPos(pos);
 			Vector2f relbpos = cam.getRelFocusPos(bPos);
 			float bhalfheight = barrel.getHeight()/2; // Used to allow barrel to be drawn around point it rotates about.
 		
 			// Rotate the barrel to the correct angle and draw both barrel and body.
 			barrel.setRotation(bAngle);
 			barrel.draw(relbpos.x , relbpos.y - (bhalfheight*scale), scale); 
 			body.render(relpos.x, relpos.y, scale);
 		
 			//Debug Mode
			if (gc.isShowingFPS()) debugRender(g, relpos);
 		}
 	}
 
	private void debugRender(Graphics g, Vector2f pos) {
 		int offset = 40;
 		g.drawString(Integer.toString(hitpoints), pos.x -offset, pos.y-15);
 		g.drawString("Wep: " + currentWeapon,pos.x + offset,pos.y+20);
 		g.drawString("Ammo: " + Integer.toString(weapons[currentWeapon].getAmmoCount()),pos.x + offset,pos.y + 35);
 		g.drawString("Power: " + launchSpeed,pos.x+offset,pos.y+50);
 		g.drawString("Angle: " + bAngle,pos.x+offset,pos.y+65);
 		g.drawString(""+hasShot,pos.x+offset,pos.y+80);
 	}
 	
 	public void update (GameContainer gc, StateBasedGame game, int delta, World world, Input in, GameState gs){
 		if (isAlive){
 			body.update(delta);
 			
 			// Keep old position
 			Vector2f old_pos = new Vector2f(pos.x, pos.y);
 			Vector2f old_bPos = new Vector2f(bPos.x,bPos.y);
 		
 			updatePositions(delta, world);// Update Position (body, barrel and all weapons)
 			checkCollisions(world, old_pos, old_bPos);// Check Collisions
 			
 			if (!hasShot) checkInputs(in, world, gs, delta); // Check Inputs
 		
 			// Update Velocity
 			vel.set(vel.x, vel.y + world.getGravity()*delta * weight/1000);
 		} else {
 			gs.getCurrentPlayer().nextTank();
 		}
 	}
 		
 	
 	public void updateInBackground (GameContainer gc, StateBasedGame game, int delta, World world){
 		if (isAlive){
 			// Keep old position
 			Vector2f old_pos = new Vector2f(pos.x, pos.y);
 			Vector2f old_bPos = new Vector2f(bPos.x, bPos.y);
 
 			updatePositions(delta, world);// Update Position (body, barrel and all weapons)
 			checkCollisions(world, old_pos, old_bPos); // Check Collisions
 
 			// Update Velocity
 			vel.set(vel.x, vel.y + world.getGravity() * delta * weight/ 1000);
 		}
 	}
 
 	private void updatePositions(int delta, World world) {
 		pos.add(new Vector2f((vel.x*delta/100),(vel.y * delta/100)));
 		bPos.set(pos.x+bx, pos.y+by);
 		for (int i = 0; i < weapons.length; i++) weapons[i].updatePosition(bPos.x+wepx, bPos.y+wepy);
 	}
 
 	private void checkCollisions(World world, Vector2f old_pos,
 			Vector2f old_bPos) {
 		if (GameState.checkCollision(this, world)){ 
 			vel.set(0,0); // TODO Not properly implemented
 			pos.set(old_pos);
 			bPos.set(old_bPos);
 		}
 	}
 	
 	private void checkInputs(Input in, World world, GameState gs, int delta) {
 		if(in.isKeyDown(Input.KEY_UP)) launchSpeedUp();
 		if(in.isKeyDown(Input.KEY_DOWN)) launchSpeedDown();
 		if(in.isKeyDown(Input.KEY_LEFT)) {barrelRotateAnticlockwise(); updateWepXY();} 
 		if(in.isKeyDown(Input.KEY_RIGHT)) {barrelRotateClockwise(); updateWepXY();}
 		
 		// TODO Tank Movement Inputs
 		if(in.isKeyDown(Input.KEY_A)) {
 			if(vel.y == 0)  this.vel.set(-0.5f*movementSpeed, -0.2f * weight);
 			else this.vel.set(-5f, vel.y);
 		}
 		if(in.isKeyDown(Input.KEY_D)) {
 			if(vel.y == 0)  this.vel.set(0.5f*movementSpeed, -0.2f * weight);
 			else this.vel.set(5f, vel.y);
 		}
 		if(in.isKeyPressed(Input.KEY_W)) {
 			if(vel.y >= -0.2f * weight && vel.y <= 0)  this.vel.add(new Vector2f(0, -15*delta/(world.getGravity()))); //this implementation is really stupid, but can't think of another
 		}
 			
 		if(in.isKeyPressed(Input.KEY_SPACE)) changeWeapon();
 		
 		if(in.isKeyPressed(Input.KEY_ENTER)) {
 			if (weapons[currentWeapon].getAmmoCount() > 0) {
 				weapons[currentWeapon].shoot(launchSpeed, bAngle, gs);
 				setHasShot(true);
 			}
 		}
 	}
 
 	private void updateWepXY(){
 		float bLength = barrel.getWidth();
 		wepx = (float) (bLength*Math.cos(Math.toRadians(bAngle)));
 		wepy = (float) (bLength*Math.sin(Math.toRadians(bAngle)));
 	}
 	
 	private void changeWeapon() {
 		if (currentWeapon + 1 == weapons.length) currentWeapon = 0;
 		else currentWeapon += 1;
 		
 	}
 
 	private void barrelRotateAnticlockwise() {
 		bAngle -= 1;
 		if (bAngle <= 0) bAngle += 360;
 		if (bAngle == 179) bAngle = 180;
 	}
 
 	private void barrelRotateClockwise() {
 		bAngle += 1;
 		if (bAngle >= 360) bAngle -= 360;
 		if (bAngle == 1) bAngle = 0;
 	}
 
 	private void launchSpeedUp() {
 		if (launchSpeed >= 1) launchSpeed = 1;
 		else launchSpeed += 0.005;
 	}
 	
 	private void launchSpeedDown() {
 		if (launchSpeed <= 0) launchSpeed = 0;
 		else launchSpeed -= 0.005;
 	}
 
 	public Vector2f getPos() {
 		return pos;
 	}
 
 	public Image getImage() {
 		return body.getImage(0);
 	}
 
 	public float getbAngle() {
 		return bAngle;
 	}
 
 	public void setbAngle(float bAngle) {
 		this.bAngle = bAngle;
 	}
 
 	public float getLaunchSpeed() {
 		return launchSpeed;
 	}
 
 	public void setLaunchSpeed(float launchSpeed) {
 		this.launchSpeed = launchSpeed;
 	}
 
 	public Weapon[] getWeapons() {
 		return weapons;
 	}
 
 	public void setWeapons(Weapon[] weapons) {
 		this.weapons = weapons;
 	}
 
 	public int getCurrentWeapon() {
 		return currentWeapon;
 	}
 
 	public void setCurrentWeapon(int currentWeapon) {
 		this.currentWeapon = currentWeapon;
 	}
 
 	public void damageBy(int baseDamage) {
 		hitpoints -= baseDamage;
 	}
 	
 	public int returnHp() {
 		return hitpoints;
 	}
 
 	public boolean isAlive() {
 		if (hitpoints <= 0) isAlive = false;
 		return isAlive;
 	}
 
 	public boolean hasShot() {
 		return hasShot;
 	}
 
 	public void setHasShot(boolean b) {
 		hasShot = b;
 	}
 
 	
 	
 }
 
