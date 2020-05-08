 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package zed;
 
 // Java for exception handling
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 // Slick for creating game
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Music;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 
 
 
 // Slick for exception handling
 import org.newdawn.slick.SlickException;
 
 /**
  * @author Richard Barella Jr.
  * @author Adam Bennett
  * @author Ryan Slyter
  */
 public class Zed {
     // Main function
     public static void main(String[] args) throws SlickException {
         
         // create game
         org.newdawn.slick.BasicGame game = new org.newdawn.slick.BasicGame("zed") {
            
             // Test-level
             Level_Manager test;
             
             // Sounds
             Music music = new Music("soundtrack/kawfy/braintwoquart.wav");
             
             // Game Initialization
             @Override
             public void init(GameContainer gc) throws SlickException {
                 /*
                 // Initialize test-images
                 link_down_bot = new Image("images/link-down-bot.png",
                         false, Image.FILTER_NEAREST);
                 link_down_top = new Image("images/link-down-top.png",
                         false, Image.FILTER_NEAREST);
                 */
                 
                 // Initialize test-level
                 test = new Level_Manager();
                 
                 music.loop();
             }
 
             // Game Updates
             @Override
             public void update(GameContainer gc, int delta) throws SlickException {
             	 boolean left = false; //these arent currently being used be needed
                  boolean right = false;
                  boolean up = false;
                  boolean down = false;
                  gc.setVSync(true); // makes it so computer doesn't heat up
                  //gc.setTargetFrameRate(120);
                  
              	Input input = gc.getInput(); // get the current input
                  
                  if (input.isKeyDown(Input.KEY_UP) || input.isKeyDown(Input.KEY_W))
                  {
                      up = true;
                      //y_pos-=.05;
                  }
                  if (input.isKeyDown(Input.KEY_LEFT) || input.isKeyDown(Input.KEY_A))
                  {
                      left = true;
                      //x_pos-=.05;
                  }
                  if (input.isKeyDown(Input.KEY_DOWN) || input.isKeyDown(Input.KEY_S))
                  {
                      down = true;
                      //y_pos+=.05;
                  }
                  if (input.isKeyDown(Input.KEY_RIGHT) || input.isKeyDown(Input.KEY_D))
                  {
                      right = true;
                      //x_pos+=.05;
                  }
                  if (input.isKeyDown(Input.KEY_SPACE))
                  {
                 	 test.player.Start_Sword_Attack();
                  }
                  else
                  {
                 	 test.player.End_Sword_Attack();
                  }
                  // change the player's movement value
                  test.move_player((right? 1:0) - (left? 1:0),
                          (down? 1:0) - (up? 1:0));
                  
                  test.update();
             }
             
             // Game Rendering
             @Override
             public void render(GameContainer gc, Graphics g) throws SlickException {
                 
                 test.display(gc, g);
                 /*
                 g.drawImage(link_down_bot, x_pos, y_pos);
                 g.drawImage(link_down_top, x_pos, y_pos-16);
                 */
                 
                 // TODO: code render
                 
             }
         };
         
         AppGameContainer container;
         try {
             container = new AppGameContainer(game); // create game instance
             container.start();                      // start game instance
         } catch (SlickException ex) { // catch exceptions
             ex.printStackTrace();
             Logger.getLogger(Zed.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
