 /*
  * CoolThemaRenderer.java
  *
  * Created on 10. November 3508, 11:56
  */
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 import de.cismet.cids.annotations.AggregationRenderer;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUIUtils;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.CidsBeanStore;
 import de.cismet.tools.gui.BorderProvider;
 import de.cismet.tools.gui.FooterComponentProvider;
 import de.cismet.tools.gui.TitleComponentProvider;
 import java.awt.CardLayout;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 /**
  * de.cismet.cids.objectrenderer.CoolThemaRenderer
  *
  * Renderer for the "Thema"-theme
  *
  * @author srichter
  */
 @AggregationRenderer
 public class Alb_baulastEditor extends JPanel implements CidsBeanStore, TitleComponentProvider, FooterComponentProvider, BorderProvider, RequestsFullSizeComponent {
 
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alb_baulastEditor.class);
     private final boolean editable;
     private CidsBean cidsBean;
     private final CardLayout cardLayout;
     public static final String TITLE_PREFIX = "Baulast";
     public static final String TITLE_AGR_PREFIX = "Baulasten";
 
     /** Creates new form CoolThemaRenderer */
     public Alb_baulastEditor(final boolean editable) {
         this.editable = editable;
         this.initComponents();
         initFooterElements();
         cardLayout = (CardLayout) getLayout();
     }
 
     private void initFooterElements() {
         ObjectRendererUIUtils.decorateJLabelAndButtonSynced(lblForw, btnForward, ObjectRendererUIUtils.FORWARD_SELECTED, ObjectRendererUIUtils.FORWARD_PRESSED);
         ObjectRendererUIUtils.decorateJLabelAndButtonSynced(lblBack, btnBack, ObjectRendererUIUtils.BACKWARD_SELECTED, ObjectRendererUIUtils.BACKWARD_PRESSED);
     }
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         if (cidsBean != null) {
             this.cidsBean = cidsBean;
             this.panMain.setCidsBean(cidsBean);
             this.alb_picturePanel.setCidsBean(cidsBean);
             final Object laufendeNr = cidsBean.getProperty("laufende_nummer");
             lblTitle.setText(TITLE_PREFIX + " " + laufendeNr);
         }
     }
 
     /** Creates new form CoolThemaRenderer */
     public Alb_baulastEditor() {
         this(true);
     }
 
     /**
      * 
      * @return true if the fields/boxes are editable
      */
     public boolean isEditable() {
         return editable;
     }
 
     /**
      *
      * @return a ThemaBean which encapsulates all the information for this theme
      * from the GUI.
      */
 //    public ThemaBean getContent() {
 //        final ThemaBean.Builder builder = ThemaBean.builder();
 //        builder.aenderungsdatum(new Date(System.currentTimeMillis())).aktAggregierenderObjektRenderer(chkAktAggObjektRenderer.isSelected()).aktAnsprechpartnerImplementierung(evalCB(cbAktImplementierung)).aktAnsprechpartnerSpezifizierung(evalCB(cbAktAnspSpezif)).aktEditoren(chkAktEditor.isSelected()).aktFeatureRenderer(chkAktFeatureRenderer.isSelected()).aktGeometrieModell(evalCB(cbAktGeometriemodell)).aktKategorie(evalCB(cbAktKategorie)).aktObjektRenderer(chkAktObjektRenderer.isSelected()).aktRealisierungsstand(evalCB(cbAktRealisierung)).aktReportTemplates(chkAktReportTemplates.isSelected()).aktSpezifischesSuchen(chkAktSpezSuchen.isSelected()).aktWartungsvertrag(evalCB(cbAktWartung)).ansprechpartner(txtAnsprechpartner.getText()).bemerkungen(txtBemerkung.getText()).bezeichnung(txtBezeichnung.getText()).fachthema(chkFachthema.isSelected()).fachverfahren(chkFachverfahren.isSelected()).gazDienst(evalCB(cbDienst)).gazGeoportalGUI(chkGeoPortal.isSelected()).gazLocationtypes(txtLocationtypes.getText()).gazWundaGUI(chkWundaGui.isSelected()).inhalt(txtInhalt.getText()).letzteAenderungDurch(evalCB(cbLetzteAktualisierung)).nutzerzahl(txtNutzerzahl.getText()).nutzungsart(evalCB(cbNutzungsart)).organisationseinheit(txtOrganisationseinheit.getText()).wmsDatenbereitstellung(evalCB(cbDatenbereitstellung)).wmsDatenquelle(evalCB(cbDatenquelle)).wmsKaskadiertAufSims(chkKaskadiert.isSelected()).wmsPubliziertImGeoportal(chkPubliziert.isSelected()).wmsWms(evalCB(cbWMS)).zukAggregierenderObjektRenderer(chkZukAggObjektRenderer.isSelected()).zukAnsprechpartnerImplementierung(evalCB(cbZukImplementierung)).zukAnsprechpartnerSpezifizierung(evalCB(cbZukAnspSpezif)).zukEditoren(chkZukEditor.isSelected()).zukFeatureRenderer(chkZukFeatureRenderer.isSelected()).zukGeometrieModell(evalCB(cbZukGeometriemodell)).zukKategorie(evalCB(cbZukKategorie)).zukObjektRenderer(chkZukObjektRenderer.isSelected()).zukRealisierungsstand(evalCB(cbZukRealisierung)).zukReportTemplates(chkZukReportTemplates.isSelected()).zukSpezifischesSuchen(chkZukSpezSuchen.isSelected()).zukWartungsvertrag(evalCB(cbZukWartung));
 //        return builder.build();
 //    }
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
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
         alb_picturePanel = new de.cismet.cids.custom.objecteditors.wunda_blau.Alb_picturePanel();
 
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
 
     private void lblBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBackMouseClicked
         btnBackActionPerformed(null);
 }//GEN-LAST:event_lblBackMouseClicked
 
     private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
         cardLayout.show(this, "card1");
         btnBack.setEnabled(false);
         btnForward.setEnabled(true);
         lblBack.setEnabled(false);
         lblForw.setEnabled(true);
 }//GEN-LAST:event_btnBackActionPerformed
 
     private void btnForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForwardActionPerformed
         cardLayout.show(this, "card2");
         btnBack.setEnabled(true);
         btnForward.setEnabled(false);
         lblBack.setEnabled(true);
         lblForw.setEnabled(false);
        alb_picturePanel.zoomToFeatureCollection();
 }//GEN-LAST:event_btnForwardActionPerformed
 
     private void lblForwMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblForwMouseClicked
         btnForwardActionPerformed(null);
 }//GEN-LAST:event_lblForwMouseClicked
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
 }
