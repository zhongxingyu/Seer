 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * DokumentenPanel.java
  *
  * Created on 5. Januar 2005, 14:01
  */
 package de.cismet.verdis.gui;
 
 import java.applet.AppletContext;
 
 import java.awt.*;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.sql.*;
 
 import java.util.Collection;
 import java.util.Vector;
 
 import javax.swing.*;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.CidsBeanStore;
 
 import de.cismet.gui.tools.*;
 
 import de.cismet.tools.URLSplitter;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 import de.cismet.verdis.CidsAppBackend;
 import de.cismet.verdis.EditModeListener;
 import de.cismet.verdis.constants.KassenzeichenPropertyConstants;
 
 /**
  * DOCUMENT ME!
  *
  * @author   hell
  * @version  $Revision$, $Date$
  */
 public class DokumentenPanel extends javax.swing.JPanel implements EditModeListener, DropTargetListener, CidsBeanStore {
 
     //~ Instance fields --------------------------------------------------------
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private java.applet.AppletContext ac = null;
     private boolean inEditMode = false;
     private CidsBean kassenzeichenBean;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel panDocs;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form DokumentenPanel.
      */
     public DokumentenPanel() {
         initComponents();
         final DropTarget dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  ac  DOCUMENT ME!
      */
     public void setAppletContext(final java.applet.AppletContext ac) {
         this.ac = ac;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void emptyPanel() {
         panDocs.removeAll();
         panDocs.repaint();
     }
 
     @Override
     public CidsBean getCidsBean() {
         return kassenzeichenBean;
     }
 
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         kassenzeichenBean = cidsBean;
         emptyPanel();
         if (cidsBean != null) {
             final Collection<CidsBean> urls = (Collection)kassenzeichenBean.getProperty("dms_urls");
 
             for (final CidsBean url : urls) {
//                if (url.getProperty("typ") != null && (Integer) url.getProperty("typ") != 0) {
                     addNewDocPanel(ac, url);
//                }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   ac  DOCUMENT ME!
      * @param   cb  description DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public DocPanel addNewDocPanel(final AppletContext ac, final CidsBean cb) {
         final String name = (String)cb.getProperty("name");
 
         final int typ = (Integer)cb.getProperty("typ");
         final String protPrefix = (String)cb.getProperty("url_id.url_base_id.prot_prefix");
         final String server = (String)cb.getProperty("url_id.url_base_id.server");
         final String path = (String)cb.getProperty("url_id.url_base_id.path");
         final String objectName = (String)cb.getProperty("url_id.object_name");
         String urlString = protPrefix + server + path + objectName;
 
         log.info("AddNewDocPanel: " + urlString);
         ImageIcon ic = null;
         boolean deletable = false;
         if (typ == 0) {
             // Setze WMS Icon und h\u00E4nge Kassenzeichen an
             ic = new javax.swing.ImageIcon(getClass().getResource(
                         "/de/cismet/verdis/res/images/filetypes/dms_default.png"));
             urlString = urlString.trim() + kassenzeichenBean.getProperty(KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER_OLD);
             deletable = false;
         }
         if (typ >= 1) {
             // Setze das Icon nach der Dateiendung
             final int pPos = urlString.lastIndexOf(".");
             final String type = urlString.substring(pPos + 1, urlString.length()).toLowerCase();
             final String filename = type + ".png";
             deletable = true;
             try {
                 ic = new javax.swing.ImageIcon(getClass().getResource(
                             "/de/cismet/verdis/res/images/filetypes/"
                                     + filename));
             } catch (Exception e) {
                 if (log.isDebugEnabled()) {
                     log.debug("Fehler beim Suchen des Icons:" + type);
                 }
                 ic = new javax.swing.ImageIcon(getClass().getResource(
                             "/de/cismet/verdis/res/images/filetypes/dms_default.png"));
             }
         }
 
         final DocPanel dp = new DocPanel();
         dp.setAplettContext(ac);
         dp.setDesc(name);
         dp.setGotoUrl(urlString);
         dp.setIcon(ic);
         dp.setDeletable(deletable);
         dp.setCidsBean(cb);
         dp.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (e.getSource() instanceof DocPanel) {
                         if (e.getActionCommand().equals(DocPanel.DELETE_ACTION_COMMAND)) {
                             final DocPanel dp = (DocPanel)e.getSource();
                             panDocs.remove(dp);
                             kassenzeichenBean.getBeanCollectionProperty("dms_urls").remove(dp.getCidsBean());
                             panDocs.revalidate();
                             repaint();
                         }
                     }
                 }
             });
 
         panDocs.add(dp);
 
         revalidate();
 
         return dp;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         panDocs = new javax.swing.JPanel();
 
         setLayout(new java.awt.BorderLayout());
 
         panDocs.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
         add(panDocs, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  DOCUMENT ME!
      */
     public static void main(final String[] args) {
         final DokumentenPanel d = new DokumentenPanel();
         d.inEditMode = true;
         final JFrame f = new JFrame();
         f.setLayout(new BorderLayout());
         f.getContentPane().add(d, BorderLayout.CENTER);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.pack();
         f.setVisible(true);
     }
 
     @Override
     public void dragEnter(final DropTargetDragEvent dtde) {
         if (!inEditMode) {
             dtde.rejectDrag();
         }
     }
 
     @Override
     public void dragOver(final DropTargetDragEvent dtde) {
     }
 
     @Override
     public void dropActionChanged(final DropTargetDragEvent dtde) {
     }
 
     @Override
     public void dragExit(final DropTargetEvent dte) {
     }
 
     @Override
     public void drop(final DropTargetDropEvent dtde) {
         dtde.acceptDrop(DnDConstants.ACTION_COPY);
         final String link = StaticSwingTools.getLinkFromDropEvent(dtde);
         if (link != null) {
             //J-
             String description = JOptionPane.showInputDialog(
                     this,
                     "Welche Beschriftung soll der Link haben?",
                     link);
             //J+
             if (description != null) {
                 try {
                     final Collection<CidsBean> urls = kassenzeichenBean.getBeanCollectionProperty("dms_urls");
 
                     final CidsBean dmsUrlCB = CidsBean.createNewCidsBeanFromTableName(CidsAppBackend.DOMAIN, "dms_url");
                     final CidsBean urlCB = CidsBean.createNewCidsBeanFromTableName(CidsAppBackend.DOMAIN, "url");
                     final CidsBean urlBaseCB = CidsBean.createNewCidsBeanFromTableName(CidsAppBackend.DOMAIN, "url_base");
 
                     dmsUrlCB.setProperty("name", description);
                     dmsUrlCB.setProperty("typ", Integer.valueOf(1));
                     dmsUrlCB.setProperty("url_id", urlCB);
 
                     final URLSplitter splitter = new URLSplitter(link);
 
                     urlCB.setProperty("object_name", splitter.getObject_name());
                     urlCB.setProperty("url_base_id", urlBaseCB);
 
                     urlBaseCB.setProperty("prot_prefix", splitter.getProt_prefix());
                     urlBaseCB.setProperty("server", splitter.getServer());
                     urlBaseCB.setProperty("path", splitter.getPath());
 
                     urls.add(dmsUrlCB);
 
                     final DocPanel dp = addNewDocPanel(ac, dmsUrlCB);
                     this.repaint();
                 } catch (Exception e) {
                     log.fatal("Fehler beim Fuellen der CidsBeans", e);
                 }
             }
         }
     }
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07 temporary disabled --> handled in Main.java.
      *
      * @param  c  DOCUMENT ME!
      */
     public void setLeftTitlebarColor(final Color c) {
         // panTitle.setLeftColor(c);
         // panTitle.repaint();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isInEditMode() {
         return inEditMode;
     }
 
     @Override
     public void editModeChanged() {
         inEditMode = CidsAppBackend.getInstance().isEditable();
     }
 }
