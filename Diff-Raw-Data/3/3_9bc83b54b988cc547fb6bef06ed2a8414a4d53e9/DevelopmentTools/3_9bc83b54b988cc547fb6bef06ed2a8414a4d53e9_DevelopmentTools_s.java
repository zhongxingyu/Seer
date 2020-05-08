 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.client.tools;
 
 /***************************************************
  *
  * cismet GmbH, Saarbruecken, Germany
  *
  *              ... and it just works.
  *
  ****************************************************/
 import Sirius.navigator.connection.Connection;
 import Sirius.navigator.connection.ConnectionFactory;
 import Sirius.navigator.connection.ConnectionInfo;
 import Sirius.navigator.connection.ConnectionSession;
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.connection.proxy.ConnectionProxy;
 
 import Sirius.server.middleware.interfaces.proxy.CatalogueService;
 import Sirius.server.middleware.interfaces.proxy.MetaService;
 import Sirius.server.middleware.interfaces.proxy.SearchService;
 import Sirius.server.middleware.interfaces.proxy.UserService;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRRewindableDataSource;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jasperreports.engine.util.JRLoader;
 import net.sf.jasperreports.swing.JRViewer;
 
 import java.awt.BorderLayout;
 
 import java.rmi.Naming;
 import java.rmi.Remote;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.UIManager;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.editors.CidsObjectEditorFactory;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsObjectRendererFactory;
 
 import de.cismet.jasperreports.CidsBeanDataSource;
 
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class DevelopmentTools {
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain  DOCUMENT ME!
      * @param   group   DOCUMENT ME!
      * @param   user    DOCUMENT ME!
      * @param   pass    DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void initSessionManagerFromRMIConnectionOnLocalhost(final String domain,
             final String group,
             final String user,
             final String pass) throws Exception {
         System.out.println("start");
         // lookup des callservers
         final Remote r;
         final SearchService ss;
         final CatalogueService cat;
         final MetaService meta;
         final UserService us;
         final User u;
 
         Log4JQuickConfig.configure4LumbermillOnLocalhost();
         r = (Remote)Naming.lookup("rmi://localhost/callServer");
         System.out.println("server gefunden");
         ss = (SearchService)r;
         cat = (CatalogueService)r;
         meta = (MetaService)r;
         us = (UserService)r;
         u = us.getUser(domain, group, domain, user, pass);
         System.out.println("user angemeldet");
         ConnectionSession session = null;
         ConnectionProxy proxy = null;
         final ConnectionInfo info = new ConnectionInfo();
         info.setCallserverURL("rmi://localhost/callServer");
         info.setUsername(user);
         info.setUsergroup(group);
         info.setPassword(pass);
         info.setUserDomain(domain);
         info.setUsergroupDomain(domain);
 
         final Connection connection = ConnectionFactory.getFactory()
                     .createConnection(
                         "Sirius.navigator.connection.RMIConnection",
                         info.getCallserverURL());
 
         session = ConnectionFactory.getFactory().createSession(connection,
                 info, true);
         proxy = ConnectionFactory.getFactory()
                     .createProxy(
                             "Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler",
                             session);
         System.out.println("sessionmanager initialisieren");
         SessionManager.init(proxy);
 
         ClassCacheMultiple.setInstance(domain);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain    DOCUMENT ME!
      * @param   group     DOCUMENT ME!
      * @param   user      DOCUMENT ME!
      * @param   pass      DOCUMENT ME!
      * @param   table     DOCUMENT ME!
      * @param   objectId  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static CidsBean createCidsBeanFromRMIConnectionOnLocalhost(final String domain,
             final String group,
             final String user,
             final String pass,
             final String table,
             final int objectId) throws Exception {
         if (!SessionManager.isInitialized()) {
             initSessionManagerFromRMIConnectionOnLocalhost(domain, group, user, pass);
         }
         System.out.println("MO abfragen");
 
         final MetaClass mc = ClassCacheMultiple.getMetaClass(domain, table);
 
         final MetaObject mo = SessionManager.getConnection()
                     .getMetaObject(SessionManager.getSession().getUser(), objectId, mc.getId(), domain);
         final CidsBean cidsBean = mo.getBean();
         System.out.println("cidsBean erzeugt");
         return cidsBean;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain  DOCUMENT ME!
      * @param   group   DOCUMENT ME!
      * @param   user    DOCUMENT ME!
      * @param   pass    DOCUMENT ME!
      * @param   table   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static CidsBean[] createCidsBeansFromRMIConnectionOnLocalhost(final String domain,
             final String group,
             final String user,
             final String pass,
             final String table) throws Exception {
         return createCidsBeansFromRMIConnectionOnLocalhost(domain, group, user, pass, table, 0);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain  DOCUMENT ME!
      * @param   group   DOCUMENT ME!
      * @param   user    DOCUMENT ME!
      * @param   pass    DOCUMENT ME!
      * @param   table   DOCUMENT ME!
      * @param   limit   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static CidsBean[] createCidsBeansFromRMIConnectionOnLocalhost(final String domain,
             final String group,
             final String user,
             final String pass,
             final String table,
             final int limit) throws Exception {
         String limitS = "";
 
         if (limit > 0) {
             limitS = "LIMIT " + limit;
         }
         if (!SessionManager.isInitialized()) {
             initSessionManagerFromRMIConnectionOnLocalhost(domain, group, user, pass);
         }
         final MetaClass mc = ClassCacheMultiple.getMetaClass(domain, table);
 
         System.out.println("bauen ...");
         final MetaObject[] metaObjects = SessionManager.getConnection()
                     .getMetaObjectByQuery(SessionManager.getSession().getUser(),
                         "SELECT "
                         + mc.getID()
                         + ", "
                         + mc.getPrimaryKey()
                         + " FROM "
                         + mc.getTableName()
                         + " order by "
                         + mc.getPrimaryKey()
                         + " "
                         + limitS);
         final CidsBean[] cidsBeans = new CidsBean[metaObjects.length];
         for (int i = 0; i < metaObjects.length; i++) {
             final MetaObject metaObject = metaObjects[i];
             cidsBeans[i] = metaObject.getBean();
         }
         System.out.println("CidsBeans erzeugt.");
         return cidsBeans;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   path    DOCUMENT ME!
      * @param   domain  DOCUMENT ME!
      * @param   group   DOCUMENT ME!
      * @param   user    DOCUMENT ME!
      * @param   pass    DOCUMENT ME!
      * @param   table   DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void showReportForCidsBeans(final String path,
             final String domain,
             final String group,
             final String user,
             final String pass,
             final String table) throws Exception {
         System.out.print("Lade JasperReport ...");
         final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(DevelopmentTools.class.getResourceAsStream(
                     path));
         System.out.println(" geladen.\nErstelle Datenquelle ...");
         final JRRewindableDataSource dataSource = new CidsBeanDataSource(createCidsBeansFromRMIConnectionOnLocalhost(
                     domain,
                     group,
                     user,
                     pass,
                     table));
         boolean hasEntries = false;
         try {
             hasEntries = dataSource.next();
         } catch (JRException e) {
         } finally {
             dataSource.moveFirst();
         }
         System.out.println("Datenquelle erstellt. Daten verfügbar? " + hasEntries + ".");
         if (!hasEntries) {
             return;
         }
         System.out.print("Fülle Report ...");
         // print aus report und daten erzeugen
         final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), dataSource);
         System.out.print(" gefüllt.\nZeige Report an ...");
         final JRViewer aViewer = new JRViewer(jasperPrint);
         final JFrame aFrame = new JFrame(path); // NOI18N
         aFrame.getContentPane().add(aViewer);
         final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         aFrame.setSize(screenSize.width / 2, screenSize.height / 2);
         final java.awt.Insets insets = aFrame.getInsets();
         aFrame.setSize(aFrame.getWidth() + insets.left + insets.right,
             aFrame.getHeight()
                     + insets.top
                     + insets.bottom
                     + 20);
         aFrame.setLocation((screenSize.width - aFrame.getWidth()) / 2,
             (screenSize.height - aFrame.getHeight())
                     / 2);
         aFrame.setVisible(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   path  DOCUMENT ME!
      * @param   c     DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void showReportForBeans(final String path, final Collection c) throws Exception {
         System.out.print("Lade JasperReport ...");
         final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(DevelopmentTools.class.getResourceAsStream(
                     path));
         System.out.println(" geladen.\nErstelle Datenquelle ...");
         final JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(c);
 
         boolean hasEntries = false;
         try {
             hasEntries = dataSource.next();
         } finally {
             dataSource.moveFirst();
         }
         System.out.println("Datenquelle erstellt. Daten verfügbar? " + hasEntries + ".");
         if (!hasEntries) {
             return;
         }
         System.out.print("Fülle Report ...");
         // print aus report und daten erzeugen
         final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), dataSource);
 //        JasperExportManager.exportReportToPdfFile(jasperPrint, "/Users/thorsten/xxx.pdf");
 
         System.out.print(" gefüllt.\nZeige Report an ...");
         final JRViewer aViewer = new JRViewer(jasperPrint);
         final JFrame aFrame = new JFrame(path); // NOI18N
         aFrame.getContentPane().add(aViewer);
         final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         aFrame.setSize(700, 970);
         final java.awt.Insets insets = aFrame.getInsets();
         aFrame.setSize(aFrame.getWidth() + insets.left + insets.right,
             aFrame.getHeight()
                     + insets.top
                     + insets.bottom
                     + 20);
         aFrame.setLocation((screenSize.width - aFrame.getWidth()) / 2,
             (screenSize.height - aFrame.getHeight())
                     / 2);
         aFrame.setVisible(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   path       DOCUMENT ME!
      * @param   cidsBeans  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void showReportForCidsBeans(final String path,
             final CidsBean[] cidsBeans) throws Exception {
         System.out.print("Lade JasperReport ...");
         final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(DevelopmentTools.class.getResourceAsStream(
                     path));
         System.out.println(" geladen.\nErstelle Datenquelle ...");
         final JRRewindableDataSource dataSource = new CidsBeanDataSource(cidsBeans);
         boolean hasEntries = false;
         try {
             hasEntries = dataSource.next();
         } catch (JRException e) {
         } finally {
             dataSource.moveFirst();
         }
         System.out.println("Datenquelle erstellt. Daten verfügbar? " + hasEntries + ".");
         if (!hasEntries) {
             return;
         }
         System.out.print("Fülle Report ...");
         // print aus report und daten erzeugen
         final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), dataSource);
 //        JasperExportManager.exportReportToPdfFile(jasperPrint, "/Users/thorsten/xxx.pdf");
 
         System.out.print(" gefüllt.\nZeige Report an ...");
         final JRViewer aViewer = new JRViewer(jasperPrint);
         final JFrame aFrame = new JFrame(path); // NOI18N
         aFrame.getContentPane().add(aViewer);
         final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         aFrame.setSize(700, 970);
         final java.awt.Insets insets = aFrame.getInsets();
         aFrame.setSize(aFrame.getWidth() + insets.left + insets.right,
             aFrame.getHeight()
                     + insets.top
                     + insets.bottom
                     + 20);
         aFrame.setLocation((screenSize.width - aFrame.getWidth()) / 2,
             (screenSize.height - aFrame.getHeight())
                     / 2);
         aFrame.setVisible(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  c  DOCUMENT ME!
      * @param  w  DOCUMENT ME!
      * @param  h  DOCUMENT ME!
      */
     public static void showTestFrame(final JComponent c, final int w, final int h) {
         final JFrame jf = new JFrame("Test");
         jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         jf.getContentPane().setLayout(new BorderLayout());
         jf.getContentPane().add(c, BorderLayout.CENTER);
         jf.setSize(
             w,
             h);
         jf.setVisible(
             true);
         final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 
         jf.setBounds(
             (screenSize.width - w)
                     / 2,
             (screenSize.height - h)
                     / 2,
             w,
             h);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain    DOCUMENT ME!
      * @param   group     DOCUMENT ME!
      * @param   user      DOCUMENT ME!
      * @param   pass      DOCUMENT ME!
      * @param   table     DOCUMENT ME!
      * @param   objectId  DOCUMENT ME!
      * @param   w         DOCUMENT ME!
      * @param   h         DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void createEditorInFrameFromRMIConnectionOnLocalhost(final String domain,
             final String group,
             final String user,
             final String pass,
             final String table,
             final int objectId,
             final int w,
             final int h) throws Exception {
         UIManager.installLookAndFeel("Plastic 3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel"); // NOI18N
         final String heavyComps = System.getProperty("contains.heavyweight.comps");
         if ((heavyComps != null) && heavyComps.equals("true")) {
             com.jgoodies.looks.Options.setPopupDropShadowEnabled(false);
         }
         UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
         final CidsBean cb = createCidsBeanFromRMIConnectionOnLocalhost(domain, group, user, pass, table, objectId);
         final JComponent c = CidsObjectEditorFactory.getInstance().getEditor(cb.getMetaObject());
         showTestFrame(c, w, h);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain    DOCUMENT ME!
      * @param   group     DOCUMENT ME!
      * @param   user      DOCUMENT ME!
      * @param   pass      DOCUMENT ME!
      * @param   table     DOCUMENT ME!
      * @param   objectId  DOCUMENT ME!
      * @param   title     DOCUMENT ME!
      * @param   w         DOCUMENT ME!
      * @param   h         DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void createRendererInFrameFromRMIConnectionOnLocalhost(final String domain,
             final String group,
             final String user,
             final String pass,
             final String table,
             final int objectId,
             final String title,
             final int w,
             final int h) throws Exception {
         UIManager.installLookAndFeel("Plastic 3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel"); // NOI18N
         final String heavyComps = System.getProperty("contains.heavyweight.comps");
         if ((heavyComps != null) && heavyComps.equals("true")) {
             com.jgoodies.looks.Options.setPopupDropShadowEnabled(false);
         }
         UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
         final CidsBean cb = createCidsBeanFromRMIConnectionOnLocalhost(domain, group, user, pass, table, objectId);
         final JComponent c = CidsObjectRendererFactory.getInstance().getSingleRenderer(cb.getMetaObject(), title);
         showTestFrame(c, w, h);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   beans  DOCUMENT ME!
      * @param   title  DOCUMENT ME!
      * @param   w      DOCUMENT ME!
      * @param   h      DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void createAggregationRendererInFrameFromRMIConnectionOnLocalhost(final Collection<CidsBean> beans,
             final String title,
             final int w,
             final int h) throws Exception {
         UIManager.installLookAndFeel("Plastic 3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel"); // NOI18N
         final String heavyComps = System.getProperty("contains.heavyweight.comps");
         if ((heavyComps != null) && heavyComps.equals("true")) {
             com.jgoodies.looks.Options.setPopupDropShadowEnabled(false);
         }
         UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
         final ArrayList<MetaObject> mos = new ArrayList<MetaObject>(beans.size());
 
         for (final CidsBean b : beans) {
             mos.add(b.getMetaObject());
         }
         final JComponent c = CidsObjectRendererFactory.getInstance().getAggregationRenderer(mos, title);
         showTestFrame(c, w, h);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain    DOCUMENT ME!
      * @param   group     DOCUMENT ME!
      * @param   user      DOCUMENT ME!
      * @param   pass      DOCUMENT ME!
      * @param   table     DOCUMENT ME!
      * @param   objectId  DOCUMENT ME!
      * @param   w         DOCUMENT ME!
      * @param   h         DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void createEditorInFrameFromRMIConnectionOnLocalhostInScrollPane(final String domain,
             final String group,
             final String user,
             final String pass,
             final String table,
             final int objectId,
             final int w,
             final int h) throws Exception {
         UIManager.installLookAndFeel("Plastic 3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel"); // NOI18N
         final String heavyComps = System.getProperty("contains.heavyweight.comps");
         if ((heavyComps != null) && heavyComps.equals("true")) {
             com.jgoodies.looks.Options.setPopupDropShadowEnabled(false);
         }
         UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
         final CidsBean cb = createCidsBeanFromRMIConnectionOnLocalhost(domain, group, user, pass, table, objectId);
         final JComponent c = CidsObjectEditorFactory.getInstance().getEditor(cb.getMetaObject());
         final JScrollPane jsp = new JScrollPane(c);
         showTestFrame(jsp, w, h);
     }
 }
