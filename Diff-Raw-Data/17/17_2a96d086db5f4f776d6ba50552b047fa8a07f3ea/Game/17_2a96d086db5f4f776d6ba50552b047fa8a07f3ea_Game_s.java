 import static org.lwjgl.opengl.GL11.*;
 
 import org.lwjgl.opengl.*;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 import de.matthiasmann.twl.utils.PNGDecoder;
 import pathFinding.AStarPathFinder;
 import pathFinding.Path;
  
 public class Game{
 	public static enum State {
 		INTRO,UPGRADE,SELECT_LEVEL,PLAY,BACKTOMENU,PAUSE,HELP,LVCOMPLETE,LVFAILED;
 	}
 	public static State state = State.INTRO;
 	private int fade;
 	private boolean changingState;
 	private State nextState;
 	private int nextLV;
 	public static enum CurrentButton {
 		NONE,MENU,PLAY,PLAY_INTRO,PAUSE,HELP,BACKTOUPGRADE,CONTINUE,BCK_TO_MENU_YES,BCK_TO_MENU_NO,
 		LV1,LV2,LV3,LV4,LV5,LV6,LV7,LV8,LV9,LV10,
 		UPGRD_MINIGUN,UPGRD_SHOTGUN,UPGRD_CANNON,UPGRD_ROCKET,
 		UPGRD_ARMOR,UPGRD_SPEED;
 	}
 	public HashMap<Integer, Boolean> unlockLV = new HashMap<Integer, Boolean>();
 	public static int currentLevel;
 	public static int maxLevel = 10;
 	private Button[] btn;
 	private CurrentButton currentButton = CurrentButton.NONE;
 	private Texture fadeTex;
 	private Texture backgndIntro;
 	private Texture backgndUpgrade;
 	private Texture moneyBackgnd;
 	private Texture backgndSelectLV;
 	private Texture selectGunBar;
 	private Texture pauseLabel;
 	private Texture helpLabel;
 	private Texture backToMenuLabel;
 	private Texture lvCompleteLabel;
 	private Texture lvFailedLabel;
 	private Texture profitLabel;
 	private Texture profitMiniLabel;
 	private Texture HPpipe;
 	private Texture HPred;
 	private Texture HPblue;
 	private Texture labelMinigun;
 	private Texture labelShotgun;
 	private Texture labelCannon;
 	private Texture labelRocket;
 	private Texture labelMinigun_unuse;
 	private Texture labelShotgun_unuse;
 	private Texture labelCannon_unuse;
 	private Texture labelRocket_unuse;
 	private Texture labelMinigun_lock;
 	private Texture labelShotgun_lock;
 	private Texture labelCannon_lock;
 	private Texture labelRocket_lock;
 	private Texture labelTextPrice;
 	private Texture labelTextArmor;
 	private Texture labelTextSpeed;
 	private Texture labelTextMinigun;
 	private Texture labelTextShotgun;
 	private Texture labelTextCannon;
 	private Texture labelTextRocket;
 	private Texture number;
 	private Texture number_brown;
 	private Texture number_no_gold;
 	private boolean not_enough_gold;
 	private int Label_X;
 	private int Label_Y;
 	private int miniLabel_Y;
 	private int timeShowLabel;
 	private static ArrayList<Entity> entities = new ArrayList<Entity>();
 	private static ArrayList<Entity> removeList = new ArrayList<Entity>();
     //public Map map;
     public static MyTank player;
     static private int camera_x,camera_y,camera_w,camera_h;
     static private int camera_x_tmp,camera_y_tmp;
     static final int WORLD_W,WORLD_H;
     private float gunRotation = 0;
     float bodyAng = 0;
     private static long timerTicksPerSecond = Sys.getTimerResolution();
     private long delta;
     private long lastLoopTime = getTime();
     private long lastFpsTime;
     private int fps;
     public static SoundManager soundManager;
     public static int SOUND_HIT;
     public static int SOUND_FIRE_MINIGUN;
     public static int SOUND_FIRE_SHOTGUN;
     public static int SOUND_FIRE_CANNON;
     public static int SOUND_FIRE_ROCKET;
     public static int SOUND_CHANGE_GUN;
     public static int SOUND_SHOT_BRICK;
     public static int SOUND_SHOT_BOX;
     public static int SOUND_SHOT_TANK;
     public static int SOUND_BOMB_TANK;
     public static int SOUND_BOMB_BRICK;
     public static int SOUND_BOMB_BOX;
     public static int SOUND_BOMB_OILTANK;
     public static int SOUND_CLICK;
     public static int SOUND_RELEASE;
     public static int SOUND_PLAY;
     public static int SOUND_GOT_HP_POTION;
     public static int SOUND_GOT_GOLD;
     public static int SOUND_UPGRADE;
     public static int SOUND_NO_MONEY;
     
     public static int SOUND_BGM_INTRO;
     public static int SOUND_BGM_PLAY;
     public static int CURRENT_SOUND;
     
     public float initBulletX,initBulletY;
     
 	private EnemyMinigun[] enemyMinigun;
 	public static int numEnemy;
 	
 	private Brick[] brick;
 	private Brick_2[] brick_2;
 	private Box[] box;
 	private BombWall[] bmWall;
 	private OilTank[] oilTank;
 	private Turret[] turret;
 	public static Gold[] gold;
 	public static int goldIndex;
 	
 	//#
 	public path_map map;
 	private Path path;
 	private AStarPathFinder finder;
 	public int path_time;
     
     static{
     	//initial world size
         WORLD_W = 1920;
         WORLD_H = 1200;
     }
    
     public Game() throws IOException, LWJGLException{
     	path_time = 0;
     	//initialed value of camera
     	camera_x = 0;
         camera_y = 0;
         camera_w = 640;
         camera_h = 650;
         
         Display.setIcon(new ByteBuffer[] {
 		        loadIcon("game.png")  // "res/game.png" size 32x32
 		    });
         //Mouse.setGrabbed(true);
 
         //initial display
         Display.setDisplayMode(new DisplayMode(camera_w, camera_h));
         Display.create();
         
         entities.clear();
         soundManager = new SoundManager();
 		soundManager.initialize(20);
 		SOUND_FIRE_MINIGUN	= soundManager.addSound("fire_minigun.wav");
 		SOUND_FIRE_SHOTGUN	= soundManager.addSound("fire_shotGun.wav");
 		SOUND_FIRE_CANNON   = soundManager.addSound("fire_cannon.wav");
 		SOUND_FIRE_ROCKET   = soundManager.addSound("fire_rocket.wav");
 		SOUND_CHANGE_GUN	= soundManager.addSound("changeGun.wav");
 		SOUND_SHOT_BRICK	= soundManager.addSound("shot_brick.wav");
 		SOUND_SHOT_BOX	= soundManager.addSound("shot_box.wav");
 		SOUND_SHOT_TANK	= soundManager.addSound("shot_tank.wav");
 		SOUND_BOMB_TANK	= soundManager.addSound("bomb_tank.wav");
 		SOUND_BOMB_BRICK	= soundManager.addSound("bomb_brick.wav");
 		SOUND_BOMB_BOX	= soundManager.addSound("bomb_box.wav");
 		SOUND_BOMB_OILTANK	= soundManager.addSound("bomb_oil.wav");
 		SOUND_CLICK		= soundManager.addSound("mouse_click.wav");
 		SOUND_RELEASE	= soundManager.addSound("mouse_release.wav");
 		SOUND_PLAY		= soundManager.addSound("play.wav");
 		SOUND_GOT_HP_POTION	= soundManager.addSound("gotHP.wav");
 		SOUND_GOT_GOLD	= soundManager.addSound("gotGold.wav");
 		SOUND_UPGRADE	= soundManager.addSound("upgrade.wav");
 	    SOUND_NO_MONEY	= soundManager.addSound("noMoney.wav");
 		
 		SOUND_BGM_INTRO		= soundManager.addSound("BGM_intro.wav");
 		SOUND_BGM_PLAY		= soundManager.addSound("BGM_play.wav");
 
 		map = new path_map();
 		finder = new AStarPathFinder(map,10,false);
 		
 		for(int i = 1;i <= maxLevel;i++){
 			if(i == 1)
 				unlockLV.put(i, true);
 			else
 				unlockLV.put(i, false);
 		}
 		loadResource();
 		
         //initialization opengl code
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         glOrtho(0 ,640 ,650 ,0 ,-1 , 1);
         //glOrtho(camera_x ,640+camera_x ,650+camera_y ,camera_y ,-1 , 1);
         glMatrixMode(GL_MODELVIEW);
         glEnable(GL_TEXTURE_2D);
         
         glEnable(GL_BLEND);
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
         while(!Display.isCloseRequested()){
         	//clear screen
             glClear(GL_COLOR_BUFFER_BIT);
             
             delta = getTime() - lastLoopTime;
     		lastLoopTime = getTime();
     		lastFpsTime += delta;
     		fps++;
 
     		// update our FPS
     		if (lastFpsTime >= 1000) {
     			//Display.setTitle("Awesome Tank (FPS: " + fps + ")");
     			lastFpsTime = 0;
     			fps = 0;
     		}
     		
     		switch(state){
     		case INTRO:
     		case UPGRADE:
     		case SELECT_LEVEL:
     			if(!soundManager.isPlayingSound() || CURRENT_SOUND != SOUND_BGM_INTRO){
     				soundManager.stopPlayingSound();
     				soundManager.playSound(SOUND_BGM_INTRO);
     				CURRENT_SOUND = SOUND_BGM_INTRO;
     			}
     			break;
 			default:
 				break;
     		}
     		
     		switch(state){
         	case INTRO:
         		draw(backgndIntro,camera_w/2,camera_h/2,camera_w,camera_h);
         		button_In_INTRO_state();
         		if(changingState)
         			fading();
         		else
         			recover();
     			break;
         	case UPGRADE:
         		draw(moneyBackgnd,camera_w/2,camera_h/2,camera_w,camera_h);
         		drawFont(String.valueOf(MyTank.myGold), 530, 85, 1, 1);
         		draw(backgndUpgrade,camera_w/2,camera_h/2,camera_w,camera_h);
         		button_In_UPGRADE_state();
         		if(changingState)
         			fading();
         		else
         			recover();
     			break;
         	case SELECT_LEVEL:
         		draw(backgndSelectLV,camera_w/2,camera_h/2,camera_w,camera_h);
         		button_In_SELECT_LEVEL_state();
         		if(changingState)
         			fading();
         		else
         			recover();
     			break;
     		case PLAY:
     		case PAUSE:
     		case HELP:
     		case BACKTOMENU:
     		case LVCOMPLETE:
     		case LVFAILED:
     			if(!soundManager.isPlayingSound() || CURRENT_SOUND != SOUND_BGM_PLAY){
     				soundManager.playSound(SOUND_BGM_PLAY);
     				CURRENT_SOUND = SOUND_BGM_PLAY;
     			}
     			if(numEnemy <= 0 && state != State.LVFAILED){
     				state = State.LVCOMPLETE;
     				if(currentLevel < maxLevel)
     					unlockLV.put(currentLevel+1, true);
         		}
         		initBulletX = (float)(player.x-Math.cos(0.0174532925*gunRotation)*player.width/1.5);
         		initBulletY = (float)(player.y-Math.sin(0.0174532925*gunRotation)*player.height/1.5);
         		
         		if(state != State.BACKTOMENU && state != State.PAUSE && state != State.HELP){
         			player.setDX(0);
         	        player.setDY(0);
         			if(state != State.LVFAILED)
         				input();
             		
                     //if(!soundManager.isPlayingSound()){
                     	for (int p = 0; p < entities.size(); p++) {
                     		Entity entity = entities.get(p);
                     		
                         	if(entity instanceof MyTank){
                         		player.move(delta, bodyAng);
                     		}else if(entity instanceof EnemyTank){
                     			map.setArrayMap(currentLevel,entities);
                         		finder = new AStarPathFinder(map,10,false);
                     				path = finder.findPath(new UnitMover() 
                     	    		,(int) ((EnemyTank)entity).get_centerX()/map.TILE_SIZE 
                     				,(int) ((EnemyTank)entity).get_centerY()/map.TILE_SIZE
                     				,(int) player.get_centerX()/map.TILE_SIZE 
                     				,(int) player.get_centerY()/map.TILE_SIZE
                     			);
                     			if(path!=null){
                     				path.removeFromLast(3);
                         			
                     			}
                     			((EnemyTank)entity).setPath(path);
                     			((EnemyTank)entity).move(map,delta);
                         	}
                         	else
                         		entity.move(delta);
                        	if(entity instanceof Gold){
                        		if(map.blocked((int)entity.x/map.TILE_SIZE, (int)entity.y/map.TILE_SIZE)){
                         			entity.setDX(0);
             						entity.setDY(0);
                        		}
                        	}
                        	if(entity instanceof Bullet){
            					if(map.blocked((int)entity.x/map.TILE_SIZE, (int)entity.y/map.TILE_SIZE)){
            						if(entity instanceof MyCannonBullet || entity instanceof MyRocketBullet){
             							entities.add(new BombEffect_BigBullet(this,entity.x,entity.y));
             							soundManager.playEffect(SOUND_BOMB_TANK);
             							if(entity instanceof MyRocketBullet)
             								player.rocketReleased = false;
             						} else {
             							entities.add(new BulletShotEffect(this,entity.x,entity.y));
             							soundManager.playEffect(SOUND_SHOT_BRICK);
             						}
             						entity.setDX(0);
             						entity.setDY(0);
             						((Bullet) entity).used = true;
             						removeList.add(entity);
            					}
            				}
             			}
                     //}
                     	
                     
                     
                     // check collisions and move
             		for (int p = 0; p < entities.size(); p++) {
             			for (int s = p + 1; s < entities.size(); s++) {
             				Entity me = entities.get(p);
             				Entity him = entities.get(s);
             				if (me.collidesWith(him)) {
             					me.collidedWith(him);
             					him.collidedWith(me);
             				}
             			}
             		}
             		//set camera
                     setCamera();
         		}
                 glMatrixMode(GL_PROJECTION);
                 glLoadIdentity();
                 glOrtho(camera_x ,640+camera_x ,650+camera_y ,camera_y ,-1 , 1);
                 
         		entities.removeAll(removeList);
         		removeList.clear();
         		/*
         		if(!enemyMinigun[path_time].died){
 	        		
         		
     				path = finder.findPath(new UnitMover() 
     	    		,(int) enemyMinigun[path_time].get_centerX()/map.TILE_SIZE 
     				,(int) enemyMinigun[path_time].get_centerY()/map.TILE_SIZE
     				,(int) player.get_centerX()/map.TILE_SIZE 
     				,(int) player.get_centerY()/map.TILE_SIZE
     				);
     			}
         		
         		if(path!=null){
     				path.removeFromLast(3);
     				
     				if( i == 0 ){
     					///enemyTank[i].Fire(bot_initBulletX, bot_initBulletY, enemyTank[i].gunAngle);
     					System.out.println("fire!");
         			}
         			
     			}
     			enemyMinigun[path_time].setPath(path);
         		path_time++;
         		if(path_time >= enemyMinigun.length){
         			path_time = 0;
         		} 
         		*/
 
         		if(state == State.PAUSE || state == State.BACKTOMENU || state == State.HELP || 
         				state == State.LVCOMPLETE || state == State.LVFAILED)
         			glColor3f(0.5f, 0.5f, 0.5f);
         		
                 //draw
                 map.draw();
                 for ( Entity entity : entities ) {
                 	if(entity instanceof Gold)
                 		entity.draw();
     			}
                 for ( Entity entity : entities ) {
                 	if(!(entity instanceof Effect) && !(entity instanceof Gold))
                 		entity.draw();
     			}
                 for ( Entity entity : entities ) {
                 	if(entity instanceof Effect)
                 		entity.draw();
     			}
                 button_In_PLAY_state();
                 if(changingState)
         			fading();
                 glColor3f(1, 1, 1);
     			break;
     		}
             
             Display.update();
             Display.sync(60);
         }
         soundManager.destroy();
         Display.destroy();
         System.exit(0);
     }
     
     private void recover() {
     	if(state == State.PLAY)
     		fade = 0;
     	if (fade > 0) {
             fade -= 10f;
         } else {
         	fade = 0;
             glColor3f(1, 1, 1);
         }
     	glPushMatrix();
         glColor4f(1, 1, 1, (float) Math.sin(Math.toRadians(fade)));
         draw(fadeTex,camera_w/2,camera_h/2,camera_w,camera_h);
         glColor3f(1, 1, 1);
         glPopMatrix();
     }
     
     private void fading() {
     	if(state == State.BACKTOMENU || state == State.LVCOMPLETE || state == State.LVFAILED)
     		fade = 90;
     	if (fade < 90) {
             fade += 10f;
         } else {
         	fade = 90;
             state = nextState;
             switch(state){
     		case UPGRADE:
     			move_to_UPGRADE_state();
     			break;
     		case SELECT_LEVEL:
     			move_to_SELECT_LEVEL_state();
     			break;
     		case PLAY:
     			move_to_PLAY_state(nextLV);
     			break;
 			default:
 				break;
             }
             changingState = false;
         }
     	glPushMatrix();
         glColor4f(1, 1, 1, (float) Math.sin(Math.toRadians(fade)));
         draw(fadeTex,camera_w/2,camera_h/2,camera_w,camera_h);
         glColor3f(1, 1, 1);
         glPopMatrix();
     }
     
     private void move_to_UPGRADE_state() {
     	btn = new Button[8];            	        		
 		btn[0] = new Button(this,CurrentButton.MENU,215,545);
 		btn[1] = new Button(this,CurrentButton.PLAY,430,545);
 		btn[2] = new Button(this,CurrentButton.UPGRD_MINIGUN,374,254);
 		btn[3] = new Button(this,CurrentButton.UPGRD_SHOTGUN,504,254);
 		btn[4] = new Button(this,CurrentButton.UPGRD_CANNON,374,378);
 		btn[5] = new Button(this,CurrentButton.UPGRD_ROCKET,504,378);
 		btn[6] = new Button(this,CurrentButton.UPGRD_ARMOR,180,252);
 		btn[7] = new Button(this,CurrentButton.UPGRD_SPEED,180,370);
 		state = State.UPGRADE;
     }
     
     private void move_to_SELECT_LEVEL_state() {
     	btn = new Button[11];            	        		
     	btn[0] = new Button(this,CurrentButton.BACKTOUPGRADE,320,550);
     	btn[1] = new Button(this,CurrentButton.LV1,120,250);
     	btn[2] = new Button(this,CurrentButton.LV2,220,250);
     	btn[3] = new Button(this,CurrentButton.LV3,320,250);
     	btn[4] = new Button(this,CurrentButton.LV4,420,250);
     	btn[5] = new Button(this,CurrentButton.LV5,520,250);
     	btn[6] = new Button(this,CurrentButton.LV6,120,370);
     	btn[7] = new Button(this,CurrentButton.LV7,220,370);
     	btn[8] = new Button(this,CurrentButton.LV8,320,370);
     	btn[9] = new Button(this,CurrentButton.LV9,420,370);
     	btn[10] = new Button(this,CurrentButton.LV10,520,370);
     	state = State.SELECT_LEVEL;
     }
     
     private void move_to_PLAY_state(int lv) {
     	SetGame(lv);
     	btn = new Button[7];            	        		
     	btn[0] = new Button(this,CurrentButton.MENU,143,610);
     	btn[1] = new Button(this,CurrentButton.PAUSE,533,610);
     	btn[2] = new Button(this,CurrentButton.CONTINUE,320,350);
     	btn[3] = new Button(this,CurrentButton.BCK_TO_MENU_YES,250,350);
     	btn[4] = new Button(this,CurrentButton.BCK_TO_MENU_NO,390,350);
     	btn[5] = new Button(this,CurrentButton.HELP,570,610);
     	btn[6] = new Button(this,CurrentButton.PLAY,320,480);
     	soundManager.stopPlayingSound();
     	soundManager.playEffect(SOUND_PLAY);
     	state = State.PLAY;
     }
 
 	private void button_In_INTRO_state() {
     	btn = new Button[1];	
 		btn[0] = new Button(this,CurrentButton.PLAY_INTRO,320,495);
 		if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()))
 			btn[0].draw_OnMouseOver();
 		else
 			btn[0].draw();
 		while (Mouse.next()) {
     	    if (Mouse.getEventButtonState()) {
     	        switch (Mouse.getEventButton()) {
     	        case 0:
     	        	if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.PLAY_INTRO;
     	        	} else
             			currentButton = CurrentButton.NONE;
 	        		break;
     	        }
     	    } else {	// mouse release
     	    	switch (Mouse.getEventButton()) {
     	    	case 0:
     	    		if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			soundManager.playEffect(SOUND_RELEASE);
     	    			if(currentButton == CurrentButton.PLAY_INTRO){
     	    				nextState = State.UPGRADE;
     	    				changingState = true;
     	    				//move_to_UPGRADE_state();
 	        			}
             		} else
         				currentButton = CurrentButton.NONE;
         			break;
     	    	}
     	    }
     	}
 	}
 
 	private void button_In_UPGRADE_state() {
     	for(int i = 0;i < btn.length;i++){
 			if(btn[i].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
 				switch(btn[i].thisButton){
 				case UPGRD_MINIGUN:
 				case UPGRD_SHOTGUN:
 				case UPGRD_CANNON:
 				case UPGRD_ROCKET:
 				case UPGRD_ARMOR:
 				case UPGRD_SPEED:
 					if(!btn[i].maxUpgrd)
 						drawPriceLabel(btn[i].thisButton, String.valueOf(btn[i].priceToUpgrd), (int)btn[i].x, (int)btn[i].y-(btn[i].width/2+3));
 					break;
 				default:
 					break;
 				}
 				btn[i].draw_OnMouseOver();
 			} else
     			btn[i].draw();
 		}
 		while (Mouse.next()) {
     	    if (Mouse.getEventButtonState()) {
     	        switch (Mouse.getEventButton()) {
     	        case 0:
     	        	if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.MENU;
     	        	} else if(btn[1].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.PLAY;
     	        	} else if(btn[2].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.UPGRD_MINIGUN;
     	        	} else if(btn[3].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.UPGRD_SHOTGUN;
     	        	} else if(btn[4].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.UPGRD_CANNON;
     	        	} else if(btn[5].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.UPGRD_ROCKET;
     	        	} else if(btn[6].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.UPGRD_ARMOR;
     	        	} else if(btn[7].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.UPGRD_SPEED;
     	        	} else
     	    			currentButton = CurrentButton.NONE;
 	        		break;
     	        }
     	    } else {	// mouse release
     	    	switch (Mouse.getEventButton()) {
     	    	case 0:
     	    		if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			soundManager.playEffect(SOUND_RELEASE);
     	    			if(currentButton == CurrentButton.MENU){
     	    				nextState = State.INTRO;
     	    				changingState = true;
     	    			}            	        		
     	    		} else if(btn[1].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			soundManager.playEffect(SOUND_RELEASE);
     	    			if(currentButton == CurrentButton.PLAY){
     	    				nextState = State.SELECT_LEVEL;
     	    				changingState = true;
     	    				//move_to_SELECT_LEVEL_state();
     	    			}
     	    		} else if(btn[2].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			if(currentButton == CurrentButton.UPGRD_MINIGUN){
     	    				if(MyTank.myGold > btn[2].priceToUpgrd){
     	    					btn[2].upgrade();
     	    					soundManager.playEffect(SOUND_UPGRADE);
     	    				} else {
     	    					not_enough_gold = true;
     	    					soundManager.playEffect(SOUND_NO_MONEY);
     	    				}
     	    			}
     	    		} else if(btn[3].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			if(currentButton == CurrentButton.UPGRD_SHOTGUN){
     	    				if(MyTank.myGold > btn[3].priceToUpgrd){
     	    					btn[3].upgrade();
     	    					soundManager.playEffect(SOUND_UPGRADE);
     	    				} else {
     	    					not_enough_gold = true;
     	    					soundManager.playEffect(SOUND_NO_MONEY);
     	    				}
     	    			}
     	    		} else if(btn[4].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			if(currentButton == CurrentButton.UPGRD_CANNON){
     	    				if(MyTank.myGold > btn[4].priceToUpgrd){
     	    					btn[4].upgrade();
     	    					soundManager.playEffect(SOUND_UPGRADE);
     	    				} else {
     	    					not_enough_gold = true;
     	    					soundManager.playEffect(SOUND_NO_MONEY);
     	    				}
     	    			}
     	    		} else if(btn[5].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			if(currentButton == CurrentButton.UPGRD_ROCKET){
     	    				if(MyTank.myGold > btn[5].priceToUpgrd){
     	    					btn[5].upgrade();
     	    					soundManager.playEffect(SOUND_UPGRADE);
     	    				} else {
     	    					not_enough_gold = true;
     	    					soundManager.playEffect(SOUND_NO_MONEY);
     	    				}
     	    			}
     	    		} else if(btn[6].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			if(currentButton == CurrentButton.UPGRD_ARMOR){
     	    				if(MyTank.myGold > btn[6].priceToUpgrd){
     	    					btn[6].upgrade();
     	    					soundManager.playEffect(SOUND_UPGRADE);
     	    				} else {
     	    					not_enough_gold = true;
     	    					soundManager.playEffect(SOUND_NO_MONEY);
     	    				}
     	    			}
     	    		} else if(btn[7].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			if(currentButton == CurrentButton.UPGRD_SPEED){
     	    				if(MyTank.myGold > btn[7].priceToUpgrd){
     	    					btn[7].upgrade();
     	    					soundManager.playEffect(SOUND_UPGRADE);
     	    				} else {
     	    					not_enough_gold = true;
     	    					soundManager.playEffect(SOUND_NO_MONEY);
     	    				}
     	    			}
     	    		} else
     	    			currentButton = CurrentButton.NONE;
         			break;
     	    	}
     	    }
     	}
 	}
 
 	private void button_In_SELECT_LEVEL_state() {
 		// button BACKTOUPGRADE
 		if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()))
 			btn[0].draw_OnMouseOver();
 		else
 			btn[0].draw();
 		// button LV1 - LVmax
 		for(int i = 1;i < btn.length;i++){
 			if(unlockLV.get(i)){
 				if(btn[i].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())) {
 					btn[i].increaseBtnSize();
 					btn[i].draw();
 				} else {
 					btn[i].decreaseBtnSize();
 					btn[i].draw();
 				}
 			} else {
 				if(btn[i].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())) {
 					btn[i].increaseBtnSize();
 					btn[i].draw_LVlock();
 				} else {
 					btn[i].decreaseBtnSize();
 					btn[i].draw_LVlock();
 				}
 			}
 		}
 		while (Mouse.next()) {
     	    if (Mouse.getEventButtonState()) {
     	        switch (Mouse.getEventButton()) {
     	        case 0:
     	        	boolean btnLVclicked = false;
     	        	for(int i = 1;i <= maxLevel;i++){
     	    			if(btn[i].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()) && unlockLV.get(i)){
     	    				soundManager.playEffect(SOUND_CLICK);
     	    				switch(i){
     	    				case 1:
     	    					currentButton = CurrentButton.LV1;
     	    					break;
     	    				case 2:
     	    					currentButton = CurrentButton.LV2;
     	    					break;
     	    				case 3:
     	    					currentButton = CurrentButton.LV3;
     	    					break;
     	    				case 4:
     	    					currentButton = CurrentButton.LV4;
     	    					break;
     	    				case 5:
     	    					currentButton = CurrentButton.LV5;
     	    					break;
     	    				case 6:
     	    					currentButton = CurrentButton.LV6;
     	    					break;
     	    				case 7:
     	    					currentButton = CurrentButton.LV7;
     	    					break;
     	    				case 8:
     	    					currentButton = CurrentButton.LV8;
     	    					break;
     	    				case 9:
     	    					currentButton = CurrentButton.LV9;
     	    					break;
     	    				case 10:
     	    					currentButton = CurrentButton.LV10;
     	    					break;
     	    				}
     	    				btnLVclicked = true;
     	    			}
     	    		}
     	        	if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        		soundManager.playEffect(SOUND_CLICK);
     	        		currentButton = CurrentButton.BACKTOUPGRADE;
     	        	} else if(btnLVclicked){
     	        	} else
     	    			currentButton = CurrentButton.NONE;
 	        		break;
     	        }
     	    } else {	// mouse release
     	    	switch (Mouse.getEventButton()) {
     	    	case 0:
     	    		boolean btnLVreleased = false;
     	        	for(int i = 1;i <= maxLevel;i++){
     	    			if(btn[i].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()) && unlockLV.get(i)){
     	    				soundManager.playEffect(SOUND_RELEASE);
     	    				btnLVreleased = true;
     	    				if(currentButton == CurrentButton.LV1 || currentButton == CurrentButton.LV2 || 
     	    						currentButton == CurrentButton.LV3 || currentButton == CurrentButton.LV4 || 
     	    						currentButton == CurrentButton.LV5 || currentButton == CurrentButton.LV6 || 
     	    						currentButton == CurrentButton.LV7 || currentButton == CurrentButton.LV8 || 
     	    						currentButton == CurrentButton.LV9 || currentButton == CurrentButton.LV10){
     	    					nextState = State.PLAY;
     	    					nextLV = i;
         	    				changingState = true;
     	    					//move_to_PLAY_state(i);
         	    				break;
         	    			}
     	    			}
     	    		}
     	    		if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	    			soundManager.playEffect(SOUND_RELEASE);
     	    			if(currentButton == CurrentButton.BACKTOUPGRADE){
     	    				nextState = State.UPGRADE;
     	    				changingState = true;
     	    				//move_to_UPGRADE_state();
     	    			}            	        		
     	    		} else if(btnLVreleased){
     	    		} else
     	    			currentButton = CurrentButton.NONE;
         			break;
     	    	}
     	    }
     	}
 	}
 	
 	private void button_In_PLAY_state() {
 		glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
 		glOrtho(0 ,640 ,650 ,0 ,-1 , 1);
 		
 		setTimeToShowProfitMiniLabel();
 		draw(profitMiniLabel, 170, 585-miniLabel_Y, 209, 26);
 		drawFont(String.valueOf(MyTank.profit), 270, 576-miniLabel_Y, 0.7f, 1);
 		
 		draw(selectGunBar, 330, 610, 542, 81);
 		drawHPpipe(360,635,260,21);
 		// ########### btn menu ###########
 		if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()))
 			btn[0].draw_OnMouseOver();
 		else
 			btn[0].draw();
 		// ########### gun label ###########
 		if(!player.unlockGun.get(TankEntity.GunType.MINIGUN)){
 			draw(labelMinigun_lock, 258,600,44,42);
 		} else if(player.gunType == TankEntity.GunType.MINIGUN){
 			draw(labelMinigun, 258,600,44,42);
 		} else
 			draw(labelMinigun_unuse, 258,600,44,42);
 		
 		if(!player.unlockGun.get(TankEntity.GunType.SHOTGUN)){
 			draw(labelShotgun_lock, 327,600,44,42);
 		} else if(player.gunType == TankEntity.GunType.SHOTGUN){
 			draw(labelShotgun, 327,600,44,42);
 		} else
 			draw(labelShotgun_unuse, 327,600,44,42);
 		
 		if(!player.unlockGun.get(TankEntity.GunType.CANNON)){
 			draw(labelCannon_lock, 392,600,44,42);
 		} else if(player.gunType == TankEntity.GunType.CANNON){
 			draw(labelCannon, 392,600,44,42);
 		} else
 			draw(labelCannon_unuse, 392,600,44,42);
 		
 		if(!player.unlockGun.get(TankEntity.GunType.ROCKET)){
 			draw(labelRocket_lock, 462,600,44,42);
 		} else if(player.gunType == TankEntity.GunType.ROCKET){
 			draw(labelRocket, 462,600,44,42);
 		} else
 			draw(labelRocket_unuse, 462,600,44,42);
 		// ########### btn pause ###########
 		if(state == State.PAUSE){
 			btn[1].draw_Clicked();
 		} else {
 			if(btn[1].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()))
 				btn[1].draw_OnMouseOver();
 			else
 				btn[1].draw();
 		}
 		// ########### btn help ###########
 		if(state == State.HELP){
 			btn[5].draw_Clicked();
 		} else {
 			if(btn[5].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()))
 				btn[5].draw_OnMouseOver();
 			else
 				btn[5].draw();
 		}
 		
 		glColor3f(1, 1, 1);
 		// ########### btn continue ###########
 		if(state == State.PAUSE){
 			draw(pauseLabel, 320, 273, 161, 46);
 			if(btn[2].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()))
 				btn[2].draw_OnMouseOver();
     		else
     			btn[2].draw();
 		}
 		// ########### btn play ###########
 		if(state == State.HELP){
 			draw(helpLabel, 320, 273, 282, 526);
 			if(btn[6].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()))
 				btn[6].draw_OnMouseOver();
     		else
     			btn[6].draw();
 		}
 		// ########### btn yes,no ###########
 		if(state == State.BACKTOMENU){
 			draw(backToMenuLabel, 320, 303, 321, 194);
 			for(int i = 3;i <= 4;i++){
 				if(btn[i].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY()))
 					btn[i].draw_OnMouseOver();
 	    		else
 	    			btn[i].draw();
 			}
 		}
 		if(state == State.LVCOMPLETE){
 			setTimeToShowEndgameLabel();
 			draw(lvCompleteLabel, 320+Label_X, 150+Label_Y, 361, 92);
 			draw(profitLabel, 320-Label_X, 490-Label_Y, 359, 74);
 			drawFont(String.valueOf(MyTank.profit), 455-Label_X, 480-Label_Y, 1, 1);
 		}
 		if(state == State.LVFAILED){
 			setTimeToShowEndgameLabel();
 			draw(lvFailedLabel, 320+Label_X, 150+Label_Y, 361, 92);
 			draw(profitLabel, 320-Label_X, 490-Label_Y, 359, 74);
 			drawFont(String.valueOf(MyTank.profit), 455-Label_X, 480-Label_Y, 1, 1);
 		}
 		while (Mouse.next()) {
     	    if (Mouse.getEventButtonState()) {
     	        switch (Mouse.getEventButton()) {
     	        case 0:
     	        	if(state == State.PLAY){
     	        		if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	        		soundManager.playEffect(SOUND_CLICK);
         	        		currentButton = CurrentButton.MENU;
         	        	} else if(btn[1].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	        		soundManager.playEffect(SOUND_CLICK);
         	        		currentButton = CurrentButton.PAUSE;
         	        	} else if(btn[5].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	        		soundManager.playEffect(SOUND_CLICK);
         	        		currentButton = CurrentButton.HELP;
         	        	} else
         	    			currentButton = CurrentButton.NONE;
     	        	} else if(state == State.PAUSE){
     	        		if(btn[2].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        			soundManager.playEffect(SOUND_CLICK);
     	        			currentButton = CurrentButton.CONTINUE;
     	        		} else
         	    			currentButton = CurrentButton.NONE;
     	        	} else if(state == State.HELP){
     	        		if(btn[6].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        			soundManager.playEffect(SOUND_CLICK);
     	        			currentButton = CurrentButton.PLAY;
     	        		} else
         	    			currentButton = CurrentButton.NONE;
     	        	} else if(state == State.BACKTOMENU){
     	        		if(btn[3].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	        		soundManager.playEffect(SOUND_CLICK);
         	        		currentButton = CurrentButton.BCK_TO_MENU_YES;
         	        	} else if(btn[4].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	        		soundManager.playEffect(SOUND_CLICK);
         	        		currentButton = CurrentButton.BCK_TO_MENU_NO;
         	        	} else
         	    			currentButton = CurrentButton.NONE;
     	        	}
 	        		break;
     	        }
     	    } else {	// mouse release
     	    	switch (Mouse.getEventButton()) {
     	    	case 0:
     	    		if(state == State.PLAY){
     	    			if(btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	    			soundManager.playEffect(SOUND_RELEASE);
         	    			if(currentButton == CurrentButton.MENU){
         	    				state = State.BACKTOMENU;
         	    			}
         	    		} else if(btn[1].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	    			soundManager.playEffect(SOUND_RELEASE);
         	    			if(currentButton == CurrentButton.PAUSE){
                 				state = State.PAUSE;
         	    			}
         	    		} else if(btn[5].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	    			soundManager.playEffect(SOUND_RELEASE);
         	    			if(currentButton == CurrentButton.HELP){
                 				state = State.HELP;
         	    			}
         	    		} else
         	    			currentButton = CurrentButton.NONE;
     	        	} else if(state == State.PAUSE){
     	        		if(btn[2].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        			soundManager.playEffect(SOUND_RELEASE);
     	        			if(currentButton == CurrentButton.CONTINUE){
     	        				state = State.PLAY;
         	    			}
     	        		} else
         	    			currentButton = CurrentButton.NONE;
     	        	} else if(state == State.HELP){
     	        		if(btn[6].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
     	        			soundManager.playEffect(SOUND_RELEASE);
     	        			if(currentButton == CurrentButton.PLAY){
     	        				state = State.PLAY;
         	    			}
     	        		} else
         	    			currentButton = CurrentButton.NONE;
     	        	} else if(state == State.BACKTOMENU){
     	        		if(btn[3].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	        		soundManager.playEffect(SOUND_RELEASE);
         	        		if(currentButton == CurrentButton.BCK_TO_MENU_YES){
         	        			MyTank.profit = 0;
         	        			nextState = State.UPGRADE;
         	    				changingState = true;
         	        			//move_to_UPGRADE_state();
         	    			}
         	        	} else if(btn[4].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         	        		soundManager.playEffect(SOUND_RELEASE);
         	        		if(currentButton == CurrentButton.BCK_TO_MENU_NO){
     	        				state = State.PLAY;
         	    			}
         	        	} else
         	    			currentButton = CurrentButton.NONE;
     	        	}
         			break;
     	    	}
     	    }
     	}
 	}
 	
 	private void setTimeToShowEndgameLabel() {
 		timeShowLabel += delta;
 		if(timeShowLabel > 50 && timeShowLabel < 250){
 			Label_Y += 3;
 		} else if(timeShowLabel >= 2400 && timeShowLabel < 2600){
 			Label_X -= (2600 - timeShowLabel)/20;
 		} else if(timeShowLabel >= 2600 && timeShowLabel < 3100){
 			Label_X += (timeShowLabel-2600)/10;
 		} else if(timeShowLabel > 3100){
 			timeShowLabel = 0;
 			Label_X = 0;
 			Label_Y = 0;
 			MyTank.myGold += MyTank.profit;
 			MyTank.profit = 0;
 			nextState = State.UPGRADE;
 			changingState = true;
 			//move_to_UPGRADE_state();
 			glMatrixMode(GL_PROJECTION);
             glLoadIdentity();
 			glOrtho(0 ,640 ,650 ,0 ,-1 , 1);
 		}
 	}
 	
 	private void setTimeToShowProfitMiniLabel() {
 		if(MyTank.gotGold){
 			if(miniLabel_Y < 27){
 				miniLabel_Y += 3;
 			}
 		} else {
 			if(miniLabel_Y > 0){
 				miniLabel_Y -= 3;
 			}
 		}
 	}
 
 	private void input() {
 		boolean KEY_W = Keyboard.isKeyDown(Keyboard.KEY_W);
     	boolean KEY_S = Keyboard.isKeyDown(Keyboard.KEY_S);
     	boolean KEY_D = Keyboard.isKeyDown(Keyboard.KEY_D);
     	boolean KEY_A = Keyboard.isKeyDown(Keyboard.KEY_A);
     	boolean KEY_1 = Keyboard.isKeyDown(Keyboard.KEY_1);
     	boolean KEY_2 = Keyboard.isKeyDown(Keyboard.KEY_2);
     	boolean KEY_3 = Keyboard.isKeyDown(Keyboard.KEY_3);
     	boolean KEY_4 = Keyboard.isKeyDown(Keyboard.KEY_4);
     	
         if(KEY_W && !KEY_S){
         	bodyAng = 90;
         	if(KEY_A)
         		bodyAng = 45;
         	if(KEY_D)
         		bodyAng = 135;
         	player.setDY(-player.speed);
         }
         if(KEY_S && !KEY_W){
         	bodyAng = 270;
         	if(KEY_A)
         		bodyAng = 315;
         	if(KEY_D)
         		bodyAng = 225;
         	player.setDY(player.speed);
         }
         if(KEY_D && !KEY_A){
         	bodyAng = 180;
         	if(KEY_W)
         		bodyAng = 135;
         	if(KEY_S)
         		bodyAng = 225;
         	player.setDX(player.speed);
         }
         if(KEY_A && !KEY_D){
         	bodyAng = 0;
         	if(KEY_W)
         		bodyAng = 45;
         	if(KEY_S)
         		bodyAng = 315;
         	player.setDX(-player.speed);
         }
         if(KEY_1 || KEY_2 || KEY_3 || KEY_4){
         	while (Keyboard.next()) {
         	    if (Keyboard.getEventKeyState()) {
         	        switch (Keyboard.getEventKey()) {
         	        	case Keyboard.KEY_1:
         	        		player.setGun(TankEntity.GunType.MINIGUN);
         	        		break;
         	        	case Keyboard.KEY_2:
         	        		player.setGun(TankEntity.GunType.SHOTGUN);
         	        		break;
         	        	case Keyboard.KEY_3:
         	        		player.setGun(TankEntity.GunType.CANNON);
         	        		break;
         	        	case Keyboard.KEY_4:
         	        		player.setGun(TankEntity.GunType.ROCKET);
         	        		break;
         	        }
         	    }
         	}
         	if(player.rocketReleased){
         		player.rocketReleased = false;
         		removeEntity(player.myRocketBullet);
         		entities.add(new BombEffect_BigBullet(this,player.myRocketBullet.x,player.myRocketBullet.y));
         		soundManager.playEffect(SOUND_BOMB_TANK);
         	}
         }
         /*if(KEY_1)
         	player.setGun(player.gunType.MINIGUN);
         if(KEY_2)
         	player.setGun(player.gunType.SHOTGUN);
         if(KEY_3)
         	player.setGun(player.gunType.RICOCHET);
         if(KEY_3)
         	player.setGun(player.gunType.CANNON);
         if(KEY_4){
         	//while (Mouse.next())
         	player.setGun(player.gunType.ROCKET);
         }
         if(KEY_6)
         	player.setGun(player.gunType.LASER);
         */
         
         //if(!soundManager.isPlayingSound()){
         	if(!btn[0].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())
         			&& !btn[1].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())
         			&& !btn[5].On_Mouse_Over(Mouse.getX(), 650 - Mouse.getY())){
         		if(player.gunType == TankEntity.GunType.ROCKET){
 	        		while (Mouse.next()) {
 	            	    if (Mouse.getEventButtonState()) {
 	            	        switch (Mouse.getEventButton()) {
 	            	        	case 0:
 	            	        		player.Fire(player.x,player.y,gunRotation);
 	            	        		break;
 	            	        }
 	            	    }
 	            	}
 	        	/*} else if(player.gunType == player.gunType.RICOCHET){
 	        		while (Mouse.next()) {
 	            	    if (Mouse.getEventButtonState()) {
 	            	        switch (Mouse.getEventButton()) {
 	            	        	case 0:
 	            	        		player.Fire(initBulletX,initBulletY,gunRotation);
 	            	        		break;
 	            	        }
 	            	    } else {	// mouse release
 	            	    	switch (Mouse.getEventButton()) {
 	        	        		case 0:
 	        	        			player.Fire(initBulletX,initBulletY,gunRotation);
 	        	        			break;
 	            	    	}
 	            	    }
 	            	}*/
 	        	} else if(Mouse.isButtonDown(0)){
 	            	player.Fire(initBulletX,initBulletY,gunRotation);
 	            }
         	}
         //}
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
         	if(state != State.LVCOMPLETE && state != State.LVFAILED)
         		state = State.PAUSE;
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_C)){
         	MyTank.myGold = 999999;
         }
 	}
     
     private void setCamera(){
     	// mouse position (0,0) at center screen
     	float mouseX = (Mouse.getX() - camera_w/2)*0.5f;
     	float mouseY = (Mouse.getY() - camera_h/2)*0.5f;
     	
     	//String str = String.valueOf(gunRotation);
     	
     	// Mouse.getX(),Mouse.getY() position (0,0) at bottom left
     	//Display.setTitle(str);
     	//gunRotation = 57.2957795f*(float)Math.atan2(camera_h/2 - Mouse.getY(),Mouse.getX() - camera_w/2);
     	gunRotation = (float) (180/Math.PI*(float)Math.atan2(camera_h/2 - Mouse.getY(),Mouse.getX() - camera_w/2));
     	gunRotation += 180;
     	player.setGunAngle(gunRotation);
     	if(player.rocketReleased){
     		player.myRocketBullet.setDX((float)-Math.cos(0.0174532925*gunRotation)*player.myRocketBullet.moveSpeed);
     		player.myRocketBullet.setDY((float)-Math.sin(0.0174532925*gunRotation)*player.myRocketBullet.moveSpeed);
     		camera_x = (int)(player.myRocketBullet.get_centerX() - camera_w/2);
         	camera_y = (int)(player.myRocketBullet.get_centerY() - camera_h/2);
         	if(Math.abs(camera_x_tmp - camera_x) > 5){
         		if(camera_x > camera_x_tmp){
         			camera_x = camera_x_tmp + 5;
         			camera_x_tmp += 5;
         		} else {
         			camera_x = camera_x_tmp - 5;
         			camera_x_tmp -= 5;
         		}
         	} else {
         		camera_x_tmp = camera_x;
         	}
         	if(Math.abs(camera_y_tmp - camera_y) > 5){
         		if(camera_y > camera_y_tmp){
         			camera_y = camera_y_tmp + 5;
         			camera_y_tmp += 5;
         		} else {
         			camera_y = camera_y_tmp - 5;
         			camera_y_tmp -= 5;
         		}
         	} else {
             	camera_y_tmp = camera_y;
         	}
     	} else {
     		camera_x = (int)mouseX + (int)(player.get_centerX() - camera_w/2);
         	camera_y = -(int)mouseY + (int)(player.get_centerY() - camera_h/2);
         	if(Math.abs(camera_x_tmp - camera_x) > 25){
         		if(camera_x > camera_x_tmp){
         			camera_x = camera_x_tmp + 25;
         			camera_x_tmp += 25;
         		} else {
         			camera_x = camera_x_tmp - 25;
         			camera_x_tmp -= 25;
         		}
         	} else {
         		camera_x_tmp = camera_x;
         	}
         	if(Math.abs(camera_y_tmp - camera_y) > 25){
         		if(camera_y > camera_y_tmp){
         			camera_y = camera_y_tmp + 25;
         			camera_y_tmp += 25;
         		} else {
         			camera_y = camera_y_tmp - 25;
         			camera_y_tmp -= 25;
         		}
         	} else {
             	camera_y_tmp = camera_y;
         	}
     	}
     }
     
     public static void addEntity(Entity entity) {
     	entities.add(entity);
 	}
     
     public static void removeEntity(Entity entity) {
     	if(entity instanceof MyTank)
     		state = State.LVFAILED;
 		removeList.add(entity);
 	}
     
     private void loadResource() {
     	fadeTex = loadTexture("black.png");
     	backgndIntro = loadTexture("intro.png");
     	backgndUpgrade = loadTexture("upgrade.png");
     	moneyBackgnd = loadTexture("moneyBackgnd.png");
     	backgndSelectLV = loadTexture("selectLV.png");
     	selectGunBar = loadTexture("selectGunBar.png");
     	pauseLabel = loadTexture("pauseLabel.png");
     	helpLabel = loadTexture("helpLabel.png");
     	backToMenuLabel = loadTexture("backToMenuLabel.png");
     	lvCompleteLabel = loadTexture("LVcomplete.png");
     	lvFailedLabel = loadTexture("LVfailed.png");
     	profitLabel = loadTexture("profitLabel.png");
     	profitMiniLabel = loadTexture("profitMiniLabel.png");
     	HPpipe = loadTexture("HPpipe.png");
 		HPred = loadTexture("HPred.png");
 		HPblue = loadTexture("HPblue.png");
 		labelMinigun = loadTexture("labelMinigun.png");
 		labelShotgun = loadTexture("labelShotgun.png");
 		labelCannon = loadTexture("labelCannon.png");
 		labelRocket = loadTexture("labelRocket.png");
 		labelMinigun_unuse = loadTexture("labelMinigun_unuse.png");
 		labelShotgun_unuse = loadTexture("labelShotgun_unuse.png");
 		labelCannon_unuse = loadTexture("labelCannon_unuse.png");
 		labelRocket_unuse = loadTexture("labelRocket_unuse.png");
 		labelMinigun_lock = loadTexture("labelMinigun_lock.png");
 		labelShotgun_lock = loadTexture("labelShotgun_lock.png");
 		labelCannon_lock = loadTexture("labelCannon_lock.png");
 		labelRocket_lock = loadTexture("labelRocket_lock.png");
 		number = loadTexture("number/num.png");
 		number_brown = loadTexture("number/num_brown.png");
 		number_no_gold = loadTexture("number/num_noGold.png");
 		labelTextPrice = loadTexture("labelPrice.png");
 		labelTextArmor = loadTexture("labelTextArmor.png");
 		labelTextSpeed = loadTexture("labelTextSpeed.png");
 		labelTextMinigun = loadTexture("labelTextMinigun.png");
 		labelTextShotgun = loadTexture("labelTextShotgun.png");
 		labelTextCannon = loadTexture("labelTextCannon.png");
 		labelTextRocket = loadTexture("labelTextRocket.png");
     	player = new MyTank(this,50);
     	brick = new Brick[27];
     	brick_2 = new Brick_2[31];
     	box = new Box[27];
     	bmWall = new BombWall[11];
     	oilTank = new OilTank[8];
     	enemyMinigun = new EnemyMinigun[15];
     	turret = new Turret[6];
     	gold = new Gold[200];
     	for(int i = 0;i < brick.length;i++){
         	brick[i] = new Brick(this,30);
     	}
     	for(int i = 0;i < brick_2.length;i++){
         	brick_2[i] = new Brick_2(this,30);
     	}
     	for(int i = 0;i < box.length;i++){
     		box[i] = new Box(this,15);
     	}
     	for(int i = 0;i < bmWall.length;i++){
         	bmWall[i] = new BombWall(this,30);
         }
     	for(int i = 0;i < oilTank.length;i++){
         	oilTank[i] = new OilTank(this,15);
     	}
     	for (int i = 0; i < enemyMinigun.length; i++) {
 			enemyMinigun[i] = new EnemyMinigun(this,50);
     	}
     	for(int i = 0;i < turret.length;i++){
     		turret[i] = new Turret(this, 50);
     	}
     	for(int i = 0;i < gold.length;i++){
     		gold[i] = new Gold(this);
     	}
 	}
     
     private void SetGame(int LV){
     	entities.clear();
     	camera_x_tmp = 0;
     	camera_y_tmp = 0;
     	currentLevel = LV;
     	switch (LV) {
 		case 1:
 			map.createMap(1);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(2,3);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(9, 1);
 	        brick[1].setPositionToMap(10, 1);
 	        brick[2].setPositionToMap(11, 1);
 	        brick[3].setPositionToMap(5, 5);
 	        brick[4].setPositionToMap(7, 5);
 	        brick[5].setPositionToMap(8, 5);
 	        brick[6].setPositionToMap(9, 5);
 	        brick[7].setPositionToMap(10, 5);
 	        brick[8].setPositionToMap(11, 5);
 	        for(int i = 0;i < 9;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 	        box[0].setPositionToMap(8, 2);
 	        box[1].setPositionToMap(9, 2);
 	        for(int i = 0;i < 2;i++){
 	        	box[i].setHP(15);
 	        	box[i].reset();
 	        	entities.add(box[i]);
 	        }
 	        bmWall[0].setPositionToMap(4, 2);
 	        bmWall[1].setPositionToMap(4, 3);
 	        bmWall[2].setPositionToMap(4, 4);
 	        bmWall[3].setPositionToMap(11, 2);
 	        bmWall[4].setPositionToMap(11, 3);
 	        bmWall[5].setPositionToMap(11, 4);
 	        for(int i = 0;i < 6;i++){
 	        	bmWall[i].setHP(30);
 	        	bmWall[i].reset();
 	        	entities.add(bmWall[i]);
 	        }
 	        oilTank[0].setPositionToMap(13, 1);
 	        oilTank[1].setPositionToMap(6, 5);
 	        for(int i = 0;i < 2;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	//entities.add(oilTank[i]);
 	        }
 			for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 			turret[0].setHP(50);
 			turret[0].setPositionToMap(14, 3);
 			turret[0].reset();
 			entities.add(turret[0]);
 			numEnemy = 5 + 1;
 			//numEnemy = 1;
 			break;
 		case 2:
 			map.createMap(2);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(2,2);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(6, 1);
 	        brick[1].setPositionToMap(6, 2);
 	        brick[2].setPositionToMap(6, 3);
 	        brick[3].setPositionToMap(6, 4);
 	        brick[4].setPositionToMap(4, 8);
 	        brick[5].setPositionToMap(5, 8);
 	        brick[6].setPositionToMap(6, 8);
 	        for(int i = 0;i < 7;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 			bmWall[0].setPositionToMap(4, 2);
 	        bmWall[1].setPositionToMap(4, 3);
 	        bmWall[2].setPositionToMap(4, 1);
 	        for(int i = 0;i < 3;i++){
 	        	bmWall[i].setHP(30);
 	        	bmWall[i].reset();
 	        	entities.add(bmWall[i]);
 	        }
 	        oilTank[0].setPositionToMap(2, 6);
 	        oilTank[1].setPositionToMap(4, 6);
 	        oilTank[2].setPositionToMap(6, 6);
 	        oilTank[3].setPositionToMap(8, 6);
 	        for(int i = 0;i < 4;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setHP(50);
 			turret[0].setPositionToMap(2, 8);
 			turret[0].reset();
 			entities.add(turret[0]);
 	        numEnemy = 5 + 1;
 			break;
 		case 3:
 			map.createMap(3);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(5,12);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(4, 1);
 	        brick[1].setPositionToMap(6, 1);
 	        brick[2].setPositionToMap(4, 2);
 	        brick[3].setPositionToMap(6, 2);
 	        brick[4].setPositionToMap(1, 3);
 	        brick[5].setPositionToMap(3, 3);
 	        brick[6].setPositionToMap(4, 3);
 	        brick[7].setPositionToMap(6, 3);
 	        brick[8].setPositionToMap(7, 3);
 	        brick[9].setPositionToMap(9, 3);
 	        brick[10].setPositionToMap(1, 5);
 	        brick[11].setPositionToMap(1, 6);
 	        brick[12].setPositionToMap(1, 7);
 	        brick[13].setPositionToMap(2, 5);
 	        brick[14].setPositionToMap(2, 6);
 	        brick[15].setPositionToMap(2, 7);
 	        brick[16].setPositionToMap(4, 6);
 	        brick[17].setPositionToMap(4, 7);
 	        brick[18].setPositionToMap(5, 7);
 	        brick[19].setPositionToMap(6, 6);
 	        brick[20].setPositionToMap(6, 7);
 	        brick[21].setPositionToMap(8, 5);
 	        brick[22].setPositionToMap(8, 6);
 	        brick[23].setPositionToMap(8, 7);
 	        brick[24].setPositionToMap(9, 5);
 	        brick[25].setPositionToMap(9, 6);
 	        brick[26].setPositionToMap(9, 7);
 	        for(int i = 0;i < 27;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 	        box[0].setPositionToMap(1, 1);
 	        box[1].setPositionToMap(3, 4);
 	        for(int i = 0;i < 2;i++){
 	        	box[i].setHP(15);
 	        	box[i].reset();
 	        	entities.add(box[i]);
 	        }
 	        oilTank[0].setPositionToMap(4, 5);
 	        oilTank[1].setPositionToMap(6, 5);
 	        for(int i = 0;i < 2;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setHP(50);
 			turret[0].setPositionToMap(2, 8);
 			turret[0].reset();
 			entities.add(turret[0]);
 	        numEnemy = 5 + 1;
 			break;
 		case 4:
 			map.createMap(4);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(1,2);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(4, 1);
 	        brick[1].setPositionToMap(4, 2);
 	        brick[2].setPositionToMap(6, 2);
 	        brick[3].setPositionToMap(6, 3);
 	        brick[4].setPositionToMap(10, 4);
 	        brick[5].setPositionToMap(11, 4);
 	        brick[6].setPositionToMap(12, 4);
 	        brick[7].setPositionToMap(9, 5);
 	        brick[8].setPositionToMap(9, 6);
 	        brick[9].setPositionToMap(7, 6);
 	        brick[10].setPositionToMap(5, 5);
 	        brick[11].setPositionToMap(5, 6);
 	        brick[12].setPositionToMap(3, 6);
 	        brick[13].setPositionToMap(1, 8);
 	        brick[14].setPositionToMap(2, 8);
 	        brick[15].setPositionToMap(3, 9);
 	        brick[16].setPositionToMap(3, 10);
 	        brick[17].setPositionToMap(4, 9);
 	        brick[18].setPositionToMap(5, 9);
 	        brick[19].setPositionToMap(5, 11);
 	        brick[20].setPositionToMap(6, 11);
 	        brick[21].setPositionToMap(7, 11);
 	        brick[22].setPositionToMap(8, 11);
 	        brick[23].setPositionToMap(8, 10);
 	        for(int i = 0;i < 24;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 	        oilTank[0].setPositionToMap(8, 2);
 	        oilTank[1].setPositionToMap(7, 7);
 	        oilTank[2].setPositionToMap(1, 7);
 	        oilTank[3].setPositionToMap(1, 11);
 	        oilTank[4].setPositionToMap(6, 9);
 	        for(int i = 0;i < 5;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 5);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setHP(50);
 			turret[0].setPositionToMap(10, 2);
 			turret[0].reset();
 			entities.add(turret[0]);
 	        numEnemy = 5 + 1;
 			break;
 		case 5:
 			map.createMap(5);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(2,2);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick_2[0].setPositionToMap(6, 2);
 	        brick_2[1].setPositionToMap(8, 2);
 	        brick_2[2].setPositionToMap(10, 2);
 	        brick_2[3].setPositionToMap(6, 4);
 	        brick_2[4].setPositionToMap(12, 4);
 	        brick_2[5].setPositionToMap(6, 6);
 	        brick_2[6].setPositionToMap(8, 6);
 	        brick_2[7].setPositionToMap(10, 6);
 	        brick_2[8].setPositionToMap(12, 6);
 	        
 	        brick_2[9].setPositionToMap(6, 11);
 	        brick_2[10].setPositionToMap(7, 11);
 	        brick_2[11].setPositionToMap(11, 11);
 	        brick_2[12].setPositionToMap(12, 11);
 	        brick_2[13].setPositionToMap(6, 12);
 	        brick_2[14].setPositionToMap(12, 12);
 	        brick_2[15].setPositionToMap(6, 13);
 	        brick_2[16].setPositionToMap(12, 13);
 	        brick_2[17].setPositionToMap(6, 15);
 	        brick_2[18].setPositionToMap(12, 15);
 	        brick_2[19].setPositionToMap(6, 16);
 	        brick_2[20].setPositionToMap(12, 16);
 	        brick_2[21].setPositionToMap(6, 17);
 	        brick_2[22].setPositionToMap(7, 17);
 	        brick_2[23].setPositionToMap(8, 17);
 	        brick_2[24].setPositionToMap(11, 17);
 	        brick_2[25].setPositionToMap(12, 17);
 	        for(int i = 0;i < 26;i++){
 	        	brick_2[i].setHP(70);
 	        	brick_2[i].reset();
 	        	entities.add(brick_2[i]);
 	        }
 	        bmWall[0].setPositionToMap(4, 2);
 	        bmWall[1].setPositionToMap(4, 3);
 	        bmWall[2].setPositionToMap(4, 4);
 	        bmWall[3].setPositionToMap(8, 9);
 	        bmWall[4].setPositionToMap(9, 9);
 	        bmWall[5].setPositionToMap(10, 9);
 	        for(int i = 0;i < 6;i++){
 	        	bmWall[i].setHP(30);
 	        	bmWall[i].reset();
 	        	entities.add(bmWall[i]);
 	        }
 	        oilTank[0].setPositionToMap(8, 13);
 	        oilTank[1].setPositionToMap(9, 14);
 	        for(int i = 0;i < 2;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setPositionToMap(8, 4);
 	        turret[1].setPositionToMap(10, 4);
 	        turret[2].setPositionToMap(11, 16);
 	        for(int i = 0;i < 3;i++){
 	        	turret[i].setHP(70);
 				turret[i].reset();
 				entities.add(turret[i]);
 	        }
 	        numEnemy = 5 + 3;
 			break;
 			//#####################################################################
 			//#####################################################################
 			//#####################################################################
 		case 6:
 			map.createMap(6);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(2,2);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(6, 1);
 	        brick[1].setPositionToMap(6, 2);
 	        brick[2].setPositionToMap(6, 3);
 	        brick[3].setPositionToMap(6, 4);
 	        brick[4].setPositionToMap(4, 8);
 	        brick[5].setPositionToMap(5, 8);
 	        brick[6].setPositionToMap(6, 8);
 	        for(int i = 0;i < 7;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 			bmWall[0].setPositionToMap(4, 2);
 	        bmWall[1].setPositionToMap(4, 3);
 	        bmWall[2].setPositionToMap(4, 1);
 	        for(int i = 0;i < 3;i++){
 	        	bmWall[i].setHP(30);
 	        	bmWall[i].reset();
 	        	entities.add(bmWall[i]);
 	        }
 	        oilTank[0].setPositionToMap(2, 6);
 	        oilTank[1].setPositionToMap(4, 6);
 	        oilTank[2].setPositionToMap(6, 6);
 	        oilTank[3].setPositionToMap(8, 6);
 	        for(int i = 0;i < 4;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setHP(50);
 			turret[0].setPositionToMap(2, 8);
 			turret[0].reset();
 			entities.add(turret[0]);
 	        numEnemy = 5 + 1;
 			break;
 		case 7:
 			map.createMap(7);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(2,2);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(6, 1);
 	        brick[1].setPositionToMap(6, 2);
 	        brick[2].setPositionToMap(6, 3);
 	        brick[3].setPositionToMap(6, 4);
 	        brick[4].setPositionToMap(4, 8);
 	        brick[5].setPositionToMap(5, 8);
 	        brick[6].setPositionToMap(6, 8);
 	        for(int i = 0;i < 7;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 			bmWall[0].setPositionToMap(4, 2);
 	        bmWall[1].setPositionToMap(4, 3);
 	        bmWall[2].setPositionToMap(4, 1);
 	        for(int i = 0;i < 3;i++){
 	        	bmWall[i].setHP(30);
 	        	bmWall[i].reset();
 	        	entities.add(bmWall[i]);
 	        }
 	        oilTank[0].setPositionToMap(2, 6);
 	        oilTank[1].setPositionToMap(4, 6);
 	        oilTank[2].setPositionToMap(6, 6);
 	        oilTank[3].setPositionToMap(8, 6);
 	        for(int i = 0;i < 4;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setHP(50);
 			turret[0].setPositionToMap(2, 8);
 			turret[0].reset();
 			entities.add(turret[0]);
 	        numEnemy = 5 + 1;
 			break;
 		case 8:
 			map.createMap(8);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(2,2);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(6, 1);
 	        brick[1].setPositionToMap(6, 2);
 	        brick[2].setPositionToMap(6, 3);
 	        brick[3].setPositionToMap(6, 4);
 	        brick[4].setPositionToMap(4, 8);
 	        brick[5].setPositionToMap(5, 8);
 	        brick[6].setPositionToMap(6, 8);
 	        for(int i = 0;i < 7;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 			bmWall[0].setPositionToMap(4, 2);
 	        bmWall[1].setPositionToMap(4, 3);
 	        bmWall[2].setPositionToMap(4, 1);
 	        for(int i = 0;i < 3;i++){
 	        	bmWall[i].setHP(30);
 	        	bmWall[i].reset();
 	        	entities.add(bmWall[i]);
 	        }
 	        oilTank[0].setPositionToMap(2, 6);
 	        oilTank[1].setPositionToMap(4, 6);
 	        oilTank[2].setPositionToMap(6, 6);
 	        oilTank[3].setPositionToMap(8, 6);
 	        for(int i = 0;i < 4;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setHP(50);
 			turret[0].setPositionToMap(2, 8);
 			turret[0].reset();
 			entities.add(turret[0]);
 	        numEnemy = 5 + 1;
 			break;
 		case 9:
 			map.createMap(9);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(2,2);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(6, 1);
 	        brick[1].setPositionToMap(6, 2);
 	        brick[2].setPositionToMap(6, 3);
 	        brick[3].setPositionToMap(6, 4);
 	        brick[4].setPositionToMap(4, 8);
 	        brick[5].setPositionToMap(5, 8);
 	        brick[6].setPositionToMap(6, 8);
 	        for(int i = 0;i < 7;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 			bmWall[0].setPositionToMap(4, 2);
 	        bmWall[1].setPositionToMap(4, 3);
 	        bmWall[2].setPositionToMap(4, 1);
 	        for(int i = 0;i < 3;i++){
 	        	bmWall[i].setHP(30);
 	        	bmWall[i].reset();
 	        	entities.add(bmWall[i]);
 	        }
 	        oilTank[0].setPositionToMap(2, 6);
 	        oilTank[1].setPositionToMap(4, 6);
 	        oilTank[2].setPositionToMap(6, 6);
 	        oilTank[3].setPositionToMap(8, 6);
 	        for(int i = 0;i < 4;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setHP(50);
 			turret[0].setPositionToMap(2, 8);
 			turret[0].reset();
 			entities.add(turret[0]);
 	        numEnemy = 5 + 1;
 			break;
 		case 10:
 			map.createMap(10);
 			player.setHP(player.maxHP);
 	        player.setPositionToMap(2,2);
 	        player.reset();
 	        player.setGun(TankEntity.GunType.MINIGUN);
 	        player.rocketReleased = false;
 	        entities.add(player);
 	        brick[0].setPositionToMap(6, 1);
 	        brick[1].setPositionToMap(6, 2);
 	        brick[2].setPositionToMap(6, 3);
 	        brick[3].setPositionToMap(6, 4);
 	        brick[4].setPositionToMap(4, 8);
 	        brick[5].setPositionToMap(5, 8);
 	        brick[6].setPositionToMap(6, 8);
 	        for(int i = 0;i < 7;i++){
 	        	brick[i].setHP(30);
 	        	brick[i].reset();
 	        	entities.add(brick[i]);
 	        }
 			bmWall[0].setPositionToMap(4, 2);
 	        bmWall[1].setPositionToMap(4, 3);
 	        bmWall[2].setPositionToMap(4, 1);
 	        for(int i = 0;i < 3;i++){
 	        	bmWall[i].setHP(30);
 	        	bmWall[i].reset();
 	        	entities.add(bmWall[i]);
 	        }
 	        oilTank[0].setPositionToMap(2, 6);
 	        oilTank[1].setPositionToMap(4, 6);
 	        oilTank[2].setPositionToMap(6, 6);
 	        oilTank[3].setPositionToMap(8, 6);
 	        for(int i = 0;i < 4;i++){
 	        	oilTank[i].setHP(15);
 	        	oilTank[i].reset();
 	        	entities.add(oilTank[i]);
 	        }
 	        for (int i = 0; i < 5; i++) {
 				enemyMinigun[i].setHP(50);
 				enemyMinigun[i].setPositionToMap(i+5, 4);
 				enemyMinigun[i].setBodyAngle(39);
 				enemyMinigun[i].reset();
 				entities.add(enemyMinigun[i]);
 			}
 	        turret[0].setHP(50);
 			turret[0].setPositionToMap(2, 8);
 			turret[0].reset();
 			entities.add(turret[0]);
 	        numEnemy = 5 + 1;
 			break;
 		}
     }
     
     public void dropGold(int x, int y) {
     	int amount = new Random().nextInt(10) + 5;	// random 5 to 15
 		int i;
 		for(i = goldIndex;i < amount + goldIndex;i++){
 			if(i >= gold.length){
 				amount += goldIndex - i;
 				goldIndex = 0;
 				i = 0;
 			}
 			gold[i].setXY(x, y);
 			gold[i].setDX(new Random().nextInt(7) - 3);	// random -3 to 3
 			gold[i].setDY(new Random().nextInt(7) - 3);
 			addEntity(gold[i]);
 		}
 		goldIndex = i;
     }
     
     public static long getTime() {
 		return (Sys.getTime() * 1000) / timerTicksPerSecond;
 	}
 
 	private ByteBuffer loadIcon(String key) throws IOException {
         InputStream is = new FileInputStream(new File("res/"+key));
         try {
             PNGDecoder decoder = new PNGDecoder(is);
             ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth()*decoder.getHeight()*4);
             decoder.decode(bb, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
             bb.flip();
             return bb;
         } finally {
             is.close();
         }
     }
 	
 	public static Texture loadTexture(String key){
 		/*try {
 			PNGDecoder decoder = new PNGDecoder(new FileInputStream("res/"+key));
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}*/
 		try {
 			if(key.indexOf(".jpg") != -1){
 				return TextureLoader.getTexture("JPG", new FileInputStream(new File("res/"+key)));
 			} else
 				return TextureLoader.getTexture("PNG", new FileInputStream(new File("res/"+key)));
     	} catch (FileNotFoundException e) {
             e.printStackTrace();
     	} catch (IOException e) {
             e.printStackTrace();
     	}
     	return null;
 	}
 	
 	public void draw(Texture tex,int x,int y,int width,int height) {
 		glPushMatrix();
 		tex.bind();
         glBegin(GL_QUADS);
         	glTexCoord2f(0,0);
         	glVertex2f(x-width/2 ,y-height/2);//upper left
         	glTexCoord2f(tex.getWidth(),0);
         	glVertex2f(x+width/2 ,y-height/2);//upper right
         	glTexCoord2f(tex.getWidth(),tex.getHeight());
         	glVertex2f(x+width/2 ,y+height/2);//bottom right
         	glTexCoord2f(0,tex.getHeight());
         	glVertex2f(x-width/2 ,y+height/2);//bottom left
         glEnd();
         glPopMatrix();
 	}
 	
 	/**
 	 * 
 	 * @param money
 	 * @param x
 	 * @param y
 	 * @param size
 	 * @param type 1 = normal, 2 = draw num brown
 	 */
 	private void drawFont(String money, int x, int y, float size, int type){
 		money += "$";
 		float left = 0,right = 0;
 		int index;
 		if(not_enough_gold){
 			number_no_gold.bind();
 			not_enough_gold = false;
 		} else if(type == 2)
 			number_brown.bind();
 		else
 			number.bind();
 		glPushMatrix();
         glTranslatef(0, 0, 0);
         glBegin(GL_QUADS);
         for (int i = money.length()-1; i >= 0; i--) {
         	if(i == money.length()-1)
         		index = 10;
         	else
         		index = (int) money.charAt(i)-48;
             switch(index){
             case 0:
             	left = 0;
             	right = 21/256f;
             	break;
             case 1:
             	left = 21/256f;
             	right = 34/256f;
             	break;
             case 2:
             	left = 34/256f;
             	right = 54/256f;
             	break;
             case 3:
             	left = 54/256f;
             	right = 73/256f;
             	break;
             case 4:
             	left = 73/256f;
             	right = 95/256f;
             	break;
             case 5:
             	left = 95/256f;
             	right = 114/256f;
             	break;
             case 6:
             	left = 114/256f;
             	right = 134/256f;
             	break;
             case 7:
             	left = 134/256f;
             	right = 153/256f;
             	break;
             case 8:
             	left = 153/256f;
             	right = 173/256f;
             	break;
             case 9:
             	left = 173/256f;
             	right = 194/256f;
             	break;
             case 10:
             	left = 194/256f;
             	right = 211/256f;
             	break;
             }            
             glTexCoord2f(right, 0);
             glVertex2f(x, y);
             
             glTexCoord2f(left, 0);
             glVertex2f(x - (right-left)*256*size, y);
             
             glTexCoord2f(left, 0 + number.getHeight());
             glVertex2f(x - (right-left)*256*size, y + number.getImageHeight()*size);
             
             glTexCoord2f(right, 0 + number.getHeight());
             glVertex2f(x, y + number.getImageHeight()*size);
             x -= (right-left)*256*size;
         }
         glEnd();
         glPopMatrix();
 	}
 
 	private float getBitmapFontWidth(String str, float size){
 		str += "$";
 		float left = 0,right = 0;
 		float width = 0;
 		int index;
         for (int i = str.length()-1; i >= 0; i--) {
         	if(i == str.length()-1)
         		index = 10;
         	else
         		index = (int) str.charAt(i)-48;
             switch(index){
             case 0:
             	left = 0;
             	right = 21/256f;
             	break;
             case 1:
             	left = 21/256f;
             	right = 34/256f;
             	break;
             case 2:
             	left = 34/256f;
             	right = 54/256f;
             	break;
             case 3:
             	left = 54/256f;
             	right = 73/256f;
             	break;
             case 4:
             	left = 73/256f;
             	right = 95/256f;
             	break;
             case 5:
             	left = 95/256f;
             	right = 114/256f;
             	break;
             case 6:
             	left = 114/256f;
             	right = 134/256f;
             	break;
             case 7:
             	left = 134/256f;
             	right = 153/256f;
             	break;
             case 8:
             	left = 153/256f;
             	right = 173/256f;
             	break;
             case 9:
             	left = 173/256f;
             	right = 194/256f;
             	break;
             case 10:
             	left = 194/256f;
             	right = 211/256f;
             	break;
             }
             width += (right-left)*256*size;
         }
 		return width;
 	}
 	
 	private void drawPriceLabel(Game.CurrentButton btn, String price, int x, int y){
 		float BitmapFontWidth;
 		BitmapFontWidth = getBitmapFontWidth(price, 0.5f);
 		switch(btn){
 		case UPGRD_MINIGUN:
 			draw(labelTextMinigun, x, y-labelTextMinigun.getImageHeight()/2, labelTextMinigun.getImageWidth(), labelTextMinigun.getImageHeight());
 			y -= (labelTextMinigun.getImageHeight()/2 + labelTextPrice.getImageHeight()/2 + 23);
 			break;
 		case UPGRD_SHOTGUN:
 			draw(labelTextShotgun, x, y-labelTextShotgun.getImageHeight()/2, labelTextShotgun.getImageWidth(), labelTextShotgun.getImageHeight());
 			y -= (labelTextShotgun.getImageHeight()/2 + labelTextPrice.getImageHeight()/2 + 23);
 			break;
 		case UPGRD_CANNON:
 			draw(labelTextCannon, x, y-labelTextCannon.getImageHeight()/2, labelTextCannon.getImageWidth(), labelTextCannon.getImageHeight());
 			y -= (labelTextCannon.getImageHeight()/2 + labelTextPrice.getImageHeight()/2 + 23);
 			break;
 		case UPGRD_ROCKET:
 			draw(labelTextRocket, x, y-labelTextRocket.getImageHeight()/2, labelTextRocket.getImageWidth(), labelTextRocket.getImageHeight());
 			y -= (labelTextRocket.getImageHeight()/2 + labelTextPrice.getImageHeight()/2 + 23);
 			break;
 		case UPGRD_ARMOR:
 			draw(labelTextArmor, x, y-labelTextArmor.getImageHeight()/2, labelTextArmor.getImageWidth(), labelTextArmor.getImageHeight());
 			y -= (labelTextArmor.getImageHeight()/2 + labelTextPrice.getImageHeight()/2 + 23);
 			break;
 		case UPGRD_SPEED:
 			draw(labelTextSpeed, x, y-labelTextSpeed.getImageHeight()/2, labelTextSpeed.getImageWidth(), labelTextSpeed.getImageHeight());
 			y -= (labelTextSpeed.getImageHeight()/2 + labelTextPrice.getImageHeight()/2 + 25);
 			break;
 		default:
 			break;
 		}
 		draw(labelTextPrice, x, y+8, (int)BitmapFontWidth+15, labelTextPrice.getImageHeight());
 		drawFont(price, (int)(x+BitmapFontWidth/2), y, 0.5f, 2);
 	}
 	
 	public void drawHPpipe(int x,int y,int width,int height) {
 		HPred.bind();
 		glPushMatrix();
 		glBegin(GL_QUADS);
 			glVertex2f(x-width/2 ,y-height/2);
 			glVertex2f((x-width/2)+(width-2)*player.HP/player.maxHP ,y-height/2);
 			glVertex2f((x-width/2)+(width-2)*player.HP/player.maxHP ,y+height/2);
 			glVertex2f(x-width/2 ,y+height/2);
 		glEnd();
 		glPopMatrix();
 		
 		HPblue.bind();
 		glPushMatrix();
 		glBegin(GL_QUADS);
 			glVertex2f((x-width/2)+(width-2)*player.HP/player.maxHP ,y-height/2);
 			glVertex2f(x+width/2 ,y-height/2);
 			glVertex2f(x+width/2 ,y+height/2);
 			glVertex2f((x-width/2)+(width-2)*player.HP/player.maxHP ,y+height/2);
 		glEnd();
 		glPopMatrix();
 		
 		HPpipe.bind();
 		glPushMatrix();
 		glBegin(GL_QUADS);
 			glTexCoord2f(0,0);
 			glVertex2f(x-width/2 ,y-height/2);
 			glTexCoord2f(HPpipe.getWidth(),0);
 			glVertex2f(x+width/2 ,y-height/2);
 			glTexCoord2f(HPpipe.getWidth(),HPpipe.getHeight());
 			glVertex2f(x+width/2 ,y+height/2);
 			glTexCoord2f(0,HPpipe.getHeight());
 			glVertex2f(x-width/2 ,y+height/2);
 		glEnd();
 		glPopMatrix();
 		y += height/2;
 	}
    
     public static void main(String[] args) throws IOException, LWJGLException {
             new Game();
     }
 }
