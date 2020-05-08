 package com.meros.playn.core;
 
 import static playn.core.PlayN.graphics;
 import static playn.core.PlayN.log;
 import playn.core.Game;
 import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
 import playn.core.ImmediateLayer.Clipped;
 import playn.core.ImmediateLayer.Renderer;
 import playn.core.PlayN;
 import playn.core.Surface;
 import playn.core.gl.GLContext;
 import pythagoras.f.Point;
 
 import com.meros.playn.core.Constants.Buttons;
 import com.meros.playn.core.Input.HitTranslator;
 import com.meros.playn.core.screens.SplashScreen;
 import com.meros.playn.core.screens.TitleScreen;
 
 public class GreenGrappler implements Game, Renderer, HitTranslator {
 
 	public interface ExitCallback {
 		public abstract void exit();
 	}
 
 	float fps = 0.0f;
 
 	int frame = 0;
 
 	ExitCallback myExitCallback;
 	Font myFont = null;
 
 	boolean myFullScreen = false;
 
 	boolean myReadyForUpdates = false;
 
 	long startTimeMillis = System.currentTimeMillis();
 
	private ImmediateLayer bufferLayer;
 
 	public GreenGrappler(boolean aFullScreen, ExitCallback exitCallback) {
 		myFullScreen = aFullScreen;
 		myExitCallback = exitCallback;
 	}
 
 	ImageLayer controlLayer;
 	boolean mySetupRenderingOnce = false;
 
 	@Override
 	public void init() {
 		log().debug("Green Grappler init");
 		
 		GameState.loadFromFile();
 
 		// create and add background image layer
 		//graphics().setSize(1280, 720);
 		graphics().ctx().setTextureFilter(GLContext.Filter.NEAREST, GLContext.Filter.NEAREST);
 
 		Input.setTouchTranslator(this);
 
 		if (myFullScreen)
 			graphics().setSize(graphics().screenWidth(),
 					graphics().screenHeight());
 
 		Resource.preLoad("data/images/boss.bmp");
 		Resource.preLoad("data/images/breakinghooktile.bmp");
 		Resource.preLoad("data/images/button.bmp");
 		Resource.preLoad("data/images/checkpoint.bmp");
 		Resource.preLoad("data/images/coin.bmp");
 		Resource.preLoad("data/images/completed_level.bmp");
 		Resource.preLoad("data/images/core.bmp");
 		Resource.preLoad("data/images/darkbitslogo_blink.bmp");
 		Resource.preLoad("data/images/darken.bmp");
 		Resource.preLoad("data/images/debris.bmp");
 		Resource.preLoad("data/images/dialogue.bmp");
 		Resource.preLoad("data/images/doctor_green_portrait.bmp");
 		Resource.preLoad("data/images/door.bmp");
 		Resource.preLoad("data/images/entities.bmp");
 		Resource.preLoad("data/images/fade.bmp");
 		Resource.preLoad("data/images/font.bmp");
 		Resource.preLoad("data/images/hand.bmp");
 		Resource.preLoad("data/images/hero_fall.bmp");
 		Resource.preLoad("data/images/hero_hurt.bmp");
 		Resource.preLoad("data/images/hero_jump.bmp");
 		Resource.preLoad("data/images/hero_run.bmp");
 		Resource.preLoad("data/images/hook.bmp");
 		Resource.preLoad("data/images/icons.bmp");
 		Resource.preLoad("data/images/lavafill.bmp");
 		Resource.preLoad("data/images/lavatop.bmp");
 		Resource.preLoad("data/images/level_exit_background.bmp");
 		Resource.preLoad("data/images/level_select.bmp");
 		Resource.preLoad("data/images/logo.bmp");
 		Resource.preLoad("data/images/movinghooktile.bmp");
 		Resource.preLoad("data/images/particles.bmp");
 		Resource.preLoad("data/images/reactor_shell.bmp");
 		Resource.preLoad("data/images/robot.bmp");
 		Resource.preLoad("data/images/rope.bmp");
 		Resource.preLoad("data/images/saw.bmp");
 		Resource.preLoad("data/images/selected_level_background.bmp");
 		Resource.preLoad("data/images/software.bmp");
 		Resource.preLoad("data/images/ted_portrait.bmp");
 		Resource.preLoad("data/images/tileset1.bmp");
 		Resource.preLoad("data/images/title.bmp");
 		Resource.preLoad("data/images/unselected_level_background.bmp");
 		Resource.preLoad("data/images/wall.bmp");
 		Resource.preLoad("data/images/z_coin.bmp");
 		Resource.preLoad("data/images/controls.png");
 
 		Resource.preLoadText("data/rooms/breaktilelevel.txt");
 		Resource.preLoadText("data/rooms/level1.txt");
 		Resource.preLoadText("data/rooms/levellava.txt");
 		Resource.preLoadText("data/rooms/olof.txt");
 		Resource.preLoadText("data/rooms/olof2.txt");
 		Resource.preLoadText("data/rooms/per1.txt");
 		Resource.preLoadText("data/rooms/per2.txt");
 		Resource.preLoadText("data/rooms/per3.txt");
 		Resource.preLoadText("data/rooms/per4.txt");
 		Resource.preLoadText("data/rooms/smalltestroom.txt");
 		Resource.preLoadText("data/rooms/test.txt");
 		Resource.preLoadText("data/rooms/tutorial.txt");
 		Resource.preLoadText("data/dialogues/1-tutorial1.txt");
 		Resource.preLoadText("data/dialogues/2-tutorial2.txt");
 		Resource.preLoadText("data/dialogues/boss_unlocked.txt");
 		Resource.preLoadText("data/dialogues/level_select.txt");
 
 		Resource.preLoadSound("data/sounds/alarm");
 		Resource.preLoadSound("data/sounds/beep");
 		Resource.preLoadSound("data/sounds/boot");
 		Resource.preLoadSound("data/sounds/boss_saw");
 		Resource.preLoadSound("data/sounds/coin");
 		Resource.preLoadSound("data/sounds/damage");
 		Resource.preLoadSound("data/sounds/green_peace");
 		Resource.preLoadSound("data/sounds/hook");
 		Resource.preLoadSound("data/sounds/hurt");
 		Resource.preLoadSound("data/sounds/jump");
 		Resource.preLoadSound("data/sounds/land");
 		Resource.preLoadSound("data/sounds/no_hook");
 		Resource.preLoadSound("data/sounds/reactor_explosion");
 		Resource.preLoadSound("data/sounds/rope");
 		Resource.preLoadSound("data/sounds/select");
 		Resource.preLoadSound("data/sounds/start");
 		Resource.preLoadSound("data/sounds/time");
 		Resource.preLoadSound("data/sounds/timeout");
 
 		Input.init();
 	}
 
 	@Override
 	public void paint(float alpha) {
 		if (myReadyForUpdates && !mySetupRenderingOnce)
 		{
 
			bufferLayer = graphics().createImmediateLayer(this);
 
 			{		
 				float w = PlayN.graphics().width();
 				float h = PlayN.graphics().height();
 
 				float aspect = w/h;
 				float targetAspect = 320/240;
 				float scale = 1.0f;
 
 				float translateX = 0;
 				float translateY = 0;
 
 				if (aspect < targetAspect) {
 					scale = w / 320;
 					translateY = h/2-240*scale/2;
 				} else {
 					scale = h / 240;
 					translateX = w/2-320*scale/2;
 				}
 
 
 
 				//bufferLayer.setTranslation(translateX, translateY);
 				bufferLayer.setScale(scale);
 				bufferLayer.setTranslation(translateX, translateY);
 			}
 
 			graphics().rootLayer().add(bufferLayer);
 
 
 			if (GlobalOptions.showTouchControls())
 			{
 				controlLayer = graphics().createImageLayer(PlayN.assets().getImage("data/images/controls.png"));
 
 
 				{
 					float w = PlayN.graphics().width();
 					float h = PlayN.graphics().height();
 
 					float aspect = w/h;
 					float targetAspect = 960/720;
 					float scale = 1.0f;
 
 					float translateX = 0;
 					float translateY = 0;
 
 					if (aspect < targetAspect) {
 						scale = w / 960;
 						translateY = h/2-720*scale/2;
 					} else {
 						scale = h / 720;
 						translateX = w/2-960*scale/2;
 					}
 
 					controlLayer.setScale(scale);
 					controlLayer.setTranslation(translateX, translateY);
 					controlLayer.setAlpha(0.5f);
 				}
 
 				graphics().rootLayer().add(controlLayer);
 			}
 
 			mySetupRenderingOnce = true;
 		}
 	}
 
 	void postPreloadInit() {
 		// ScreenManager.add(new EndScreen());
 		ScreenManager.add(new TitleScreen());
 		ScreenManager.add(new SplashScreen());
 		myFont = Resource.getFont("data/images/font.bmp");
 	}
 
 	void postPreloadUpdate() {
 		ScreenManager.onLogic();
 		Music.update();
 	}
 
 	int fpsCount = 0;
 	int lastFpsCount = 0;
 
 	@Override
 	public void render(Surface surface) {
 		if (!myReadyForUpdates)
 			return;
 		ScreenManager.draw(surface);
 
 		long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
 		if (elapsedTimeMillis > 1000)
 		{
 			startTimeMillis += 1000;
 			lastFpsCount = fpsCount;
 			fpsCount = 0;
 		}
 		else
 		{
 			fpsCount ++;
 		}
 
 		myFont.draw(surface, "fps: " + lastFpsCount, 10, 10);
 	}
 
 	@Override
 	public void update(float delta) {		
 		if (myReadyForUpdates) {
 			postPreloadUpdate();
 		} else if (Resource.isDonePreloading()) {
 			postPreloadInit();
 			myReadyForUpdates = true;
 		}
 
 		if (Input.isPressed(Buttons.ForceQuit) || ScreenManager.isEmpty()) {
 			myExitCallback.exit();
 		}
 
 		Input.update();
 	}
 
 	@Override
 	public int updateRate() {
 		return 1000 / Time.TicksPerSecond;
 	}
 
 	@Override
 	public void translateHit(Point aHitpoint) {
 		controlLayer.transform().inverseTransform(aHitpoint, aHitpoint);
 	}
 }
