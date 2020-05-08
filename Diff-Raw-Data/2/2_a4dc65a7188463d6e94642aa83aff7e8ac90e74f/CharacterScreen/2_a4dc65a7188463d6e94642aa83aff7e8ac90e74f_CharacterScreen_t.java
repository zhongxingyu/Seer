 package com.soc.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.soc.core.Constants;
 import com.soc.core.SoC;
 import com.soc.utils.GameLoader;
 import com.soc.utils.MusicPlayer;
 
 public class CharacterScreen extends AbstractScreen implements InputProcessor{
 	private Texture background;
 	private int focusedBotton;
 	private TextButton warriorButton;
 	private TextButton mageButton;
 	private TextButton []buttons;
 	private TextButtonStyle normalStyle;
 	private TextButtonStyle focusedStyle;
 	public CharacterScreen(SoC game) {
 		super(game);
 		background=new Texture(Gdx.files.internal("resources/background.jpg"));
 		normalStyle=new TextButtonStyle();
 		normalStyle.font=getSkin().getFont("buttonFont");
 		normalStyle.up=getSkin().getDrawable("normal-button");
 		normalStyle.down=getSkin().getDrawable("pushed-button");
 		focusedStyle=new TextButtonStyle();
 		focusedStyle.font=getSkin().getFont("buttonFont");
 		focusedStyle.up=getSkin().getDrawable("focused-button");
 		focusedStyle.down=getSkin().getDrawable("pushed-button");
 		warriorButton = new TextButton( "WARRIOR", normalStyle);
 		mageButton = new TextButton( "MAGE", normalStyle);
 		buttons=new TextButton[2];
 		buttons[0]=warriorButton;
 		buttons[1]=mageButton;
 
 		focusedBotton=1;
 		SoC.game.inputMultiplexer.addProcessor(this);
 		if(game.getScreen()!=null){
 			game.getScreen().dispose();
 		}
 	}
     @Override
     public void show()
     {
         super.show();
         // retrieve the default table actor
         Table table = super.getTable();
         // register the button "start game"
         warriorButton.addListener( new InputListener() {
             @Override
             public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
             {
                 return true;
             }
             @Override
             public void touchUp(
                 InputEvent event,
                 float x,
                 float y,
                 int pointer,
                 int button )
             {
             	if(button==0){
 	                SoC.game.clearProcessors();
 	                GameLoader.newGame(Constants.Characters.WARRIOR);
             	}
 
             }
 
         } );
         
         warriorButton.addListener(new ClickListener(){
         	public boolean mouseMoved(InputEvent event,
                     float x,
                     float y){
         		if(focusedBotton!=1){
         			buttons[focusedBotton-1].setStyle(normalStyle);
         		}
         		focusedBotton=1;
         		return true;
         		
         	}
 
         });
         table.add( warriorButton ).size( 300, 60 ).uniform().spaceBottom( 10 );
         table.row();
 
         // register the button "options"
         mageButton.addListener( new InputListener() {
             @Override
             public void touchUp(
                 InputEvent event,
                 float x,
                 float y,
                 int pointer,
                 int button )
             {
             	if(button==0){
 	                SoC.game.clearProcessors();
 	                GameLoader.newGame(Constants.Characters.MAGE);
             	}
 
             }
             @Override
             public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
             {
                 return true;
             }
         } );
         
         mageButton.addListener(new ClickListener(){
         	public boolean mouseMoved(InputEvent event,
                     float x,
                     float y){
         		if(focusedBotton!=2){
         			buttons[focusedBotton-1].setStyle(normalStyle);
         		}
         		focusedBotton=2;
         		return true;
         		
         	}
 
         });
         table.add( mageButton ).uniform().fill().spaceBottom( 10 );
         table.row();
 
     }
     
 	public void render(float delta) {
 	       Gdx.gl.glClearColor( 0f, 0f, 0f, 1f );
 	        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
 	        batch.begin();
 	        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 	        buttons[focusedBotton-1].setStyle(focusedStyle);
 	        //Update delta and draw the actors inside the stage
 	        batch.end();
 	        stage.act( delta );
 	        stage.draw();
 		
 	}
 	@Override
 	public boolean keyDown(int keycode) {
 		if( keycode == Keys.W || keycode == Keys.UP){
 			buttons[focusedBotton-1].setStyle(normalStyle);
 			if(focusedBotton==1)
 				focusedBotton=4;
 			else
 				focusedBotton--;
 			return true;
 		}else{
 			if(keycode == Keys.S || keycode == Keys.DOWN){
 				buttons[focusedBotton-1].setStyle(normalStyle);
				if(focusedBotton==2)
 					focusedBotton=1;
 				else
 					focusedBotton++;
 				return true;
 			}else{
 				if(keycode == Keys.ENTER){
 					if(focusedBotton==1){
 		                SoC.game.clearProcessors();
 		                GameLoader.newGame(Constants.Characters.WARRIOR);
 					}else{
 						if(focusedBotton==2){
 			                SoC.game.clearProcessors();
 			                GameLoader.newGame(Constants.Characters.MAGE);	
 						}
 					}
 					return true;
 				}
 			}
 		}	
 		return false;
 	}
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 	@Override
 	public boolean keyTyped(char character) {	
 		return false;
 	}
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		return false;
 	}
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 }
