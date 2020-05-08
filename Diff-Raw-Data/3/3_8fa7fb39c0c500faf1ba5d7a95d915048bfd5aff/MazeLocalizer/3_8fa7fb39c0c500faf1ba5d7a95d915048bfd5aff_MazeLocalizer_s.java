 package edu.cmu.ri.mrpl.maze;
 
 import java.awt.geom.Point2D;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.cmu.ri.mrpl.kinematics2D.Angle;
 import edu.cmu.ri.mrpl.kinematics2D.RealPoint2D;
 import edu.cmu.ri.mrpl.kinematics2D.RealPose2D;
 import edu.cmu.ri.mrpl.maze.MazeWorld.Direction;
 import static java.lang.Math.*;
 
 public class MazeLocalizer {
 	public static final double METERS_PER_INCH = 0.0254;
 	public static final int WALL_INCHES = 29;
 	public static final double WALL_METERS = WALL_INCHES * METERS_PER_INCH;
 	public static final double CELL_RADIUS = WALL_METERS / 2;
 	
 	private RealPose2D initRelWorld;
 	private MazeWorld maze;
 	
 	public MazeLocalizer (MazeWorld maze, boolean isWrong) {
 		initRelWorld = isWrong
 			? mazeStateToWorldPose(maze.getInits().iterator().next())
 			: new RealPose2D();
 		this.maze = maze;
 	}
 	
 	// given the pose of the robot relative to its origin,
 	// compute the cell coordinates of the robot pose
 	public RealPose2D fromInitToCell (RealPose2D robotRelInit) {
 		return fromWorldToCell(fromInitToWorld(robotRelInit));
 	}
 	
 	// given the pose of the robot relative to its origin,
 	// compute the pose of the robot in the maze world
 	public RealPose2D fromInitToWorld (RealPose2D robotRelInit) {
 		return RealPose2D.multiply(initRelWorld, robotRelInit);
 	}
 	
 	public Point2D transformInitToWorld (Point2D pointRelInit) {
 		return initRelWorld.transform(pointRelInit, null);
 	}
 	
 	// converts a pose in the maze world to cell coordinates
 	public static RealPose2D fromWorldToCell (RealPose2D worldPose) {
 		double x = (worldPose.getX() - CELL_RADIUS) / WALL_METERS;
 		double y = (worldPose.getY() - CELL_RADIUS) / WALL_METERS;
 		double theta = Angle.normalize(worldPose.getTh());
 		/*if (theta < 0) {
 			theta += 2*PI;
 		}*/
 		theta /= PI/2;
 		return new RealPose2D(x, y, theta);
 	}
 	
 	public static MazeState fromWorldToMazeState (RealPose2D worldPose) {
 		RealPose2D cellPose = fromWorldToCell(worldPose);
 		int x = (int) round(cellPose.getX());
 		int y = (int) round(cellPose.getY());
 		int dirIndex = (int) round(cellPose.getTh());
 		Direction dir = Direction.values()[dirIndex];
 		return new MazeState(x, y, dir);
 	}
 	
 	public static RealPose2D fromMazeToWorld (RealPose2D cellPose) {
 		double x = cellPose.getX()*WALL_METERS + CELL_RADIUS;
 		double y = cellPose.getY()*WALL_METERS + CELL_RADIUS;
 		double theta = Angle.normalize(cellPose.getTh());
 		/*if (theta < 0) {
 			theta += 2*PI;
 		}*/
 		theta /= PI/2;
 		return new RealPose2D(x, y, theta);
 	}
 	
 	// converts a (x, y, dir) maze state
 	// to an (x, y, theta) pose in the maze world
 	public static RealPose2D mazeStateToWorldPose (MazeState state) {
 		double x = CELL_RADIUS + (state.x() * WALL_METERS);
 		double y = CELL_RADIUS + (state.y() * WALL_METERS);
 		double theta = Angle.normalize(state.dir.ordinal() * (PI / 2));
 		return new RealPose2D(x, y, theta);
 	}
 	
 	public static List<RealPose2D> statesToPoses (List<MazeState> states) {
 		List<RealPose2D> result = new LinkedList<RealPose2D>();
 		
 		// skip all but the last state in the same place
 		MazeSolver.dedupPositions(states);
 		
 		for (MazeState s : states) {
 			RealPose2D pose = mazeStateToWorldPose(s);
 			
 			// TODO generalize this?
 			result.add(new RealPose2D(pose.getX()-CELL_RADIUS, pose.getY()-CELL_RADIUS, pose.getTh()));
 		}
 		
 		return result;
 	}
 	
 	/*
 	 * Given a point relative to the maze frame (origin at southwest corner),
 	 * returns a MazeState representing the nearest wall.
 	 * This MazeState can be used with ProbabilisticWallGrid.hitWall to associate sonar hits.
 	 */
 	public static MazeState getClosestWall (Point2D hitRelMaze, int width, int height) {
 		double x = hitRelMaze.getX();
 		double y = hitRelMaze.getY();
 		
 		int col = (int) floor(x / WALL_METERS);
 		// clamp to maze
 		col = max(col, 0);
 		col = min(col, width-1);
 		
 		int row = (int) floor(y / WALL_METERS);
 		// clamp to maze
 		row = max(row, 0);
 		row = min(row, height-1);
 		
 		double xClosestWall = round(x / WALL_METERS) * WALL_METERS;
 		double yClosestWall = round(y / WALL_METERS) * WALL_METERS;
 		
 		double xDist = abs(x - xClosestWall);
 		double yDist = abs(y - yClosestWall);
 		
 		boolean isVertical = xDist < yDist;
 		boolean isEast = x - col*WALL_METERS > CELL_RADIUS;
 		boolean isNorth = y - row*WALL_METERS > CELL_RADIUS;
 		
 		Direction dir = isVertical
 				? (isEast ? Direction.East : Direction.West)
 				: (isNorth ? Direction.North : Direction.South);
 		
 		return new MazeState(col, row, dir);
 	}
 	
 	
 	public static void main (String... args) {
 		int width = 6;
 		int height = 4;
 		
 		for (int i = 0; i < 10; i++) {
 			double cellX = random()*width;
 			double cellY = random()*height;
 			
 			double hitX = cellX * WALL_METERS;
 			double hitY = cellY * WALL_METERS;
 			
 			Point2D hit = new Point2D.Double(hitX, hitY);
 			
 			System.out.printf("cell coords: (%.2f, %.2f)\n", cellX, cellY);
 			System.out.println(getClosestWall(hit, width, height));
 			System.out.println();
 		}
 	}
 }
