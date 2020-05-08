 package com.icbat.game.tradesong.screens;
 
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.icbat.game.tradesong.Tradesong;
 
 /**
  * Generic abstraction of things shared by all screens
  * */
 public abstract class AbstractScreen implements Screen {
 
 	protected final Tradesong gameInstance;
 	protected Skin skin;
 	protected Stage stage;
 	protected SpriteBatch batch;
 	
 	public AbstractScreen( Tradesong game ) {
 		this.gameInstance = game;
 		log( "Creating Screen" );
 	}
 	
 	@Override
 	public void render( float delta ) {
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		log( "Resizing screen to " + width + "w by " + height + "h" );
 //		super.resize(width, height);
 
 	}
 
 	@Override
 	public void show() {
 		log( "Showing Screen: " );
 
 	}
 
 	@Override
 	public void hide() {
 		log( "Hiding Screen: " );
 
 	}
 
 	@Override
 	public void pause() {
 		log( "Pausing Screen: " );
 
 	}
 
 	@Override
 	public void resume() {
 		log( "Resuming Screen: " );
 
 	}
 
 	@Override
 	public void dispose() {
 		log( "Disposing of Screen: " );
 
 	}
 	
 	protected void log( String message ) {
		this.gameInstance.log.info( getClass().getSimpleName() + " says:  " + message);
 	}	
 }
