 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim.cam;
 
 import heartsim.Application;
 import heartsim.cam.parameter.CAModelIntParameter;
 import heartsim.util.StringUtils;
 import java.util.Random;
 
 /**
  * 2D implementation of the cellular automata model by A. Nishiyama, H. Tanakab
  * and T. Tokihiroa presented in "An isotropic cellular automaton for excitable
  * media"
  * @author Lee Boynton
  */
 public class Nishiyama extends CellularAutomataModel
 {
     protected int delta[][]; // delta values for each cell
     protected Random generator = new Random();
     protected int N;
     protected final int excitationValue = 1;
 
     public Nishiyama()
     {
         // set name of CA model
         super("Nishiyama");
 
         this.setDescription("2D implementation of the cellular automata model by " +
                 "A. Nishiyama, H. Tanakab and T. Tokihiroa presented in \"An " +
                 "isotropic cellular automaton for excitable media\"");
 
         // create parameters
         CAModelIntParameter Nparam = new CAModelIntParameter(6);
         CAModelIntParameter delta1 = new CAModelIntParameter(3);
         CAModelIntParameter delta2 = new CAModelIntParameter(7);
 
         // set descriptions of parameters
         Nparam.setDescription("This value affects how high the voltage/recovery of a " +
                 "cell can go before it plateaus. A larger value for N will " +
                 "result in a thicker wave.");
         delta1.setDescription("Cells are randomly assigned either delta 1 or " +
                 "delta 2. The sum of the Moore neighbours must be at least this " +
                 "value before a cell becomes excited.\n\n" +
                 "Using the same value for delta 1 and delta 2 will result in " +
                 "an unnatural uniform wave with straight edges.");
         delta2.setDescription(delta1.getDescription());
 
         // add parameters
         this.setParameter("N", Nparam);
         this.setParameter("Delta 1", delta1);
         this.setParameter("Delta 2", delta2);
     }
 
     public void initCells()
     {
         int delta1 = (Integer) this.getParameter("Delta 1").getValue();
         int delta2 = (Integer) this.getParameter("Delta 2").getValue();
         N = (Integer) this.getParameter("N").getValue();
 
         delta = new int[height][width]; // delta values for each cell
 
         for (int row = 0; row < height; row++)
         {
             for (int col = 0; col < width; col++)
             {
                 // initialise delta values
                 int randomDelta = generator.nextInt(2);
 
                 if (randomDelta == 0)
                 {
                     delta[row][col] = delta1;
                 }
                 else
                 {
                     delta[row][col] = delta2;
                 }
             }
         }
     }
 
     public void printArrays()
    {      
         int values[][][] =
         {
             delta
         };
 
         String names[] =
         {
             "Delta"
         };
 
         for (int i = 0; i < names.length; i++)
         {
             names[i] = StringUtils.padRight(names[i], width);
         }
 
         for (int row = -1; row < height; row++)
         {
             for (int i = 0; i < names.length; i++)
             {
                 if (row == -1)
                 {
                     System.out.print(names[i] + " ");
                     continue;
                 }
 
                 for (int col = 0; col < width; col++)
                 {
                     System.out.print(values[i][row][col]);
                 }
 
                 System.out.print(" ");
             }
 
             System.out.println();
         }
     }
 
     @Override
     public int getMax()
     {
         return (Integer) this.getParameter("N").getValue();
     }
 
     @Override
     public int getMin()
     {
         return 0;
     }
 
     @Override
     public boolean isCellRecovered(int u, int v)
     {
         return u == 0 && v == 0;
     }
 
     @Override
     public int getCellThreshold()
     {
         return excitationValue;
     }
 
     @Override
     public void processCell(int row, int col, int[][] u, int[][] v, int[][] tempu)
     {
         if (u[row][col] == 0)
         {
             if (v[row][col] == 0)
             {
                 // check for stimulation
                 // inefficient count of neighbours
                 if (tempu[row - 1][col - 1] + tempu[row][col - 1] +
                         tempu[row + 1][col - 1] + tempu[row - 1][col] +
                         tempu[row + 1][col] + tempu[row - 1][col + 1] +
                         tempu[row][col + 1] + tempu[row + 1][col + 1] >= delta[row][col])
                 {
                     u[row][col] = 1;  // stimulated
                 }
             }
             else
             {
                 v[row][col]--;  // refractory
             }
         }
         else if (v[row][col] == N - 1)
         {
             u[row][col]--;  // downstoke
         }
         else if (u[row][col] == N - 1)
         {
             v[row][col]++;  // plateau
         }
         else
         {
             u[row][col]++;  // upstroke
         }
     }
 }
