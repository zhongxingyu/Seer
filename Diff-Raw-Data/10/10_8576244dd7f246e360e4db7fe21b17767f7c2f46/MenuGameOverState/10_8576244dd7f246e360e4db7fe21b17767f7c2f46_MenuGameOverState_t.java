 package org.ssg.Cambridge;
 import org.newdawn.slick.AngelCodeFont;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import paulscode.sound.SoundSystem;
 import paulscode.sound.SoundSystemConfig;
 
 public class MenuGameOverState extends BasicGameState {
 	
 	private GlobalData data;
 	private SoundSystem mySoundSystem;
 	
 	CambridgePlayerAnchor[] anchors;
 	
 	private int stateID;
 	private boolean shouldRender;
 	
 	private Image bgImg;
 	
 	private boolean down, up, left, right, back, enter, start;
 //	private int inputDelay;
 //	private final int inputDelayConst = 200;
 
 	private int selected;
 	private float cursorY, cursorYTarget;
 	
 	Font font, font_white, font_small;
 	private String[] menuOptions;
 	
 	public MenuGameOverState(int n, boolean renderon){
 		stateID = n;
 		shouldRender = renderon;
 	}
 	
 	public void setShouldRender(boolean renderon){
 		shouldRender = renderon;
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		data = ((Cambridge) sbg).getData();
 		mySoundSystem = data.mySoundSystem();
 		
 		up = false;
 		down = false;
 		left = false;
 		right = false;
 		enter = false;
 		back = false;
 		start = false;
 //		inputDelay = 0;
 		
 		cursorY = 0;
 		cursorYTarget = 0;
 		
 		font = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0.png"));
 		font_white = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0_white.png"));
 		font_small = new AngelCodeFont(data.RESDIR + "8bitoperator_small.fnt", new Image(data.RESDIR + "8bitoperator_small_0.png"));
 		
 		menuOptions = new String[] {
 				"Rematch",
 				"Change Characters",
 				"Exit",
 		};
 		
 		anchors = data.playerAnchors();
 		
 		reset();
 	}
 
 	public void reset() {
 		selected = 0;
 		cursorYTarget = data.screenHeight()*.3f + (font.getLineHeight()*1.2f)*(float)selected;
 		cursorY = cursorYTarget;
 //		cursorY = data.screenHeight()/3f-font_small.getLineHeight()/2f-5;
 //		cursorYTarget = cursorY;
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		if(!shouldRender)
 			return;
 		
 		g.setLineWidth(2);
 		g.drawImage(bgImg, 0,0, Color.darkGray);
 		
 		g.setColor(Color.black);
 		g.fillRect(-10, cursorY-10, data.screenWidth()+20, font.getLineHeight()+20);
 
 		g.setColor(Color.white);
 		g.drawRect(-10, cursorY-10, data.screenWidth()+20, font.getLineHeight()+20);
 
 		g.setFont(font_white);
 		
 		for (int i = 0; i < menuOptions.length; i++) {
 			g.drawString(
 					menuOptions[i],
 					data.screenWidth() / 2f - font.getWidth(menuOptions[1]) / 2f,
 					data.screenHeight()*.3f + (font.getLineHeight()*1.2f)*(float)i
 					);
 		}
 	
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 
 		Input input = gc.getInput();

 		cursorYTarget = data.screenHeight()*.3f + (font.getLineHeight()*1.2f)*(float)selected;
 		cursorY = approachTarget(cursorY, cursorYTarget, delta);
 		
 		up = false;
 		down = false;
 		left = false;
 		right = false;
 		back = false;
 		enter = false;
 		start = false;
 		for (CambridgePlayerAnchor a : anchors) {
 			if (a.initiated()) {
 				if (a.down(gc, delta)) {
 					down = true;
 				} else if (a.up(gc, delta)) {
 					up = true;
 				} else if (a.left(gc, delta)) {
 					left = true;
 				} else if (a.right(gc, delta)) {
 					right = true;
 				} else if (a.select(gc, delta)) {
 					enter = true;
 				} else if (a.back(gc, delta)) {
 					back = true;
 				} else if (a.start(gc, delta)) {
 					start = true;
 				}
 			}
 		}
 
 		if (up) {
 			if (selected > 0) {
 				selected--;
 				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			}
 		} else if (down) {
 			if (selected < 2) {
 				selected++;
 				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			}
 		} else if (enter) {
 			mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			switch(selected){
 			case 0://rematch
 				setShouldRender(false);
 				((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).setShouldRender(true);
 				((GameplayState)sbg.getState(data.GAMEPLAYSTATE)).resetPositions();
 				sbg.enterState(data.GAMEPLAYSTATE);
 				break;
 			case 1://change characters
 				setShouldRender(false);
 				((MenuPlayerSetupState)sbg.getState(data.MENUPLAYERSETUPSTATE)).setShouldRender(true);
 				sbg.enterState(data.MENUPLAYERSETUPSTATE);
 				break;
 			case 2://exit
 				setShouldRender(false);
 				((MenuMainState)sbg.getState(data.MENUMAINSTATE)).setShouldRender(true);
 				sbg.enterState(data.MENUMAINSTATE);
 				break;
 			default:
 			
 			}
 		} else if(start || back){
 			mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			setShouldRender(false);
 			((GameOverState)sbg.getState(data.GAMEOVERSTATE)).setShouldRender(true);
 			sbg.enterState(data.GAMEOVERSTATE);
 		}
 		
 		input.clearKeyPressedRecord();
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg){
 		reset();
 	}
 	
 	public void setImage(Image i){
 		bgImg = i;
 	}
 	
 	public float approachTarget(float val, float target, float inc){
 
 		if(val<target){
 			val+=inc;
 			if(val>target)
 				val=target;
 		}
 		if(val>target){
 			val-=inc;
 			if(val<target)
 				val=target;
 		}
 
 		return val;
 	}
 	
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
