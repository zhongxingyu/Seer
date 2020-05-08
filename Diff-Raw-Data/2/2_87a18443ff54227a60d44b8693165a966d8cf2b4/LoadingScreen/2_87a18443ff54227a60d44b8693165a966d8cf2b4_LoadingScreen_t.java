 /*******************************************************************************
  * Copyright 2013 See AUTHORS file.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.mangecailloux.pebble.screens.loading;
 
 import com.mangecailloux.pebble.Pebble;
 import com.mangecailloux.pebble.screens.Screen;
 import com.mangecailloux.pebble.screens.ScreenManager;
 import com.mangecailloux.pebble.screens.ScreenUpdatePriority;
 import com.mangecailloux.pebble.updater.Updater;
 import com.mangecailloux.pebble.updater.impl.ConstantUpdater;
 
 
 public abstract class LoadingScreen extends Screen 
 {
 	
 	protected final Screen 	screenToLoad;
 	protected final boolean autoScreenChange;
 	protected 		boolean	nextIsLoaded;
 	private			float		timer;
 	private final	float		duration;
 	
 	private LoadingScreen(String _name, Screen _ToLoad, boolean _autoScreenChange, float _duration)
 	{
 		super(_name);
 		
 		if(_ToLoad == null)
 			throw new IllegalArgumentException("Screen to load must not be null");
 		
 		screenToLoad = _ToLoad;
 		autoScreenChange = _autoScreenChange;
 		nextIsLoaded = false;
 		
 		duration = _duration;
 		timer = duration;
 	}
 	
 	public LoadingScreen(String _name, Screen _ToLoad, boolean _autoScreenChange)
 	{
 		this(_name, _ToLoad, _autoScreenChange, -1.0f);
 	}
 	
 	public LoadingScreen(String _name, ScreenManager _Manager, Screen _ToLoad, float _duration)
 	{
 		this(_name, _ToLoad, true, _duration);
 	}
 
 	protected void onUpdate(float _fDt) 
 	{		
		if(Pebble.assets != null && screenToLoad != null && !nextIsLoaded)
 		{
 			if(Pebble.assets.processLoadingQueue())
 				nextIsLoaded = true;
 		}
 		
 		if(timer >= 0.0f)
 			timer -= _fDt;
 		
 		if(autoScreenChange && nextIsLoaded && timer < 0.0f)
 			changeScreen();
 	}
 	
 	protected void onActivation()
 	{
 		timer = duration;
 	}
 	
 	protected void onFirstActivation()
 	{
 		addUpdater(update);
 	}
 	
 	protected void onLoad ()
 	{
 		if(screenToLoad != null)
 			screenToLoad.load();
 	}
 	
 	protected void onUnload ()
 	{
 		
 	}
 	
 	protected void changeScreen()
 	{
 		manager.setScreen(screenToLoad);
 	}
 	
 	Updater update = new ConstantUpdater(ScreenUpdatePriority.BeforeRender) 
 	{
 		@Override
 		public void doUpdate(float _dt) 
 		{
 			onUpdate(_dt);
 		}
 	};
 }
