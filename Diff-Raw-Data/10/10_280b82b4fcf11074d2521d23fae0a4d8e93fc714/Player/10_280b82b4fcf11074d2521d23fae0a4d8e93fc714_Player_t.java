 package Model;
 
 
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 import org.newdawn.slick.Image;
 
 import Model.Items.Item;
 import Model.Timers.RepeatingAnimationTimer;
 
 public class Player {
 	
 	private Image userImage;
 	private Image noStepImage;
 	private Image portraitImage;
 	private Image portraitImageMini;
 	private Image framedImage;
 	
 	private RepeatingAnimationTimer changedModelAnimation;
 	private RepeatingAnimationTimer regularModelAnimation;
 	private RepeatingAnimationTimer noStepChangedModelAnimation;
 	
 	
 	
 	private boolean changeModel = false;
 	
 	private float imgX;
 	private float imgY;
 	private float startingPosX;
 	private float startingPosY;
 	private boolean isReady;
 	private boolean hasClickedStartGame;
 	private String mode;
 	private String ctrlType;
 	private boolean connected;
 	
 	private int playerListIndex;
 	private int currentActiveSkillIndex;
 	private boolean isAlive = true;
 	private boolean nameTaken = false;
 	
 	private int HP;
 	private int maxHP;
 	private String name;
 	private String classType;
 	private double armor=0;
 	private int evasion=0;
 	private int totalKills=0;
 	private int killsThisRound=0;
 	private int totalDamageDone=0;
 	private int roundDamageDone=0;
 	private int deaths = 0;
 	private int gold=500;
 	private Skill[] skillList = new Skill[5];
 	private ArrayList<Skill> ownedSkills = new ArrayList<Skill>();
 	private ArrayList<Item> ownedItems = new ArrayList<Item>();
 	
 	private ArrayList<Item> passiveItems = new ArrayList<Item>();
 	private ArrayList<Skill> passiveSkills = new ArrayList<Skill>();
 	private ArrayList<StatusEffect> statusEffectList = new ArrayList<StatusEffect>();
 	
 	//Movement variables
 	private float mouseXPosMove;
 	private float mouseYPosMove;
 	private double moveSpeed = 1;
 	
 	float rotation=0;
 	
 	private int moveCounter=0;
 	private float xDirMove;
 	private float yDirMove;
 	private float genDirMove;
	// Just made it "not nothing"
	private Double findNaN = 0.0;
 
 	private boolean isChanneling = false;
 	private boolean isStunned = false;
 	private boolean isPushed = false;
 	private double pushSpeed = 1.5;
 	
 	private boolean isStealthed = false;
 	
 	private boolean isRunning = false;
 	private boolean canAttack = true;
 	private boolean canWalk = true;
 	
 	public Player(String name, String ctrlType, String type, float x, float y, int maxHP, double speed, double armor, int index){
 		this.name = name;
 		this.classType = type;
 		this.isReady = false;
 		this.hasClickedStartGame = false;
 		this.mode = "lobby";
 		this.ctrlType = ctrlType;
 		this.connected = false;
 		
 		imgX = startingPosX = x;
 		imgY = startingPosY = y;
 		
 		HP = this.maxHP = maxHP;
 		moveSpeed = speed;
 		this.armor = armor;
 		
 		playerListIndex = index;
 		
 		statusEffectList = new ArrayList<StatusEffect>();
 		passiveSkills = new ArrayList<Skill>();
 	}
 	
 	// Getters
 	public int getPlayerListIndex(){
 		return playerListIndex;
 	}
 	public String getControlType() {
 		return ctrlType;
 	}
 	public boolean getNameTaken() {
 		return nameTaken;
 	}
 	public Image getImage(){
 		return userImage;
 	}
 	public float getX(){
 		return imgX;
 	}
 	public float getY(){
 		return imgY;
 	}
 	public float getRotation(){
 		return rotation;
 	}
 	public boolean getRunningState(){
 		return isRunning;
 	}
 	public float getStartX(){
 		return startingPosX;
 	}
 	public float getStartY(){
 		return startingPosY;
 	}
 	public int getMoveCounter(){
 		return moveCounter;
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
 	public float getMouseXPosMove(){
 		return mouseXPosMove;
 	}
 	public float getMouseYPosMove(){
 		return mouseYPosMove;
 	}
 	public double getMovementSpeed(){
 		return moveSpeed;
 	}
 	public boolean getCanWalkState(){
 		boolean tempWalk = canWalk;
 		canWalk = false;
 		return tempWalk;
 	}
 	public double getFindNaN(){
 		return findNaN;
 	}
 	public String getType(){
 		return classType;
 	}
 	public int getHP(){
 		return HP;
 	}
 	public int getMaxHP(){
 		return maxHP;
 	}
 	public int getGold(){
 		return gold;
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
 	public int getKillsThisRound(){
 		return killsThisRound;
 	}
 	public int getRoundDamageDone(){
 		return roundDamageDone;
 	}
 	public int getTotalDamageDone(){
 		return totalDamageDone;
 	}
 	public int getDeaths(){
 		return deaths;
 	}
 	public int getTotalKills(){
 		return totalKills;
 	}
 	public Image getFramedImage(){
 		return framedImage;
 	}
 	public Image getPortraitImage(){
 		return portraitImage;
 	}
 	public Image getPortraitImageMini(){
 		return portraitImageMini;
 	}
 	public ArrayList<Item> getOwnedItems(){
 		return ownedItems;
 	}
 	public ArrayList<Item> getPassiveItems(){
 		return passiveItems;
 	}
 	public ArrayList<Skill> getPassiveSkills(){
 		return passiveSkills;
 	}
 	public Skill[] getSkillList(){
 		return skillList;
 	}
 	public ArrayList<Skill> getOwnedSkills(){
 		return ownedSkills;
 	}
 	public int getCurrentActiveSkillIndex(){
 		return currentActiveSkillIndex;
 	}
 	public ArrayList<StatusEffect> getStatusEffects(){
 		return statusEffectList;
 	}
 	public boolean getStunState(){
 		return isStunned;
 	}
 	public boolean getPushState(){
 		return isPushed;
 	}
 	public double getPushSpeed(){
 		return pushSpeed;
 	}
 	public boolean getChannel(){
 		return isChanneling;
 	}
 	public boolean getStealthState(){
 		return isStealthed;
 	}
 	public boolean getCanAttackState(){
 		return canAttack;
 	}
 	public boolean getAliveState(){
 		return isAlive;
 	}
 	public String getMode() {
 		return mode;
 	}
 	public boolean getConnected() {
 		return this.connected;
 	}
 	public boolean getReadyState() {
 		return isReady;
 	}
 	public boolean getHasClickedStartGame(){
 		return hasClickedStartGame;
 	}
 	
 	// Setters
 	public void setPlayerListIndex(int index){
 		this.playerListIndex = index;
 	}
 	public void setControlType(String ctrlType) {
 		this.ctrlType = ctrlType;
 	}
 	public void setNameTaken(boolean nameTaken) {
 		this.nameTaken = nameTaken;
 	}
 	public void setImages(Image image, Image first, Image second){
 		userImage = noStepImage = image;
 		Image[] tempImages = new Image[2];
 		tempImages[0] = first;
 		tempImages[1] = second;
 		regularModelAnimation = new RepeatingAnimationTimer(500, tempImages);
 	}
 	public void setX(float x){
 		imgX = x;
 	}
 	public void setY(float y){
 		imgY = y;
 	}
 	public void addX(float x){
 		imgX += x;
 	}
 	public void addY(float y){
 		imgY += y;
 	}
 	public void setRunningState(boolean state){
 		isRunning = state;
 	}
 	public void setRotation(float angle){
 		userImage.setRotation(angle);
 		rotation = angle;
 	}
 	public void resetMoveCounter(){
 		moveCounter = 0;
 	}
 	public void incMoveCounter(){
 		moveCounter++;
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
 	public void setMouseXPosMove(float x){
 		mouseXPosMove = x;
 	}
 	public void setMouseYPosMove(float y){
 		mouseYPosMove = y;
 	}
 	public void setMovementSpeed(double speed) {
 		this.moveSpeed = speed;
 	}
 	public void addMovementSpeed(double speed){
 		this.moveSpeed += speed;
 	}
 	public void setCanWalkState(boolean state){
 		canWalk = state;
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
 	public void resetHP(){
 		HP = maxHP;
 		isAlive = true;
 	}
 	public void setMaxHP(int maxHP) {
 		this.maxHP = maxHP;
 	}
 	public void setGold(int gold){
 		this.gold=gold;
 	}
 	public void addGold(int gold) {
 		this.gold += gold;
 	}
 	public void setArmor(double armor){
 		this.armor = armor;
 	}
 	public void addArmor(double armor){
 		this.armor += armor;
 	}
 	public void setEvasion(int evasion){
 		this.evasion = evasion;
 	}
 	public void addEvasion(int evasion){
 		this.evasion += evasion;
 	}
 	public void setKillsThisRound(int kills){
 		killsThisRound=kills;
 	}
 	public void incKillsThisRound(){
 		killsThisRound++;
 	}
 	public void setRoundDamageDone(int damage){
 		roundDamageDone=damage;
 	}
 	public void incRoundDamageDone(int damageAmount){
 		roundDamageDone=roundDamageDone+damageAmount;
 	}
 	public void incTotalDamageDone(int damageAmount){
 		totalDamageDone=totalDamageDone+damageAmount;
 	}
 	public void incDeaths(){
 		deaths++;
 	}
 	public void setTotalKills(int kills) {
 		totalKills = kills;
 	}
 	public void incTotalKills(int incAmount){
 		totalKills=totalKills+incAmount;
 	}
 
 	public void addTotalDamageDone(int newDamage){
 		this.totalDamageDone += newDamage;
 	}
 	public void setPortraits(Image frameImage, Image image,Image miniImage){
 		portraitImage= image;
 		portraitImageMini= miniImage;
 		framedImage = frameImage;
 	}
 	public void addItemOwned(Item item){
 		ownedItems.add(item);
 	}
 	public void addPassiveItem(Item item){
 		passiveItems.add(item);
 		StatusEffectShell SE = item.getSelfAffectingStatusEffect();
 		SE.setArmor(armor*SE.getArmEff());
 		armor += SE.getArmEff();
 		evasion += SE.getEvasionEff();
 		moveSpeed += SE.getMoveSpeedEff();
 	}
 	public void removePassiveItem(Item item){
 		passiveItems.remove(item);
 		StatusEffectShell SE = item.getSelfAffectingStatusEffect();
 		armor -= SE.getArmEff();
 		evasion -= SE.getEvasionEff();
 		moveSpeed -= SE.getMoveSpeedEff();
 	}
 	public void setPassiveSkillList(ArrayList<Skill> passiveSkills) {
 		this.passiveSkills = passiveSkills;
 	}
 	public void addPassiveSkill(Skill skill){
 		passiveSkills.add(skill);
 		StatusEffectShell SE = skill.getSelfAffectingStatusEffect();
 		SE.setArmor(armor*SE.getArmEff());
 		armor += SE.getArmEff();
 		evasion += SE.getEvasionEff();
 		moveSpeed += SE.getMoveSpeedEff();
 	}
 	public void removePassiveSkill(Skill skill){
 		passiveSkills.remove(skill);
 		StatusEffectShell SE = skill.getSelfAffectingStatusEffect();
 		armor -= SE.getArmEff();
 		evasion -= SE.getEvasionEff();
 		moveSpeed -= SE.getMoveSpeedEff();
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
 	public void addSkillAsOwned(Skill skill){
 		ownedSkills.add(skill);
 	}
 	public void setCurrentActiveSkillIndex(int index){
 		currentActiveSkillIndex = index;
 	}
 	public void addStatusEffect(StatusEffect SE){
 		
 		statusEffectList.add(SE);
 	}
 	public void removeStatusEffect(StatusEffect SE){
 		
 		statusEffectList.remove(SE);
 	}
 	public void resetStatusEffects(){
 		for(int j=0; j<statusEffectList.size(); j++){
 			statusEffectList.get(j).setResetOfStatusEffect();
 		}
 		changeModel = false;
 		statusEffectList = new ArrayList<StatusEffect>();
 	}
 	public void setStunState(boolean state){
 		isStunned = state;
 	}
 	public void setPushState(boolean state){
 		isPushed = state;
 	}
 	public void setChannel(boolean channelState){
 		this.isChanneling = channelState;
 	}
 	public void setStealthState(boolean state){
 		isStealthed = state;
 	}
 	public void setCanAttackState(boolean state){
 		canAttack = state;
 	}
 	public void setAliveState(boolean state){
 		isAlive = state;
 	}
 	public void setMode(String mode) {
 		this.mode = mode;
 	}
 	public void setConnected(boolean connected) {
 		this.connected = connected;
 	}
 	public void setReadyState(boolean ready) {
 		isReady = ready;
 	}
 	public void setHasClickedStartGame(boolean hasClicked){
 		hasClickedStartGame = hasClicked;
 	}
 	public void setChangedModelState(boolean state){
 		changeModel = state;
 	}
 	
 	// Misc methods
 	public void dealDamage(int damage){
 		damage *= (1-armor);
 		if(damage >= 0){
 			HP -= damage;
 			if(damage != 0){
 				playSound("res/sounds/takingDamage.wav");
 			}
 		}
 	}
 	public void setChangedModelImages(Image[] walkingImages, Image[] standingImages){
 		changedModelAnimation = new RepeatingAnimationTimer(1000, walkingImages);
 		noStepChangedModelAnimation = new RepeatingAnimationTimer(1000, standingImages);
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
