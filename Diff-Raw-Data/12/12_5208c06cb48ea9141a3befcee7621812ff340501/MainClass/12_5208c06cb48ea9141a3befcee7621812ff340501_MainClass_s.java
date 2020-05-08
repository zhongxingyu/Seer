 package game;
 import camera.Camera;
 import entity.CameraFocusEntity;
 import entity.Entity;
 import entity.Player;
 import entity.Wall;
 import frame.GameFrame;
 
 import java.awt.*;
 
 public class MainClass {
     static GameFrame frame;
     static Game game;
 
     public static void main(String args[]) {
         Game BallGame = new Game();
 
         Entity player1 = new Player(1);
         player1.setxPos(100);
         player1.addToList();
         Entity player2 = new Player(2);
         player2.setxPos(-100);
         player2.addToList();
 
         new Wall(-520,380,520,360).addToList();
         new Wall(-520,-380,520,-360).addToList();
         new Wall(-520,380,-500,-380).addToList();
         new Wall(520,380,500,-380).addToList();
 
         game = new Game();
         frame = new GameFrame("Ball Game",game);
         frame.setSize(1024,768);
         frame.setLocationRelativeTo(null);
     }
 }
