 package jam.ld27.game;
  
 import infinitedog.frisky.entities.Entity;
 import infinitedog.frisky.events.InputEvent;
 import infinitedog.frisky.game.ManagedGameState;
 import jam.ld27.sprites.Player;
 import jam.ld27.sprites.Heart;
 import jam.ld27.entities.Initiator;
 import jam.ld27.sprites.Dragon;
 import jam.ld27.sprites.Enemy;
 import jam.ld27.sprites.Knight;
 import jam.ld27.sprites.Wall;
 import jam.ld27.tilemap.MapGenerator;
 import jam.ld27.tilemap.TileMap;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 public class MainState extends ManagedGameState {
     private boolean paused = false;
     private Player player;
     private Knight knight;
     private TileMap tileMap;
     private Camera camera;
     private Dragon dragon;
     //Generation of enemies and hearts.
     private int nEnemies = 10;
     private int nHearts = 50;
     private Random r = new Random();
     //Animation
     private int contador = 0;
     private Initiator initiator;
     
     private int difficulty;
     private boolean musicOn = false;
     
     private boolean ending = false;
     private int endingTimer = 0;
         
     public MainState(int stateID)
     {
         super(stateID);
         em.setGameState(C.States.MAIN_STATE.name);
     }
 
     @Override
     public void init(GameContainer gc, StateBasedGame game) throws SlickException {
         em.setGameState(C.States.MAIN_STATE.name);
         //Bind events
         evm.addEvent(C.Events.MOVE_LEFT.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_LEFT));
         evm.addEvent(C.Events.MOVE_RIGHT.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_RIGHT));
         evm.addEvent(C.Events.CLOSE_WINDOW.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_ESCAPE));
         evm.addEvent(C.Events.SOUND_OFF.name, new InputEvent(InputEvent.KEYBOARD, Input.KEY_M, 1000));
         
         //Load Textures
         tm.addTexture(C.Textures.CASTLE.name, C.Textures.CASTLE.path);
         tm.addTexture(C.Textures.TILE_SET.name, C.Textures.TILE_SET.path);
         tm.addTexture(C.Textures.ENEMY.name, C.Textures.ENEMY.path);
         tm.addTexture(C.Textures.KNIGHT_SET.name, C.Textures.KNIGHT_SET.path);
         tm.addTexture(C.Textures.PRINCESS_SET.name, C.Textures.PRINCESS_SET.path);
         tm.addTexture(C.Textures.CONCRETE_WALL_SET.name, C.Textures.CONCRETE_WALL_SET.path);
         tm.addTexture(C.Textures.FRAGILE_WALL_SET.name, C.Textures.FRAGILE_WALL_SET.path);
         tm.addTexture(C.Textures.BLOWING_WALL_SET.name, C.Textures.BLOWING_WALL_SET.path);
         tm.addTexture(C.Textures.DRAGON_SET.name, C.Textures.DRAGON_SET.path);
         tm.addTexture(C.Textures.HEART.name, C.Textures.HEART.path);
         //Load Sounds
         sm.addMusic(C.Sounds.MUSIC.name, C.Sounds.MUSIC.path);
         sm.addSound(C.Sounds.MUERTE.name, C.Sounds.MUERTE.path);
         sm.addSound(C.Sounds.HEART.name, C.Sounds.HEART.path);
 
         tileMap = new TileMap(200, 25, C.Textures.TILE_SET.name, 32);
         camera = new Camera(tileMap);
         player = new Player();
         knight = new Knight(tileMap.getHeight());
         initiator = new Initiator(tileMap.getHeight());
         em.addEntity(C.Entities.HEART.name, new Heart(400, 800));
         em.addEntity(C.Entities.ENEMY.name+"yoshi=motherfucker", new jam.ld27.sprites.Enemy(400, 400, C.Entities.ENEMY.name+"yoshi=motherfucker"));
         em.addEntity(C.Entities.PLAYER.name, player);     
         em.addEntity(C.Entities.KNIGHT.name, knight);
         
         dragon = new Dragon();
     }
     
     @Override
     public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
         em.setGameState(C.States.MAIN_STATE.name);
 
         g.pushTransform();        
         g.translate(-camera.getOffsetX(), -camera.getOffsetY());
         Image castle = tm.getTexture(C.Textures.CASTLE.name);
         tileMap.render(gc, g);
         g.drawImage(castle, 0, tileMap.getY() + tileMap.getHeight() - castle.getHeight());
         em.render(gc, g);
         g.popTransform();
         
         g.drawString("Score: " + player.getScore(), 0, 0);
         if(contador <= 601) {
             initiator.render(gc, g);
             dragon.render(gc, g);
         }
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
         em.setGameState(C.States.MAIN_STATE.name);
         if(contador > 601) {
             if (ending) {
                 endingTimer += delta;
                 if (endingTimer > 1500) {
                     gameOver(gc, game);
                 }
             } else {
                 evm.update(gc, delta);
                 camera.update(gc, delta);
                 
                 checkEnemiesCollision(gc, game, delta);
                 checkPlayerCollision(gc, game);
 
                 if (player.isDead()) {
                     gameOver(gc, game);
                 }
 
                 em.update(gc, delta);
             }
         } else if(contador == 601) {
             contador++;
             camera.follow(player);
         } else {
             contador++;
             initiator.update(gc, delta);
             dragon.update(gc, delta);
             camera.update(gc, delta);
         }
         
         if(evm.isHappening(C.Events.CLOSE_WINDOW.name, gc)) {
             gc.exit();
         }
         
         if(evm.isHappening(C.Events.SOUND_OFF.name, gc)) {
             if(musicOn)
                 sm.getMusic(C.Sounds.MUSIC.name).stop();
             else
                 sm.playMusic(C.Sounds.MUSIC.name);
             musicOn = !musicOn;
         }
         if(!((Music)sm.getMusic(C.Sounds.MUSIC.name)).playing() && musicOn) {
             sm.playMusic(C.Sounds.MUSIC.name);
         }
     }
 
     void restart() {
         em.setGameState(C.States.MAIN_STATE.name);
         player.respawn();
         contador = 0;
         initiator.setH(tileMap.getHeight());
         initiator.setFrames(0);
         camera.follow(initiator);
        knight.setFrame(0);
         
         initEnemies();
         initWalls();   
         initHearts();
     }
     
     private void checkEnemiesCollision(GameContainer gc, StateBasedGame game, int delta) {
         ArrayList<Entity> enemies = (ArrayList<Entity>) em.getEntityGroup(C.Groups.ENEMIES.name);
         Iterator it = enemies.iterator();
         
         while(it.hasNext()) {
             Enemy enemy = (Enemy) it.next();
             float x = enemy.getNextStep(delta);
             float width = enemy.getWidth();
             
             if ((x + width) > (tileMap.getX() + tileMap.getWidth()) || (x < 0)) {
                 enemy.changeDirection();
             }
             
             if (enemy.isActive() && enemy.collideWith(player)){
                 player.lossHp();
                 player.setCrying(0);
                 enemy.setActive(false);
                 return;
             }
         }
     }
 
     private void checkPlayerCollision(GameContainer gc, StateBasedGame game) {
         float px = player.getX(), py = player.getY();
         
         if (px < 0) {
             player.setPosition(new Vector2f(0, py));
         } else {
             if((px + player.getWidth()) > (tileMap.getX() + tileMap.getWidth())) {
                 player.setPosition(new Vector2f(tileMap.getX() + tileMap.getWidth() - player.getWidth(), py));
             }
         }
         
         ArrayList<Entity> walls = (ArrayList<Entity>) em.getEntityGroup(C.Groups.WALLS.name);
         Iterator it = walls.iterator();
         while(it.hasNext()) {
             Wall wall = (Wall) it.next();
             if (wall.collideWith(player)) {
                 if (wall.isDestroyable()) {
                     em.removeEntity(wall.getName());
                 }
                 return;
             }
         }
         
         for(Entity e: (ArrayList<Entity>) em.getEntityGroup(C.Groups.HEARTS.name)) {
             Heart heart = (Heart) e;
             if(heart.isActive()) {
                 heart.checkCollision(player);
             }
         }
         
         if (player.collideWith(knight)) {
             player.saved();
             ending = true;
             knight.setFrame(2);
             knight.stopAnimation();
         } else if (player.collideWithFloor(tileMap)) {
             player.die();
         }
     }
     
     private void gameOver(GameContainer gc, StateBasedGame game) {
        ((GameOverState)game.getState(C.States.GAME_OVER_STATE.value)).setScore(player.getScore());
        game.enterState(C.States.GAME_OVER_STATE.value, new FadeOutTransition(), new FadeInTransition());
     }
 
     private void initEnemies() {
         em.removeEntityGroup(C.Groups.ENEMIES.name);
         em.forceRemoval();
         
         int i;
         for (i = 0; i < nEnemies; i += 1) {
             float x = r.nextFloat() * (tileMap.getWidth() - tileMap.getTileSize());
             float y = r.nextFloat() * (tileMap.getHeight() - tileMap.getTileSize());
             em.addEntity(C.Entities.ENEMY.name + i, new Enemy(x, y, C.Entities.ENEMY.name + i));
         }
     }
     
     private void initHearts() {
         em.removeEntityGroup(C.Groups.HEARTS.name);
         em.forceRemoval();
         
         for(int i = 0; i < nHearts; i++) {
             float x = r.nextFloat() * (tileMap.getWidth() - tileMap.getTileSize());
             float frac = (tileMap.getHeight() - tileMap.getTileSize())/nHearts;
             float y = r.nextFloat() * frac + i*frac;
             em.addEntity(C.Entities.HEART.name + i, new Heart(x, y));
         }
     }
     
     public void setDifficulty(int d) {
         difficulty = d;
     }
     
     private void initWalls() {
         MapGenerator mapGenerator = new MapGenerator(em, tileMap, difficulty);
         
         mapGenerator.generateWalls();
     }
 }
