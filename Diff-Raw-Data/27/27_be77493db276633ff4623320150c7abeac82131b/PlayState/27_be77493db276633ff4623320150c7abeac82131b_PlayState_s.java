 package org.gdc.gdcalaga;
 import java.util.LinkedList;
 import java.util.List;
 import org.gdc.gdcalaga.audio.AudioAsset;
 import org.gdc.gdcalaga.audio.AudioManager;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.geom.Vector2f;
 
 public class PlayState extends BasicGameState {
 	public static final int ID = 0;
 
     private boolean paused = false;
     private Player player;
 
     private EntityManager entities = new EntityManager();
     private Input input;
 
     private AudioManager audioManager = AudioManager.getAudioManager();
     
     private List<DisplayObject> disObjs = new LinkedList<DisplayObject>();
     
     PathRegistry paths;
     
     @Override
 	public void init(GameContainer container, StateBasedGame game) throws SlickException {
         input = container.getInput();
         Vector2f startPosition = new Vector2f(50, 300);
         player= new Player(entities, startPosition);
         
         audioManager.loadAudioAssets();
         audioManager.playMusic(AudioAsset.MUSIC_LEVEL_1);
         
         paths = new PathRegistry();
         paths.loadFromJson("./data/paths.json");
         
         disObjs.add(new Background(container.getWidth(), container.getHeight()));
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
 		for (DisplayObject obj : disObjs) {
         	obj.draw(g);
         }
 		entities.draw(g);
 		
 		/* If we use this same monospace font, each letter takes up 10 pixels
 		 * so we can space the text according to this metric
 		 */
 		final int desiredSpacing = 15;    //Spacing in pixels between each statistic
 		
 		String playerHP = new String("Player HP: " + player.getHealth());
 		int xPixel = 5;
         g.drawString(playerHP, xPixel, 30);
         
         String shieldsLeft = new String("Player Shields: " + player.getShields());
         xPixel = xPixel + (playerHP.length() * 10) + desiredSpacing;
         g.drawString(shieldsLeft, xPixel, 30);
         
         String waveCount = new String("Round: " + Spawn.getWave());
         xPixel = xPixel + (shieldsLeft.length() * 10) + desiredSpacing;
         g.drawString(waveCount, xPixel, 30);
         
         String pointCount = new String("Points " + Player.getTotalPoints());
         xPixel = xPixel + (waveCount.length() * 10) + desiredSpacing;
         g.drawString(pointCount, xPixel, 30);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
 		
 		if(!paused){
 			
 	        Collision.checkCollisions(entities.getEntities());
 	        
 	        Spawn.spawnWave(paths, entities);
 	        
             entities.update(delta);
             
             if( ( Math.random()*100) < .1  )
             {
             	((Background)disObjs.get(0)).addRandomFloater();
             }
             
             if(input.isKeyDown(Input.KEY_W)){
                 player.moveUp(delta);
             }
             if(input.isKeyDown(Input.KEY_S)){
                 player.moveDown(delta);
             }
             if(input.isKeyDown(Input.KEY_A)){
                 player.moveLeft(delta);
             }
             if(input.isKeyDown(Input.KEY_D)){
                 player.moveRight(delta);
             }
             
             if(input.isKeyDown(Input.KEY_SPACE))
             {
                 if (player.fire(delta))
                 {
                     audioManager.playSFX(AudioAsset.SFX_FIRE1);
                 }
             }
             
             if(input.isKeyDown(Input.KEY_Z))
             {
                 if (player.activateShield(delta))
                 {
                     //audioManager.playSFX(AudioAsset.SFX_SHIELD1);
                     //should we play a sound, or just show a graphic?
                 }
             }
             
             for (DisplayObject obj : disObjs) {
             	obj.update(delta);
             }
 		}
 		
         
         if(input.isKeyPressed(Input.KEY_ESCAPE))
         {
             paused=!paused;
         }
 		
 
 		/* will put this back in later
         if(Input.WasKeyPressed(KeyEvent.VK_H))
         {
             screenShot();
         }
         */
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 }
