 package com.mel.wallpaper.starWars.process;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.shape.RectangularShape;
 import org.andengine.entity.shape.Shape;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.color.Color;
 import org.andengine.util.debug.Debug;
 
 import android.content.Context;
 import android.widget.Toast;
 
 import com.mel.entityframework.Game;
 import com.mel.entityframework.Process;
 import com.mel.util.Point;
 import com.mel.wallpaper.starWars.StarWarsLiveWallpaper;
 import com.mel.wallpaper.starWars.entity.Bubble;
 import com.mel.wallpaper.starWars.entity.InvisibleWalls;
 import com.mel.wallpaper.starWars.entity.JediKnight;
 import com.mel.wallpaper.starWars.entity.Jumper;
 import com.mel.wallpaper.starWars.entity.LaserBeam;
 import com.mel.wallpaper.starWars.entity.Map;
 import com.mel.wallpaper.starWars.entity.Shooter;
 import com.mel.wallpaper.starWars.entity.Walker;
 import com.mel.wallpaper.starWars.entity.commands.BubbleCommand;
 import com.mel.wallpaper.starWars.entity.commands.MoveCommand;
 import com.mel.wallpaper.starWars.entity.commands.ShootLaserCommand;
 import com.mel.wallpaper.starWars.settings.GameSettings;
 import com.mel.wallpaper.starWars.sound.SoundAssets;
 import com.mel.wallpaper.starWars.sound.SoundAssets.Sample;
 import com.mel.wallpaper.starWars.view.SpriteFactory;
 
 public class TouchProcess extends Process implements IOnSceneTouchListener
 {
 	private Game game;
 	private Map partido;
 	private List<Walker> jedis;
 	private Scene scene;
 	private Context toastBoard;
 	private RectangularShape touchMarker;
 	private TouchEvent lastTouch;
 	
	private int TOUCH_RATIO = 50;
 	
 	public TouchProcess(Game game, Scene scene, Context context){
 		this.game = game;
 		this.scene = scene;
 		this.toastBoard = context;
 		this.touchMarker = SpriteFactory.getInstance().newBall(6, 6);
 		this.touchMarker.setColor(Color.RED);
 	}
 	
 	
 	@Override
 	public void onAddToGame(Game game){
 		//inicializar listeners (touch, accelerometer?, keyboard?)
 		scene.setTouchAreaBindingOnActionDownEnabled(true);
 		scene.setOnSceneTouchListener(this);
 		
 		getEntitiesFromGame(game);
 	}
 	
 	public void getEntitiesFromGame(Game game) {
 		this.partido = (Map)game.getEntity(Map.class);
 
 		this.jedis = (List<Walker>) game.getEntities(Jumper.class);
 		this.jedis.addAll(game.getEntities(Walker.class));
 		this.jedis.addAll(game.getEntities(Shooter.class));
 		this.jedis.addAll(game.getEntities(JediKnight.class));
 	}
 	
 	@Override
 	public void onRemoveFromGame(Game game){
 		scene.setTouchAreaBindingOnActionDownEnabled(false);
 		scene.setOnSceneTouchListener(null);
 		
 	}
 	
 	@Override
 	public void update(){
 
 		getEntitiesFromGame(game);
 		
 		if(this.lastTouch != null){
 			processLastTouch(this.lastTouch);
 			this.lastTouch = null;
 		}
 		
 	}
 	
 	
 	public boolean processLastTouch(TouchEvent touchEvent) {
 
 		//printTouchDebuger(touchEvent);		
 
 		if(GameSettings.getInstance().godsFingerEnabled){
 			List<Walker> touchedPlayers = getPlayersUnderTouch(touchEvent, TOUCH_RATIO);
 
 			if(touchedPlayers.size() > 0){
 				
 				Walker walker=null;
 				
 				for(Walker touchedWalker:touchedPlayers){
 					if(walker==null || walker.getPosition().getY() < touchedWalker.getPosition().getY())
 						walker = touchedWalker;
 				}
 				
 				if(walker!=null)
 				{
 					Debug.d("Touch player " + walker);
 					
 					BubbleCommand moveb = new BubbleCommand(walker,game);
 					walker.addCommand(moveb);
 				}
 				
 				return true;
 			}
 		}
 		
 //		SoundLibrary.playSample(Sample.LASER);
 		
 		return false;
 	}
 	
 	private List<Walker> getPlayersUnderTouch(TouchEvent pSceneTouchEvent, int touchRatio){
 		ArrayList<Walker> touchedPlayers = new ArrayList<Walker>();
 		Point spriteCenter = null;
 		for(Walker p:this.jedis){
 			spriteCenter = new Point(p.sprite.getSceneCenterCoordinates());
 			if(spriteCenter.distance(pSceneTouchEvent.getX(), pSceneTouchEvent.getY()) < TOUCH_RATIO){
 				touchedPlayers.add(p);
 			}
 		}
 		
 		return touchedPlayers;
 	}
 	
 	
 	private void printTouchDebuger(TouchEvent touchEvent){
 		if(this.touchMarker != null){
 			if(!this.touchMarker.hasParent()){
 				//map.field.background.attachChild(this.touchMarker);
 				this.scene.attachChild(this.touchMarker);
 			}
 			
 			//float[] pointOnField = map.field.background.convertSceneToLocalCoordinates(touchEvent.getX(), touchEvent.getY());
 			float[] pointOnField = {touchEvent.getX(), touchEvent.getY()};
 			
 			this.touchMarker.setPosition(pointOnField[0]-touchMarker.getRotationCenterX(), pointOnField[1]-touchMarker.getRotationCenterY());
 		}
 		
 	}
 	
 	
 	
 	
 	/* TOUCH LISTENERS */
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		pSceneTouchEvent.set(pSceneTouchEvent.getX(), pSceneTouchEvent.getY()+10);
 		if(this.partido.status == Map.Status.PLAYING){
 			this.lastTouch = pSceneTouchEvent;
 			return true;
 		}else{
 			//Toast.makeText(this.toastBoard, "Be gentle, let the players catch their breath!", Toast.LENGTH_LONG).show();
 			//HAY QUE ENCONTRAR COMO PINTAR EL TEXTO EN PANTALLA!!
 			return false;
 		}
 	}
 	
 	
 	
 
 }
