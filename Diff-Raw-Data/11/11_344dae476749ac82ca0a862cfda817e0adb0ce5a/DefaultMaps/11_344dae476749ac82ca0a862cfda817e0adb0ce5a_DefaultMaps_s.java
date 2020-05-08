 package chalmers.dax021308.ecosystem.model.environment.mapeditor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.environment.SimulationSettings;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.EllipticalObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.RectangularObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.TriangleObstacle;
 import chalmers.dax021308.ecosystem.model.util.Position;
 
 public class DefaultMaps {
 	public static final SimulationMap elliptical_map;
 	public static final SimulationMap rectangular_map;
 	public static final SimulationMap triangle_map;
 	public static final SimulationMap rivers_map;
 	public static final SimulationMap tube_map;
 	public static final SimulationMap empty_map;
 	public static final List<SimulationMap> defaultMaps;
 	
 	static {
 		Dimension d = SimulationMap.DEFAULT_OBSTACLE_DIMENSION;
 		List<IObstacle> obstacles = new ArrayList<IObstacle>(1);
 		obstacles.add(new EllipticalObstacle(d.getWidth() * 0.2, d
 				.getHeight() * 0.15, new Position(d.getWidth() / 2, d
 				.getHeight() / 2), new Color(0, 128, 255)));
 		elliptical_map = new SimulationMap(obstacles, "Elliptical obstacle");
 		obstacles = new ArrayList<IObstacle>(1);
 		obstacles.add(new RectangularObstacle(d.getWidth() * 0.2, d
 				.getHeight() * 0.1, new Position(d.getWidth() / 2, d
 				.getHeight() / 2), new Color(0, 128, 255)));
 		rectangular_map = new SimulationMap(obstacles, "Rectangular obstacle");
 		obstacles = new ArrayList<IObstacle>(1);
 		obstacles.add(new TriangleObstacle(d.getWidth() * 0.2, d
 				.getHeight() * 0.2, new Position(d.getWidth() / 2, d
 				.getHeight() / 2), new Color(0, 128, 255)));
 		triangle_map = new SimulationMap(obstacles, "Triangle obstacle");
 		obstacles = new ArrayList<IObstacle>(1);
		obstacles.add(new TriangleObstacle(d.getWidth() * 0.2, d
				.getHeight() * 0.2, new Position(d.getWidth() / 2, d
				.getHeight() / 2), new Color(0, 128, 255)));
 		rivers_map = new SimulationMap(obstacles, "Rivers");
 		obstacles = new ArrayList<IObstacle>(3);
		obstacles.add(new TriangleObstacle(d.getWidth() * 0.2, d
				.getHeight() * 0.2, new Position(d.getWidth() / 2, d
				.getHeight() / 2), new Color(0, 128, 255)));
 		obstacles.add(new RectangularObstacle(d.getWidth() * 0.5, d.getHeight() * 0.225, new Position(d.getWidth() * 0.5, d.getHeight() * 0.225), new Color(25, 25, 25)));
 		obstacles.add(new RectangularObstacle(d.getWidth() * 0.5, d.getHeight() * 0.225, new Position(d.getWidth() * 0.5, d.getHeight() * 0.775), new Color(25, 25, 25)));
 		tube_map = new SimulationMap(obstacles, "Tube");
 		empty_map = new SimulationMap(new ArrayList<IObstacle>(0), "No obstacles");
 		defaultMaps = new ArrayList<SimulationMap>();
 		defaultMaps.add(elliptical_map);
 		defaultMaps.add(rectangular_map);
 		defaultMaps.add(triangle_map);
 		defaultMaps.add(rivers_map);
 		defaultMaps.add(tube_map);
 	}
 }
