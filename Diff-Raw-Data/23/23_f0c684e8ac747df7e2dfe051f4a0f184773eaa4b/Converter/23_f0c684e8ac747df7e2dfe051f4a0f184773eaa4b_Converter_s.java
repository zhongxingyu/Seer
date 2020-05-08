 /*
     Open Aviation Map
     Copyright (C) 2012 Ákos Maróy
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as
     published by the Free Software Foundation, either version 3 of the
     License, or (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package hu.tyrell.openaviationmap.converter;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 import hu.tyrell.openaviationmap.model.Airspace;
 import hu.tyrell.openaviationmap.model.Boundary;
 import hu.tyrell.openaviationmap.model.Circle;
 import hu.tyrell.openaviationmap.model.Elevation;
 import hu.tyrell.openaviationmap.model.Point;
 import hu.tyrell.openaviationmap.model.Ring;
 import hu.tyrell.openaviationmap.model.UOM;
 import hu.tyrell.openaviationmap.model.oam.Action;
 import hu.tyrell.openaviationmap.model.oam.Oam;
 import hu.tyrell.openaviationmap.model.oam.OsmNode;
 import hu.tyrell.openaviationmap.model.oam.Way;
 
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * A command line tool to convert between various formats.
  */
 public final class Converter {
     /**
      * Private default constructor.
      */
     private Converter() {
     }
 
     /**
      * Print a help message.
      */
     private static void printHelpMessage() {
         System.out.println(
             "Open Aviation Map converter utility");
         System.out.println("");
         System.out.println(
             "usage:");
         System.out.println("");
         System.out.println(
             "  -i | --input <input.file>");
         System.out.println(
             "                             specify the input file, required");
         System.out.println(
             "  -f | --input-format <input.format>");
         System.out.println(
             "                             specify the input format, required");
         System.out.println(
             "                             supported formats: eAIP.Hungary");
         System.out.println(
             "  -o | --output <output.file>");
         System.out.println(
             "                             specify the output file, required");
         System.out.println(
             "  -F | --output-format <output.format>");
         System.out.println(
             "                             specify the output format, required");
         System.out.println(
             "                             supported formats: OAM");
         System.out.println(
             "  -c | --create");
         System.out.println(
             "                             if specified, the OAM output file");
         System.out.println(
             "                             is created in 'create' mode, for");
         System.out.println(
             "                             adding new OAM nodes & ways");
         System.out.println(
                 "  -b | --border");
         System.out.println(
                 "                         a border polygon file in OSM format");
         System.out.println(
                 "                         to be used for airspaces that refer");
         System.out.println(
                 "                         to national borders. optional");
         System.out.println(
             "  -v | --version");
         System.out.println(
             "                             specify the OAM node versions");
         System.out.println(
             "                             required if output format is OAM");
         System.out.println(
             "  -h | --help");
         System.out.println(
             "                             show this usage page");
     }
 
     /**
      * Program entry point.
      *
      * @param args command line arguments.
      */
     public static void main(String[] args) {
 
         LongOpt[] longopts = new LongOpt[8];
 
         longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
         longopts[1] = new LongOpt("input", LongOpt.REQUIRED_ARGUMENT,
                 null, 'i');
         longopts[2] = new LongOpt("input-format", LongOpt.REQUIRED_ARGUMENT,
                 null, 'f');
         longopts[3] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
                 null, 'o');
         longopts[4] = new LongOpt("output-format", LongOpt.REQUIRED_ARGUMENT,
                 null, 'F');
         longopts[5] = new LongOpt("create", LongOpt.NO_ARGUMENT,
                 null, 'c');
         longopts[6] = new LongOpt("border", LongOpt.REQUIRED_ARGUMENT,
                 null, 'b');
         longopts[7] = new LongOpt("version", LongOpt.REQUIRED_ARGUMENT,
                 null, 'v');
 
         Getopt g = new Getopt("Converter", args, "hi:f:o:F:cb:v:", longopts);
 
         int c;
 
         String                  inputFile    = null;
         String                  inputFormat  = null;
         String                  outputFile   = null;
         String                  outputFormat = null;
         boolean                 create       = false;
         String                  borderFile   = null;
         int                     version      = 0;
         List<ParseException>    errors       = new Vector<ParseException>();
 
         while ((c = g.getopt()) != -1) {
             switch (c) {
             case 'i':
                 inputFile = g.getOptarg();
                 break;
 
             case 'f':
                 inputFormat = g.getOptarg();
                 break;
 
             case 'o':
                 outputFile = g.getOptarg();
                 break;
 
             case 'F':
                 outputFormat = g.getOptarg();
                 break;
 
             case 'c':
                 create = true;
                 break;
 
             case 'b':
                 borderFile = g.getOptarg();
                 break;
 
             case 'v':
                 version = Integer.parseInt(g.getOptarg());
                 break;
 
             default:
             case 'h':
                 printHelpMessage();
                 return;
 
             case '?':
                 System.out.println("Invalid option '" + g.getOptopt()
                                    + "' specified");
                 return;
             }
         }
 
         if (inputFile == null) {
             System.out.println("Required option input not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (inputFormat == null) {
             System.out.println("Required option input-format not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (outputFile == null) {
             System.out.println("Required option output not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (outputFormat == null) {
             System.out.println("Required option output-format not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (version <= 0) {
             System.out.println("Version not specified as positive ingeger");
             System.out.println();
             printHelpMessage();
             return;
         }
 
         try {
             convert(inputFile,
                     inputFormat,
                     outputFile,
                     outputFormat,
                     create ? Action.CREATE : Action.NONE,
                     borderFile,
                     version,
                     errors);
         } catch (Exception e) {
             System.out.println("Conversion failed.");
             System.out.println();
             System.out.println("error details: " + e.toString());
             e.printStackTrace(System.out);
             return;
         }
 
         if (!errors.isEmpty()) {
             System.out.println("The following errors were encountered:");
             for (ParseException e : errors) {
                 System.out.println(e.toString());
             }
             System.out.println();
         }
 
         System.out.println("Conversion successful");
     }
 
     /**
      * Process a Circle boundary object, by turning it into a series of node
      * elements.
      *
      * @param oam the OAM object that will contain the nodes
      * @param circle the circle to process
      * @param nodeIdIx the last valid node id used, only ids above
      *        this one will be used for node ids
      * @param action specify this action for each created node
      * @param version the version of the node
      * @return the highest used node id
      */
     private static int processCircle(Oam                 oam,
                                      Circle              circle,
                                      int                 nodeIdIx,
                                      Action              action,
                                      int                 version) {
 
         int nodeIx = nodeIdIx + 1;
 
         double radiusInNm  = circle.getRadius().inUom(UOM.NM).getDistance();
         double radiusInDeg = radiusInNm / 60.0;
         double radiusLat   = radiusInDeg;
         double radiusLon   = radiusInDeg / Math.cos(
                               Math.toRadians(circle.getCenter().getLatitude()));
 
         // FIXME: calculate number of points on some required precision metric
         int totalPoints = 32;
         double tpHalf = totalPoints / 2.0;
         for (int i = 0; i < totalPoints; ++i, ++nodeIx) {
             double theta = Math.PI * i / tpHalf;
             double x = circle.getCenter().getLongitude()
                     + (radiusLon * Math.cos(theta));
             double y = circle.getCenter().getLatitude()
                     + (radiusLat * Math.sin(theta));
 
             OsmNode node = new OsmNode();
             node.setLongitude(x);
             node.setLatitude(y);
            node.setId(nodeIx);
             node.setVersion(version);
             node.setAction(action);
 
             oam.getNodes().put(nodeIx, node);
         }
 
         return nodeIx;
     }
 
     /**
      * Process a Ring boundary object, by turning it into a series of node
      * elements.
      *
      * @param oam the OAM object that will contain the nodes
      * @param ring the ring to process
      * @param nodeIdIx the last valid node id used, only ids above
      *        this one will be used for node ids
      * @param action specify this action for each created node
      * @param version the version of the node
      * @return the highest used node id
      */
     private static int processRing(Oam                 oam,
                                    Ring                ring,
                                    int                 nodeIdIx,
                                    Action              action,
                                    int                 version) {
 
         int nodeIx = nodeIdIx + 1;
 
         // omit the last node, as it will be a duplicate of the first one
         for (int i = 0; i < ring.getPointList().size() - 1; ++i, ++nodeIx) {
             Point point = ring.getPointList().get(i);
 
             OsmNode node = new OsmNode();
 
             node.setLatitude(point.getLatitude());
             node.setLongitude(point.getLongitude());
            node.setId(nodeIx);
             node.setVersion(version);
             node.setAction(action);
 
             oam.getNodes().put(nodeIx, node);
         }
 
         return nodeIx;
     }
 
     /**
      * Process a Boundary object, by turning it into a series of node
      * elements.
      *
      * @param oam the OAM object that will contain the nodes
      * @param boundary the boundary to process
      * @param nodeIdIx the last valid node id used, only ids above
      *        this one will be used for node ids
      * @param action specify this action for each created node
      * @param version the version of the node
      * @return the highest used node id
      */
     private static int processBoundary(Oam                 oam,
                                        Boundary            boundary,
                                        int                 nodeIdIx,
                                        Action              action,
                                        int                 version) {
 
         switch (boundary.getType()) {
         case RING:
             return processRing(oam, (Ring) boundary,
                                nodeIdIx, action, version);
 
         case CIRCLE:
             return processCircle(oam, (Circle) boundary,
                                  nodeIdIx, action, version);
 
         default:
             return nodeIdIx;
         }
     }
 
     /**
      * Add OAM tags related to elevation limits.
      *
      * @param airspace the airspace the limits are about
      * @param way the OAM way to add the limits to.
      */
     private static void addElevationLimits(Airspace airspace,
                                            Way      way) {
 
         if (airspace.getLowerLimit() != null) {
             Elevation elevation = airspace.getLowerLimit();
 
             way.getTags().put("height:lower",
                     Integer.toString((int) elevation.getElevation()));
             way.getTags().put("height:lower:unit",
                     elevation.getUom().toString().toLowerCase());
 
             switch (elevation.getReference()) {
             default:
             case MSL:
                 way.getTags().put("height:lower:class", "amsl");
                 break;
             case SFC:
                 way.getTags().put("height:lower:class", "agl");
                 break;
             }
         }
 
         if (airspace.getUpperLimit() != null) {
             Elevation elevation = airspace.getUpperLimit();
 
             way.getTags().put("height:upper",
                     Integer.toString((int) elevation.getElevation()));
             way.getTags().put("height:upper:unit",
                     elevation.getUom().toString().toLowerCase());
 
             switch (elevation.getReference()) {
             default:
             case MSL:
                 way.getTags().put("height:upper:class", "amsl");
                 break;
             case SFC:
                 way.getTags().put("height:upper:class", "agl");
                 break;
             }
         }
     }
 
     /**
      * Convert an Airspace list to an Oam object.
      *
      * @param airspaces the airspaces to convert
      * @param oam the Oam object to put the airspaces into
      * @param action the OAM action to set for each object
      * @param version the version to set of reach object
      * @param nodeIdIx the minimal index of unique node ids. it is assumed
      *        that any id above this index can be given to OAM nodes
      * @param wayIdIx the minimal index of unique way ids. it is assumed
      *        that any id above this index can be given to OAM ways
      */
     public static void airspacesToOam(List<Airspace> airspaces,
                                       Oam            oam,
                                       Action         action,
                                       int            version,
                                       int            nodeIdIx,
                                       int            wayIdIx) {
 
         int nIdIx = nodeIdIx;
         int wIdIx = wayIdIx;
 
         for (Airspace airspace : airspaces) {
 
             // create a node for each point in the airspace
             int minIx  = nIdIx + 1;
             int nodeIx = processBoundary(oam, airspace.getBoundary(),
                                          nIdIx, action, version);
 
             // create a 'way' object for the airspace itself
             ++wIdIx;
             Way way = new Way();
 
            way.setId(wIdIx);
             way.setVersion(version);
             way.setAction(action);
 
             // insert the node references
            for (int i = minIx; i < nodeIx; ++i) {
                way.getNodeList().add(i);
             }
            way.getNodeList().add(minIx);
 
             nIdIx = nodeIx;
 
             // insert the airspace metadata
             way.getTags().put("airspace", "yes");
 
             if (airspace.getDesignator() != null
              && !airspace.getDesignator().isEmpty()) {
 
                 way.getTags().put("icao", airspace.getDesignator());
             }
 
             if (airspace.getName() != null && !airspace.getName().isEmpty()) {
 
                 way.getTags().put("name", airspace.getName());
             }
 
             if (airspace.getRemarks() != null
              && !airspace.getRemarks().isEmpty()) {
 
                 way.getTags().put("remark", airspace.getRemarks());
             }
 
             if (airspace.getOperator() != null
              && !airspace.getOperator().isEmpty()) {
 
                 way.getTags().put("operator", airspace.getOperator());
             }
 
             if (airspace.getActiveTime() != null
              && !airspace.getActiveTime().isEmpty()) {
 
                 way.getTags().put("activetime", airspace.getActiveTime());
             }
 
             if (airspace.getType() != null) {
 
                 way.getTags().put("airspace:type", airspace.getType());
             }
 
             if (airspace.getAirspaceClass() != null) {
 
                 way.getTags().put("airspace:class",
                                   airspace.getAirspaceClass());
             }
 
             if (airspace.getCommFrequency() != null) {
 
                 way.getTags().put("comm:ctrl",
                                   airspace.getCommFrequency());
             }
 
             addElevationLimits(airspace, way);
 
             if (airspace.getBoundary().getType() == Boundary.Type.CIRCLE) {
                 Circle c = (Circle) airspace.getBoundary();
 
                 way.getTags().put("airspace:center:lat",
                         Double.toString(c.getCenter().getLatitude()));
                 way.getTags().put("airspace:center:lon",
                         Double.toString(c.getCenter().getLongitude()));
                 way.getTags().put("airspace:center:radius",
                         Double.toString(c.getRadius().getDistance()));
                 way.getTags().put("airspace:center:unit",
                         c.getRadius().getUom().toString());
             }
 
             oam.getWays().put(way.getId(), way);
         }
 
     }
 
     /**
      * Perform the conversion itself.
      *
      * @param inputFile the name of the input file
      * @param inputFormat the name of the input format
      * @param outputFile the name of the output file
      * @param outputFormat the name of the output format
      * @param action the action to specify for all OAM elements
      * @param borderFile a file describing the country border to be used
      *        for airspaces that refer to country borders. may be null
      * @param version the OAM node / way version to be set
      * @param errors all parsing errors will be put into this list
      * @throws Exception on conversion problems.
      */
     public static void convert(String                   inputFile,
                                String                   inputFormat,
                                String                   outputFile,
                                String                   outputFormat,
                                Action                   action,
                                String                   borderFile,
                                int                      version,
                                List<ParseException>     errors)
                                                            throws Exception {
 
         List<Airspace> airspaces    = new Vector<Airspace>();
         List<Point>    borderPoints = null;
 
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
         if (borderFile != null) {
             DocumentBuilder db     = dbf.newDocumentBuilder();
             Document        d      = db.parse(new FileInputStream(borderFile));
             OAMReader       reader = new OAMReader();
             Oam             oam    = new Oam();
             reader.processOsm(d.getDocumentElement(), oam, errors);
 
             if (!oam.getWays().isEmpty()) {
                 // extract the border as a point list
                 Way border = oam.getWays().values().iterator().next();
 
                 borderPoints = new Vector<Point>(border.getNodeList().size());
                 for (Integer ref : border.getNodeList()) {
                     borderPoints.add(new Point(oam.getNodes().get(ref)));
                 }
             }
         }
 
         if ("eAIP.Hungary".equals(inputFormat)) {
             Node eAipNode;
 
             DocumentBuilder db  = dbf.newDocumentBuilder();
             Document   d = db.parse(new FileInputStream(inputFile));
             eAipNode = d.getDocumentElement();
 
             EAIPHungaryReader reader = new EAIPHungaryReader();
             reader.processEAIP(eAipNode, borderPoints, airspaces, errors);
         } else {
             throw new Exception("input format " + inputFormat
                               + " not recognized");
         }
 
         if ("OAM".equals(outputFormat)) {
             // convert the airspaces to OAM
             Oam oam = new Oam();
 
             airspacesToOam(airspaces, oam, action, version, 0, 0);
 
             OAMWriter.write(oam, new FileWriter(outputFile));
 
         } else {
             throw new Exception("output format " + outputFormat
                               + " not recognized");
         }
     }
 
 }
