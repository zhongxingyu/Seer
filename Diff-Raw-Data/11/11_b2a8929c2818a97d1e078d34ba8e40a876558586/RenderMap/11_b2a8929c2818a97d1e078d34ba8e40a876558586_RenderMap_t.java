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
 package hu.tyrell.openaviationmap.rendering;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.image.ColorModel;
 import java.awt.image.SampleModel;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.media.jai.JAI;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.geometry.DirectPosition2D;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.map.FeatureLayer;
 import org.geotools.map.MapContent;
 import org.geotools.referencing.GeodeticCalculator;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.renderer.GTRenderer;
 import org.geotools.renderer.lite.StreamingRenderer;
 import org.geotools.styling.DefaultResourceLocator;
 import org.geotools.styling.SLDParser;
 import org.geotools.styling.Style;
 import org.jaitools.tiledimage.DiskMemImage;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.TransformException;
 import org.xml.sax.SAXException;
 
 import com.vividsolutions.jts.geom.Coordinate;
 
 /**
  * Command line utility to render a map into a bitmap, possibly for printing.
  */
 public final class RenderMap {
 
     /** The default DPI value. */
     public static final double DEFAULT_DPI = 300;
 
     /** Float formatter. */
     private static final DecimalFormat FLOAT_FORMAT =
                                                 new DecimalFormat("###.##");
 
     /**
      * Private constructor.
      */
     private RenderMap() {
     }
 
     /**
      * Print a help message.
      */
     private static void printHelpMessage() {
         System.out.println(
         "Open Aviation Map SLD scaling utility");
         System.out.println();
         System.out.println(
         "usage:");
         System.out.println();
         System.out.println(
         "  -a | --oam host,db,user,pw   the Open Aviation Map PostGIS");
         System.out.println(
         "                               database connection parameters in a");
         System.out.println(
         "                               comma-separated list");
         System.out.println(
         "  -c | --coverage A,B,C,D      map coverage in degrees, with A,B");
         System.out.println(
         "                               the lower-left, C,D the upper-right");
         System.out.println(
         "                               corner. defaults to the whole map");
         System.out.println(
         "  -d | --dpi <value>           the target device dpi");
         System.out.println(
         "                               optional, defaults to "
                                       + DEFAULT_DPI);
         System.out.println(
         "  -m | --osm host,db,user,pw   the Open Street Map PostGIS database");
         System.out.println(
         "                               connection parameters in a");
         System.out.println(
         "                               comma-separated list");
         System.out.println(
         "  -o | --output <output.file>  the output file, a TIFF image");
         System.out.println(
         "  -s | --scale <value>         the scale to generate the map in");
         System.out.println(
         "                               e.g. 1:<value>");
         System.out.println(
         "  -u | --sldurl <value>        the base URL where the SLD files are");
         System.out.println(
         "                               located");
         System.out.println(
         "  -h | --help                  show this usage page");
         System.out.println();
     }
 
     /**
      * Program entry point.
      *
      * @param args command line parameters
      * @throws IOException on I/O errors
      * @throws ParserConfigurationException on XML parser errors
      * @throws SAXException on XML parser errors
      * @throws FactoryException on CRS transformation errors
      * @throws TransformException on CRS transformation errors
      */
     public static void main(String[] args)
                                   throws IOException,
                                          SAXException,
                                          ParserConfigurationException,
                                          TransformException,
                                          FactoryException {
 
         LongOpt[] longopts = new LongOpt[8];
 
         longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
         longopts[1] = new LongOpt("oam", LongOpt.REQUIRED_ARGUMENT,
                 null, 'a');
         longopts[2] = new LongOpt("coverage", LongOpt.REQUIRED_ARGUMENT,
                 null, 'c');
         longopts[3] = new LongOpt("dpi", LongOpt.REQUIRED_ARGUMENT,
                 null, 'd');
         longopts[4] = new LongOpt("osm", LongOpt.REQUIRED_ARGUMENT,
                 null, 'm');
         longopts[5] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
                 null, 'o');
         longopts[6] = new LongOpt("scales", LongOpt.REQUIRED_ARGUMENT,
                 null, 's');
         longopts[7] = new LongOpt("sldurl", LongOpt.REQUIRED_ARGUMENT,
                 null, 'u');
 
         Getopt g = new Getopt("RenderMap", args, "a:c:d:hm:o:s:u:", longopts);
 
         int c;
 
         String      oamStr      = null;
         String      osmStr      = null;
         String      outputFile  = null;
         String      strScale    = null;
         String      strDpi      = null;
         String      sldUrlStr   = null;
         String      coverageStr = null;
 
         while ((c = g.getopt()) != -1) {
             switch (c) {
             case 'a':
                 oamStr = g.getOptarg();
                 break;
 
             case 'c':
                 coverageStr = g.getOptarg();
                 break;
 
             case 'd':
                 strDpi = g.getOptarg();
                 break;
 
             case 'm':
                 osmStr = g.getOptarg();
                 break;
 
             case 'o':
                 outputFile = g.getOptarg();
                 break;
 
             case 's':
                 strScale = g.getOptarg();
                 break;
 
             case 'u':
                 sldUrlStr = g.getOptarg();
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
 
         if (oamStr == null) {
             System.out.println("Required option oam not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (osmStr == null) {
             System.out.println("Required option osm not specified");
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
         if (strScale == null) {
             System.out.println("Required option scale not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (sldUrlStr == null) {
             System.out.println("Required option sldurl not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
 
         // normalize the base URL
         if (!sldUrlStr.endsWith("/")) {
             sldUrlStr = sldUrlStr + "/";
         }
 
         double scale;
         try {
             scale = Double.parseDouble(strScale);
         } catch (Exception e) {
             System.out.println("Error parsing scale value.");
             System.out.println();
             e.printStackTrace(System.out);
             return;
         }
 
         // parse the DPI value, if supplied
         double dpi = DEFAULT_DPI;
         if (strDpi != null) {
             try {
                 dpi = Double.parseDouble(strDpi);
             } catch (Exception e) {
                 System.out.println("Error parsing dpi value.");
                 System.out.println();
                 e.printStackTrace(System.out);
                 return;
             }
         }
 
         ReferencedEnvelope coverage = null;
         if (coverageStr != null) {
             coverage = parseCoverage(coverageStr);
         }
 
         Map<String, Object> osmParams = parseDbParams(osmStr);
         DataStore osmDataStore = DataStoreFinder.getDataStore(osmParams);
         if (osmDataStore == null) {
             System.out.println(
                     "can't connect to the Open Street Map database");
             return;
         }
 
         Map<String, Object> oamParams = parseDbParams(oamStr);
         DataStore oamDataStore = DataStoreFinder.getDataStore(oamParams);
         if (oamDataStore == null) {
             System.out.println(
                     "can't connect the to Open Aviation Map database");
             return;
         }
 
         System.out.println("Rendering map at scale 1:" + ((int) scale)
                          + " at " + ((int) dpi) + " dpi to " + outputFile);
 
         renderMap(osmDataStore, oamDataStore, coverage, sldUrlStr, scale, dpi,
                   outputFile);
     }
 
     /**
      * Render a map.
      *
      * @param osmDataStore the Open Street Map datastore to use
      * @param oamDataStore the Open Aviation Map datastore to use
      * @param coverage the coverage of the rendered map, if null, the whole
      *        map area covered by the datastores is used
      * @param sldUrlStr the base URL for SLD files &amp; related resources.
      * @param scale the scale, that is, 1:scale will be used
      * @param dpi the number of dots per inch on the target image
      * @param outputFile the name of the output TIFF file to create
      * @throws IOException on I/O errors
      * @throws FactoryException on CRS transformation errors
      * @throws TransformException on CRS transformation errors
      */
     private static void
     renderMap(DataStore             osmDataStore,
               DataStore             oamDataStore,
               ReferencedEnvelope    coverage,
               String                sldUrlStr,
               double                scale,
               double                dpi,
               String                outputFile)    throws IOException,
                                                           TransformException,
                                                           FactoryException {
 
         // create the parser with the sld configuration
         final URL sldUrl = new URL(sldUrlStr);
         SLDParser sldParser = new SLDParser(
                                     CommonFactoryFinder.getStyleFactory(null));
         DefaultResourceLocator rl = new DefaultResourceLocator();
         rl.setSourceUrl(sldUrl);
         sldParser.setOnLineResourceLocator(rl);
 
         MapContent map = new MapContent();
 
         System.out.println("Opening Open Street Map database...");
 
         // add the ground layers
         addLayer(osmDataStore, sldParser, sldUrl,
                 "planet_osm_polygon", "oam_waters.sld", scale, dpi, map);
         addLayer(osmDataStore, sldParser, sldUrl,
                 "planet_osm_polygon", "oam_forests.sld", scale, dpi, map);
         addLayer(osmDataStore, sldParser, sldUrl,
                 "planet_osm_polygon", "oam_cities.sld", scale, dpi, map);
         addLayer(osmDataStore, sldParser, sldUrl,
                 "planet_osm_point", "oam_peaks.sld", scale, dpi, map);
         addLayer(osmDataStore, sldParser, sldUrl,
                 "planet_osm_point", "oam_city_markers.sld", scale, dpi, map);
         addLayer(osmDataStore, sldParser, sldUrl,
                 "planet_osm_line", "oam_roads.sld", scale, dpi, map);
         addLayer(osmDataStore, sldParser, sldUrl,
                 "planet_osm_point", "oam_labels.sld", scale, dpi, map);
 
 
         System.out.println("Opening Open Aviation Map database...");
 
         // add the aviation layers
         addLayer(oamDataStore, sldParser, sldUrl,
                 "planet_osm_polygon", "oam_airspaces.sldt", scale, dpi, map);
         addLayer(oamDataStore, sldParser, sldUrl,
                  "planet_osm_point", "oam_navaids.sldt", scale, dpi, map);
         addLayer(oamDataStore, sldParser, sldUrl,
                 "planet_osm_line", "oam_runways.sldt", scale, dpi, map);
 
 
         // calculate map coverage and image size
         ReferencedEnvelope mapBounds;
         if (coverage == null) {
             mapBounds = map.getMaxBounds();
         } else {
             mapBounds = coverage.transform(map.getCoordinateReferenceSystem(),
                                            false);
         }
 
         ReferencedEnvelope mapBoundsWgs84 =
                 mapBounds.transform(DefaultGeographicCRS.WGS84, false);
 
         System.out.println("Map coverage: "
                + FLOAT_FORMAT.format(mapBoundsWgs84.getMinX())
                    + "\u00b0," + FLOAT_FORMAT.format(mapBoundsWgs84.getMinY())
                + "\u00b0 x "
                + FLOAT_FORMAT.format(mapBoundsWgs84.getMaxX())
                    + "\u00b0," + FLOAT_FORMAT.format(mapBoundsWgs84.getMaxY())
                    + "\u00b0");
 
         Rectangle imageBounds = calcImageBounds(scale, dpi, mapBounds,
                                         map.getCoordinateReferenceSystem());
 
 
         System.out.println("Image size: " + ((int) imageBounds.getWidth())
                 + "x" + ((int) imageBounds.getHeight()) + " pixels");
 
 
         saveMap(map, mapBounds, imageBounds, dpi, outputFile);
     }
 
     /**
      * Parse a db parameter string, and create a parameter object that
      * is acceptable by the GeoTools DataStoreFinder.
      *
      * @param paramStr the parameter string
      * @return a map that can be used by DataStoreFinder.getDataStore()
      *         to find a data store
      */
     private static Map<String, Object>
     parseDbParams(String paramStr) {
         Map<String, Object> params = new HashMap<String, Object>();
 
         StringTokenizer tok = new StringTokenizer(paramStr, ",");
         if (tok.countTokens() != 4) {
             throw new IllegalArgumentException(
                     "wrong number of commas in DB parameter string");
         }
 
         params.put("dbtype", "postgis");
         params.put("host", tok.nextToken());
         params.put("port", 5432);
         params.put("schema", "public");
         params.put("database", tok.nextToken());
         params.put("user", tok.nextToken());
         params.put("passwd", tok.nextToken());
 
         return params;
     }
 
     /**
      * Add a layer to a map.
      *
      * @param dataStore the data store to add the layer from
      * @param sldParser the SLD parser to use for parsing SLDs
      * @param urlBase the base URL for the SLD & related resources
      * @param featureName the name of the feature from the data store
      * @param styleName the name of the SLD file to use
      * @param scale the scale of the rendering, used to re-scale SLD templates
      * @param dpi the target DPI, used to re-scale SLD templates
      * @param map the map to add the layer to
      * @throws IOException on I/O errors
      */
     private static void
     addLayer(DataStore          dataStore,
              SLDParser          sldParser,
              final URL          urlBase,
              String             featureName,
              String             styleName,
              double             scale,
              double             dpi,
              MapContent         map) throws IOException {
 
         if (styleName.endsWith(".sldt")) {
 
             CoordinateReferenceSystem crs = map.getCoordinateReferenceSystem();
             String crsName = crs.getIdentifiers().iterator().next().toString();
 
             Coordinate centerPoint = map.getMaxBounds().centre();
 
             try {
                 Reader scaledSld = scaleSld(urlBase,
                                             styleName,
                                             crsName,
                                             scale,
                                             dpi,
                                             centerPoint);
 
                 sldParser.setInput(scaledSld);
                 Style[] styles = sldParser.readXML();
                 FeatureLayer layer = new FeatureLayer(
                                      dataStore.getFeatureSource(featureName),
                                      styles[0]);
                 map.addLayer(layer);
             } catch (Exception e) {
                 System.out.println("error scaling SLD template " + styleName);
             }
         } else {
             sldParser.setInput(new URL(urlBase + styleName));
             Style[] styles = sldParser.readXML();
             FeatureLayer layer = new FeatureLayer(
                                      dataStore.getFeatureSource(featureName),
                                      styles[0]);
             map.addLayer(layer);
         }
     }
 
     /**
      * Scale an SLD template into an SLD, tailor made for the specified
      * scale and DPI value.
      *
      * @param urlBase the base URL where to find the SLD template
      * @param styleName the name of the SLD template, relative to urlBase
      * @param crsName the name of the CRS to use for scaling
      * @param scale the target scale, which is 1:scale
      * @param dpi the target DPI
      * @param centerPoint a reference point to calculate real-world scale,
      *        in CRS notation
      * @return the transformed SLD
      * @throws Exception on scaling errors
      */
     private static Reader
     scaleSld(URL        urlBase,
             String      styleName,
             String      crsName,
             double      scale,
             double      dpi,
             Coordinate  centerPoint) throws Exception {
 
         URL    url    = new URL(urlBase + styleName);
         Reader reader = new InputStreamReader(url.openStream());
 
         List<Double> scales = new ArrayList<Double>(2);
         scales.add(scale * 0.75d);
         scales.add(scale * 1.25d);
 
         double[] refXY = {centerPoint.x, centerPoint.y};
 
         StringWriter output = new StringWriter();
 
         ScaleSLD.scaleSld(reader, scales, dpi, crsName, refXY, output);
 
         StringReader result = new StringReader(output.toString());
 
         return result;
     }
 
     /**
      * Save a map image.
      *
      * @param map the map to save
      * @param mapBounds the part of the map to render
      * @param imageBounds the size of the image to render
      * @param dpi the DPI of rendering
      * @param file the name of the file to save to
      * @throws FactoryException on CRS transformation errors
      * @throws TransformException on CRS transformation errors
      */
     public static void
     saveMap(final MapContent          map,
             final ReferencedEnvelope  mapBounds,
             final Rectangle           imageBounds,
             final double              dpi,
             final String              file)
                                                   throws TransformException,
                                                          FactoryException {
 
         // set up the renderer
         GTRenderer renderer = new StreamingRenderer();
         renderer.setMapContent(map);
 
         Map<Object, Object> rendererParams = new HashMap<Object, Object>();
         rendererParams.put(StreamingRenderer.DPI_KEY, new Double(dpi));
         rendererParams.put(StreamingRenderer.SCALE_COMPUTATION_METHOD_KEY,
                            StreamingRenderer.SCALE_ACCURATE);
         rendererParams.put(StreamingRenderer.ADVANCED_PROJECTION_HANDLING_KEY,
                            new Boolean(true));
         rendererParams.put(StreamingRenderer.VECTOR_RENDERING_KEY,
                             new Boolean(true));
         renderer.setRendererHints(rendererParams);
 
         RenderingHints hints2D =
                 new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
         hints2D.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         renderer.setJava2DHints(hints2D);
 
         // render the map
         ColorModel cm = ColorModel.getRGBdefault();
         SampleModel sm = cm.createCompatibleSampleModel(1024, 1024);
 
         DiskMemImage image = new DiskMemImage(0, 0,
                                          imageBounds.width, imageBounds.height,
                                          0, 0, sm, cm);
 
         Graphics2D gr = image.createGraphics();
         // TOOD: maybe we want this transparent
         gr.setPaint(Color.WHITE);
         gr.fill(imageBounds);
 
         System.out.println("Rendering map...");
 
         renderer.paint(gr, imageBounds, mapBounds);
 
         JAI.create("filestore", image, file, "TIFF", null);
 
         System.out.println("Map saved to " + file);
     }
 
     /**
      * Calculate the size of the generated image.
      *
      * @param scale the scale to use for generating the image
      * @param dpi the dots per inch in the image
      * @param mapBounds the bounds of the map used for generating the image
      * @param crs the CRS of the map (reference of the map bounds)
      * @return the rectangle depicting the size of the generated image
      * @throws TransformException on coordinate transformation errors
      */
     private static Rectangle
     calcImageBounds(final double                scale,
                     final double                dpi,
                     ReferencedEnvelope          mapBounds,
                     CoordinateReferenceSystem   crs)
                                                 throws TransformException {
         Rectangle imageBounds;
         // calculate the width of the area in meters
         double[] sp = {mapBounds.getMinimum(0), mapBounds.getMinimum(1)};
         double[] dp = {mapBounds.getMaximum(0), mapBounds.getMinimum(1)};
 
         GeodeticCalculator gc = new GeodeticCalculator(crs);
 
         gc.setStartingPosition(new DirectPosition2D(crs, sp[0], sp[1]));
         gc.setDestinationPosition(new DirectPosition2D(crs, dp[0], dp[1]));
 
         double widthInMeters = gc.getOrthodromicDistance();
         double dotInMeters = 0.0254d / dpi;
         double imageWidth = widthInMeters / (scale * dotInMeters);
 
         double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
         imageBounds = new Rectangle(
                             0, 0, (int) imageWidth,
                             (int) Math.round(imageWidth * heightToWidth));
 
         return imageBounds;
     }
 
     /**
      * Parse the map coverage string parameter and create a WGS84 referenced
      * envelope out of it.
      *
      * @param coverageStr the coverage string to parse, in A,B,C,D format,
      *        which is coordinates in degrees
      * @return the corresponding referenced envelope
      */
     private static ReferencedEnvelope
     parseCoverage(String coverageStr) {
 
         StringTokenizer tok = new StringTokenizer(coverageStr, ",");
         if (tok.countTokens() != 4) {
             throw new IllegalArgumentException(
                     "incorrect coverage string: " + coverageStr);
         }
 
        double x1 = Double.parseDouble(tok.nextToken());
        double y1 = Double.parseDouble(tok.nextToken());
        double x2 = Double.parseDouble(tok.nextToken());
        double y2 = Double.parseDouble(tok.nextToken());

         ReferencedEnvelope coverage = new ReferencedEnvelope(
                                           x1, x2, y1, y2,
                                            DefaultGeographicCRS.WGS84);
 
         return coverage;
     }
 
 }
