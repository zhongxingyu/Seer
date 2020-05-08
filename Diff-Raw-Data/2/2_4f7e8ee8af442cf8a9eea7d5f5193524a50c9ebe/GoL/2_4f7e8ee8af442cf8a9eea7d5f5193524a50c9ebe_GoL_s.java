 import java.util.*;
 import java.io.*;
 import java.lang.*;
 import java.nio.charset.Charset;
 
 class LifeThread extends Thread
 {
 	private final int tid, start, stop, DIM;
 	private Grid g1;
     private int x, y;
 
 	public LifeThread(int t, int st, int sp, Grid g, int D)
 	{
 		tid = t;
 
 		start = st;
 		stop = sp;
 
 		DIM = D;
 
 		g1 = g;
 	}
 
 	public void run()
 	{
         try
         {
             processChunk();      
         }
         catch(Exception e)
         {
             System.out.println("Exception: " + e.toString() + " Thread: " + tid + " at: " + x + "," + y);
         }
 	}
 
     private void processChunk()
     {
         for(x = start; x <= stop; x++)
         {
             for(y = 1; y <= DIM; y++)
             {
                 int count = g1.getCell(x-1,y-1) + g1.getCell(x-1,y) + g1.getCell(x-1, y+1) +
                                         g1.getCell(x,y-1) + g1.getCell(x,y+1) +
                                         g1.getCell(x+1,y-1) + g1.getCell(x+1,y) + g1.getCell(x+1,y+1);
 
                 if(count == 3 || (count == 2 && g1.getCell(x, y) == 1))
                 {
                     g1.setCell(x, y, 1);                    
                 }
                 else if(count < 2 || count > 3)
                 {
                     g1.setCell(x, y, 0);
                 }
                 else if(count == 2)
                 {
                     g1.setCell(x, y, g1.getCell(x, y));
                 }
             }
         }
     }
     
 	public int getTid()
 	{
 		return tid;
 	}
 }
 
 class Grid
 {
 	private final int DIM, LIFE;
 
     private final String FILENAME;
 
 	private int[][] grid, new_grid;
 
     private Random r1;
     
 
 	public Grid(int D, int L, String F)
 	{
 		DIM = D;
 		LIFE = L;
         FILENAME = F;
 
 		grid = new int[DIM+2][DIM+2];
 		new_grid = new int[DIM+2][DIM+2];
 
         r1 = new Random(2012);
         
 		//fillArrayRand();
         readFileIn(FILENAME);
 		copyGhostCells();
 		//printGrid();
 
 	}
 
 	public void finishGen()
 	{
         //note for loop just as fast as system.arraycopy, clone and copyOf
         
         for(int x = 0; x < DIM+2; x++)
         {
             for(int y = 0; y < DIM+2; y++)
             {
                 grid[x][y] = new_grid[x][y];
             }
         }
         
         copyGhostCells();
         printGrid();
 	}
 
 	public void printGrid()
 	{
 		int cellCount = 0;
 		int aliveCount = 0;
 
 		for(int x = 1; x <= DIM; x++)
 		{
 			for(int y = 1; y <= DIM; y++)
 			{
 				if(getCell(x,y) == 1)
 					aliveCount++;
 
 				cellCount++;
 				//System.out.print(getCell(x,y));
 			}
 			//System.out.print("\n");
 		}
 
 		//System.out.println("Cells: " + cellCount);
 		System.out.println("Alive: " + aliveCount);
 	}
 
 	public int getCell(int x, int y)
 	{
 		return grid[x][y];
 	}
 
 	public void setCell(int x, int y, int val)
 	{
 		new_grid[x][y] = val;
 	}
 
 	private void fillArrayRand()
     {
         for(int x = 1; x <= DIM; x++)
         {
           for(int y = 1; y <= DIM; y++)
           {
                 
                 if (r1.nextInt(LIFE) == 1)
                     grid[x][y] = 1;
                 else
                     grid[x][y] = 0;
           }
         }
     }
 
 	public void copyGhostCells()
 	{
 
 		/*copy ghost columns to grid*/
 		for(int x = 1; x <= DIM; x++)
 		{
 			grid[x][DIM+1] = grid[x][1];
 			grid[x][0] = grid[x][DIM];
 		}
 
 		/*copy ghost rows to grid*/
 		for(int y = 0; y <= DIM+1; y++)
 		{
 			grid[0][y] = grid[DIM][y];
 			grid[DIM+1][y] = grid[1][y];
 		}
 	}
 
     private void readFileIn(String filename)
     {
         int x = 1, y = 1, r;
         
         try
         {
             Charset encoding = Charset.defaultCharset();
             File file = new File(filename);
             InputStream in = new FileInputStream(file);
             Reader reader = new InputStreamReader(in, encoding);
             Reader buffer = new BufferedReader(reader);
             while((r = buffer.read()) != -1) //similar to reading line in C
             {
                 if((char)r == '1')
                 {
                     grid[x][y] = 1;
                     y++;
                 }
                 else if((char)r == '0')
                 {
                     grid[x][y] = 0;
                     y++;
                 }
                 else if(r == 10)
                 {
                     x++;
                     y = 1;
                 }
             }
         }
         catch(Exception e)
         {
             System.out.println(e);
         }
     }
 
 }
 
 public class GoL
 {
 
    private static final int THREADS = 4, DIM = 512, LIFE = 3, GEN = 100;
 
     private static final String FILENAME = "512.dat";
 
     public static void main(String args[])
     {
 		Grid g1 = new Grid(DIM, LIFE, FILENAME);
         Stopwatch stopwatch = new Stopwatch();
 		Vector<LifeThread> life_vec = new Vector<LifeThread>();
 
         try
         {
         
             stopwatch.start();
             for(int gen = 0; gen < GEN; gen++)
             {
                 for(int x = 0; x < THREADS; x++)
                 {
                     int start = ((DIM / THREADS) * x) + 1;
                     int stop = (DIM / THREADS) + start - 1;
 
                     LifeThread life = new LifeThread(x, start, stop, g1, DIM);
 
                     life_vec.add(x, life);
                 }
 
                 for(int x = 0; x < THREADS; x++)
                 {
                     life_vec.get(x).start();
                     
                     //System.out.println("Thread: " + life_vec.get(x).getTid() + " at " + gen);
                     
                 }   
 
                 for(int x = 0; x < THREADS; x++)
                     life_vec.get(x).join();
                 
                 if(gen != 0) //Why?? No one knows!
                     g1.finishGen();
                 
             }
             stopwatch.stop();
             
             //g1.printGrid();
             System.out.println("Time: " + stopwatch);
         }
         catch(Exception e)
         {
             System.out.println("Exception: " + e);
         }
         
     }
 }
 
 
