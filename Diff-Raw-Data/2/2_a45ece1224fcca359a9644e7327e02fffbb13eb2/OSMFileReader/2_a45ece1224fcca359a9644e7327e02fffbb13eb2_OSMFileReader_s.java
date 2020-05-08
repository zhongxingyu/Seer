 package org.life.sl.readers.osm;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import org.life.sl.graphs.PathSegmentGraph;
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.Preferences;
 import org.openstreetmap.josm.data.coor.LatLon;
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.data.osm.Node;
 import org.openstreetmap.josm.data.osm.Way;
 import org.openstreetmap.josm.data.projection.Epsg4326;
 import org.openstreetmap.josm.io.IllegalDataException;
 import org.openstreetmap.josm.io.OsmReader;
 import org.openstreetmap.josm.tools.Pair;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.CoordinateSequence;
 import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 
 //import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
 
 /**
  * @author Bernhard Snizek <besn@life.ku.dk>
  *
  */
 public class OSMFileReader {
 
 	private static PathSegmentGraph psg;
 	private static GeometryFactory gf;
 
 	public OSMFileReader() {
 		// initialize the geometry factory
 		gf = new GeometryFactory();
 		psg = new PathSegmentGraph();
 	}
 
 	/**
 	 * loads an OSM File and builds up the road Network (Path Segmented Graph)
 	 * 
 	 * @param osmFileName : OSM File Name as a String
 	 * @throws FileNotFoundException
 	 * @throws IllegalDataException
 	 * 
 	 */
 	public void readOSMFile(String osmFileName)  throws FileNotFoundException, IllegalDataException {
 
 		Main.pref = new Preferences();
 
 		Main.proj = new Epsg4326();
 		
 		Main.pref.put("tags.direction", false);
 
 		DataSet dsRestriction = OsmReader.parseDataSet(new FileInputStream(osmFileName), null);
 
 		Collection<Way> ways = dsRestriction.getWays();
 
 		for (Way way : ways) {
 			if (way.get("highway") != null) {
 				if (way.get("highway").equals("residential")) {
 
 					String roadName = way.getName();
 					System.out.println(roadName);
 					
 					List<Node> nodes = way.getNodes();
 					
 					Coordinate[] array1 = new Coordinate[nodes.size()];
 					
 					int counter = 0;
 					
 					for (Node node : nodes) {
 						LatLon ll = node.getCoor();
 						Coordinate c = new Coordinate(ll.lat(), ll.lon()); // z = 0, no elevation	
 						array1[counter] = c;
 						counter = counter +1;
 					
 					}
 					com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
 					com.vividsolutions.jts.geom.LineString lineString = fact.createLineString(array1);
 					
 					HashMap<String, Object> hm = new HashMap<String, Object>();
 					
 					hm.put("roadname", roadName);
 					hm.put("geometry", lineString);
 					lineString.setUserData(hm);
 					
					psg.addLineString(lineString);
 				}
 			}
 		}
 	}
 
 
 
 	public void readOnline() {
 
 	}
 
 	public PathSegmentGraph asLineMergeGraph() {
 		return psg;
 
 	}
 	
 
 //	/**
 //	 * @param args
 //	 * @throws FileNotFoundException
 //	 * @throws IllegalDataException
 //	 */
 //	public static void main(String[] args) throws FileNotFoundException, IllegalDataException {
 //		String filename = "testdata/testnet.osm";
 //		OSMFileReader osm_reader = new OSMFileReader();
 //		osm_reader.readOSMFile(filename);
 //		PathSegmentGraph psg = osm_reader.asLineMergeGraph();
 //	}
 
 }
