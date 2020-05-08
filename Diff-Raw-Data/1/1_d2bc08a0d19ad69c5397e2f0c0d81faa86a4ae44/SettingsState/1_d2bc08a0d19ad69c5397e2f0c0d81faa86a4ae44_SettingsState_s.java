 package pro.oneredpixel.deflektorclassic;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class SettingsState extends State {
 	
 	Button bBack;
 	
 	Button bZX;
 	Button bAmiga;
 	//Button bModern;
 	Button bResetProgress;
 	Button bUnlockLevels;
 	Button bCheat;
 	Button bExitGame;
 	
 	Button bMinusSensitivity;
 	Button bPlusSensitivity;
 	
 	Button bDifficultyEasy;
 	Button bDifficultyClassic;
 	
 	SettingsState(Deflektor defl) {
 		super(defl);
 	
 		bBack = new Button(8,160-8-24, 96,160);
 		bBack.box=false;
 		
 		bZX = new Button(16+8*6+8+8+8*8, 32,0,0,false,"ZX");
 		bAmiga = new Button(16+16+8*2+8*6+8+8+8*8, 32,0,0,false,"AMIGA");
 		
 		bDifficultyEasy = new Button(16+12*6+8+8+8, 64,0,0,false,"EASY");
 		bDifficultyClassic = new Button(16+4*8+8+12*6+8+16+8, 64,0,0,false,"CLASSIC");
 		
 		bMinusSensitivity = new Button(8+8*12+7*8,96, 64,160);
 		bPlusSensitivity = new Button(8+24+24+8*12+7*8,96, 80,160);
 		
 		bResetProgress = new Button(16+24+8, 160-24-8,0,0,false,"RESET PROGRESS");
 		bUnlockLevels = new  Button(16+24+8, 160-24-8,0,0,false,"UNLOCK  LEVELS");
 		bCheat = new Button(16+8+8+15*8+16+8,160-24-8,0,0,false,"CHEAT");
 		bExitGame = new  Button(240-12*8, 160-24-8,0,0,false,"QUIT GAME");
 		
 	}
 	
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		int touchx=(int)(x-app.winX)/app.sprScale;
 		int touchy=(int)(y-app.winY)/app.sprScale;
 		if (bBack.checkRegion(touchx,touchy)) {
 			bBack.touched=true;
 			app.playSound(Deflektor.SND_TAP);
 		};
 		if (bZX.checkRegion(touchx,touchy)&& app.appGfxId!=Deflektor.APPGFX_ZX) {
 			bZX.touched=true;
 			app.playSound(Deflektor.SND_TAP);
 		};
 		if (bAmiga.checkRegion(touchx,touchy)&& app.appGfxId!=Deflektor.APPGFX_AMIGA) {
 			bAmiga.touched=true;
 			app.playSound(Deflektor.SND_TAP);
 		};
 		
 		if (bDifficultyEasy.checkRegion(touchx,touchy) && app.difficultyClassic) {
 			bDifficultyEasy.touched=true;
 			app.playSound(Deflektor.SND_TAP);
 		};
 		if (bDifficultyClassic.checkRegion(touchx,touchy) && !app.difficultyClassic) {
 			bDifficultyClassic.touched=true;
 			app.playSound(Deflektor.SND_TAP);
 		};
 		if (bMinusSensitivity.checkRegion(touchx,touchy) && app.controlsSensitivity>1) {
 			bMinusSensitivity.touched=true;
 			app.playSound(Deflektor.SND_TAP);
 		};
 		if (bPlusSensitivity.checkRegion(touchx,touchy) && app.controlsSensitivity<8) {
 			bPlusSensitivity.touched=true;
 			app.playSound(Deflektor.SND_TAP);
 		};
 		if (bResetProgress.checkRegion(touchx,touchy)) {
 			bResetProgress.touched=true;
 			app.playSound(Deflektor.SND_TAP);
 		};
 		if (app.showingCheatControls) {
 			if (bUnlockLevels.checkRegion(touchx,touchy)) {
 				bUnlockLevels.touched=true;
 				app.playSound(Deflektor.SND_TAP);
 			};
 			if (bCheat.checkRegion(touchx,touchy)) {
 				bCheat.touched=true;
 				app.playSound(Deflektor.SND_TAP);
 			};
 		} else {
 			if (bExitGame.checkRegion(touchx,touchy)) {
 				bExitGame.touched=true;
 				app.playSound(Deflektor.SND_TAP);
 			};
 		}
 		return false;
 	}
 	
 	public boolean touchUp (int x, int y, int pointer, int button) {
 		untouchButtons();
 		return false;
 	};
 	
 	void untouchButtons() {
 		if (bBack.touched || bZX.touched || bAmiga.touched ||
 				bDifficultyEasy.touched || bDifficultyClassic.touched || bMinusSensitivity.touched || bPlusSensitivity.touched ||
 				bResetProgress.touched || bUnlockLevels.touched || bCheat.touched)  app.playSound(Deflektor.SND_UNTAP);
 		bBack.touched=false;
 		bZX.touched=false;
 		bAmiga.touched=false;
 		bDifficultyEasy.touched=false;
 		bDifficultyClassic.touched=false;
 		bMinusSensitivity.touched=false;
 		bPlusSensitivity.touched=false;
 		bResetProgress.touched=false;
 		bUnlockLevels.touched=false;
 		bCheat.touched=false;
 		bExitGame.touched=false;
 	};
 	
 	public boolean tap(float x, float y, int tapCount, int button) {
 		int tapx=(int)(x-app.winX)/app.sprScale;
 		int tapy=(int)(y-app.winY)/app.sprScale;
 		
 		if (bBack.checkRegion(tapx,  tapy)) {
 			app.gotoAppState(Deflektor.APPSTATE_MENU);
 			bBack.touched = false;
 		}
 		
 		if (bZX.checkRegion(tapx,  tapy) && app.appGfxId!=Deflektor.APPGFX_ZX) {
 			app.appGfxId=Deflektor.APPGFX_ZX;
 			app.loadMedia();
 			app.playMelody();
 			bZX.touched=false;
 		};
 		if (bAmiga.checkRegion(tapx,  tapy) && app.appGfxId!=Deflektor.APPGFX_AMIGA) {
 			app.appGfxId=Deflektor.APPGFX_AMIGA;
 			app.loadMedia();
 			app.playMelody();
 			bAmiga.touched = false;
 		};
 		
 		if (bMinusSensitivity.checkRegion(tapx, tapy) && app.controlsSensitivity>1) {
 			app.controlsSensitivity--;
 			app.initInput();
 			bMinusSensitivity.touched=false;
 		}
 		if (bPlusSensitivity.checkRegion(tapx, tapy) && app.controlsSensitivity<8) {
 			app.controlsSensitivity++;
 			app.initInput();
 			bPlusSensitivity.touched=false;
 		}
 
 		
 		if (app.difficultyClassic && bDifficultyEasy.checkRegion(tapx, tapy)) {
 			app.difficultyClassic=false;
 			bDifficultyEasy.touched = false;
 		};
 		if (!app.difficultyClassic && bDifficultyClassic.checkRegion(tapx, tapy)) {
 			app.difficultyClassic=true;
 			bDifficultyClassic.touched = false;
 		};
 		
 		if (app.showingCheatControls) {
 			if (!app.cheat && bCheat.checkRegion(tapx,  tapy)) {
 				app.cheat=true;
 				app.gotoAppState(Deflektor.APPSTATE_MENU);
 				bCheat.touched=false;
 			};
 			
 			if (app.unlockedLevel==60) {
 				if (bResetProgress.checkRegion(tapx,  tapy)) {
 					app.unlockedLevel=1;
 					app.gotoAppState(Deflektor.APPSTATE_MENU);
 					bResetProgress.touched=false;
 				}
 			} else {
 				if (bUnlockLevels.checkRegion(tapx,  tapy)) {
 					app.unlockedLevel=60;
 					app.gotoAppState(Deflektor.APPSTATE_MENU);
 					bUnlockLevels.touched=false;
 				}
 			}
 		} else {
 			if (bExitGame.checkRegion(tapx,  tapy)) {
				app.gotoAppState(Deflektor.APPSTATE_MENU);
 				bExitGame.touched=false;
 				Gdx.app.exit();
 			};
 		}
 
 		
 		return false;
 	}
 	
 	public boolean keyUp(int k) {
 		if (k==Keys.BACK || k==Keys.MENU) {
 			app.gotoAppState(Deflektor.APPSTATE_MENU);
 			app.playSound(Deflektor.SND_TAP);
 			return true;
 		};
 		return false;
 	}
 
 	public void render(SpriteBatch batch) {
 		batch.setProjectionMatrix(app.camera.combined);
 		batch.begin();
 		app.showString((240-8*8)/2, 8, "SETTINGS");
 		
 		app.drawButton(bBack);
 
 		app.showString(8, 40, "DESIGN");
 		if (app.appGfxId!=Deflektor.APPGFX_ZX) app.drawButton(bZX); else app.drawButton(bZX, 24, 176); 
 		if (app.appGfxId!=Deflektor.APPGFX_AMIGA) app.drawButton(bAmiga); else app.drawButton(bAmiga, 24, 176);
 		
 		app.showString(8, 72, "DIFFICULTY");
 		if (app.difficultyClassic)  app.drawButton(bDifficultyEasy);    else app.drawButton(bDifficultyEasy, 24, 176);
 		if (!app.difficultyClassic) app.drawButton(bDifficultyClassic); else app.drawButton(bDifficultyClassic, 24, 176);
 
 		app.showString(8, 104, "SENSITIVITY");
 		if (app.controlsSensitivity>1) app.drawButton(bMinusSensitivity); else app.drawButton(bMinusSensitivity, 24, 176); 
 		app.showString(8+24+8+8*12+7*8,96+8, String.format("%d", app.controlsSensitivity));
 		if (app.controlsSensitivity<8) app.drawButton(bPlusSensitivity); else app.drawButton(bPlusSensitivity, 24, 176);
 		
 		if (app.showingCheatControls) {
 			if (app.unlockedLevel==60) app.drawButton(bResetProgress);
 			else app.drawButton(bUnlockLevels);
 			if (!app.cheat) app.drawButton(bCheat);
 		} else app.drawButton(bExitGame);
 		
 		batch.end();
 	};
 }
