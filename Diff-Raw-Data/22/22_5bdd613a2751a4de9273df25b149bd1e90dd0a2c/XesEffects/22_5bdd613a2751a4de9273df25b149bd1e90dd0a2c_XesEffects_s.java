 /*******************************************************************************
  * Copyright (c) 2013 Paweł Kapalla, Xessenix. All rights reserved. This program
  * and the accompanying materials are made available under the terms of the GNU
  * Lesser Public License v2.1 which accompanies this distribution, and is
  * available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors: Paweł Kapalla, Xessenix - initial API and implementation
  ******************************************************************************/
 
 package pl.xesenix.games.effects;
 
 import pl.xesenix.games.effects.screens.AbstractScreen;
 import pl.xesenix.games.effects.screens.MenuScreen;
 import pl.xesenix.games.effects.screens.SplashScreen;
 import pl.xesenix.games.tweens.ColorAccessor;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 
 
 public class XesEffects extends Game
 {
	public static final String LOG = XesEffects.class.getSimpleName();
 
 
 	private FPSLogger fpsLogger;
 	
 	
 	private TweenManager tweenManager;
 
 
 	private Skin skin;
 
 
 	@Override
 	public void create()
 	{
 		Gdx.app.log(XesEffects.LOG, "Creating game object..");
 		
 		this.fpsLogger = new FPSLogger();
 		this.tweenManager = new TweenManager();
 		
 		Tween.setCombinedAttributesLimit(4);
 		Tween.registerAccessor(Color.class, new ColorAccessor());
 		
 		this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
 		
 		this.setScreen(this.getSplashScreen());
 	}
 
 
 	@Override
 	public void dispose()
 	{
 		super.render();
 	}
 
 
 	@Override
 	public void render()
 	{
 		final float delta = Gdx.graphics.getDeltaTime();
 		
 		super.render();
 		
 		this.tweenManager.update(delta);
 		
 		this.fpsLogger.log();
 	}
 
 
 	@Override
 	public void resize(final int width, final int height)
 	{
 		super.resize(width, height);
 	}
 
 
 	@Override
 	public void pause()
 	{
 	}
 
 
 	@Override
 	public void resume()
 	{
 	}
 
 
 	public SplashScreen getSplashScreen()
 	{
 		return new SplashScreen(this);
 	}
 	
 	public MenuScreen getMenuScreen()
 	{
 		return new MenuScreen(this);
 	}
 
 
 	/**
 	 * @return the tweenManager
 	 */
 	public TweenManager getTweenManager()
 	{
 		return tweenManager;
 	}
 
 
 	public Skin getSkin()
 	{
 		return this.skin;
 	}
 }
