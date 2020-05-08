 package org.vfny.geoserver.wms.responses.map;
 
 import java.awt.Color;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Panel;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.namespace.QName;
 
 import junit.framework.Test;
 
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.StyleInfo;
 import org.geoserver.data.test.MockData;
 import org.geoserver.wms.WMSTestSupport;
 import org.geotools.data.FeatureSource;
 import org.geotools.filter.IllegalFilterException;
 import org.geotools.map.FeatureSourceMapLayer;
 import org.geotools.styling.Style;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.responses.DefaultRasterMapProducer;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 public class DefaultRasterMapProducerTest extends WMSTestSupport {
     /** DOCUMENT ME! */
     private static final int SHOW_TIMEOUT = 200;
 
     /** DOCUMENT ME! */
     private static final boolean INTERACTIVE = false;
 
     /** DOCUMENT ME! */
     private static final Logger LOGGER = org.geotools.util.logging.Logging
             .getLogger(DefaultRasterMapProducerTest.class.getPackage().getName());
 
     /** DOCUMENT ME! */
     private static final Color BG_COLOR = Color.white;
 
     /** DOCUMENT ME! */
     private DefaultRasterMapProducer rasterMapProducer;
 
     /**
      * This is a READ ONLY TEST so we can use one time setup
      */
     public static Test suite() {
         return new OneTimeTestSetup(new DefaultRasterMapProducerTest());
     }
 
     /**
      * DOCUMENT ME!
      * 
      * @throws Exception
      *             DOCUMENT ME!
      */
     public void setUpInternal() throws Exception {
         super.setUpInternal();
         this.rasterMapProducer = getProducerInstance();
     }
 
     /**
      * DOCUMENT ME!
      * 
      * @return DOCUMENT ME!
      */
     protected DefaultRasterMapProducer getProducerInstance() {
         return new DummyRasterMapProducer();
     }
 
     /**
      * DOCUMENT ME!
      * 
      * @throws Exception
      *             DOCUMENT ME!
      */
     public void tearDownInternal() throws Exception {
         this.rasterMapProducer = null;
         super.tearDownInternal();
     }
 
     /**
      * DOCUMENT ME!
      * 
      * @throws Exception
      *             DOCUMENT ME!
      */
     public void testSimpleGetMapQuery() throws Exception {
         final String mapFormat = "image/gif";
 
         
        Catalog catalog = getCatalog();
        final FeatureSource fs = catalog.getFeatureTypeByName(MockData.BASIC_POLYGONS.getPrefix(),
                 MockData.BASIC_POLYGONS.getLocalPart()).getFeatureSource(null, null);
         
         final Envelope env = fs.getBounds();
 
         LOGGER.info("about to create map ctx for BasicPolygons with bounds " + env);
 
         final WMSMapContext map = new WMSMapContext();
         map.setAreaOfInterest(env);
         map.setMapWidth(300);
         map.setMapHeight(300);
         map.setBgColor(Color.red);
         map.setTransparent(false);
        map.setRequest(new GetMapRequest(getWMS()));
 
        StyleInfo styleByName = catalog.getStyleByName("Default");
        Style basicStyle = styleByName.getStyle();
         map.addLayer(fs, basicStyle);
 
         this.rasterMapProducer.setOutputFormat(mapFormat);
         this.rasterMapProducer.setMapContext(map);
         this.rasterMapProducer.produceMap();
 
         assertNotBlank("testSimpleGetMapQuery", this.rasterMapProducer);
     }
 
     /**
      * DOCUMENT ME!
      * 
      * @throws Exception
      *             DOCUMENT ME!
      */
     public void testDefaultStyle() throws Exception {
         List<org.geoserver.catalog.FeatureTypeInfo> typeInfos = getCatalog().getFeatureTypes();
 
         for (org.geoserver.catalog.FeatureTypeInfo info : typeInfos) {
             if (info.getQualifiedName().getNamespaceURI().equals(MockData.CITE_URI)
                     && info.getFeatureType().getGeometryDescriptor() != null)
                 testDefaultStyle(info.getFeatureSource(null, null));
         }
     }
 
     /**
      * DOCUMENT ME!
      * 
      * @throws IOException
      *             DOCUMENT ME!
      * @throws IllegalFilterException
      *             DOCUMENT ME!
      * @throws IOException
      *             DOCUMENT ME!
      */
     public void testBlueLake() throws IOException, IllegalFilterException, Exception {
         final Catalog catalog = getCatalog();
         org.geoserver.catalog.FeatureTypeInfo typeInfo = catalog.getFeatureTypeByName(MockData.LAKES.getNamespaceURI(), MockData.LAKES.getLocalPart());
        Envelope env = typeInfo.getFeatureSource(null, null).getBounds();
         double shift = env.getWidth() / 6;
 
         env = new Envelope(env.getMinX() - shift, env.getMaxX() + shift, env.getMinY() - shift, env
                 .getMaxY()
                 + shift);
 
         final WMSMapContext map = new WMSMapContext();
         int w = 400;
         int h = (int) Math.round((env.getHeight() * w) / env.getWidth());
         map.setMapWidth(w);
         map.setMapHeight(h);
         map.setBgColor(BG_COLOR);
         map.setTransparent(true);
        map.setRequest(new GetMapRequest(getWMS()));
 
         addToMap(map, MockData.FORESTS);
         addToMap(map, MockData.LAKES);
         addToMap(map, MockData.STREAMS);
         addToMap(map, MockData.NAMED_PLACES);
         addToMap(map, MockData.ROAD_SEGMENTS);
         addToMap(map, MockData.PONDS);
         addToMap(map, MockData.BUILDINGS);
         addToMap(map, MockData.DIVIDED_ROUTES);
         addToMap(map, MockData.BRIDGES);
         addToMap(map, MockData.MAP_NEATLINE);
 
         map.setAreaOfInterest(env);
 
         this.rasterMapProducer.setOutputFormat("image/gif");
         this.rasterMapProducer.setMapContext(map);
         this.rasterMapProducer.produceMap();
 
         assertNotBlank("testBlueLake", this.rasterMapProducer);
     }
 
     private void addToMap(final WMSMapContext map, final QName typeName) throws IOException {
         final FeatureTypeInfo ftInfo = getCatalog().getFeatureTypeByName(typeName.getNamespaceURI(), typeName.getLocalPart());
         
         List<LayerInfo> layers = getCatalog().getLayers(ftInfo);
         StyleInfo defaultStyle = layers.get(0).getDefaultStyle();
         Style style = defaultStyle.getStyle();
         
         map.addLayer(new FeatureSourceMapLayer(ftInfo.getFeatureSource(null, null), style));
     }
 
     /**
      * Checks that the image generated by the map producer is not blank.
      * 
      * @param testName
      * @param producer
      */
     protected void assertNotBlank(String testName, DefaultRasterMapProducer producer) {
         BufferedImage image = (BufferedImage) producer.getImage();
         assertNotBlank(testName, image, BG_COLOR);
         showImage(testName, image);
     }
 
     /**
      * DOCUMENT ME!
      * 
      * @param frameName
      *            DOCUMENT ME!
      * @param image
      *            DOCUMENT ME!
      */
     protected void showImage(String frameName, final BufferedImage image) {
         showImage(frameName, SHOW_TIMEOUT, image);
     }
 
     /**
      * Shows <code>image</code> in a Frame.
      * 
      * @param frameName
      * @param timeOut
      * @param image
      */
     protected void showImage(String frameName, long timeOut, final BufferedImage image) {
         int width = image.getWidth();
         int height = image.getHeight();
 
         if (((System.getProperty("java.awt.headless") == null) || !System.getProperty(
                 "java.awt.headless").equals("true"))
                 && INTERACTIVE) {
             Frame frame = new Frame(frameName);
             frame.addWindowListener(new WindowAdapter() {
                 public void windowClosing(WindowEvent e) {
                     e.getWindow().dispose();
                 }
             });
 
             Panel p = new Panel(null) { // no layout manager so it respects
                                         // setSize
                 public void paint(Graphics g) {
                     g.drawImage(image, 0, 0, this);
                 }
             };
 
             frame.add(p);
             p.setSize(width, height);
             frame.pack();
             frame.setVisible(true);
 
             try {
                 Thread.sleep(timeOut);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
 
             frame.dispose();
         }
     }
 
     /**
      * DOCUMENT ME!
      * 
      * @param fSource
      *            DOCUMENT ME!
      * 
      * @throws Exception
      *             DOCUMENT ME!
      */
     private void testDefaultStyle(FeatureSource fSource) throws Exception {
         Catalog catalog = getCatalog();
         Style style = catalog.getStyleByName("Default").getStyle();
 
         FeatureTypeInfo typeInfo = catalog.getFeatureTypeByName(MockData.LAKES.getNamespaceURI(), MockData.LAKES.getLocalPart());
        Envelope env = typeInfo.getFeatureSource(null, null).getBounds();
         env.expandToInclude(fSource.getBounds());
 
         int w = 400;
         int h = (int) Math.round((env.getHeight() * w) / env.getWidth());
 
         double shift = env.getWidth() / 6;
 
         env = new Envelope(env.getMinX() - shift, env.getMaxX() + shift, env.getMinY() - shift, env
                 .getMaxY()
                 + shift);
 
         WMSMapContext map = new WMSMapContext();
        map.setRequest(new GetMapRequest(getWMS()));
         map.addLayer(fSource, style);
         map.setAreaOfInterest(env);
         map.setMapWidth(w);
         map.setMapHeight(h);
         map.setBgColor(BG_COLOR);
         map.setTransparent(false);
 
         this.rasterMapProducer.setOutputFormat("image/gif");
         this.rasterMapProducer.setMapContext(map);
         this.rasterMapProducer.produceMap();
 
         RenderedImage image = this.rasterMapProducer.getImage();
 
         String typeName = fSource.getSchema().getName().getLocalPart();
         assertNotBlank("testDefaultStyle " + typeName, this.rasterMapProducer);
     }
 
     /**
      * This dummy producer adds no functionality to DefaultRasterMapProducer,
      * just implements a void formatImageOutputStream to have a concrete class
      * over which test that DefaultRasterMapProducer correctly generates the
      * BufferedImage.
      * 
      * @author Gabriel Roldan, Axios Engineering
      * @version $Id: DefaultRasterMapProducerTest.java 6797 2007-05-16 10:23:50Z
      *          aaime $
      */
     private static class DummyRasterMapProducer extends DefaultRasterMapProducer {
 
         public DummyRasterMapProducer() {
             super("image/gif", new String[] { "image/gif" }, null);
         }
 
         public void formatImageOutputStream(RenderedImage image, OutputStream outStream)
                 throws WmsException, IOException {
             /*
              * Intentionally left blank, since this class is used just to ensure
              * the abstract raster producer correctly generates a BufferedImage.
              */
         }
 
         public String getContentDisposition() {
             // can be null
             return null;
         }
     }
 
 }
