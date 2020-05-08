 package qahweh.almostdarkness;
 
 import qahweh.almostdarkness.ui.UserInterface;
 import qahweh.almostdarkness.ui.UI1;
 import qahweh.almostdarkness.ui.UserInterfaceCallBack;
 import qahweh.almostdarkness.gamelogic.Game;
 import qahweh.almostdarkness.gamelogic.GameCallBack;
 import qahweh.almostdarkness.gamelogic.piece.PieceI;
 
 import java.util.List;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 
 /*
     STUFF TO DO?
     // lose sanity all the time. Will force player to play game more quick and then can not wait for sneak pass ALL enemies
     // TNT. good to go through walls
     // Lights, will make a ray cast longer if pass light
     // See pass solid blocks magic
     // Silencer on gun magic
     //If enemy die. make new enemy elsewhere? This will make so game cannot be solved by attacking instead of sneak (not sure of this yeT)
     //mouse click when go.
     // Stand still enemys. if walk around to much might make game to easy. Just wait til all is gone.
     // the eye view is offset when walk. fix order on eye and update position logic.
 
 */
 
 public class AlmostDarkness implements GameCallBack, UserInterfaceCallBack
 {
     private UserInterface ui;
     private Game game;
     private int screenWidth = 85;
     private int screenHeight = 26;
 
     private AlmostDarkness()
     {
         ui = new UI1();
         ui.setCallBack(this);
         game = new Game();
         game.setCallBack(this);
     }
 
     private void run()
     {
         ui.start();
         try
         {
             game.start();
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         ui.stop();
     }
 
     public static void runGame()
     {
         AlmostDarkness ad = new AlmostDarkness();
         ad.run();
     }
     
 
     public static void main(String[] args)
     {
         runGame();
     }
 
     @Override
     public void refresh(Game game)
     {
         if(false)return;
 
         char[][] draw = new char[screenWidth][screenHeight];
 
         for(int x=0; x<screenWidth; x++)
         {
             for(int y=0; y<screenHeight; y++)
             {
                 draw[x][y] = ( game.canSee(x+game.cameraX,y+game.cameraY) ? game.world.matris[x+game.cameraX][y+game.cameraY] : ' ');
             }
         }
 
         List keys = new ArrayList(game.piecePositions.keySet());
         for(int i=0; i<keys.size(); i++)
         {
             PieceI o = (PieceI)keys.get(i);
             Point p = game.piecePositions.get(o);
             int x = p.x-game.cameraX;
             int y = p.y-game.cameraY;
             if(x>-1 && y>-1 && x<screenWidth && y<screenHeight)
             {
                 if(game.canSee( p.x,p.y))
                     draw[x][y] = game.pieceCharacters.get(o);
             }
         }
 /*
         for(int i=0; i<game.piecePositions.size(); i++)
         {
             Point pos = game.piecePositions.get(i)
         }
 */
         String output = "";
         for(int y=0; y<screenHeight; y++)
         {
             for(int x=0; x<screenWidth; x++)
             {
                 output += draw[x][y]; 
             }
             output += "\n";
         }
         ui.draw(output);
         
 
 /*        
 
         }*/
     }
 
     @Override
    public void keyPressed(int k) throws Exception
     {
         game.input(k);
     }
 }
