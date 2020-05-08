 package Model;
 
 import java.awt.List;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skills.*;
 import Model.Timers.RepeatingAnimationTimer;
 
 public class Player {
 	
 	private Image userImage;
 	private Image noStepImage;
 	
 	private RepeatingAnimationTimer changedModelAnimation;
 	private RepeatingAnimationTimer regularModelAnimation;
 	private RepeatingAnimationTimer noStepChangedModelAnimation;
 	
 	
 	
 	private boolean changeModel = false;
 	
 	private float imgX;
 	private float imgY;
 	private float startingPosX;
 	private float startingPosY;
 	private boolean isReady;
 	private String mode;
 	private String ctrlType;
 	private boolean connected;
 	
 	private int playerListIndex;
 	private boolean isAlive = true;
 	private boolean nameTaken = false;
 	
 	private int HP;
 	private int maxHP;
 	private String name;
 	private String classType;
 	private double baseArmor = 0;
 	private double armor=0;
 	private int baseEvasion = 0;
 	private int evasion=0;
 	private int kills=0;
 	private int deaths = 0;
 	private int gold=500;
 	private Skill[] skillList = new Skill[5];
 	private ArrayList<Skill> ownedSkills = new ArrayList<Skill>();
 	
 	private ArrayList<Skill> passiveSkills = new ArrayList<Skill>();
 	private ArrayList<StatusEffect> statusEffectList = new ArrayList<StatusEffect>();
 	
 	//Movement variables
 	private float mouseXPosMove;
 	private float mouseYPosMove;
 	private double baseMoveSpeed = 1;
 	private double moveSpeed = 1;
 	
 	float rotation=0;
 	
 	private int moveCounter=0;
 	private float xDirMove;
 	private float yDirMove;
 	private float genDirMove;
 	private Double findNaN;
 
 	private boolean isChanneling = false;
 	private boolean isStunned = false;
 	private boolean isPushed = false;
 	private double pushSpeed = 1.5;
 	
 	private boolean isRunning = false;
 	
 	public Player(String name, String ctrlType, String type, float x, float y, int maxHP, double speed, double armor, int index){
 		this.name = name;
 		this.classType = type;
 		this.isReady = false;
 		this.mode = "lobby";
 		this.ctrlType = ctrlType;
 		this.connected = false;
 		
 		imgX = startingPosX = x;
 		imgY = startingPosY = y;
 		
 		HP = this.maxHP = maxHP;
 		baseMoveSpeed = moveSpeed = speed;
 		baseArmor = this.armor = armor;
 		
 		playerListIndex = index;
 		
 		statusEffectList = new ArrayList<StatusEffect>();
 		passiveSkills = new ArrayList<Skill>();
 	}
 	
 	public void setIndex(int index){
 		playerListIndex = index;
 	}
 
 	public String getType(){
 		return classType;
 	}
 	
 	public boolean isAlive(){
 		return isAlive;
 	}
 	public void setAliveState(boolean state){
 		isAlive = state;
 	}
 	public int getPlayerListIndex(){
 		return playerListIndex;
 	}
 	public void setPlayerListIndex(int index){
 		this.playerListIndex = index;
 	}
 	
 	//Getters for the movements
 	
 	public Image getImage(){
 		return userImage;
 	}
 
 	public float getX(){
 		return imgX;
 	}
 	public float getY(){
 		return imgY;
 	}
 	public float getStartX(){
 		return startingPosX;
 	}
 	public float getStartY(){
 		return startingPosY;
 	}
 	
 	public void addX(float x){
 		imgX += x;
 	}
 	public void addY(float y){
 		imgY += y;
 	}
 	public void setX(float x){
 		imgX = x;
 	}
 	public void setY(float y){
 		imgY = y;
 	}
 	
 	public void setMouseXPosMove(float x){
 		mouseXPosMove = x;
 	}
 	public void setMouseYPosMove(float y){
 		mouseYPosMove = y;
 	}
 	
 	public float getMouseXPosMove(){
 		return mouseXPosMove;
 	}
 	public float getMouseYPosMove(){
 		return mouseYPosMove;
 	}
 	public double getMoveSpeed(){
 		return moveSpeed;
 	}
 	public int getMoveCounter(){
 		return moveCounter;
 	}
 	public void incMoveCounter(){
 		moveCounter++;
 	}
 	public void resetMoveCounter(){
 		moveCounter = 0;
 	}
 	
 	public float getXDirMove(){
 		return xDirMove;
 	}
 	public float getYDirMove(){
 		return yDirMove;
 	}
 	public float getGenDirMove(){
 		return genDirMove;
 	}
 	public void setXDirMove(float dir){
 		xDirMove = dir;
 	}
 	public void setYDirMove(float dir){
 		yDirMove = dir;
 	}
 	public void setGenDirMove(float dir){
 		genDirMove = dir;
 	}
 	
 	
 	public double getFindNaN(){
 		return findNaN;
 	}
 	//Getters for the attacks
 	public boolean isRunning(){
 		return isRunning;
 	}
 	public void setRunningState(boolean state){
 		isRunning = state;
 	}
 	public float getRotation(){
 		return rotation;
 	}
 	
 	public void setRotation(float angle){
 		userImage.setRotation(angle);
 		rotation = angle;
 	}
 	
 	//Getters and setters for stats
 	public int getHP(){
 		return HP;
 	}
 	public int getMaxHP(){
 		return maxHP;
 	}
 	public int getGold(){
 		return gold;
 	}
 	public void setGold(int gold){
 		this.gold=gold;
 	}
 	public void setHP(int HP) {
 		this.HP = HP;
 	}
 	public void addHP(int addAmount){
 		this.HP += addAmount;
 		if(HP>maxHP){
 			HP = maxHP;
 		}
 	}
 	public void setMaxHP(int maxHP) {
 		this.maxHP = maxHP;
 	}
 	public void dealDamage(int damage){
 		//Remove some damage given depending on armor
 		damage *= (1-armor);
 		if(damage >= 0){
 			HP -= damage;
			if(damage != 0){
				playSound("res/sounds/takingDamage.wav");
			}
 		}
 	//	System.out.println("DAMAGE DEALT!");
 	}
 	public void resetHP(){
 		HP = maxHP;
 		isAlive = true;
 	//	System.out.println("HEALTH RESTORED");
 	}
 	public String getName(){
 		return name;
 	}
 	public double getArmor(){
 		return armor;
 	}
 	public int getEvasion(){
 		return evasion;
 	}
 	public void addEvasion(int evasion){
 		this.evasion += evasion;
 	}
 	public void addArmor(double armor){
 		this.armor += armor;
 	}
 	public void addMovementSpeed(double speed){
 		this.moveSpeed += speed;
 	}
 	public void setMovementSpeed(double speed) {
 		this.moveSpeed = speed;
 	}
 	public int getKills(){
 		return kills;
 	}
 	public void incKills(){
 		kills++;
 	}
 	public void setKills(int kills) {
 		this.kills = kills;
 	}
 
 	public void addGold(int gold) {
 		this.gold += gold;
 	}
 
 	public void setImages(Image image, Image first, Image second){
 		userImage = noStepImage = image;
 		Image[] tempImages = new Image[2];
 		tempImages[0] = first;
 		tempImages[1] = second;
 		regularModelAnimation = new RepeatingAnimationTimer(500, tempImages);
 	}
 	
 	public void setChangedModelImages(Image[] walkingImages, Image[] standingImages){
 		changedModelAnimation = new RepeatingAnimationTimer(2000, walkingImages);
 		noStepChangedModelAnimation = new RepeatingAnimationTimer(2000, standingImages);
 	}
 	
 	public void changeUserImage(){
 		if(isRunning && !isStunned  && moveSpeed > 0){
 			if(changeModel){
 				userImage = changedModelAnimation.getCurrentAnimationImage();
 			}else{
 				userImage = regularModelAnimation.getCurrentAnimationImage();
 			}
 		} else {
 			if(changeModel){
 				userImage = noStepChangedModelAnimation.getCurrentAnimationImage();
 			}else{
 				userImage = noStepImage;
 			}
 		}
 		setRotation(rotation);
 	}
 
 	public void setSkillList(Skill[] chosenSkills) {
 		if(chosenSkills != null){
 			skillList[0] = chosenSkills[0];
 			skillList[1] = chosenSkills[1];
 			skillList[2] = chosenSkills[2];
 			skillList[3] = chosenSkills[3];
 			skillList[4] = chosenSkills[4];
 		}
 	}
 	public void setSkill(Skill skill, int index){
 		if(index <= 4 && index >= 0){
 			skillList[index] = skill;
 		}	
 	}
 	public void setPassiveSkillList(ArrayList<Skill> passiveSkills) {
 		this.passiveSkills = passiveSkills;
 	}
 	
 	public Skill[] getSkillList(){
 		return skillList;
 	}
 	public ArrayList<Skill> getOwnedSkills(){
 		return ownedSkills;
 	}
 	public void addSkillAsOwned(Skill skill){
 		ownedSkills.add(skill);
 	}
 	public ArrayList<Skill> getPassiveSkills(){
 		return passiveSkills;
 	}
 	public void addPassiveSkill(Skill skill){
 		passiveSkills.add(skill);
 	}
 	public void removePassiveSkill(Skill skill){
 		passiveSkills.remove(skill);
 	}
 	public void activatePassiveEffects(){
 		evasion = baseEvasion;
 		armor = baseArmor;
 		moveSpeed = baseMoveSpeed;
 		for(int i=0; i<passiveSkills.size(); i++){
 			StatusEffectShell SE = passiveSkills.get(i).getSelfAffectingStatusEffect();
 			armor += SE.getArmEff();
 			evasion += SE.getEvasionEff();
 			moveSpeed += SE.getMoveSpeedEff();
 		}
 	}
 	public ArrayList<StatusEffect> getStatusEffects(){
 		return statusEffectList;
 	}
 	public void addStatusEffect(StatusEffect SE){
 		if(SE.getChangeModel()){
 			changeModel = true;
 		}
 		statusEffectList.add(SE);
 	}
 	public void removeStatusEffect(StatusEffect SE){
 		if(SE.getChangeModel()){
 			changeModel = false;
 		}
 		statusEffectList.remove(SE);
 	}
 	public void resetStatusEffects(){
 		for(int j=0; j<statusEffectList.size(); j++){
 			statusEffectList.get(j).setResetOfStatusEffect();
 		}
 		changeModel = false;
 		statusEffectList = new ArrayList<StatusEffect>();
 	}
 	public boolean isStunned(){
 		return isStunned;
 	}
 	public void setStunState(boolean state){
 		isStunned = state;
 	}
 	public boolean isPushed(){
 		return isPushed;
 	}
 	public void setPushState(boolean state){
 		isPushed = state;
 	}
 	public double getPushSpeed(){
 		return pushSpeed;
 	}
 	
 	public String getMode() {
 		return mode;
 	}
 	
 	public void setMode(String mode) {
 		this.mode = mode;
 	}
 	public boolean getChannel(){
 		return isChanneling;
 	}
 	public void setChannel(boolean channelState){
 		this.isChanneling = channelState;
 	}
 	public boolean isReady() {
 		return isReady;
 	}
 	public void setReadyness(boolean ready) {
 		isReady = ready;
 	}
 	public String getControlType() {
 		return ctrlType;
 	}
 	public void setControlType(String ctrlType) {
 		this.ctrlType = ctrlType;
 	}
 	public boolean isConnected() {
 		return this.connected;
 	}
 	public void setConnected(boolean connected) {
 		this.connected = connected;
 	}
 	public boolean nameTaken() {
 		return nameTaken;
 	}
 	public void takeName(boolean nameTaken) {
 		this.nameTaken = nameTaken;
 	}
 	//Handling the soundfiles
 	public static synchronized void playSound(String filename) {
 		try{
 			Clip clip = AudioSystem.getClip();
 			clip.open(AudioSystem.getAudioInputStream(new File(filename)));
 			clip.start();
 		}catch (Exception exc){
 				exc.printStackTrace(System.out);
 		}	
 	}
 }
