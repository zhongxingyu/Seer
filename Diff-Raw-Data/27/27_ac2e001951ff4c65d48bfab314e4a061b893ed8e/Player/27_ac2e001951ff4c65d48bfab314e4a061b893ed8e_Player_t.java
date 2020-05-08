 package ch.ilikechickenwings.karpfengame.Entity;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 
 import ch.ilikechickenwings.karpfengame.Level;
 import ch.ilikechickenwings.karpfengame.Handler.InputHandler;
 import ch.ilikechickenwings.karpfengame.Skill.Skill;
 import ch.ilikechickenwings.karpfengame.Entity.Entity;
 import ch.ilikechickenwings.karpfengame.Entity.Item.HealthPack;
 import ch.ilikechickenwings.karpfengame.Entity.Item.Coffee;
 import ch.ilikechickenwings.karpfengame.Entity.Mob.Mob;
 import ch.ilikechickenwings.karpfengame.Entity.Projectile.Carp;
 import ch.ilikechickenwings.karpfengame.Entity.Projectile.Eel;
 import ch.ilikechickenwings.karpfengame.Entity.Projectile.Projectile;
 
 public class Player extends Entity{
 
 	private boolean invincible=false;
 	private long currTimeInvincible;
 	private long oldTimeInvincible;
 	private long[] currTimeSkill= new long[Skill.getNr()];
 	private long[] oldTimeSkill= new long[Skill.getNr()];
 	private int dir=0;
 	private boolean[] enableSkill;
 	private int enabledSkill=0;
 	private int xVel;
 	private int yVel;
 	private boolean gravityOn;
 	private int g=1;
 	
 	private int jumpVel=-15;
 	private int xAbsVel=4;
 	
 	// coffee:
 	private int coffee;
 
 	public Player(int x, int y, int lifes, int coffee, boolean[] useableSkill) {
 		
 		setX_Point(x);
 		setY_Point(y);
 		setLifes(lifes);
 		setWidth(30);
 		setHeight(70);
 		setxVel(0);
 		setyVel(0);
 		setCoffee(coffee);
 		setInvincible(false);
 		setEnableSkill(useableSkill);
 		setGravityOn(true);
 		//super(x, y, lifes);
 		// TODO Auto-generated constructor stub
 	}
 
 
 	public void update(InputHandler inHandler) {
 		
 		//Skill
 		for(int i=0;i<Skill.getNr();i++){
 		    if(currTimeSkill[i]>=oldTimeSkill[i]+500){
 			    enableSkill[i]=true;
 		    }else{
 			    currTimeSkill[i]=System.currentTimeMillis();
 		    }
 		}
 		
 		if(inHandler.getKeys()[KeyEvent.VK_1]){ // Carp
 			enabledSkill=0;
 		}else if(inHandler.getKeys()[KeyEvent.VK_2]){ // Eel
 			enabledSkill=1;
 		}
 		
 		for(int i=0;i<Skill.getNr();i++){
 			//The problem lies here... i debugged the game and the forbiddenskill always jumps back to true!
			if(inHandler.getKeys()[KeyEvent.VK_S]&&enableSkill[i]&&Level.useableSkill[i]&&enabledSkill==i){ // Carp
 				Skill skill=(Skill) Level.getSkills()[i];
 				if(getCoffee()>=skill.getCoffee()){
 					setCoffee(getCoffee()-skill.getCoffee());
 					enableSkill[i]=false;
 					oldTimeSkill[i]=System.currentTimeMillis();
 					switch(i){
 					    case 0:
 					    Carp carp=new Carp(getX_Point()+getWidth(),getY_Point()+getHeight()/2);
 					    Level.getEntities().add(carp);
 					    break;
 					    case 1:
 					    Eel eel=new Eel(getX_Point()+getWidth(),getY_Point()+getHeight()/2,this);
 					    Level.getEntities().add(eel);
 					    break;
 					}
 				}
 			}
 		}
 		
 		
 			// movement
 			if ((inHandler.getKeys()[KeyEvent.VK_W]||inHandler.getKeys()[KeyEvent.VK_UP]||inHandler.getKeys()[KeyEvent.VK_SPACE])&&!isGravityOn()) { // 
 				setGravityOn(true);
 				setyVel(getJumpVel()); 
 				setDir(3);
 			}
 
 			if (inHandler.getKeys()[KeyEvent.VK_A]||inHandler.getKeys()[KeyEvent.VK_LEFT]) {
 				setxVel(-getxAbsVel());
 				setDir(2);
 			}
 			if (inHandler.getKeys()[KeyEvent.VK_D]||inHandler.getKeys()[KeyEvent.VK_RIGHT]) {
 				setxVel(getxAbsVel()); 
 				setDir(1);
 			}
 			if(!(inHandler.getKeys()[KeyEvent.VK_A]||inHandler.getKeys()[KeyEvent.VK_LEFT]||inHandler.getKeys()[KeyEvent.VK_D]||inHandler.getKeys()[KeyEvent.VK_RIGHT])){
 				setxVel(0);
 			}
 			if(isGravityOn()){
 				setyVel(getyVel()+g);
 		    }
 			
 			walk();
 			
 			//Added Cooldown for damage
 			if(invincible){
 			    currTimeInvincible=System.currentTimeMillis();
 				if(currTimeInvincible>oldTimeInvincible+1000){ // 1 sec
 				    oldTimeInvincible=System.currentTimeMillis();
 				    setInvincible(false);
 			    }	
 			}
 			
 	}
 
 	
 	public void draw(Graphics2D g2,int xOffset){
 		g2.setColor(Color.black);
 		//coffe shouldn't be above the player
 		
 		Image imge1 = null;
 			imge1 = Toolkit.getDefaultToolkit().getImage(
 					getClass().getResource("/res/loadingbar.png"));
 		// coffee
 		g2.drawImage(imge1,5, 5, 150, 30,null);
 		g2.fillRect(5+(int)(150*coffee/100), 5,150,30);
 		// lifes
 		// TODO: Load life-image
 		g2.drawImage(imge1,160, 5, 150, 30,null);
 		g2.fillRect(160+(int)(150*getLifes()/100), 5,150,30);
 		
 		// player
 		g2.setColor(Color.green);
 		g2.fillRect(getX_Point()-xOffset, getY_Point(), getWidth(), getHeight());
 		if(invincible){
 		g2.setColor(Color.blue);
 		g2.drawRect(getX_Point()-xOffset, getY_Point(), getWidth(), getHeight());
 		}
 	}
 	
 	
 	
 	private void walk(){
 		setX_Point(getX_Point() + getxVel());
 		setY_Point(getY_Point()+getyVel());
 	}
 	
 	
 	
 	public void getDamaged(Mob mob){
 		if(!invincible){
 		setLifes(getLifes()-mob.getDamage());
 		setInvincible(true);
 		}
 	}
 	public void getDamaged(Projectile pro){
 		if(!invincible){
 		setLifes(getLifes()-pro.getDamage());
 		setInvincible(true);
 		}
 	}
 	
 	public void getHealed(HealthPack hp) {
 		setLifes(getLifes()+hp.getHealth());
 		if(getLifes()>Level.getMaxLife()){
 			setLifes(Level.getMaxLife());// this is totally static and should totally not be static
 		}
 		
 	}
 	public void getCaffeined(Coffee co){
 		setCoffee(getCoffee()+co.getCaffeine());
 		if(getCoffee()>Level.getMaxCoffee()){
 			setCoffee(Level.getMaxCoffee()); // this is totally static and should totally not be static
 		}
 	}
 	
 	public void updateGravity(){
 		setyVel(getyVel()+g);
 	}
 	
 	// Getters and Setters:
 	
 	public boolean isGravityOn() {
 		return gravityOn;
 	}
 
 
 	public int getJumpVel() {
 		return jumpVel;
 	}
 
 
 	public void setJumpVel(int jumpVel) {
 		this.jumpVel = jumpVel;
 	}
 
 
 	public int getxAbsVel() {
 		return xAbsVel;
 	}
 
 
 	public void setxAbsVel(int xAbsVel) {
 		this.xAbsVel = xAbsVel;
 	}
 
 
 	public void setGravityOn(boolean gravityOn) {
 		this.gravityOn = gravityOn;
 		setyVel(0);
 	}
 
 
 	public int getxVel() {
 		return xVel;
 	}
 
 
 	public void setxVel(int xVel) {
 		this.xVel = xVel;
 	}
 
 
 	public int getyVel() {
 		return yVel;
 	}
 
 
 	public void setyVel(int yVel) {
 		this.yVel = yVel;
 	}
 
 
 	public boolean[] getEnableSkill() {
 		return enableSkill;
 	}
 
 
 	public void setEnableSkill(boolean[] useableSkill) {
		this.enableSkill = useableSkill.clone();
 	}
 
 
 	public boolean isInvincible(){
 		return invincible;
 	}
 	
 	public void setInvincible(boolean b) {
 		this.invincible=b;
 	}
 
 	public int getCoffee() {
 		return coffee;
 	}
 
 	public void setCoffee(int coffee) {
 		this.coffee = coffee;
 	}
 
 	/**
 	 * @return the dir
 	 */
 	public int getDir() {
 		return dir;
 	}
 
 	/**
 	 * @param dir the dir to set
 	 */
 	public void setDir(int dir) {
 		this.dir = dir;
 	}
 
 
 }
