 /**
  * @author Glenn Rivkees (grivkees)
  */
 
 package game;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import setting.BasePlatform;
 import setting.BreakableDecorator;
 import setting.Platform;
 
 import keyconfiguration.KeyConfig;
 import mario.Mario;
 
 import charactersprites.Player;
 import collision.CharacterPlatformCollision;
 
 import com.golden.gamedev.Game;
 import com.golden.gamedev.GameLoader;
 import com.golden.gamedev.object.background.ColorBackground;
 
 public class DemoGame3 extends Game {
 	
     private AdvancedPlayField myPlayfield;
 	
     public void initResources ()
     {
     	// Playfield Init
     	myPlayfield = new AdvancedPlayField(10000, 500);
     	myPlayfield.setBackground(new ColorBackground(Color.gray));
     	myPlayfield.setGameScroller(new CenterFirstPlayerGameScroller());
 
         // Collisions
         myPlayfield.addCollisionGroup(myPlayfield.getPlayers(),
                                       myPlayfield.getSetting(),
                                       new CharacterPlatformCollision());
         
         // Sprite Init / Or load funcitonality
         
         Player temp = new Mario(this);
         temp.setKeyList(new KeyConfig(temp,this).getInputKeyList());
         temp.setImages(this.getImages("resources/Mario1.png", 1, 1));
         temp.setLocation(25, 20);
         myPlayfield.addPlayer(temp);
         
         Platform temp1 = new BasePlatform(this);
         temp1.setImages(this.getImages("resources/Bar.png", 1, 1));
         temp1.setLocation(0, 440);
         myPlayfield.addSetting(temp1);
         
         Platform temp2 = new BasePlatform(this);
         temp2.setImages(this.getImages("resources/Bar.png", 1, 1));
        temp2.setLocation(600, 440);
         myPlayfield.addSetting(temp2);
         
         Platform block2 = new BreakableDecorator(new BasePlatform(this));
         block2.setMass(6);
         block2.setMovable(false);
         block2.setImages(this.getImages("resources/Block2.png", 1, 1));
         block2.setLocation(160, 200);
         myPlayfield.addSetting(block2);
 
     }
 
 
     public void update (long arg0)
     {
         myPlayfield.update(arg0);
     }
 
 
     public void render (Graphics2D arg0)
     {
         myPlayfield.render(arg0);
     }
     
     public static void main(String[] args) {
         GameLoader game = new GameLoader();
         game.setup(new DemoGame3(), new Dimension(640,480), false);
         game.start();
     }
 
 }
