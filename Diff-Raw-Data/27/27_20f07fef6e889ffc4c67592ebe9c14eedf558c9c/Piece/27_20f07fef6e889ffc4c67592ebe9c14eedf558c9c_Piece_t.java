 package Component;
 
 public class Piece
 {
     public static final int cardinal = 7;
 
     protected static final int move_down = 0;
     protected static final int move_left = 1;
     protected static final int move_right = 2;
 
     protected final int id;
     protected final Point[][] face;
 
     private final int spawn_abcissa;
     private final int spawn_ordinate;
     private final int spawn_rotation;
 
     private int rotation;
     private Point[] coordinates;
 
     public Piece(int n, int x, int y)
     {
         id = n;
         spawn_rotation = 3;
         spawn_abcissa = x;
         spawn_ordinate = y;
         face = load_face(id);
         coordinates = new Point[4];
         for (int i = 0; i < coordinates.length; i++)
             coordinates[i] = new Point();
 
        init();
     }
 
    public void init()
     {
         rotation = spawn_rotation;
 
         int center_x = face[rotation][0].abcissa();
         int center_y = face[rotation][0].ordinate();
 
         for (int i = 0; i < coordinates.length; i++)
         {
             Point current = coordinates[i];
             int i_x = face[rotation][i].abcissa();
             int i_y = face[rotation][i].ordinate();
             current.set_abcissa(spawn_abcissa + i_x - center_x);
             current.set_ordinate(spawn_ordinate + i_y - center_y);
         }
     }
 
     public int rotation()
     {
         return rotation;
     }
 
     public Point[][] face()
     {
         return face;
     }
 
     public Point[] coordinates()
     {
         return coordinates;
     }
 
     public int id()
     {
         return id;
     }
 
     public void rotate()
     {
         if (id != 5)
         {
             rotation += 3;
             if (id == 0 || id == 4 || id == 6)
                 rotation = 2 + rotation % 2;
             else
                 rotation = rotation % 4;
 
             for (int i = 1; i < 4; i++)
             {
                 int center_x = face[rotation][0].abcissa();
                 int center_y = face[rotation][0].ordinate();
                 int i_x = face[rotation][i].abcissa();
                 int i_y = face[rotation][i].ordinate();
                 Point current = coordinates[i];
 
                 current.set(coordinates[0]);
                 current.shift_abcissa(i_x - center_x);
                 current.shift_ordinate(i_y - center_y);
             }
         }
     }
 
     public void fall()
     {
         shift(0, -1);
     }
 
     public void left()
     {
         shift(-1, 0);
     }
 
     public void right()
     {
         shift(1, 0);
     }
 
     private void shift(int horizontal_shift, int vertical_shift)
     {
         for (Point point : coordinates)
             point.shift(horizontal_shift, vertical_shift);
     }
 
     public void needed_space_fall(Point[] space)
     {
         needed_space(space, 0);
     }
 
     public void needed_space_left(Point[] space)
     {
         needed_space(space, 1);
     }
 
     public void needed_space_right(Point[] space)
     {
         needed_space(space, 2);
     }
 
     private void needed_space(Point[] space, int move)
     {
         int h_shift = (move == move_down) ? 0 : ((move == move_left) ? -1 : 1);
         int v_shift = (move == move_down) ? -1 : 0;
 
         for (int i = 0; i < coordinates.length; i++)
         {
             space[i].set(coordinates[i]);
             space[i].shift(h_shift, v_shift);
         }
     }
 
     public void needed_space_rotation(Point[] space)
     {
         rotate();
 
         for (int i = 0; i < coordinates.length; i++)
             space[i].set(coordinates[i]);
 
         for (int i = 0; i < 3; i++)
             rotate();
     }
 
     private static Point[][] load_face(int p)
     {
         Point[][] face = new Point[4][4];
         for (int i = 0; i < face.length; i++)
             for (int j = 0; j < face[i].length; j++)
                 face[i][j] = new Point();
 
         /* Centre de rotation. */
         for (int i = 0; i < face.length; i++)
             face[i][0].set(0, 1);
 
         face[0][1].set_abcissa(p != 0 ? 1 : 0);
         face[0][1].set_ordinate(p >= 4 ? 1 : (3 - p));
 
         face[0][2].set_abcissa(p == 4 ? 1 : 0);
         face[0][2].set_ordinate(((p % 2) == 0) ? 2 : 0);
 
         face[0][3].set_abcissa(p > 4 ? 1 : 0);
         face[0][3].set_ordinate((p == 1 || p == 3) ? 2 : 0);
 
         /* Rotations. */
         for (int i = 1; i < face.length; i++)
             for (int j = 0; j < face[i].length; j++)
             {
                 int h = i - 1;
                 int x = -face[h][j].ordinate();
                 int y = face[h][j].abcissa();
                 face[i][j].set(x, y);
             }
 
         return face;
     }
 
     public static Piece[] full_set_factory(int x, int y)
     {
         Piece[] set = new Piece[cardinal];
         for (int i = 0; i < cardinal; i++)
             set[i] = new Piece(i, x, y);
 
         return set;
     }
 }
