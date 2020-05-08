 package Screens;
 
 import Application.Assets;
 import Application.Desktop;
 import Application.GameSettings;
 import Application.LaunchFrozenWars;
 import Server.SmartFoxServer;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 
 public class InitialScreen implements Screen{
 	private static InitialScreen instance;
 	
 	public static InitialScreen getInstance(){
 		if (instance == null) instance = new InitialScreen();
 		return instance;
 	}
 
 	    /** The gui cam. */
 	private OrthographicCamera guiCam;
 	
 	/** Se utiliza para dibujar y optimizar las imagenes en el renderizado de la pantalla. */
 	private SpriteBatch batcher;
 	private GameSettings gSettings;
 	private Vector3 touchPoint;
 	private BoundingBox playClick;
 	private BoundingBox settingsClick;
 	private BoundingBox helpClick;
 	private BoundingBox exitClick;
 	private BoundingBox J1Click;
 	private BoundingBox J2Click;
 	private BoundingBox J3Click;
 	private BoundingBox J4Click;
 	private Game game;
 	private boolean developer;	
 	
 	public InitialScreen() {
 		instance = this;
 		this.game = LaunchFrozenWars.getGame();
 		gSettings = GameSettings.getInstance();
 	    if (gSettings.isSoundOn()){
 			Assets.music.setVolume(0.5f);
 			//  Assets.music.play();
 			Assets.music.setLooping(true);
 	    }
 
 
 
 		guiCam = new OrthographicCamera(1024,630);
 		guiCam.position.set(512,315,0);
 	    
 	    batcher = new SpriteBatch();
 	    touchPoint = new Vector3();
 	    //Esquina inferior izq y superior derecha
 	    playClick = new BoundingBox(new Vector3(150,500,0), new Vector3(440,550,0));
 	    helpClick = new BoundingBox(new Vector3(150,400,0), new Vector3(440,445,0));
 	    settingsClick = new BoundingBox(new Vector3(590,500,0), new Vector3(880,540,0));
 	    exitClick = new BoundingBox(new Vector3(590,400,0), new Vector3(880,445,0));
 	    
 	    J1Click = new BoundingBox(new Vector3(184,178,0), new Vector3(212,204,0));
 	    J2Click = new BoundingBox(new Vector3(219,179,0), new Vector3(245,204,0));
 	    J3Click = new BoundingBox(new Vector3(185,144,0), new Vector3(211,168,0));
 	    J4Click = new BoundingBox(new Vector3(219,145,0), new Vector3(246,168,0));
 	    
	    developer = false;
 	}
 
 	
 	@Override
 	public void dispose() {
 		batcher.dispose();
 		Assets.music.dispose();
 		System.exit(0);	
 
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub	
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub	
 	}
 
 	@Override
 	public void render(float arg0) {
 		
 		//detectamos si se ha tocado la pantalla
 		if (Gdx.input.justTouched()){
 			guiCam.unproject(touchPoint.set(Gdx.input.getX(),Gdx.input.getY(),0));
       		//System.out.println(Integer.toString((int)touchPoint.x).concat(",").concat(Integer.toString((int)touchPoint.y)));
 
 			//compruebo si he tocado play (se abre ventana de introduccion de usuario si no esta logeado)
 			if (playClick.contains(touchPoint)){
 				GameSettings sett = GameSettings.getInstance();
 				if (SmartFoxServer.getInstance().isLoggedIn()) game.setScreen(MultiplayerScreen.getInstance());
 				else if (sett.isLoggedIn()){
 					
 					SmartFoxServer.getInstance().conectaSala(sett.getUserName(), sett.getUserPassword());
 				}
 				else game.setScreen(LogSignScreen.getInstance());
 				
 
       		}else if(settingsClick.contains(touchPoint)){ //compruebo si he tocado settings
       				SettingsScreen settingsScreen = new SettingsScreen();
       				game.setScreen(settingsScreen);
       		}else if (helpClick.contains(touchPoint)){ //compruebo si he tocado help (Se abre ventana de ayuda)
       				IndexScreen indexScreen = new IndexScreen();
       				game.setScreen(indexScreen);
       				
       		}else if(exitClick.contains(touchPoint)){ //compruebo si he tocado exit (Se abre ventana de confirmacion)
       				gSettings.saveSettings();
       				
       				if (!gSettings.isConfirmedExitOn()) {
       					this.dispose();
       				} else{
       					ConfirmScreen.getInstance().setNewConfirmScreen("Exit", "");   
       				}
       		} else if (developer){
       			if (J1Click.contains(touchPoint)){
       				if (SmartFoxServer.getInstance().isLoggedIn()) game.setScreen(MultiplayerScreen.getInstance());
     				else SmartFoxServer.getInstance().conectaSala("UT1", "UT11");
       			} else if (J2Click.contains(touchPoint)){
       				if (SmartFoxServer.getInstance().isLoggedIn()) game.setScreen(MultiplayerScreen.getInstance());
     				else SmartFoxServer.getInstance().conectaSala("UT2", "UT22");
       			} else if (J3Click.contains(touchPoint)){
       				if (SmartFoxServer.getInstance().isLoggedIn()) game.setScreen(MultiplayerScreen.getInstance());
     				else SmartFoxServer.getInstance().conectaSala("UT3", "UT33");
       			} else if (J4Click.contains(touchPoint)){
       				if (SmartFoxServer.getInstance().isLoggedIn()) game.setScreen(MultiplayerScreen.getInstance());
     				else SmartFoxServer.getInstance().conectaSala("UT4", "UT44");
       			} 
       		}
       		
 		}
 		//crear solamente un batcher por pantalla y eliminarlo cuando no se use
 
       		GL10 gl = Gdx.graphics.getGL10(); //referencia a OpenGL 1.0
             gl.glClearColor(0,1,0,1);
             gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            
             guiCam.update();
             batcher.setProjectionMatrix(guiCam.combined);
              
             //Dibujando el Background
             batcher.disableBlending();
             //se elimina graficamente la transparencia ya que es un fondo
             batcher.begin();
             batcher.draw(Assets.initialBack,0,0);
             batcher.end();
 
             //Dibujando elementos en pantalla activamos el Blending
             batcher.enableBlending();
             batcher.begin();
 	                
 	        batcher.draw(Assets.play, 120, 500);
 	        batcher.draw(Assets.settings, 560, 500);
 	        batcher.draw(Assets.help, 120, 400);
 	        batcher.draw(Assets.exit, 560, 400);
 	        
 	        if (developer) {
 	        	batcher.draw(Assets.J1, 185, 180);
 	        	batcher.draw(Assets.J2, 220, 180);
 	        	batcher.draw(Assets.J3, 185, 145);
 	        	batcher.draw(Assets.J4, 220, 145);
 	        }
 	        
             batcher.end();
             
             MultiplayerScreen.getInstance().changeToThisIfNeeded();
             ConfirmScreen.getInstance().createConfirmIfNeeded();
             AcceptScreen.getInstance().createAcceptIfNeeded();
 
 	}
 
 	
 	@Override
 	public void resize(int arg0, int arg1) {
 		if (arg0!=1024 || arg1!=630) Desktop.resetScreenSize();
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
