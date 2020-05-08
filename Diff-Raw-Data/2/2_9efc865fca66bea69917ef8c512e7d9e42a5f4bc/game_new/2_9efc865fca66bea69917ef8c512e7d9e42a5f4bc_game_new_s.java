 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit; 
 import java.awt.Point;
 import java.awt.Event;
 import java.awt.Graphics;
 import java.awt.image.MemoryImageSource;
 import java.io.IOException;
 import java.applet.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import render.*;
 import com.jcraft.jorbis.JOrbisBGM;
 
 public class game_new extends RenderApplet{
 	
 	static final double explodeTime = .25;	// how long a pumpkin takes to explode
 	static final double blinkTime = .15;		// how long a ghost takes to blink out
 	static final double ghostTime = 20.;// how long a ghost takes to approach
 	static final double pumpkinTime = 20.;	// how long a pumpkin takes to approach
 	static final double startDist = 50.;		// distance from which enemies appear
 	static final double ouchTime = .25;
 	
 	static final double warningTime = .25;
 	static final double levelUpTime = 1.5;
 	static final double ringRadius = 10;// radius of the bunker
 	static final int lvlUpAniPieceNum = 20;
 	
 	Font bigFont = new Font("Helvetica", Font.BOLD, 23);
 	Font Font1 = new Font("Broadway", Font.BOLD, 26);
 	Font Font2 = new Font("Broadway", Font.BOLD, 12);
 	Font Font3 = new Font("Broadway", Font.BOLD, 72);
 	int enemyNumber = 3;
 	int springNumber = 10;
 	int pumpkinNumber = 4;
 	int pumpkinSections = 4;
 	int score = 0;
 	int levelScore = 10;
 	int miss = 0;
 	int bullet = 0;
 	int totalLife = 100;
 	int level = 1;
 	int gameState = 2; // 0:game playing 1:game over 2:game initialized
 	int reStart = 0;
 	int turn = 0, leftKey = 0, rightKey = 0;
 	int gunEnergy = 100;
 	int shootEnergy = 8; //energy consumed per shot
 	int H, W; //window size
 	int mouseX, mouseY; //mouse position
 	
 	double chargeRate = .15; // energy charges rate
 	double reloadCount = 0;
 	double previousTime = 0;
 	double turningAngle = 0;
 	double time = 0;
 	double shootTime = 0;
 	double clickTime[] = new double[enemyNumber];
 	double swingTime;
 	double runTime[] = new double[enemyNumber];
 	double turnStartTime = 0;
 	double ouchTimer = 0;
 	double ouchScale = 0;
 	double point[] = new double[3];
 	double pumpkin_vecs[][] = new double[pumpkinSections*pumpkinSections][5]; // x, y, z, u, v
 	double warningTimer = 0;
 	double levelUpTimer = 0;
 	
 	boolean isCapturedClick = true;
 	boolean isShoot[] = new boolean[enemyNumber];
 	boolean showWarning = false;
 	boolean isAlarm = false;
 	
 	Material boxColor, pumpkinColor1, springColor, stalkColor,lineColor,wallColor,groundColor;
 	Material gunColor,barrelColor,gunheadColor,gunRing1Color,gunRing2Color,gunRing3Color,laserColor,gunWingColor;
 	Material pumpkinFadeColor[] = new Material[enemyNumber];
 	Material shirtColor, skinColor, bodyColor, eyeColor;	// ghost
 	Material lvlUpAniPieceColor1;
 	Material lvlUpAniPieceColor2[] = new Material[lvlUpAniPieceNum];
 	
 	Geometry box[][] = new Geometry[enemyNumber][2];
 	Geometry stalk[] = new Geometry[enemyNumber];
 	Geometry spring[] = new Geometry[springNumber*enemyNumber];
 	Geometry pumpkin[] = new Geometry[pumpkinNumber*enemyNumber];
 	Geometry line,wall,ground;
 	Geometry gun,gunBody,gunHead,barrel,gunRing1,gunRing2,gunRing3,laser,gunWing;
 	//ghost
 	Geometry body[] = new Geometry[enemyNumber];
 	Geometry shoulder_r[] = new Geometry[enemyNumber];
 	Geometry shoulder_l[] = new Geometry[enemyNumber];
 	Geometry eye_r[] = new Geometry[enemyNumber];
 	Geometry eye_l[] = new Geometry[enemyNumber];
 	Geometry torso[] = new Geometry[enemyNumber];
 	Geometry hand_r[] = new Geometry[enemyNumber];
 	Geometry hand_l[] = new Geometry[enemyNumber];
 	//level up animation
 	Geometry lvlUpAniPiece1[] = new Geometry[lvlUpAniPieceNum];
 //	Geometry lvlUpAniPiece2[] = new Geometry[lvlUpAniPieceNum];
 	Geometry g = new Geometry();
 	Geometry obj1;
 	Matrix m;
 	
 
 	Texture texture;
 	
 	AudioClip gunShot = null;
 	AudioClip blowup = null;
 	AudioClip levelUpSound = null;
 	AudioClip wLaugh = null;
 	AudioClip alarm = null;
 	AudioClip hit = null;
 	JOrbisBGM bgm = null;
 	Image bg1,bg2;
 	
 	public boolean keyDown(Event evt, int key){
 		if (key == 'A' || key == 'a'){
 			leftKey = 1;
 			turn = -1;
 			turnStartTime = time;
 		}
 		else if (key == 'D' || key == 'd'){
 			rightKey = 1;
 			turn = 1;
 			turnStartTime = time;
 		}
 			
 		return true;
 	}
 	
 	public boolean keyUp(Event evt, int key){
 		if (key == 'A' || key == 'a'){
 			leftKey = 0;
 			if (rightKey == 0)
 				turn = 0;
 			else 
 				turn = 1;
 		}
 		else if (key == 'D' || key == 'd'){
 			rightKey = 0;
 			if (leftKey == 0)
 				turn = 0;
 			else 
 				turn = -1;
 		}
 			
 		return true;
 	}
 	
 	public boolean mouseDown(Event e, int x, int y) {
 		isCapturedClick = true;
 		if (gameState == 0){
 			if (gunEnergy >= shootEnergy){
 				gunEnergy -= shootEnergy;
 				bullet++;
 		//		totalLife--;
 				g = queryCursor(point);
 				
 				shootTime = time;
 		//		gunTheta = -1*Math.atan2(point[0],-point[2]);
 		//	    gunPhi = 1*Math.atan2(point[1], Math.sqrt(Math.pow((point[0]),2)+Math.pow(-point[2], 2)));	
 				gunShot.play();
 				if (g == null)
 					return true;
 				for (int i=0;i<stalk.length;i++){
 					if((g==stalk[i] || g.isDescendant(box[i][1])) && isShoot[i] == false){
 						isShoot[i] = true;
 						clickTime[i] = time;
 						score=score+2;
 		//				totalLife = totalLife + 10;
 		//				System.out.print("stalk shot");
 					}
 				}
 				for (int i=0;i<pumpkin.length;i++){
 					if (g.isDescendant(pumpkin[i]) && isShoot[(int)i/pumpkinNumber] == false){
 						isShoot[(int)i/pumpkinNumber] = true;
 						clickTime[(int)i/pumpkinNumber] = time;
 						score++;
 					}
 				}
 			}
 			else 
 				System.out.print("run out of energy");
 		}
 		return true;
 	}
 	
 	public boolean mouseMove(Event e, int x, int y){
 		mouseX = x;
 		mouseY = y;
 		return false;
 	}
 		
 	public boolean mouseDrag(Event e, int x, int y) {   
 		mouseX = x;
 		mouseY = y;
 		return false;
 	}
 
 	public boolean mouseUp(Event e, int x, int y) {
 		if (isCapturedClick) {
 			isCapturedClick = false;
 		}
 		if (gameState != 0 && x< 450 && x > 350 && y < 525 && y > 475){
 			wLaugh.play();
 			reStart = 1;
 			
 		}
 		return false;
 	}
 	   
 	public void initialize() {
 		    
 		//hide cursor
 		int[] pixels = new int[16 * 16];  
 		Image image = java.awt.Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));   
 		Cursor transparentCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisiblecursor"); //invisiblecursor 
 		setCursor(transparentCursor);   
 		   
 		if (gunShot == null) {
 			gunShot = getAudioClip(getCodeBase(), "sounds/LASER.wav");
 		}
 		if (blowup == null) {
 			blowup = getAudioClip(getCodeBase(), "sounds/pop8.wav");
 		}
 		if (levelUpSound == null) {
 			levelUpSound = getAudioClip(getCodeBase(), "sounds/levelup.wav");
 		}
 		if (wLaugh == null) {
			wLaugh = getAudioClip(getCodeBase(), "sounds/wLaugh.wav");
 		}
 		if (hit == null) {
 			hit = getAudioClip(getCodeBase(), "sounds/hit.wav");
 		}
 		if (alarm == null) {
 			alarm = getAudioClip(getCodeBase(), "sounds/alarm.wav");
 		}
 		if (bgm == null) {
 			bgm = new JOrbisBGM();
 			bgm.set_URL(getClass().getResource("sounds/SD2D_22.ogg"));
 			new Thread(bgm).start();
 		}
 		bg1 = Toolkit.getDefaultToolkit().getImage("images/bg1.jpg");
 		bg2 = Toolkit.getDefaultToolkit().getImage("images/bg2.jpg");
 		   
 		getRenderer().setH(600);
 		getRenderer().setW(800);
 		H = getRenderer().getH();
 		W = getRenderer().getW();
 
 		setBgColor(0, 0, 0);
 
 		addLight( 1, 1, 1, .8, .85, 1);
 		addLight(-1,-1,-1, 1, 1, 1);
 		addLight(-1, 1, 1, .8, .85, 1);
 		addLight(-1, 1,-1, .5, .5, .5);
 		gunColor = new Material();
 		gunColor.setAmbient(1, 1, 1);
 		gunColor.setDiffuse(.2, .2, .2);
 		gunColor.setSpecular(.5, .5, .5, 20);
 		barrelColor = new Material();
 		barrelColor.setAmbient(0, 1, 0);
 		barrelColor.setDiffuse(0, 1, 0);
 		barrelColor.setSpecular(.8, .5, 0, 20);
 		gunheadColor = new Material();
 		gunheadColor.setAmbient(.1, .1, .1);
 		gunheadColor.setDiffuse(.2, .2, .2);
 		gunheadColor.setSpecular(.2, .2, .2, 20);
 		gunRing1Color = new Material();
 		gunRing1Color.setAmbient(.5, .1, .5);
 		gunRing1Color.setDiffuse(.2, .2, .2);
 		gunRing1Color.setSpecular(.2, .2, .2, 20);
 		gunRing2Color = new Material();
 		gunRing2Color.setAmbient(.5, .1, .5);
 		gunRing2Color.setDiffuse(.2, .2, .2);
 		gunRing2Color.setSpecular(.2, .2, .2, 20);
 		gunRing3Color = new Material();
 		gunRing3Color.setAmbient(.5, .1, .5);
 		gunRing3Color.setDiffuse(.2, .2, .2);
 		gunRing3Color.setSpecular(.2, .2, .2, 20);
 		laserColor = new Material();
 		laserColor.setAmbient(0, .4, 0);
 		laserColor.setDiffuse(0, .2, 0);
 		laserColor.setSpecular(0, 1, 0, 1);
 		gunWingColor = new Material();
 		gunWingColor.setAmbient(.7, .7, .7);
 		gunWingColor.setDiffuse(.1, .1, .1);
 		gunWingColor.setSpecular(.1, .1, .1, 20);
 	      
 	      //ghost
 	      // Body Color
 		bodyColor = new Material();
 		bodyColor.setAmbient(0.4, 0.4,0.4);
 		bodyColor.setDiffuse(0.8, 0.8, 0.75);
 		bodyColor.setSpecular(0,0,0,1);
    
 		// Eye Color
 		eyeColor = new Material();
 		eyeColor.setAmbient(0, 0, 0);
 		eyeColor.setDiffuse(0, 0, 0);
 		eyeColor.setSpecular(1,1,1,10);
 	      
 	      
 		wallColor = new Material();
 		wallColor.setAmbient(0, 0, 0);
 		wallColor.setDiffuse(0, 0, 0);
 		wallColor.setSpecular(.2, .2, .2, 20);
 	      
 		groundColor = new Material();
 		groundColor.setAmbient(.5, .1, .5);
 		groundColor.setDiffuse(.2, .2, .2);
 		groundColor.setSpecular(.2, .2, .2, 20);
 	      
 		lineColor = new Material();
 		lineColor.setAmbient(0.0, 0.0, 0.5);
 		lineColor.setDiffuse(0.0, 0.0, 0.8);
 		lineColor.setSpecular(.2, .5, 0, 20);
 	      
 		boxColor = new Material();
 		boxColor.setAmbient(0.5, 0.0, 0.2);
 		boxColor.setDiffuse(0.0, 0.0, 0.8);
 		boxColor.setSpecular(.2, .5, 0, 20);
 
 		pumpkinColor1 = new Material();
 		pumpkinColor1.setAmbient(0.6, 0.2, 0);
 		pumpkinColor1.setDiffuse(.6, .3, 0);
 	      
 		for (int i = 0; i < enemyNumber; i++){
 			pumpkinFadeColor[i] = new Material();
 			pumpkinFadeColor[i].copy(pumpkinColor1);
 			pumpkinFadeColor[i].setTransparency(0.);
 		}
 		
 		springColor = new Material();
 		springColor.setAmbient(0.2, 0.2, 0.2);
 		springColor.setDiffuse(0.1, 0.1, 0.1);
 		springColor.setSpecular(1, 1, 1, 50);
 
 		stalkColor = new Material();
 		stalkColor.setAmbient(.0, 0.2, .0);
 		stalkColor.setDiffuse(0.0, 0, 0);
 		stalkColor.setSpecular(0, 0, 0, 1);
 		
 		lvlUpAniPieceColor1 = new Material();
 		lvlUpAniPieceColor1.setAmbient(0.0, 0.0, 0.5);
 		lvlUpAniPieceColor1.setDiffuse(0.0, 0.0, 0.8);
 		lvlUpAniPieceColor1.setSpecular(.2, .5, 0, 20);
 		
 
 	}
 
 	   
 	   
 	   
 	   
 	   public void newGame() {
 		   
 		    box = new Geometry[enemyNumber][2];
 			stalk = new Geometry[enemyNumber];
 			spring = new Geometry[springNumber*enemyNumber];
 			pumpkin = new Geometry[pumpkinNumber*enemyNumber];
 			pumpkin_vecs = new double[pumpkinSections*pumpkinSections][5];
 			pumpkinFadeColor = new Material[enemyNumber];
 			//ghost
 			body = new Geometry[enemyNumber];
 			shoulder_r = new Geometry[enemyNumber];
 			shoulder_l = new Geometry[enemyNumber];
 			eye_r = new Geometry[enemyNumber];
 			eye_l = new Geometry[enemyNumber];
 			torso = new Geometry[enemyNumber];
 			hand_r = new Geometry[enemyNumber];
 			hand_l = new Geometry[enemyNumber];
 			obj1 = new Geometry();
 			
 			
 			lvlUpAniPiece1 = new Geometry[lvlUpAniPieceNum];
 //			lvlUpAniPiece2 = new Geometry[lvlUpAniPieceNum];
 			isCapturedClick = true;
 			point = new double[3];
 			//time = 0;
 			shootTime = 0;
 			clickTime = new double[enemyNumber];
 			isShoot = new boolean[enemyNumber];
 			runTime = new double[enemyNumber];
 			ouchTimer = 0;
 		   
 			Y= new double[enemyNumber];
 			swingX = 0;
 		    swingY = new double[enemyNumber];
 		    pop = 0;
 		    dx = new double[enemyNumber]; //moving formula of x-coordinate 
 		    dz = new double[enemyNumber]; //moving formula of z-coordinate
 		    enemyAngle = new double[enemyNumber];
 		    enemySpeed = new double[enemyNumber];
 
 		    isMiss = new boolean[enemyNumber];
 		    c = 0;
 		    isAlarm = false;
 		   for (int i=0;i<enemyNumber;i++)
 			   Y[i] = Math.random();
 		   //when does the enemy start to come out
 		   for (int i=0;i<runTime.length;i++)
 			   runTime[i] = time;
 		   //enemy is shoot or not
 		   for (int i=0;i<isShoot.length;i++)
 				isShoot[i] = false;
 		   //is miss counted or not
 		   for (int i=0;i<isMiss.length;i++)
 			   isMiss[i] = false;
 	            
 	      for (int i = 0; i < enemyNumber; i++){
 	    	  pumpkinFadeColor[i] = new Material();
 	    	  pumpkinFadeColor[i].copy(pumpkinColor1);
 	    	  pumpkinFadeColor[i].setTransparency(0.);
 	      }
 	      	
 	      //add geometries
 	      	gun = getWorld().add();
 	      	gunBody = gun.add().sphere(16);
 	      	barrel = gun.add().cylinder(16);
 	      	gunHead = gun.add().sphere(8);
 	      	gunRing1 = gun.add().torus(16, 16, .1);
 	      	gunRing2 = gun.add().torus(16, 16, .1);
 	      	gunRing3 = gun.add().torus(16, 16, .1);
 	      	gunWing = gun.add().sphere(16);
 	      	laser = gun.add().cylinder(16);
 	      	laser.setVisible(false);
 	      	
 	      	gunBody.setMaterial(gunColor);
 	      	barrel.setMaterial(barrelColor);
 	      	gunHead.setMaterial(gunheadColor);
 	      	gunRing1.setMaterial(gunRing1Color);
 	      	gunRing2.setMaterial(gunRing2Color);
 	      	gunRing3.setMaterial(gunRing3Color);
 	      	gunWing.setMaterial(gunWingColor);
 	      	laser.setMaterial(laserColor);
 	      	
 //	      	wall = getWorld().add().torus(16, 16, .2);
 //	      	wall.setMaterial(wallColor);
 	      	ground = getWorld().add().cube();
 	      	ground.setMaterial(groundColor);
 	      	line = getWorld().add().torus(8, 8, .2);
 	      	line.setMaterial(lineColor);
 	      //the Hierarchy of the enemy
 	      //cube for box->first torus for spring->rest toruses for spring->all spheres for pumpkin->cylinder for stalk
 	      //add all boxes to world
 	      	obj1 = getWorld().add(obj1 = Obj.newObj("wateringcan.obj"));
 	      	Obj.normalizeSize(obj1);
 	      	obj1.setMaterial(pumpkinColor1);
 	      	for (int i=0;i<box.length;i++){
 	      	box[i][0] = getWorld().add().cube();
 	      	
 	      	 //ghost
 	          body[i] = getWorld().add();
 	          box[i][1] = body[i];
 	          shoulder_r[i] = body[i].add();
 	          shoulder_l[i] = body[i].add();
 	          
 	          torso[i] = body[i].add().partialSphere(32);
 	          torso[i].setMaterial(bodyColor);
 	          
 	          eye_r[i] = body[i].add().sphere(16);
 	          eye_r[i].setMaterial(eyeColor);
 	          
 	          eye_l[i] = body[i].add().sphere(16);
 	          eye_l[i].setMaterial(eyeColor);
 	          
 	          hand_r[i] = shoulder_r[i].add().sphere(16);
 	          hand_r[i].setMaterial(bodyColor);
 	          
 	          hand_l[i] = shoulder_l[i].add().sphere(16);
 	          hand_l[i].setMaterial(bodyColor);
 	          
 	          previousTime = time;
 	      	
 	      }
 	      	//add first torus to box
 	      for (int i=0;i<enemyNumber;i++){
 	    	  spring[i*springNumber] = box[i][0].add().torus(16, 16, .2);
 	      }
 	      //add rest torus to the previous one
 	      for (int i = 0;i <enemyNumber;i++)
 	    	  for (int j=1;j<springNumber;j++)
 	    	  spring[i*springNumber+j] = spring[i*springNumber+j-1].add().torus(16, 16, .2);
 	      
 	      	// compute coordinates for the parts of the pumpkin (pre-explosion)
 		    // based on Geometry::globe(int m, int n, double uLo, double uHi, double vLo, double vHi)
 		    for (int u = 0, i = 0;u < pumpkinSections;u++)
 			  for (int v = 0;v < pumpkinSections;v++)
 				  {
 					  double su = (u*1.+.5)/pumpkinSections;	// u scaled to [0..1) 
 					  double sv = (v*1.+.5)/pumpkinSections;
 					  double theta = 2 * su * Math.PI;
 			          double phi = (sv-.5) * Math.PI;
 			          double x = Math.cos(phi) * Math.cos(theta);
 			          double y = Math.cos(phi) * Math.sin(theta);
 			          double z = Math.sin(phi);
 			          
 			          pumpkin_vecs[i][0] = x;
 			          pumpkin_vecs[i][1] = y;
 			          pumpkin_vecs[i][2] = z;
 			          pumpkin_vecs[i][3] = su;
 			          pumpkin_vecs[i][4] = sv;
 			          
 			          i++;
 				  }
 	   
 	      //add all sphere of pumpkin to the last torus of spring
 	      for (int i = 0;i <enemyNumber;i++)
 	    	  for (int j = 0;j < pumpkinNumber;j++)
 	    	  {
 	    		  Geometry g = pumpkin[i*pumpkinNumber+j] = spring[(i+1)*springNumber-1].add();
 	    		  for (double[] v : pumpkin_vecs)
 	    		  {
 	    				 Geometry piece = g.add().globe(4, 4, v[3], v[3]+(1.0/pumpkinSections), v[4], v[4]+(1.0/pumpkinSections));
 	    		  }
 	    	  }
 	      
 	      //add cylinder to the last torus of spring
 	      for (int i=0;i<enemyNumber;i++){
 	    	  stalk[i] = spring[(i+1)*springNumber-1].add().cylinder(16);	          
 	      }    
 	      
 	      for (int i=0;i<stalk.length;i++){
 	    	  stalk[i].setMaterial(stalkColor);
 	      }
 	      //set material of all geometries
 	      for (int i=0;i<box.length;i++)
 	    	  box[i][0].setMaterial(boxColor);
 	      for (int i = 0;i < spring.length;i++)
 	    	  spring[i].setMaterial(springColor);
 	      
 	      for (int i = 0; i < enemyNumber; i++)
 	    	  for (int j = 0; j < pumpkinNumber; j++)
 	    	  {
 		    	  pumpkin[i*pumpkinNumber+j].setMaterial(pumpkinFadeColor[i]);
 	    	  }
 	      
 	      for (int i = 0; i < lvlUpAniPiece1.length; i++){
 	    	  lvlUpAniPiece1[i] = getWorld().add().cube();
 	    	  lvlUpAniPiece1[i].setMaterial(lvlUpAniPieceColor1);
 	      }
 
 	     
 	      
 	      for (int i = 0; i < enemyNumber; i++)
 	      {
 	    	  respawn(i, 1);
 	      }
 	      
 	      
 	   }
 
 //	   double waveDuration = 2.0; // DURATION OF ONE WAVE ANIMATION
 		double Y[]= new double[enemyNumber];
 		double swingX = 0;
 	      double swingY[] = new double[enemyNumber];
 	      double pop = 0;
 	      double dx[] = new double[enemyNumber]; //moving formula of x-coordinate 
 	      double dz[] = new double[enemyNumber]; //moving formula of z-coordinate 
 	      double enemyAngle[] = new double[enemyNumber];  // enemy's angle of attack
 	      double enemySpeed[] = new double[enemyNumber];
 //	      double runDu = 5;
 	      boolean isMiss[] = new boolean[enemyNumber];
 	      double c = 0;
 	      double gunTheta, gunPhi;
 //	      int gunInit = 0;
 	      
 	   public void animate(double time) {
 //		   System.out.println(turn);
 //		   gunColor.setAmbient(gunEnergy/100, 1, 1);
 		   
 		   if (gameState != 0 && reStart == 1){
 			   enemyNumber = 3;
 		    	score = 0;
 		    	levelScore = 10;
 		    	miss = 0;
 		    	bullet = 0;
 		    	totalLife = 100;
 		    	level = 1;
 		    	turningAngle = 0;
 			   reStart = 0;
 			   gameState = 0; 
 			   newGame();
 			  for (int i=0;i<enemyNumber;i++)
 				  runTime[i]=time;
 		   }
 		   else if (gameState == 2 && reStart == 0){
 			   
 			   totalLife = -1;
 		   }
 		   else{//game playing!! animation playing in game should be here 
 			   m = obj1.getMatrix();
 			   m.translate(0,-1, 0);
 //			   obj1.
 			   if (reloadCount >= chargeRate){
 				   gunEnergy = Math.min(gunEnergy+1, 100);
 				   reloadCount = 0;
 			   }
 			   
 			   if (time - levelUpTimer < levelUpTime){
 				   double lTime = time - levelUpTimer;
 				   double angle;
 				   for (int i=0;i<lvlUpAniPiece1.length;i++){
 					   m = lvlUpAniPiece1[i].getMatrix();
 					   m.identity();
 					   angle = i*2*Math.PI/lvlUpAniPieceNum;
 					   m.translate(0, -3, 0);
 //					   m.translate(0, -i*.4, 0);
 //					   m.translate(ringRadius * Math.sin(angle), lTime*6, ringRadius * Math.cos(angle));
 					   
 					   m.rotateY(-angle);
 					   m.translate(0, lTime*6, -ringRadius);
 					   if (lTime<levelUpTime/2)
 						   m.scale(1.8,1.2*lTime*6,.5);
 					   else
 						   m.scale(1.8, 1.2*(levelUpTime-lTime)*6, .5);
 //					   double scale = lTime/levelUpTime;
 //					   lvlUpAniPieceColor1.setAmbient(0, scale/2, 1-scale/2);
 //					   lvlUpAniPieceColor1.setDiffuse(0, scale/2, 1-scale/2);
 //					   lvlUpAniPieceColor1.setSpecular(.5 ,.2, 0, 20);
 		
 					   
 				   }	   
 			   }
 			   
 
 			   else{
 				   for (int i=0;i<lvlUpAniPiece1.length;i++){
 					   m = lvlUpAniPiece1[i].getMatrix();
 					   m.identity();
 					   m.translate(0,-1000,0);
 				   }
 
 				   lvlUpAniPieceColor1.setAmbient(0.0, 0.0, 0.5);
 				   lvlUpAniPieceColor1.setDiffuse(0.0, 0.0, 0.8);
 				   lvlUpAniPieceColor1.setSpecular(.2, .5, 0, 20);
 				    
 //				   for (int i=0;i<lvlUpAniPiece2.length;i++){
 //					   m = lvlUpAniPiece2[i].getMatrix();
 //					   m.identity();
 //					   m.translate(0,-1000,0);
 //				   }
 			   }
 				   
 			   double deltaT = time - previousTime;
 			   
 			   reloadCount = reloadCount + time - previousTime;
 			   previousTime = time;
 			   
 			   if (turn != 0)
 			   {
 				   turningAngle += turn * Math.PI/2. * (time-turnStartTime);
 				   turnStartTime = time;
 			   }
 			   
 			   if(score >= levelScore){
 				   	  levelUpTimer = time;
 					  getWorld().child = null;	  
 					  levelScore = levelScore + 20;
 					  bullet = 0;
 	//				  totalLife = totalLife + 20;
 					  totalLife = 100;
 					  gunEnergy = 100;
 					  level++;
 					  enemyNumber++;
 					  levelUpSound.play();
 					  newGame();
 					  
 			   }
 			   
 			   if(totalLife >0 && totalLife <= 30){
 				   chargeRate = .075;
 				   if (!isAlarm){
 					   alarm.loop();
 					   isAlarm = true;
 				   }
 				   
 				   if (time - warningTimer > 2*warningTime){
 					   warningTimer = time;
 				   }
 				   else if(time - warningTimer <= warningTime)
 					   showWarning = true;
 				   else if(time - warningTimer > warningTime && time - warningTimer < 2*warningTime){
 					   showWarning = false;
 				   }
 			
 				   
 			   }
 			   else {
 				   	chargeRate = .15;
 //				   if (isAlarm){
 					   alarm.stop();
 					   isAlarm = false;
 //				   }
 				   showWarning = false;
 				   
 			   }
 			   //rotate camera
 			   m = getRenderer().getCamera();
 			   m.identity();
 			   m.rotateY(turningAngle);
 			   
 			   if(totalLife <=0 && gameState == 0){
 				   getWorld().child = null;
 				   gameState = 1;
 			   }
 			   
 		      this.time = time;
 		      
 		      m = getWorld().getMatrix();
 		      m.identity();
 		      //set gun
 		      setGun(time);
 		      
 		            
 		      for (int i=0;i<enemyNumber;i++)
 		    	  swingY[i] = Y[i]*Math.cos(3*(time - swingTime)+Y[i]); //this is how they swing their head;
 		      
 		    pop = 0.5;
 		    
 		    //setEnviornment
 		    m = line.getMatrix();
 		    m.identity();
 		    m.translate(0,-3, 0);
 		    m.rotateX(Math.PI/2);
 		    m.scale(10,10,5);
 //		    m = wall.getMatrix();
 //		    m.identity();
 //		    m.translate(0, -3, 0);
 //		    m.rotateX(Math.PI/2);
 //		    m.scale(60,60,200);
 		    m = ground.getMatrix();
 		    m.identity();
 		    m.translate(0, -3.5, 0);
 		    m.scale(100,.2,100);
 		    
 		    if (ouchTimer > 0 || ouchScale > 0)
 		    {
 		    	if (ouchTimer > 0) {
 		    		ouchScale = 1;
 		    		ouchTimer -= deltaT;
 		    	}
 		    	if (ouchScale < .001)
 		    	{
 		    		ouchScale = 0;
 		    	}
 		    	
 		    	lineColor.setAmbient(0.5*ouchScale, 0.0, 0.5*(1.-ouchScale));
 			    lineColor.setDiffuse(0.8*ouchScale, 0.0, 0.8*(1.-ouchScale));
 			    lineColor.setSpecular(.2, .5, 0, 20);
 		    	ouchScale /= 1.1;
 		    }
 		    else
 		    {
 		    	lineColor.setAmbient(0.0, 0.0, 0.5);
 			    lineColor.setDiffuse(0.0, 0.0, 0.8);
 			    lineColor.setSpecular(.2, .5, 0, 20);
 		    }
 		    
 		      for (int i=0;i<box.length;i++){ // here is how the enemies move
 	//	    	  m.scale(2);
 		    	  
 		    	  
 		    	  
 		    	  if (box[i][0].isVisible) {
 		    		  // pumpkin movements
 		    		  
 		    		  double t = 2*(time-runTime[i])-i;
 		    		  
 		    		  //enemyAngle[i] = (i/enemyNumber)*2.*Math.PI; //Math.atan2(dz[i], dx[i]);
 		    		  
 			    	  //dx[i] = (i-enemyNumber/2)*8/(1+(time-runTime[i])/4); // set x; they are separated by there index and will closer to each other when they moving towards you 
 			    	  //dz[i] = -startDist+2.5*(2*(time-runTime[i])-i); //moving outward
 		    		  double dist = (pumpkinTime-t)/pumpkinTime*startDist;
 		    		  if (enemySpeed[i] != 0)
 		    		  {
 		    		  dx[i] = Math.cos(enemyAngle[i])*dist;
 		    		  dz[i] = Math.sin(enemyAngle[i])*dist;
 		    		  }
 		    		  
 			    	  m = box[i][0].getMatrix();
 			    	  m.identity();
 		
 			    	  if (t < Math.PI){
 				    	  // first big bounce (from below playfield)
 			    		  m.translate(dx[i], 3*Math.abs(Math.sin(t))-7+4*t/Math.PI, dz[i]);
 			    	  } else {
 			    		  m.translate(dx[i], -3+3*Math.abs(Math.sin(t)), dz[i]);
 			    	  }
 			    	  m.scale(.5);
 			    	  m.rotateY(Math.PI/2-enemyAngle[i]);
 		    	  }
 		    	  
 		    	  else if (box[i][1].isVisible) {
 				      //ghost
 		    		  
 		    		  double t = time-runTime[i];
 		    		  //System.out.println("ghost!");
 		    		  double dist = (ghostTime-t)/ghostTime*startDist;
 		    		  double[] ghostFocus = new double [3];	// center of the ghost's motion
 		    		  Vec.set(ghostFocus, Math.cos(enemyAngle[i])*dist,
 		    				  0, Math.sin(enemyAngle[i])*dist);
 		    		  double swayScale = dist/6.0*Math.sin(t*2.);
 		    		  double[] ghostTrans = new double[3];	// side-to-side motion is along this vector
 		    		  Vec.set(ghostTrans, Math.cos(enemyAngle[i]+Math.PI/2.)*swayScale, 0,
 		    				  Math.sin(enemyAngle[i]+Math.PI/2.)*swayScale);
 	//
 		    		  if (enemySpeed[i] != 0)
 		    		  {
 		    			  dx[i] = ghostFocus[0]+ghostTrans[0];
 		    		  	dz[i] = ghostFocus[2]+ghostTrans[2];
 		    		  }
 	
 		    		  m = box[i][1].getMatrix();
 		    		  m.identity();
 		    		  m.translate(dx[i], 0, dz[i]);
 		    		  m.rotateX(Math.PI/2);
 		    		  m.rotateZ(Math.PI/2.+enemyAngle[i]);
 		    		  m.scale(1.2,1.1,1.5);
 		    		  m.scale(0.5);
 		    		  
 		    		    m = torso[i].getMatrix();
 		    		    m.identity();
 					  		  	
 					  	m = eye_r[i].getMatrix();
 					  	m.identity();
 					  	m.translate(0.35,0.8,-0.35);
 					  	m.scale(0.25,0.25,0.25);
 					  	
 					  	m = eye_l[i].getMatrix();
 					  	m.identity();
 					  	m.translate(-0.35,0.8,-0.35);
 					  	m.scale(0.25,0.25,0.25);
 					  	
 					  	m = shoulder_r[i].getMatrix();
 					  	m.identity();
 					  	m.translate(0.8, 0, -0.25);
 					  	
 					  	m = shoulder_l[i].getMatrix();
 					  	m.identity();
 					  	m.translate(-0.8, 0, -0.25);
 					  		  	
 					  	m = hand_r[i].getMatrix();
 					  	m.identity();
 					  	m.translate(0.4, 0.1, 0);
 				      	m.rotateY(Math.PI*8/12);
 				      	m.scale(0.3,0.25,0.60);
 				      	
 				    	m = hand_l[i].getMatrix();
 					  	m.identity();
 					  	m.translate(-0.4, 0.1, 0);
 				      	m.rotateY(-Math.PI*8/12);
 				      	m.scale(0.3,0.25,0.60);
 		    	  }
 		      }
 		      
 		      for (int i=0;i<enemyNumber;i++){
 		    	  if (dx[i]*dx[i]+dz[i]*dz[i]<(5*5)){ // if one cross the bar
 		    		  if (isShoot[i] == false && isMiss[i] == false && gameState == 0){
 		    			  miss++;
 		    			  
 		    			  if (box[i][0].isVisible == true )
 		    			  {		
 		    				  hit.play();
 		    				  totalLife = totalLife-5;
 			    			  ouchTimer = ouchTime;
 		    			  }
 		    			  else if (box[i][1].isVisible == true )
 		    			  {	
 		    				  hit.play();
 		    				  totalLife = totalLife-10;
 			    			  ouchTimer = ouchTime;
 		    			  }
 		    			  isMiss[i] = true;
 		    		  }
 		    		  enemySpeed[i] = 0;
 		    		  for (int j=0;j<pumpkinNumber;j++){
 			    		  pumpkin[i*pumpkinNumber+j].setVisible(false);
 			    		  spring[i*springNumber+j].setVisible(false);
 			    	  }
 			    	  stalk[i].setVisible(false);
 			    	  box[i][0].setVisible(false);
 			    	  box[i][1].setVisible(false);
 			    	  isShoot[i] = false;
 			    	  if (Math.random()<.08){ //they have 8% chance every frame to go back to the origin and appear again
 			    		  respawn(i, .7);
 			    	  }
 		    	  }
 		      }
 		      
 		      for (int i=0;i<enemyNumber;i++){
 			      m = spring[i*springNumber].getMatrix(); //the first torus of the spring
 			      m.identity();
 			      m.rotateX(-Math.PI/2);
 			      m.translate(0, 0, 2*pop);
 			      m.rotateY(.1*swingY[i]);
 			      m.scale(.3);
 		      }
 		      for (int i = 0;i <enemyNumber;i++){ // spring movement
 		    	  for (int j=1;j<springNumber;j++){
 		    	  
 		    	  m = spring[i*springNumber+j].getMatrix();
 			      m.identity();	     
 			      m.rotateX(.1*swingX);
 			      m.rotateY(.2*swingY[i]);
 			      m.translate(0, 0, pop+Math.random()*.1);
 	
 		    	  }
 		      }
 		      for (int i = 0;i <enemyNumber;i++){ //make spheres to the pumpkin
 		    	  for (int j = 0;j < pumpkinNumber;j++){
 		    		  Geometry pg = pumpkin[i*pumpkinNumber+j];
 		    		  for (int c=0; c < pg.nChildren(); c++) {
 		    			  Matrix cm = pg.child(c).getMatrix();
 		    			  cm.identity();
 		    		  }
 				      m = pumpkin[i*pumpkinNumber+j].getMatrix();
 				      m.identity();
 				      m.translate(0, 0, 8*pop);
 				      m.rotateZ(i*pumpkinNumber+j*Math.PI/5);
 				      m.scale(8*pop,4*pop,6*pop);
 				      m.scale(2);
 				      
 		    	  }
 		      
 		    	  // normally opaque
 		    	  pumpkinFadeColor[i].setTransparency(0.);
 			  }
 		      
 		      for (int i=0;i<enemyNumber;i++){ // the stalk
 			      m = stalk[i].getMatrix();
 			      m.identity();
 			      m.translate(0, 0, 12*pop);
 			      m.scale(.45,.45,1);
 			      m.rotateX(Math.PI/10);
 			      m.scale(2.5);
 			      
 		      }
 	
 		      for (int i=0;i<enemyNumber;i++){
 	
 		    	  //what if the pumpkin get shot
 		    	  if (box[i][0].isVisible) {
 		    		  if (time-clickTime[i]<explodeTime && isShoot[i] == true){ //explode!
 		    			  double s = 1+(time - clickTime[i])/explodeTime*3;
 	
 		    			  if (stalk[i].isVisible) {
 		    				  // using the visibility of the stalk as a kludge to avoid
 		    				  // playing twice
 		    				  blowup.play();
 		    				  stalk[i].setVisible(false);
 		    			  }
 	
 		    			  for (int j=0;j<pumpkinNumber;j++){
 		    				  Geometry pg = pumpkin[i*pumpkinNumber+j];
 	
 		    				  for (int c=0; c < pg.nChildren(); c++) {
 		    					  Matrix cm = pg.child(c).getMatrix();
 		    					  double[] pv = pumpkin_vecs[c];
 	
 		    					  cm.identity();
 		    					  // expand outward
 		    					  cm.translate((s-1)*pv[0], (s-1)*pv[1] , (s-1)*pv[2]);
 		    				  }
 		    			  }
 	
 		    			  // fade out this pumpkin's bits
 		    			  pumpkinFadeColor[i].setTransparency((time - clickTime[i])/explodeTime);
 	
 	
 		    		  }
 		    		  else if (time-clickTime[i]>=explodeTime && isShoot[i] == true){ //then disappear
 		    			  for (int j=0;j<pumpkinNumber;j++){
 		    				  pumpkin[i*pumpkinNumber+j].setVisible(false);
 		    				  spring[i*springNumber+j].setVisible(false);
 		    			  }
 		    			  stalk[i].setVisible(false);
 		    			  box[i][0].setVisible(false);
 		    			  box[i][1].setVisible(false);
 		    			  isShoot[i] = false;
 		    			  if (Math.random()<.5){ //they have 50% chance to go back to the origin and appear again; or they have to wait until they cross the bar
 		    				  System.out.println("instant respawn "+i);
 		    				  respawn(i, .7);
 		    			  }
 		    			  else
 		    			  {
 		    				  System.out.println("defer respawn "+i);
 		    				  dx[i]=0;
 		    				  dz[i]=0;
 		    			  }
 		    		  }
 		    	  }
 			      //what if a ghost gets shot
 			      else if (box[i][1].isVisible)
 			      {
 		    		  if (time-clickTime[i]<blinkTime && isShoot[i] == true){ // blink!
 		    			  double s = 1+(time - clickTime[i])/blinkTime*5;
 	
 	
 		    			  m = torso[i].getMatrix();
 		    			  m.scale(1./s, 1./s, s);
 		    		  }
 		    		  else if (time-clickTime[i]>=blinkTime && isShoot[i] == true){ // disappear
 			    		  box[i][1].setVisible(false);
 			    		  
 			    		  blowup.play();
 		    			  
 			    		  isShoot[i] = false;
 			    		  if (Math.random()<.5){ //they have 50% chance to go back to the origin and appear again; or they have to wait until they cross the bar
 			    			  System.out.println("instant respawn "+i);
 			    			  respawn(i, .7);
 			    		  }
 			    		  else
 			    		  {
 			    			  System.out.println("defer respawn "+i);
 			    			  dx[i]=0;
 			    			  dz[i]=0;
 			    		  }
 			    	  }
 			      }
 			   }
 		   }
 	   }
 	   Color C = new Color(255,255,255,100); 
 
 	   public void drawOverlay(Graphics g) {
 		   
            if(gameState == 0){
            g.setColor(C);
            g.fillOval((int)(150*1.0)-50+400+180, (int)(150*1.0)-110-20,100,100);
            g.setColor(Color.green);
            g.fillArc((int)(150*1.0)-50+400+180, (int)(150*1.0)-110-20, 100, 100, 135-(int)(turningAngle/Math.PI*360/2), -90);
            //g.setColor(Color.RED);
 	           for(int i=0; i<enemyNumber; i++){
 	               if(box[i][0].isVisible == true){
 	                   g.setColor(Color.RED);
 	                   double tempDouble = (box[i][0].getMatrix().get(3, 0))*box[i][0].vertices[0][0] + 
 	                   (box[i][0].getMatrix().get(3, 1))*box[i][0].vertices[0][1] +
 	                   (box[i][0].getMatrix().get(3, 2))*box[i][0].vertices[0][2] +
 	                   (box[i][0].getMatrix().get(3, 3))*box[i][0].vertices[0][3];
 	                   
 	                   g.fillOval((int)(((((box[i][0].getMatrix().get(0, 0))*box[i][0].vertices[0][0] + 
 	                           (box[i][0].getMatrix().get(0, 1))*box[i][0].vertices[0][1] +
 	                           (box[i][0].getMatrix().get(0, 2))*box[i][0].vertices[0][2] +
 	                           (box[i][0].getMatrix().get(0, 3))*box[i][0].vertices[0][3])/tempDouble)*0.8 + 150)*1.0)+400+180, (int)(((((box[i][0].getMatrix().get(2, 0))*box[i][0].vertices[0][0] + 
 	                           (box[i][0].getMatrix().get(2, 1))*box[i][0].vertices[0][1] +
 	                           (box[i][0].getMatrix().get(2, 2))*box[i][0].vertices[0][2] +
 	                           (box[i][0].getMatrix().get(2, 3))*box[i][0].vertices[0][3])/tempDouble)*0.8 + 150)*1.0)-60-20, 5, 5);
 	               } 
 	               if(box[i][1].isVisible == true){
 	                   g.setColor(Color.blue);
 	                   
 	                   g.fillOval((int)(dx[i]*0.8+150+400+180),(int)(dz[i]*0.8+150-60-20),5,5);
 	               }
                if (time - levelUpTimer < levelUpTime){
             	   g.setFont(Font3);
             	   g.setColor(Color.yellow);
             	   g.drawString("Level Up!", W/2-180, H/2);
                }
             	   
                
 	           }
 	           g.setColor(Color.green);
 	           g.fillOval((int)(145*1.0)+400+180+2, (int)(150*1.0)-60-20-1, 5, 5);
 
            }
            
            if( gameState == 2 ){
          	  
         	   g.drawImage(bg1, 0, 0, null);
         	   g.setFont(bigFont);
         	   g.setColor(Color.orange);
         	   g.fillRect(350, 475, 100, 50);
         	   g.setColor(Color.black);
         	   g.drawString("Play", 375, 510);
 	                  
            }
            else if ( gameState == 1 ){
         	   g.drawImage(bg2, 0, 0, null);
                g.setFont(bigFont);
                g.setColor(Color.orange);
                g.fillRect(350, 475, 100, 50);
                g.setColor(Color.black);
                g.drawString("Play", 375, 510);
                g.setColor(Color.red);
                g.drawString("Game Over !", 210+100, 160+50);
                g.drawString("Total Score: "+score, 200+100, 190+50);
                g.drawString("Final  Level: "+level, 200+100, 220+50);
            }
            else{ 
         	   if (!showWarning){
         		   for (int i=0;i<totalLife;i++){
         			   g.setColor(new Color(Math.max(0,(255-3*i)),Math.min(255,0+3*i),0));
         			   g.fillRect(30+3*i, 90, 3, 14);
 	               }
         	   }
 	              for (int i=0;i<gunEnergy;i++){
 		           	   g.setColor(new Color(200,Math.min(255,50+3*i),0));
 		           	   g.fillRect(30+3*i, 110, 3, 14);
 		          }
 	              
 	              g.setColor(Color.white);
 	              g.drawRect(30, 90, 300, 14);
 	              g.drawRect(30, 110, 300, 14);
 	              
 	              g.setFont(Font1);
 	              g.drawString("NEXT LEVEL: "+levelScore, W/2-100, 30);
 	              g.drawString("level: "+ level, 50, 30);
 	              g.drawString(""+score, W/2, 70);
 	              g.setFont(Font2);
 	              g.drawString("Energy: "+gunEnergy, 40, 122);
 	              g.drawString("Life: "+ totalLife, 40, 102);
 	              
            
            
            
 	              if(showWarning){
 	            	  g.setFont(Font1);
 	            	  g.setColor(Color.red);
 	            	  g.drawString("Warning!! ", W/2 - 50, 200);
 	            	  g.drawString("Life is low!! ", W/2 - 50, 250);
 	              }
            }
            //draw the front sight
            g.setColor(Color.white);
            g.drawOval(mouseX-50, mouseY-50, 100, 100);
            g.drawLine(mouseX-50, mouseY, mouseX+50, mouseY);
            g.drawLine(mouseX, mouseY-50, mouseX, mouseY+50);
        }
 
 	   
 	   public void setGun(double time){
 //		   	  gunTheta = -.3*Math.atan2(mouseX-800/2,50);
 //		      gunPhi = -.3*Math.atan2(mouseY-600/2, Math.sqrt(Math.pow((mouseX-800/2),2)+Math.pow(50, 2)));
 		   
 		      m = gun.getMatrix();
 		      m.identity();
 		      m.rotateY(-turningAngle);
 		      m.translate(0,-2, 3);
 //		      System.out.println(mouseX-W/2+" "+gunTheta+" "+W+" "+H);
 		      m.rotateY(gunTheta);
 		      m.rotateX(gunPhi);
 //		      m.scale(.1,.1,100);	      
 		      m = gunBody.getMatrix();
 		      m.identity();
 		      m.scale(.5, .6,2); 
 		      m = barrel.getMatrix();
 		      m.identity();
 		      m.translate(0, 0, -3);
 		      m.scale(.06,.06,1);
 		      m = gunHead.getMatrix();
 		      m.identity();
 		      m.translate(0, 0, -4);
 		      m.scale(.15);
 		      m = gunRing1.getMatrix();
 		      m.identity();
 		      m.translate(0, 0, -2);
 		      m.scale(.3,.3,1);
 		      m = gunRing2.getMatrix();
 		      m.identity();
 		      m.translate(0, 0, -2.5);
 		      m.scale(.3,.3,1);
 		      m = gunRing3.getMatrix();
 		      m.identity();
 		      m.translate(0, 0, -3);
 		      m.scale(.3,.3,1);
 		      m = gunWing.getMatrix();
 		      m.identity();
 		      m.translate(0, .2, 1);
 		      m.scale(.1,.6,.6);
 		      if (time-c>.5)
 		    	  c = time;
 		      gunRing1Color.setAmbient((time-c)*2, (time-c)*2, 0);
 		      gunRing2Color.setAmbient(0, (time-c)*2, 0);
 		      gunRing3Color.setAmbient(0, (time-c)*2, (time-c)*2);
 
 		    //set laser
 		      m = laser.getMatrix();
 		      m.identity();
 		      m.translate(0, 0, -50);
 		      m.scale(.07, .07, 50);
 		      if (time - shootTime<.2)
 		    	  laser.setVisible(true);
 		      else {
 		    	  laser.setVisible(false);
 			   	  gunTheta = -.2*Math.atan2(mouseX-800/2,80);
 			      gunPhi = -.2*Math.atan2(mouseY-600/2, Math.sqrt(Math.pow((mouseX-800/2),2)+Math.pow(80, 2)));
 		      }
 	   }
 
 	  void respawn(int i, double pumpkinProbability) {
 		  runTime[i] = time;
 		  //dx[i] = (i-enemyNumber/2)*8/(1+(time-runTime[i])/4);
 	 	  //dz[i] = -startDist+5*(time-runTime[i]);
 		  double enemyArc = (2. - ( 1.5*3./(enemyNumber-1) ))*Math.PI;
 		  enemyAngle[i] = i*enemyArc/(enemyNumber-1) - (enemyArc+Math.PI)/2.;
 		  
 	 	  dx[i] = Math.cos(enemyAngle[i])*startDist;
 	 	  dz[i] = Math.sin(enemyAngle[i])*startDist;
 		  for (int j=0;j<pumpkinNumber;j++){
 	 		  pumpkin[i*pumpkinNumber+j].setVisible(true);
 	 		  spring[i*springNumber+j].setVisible(true);
 	 	  }
 	 	  stalk[i].setVisible(true);
 	 	  if(Math.random()<pumpkinProbability){
 	 		  box[i][0].setVisible(true);
 	 		  box[i][1].setVisible(false);
 	 		  //System.out.println("pumpkin");
 	 		  enemySpeed[i] = 50/4;
 	 	  }else{
 	 		  box[i][0].setVisible(false);
 		    	  box[i][1].setVisible(true);
 		 		  //System.out.println("ghost");
 		      enemySpeed[i] = 50/4;
 	 	  }
 	 	 isShoot[i] = false;
 	 	 isMiss[i] = false;
 	 	 
 	 	 Y[i] = Math.random(); //get a new swing coefficient
 	 	 
 	 	 
 	 	 {
 	 		 // move offscreen so it doesn't show up yet this frame
 	 		 Matrix m = box[i][0].getMatrix();
 	 		 m.identity();
 	 		 m.translate(-1000,-1000,-1000);
 	 		 
 	 		 m = box[i][1].getMatrix();
 	 		 m.identity();
 	 		 m.translate(-1000,-1000,-1000);
 	 	 }
  	  }
 
 }
