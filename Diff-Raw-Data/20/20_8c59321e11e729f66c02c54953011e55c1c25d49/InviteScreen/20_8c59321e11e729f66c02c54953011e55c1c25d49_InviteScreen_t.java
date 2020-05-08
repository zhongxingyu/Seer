 package Screens;
 
 import java.util.Vector;
 
 
 import Application.Assets;
 import Application.Desktop;
 import Application.LaunchFrozenWars;
 import Server.SmartFoxServer;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 
 public class InviteScreen implements Screen{
 	
 	private static InviteScreen instance;
 	
 	public static InviteScreen getInstance() {
 		if (instance == null) instance = new InviteScreen();
 		return instance;
 	}
 	
 	Game game;
 	    /** The gui cam. */
 	private OrthographicCamera guiCam;
 	
 	/** Se utiliza para dibujar y optimizar las imagenes en el renderizado de la pantalla. */
 	private SpriteBatch batcher;
 	private Vector3 touchPoint;
 	private SmartFoxServer sfsClient;
 	private BitmapFont font;
 	private MultiplayerScreen multiplayerScreen;
     
     private BoundingBox inviteButtonClick;
     private BoundingBox invitePlayersButtonClick;
     private BoundingBox backButtonClick;
     
     private BoundingBox scrollDownNotInvitedClick;
     private BoundingBox scrollUpNotInvitedClick;
     private BoundingBox scrollDownInvitedClick;
     private BoundingBox scrollUpInvitedClick;
     
     private BoundingBox[] invitedPlayer;
     private BoundingBox moveInvited;
     private BoundingBox[] notInvitedPlayer;
     private BoundingBox moveNotInvited;
     
     private Vector<String> notInvited;
     private Vector<String> invited;
     private int notInvitedScroll;
 	private int invitedScroll;
 	
 	private boolean noFriendsOn;
     
 	public void setNotInvited(Vector<String> friends){
 		notInvited = friends;
 	}
     
     public InviteScreen() {
     	instance = this;
 		this.multiplayerScreen = MultiplayerScreen.getInstance();
 		this.game = LaunchFrozenWars.getGame();
 		
 		guiCam = new OrthographicCamera(1024,630);
 		guiCam.position.set(512,315,0);
 		
 	    batcher = new SpriteBatch();
 	    touchPoint = new Vector3();
 	    font = new BitmapFont(Gdx.files.internal("data/first.fnt"), Gdx.files.internal("data/first.png"), false);
 	    
 	    this.sfsClient = SmartFoxServer.getInstance();
 	    
 	    invited = new Vector<String>();
 	    notInvited = new Vector<String>();
 	    sfsClient.getConnectedFriendsRequest();
 	    
 	    notInvitedScroll = 0;
 	    invitedScroll = 0;
 
 	    inviteButtonClick = new BoundingBox(new Vector3(260,50,0), new Vector3(500,100,0));
 	    invitePlayersButtonClick = new BoundingBox(new Vector3(400, 160,0), new Vector3(640, 200,0));
 	    backButtonClick = new BoundingBox(new Vector3(540,45,0), new Vector3(790,110,0));
 	    
 	    scrollDownNotInvitedClick = new BoundingBox(new Vector3(430,185,0), new Vector3(485,220,0));
 	    scrollUpNotInvitedClick = new BoundingBox(new Vector3(435,365,0), new Vector3(485,405,0));
 	    
 	    scrollDownInvitedClick = new BoundingBox(new Vector3(800,185,0), new Vector3(850,225,0));
 	    scrollUpInvitedClick = new BoundingBox(new Vector3(800,365,0), new Vector3(850,400,0));
 	    
 	    notInvitedPlayer = new BoundingBox[5];
 	    notInvitedPlayer[0] = new BoundingBox(new Vector3(150,365,0), new Vector3(430,405,0));
 	    notInvitedPlayer[1] = new BoundingBox(new Vector3(150,320,0), new Vector3(430,360,0));
 	    notInvitedPlayer[2] = new BoundingBox(new Vector3(150,275,0), new Vector3(430,315,0));
 	    notInvitedPlayer[3] = new BoundingBox(new Vector3(150,230,0), new Vector3(430,270,0));
 	    notInvitedPlayer[4] = new BoundingBox(new Vector3(150,185,0), new Vector3(430,225,0));
 	    moveNotInvited =  new BoundingBox(new Vector3(150,185,0), new Vector3(430,405,0));
 	    
 	    invitedPlayer = new BoundingBox[5];
 	    invitedPlayer[0] = new BoundingBox(new Vector3(525,365,0), new Vector3(800,405,0));
 	    invitedPlayer[1] = new BoundingBox(new Vector3(525,320,0), new Vector3(800,360,0));
 	    invitedPlayer[2] = new BoundingBox(new Vector3(525,275,0), new Vector3(800,315,0));
 	    invitedPlayer[3] = new BoundingBox(new Vector3(525,230,0), new Vector3(800,270,0));
 	    invitedPlayer[4] = new BoundingBox(new Vector3(525,185,0), new Vector3(800,225,0));
 	    moveInvited = new BoundingBox(new Vector3(525,185,0), new Vector3(800,405,0));
 	    
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
 		
 		if(this.invited.isEmpty() && this.notInvited.isEmpty())
 			this.noFriendsOn = true;
 		else 
 			this.noFriendsOn = false;
 		
 		//detectamos si se ha tocado la pantalla
 		if (Gdx.input.justTouched()){
 			guiCam.unproject(touchPoint.set(Gdx.input.getX(),Gdx.input.getY(),0));
 						
 			if (this.noFriendsOn){
 				if (invitePlayersButtonClick.contains(touchPoint)){
 	      			game.setScreen(FriendsListScreen.getInstance());
	      			sfsClient.getMyFriendsRequest();
 	      		} else if (backButtonClick.contains(touchPoint)){
 	      			game.setScreen(multiplayerScreen);
 	      		} 
 			} else {
 			
 				//compruebo si he tocado play (se abre ventana de introduccion de usuario si no esta logeado)
 				if (scrollDownNotInvitedClick.contains(touchPoint)){
 	      			if (notInvitedScroll < notInvited.size() - 5) notInvitedScroll++;
 	      		} else if (scrollUpNotInvitedClick.contains(touchPoint)){
 	      			if (notInvitedScroll != 0) notInvitedScroll--;     	
 				} if (scrollDownInvitedClick.contains(touchPoint)){
 	      			if (invitedScroll < invited.size() - 5) invitedScroll++;
 	      		} else if (scrollUpInvitedClick.contains(touchPoint)){
 	      			if (invitedScroll != 0) invitedScroll--;     	
 				} else if (backButtonClick.contains(touchPoint)){
 	      			game.setScreen(multiplayerScreen);
 	      		} else if (inviteButtonClick.contains(touchPoint)){
 	      			for (int i = 0; i < invited.size(); i++) {
 	      				sfsClient.inviteRequest(invited.elementAt(i));
 	      			}
 	      			game.setScreen(multiplayerScreen);
 	      		} else if (moveInvited.contains(touchPoint)){
 	      			moveToUninvite();
 	      		} else if (moveNotInvited.contains(touchPoint)){
 	      			moveToInvite();
 	      		} 
 			}
 
 
 			return;
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
             batcher.draw(Assets.backGrey,0,0,1024,630);
             batcher.end();
 
             //Dibujando elementos en pantalla activamos el Blending
             batcher.enableBlending();
             batcher.begin();
             
             if (this.noFriendsOn){
             	 batcher.draw(Assets.noFriendsOnText, 0, 0);     	
             } else{
                 batcher.draw(Assets.inviteGameTitle, 320, 540);             
                 batcher.draw(Assets.inviteButton, 250,60); 
                               
                 drawInvited();
                 drawNotInvited();
             }
 
             batcher.draw(Assets.backButton, 540,60); 
 
             
             batcher.end();
             
             ConfirmScreen.getInstance().createConfirmIfNeeded();
             AcceptScreen.getInstance().createAcceptIfNeeded();
 
 	}
 
 	
 	private void moveToUninvite() {
 		for (int i = 0; i < Math.min(invited.size(), 5); i++){
 			if (invitedPlayer[i].contains(touchPoint)){
 				String player = invited.elementAt(i+invitedScroll);
 				invited.remove(i+invitedScroll);
 				notInvited.addElement(player);
 				if (4 + invitedScroll >= invited.size()) invitedScroll = Math.max(0, invitedScroll-1);
 			}
 		}
 	}
 
 
 	private void moveToInvite() {
 		for (int i = 0; i < Math.min(notInvited.size(), 5); i++){
 			if (notInvitedPlayer[i].contains(touchPoint)){
 				String player = notInvited.elementAt(i+notInvitedScroll);
 				notInvited.remove(i+notInvitedScroll);
 				invited.addElement(player);
 				if (4 + notInvitedScroll >= notInvited.size()) notInvitedScroll = Math.max(0, notInvitedScroll-1);
 			}
 		}
 		
 	}
 
 
 	private void drawNotInvited() {
 		batcher.draw(Assets.list, 160, 180); 
 		batcher.draw(Assets.availablePlayersText, 163, 407); 
 		
 		for (int i = 0; i < Math.min(notInvited.size(), 5); i++){
 			batcher.draw(Assets.add, 175, (368-45*i));
 			
 			String name = notInvited.elementAt(i+notInvitedScroll);
 			if (font.getBounds(name).width > 195){
 				int j = 6;
 				while (font.getBounds(name.substring(0, j)).width < 168) j++;
 				font.draw(batcher, name.substring(0, j)+"...", 230,(402-45*i));
 			} else font.draw(batcher, name, 230,(402-45*i));
 		}
 	}
 	
 	private void drawInvited() {
 		batcher.draw(Assets.list, 530, 180); 
 		batcher.draw(Assets.inviteListText, 590, 407); 
 		for (int i = 0; i < Math.min(invited.size(), 5); i++){
 			batcher.draw(Assets.minus, 550, (368-45*i));
 			
 			String name = invited.elementAt(i+invitedScroll);
 			if (font.getBounds(name).width > 195){
 				int j = 6;
 				while (font.getBounds(name.substring(0, j)).width < 168) j++;
 				font.draw(batcher, name.substring(0, j)+"...", 600,(402-45*i));
 			} else font.draw(batcher, name, 600,(402-45*i));
 		}
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
