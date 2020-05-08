 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import Sirius.navigator.ui.ComponentRegistry;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import net.sf.jasperreports.engine.JRPrintPage;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.util.JRLoader;
 import net.sf.jasperreports.view.JRViewer;
 
 import org.jdesktop.swingx.JXHyperlink;
 import org.jdesktop.swingx.graphics.ShadowRenderer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.color.ColorSpace;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 
 import java.net.URL;
 
 import java.sql.Timestamp;
 
 import java.text.DateFormat;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import de.cismet.cids.annotations.AggregationRenderer;
 import de.cismet.cids.annotations.CidsAttribute;
 import de.cismet.cids.annotations.CidsAttributeVector;
 
 import de.cismet.cids.custom.deprecated.JBreakLabel;
 import de.cismet.cids.custom.deprecated.JLoadDots;
 import de.cismet.cids.custom.objectrenderer.utils.PrintingWaitDialog;
 
 import de.cismet.cids.tools.metaobjectrenderer.BlurredMapObjectRenderer;
 
import de.cismet.tools.BrowserLauncher;

 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * de.cismet.cids.objectrenderer.CoolLuftBildRenderer.
  *
  * <p>Renderer speziell fuer Luftbildschraegaufnahmen.</p>
  *
  * @author   nhaffke
  * @version  $Revision$, $Date$
  */
 @AggregationRenderer
 public class LuftbildschraegaufnahmenRenderer extends BlurredMapObjectRenderer {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final String ERROR_STR = "/de/cismet/cids/tools/metaobjectrenderer/examples/error.png";
 
     private static final String TITLE = "Luftbildschr\u00E4gaufnahme";
     private static final String TITLE_AGR = "Luftbildschr\u00E4gaufnahmen";
 
     //~ Instance fields --------------------------------------------------------
 
     // Rendererzuweisungen
     @CidsAttribute("NAME")
     public String name = "";
 
     @CidsAttribute("BILDNUMMER")
     public String nummer = "";
 
     @CidsAttribute("Strasse/Lage")
     public String lage = "";
 
     @CidsAttribute("Hinweise")
     public String hinweise = "";
 
     @CidsAttribute("Aufnahmedatum")
     public Timestamp aufnahme = null;
 
     @CidsAttribute("Erfassungsdatum")
     public Timestamp erfassung = null;
 
     @CidsAttribute("Datum der Aktualisierung")
     public Timestamp aktualisierung = null;
 
     @CidsAttribute("Auftraggeber")
     public String auftraggeber = "";
 
     @CidsAttribute("Fotograf")
     public String fotograf = "";
 
     @CidsAttribute("Filmart")
     public String filmart = "";
 
     @CidsAttribute("Bild im Original vorr\u00E4tig")
     public String bildOriginal = "";
 
     @CidsAttribute("Dateiname")
     public String dateiname = "";
 
     @CidsAttribute("Organisationskennzeichen")
     public String orgaKennzeichen = "";
 
 //    @CidsAttribute("DESCRIPTION")
     public URL link = null;
 
     @CidsAttribute("Georeferenz.GEO_STRING")
     public Geometry geometry = null;
 
     // AggregationRenderer-Zuweisungen
     @CidsAttributeVector("Strasse/Lage")
     public Vector<String> agrLage = new Vector();
 
     @CidsAttributeVector("Hinweise")
     public Vector<String> agrHinweis = new Vector();
 
     @CidsAttributeVector("Dateiname")
     public Vector<String> agrDatei = new Vector();
 
     @CidsAttributeVector("BILDNUMMER")
     public Vector<String> agrNummer = new Vector();
 
     @CidsAttributeVector("Georeferenz.GEO_STRING")
     public Vector<Geometry> geoAgr = new Vector();
     PrintingWaitDialog printingWaitDialog = new PrintingWaitDialog(ComponentRegistry.getRegistry().getMainWindow(),
             true);
     Properties properties = new Properties();
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private ImageIcon error = new ImageIcon(getClass().getResource(ERROR_STR));
 
     private Geometry allGeom;
     private JXHyperlink[] jxhImages;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private org.jdesktop.swingx.JXHyperlink jxhAgrPrint;
     private org.jdesktop.swingx.JXHyperlink jxhPicture;
     private org.jdesktop.swingx.JXHyperlink jxhPrint;
     private javax.swing.JLabel lblAgrTitle;
     private javax.swing.JLabel lblAktdat;
     private javax.swing.JLabel lblAufnahmedat;
     private javax.swing.JLabel lblAuftraggeber;
     private javax.swing.JLabel lblBildOrig;
     private javax.swing.JLabel lblDateiname;
     private javax.swing.JLabel lblErfassungsdat;
     private javax.swing.JLabel lblFilmart;
     private javax.swing.JLabel lblFotograf;
     private javax.swing.JLabel lblHinweise;
     private javax.swing.JLabel lblLage;
     private javax.swing.JLabel lblLink;
     private javax.swing.JLabel lblOrganisation;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JPanel panAggregation;
     private javax.swing.JPanel panAgrContent;
     private javax.swing.JPanel panAgrTitle;
     private javax.swing.JPanel panContent;
     private javax.swing.JPanel panInter;
     private javax.swing.JPanel panMap;
     private javax.swing.JPanel panPicture;
     private javax.swing.JPanel panSpinner;
     private javax.swing.JPanel panTitle;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Konstruktor.
      */
     public LuftbildschraegaufnahmenRenderer() {
         initComponents();
         setPanContent(this.panContent);
         setPanInter(null);
         setPanMap(panMap);
         setPanTitle(panTitle);
         setSpinner(panSpinner);
         extraAggregationRendererComponent = panAggregation;
         try {
             properties.load(getClass().getResourceAsStream("/renderer.properties"));
         } catch (Exception e) {
             log.warn("Fehler beim Laden der Properties", e);
         }
         allGeom = null;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void assignAggregation() {
         // GridLayout der Anzahl Objekte anpassen
         if ((agrDatei.size() % 3) == 0) {
             ((GridLayout)panAgrContent.getLayout()).setRows(agrDatei.size() / 3);
         } else if ((agrDatei.size() % 3) == 1) {
             ((GridLayout)panAgrContent.getLayout()).setRows((agrDatei.size() + 2) / 3);
         } else {
             ((GridLayout)panAgrContent.getLayout()).setRows((agrDatei.size() + 1) / 3);
         }
 
         lblAgrTitle.setText(agrDatei.size() + " " + TITLE_AGR);
         jxhImages = new JXHyperlink[agrDatei.size()];
 
         // In Schleife alle Objekte erzeugen und einfuegen
         for (int i = 0; i < agrDatei.size(); i++) {
             if ((allGeom != null) && (geoAgr.get(i) != null)) {
                 allGeom = allGeom.union(geoAgr.get(i));
             } else if ((allGeom == null) && (geoAgr.get(i) != null)) {
                 allGeom = geoAgr.get(i);
             }
             final RoundedPanel rnd = new RoundedPanel();
             rnd.setLayout(new BorderLayout());
             final JLabel lblAgrLage = new JLabel(agrLage.get(i));
             lblAgrLage.setFont(new Font("Tahoma", Font.BOLD, 11));
             final JLabel lblAgrHinweis = new JBreakLabel(agrHinweis.get(i), 50, true);
             final JPanel panLabels = new JPanel(new BorderLayout());
             panLabels.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
             panLabels.setOpaque(false);
             final JPanel panArr = new JPanel(new FlowLayout(FlowLayout.CENTER));
             panArr.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
             panArr.setOpaque(false);
 
             jxhImages[i] = new JXHyperlink();
             jxhImages[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/load.png")));
             jxhImages[i].setFocusPainted(false);
             panArr.add(jxhImages[i]);
 
             panLabels.add(lblAgrLage, BorderLayout.NORTH);
             panLabels.add(lblAgrHinweis, BorderLayout.CENTER);
             rnd.add(panLabels, BorderLayout.NORTH);
             rnd.add(panArr, BorderLayout.SOUTH);
             panAgrContent.add(rnd);
 
             // Luftbild von Server laden
             final int index = i;
             final Thread t = new Thread(new Runnable() {
 
                         @Override
                         public void run() {
                             try {
                                 String url = properties.getProperty("luftbildschraegaufnahmenservicesmall");
                                 ImageIcon ii;
                                 if (url == null) {
                                     log.fatal("Aggregation Wupp " + EventQueue.isDispatchThread());
                                     ii = new ImageIcon(
                                             new URL(
                                                 "http://s10220:8098/luft/tiffer?bnr="
                                                         + agrNummer.get(index)
                                                         + "&scale=0.075&format=JPG"));
                                 } else {
                                     log.fatal("Aggregation Kif " + EventQueue.isDispatchThread());
                                     final String newUrl = url.replaceAll("<cismet::nummer>", agrNummer.get(index));
                                     ii = new ImageIcon(new URL(newUrl));
                                 }
 
                                 // falls kein Bild geladen werden konnte, erzeuge Ausnahme
                                 if (ii == null) {
                                     throw new Exception("Luftbildaufnahme konnte nicht geladen werden.");
                                 }
                                 final ShadowRenderer renderer = new ShadowRenderer(
                                         shadowLength,
                                         shadowIntensity,
                                         shadowColor);
                                 final BufferedImage temp = new BufferedImage(
                                         ii.getIconWidth(),
                                         ii.getIconHeight(),
                                         BufferedImage.TYPE_4BYTE_ABGR);
                                 final Graphics tg = temp.createGraphics();
                                 tg.drawImage(ii.getImage(), 0, 0, null);
                                 tg.dispose();
 
                                 // Schatten erstellen
                                 final BufferedImage shadow = renderer.createShadow(temp);
 
                                 final BufferedImage result = new BufferedImage(
                                         ii.getIconWidth()
                                                 + 6,
                                         ii.getIconHeight()
                                                 + 6,
                                         BufferedImage.TYPE_INT_ARGB);
                                 final Graphics rg = result.createGraphics();
                                 rg.drawImage(shadow, 0, 0, null);
                                 rg.drawImage(temp, 0, 0, null);
                                 rg.setColor(new Color(0, 0, 0, 120));
                                 rg.drawRect(0, 0, ii.getIconWidth(), ii.getIconHeight());
                                 rg.dispose();
                                 shadow.flush();
 
                                 // RollOver-Image erzeugen
                                 BufferedImage resGray = null;
                                 final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                                 final ColorConvertOp op = new ColorConvertOp(cs, null);
                                 resGray = op.filter(result, null);
 
                                 // Images zu konstanten Icons machen fuer Thread
                                 final ImageIcon icon_orig = new ImageIcon(result);
                                 final ImageIcon icon_gray = new ImageIcon(resGray);
 
                                 EventQueue.invokeLater(new Runnable() {
 
                                         @Override
                                         public void run() {
                                             jxhImages[index].setIcon(icon_orig);
                                             jxhImages[index].setRolloverEnabled(true);
                                             jxhImages[index].setRolloverIcon(icon_gray);
                                             jxhImages[index].addActionListener(new ActionListener() {
 
                                                     @Override
                                                     public void actionPerformed(final ActionEvent e) {
                                                         String url = "";
                                                         try {
                                                             url = properties.getProperty(
                                                                     "luftbildschraegaufnahmenservicefull");
                                                             if (url == null) {
                                                                 BrowserLauncher.openURL(
                                                                     "http://s10220:8098/luft/tiffer?bnr="
                                                                             + agrNummer.get(index)
                                                                             + "&scale=1&format=JPG");
                                                             } else {
                                                                 final String newUrl = url.replaceAll(
                                                                         "<cismet::nummer>",
                                                                         agrNummer.get(index));
                                                                 BrowserLauncher.openURL(newUrl);
                                                             }
                                                         } catch (Exception ex) {
                                                             log.error(
                                                                 "Fehler beim OEffnen der URL \""
                                                                         + url
                                                                         + "\"",
                                                                 ex);
                                                         }
                                                     }
                                                 });
                                         }
                                     });
                             } catch (Exception e) {
                                 log.error(
                                     "Konnte Bild aus URL \"http://s10220:8098/luft/tiffer?bnr="
                                             + agrNummer.get(index)
                                             + "&scale=0.15&format=JPG\" nicht erzeugen.");
 
                                 EventQueue.invokeLater(new Runnable() {
 
                                         @Override
                                         public void run() {
                                             jxhImages[index].setIcon(error);
                                         }
                                     });
                             }
                         }
                     });
             t.start();
         }
         ((BlurredMapObjectRenderer)panAggregation).setGeometry(allGeom);
     }
 
     @Override
     public void assignSingle() {
         if (!name.equals("")) {
             lblTitle.setText(TITLE + " - " + name);
         } else {
             lblTitle.setText(TITLE);
         }
 
         if (!lage.equals("")) {
             lblLage.setText(lage);
         } else {
             lblLage.setVisible(false);
             jLabel1.setVisible(false);
         }
 
         if (!hinweise.equals("")) {
             lblHinweise.setText(hinweise.trim());
         } else {
             lblHinweise.setVisible(false);
             jLabel2.setVisible(false);
         }
 
         if (aufnahme != null) {
             lblAufnahmedat.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY).format(aufnahme));
         } else {
             lblAufnahmedat.setVisible(false);
             jLabel3.setVisible(false);
         }
 
         if (erfassung != null) {
             lblErfassungsdat.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY).format(erfassung));
         } else {
             lblErfassungsdat.setVisible(false);
             jLabel4.setVisible(false);
         }
 
         if (aktualisierung != null) {
             lblAktdat.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY).format(aktualisierung));
         } else {
             lblAktdat.setVisible(false);
             jLabel5.setVisible(false);
         }
 
         if (!auftraggeber.equals("")) {
             lblAuftraggeber.setText(auftraggeber);
         } else {
             lblAuftraggeber.setVisible(false);
             jLabel6.setVisible(false);
         }
 
         if (!fotograf.equals("")) {
             lblFotograf.setText(fotograf);
         } else {
             lblFotograf.setVisible(false);
             jLabel7.setVisible(false);
         }
 
         if (!filmart.equals("")) {
             lblFilmart.setText(filmart);
         } else {
             lblFilmart.setVisible(false);
             jLabel8.setVisible(false);
         }
 
         if (!bildOriginal.equals("")) {
             if (bildOriginal.equals("1")) {
                 lblBildOrig.setText("ja");
             } else {
                 lblBildOrig.setText("nein");
             }
         } else {
             lblBildOrig.setVisible(false);
             jLabel9.setVisible(false);
         }
 
         if (!dateiname.equals("")) {
             lblDateiname.setText(dateiname);
         } else {
             lblDateiname.setVisible(false);
             jLabel10.setVisible(false);
         }
 
         if (!orgaKennzeichen.equals("")) {
             lblOrganisation.setText(orgaKennzeichen);
         } else {
             lblOrganisation.setVisible(false);
             jLabel11.setVisible(false);
         }
 
         if (link != null) {
             lblLink.setText(link.toString());
         } else {
             lblLink.setVisible(false);
             jLabel12.setVisible(false);
         }
 
         if (geometry != null) {
             super.setGeometry(geometry);
         }
 
         final Thread t = new Thread(new Runnable() {
 
                     @Override
                     public void run() {
                         try {
                             String url = properties.getProperty("luftbildschraegaufnahmenservicesmall");
                             ImageIcon i;
                             if (url == null) {
                                 log.fatal("Single Wupp " + EventQueue.isDispatchThread());
                                 i = new ImageIcon(
                                         new URL(
                                             "http://s10220:8098/luft/tiffer?bnr="
                                                     + nummer
                                                     + "&scale=0.075&format=JPG"));
                             } else {
                                 log.fatal("Single Kif " + EventQueue.isDispatchThread());
                                 final String newUrl = url.replaceAll("<cismet::nummer>", nummer);
                                 i = new ImageIcon(new URL(newUrl));
                             }
 
                             // falls kein Bild geladen werden konnte, erzeuge Ausnahme
                             if (i == null) {
                                 throw new Exception("Luftbildaufnahme konnte nicht geladen werden.");
                             }
                             final ShadowRenderer renderer = new ShadowRenderer(3, 0.5f, Color.BLACK);
                             final BufferedImage temp = new BufferedImage(
                                     i.getIconWidth(),
                                     i.getIconHeight(),
                                     BufferedImage.TYPE_4BYTE_ABGR);
                             final Graphics tg = temp.createGraphics();
                             tg.drawImage(i.getImage(), 0, 0, null);
                             tg.dispose();
 
                             // Schatten erstellen
                             final BufferedImage shadow = renderer.createShadow(temp);
 
                             final BufferedImage result = new BufferedImage(
                                     i.getIconWidth()
                                             + 6,
                                     i.getIconHeight()
                                             + 6,
                                     BufferedImage.TYPE_4BYTE_ABGR);
                             final Graphics rg = result.createGraphics();
                             rg.drawImage(shadow, 0, 0, null);
                             rg.drawImage(temp, 0, 0, null);
                             rg.setColor(new Color(0, 0, 0, 120));
                             rg.drawRect(0, 0, i.getIconWidth(), i.getIconHeight());
                             rg.dispose();
                             shadow.flush();
                             final ImageIcon icon = new ImageIcon(result);
 
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         jxhPicture.setIcon(icon);
                                         jxhPicture.setToolTipText("Klicken, um Bild im Browser zu ?ffnen");
                                         jxhPicture.addActionListener(new ActionListener() {
 
                                                 @Override
                                                 public void actionPerformed(final ActionEvent e) {
                                                     String url = "";
                                                     try {
                                                         url = properties.getProperty(
                                                                 "luftbildschraegaufnahmenservicefull");
                                                         if (url == null) {
                                                             BrowserLauncher.openURL(
                                                                 "http://s10220:8098/luft/tiffer?bnr="
                                                                         + nummer
                                                                         + "&scale=1&format=JPG");
                                                         } else {
                                                             final String newUrl = url.replaceAll(
                                                                     "<cismet::nummer>",
                                                                     nummer);
                                                             BrowserLauncher.openURL(newUrl);
                                                         }
                                                     } catch (Exception ex) {
                                                         log.error("Fehler beim ?ffnen der URL \"" + url + "\"", ex);
                                                     }
                                                 }
                                             });
                                     }
                                 });
                         } catch (Exception e) {
                             log.error(
                                 "Konnte Bild aus URL \"http://s10220:8098/luft/tiffer?bnr="
                                         + nummer
                                         + "&scale=0.15&format=JPG\" nicht erzeugen.");
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         jxhPicture.setIcon(error);
                                     }
                                 });
                         }
                     }
                 });
         t.start();
     }
 
     /**
      * Gibt das Verhaeltnis der Breite des Renderers zur Breite des internen Browsers aus.
      *
      * @return  Verhaeltnis Renderers / interner Browser
      */
     @Override
     public double getWidthRatio() {
         return 1.0;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         panAggregation = new BlurredMapObjectRenderer();
         panAgrTitle = new javax.swing.JPanel();
         jxhAgrPrint = new org.jdesktop.swingx.JXHyperlink();
         lblAgrTitle = new javax.swing.JLabel();
         panAgrContent = new javax.swing.JPanel();
         panTitle = new javax.swing.JPanel();
         jxhPrint = new org.jdesktop.swingx.JXHyperlink();
         lblTitle = new javax.swing.JLabel();
         panInter = new javax.swing.JPanel();
         panMap = new javax.swing.JPanel();
         panSpinner = new JLoadDots();
         panContent = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         lblLage = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         lblHinweise = new JBreakLabel(50, true);
         jLabel3 = new javax.swing.JLabel();
         lblAufnahmedat = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         lblErfassungsdat = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         lblAktdat = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         lblAuftraggeber = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         lblFotograf = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         lblFilmart = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         lblBildOrig = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         lblDateiname = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         lblOrganisation = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         lblLink = new javax.swing.JLabel();
         panPicture = new javax.swing.JPanel();
         jxhPicture = new org.jdesktop.swingx.JXHyperlink();
 
         panAggregation.setLayout(new java.awt.BorderLayout());
 
         panAgrTitle.setOpaque(false);
 
         jxhAgrPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/printer.png"))); // NOI18N
         jxhAgrPrint.setFocusPainted(false);
         jxhAgrPrint.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     AgrPrintActionPerformed(evt);
                 }
             });
 
         lblAgrTitle.setFont(new java.awt.Font("Tahoma", 1, 18));
         lblAgrTitle.setText("2 Luftbildaufnahmen");
 
         final javax.swing.GroupLayout panAgrTitleLayout = new javax.swing.GroupLayout(panAgrTitle);
         panAgrTitle.setLayout(panAgrTitleLayout);
         panAgrTitleLayout.setHorizontalGroup(
             panAgrTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 javax.swing.GroupLayout.Alignment.TRAILING,
                 panAgrTitleLayout.createSequentialGroup().addContainerGap().addComponent(lblAgrTitle).addPreferredGap(
                     javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                     62,
                     Short.MAX_VALUE).addComponent(
                     jxhAgrPrint,
                     javax.swing.GroupLayout.PREFERRED_SIZE,
                     javax.swing.GroupLayout.DEFAULT_SIZE,
                     javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
         panAgrTitleLayout.setVerticalGroup(
             panAgrTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 panAgrTitleLayout.createSequentialGroup().addContainerGap().addGroup(
                     panAgrTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                         lblAgrTitle).addComponent(
                         jxhAgrPrint,
                         javax.swing.GroupLayout.PREFERRED_SIZE,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(
                     javax.swing.GroupLayout.DEFAULT_SIZE,
                     Short.MAX_VALUE)));
 
         panAggregation.add(panAgrTitle, java.awt.BorderLayout.NORTH);
 
         panAgrContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 15, 15));
         panAgrContent.setOpaque(false);
         panAgrContent.setLayout(new java.awt.GridLayout(1, 2, 10, 10));
         panAggregation.add(panAgrContent, java.awt.BorderLayout.CENTER);
 
         setOpaque(false);
         setLayout(new java.awt.BorderLayout());
 
         panTitle.setOpaque(false);
 
         jxhPrint.setForeground(new java.awt.Color(255, 255, 255));
         jxhPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/printer.png"))); // NOI18N
         jxhPrint.setText("");
         jxhPrint.setToolTipText("Drucken");
         jxhPrint.setClickedColor(new java.awt.Color(204, 204, 204));
         jxhPrint.setFocusPainted(false);
         jxhPrint.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
         jxhPrint.setUnclickedColor(new java.awt.Color(255, 255, 255));
         jxhPrint.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     printActionPerformed(evt);
                 }
             });
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 18));
         lblTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblTitle.setText("Luftbildschr\u00E4gaufnahme - 571203");
 
         final javax.swing.GroupLayout panTitleLayout = new javax.swing.GroupLayout(panTitle);
         panTitle.setLayout(panTitleLayout);
         panTitleLayout.setHorizontalGroup(
             panTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 panTitleLayout.createSequentialGroup().addContainerGap().addComponent(lblTitle).addPreferredGap(
                     javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                     252,
                     Short.MAX_VALUE).addComponent(
                     jxhPrint,
                     javax.swing.GroupLayout.PREFERRED_SIZE,
                     javax.swing.GroupLayout.DEFAULT_SIZE,
                     javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
         panTitleLayout.setVerticalGroup(
             panTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 panTitleLayout.createSequentialGroup().addContainerGap().addGroup(
                     panTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(
                         jxhPrint,
                         javax.swing.GroupLayout.PREFERRED_SIZE,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(lblTitle)).addContainerGap(
                     13,
                     Short.MAX_VALUE)));
 
         add(panTitle, java.awt.BorderLayout.NORTH);
 
         panInter.setOpaque(false);
 
         final javax.swing.GroupLayout panInterLayout = new javax.swing.GroupLayout(panInter);
         panInter.setLayout(panInterLayout);
         panInterLayout.setHorizontalGroup(
             panInterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 600,
                 Short.MAX_VALUE));
         panInterLayout.setVerticalGroup(
             panInterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 14,
                 Short.MAX_VALUE));
 
         add(panInter, java.awt.BorderLayout.SOUTH);
 
         panMap.setOpaque(false);
         panMap.setLayout(new java.awt.GridBagLayout());
 
         panSpinner.setMaximumSize(new java.awt.Dimension(100, 100));
         panSpinner.setMinimumSize(new java.awt.Dimension(100, 100));
         panSpinner.setOpaque(false);
 
         final javax.swing.GroupLayout panSpinnerLayout = new javax.swing.GroupLayout(panSpinner);
         panSpinner.setLayout(panSpinnerLayout);
         panSpinnerLayout.setHorizontalGroup(
             panSpinnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 100,
                 Short.MAX_VALUE));
         panSpinnerLayout.setVerticalGroup(
             panSpinnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                 0,
                 100,
                 Short.MAX_VALUE));
 
         panMap.add(panSpinner, new java.awt.GridBagConstraints());
 
         add(panMap, java.awt.BorderLayout.CENTER);
 
         panContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 20));
         panContent.setOpaque(false);
         panContent.setLayout(new java.awt.GridBagLayout());
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel1.setText("Strasse / Lage:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel1, gridBagConstraints);
 
         lblLage.setText("Am Schaffstal");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblLage, gridBagConstraints);
 
         jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel2.setText("Hinweise:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel2, gridBagConstraints);
 
         lblHinweise.setText("Baustelle");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblHinweise, gridBagConstraints);
 
         jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel3.setText("Aufnahmedatum:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel3, gridBagConstraints);
 
         lblAufnahmedat.setText("2004-08-02");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblAufnahmedat, gridBagConstraints);
 
         jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel4.setText("Erfassungsdatum:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel4, gridBagConstraints);
 
         lblErfassungsdat.setText("2004-08-30");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblErfassungsdat, gridBagConstraints);
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel5.setText("Aktualisierungsdatum:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel5, gridBagConstraints);
 
         lblAktdat.setText("2005-03-11");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblAktdat, gridBagConstraints);
 
         jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel6.setText("Auftraggeber:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel6, gridBagConstraints);
 
         lblAuftraggeber.setText("Sadowski");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblAuftraggeber, gridBagConstraints);
 
         jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel7.setText("Fotograf:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel7, gridBagConstraints);
 
         lblFotograf.setText("Meiswinkel");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblFotograf, gridBagConstraints);
 
         jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel8.setText("Filmart:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel8, gridBagConstraints);
 
         lblFilmart.setText("4,5x6 Color-Neg");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblFilmart, gridBagConstraints);
 
         jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel9.setText("Bild im Original vorr\u00E4tig:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel9, gridBagConstraints);
 
         lblBildOrig.setText("ja");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblBildOrig, gridBagConstraints);
 
         jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel10.setText("Dateiname:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel10, gridBagConstraints);
 
         lblDateiname.setText("571283.tiff");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblDateiname, gridBagConstraints);
 
         jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel11.setText("Organisationskennzeichen:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel11, gridBagConstraints);
 
         lblOrganisation.setText("105 25");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblOrganisation, gridBagConstraints);
 
         jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel12.setText("Link:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 20);
         panContent.add(jLabel12, gridBagConstraints);
 
         lblLink.setText("http://s10220:8098/luft/");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panContent.add(lblLink, gridBagConstraints);
 
         panPicture.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));
         panPicture.setOpaque(false);
         panPicture.setLayout(new java.awt.BorderLayout());
 
         jxhPicture.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/load.png"))); // NOI18N
         jxhPicture.setFocusPainted(false);
         panPicture.add(jxhPicture, java.awt.BorderLayout.CENTER);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridheight = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
         panContent.add(panPicture, gridBagConstraints);
 
         add(panContent, java.awt.BorderLayout.WEST);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void AgrPrintActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_AgrPrintActionPerformed
         final Thread t = new Thread(new Runnable() {
 
                     @Override
                     public void run() {
                         try {
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         StaticSwingTools.showDialog(printingWaitDialog);
                                     }
                                 });
 
                             final String url = properties.getProperty("luftbildschraegaufnahmenservicefull");
                             final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(
                                     getClass().getResourceAsStream(
                                         "/de/cismet/cids/objectrenderer/LuftbildschraegaufnahmenA4H.jasper"));
                             JasperPrint jasperPrint = null;
                             try {
                                 for (int j = 0; j < agrNummer.size(); ++j) {
                                     ImageIcon i;
                                     if (url == null) {
                                         i = new ImageIcon(
                                                 new URL(
                                                     "http://s10220:8098/luft/tiffer?bnr="
                                                             + nummer
                                                             + "&scale=1&format=JPG"));
                                     } else {
                                         final String newUrl = url.replaceAll("<cismet::nummer>", agrNummer.get(j));
                                         if (log.isDebugEnabled()) {
                                             log.debug("Url der LSA:" + newUrl);
                                         }
                                         i = new ImageIcon(new URL(newUrl));
                                     }
 
                                     // falls kein Bild geladen werden konnte, erzeuge Ausnahme
                                     if (i == null) {
                                         throw new Exception("Luftbildaufnahme konnte nicht geladen werden.");
                                     }
 
                                     ImageIcon icon = i;
 
                                     final int w = icon.getIconWidth();
                                     final int h = icon.getIconHeight();
                                     if (w > h) {
                                         try {
                                             // drehen
                                             final BufferedImage src = new BufferedImage(
                                                     icon.getIconWidth(),
                                                     icon.getIconHeight(),
                                                     BufferedImage.TYPE_4BYTE_ABGR);
                                             final int iSizeDiff = src.getWidth() - src.getHeight();
 
                                             final Graphics2D o2d = (Graphics2D)src.createGraphics();
                                             o2d.drawImage(icon.getImage(), 0, 0, null);
                                             o2d.dispose();
 
                                             final AffineTransform affineTransform = AffineTransform.getRotateInstance(
                                                     Math.toRadians(90),
                                                     src.getWidth()
                                                             / 2,
                                                     src.getHeight()
                                                             / 2);
                                             final BufferedImage rotatedImage = new BufferedImage(
                                                     src.getHeight(),
                                                     src.getWidth(),
                                                     src.getType());
                                             final Graphics2D g = (Graphics2D)rotatedImage.getGraphics();
                                             g.setTransform(affineTransform);
                                             g.drawImage(src, iSizeDiff / 2, iSizeDiff / 2, null);
                                             icon = new ImageIcon(rotatedImage);
                                         } catch (Exception ex) {
                                             log.error("Drehen des Images fehlgeschlagen.", ex);
                                         }
                                     }
                                     if (log.isDebugEnabled()) {
                                         log.debug("LSA hinzugef\u00FCgt");
                                     }
                                     final HashMap params = new HashMap();
                                     params.put("Ueberschrift", lage + " (" + TITLE + ")");
                                     params.put("Unterschrift", hinweise);
                                     params.put("image", icon.getImage());
                                     if (log.isDebugEnabled()) {
                                         log.debug(params);
                                     }
                                     if (jasperPrint == null) {
                                         jasperPrint = JasperFillManager.fillReport(jasperReport, params);
                                     } else {
                                         jasperPrint.addPage(
                                             (JRPrintPage)JasperFillManager.fillReport(jasperReport, params).getPages()
                                                         .get(0));
                                     }
                                 }
 
                                 final JasperPrint jasperPrintCopy = jasperPrint;
                                 EventQueue.invokeLater(new Runnable() {
 
                                         @Override
                                         public void run() {
                                             printingWaitDialog.setVisible(false);
                                             final JRViewer aViewer = new JRViewer(jasperPrintCopy);
                                             aViewer.setZoomRatio(0.35f);
                                             final JFrame aFrame = new JFrame("Druckvorschau");
                                             aFrame.getContentPane().add(aViewer);
                                             final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit()
                                                         .getScreenSize();
                                             aFrame.setSize(screenSize.width / 2, screenSize.height / 2);
                                             final java.awt.Insets insets = aFrame.getInsets();
                                             aFrame.setSize(
                                                 aFrame.getWidth()
                                                         + insets.left
                                                         + insets.right,
                                                 aFrame.getHeight()
                                                         + insets.top
                                                         + insets.bottom
                                                         + 20);
                                             aFrame.setLocation(
                                                 (screenSize.width - aFrame.getWidth())
                                                         / 2,
                                                 (screenSize.height - aFrame.getHeight())
                                                         / 2);
                                             aFrame.setVisible(true);
                                         }
                                     });
                             } catch (Throwable e) {
                                 log.error("Fehler beim Jaspern", e);
                                 EventQueue.invokeLater(new Runnable() {
 
                                         @Override
                                         public void run() {
                                             printingWaitDialog.setVisible(false);
                                         }
                                     });
                             }
                         } catch (Exception e) {
                             log.error(
                                 "Fehler beim laden des Bildes mit URL \"http://s10220:8098/luft/tiffer?bnr="
                                         + nummer
                                         + "&scale=1&format=JPG\"",
                                 e);
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         printingWaitDialog.setVisible(false);
                                     }
                                 });
                         }
                     }
                 });
         t.start();
     } //GEN-LAST:event_AgrPrintActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void printActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_printActionPerformed
         final Thread t = new Thread(new Runnable() {
 
                     @Override
                     public void run() {
                         try {
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         StaticSwingTools.showDialog(printingWaitDialog);
                                     }
                                 });
 
                             final String url = properties.getProperty("luftbildschraegaufnahmenservicefull");
                             ImageIcon i;
                             if (url == null) {
                                 i = new ImageIcon(
                                         new URL(
                                             "http://s10220:8098/luft/tiffer?bnr="
                                                     + nummer
                                                     + "&scale=1&format=JPG"));
                             } else {
                                 final String newUrl = url.replaceAll("<cismet::nummer>", nummer);
                                 i = new ImageIcon(new URL(newUrl));
                             }
 
                             // falls kein Bild geladen werden konnte, erzeuge Ausnahme
                             if (i == null) {
                                 throw new Exception("Luftbildaufnahme konnte nicht geladen werden.");
                             }
 
                             ImageIcon icon = i;
 
                             final int w = icon.getIconWidth();
                             final int h = icon.getIconHeight();
                             if (w > h) {
                                 try {
                                     // drehen
                                     final BufferedImage src = new BufferedImage(
                                             icon.getIconWidth(),
                                             icon.getIconHeight(),
                                             BufferedImage.TYPE_4BYTE_ABGR);
                                     final int iSizeDiff = src.getWidth() - src.getHeight();
 
                                     final Graphics2D o2d = (Graphics2D)src.createGraphics();
                                     o2d.drawImage(icon.getImage(), 0, 0, null);
                                     o2d.dispose();
 
                                     final AffineTransform affineTransform = AffineTransform.getRotateInstance(
                                             Math.toRadians(90),
                                             src.getWidth()
                                                     / 2,
                                             src.getHeight()
                                                     / 2);
                                     final BufferedImage rotatedImage = new BufferedImage(
                                             src.getHeight(),
                                             src.getWidth(),
                                             src.getType());
                                     final Graphics2D g = (Graphics2D)rotatedImage.getGraphics();
                                     g.setTransform(affineTransform);
                                     g.drawImage(src, iSizeDiff / 2, iSizeDiff / 2, null);
                                     icon = new ImageIcon(rotatedImage);
                                 } catch (Exception ex) {
                                     log.error("Drehen des Images fehlgeschlagen.", ex);
                                 }
                             }
 
                             final HashMap params = new HashMap();
                             params.put("Ueberschrift", lage + " (" + TITLE + ")");
                             params.put("Unterschrift", hinweise);
                             params.put("image", icon.getImage());
                             // log.fatal(params);
 
                             try {
                                 final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(
                                         getClass().getResourceAsStream(
                                             "/de/cismet/cids/objectrenderer/LuftbildschraegaufnahmenA4H.jasper"));
                                 final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params);
                                 EventQueue.invokeLater(new Runnable() {
 
                                         @Override
                                         public void run() {
                                             printingWaitDialog.setVisible(false);
                                             final JRViewer aViewer = new JRViewer(jasperPrint);
                                             final JFrame aFrame = new JFrame("Druckvorschau");
                                             aFrame.getContentPane().add(aViewer);
                                             final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit()
                                                         .getScreenSize();
                                             aFrame.setSize(screenSize.width / 2, screenSize.height / 2);
                                             final java.awt.Insets insets = aFrame.getInsets();
                                             aFrame.setSize(
                                                 aFrame.getWidth()
                                                         + insets.left
                                                         + insets.right,
                                                 aFrame.getHeight()
                                                         + insets.top
                                                         + insets.bottom
                                                         + 20);
                                             aFrame.setLocation(
                                                 (screenSize.width - aFrame.getWidth())
                                                         / 2,
                                                 (screenSize.height - aFrame.getHeight())
                                                         / 2);
                                             aFrame.setVisible(true);
                                         }
                                     });
                             } catch (Throwable e) {
                                 log.error("Fehler beim Jaspern", e);
                                 EventQueue.invokeLater(new Runnable() {
 
                                         @Override
                                         public void run() {
                                             printingWaitDialog.setVisible(false);
                                         }
                                     });
                             }
                         } catch (Exception e) {
                             log.error(
                                 "Fehler beim laden des Bildes mit URL \"http://s10220:8098/luft/tiffer?bnr="
                                         + nummer
                                         + "&scale=1&format=JPG\"",
                                 e);
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         printingWaitDialog.setVisible(false);
                                     }
                                 });
                         }
                     }
                 });
         t.start();
     } //GEN-LAST:event_printActionPerformed
 }
