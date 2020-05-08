 package org.ssg.Cambridge;
 
 import java.util.ArrayList;
 import java.io.*;
 import java.net.MalformedURLException;
 
 import net.java.games.input.Controller;
 
 import org.ini4j.*;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.AngelCodeFont;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import paulscode.sound.SoundSystem;
 import paulscode.sound.SoundSystemConfig;
 
 public class GameplayState extends BasicGameState implements KeyListener {
 	private GlobalData data;
 
 	final String RESDIR = "res/";
 	
 	final String TENNIS = "TENNIS";
 	final String SQUASH = "SQUASH";
 	final String SOCCER = "SOCCER";
 	final String HOCKEY = "HOCKEY";
 	
 	public int stateID = 1;
 	
 	int gameType;
 	int NUMGAMES;
 	String NAME;
 	float gameModeAlpha;
 	
 	public float SCREENWIDTH;
 	public float SCREENHEIGHT;
 	public int ACTIONCAM;
 	public float maxZoom;
 	
 	public int FIELDWIDTH;// = 1600;
 	public int FIELDHEIGHT;// = 900;
 	public int FULLCOURT;// = 1;//1 is full court, 0 is halfs
 	public int GOALSIZE;
 	public int GOALTYPE;
 	public float KICKRANGE;// = 50f;
 	public float BALLSIZE = 20f;
 	public int postWidth;//Is actually the size of goalie box
 	public Goal[] goals;
 	public Color[] teamColors;
 	
 	private int minX, minY;//The top left bounds of active objects
 	private int maxX, maxY;// bottom right bounds
 	float tempX, tempY;//Width and height of camera 'box'
 	private float targetViewX, targetViewY;
 	private float viewX, viewY;//Top left corner of the camera
 	private float scaleFactor, targetScaleFactor;
 	private int boundingWidth = 100;
 	private boolean shouldRender;
 	
 	AngelCodeFont font, font_white, font_small;
 	Image triangle, hemicircleL, hemicircleR, slice, slice_tri, slice_wide, slice_twin;
 	Image goalScroll1, goalScroll2, goalScroll1v, goalScroll2v, goalScroll;
 	int scrollX;//For the "GOAL" scroll
 	int scrollY;
 	int scrollXDir, scrollYDir;
 	float resetVelocity[], targetX, targetY;
 	
 	Ball ball;
 	//Player p1, p2;
 	Player[] players;
 	
 	boolean slowMo;
 	
 	float[] kickFloat;//Unit vector to set ball velocity after kicking
 	float[] spinFloat;//vector used to store orthogonal projection of player's v on kickFloat
 	float[] tempTrailArr;//Used in trail drawing, size 4
 	
 	int centreX;
 	int centreY;
 	
 	int scores[];//Scores for p1 and p2
 	boolean scored;//Did a goal just get scored
 	
 	SoundSystem mySoundSystem;
 	CambridgeController c1, c2, c3, c4;
 	boolean c1Exist, c2Exist, c3Exist, c4Exist;
 	
 	float deltaf;
 	boolean temp;
 	float tempf;
 	float[] tempArr;
 	
 	public GameplayState(int i, boolean renderon, int gt) {
 		stateID = i;
 		gameType = gt;
 		shouldRender = renderon;
 	}
 	
 	public void setShouldRender(boolean shouldRender) {
 		this.shouldRender = shouldRender;
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		
 		data = ((Cambridge) sbg).getData();
 		mySoundSystem = data.mySoundSystem();
 		
 		c1 = data.getC()[0];
 		c2 = data.getC()[1];
 		c3 = data.getC()[2];
 		c4 = data.getC()[3];
 		c1Exist = (c1 != null);
 		c2Exist = (c2 != null);
 		c3Exist = (c3 != null);
 		c4Exist = (c4 != null);
 		
 		try {
 			font = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0.png"));
 			font_white = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0_white.png"));
 			font_small = new AngelCodeFont(RESDIR + "8bitoperator_small.fnt", new Image(RESDIR + "8bitoperator_small_0.png"));
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Fonts not loaded properly. Uh oh. Spaghettio.");
 			e.printStackTrace();
 		}
 		
 		triangle = new Image(RESDIR + "triangle.png");
 		hemicircleL = new Image(RESDIR + "hemicircleL.png");
 		hemicircleR = new Image(RESDIR + "hemicircleR.png");
 		slice = new Image(RESDIR + "slice.png");
 		slice_tri = new Image(RESDIR + "slice_tri.png");
 		slice_wide = new Image(RESDIR + "slice_wide.png");
 		slice_twin = new Image(RESDIR + "slice_twin.png");
 		goalScroll1 = new Image(RESDIR + "goal.png");
 		goalScroll2 = new Image(RESDIR + "goal_own.png");
 		goalScroll1v = new Image(RESDIR + "goal_v.png");
 		goalScroll2v = new Image(RESDIR + "goal_own_v.png");
 		goalScroll = goalScroll1;
 		
 		initFields(gc);
 	}
 
 	public void initFields(GameContainer gc) throws SlickException {
 		
 		Ini ini, userIni;
 		
 		float[] playerConsts = new float[8];
 		float[] ballConsts = new float[3];
 		
 		try {
 			ini = new Ini(new File(RESDIR + "config.cfg"));
 			userIni = new Ini(new File(RESDIR + "user_config.cfg"));
 			
 			NUMGAMES = ini.get("CONF","NUMGAMES", int.class);
 			SCREENWIDTH = userIni.get("DISPLAY", "SCREENWIDTH", float.class);
 			SCREENHEIGHT = userIni.get("DISPLAY", "SCREENHEIGHT", float.class);
 			ACTIONCAM = ini.get("CONF", "ACTIONCAM", int.class);
 			
 			Ini.Section section = ini.get(""+gameType);
 			
 			NAME = section.get("NAME");
 			FIELDWIDTH = section.get("FIELDWIDTH", int.class);
 			FIELDHEIGHT = section.get("FIELDHEIGHT", int.class);
 			GOALSIZE = section.get("GOALSIZE", int.class);
 			postWidth = GOALSIZE+150;
 			if(postWidth>FIELDHEIGHT)
 				postWidth = FIELDHEIGHT;
 			GOALTYPE = section.get("GOALTYPE", int.class);
 			FULLCOURT = section.get("FULLCOURT", int.class);
 			KICKRANGE = section.get("KICKRANGE", float.class);
 			
 			playerConsts[0] = section.get("VELMAG", float.class);
 			playerConsts[1] = section.get("POWERVELMAG", float.class);
 			playerConsts[2] = section.get("KICKCOOLDOWN", float.class);
 			playerConsts[3] = section.get("MAXPOWER", float.class);
 			playerConsts[4] = section.get("POWERCOOLDOWN", float.class);
 			playerConsts[5] = section.get("NORMALKICK", float.class);
 			playerConsts[6] = section.get("POWERKICK", float.class);
 			playerConsts[7] = KICKRANGE;
 			
 			ballConsts[0] = section.get("CURVESCALE", float.class);
 			ballConsts[1] = section.get("BOUNCEDAMP", float.class);
 			ballConsts[2] = section.get("FLOORFRICTION", float.class);
 			
 		} catch (InvalidFileFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		minX = FIELDWIDTH/2-(int)SCREENWIDTH/2;
 		minY = FIELDHEIGHT/2-(int)SCREENHEIGHT/2;
 		maxX = FIELDWIDTH/2+(int)SCREENWIDTH/2;
 		maxY = FIELDHEIGHT/2+(int)SCREENHEIGHT/2;
 		viewX = minX;
 		viewY = minX;
 		targetViewX = viewX;
 		targetViewY = viewY;
 		scaleFactor = 1;
 		
 		if(maxZoom == 0){
 			if(GOALTYPE == 0){
 				maxZoom = 2;
 			}else{
 				maxZoom = 1;
 			}
 		}
 		
 		int randomNum = (int)(Math.random()*2);
 
 		initGoals(randomNum);
 
 		//ball = new Player(new int[]{FIELDWIDTH,FIELDHEIGHT},new int[]{Input.KEY_I, Input.KEY_K, Input.KEY_J, Input.KEY_L}, new int[]{FIELDWIDTH/2, FIELDHEIGHT/2}, Color.white);
 		ball = new Ball(0, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem);
 		
 		float[] p1Start = {FIELDWIDTH/2-250, FIELDHEIGHT/2};
 		float[] p2Start = {FIELDWIDTH/2+250, FIELDHEIGHT/2};
 		if(NAME.equals(TENNIS)){
 			p1Start = new float[]{120, FIELDHEIGHT/2};
 			p2Start = new float[]{FIELDWIDTH-120, FIELDHEIGHT/2};
 			ball.setPos(350+randomNum*(FIELDWIDTH-700), FIELDHEIGHT/2+150-300*randomNum);
 		}else if(NAME.equals(SQUASH)){
 			p1Start = new float[]{100, FIELDHEIGHT/2-300};
 			p2Start = new float[]{100, FIELDHEIGHT/2+300};
 			ball.setPos(FIELDWIDTH/2-200, FIELDHEIGHT/2-300+600*randomNum);
 		}
 		
 		int[] p1lim;
 		int[] p2lim;
 		if(FULLCOURT==1){
 			p1lim = new int[]{0,FIELDWIDTH, 0, FIELDHEIGHT};
 			p2lim = p1lim;
 		}else {
 			p1lim = new int[]{0,FIELDWIDTH/2, 0, FIELDHEIGHT};
 			p2lim = new int[]{FIELDWIDTH/2, FIELDWIDTH, 0, FIELDHEIGHT};
 		}
 		
 		int[] p1Controls = new int[]{Input.KEY_W, Input.KEY_S, Input.KEY_A, Input.KEY_D, Input.KEY_LSHIFT, Input.KEY_LCONTROL};
 		int[] p2Controls = new int[]{Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_RSHIFT, Input.KEY_RCONTROL};
		Ball predictor  = new Ball(1, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem);
		PlayerTwoTouch p1 = new PlayerTwoTouch(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball, predictor);
 //		PlayerTwin p1L = new PlayerTwin(0, playerConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_twin, 0, hemicircleL, ball);
 //		PlayerTwin p1R = new PlayerTwin(0, playerConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, p1Controls, c1, new float[]{p1L.getX(),p1L.getY()+1}, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_twin, 1, hemicircleR, ball);
 //		p1L.setTwin(p1R);
 //		p1R.setTwin(p1L);
 		//PlayerNeo p1 = new PlayerNeo(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice);
 		//PlayerNeutron p1 = new PlayerNeutron(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
 		//PlayerBack p1 = new PlayerBack(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_wide, ball);
 		//PlayerDash p1 = new PlayerDash(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_tri, ball, hemicircleL);
 		//PlayerEnforcer p1 = new PlayerEnforcer(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
//		PlayerTricky p1 = new PlayerTricky(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
//		p1.setFakeBall(new BallFake(1, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem));
//		PlayerDummy p1D1 = new PlayerDummy(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice);
//		p1.setDummy(p1D1);
 		Ball predictor2  = new Ball(1, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem);
 		PlayerTwoTouch p2 = new PlayerTwoTouch(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice, ball, predictor2);
 		//PlayerNeo p2 = new PlayerNeo(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice);
 		//PlayerDash p2 = new PlayerDash(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice, slice_tri, ball, hemicircleL);
 		//PlayerEnforcer p2 = new PlayerEnforcer(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow1", slice, ball);
 //		PlayerBack p2 = new PlayerBack(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice, slice_wide, ball);
 		
		players = new Player[]{p1, p2};
 		for(Player p: players)
 			p.setPlayers(players);
 		
 		ball.setPlayers(players);
 		
 		teamColors = new Color[2];
 		teamColors[0] = players[0].getColor();
 		teamColors[1] = players[players.length-1].getColor();
 		
 		Input input = gc.getInput();
 		//input.addKeyListener(ball);
 		for(Player p: players)
 			input.addKeyListener(p);
 		//ball.inputStarted();
 		//p1.inputStarted();
 		//p2.inputStarted();
 		
 		kickFloat = new float[]{0f,0f};
 		spinFloat = new float[]{0f,0f};
 		scores = new int[]{0,0};
 		scored = false;
 		
 		tempTrailArr = new float[]{0,0,0,0};
 		
 		gameModeAlpha = 1f;
 		
 		scrollX = 2000;
 		scrollY = 2000;
 		scrollXDir = 0;
 		scrollYDir = 0;
 		resetVelocity = new float[]{0,-1f};
 		targetX = FIELDWIDTH/2;
 		targetY = 0;
 		
 		slowMo = false;
 //		gc.getGraphics().setAntiAlias(true);
 		
 		tempArr = new float[2];
 	}
 	
 	public void initGoals(int randomNum){
 		if(GOALTYPE == 0){//Left and Right goals
 			goals = new Goal[2];
 			goals[0] = new Goal(0,FIELDHEIGHT/2-GOALSIZE/2, -25, GOALSIZE, -1 , 0, 0);
 			goals[1] = new Goal(FIELDWIDTH, FIELDHEIGHT/2-GOALSIZE/2, 25, GOALSIZE, 1, 0, 1);
 		}else if(GOALTYPE == 1){//One sided goals. Squash
 			goals = new Goal[1];
 			goals[0] = new Goal(0, 0, -25, FIELDHEIGHT, -1, 0, randomNum);
 		}else if(GOALTYPE == -1){//Horizontal Squash Goals
 			goals = new Goal[1];
 			goals[0] = new Goal(0,0, FIELDWIDTH, -25, 0, -1, 0);
 		}else if(GOALTYPE == 2){//FOURSQUARE STYLE GOALS
 			goals = new Goal[4];//= new Goal[8]
 			goals[0] = new Goal(0,0,FIELDWIDTH/2,-25,0,-1,0);
 			goals[1] = new Goal(0,0,-25,FIELDHEIGHT/2,-1,0,0);
 			goals[2] = new Goal(FIELDWIDTH/2,0,FIELDWIDTH/2,-25,0,-1,1);
 			goals[3] = new Goal(FIELDWIDTH,0,25,FIELDHEIGHT/2,1,0,1);
 /**			goals[4] = new Goal(FIELDWIDTH,FIELDHEIGHT/2,25,FIELDHEIGHT/2,1,0,2);
 			goals[5] = new Goal(FIELDWIDTH/2,FIELDHEIGHT,FIELDWIDTH/2,25,0,1,2);
 			goals[6] = new Goal(0,FIELDHEIGHT/2,-25,FIELDHEIGHT/2,-1,0,3);
 			goals[7] = new Goal(0,FIELDHEIGHT,FIELDWIDTH/2,25,0,1,3); */
 		}
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		mySoundSystem.backgroundMusic("BGM", "BGMHotline.ogg", true);
 		mySoundSystem.setVolume("BGM", data.ambientSound()/10f);
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		if (!shouldRender)
 			return;
 		
 		g.setAntiAlias(true);
 
 		g.scale(scaleFactor,  scaleFactor);
 		g.translate(-viewX, -viewY);
 
 		//Background
 		g.setColor(Color.white);
 		g.fillRect(-2000, -2000, FIELDWIDTH+4000, FIELDHEIGHT+4000);
 		g.setColor(Color.black);
 		g.fillRect(0,0, FIELDWIDTH, FIELDHEIGHT);
 		g.setColor(Color.white);
 		
 		//Draw camera box for debug
 		//g.setColor(Color.green);
 		//g.drawRect(viewX, viewY, tempX, tempY);
 		//g.setColor(Color.white);
 		
 		//Draw Field
 		drawField(g);
 		
 		//Draw Players
 		for(Player p: players){
 			p.render( g, BALLSIZE, triangle, font_small);
 		}
 		
 //		g.drawLine(p1.getX(), p1.getY(), p1.getX()+spinFloat[0]*100, p1.getY()+spinFloat[1]*100);
 		
 		//Draw ball
 		if(!scored){
 			g.setColor(Color.red);
 			g.rotate(ball.getX(), ball.getY(), ball.getTheta());
 			g.fillRect(ball.getX()-BALLSIZE/2, ball.getY()-BALLSIZE/2, BALLSIZE, BALLSIZE);
 			g.rotate(ball.getX(), ball.getY(), -ball.getTheta());
 			g.setColor(Color.white);
 		}
 		
 		//draw goal scroll
 		g.drawImage(goalScroll, scrollX, scrollY);
 		
 		//draw bars
 		g.setColor(Color.white);
 		g.fillRect(-1000, -1000, FIELDWIDTH+2000, 1000);
 		g.fillRect(-1000, 0, 1000, FIELDHEIGHT);
 		g.fillRect(-1000, FIELDHEIGHT, FIELDWIDTH+2000, 1000);
 		g.fillRect(FIELDWIDTH, 0, 1000, FIELDHEIGHT);
 		
 		//Draw goals
 		for(Goal goal: goals){
 			g.setColor(teamColors[goal.getPlayer()].darker());
 			g.fillRect(goal.getX(), goal.getMinY(), goal.getWidth(), goal.getHeight());
 		}
 		//g.fillRect(FIELDWIDTH-10, FIELDHEIGHT/2-GOALWIDTH/2, 15, GOALWIDTH);
 		
 		//Draw Header
 		drawHeader(g);
 		
 		//draw gamemode
 		if(gameModeAlpha>0){
 			g.setFont(font_white);
 			g.setColor(new Color(1f, 1f, 1f, gameModeAlpha));
 			g.drawString(NAME, FIELDWIDTH/2-font.getWidth(NAME)/2, FIELDHEIGHT/2-font.getHeight("0")/2);
 		}
 		
 		g.resetTransform();
 	}
 	
 	public void drawHeader(Graphics g){
 		//Draw Text
 		g.setFont(font);
 		if(GOALTYPE == 1 || GOALTYPE == -1){//Squash or FourSquare
 			g.rotate(0,0,-90);
 			g.drawString("PLAY "+NAME, -FIELDHEIGHT/2 - font.getWidth("PLAY "+NAME)/2, FIELDWIDTH);
 			g.rotate(0,0,90);
 			g.drawString("FERNANDO TORANGE", 0, -font.getHeight("0")-10);
 			g.drawString("DIDIER DROGBLUE", 0 , FIELDHEIGHT);
 			
 			g.drawString(""+scores[1], FIELDWIDTH-font.getWidth(""+scores[1]) , -font.getHeight("0")-10);
 			g.drawString(""+scores[0], FIELDWIDTH-font.getWidth(""+scores[0]),  FIELDHEIGHT);
 		}else if(GOALTYPE==2){//Same as above but more spacing, for onesquare
 			g.rotate(0,0,-90);
 			g.drawString("PLAY "+NAME, -FIELDHEIGHT/2 - font.getWidth("PLAY "+NAME)/2, FIELDWIDTH+10);
 			g.rotate(0,0,90);
 			g.drawString("FERNANDO TORANGE", 0, -font.getHeight("0")-30);
 			g.drawString("DIDIER DROGBLUE", 0 , FIELDHEIGHT+12);
 			
 			g.drawString(""+scores[1], FIELDWIDTH-font.getWidth(""+scores[1]) , -font.getHeight("0")-30);
 			g.drawString(""+scores[0], FIELDWIDTH-font.getWidth(""+scores[0]),  FIELDHEIGHT+12);
 		}else{
 			//Draw game mode
 			g.drawString("PLAY "+NAME, FIELDWIDTH/2 - font.getWidth("PLAY "+NAME)/2, FIELDHEIGHT);
 			
 			//Draw Player Names. have these be randomized for now, maybe based on character select
 			g.drawString("FERNANDO TORANGE", 0, -font.getHeight("0")-10);
 			g.drawString("DIDIER DROGBLUE", FIELDWIDTH - font.getWidth("DIDIER DROGBLUE"), -font.getHeight("0")-10);
 			
 			//Draw scores
 			g.drawString(""+scores[1], FIELDWIDTH/2-font.getWidth(""+scores[1])-20 , -font.getHeight("0")-10);
 			g.drawString(""+scores[0], FIELDWIDTH/2+20,  -font.getHeight("0")-10);
 			g.drawString(":", FIELDWIDTH/2-font.getWidth(":")/2, -font.getHeight("0")-14);
 		}
 	}
 	
 	public void drawField(Graphics g){
 		//Draw the field markings
 		g.setColor(Color.white);
 		g.setLineWidth(1);
 		g.drawRect(0,0,FIELDWIDTH, FIELDHEIGHT);
 		g.setLineWidth(5);
 		if(NAME.equals(SOCCER)){
 			g.drawLine(FIELDWIDTH/2,0,FIELDWIDTH/2,FIELDHEIGHT);
 			g.drawOval(FIELDWIDTH/2-150, FIELDHEIGHT/2-150, 300, 300);
 			g.drawLine(FIELDWIDTH/2, 0, FIELDWIDTH/2, FIELDHEIGHT);
 			g.drawOval(-50, FIELDHEIGHT/2-190,380,380);
 			g.drawOval(FIELDWIDTH+50, FIELDHEIGHT/2-190,-380,380);
 			g.setColor(Color.black);//Fill in goalie box
 			g.fillRect(0,FIELDHEIGHT/2-postWidth/2-100,250,postWidth+200);
 			g.fillRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2-100,-250,postWidth+200);
 			g.setColor(Color.white);//Goalie box
 			g.drawRect(0,FIELDHEIGHT/2-postWidth/2-100,250,postWidth+200);
 			g.drawRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2-100,-250,postWidth+200);
 			g.drawRect(0,FIELDHEIGHT/2-postWidth/2,100,postWidth);
 			g.drawRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2,-100,postWidth);
 		}else if (NAME.equals(HOCKEY)){
 			g.drawLine(FIELDWIDTH/2,0,FIELDWIDTH/2,FIELDHEIGHT);
 			g.drawRect(FIELDWIDTH/3, 0, FIELDWIDTH/3, FIELDHEIGHT);
 			g.drawOval(-40,FIELDHEIGHT/2-GOALSIZE/2,80,GOALSIZE);
 			g.drawOval(FIELDWIDTH+40, FIELDHEIGHT/2-GOALSIZE/2,-80,GOALSIZE);
 			g.fillOval(80+FIELDWIDTH/12-10, 80+FIELDWIDTH/12-10, 20, 20);
 			g.fillOval(80+FIELDWIDTH/12-10, FIELDHEIGHT-80-FIELDWIDTH/12-10, 20, 20);
 			g.fillOval(FIELDWIDTH-80-FIELDWIDTH/12-10,80+FIELDWIDTH/12-10,20,20);
 			g.fillOval(FIELDWIDTH-80-FIELDWIDTH/12-10,FIELDHEIGHT-80-FIELDWIDTH/12-10,20,20);
 			g.fillOval(FIELDWIDTH/2-200, 80+FIELDWIDTH/12-10, 20, 20);
 			g.fillOval(FIELDWIDTH/2-200, FIELDHEIGHT-80-FIELDWIDTH/12-10, 20, 20);
 			g.fillOval(FIELDWIDTH/2+200,80+FIELDWIDTH/12-10,-20,20);
 			g.fillOval(FIELDWIDTH/2+200,FIELDHEIGHT-80-FIELDWIDTH/12-10,-20,20);
 			g.setLineWidth(3);
 			g.setColor(Color.black);
 			g.fillOval(FIELDWIDTH/2-FIELDWIDTH/12, FIELDHEIGHT/2-FIELDWIDTH/12, FIELDWIDTH/6, FIELDWIDTH/6);
 			g.setColor(Color.white);
 			g.drawOval(FIELDWIDTH/2-FIELDWIDTH/12, FIELDHEIGHT/2-FIELDWIDTH/12, FIELDWIDTH/6, FIELDWIDTH/6);
 			g.drawOval(80,80,FIELDWIDTH/6,FIELDWIDTH/6);
 			g.drawOval(80,FIELDHEIGHT-80,FIELDWIDTH/6,-FIELDWIDTH/6);
 			g.drawOval(FIELDWIDTH-80,80,-FIELDWIDTH/6,FIELDWIDTH/6);
 			g.drawOval(FIELDWIDTH-80,FIELDHEIGHT-80,-FIELDWIDTH/6,-FIELDWIDTH/6);
 			g.setLineWidth(5);
 		}else if(NAME.equals(TENNIS)){
 			g.drawRect(150,150,FIELDWIDTH-300,FIELDHEIGHT-300);
 			g.drawLine(FIELDWIDTH/2, 150, FIELDWIDTH/2, FIELDHEIGHT-150);
 			g.drawRect(150,225,FIELDWIDTH-300,FIELDHEIGHT-450);
 			g.drawRect(350,225,FIELDWIDTH-700,FIELDHEIGHT-450);
 			g.drawLine(350, FIELDHEIGHT/2, FIELDWIDTH-350, FIELDHEIGHT/2);
 		}else if(NAME.equals(SQUASH)){
 			g.drawLine(FIELDWIDTH/2-100, 0, FIELDWIDTH/2-100, FIELDHEIGHT);
 			g.drawLine(0, FIELDHEIGHT/2, FIELDWIDTH/2-100, FIELDHEIGHT/2);
 			g.drawRect(FIELDWIDTH/2-100, 0, -200, 200);
 			g.drawRect(FIELDWIDTH/2-100,FIELDHEIGHT,-200,-200);
 		}else if(NAME.equals("FOURSQUARE")){
 			g.drawLine(0, FIELDHEIGHT/2, FIELDWIDTH, FIELDHEIGHT/2);
 			g.drawLine(FIELDWIDTH/2, 0, FIELDWIDTH/2, FIELDHEIGHT);
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		
 		deltaf = (float)delta;
 		
 		slowMo = false;
 		for(Player p: players){
 			if(p.isSlowMoPower()){
 				slowMo = true;
 				break;
 			}
 		}
 		
 		if(slowMo){
 			deltaf=deltaf/4f;
 			ball.setSlowOn(true);
 		}else{
 			ball.setSlowOn(false);
 		}
 		
 		gameModeAlpha -= deltaf/1200f;
 		if(gameModeAlpha < 0){
 			gameModeAlpha = 0;
 		}
 		
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_U)){
 			reset(gc);
 		}else if(input.isKeyPressed(Input.KEY_I)){
 			gameType = (gameType+1)%NUMGAMES;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_1)){
 			gameType = 0;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_2) && NUMGAMES>=2){
 			gameType = 1;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_3) && NUMGAMES>=3){
 			gameType = 2;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_4) && NUMGAMES>=4){
 			gameType = 3;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_5) && NUMGAMES>=5){
 			gameType = 4;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_6) && NUMGAMES>=6){
 			gameType = 5;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_7) && NUMGAMES>=7){
 			gameType = 6;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_8) && NUMGAMES>=8){
 			gameType = 7;
 			reset(gc);
 		}else if (input.isKeyPressed(Input.KEY_9) && NUMGAMES>=9){
 			gameType = 8;
 			reset(gc);
 		}else if( input.isKeyPressed(Input.KEY_PERIOD)){
 			if(maxZoom<2)
 				maxZoom+=.2f;
 		}else if( input.isKeyPressed(Input.KEY_COMMA)){
 			if(maxZoom>.8)
 				maxZoom-=.2f;
 		}else if(input.isKeyPressed(Input.KEY_ESCAPE)){
 			gc.exit();
 			mySoundSystem.cleanup();
 		}
 		
 		scrollX+=deltaf*scrollXDir;
 		if(scrollX>FIELDWIDTH+500 || scrollX<0-goalScroll.getWidth()-500)
 			scrollXDir=0;
 		
 		scrollY+=deltaf*scrollYDir;
 		if(scrollY>FIELDHEIGHT+500 || scrollY<0-goalScroll.getHeight()-500)
 			scrollYDir=0;
 		
 		ball.update(deltaf);
 		
 		//Put ball back in play
 		if(scored){
 			if(Math.abs(ball.getX()-targetX)<15f){
 				temp = true;
 				for(Player p: players){if(dist(p) < p.getKickRange()/2){temp = false;}}
 				if(temp){
 					ball.setVel(resetVelocity, .5f);
 					ball.setPos(targetX, ball.getY());
 					ball.setScored(false);
 					ball.setSoundCoolDown(50);
 					scored = false;
 					mySoundSystem.quickPlay( true, slowMo? "BallLaunchSlow.ogg": "BallLaunch.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 				}else{
 					ball.setVel(resetVelocity, 0);
 				}
 			}
 		}
 		
 		//Scoring a goal
 		if((ball.getX()<=0 || ball.getX()>=FIELDWIDTH || ball.getY()<=0 || ball.getY()>=FIELDHEIGHT) && !scored){
 			//Will put ball in from top if score on left and bottom if score on right
 			
 			for(Goal goal: goals){
 				//For vertical goal
 				if(ball.getY()>goal.getMinY() && ball.getY()<goal.getMaxY() && sameDir(ball.getVelX(), goal.getXDir())){
 					if(ball.getLastKicker()==goal.getPlayer()){//Own Goal
 						mySoundSystem.quickPlay( true, "GoalOwnScored.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						goalScroll = goalScroll2;
 					}else{
 						mySoundSystem.quickPlay( true, "GoalScored.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						goalScroll = goalScroll1;
 					}
 					scores[goal.getPlayer()]++;
 					scored = true;
 					scrollX = goal.getX()+((goal.getXDir() < 0 ? goalScroll.getWidth() + 100 : 100)*(goal.getXDir()));
 					scrollY = goal.getMinY()+goal.getHeight()/2-goalScroll.getHeight()/2;
 					scrollXDir = -goal.getXDir();
 					scrollYDir = 0;
 				}
 				//For horizontal goals
 				if(ball.getX()>goal.getMinX() && ball.getX()<goal.getMaxX() && sameDir(ball.getVelY(), goal.getYDir())){
 					if(ball.getLastKicker()==goal.getPlayer()){//Own Goal
 						mySoundSystem.quickPlay( true, "GoalOwnScored.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						goalScroll = goalScroll2v;
 					}else{
 						mySoundSystem.quickPlay( true, "GoalScored.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						goalScroll = goalScroll1v;
 					}
 					scores[goal.getPlayer()]++;
 					scored = true;
 					scrollX = goal.getMinX()+goal.getWidth()/2-goalScroll.getWidth()/2;
 					scrollY = goal.getMinY()+((goal.getYDir() < 0 ? goalScroll.getHeight() + 100 : 100)*(goal.getYDir()));
 					scrollXDir = 0;
 					scrollYDir = -goal.getYDir();
 				}
 
 			}
 			
 			if(scored){
 				if(NAME.equals(TENNIS)){//Put in at side from player baseline, as a kind of serve.
 					if(ball.getLastKicker()==0){
 						targetX = FIELDWIDTH-150;
 						targetY = FIELDHEIGHT;
 						resetVelocity[0] = 0;
 						resetVelocity[1] = -1f;
 					}else if(ball.getLastKicker()==1){
 						targetX = 150;
 						targetY = 0;
 						resetVelocity[0] = 0;
 						resetVelocity[1] = 1f;
 					}
 					
 				}else if(NAME.equals(SOCCER) || NAME.equals(HOCKEY)){//If two goals, put in from sides at center
 					targetX = FIELDWIDTH/2;
 					if(ball.getLastKicker()==0){
 						targetY=0;
 						resetVelocity[0] = 0;
 						resetVelocity[1] = 1f;
 					}else{
 						targetY=FIELDHEIGHT;
 						resetVelocity[0] = 0;
 						resetVelocity[1] = -1f;
 					}
 				}else if(NAME.equals(SQUASH)){//Squash, put in from opposite wall
 					targetX = FIELDWIDTH-10;
 					targetY = FIELDHEIGHT/2;
 					resetVelocity[0] = -1f;
 					resetVelocity[1] = 0;
 				}else if(GOALTYPE==2){//OneSquare, put in middle
 					targetX = FIELDWIDTH/2;
 					targetY = FIELDHEIGHT/2;
 					resetVelocity[0]=0;
 					resetVelocity[1]=0;
 				}
 				ball.setVel(new float[]{(targetX-ball.getX()),(targetY-ball.getY())}, 1f);
 				ball.setCurve(new float[]{0f,0f}, 0f);
 				ball.cancelAcc();
 				ball.setReadyForGust(false);
 				ball.setAssistTwin(-1,-1);
 				//Scoring a goal pulls out of slowmo
 				for(Player p: players){
 					if(p.isSlowMoPower())
 						p.setPower();
 				}
 				ball.setScored(true);//just long enough for it to reach reset
 			}
 		}
 
 		//Kicking the ball
 		for(Player p: players){
 			p.update(deltaf);
 			if(p.isKicking() && !scored ){
 				if(dist(p)<p.getKickRange()/2 /* && ball.canBeKicked(p.getPlayerNum())*/) {//Perform a kick
 					//Use prevX to prevent going through the player
 					kickFloat[0] = (ball.getPrevX()-p.getX());
 					kickFloat[1] = (ball.getPrevY()-p.getY());
 
 					unit(kickFloat);
 //					tempf = 0;//Used to store the amount of player velocity added to the kick
 					if(sameDir(p.getVel()[0], kickFloat[0])){
 						kickFloat[0] += p.getKick()[0];
 //						tempf += p.getKick()[0]*p.getKick()[0];
 					}
 					if(sameDir(p.getVel()[1], kickFloat[1])){
 						kickFloat[1] += p.getKick()[1];
 //						tempf += p.getKick()[1]*p.getKick()[1];
 					}
 					unit(kickFloat);
 					tempf = Math.abs(dot(p.getVel(), kickFloat)); 
 					ball.setVel(new float[]{kickFloat[0], kickFloat[1]}, p.isPower()? p.POWERVELMAG*p.kickStrength() : .2f+p.getVelMag()*tempf*p.kickStrength() );
 
 					spinFloat = normal(p.getCurve(), kickFloat);
 					//System.out.println(p.kickStrength() + "-" + (p.kickStrength()*0.5f+(float)Math.sqrt(tempf)*0.5f));
 					ball.setCurve(spinFloat, mag(spinFloat)*p.curveStrength());
 					
 //					System.out.println("kickFloat:"+kickFloat[0]+", "+kickFloat[1]);
 //					System.out.println("spinFloat:"+spinFloat[0]+", "+spinFloat[1]);
 					
 //					System.out.println("Ball:"+ball.curveMag+", "+ball.curveAcc[0]+", "+ball.curveAcc[1]);
 					
 					if(p.flashKick()){//If you want the kick flash and sound effect
 						p.setLastKick(ball.getPrevX(), ball.getPrevY(), ball.getPrevX()+kickFloat[0], ball.getPrevY()+kickFloat[1], 1f);//player stores coordinates of itself and ball at last kicking event;
 						p.setPower();
 						mySoundSystem.quickPlay( true, slowMo?"PowerKickSlow.ogg":"PowerKick.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 					}else{
 						mySoundSystem.quickPlay( true, slowMo?"KickBumpSlow.ogg":"KickBump.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 					}
 					
 					p.setKicking(ball);//really this does resetKicking()
 					ball.cancelAcc();//Cancels any speeding up or slowing down. Does not affect curve
 					ball.setReadyForGust(false);
 					ball.setLastKicker(p.getPlayerNum());
 					ball.clearLocked();
 					
 					if(GOALTYPE == 1 || GOALTYPE == -1){//Squash
 						for(Goal goal: goals)
 							goal.changeSides();
 					}
 					if(GOALTYPE == 2){//If OneSquare
 						for(Goal goal: goals){
 							goal.setPlayerNum(ball.getLastKicker());
 							goal.changeSides();//Hacky ass way to set goal to opposite side as whoever kicked
 						}
 					}
 				}
 //				 else{
 //					if(dist(p) > p.getKickRange()/2)
 //						ball.setCanBeKicked(p.getPlayerNum(), true);
 //				}
 			}else if(!p.isKicking() && !scored && !(p instanceof PlayerTwoTouch && p.isPower()) && !(p instanceof PlayerBack && p.isPower())){
 				if(dist(p)<p.getKickRange()/2){//Nudge it out of the way
 					//Don't use prevX, to prevent weird sliding
 					kickFloat[0] = (ball.getX()-p.getX());
 					kickFloat[1] = (ball.getY()-p.getY());
 					unit(kickFloat);
 					tempf = (float)Math.atan2(kickFloat[1], kickFloat[0]);
 					ball.setPos(p.getX()+p.getKickRange()/2f*(float)Math.cos(tempf), p.getY()+p.getKickRange()/2f*(float)Math.sin(tempf));
 //					ball.setVel(new float[]{kickFloat[0], kickFloat[1]}, ball.getVelMag());
 					ball.setLastKicker(p.getPlayerNum());
 					//It shouldn't reset anything because this was added to prevent players from double tapping and clearing their own powers
 				}
 			}
 		}
 		
 		//Camera code
 		tempX = ball.getX();
 		tempY = ball.getY();
 		for(Player p: players){
 			tempX = Math.min(tempX, p.getX());
 			tempY = Math.min(tempY, p.getY());
 		}
 		minX = (int)tempX;
 		minY = (int)tempY;
 		
 		tempX = ball.getX();
 		tempY = ball.getY();
 		for(Player p: players){
 			tempX = Math.max(tempX, p.getX());
 			tempY = Math.max(tempY, p.getY());
 		}
 		maxX = (int)tempX;
 		maxY = (int)tempY;
 		
 		if(ACTIONCAM == 1){
 			tempX = maxX - minX;//dimensions of the camera's viewing "box"
 			tempY = maxY - minY;
 		}else{
 			tempX = FIELDWIDTH;
 			tempY = FIELDHEIGHT;
 		}
 		//Scale the box so it's in the ratio of the window
 		if(tempX/tempY > SCREENWIDTH/SCREENHEIGHT){
 			tempY = (SCREENHEIGHT/SCREENWIDTH * tempX);
 		}else{
 			tempX = (SCREENWIDTH/SCREENHEIGHT * tempY);
 		}
 		
 		//Limit the zoom to 2x
 		if(tempX < SCREENWIDTH/maxZoom){
 			tempX = SCREENWIDTH/maxZoom;
 			tempY = SCREENHEIGHT/maxZoom;
 		}
 
 		if(ACTIONCAM == 1){
 			tempX = (tempX*1.4f);
 			tempY = (tempY*1.4f);
 			
 			targetViewX = minX - (((int)tempX - (maxX - minX))/2);
 			targetViewY = minY - (((int)tempY - (maxY - minY))/2);
 			
 			if(viewX != targetViewX)
 				viewX += (targetViewX-viewX)*deltaf/300f;
 			if(viewY != targetViewY)
 				viewY += (targetViewY-viewY)*deltaf/300f;
 			
 			targetScaleFactor = SCREENWIDTH/tempX;
 			
 			if(scaleFactor != targetScaleFactor)
 				scaleFactor += (targetScaleFactor - scaleFactor)*deltaf/240f;
 		}else{
 			tempX = tempX * 1.2f;
 			tempY = tempY * 1.2f;
 			
 			viewX = -(int)(tempX-FIELDWIDTH)/2;
 			viewY = -(int)(tempY-FIELDHEIGHT)/2;
 			scaleFactor = SCREENWIDTH/tempX;
 		}
 		//System.out.println(scaleFactor);
 		
 	}
 	
 	public void reset(GameContainer gc) throws SlickException{
 		if(mySoundSystem.playing("slow1"))
 			mySoundSystem.pause("slow1");
 		if(mySoundSystem.playing("slow2"))
 			mySoundSystem.pause("slow2");
 		mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 		gameModeAlpha = 1f;
 		initFields(gc);
 	}
 	
 	public float dist(Player p){//dist to ball
 		return (float)Math.sqrt((p.getX()-ball.getX())*(p.getX()-ball.getX()) + (p.getY()-ball.getY())*(p.getY()-ball.getY()));
 	}
 	
 	public float dist(float x, float y){//dist to ball
 		return (float)Math.sqrt((x-ball.getX())*(x-ball.getX()) + (y-ball.getY())*(y-ball.getY()));
 	}
 	
 	public float dist(float[] a, float[]b){
 		return (float)Math.sqrt((a[0]-b[0])*(a[0]-b[0])+(a[1]-b[1])*(a[1]-b[1]));
 	}
 	
 	public float dist(float a, float b, float c, float d){
 		return (float)Math.sqrt((c-a)*(c-a)+(d-b)+(d-b));
 	}
 	
 	public float dot(float[] a, float[] b){
 		return a[0]*b[0]+a[1]*b[1];
 	}
 	
 	public float mag(float[] a){
 		return (float)Math.sqrt(a[0]*a[0]+a[1]*a[1]);
 	}
 	
 	public float[] normalNeg(float[] v, float[] w){//orthogonal proj v on w, negative
 		tempX = dot(v,w)/mag(w);//Repurposing this as a temp calculation holder
 		return new float[]{-v[0]+tempX*w[0], -v[1]+tempX*w[1]};
 	}
 	
 	public float[] normal(float[] v, float[] w){
 		if(mag(w)>0){
 			tempX = dot(v,w)/mag(w);//Repurposing this as a temp calculation holder
 			return new float[]{v[0]-tempX*w[0], v[1]-tempX*w[1]};
 		}else{
 			return new float[]{0,0};
 		}
 	}
 	
 	//Parallel component of u on v, written to w
 	public void parallelComponent(float[] u, float[] v, float[] w){
 		tempf = (u[0]*v[0]+u[1]*v[1])/mag(v)/mag(v);
 		w[0] = v[0]*tempf;
 		w[1] = v[1]*tempf;		
 	}
 	
 	public boolean sameDir(float vx, int dir){
 		if(vx == 0)
 			return false;
 		return vx/Math.abs(vx) == (float)dir;
 	}
 	
 	public boolean sameDir(float vx, float dir){
 		if(vx == 0)
 			return false;
 		return vx/Math.abs(vx) == dir/Math.abs(dir);
 	}
 	
 	public void unit(float[] f){
 		if(f[0]==0 && f[1]==0){
 			return;
 		}else if (f[0]==0 && f[1]!=0 ){
 			f[1]=f[1]/Math.abs(f[1]);
 		}else if( f[0]!=0 && f[1]==0){
 			f[0]=f[0]/Math.abs(f[0]);
 		}else{
 			tempf = (float)Math.sqrt(f[0]*f[0]+f[1]*f[1]); 
 			f[0]/= tempf;
 			f[1]/= tempf;
 		}
 	}
 	
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
