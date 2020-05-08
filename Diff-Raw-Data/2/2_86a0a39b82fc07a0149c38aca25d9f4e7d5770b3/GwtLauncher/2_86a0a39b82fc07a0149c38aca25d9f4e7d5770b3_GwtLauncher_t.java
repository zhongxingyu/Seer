 package com.me.godric.client;
 
 import com.me.godric.GodricGame;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.backends.gwt.GwtApplication;
 import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
 
 public class GwtLauncher extends GwtApplication {
 	@Override
 	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(780, 585);
 		return cfg;
 	}
 
 	@Override
 	public ApplicationListener getApplicationListener () {
 		return new GodricGame();
 	}
 }
