 /**
  * $$\\ToureNPlaner\\$$
  * 
  */
 package utils;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 
 /**
  * This utility class is used to convert the result of a /algsp request to gpx
  * and prints it on stdout
  * 
  * @author Niklas Schnelle
  * 
  */
 public class SPtoGPX {
 
 	public static void main(String[] args) {
 		if (args.length != 1) {
 			System.err
 					.println("Please supply a .json file as first parameter or - to read from stdin");
 			return;
 		}
 		InputStream input;
 		try {
 			input = args[0].equals("-") ? System.in : new FileInputStream(
 					args[0]);
 		} catch (FileNotFoundException e) {
 			System.err.println("The file: " + args[0] + " could not be found");
 			return;
 		}
 		ObjectMapper mapper = new ObjectMapper();
 
 		try {
 			Map<String, Object> requestJSON = mapper.readValue(input,
 					new TypeReference<Map<String, Object>>() {
 					});
 			@SuppressWarnings("unchecked")
 			ArrayList<Map<String, Object>> points = (ArrayList<Map<String, Object>>) requestJSON
					.get("way");
 
 			System.out
 					.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
 			System.out
 					.println("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" creator=\"Oregon 400t\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">");
 			System.out.println("  <trk>\n"
 					+ "    <name>Example GPX Document</name>");
 			System.out.println("<trkseg>");
 			for (Map<String, Object> point : points) {
 				System.out.println("<trkpt lat=\""
 						+ ((Integer) point.get("lt")).doubleValue()
 						/ 10000000.0 + "\" lon=\""
 						+ ((Integer) point.get("ln")).doubleValue()
 						/ 10000000.0 + "\"></trkpt>");
 			}
 			System.out.println("</trkseg>\n</trk>\n</gpx>");
 
 		} catch (IOException e) {
 			System.err.println("An IO Error ocurred: " + e.getMessage());
 		}
 	}
 }
