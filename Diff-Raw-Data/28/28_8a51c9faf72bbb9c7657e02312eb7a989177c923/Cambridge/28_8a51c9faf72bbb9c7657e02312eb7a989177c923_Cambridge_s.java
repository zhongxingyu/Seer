 package org.ssg.Cambridge;
 
 import java.io.File;
 import java.io.IOException;
 
 import net.java.games.input.*;
 
 import org.ini4j.Ini;
 import org.ini4j.InvalidFileFormatException;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.openal.SoundStore;
 import org.newdawn.slick.state.StateBasedGame;
 
 import paulscode.sound.SoundSystem;
 import paulscode.sound.SoundSystemConfig;
 import paulscode.sound.SoundSystemException;
 import paulscode.sound.codecs.CodecJOrbis;
 import paulscode.sound.codecs.CodecWav;
 import paulscode.sound.libraries.LibraryLWJGLOpenAL;
 
 public class Cambridge extends StateBasedGame {
 
 	SoundSystem mySoundSystem;
 	
 	private GlobalData data;
 
 	public Cambridge() throws SlickException{
 		super("Kick Kickerung");
 		
 		data = new GlobalData();
 
 		try {
 			SoundSystemConfig.addLibrary( LibraryLWJGLOpenAL.class );
 			SoundSystemConfig.setCodec( "wav", CodecWav.class );
 			SoundSystemConfig.setCodec( "ogg", CodecJOrbis.class );
 			SoundSystemConfig.setSoundFilesPackage( "org/ssg/Cambridge/Sounds/" );
 			SoundSystemConfig.setStreamQueueFormatsMatch( true );
 		} catch (SoundSystemException e) {
 			e.printStackTrace();
 		} 
 
 		mySoundSystem = new SoundSystem();
 		data.setMySoundSystem(mySoundSystem);
 
 		this.addState(new GameplayState(data.GAMEPLAYSTATE, false, 0));
 		this.addState(new OptionsMenuState(data.OPTIONSMENUSTATE, false));
 		this.addState(new PlayerSelectMenuState(data.PLAYERSELECTMEUNSTATE, false));
 		this.addState(new MainMenuState(data.MAINMENUSTATE, true));
 		this.enterState(data.MAINMENUSTATE);
 	}
 	
 	public GlobalData getData() {
 		return data;
 	}
 
 	public void initStatesList(GameContainer gc) throws SlickException {
 
 		SoundStore.get().setMaxSources(16);
 		initSounds();
 
 		AppGameContainer appContainer = (AppGameContainer) gc;
 
 		if (!appContainer.isFullscreen()) {
 			String[] icons = {"res/icon16.png", "res/icon32.png"};
 			appContainer.setIcons(icons);
 		}
 
 		this.getState(data.MAINMENUSTATE).init(gc, this);
 		this.getState(data.OPTIONSMENUSTATE).init(gc, this);
 		this.getState(data.GAMEPLAYSTATE).init(gc, this);
 		this.getState(data.PLAYERSELECTMEUNSTATE).init(gc, this);
 	}
 
 	public void initSounds() throws SlickException{
 		mySoundSystem.loadSound("BackLock.ogg");
 		mySoundSystem.loadSound("BackLockSlow.ogg");
 		mySoundSystem.loadSound("BackActivate.ogg");
 		mySoundSystem.loadSound("BackActivateSlow.ogg");
 		
 		mySoundSystem.loadSound("BallLaunch.ogg");
 		mySoundSystem.loadSound("BallLaunchSlow.ogg");
 		mySoundSystem.loadSound("BallBounce.ogg");
 		mySoundSystem.loadSound("BallBounceSlow.ogg");
 
 		mySoundSystem.loadSound("DashCharging.ogg");
 		mySoundSystem.loadSound("DashChargingSlow.ogg");
 		mySoundSystem.loadSound("DashDash.ogg");
 		mySoundSystem.loadSound("DashDashSlow.ogg");
 		mySoundSystem.loadSound("DashGust.ogg");
 		mySoundSystem.loadSound("DashGustSlow.ogg");
 		mySoundSystem.loadSound("DashWindingDown.ogg");
 		mySoundSystem.loadSound("DashWindingDownSlow.ogg");
 		mySoundSystem.loadSound("DashShortDash.ogg");
 		mySoundSystem.loadSound("DashShortDashSlow.ogg");
 		
 		mySoundSystem.loadSound("EnforcerBump.ogg");
 		mySoundSystem.loadSound("EnforcerBumpSlow.ogg");
 		mySoundSystem.loadSound("EnforcerStep.ogg");
 		mySoundSystem.loadSound("EnforcerStepSlow.ogg");
 		mySoundSystem.loadSound("EnforcerActivate.ogg");
 		mySoundSystem.loadSound("EnforcerActivateSlow.ogg");
 		mySoundSystem.loadSound("EnforcerTurn.ogg");
 		mySoundSystem.loadSound("EnforcerTurnSlow.ogg");
 		mySoundSystem.loadSound("EnforcerWallBounce.ogg");
 		mySoundSystem.loadSound("EnforcerWallBounceSlow.ogg");
 		
 		mySoundSystem.loadSound("KickBump.ogg");
 		mySoundSystem.loadSound("KickBumpSlow.ogg");
 		
 		mySoundSystem.loadSound("GoalScored.ogg");
 		mySoundSystem.loadSound("GoalOwnScored.ogg");
 
 		mySoundSystem.loadSound("NeoSlowOut.ogg");
 		mySoundSystem.loadSound("NeoSlowIn.ogg");
 		mySoundSystem.loadSound("NeoRecharged.ogg");
 		mySoundSystem.loadSound("NeoRechargedSlow.ogg");
 		
 		mySoundSystem.loadSound("NeutronPush.ogg");
 		mySoundSystem.loadSound("NeutronPushSlow.ogg");
 		mySoundSystem.loadSound("NeutronPull.ogg");
 		mySoundSystem.loadSound("NeutronPullSlow.ogg");
 		mySoundSystem.loadSound("NeutronCatch.ogg");
 		mySoundSystem.loadSound("NeutronCatchSlow.ogg");
 		mySoundSystem.loadSound("NeutronSwing.ogg");
 		mySoundSystem.loadSound("NeutronSwingSlow.ogg");
 		
 		mySoundSystem.loadSound("PowerKick.ogg");
 		mySoundSystem.loadSound("PowerKickSlow.ogg");
 		
 		mySoundSystem.loadSound("Rumble.ogg");
 		
 		mySoundSystem.loadSound("MenuThud.ogg");
 
 		mySoundSystem.loadSound("TwinsLtoR.ogg");
 		mySoundSystem.loadSound("TwinsLtoRSlow.ogg");
 		mySoundSystem.loadSound("TwinsRtoL.ogg");
 		mySoundSystem.loadSound("TwinsRtoLSlow.ogg");
 		
 		mySoundSystem.loadSound("TwoTouchActivate.ogg");
 		mySoundSystem.loadSound("TwoTouchActivateSlow.ogg");
 		mySoundSystem.loadSound("TwoTouchLockOn.ogg");
 		mySoundSystem.loadSound("TwoTouchLockOnSlow.ogg");
 
 		//TODO: Have streams created after character selection?
 		mySoundSystem.newStreamingSource(false, "slow1", "Rumble.ogg", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0f);
 		mySoundSystem.newStreamingSource(false, "slow2", "Rumble.ogg", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0f);
 	}
 
 	public static void main(String[] args) throws SlickException {
 		Cambridge kickerung = new Cambridge();
 
 		AppGameContainer app = new AppGameContainer(kickerung);
 		kickerung.mySoundSystem.setMasterVolume(kickerung.data.masterSound() / 10f);
 		System.out.println(kickerung.data.masterSound() / 10f);
 		app.setDisplayMode(kickerung.data.screenWidth(), kickerung.data.screenHeight(), false);
		app.setVSync(true);
		app.setTargetFrameRate(60);
 		app.setAlwaysRender(true);
 		app.setShowFPS(true);
 		app.setTitle("Kick Kickerung");
 //		app.setSmoothDeltas(true);
 //		if (app.supportsMultiSample())
 //			app.setMultiSample(2);
 		app.setFullscreen((kickerung.data.fullscreen()));
 		app.setMaximumLogicUpdateInterval(24);
 		app.setMinimumLogicUpdateInterval(24);
 		app.start();
 	}
 
 }
