 package Screens;
 
 import java.util.Vector;
 
 import Application.Assets;
 import Application.LaunchFrozenWars;
 
 import Server.SmartFoxServer;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 import com.badlogic.gdx.utils.ScreenUtils;
 
 public class ConfirmScreen implements Screen{
 	private static ConfirmScreen instance;
 	private OrthographicCamera guiCam;
 	private SpriteBatch batcher; 
 	private Vector3 touchPoint;
 	private BoundingBox yesClick;
 	private BoundingBox noClick;
 	private Game game;
 	private BitmapFont font;
 	private TextureRegion background;
 	private Vector<String> screenModeV;
 	private Vector<String> userV;
 	private Screen ancestor; 
 	private String screenMode;
 	private String user;
 	
 	public static ConfirmScreen getInstance() {
 		if (instance == null) instance = new ConfirmScreen();
 		return instance;
 	}
 
 	public ConfirmScreen() {
 		instance = this;
 		this.game = LaunchFrozenWars.getGame();
 		screenModeV = new Vector<String>();
 		userV = new Vector<String>();
 		guiCam = new OrthographicCamera(1024,629);
 		guiCam.position.set(512,315,0);
 		
 		font = new BitmapFont(Gdx.files.internal("data/first.fnt"), Gdx.files.internal("data/first.png"), false);;
 
 	    batcher = new SpriteBatch();
 	    touchPoint = new Vector3();
 
 	    yesClick = new BoundingBox(new Vector3(350,340,0), new Vector3(500,400,0));
 	    noClick = new BoundingBox(new Vector3(510,340,0), new Vector3(660,660,0));        
 	}
 	
 	public void setNewConfirmScreen(String mode, String usr){
		screenModeV.add(mode);
		userV.add(usr);
 	}
 	
 	public void createConfirmIfNeeded(){
 		if (!screenModeV.isEmpty()){
 			background = ScreenUtils.getFrameBufferTexture();
 			ancestor = game.getScreen();
 			screenMode = screenModeV.remove(0);
 			user = userV.remove(0);
 			game.setScreen(this);
 		}
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
       		System.out.println(Integer.toString((int)touchPoint.x).concat(",").concat(Integer.toString((int)touchPoint.y)));
 
       		//compruebo si he tocado yes 
       		if (yesClick.contains(touchPoint)){
       			if (screenMode.equals("Exit")) this.dispose();
       			else if (screenMode.equals("InviteGame")) {
       				SmartFoxServer.getInstance().acceptRequest(user);
       				MultiplayerScreen.getInstance().setGameAdmin(user);
       				game.setScreen(MultiplayerScreen.getInstance());
       			} else if (screenMode.equals("FullTeam") || screenMode.equals("QueueExit") || screenMode.equals("GameNotFound") || screenMode.equals("LeaderLeft") || screenMode.equals("UserOutOfQueue")){
       				MultiplayerScreen.getInstance().setDefault();
       				game.setScreen(MultiplayerScreen.getInstance());
       			} else if (screenMode.equals("InvitedDisconnected")){
       				game.setScreen(new InviteScreen());
       			}
       			
       		} else if(noClick.contains(touchPoint)){ //compruebo si he tocado no
       			if (screenMode.equals("Exit") || screenMode.equals("FullTeam") || screenMode.equals("QueueExit") || screenMode.equals("InvitedDisconnected")) game.setScreen(ancestor);
       			else if (screenMode.equals("InviteGame")) {
       				SmartFoxServer.getInstance().refuseRequest(user);
       				game.setScreen(ancestor);
       			} else if (screenMode.equals("GameNotFound") || screenMode.equals("LeaderLeft") || screenMode.equals("UserOutOfQueue")){
       				MultiplayerScreen.getInstance().setDefault();
       				game.setScreen(ancestor);
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
             batcher.draw(background,0,0);
             batcher.end();
 
           //Dibujando elementos en pantalla activamos el Blending
             batcher.enableBlending();
             batcher.begin();    
             batcher.draw(Assets.window, 330, 300);
             if (screenMode.equals("Exit")) batcher.draw(Assets.exitText, 330, 300);
             else if (screenMode.equals("InviteGame")) {
             	String message = user.concat(" has invited you to join his game.");
             	font.drawWrapped(batcher, message, 350, 525, 330);
             	font.draw(batcher, "Do you want to join him?", 350, 450);
             } else if (screenMode.equals("FullTeam")){
             	String message = "You can't join this game because the team is full. Do you want to create your own game?";
             	font.drawWrapped(batcher, message, 350, 538, 330);
             } else if (screenMode.equals("QueueExit")){
             	String message = "Someone in your team left so you all have left the queue. Do you want to create a game?";
             	font.drawWrapped(batcher, message, 350, 538, 330);
             } else if (screenMode.equals("InvitedDisconnected")){
             	String message = user.concat(" has disconnected. Do you want to invite anyone else?");
             	font.drawWrapped(batcher, message, 350, 538, 330);
             } else if (screenMode.equals("GameNotFound")){
             	String message = "Game not found. Do you want to create your own game?";
             	font.drawWrapped(batcher, message, 350, 538, 330);
             } else if (screenMode.equals("LeaderLeft")){
             	String message = "Game's leader left. Do you want to create your own game?";
             	font.drawWrapped(batcher, message, 350, 538, 330);
             } else if (screenMode.equals("UserOutOfQueue")){
             	String message = "A teammate has disconnected so you all have left the queue. Do you want to create a game?";
             	font.drawWrapped(batcher, message, 350, 538, 330);
             }
             batcher.end();	
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		// TODO Auto-generated method stub	
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
