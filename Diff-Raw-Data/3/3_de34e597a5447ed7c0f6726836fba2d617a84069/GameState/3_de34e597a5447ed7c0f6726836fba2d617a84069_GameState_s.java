 package pro.oneredpixel.deflektorclassic;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.TimeUtils;
 
 public class GameState extends State {
 
 	Button bResume;
 	Button bRestart;
 	Button bLevels;
 	Button bSoundOn;
 	Button bSoundOff;
 	Button bNext;
 	
 	GameState(Deflektor defl) {
 		super(defl);
 		
 		bResume = new Button(108, 56,64,176);
 		bRestart = new Button(64, 104,96,176);
 		bLevels = new Button(32, 104,48,176);
 		bSoundOn = new Button(184, 104,112,176);
 		bSoundOff = new Button(184, 104,112,160);
 		bNext = new Button(184, 104,80, 160);
 	}
 	
 	final int desiredFPS = 20;
 	
 	final int WINSTATE_GAMING = 0;
 	final int WINSTATE_PAUSED = 1;
 	final int WINSTATE_STOPED = 2;
 	int winStateId = WINSTATE_GAMING;
 
 	final int GAMESTATE_ACCUMULATING_ENERGY =0;
 	final int GAMESTATE_GAMING = 1;
 	final int GAMESTATE_GAMEOVER_OVERHEAT = 2;
 	final int GAMESTATE_GAMEOVER_NOENERGY = 3;
 	final int GAMESTATE_LEVELCOMPLETED = 4;
 	int gameStateId = GAMESTATE_ACCUMULATING_ENERGY;
 	
 	final int BEAMSTATE_NORMAL = 0;
 	final int BEAMSTATE_OVERHEAT = 1;
 	final int BEAMSTATE_BOMB = 2;
 	final int BEAMSTATE_CONNECTED = 3;
 	final int BEAMSTATE_CHARGING = 4;
 	int beamState;
 	int prevBeamState;
 	
 	final int MAXIMUM_REFLECTIONS = 100;
 	
 	//current energy (timer)
 	int energy=0;
 	//start energy
 	int energySteps = 1024;
 	//current overheat
 	int overheat=0;
 	//maximum overheat
 	int overheatSteps = 1024;
 	int overheatReflectionStep = 0;
 	int overheatBombStep = 0;
 	
 	int countOfGremlins = 0;
 	
 	//for score
 	int killedGremlins;
 	int burnedCells;
 	int countScore;
 	int countKilledGremlins;
 	int countBurnedCells;
 	int countEnergy;
 	int topScore;
 	
 	boolean cursorEnabled = false;
 	int cursorPhase = 0;
 	final int cursorPhases = 6;
 	final int cursorDisplayPhases = 3;
 	int cursorX = 0;
 	int cursorY = 0;
 	
 	// , 0..63,    animateField,   
 	int flash;
 	
 	void create() {
 		grm=new Gremlin[8];
 		for (int i=0;i<8;i++) {
 			grm[i]=new Gremlin();
 		};
 	};
 	
 	void destroy() {
 		
 	};
 	
 	//init state for showing
 	void start() {
 		initGame();
 	};
 	
 	//state stoped
 	void stop() {
 		winStateId = WINSTATE_STOPED;
 		app.stopContinuousSound();
 		
 	};
 	
 	void pause() {
 		if (winStateId==WINSTATE_GAMING && (gameStateId==GAMESTATE_ACCUMULATING_ENERGY || gameStateId==GAMESTATE_GAMING))
 			winStateId=WINSTATE_PAUSED;
 		app.stopContinuousSound();
 	};
 	
 	void resume () {
 
 	};
 	
 	public void render(SpriteBatch batch) {
 		//game
 		prevBeamState = beamState;
 		
 		batch.setProjectionMatrix(app.camera.combined);
 		batch.begin();
 		drawField();
 		drawGameInfo();
 		
 		if (winStateId==WINSTATE_PAUSED) {
 			app.drawBox(24, 8, 240-48, 160-32, 0,176);
 			app.showString(32+3*8+4, 24, String.format("LEVEL %02d PAUSED",app.playingLevel));
 			
 			app.drawButton(bResume);
 			app.drawButton(bRestart);
 			app.drawButton(bLevels);
 			
 			if (app.soundEnabled) app.drawButton(bSoundOn);
 			else app.drawButton(bSoundOff);
 		} else {
 			//show message
 			switch (gameStateId) {
 			case GAMESTATE_ACCUMULATING_ENERGY:
 				if ((flash&4)==0) {
 					app.showMessage(240/2, 160/2, "CHARGING LASER", true);
 				};
 				break;
 			case GAMESTATE_GAMEOVER_NOENERGY:
 				if ((flash&2)==0) {
 					app.showMessage(240/2, 160/2, "ENERGY DRAINED", true);
 				};
 				break;
 			case GAMESTATE_GAMEOVER_OVERHEAT:
 				if ((flash&2)==0) {
 					app.showMessage(240/2, 160/2, "BOOM BOOM BOOM", true);
 				};
 				break;
 			case GAMESTATE_LEVELCOMPLETED:
 				app.drawBox(24, 8, 240-48, 160-32, 0,176);
 				app.showString(32+8+8, 24, String.format("LEVEL %02d COMPLETED",app.playingLevel));
 				
 				if (countBurnedCells<burnedCells) {
 					countBurnedCells++;
 					countScore+=app.difficultyClassic?30:15;
 				};
 				if (countKilledGremlins<killedGremlins) {
 					countKilledGremlins++;
 					countScore+=app.difficultyClassic?160:80;
 				};
 				if (energy>0) {
 					energy-=energySteps/64;
 					if (energy<0) energy=0;
 					countEnergy++;
 					countScore+=app.difficultyClassic?10:2;
 				};
 				if (topScore<countScore) topScore=countScore;
 				
 				app.showString(32+8*6, 24+8+8, String.format("CELLS %d",countBurnedCells));
 				app.showString(32+8*3, 24+8+8+8, String.format("GREMLINS %d",countKilledGremlins));
 				app.showString(32+8*5, 24+8+8+8+8, String.format("ENERGY %d",countEnergy));
 				app.showString(32+8*6, 24+8+8+8+8+8+8, String.format("SCORE %d",countScore));
 				if (topScore<=countScore) {
 					if ((flash&2)==0) app.showString(32+8+8+8*4, 24+8+8+8+8+8+8+8+8, "NEW RECORD");
 				} else app.showString(32+8*8, 24+8+8+8+8+8+8+8+8, String.format("TOP %d",topScore));
 				
 				
 				
 				
 				app.drawButton(bLevels);
 				app.drawButton(bNext);
 				app.drawButton(bRestart);
 
 				break;
 			case GAMESTATE_GAMING:
 				break;
 			};
 		}
 		
 		batch.end();
 		
 		if(TimeUtils.nanoTime() - app.lastFrameTime > (1000000000/desiredFPS)) {
 			flash=(flash+1)&63;
 			if (winStateId==WINSTATE_GAMING && gameStateId!=GAMESTATE_LEVELCOMPLETED) animateField();			
 		} else return;
 		app.lastFrameTime = TimeUtils.nanoTime();
 		
 	};
 	
 	//------
 	//--- controlling
 	//------
 	public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
 		return false;
 	}
 
 	public boolean fling(float arg0, float arg1, int arg2) {
 		return false;
 	}
 
 	public boolean longPress(float arg0, float arg1) {
 		return false;
 	}
 	
 	public boolean zoom(float arg0, float arg1) {
 		return false;
 	}
 
 	public boolean tap(float x, float y, int tapCount, int button) {
 		int tapx=(int)(x-app.winX)/app.sprScale;
 		int tapy=(int)(y-app.winY)/app.sprScale;
 		switch (winStateId) {
 		case WINSTATE_GAMING:
 			if (gameStateId==GAMESTATE_LEVELCOMPLETED) {
 				if (bNext.checkRegion(tapx,tapy)) {
 					//play next level?
 					app.playingLevel++;
 					if (app.playingLevel<=app.countOfLevels) {
 						initGame();
 					} else {
 						app.timeToShowFinalCut=true;
 						app.gotoAppState(Deflektor.APPSTATE_SELECTLEVEL);
 					}
 					//app.playSound(Deflektor.SND_UNTAP);
 					bNext.touched=false;
 					
 				};
 				if (bRestart.checkRegion(tapx,tapy)) {
 					initGame();
 					//app.playSound(Deflektor.SND_UNTAP);
 					bRestart.touched=false;
 				};
 				if (bLevels.checkRegion(tapx,tapy)) {
 					app.gotoAppState(Deflektor.APPSTATE_SELECTLEVEL);
 					bLevels.touched=false;
 					//app.playSound(Deflektor.SND_UNTAP);
 				};
 				break;
 				
 			} else if ((gameStateId==GAMESTATE_GAMEOVER_OVERHEAT) || (gameStateId==GAMESTATE_GAMEOVER_NOENERGY)) {
 				break;
 			};
 			boolean changedCoordinates = setCursor((int)x,(int)y);
 			x=x-app.winX;
 			y=y-app.winY;
 			if (x>=0 && x<app.winWidth && y>=0 && y<app.winHeight && beamState!=BEAMSTATE_CONNECTED) {
 				if ((!changedCoordinates || !app.controlsTapThenDrag) && app.controlsTapToRotate) touchField(((int)x)/(app.sprSize*2)/app.sprScale, ((int)y)/(app.sprSize*2)/app.sprScale);
 				killGremlins((int)(x/app.sprScale), (int)(y/app.sprScale));
 			};
 			if (checkInBox((int)(x/app.sprScale),(int)(y/app.sprScale),(field_width-1)*16, field_height*16,16,16)) {
 				winStateId=WINSTATE_PAUSED;
 				app.playSound(Deflektor.SND_UNTAP);
 			}
 			break;
 		case WINSTATE_PAUSED:
 			if (bResume.checkRegion(tapx,tapy)) {
 				winStateId=WINSTATE_GAMING;
 				bResume.touched=false;
 			};
 			if (bRestart.checkRegion(tapx,tapy)) {
 				initGame();
 				bRestart.touched=false;
 			}
 			if (bLevels.checkRegion(tapx,tapy)) {
 				app.gotoAppState(Deflektor.APPSTATE_SELECTLEVEL);
 				bLevels.touched=false;
 			};
 			if (bSoundOn.checkRegion(tapx,tapy)) {
 				app.soundEnabled=!app.soundEnabled;
 				bSoundOn.touched=false;
 			}
 			break;
 		};
 		return false;
 	}
 
 	boolean checkInBox(int x,int y, int bx, int by, int bwidth, int bheight) {
 		return (x>=bx)&&(x<(bx+bwidth))&&(y>=by)&&(y<(by+bheight));
 	};
 	
 	float touchX=0;
 	float touchY=0;
 	int restDelta = 0;
 	boolean touched = false;
 	
 	public boolean touchDown(float x, float y, int pointer, int button) {
 		
 		int touchx=(int)(x-app.winX)/app.sprScale;
 		int touchy=(int)(y-app.winY)/app.sprScale;
 
 		
 		touched=true;
 		switch (winStateId) {
 		case WINSTATE_GAMING:
 			touchX = x;
 			touchY = y;
 			restDelta = 0;
 			if (app.controlsTouchAndDrag) setCursor((int)x,(int)y);
 			if (gameStateId==GAMESTATE_LEVELCOMPLETED) {
 				if (bNext.checkRegion(touchx,touchy)) {
 					bNext.touched=true;
 					app.playSound(Deflektor.SND_TAP);
 				};
 				if (bRestart.checkRegion(touchx,touchy)) {
 					bRestart.touched=true;
 					app.playSound(Deflektor.SND_TAP);
 				};
 				if (bLevels.checkRegion(touchx,touchy)) {
 					bLevels.touched=true;
 					app.playSound(Deflektor.SND_TAP);
 				};
 				break;
 				
 			}
 			break;
 		case WINSTATE_PAUSED:
 			if (bResume.checkRegion(touchx,touchy)) {
 				bResume.touched=true;
 				app.playSound(Deflektor.SND_TAP);
 			}
 			if (bRestart.checkRegion(touchx,touchy)) {
 				bRestart.touched=true;
 				app.playSound(Deflektor.SND_TAP);
 			}
 			if (bLevels.checkRegion(touchx,touchy)) {
 				bLevels.touched=true;
 				app.playSound(Deflektor.SND_TAP);
 			};
 			if (bSoundOn.checkRegion(touchx,touchy)) {
 				bSoundOn.touched=true;
 				app.playSound(Deflektor.SND_TAP);
 			}
 			
 			break;
 		};
 		
 		
 
 
 		return false;
 	}
 	
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		touched=false;
 		if (!app.controlsTapThenDrag) {
 			disableCursor();
 		};
 		untouchButtons();
 		return false;
 	}
 	
 	void untouchButtons() {
 		if (bResume.touched || bRestart.touched || bLevels.touched || bSoundOn.touched || bSoundOff.touched || bNext.touched) app.playSound(Deflektor.SND_UNTAP);
 		bResume.touched=false;
 		bRestart.touched=false;
 		bLevels.touched=false;
 		bSoundOn.touched=false;
 		bSoundOff.touched=false;
 		bNext.touched=false;
 	};
 	
 
 	public boolean pan(float x, float y, float deltaX, float deltaY) {
 		switch (winStateId) {
 		case WINSTATE_GAMING:
 			if (cursorEnabled && beamState!=BEAMSTATE_CONNECTED && (app.controlsTapThenDrag || app.controlsTouchAndDrag) && (gameStateId==GAMESTATE_GAMING || gameStateId == GAMESTATE_ACCUMULATING_ENERGY)) {
 				int delta=(int)Math.sqrt((deltaX)*(deltaX)+(deltaY)*(deltaY));
 				if (deltaX<(-deltaY)) delta=-delta;
 				delta = delta + restDelta;
 				rotateMirror( cursorX, cursorY, ((int)(delta/app.panScale))&0x1f);
 				restDelta=(int)(delta-((int)(delta/app.panScale))*app.panScale);
 			};
 			break;
 		};
 		return false;
 	}
 	
 	public boolean keyDown(int k) {
 		return false;
 	}
 
 	public boolean keyUp(int k) {
 		if (k==Keys.BACK || k==Keys.MENU) {
 		//if(Gdx.input.isKeyPressed(Keys.BACK)) {
 			if (winStateId==WINSTATE_GAMING) {
 				if ((gameStateId==GAMESTATE_LEVELCOMPLETED) || (gameStateId==GAMESTATE_GAMEOVER_OVERHEAT) || (gameStateId==GAMESTATE_GAMEOVER_NOENERGY)) {
 					app.gotoAppState(Deflektor.APPSTATE_SELECTLEVEL);
 				} else {
 					app.stopContinuousSound();
 					winStateId=WINSTATE_PAUSED;
 				};
 			} else winStateId=WINSTATE_GAMING;//app.gotoAppState(Deflektor.APPSTATE_MENU);
 			app.playSound(Deflektor.SND_TAP);
 			return true;
 		};
 		return false;
 	}
 
 	
 
 	
 	
 	/// game ///
 	public final static int field_width = 15;
 	public final static int field_height = 9;
 	int field[];
 	
 	final int NULL = 0;
 	final int LASR = 0x0100;
 	final int RCVR = 0x0200;
 	final int MIRR = 0x0300;
 	final int WARP = 0x0400;			//(teleport) warpbox will connect you with next warpbox
 	final int CELL = 0x500;
 	final int MINE = 0x600;				//Mine causes overload and increases overload meter
 	final int WL_A = 0x700;			//reflects laser
 	final int WL_B = 0x800;			//stops laser
 	final int PRSM = 0x900;			//prism turns laser at random
 	final int SL_A = 0xa00;			//reflects laser if angle is different - 
 	final int SL_B = 0xb00;			//if the angle is different the laser will stop - 
 	final int _EXPL = 0xc00;			// CELL/WALL/MINE  -  
 	
 	final int ROTATING = 0x2000;	//autorotate
 	final int EXPLODE = 0x4000;		//kill this brick when all cells burned off
 	
 	Gremlin grm[];
 	
 	final int OriginalFPS = 20;
 	
 	int packedLevels[][] = {
 		//01
 		{
 			116*OriginalFPS, //energySteps
 			0,	//count of gremlins
 			MIRR|6,NULL|3,WL_B|0x0a,CELL,CELL,WL_A|0x05,MIRR,NULL|1,SL_B|7|ROTATING,CELL,CELL,WL_A|0x05,WL_A|0x0b,
 			NULL|1,CELL,CELL,CELL,WL_B|0x0a,CELL,CELL,WL_A|0x05,NULL|3,CELL,NULL|2,WL_A|0x05,
 			NULL|1,CELL,MIRR|ROTATING,CELL,WL_B|0x0f,PRSM,WL_A|0x0c,WL_A|0x0c,NULL|4,SL_B|7,NULL|2,
 			NULL|1,CELL,CELL,CELL,WL_B|0x0f,NULL|5,WARP|1,NULL|3,MIRR,
 			NULL|1,WL_B|0x0c,WL_B|0x0c,WL_B|0x0c,WL_B|0x0f,NULL|2,	SL_A|4|ROTATING,NULL|1,CELL,NULL|5,
 			NULL|2,MIRR,NULL|11,MIRR,
 			NULL|11,MINE,SL_B|7, NULL|2,
 			MIRR|12,NULL|2,LASR|3,WL_A|0x05,NULL|4,WL_A|0x0d,CELL,MINE,CELL,NULL|1,WARP|1,
 			NULL|2,WL_A|0x0a,RCVR|1,WL_A|0x05|EXPLODE,NULL|3,MIRR,WL_A|0x05,CELL,NULL|1,WL_A|0x03,WL_A|0x03,WL_A|0x02,
 
 		},
 		//02
 		{
 			141*OriginalFPS, //energySteps
 			0,	//count of gremlins
 			MIRR,CELL,MINE,NULL,CELL,WL_A|4,NULL|3,CELL,WL_A|1,NULL,MINE,NULL,RCVR|2,
 			NULL,WL_A|1,WL_A|4,CELL,WL_A|8,WL_A|4,WL_A|9,NULL,MIRR,NULL|3,WL_A|2,CELL,WL_A|3|EXPLODE,
 			WL_A|8,NULL,WL_A|4,WL_A|8,MINE,NULL,WL_A|2,WL_A|2,NULL,WL_A|2,WL_A|8,MINE,WL_A|1,NULL|2,
 			NULL,MIRR,WL_A|1,NULL,WL_A|8,WL_A|3,NULL,MINE,NULL|2,CELL,WL_A|8,CELL,WL_A|9,NULL,
 			MINE,WL_A|2,CELL,NULL,WL_A|2,NULL,MIRR,NULL,WL_A|8,NULL,WL_A|9,NULL|2,MINE,NULL,
 			NULL|2,WL_A|2,NULL,WL_A|4,NULL|2,WL_A|6,NULL,CELL,NULL,WL_A|1,WL_A|4,WL_A|4,WL_A|5,
 			LASR|2,CELL,NULL|2,MIRR,NULL,WL_A|2,NULL,WL_A|4,NULL,MIRR,NULL,WL_A|4,NULL|2,
 			NULL,WL_A|8,WL_A|6,MINE,NULL,WL_A|1,CELL,WL_A|6,NULL|2,WL_A|2,WL_A|4,MINE,WL_A|2,MIRR,
 			MIRR,NULL,WL_A|1,NULL,WL_A|4,NULL,WL_A|1,NULL,MIRR,NULL|2,WL_A|9,WL_A|1,NULL,CELL,
 		},
 		//03
 		{
 			116*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			CELL,CELL,WL_A|11,NULL|3,MIRR,NULL,CELL,WL_A|5,NULL|2,CELL,NULL,MIRR,
 			CELL,CELL,WL_A|4,WL_A|13,NULL|5,WL_A|5,NULL|3,WL_A|3,NULL,
 			CELL,CELL,PRSM,NULL,MIRR,NULL|3,WL_A|3,WL_A|15,WL_A|10,NULL|2,WL_A|10,SL_A|1|ROTATING,
 			CELL,CELL,WL_A|1,WL_A|7,NULL|9,WL_A|11,CELL,
 			CELL,CELL,WL_A|14,NULL|4,WL_A|1,WL_A|3,WL_A|2,NULL,MIRR,NULL,WL_A|4,WL_A|12,
 			WL_A|12,WL_A|12,WL_A|8,NULL,WL_A|15,WL_A|10,PRSM,WL_B|5|EXPLODE,RCVR|3,WL_A|14,WL_A|12,NULL,SL_A|2|ROTATING,NULL|2,
 			WL_A|2,CELL,NULL|5,WL_A|4,WL_A|12,WL_A|10,MIRR,NULL,WL_A|3,WL_A|7,NULL,
 			WL_A|10,NULL|5,SL_A|6|ROTATING,NULL|2,WL_A|13,NULL|2,WL_A|12,WL_A|12,CELL,
 			WL_A|11,WL_A|3,NULL,MIRR,NULL|4,CELL,WL_A|5,LASR|0,WL_A|11,WL_A|3,NULL|2,
 			
 		},
 		//04
 		{
 			128*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MINE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,MIRR,CELL,CELL,CELL,MINE,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,
 			CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,CELL,CELL,MINE,
 			CELL,CELL,CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,CELL,
 			RCVR|1,WL_B|0x05|EXPLODE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,NULL,LASR|3,
 			
 		},
 		//05
 		{
 			90*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,RCVR|2,
 			CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,MINE|EXPLODE,
 			MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,
 			NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,
 			MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,
 			NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,
 			MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,
 			NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,
 			LASR,NULL,MIRR,NULL,MIRR,CELL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,
 			
 		},
 		//06
 		{
 			103*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			LASR|1,NULL,MIRR,NULL,WL_A|10,MIRR,NULL,WL_A|2,MIRR,NULL|5,PRSM,
 			WL_A|15,WL_A|3,WL_A|3,NULL|4,WL_A|10,NULL|2,SL_A|3|ROTATING,NULL,SL_A|5|ROTATING,CELL,NULL,
 			MINE,NULL|4,WL_A|15,NULL,WL_A|10,SL_A|5|ROTATING,NULL|2,MINE,NULL|2,PRSM,
 			WL_A|10,CELL,WL_A|1,NULL|2,WL_A|15,NULL,WL_A|10,CELL,NULL,SL_A|1|ROTATING,NULL,SL_A|1|ROTATING,CELL,NULL,
 			WL_A|10,NULL,WL_A|5,NULL,CELL,NULL|2,WL_A|11,WL_A|3,WL_A|2,NULL|4,PRSM,
 			WL_A|10,CELL,WL_A|15,NULL|2,CELL,NULL|2,CELL,WL_A|10,SL_A|2|ROTATING,CELL,SL_A|5|ROTATING,CELL,NULL,
 			WL_A|10,NULL|3,WL_A|5,WL_A|12,WL_A|12,NULL|2,WL_A|10,CELL,NULL|3,PRSM,
 			WL_A|10,NULL|6,MIRR,NULL,WL_A|10,SL_A|3|ROTATING,CELL,SL_A|5|ROTATING,CELL,WL_B|3|EXPLODE,
 			WL_A|15,NULL,MIRR,NULL|5,MINE,WL_A|11,WL_A|3,WL_A|3,WL_A|3,WL_A|3,RCVR,
 			
 		},
 		//07
 		{
 			141*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			CELL,NULL,MIRR,NULL,WL_A|12,WL_A|8,MIRR,NULL|3,MIRR,WL_A|5,RCVR|1,WL_B|15|EXPLODE,MIRR,
 			WL_A|14,WL_A|12,NULL|2,CELL,NULL|2,WL_A|15,NULL,WL_A|13,NULL,WL_A|5,WL_A|12,WL_A|13,NULL,
 			NULL,CELL,NULL|3,WL_A|3,NULL|2,CELL,WL_A|5,NULL|2,CELL,WL_A|5,NULL,
 			WL_A|3,WL_A|7,WL_A|12,NULL,WL_A|5,WL_A|10,NULL,WL_A|13,WL_A|2,NULL|2,MIRR,NULL,WL_A|15,NULL,
 			NULL|2,MIRR,NULL|3,CELL,WL_A|5,WL_A|14,WL_A|12,NULL|3,WL_A|12,NULL,
 			NULL|2,WL_A|3,WL_A|3,CELL,NULL|3,WL_A|12,NULL,CELL,NULL,WL_A|5,NULL|2,
 			MIRR,NULL|2,WL_A|4,WL_A|10,NULL,WL_A|5,NULL|2,WL_A|10,WL_A|15,NULL,WL_A|5,NULL,MIRR,
 			WL_A|3,WL_A|3,NULL|2,WL_A|10,CELL,WL_A|5,NULL,MIRR,NULL|4,CELL,NULL,
 			LASR|1,NULL,MIRR,NULL,WL_A|11,WL_A|3,WL_A|14,CELL,NULL|2,WL_A|3,WL_A|3,WL_A|3,WL_A|3,WL_A|15,
 		},
 		//08
 		{
 			116*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			LASR|2,WL_B|5,WL_B|15,WL_A|15,WL_A|12,WL_A|12,WL_A|12,WL_A|13,WL_A|10,NULL,WL_A|12,NULL,MINE,NULL,WARP|0,
 			NULL,WL_B|5,RCVR|1,WL_A|5|EXPLODE,CELL,CELL,CELL,WL_A|4,WL_A|8,NULL|3,MIRR,NULL|2,
 			NULL,WL_B|5,WL_B|15,WL_A|14,CELL,MINE,CELL,CELL,WL_A|13,NULL|6,
 			NULL|2,WL_A|15,WL_A|10,MINE,MINE,MINE,CELL,WL_A|5,WL_A|3,NULL|5,
 			MIRR,NULL,PRSM,NULL,CELL,MINE,CELL,MINE,NULL,PRSM,NULL|4,MIRR,
 			NULL|2,WL_A|12,WL_A|10,MINE,CELL,MINE,CELL,WL_A|5,WL_A|12,NULL|4,MIRR,
 			NULL|3,WL_A|11,WL_A|3,WL_A|3,WL_A|3,WL_A|3,WL_A|7,NULL|4,WL_B|1,WL_B|3,
 			NULL|11,WL_B|7,NULL,WL_B|12,CELL,
 			NULL|2,MIRR,NULL,MINE,MIRR,MINE,NULL,MIRR,NULL|2,WL_B|10,WARP|0,WL_B|1,WL_B|15,
 		},
 		//09
 		{
 			116*OriginalFPS, //energySteps
 			4,	//count of gremlins
 			MINE,CELL,CELL,PRSM,CELL,CELL,CELL,LASR|2,CELL,CELL,CELL,PRSM,CELL,CELL,MINE,
 			CELL,CELL,CELL,CELL,MINE,CELL,CELL,NULL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,MINE,CELL,CELL,WL_B|3|EXPLODE,CELL,CELL,MINE,CELL,CELL,CELL,CELL,
 			MINE,CELL,CELL,PRSM,CELL,CELL,CELL,RCVR|0,CELL,CELL,CELL,PRSM,CELL,CELL,MINE,
 		},
 		//10
 		{
 			90*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,MINE,MIRR,NULL,MIRR,
 			NULL,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,MINE,
 			MIRR,CELL,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,MINE,MIRR,CELL,MIRR,CELL,MIRR,
 			NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,
 			MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,
 			NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,
 			MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,
 			NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,MINE|EXPLODE,
 			LASR|0,NULL,MIRR,NULL,MIRR,CELL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,NULL,RCVR|0,
 		},
 		//11
 		{
 			116*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			RCVR|2,NULL|3,MIRR,NULL,WARP|0,NULL,CELL,NULL|2,MINE,NULL,MINE,NULL,
 			MINE|EXPLODE,NULL,CELL,WL_A|11,WL_A|3,NULL|3,WL_A|14,MINE,NULL|2,CELL,NULL|2,
 			MIRR,NULL,SL_A|2|ROTATING,NULL,WL_A|10,NULL|3,WL_A|10,NULL,CELL,MINE,NULL|2,CELL,
 			NULL,MIRR,NULL,CELL,WL_A|8,NULL|4,CELL,NULL|3,MINE,WL_A|5,
 			WL_A|3,WL_A|3,WL_A|3,WL_A|10,SL_A|7|ROTATING,NULL,MIRR,NULL,WL_A|10,NULL,WL_A|10,NULL|3,WL_A|5,
 			CELL,CELL,CELL,WL_A|10,NULL|4,WL_A|12,NULL,WL_A|13,NULL,MIRR,NULL,WL_A|5,
 			CELL,PRSM,CELL,WL_A|15,NULL|2,WL_A|11,NULL,SL_A|2|ROTATING,NULL|5,WL_A|15,
 			NULL|3,WL_A|10,NULL,WL_A|10,WL_A|5,NULL|2,WL_A|5,WL_A|3,NULL|2,WL_A|12,WL_A|15,
 			NULL,WARP|0,NULL,WL_A|10,MIRR,CELL,WL_A|5,NULL,MIRR,NULL|3,MIRR,NULL,LASR|3,
 		},		
 		//12
 		{
 			90*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MIRR,NULL,MINE,NULL,CELL,NULL|2,MINE,NULL|2,MIRR,NULL|2,CELL,NULL,
 			NULL,WL_A|15,NULL,CELL,WL_A|12,WL_A|12,WL_A|12,NULL,MIRR,CELL,NULL|3,MINE,CELL,
 			LASR|1,NULL|3,MIRR,NULL,MIRR,NULL|4,MIRR,NULL|3,
 			NULL,MINE,NULL|5,WL_A|2,NULL|2,WARP|0,NULL|3,MIRR,
 			WL_B|15,WL_A|10,MIRR,NULL|2,CELL,NULL,WL_A|10,SL_B|6,SL_B|1,NULL|2,MINE,NULL|2,
 			CELL,NULL|6,WL_A|10,NULL|5,WL_B|14,WL_B|12,
 			CELL,NULL,MIRR,NULL,SL_B,NULL|2,WL_A|15,NULL|2,MIRR,NULL|2,WL_A|10|EXPLODE,RCVR|3,
 			MIRR,NULL|5,WARP|0,NULL,MIRR,NULL,CELL,NULL|2,WL_B|11,WL_B|3,
 			MINE,NULL,MIRR,NULL|2,MINE,NULL|2,CELL,WL_B|3,WL_B|3,MIRR,NULL|2,CELL,
 		},		
 		//13
 		{
 			116*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			NULL,CELL,NULL,WL_A|3,WL_A|15,NULL,WL_A|10,NULL,CELL,WL_A|3,WL_A|14,NULL,CELL,WL_A|15,LASR|2,
 			MIRR|ROTATING,NULL,CELL,WL_A|10,NULL|2,WL_A|10,MIRR,NULL,WL_A|10,NULL,WL_A|7,WL_A|3,WL_A|15,NULL,
 			NULL|3,WL_A|10,MIRR,NULL,WL_A|10,NULL|2,WL_A|10,MIRR,WL_A|1,NULL|2,MIRR,
 			NULL|3,WL_A|10,NULL|2,WL_A|8,WL_A|5,NULL,WL_A|3,NULL,WL_A|5,NULL,WL_A|15,WL_A|3,
 			NULL,WL_A|10,NULL|2,WL_A|3,NULL|2,WL_A|11,WL_A|2,WL_A|5,NULL|3,WL_A|5,WL_A|12,
 			NULL,WL_A|10,NULL|2,WL_A|10,SL_A|4|ROTATING,NULL|2,WL_A|10,WL_A|5,NULL,WL_A|4,NULL|2,MIRR,
 			NULL,WL_A|15,WL_A|5,WL_A|12,WL_A|8,NULL|3,WL_A|10,WL_A|5,NULL,PRSM,WL_A|5,WL_A|13,NULL,
 			SL_B|4|EXPLODE,WL_A|15,WL_A|12,NULL,MIRR,NULL,CELL,WL_A|15,WL_A|10,WL_A|5,NULL,CELL,CELL,WL_A|5,NULL,
 			RCVR,WL_A|15,NULL,CELL,NULL|2,WL_A|5,CELL,MIRR,WL_A|5,NULL,CELL,CELL,WL_A|5,CELL,
 		},		
 		//14
 		{
 			167*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			LASR|2,RCVR|1,WL_B|5|EXPLODE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,MINE,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,
 			CELL,CELL,CELL,CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,PRSM,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			MINE,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,
 		},		
 		//15
 		{
 			103*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MIRR,CELL,MIRR|ROTATING,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR|ROTATING,CELL,MIRR,
 			NULL,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,
 			MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,MINE,MIRR,CELL,MIRR,CELL,MIRR,
 			CELL,MIRR,NULL,MIRR,NULL,MIRR,MINE|EXPLODE,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,
 			MIRR,NULL,MIRR,CELL,MIRR,NULL,RCVR,NULL,MIRR,NULL,MIRR|ROTATING,NULL,MIRR,NULL,MIRR,
 			NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,
 			MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,
 			NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,CELL,
 			LASR,NULL,MIRR,NULL,MIRR|ROTATING,CELL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,MINE,MIRR,
 		},		
 		//16
 		{
 			90*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			WL_A|10,NULL|2,WL_A|5,CELL,NULL,CELL,WL_A|10,WARP|3,WL_B|15,WARP|2,NULL,WL_B|7,MINE,CELL,
 			WL_A|10,NULL,CELL,WL_A|4,SL_B|6,NULL|2,WL_A|10,NULL,WARP|0,NULL|2,PRSM,CELL,MINE,
 			WL_A|10,WL_A|5,NULL,WL_A|1,WL_A|5,NULL,WL_A|5,WL_A|8,WL_A|15,WL_A|3,NULL,CELL,WL_B|13,MINE,CELL,
 			WL_A|10,WL_A|5,NULL,WL_A|5,NULL|2,WL_A|5,NULL|2,WL_A|10,NULL,MINE,WL_B|4,WL_B|15,WL_B|15,
 			WL_A|10,WL_A|5,NULL,WL_A|7,NULL|2,WL_A|5,WL_A|3,NULL,WL_A|10,MIRR,NULL|2,SL_B|EXPLODE,RCVR|3,
 			WL_A|10,CELL,NULL,WL_A|10,NULL|3,WL_A|5,NULL,WL_A|13,WL_A|3,WL_A|14,WL_A|12,WL_A|12,WL_A|13,
 			WL_A|10,NULL,CELL,WL_A|10,NULL|5,WL_A|5,LASR|1,NULL,MIRR,NULL,WARP|0,
 			WARP|1,NULL|2,WL_A|10,PRSM,NULL|2,MIRR,NULL,WL_A|5,NULL|4,WL_A|5,
 			NULL,MIRR,NULL,WL_A|11,WL_A|3,WL_A|3,NULL|2,CELL,WL_A|5,WARP|3,WL_A|3,WARP|2,WL_A|3,WARP|1,
 			
 		},		
 		//17
 		{
 			128*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			RCVR|1,SL_A|EXPLODE,NULL,SL_A|4,SL_A|3|ROTATING,NULL|3,CELL,NULL|4,MIRR,NULL,
 			NULL|2,CELL,NULL|3,MINE,SL_A|6,SL_A|6,SL_A|6,CELL,NULL,SL_A|1|ROTATING,NULL,SL_A|4,
 			MINE,SL_A,SL_A,SL_A,NULL,MIRR,NULL|5,MIRR,NULL|2,SL_A|4,
 			SL_A|2,NULL|6,SL_A|1|ROTATING,NULL,MIRR,NULL|2,CELL,NULL|2,
 			NULL|3,MIRR,NULL,SL_A|3,NULL|2,CELL,NULL|3,SL_A|6,SL_A|6,MINE,
 			NULL,SL_A|2|ROTATING,NULL|3,SL_A|3,NULL,MIRR,NULL,SL_A|4,SL_A|4,CELL,NULL|3,
 			CELL,NULL|4,SL_A|3,NULL|4,SL_A|4,NULL|2,MIRR,CELL,
 			SL_A|3,CELL,NULL|2,MINE,NULL|3,SL_A|2|ROTATING,NULL|6,
 			SL_A|3,NULL,SL_A|4,LASR,NULL|2,CELL,NULL|2,MINE,SL_A|2,CELL,MIRR,NULL,SL_A|3,
 			
 		},	
 		//18
 		{
 			141*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			LASR|1,NULL|2,MIRR,WL_A|5,WL_A|8,CELL,WL_A|4,WL_A|15,WL_A|10,RCVR|1,WL_A|5|EXPLODE,CELL,NULL,MIRR,
 			WL_A|12,WL_A|13,WL_A|11,NULL,WL_A|7,NULL,MIRR,NULL,WL_A|15,CELL,NULL,WL_A|4,WL_A|15,WL_A|11,NULL,
 			MIRR,NULL|3,WL_A|13,NULL|3,WL_A|12,NULL|2,CELL,NULL,WL_A|15,NULL,
 			NULL|3,MIRR,NULL,CELL,NULL|2,MIRR,NULL,WL_A|15,WL_A|15,NULL,WL_A|13,NULL,
 			NULL,WL_A|14,NULL|2,WL_A|1,WL_A|15,NULL,WL_A|2,NULL|3,WL_A|13,WL_A|11,NULL,CELL,
 			NULL,WL_A|10,MIRR,NULL,WL_A|7,WL_A|14,WL_A|12,WL_A|8,CELL,NULL,MIRR,NULL,WL_A|15,NULL|2,
 			CELL,WL_A|11,WL_A|3,NULL|3,MIRR,NULL|2,WL_A|11,NULL|2,WL_A|15,NULL,MIRR,
 			WL_A|7,WL_A|14,CELL,NULL,WL_A|15,WL_A|11,WL_A|3,WL_A|2,NULL,WL_A|13,NULL|2,CELL,NULL|2,
 			WL_A|15,WL_A|10,MIRR,NULL,CELL,NULL,CELL,NULL,MIRR,WL_A|5,WL_A|3,WL_A|3,WL_A|3,WL_A|3,WL_A|7,
 		},	
 		//19
 		{
 			167*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			CELL,CELL,CELL,CELL,MINE,CELL,WL_A|5,WL_A|15,LASR|1,NULL,NULL,MIRR,CELL,CELL,MINE,
 			MIRR,CELL,CELL,CELL,CELL,MINE,WL_A|5,CELL,MINE,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,MIRR,CELL,CELL,WL_A|10|EXPLODE,RCVR|3,WL_A|15,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,
 			MINE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,
 		},
 		//20
 		{
 			116*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,
 			NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,
 			LASR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,MINE,MIRR,CELL,MIRR,CELL,MIRR,
 			NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR|ROTATING,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,
 			RCVR|2,NULL,MIRR,CELL,MIRR,NULL,MIRR|ROTATING,MINE,MIRR|ROTATING,CELL,MIRR,NULL,MIRR,CELL,MIRR,
 			MINE|EXPLODE,MIRR,NULL,MIRR,MINE,MIRR,CELL,MIRR|ROTATING,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,
 			MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,
 			NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,CELL,
 			MIRR,MINE,MIRR,MINE,MIRR,CELL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,
 		},
 		//21
 		{
 			167*OriginalFPS, //energySteps
 			0,	//count of gremlins
 			PRSM,NULL|11,CELL,WL_B|5|EXPLODE,RCVR|3,
 			NULL|8,PRSM,NULL|2,CELL,NULL|3,
 			MIRR,NULL|2,PRSM,NULL,CELL,NULL,CELL,NULL,CELL,NULL|3,MIRR,NULL,
 			NULL|6,WL_B|15,WL_B|15,WL_B|15,NULL|5,CELL,
 			NULL,CELL,NULL|13,
 			NULL|2,PRSM,NULL|7,PRSM,NULL|2,CELL,NULL,
 			NULL|7,PRSM,NULL|3,MIRR,NULL|3,
 			NULL|4,MIRR,NULL|8,PRSM,NULL,
 			MIRR,NULL|2,CELL,NULL|3,LASR,NULL|2,CELL,NULL|3,CELL,
 		},
 		//22
 		{
 			128*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			WL_A|15,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|10,WL_B|15,WL_A|12,WL_A|12,WL_A|15,NULL,SL_A|7|ROTATING,CELL,SL_A|2|ROTATING,
 			NULL|4,WARP|0,NULL,WL_A|10,NULL,WL_A|5,NULL|3,CELL,CELL,CELL,
 			PRSM,NULL,PRSM,NULL|2,MIRR,WL_A|10,MIRR,WL_A|5,NULL|3,SL_A|6|ROTATING,CELL,SL_A|5|ROTATING,
 			CELL,NULL|3,WL_A|5,WL_A|12|EXPLODE,WL_A|10,NULL,WL_A|5,NULL,MIRR,NULL|3,CELL,
 			PRSM,CELL,PRSM,NULL,WL_A|5,RCVR|0,WL_A|15,LASR|0,WL_A|15,NULL|3,SL_A|2|ROTATING,NULL,SL_A|1|ROTATING,
 			CELL,NULL|3,WL_A|4,WL_A|12,WL_A|12,WL_A|12,WL_A|8,NULL|5,CELL,
 			PRSM,CELL,PRSM,NULL,MIRR,WL_A|5,WL_A|3,WL_A|14,NULL|2,WL_A|15,NULL,SL_A|1|ROTATING,NULL,SL_A|6|ROTATING,
 			CELL,NULL,CELL,NULL|4,WL_A|10,MIRR,NULL,WL_A|15,NULL|4,
 			PRSM,CELL,PRSM,CELL,NULL|2,WARP|0,NULL|3,WL_A|15,WL_A|3,WL_A|3,WL_A|3,WL_A|7,
 		},
 		//23
 		{
 			167*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			WL_B|15,CELL,NULL,MIRR,NULL,CELL,NULL|3,CELL,NULL,MIRR,NULL,CELL,WL_B|15,
 			WL_B|15,NULL,CELL,NULL,SL_B|6,NULL,WL_B|15,MINE,WL_B|15,NULL,SL_B|2,NULL,CELL,NULL,WL_B|15,
 			WL_B|15,WL_B|13,NULL|5,SL_B,NULL|5,WL_B|14,WL_B|15,
 			WL_B|4,WL_B|13,WL_B|2,MINE,NULL|2,SL_B|6,NULL,SL_B|2,NULL|2,MINE,WL_B|1,WL_B|14,WL_B|8,
 			SL_A,WL_B|5,LASR|1,NULL,PRSM,NULL|2,MIRR|ROTATING,NULL|2,PRSM,NULL,MINE,WL_B|10,SL_A,
 			WL_B|1,WL_B|7,WL_B|8,MINE,NULL|2,SL_B|2,NULL,SL_B|6,NULL|2,MINE,WL_B|4,WL_B|11,WL_B|2,
 			WL_B|15,WL_B|7,NULL|5,SL_B,NULL|5,WL_B|11,WL_B|15,
 			WL_B|15,NULL,CELL,NULL,SL_B|2,NULL,WL_B|15,MINE|EXPLODE,WL_B|15,NULL,SL_B|6,NULL,CELL,NULL,WL_B|15,
 			WL_B|15,CELL,NULL,MIRR,NULL,CELL,NULL,RCVR|0,NULL,CELL,NULL,MIRR,NULL,CELL,WL_B|15,
 		},
 		//24
 		{
 			141*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MINE,CELL,CELL,CELL,CELL,CELL,CELL,LASR|2,CELL,CELL,CELL,MIRR,CELL,CELL,MINE,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,NULL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,WL_B|14,WL_B|12|EXPLODE,WL_B|13,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,WL_B|10,RCVR|0,WL_B|5,CELL,MIRR,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,WL_B|11,WL_B|3,WL_B|7,CELL,CELL,CELL,CELL,CELL,CELL,
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,WL_B|15,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR|ROTATING,CELL,
 			MINE,CELL,CELL,CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,
 		},
 		//25
 		{
 			116*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,
 			NULL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,MINE,
 			MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,CELL,MIRR,CELL,MIRR,
 			NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,
 			LASR|1,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,MINE|EXPLODE,RCVR|3,
 			NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,
 			MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,
 			NULL,MIRR,MINE,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,NULL,
 			MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,
 		},
 		//26
 		{
 			167*OriginalFPS, //energySteps
 			0,	//count of gremlins
 			RCVR|1,WL_B|5|EXPLODE,NULL,WARP|0,NULL|2,CELL,NULL|2,MIRR,WL_A|12,WL_A|12,WL_A|12,WL_A|12,CELL,
 			WL_B|15,WL_B|12,WL_B|8,NULL,WL_B|4,WL_B|12,WL_B|15,WL_B|15,WL_B|10,NULL,SL_B|7|ROTATING,CELL,SL_B|3|ROTATING,NULL,WL_A|5,
 			MINE,MIRR,CELL,PRSM,CELL,MIRR,MINE,WL_B|5,WL_B|10,SL_B|7|ROTATING,NULL|3,MIRR,WL_A|4,
 			MIRR,CELL,MIRR,CELL,MIRR,CELL,MIRR,WL_B|5,NULL|2,WL_A|1,WL_A|3,WL_A|3,WL_A|3,WL_A|2,
 			CELL,MIRR,CELL,MIRR,CELL,MIRR,CELL,WL_B|5,NULL|2,WL_A|7,NULL|2,CELL,WL_A|11,
 			MIRR,CELL,MIRR,MINE,MIRR,CELL,MIRR,WL_B|5,MIRR,NULL|5,WL_A|5,
 			MINE,MIRR,MINE,MIRR,CELL,MIRR,CELL,WL_B|5,CELL,WL_A|3,WL_A|3,WL_A|3,WL_A|2,NULL,WL_A|5,
 			MIRR,CELL,MIRR,CELL,MIRR,MINE,MIRR,WL_B|5,NULL|6,WL_A|7,
 			CELL,MIRR,MINE,MIRR,CELL,MIRR,MINE,WL_B|15,WARP|0,NULL|3,MIRR,NULL,LASR|3,
 		},
 		//27
 		{
 			180*OriginalFPS, //energySteps
 			4,	//count of gremlins
 			WL_B|14,WL_B|12,WL_B|8,MIRR,NULL,MIRR,WL_B|4,WL_B|12,WL_B|8,MIRR,NULL,MIRR,WL_B|4,WL_B|12,WL_B|13,
 			WL_B|10,SL_B|2,NULL|5,RCVR|2,NULL|5,SL_B|6,WL_B|5,
 			WL_B|10,NULL|6,WL_B|3|EXPLODE,NULL|6,WL_B|5,
 			NULL|15,
 			MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,
 			NULL|6,MINE,NULL,MINE,NULL|6,
 			WL_B|10,NULL|13,WL_B|5,
 			WL_B|10,NULL|13,WL_B|5,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,LASR|0,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 		},
 		//28
 		{
 			167*OriginalFPS, //energySteps
 			1,	//count of gremlins
 			MIRR,MIRR,NULL|2,WARP|3,WL_A|5,NULL,MIRR,MIRR,NULL|3,MIRR,WL_A|5,CELL,
 			MIRR,MIRR,NULL|3,WL_A|5,NULL,MIRR,MIRR,NULL|2,WARP|1,WL_A|1,WL_A|6,CELL,
 			WL_A|3,WL_A|3,NULL|2,CELL,WL_A|5,NULL|3,WARP|0,NULL|3,CELL,CELL,
 			CELL,WL_A|4,NULL|3,WL_A|14,WL_A|12,WL_A|13,WL_A|3,WL_A|7,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|12,
 			RCVR|1,MINE|EXPLODE,NULL|2,MIRR,WL_A|10,WARP|1,NULL|2,WL_A|10,WARP|0,CELL,CELL,CELL,CELL,
 			WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|12,MIRR,MIRR,NULL|2,WL_A|10,CELL,NULL|4,
 			MINE,CELL,NULL,WL_A|5,NULL,MIRR,MIRR,NULL|2,WL_A|10,MINE,NULL|4,
 			WL_A|5,WL_A|12,WL_A|12,WL_A|12,NULL|5,WL_A|10,CELL,NULL|2,MIRR,MIRR,
 			LASR|1,NULL,MIRR,NULL|6,WL_A|10,WARP|3,NULL|2,MIRR,MIRR,
 		},
 		//29
 		{
 			180*OriginalFPS, //energySteps
 			0,	//count of gremlins
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,MIRR,
 			CELL,MINE,CELL,CELL,MINE,CELL,CELL,CELL,MINE,MIRR|ROTATING,MINE,CELL,CELL,CELL,CELL,
 			RCVR|2,CELL,CELL,MINE,CELL,CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,CELL,
 			WL_A|3|EXPLODE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			MIRR,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,
 			NULL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,
 			LASR|0,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 
 		},
 		//30
 		{
 			116*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MIRR,CELL,MIRR,MINE,MIRR,MINE,MIRR,MINE,MIRR,CELL,MIRR,MINE,MIRR,MINE,MIRR,
 			MINE,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,MINE,
 			MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,CELL,MIRR,
 			CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,MINE,
 			MIRR,NULL,MIRR,NULL,MIRR,NULL,LASR,NULL,RCVR|1,MINE|EXPLODE,MIRR,NULL,MIRR,NULL,MIRR,
 			MINE,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,
 			MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,
 			MINE,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,CELL,
 			MIRR,CELL,MIRR,MINE,MIRR,MINE,MIRR,MINE,MIRR,MINE,MIRR,CELL,MIRR,MINE,MIRR,
 		},
 		//31
 		{
 			103*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			NULL,WL_A|12,WL_A|12,WL_A|13,LASR|2,WL_B|15,MIRR,NULL|5,MIRR,WL_A|5,MINE,
 			CELL,WL_A|15,WL_A|15,WL_A|3,NULL,WL_B|15,NULL,WL_B|15,NULL|5,WL_A|5,CELL,
 			NULL|4,MIRR,WL_B|15,WARP|0|EXPLODE,WL_B|15,WARP|1,NULL|3,MIRR,WL_A|5,CELL,
 			WL_A|12,WL_A|12,WL_A|12,WL_A|10,NULL,WL_B|5,RCVR,WL_B|10,NULL|5,WL_A|5,CELL,
 			MINE,CELL,CELL,WARP|0,MINE,WL_B|4,WL_B|12,WL_B|8,NULL,CELL,NULL|4,SL_A|ROTATING,
 			CELL,CELL,CELL,PRSM,WARP|1,WL_A|3,NULL|3,SL_B|ROTATING,NULL,SL_B|1|ROTATING,NULL|3,
 			MINE,CELL,CELL,CELL,CELL,WL_A|5,NULL|5,CELL,NULL,CELL,NULL,
 			CELL,MINE,CELL,CELL,CELL,WL_A|5,NULL|2,MIRR,NULL,SL_B|1|ROTATING,NULL,SL_B|5|ROTATING,NULL|2,
 			MINE,CELL,MINE,CELL,MINE,WL_A|5,NULL,MIRR,NULL|6,MIRR,
 		},
 		//32
 		{
 			154*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			CELL,NULL|2,WL_A|9,NULL,WL_A|8,MIRR,NULL,WL_A|8,NULL,WL_A|2,NULL,LASR|1,NULL,MIRR,
 			NULL,WL_A|4,MINE,WL_A|2,WL_A|4,NULL|2,WL_A|2,CELL,WL_A|8,NULL,MINE,WL_A|6,WL_A|1,NULL,
 			NULL,MIRR,NULL|2,WL_A|4,NULL,WL_A|2,NULL,WL_A|4,NULL,WL_A|9,NULL|2,CELL,NULL,
 			WL_A|10,WL_A|2,WL_A|2,WL_A|8,CELL,NULL|2,WL_A|6,NULL,WL_A|8,NULL|2,WL_A|4,NULL|2,
 			NULL,MINE,NULL|2,WL_A|9,NULL,WL_A|1,NULL|3,MIRR,NULL,CELL,WL_A|4,MINE,
 			WL_A|2,WL_A|6,CELL,WL_A|1,CELL,NULL|2,MINE,NULL,WL_A|8,WL_A|1,NULL,WL_A|8,MIRR,NULL,
 			CELL,NULL,WL_A|1,MINE,WL_A|1,WL_A|4,NULL,WL_A|4,NULL|3,WL_A|1,WL_A|2,NULL,WL_A|4,
 			MINE,MIRR,WL_A|4,NULL|3,MIRR,NULL,WL_A|9,WL_A|4,WL_A|1,CELL,WL_A|2,WL_A|8,MIRR,
 			MIRR,NULL,WL_A|4,CELL,WL_A|8,CELL,NULL,MINE|EXPLODE,RCVR|3,NULL,CELL,NULL,MINE,CELL,NULL,
 		},
 		//33
 		{
 			116*OriginalFPS, //energySteps
 			4,	//count of gremlins
 			RCVR|2,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|13,MINE,NULL|5,MIRR,NULL,WL_A|15,
 			WL_B|12|EXPLODE,CELL,SL_B|7|ROTATING,CELL,MINE,WL_A|5,NULL,MIRR,NULL|6,WL_A|5,
 			PRSM,NULL|2,MINE,MINE,WL_A|5,NULL|2,WL_A|3,WL_A|3,WL_A|10,NULL|3,WL_A|5,
 			NULL,CELL,SL_B|6|ROTATING,MINE,SL_B|4|ROTATING,WL_A|5,CELL,NULL|2,CELL,NULL|2,WL_A|14,PRSM,WL_A|12,
 			PRSM,NULL|4,WL_A|4,WL_A|12,WL_A|8,NULL|2,CELL,NULL,WL_A|10,CELL,CELL,
 			NULL,CELL,SL_B|2|ROTATING,CELL,SL_B|2|ROTATING,NULL,CELL,WL_A|5,NULL,WL_A|15,NULL|2,WL_A|8,CELL,CELL,
 			PRSM,NULL|2,MINE,NULL|2,SL_B|3|ROTATING,WL_A|5,NULL,WL_A|15,NULL|3,MINE,MINE,
 			CELL,CELL,PRSM,NULL,SL_B|3|ROTATING,NULL|2,WL_A|5,NULL|4,WL_A|12,WL_A|12,WL_A|15,
 			CELL,CELL,NULL|4,MIRR,WL_A|4,NULL,MIRR,WL_A|5,NULL,MIRR,NULL,LASR|3,
 		},
 		//34
 		{
 			180*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MINE,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE|EXPLODE,RCVR|3,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,MINE,
 			CELL,MIRR,CELL,CELL,CELL,CELL,LASR|2,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,NULL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			MINE,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,
 			
 		},
 		//35
 		{
 			128*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MIRR,MINE,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,NULL,LASR|3,
 			CELL,WARP|0,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,
 			MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,WARP|1,NULL,MIRR,NULL,MIRR,
 			MINE,MIRR,NULL,MIRR,CELL,MIRR,MINE,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,
 			MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,
 			NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,MINE,
 			MIRR,CELL,MIRR,NULL,WARP|1,MINE,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,
 			MINE|EXPLODE,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,WARP|0,MINE,
 			RCVR|0,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,CELL,MIRR,CELL,MIRR,
 		},
 		//36
 		{
 			154*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			RCVR|2,WL_B|5,WL_B|12,WL_B|13,CELL,WL_A|10,MIRR,NULL|2,CELL,CELL,CELL,WL_B|14,WL_B|12,WL_B|12,
 			WL_B|3|EXPLODE,WL_B|7,MIRR,WL_B|5,CELL,WL_A|10,NULL|2,MIRR,WL_B|12,WL_B|13,LASR|1,NULL|2,MIRR,
 			WARP|0,NULL|7,SL_B|6,NULL|6,
 			MIRR,NULL|3,SL_A,SL_A,NULL|2,CELL,SL_B|6,NULL|2,MIRR,NULL,WL_B|14,
 			NULL|7,SL_A|1,NULL|3,WL_A|3,WL_A|3,WL_A|3,WL_B|10,
 			MINE,SL_B|7,NULL,WARP|1,WARP|0,NULL|3,WARP|1,NULL|2,CELL,CELL,CELL,WL_B|10,
 			NULL,CELL,NULL|5,WL_A|3,WL_A|3,WL_A|3,MINE,CELL,MIRR|ROTATING,CELL,WL_B|10,
 			WL_B|10,NULL,WL_B|15,WL_B|15,MIRR,NULL|2,WL_A|10,CELL,CELL,WL_A|12,CELL,CELL,CELL,WL_B|10,
 			WL_B|15,NULL,CELL,CELL,SL_B|6|ROTATING,NULL,MIRR,WL_A|10,CELL,CELL,PRSM,MINE,WL_B|14,WL_B|12,WL_B|8,
 		},
 		//37
 		{
 			167*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			WL_A|15,WL_A|12,WL_A|12,WL_A|12,NULL,MINE,NULL,CELL,WL_A|7,NULL,WL_A|13,NULL,MIRR,NULL,LASR|3,
 			NULL,CELL,NULL|4,MIRR,NULL,WL_A|10,CELL,WL_A|5,NULL|2,WL_A|12,WL_A|12,
 			MIRR,NULL,WL_A|13,NULL,WL_A|15,NULL|3,WL_A|10,NULL,WL_A|5,WL_A|2,NULL,MIRR,NULL,
 			NULL,WL_A|13,WL_A|5,NULL,CELL,NULL,MINE,NULL|3,CELL,WL_A|12,WL_A|12,NULL|2,
 			NULL,MINE,NULL,MIRR,NULL,WL_A|3,SL_A|3,NULL,CELL,NULL|3,MIRR,NULL|2,
 			NULL,WL_A|3,WL_A|3,MIRR,NULL|2,SL_A|3,NULL|2,WL_A|6,WL_A|10,WL_A|1,WL_A|3,WL_A|14,WARP|0,
 			NULL,WL_A|10,WARP|0,NULL|2,WL_A|10,CELL,NULL|2,WL_A|12,NULL,WL_A|7,NULL,CELL,MINE,
 			MINE|EXPLODE,WL_A|11,NULL|3,WL_A|11,NULL|4,WL_A|14,WL_A|12,NULL,WL_A|3,WL_A|7,
 			RCVR|0,WL_A|15,NULL|2,MIRR,NULL|3,MIRR,NULL,WL_A|10,MIRR,NULL|2,CELL,
 		},
 		//38
 		{
 			128*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			WL_B|14,WL_B|12,WL_B|12,WL_B|8,WL_A|15,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|10,WL_B|12,WL_B|13,RCVR|2,
 			WARP|1,NULL,MIRR,NULL,PRSM,CELL,CELL,MINE,MINE,CELL,MINE,WL_A|10,WARP|0,WL_B|5,WL_B|3|EXPLODE,
 			NULL|4,WL_A|13,CELL,CELL,CELL,CELL,MINE,CELL,WL_A|13,WL_A|3,NULL|2,
 			NULL,SL_B,SL_B,SL_B,WL_A|5,MINE,MINE,CELL,MINE,CELL,MINE,CELL,PRSM,NULL,MIRR,
 			CELL,WL_A|2,NULL|2,WL_A|7,CELL,CELL,MINE,MINE,MINE,CELL,CELL,WL_A|14,NULL|2,
 			CELL,WL_A|10,MIRR,NULL,PRSM,CELL,CELL,CELL,MINE,CELL,MINE,CELL,WL_A|11,NULL|2,
 			CELL,WL_A|10,NULL|2,WL_A|12,WL_A|10,CELL,CELL,CELL,MINE,CELL,CELL,PRSM,NULL,MIRR,
 			MINE,NULL,WARP|1,NULL|2,WL_A|12,WL_A|11,MINE,CELL,CELL,CELL,CELL,WL_A|14,NULL|2,
 			WL_B|11,WL_B|3,WL_B|3,WL_B|3,WL_B|2,WARP|0,WL_A|5,WL_A|3,WL_A|3,WL_A|3,WL_A|3,WL_A|3,WL_A|10,WL_B|7,LASR|0,
 			
 		},
 		//39
 		{
 			167*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MINE,CELL,CELL,PRSM,CELL,CELL,CELL,CELL,MIRR,CELL,MIRR,CELL,CELL,CELL,MINE,
 			CELL,CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,SL_A|3|ROTATING,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,PRSM,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,MIRR,
 			MINE|EXPLODE,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,SL_A|ROTATING,CELL,CELL,CELL,NULL,
 			RCVR|0,MINE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,LASR|0,
 		},
 		//40
 		{
 			116*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			RCVR|1,MINE|EXPLODE,MIRR,NULL,MIRR,NULL,MIRR,MINE,MIRR,CELL,MIRR,NULL,MIRR,NULL,LASR|2,
 			MINE,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,
 			MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,
 			CELL,MIRR,NULL,SL_A|5|ROTATING,CELL,MIRR,NULL,SL_A|5|ROTATING,CELL,MIRR,NULL,SL_A|7|ROTATING,NULL,MIRR,NULL,
 			MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,
 			CELL,MIRR,NULL,SL_A|3|ROTATING,NULL,MIRR,CELL,SL_A|5|ROTATING,NULL,MIRR,NULL,SL_A|2|ROTATING,NULL,MIRR,MINE,
 			MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,
 			MINE,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,MINE,
 			MIRR,CELL,MIRR,MINE,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,CELL,MIRR,CELL,MIRR,
 		},
 		//41
 		{
 			128*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			LASR|1,NULL,MIRR,NULL|3,MIRR,NULL,WL_A|10,CELL,MIRR,WL_A|5,CELL,WARP|0,CELL,
 			WL_A|15,WL_A|3,NULL|2,WL_A|12,WL_A|10,NULL|2,WL_A|10,WL_A|5,NULL,WL_A|5,CELL,CELL,MINE,
 			WL_A|15,NULL|5,SL_A|2|ROTATING,NULL,WL_A|13,NULL|2,WL_A|15,CELL,PRSM,CELL,
 			WL_A|10,NULL,PRSM,NULL,WL_A|11,NULL,WL_A|3,NULL|4,WL_A|5,CELL,CELL,CELL,
 			WL_A|10,NULL|3,WL_A|5,NULL,WL_A|5,NULL,MIRR,NULL,SL_A|7|ROTATING,WL_A|5,WL_A|12,WL_A|12,WL_A|12,
 			WL_A|10,MINE,NULL|3,CELL,NULL|4,WL_A|1,NULL,CELL,CELL,CELL,
 			CELL,NULL|2,MINE,CELL,NULL,WL_A|5,NULL|3,WL_A|5,NULL,CELL,MIRR|ROTATING,CELL,
 			NULL|2,CELL,NULL|2,MINE,WL_A|7,NULL|3,WL_A|12,WL_A|13,CELL,CELL,CELL,
 			RCVR|1,MINE|EXPLODE,NULL,CELL,CELL,NULL,MIRR,NULL,WARP|0,NULL,MIRR,WL_A|5,WL_A|3,WL_A|3,WL_A|3,
 		},
 		//42
 		{
 			167*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			WL_B|3,CELL,NULL,MIRR,WL_B|4,WL_B|12,CELL,NULL|2,MINE,NULL|2,MIRR,NULL,MINE,
 			WL_B|12,WL_B|13,NULL|3,MIRR,NULL|2,WARP|0,NULL|5,MIRR,
 			RCVR|1,WL_B|5|EXPLODE,NULL|2,MIRR,NULL|5,SL_B,NULL,MIRR,NULL,CELL,
 			WL_B|3,WL_B|7,CELL,WL_A|3,NULL,WL_A|1,WL_A|15,WL_A|10,NULL|6,CELL,
 			WL_B|12,NULL,MINE,WL_A|15,WL_A|12,WL_A|12,NULL|3,CELL,NULL|2,MIRR,WL_B|5,WL_B|15,
 			MIRR,NULL|3,WARP|0,NULL|2,WL_A|10,NULL|5,MINE,NULL,
 			NULL|3,MIRR,NULL|3,WL_A|10,MIRR,NULL,MIRR,NULL|3,MIRR,
 			CELL,MINE,NULL|3,CELL,NULL,WL_A|3,WL_A|3,WL_A|3,WL_A|3,CELL,NULL,WL_B|15,NULL,
 			WL_B|15,CELL,NULL|2,MIRR,MINE,WL_A|3,NULL,WL_A|3,NULL,CELL,NULL,MINE,NULL,LASR|0,
 		},
 		//43
 		{
 			141*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MINE,WL_B|5,CELL,NULL,MIRR,WL_B|5,CELL,WL_A|10,WARP|0,CELL,PRSM,CELL,MINE,WL_A|15,LASR|2,
 			CELL,MINE,MINE,WL_B|15,WL_B|3,NULL,CELL,WL_A|11,WL_A|3,WL_A|7,NULL,WL_A|11,WL_A|3,WL_A|15,NULL,
 			MIRR,NULL|5,PRSM,NULL|3,MIRR,NULL|3,MIRR,
 			CELL,MINE,MINE,WL_B|15,WL_B|12,NULL,CELL,WL_A|15,NULL,WL_A|5,NULL|3,WL_A|15,WL_A|3,
 			WARP|1,WL_B|5,CELL,NULL,MIRR,WL_B|5,CELL,WL_A|15,WL_A|2,WL_A|5,NULL|2,MINE,WL_A|10,NULL,
 			WL_B|3,WL_B|7,WL_B|3,WL_B|7,WL_B|12,WL_B|12,MIRR,CELL,WL_A|10,WL_A|5,NULL|2,MINE,WL_A|10,WARP|1,
 			MINE,CELL,CELL,NULL,WARP|0,MINE,CELL,MINE,WL_A|10,WL_A|5,WL_A|2,PRSM,WL_A|5,WL_A|10,NULL,
 			CELL,CELL,NULL,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|8,NULL,WL_A|10,CELL,CELL,WL_A|13,WL_B|12|EXPLODE,
 			NULL,NULL,PRSM,NULL,WL_A|3,WL_A|15,WL_A|3,NULL,MIRR,NULL,WL_A|10,CELL,MINE,WL_A|5,RCVR|0,
 		},
 		//44
 		{
 			154*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,
 			CELL,CELL,CELL,MINE,CELL,CELL,CELL,NULL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,NULL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,LASR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,MIRR,CELL,MINE,RCVR|2,MINE,CELL,MIRR,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,WL_A|3|EXPLODE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,MINE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			MIRR|ROTATING,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,MIRR|ROTATING,
 		},
 		//45
 		{
 			128*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MIRR,MINE,MIRR|ROTATING,CELL,MIRR,NULL,MIRR,MINE,MIRR|ROTATING,NULL,MIRR,CELL,MIRR,NULL,LASR|2,
 			MINE,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,
 			MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR|ROTATING,NULL,MIRR,NULL,MIRR,
 			MINE,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,NULL,
 			MIRR,NULL,MIRR,NULL,MIRR|ROTATING,NULL,MIRR,MINE|EXPLODE,RCVR|3,MINE,MIRR,CELL,MIRR,NULL,MIRR|ROTATING,
 			CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,CELL,
 			MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR|ROTATING,NULL,MIRR,MINE,MIRR,
 			MINE,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,MIRR,MINE,
 			MIRR,CELL,MIRR|ROTATING,NULL,MIRR,CELL,MIRR,CELL,MIRR|ROTATING,NULL,MIRR,CELL,MIRR|ROTATING,CELL,MIRR,
 		},
 		//46
 		{
 			141*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			LASR|1,NULL|2,MIRR,NULL,WL_A|10,MIRR,NULL,MINE,WL_A|12,WL_A|12,WL_A|13,NULL,MIRR,NULL,
 			NULL|5,WL_A|10,NULL|2,PRSM,CELL,CELL,WL_A|5,NULL|2,WARP|1,
 			NULL,WARP|3,NULL,WARP|1,NULL,WL_A|10,NULL|3,CELL,CELL,WL_A|5,CELL,NULL,WL_A|5,
 			WL_A|11,WL_A|3,WL_A|7,WL_A|12,WL_A|12,WL_A|10,WARP|3,WL_A|5,NULL,CELL,CELL,WL_A|5,NULL,CELL,NULL,
 			RCVR|1,WL_B|15|EXPLODE,PRSM,NULL,CELL,WL_A|10,NULL,WL_A|5,WL_A|10,CELL,CELL,WL_A|14,NULL,WL_A|15,WL_A|15,
 			WL_B|14,WL_B|12,WL_B|13,WL_B|2,NULL,WL_A|13,WL_A|15,WL_A|15,WL_A|10,CELL,CELL,WL_A|10,NULL|2,WL_A|5,
 			CELL,CELL,MINE,WL_B|11,NULL|3,WL_A|5,WL_A|15,WL_A|15,WL_A|15,WL_A|8,NULL,WL_A|10,WL_A|5,
 			MINE,PRSM,CELL,PRSM,NULL,MIRR,NULL|6,CELL,MINE,WL_A|5,
 			CELL,CELL,MINE,WL_B|15,NULL|3,MIRR,CELL,MINE,MIRR,NULL|2,MINE,WL_A|5,
 		},
 		//47
 		{
 			154*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			SL_A|3,MINE,MIRR,NULL,SL_A|4,SL_A|4,NULL|2,CELL,MINE,NULL|2,SL_A|4,NULL,RCVR|2,
 			MINE,NULL|6,SL_A|5|ROTATING,NULL|2,MINE,NULL|2,CELL,SL_A|4|EXPLODE,
 			LASR|1,NULL,MIRR,NULL,SL_A|4,SL_A|4,NULL|5,SL_A|2|ROTATING,NULL|2,MIRR,
 			NULL|7,MIRR,NULL,SL_A|3,NULL|3,SL_A|3|ROTATING,NULL,
 			MINE,SL_A|6,SL_A|6,NULL|3,CELL,NULL|2,SL_A|3,NULL,MIRR,NULL,CELL,NULL,
 			NULL|2,CELL,NULL,SL_A|7,NULL|2,SL_A|2|ROTATING,NULL,CELL,NULL|4,SL_A|2,
 			NULL,SL_A|2|ROTATING,NULL|7,MIRR,NULL,SL_A,SL_A,SL_A,MINE,
 			CELL,NULL,SL_A|2|ROTATING,NULL,MIRR,NULL,SL_A|6,SL_A|6,MINE,NULL|3,CELL,NULL|2,
 			MINE,MIRR,NULL|4,CELL,NULL|4,SL_A|4,NULL,MIRR,CELL,			
 		},
 		//48
 		{
 			154*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			WL_A|14,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|10,MIRR,NULL,CELL,WL_A|15,CELL,MIRR,NULL,WL_A|5,WL_A|15,
 			NULL|2,CELL,NULL|2,WL_A|11,NULL,WL_A|4,WL_A|12,WL_A|13,WL_A|15,NULL,CELL,WL_A|7,LASR|2,
 			CELL,NULL,WL_A|15,NULL|2,WL_A|13,NULL|2,MIRR,NULL|3,WL_A|12,WL_A|13,NULL,
 			WL_A|12,WL_A|15,WL_A|15,NULL,MIRR,NULL,CELL,WL_A|1,WL_A|3,WL_A|7,WL_A|14,NULL,MIRR,WL_A|5,NULL,
 			MIRR,NULL,WL_A|12,WL_A|11,NULL|3,WL_A|4,MINE,WL_A|15,WL_A|8,NULL|2,WL_A|7,NULL,
 			NULL,WL_A|3,NULL,WL_A|15,WL_A|15,NULL,MIRR,NULL|2,CELL,NULL|5,
 			NULL,WL_A|15,NULL,CELL,NULL|2,WL_A|3,NULL|3,WL_A|11,NULL|3,MIRR,
 			NULL,WL_A|13,WL_A|12,WL_A|12,WL_A|12,WL_A|15,WL_A|15,NULL,MIRR,NULL,WL_A|14,NULL,WL_A|13,WL_A|11,WL_A|3,
 			MIRR,NULL|3,CELL,MINE,WL_A|5,WL_A|2,CELL,WL_A|1,WL_A|10,MIRR,NULL,WL_A|10|EXPLODE,RCVR|3,
 		},
 		//49
 		{
 			141*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,
 			CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,WL_B|15,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,
 			CELL,CELL,CELL,CELL,CELL,CELL,MINE,WL_B|15,MINE,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,MINE,WL_B|15,MINE,CELL,CELL,CELL,MIRR,CELL,CELL,
 			MINE,CELL,CELL,CELL,MIRR,NULL,LASR|3,WL_B|15,RCVR|1,MINE|EXPLODE,CELL,CELL,CELL,MIRR,MINE,
 		},
 		//50
 		{
 			116*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,
 			NULL,MIRR,MINE,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR|ROTATING,CELL,MIRR,NULL,
 			MIRR,CELL,SL_B|ROTATING,NULL,MIRR,NULL,MIRR,NULL,MIRR,MINE,MIRR|ROTATING,CELL,MIRR|ROTATING,CELL,MIRR,
 			NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR|ROTATING,NULL,MIRR,NULL,MIRR|ROTATING,NULL,MIRR,NULL,
 			RCVR|2,NULL,MIRR,CELL,MIRR,NULL,MIRR|ROTATING,MINE,MIRR|ROTATING,CELL,MIRR,NULL,MIRR,CELL,LASR,
 			MINE|EXPLODE,MIRR,NULL,MIRR|ROTATING,MINE,MIRR,CELL,MIRR|ROTATING,NULL,MIRR,CELL,MIRR,NULL,MIRR,CELL,
 			MIRR,NULL,MIRR|ROTATING,NULL,MIRR|ROTATING,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,SL_B|5|ROTATING,NULL,MIRR,
 			NULL,MIRR,CELL,MIRR|ROTATING,CELL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,
 			MIRR,MINE,MIRR,MINE,MIRR,CELL,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,MINE,MIRR,
 		},
 		//51
 		{
 			180*OriginalFPS, //energySteps
 			0,	//count of gremlins
 			WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|12,WL_A|8,MIRR,WL_A|1,WL_A|7,CELL,MIRR,MINE,MIRR,CELL,
 			CELL,NULL|7,WL_A|7,MINE,MIRR,CELL,MIRR,CELL,MIRR,
 			MINE,NULL,WL_A|4,WL_A|12,WL_A|12,WL_A|12,WL_A|13,NULL,WL_A|10,MIRR,CELL,MIRR,CELL,MIRR,MINE,
 			WL_A|10,NULL|7,WL_A|10,CELL,MIRR,MINE,MIRR,CELL,MIRR,
 			WL_A|13,CELL,NULL|2,WL_A|14,WL_A|12,WL_A|10,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,CELL,
 			WL_A|4,WL_A|12,WL_A|12,WL_A|12,WL_A|8,NULL|5,MIRR,CELL,MIRR,CELL,MIRR,
 			WL_A|2,MIRR,NULL|3,SL_B|4,WL_B|1,PRSM,WL_B|2,SL_B|4,NULL,MIRR,CELL,MIRR,MINE,
 			WL_A|10,NULL,SL_A|5|ROTATING,CELL,SL_A|1|ROTATING,NULL,WL_B|14,NULL,WL_B|13,NULL,WL_A|1,WL_A|3,WL_A|3,WL_A|3,WL_A|3,
 			CELL,WL_A|3,WL_A|3,WL_A|3,WL_A|3,CELL,WL_B|10,LASR,WL_B|5,MIRR,NULL,CELL,NULL,WL_B|10|EXPLODE,RCVR|3,
 		},
 		//52
 		{
 			116*OriginalFPS, //energySteps
 			8,	//count of gremlins
 			PRSM,CELL,PRSM,NULL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,NULL,PRSM,CELL,PRSM,
 			CELL,NULL,CELL,NULL|9,CELL,NULL,CELL,
 			PRSM,CELL,PRSM,NULL,PRSM,NULL,PRSM,NULL,PRSM,NULL,PRSM,NULL,PRSM,CELL,PRSM,
 			CELL,NULL|13,CELL,
 			LASR|1,NULL|2,MIRR,NULL|7,MIRR,NULL,WL_B|5|EXPLODE,RCVR|3,
 			CELL,NULL|13,CELL,
 			SL_A|2|ROTATING,CELL,SL_A|3|ROTATING,NULL,SL_A|1|ROTATING,NULL,SL_A|5|ROTATING,NULL,SL_A|4|ROTATING,NULL,SL_A|2|ROTATING,NULL,SL_A|5|ROTATING,CELL,SL_A|6|ROTATING,
 			CELL,NULL,CELL,NULL|9,CELL,NULL,CELL,
 			SL_A|7|ROTATING,CELL,SL_A|4|ROTATING,NULL,SL_A|4|ROTATING,CELL,SL_A|6|ROTATING,CELL,SL_A|2|ROTATING,CELL,SL_A|5|ROTATING,NULL,SL_A|ROTATING,CELL,SL_A|3|ROTATING,
 		},
 		//53
 		{
 			154*OriginalFPS, //energySteps
 			4,	//count of gremlins
 			WL_A|10,MINE,CELL,MIRR,NULL|4,SL_B|4|ROTATING,CELL,MINE,WL_A|5,WL_A|15,WL_A|15,WL_A|15,
 			WL_A|15,WL_A|12,WL_A|12,NULL|2,WL_B|12,WL_B|15,WL_A|15,WL_A|3,WL_A|3,WL_A|3,WL_A|7,NULL|2,WL_A|5,
 			NULL|6,WARP|0,NULL|5,MIRR,NULL,WL_A|5,
 			MIRR,NULL,WL_B|15,NULL|3,WL_B|15,WL_A|15,WL_A|15,WL_A|15,WL_A|15,WL_A|15,NULL,WL_A|15,WL_A|5,
 			WL_B|3,NULL,WL_A|3,NULL|4,WL_A|15,WL_A|2,NULL|4,WL_A|15,WL_A|5,
 			WL_B|12,NULL,WL_A|12,WL_A|15,WL_A|15,WL_A|10,MIRR|ROTATING,CELL,WL_A|10,WL_A|5,WL_A|15,WL_A|15,WL_A|15,WL_A|14,WL_A|5,
 			MIRR,NULL,WARP|0,MINE,CELL,WL_A|10,CELL,CELL,WL_A|10,WL_A|5,WL_A|2,MINE,WL_A|13,WL_A|10,MIRR,
 			NULL|2,WL_A|3,WL_A|15,WL_A|15,WL_A|15,WL_A|15,WL_A|12,WL_A|8,NULL,WL_A|10,CELL,CELL,WL_A|13,WL_B|15|EXPLODE,
 			LASR,NULL,WL_A|15,MINE,CELL,CELL,NULL,MIRR,WL_B|15,MIRR,NULL,PRSM,CELL,MINE,RCVR,
 		},
 		//54
 		{
 			154*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			LASR|2,MINE,CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,
 			NULL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE,CELL,CELL,CELL,CELL,
 			MIRR,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,MIRR,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,MIRR,CELL,CELL,CELL,MIRR|ROTATING,CELL,CELL,MIRR,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,WL_A|14,WL_A|12|EXPLODE,WL_A|13,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,MINE,CELL,CELL,CELL,WL_A|10,RCVR,WL_A|5,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,MIRR,CELL,CELL,WL_A|11,WL_A|3,WL_A|7,CELL,MINE,CELL,CELL,MINE,MINE,CELL,
 		},
 		//55
 		{
 			128*OriginalFPS, //energySteps
 			3,	//count of gremlins
 			MIRR,CELL,MIRR|ROTATING,CELL,MIRR|ROTATING,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,
 			MINE,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,CELL,
 			MIRR,CELL,MIRR|ROTATING,NULL,MIRR|ROTATING,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,
 			MINE,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,
 			RCVR|1,MINE|EXPLODE,MIRR|ROTATING,NULL,MIRR|ROTATING,NULL,MIRR,MINE,MIRR,NULL,MIRR,NULL,MIRR,NULL,LASR|3,
 			MINE,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,NULL,MIRR,NULL,
 			MIRR,CELL,MIRR|ROTATING,NULL,MIRR|ROTATING,NULL,MIRR,NULL,MIRR,CELL,MIRR,NULL,MIRR,NULL,MIRR,
 			MINE,MIRR,MINE,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,NULL,MIRR,MINE,MIRR,CELL,
 			MIRR,CELL,MIRR|ROTATING,CELL,MIRR|ROTATING,CELL,MIRR,NULL,MIRR,NULL,MIRR,CELL,MIRR,CELL,MIRR,
 		},
 		//56
 		{
 			180*OriginalFPS, //energySteps
 			0,	//count of gremlins
 			CELL,NULL|5,MIRR,NULL|4,CELL,NULL,WL_B|15|EXPLODE,RCVR|3,
 			NULL,PRSM,NULL|8,MIRR,NULL|4,
 			NULL|3,MIRR,NULL|3,CELL,PRSM,NULL|4,CELL,NULL,
 			NULL,CELL,NULL|10,PRSM,NULL,CELL,
 			LASR|1,NULL|3,PRSM,NULL|5,WL_B|15,CELL,NULL|3,
 			NULL|3,CELL,NULL|3,MIRR,NULL|7,
 			NULL,MIRR,NULL|7,CELL,NULL,PRSM,NULL|2,MIRR,
 			NULL|3,CELL,NULL|2,PRSM,NULL|6,CELL,NULL,
 			CELL,NULL,PRSM,NULL|5,MIRR,NULL,CELL,NULL|3,PRSM,
 		},
 		//57
 		{
 			167*OriginalFPS, //energySteps
 			2,	//count of gremlins
 			MIRR,MIRR,MIRR,NULL,WARP|3,NULL|2,WL_A|12,WL_A|12,WL_A|12,NULL|2,MIRR,NULL,LASR|3,
 			MIRR,MIRR,MIRR,NULL,CELL,WL_A|5,NULL,MIRR,MIRR,MIRR,NULL,MINE,WL_A|3,WL_A|3,WL_A|3,
 			NULL|4,MINE,WL_A|4,WL_A|10,MIRR,MIRR,MIRR,WL_A|5,NULL,CELL,NULL,WARP|1,
 			WL_A|12,WL_A|12,WL_A|13,NULL,CELL,NULL,WL_A|11,WL_A|3,WL_A|3,WL_A|3,WL_A|7,WL_A|3,WL_A|3,WL_A|3,WL_A|3,
 			MIRR,MIRR,WL_A|5,CELL,MIRR,NULL|2,MINE|EXPLODE,RCVR|3,WL_A|15,NULL|2,CELL,NULL,MINE,
 			MIRR,MIRR,WL_A|5,WL_A|3,WL_A|3,WL_A|3,WL_A|3,WL_A|14,WL_A|12,WL_A|13,CELL,NULL|4,
 			NULL|2,CELL,MINE,NULL|4,WARP|0,WL_A|5,NULL,MINE,NULL,MIRR,MIRR,
 			WARP|3,NULL|2,WARP|1,NULL|2,MIRR,MIRR,NULL,WL_A|14,CELL,NULL|2,MIRR,MIRR,
 			CELL,WL_A|5,CELL,NULL,MIRR,NULL,MIRR,MIRR,NULL,WL_A|10,WARP|0,CELL,NULL,MIRR,MIRR,
 		},
 		//58
 		{
 			180*OriginalFPS, //energySteps
 			4,	//count of gremlins
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,LASR|2,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,NULL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,NULL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,MIRR|ROTATING,CELL,MIRR|ROTATING,CELL,MIRR|ROTATING,MINE,NULL,MINE,MIRR|ROTATING,CELL,MIRR|ROTATING,CELL,MIRR|ROTATING,CELL,
 			MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,MIRR|ROTATING,
 			MIRR|ROTATING,NULL,MIRR|ROTATING,NULL,MIRR|ROTATING,NULL|5,MIRR|ROTATING,NULL,MIRR|ROTATING,NULL,MIRR|ROTATING,
 			NULL|6,WL_B|1,WL_B|3|EXPLODE,WL_B|2,NULL|6,
 			WL_B|12,WL_B|13,NULL|4,WL_B|5,RCVR,WL_B|10,NULL|4,WL_B|14,WL_B|12,
 			SL_B|2,WL_B|5,WL_B|3,MIRR,WL_B|3,MIRR,WL_B|7,WL_B|12,WL_B|11,MIRR,WL_B|3,MIRR,WL_B|3,WL_B|10,SL_B|6,
 		},
 		//59
 		{
 			167*OriginalFPS, //energySteps
 			4,	//count of gremlins
 			WL_B|7,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,WL_B|11,
 			WL_B|15,CELL,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,CELL,WL_B|15,
 			SL_B|2,WL_B|15,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,WL_B|15,SL_B|6,
 			NULL,WL_B|7,MIRR,MIRR,MIRR,MIRR,MINE,MIRR,MINE,MIRR,MIRR,MIRR,MIRR,WL_B|10,NULL,
 			MIRR,NULL,MIRR,MIRR,MINE,MIRR|ROTATING,MIRR,CELL,MIRR,MIRR|ROTATING,MINE,MIRR,MIRR,NULL,MIRR,
 			NULL,WL_B|7,MIRR,MIRR,MIRR,MIRR,MINE,MIRR,MINE,MIRR,MIRR,MIRR,MIRR,WL_B|10,MINE|EXPLODE,
 			LASR,WL_B|15,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,WL_B|15,RCVR,
 			WL_B|15,CELL,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,MIRR,CELL,WL_B|15,
 			WL_B|13,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,CELL,WL_B|15,WL_B|14,
 		},
 		//60
 		{
 			116*OriginalFPS, //energySteps
 			8,	//count of gremlins
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,RCVR|2,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,MINE|EXPLODE,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,
 			CELL,CELL,CELL,CELL,CELL,CELL,NULL|3,CELL,CELL,CELL,CELL,CELL,CELL,
 			CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,LASR,CELL,PRSM,CELL,PRSM,CELL,PRSM,CELL,
 			CELL,CELL,CELL,CELL,WL_B|1,WL_B|3,WL_B|7,WL_B|15,WL_B|11,WL_B|3,WL_B|2,CELL,CELL,CELL,CELL,
 		},
 		
 		
 	};
 
 	
 
 	
 	
 	//  
 	int angleNodeSteps[][]={
 		{0,-2}, //0
 		{1,-2}, //1
 		{2,-2}, //2
 		{2,-1}, //3
 		{2,0},  //4
 		{2,1}, //5
 		{2,2}, //6
 		{1,2}, //7
 		{0,2}, //8
 		{-1,2}, //9
 		{-2,2}, //10
 		{-2,1}, //11
 		{-2,0}, //12
 		{-2,-1},//13
 		{-2,-2},//14
 		{-1,-2},//15
 	};
 	
 	
 	public void initGame() {
 		
 		winStateId=WINSTATE_GAMING;
 		beamState = BEAMSTATE_CHARGING;
 		gameStateId = GAMESTATE_ACCUMULATING_ENERGY;
 		energy=0;
 		overheat=0;
 		
 		topScore=500;
 		countScore=0;
 		countKilledGremlins=0;
 		countBurnedCells=0;
 		countEnergy=0;
 		
 		killedGremlins=0;
 		burnedCells=0;
 		
 		cursorEnabled = false;		
 		
 		field=new int[field_width*field_height];
 		
 		unpackLevel(app.playingLevel);
 	}
 	
 	void unpackLevel(int levelNumber) {
 		int piece;
 		int fieldIndex=0;
 		levelNumber--;
 		if (levelNumber>=packedLevels.length) return;
 
 		overheatReflectionStep = overheatSteps/64;
 		overheatBombStep = overheatSteps/16;
 		
 		energySteps = packedLevels[levelNumber][0];
 		if (!app.difficultyClassic) {
 			energySteps*=2;
 			overheatReflectionStep/=2;
 			overheatBombStep/=overheatReflectionStep;
 		}
 		
 		for (int i=2;i<packedLevels[levelNumber].length;i++) {
 			if (fieldIndex>=field_width*field_height) break;
 			piece = packedLevels[levelNumber][i];
 			if ((piece&0xFF00)==NULL) {
 				piece&=0xFF;
 				if (piece<1) piece=1;
 				for (int n=0;n<piece;n++) {
 					field[fieldIndex++]=NULL;
 					if (fieldIndex>=field_width*field_height) break;
 				}
 			} else {
 				if ((piece&0xf00)==MIRR) piece=piece&0xFFFFFF00 | ((int)(Math.random()*0x1f));
 				field[fieldIndex++] = piece;
 			}
 		}
 		while (fieldIndex<field_width*field_height)
 			field[fieldIndex++]=NULL;
 		
 		countOfGremlins = packedLevels[levelNumber][1];
 		if (countOfGremlins>0)
 			for (int i=0;i<countOfGremlins;i++) {
 				grm[i].init();
 			};
 		
 	}
 	
 	void disableCursor() {
 		cursorEnabled=false;
 	}
 	
 	boolean setCursor(int x, int y) {
 		int newCursorX=(x-app.winX)/(app.sprSize*2)/app.sprScale;
 		int newCursorY=(y-app.winY)/(app.sprSize*2)/app.sprScale;
 		boolean changedCoordinates = false;
 		
 		if (newCursorX>=0 && newCursorX<field_width && newCursorY>=0 && newCursorY<field_height) {
 			int f=field[newCursorX+newCursorY*field_width];
 			if ((f&0xF00)==MIRR) {
 				cursorEnabled=true;
 				cursorPhase=0;
 				if ((cursorX!=newCursorX) || (cursorY!=newCursorY)) changedCoordinates=true;
 				cursorX=newCursorX;
 				cursorY=newCursorY;
 			};
 		};
 		return changedCoordinates;
 	}
 	
 	void animateField() {
 		int f=0;
 		boolean needToExplodeBarrier=true;
 		boolean barrierFound = false;
 
 		if (++cursorPhase>=cursorPhases) cursorPhase=0;
 		if (touched) cursorPhase=cursorPhases/2-1;
 		
 		switch (gameStateId) {
 		case GAMESTATE_ACCUMULATING_ENERGY:
 			if (energy==0) //app.laserFillInSound.loop();
 				app.playSound(Deflektor.SND_LASERFILLIN_LOOP);
 			energy+=energySteps/40;
 			
 			//rotate mirrors randomly
 			for (int i=0;i<field.length;i++) {
 				if ((field[i]&0xf00)==MIRR) {
 					int angle=(int)(Math.random()*0x8)-1;
 					if (angle>1) angle=0;
 					field[i]=(field[i]+(angle&0x1f))&0xFFFFFF1F;
 				} else if ((field[i]&ROTATING)!=0) {
 					field[i]=(field[i]+1)&0xFFFFFF1F;
 				}
 			}
 			
 			if (energy>=energySteps-1) {
 				energy=energySteps-1;
 				app.playSound(Deflektor.SND_LASERREADY);
 				gameStateId=GAMESTATE_GAMING;
 			}
 
 			break;
 		case GAMESTATE_GAMEOVER_NOENERGY:
 		case GAMESTATE_GAMEOVER_OVERHEAT:
 			if (flash>32) initGame();
 			break;
 		case GAMESTATE_GAMING:
 			if (app.cheat) energy++;
 			energy--;
 			if (energy<=0) {
 				gameStateId=GAMESTATE_GAMEOVER_NOENERGY;
 				app.stopContinuousSound();
 				flash=0;
 				break;
 			}
 			
 			if (beamState==BEAMSTATE_OVERHEAT) overheat+=overheatReflectionStep;
 			else if (beamState==BEAMSTATE_BOMB) overheat+=overheatBombStep;
 			else overheat-=overheatReflectionStep;
 
 			if (overheat <=0) overheat =0;
 			if (overheat>=overheatSteps) {
 				if (app.cheat) overheat=0;
 				else {
 					gameStateId=GAMESTATE_GAMEOVER_OVERHEAT;
 					app.stopContinuousSound();
 					flash=0;
 					break;
 				}
 			}
 			
 			if (beamState==BEAMSTATE_CONNECTED) {
 				app.unlockLevel(app.playingLevel+1);
 				gameStateId = GAMESTATE_LEVELCOMPLETED;
 				app.playSound(Deflektor.SND_LEVELCOMPLETED);
 			} else {
 				for (int i=0;i<field_width;i++) 
 					for (int j=0;j<field_height;j++) {
 						f=field[j*field_width+i];
 						if ((f&ROTATING)!=0) {
 							if (!(((f&0xf00)==MIRR) && (i==cursorX) && (j==cursorY) && touched))
 								rotateThing(i,j);
 						};
 						if ((f&EXPLODE)!=0) barrierFound = true;
 						if ((f&0xf00)==_EXPL) {
 							f++;
 							if ((f&0xf)>4) f=NULL;
 							else needToExplodeBarrier = false;
 							field[j*field_width+i]=f;
 						};
 						if ((f&0xF00)==CELL) needToExplodeBarrier = false;
 						if ((f&0xF00)==PRSM) field[j*field_width+i]=(f&0xF00)|((int)((8*Math.random())+0.5));
 					};
 				if (needToExplodeBarrier && barrierFound) {
 					app.playSound(Deflektor.SND_EXITOPEN);
 					for (int i=0;i<field_width*field_height;i++)
 						if ((field[i]&EXPLODE)!=0) field[i]=_EXPL;
 				};
 			};
 				
 			if (countOfGremlins>0)
 				for (int i=0;i<countOfGremlins;i++) {
 					if (grm[i].animate()) app.playSound(Deflektor.SND_GREMLINAPPEAR);
 				}
 			if (touched && cursorEnabled && cursorX>=0 && cursorY>=0)
 				killGremlins((cursorX*2+1)*app.sprSize, (cursorY*2+1)*app.sprSize);
 			
 			if (winStateId==WINSTATE_GAMING) {
 				switch (beamState) {
 				case BEAMSTATE_NORMAL:
 				case BEAMSTATE_CONNECTED:
 					app.stopContinuousSound();
 					break;
 				case BEAMSTATE_OVERHEAT:
 					app.playSound(Deflektor.SND_LASEROVERHEAT_LOOP);
 					break;
 				case BEAMSTATE_BOMB:
 					app.playSound(Deflektor.SND_LASERBOMB_LOOP);
 					break;
 				};
 			};
 			
 			break;
 		case GAMESTATE_LEVELCOMPLETED:
 			break;
 		};
 		
 	}
 	
 	void rotateThing(int x, int y) {
 		rotateThing(x,y,1);
 	}
 	
 	void rotateThing(int x, int y, int angle) {
 		int f=field[y*field_width+x];
 		field[y*field_width+x]=(f+angle)&0xFFFFFF1F;
 	}
 	
 	void rotateMirror(int x, int y, int angle) {
 		if (x>=field_width || y>=field_height) return;
 		if ((field[y*field_width+x]&0x0F00)==MIRR)
 			rotateThing(x,y,angle);
 	}
 	
 	void drawGameInfo () {
 		/*
 		int nrg = (energy *64) /energySteps;
 		int ovh = (overheat * 64) / overheatSteps;
 		if (nrg>63) nrg=63;
 		if (nrg<0) nrg=0;
 		if (ovh>63) ovh=63;
 		if (ovh<0) ovh=0;
 		app.spr_putRegion( 28, field_height*16, 64, 8, 0, 8+144);
 		if (nrg>0) app.spr_putRegion( 28, field_height*16+8, nrg, 8, 0, 0+144);
 		if (nrg<63) app.spr_putRegion( 28+nrg, field_height*16+8, 64-nrg, 8, 64+nrg, 0+144);
 		app.spr_putRegion( 100, field_height*16, 64, 8, 64, 8+144);
 		if (ovh>0) app.spr_putRegion( 100, field_height*16+8, ovh, 8, 0, 0+144);
 		if (ovh<63) app.spr_putRegion( 100+ovh, field_height*16+8, 64-ovh, 8, 64+ovh, 0+144);
 		app.spr_putRegion( 172, field_height*16, 48, 16, 0, 16+144);
 		
 		
 		//level
 		app.showBigNumber(6, field_height*16+4, app.playingLevel);
 		*/
 		
 		if (!(gameStateId==GAMESTATE_GAMING && winStateId==WINSTATE_GAMING && (energy*8/energySteps)<1 && (flash&2)==0)) 
 			app.spr_putRegion( 0, field_height*16, 64-8, 8, 0, 8+144);
 		drawStripe(56, field_height*16, 240-56-16,energy*(240-56-16)/energySteps,true);
 		if (!(gameStateId==GAMESTATE_GAMING && winStateId==WINSTATE_GAMING && (overheat*4/overheatSteps)>=3 && (flash&1)==0))
 			app.spr_putRegion( 0, field_height*16+8, 64-8, 8, 64, 8+144);
 		drawStripe(56, field_height*16+8, 240-56-16,overheat*(240-56-16)/overheatSteps,false);
 		
 		
 		//pause button
 		app.spr_putRegion( (field_width-1)*16, field_height*16, 16, 16, 48, 16+144);
 	}
 	
 	void drawStripe(int x, int y, int width, int filledWidth, boolean emptyIsBad) {
 		int sx_filled=16;
 		int sx_empty=80;
 		int color=filledWidth*4/width;
 		if (!emptyIsBad) color=3-color;
 		switch (color) {
 		case -1:
 		case 0:
 			sx_filled=8; sx_empty=72;
 			break;
 		case 1:
 		case 2:
 			sx_filled=48; sx_empty=112;
 			break;
 		case 3:
 		case 4:
 			sx_filled=16; sx_empty=80;
 			break;
 		}
 		while (filledWidth>=8) {
 			app.spr_putRegion( x, y, 8, 8, sx_filled, 144);
 			filledWidth-=8;
 			width-=8;
 			x+=8;
 		};
 		if (filledWidth>0) {
 			app.spr_putRegion( x, y, filledWidth, 8, sx_filled, 144);
 			app.spr_putRegion( x+filledWidth, y, 8-filledWidth, 8, sx_empty+filledWidth, 144);
 			width-=8;
 			x+=8;
 		};
 		while (width>=8) {
 			app.spr_putRegion( x, y, 8, 8, sx_empty, 144);
 			width-=8;
 			x+=8;
 		};
 		
 		
 	};
 	
 	void drawField() {
 		int f_angle;
 		int beam_x=0;
 		int beam_y=0;
 		int beam_angle=0;
 
 		
 		if (app.appGfxId == Deflektor.APPGFX_ZX) {
 		//zx-drawing
 		//clear field by null-sprite and search beam;
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				int f=field[j*field_width+i];
 				f_angle = f&0x1f;
 				if ((f&0xf00)==LASR) {
 					beam_x=i*4+2+angleNodeSteps[f_angle*4][0];
 					beam_y=j*4+2+angleNodeSteps[f_angle*4][1];
 					beam_angle=(f_angle&3)*4;					
 				};
 				//clear field by null-sprite only in zx-version
 				if (((f&0xf00)==NULL) || ((f&0xf00)==MINE) || ((f&0xf00)==CELL) || ((f&0xf00)==_EXPL) || ((f&0xf00)==WL_A) || ((f&0xf00)==WL_B) ) {
 					app.spr_putRegion( i*16, j*16, 16, 16, 7*16, 6*16);
 				};
 			};
 		
 		switch (gameStateId) {
 		case GAMESTATE_LEVELCOMPLETED:
 		case GAMESTATE_GAMING:
 			drawBeam(beam_x,beam_y,beam_angle);
 			break;
 		};
 
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				f_angle=field[j*field_width+i]&0x1f;
 				switch (field[j*field_width+i]&0xf00) {
 				case NULL:
 					break;
 				case LASR:
 					app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&3)*16), 4*16);
 					break;
 				case RCVR:
 					app.spr_putRegion( i*16, j*16, 16, 16, (((f_angle&3)+4)*16), 4*16);
 					break;
 				case MIRR:
 					app.spr_putRegion( i*16, j*16, 16, 16, (f_angle&7)*16, ((f_angle>>3)&1)*16);
 					break;
 				case WARP:
 					app.spr_putRegion( i*16, j*16, 16, 16, (((f_angle&3)+2)*16), 5*16);
 					break;
 				case CELL:
 					app.spr_putRegion( i*16, j*16, 16, 16, 0, 5*16);
 					break;
 				case MINE:
 					app.spr_putRegion( i*16, j*16, 16, 16, 16, 5*16);
 					break;
 				case WL_A:
 					putWallA(i,j,f_angle);
 					break;
 				case WL_B:
 					putWallB(i,j,f_angle);
 					break;
 				case PRSM:
 					app.spr_putRegion( i*16, j*16, 16, 16, 6*16, 5*16);
 					break;
 				case SL_A:
 					app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&7)*16), 3*16);
 					break;
 				case SL_B:
 					app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&7)*16), 2*16);
 					break;
 				case _EXPL:
 					app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&7)*16), 6*16);
 					break;
 				}
 			};
 			
 		} else {
 			//amiga drawing
 			//
 			//search beam and draw some sprites;
 			for (int i=0;i<field_width;i++) 
 				for (int j=0;j<field_height;j++) {
 					int f=field[j*field_width+i];
 					f_angle = f&0x1f;
 					switch (f&0xf00) {
 					case LASR:
 						beam_x=i*4+2+angleNodeSteps[f_angle*4][0];
 						beam_y=j*4+2+angleNodeSteps[f_angle*4][1];
 						beam_angle=(f_angle&3)*4;
 						break;
 					case MIRR:
 						app.spr_putRegion( i*16, j*16, 16, 16, (f_angle&7)*16, ((f_angle>>3)&1)*16);
 						break;
 					case WARP:
 						app.spr_putRegion( i*16, j*16, 16, 16, (((f_angle&3)+2)*16), 5*16);
 						break;
 					case CELL:
 						app.spr_putRegion( i*16, j*16, 16, 16, 0, 5*16);
 						break;
 					case MINE:
 						app.spr_putRegion( i*16, j*16, 16, 16, 16, 5*16);
 						break;
 					case SL_A:
 						app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&7)*16), 3*16);
 						break;
 					case SL_B:
 						app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&7)*16), 2*16);
 						break;
 					}
 					
 				};
 			
 			switch (gameStateId) {
 			case GAMESTATE_LEVELCOMPLETED:
 			case GAMESTATE_GAMING:
 				drawBeam(beam_x,beam_y,beam_angle);
 				break;
 			};
 
 			for (int i=0;i<field_width;i++) 
 				for (int j=0;j<field_height;j++) {
 					f_angle=field[j*field_width+i]&0x1f;
 					switch (field[j*field_width+i]&0xf00) {
 					case NULL:
 						break;
 					case LASR:
 						app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&3)*16), 4*16);
 						break;
 					case RCVR:
 						app.spr_putRegion( i*16, j*16, 16, 16, (((f_angle&3)+4)*16), 4*16);
 						break;
 					case WL_A:
 						putWallA(i,j,f_angle);
 						break;
 					case WL_B:
 						putWallB(i,j,f_angle);
 						break;
 					case PRSM:
 						app.spr_putRegion( i*16, j*16, 16, 16, 6*16, 5*16);
 						break;
 					case _EXPL:
 						app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&7)*16), 6*16);
 						break;
 					}
 				};
 
 		}
 			
 		if (countOfGremlins>0)
 			for (int i=0;i<countOfGremlins;i++) grm[i].draw();
 		
 		if (cursorEnabled && cursorPhase<cursorDisplayPhases && gameStateId!=GAMESTATE_GAMEOVER_NOENERGY && gameStateId!=GAMESTATE_GAMEOVER_OVERHEAT) {
 			app.spr_putRegion( cursorX*16, cursorY*16, 16, 16, 6*16, 6*16);
 		};
 	};
 	
 	void putWallA(int x, int y, int type) {
 		if ((type&8)!=0)	app.spr_putRegion( x*16, y*16, 8, 8, 7*16+8, 5*16);
 		if ((type&4)!=0)	app.spr_putRegion( x*16+8, y*16, 8, 8, 7*16+8, 5*16);
 		if ((type&2)!=0)	app.spr_putRegion( x*16, y*16+8, 8, 8, 7*16+8, 5*16);
 		if ((type&1)!=0)	app.spr_putRegion( x*16+8, y*16+8, 8, 8, 7*16+8, 5*16);
 	}
 	
 	void putWallB(int x, int y, int type) {
 		if ((type&8)!=0)	app.spr_putRegion( x*16, y*16, 8, 8, 7*16, 5*16);
 		if ((type&4)!=0)	app.spr_putRegion( x*16+8, y*16, 8, 8, 7*16, 5*16);
 		if ((type&2)!=0)	app.spr_putRegion( x*16, y*16+8, 8, 8, 7*16, 5*16);
 		if ((type&1)!=0)	app.spr_putRegion( x*16+8, y*16+8, 8, 8, 7*16, 5*16);
 	}
 	
 	final int wall_a_angle_matrix[]={
 			//x,y;u&3==0 true=1, false=0;walls	u
 			//        
 		-1,	//0,0,0,0000	  
 		2,	//0,0,0,0001	2
 		6,	//0,0,0,0010	6
 		4,	//0,0,0,0011	4
 		6,	//0,0,0,0100	6
 		0,	//0,0,0,0101	0
 		2,	//0,0,0,0110	2
 		2,	//0,0,0,0111	2
 		2,	//0,0,0,1000	2
 		6,	//0,0,0,1001	6
 		0,	//0,0,0,1010	0
 		6,	//0,0,0,1011	6
 		4,	//0,0,0,1100	4
 		6,	//0,0,0,1101	6
 		2,	//0,0,0,1110	2
 		-1,	//0,0,0,1111	  (   )
 		
 			//       
 		-1,	//0,0,1,0000	  
 		-1,	//0,0,1,0001	  
 		-1,	//0,0,1,0010	 
 		4,	//0,0,1,0011	4
 		-1,	//0,0,1,0100	  
 		0,	//0,0,1,0101	0
 		-2,	//0,0,1,0110	 
 		-2,	//0,0,1,0111	 
 		-1,	//0,0,1,1000	  
 		-2,	//0,0,1,1001	 
 		0,	//0,0,1,1010	0
 		-2,	//0,0,1,1011	6
 		4,	//0,0,1,1100	4
 		-2,	//0,0,1,1101	6
 		-2,	//0,0,1,1110	2
 		-1	//0,0,1,1111	  (   )					
 	};
 	
 	void drawBeam(int beamX, int beamY, int beamAngle) {
 		
 		int newBeamX;
 		int newBeamY;
 		int oldBeamAngle;
 		int endBeam=MAXIMUM_REFLECTIONS;
 		
 		beamState = BEAMSTATE_NORMAL;
 		
 		while ((endBeam--)>0) {
 
 			newBeamX = beamX+angleNodeSteps[beamAngle][0];
 			newBeamY = beamY+angleNodeSteps[beamAngle][1];
 			oldBeamAngle = beamAngle;
 			if (newBeamX>field_width*4 || newBeamX<0 || newBeamY>field_height*4 || newBeamY<0) {
 				endBeam=0;
 				continue;
 			};
 			
 			drawSpriteLine(beamX, beamY, newBeamX, newBeamY);
 
 			if (newBeamX>=field_width*4 || newBeamX<0 || newBeamY>=field_height*4 || newBeamY<0) {
 				endBeam=0;
 				continue;
 			};
 			
 			beamX = newBeamX;
 			beamY = newBeamY;
 			
 			int sx=beamX&3;
 			int sy=beamY&3;
 			int fx=beamX/4;
 			int fy=beamY/4;
 			int f=field[fx+fy*field_width];
 			int f_angle=f&0x1f;
 			
 			
 			//      
 			if ((sx==2) && (sy==2)) {
 				switch (f&0x0f00) {
 				case LASR:
 					endBeam=0;
 					continue;
 				case RCVR:
 					//if right angle
 					if ((f_angle*4)==((beamAngle+8)&15)) beamState = BEAMSTATE_CONNECTED;
 					endBeam=0;
 					break;
 				case MIRR:
 					beamAngle =(((f_angle<<1)-beamAngle-beamAngle)>>1)&0xf;
 					break;
 				case WARP:
 					for (int i=0;i<field.length;i++) {
 						if ( ((field[i]&0xFFF)==(f&0xfff)) && (i!=(fx+fy*field_width))) {
 							beamY=(i/field_width)*4+2;
 							beamX=(i-(((int)(beamY/4))*field_width))*4+2;
 							break;
 						};
 					};
 					break;
 				case CELL:
 					field[fx+fy*field_width]=_EXPL;
 					app.playSound(Deflektor.SND_BURNCELL);
 					burnedCells++;
 					endBeam=0;
 					continue;
 				case MINE:
 					beamState = BEAMSTATE_BOMB;
 					endBeam=0;
 					continue;
 				case PRSM:
 					beamAngle= (beamAngle-4+(f&0xFF))&0xf;
 					break;
 				case _EXPL:
 					if (f_angle>2) break;
 					endBeam=0;
 					continue;
 				}
 			}
 
 			//      (, ).
 			int mp_beam_x = (beamX+beamX+angleNodeSteps[beamAngle][0])/2;
 			int mp_beam_y = (beamY+beamY+angleNodeSteps[beamAngle][1])/2; 	
 			
 			
 			int f1=field[(mp_beam_x/4)+(mp_beam_y/4)*field_width];
 			switch (f1&0x0F00) {
 			case WL_A:
 				int wall_angle=-1;
 				int wallsAround=f;
 				int wallUP=0;
 				int wallUPLEFT=0;
 				int wallLEFT=0;
 				
 				//     , -, .
 				if (fx>0) wallLEFT=field[fx-1+fy*field_width];
 				if ((fx>0) && (fy>0)) wallUPLEFT=field[fx-1+(fy-1)*field_width];
 				if (fy>0) wallUP=field[fx+(fy-1)*field_width];
 				
 				if ((wallsAround&0xF00)!=	WL_A) wallsAround=0;	else wallsAround&=0xf;
 				if ((wallLEFT&0xF00)!=		WL_A) wallLEFT=0;		else wallLEFT&=0xf;
 				if ((wallUPLEFT&0xF00)!=	WL_A) wallUPLEFT=0;	else wallUPLEFT&=0xf;
 				if ((wallUP&0xF00)!=		WL_A)	wallUP=0;		else wallUP&=0xf;
 				
 				
 				if ((beamY&2)==0) {
 					// :
 					wallsAround = (wallsAround>>2) | ((wallUP<<2)&0xC);
 					wallUP = wallUP>>2;
 					wallLEFT = (wallLEFT>>2) | ((wallUPLEFT<<2)&0xC);
 					wallUPLEFT = wallUPLEFT>>2;
 				};
 				
 				if ((beamX&2)==0) {
 					// :
 					wallsAround = ((wallsAround >>1) &5 ) | ((wallLEFT &5) <<1);
 				};
 				
 				if (((beamX&1)+(beamY&1))==0) {
 					wall_angle=wall_a_angle_matrix[( ((beamAngle&3)==0)?16:0 )|(wallsAround&0xF)];
 					//      
 					if ((beamAngle&3)!=0 && wallsAround!=6 && wallsAround!=9) {
 						// ,           .
 						if ((beamAngle<4) && ((wallsAround&4)==0)) wall_angle=-1;
 						else if ((beamAngle>4) && (beamAngle<8) && ((wallsAround&1)==0)) wall_angle=-1;
 						else if ((beamAngle>8) && (beamAngle<12) && ((wallsAround&2)==0)) wall_angle=-1;
 						else if ((beamAngle>12) && (wallsAround&8)==0) wall_angle=-1;
 					}
 				} else {
 
 				if ((beamX&1)==1) switch (wallsAround&0x5) {
 					case 1: case 4: wall_angle=4; break;
 					case 5: endBeam=0; 
 					};
 					
 				if ((beamY&1)==1) switch (wallsAround&0x3) {
 					case 1: case 2: wall_angle=0; break;
 					case 3: endBeam=0; break;
 					};
 					
 				};
 				
 				if (wall_angle>=0) beamAngle=(wall_angle*2-beamAngle)&0xf;
 				else if (wall_angle==-1) break;
 				else if (wall_angle==-2) { beamAngle=(beamAngle+8)&0xf; break; };
 				
 				//continue;
 				break;
 			case WL_B:
 				int crd=((mp_beam_x>>1)&1)+((mp_beam_y)&2);
 				if ((crd==0) && ((f1&8)!=0)) {	endBeam=0;	continue; };
 				if ((crd==1) && ((f1&4)!=0)) {	endBeam=0;	continue; };
 				if ((crd==2) && ((f1&2)!=0)) {	endBeam=0;	continue; };
 				if ((crd==3) && ((f1&1)!=0)) {	endBeam=0;	continue; };
 				break;
 			case SL_A:
 				if ((f1&7)!=(beamAngle&7)) {
 					if ((beamX&3)==0 && (((beamY&3)!=0) || !getMirrorableWall(beamX-(mp_beam_x-beamX), mp_beam_y,beamAngle))) beamAngle=(0-beamAngle)&0xf;
 					if ((beamY&3)==0 && (((beamX&3)!=0) || !getMirrorableWall(mp_beam_x, beamY-(mp_beam_y-beamY),beamAngle))) beamAngle=(4*2-beamAngle)&0xf;
 					break;
 				};
 				break;
 			case SL_B:
 				if ((f1&7)!=(beamAngle&7)) endBeam=0;
 				break;
 			}
 			
 			//      -  
 			if (((beamX&3)==0) && ((beamY&3)==0)) {
 				if (getReduceWall(beamX,beamY,beamAngle) && getReduceWall(beamX-1,beamY-1,beamAngle)) endBeam=0;
 				if (getReduceWall(beamX-1,beamY,beamAngle) && getReduceWall(beamX,beamY-1,beamAngle)) endBeam=0;
 			}
 			
 			
 			//     
 			if (Math.abs(beamAngle-oldBeamAngle)==8) {
 				endBeam=0;
 				beamState = BEAMSTATE_OVERHEAT;
 			}
 			
 		}
 		
 		
 	};
 	
 	int getField(int x, int y) {
 		if (x<0 || x>=field_width || y<0 || y>=field_height) return NULL;
 		return field[x+y*field_width];
 	};
 	
 	boolean getMirrorableWall(int x, int y, int angle) {
 		int f=getField(x/4,y/4);
 		switch (f&0xf00) { 
 		case SL_A:
 			if ((angle&7)!=(f&7)) return true;
 			break;
 		case WL_A:
 			int crd=((x>>1)&1)+(y&2);
 			if ((crd==0) && ((f&8)!=0)) return true;
 			if ((crd==1) && ((f&4)!=0)) return true;
 			if ((crd==2) && ((f&2)!=0)) return true;
 			if ((crd==3) && ((f&1)!=0)) return true;
 			break;
 		};
 		return false;
 	};
 	
 	boolean getReduceWall(int x, int y, int angle) {
 		int f=getField(x/4,y/4);
 		switch (f&0xf00) { 
 		case SL_B:
 			if ((angle&7)!=(f&7)) return true;
 			break;
 		case WL_B:
 			int crd=((x>>1)&1)+(y&2);
 			if ((crd==0) && ((f&8)!=0)) return true;
 			if ((crd==1) && ((f&4)!=0)) return true;
 			if ((crd==2) && ((f&2)!=0)) return true;
 			if ((crd==3) && ((f&1)!=0)) return true;
 			break;
 		};
 		return false;
 	}
 	
 	void drawSpriteLine(int x0, int y0, int x1, int y1) {
 		int lx0, lx1, ly0, ly1;
 		if (x0<x1) { lx0=x0; lx1=x1; ly0=y0; ly1=y1;  }
 		else { lx0=x1; lx1=x0; ly0=y1; ly1=y0;  };
 		int sx=lx1-lx0;
 		int sy=ly1-ly0;
 		if (sx==0 && sy==-2)					app.spr_putRegionSafe( lx0*4-4, ly0*4-8, 8, 8, 0, 112);
 		if (sx==1 && sy==-2 && ((lx0&1)==0))	app.spr_putRegionSafe( lx0*4-4, ly0*4-8, 16, 8, 8, 120);
 		if (sx==1 && sy==-2 && ((lx0&1)==1))	app.spr_putRegionSafe( lx0*4-4, ly0*4-8, 16, 8, 12, 112);
 		if (sx==2 && sy==-2)					app.spr_putRegionSafe( lx0*4-4, ly0*4-12, 16, 16, 24, 112);
 		if (sx==2 && sy==-1 && ((ly0&1)==0))	app.spr_putRegionSafe( lx0*4,   ly0*4-4, 16, 8, 40, 120);
 		if (sx==2 && sy==-1 && ((ly0&1)==1))	app.spr_putRegionSafe( lx0*4-8, ly0*4-8, 16, 8, 40, 112);
 		if (sx==2 && sy==0)						app.spr_putRegionSafe( lx0*4,   ly0*4-4, 8, 8, 56, 112);
 		if (sx==2 && sy==1 && ((ly0&1)==0))		app.spr_putRegionSafe( lx0*4,   ly0*4-4, 16, 8, 64, 112);
 		if (sx==2 && sy==1 && ((ly0&1)==1)) 	app.spr_putRegionSafe( lx0*4-8, ly0*4, 16, 8, 64, 120);
 		if (sx==2 && sy==2)						app.spr_putRegionSafe( lx0*4-4, ly0*4-4, 16, 16, 80, 112);
 		if (sx==1 && sy==2 && ((lx0&1)==0))		app.spr_putRegionSafe( lx0*4-4, ly0*4, 16, 8, 96, 112);
 		if (sx==1 && sy==2 && ((lx0&1)==1))		app.spr_putRegionSafe( lx0*4-4, ly0*4, 16, 8, 100, 120);
 		if (sx==0 && sy==2)						app.spr_putRegionSafe( lx0*4-4, ly0*4, 8, 8, 0, 112);
 		
 		
 	}
 	
 	void touchField(int x, int y) {
 		if (x<0 || x>= field_width || y< 0 || y>=field_height) return;
 		int f=field[y*field_width+x];
 		if ((f&0xFF00)==MIRR) {
 			rotateThing(x,y);
 		};
 	};
 		
 	void killGremlins(int x, int y) {
 		if (countOfGremlins>0)
 			for (int i=0;i<countOfGremlins;i++)
 				if (grm[i].attemptToKill(x, y)) {
 					killedGremlins++;
 					app.playSound(Deflektor.SND_GREMLINDEAD);
 				};
 	};
 	
 	class Gremlin {
 		int x;
 		int y;
 		int phase;
 		int delay;
 		
 		void init() {
 			do {
 				x=((int)(Math.random()*(field_width-1)))*2;
 				y=((int)(Math.random()*(field_height-1)))*2;
 			} while (!checkFreeSpace(x,y));
 			phase=0;
 			delay=(int)(Math.random()*50)+30;
 		};
 		
 		void draw() {
 			if (delay==0)
 				switch (app.appGfxId) {
 				case Deflektor.APPGFX_ZX:
 					app.spr_putRegion(x*8, y*8, 16, 16, 0+(phase&3)*16, 128);
 					break;
 				case Deflektor.APPGFX_AMIGA:
 					if (phase<=3) app.spr_putRegion(x*8, y*8, 16, 16, 0+(phase&3)*16, 128);
 					else app.spr_putRegion(x*8, y*8, 16, 16, 0+(7-phase)*16, 128);
 					break;
 				};
 				
 		};
 		
 		boolean animate() {
 			if (delay>0) {
 				delay--;
 				if (delay==0) return true;
 			}
 			else {
 				int sx=(int)(Math.random()*4);
 				int sy=(int)(Math.random()*4);
 				if ((sx==0) && (x>0) && checkFreeSpace(x-1,y)) x--;
 				if ((sx==3) && (x<(field_width-1)*2) && checkFreeSpace(x+1,y)) x++;
 				if ((sy==0) && (y>0) && checkFreeSpace(x,y-1)) y--;
 				if ((sy==3) && (y<(field_height-1)*2) && checkFreeSpace(x,y+1)) y++;
 
 				if (((x&1)+(y&1))==0) {
 					rotateMirror(x/2,y/2,((int)(Math.random()*3)-1)&0x1f); 
 				}
 			};
 			phase=(phase+1)&0x7;
 			return false;
 		};
 		
 		boolean checkFreeSpace(int cx,int cy) {
 			
 			return checkSmallField(cx,cy) && checkSmallField(cx+1,cy) && checkSmallField(cx,cy+1) && checkSmallField(cx+1,cy+1);
 			
 		};
 		
 		boolean checkSmallField(int dx, int dy) {
 			int f;
 			f=field[(dx/2)+((dy/2)*field_width)];
 			switch (f&0x0F00) {
 			case LASR:
 			case RCVR:
 			case WARP:
 			case PRSM:
 			case SL_A:
 			case SL_B:
 				return false;
 			case WL_A:
 			case WL_B:
 				int i = (8>>(dx&1))>>((dy+dy)&2);
 				if ((f&i)==0) return true;
 				else return false;
 			}
 			return true;
 		}
 		
 		boolean attemptToKill(int tapx, int tapy) {
 			if ((delay==0) && (tapx>=x*app.sprSize) && (tapx<(x+2)*app.sprSize) && (tapy>=y*app.sprSize) && (tapy<(y+2)*app.sprSize) ) {
 				init();
 				delay*=6;
 				return true;
 			};
 			return false;
 		}
 		
 	}
 	
 	
 	
 }
