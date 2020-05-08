 package Model.Skills;
 
 import Control.AnimationTimer;
 import Control.EndStateIntervalTimer;
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
 	private int imgWidth;
 	private int imgHeight;
 	
 	private Image endStateImage;
 	private int endStateImgWidth;
 	private int endStateImgHeight;
 	
 	private int currentWidth;
 	private int currentHeight;
 	
 	//Attack variables
 	private float mouseXPosAtt;
 	private float mouseYPosAtt;
 	private double attSpeed;
 	
 	private int attCounter=0;
 	private float xDirAtt;
 	private float yDirAtt;
 	private float genDirAtt;
 	
 	private boolean isChosen = false;
 	private Image[] skillBarImages;
 
 	private float attackRange;
 	private boolean isAttacking = false;
 	
 	private boolean hasEndState = false;
 	private long endStateStartTime = 0;
 	private long endStateElapsedTime = 0;
 	private int endStateDuration;
 	private boolean isEndState = false;
 	private int ESColInterval;
 	
 	EndStateIntervalTimer ESIT;
 	
 	AnimationTimer animation;
 //	Image[] animationImages;
 	
 	private boolean isProjectile = true;
 
 	private long CDstartTime = 0;
 	private long CDelapsedTime = 0;
 	
 	private boolean isPiercing = false;
 	private int piercingDamage;
 	
 	public Skill(String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE){
 		this.name = name;
 		cooldown = cd;
 		this.range = range;
 		areaOfEffect = aoe;
 		this.cost = cost;
 		this.damage = damage;
 		spellEffect = SE;
 		attackRange = range;
 		if(speed < 100){
 			attSpeed = 3*speed;
 		}else{
 			isProjectile = false;
 		}
 		
 		//Backup image if it doesn't get one set by the extended skillClass
 		try {
 			attackImage = new Image("res/awesomeGreenSquare.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		setImage(attackImage, 5, 4);
 		
 		
 	}
 	
 	public void setImage(Image image, int height, int width){
 		if(image != null)
 			attackImage = image;
 		
 		currentHeight = imgHeight = height;
 		currentWidth = imgWidth = width;
 	}
 	public void setAnimationImages(Image[] images){
 		animation = new AnimationTimer(200, images, this);
 	}
 	public void setEndStateImage(Image image){
 		if(image != null)
 			endStateImage = image;
 		
 		endStateImgHeight = image.getHeight();
 		endStateImgWidth = image.getWidth();
 	}
 	public void setEndState(Image[] images, int duration, int interval){
 		if(images[0] != null){
 			endStateImage = images[0];
 		
 			endStateImgHeight = images[0].getHeight();
 			endStateImgWidth = images[0].getWidth();
 			
 			hasEndState = true;
 			endStateDuration = duration;
 			ESColInterval = interval;
 			
 			animation = new AnimationTimer(duration/(images.length), images, this);
 		}
 	}
 	
 	
 	public int getCurrentHeight(){
 		return currentHeight;
 	}
 	public int getCurrentWidth(){
 		return currentWidth;
 	}
 	public int getEndStateImgHeight(){
 		return endStateImgHeight;
 	}
 	public int getEndStateImgWidth(){
 		return endStateImgWidth;
 	}
 	public Image getEndStateImage(){
 		return endStateImage;
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
 		attImgX = player.getX()+player.getFirstStepImage().getWidth()/2-currentWidth/2;
 		attImgY = player.getY()+player.getFirstStepImage().getHeight()/2-currentHeight/2;
 	}
 	public void setNonProjectileShot(){
 		addAttX((float)(getXDirAtt()*getGenDirAtt()));
 		addAttY((float)(getYDirAtt()*getGenDirAtt()));
 	}
 	
 	public void collidedShot(){
 		attImgX = -1000;
 		attImgY = -1000;
 	}
 	
 	public void setMouseXPos(int x){
 		mouseXPosAtt = x;
 	}
 	public void setMouseYPos(int y){
 		mouseYPosAtt = y;
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
 		if(isAttacking){
 			
 		}
 		return isAttacking;
 	}
 	
 	//returns true if player has this skill as current active skill
 	public boolean isChosen(){
 		return isChosen;
 	}
 	public void setChosenState(boolean state){
 		isChosen = state;
 	}
 	public void setSkillBarImages(Image[] images){
 		skillBarImages = images;
 	}
 	//returns skillbarpicture depending on if it is the active skill or not
 	public Image getSkillBarImage(){
 		if(!isChosen){
 			return skillBarImages[0];
 		}else{
 			return skillBarImages[1];
 		}
 	}
 	
 	public void setAttackingState(boolean state){
 		isAttacking = state;
		if(state == false){
			collidedShot();
		}
 	}
 	
 	public void activateSkill(){
     	CDstartTime = System.currentTimeMillis();
     	CDelapsedTime = 0;
     }
     
     public long checkCooldown(){
     	CDelapsedTime = System.currentTimeMillis() - CDstartTime;
     	if(CDelapsedTime >= cooldown){
     		CDelapsedTime = 0;
     	}
     	return (cooldown - CDelapsedTime)/1000;
     }
 	public boolean hasEndState(){
 		return hasEndState;
 	}
 	public void activateEndState(){
 		endStateStartTime = System.currentTimeMillis();
 		endStateElapsedTime = 0;
 		currentHeight = endStateImgHeight;
 		currentWidth = endStateImgWidth;
 	//	attImgX -= endStateImgWidth/2;
 	//	attImgY -= endStateImgHeight/2;
 		isEndState = true;
 		
 	//	if(animation != null){
     	animation.resetCounterAndTimer();
     //	}
 	}
 	public void activatePreEndState(){
 		//Setting direction to 0 so it will count as reaching it's goal to begin End State
 		setXDirAtt(0);
 		setYDirAtt(0);
 		
 		setGenDirAtt(0);
 	}
 	public long checkEndStateTimer(){
 		endStateElapsedTime = System.currentTimeMillis() - endStateStartTime;
 		if(endStateElapsedTime >= endStateDuration){
 			endStateElapsedTime = 0;
 		}
 		return (endStateDuration - endStateElapsedTime);
 	}
 	public int getEndStateDuration(){
 		return endStateDuration;
 	}
 	public void finishEndState(){
 		currentHeight = imgHeight;
 		currentWidth = imgWidth;
 		
 		isEndState = false;
 	}
 	public boolean isEndState(){
 		return isEndState;
 	}
 	public int getESColInterval(){
 		return ESColInterval;
 	}
 	public void activateESIT(Player player){
 		ESIT = new EndStateIntervalTimer(getESColInterval(), player, this);
 	}
 	public EndStateIntervalTimer getESIT(){
 		return ESIT;
 	}
 	
 	public AnimationTimer getAnimationTimer(){
 		return animation;
 	}
 	
 	public boolean isPiercing(){
 		return isPiercing;
 	}
 	public int getPiercingDamage(){
 		return piercingDamage;
 	}
 	public boolean isProjectile(){
 		return isProjectile;
 	}
 	
 }
