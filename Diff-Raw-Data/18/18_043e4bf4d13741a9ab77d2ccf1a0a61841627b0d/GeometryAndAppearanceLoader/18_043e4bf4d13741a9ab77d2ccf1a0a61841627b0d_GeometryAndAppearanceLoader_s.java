 package se2.e.engine3d;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import se2.e.geometry.Geometry;
 import se2.e.geometry.GeometryObject;
 import se2.e.geometry.Position;
 import se2.e.geometry.SimplePosition;
 import se2.e.geometry.Track;
 import se2.e.utilities.Vector2D;
 import appearance.AppearanceInfo;
 import appearance.AppearanceModel;
 
 /**
  * The GeometryAndAppearanceLoader handles the loading of the geometry and appearance configurations.
  * 
  * @author marius, cosmin
  */
 public class GeometryAndAppearanceLoader {
 
 	/** The track objects. */
 	private HashMap<String, Vector2D[]> trackObjects;
 
 	/** The simple position objects. */
 	private HashMap<String, SimplePosition> simplePositionObjects;
 
 	/** The appearance information. */
 	private HashMap<String, AppearanceInfo> appearanceInfo;
 
 	/** The log. */
 	private Logger log = Logger.getLogger("GeometryAndAppearanceLoader");
 
 	/** The min x. */
 	public double minX = Double.MAX_VALUE;
 
 	/** The max x. */
 	public double maxX = Double.MIN_VALUE;
 
 	/** The min y. */
 	public double minY = Double.MAX_VALUE;
 
 	/** The max y. */
 	public double maxY = Double.MIN_VALUE;
 
 	/**
 	 * Updates the limits based on a position
 	 * 
 	 * @param pos the position
 	 * @author cosmin
 	 */
 	private void updateLimits(Position pos) {
 		if (pos.getX() > maxX)
 			maxX = pos.getX();
 		if (pos.getX() < minX)
 			minX = pos.getX();
 		if (pos.getY() > maxY)
 			maxY = pos.getY();
 		if (pos.getY() < minY)
 			minY = pos.getY();
 	}
 
 	/**
 	 * Builds the Vector2D object.
 	 * 
 	 * @param pos the position
 	 * @return the vector2D
 	 */
 	private Vector2D buildVector2D(Position pos) {
 		return Vector2D.cartesian(pos.getX(), pos.getY());
 	}
 
 	/**
 	 * Instantiates a new geometry loader, loading the geometry and appearance configurations and connecting them with
 	 * each-other.
 	 * 
 	 * @param geometry the geometry
 	 * @param appearance the appearance
 	 * @author cosmin, marius
 	 */
 	public GeometryAndAppearanceLoader(Geometry geometry, AppearanceModel appearance) {
 		log.info("Loading geometry and appearance configurations...");
 		List<GeometryObject> geomObjs = geometry.getGeoObjects();
 		trackObjects = new HashMap<String, Vector2D[]>();
 		simplePositionObjects = new HashMap<String, SimplePosition>();
 		// Load up geometry objects
 		for (GeometryObject geomObj : geomObjs) {
 			if (geomObj instanceof Track) {
 				Track track = (Track) geomObj;
 				// Build the track points array for the track
 				Vector2D trackPoints[] = new Vector2D[track.getIntermediatePositions().size() + 2];
 				int curIndex = 0;
 				// Start points
 				trackPoints[curIndex++] = buildVector2D(track.getStartPosition().getPosition());
 				updateLimits(track.getStartPosition().getPosition());
 				// IntermediatePoints
 				for (Position pos : track.getIntermediatePositions()) {
 					updateLimits(pos);
 					trackPoints[curIndex++] = buildVector2D(pos);
 				}
 				// EndPoint
 				trackPoints[curIndex++] = buildVector2D(track.getEndPosition().getPosition());
 				updateLimits(track.getEndPosition().getPosition());
 				System.out.println(Arrays.toString(trackPoints));
 				trackObjects.put(track.getLabel(), trackPoints);
 			} else if (geomObj instanceof SimplePosition) {
 				simplePositionObjects.put(((SimplePosition) geomObj).getLabel(), (SimplePosition) geomObj);
 				updateLimits(((SimplePosition) geomObj).getPosition());
 			}
 		}
 		log.info("Loaded trackPoints for geometry: " + trackObjects);
 		log.info("Loaded simplePositionObjects geometry: " + simplePositionObjects);
 		log.info(String.format("Limits - X: (%f,  %f), Y: (%f, %f)", minX, maxX, minY, maxY));
 
 		// Load up appearance info
 		List<AppearanceInfo> appInfoObjs = appearance.getAppearanceInfos();
 		appearanceInfo = new HashMap<String, AppearanceInfo>();
 		for (AppearanceInfo appInfo : appInfoObjs) {
 			appearanceInfo.put(appInfo.getLabel(), appInfo);
 		}
 		log.info("Loaded appearanceInfo: " + appearanceInfo);
 	}
 
 	/**
 	 * Gets the track points for a track with a given name.
 	 * 
 	 * @param label the label
 	 * @return the track points
 	 */
 	public Vector2D[] getTrackPoints(String label) {
 		return trackObjects.get(label);
 	}
 
 	/**
 	 * Gets the simple position object with a given name.
 	 * 
 	 * @param label the label
 	 * @return the simple position object
 	 */
 	public SimplePosition getSimplePositionObject(String label) {
 		return simplePositionObjects.get(label);
 	}
 
 	/**
 	 * Gets the appearance info.
 	 * 
 	 * @param label the label
 	 * @return the appearance info
 	 */
 	public AppearanceInfo getAppearanceInfo(String label) {
 		return appearanceInfo.get(label);
 	}
 
 	/**
 	 * Gets the track labels.
 	 * 
 	 * @return the track labels
 	 */
 	public Set<String> getTrackLabels() {
 		return trackObjects.keySet();
 	}
 
 	/**
 	 * Gets the simple position labels.
 	 * 
 	 * @return the simple position labels
 	 */
 	public Set<String> getSimplePositionLabels() {
 		return simplePositionObjects.keySet();
 	}
 
 	/**
 	 * Gets the appearance info labels.
 	 * 
 	 * @return the appearance info labels
 	 */
 	public Set<String> getAppearanceInfoLabels() {
 		return appearanceInfo.keySet();
 	}
 }
