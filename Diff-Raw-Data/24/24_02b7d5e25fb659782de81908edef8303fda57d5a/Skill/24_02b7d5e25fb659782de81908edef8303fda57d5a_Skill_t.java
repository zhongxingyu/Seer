 package Model.Skills;
 
 import Model.Player;
 import Model.StatusEffect;
 
 import java.sql.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class Skill{
 
 	private String name;
 	private int cooldown;
 	private int range;
 	private int areaOfEffect;
 	private int cost;
 	private int damage;
 	private StatusEffect spellEffect = new StatusEffect();
 	
 	private Image attackImage;
 	private float attImgX;
 	private float attImgY;
 	
 	//Attack variables
 	private float mouseXPosAtt;
 	private float mouseYPosAtt;
 	private double attSpeed;
 	
 	private int attCounter=0;
 	private float xDirAtt;
 	private float yDirAtt;
 	private float genDirAtt;
 
 	private float attackRange;
 	private boolean isAttacking = false;
 	
 	private boolean isColliding = false;
 	
 
 	private long startTime = 0;
 	private long elapsedTime = 0;
 	
 	public Skill(String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE){
 		this.name = name;
 		cooldown = cd;
 		this.range = range;
 		areaOfEffect = aoe;
 		this.cost = cost;
 		this.damage = damage;
 		spellEffect = SE;
 		attackRange = range;
 		attSpeed = speed;
 		
 		try {
 			attackImage = new Image("res/awesomeGreenSquare.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	 
 	
     
     public void activateSkill(){
     	startTime = System.currentTimeMillis();
     	elapsedTime = 0;
 
     }
     
     public long checkCooldown(){
     	elapsedTime = System.currentTimeMillis() - startTime;
     	if(elapsedTime >= cooldown){
     		elapsedTime = 0;
     	}
     	return (cooldown - elapsedTime)/1000;
     }
 	
 	public String getName(){
 		return name;
 	}
 	public int getCoolDown(){
 		return cooldown/1000;
 	}
 	public int getRange(){
 		return range;
 	}
 	public int getAOE(){
 		return areaOfEffect;
 	}
 	public int getCost(){
 		return cost;
 	}
 	public int getDamage(){
 		return damage;
 	}
 	
 	public Image getAttImage(){
 		return attackImage;
 	}
 	public float getAttX(){
 		return attImgX;
 	}
 	public float getAttY(){
 		return attImgY;
 	}
 	
 	
 	
 	public void addAttX(float x){
 		attImgX += x;
 	}
 	public void addAttY(float y){
 		attImgY += y;
 	}
 	public void setAttX(float x){
 		attImgX = x;
 	}
 	public void setAttY(float y){
 		attImgY = y;
 	}
 	
 	public void resetShot(Player player){
 		attImgX = player.getX();
 		attImgY = player.getY();
 	}
 	
	public void collidedShot(){
		attImgX = -1000;
		attImgY = -1000;
	}
	
 	
 	public float getMouseXPosAtt(){
 		return mouseXPosAtt;
 	}
 	public float getMouseYPosAtt(){
 		return mouseYPosAtt;
 	}
 	public double getAttSpeed(){
 		return attSpeed;
 	}
 	public int getAttCounter(){
 		return attCounter;
 	}
 	public void incAttCounter(){
 		attCounter++;
 	}
 	public void resetAttCounter(){
 		attCounter = 0;
 	}
 	public float getXDirAtt(){
 		return xDirAtt;
 	}
 	public float getYDirAtt(){
 		return yDirAtt;
 	}
 	public float getGenDirAtt(){
 		return genDirAtt;
 	}
 	public void setXDirAtt(float dir){
 		xDirAtt = dir;
 	}
 	public void setYDirAtt(float dir){
 		yDirAtt = dir;
 	}
 	public void setGenDirAtt(float dir){
 		genDirAtt = dir;
 	}
 	
 	public float getAttackRange(){
 		return attackRange;
 	}
 	
 	
 	public boolean isAttacking(){
 		return isAttacking;
 	}
 	
 	public void setAttackingState(boolean state){
 		isAttacking = state;
 	}
 	
 	public void setCollidingState(boolean state){
 		isColliding = state;
 	}
 	public boolean isColliding(){
 		return isColliding;
 	}
 
 }
