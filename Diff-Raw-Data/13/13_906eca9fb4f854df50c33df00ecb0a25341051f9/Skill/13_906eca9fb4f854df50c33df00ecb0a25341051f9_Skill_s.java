 package Model.Skills;
 
 import Model.Player;
 import Model.StatusEffect;
 import Model.StatusEffectShell;
 import Model.Timers.EndStateAnimationTimer;
 import Model.Timers.RepeatingAnimationTimer;
 import Model.Timers.SkillCheckingTimer;
 
 import java.sql.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class Skill{
 
 	private String name;
 	private String smallName;
 	private int cooldown;
 	private int range;
 	private int areaOfEffect;
 	private int cost;
 	private int damage;
 	private int damageLvl1;
 	private int damageLvl2;
 	private int damageLvl3;
 	private int damageLvl4;
 	private int lvlOfSkill;
 	
 	private String describe;
 	private boolean affectSelf = false;
 	private boolean affectOthers = false;
 	
 	private StatusEffectShell offensiveSE = null;
 	private StatusEffectShell selfAffectingSE = null;
 	private StatusEffectShell selfAffectingOnHitSE = null;
 	
 	private Image attackImage;
 	private float attImgX;
 	private float attImgY;
 	private int imgWidth;
 	private int imgHeight;
 	private float rotation;
 	
 	private Image endStateImage;
 	private int endStateImgWidth;
 	private int endStateImgHeight;
 	
 	private int currentWidth;
 	private int currentHeight;
 	
 	//Attack variables
 	private float mouseXPos;
 	private float mouseYPos;
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
 	
 	SkillCheckingTimer ESIT;
 	
 	EndStateAnimationTimer animation;
 	RepeatingAnimationTimer projectileAnimation;
 	
 	private boolean isProjectile = true;
 
 	private long CDstartTime = 0;
 	private long CDelapsedTime = 0;
 	
 	private boolean isPiercing = false;
 	private int piercingDamage;
 	
 	private boolean isGuided = false;
 	private Player guidedTarget;
 	
 	private boolean isPassive = false;
 	private boolean affectSelfOnHit = false;
 	
 	public Skill(String name, int cd, int range, double speed, int aoe, int cost, int damageLvl1,int damageLvl2,
 			int damageLvl3,int damageLvl4, String describe){
 		this.name = name;
 		this.smallName = name.toLowerCase().replaceAll("\\s", "");
 		cooldown = cd;
 		this.range = range;
 		areaOfEffect = aoe;
 		this.cost = cost;
 		damage = this.damageLvl1 = damageLvl1;
 		lvlOfSkill = 1;
 		
 		attackRange = range;
 		if(speed < 100){
 			attSpeed = 3*speed;
 		}else{
 			isProjectile = false;
 		}
 		
 		
 		this.describe = describe;
 		this.affectSelf = affectSelf;
 		
 		//Backup image if it doesn't get one set by the extended skillClass
 		try {
 			attackImage = new Image("res/miscImages/awesomeGreenSquare.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		currentHeight = imgHeight = attackImage.getHeight();
 		currentWidth = imgWidth = attackImage.getWidth();
 		
 	}
 	
 	//Set new image for projectile
 	public void setImage(Image image){
 		if(image != null){
 			attackImage = image;
 		
 			currentHeight = imgHeight = image.getHeight();
 			currentWidth = imgWidth = image.getWidth();
 		}
 	}
 	
 	//Set new animation for projectile
 	public void setImage(Image[] images, int duration){
 		if(images[0] != null){
 			attackImage = images[0];
 		
 			currentHeight = imgHeight = images[0].getHeight();
 			currentWidth = imgWidth = images[0].getWidth();
 			
 			projectileAnimation = new RepeatingAnimationTimer(duration, images);
 		}
 	}
 	//public void setAnimationImages(Image[] images){
 	//	animation = new AnimationTimer(200, images, this);
 	//}
 	public void setEndStateImage(Image image){
 		if(image != null)
 			endStateImage = image;
 		
 		endStateImgHeight = image.getHeight();
 		endStateImgWidth = image.getWidth();
 	}
 	public float getRotation(){
 		return rotation;
 	}
 	public void setRotation(float angle){
 		attackImage.setRotation(angle);
 		rotation = angle;
 	}
 	public void setEndState(Image[] images, int duration, int interval){
 		if(images[0] != null){
 			endStateImage = images[0];
 		
 			endStateImgHeight = images[0].getHeight();
 			endStateImgWidth = images[0].getWidth();
 			
 			hasEndState = true;
 			endStateDuration = duration;
 			ESColInterval = interval;
 			
 			animation = new EndStateAnimationTimer(duration, images, this);
 		}
 	}
 	public void setOffensiveStatusEffectShell(StatusEffectShell SE){
 		affectOthers = true;
 		offensiveSE = SE;
 	}
 	public void setSelfAffectingStatusEffectShell(StatusEffectShell SE){
 		affectSelf = true;
 		selfAffectingSE = SE;
 	}
 	public void setSelfAffectingOnHitStatusEffectShell(StatusEffectShell SE){
 		affectSelfOnHit = true;
 		selfAffectingOnHitSE = SE;
 	}
 	public void setPassive(){
 		isPassive = true;
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
 	public String getSmallName() {
 		return smallName;
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
 	public int getDamageLvl1(){
 		return damageLvl1;
 	}
 	public int getDamageLvl2(){
 		return damageLvl2;
 	}
 	public int getDamageLvl3(){
 		return damageLvl3;
 	}
 	public int getDamageLvl4(){
 		return damageLvl4;
 	}
 	public void upgradeSkill(){
 		if(lvlOfSkill <= 4){
 			lvlOfSkill++;
 			
 			switch(damage){
 			case 2:
 				damage = damageLvl2;
 				break;
 			case 3:
 				damage = damageLvl3;
 				break;
 			case 4:
 				damage = damageLvl4;
 				break;
 			}
 		}
 		
 	}
 	public int getCurrentLvl(){
 		return lvlOfSkill;
 	}
 	public void setCurrentLvl(int lvl) {
 		lvlOfSkill = lvl;
 		switch(lvl) {
 		case 1:
 			damage = damageLvl1;
 			break;
 		case 2:
 			damage = damageLvl2;
 			break;
 		case 3:
 			damage = damageLvl3;
 			break;
 		case 4:
 			damage = damageLvl4;
 			break;
 		}
 	}
 	public String getDescription(){
 		return describe;
 	}
 	public boolean getAffectSelf(){
 		return affectSelf;
 	}
 	public boolean getAffectOthers(){
 		return affectOthers;
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
 		attImgX = player.getX()+player.getImage().getWidth()/2-currentWidth/2;
 		attImgY = player.getY()+player.getImage().getHeight()/2-currentHeight/2;
 	}
 	public void resetCooldown(){
 		CDstartTime = System.currentTimeMillis() - cooldown;
 	}
 	
 	public void setNonProjectileShot(){
 		animation.resetCounterAndTimer();
 		addAttX((float)(getXDirAtt()*getGenDirAtt())/*-endStateImgWidth/2*/);
 		addAttY((float)(getYDirAtt()*getGenDirAtt())/*-endStateImgHeight/2*/);
 	}
 	
 	public void collidedShot(){
 		attImgX = -1000;
 		attImgY = -1000;
 	}
 	
 	public void setMouseXPos(float x){
 		mouseXPos = x;
 	}
 	public void setMouseYPos(float y){
 		mouseYPos = y;
 	}
 	public float getMouseXPos(){
 		return mouseXPos;
 	}
 	public float getMouseYPos(){
 		return mouseYPos;
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
 	public void addAttackRange(int range){
 		attackRange += range;
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
 	//returns skillbarpicture depending on if it is the active skill or not, or in use
 	public Image getSkillBarImage(){
 		if(checkCooldown() == getCoolDown()){
 			if(!isChosen){
 				return skillBarImages[0];
 			}else{
 				return skillBarImages[1];
 			}
 		}else{
 			return skillBarImages[2];
 		}
 	}
 	
 	public void setAttackingState(boolean state){
 		isAttacking = state;
 		if(state == false){
 			collidedShot();
 			if(offensiveSE != null){
 				offensiveSE.resetCloning();
 			}
 		}
 	}
 	
 	public void activateSkill(){
     	CDstartTime = System.currentTimeMillis();
     	CDelapsedTime = 0;
     }
     
     public long checkCooldown(){
     	//TODO make own class for this and remake to make it easier to understand
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
	//	addAttX(-currentWidth/2);
 		isEndState = true;
     	animation.resetCounterAndTimer();
 	}
 	public void activateCollisionEndState(){
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
 		
 		if(offensiveSE != null)
 			offensiveSE.resetCloning();
 	}
 	public boolean isEndState(){
 		return isEndState;
 	}
 	public int getESColInterval(){
 		return ESColInterval;
 	}
 	public void activateESIT(Player player){
 		ESIT = new SkillCheckingTimer(getESColInterval(), player, this);
 	}
 	public SkillCheckingTimer getESIT(){
 		return ESIT;
 	}
 	
 	public RepeatingAnimationTimer getProjectileAnimationTimer(){
 		return projectileAnimation;
 	}
 	public EndStateAnimationTimer getAnimationTimer(){
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
 	
 	public boolean isGuided(){
 		return isGuided;
 	}
 	public void setGuided(){
 		isGuided = true;
 	}
 	public void setGuidedTarget(Player player){
 		guidedTarget = player;
 	}
 	public Player getGuidedTarget(){
 		return guidedTarget;
 	}
 	public boolean isPassive(){
 		return isPassive;
 	}
 	public boolean getAffectSelfOnHit(){
 		return affectSelfOnHit;
 	}
 	
 	//Methods for StatusEffect Control
 	public StatusEffectShell getOffensiveStatusEffect(){
 		return offensiveSE;
 	}
 	public StatusEffectShell getSelfAffectingStatusEffect(){
 		return selfAffectingSE;
 	}
 	public StatusEffectShell getSelfAffectingOnHitStatusEffect(){
 		return selfAffectingOnHitSE;
 	}
 	
 }
