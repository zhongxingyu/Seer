 package com.deadpixels.lib.screens;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.Texture;
 
 public abstract class ScreenManager implements ApplicationListener {
     private Screen  	 screen;
     private Boolean 	 manageScreenDisposal;
     private float 		 updateTimer;
     private AssetManager assetManager;
     private Boolean		 appCreated;
     
     public ScreenManager()
     {
     	updateTimer = 0.0f;
     	manageScreenDisposal = false;
     	appCreated = false;
     	assetManager = new AssetManager();
     	Texture.setAssetManager(assetManager);
     }
     
     public AssetManager getAssetManager()
     {
     	return assetManager;
     }
     
     public void manageScreenDisposal(Boolean _Manage)
     {
     	manageScreenDisposal = _Manage;
     }
 
     @Override
     public void dispose () {
    		setScreen(null);
             assetManager.dispose();
     }
     
     @Override
     public void create ()
     {
     	appCreated = true;
     	setScreen(getInitialScreen());
     }
     
     protected abstract Screen getInitialScreen();
 
     @Override
     public void pause () {
             if (screen != null) 
             	screen.pause(true);
     }
 
     @Override
     public void resume () {
             if (screen != null) 
             	screen.pause(false);
     }
 
     @Override
     public void render () {
             if (screen != null)
             {
             	float fDt = Gdx.graphics.getDeltaTime();
             	
             	float fUpdateDt = screen.getUpdateDt();
             	
             	if(fUpdateDt <= 0.0f)
             		screen.update(fDt);
             	else
             	{
             		updateTimer -= fDt;
                 	if(updateTimer <= 0.0f)
                 	{
                 		updateTimer = screen.getUpdateDt();
                 		screen.update(updateTimer);	
                 	}
             	}
             		
             	screen.render(fDt);
             }
     }
 
     @Override
     public void resize (int _width, int _height) {
             if (screen != null) 
             	screen.resize(_width, _height);
     }
 

     public void setScreen (Screen _screen) {
     	if(appCreated && _screen != screen)
         {   
             if(_screen != null && !_screen.isLoaded())
             	_screen.load();
             
     		if (screen != null) 
             {
             	screen.activate(false);
             	screen.unload();
             	if(manageScreenDisposal)
             		screen.dispose();
             }
             
             screen = _screen;
             
             if(screen != null)
             {
 	            Gdx.input.setInputProcessor(screen.getInputMultiplexer());
 		        screen.activate(true);
 		        screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
             }
         }
     }
 
     public Screen getScreen () {
             return screen;
     }
 }
