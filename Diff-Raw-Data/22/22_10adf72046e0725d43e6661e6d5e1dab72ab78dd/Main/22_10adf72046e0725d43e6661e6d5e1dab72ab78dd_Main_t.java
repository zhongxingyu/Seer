 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package projetx;
 
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

 /**
  *
  * @author jonas
  */

 public class Main {
 
     /**
      * @param args the command line arguments
      */

     public static void main(String[] args) throws SlickException
     {

         Game theGame = new Game(null);
          AppGameContainer app = new AppGameContainer(new Game( Game.title));
          app.setDisplayMode(Game.width, Game.height, Game.fullscreen);
          app.setSmoothDeltas(true);
          app.setTargetFrameRate( Game.fpslimit);
          app.setShowFPS( Game.showFPS);
          app.start();
     }
 
 }
