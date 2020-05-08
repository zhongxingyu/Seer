 // Connexion IA
 import java.net.*;
 import java.rmi.*;
 
 // Graphique
 import javax.swing.*;
 
 // Clavier
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyListener;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import Graphic.Draw;
 import Component.Game;
 
 class Tetrist
 {
     static Draw draw;
     static Game game;
     static JFrame frame;
     static KeyListener in_game = new KeyAdapter()
         {
             public void keyPressed(KeyEvent e)
             {
                 switch (e.getKeyCode())
                 {
                     case KeyEvent.VK_RIGHT:
                         action_right();
                         break;
                     case KeyEvent.VK_LEFT:
                         action_left();
                         break;
                     case KeyEvent.VK_UP:
                         action_rotation();
                         break;
                     case KeyEvent.VK_DOWN:
                         action_fall();
                         break;
                     case KeyEvent.VK_ESCAPE:
                         action_pause();
                         break;
                 }
             }
 
             public void keyReleased(KeyEvent e)
             {
                 // KEY RELEASED
             }
         };
 
     static KeyListener in_pause = new KeyAdapter()
         {
             public void keyPressed(KeyEvent e)
             {
                 switch (e.getKeyCode())
                 {
                     case KeyEvent.VK_ESCAPE:
                         action_pause();
                         break;
                 }
             }
 
             public void keyReleased(KeyEvent e)
             {
             }
         };
 
     static int level = 0;
     static final int time_max = 500;
     static final int time_min = 100;
     static final int time_step = 100;
     static Timer timer = null;
     static int x, y; // For example
     static boolean pause = false;
 
     public static void action_pause()
     {
         if (pause)
         {
             frame.removeKeyListener(in_pause);
             int time = time_of_level(level);
             new_timer(time);
             frame.addKeyListener(in_game);
 
         }
         else
         {
             frame.removeKeyListener(in_game);
             timer.cancel();
             frame.addKeyListener(in_pause);
         }
         pause = ! pause;
     }
 
     public static void action_right()
     {
         game.right();
         draw.refresh();
     }
 
     public static void action_left()
     {
         game.left();
         draw.refresh();
     }
 
     public static void action_rotation()
     {
         game.rotate();
         draw.refresh();
         return;
     }
 
     public static void action_fall()
     {
         game.fall();
         draw.refresh();
         new_level(game.level());
         if (game.game_is_over())
         {
             System.out.println("Score : " + game.score());
             System.out.println("Level : " + game.level());
             System.out.println("Lines : " + game.lines_destroyed());
             System.exit(0);
         }
     }
 
     public static int time_of_level(int n)
     {
         int time = time_max - (n-1) * time_step;
         return time >= time_min ? time : time_min;
     }
 
     public static void new_timer(int time)
     {
         if (timer != null)
             timer.cancel();
         timer = new Timer();
         TimerTask fall_task = new TimerTask()
             {
                 public void run()
                 {
                     action_fall();
                 }
             };
         timer.schedule(fall_task, 0, time);
     }
 
     public static void new_level(int n)
     {
         if (level != n)
         {
             int time = time_of_level(n);
             level = n;
             new_timer(time);
         }
     }
 
     public static void main(String[] args)
     {
         // Connexion IA
         try
         {
             game = new Game();
            Naming.rebind("matrice", game);
         }
         catch (RemoteException re)
         {
             System.out.println(re);
         }
         catch (MalformedURLException e)
         {
             System.out.println(e);
         }
 
         // Base Graphique
         frame = new JFrame("Tetrist");
         draw = Draw.factory(game);
         frame.getContentPane().add(draw);
         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
 
         // Clavier
         frame.addKeyListener(in_game);
 
         /* Chute. */
         new_level(game.level());
     }
 }
