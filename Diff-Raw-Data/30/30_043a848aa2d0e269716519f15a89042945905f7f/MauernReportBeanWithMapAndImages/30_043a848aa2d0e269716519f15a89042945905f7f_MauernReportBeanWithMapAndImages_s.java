 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.reports.wunda_blau;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import org.openide.util.NbBundle;
 
 import java.awt.Color;
 import java.awt.Image;
 
 import java.io.InputStream;
 
 import java.text.NumberFormat;
 
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.imageio.ImageIO;
 
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cismap.commons.HeadlessMapProvider;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
 
 import de.cismet.netutil.Proxy;
 
import de.cismet.security.WebDavClient;

 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.PasswordEncrypter;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class MauernReportBeanWithMapAndImages extends MauernReportBean {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
             MauernReportBeanWithMapAndImages.class);
     private static String WEB_DAV_DIRECTORY;
     private static String WEB_DAV_USER;
     private static String WEB_DAV_PASSWORD;
     private static final int MAP_DPI = 300;
 
     //~ Instance fields --------------------------------------------------------
 
     Image mapImage = null;
     Image img0 = null;
     Image img1 = null;
    private final WebDavClient webDavClient;
     private boolean proceed = false;
     private boolean mapError = false;
     private boolean image0Error = false;
     private boolean image1Error = false;
     private String masstab = "";
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MauernBeanWithMapAndImages object.
      *
      * @param  mauer  DOCUMENT ME!
      */
     public MauernReportBeanWithMapAndImages(final CidsBean mauer) {
         super(mauer);
         final ResourceBundle bundle = ResourceBundle.getBundle("WebDav");
         String pass = bundle.getString("password");
 
         if ((pass != null) && pass.startsWith(PasswordEncrypter.CRYPT_PREFIX)) {
             pass = PasswordEncrypter.decryptString(pass);
         }
 
         WEB_DAV_PASSWORD = pass;
         WEB_DAV_USER = bundle.getString("user");
         WEB_DAV_DIRECTORY = bundle.getString("url");
        this.webDavClient = new WebDavClient(Proxy.fromPreferences(), WEB_DAV_USER, WEB_DAV_PASSWORD, true);
 //        final SimpleWMS s = new SimpleWMS(new SimpleWmsGetMapUrl("http://www.wms.nrw.de/geobasis/DOP?&VERSION=1.1.1&REQUEST=GetMap&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&BBOX=<cismap:boundingBox>&SRS=EPSG:25832&FORMAT=image/png&TRANSPARENT=TRUE&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=WMS,0,Metadaten&STYLES="));
 //        final SimpleWMS s = new SimpleWMS(new SimpleWmsGetMapUrl(
 //                    "http://s102x284:8399/arcgis/services/WuNDa-Orthophoto-WUP/MapServer/WMSServer?service=WMS&VERSION=1.1.1&REQUEST=GetMap&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&BBOX=<cismap:boundingBox>&SRS=EPSG:25832&FORMAT=image/png&TRANSPARENT=TRUE&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=6&STYLES=default"));
         final SimpleWMS s = new SimpleWMS(new SimpleWmsGetMapUrl(
                     "http://S102X284:8399/arcgis/services/WuNDa-DGK/MapServer/WMSServer?&VERSION=1.1.1&REQUEST=GetMap&BBOX=<cismap:boundingBox>&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&SRS=EPSG:25832&FORMAT=image/png&TRANSPARENT=TRUE&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=48&STYLES=default"));
 
 //        final SimpleWMS s = new SimpleWMS(new SimpleWmsGetMapUrl(
 //                    "http://S102X284:8399/arcgis/services/ALKIS-EXPRESS/MapServer/WMSServer?&VERSION=1.1.1&REQUEST=GetMap&FORMAT=image/png&TRANSPARENT=FALSE&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_xml&LAYERS=2,4,5,6,7,8,10,11,12,13,14,16,17,18,19,20,21,22,23,25,26,27,28,29,30,31,32,33,34,35,36,37,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100&STYLES=&BBOX=<cismap:boundingBox>&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&SRS="));
         final Geometry g = (Geometry)mauer.getProperty("georeferenz.geo_field");
 
         final DefaultStyledFeature dsf = new DefaultStyledFeature();
         dsf.setGeometry(g);
         dsf.setLineWidth(5);
         dsf.setLinePaint(Color.RED);
 
         final HeadlessMapProvider mapProvider = new HeadlessMapProvider();
         mapProvider.addLayer(s);
         mapProvider.addFeature(dsf);
         mapProvider.setMinimumScaleDenomimator(750);
         mapProvider.setRoundScaleTo(HeadlessMapProvider.RoundingPrecision.HUNDRETH);
         mapProvider.setCenterMapOnResize(true);
 
         final int height = Integer.parseInt(NbBundle.getMessage(
                     MauernReportBeanWithMapAndImages.class,
                     "MauernReportBeanWithMapAndImages.mapHeight"));
         final int width = Integer.parseInt(NbBundle.getMessage(
                     MauernReportBeanWithMapAndImages.class,
                     "MauernReportBeanWithMapAndImages.mapWidth"));
         final XBoundingBox boundingBox = new XBoundingBox(g);
         mapProvider.setBoundingBox(boundingBox);
         final Future<Image> f = mapProvider.getImage(72, MAP_DPI, width, height);
         try {
             final Image img = f.get();
             masstab = "1:" + NumberFormat.getIntegerInstance().format(mapProvider.getImageScaleDenominator());
             mapImage = img;
         } catch (InterruptedException ex) {
             mapError = true;
         } catch (ExecutionException ex) {
             mapError = true;
         }
 
         final List<CidsBean> images = CidsBeanSupport.getBeanCollectionFromProperty(mauer, "bilder");
         final StringBuilder url0Builder = new StringBuilder();
 
         final StringBuilder url1Builder = new StringBuilder();
         for (final CidsBean b : images) {
             final Integer nr = (Integer)b.getProperty("laufende_nummer");
             if (nr == 1) {
                url0Builder.append(b.getProperty("url").toString());
             } else if (nr == 2) {
                url1Builder.append(b.getProperty("url").toString());
             }
         }
 
         CismetThreadPool.execute(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         if (url0Builder.toString().equals("")) {
                             image0Error = true;
                             return;
                         }
                        final InputStream iStream = webDavClient.getInputStream(
                                url0Builder.toString());
                         img0 = ImageIO.read(iStream);
                         if (img0 == null) {
                             image0Error = true;
                             LOG.warn("error during image retrieval from Webdav");
                         }
 
                         System.out.println(img0);
                     } catch (Exception e) {
                         image0Error = true;
                     }
                 }
             });
 
         CismetThreadPool.execute(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         if (url1Builder.toString().equals("")) {
                             image1Error = true;
                             return;
                         }
                        final InputStream iStream = webDavClient.getInputStream(
                                url1Builder.toString());
                         img1 = ImageIO.read(iStream);
                         if (img1 == null) {
                             image1Error = true;
                             LOG.warn("error during image retrieval from Webdav");
                         }
                         System.out.println(img1);
                     } catch (Exception e) {
                         image1Error = true;
                     }
                 }
             });
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Image getImg0() {
         return img0;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  img0  DOCUMENT ME!
      */
     public void setImg0(final Image img0) {
         this.img0 = img0;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Image getImg1() {
         return img1;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  img1  DOCUMENT ME!
      */
     public void setImg1(final Image img1) {
         this.img1 = img1;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Image getMapImage() {
         return mapImage;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  mapImage  DOCUMENT ME!
      */
     public void setMapImage(final Image mapImage) {
         this.mapImage = mapImage;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getMasstab() {
         return masstab;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  masstab  DOCUMENT ME!
      */
     public void setMasstab(final String masstab) {
         this.masstab = masstab;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public boolean isReadyToProceed() {
         return ((mapImage != null) || mapError)
                     && ((img0 != null) || image0Error)
                     && ((img1 != null) || image1Error);
     }
 }
