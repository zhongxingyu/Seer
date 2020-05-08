 package com.codefuss;
 
 
 import com.codefuss.actions.Attack;
 import com.codefuss.actions.MoveLeft;
 import com.codefuss.actions.MoveRight;
 import com.codefuss.actions.StopAction;
 import com.codefuss.entities.Player;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.BasicGameState;
 
 import java.util.ArrayList;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.util.Log;
 
 /**
  *
  * @author Martin Vium <martin.vium@gmail.com>
  */
 public class GameState extends BasicGameState {
 
     static public final int ID = 1;
     
     GameFactory gameFactory;
     ArrayList<Entity> entities = new ArrayList<Entity>();
     Player player;
     float offsetX = 0;
 
     @Override
     public int getID() {
         return ID;
     }
 
     @Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
         gameFactory = new GameFactory(Game.getProperties(), container.getInput());
         gameFactory.getMap().initBlockEntities(entities);
         gameFactory.getMap().initCreatureEntities(entities);
 
         initPlayer();
 
         container.setMaximumLogicUpdateInterval(100);
         container.setDefaultFont(gameFactory.getLabelFont());
     }
 
     void initPlayer() {
         player = gameFactory.getEntityFactory().getPlayer(new Vector2f(0, 0));
         gameFactory.getInputManager().setDefaultAction(new StopAction(player.getSprite()));
         gameFactory.getInputManager().mapToKey(new Attack(player.getSprite()), Input.KEY_SPACE);
         gameFactory.getInputManager().mapToKey(new MoveLeft(player.getSprite()), Input.KEY_LEFT);
         gameFactory.getInputManager().mapToKey(new MoveRight(player.getSprite()), Input.KEY_RIGHT);
         entities.add(player);
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) {
         gameFactory.getInputManager().getAction().invoke();
         for(Entity e : entities) {
             e.update(container, game, delta);
         }
         
        offsetX = player.getSprite().getX() - (container.getWidth() / 2) + 128;
         if(offsetX < 0) {
             offsetX = 0;
         }
 
         if(offsetX > gameFactory.getMap().getWidth() - gameFactory.getMap().getTiledMap().getTileWidth() - container.getWidth()) {
             offsetX = gameFactory.getMap().getWidth() - gameFactory.getMap().getTiledMap().getTileWidth() - container.getWidth();
         }
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
         gameFactory.getMap().render(-offsetX, 0);
         for(Entity e : entities) {
             e.render(container, game, g, offsetX);
         }
     }
 }
