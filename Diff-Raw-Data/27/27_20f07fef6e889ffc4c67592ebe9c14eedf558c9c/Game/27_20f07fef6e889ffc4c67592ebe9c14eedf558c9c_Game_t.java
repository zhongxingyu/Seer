 package Component;
 
 import java.rmi.server.UnicastRemoteObject;
 import java.rmi.RemoteException;
 import java.util.Random;
 
 public class Game extends UnicastRemoteObject implements MatrixInterface
 {
     protected static final int[] score_scale = { 0, 40, 100, 300, 1200 };
 
     private Grid grid;
     private Piece current;
     private Piece next;
     private int score;
     private int lines;
     private int level;
    private int middle;
    private int top;
 
     private Point[] temp_points;
     private Random generator;
 
     private boolean is_busy;
     private boolean matrice_IA[][];
 
     private Piece pieces[];
 
     public Game() throws RemoteException
     {
         super();
 
         temp_points = new Point[4];
         for (int i = 0; i < temp_points.length; i++)
             temp_points[i] = new Point();
         grid = new Grid();
         matrice_IA = new boolean[grid.width()][grid.height()];
         generator = new Random();
         score = 0;
         pieces = Piece.full_set_factory(grid.width()/2, grid.height()-1);
        middle = grid.width()/2;
        top = grid.height()-1;
        next = new Piece(generator.nextInt(7), middle, top);
         new_piece();
     }
 
     // Appel distant pour la IA
     public boolean[][] get_matrice() throws RemoteException
     {
         if (is_busy)
             return null;
 
         return matrice_IA;
     }
     // Fin de l'appel distant.
 
     public void refresh()
     {
         is_busy = true;
         for (int y = 0; y < grid.height(); y++)
             for (int x = 0; x < grid.width(); x++)
                 matrice_IA[x][y] = ! grid.is_free(x, y);
 
         Point[] coordinates = current.coordinates();
         for (Point point : coordinates)
             if (grid.in_bonds(point))
                 matrice_IA[point.abcissa()][point.ordinate()] = true;
         is_busy = false;
     }
 
     public Grid grid()
     {
         return grid;
     }
 
     public Piece current_piece()
     {
         return current;
     }
 
     public Piece next_piece()
     {
         return next;
     }
 
     public void new_piece()
     {
         current = next;
        int i = generator.nextInt(7);
        next = new Piece(i, middle, top);
     }
 
     public int level()
     {
         return level;
     }
 
     public int lines_destroyed()
     {
         return lines;
     }
 
     private void compute_level()
     {
         level = score / 1000;
     }
 
     public void fall()
     {
         current.needed_space_fall(temp_points);
         if (grid.in_bonds(temp_points) && grid.is_free(temp_points))
             current.fall();
         else
         {
             grid.put(current.coordinates(), current.id());
             int destroyed = grid.check_and_delete(current.coordinates());
             lines += destroyed;
             score += lines_to_score(destroyed);
             compute_level();
            new_piece();
         }
     }
 
     public void left()
     {
         current.needed_space_left(temp_points);
         if (grid.in_bonds(temp_points) && grid.is_free(temp_points))
             current.left();
     }
 
     public void right()
     {
         current.needed_space_right(temp_points);
         if (grid.in_bonds(temp_points) && grid.is_free(temp_points))
             current.right();
     }
 
     public void rotate()
     {
         current.needed_space_rotation(temp_points);
         if (grid.in_bonds(temp_points) && grid.is_free(temp_points))
             current.rotate();
     }
 
     public boolean game_is_over()
     {
         return grid.full();
     }
 
     private int lines_to_score(int destroyed)
     {
         int index = (destroyed < 0 || destroyed >= score_scale.length) ? 0 : destroyed;
         return score_scale[index];
     }
 
     public int score()
     {
         return score;
     }
 }
