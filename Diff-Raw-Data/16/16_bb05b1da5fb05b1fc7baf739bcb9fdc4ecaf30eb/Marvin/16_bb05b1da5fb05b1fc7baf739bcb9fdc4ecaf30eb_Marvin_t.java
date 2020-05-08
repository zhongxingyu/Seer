 // Robot
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 // Client
 import java.rmi.* ;
 import java.net.MalformedURLException ;
 import java.util.*;
 
 import IA.KeySender;
 import IA.Eval;
 import IA.Predict;
 import IA.Orders;
 import Component.Point;
 import Component.Grid;
 import Component.Piece;
 import Component.GameInterface;
 
 public class Marvin
 {
     static Robot robot;
     static GameInterface game;
     static KeySender sender;
     static Grid grid;
     static Piece piece;
     static int attempts;
     static int MAX_ATTEMPTS = 2;
     static int last_ordinate;
 
     static void my_sleep(int ms)
     {
         try
         {
             Thread.sleep(ms);
         }
         catch (InterruptedException ex)
         {
             Thread.currentThread().interrupt();
         }
     }
 
     static public void run(String exec)
     {
         try
         {
             Runtime.getRuntime().exec(exec);
             System.out.println("IA : Wait running " + exec);
             my_sleep(1000);
         }
         catch (IOException ex)
         {
             Logger.getLogger(Marvin.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static void display_matrice(boolean t[][])
     {
         int x, y;
 
         System.out.println("Matrice vue par la IA : ");
         for (y = 0; y < 20; y++)
         {
             for (x = 0; x < 10; x++)
                 if (t[x][y])
                     System.out.print("[]");
                 else
                     System.out.print("--");
             System.out.println("");
         }
     }
 
     public static void get_game()
     {
         boolean got_game = false;
         try
         {
             do
             {
                 int[][] g = game.get_grid();
                 int[][] p = game.get_piece();
                 int id = game.get_piece_id();
 
                 if (g == null || p == null || id == -1)
                     my_sleep(10);
                 else
                 {
                     piece = new Piece(id, g.length/2, g[0].length-1);
                     Point[] c = new Point[4];
                     for (int i = 0; i < 4; i++)
                         c[i] = new Point(p[i][0], p[i][1]);
                     piece.set_coordinates(c);
                     grid = new Grid(g.length, g[0].length);
                     for (int i = 0; i < grid.width(); i++)
                         for (int j = 0; j < grid.height(); j++)
                             grid.put(i, j, g[i][j]);
                     got_game = true;
                     attempts = 0;
                 }
             }
             while (! got_game);
         }
         catch (RemoteException re)
         {
             System.out.println(re);
             if (attempts < MAX_ATTEMPTS)
                 attempts++;
             else System.exit(1);
         }
     }
 
     private static void print_grid()
     {
         for (int j = grid.height() - 1; j >= 0; j--)
         {
             System.out.print("|");
             for (int i = 0; i < grid.width(); i++)
             {
                 int value = grid.get(i, j);
                 if (value != -1)
                     System.out.print(" " + value + " |");
                 else
                     System.out.print("   |");
             }
             System.out.println("");
         }
         System.out.println("");
     }
 
     public static void main(String[] args) throws AWTException, IOException
     {
         // Execution
         if (args.length > 0)
             run(args[0]);
 
         // Robot
         try
         {
             robot = new Robot();
             robot.setAutoDelay(1); // 100 ms
             robot.setAutoWaitForIdle(false);
         }
         catch (AWTException ex)
         {
             Logger.getLogger(Marvin.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         // Client
         try
         {
            game = (GameInterface)Naming.lookup("//localhost/tetrist");
         }
         catch (MalformedURLException e)
         {
             System.out.println(e);
         }
         catch (NotBoundException re)
         {
             System.out.println(re);
         }
 
         sender = new KeySender(robot);
         last_ordinate = -1;
         while (true)
         {
             get_game();
             int center_ordinate = piece.coordinates()[0].ordinate();
             if (center_ordinate != last_ordinate)
             {
                 Orders orders = Eval.eval_possibilities(grid, piece);
                 sender.send_key(orders);
                 if (orders.direction() != KeySender.NOTHING)
                     print_grid();
                 last_ordinate = center_ordinate;
                 sender.send_key(KeySender.DOWN);
             }
         }
     }
 }
