 /*
  * 
  */
 package atrophy.combat.display;
 
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Stack;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.imageio.ImageIO;
 
 import watoydoEngine.designObjects.display.Crowd;
 import watoydoEngine.io.ReadWriter;
 import atrophy.combat.ai.Ai;
 import atrophy.combat.ai.AiGenerator;
 import atrophy.combat.ai.ThinkingAi;
 import atrophy.combat.ai.VehicleAi;
 import atrophy.gameMenu.saveFile.Squad;
 import atrophy.gameMenu.saveFile.Squad.Squaddie;
 
 public class AiCrowd extends Crowd {
 	
 	private static final int ANIMATION_FRAME_LENGTH = 300;
 
 	private ArrayList<Ai> actors;
 	private ArrayList<AiImage> masks;
 	
 	private Stack<Ai> masterStack;
 	private Stack<Ai> shuffledStack;
 	
 	private Map<String, BufferedImage> images;
 	private Map<String, AnimationBlob> animations;
 	
 	private Squad squad;
 	
 	private static Timer animationTimer;
 	
 	public AiCrowd(Squad squad) {
 		super(false);
 		images = new HashMap<>();
 		animations = new HashMap<>();
 		actors = new ArrayList<Ai>();
 		masks = new ArrayList<AiImage>();
 		loadPortraits();
 		loadAnimations();
 		
 		this.squad = squad;
 		
 		if(animationTimer != null) {
 			animationTimer.cancel();
 			animationTimer.purge();
 		}
 		
 		animationTimer = new Timer();
 		animationTimer.scheduleAtFixedRate(new AnimationTimerEvent(this), 0, ANIMATION_FRAME_LENGTH);
 	}
 	
 	private static class AnimationTimerEvent extends TimerTask {
 
 		private AiCrowd aiCrowd;
 
 		public AnimationTimerEvent(AiCrowd aiCrowd) {
 			this.aiCrowd = aiCrowd;
 		}
 
 		@Override
 		public void run() {
 			aiCrowd.updateAnimations();
 		}
 		
 	}
 	
 	private void loadPortraits(){
 		try{
 			// Location of head portrait images
 			String[] files = {"images/atrophy/combat/heads/beardHead.png",
 							  "images/atrophy/combat/heads/beardHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/messyHead.png",
 							  "images/atrophy/combat/heads/messyHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/nazcaHead.png",
 							  "images/atrophy/combat/heads/nazcaHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/plainHead.png",
 							  "images/atrophy/combat/heads/plainHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/raziel.png",
 							  "images/atrophy/combat/heads/razielDamaged.png",
 							  
 							  "images/atrophy/combat/heads/stripeHead.png",
 							  "images/atrophy/combat/heads/stripeHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/engineerHead.png",
 							  "images/atrophy/combat/heads/engineerHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/sensorHead.png",
 							  "images/atrophy/combat/heads/sensorHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/stealthHead.png",
 							  "images/atrophy/combat/heads/stealthHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/speedHead.png",
 							  "images/atrophy/combat/heads/speedHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/armourHead.png",
 							  "images/atrophy/combat/heads/armourHeadDamaged.png",
 							  
 							  "images/atrophy/combat/heads/turret.png",
 							  "images/atrophy/combat/heads/turret.png",
 							  
 							  "images/atrophy/combat/heads/mule.png",
 							  "images/atrophy/combat/heads/muleDead.png",
 							  };
 			BufferedImage img;
 			
 			String[] names = {"BeardHead", "BeardHeadDead",
 							  "MessyHead", "MessyHeadDead",
 							  "NazcaHead", "NazcaHeadDead",
 							  "PlainHead", "PlainHeadDead",
 							  "Raziel",    "RazielDead",
 							  "StripeHead","StripeHeadDead",
 							  "Engineer",  "EngineerDead",
 							  "Sensor",	   "SensorDead",
 							  "Stealthed", "StealthedDead",
 							  "Scout",	   "ScoutDead",
 							  "Armour",	   "ArmourDead",
 							  "Turret",    "TurretDead",
 							  "Mule",	   "MuleDead"
 							  };
 			
 			// Add all heads to the image bank
 			for(int i = 0; i < files.length; i++){
 				img = ImageIO.read(ReadWriter.getResourceAsInputStream(files[i]));
 				this.images.put(names[i], img);
 			}
 		
 		}
 		catch(IOException ioexcept){
 			System.err.println("Unable to load portraits");
 			System.exit(-1);
 		}
 	}
 	
 	private void loadAnimations() {
 		try{
 			
 			final Animation[] ANIMATION_ORDER = {Animation.DEAD, Animation.IDLE_MELEE, Animation.IDLE_WEP1, Animation.IDLE_WEP2, Animation.IDLE_WEP3, Animation.WALK};
 			
 			final int[] TILE_WIDTHS = {66, 43, 39, 72, 74, 33};
 			
 			final String AF = "images/atrophy/combat/heads/animations/";
 			
			String[] head1Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head1Blob = new AnimationBlob(ANIMATION_ORDER, head1Files, TILE_WIDTHS);
 			this.animations.put("BeardHeadFull", head1Blob);
 			
			String[] head2Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head2Blob = new AnimationBlob(ANIMATION_ORDER, head2Files, TILE_WIDTHS);
 			this.animations.put("MessyHeadFull", head2Blob);
 			
			String[] head3Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head3Blob = new AnimationBlob(ANIMATION_ORDER, head3Files, TILE_WIDTHS);
 			this.animations.put("NazcaHeadFull", head3Blob);
 			
			String[] head4Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head4Blob = new AnimationBlob(ANIMATION_ORDER, head4Files, TILE_WIDTHS);
 			this.animations.put("PlainHeadFull", head4Blob);
 			
			String[] head5Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head5Blob = new AnimationBlob(ANIMATION_ORDER, head5Files, TILE_WIDTHS);
 			this.animations.put("RazielFull", head5Blob);
 			
			String[] head6Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head6Blob = new AnimationBlob(ANIMATION_ORDER, head6Files, TILE_WIDTHS);
 			this.animations.put("StripeHeadFull", head6Blob);
 			
			String[] head7Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head7Blob = new AnimationBlob(ANIMATION_ORDER, head7Files, TILE_WIDTHS);
 			this.animations.put("EngineerFull", head7Blob);
 			
			String[] head8Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head8Blob = new AnimationBlob(ANIMATION_ORDER, head8Files, TILE_WIDTHS);
 			this.animations.put("SensorFull", head8Blob);
 			
			String[] head9Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head9Blob = new AnimationBlob(ANIMATION_ORDER, head9Files, TILE_WIDTHS);
 			this.animations.put("StealthedFull", head9Blob);
 			
			String[] head10Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head10Blob = new AnimationBlob(ANIMATION_ORDER, head10Files, TILE_WIDTHS);
 			this.animations.put("ScoutFull", head10Blob);
 			
			String[] head11Files = {AF+"deadSpriteSheet.png", AF+"idleSpriteSheet1.png", AF+"idleSpriteSheet2.png", AF+"idleSpriteSheet3.png", AF+"idleSpriteSheet4.png", AF+"walkSpriteSheet.png"};
 			AnimationBlob head11Blob = new AnimationBlob(ANIMATION_ORDER, head11Files, TILE_WIDTHS);
 			this.animations.put("ArmourFull", head11Blob);
 			
 		}
 		catch(IOException ioexcept){
 			System.err.println("Unable to load animations");
 			ioexcept.printStackTrace();
 			System.exit(-1);
 		}
 	}
 	
 	public ArrayList<Ai> getShuffledAi(){
 		
 		ArrayList<Ai> shuffledList = new ArrayList<Ai>();
 		
 		// move the Ai to the shuffled list since actors needs to stay in order
 		// actors.get(i) has its mask at masks.get(i)
 		shuffledList.addAll(this.actors);
 		
 		Collections.shuffle(shuffledList);
 		
 		return shuffledList;
 	}
 	
 	public Stack<Ai> getShuffledStack() {
 		
 		if(this.masterStack == null || this.masterStack.size() == 0){
 			masterStack = new Stack<Ai>();
 			
 			masterStack.addAll(this.actors);
 			
 			Collections.shuffle(masterStack);
 		}
 		
 		if(this.shuffledStack == null){
 			this.shuffledStack = new Stack<Ai>(); 
 		}
 		
 		if(this.shuffledStack.size() == 0){
 			shuffledStack.addAll(masterStack);
 		}
 		
 		return shuffledStack;
 	}
 	
 	@Override
 	public boolean mC(Point mousePosition, MouseEvent e) {
 		if(this.isActive()){
 			for(int i = 0; i < this.getMouseActionList().size(); i++){
 				// if the mouse click is in the hitbox then peform the action
 				if(this.getMouseActionList().get(i).isActive() && 
 				   this.getMouseActionList().get(i).isInBounds(mousePosition.x,mousePosition.y)){
 					this.getMouseActionList().get(i).mC(mousePosition, e);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public Ai getActor(String tag){
 		int i;
 		
 		for(i = 0; i < actors.size();i ++){
 			if(actors.get(i).getName().equals(tag)){
 				break;
 			}
 		}
 		
 		return actors.get(i);
 	}
 
 	public Ai getActor(int i){
 		return actors.get(i);
 	}
 	
 	public int getActorCount(){
 		return actors.size();
 	}
 	
 	public AiImage getActorMask(Ai ai){
 		int i;
 		
 		for(i = 0; i < this.masks.size(); i++){
 			if(this.masks.get(i).getAi() == ai){
 				break;
 			}
 		}
 		
 		return this.masks.get(i);
 	}
 	
 	public AiImage getMask(int i){
 		return masks.get(i);
 	}
 	
 	public ArrayList<Ai> getActors(){
 		return this.actors;
 	}
 	
 	public void addActor(Ai ai){
 		actors.add(ai);
 	}
 	
 	public void addMask(AiImage aiImg){
 		masks.add(aiImg);
 		aiImg.updateImage();
 	}
 
 	public ArrayList<AiImage> getMasks() {
 		return this.masks;
 	}
 
 	public Squad saveToSquad() {
 		Squad squad = new Squad();
 		
 		squad.setAdvance(this.squad.getAdvance());
 		squad.setKills(this.squad.getSquadKills());
 		
 		for(int i = 0; i < this.actors.size(); i++){
 			if(actors.get(i).getFaction().equals(AiGenerator.PLAYER) && !actors.get(i).isDead()){
 				Ai ai = actors.get(i);
 				
 				
 				Squaddie squadMember;
 				
 				if(this.getActorMask(ai) instanceof VehicleImage)
 					squadMember = new Squaddie(ai.getName(), ai.getImage(), ai.getWeapon().getName(), ((VehicleAi)ai).getVehicleType());
 				else
 					squadMember = new Squaddie(ai.getName(), ai.getImage(), ai.getWeapon().getName(), "");
 				
 				for(int j = 0; j < ai.getInventory().getItemCount(); j++){
 					squadMember.addItem(ai.getInventory().getItemAt(j).getName());
 				}
 				
 				Iterator<String> skillIt = ai.getSkills().keySet().iterator();
 				
 				while(skillIt.hasNext()){
 					String skill = skillIt.next();
 					squadMember.setSkillLevel(skill, ai.getSkillLevel(skill));
 				}
 				
 				squad.addSquaddie(squadMember);
 			}
 		}
 		
 		return squad;
 		
 	}
 
 	public void replaceAi(ThinkingAi thinkingAi, Ai playerAi) {
 		for(int i = 0; i < masterStack.size(); i++){
 			if(masterStack.get(i) == thinkingAi){
 				masterStack.set(i, playerAi);
 			}
 		}
 	}
 
 	public BufferedImage getPortraitImage(String image) {
 		return this.images.get(image);
 	}
 
 	public void updateAnimations() {
 		for(AiImage image : this.masks) {
 			image.updateAnimation();
 		}
 	}
 
 	public BufferedImage getAnimationFrame(String image, int frame, Animation animation) {
 		return this.animations.get(image).getFrame(frame, animation);
 	}
 	
 	private static class AnimationBlob {
 		
 		private Map<Animation, BufferedImage[]> animations;
 		
 		public AnimationBlob(final Animation[] ANIMATION_ORDER, String[] files, int[] tileWidths) throws FileNotFoundException, IOException {
 			this.animations = new HashMap<>();
 			
 			int animationIndex = 0;
 			BufferedImage[] img = null;
 			
 			// Load files
 			for(int i = 0; i < files.length; i++){
 				
 				BufferedImage spriteSheet = ImageIO.read(ReadWriter.getResourceAsInputStream(files[i]));
 				
 				int tileWidth = tileWidths[animationIndex];
 				
 				img = new BufferedImage[spriteSheet.getWidth() / tileWidth];
 				
 				for(int k = 0; k < spriteSheet.getWidth(); k+= tileWidth) {
 					img[k / tileWidth] = spriteSheet.getSubimage(k, 0, tileWidth, spriteSheet.getHeight());
 				}
 				
 				this.animations.put(ANIMATION_ORDER[animationIndex], img);
 				animationIndex++;
 			}
 		}
 		
 		public BufferedImage getFrame(int frame, Animation animation) {
 			return this.animations.get(animation)[frame];
 		}
 		
 	}
 	
 }
