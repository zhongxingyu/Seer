 /***************************************************
  *
  * cismet GmbH, Saarbruecken, Germany
  *
  *              ... and it just works.
  *
  ****************************************************/
 /*
  * CoolThemaRenderer.java
  *
  * Created on 10. November 3508, 11:56
  */
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 
 import Sirius.server.middleware.types.MetaObject;
 import de.cismet.cids.editors.EditorClosedEvent;
 
 import org.apache.log4j.Logger;
 
 import java.awt.CardLayout;
 
 import java.util.Collection;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 import de.cismet.cids.annotations.AggregationRenderer;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.DisposableCidsBeanStore;
 
 import de.cismet.cids.editors.EditorSaveListener;
 
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.TitleComponentProvider;
 import java.awt.event.MouseListener;
 import java.sql.Date;
 
 /**
  * de.cismet.cids.objectrenderer.CoolThemaRenderer.
  *
  * <p>Renderer for the "Thema"-theme</p>
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 @AggregationRenderer
 public class Alb_baulastEditor extends JPanel implements DisposableCidsBeanStore,
         TitleComponentProvider,
         FooterComponentProvider,
         BorderProvider,
         RequestsFullSizeComponent,
         EditorSaveListener {
 
     //~ Static fields/initializers ---------------------------------------------
     private static final Logger LOG = Logger.getLogger(Alb_baulastEditor.class);
     public static final String TITLE_AGR_PREFIX = "Baulasten";
     private static final String ACTION_TAG = "custom.baulast.document";
     //~ Instance fields --------------------------------------------------------
     private final boolean editable;
     private CidsBean cidsBean;
     private final CardLayout cardLayout;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private de.cismet.cids.custom.objecteditors.wunda_blau.Alb_picturePanel alb_picturePanel;
     private javax.swing.JButton btnBack;
     private javax.swing.JButton btnForward;
     private javax.swing.JLabel lblBack;
     private javax.swing.JLabel lblForw;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JPanel panButtons;
     private javax.swing.JPanel panFooter;
     private javax.swing.JPanel panFooterLeft;
     private javax.swing.JPanel panFooterRight;
     private de.cismet.cids.custom.objecteditors.wunda_blau.Alb_baulastEditorPanel panMain;
     private javax.swing.JPanel panTitle;
     private de.cismet.cids.editors.converters.SqlDateToUtilDateConverter sqlDateToUtilDateConverter;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
     /**
      * Creates new form CoolThemaRenderer.
      */
     public Alb_baulastEditor() {
         this(true);
     }
 
     /**
      * Creates new form CoolThemaRenderer.
      *
      * @param  editable  DOCUMENT ME!
      */
     public Alb_baulastEditor(final boolean editable) {
         this.editable = editable;
         this.initComponents();
         initFooterElements();
         cardLayout = (CardLayout) getLayout();
     }
 
     public static void addPruefungsInfoToBean(CidsBean cidsBean) {
         try {
             if (cidsBean != null && cidsBean.getMetaObject().getStatus() == MetaObject.MODIFIED) {
                 Object geprueftObj = cidsBean.getProperty("geprueft");
                 if (geprueftObj instanceof Boolean && ((Boolean) geprueftObj)) {
                     cidsBean.setProperty("geprueft_von", SessionManager.getSession().getUser().getName());
                     cidsBean.setProperty("pruefdatum", new Date(System.currentTimeMillis()));
                 } else {
                     cidsBean.setProperty("geprueft_von", null);
                     cidsBean.setProperty("pruefdatum", null);
                 }
             }
         } catch (Exception ex) {
             LOG.error("Can not set Pruefunfsinfo for Bean!", ex);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
     /**
      * DOCUMENT ME!
      */
     private void initFooterElements() {
         ObjectRendererUtils.decorateJLabelAndButtonSynced(
                 lblForw,
                 btnForward,
                 ObjectRendererUtils.FORWARD_SELECTED,
                 ObjectRendererUtils.FORWARD_PRESSED);
         ObjectRendererUtils.decorateJLabelAndButtonSynced(
                 lblBack,
                 btnBack,
                 ObjectRendererUtils.BACKWARD_SELECTED,
                 ObjectRendererUtils.BACKWARD_PRESSED);
     }
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  selection  DOCUMENT ME!
      */
     public void setAllSelectedMetaObjects(final Collection<MetaObject> selection) {
         this.panMain.setAllSelectedMetaObjects(selection);
     }
 
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         if (cidsBean != null) {
             this.cidsBean = cidsBean;
             disableSecondPageIfNoPermission();
             this.panMain.setCidsBean(cidsBean);
             this.alb_picturePanel.setCidsBean(cidsBean);
             final Object laufendeNr = cidsBean.getProperty("laufende_nummer");
             final Object blattNummer = cidsBean.getProperty("blattnummer");
             lblTitle.setText("Baulastblatt " + blattNummer + ": lfd. Nummer " + laufendeNr);
         }
     }
 
     private void disableSecondPageIfNoPermission() {
        if (ObjectRendererUtils.checkActionTag(ACTION_TAG)) {
             for (MouseListener l : lblForw.getMouseListeners()) {
                 lblForw.removeMouseListener(l);
             }
             lblForw.setEnabled(false);
             btnForward.setEnabled(false);
             for (MouseListener l : lblBack.getMouseListeners()) {
                 lblBack.removeMouseListener(l);
             }
             lblBack.setEnabled(false);
             btnBack.setEnabled(false);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  true if the fields/boxes are editable
      */
     public boolean isEditable() {
         return editable;
     }
 
     /**
      * WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         panTitle = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         sqlDateToUtilDateConverter = new de.cismet.cids.editors.converters.SqlDateToUtilDateConverter();
         panFooter = new javax.swing.JPanel();
         panButtons = new javax.swing.JPanel();
         panFooterLeft = new javax.swing.JPanel();
         lblBack = new javax.swing.JLabel();
         btnBack = new javax.swing.JButton();
         panFooterRight = new javax.swing.JPanel();
         btnForward = new javax.swing.JButton();
         lblForw = new javax.swing.JLabel();
         panMain = new de.cismet.cids.custom.objecteditors.wunda_blau.Alb_baulastEditorPanel(editable);
         alb_picturePanel = new de.cismet.cids.custom.objecteditors.wunda_blau.Alb_picturePanel(!editable);
 
         panTitle.setOpaque(false);
         panTitle.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("Tahoma", 1, 14));
         lblTitle.setForeground(new java.awt.Color(255, 255, 255));
         lblTitle.setText("Baulast");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panTitle.add(lblTitle, gridBagConstraints);
 
         panFooter.setOpaque(false);
         panFooter.setLayout(new java.awt.BorderLayout());
 
         panButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
         panButtons.setOpaque(false);
         panButtons.setLayout(new java.awt.GridBagLayout());
 
         panFooterLeft.setMaximumSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setMinimumSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setOpaque(false);
         panFooterLeft.setPreferredSize(new java.awt.Dimension(124, 40));
         panFooterLeft.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));
 
         lblBack.setFont(new java.awt.Font("Tahoma", 1, 14));
         lblBack.setForeground(new java.awt.Color(255, 255, 255));
         lblBack.setText("Info");
         lblBack.setEnabled(false);
         lblBack.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblBackMouseClicked(evt);
             }
         });
         panFooterLeft.add(lblBack);
 
         btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-left.png"))); // NOI18N
         btnBack.setBorder(null);
         btnBack.setBorderPainted(false);
         btnBack.setContentAreaFilled(false);
         btnBack.setEnabled(false);
         btnBack.setFocusPainted(false);
         btnBack.setMaximumSize(new java.awt.Dimension(30, 30));
         btnBack.setMinimumSize(new java.awt.Dimension(30, 30));
         btnBack.setPreferredSize(new java.awt.Dimension(30, 30));
         btnBack.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBackActionPerformed(evt);
             }
         });
         panFooterLeft.add(btnBack);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panButtons.add(panFooterLeft, gridBagConstraints);
 
         panFooterRight.setMaximumSize(new java.awt.Dimension(124, 40));
         panFooterRight.setOpaque(false);
         panFooterRight.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
 
         btnForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/arrow-right.png"))); // NOI18N
         btnForward.setBorder(null);
         btnForward.setBorderPainted(false);
         btnForward.setContentAreaFilled(false);
         btnForward.setFocusPainted(false);
         btnForward.setMaximumSize(new java.awt.Dimension(30, 30));
         btnForward.setMinimumSize(new java.awt.Dimension(30, 30));
         btnForward.setPreferredSize(new java.awt.Dimension(30, 30));
         btnForward.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnForwardActionPerformed(evt);
             }
         });
         panFooterRight.add(btnForward);
 
         lblForw.setFont(new java.awt.Font("Tahoma", 1, 14));
         lblForw.setForeground(new java.awt.Color(255, 255, 255));
         lblForw.setText("Dokumente");
         lblForw.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblForwMouseClicked(evt);
             }
         });
         panFooterRight.add(lblForw);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         panButtons.add(panFooterRight, gridBagConstraints);
 
         panFooter.add(panButtons, java.awt.BorderLayout.CENTER);
 
         setOpaque(false);
         setLayout(new java.awt.CardLayout());
 
         panMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         add(panMain, "card1");
         add(alb_picturePanel, "card2");
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblBackMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBackMouseClicked
         btnBackActionPerformed(null);
     }//GEN-LAST:event_lblBackMouseClicked
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnBackActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
         cardLayout.show(this, "card1");
         btnBack.setEnabled(false);
         btnForward.setEnabled(true);
         lblBack.setEnabled(false);
         lblForw.setEnabled(true);
     }//GEN-LAST:event_btnBackActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnForwardActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForwardActionPerformed
         cardLayout.show(this, "card2");
         btnBack.setEnabled(true);
         btnForward.setEnabled(false);
         lblBack.setEnabled(true);
         lblForw.setEnabled(false);
         alb_picturePanel.updateIfPicturePathsChanged();
         final String fileCollisionWarning = alb_picturePanel.getCollisionWarning();
         if (fileCollisionWarning.length() > 0) {
             JOptionPane.showMessageDialog(
                     this,
                     fileCollisionWarning,
                     "Unterschiedliche Dateiformate",
                     JOptionPane.WARNING_MESSAGE);
         }
         alb_picturePanel.clearCollisionWarning();
     }//GEN-LAST:event_btnForwardActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblForwMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblForwMouseClicked
         btnForwardActionPerformed(null);
     }//GEN-LAST:event_lblForwMouseClicked
 
     @Override
     public JComponent getTitleComponent() {
         return panTitle;
     }
 
     @Override
     public JComponent getFooterComponent() {
         return panFooter;
     }
 
     @Override
     public Border getTitleBorder() {
         return new EmptyBorder(10, 10, 10, 10);
     }
 
     @Override
     public Border getFooterBorder() {
         return new EmptyBorder(5, 5, 5, 5);
     }
 
     @Override
     public Border getCenterrBorder() {
         return new EmptyBorder(0, 5, 0, 5);
     }
 
     @Override
     public void dispose() {
         panMain.dispose();
         alb_picturePanel.dispose();
     }
 
 
     @Override
     public void editorClosed(EditorClosedEvent event) {        
     }
 
     @Override
     public boolean prepareForSave() {
         try {
             final Object laufendeNrObj = cidsBean.getProperty("laufende_nummer");
             final Object blattNrObj = cidsBean.getProperty("blattnummer");
             final boolean unique = Alb_Constraints.checkUniqueBaulastNummer(String.valueOf(blattNrObj),
                     String.valueOf(laufendeNrObj),
                     cidsBean.getMetaObject().getID());
             if (!unique) {
                 JOptionPane.showMessageDialog(
                         this,
                         "Die Laufende Nummer "
                         + laufendeNrObj
                         + " existiert bereits unter Baulastblatt "
                         + blattNrObj
                         + "! Bitte geben Sie eine andere Nummer ein.");
                 return false;
             }
             if (!Alb_Constraints.checkBaulastHasBelastetesFlurstueck(cidsBean)) {
                 JOptionPane.showMessageDialog(
                         this,
                         "Der Baulast ist noch kein belastetes Flurstück zugeordnet!\nBitte ordnen Sie mind. ein belastetes Flurstück zu, erst dann kann der Datensatz gespeichert werden.");
                 return false;
             }
             if (!Alb_Constraints.checkBaulastDates(cidsBean)) {
                 JOptionPane.showMessageDialog(
                         this,
                         "Sie haben unplausible Datumsangaben vorgenommen (Eingabedatum fehlt oder liegt nach dem Lösch- Schließ oder Befristungsdatum).\nBitte korrigieren Sie die fehlerhaften Datumsangaben, erst dann kann der Datensatz gespeichert werden.");
                 return false;
             }
 
             addPruefungsInfoToBean(cidsBean);
 
             return true;
         } catch (Exception ex) {
             ObjectRendererUtils.showExceptionWindowToUser("Fehler beim Speichern", ex, this);
             throw new RuntimeException(ex);
         }
     }
 }
