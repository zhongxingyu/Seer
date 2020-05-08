 package com.codefuss;
 
 
 import com.codefuss.actions.Attack;
 import com.codefuss.actions.JumpAction;
 import com.codefuss.actions.MoveLeft;
 import com.codefuss.actions.MoveRight;
 import com.codefuss.actions.StopAction;
 import com.codefuss.entities.Player;
 import com.codefuss.physics.Body;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.BasicGameState;
 
 import java.util.ArrayList;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  *
  * @author Martin Vium <martin.vium@gmail.com>
  */
 public class GameState extends BasicGameState {
 
     static public final int ID = 1;
     static final int PLAYER_WIDTH = 128;
     
     GameFactory gameFactory;
     ArrayList<Entity> entities = new ArrayList<Entity>();
     Player player;
     float offsetX = 0;
     Body ground;
 
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
         initGround();
 
         container.setMaximumLogicUpdateInterval(100);
         container.setDefaultFont(gameFactory.getLabelFont());
     }
 
     void initPlayer() {
         player = gameFactory.getEntityFactory().getPlayer(new Vector2f(0, 0));
         gameFactory.getInputManager().setDefaultAction(new StopAction(player));
         gameFactory.getInputManager().mapToKey(new Attack(player), Input.KEY_SPACE);
         gameFactory.getInputManager().mapToKey(new MoveLeft(player), Input.KEY_LEFT);
         gameFactory.getInputManager().mapToKey(new MoveRight(player), Input.KEY_RIGHT);
        gameFactory.getInputManager().mapToKey(new JumpAction(player), Input.KEY_SPACE);
         entities.add(player);
     }
 
     void initGround() {
         ground = gameFactory.getPhysicsFactory().getStaticBox(0, 300, 640, 10);
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) {
         // run action and apply any resulting entities to stack
         for(Entity e : gameFactory.getInputManager().getAction().invoke()) {
             entities.add(e);
         }
 
         // update all entities
         for(Entity e : entities) {
             e.update(container, game, delta);
         }
 
         gameFactory.getPhysicsFactory().getWorld().update(delta);
 
         // calculate screen offset
         offsetX = player.getX() - (container.getWidth() / 2) + (player.getWidth() / 2);
         offsetX = getNormalizedOffset(container, offsetX);
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
         gameFactory.getMap().render(-offsetX, 0);
         for(Entity e : entities) {
             e.render(container, game, g, offsetX);
         }
 
         gameFactory.getPhysicsFactory().getWorld().render(g);
     }
 
     float getNormalizedOffset(GameContainer container, float offset) {
         if(offset < 0) {
             offset = 0;
         }
 
         if(offset > gameFactory.getMap().getWidth() - gameFactory.getMap().getTiledMap().getTileWidth() - container.getWidth()) {
             offset = gameFactory.getMap().getWidth() - gameFactory.getMap().getTiledMap().getTileWidth() - container.getWidth();
         }
 
         return offset;
     }
 }
