 import java.util.Random;
 
 public class Board
 {
     private final int[][] m_solved;
     private int[][] m_toSolve;
     private final boolean[][] m_editable;
 
     private State m_state;
     
     int m_lives;
     
     public Board(int[][] toSolve, final int[][] solved, int lives)
     {
     	m_solved = new int[9][9];
     	m_toSolve = new int[9][9];
     	m_editable = new boolean[9][9];
     	
         m_state = new 
         
         for (int i = 0; i < 9; ++i)
         {
             System.arraycopy(toSolve[i], 0, m_toSolve[i], 0, 9);
             System.arraycopy(solved[i], 0, m_solved[i], 0, 9);
         }
 
         for (int i = 0; i != 9; ++i)
         {
             for (int j = 0; j != 9; ++j)
             {
                 m_editable[i][j] = (m_toSolve[i][j] == 0);
             }
         }
         
         m_lives = lives;
     }
 
 	public boolean setNode(int x, int y, int val)
     {
         if (m_editable[x][y] == true && val >= 0 && val < 10)
         {
             m_state = new State(m_state, null, m_lives, m_hints, new Vector3D(x, y, val));
             m_toSolve[x][y] = val;
         }
 
        return m_editable[x][y];
     }
 
     public String getBoard()
     {
         String str = new String();
 
         for (int i = 0; i != 9; ++i)
         {
             for (int j = 0; j != 9; ++j)
             {
                 str = str.concat(Integer.toString(m_toSolve[i][j]) + " ");
             }
         }
         
         return str;
     }
 
     int getLives()
     {
         return m_lives;
     }
     
     void setLives(int lives)
     {
         m_lives = lives;
     }
     
     int addLife()
     {
         return ++m_lives;
     }
     
     int removeLife()
     {
         return --m_lives;
     }
     
     Vector3D getHint()
     {
         int x;
         int y;
         Random generator = new Random();
         
         do
         {
             x = generator.nextInt(9);
             y = generator.nextInt(9);
         }
         while (m_toSolve[x][y] != 0);
         
         m_toSolve[x][y] = m_solved[x][y];
         return new Vector3D(x, y, m_solved[x][y]);
     }
     
     void undo()
     {
         State temp = m_state;
         Vector3D v;
         
         m_state = m_state.getPast();
         
         v = m_state.getValue();
         m_lives = m_state.getLives();
         m_hints = m_state.getHints();
         m_toSolve[v.getX()][v.getY()] = v.getZ();
         m_state.setFuture(temp);
     }
     
     void redo()
     {
         Vector3D v;
         
         m_state = m_state.getFuture();
         
         v = m_state.getValue();
         m_lives = m_state.getLives();
         m_hints = m_state.getHints();
         m_toSolve[v.getX()][v.getY()] = v.getZ();
     }
 }
