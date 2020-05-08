 package com.WebSiteNameHere;
 
 import java.util.Random;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.XMLPackedSheet;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Hero extends Entity{
 	public int lives;
 	public boolean rolling = false;
 	private final float ssjSpeed = 108;
 	private final float maxJumpHeight = 128; //pixels
 	private boolean ascending = false;
 	private boolean falling = true;
 	private float yDelta;
 
 	private Animation run;
 	private Animation ascend;
 	private Animation fall;
 	private Animation land;
 	private Animation roll;
 	private Animation ssj;
 	private Animation aura;
 
 	private Animation[] allAnimations = new Animation[5];
 
 	private int animationFlag = 0; // 0=run; 1=ascend; 2=fall; 3=land; 4=roll; 5=death; 5 will be implemented later
 	private int prevAnimation;
 	private boolean ssjMode = false;
 	private boolean firstSSJ = true;
 	int i = 0;
 
 	private Collidable heroColBox;
 
 	private XMLPackedSheet sheet;
 	private XMLPackedSheet auraSheet;
 	private float spd = 1.5f;  //speed of overall animation
 
 	public Hero(int x, int y)
 	{
 		this.x=x;
 		this.y=y;
 
 		heroColBox = new Collidable("red", (int)x, (int)y, 55, 27, 106, 128, true, false);
 
 		//setting up animations
 		setUpAnimations();
 		//run animation set to start at the beginning, other animations have to be stopped when setting them up.
 		run.start();
 		prevAnimation = animationFlag;
 
 	}
 
 	public boolean getMode(){
 		return ssjMode;
 	}
 	
 	public void setAscending(boolean asc){
 		if(!falling)
 			ascending = asc;
		if(asc && (yDelta - y >= maxJumpHeight)){
 			ascending = false;
 			falling = true;
 		}
 		
 	}
 
 	public void update(){
 		if(GameplayState.getForegroundSpeed()>ssjSpeed)
 			ssjMode=true;
 
 		if(!ssjMode){
 			//to check later if the flag has changed
 			prevAnimation = animationFlag;
 
 			//logic to change the animation flag. Needs improvement!
 			if(!rolling){
 				if(ascending && GameplayState.collidingGround())
 					animationFlag = 1;
				else if((falling || !ascending) && !GameplayState.collidingGround())
 					animationFlag = 2;
				else if(GameplayState.collidingGround() && roll.isStopped()){
					if(land.isStopped())
 						animationFlag = 0;
 					else
 						animationFlag = 3;
 				}
 			} else {
 				animationFlag = 4;
 				rolling = false;
 			}
 
 			if(animationFlag == 0 || animationFlag == 3) {
 				heroColBox.setCol((int)x, (int)y, 55, 27, 106, 128);
 			}
 
 			if(animationFlag == 1 ||animationFlag == 2){
 				if(ascend.getFrame() > 1 || fall.getFrame() <= 2){
 					heroColBox.setCol((int)x, (int)y, 55, 27, 106, 113);
 				}
 				else if(ascend.getFrame() == 1){
 					heroColBox.setCol((int)x, (int)y, 55, 27, 106, 128);
 				}
 
 			}
 			if(animationFlag == 4){
 				if(roll.getFrame() > 1 && roll.getFrame() < 12){
 					heroColBox.setCol((int)x, (int)y, 37, 67, 120, 128);
 				}
 				else{
 					heroColBox.setCol((int)x, (int)y, 55, 27, 106, 128);
 				}
 			}
 
 			//if flag has changed after application of logic, start animation corresponding with the new flag
 			if(prevAnimation != animationFlag)
 				startAnimation(allAnimations[animationFlag]);
 		} else {
 			for (int i=0; i<allAnimations.length; i++)
 				allAnimations[i].stop();
 			ssj.start();
 			aura.start();
 			heroColBox.setCol((int)x, (int)y, 16, 36, 122, 94);	
 		}
 		
 		//Jump Stuff
 		
 		if(GameplayState.collidingGround()){
 			int tempY = GameplayState.collidingGroundY();
 			if(tempY != 0 && y > tempY - 120)
 				y = GameplayState.collidingGroundY() - 120;
 			if(yDelta != y)
 				yDelta = y;
 			falling = false;
 		}
		if(ascending)
			y-=7;
		if(falling || !ascending){
			y+=7;
 		}
 	}
 
 	private void startAnimation(Animation anim){
 		//stops all animations with exception of passed animation
 		for (int i=0; i<allAnimations.length; i++){
 			allAnimations[i].stop();
 		}
 		if(anim.isStopped())
 			anim.restart();
 	}
 
 	public void pauseAnimation(){
 		if(!ssjMode)
 			allAnimations[animationFlag].stopAt(allAnimations[animationFlag].getFrame());
 		else{
 			ssj.stopAt(ssj.getFrame());
 			aura.stopAt(aura.getFrame());
 		}
 	}
 
 	public void resumeAnimation(){
 		if(!ssjMode){
 			allAnimations[animationFlag].stopAt(allAnimations[animationFlag].getFrameCount());
 			if(animationFlag==0)
 				allAnimations[animationFlag].setLooping(true);
 			allAnimations[animationFlag].setCurrentFrame(allAnimations[animationFlag].getFrame());
 			allAnimations[animationFlag].start();
 		} else {
 			ssj.stopAt(ssj.getFrameCount());
 			ssj.setLooping(true);
 			ssj.setCurrentFrame(ssj.getFrame());
 			ssj.start();
 			aura.stopAt(aura.getFrameCount());
 			aura.setLooping(true);
 			aura.setCurrentFrame(aura.getFrame());
 			aura.start();
 		}
 	}
 
 	public Collidable getHeroCol(){
 		return heroColBox;
 	}
 
 	public void render(){};
 
 	public void render(StateBasedGame sb, Graphics g){ 
 		if(ssjMode&&firstSSJ){
 			sb.pauseUpdate();
 			firstTimeSSJ(sb, g);
 		}
 		if(!ssjMode)
 			//rendering according to animation flag
 			allAnimations[animationFlag].draw(x, y);
 		else if(!firstSSJ&&ssjMode){
 			ssj.draw(x,y);
 			aura.draw(x-128, y);
 		}
 	}
 
 	private void firstTimeSSJ(StateBasedGame sb, Graphics g){
 		//Image bg = new Image();
 		Image rocks = null;
 		Image lightn = null;
 		Image norm = null;
 		Image transf = null;
 		Image pLevel = null;
 		Random ran = new Random();
 		try {
 			rocks = new Image("/res/sprites/player/ssj/stones.png");
 			lightn = new Image("/res/sprites/player/ssj/lightn.png");
 			norm = new Image("/res/sprites/player/ssj/normal.png");
 			transf = new Image("/res/sprites/player/ssj/transformed.png");
 			pLevel = new Image("/res/sprites/player/ssj/pLevel.png");
 		} catch (SlickException e) {e.printStackTrace();}
 		g.clear();
 		if(ran.nextBoolean())
 			lightn.draw(0,0);
 		norm.draw(0, 0);
 		if(i>150 && i<250){
 			if(ran.nextBoolean())
 				transf.draw(0,0);
 		}
 		if(i>=250){
 			transf.draw(0,0);
 		}
 		rocks.draw(0,600-i*2);
 		pLevel.draw(0, 0);
 		i++;
 		if(i>300){
 			sb.unpauseUpdate();
 			firstSSJ = false;
 		}
 	}
 
 	private void setUpAnimations(){
 		//I am too tired atm, thus copy paste code
 		//getting SpriteSheet
 
 		try {
 			sheet = new XMLPackedSheet("res/sprites/player/RedSpriteSheet.png", "res/sprites/player/RedSpriteSheet.xml");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			auraSheet = new XMLPackedSheet("res/sprites/player/ssj/Glow.png", "res/sprites/player/ssj/Glow.xml");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 		//setting up running animation according to sprite names
 		run = new Animation();
 		for (int i=1;i<=9;i++) {
 			run.addFrame(sheet.getSprite("run"+(i)+".png"), 150);
 		}
 		//these should be self-explanatory
 		run.setAutoUpdate(true);
 		run.setLooping(true);
 		run.setSpeed(spd);
 
 		//setting up jumping animation (3 parts of it) according to sprite names.
 		ascend = new Animation();
 		for (int i=1;i<=2;i++) {
 			ascend.addFrame(sheet.getSprite("jump"+(i)+".png"), 150);
 		}
 		ascend.setAutoUpdate(true);
 		ascend.setLooping(false);
 		ascend.setSpeed(spd - 0.99f);
 		ascend.stop();
 
 		fall = new Animation();
 		for (int i=3;i<=4;i++) {
 			fall.addFrame(sheet.getSprite("jump"+(i)+".png"), 150);
 		}
 		fall.setAutoUpdate(true);
 		fall.setLooping(false);
 		fall.setSpeed(spd -.5f);
 		fall.stop();
 
 		land = new Animation();
 		for (int i=5;i<=7;i++) {
 			land.addFrame(sheet.getSprite("jump"+(i)+".png"), 150);
 		}
 		land.setAutoUpdate(true);
 		land.setLooping(false);
 		land.setSpeed(spd + .5f);
 		land.stop();
 
 		roll = new Animation();
 		for (int i=1;i<=13;i++) {
 			roll.addFrame(sheet.getSprite("roll"+(i)+".png"), 150);
 		}
 		roll.setAutoUpdate(true);
 		roll.setLooping(false);
 		roll.setSpeed(spd + 1f);
 		roll.stop();
 
 		ssj = new Animation();
 		ssj.addFrame(sheet.getSprite("ssjMode1.png"), 150);
 		ssj.addFrame(sheet.getSprite("ssjMode2_4.png"), 150);
 		ssj.addFrame(sheet.getSprite("ssjMode3.png"), 150);
 		ssj.addFrame(sheet.getSprite("ssjMode2_4.png"), 150);
 		ssj.setAutoUpdate(true);
 		ssj.setLooping(true);
 		ssj.setSpeed(spd + .8f);
 		ssj.stop();
 
 		aura = new Animation();
 		for(int i=1; i<=3; i++){
 			Image img = auraSheet.getSprite("a" + i);
 			img.setAlpha(0.7f);
 			aura.addFrame(img, 150);
 		}
 		aura.setAutoUpdate(true);
 		aura.setLooping(true);
 		aura.setSpeed(spd);
 		aura.stop();
 
 		//adding animations to the array
 		allAnimations[0] = run;
 		allAnimations[1] = ascend;
 		allAnimations[2] = fall;
 		allAnimations[3] = land;
 		allAnimations[4] = roll;
 		//		allAnimations[5] = death;  //this is for later
 	}
 
 
 	/**
 	 * Returns the current animationFlag value
 	 * @return int animationFlag
 	 */
 	public int getAnimationFlag()
 	{
 		return animationFlag;
 	}
 }
