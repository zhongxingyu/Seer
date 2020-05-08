 package sokoban;
 
 import java.io.File;
 import java.util.List;
 
 import sokoban.solvers.Solver;
 import sokoban.solvers.SolverFactory;
 
 public class RunOffline {
 
     public static void main(String[] args)
     {
         if (args.length < 3) {
             System.err.println("Usage: java sokoban.RunOffline  solver  level_file  level_number\n");
             System.exit(2);
         }
         
         int boardNumber = Integer.parseInt(args[2]);
         Solver solver = SolverFactory.loadSolver(args[0]);
         
         File slc = new File(args[1]);
         List<String> boards = BoardParser.getBoardStrings(slc);
         
        Board board = BoardParser.parse(boards.get(boardNumber-1).getBytes());
         
         System.out.println(board);
         
         long beforeSolve = System.currentTimeMillis();
         String solution = solver.solve(board);
         long solveTime = System.currentTimeMillis() - beforeSolve;
 
         System.out.println(solution);
         System.out.println("Solve time (ms): " + solveTime);
 
     }
     
 }
