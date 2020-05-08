 package org.ssg.Cambridge;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import net.java.games.input.Controller;
 
 import org.ini4j.Ini;
 import org.ini4j.InvalidFileFormatException;
 import org.newdawn.slick.AngelCodeFont;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import paulscode.sound.SoundSystem;
 import paulscode.sound.SoundSystemConfig;
 
 public class MenuOptionsState extends BasicGameState implements KeyListener{
 	private GlobalData data;
 	private String[] menuOptions;
 	private Ini ini;
 	private int stateID;
 	SoundSystem mySoundSystem;
 	Ini.Section display, sound, gameplay;
 	
 	private int selected;
 	private boolean focus;
 	private final int menuHeight = 30;
 	private int tempHeight, tempWidth;
 
 	CambridgeController[] controllers;
 	
 	private final float deadzone = 0.28f;
 	private boolean down, up, left, right, back, enter;
 	private boolean selectFlag, backFlag, leftFlag, rightFlag, upFlag, downFlag;
 	private int inputDelay;
 	private final int inputDelayConst = 200;
 	
 	private boolean shouldRender;
 	
 	final static String RESDIR = "res/";
 	private AngelCodeFont font, font_white, font_small;
 	
 	private float cursorY, cursorYTarget;
 	
 	private Cambridge cambridge;
 	private AppGameContainer appGc;
 	
 	//Constructor
 	public MenuOptionsState(int i, boolean renderon) {
 		stateID = i;
 		shouldRender = renderon;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		
 		data = ((Cambridge) sbg).getData();
 		mySoundSystem = data.mySoundSystem();
 		display = data.displayConfig();
 		sound = data.soundConfig();
 		gameplay = data.gameplayConfig();
 		
 		controllers = data.getC();
 		
 		up = false;
 		down = false;
 		left = false;
 		right = false;
 		enter = false;
 		back = false;
 		inputDelay = 0;
 		resetButtons();
 		
 		tempWidth = display.get("SCREENWIDTH", int.class);
 		tempHeight = display.get("SCREENHEIGHT", int.class);
 		
 		font = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0.png"));
 		font_white = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0_white.png"));
 		font_small = new AngelCodeFont(RESDIR + "8bitoperator_small.fnt", new Image(RESDIR + "8bitoperator_small_0.png"));
 		
 		menuOptions = new String[] {
 			"Resolution",
 			"Fullscreen",
 			"Sound - Master",
 			"Sound - Effects",
 			"Sound - Ambience",
 			"Display Player ID"
 		};
 		
 		selected = 0;
 		
 		cambridge = (Cambridge) sbg;
 		appGc = (AppGameContainer) gc;
 	}
 	
 	public void resetButtons() {
 		selectFlag = true;
 		backFlag = true;
 		leftFlag = true;
 		rightFlag = true;
 		upFlag = true;
 		downFlag = true;
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		if (!shouldRender)
 			return;
 		
 		g.setAntiAlias(true);
 		
 		g.setBackground(Color.black);
 //		g.fillRect(0, 0, gc.getScreenWidth(), gc.getScreenHeight());
 		
 		g.setColor(Color.white);
 		g.setFont(font_white);
 		g.drawString("Options", data.screenWidth()/6, data.screenHeight()*0.1f);
 		
 		g.setFont(font_small);
 		
 		g.setLineWidth(2);
 		
 		for (int i = 0; i < menuOptions.length; i++) {
 			g.drawString(menuOptions[i], data.screenWidth()/6, data.screenHeight()*0.4f+menuHeight*i);
 			switch(i) {
 				case 0:
 					g.drawString(tempWidth+"x"+tempHeight, data.screenWidth()/5*3, data.screenHeight()*0.4f+menuHeight*i);
 					break;
 				case 1:
 					g.drawString((data.fullscreen() ? "On" : "Off"), data.screenWidth()/5*3, data.screenHeight()*0.4f+menuHeight*i);
 					break;
 				case 2:
 				case 3:
 				case 4:
 					int volume = 0;
 					if (i == 2) {
 						volume = data.masterSound();
 					} else if (i == 3) {
 						volume = data.effectSound();
 					} else if (i == 4) {
 						volume = data.ambientSound();
 					}
 					for (int j = 0; j < 10; j++) {
 						if (j < volume) {
 							g.fillRect(data.screenWidth()/5*3 + menuHeight/2*j, data.screenHeight()*0.4f+menuHeight*i+2, menuHeight/3, menuHeight/5*4);
 						} else {
 							g.drawRect(data.screenWidth()/5*3 + menuHeight/2*j, data.screenHeight()*0.4f+menuHeight*i+2, menuHeight/3, menuHeight/5*4);
 						}
 					}
 					break;
 				case 5:
					g.drawString((data.playerIdDisplay() ? "On" : "Off"), data.screenWidth()/5*3, data.screenHeight()*0.4f+menuHeight*i);
 					break;
 				default:
 					break;
 			}
 		}
 		
 		if (focus) {
 //			g.drawRect(data.screenWidth()/6 - 12, data.screenHeight()*0.4f+selected*menuHeight-2, font_small.getWidth(menuOptions[selected]) + 24, menuHeight+4);
 			g.setLineWidth(5);
 		}else{
 			g.setLineWidth(2);
 		}
 		
 //		g.drawRect(data.screenWidth()/6 - 10, data.screenHeight()*0.4f+selected*menuHeight, font_small.getWidth(menuOptions[selected]) + 20, menuHeight);
 		g.drawRect(-10, cursorY, data.screenWidth()+20, menuHeight);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		
 		cursorYTarget = data.screenHeight()*0.4f+selected*menuHeight;
 		cursorY = approachTarget(cursorY, cursorYTarget, delta/2f);
 		
 		//System.out.println(data.getResolution()[0] + "x" + data.getResolution()[1] + " " + data.resolutionIndex());
 		Input input = gc.getInput();
 		
 		up = false;
 		down = false;
 		left = false;
 		right = false;
 		back = false;
 		enter = false;
 		for (CambridgeController c: controllers) {
 			if (c.exists() && c.poll()) {
 				
 				// Analog stick checking
 				if (inputDelay <= 0) {
 					if (c.getLeftStickY() > deadzone) {
 						down = true;
 					} else if (c.getLeftStickY() < -deadzone) {
 						up = true;
 					}
 					if (c.getLeftStickX() > deadzone) {
 						right = true;
 					} else if (c.getLeftStickX() < -deadzone) {
 						left = true;
 					}
 					if (up || down || left || right) {
 						inputDelay = inputDelayConst;
 					}
 				} else {
 					inputDelay-=delta;
 				}
 				
 				// D-Pad input checking
 				if (downFlag) {
 					if (c.getDPad() != data.DPAD_DOWN) {
 						downFlag = false;
 					}
 				} else {
 					if (c.getDPad() == data.DPAD_DOWN) {
 						downFlag = true;
 						down = true;
 					}
 				}
 				if (upFlag) {
 					if (c.getDPad() != data.DPAD_UP) {
 						upFlag = false;
 					}
 				} else {
 					if (c.getDPad() == data.DPAD_UP) {
 						upFlag = true;
 						up = true;
 					}
 				}
 				if (leftFlag) {
 					if (c.getDPad() != data.DPAD_LEFT) {
 						leftFlag = false;
 					}
 				} else {
 					if (c.getDPad() == data.DPAD_LEFT) {
 						leftFlag = true;
 						left = true;
 					}
 				}
 				if (rightFlag) {
 					if (c.getDPad() != data.DPAD_RIGHT) {
 						rightFlag = false;
 					}
 				} else {
 					if (c.getDPad() == data.DPAD_RIGHT) {
 						rightFlag = true;
 						right = true;
 					}
 				}
 				
 				// A and B button checking
 				if (backFlag) {
 					if (!c.getMenuBack()) {
 						backFlag = false;
 					}
 				} else {
 					if (c.getMenuBack()) {
 						backFlag = true;
 						back = true;
 					}
 				}
 				if (selectFlag) {
 					if (!c.getMenuSelect()) {
 						selectFlag = false;
 					}
 				} else {
 					if (c.getMenuSelect()) {
 						selectFlag = true;
 						enter = true;
 					}
 				}
 			}
 		}
 		
 		if (input.isKeyPressed(Input.KEY_UP) || up) {
 			if (!focus) {
 				//selected = --selected % menuOptions.length;
 				if(selected > 0) selected--;
 				if (selected == -1) {
 					selected = menuOptions.length-1;
 				}
 				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			}
 		} else if (input.isKeyPressed(Input.KEY_DOWN) || down) {
 			if (!focus) {
 				//selected = ++selected % menuOptions.length;
 				if(selected<menuOptions.length-1) selected++;
 				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			}
 			mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 		} else if (input.isKeyPressed(Input.KEY_LEFT) || left) {
 			if (focus) {
 				switch(selected) {
 					case 0:
 						data.setResolutionIndex(data.resolutionIndex() < 1 ? data.resolutions()-1 : data.resolutionIndex()-1);
 						tempWidth = data.getResolution()[0];
 						tempHeight = data.getResolution()[1];
 						break;
 					case 1:
 						data.setFullscreen(!data.fullscreen());
 						break;
 					case 2:
 						data.setMasterSound(data.masterSound() - 1 < 0 ? 0 : data.masterSound() - 1);
 						mySoundSystem.setMasterVolume(data.masterSound() / 10f);
 						break;
 					case 3:
 						data.setEffectSound(data.effectSound() - 1 < 0 ? 0 : data.effectSound() - 1);
 						break;
 					case 4:
 						data.setAmbientSound(data.ambientSound() - 1 < 0 ? 0 : data.ambientSound() - 1);
 						mySoundSystem.setVolume("BGM", data.ambientSound()/10f);
 						break;
 					case 5:
 						data.setPlayerIdDisplay(!data.playerIdDisplay());
 						break;
 					default:
 						break;
 				}
 				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			}
 		} else if (input.isKeyPressed(Input.KEY_RIGHT) || right) {
 			if (focus) {
 				switch(selected) {
 					case 0:
 						data.setResolutionIndex(data.resolutionIndex() >= data.resolutions()-1 ? 0 : data.resolutionIndex()+1);
 						tempWidth = data.getResolution()[0];
 						tempHeight = data.getResolution()[1];
 						break;
 					case 1:
 						data.setFullscreen(!data.fullscreen());
 						break;
 					case 2:
 						data.setMasterSound(data.masterSound() + 1 > 10 ? 10 : data.masterSound() + 1);
 						mySoundSystem.setMasterVolume(data.masterSound() / 10f);
 						break;
 					case 3:
 						data.setEffectSound(data.effectSound() + 1 > 10 ? 10 : data.effectSound() + 1);
 						break;
 					case 4:
 						data.setAmbientSound(data.ambientSound() + 1 > 10 ? 10 : data.ambientSound() + 1);
 						mySoundSystem.setVolume("BGM", data.ambientSound()/10f);
 						break;
 					case 5:
 						data.setPlayerIdDisplay(!data.playerIdDisplay());
 						break;
 					default:
 						break;
 				}
 				mySoundSystem.quickPlay( true, "MenuShift.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			}
 		} else if(input.isKeyPressed(Input.KEY_ESCAPE) || back) {
 			if (focus) {
 				focus = false;
 				switch(selected) {
 					case 0:
 						if (!data.fullscreen()) {
 							data.setScreenWidth(display.get("SCREENWIDTH", int.class));
 							data.setScreenHeight(display.get("SCREENHEIGHT", int.class));
 						}
 						tempWidth = display.get("SCREENWIDTH", int.class);
 						tempHeight = display.get("SCREENHEIGHT", int.class);
						boolean resolutionFlag = false;
						for (int i = 0; i < data.resolutions(); i++) {
							if (!resolutionFlag && tempWidth == data.getResolution(i)[0] && tempHeight == data.getResolution(i)[1]) {
								data.setResolutionIndex(i);
								resolutionFlag = true;
							}
						}
						if (!resolutionFlag) {
							data.setResolutionIndex(-1);
						}
 						break;
 					case 1:
 						data.setFullscreen(display.get("FULLSCREEN", boolean.class));
 						break;
 					case 2:
 						data.setMasterSound(sound.get("MASTER", int.class));
 						mySoundSystem.setMasterVolume(data.masterSound() / 10f);
 						break;
 					case 3:
 						data.setEffectSound(sound.get("EFFECTS", int.class));
 						break;
 					case 4:
 						data.setAmbientSound(sound.get("AMBIENT", int.class));
 						break;
 					case 5:
 						data.setPlayerIdDisplay(gameplay.get("PLAYERID", boolean.class));
 						break;
 					default:
 						break;
 				}
 				mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			} else {
 				mySoundSystem.quickPlay( true, "MenuBack.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 				((MenuMainState)sbg.getState(data.MENUMAINSTATE)).setShouldRender(true);
 				setShouldRender(false);
 				sbg.enterState(data.MENUMAINSTATE);
 			}
 		} else if (input.isKeyPressed(Input.KEY_ENTER) || enter) {
 			if (focus) {
 				focus = false;
 				switch(selected) {
 					case 0:
 						tempWidth = data.getResolution()[0];
 						tempHeight = data.getResolution()[1];
 						display.put("SCREENWIDTH", tempWidth);
 						display.put("SCREENHEIGHT", tempHeight);
 						if (!data.fullscreen()) {
 							data.setScreenWidth(tempWidth);
 							data.setScreenHeight(tempHeight);
 							appGc.setDisplayMode(data.screenWidth(), data.screenHeight(), data.fullscreen());
 						}
 						break;
 					case 1:
 						display.put("FULLSCREEN", data.fullscreen());
 						if (!data.fullscreen()) {
 							data.setScreenHeight(display.get("SCREENHEIGHT", int.class));
 							data.setScreenWidth(display.get("SCREENWIDTH", int.class));
 						} else {
 							data.setScreenHeight(gc.getScreenHeight());
 							data.setScreenWidth(gc.getScreenWidth());
 						}
 						appGc.setDisplayMode(data.screenWidth(), data.screenHeight(), data.fullscreen());
 						break;
 					case 2:
 						sound.put("MASTER", data.masterSound());
 						break;
 					case 3:
 						sound.put("EFFECTS", data.effectSound());
 						break;
 					case 4:
 						sound.put("AMBIENT", data.ambientSound());
 						break;
 					case 5:
 						gameplay.put("PLAYERID", data.playerIdDisplay());
 						break;
 					default:
 						break;
 				}
 				try {
 					data.userConfig().store();
 				} catch (IOException e) {
 					System.out.println("Failed to save config!");
 					e.printStackTrace();
 				}
 			} else {
 				focus = true;
 				tempHeight = display.get("SCREENHEIGHT", int.class);
 				tempWidth = display.get("SCREENWIDTH", int.class);
 			}
 			mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 		}
 		
 		input.clearKeyPressedRecord();
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		resetButtons();
 		cursorYTarget = data.screenHeight()*0.4f;
 		cursorY = cursorYTarget;
 	}
 
 	@Override
 	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		
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
 	
 	public void setShouldRender(boolean shouldRender) {
 		this.shouldRender = shouldRender;
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return stateID;
 	}
 
 }
