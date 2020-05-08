 package editor;
 
 import game.DataHandler;
 import game.GamePanel;
 
 import java.awt.Point;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Scanner;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import structures.Ball;
 import structures.Level;
 import structures.Point2d;
 
 /**
  * Utility for computing the solutions of levels in parallel. The main method
  * can be used as a command line utility that will output the data to pre-set
  * file paths.
  * @author Sean Lewis
  */
 public class LevelSolver {
 	
 	/**
 	 * Args input syntax:
 	 *  - put "GG all" to output all standard GG level data.
 	 *  
 	 *  - put "GG" if standard game levels are being solved:
 	 *    - if so, place numbers afterwards for which levels
 	 *    - ex: GG 1 2 3 4 5
 	 *      - this solves the first 5 levels
 	 *      
 	 *  - otherwise, put "OTHER" as the first command
 	 *    - the 2nd command should be the input file 
 	 *    - the 3rd command should be the output directory destination
 	 *    - then place the file name where levels are being read from
 	 *    - place numbers the same was as in standard input
 	 *    - NOTE: the first level will be level 0
 	 */
 	public static void main(String[] args) throws IOException {
 		
 		
 		if(args[0].equals("GG") && args[1].equals("all")) {
 			System.out.println("Reading all stanard input levels.");
			List<Level> levels = new DataHandler().getLevelData("levels/levels.txt");	
			for(int i = 0; i < levels.size(); i++) {
				System.out.println("Reading level " + (i+1) + ".");
				File f = new File("levels/data/level" + (i+1) + ".txt");
 				PrintWriter pw = new PrintWriter(f);
 				LevelSolver.printSolutionSet(levels.get(i), pw);
 				pw.close();
 			}
 			
 			
 		} else if(args[0].equals("GG")) {
 			System.out.println("Reading stanard input levels.");
 			List<Level> levels = new DataHandler().getLevelData("levels/levels.txt");
 			
 			for(int i = 1; i < args.length; i++) {
 				int level = Integer.parseInt(args[i]);
 				System.out.println("Reading level " + level + ".");
 				File f = new File("levels/data/level" + level + ".txt");
 				PrintWriter pw = new PrintWriter(f);
				LevelSolver.printSolutionSet(levels.get(level - 1), pw);
 				pw.close();
 			}			
 			
 			
 		} else {
 			System.out.println("Reading non-stanard input levels.");
 			List<Level> levels = new DataHandler().getLevelData(args[1]);
 			String outDir = args[2];
 			for(int i = 2; i < args.length; i++) {
 				int level = Integer.parseInt(args[i]);
 				System.out.println("Reading level " + level + ".");
 				File f = new File(outDir + "/level" + level + ".txt");
 				PrintWriter pw = new PrintWriter(f);
 				LevelSolver.printSolutionSet(levels.get(i), pw);
 				pw.close();
 			}		
 			
 		}
 		System.out.println("Finished.");
 		
 	}
 
 	private static final int MAX = GamePanel.MaxInitialMagnitude;	
 	
 	/**
 	 * Computes all solutions to the level and returns in the points in a List.
 	 * @param level the level to solve
 	 * @return all points such that level.possibleWin(p) returns true
 	 */
 	public static Collection<java.awt.Point> getSolutionSet(Level level) {
 		Ball ball = level.getBall();
 		List<Task> tasks = new ArrayList<Task>();
 		
 		// iterate over all possible x values
 		int leftX = (int) (ball.getCenter().x - MAX);
 		int rightX = (int) (ball.getCenter().x + MAX);
 		for (int x = leftX; x <= rightX; x++) {
 			if (xOutOfBounds(level, x)) {
 				continue;
 			}
 
 			// iterate over all possible y values
 			int bottomY = (int) (ball.getCenter().y - MAX);
 			int topY = (int) (ball.getCenter().y + MAX);
 			List<Point2d> points = new ArrayList<Point2d>();
 			for (int y = bottomY; y <= topY; y++) {
 				points.add(new Point2d(x, y));
 			}
 			tasks.add(new LevelSolver().new Task(level, points));
 		}
 
 		// parallel computations:
 		ExecutorService executor = Executors.newCachedThreadPool();  
 		for (Task t : tasks)
 			executor.execute(t);
 		executor.shutdown();
 		
 		try {
 			executor.awaitTermination(1, TimeUnit.HOURS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
   		
   		List<java.awt.Point> solutions = new ArrayList<java.awt.Point>();
   		for(Task t : tasks)
   			solutions.addAll(t.getSolutions());
 		return solutions;
 	}
 	
 	/*
 	 * Inner class for computing a list of test points.
 	 */
 	private class Task implements Runnable {
 		private static final double sqr = MAX * MAX;
 		private List<Point2d> inputPoints;
 		private List<Point> solutionPoints;
 		private Level level;
 		
 		public Task(Level level, List<Point2d> inputPoints) {
 			this.level = level;
 			this.inputPoints = inputPoints;
 			solutionPoints = new ArrayList<Point>();
 		}
 
 		public void run() {
 			Ball ball = level.getBall();
 
 			for (Point2d point : inputPoints) {
 				double x = point.getX();
 				double y = point.getY();
 
 				boolean outOfBounds = yOutOfBounds(level, y)
 						|| isOutOfBounds(level, ball.getCenter());
 
 				boolean inShotRange = Math.pow(x - ball.getCenter().x, 2)
 						+ Math.pow(y - ball.getCenter().y, 2) <= sqr;
 
 				if (!outOfBounds && inShotRange) {
 					if (level.possibleWin(point, MAX)) {
 						solutionPoints.add(point.getIntegerPoint());
 					}
 				}
 			}			
 		}
 		
 		public Collection<Point> getSolutions() {
 			return solutionPoints;
 		}
 
 	}
 
 	/**
 	 * Prints a solution set through a PrintWriter.
 	 * @param solutions the solution set to print
 	 * @param pw the PrintWriter to print through
 	 */
 	public static void printSolutionSet(Collection<java.awt.Point> solutions,
 			PrintWriter pw) {
 		for (java.awt.Point p : solutions) {
 			pw.println(p.x + " " + p.y);
 		}
 		pw.close();
 	}
 
 	/**
 	 * Computes the level data and then prints the information to a PrintWriter.
 	 * @param level the Level to solve
 	 * @param pw the PrintWriter to print through
 	 */
 	public static void printSolutionSet(Level level, PrintWriter pw) {
 		printSolutionSet(getSolutionSet(level), pw);
 	}
 
 	/**
 	 * Reads in a solution set from a file.
 	 * @param fileName the file to read from
 	 * @return a List of solution points for the level
 	 * @throws FileNotFoundException if the file was not found
 	 */
 	public static Collection<java.awt.Point> readSolutionSet(String fileName)
 			throws FileNotFoundException {
 		List<java.awt.Point> solutions = new ArrayList<java.awt.Point>();
 		Scanner infile = new Scanner(new File(fileName));
 		while (infile.hasNext()) {
 			int x = infile.nextInt();
 			int y = infile.nextInt();
 			java.awt.Point p = new java.awt.Point(x, y);
 			solutions.add(p);
 		}
 		infile.close();
 		return solutions;
 	}
 
 	private static boolean xOutOfBounds(Level level, double x) {
 		boolean onLeft  = x + level.getScreenXShift() < 0;
 		boolean onRight = x + level.getScreenXShift() > GamePanel.Width;
 		return  onLeft || onRight;
 	}
 
 	private static boolean yOutOfBounds(Level level, double y) {
 		boolean onTop    = y + level.getScreenYShift() < 0;
 		boolean onBottom = y + level.getScreenYShift() > GamePanel.Height - 20;
 		return  onTop || onBottom;
 	}
 
 	private static boolean isOutOfBounds(Level level, Point2d center) {
 		return xOutOfBounds(level, center.getX())
 			|| yOutOfBounds(level, center.getY());
 	}
 
 }
