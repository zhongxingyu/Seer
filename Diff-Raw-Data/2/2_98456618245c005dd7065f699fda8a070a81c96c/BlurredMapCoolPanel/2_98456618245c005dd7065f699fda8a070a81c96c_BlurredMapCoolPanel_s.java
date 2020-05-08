 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.tools.metaobjectrenderer;
 
 import Sirius.navigator.resource.PropertyManager;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import org.jdesktop.fuse.ResourceInjector;
 import org.jdesktop.swingx.image.StackBlurFilter;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.retrieval.RetrievalEvent;
 import de.cismet.cismap.commons.retrieval.RetrievalListener;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.gui.FuseLoader;
 import de.cismet.tools.gui.PainterCoolPanel;
 
 /**
  * DOCUMENT ME!
  *
  * @author   dmeiers
  * @version  $Revision$, $Date$
  */
 public class BlurredMapCoolPanel extends PainterCoolPanel implements ComponentListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Dimension MAX_SIZE = new Dimension(2048, 1024);
     private static final int IMAGE_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
 //    private static final Dimension MAX_SIZE = new Dimension(2048, 1024);
     public static int offset = 6;
     public static float blurredMapOpacity = 0.2f;
     public static float cutOutMapOpacity = 0.6f;
     public static Color colorMapBorder = Color.black;
     public static Color gradientColorTop = new Color(120, 120, 120);
     public static Color gradientColorBottom = new Color(200, 200, 200);
 
     //~ Instance fields --------------------------------------------------------
 
     // Lumbermill Logger initialisieren
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private boolean mustBlur;
     private double geoBuffer;
     private int lastX;
     private int lastWidth;
     private int panelWidth;
     private Image map;
     private BufferedImage cacheImage;
     private BufferedImage blurredMap;
     private BufferedImage cachedBlurredMap;
     private BufferedImage gradientImage;
     private BufferedImage orgMap;
     private Geometry geometry;
     private SimpleWMS swms;
     private JPanel spinner;
     private JComponent panMap;
     private JComponent panContent;
     private Rectangle mapBounds;
     private ImageIcon icons;
     private boolean noTitlePanel;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new BlurredMapCoolPanel object.
      */
     public BlurredMapCoolPanel() {
         super();
         // FUSE initialisieren
//        Ressourcen hierarchisch rekursiv nach oben einfuegen
         // NOI18N
         gradientColorTop = javax.swing.UIManager.getDefaults().getColor("Button.shadow");                        // NOI18N
         gradientColorBottom = javax.swing.UIManager.getDefaults().getColor("Button.background");                 // NOI18N
         mapBounds = null;
         cacheImage = null;
         mustBlur = true;
         geoBuffer = 40d;
         lastX = 0;
         lastWidth = 0;
         panelWidth = 0;
         map = null;
         blurredMap = null;
         orgMap = null;
         geometry = null;
         try {
             final SAXBuilder builder = new SAXBuilder(false);
             final Document doc = builder.build(getClass().getResource("/coolobjectrenderer/backgroundWMS.xml")); // NOI18N
             final Element prefs = doc.getRootElement();
 
             swms = new SimpleWMS(prefs);
             swms.addRetrievalListener(new RetrievalListener() {
 
                     @Override
                     public void retrievalAborted(final RetrievalEvent retrievalEvent) {
                     }
 
                     @Override
                     public void retrievalComplete(final RetrievalEvent retrievalEvent) {
                         final Object o = retrievalEvent.getRetrievedObject();
                         if (o instanceof Image) {
                             map = (Image)o;
                             final BufferedImage erg = new BufferedImage(
                                     map.getWidth(null),
                                     map.getHeight(null),
                                     IMAGE_TYPE);
                             final Graphics2D g = erg.createGraphics();
 //                            g.setColor(Color.green);
 //                            g.fillRect(0, 0, map.getWidth(null), map.getHeight(null));
                             g.drawImage(map, 0, 0, null);
                             g.dispose();
                             cacheImage = null;
                             lastWidth = getWidth();
                             lastX = 0;
                             mustBlur = true;
                             if (getSpinner() != null) {
                                 getSpinner().setVisible(false);
                             }
                             createBackground(erg);
                             if (log.isDebugEnabled()) {
                                 log.debug("MapRetrieval completed"); // NOI18N
                             }
                         } else {
                             if (getSpinner() != null) {
                                 getSpinner().setVisible(false);
                             }
 
                             log.warn("no image"); // NOI18N
                         }
                     }
 
                     @Override
                     public void retrievalError(final RetrievalEvent retrievalEvent) {
                         if (getSpinner() != null) {
                             getSpinner().setVisible(false);
                         }
                     }
 
                     @Override
                     public void retrievalProgress(final RetrievalEvent retrievalEvent) {
                     }
 
                     @Override
                     public void retrievalStarted(final RetrievalEvent retrievalEvent) {
                         if (log.isDebugEnabled()) {
                             log.debug("retrievalStarted"); // NOI18N
                         }
                     }
                 });
         } catch (Exception e) {
             log.error("Error while loading the map info", e); // NOI18N
             if (getSpinner() != null) {
                 getSpinner().setVisible(false);
             }
         }
         addComponentListener(this);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Dimension getMaximumSize() {
         return MAX_SIZE;
     }
 
     /**
      * ueberschreibt die Standard-Zeichenmethode eines JPanels. Zeichnet die "coolen" Effekte des CoolPanels.
      *
      * @param  g  DOCUMENT ME!
      */
     @Override
     protected void paintComponent(final Graphics g) {
         super.paintComponent(g);
         final Graphics2D g2d = (Graphics2D)g;
 
         if (cacheImage == null) {
             // Image zum Zeichnen erstellen von dem wird spaeter der Schlagschatten erstellt wird
             final BufferedImage box = new BufferedImage(getWidth() - offset,
                     getHeight()
                             - offset,
                     IMAGE_TYPE);
 
             // Graphics-Objekt der Box erzeugen
             final Graphics2D bg = box.createGraphics();
 
             /*
              * Transparenz zeichnen...
              */
 
             // Standard-Zeichenmodus speichern
             final Composite orig = bg.getComposite();
             final Color origColor = bg.getColor();
 
             bg.setComposite(orig);
             bg.setColor(origColor);
 
             // Karte zeichnen
             if (getBlurredMap() != null) {
                 bg.setComposite(AlphaComposite.Src.derive(blurredMapOpacity));
                 bg.drawImage(getBlurredMap(), 0, 0, null);
                 bg.setComposite(orig);
 
                 // "Fenster zum Hof" ausschneiden und zeichnen, falls panMap gesetzt wurde
                 if (getPanMap() != null) {
                     final Rectangle b = getPanMap().getBounds();
 
                     // Karte in Ausschnitt zeichnen
                     if (getMap() != null) {
                         log.info("CoolPanel: draw small map"); // NOI18N
                         bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.2f));
                         bg.setColor(colorMapBorder);
                         if (b.width < getMap().getWidth()) {
                             bg.fillRect(
                                 b.x,
                                 b.y
                                         + panContent.getBounds().y
                                         + (2 * offset),
                                 b.width
                                         - (3 * offset),
                                 b.height
                                         - (4 * offset));
                             bg.setComposite(orig);
                             bg.drawRect(
                                 b.x,
                                 b.y
                                         + panContent.getBounds().y
                                         + (2 * offset),
                                 b.width
                                         - (3 * offset),
                                 b.height
                                         - (4 * offset));
                         } else {
                             bg.fillRect(
                                 b.x,
                                 b.y
                                         + panContent.getBounds().y
                                         + (2 * offset),
                                 getMap().getWidth(),
                                 b.height
                                         - (4 * offset));
                             bg.setComposite(orig);
                             bg.drawRect(
                                 b.x,
                                 b.y
                                         + panContent.getBounds().y
                                         + (2 * offset),
                                 getMap().getWidth(),
                                 b.height
                                         - (4 * offset));
                         }
                         // Fensterausschnitt zeichnen
                         bg.setComposite(AlphaComposite.DstOver.derive(cutOutMapOpacity));
                         final BufferedImage subMap = getMap().getSubimage(
                                 0,
                                 0,
                                 getMap().getWidth(),
                                 getMap().getHeight()
                                         - (4 * offset));
                         bg.drawImage(subMap, b.x, b.y
                                     + panContent.getBounds().y
                                     + (2 * offset), null);
                     }
                 }
             } // Ende Karte zeichnen
             // rander sauber zeichen
             // Fertige Box und ihren Schatten zeichnen.
             cacheImage = new BufferedImage(box.getWidth(), box.getHeight(), IMAGE_TYPE);
             final Graphics2D cg = cacheImage.createGraphics();
             final Composite cgOrigComp = cg.getComposite();
             final Color cgOrigColor = cg.getColor();
             cg.setColor(Color.red);
             cg.fillRoundRect(offset, 0, cacheImage.getWidth(), cacheImage.getHeight(), 30, 30);
             cg.setComposite(AlphaComposite.SrcIn);
 
             cg.setColor(cgOrigColor);
             cg.drawImage(box, offset, 0, null);
             cg.setComposite(orig);
             bg.dispose();
             cg.dispose();
             box.flush();
         }
 
 //         Entgueltiges Bild in Panel zeichnen
         g2d.drawImage(cacheImage, 0, 0, null);
     }
 
     /**
      * Liefert den weichgezeichneten Kartenausschnitt zurueck.
      *
      * @return  der bereits weichgezeichnete Kartenausschnitt
      */
     private BufferedImage getBlurredMap() {
         return blurredMap;
     }
 
     /**
      * Liefert den Kartenausschnitt ohne Weichzeichner zurueck.
      *
      * @return  der "originale" Kartenausschnitt
      */
     private BufferedImage getMap() {
         return orgMap;
     }
 
     /**
      * Setzt den relevanten Kartenausschnitt. Hierbei wird der Ausschnitt automatisch weichgezeichnet. Um diese Karte im
      * CoolPanel zu verwenden sollte er mit der {@link getBlurredMap()}-Methode aufgerufen werden.
      *
      * @param  newMap  der geaenderte zu blurrende Kartenauschnitt
      */
     private void createBackground(final BufferedImage newMap) {
         if (newMap != null) {
             orgMap = newMap;
             // Ausfuehrung des Blurrens im Thread
             final Thread t = new Thread(new Runnable() {
 
                         @Override
                         public void run() {
                             if (mustBlur) {
                                 final StackBlurFilter blur = new StackBlurFilter(6);
                                 cachedBlurredMap = blur.filter(orgMap, null);
                                 blurredMap = new BufferedImage(getWidth(), getHeight(), IMAGE_TYPE);
                                 final Graphics2D b = blurredMap.createGraphics();
                                 b.drawImage(cachedBlurredMap, 0, 0, null);
                                 b.dispose();
                             } else {
                                 blurredMap.flush();
                                 final Graphics2D b = blurredMap.createGraphics();
                                 b.drawImage(cachedBlurredMap, lastX, 0, null);
                                 b.dispose();
                             }
                             if (getPanMap() != null) {
                                 final Rectangle bounds = getPanMap().getBounds();
                                 if ((bounds.width - (3 * offset)) > getWidth()) {
                                     orgMap = orgMap.getSubimage(
                                             bounds.x,
                                             bounds.y
                                                     + (2 * offset),
                                             getWidth(),
                                             bounds.height);
                                 } else {
                                     orgMap = orgMap.getSubimage(
                                             bounds.x,
                                             bounds.y
                                                     + (2 * offset),
                                             bounds.width
                                                     - (3 * offset),
                                             bounds.height);
                                 }
                             }
                             // CoolPanel neu zeichnen, sobald die geblurrte Karte fertig erstellt wurde
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         cacheImage = null;
                                         repaint();
                                     }
                                 });
                         }
                     });
             CismetThreadPool.execute(t);
         }
     }
 
     /**
      * Interne Methode die den WMS-Server anstoesst eine neue Karte zu liefern.
      */
     private void mapIt() {
         if (log.isDebugEnabled()) {
             log.debug("MAPIT"); // NOI18N
         }
         try {
             if ((getSpinner() != null) && !getSpinner().isVisible()) {
                 getSpinner().setVisible(true);
             }
 
             if (geometry != null) {
                 mapBounds = getBounds();
                 // Neue BoundingBox 40 Einheiten um die Geometrie herum erzeugen
                 BoundingBox bb = new BoundingBox(geometry.buffer(geoBuffer));
 
                 // Panelgroessen speichern
                 this.panelWidth = getWidth();
                 if (getPanMap() != null) {
                     final double panWith = new Integer(getWidth() - (3 * offset)).doubleValue();
                     final double panHeight = new Integer(getHeight() - (4 * offset)).doubleValue();
                     final double cutOutWidth = new Integer(getPanMap().getWidth() - offset).doubleValue();
                     final double cutOutHeight = new Integer(getPanMap().getHeight() - offset).doubleValue();
 
                     // Mittelpunkt der BoundingBox bestimmen
                     final double midX = bb.getX1() + ((bb.getX2() - bb.getX1()) / 2);
                     final double midY = bb.getY1() + ((bb.getY2() - bb.getY1()) / 2);
 
                     // Groesse des Kartenausschnitts in WMS-Einheiten speichern
                     double worldWidth = bb.getWidth();
                     double worldHeight = bb.getHeight();
 
                     // Verhaeltnis Breite/Hoehe berechnen
                     final double widthToHeightRatio = cutOutWidth / cutOutHeight;
 
                     // Testen, wie die RealWorld-Groessen angepasst werden muessen
                     if ((widthToHeightRatio / (worldWidth / worldHeight)) > 1) {
                         // Breite der Hoehe anpassen
                         worldWidth = worldHeight * widthToHeightRatio;
                     } else {
                         // Hoehe der Breite anpassen
                         worldHeight = worldWidth * widthToHeightRatio;
                     }
 
                     // Pixel-WMS-Einheit-Verhaeltnis bezeichnen
                     final double widthValuePerPixel = worldWidth / cutOutWidth;
                     final double heightValuePerPixel = worldHeight / cutOutHeight;
 
                     final double foo = worldWidth / widthValuePerPixel;
                     // Versatz berechnen
                     final Rectangle b = getPanMap().getBounds();
                     final double offTop = b.getY() * heightValuePerPixel;
                     final double offBottom = (panHeight - (b.getY() + b.height)) * heightValuePerPixel;
                     final double offLeft = b.getX() * widthValuePerPixel;
                     final double offRight = (panWith - (b.getX() + b.width - (3 * offset))) * widthValuePerPixel;
 
                     // BoundingBox mit neuer Groesse erstellen
 
                     bb = new BoundingBox((midX - ((worldWidth / 2) + offLeft)),
                             (midY - ((worldHeight / 2) + offTop) - 95),
                             (midX + (worldWidth / 2) + offRight),
                             (midY + (worldHeight / 2) + offBottom)
                                     - 95);
                 } else {
                     final double midX = bb.getX1() + ((bb.getX2() - bb.getX1()) / 2);
                     final double midY = bb.getY1() + ((bb.getY2() - bb.getY1()) / 2);
                     double realWorldWidth = bb.getWidth();
                     double realWorldHeight = bb.getHeight();
                     final double widthToHeightRatio = getWidth() / getHeight();
 
                     if ((widthToHeightRatio / (realWorldWidth / realWorldHeight)) > 1) {
                         // height is bestimmer ;-)
                         realWorldWidth = realWorldHeight * widthToHeightRatio;
                     } else {
                         realWorldHeight = realWorldWidth * widthToHeightRatio;
                     }
 
                     bb = new BoundingBox(midX - (realWorldWidth / 2),
                             midY
                                     - (realWorldHeight / 2),
                             midX
                                     + (realWorldWidth / 2),
                             midY
                                     + (realWorldHeight / 2));
                 }
 
                 // Karte von WMS-Server holen
                 swms.setBoundingBox(bb);
                 swms.setSize(getHeight(), getWidth());
                 swms.retrieve(true);
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("No geometry object available."); // NOI18N
                 }
                 if (getSpinner() != null) {
                     getSpinner().setVisible(false);
                 }
                 repaint();
             }
         } catch (Exception e) {
             log.warn("Error while displaying the map.", e);     // NOI18N
             if (getSpinner() != null) {
                 getSpinner().setVisible(false);
             }
         }
     }
 
     /**
      * Setzt das Geometry-Objekt des momentan im Konfigurator angewaehlten Objekts. Wird benoetigt, um einen
      * Kartenhintergrund zu zeichnen.
      *
      * @param  geometry  Geometry-Objekt
      */
     public void setGeometry(final Geometry geometry) {
         this.geometry = geometry;
     }
 
     @Override
     public void componentResized(final ComponentEvent e) {
         cacheImage = null;
         if ((getMap() == null) || (mapBounds.height < getHeight()) || (mapBounds.width < getWidth())) {
             map = null;
             blurredMap = null;
             orgMap = null;
             repaint();
             mapIt();
         } else {
             // Test, ob Groesse zum ersten Mal geaendert wird
             if (lastWidth == 0) {
                 lastWidth = panelWidth;
             }
 
             final int width = getWidth();
 
             // Test, ob vergroessert oder verkleinert wird
             if (width > lastWidth) {
                 lastX += (width - lastWidth) / 2;
             } else {
                 lastX -= (lastWidth - width) / 2;
             }
 
             // letzte Panelbreite speichern fuer naechsten Aufruf
             lastWidth = width;
 
             // neues Hintergrundbild erstellen und zeichnen
             final BufferedImage erg = new BufferedImage(getWidth(), getHeight(), IMAGE_TYPE);
             final Graphics2D g = erg.createGraphics();
             g.drawImage(map, lastX, 0, null);
             g.dispose();
 
             // ChachedBlurImage verwenden
             mustBlur = false;
             createBackground(erg);
         }
     }
 
     @Override
     public void componentMoved(final ComponentEvent e) {
     }
 
     @Override
     public void componentShown(final ComponentEvent e) {
         mapIt();
     }
 
     @Override
     public void componentHidden(final ComponentEvent e) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JComponent getPanMap() {
         return panMap;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  panMap  DOCUMENT ME!
      */
     public void setPanMap(final JComponent panMap) {
         this.panMap = panMap;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  panContent  DOCUMENT ME!
      */
     public final void setPanContent(final JComponent panContent) {
         this.panContent = panContent;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JPanel getSpinner() {
         return spinner;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  spinner  DOCUMENT ME!
      */
     public void setSpinner(final JPanel spinner) {
         this.spinner = spinner;
     }
 }
