 package demo;
 
 import game.AdvancedPlayField;
 import game.KeepLeftFirstPlayerGameScroller;
 import items.CollectibleInstantItem;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.util.List;
 import keyconfiguration.Key;
 import keyconfiguration.KeyConfig;
 import mario.Goomba;
 import mario.Mario;
 import setting.BasePlatform;
 import setting.BreakableDecorator;
 import setting.ItemDecorator;
 import setting.Platform;
 import charactersprites.NPC;
 import charactersprites.Player;
 import charactersprites.ai.MoveState;
 import charactersprites.ai.PatrolState;
 import collision.CharacterPlatformCollision;
 import collision.GameElementCollision;
 import collision.PlayerCollectibleItemCollision;
 import com.golden.gamedev.Game;
 import com.golden.gamedev.GameLoader;
 import com.golden.gamedev.object.background.ColorBackground;
 
 /**
  * @author Eric Mercer (JacenLakiir)
  * 
  * For testing NPC AI
  */
 public class DemoGame4 extends Game{
 
     private List<Key>           keyList;
     private AdvancedPlayField   myPlayfield;  
 
     @Override
     public void initResources()
     { 
         myPlayfield = new AdvancedPlayField(10000, 500);
         myPlayfield.setGameScroller(new KeepLeftFirstPlayerGameScroller());
         myPlayfield.setBackground(new ColorBackground(Color.gray, 640, 480));
         
         Player mario = new Mario(this);
         keyList = new KeyConfig(mario, this).getKeyList();
         mario.setImages(this.getImages("resources/Mario1.png", 1, 1));
         mario.setLocation(25, 20);
         myPlayfield.addPlayer(mario);
         
         NPC goomba1 = new Goomba(this);
         goomba1.setImages(this.getImages("resources/Goomba.png", 1, 1));
        goomba1.setLocation(325, 20);
         goomba1.setMovable(true);
         myPlayfield.addCharacter(goomba1);
         
         NPC goomba2 = new Goomba(this);
         goomba2.addPossibleState(new PatrolState(goomba2, 75));
         goomba2.setImages(this.getImages("resources/Goomba.png", 1, 1));
         goomba2.setLocation(500, 20);
         goomba2.setMovable(true);
         myPlayfield.addCharacter(goomba2);
         
         NPC goomba3 = new Goomba(this);
         goomba3.addPossibleState(new MoveState(goomba3, 1, true));
         goomba3.setImages(this.getImages("resources/Goomba.png", 1, 1));
        goomba3.setLocation(275, 20);
         goomba3.setMovable(true);
         myPlayfield.addCharacter(goomba3);
         
         Platform floor = new BasePlatform(this);
         floor.setImages(this.getImages("resources/Bar.png", 1, 1));
         floor.setLocation(0, 440);
         myPlayfield.addSetting(floor);
         
         Platform ceiling = new BasePlatform(this);
         ceiling.setImages(this.getImages("resources/Bar.png", 1, 1));
         ceiling.setLocation(70, -20);
         myPlayfield.addSetting(ceiling);
 
         CollectibleInstantItem coin = new CollectibleInstantItem(this);
         coin.setImages(this.getImages("resources/Coin.png", 1, 1));
         coin.setActive(false);
         coin.setValue(3);
         myPlayfield.addItem(coin);
         
         ItemDecorator block1 = new ItemDecorator(new BasePlatform(this));
         block1.setMass(6);
         block1.setMovable(false);
         block1.setImages(this.getImages("resources/Block1.png", 1, 1));
         block1.setLocation(100, 200);
         block1.addItem(coin);
         myPlayfield.addSetting(block1);
 
         Platform block2 = new BreakableDecorator(new BasePlatform(this));
         block2.setMass(6);
         block2.setMovable(false);
         block2.setImages(this.getImages("resources/Block2.png", 1, 1));
         block2.setLocation(160, 200);
         myPlayfield.addSetting(block2);
         
         Platform wall1 = new BasePlatform(this);
         wall1.setImages(this.getImages("resources/Wall.png", 1, 1));
         wall1.setLocation(0, 0);
         myPlayfield.addSetting(wall1);
         
         Platform wall2 = new BasePlatform(this);
         wall2.setImages(this.getImages("resources/Wall.png", 1, 1));
         wall2.setLocation(620, 0);
         myPlayfield.addSetting(wall2);
         
         myPlayfield.addCollisionGroup(myPlayfield.getPlayers(),
                                       myPlayfield.getItems(),
                                       new PlayerCollectibleItemCollision());
         
         // currently no way to check all characters against setting in one collision
         
         // tried adding 'mario' to player and character spritegroups and then
         // checking characters against setting, but physics didn't work
         myPlayfield.addCollisionGroup(myPlayfield.getPlayers(),
                                       myPlayfield.getSetting(),
                                       new CharacterPlatformCollision());
         myPlayfield.addCollisionGroup(myPlayfield.getCharacters(),
                                       myPlayfield.getSetting(),
                                       new CharacterPlatformCollision());
         
        // same flaws as the two collisions above and uses GameElementCollision
        // since no PlayerCharacterCollision or CharacterCharacterCollision exists
         myPlayfield.addCollisionGroup(myPlayfield.getPlayers(),
                                       myPlayfield.getCharacters(),
                                       new GameElementCollision());
         myPlayfield.addCollisionGroup(myPlayfield.getCharacters(),
                                       myPlayfield.getCharacters(),
                                       new GameElementCollision());
     }
 
     @Override
     public void render (Graphics2D g)
     {
         myPlayfield.render(g);
     }
 
     @Override
     public void update (long t)
     {
         myPlayfield.update(t);
         checkKeyboardInput(t);
     }
     
     private void checkKeyboardInput (long milliSec)
     {
         for (Key key : keyList)
             if (key.isKeyDown(milliSec))
                 key.notifyObserver();
     }
     
     public static void main (String[] args)
     {
         GameLoader game = new GameLoader();
         game.setup(new DemoGame4(), new Dimension(640,480), false);
         game.start();
     }
 
 }
