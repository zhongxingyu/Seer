 package board;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import agent.Agent;
 
 public class Board {
 
 	public enum Physics {
 		TEMPERATURE, CO
 	}
 
 	/**
 	 * Zwraca dane fizyczne zadanego typu. Jak nie ma dla tego punktu takich
 	 * danych, rzuca wyjtek.
 	 * 
 	 * @param where
 	 * @param what
 	 * @return
 	 * @throws NoPhysicsDataException
 	 */
 	public double getPhysics(Point where, Physics what)
 			throws NoPhysicsDataException {
 		try {
 			return getDataCell(where).getPhysics(what);
 		} catch (IndexOutOfBoundsException e) {
 			// jeli nie ma adnej komrki na pozycji {@code where}
 			throw new NoPhysicsDataException();
 		}
 	}
 
 	public void setPhysics(Point where, Physics what, double value) {
 		getDataCell(where).setPhysics(what, value);
 	}
 
 	public Point getDimension() {
 		return dimension;
 	}
 
 	public List<Obstacle> getObstacles() {
 		return obstacles;
 	}
 
 	public class NoPhysicsDataException extends Exception {
 		private static final long serialVersionUID = 1L;
 	}
 
 	// ------------- internals start here, an Agent should not use those
 
 	private Point dimension;
 
 	// leave these package-private (without access modifier) -- BoardView
 	// has to be able to read them
 	Point dataCellDimension;
 
 	List<Agent> agents;
 	List<Obstacle> obstacles;
 	List<Exit> exits;
 	List<List<DataCell>> dataCells;
 
 	private static final long MAX_RANDOM_FAILURES = 10;
 	private Random rng;
 
 	public Board() {
 		agents = new ArrayList<Agent>();
 		obstacles = new ArrayList<Obstacle>();
 		exits = new ArrayList<Exit>();
 		rng = new Random();
 	}
 
 	/**
 	 * Jedna iteracja symulacji.
 	 * 
 	 * @param dt
 	 *            czas w [ms] ktry upyn od poprzedniej iteracji
 	 */
 	public void update(double dt) {
 		for (Agent agent : agents)
 			agent.update(dt);
 	}
 
 	/**
 	 * Ustawia geometri planszy.
 	 * 
 	 * @param dimension
 	 *            Rozmiar planszy (najdalszy punkt od jej pocztku).
 	 * @param numCellsX
 	 *            Rozdzielczo w OX (na ile odcinkw dzielimy OX).
 	 * @param numCellsY
 	 *            Rozdzielczo w OY.
 	 */
 	public void setGeometry(Point dimension, long numCellsX, long numCellsY) {
 		this.dimension = dimension;
 		this.dataCellDimension = new Point(dimension.x / numCellsX, dimension.y
 				/ numCellsY);
 
 		dataCells = new ArrayList<List<DataCell>>();
 		for (long y = 0; y < numCellsY; y++) {
 			List<DataCell> row = new ArrayList<DataCell>();
 			for (long x = 0; x < numCellsX; x++)
 				row.add(new DataCell());
 			dataCells.add(row);
 		}
 	}
 
 	public void addObstacle(Point start, Point end) {
		Point newStart = new Point(Math.min(start.x, end.x), Math.min(start.y, end.y));
		Point newEnd   = new Point(Math.max(start.x, end.x), Math.max(start.y, end.y));
		
		obstacles.add(new Obstacle(newStart, newEnd));
 	}
 
 	public void addExit(Point start, Point end) {
 		exits.add(new Exit(start, end));
 	}
 
 	public void initAgentsRandomly(long num) {
 		for (long i = 0; i < num; i++) {
 			long numFails = 0;
 
 			while (numFails++ < MAX_RANDOM_FAILURES) {
 				Point position = new Point(rng.nextDouble() * dimension.x,
 						rng.nextDouble() * dimension.y);
 
 				Agent agent = new Agent(this, position);
 				if (!agent.hasCollision()) {
 					agents.add(agent);
 					break;
 				}
 			}
 		}
 	}
 
 	DataCell getDataCell(Point where) {
 		if (where.x < 0 || where.y < 0 || where.x > dimension.x
 				|| where.y > dimension.y)
 			throw new IndexOutOfBoundsException();
 
 		long x = Math.round(Math.floor(where.x / dataCellDimension.x));
 		long y = Math.round(Math.floor(where.y / dataCellDimension.y));
 
 		// correct a rare situation when:
 		//
 		// a) where.y == dimensions.y and/or
 		// b) where.x == dimensions.x
 
 		if (y > dataCells.size() - 1)
 			y = dataCells.size() - 1;
 		if (x > dataCells.get(0).size() - 1)
 			x = dataCells.get(0).size() - 1;
 
 		return dataCells.get((int) y).get((int) x);
 	}
 
 	final class DataCell {
 		private Map<Physics, Double> physics;
 
 		public DataCell() {
 			physics = new HashMap<Physics, Double>();
 		}
 
 		public double getPhysics(Physics what) throws NoPhysicsDataException {
 			Double value = physics.get(what);
 			if (value == null)
 				throw new NoPhysicsDataException();
 			return value;
 		}
 
 		public void setPhysics(Physics what, double value) {
 			physics.put(what, value);
 		}
 	}
 
 	public class TwoPointStructure {
 		private Point start, end;
 
 		public TwoPointStructure(Point start, Point end) {
 			this.start = start;
 			this.end = end;
 		}
 
 		public Point getStartPoint() {
 			return start;
 		}
 
 		public Point getEndPoint() {
 			return end;
 		}
 	}
 
 	public final class Exit extends TwoPointStructure {
 		public Exit(Point start, Point end) {
 			super(start, end);
 		}
 	}
 
 	public final class Obstacle extends TwoPointStructure {
 		public Obstacle(Point start, Point end) {
 			super(start, end);
 		}
 	}
 }
