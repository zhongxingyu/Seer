 package fr.nantes1900.models.islets.steps;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.filechooser.FileSystemView;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import fr.nantes1900.models.basis.Edge;
 import fr.nantes1900.models.basis.Mesh;
 import fr.nantes1900.models.basis.Point;
 import fr.nantes1900.models.basis.Polygon;
 import fr.nantes1900.models.basis.Triangle;
 import fr.nantes1900.models.decimation.Decimator;
 import fr.nantes1900.models.exceptions.ImpossibleProjectionException;
 import fr.nantes1900.models.extended.Building;
 import fr.nantes1900.models.extended.Ground;
 import fr.nantes1900.models.extended.Roof;
 import fr.nantes1900.models.extended.Wall;
 import fr.nantes1900.models.islets.AbstractBuildingsIslet;
 import fr.nantes1900.utils.AbstractWriter;
 import fr.nantes1900.utils.CityGMLWriter;
 import fr.nantes1900.utils.STLWriter;
 
 /**
  * Implements a step of the process. This step is after the determination of the
  * neighbours and before the sort of the neighbours.
  * 
  * @author Daniel Lefèvre
  */
 public class BuildingsIsletStep6 extends AbstractBuildingsIsletStep implements Writable {
 
 	/**
 	 * The list of buildings.
 	 */
 	private List<Building> buildings;
 	/**
 	 * The grounds.
 	 */
 	private Ground grounds;
 
 	/**
 	 * Association of borders and building they sould be resticked with.
 	 */
 	private HashMap<Polygon, Building> bordersToRestick;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param buildingsIn
 	 *            the list of buildings
 	 * @param groundsIn
 	 *            te grounds
 	 */
 	public BuildingsIsletStep6(final List<Building> buildingsIn,
 			final Ground groundsIn) {
 		this.buildings = buildingsIn;
 		this.grounds = groundsIn;
 	}
 
 	/**
 	 * Getter.
 	 * 
 	 * @return the list of buildings
 	 */
 	public final List<Building> getBuildings() {
 		return this.buildings;
 	}
 
 	/**
 	 * Getter.
 	 * 
 	 * @return the grounds
 	 */
 	public final Ground getGrounds() {
 		return this.grounds;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.nantes1900.models.islets.steps.AbstractBuildingsIsletStep
 	 * #launchProcess()
 	 */
 	@Override
 	public final BuildingsIsletStep7 launchProcess() {
 		// Decimation
 		Decimator decim = new Decimator(this.grounds.getMesh());
 		this.grounds.setMesh(decim.launchDecimation());
		
		// resticking to buildings
 		this.rmvTrianglesInsideBuildings();
 		this.findBordersToRestick();
 		this.projectBordersOnWalls();
 		this.grounds.getMesh().refresh();
 
 		for (Building b : this.buildings) {
 			b.launchProcess6();
 		}
 
 		writesResult();
 
 		return new BuildingsIsletStep7(this.buildings, this.grounds);
 	}
 
 	/**
 	 * Temporary methods to writes simplified buildings and ground in the same
 	 * file in order to watch results with the program.
 	 */
 	private void writesResult() {
         String fileName = new String(FileSystemView.getFileSystemView().getDefaultDirectory() + "/lastResult.stl");
         
         STLWriter writer = new STLWriter(fileName, this);
         
         writer.makeFileFromWritable();
         
         writer.write();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.nantes1900.models.islets.steps.AbstractBuildingsIsletStep
 	 * #returnNode()
 	 */
 	@Override
 	public final DefaultMutableTreeNode returnNode() {
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode(this);
 
 		int counter = 0;
 		for (Building b : this.buildings) {
 			root.add(b.returnNode6(counter));
 			counter++;
 		}
 
 		if (!this.getGrounds().getMesh().isEmpty()) {
 			this.grounds.setNodeString("Sols");
 			root.add(new DefaultMutableTreeNode(this.grounds));
 		}
 
 		return root;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.nantes1900.models.islets.steps.AbstractBuildingsIsletStep
 	 * #toString()
 	 */
 	@Override
 	public final String toString() {
 		return super.toString() + AbstractBuildingsIslet.SIXTH_STEP;
 	}
 
 	/**
 	 * Removes triangles from the ground that have a point inside the building.
 	 * 
 	 * @throws ImpossibleProjectionException
 	 *             if one surface has not been simplified
 	 */
 	private void rmvTrianglesInsideBuildings() {
 		Mesh toRemove = new Mesh();
 
 		this.grounds.getMesh().writeSTL("rmvTrianglesBefore");
 		for (Building b : this.buildings) {
 			com.vividsolutions.jts.geom.Polygon polygon;
 			try {
 				polygon = getGroundProjection(b.getbStep6().getWalls());
 
 				// Looks for each triangle of the ground.
 				for (Triangle tri : this.grounds.getMesh()) {
 					if (polygon.intersects(new Polygon(tri.getEdges())
 							.convertPolygonToJts())) {
 						toRemove.add(tri);
 					}
 				}
 			} catch (ImpossibleProjectionException e) {
 				// Do nothing : does not try to restick to this building
 			}
 		}
 		this.grounds.getMesh().remove(toRemove);
 	}
 
 	/**
 	 * Associates each building with a border and stores them into the borders
 	 * to restick map.
 	 * 
 	 * @throws ImpossibleProjectionException
 	 *             if one surface has not been simplified
 	 */
 	private void findBordersToRestick() {
 		this.bordersToRestick = new HashMap<>();
 
 		// Gets ground borders
 		List<Polygon> borders = this.grounds.getMesh().returnSortedBorders();
 
 		BuildingsIsletStep6.removeExternalBorder(borders);
 
 		// Matches buildings with borders
 		for (Polygon border : borders) {
 			// Finds buildings contained in the border
 			int counter = 0;
 			Building matchingBuilding = null;
 			for (Building building : this.buildings) {
 				try {
 					if (border.containsWithJts(getGroundProjection(building
 							.getbStep6().getWalls()))) {
 						counter++;
 						matchingBuilding = building;
 					}
 				} catch (ImpossibleProjectionException e) {
 					// Do not try to restick with this building
 				}
 			}
 			// If the border contains only one building, we can associate them
 			if (counter == 1) {
 				this.bordersToRestick.put(border, matchingBuilding);
 			}
 		}
 	}
 
 	/**
 	 * Projects every edge of a border to the lowest edge of the closest wall.
 	 * Needs as previous step that borders and building have been associated.
 	 */
 	private void projectBordersOnWalls() {
 		Set<Polygon> borders = this.bordersToRestick.keySet();
 		for (Polygon border : borders) {
 			List<Wall> walls = this.bordersToRestick.get(border).getbStep6()
 					.getWalls();
 
 			// Gets the basis of the building
 			List<Edge> downEdges = new ArrayList<>();
 			for (Wall wall : walls) {
 				downEdges.add(wall.getPolygon().getDownEdge());
 			}
 
 			for (Point p : border.getPointList()) {
 				Point pProj = getCloserProjectedPointOnEdge(p, downEdges);
 				p.set(pProj.getPointAsCoordinates());
 			}
 
 			Map<Edge, List<Point>> map = getEdgePointsMap(
 					border.getPointList(), downEdges);
 
 			// Fixes angles issues.
 			for (Edge edge : downEdges) {
 				Point wallPoint = edge.getP1();
 				// If it is empty, it means that this edge is probably no
 				// touching the ground.
 				if (!map.get(edge).isEmpty()) {
 					Point pClose = wallPoint.getCloser(map.get(edge));
 					pClose.set(wallPoint.getPointAsCoordinates());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the ground projection of a list of walls which make a building.
 	 * 
 	 * @param walls
 	 *            the list of a building's walls
 	 * @return polygon of the ground projection of walls
 	 * @throws ImpossibleProjectionException
 	 *             if one surface (wall or roof) has not been simplified
 	 */
 	private static com.vividsolutions.jts.geom.Polygon getGroundProjection(
 			final List<Wall> walls) throws ImpossibleProjectionException {
 		Polygon poly = new Polygon();
 		for (Wall w : walls) {
 			// Checks if the wall is well simplified
 			if (w.getPolygon() == null
 					|| w.getPolygon().getPointList().size() <= 2) {
 				throw new ImpossibleProjectionException();
 			}
 
 			// Gets the lowest edge of the wall
 			Edge wallEdge = w.getPolygon().getDownEdge();
 			if (wallEdge == null) {
 				throw new ImpossibleProjectionException();
 			}
 
 			poly.add(wallEdge);
 		}
 
 		List<Edge> orderedPoly = new ArrayList<>();
 		Edge e = poly.getOne();
 		e.returnNeighbours(poly.getEdgeList(), orderedPoly);
 
 		poly = new Polygon(orderedPoly);
 
 		// Transforms into jts structure
 		return poly.convertPolygonToJts();
 	}
 
 	/**
 	 * Searches for the external border of the ground : the border which
 	 * contains all the others. Then removes it.
 	 * 
 	 * @param borders
 	 *            the list of borders
 	 */
 	private static void removeExternalBorder(final List<Polygon> borders) {
 		Polygon externalBorder = null;
 		for (Polygon border : borders) {
 			if (border.containsAllWithJts(borders)) {
 				externalBorder = border;
 			}
 		}
 		borders.remove(externalBorder);
 	}
 
 	/**
 	 * Find the closest projection of the point on the edges.
 	 * 
 	 * @param p
 	 *            the point
 	 * @param edges
 	 *            the list of edges
 	 * @return the projected point, closest to p
 	 */
 	public static Point getCloserProjectedPointOnEdge(final Point p,
 			final List<Edge> edges) {
 		Point pProj = edges.get(0).getP1();
 		Point pProjCurrent;
 		double distance = edges.get(0).getP1().distance(p);
 
 		for (Edge downEdge : edges) {
 			pProjCurrent = downEdge.project(p);
 			if (pProjCurrent.distance(p) < distance) {
 				pProj = pProjCurrent;
 				distance = pProjCurrent.distance(p);
 			}
 		}
 
 		return pProj;
 	}
 
 	/**
 	 * Builds a map associating the edges and the points which can will be
 	 * projected on these edges.
 	 * 
 	 * @param points
 	 *            the list of all points
 	 * @param edges
 	 *            the list of all edges
 	 * @return the map
 	 */
 	private static Map<Edge, List<Point>> getEdgePointsMap(
 			final List<Point> points, final List<Edge> edges) {
 
 		Map<Edge, List<Point>> map = new HashMap<>();
 		for (Edge edge : edges) {
 			map.put(edge, new ArrayList<Point>());
 		}
 		Point proj;
 
 		for (Point point : points) {
 			for (Edge edge : edges) {
 				proj = edge.project(point);
 				if (!proj.equals(edge.getP1()) && !proj.equals(edge.getP2())) {
 					map.get(edge).add(point);
 				}
 			}
 		}
 
 		return map;
 	}
 }
