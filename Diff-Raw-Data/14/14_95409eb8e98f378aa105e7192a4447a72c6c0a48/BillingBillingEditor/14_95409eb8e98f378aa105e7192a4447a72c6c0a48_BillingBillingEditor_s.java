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
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 
 import org.apache.log4j.Logger;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import java.util.Date;
 
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.client.tools.DevelopmentTools;
 
 import de.cismet.cids.custom.objecteditors.utils.RendererTools;
 import de.cismet.cids.custom.objectrenderer.converter.BooleanConverter;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.editors.DefaultCustomObjectEditor;
 import de.cismet.cids.editors.EditorClosedEvent;
 import de.cismet.cids.editors.EditorSaveListener;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 
 import de.cismet.tools.gui.RoundedPanel;
 
 /**
  * DOCUMENT ME!
  *
  * @author   Gilles Baatz
  * @version  $Revision$, $Date$
  */
 public class BillingBillingEditor extends javax.swing.JPanel implements CidsBeanRenderer,
     EditorSaveListener,
     RequestsFullSizeComponent {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(BillingBillingEditor.class);
     private static DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
 
     //~ Instance fields --------------------------------------------------------
 
     String originalGeschaeftsbuchnummer;
     String originalProjektbezeichnung;
     private final boolean editable;
     private CidsBean cidsBean;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnStornoBuchung;
     private de.cismet.cids.editors.DefaultBindableJCheckBox cboAbgerechnet;
     private de.cismet.cids.editors.DefaultBindableJCheckBox cboStorno;
     private de.cismet.cids.editors.DefaultBindableReferenceCombo coboStornogrund;
     private de.cismet.cids.editors.DefaultBindableJTextField defaultBindableJTextField5;
     private de.cismet.cids.editors.DefaultBindableJTextField defaultBindableJTextField7;
     private de.cismet.cids.editors.DefaultBindableJTextField defaultBindableJTextField8;
     private javax.swing.Box.Filler filler2;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JLabel lblGeschaeftsbuchnummer;
     private javax.swing.JLabel lblProjektbezeichnung;
     private javax.swing.JLabel lblStornogrund;
     private javax.swing.JPanel roundedPanel;
     private javax.swing.JScrollPane scpMain;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel1;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel2;
     private de.cismet.tools.gui.SemiRoundedPanel semiRoundedPanel3;
     private de.cismet.cids.editors.DefaultBindableJTextField txtAngelegt_am;
     private de.cismet.cids.editors.DefaultBindableJTextField txtAngelegt_durch;
     private de.cismet.cids.editors.DefaultBindableJTextField txtGeschaeftsbuchnummer;
     private de.cismet.cids.editors.DefaultBindableJTextField txtModus;
     private de.cismet.cids.editors.DefaultBindableJTextField txtProjektbezeichnung;
     private de.cismet.cids.editors.DefaultBindableJTextField txtVerwendungszweck;
     private javax.swing.JTextArea txtaAenderung;
     private javax.swing.JTextArea txtaBerechnung;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form BillingBillingEditor.
      */
     public BillingBillingEditor() {
         this(true);
     }
 
     /**
      * Creates a new BillingBillingEditor object.
      *
      * @param  editable  DOCUMENT ME!
      */
     public BillingBillingEditor(final boolean editable) {
         this.editable = editable;
         initComponents();
         if (!editable) {
             RendererTools.makeReadOnly(txtGeschaeftsbuchnummer);
             RendererTools.makeReadOnly(txtProjektbezeichnung);
             RendererTools.makeReadOnly(coboStornogrund);
         }
 
        RendererTools.makeReadOnly(btnStornoBuchung);
         RendererTools.makeReadOnly(cboStorno);
         RendererTools.makeReadOnly(cboAbgerechnet);
         RendererTools.makeReadOnly(defaultBindableJTextField5);
         RendererTools.makeReadOnly(defaultBindableJTextField7);
         RendererTools.makeReadOnly(defaultBindableJTextField8);
        RendererTools.makeReadOnly(lblGeschaeftsbuchnummer);
        RendererTools.makeReadOnly(lblProjektbezeichnung);
        RendererTools.makeReadOnly(lblStornogrund);
         RendererTools.makeReadOnly(txtAngelegt_am);
         RendererTools.makeReadOnly(txtAngelegt_durch);
         RendererTools.makeReadOnly(txtModus);
         RendererTools.makeReadOnly(txtVerwendungszweck);
         RendererTools.makeReadOnly(txtaAenderung);
         RendererTools.makeReadOnly(txtaBerechnung);
         scpMain.getViewport().setOpaque(false);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         roundedPanel = new RoundedPanel();
         scpMain = new javax.swing.JScrollPane();
         jPanel1 = new javax.swing.JPanel();
         jPanel4 = new RoundedPanel();
         jLabel12 = new javax.swing.JLabel();
         cboAbgerechnet = new de.cismet.cids.editors.DefaultBindableJCheckBox();
         jLabel4 = new javax.swing.JLabel();
         txtModus = new de.cismet.cids.editors.DefaultBindableJTextField();
         jLabel5 = new javax.swing.JLabel();
         txtVerwendungszweck = new de.cismet.cids.editors.DefaultBindableJTextField();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         defaultBindableJTextField5 = new de.cismet.cids.editors.DefaultBindableJTextField();
         jLabel9 = new javax.swing.JLabel();
         txtAngelegt_am = new de.cismet.cids.editors.DefaultBindableJTextField();
         jLabel10 = new javax.swing.JLabel();
         defaultBindableJTextField7 = new de.cismet.cids.editors.DefaultBindableJTextField();
         jLabel11 = new javax.swing.JLabel();
         defaultBindableJTextField8 = new de.cismet.cids.editors.DefaultBindableJTextField();
         txtaAenderung = new javax.swing.JTextArea();
         txtaBerechnung = new javax.swing.JTextArea();
         txtAngelegt_durch = new de.cismet.cids.editors.DefaultBindableJTextField();
         jLabel13 = new javax.swing.JLabel();
         semiRoundedPanel3 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel3 = new javax.swing.JLabel();
         filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 32767));
         jPanel3 = new RoundedPanel();
         txtProjektbezeichnung = new de.cismet.cids.editors.DefaultBindableJTextField();
         lblProjektbezeichnung = new javax.swing.JLabel();
         lblGeschaeftsbuchnummer = new javax.swing.JLabel();
         txtGeschaeftsbuchnummer = new de.cismet.cids.editors.DefaultBindableJTextField();
         semiRoundedPanel1 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel1 = new javax.swing.JLabel();
         jPanel2 = new RoundedPanel();
         lblStornogrund = new javax.swing.JLabel();
         coboStornogrund = new de.cismet.cids.editors.DefaultBindableReferenceCombo(true);
         if (editable) {
             btnStornoBuchung = new javax.swing.JButton();
         }
         cboStorno = new de.cismet.cids.editors.DefaultBindableJCheckBox();
         semiRoundedPanel2 = new de.cismet.tools.gui.SemiRoundedPanel();
         jLabel2 = new javax.swing.JLabel();
 
         setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
         setLayout(new java.awt.GridBagLayout());
 
         roundedPanel.setBackground(new java.awt.Color(254, 254, 254));
         roundedPanel.setForeground(new java.awt.Color(254, 254, 254));
         roundedPanel.setOpaque(false);
         roundedPanel.setLayout(new java.awt.GridBagLayout());
 
         scpMain.setBorder(null);
         scpMain.setFocusable(false);
         scpMain.setOpaque(false);
 
         jPanel1.setBackground(new java.awt.Color(254, 254, 254));
         jPanel1.setFocusable(false);
         jPanel1.setOpaque(false);
         jPanel1.setLayout(new java.awt.GridBagLayout());
 
         jPanel4.setOpaque(false);
         jPanel4.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel12,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel12.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         jPanel4.add(jLabel12, gridBagConstraints);
 
         cboAbgerechnet.setBorder(null);
         org.openide.awt.Mnemonics.setLocalizedText(
             cboAbgerechnet,
             org.openide.util.NbBundle.getMessage(
                 BillingBillingEditor.class,
                 "BillingBillingEditor.cboAbgerechnet.text")); // NOI18N
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.abgerechnet}"),
                 cboAbgerechnet,
                 org.jdesktop.beansbinding.BeanProperty.create("selected"));
         binding.setSourceNullValue(false);
         binding.setSourceUnreadableValue(false);
         binding.setConverter(new BooleanConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         jPanel4.add(cboAbgerechnet, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel4,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel4.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel4.add(jLabel4, gridBagConstraints);
 
         txtModus.setBorder(null);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.modusbezeichnung}"),
                 txtModus,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel4.add(txtModus, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel5,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel5.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel4.add(jLabel5, gridBagConstraints);
 
         txtVerwendungszweck.setBorder(null);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.verwendungszweck}"),
                 txtVerwendungszweck,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 "");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel4.add(txtVerwendungszweck, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel6,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel6.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel4.add(jLabel6, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel7,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel7.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel4.add(jLabel7, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel8,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel8.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel4.add(jLabel8, gridBagConstraints);
 
         defaultBindableJTextField5.setBorder(null);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.produktbezeichnung}"),
                 defaultBindableJTextField5,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel4.add(defaultBindableJTextField5, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel9,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel9.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel4.add(jLabel9, gridBagConstraints);
 
         txtAngelegt_am.setBorder(null);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel4.add(txtAngelegt_am, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel10,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel10.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel4.add(jLabel10, gridBagConstraints);
 
         defaultBindableJTextField7.setBorder(null);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.netto_summe}"),
                 defaultBindableJTextField7,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel4.add(defaultBindableJTextField7, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel11,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel11.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
         jPanel4.add(jLabel11, gridBagConstraints);
 
         defaultBindableJTextField8.setBorder(null);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.mwst_satz}"),
                 defaultBindableJTextField8,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
         jPanel4.add(defaultBindableJTextField8, gridBagConstraints);
 
         txtaAenderung.setColumns(20);
         txtaAenderung.setRows(4);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel4.add(txtaAenderung, gridBagConstraints);
 
         txtaBerechnung.setColumns(20);
         txtaBerechnung.setRows(5);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel4.add(txtaBerechnung, gridBagConstraints);
 
         txtAngelegt_durch.setBorder(null);
         txtAngelegt_durch.setText(org.openide.util.NbBundle.getMessage(
                 BillingBillingEditor.class,
                 "BillingBillingEditor.txtAngelegt_durch.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel4.add(txtAngelegt_durch, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel13,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel13.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel4.add(jLabel13, gridBagConstraints);
 
         semiRoundedPanel3.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel3.setLayout(new java.awt.FlowLayout());
 
         jLabel3.setForeground(new java.awt.Color(238, 238, 238));
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel3,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel3.text")); // NOI18N
         semiRoundedPanel3.add(jLabel3);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         jPanel4.add(semiRoundedPanel3, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         jPanel1.add(jPanel4, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 1.0;
         jPanel1.add(filler2, gridBagConstraints);
 
         jPanel3.setOpaque(false);
         jPanel3.setLayout(new java.awt.GridBagLayout());
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.projektbezeichnung}"),
                 txtProjektbezeichnung,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         txtProjektbezeichnung.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     txtProjektbezeichnungActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
         jPanel3.add(txtProjektbezeichnung, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblProjektbezeichnung,
             org.openide.util.NbBundle.getMessage(
                 BillingBillingEditor.class,
                 "BillingBillingEditor.lblProjektbezeichnung.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
         jPanel3.add(lblProjektbezeichnung, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblGeschaeftsbuchnummer,
             org.openide.util.NbBundle.getMessage(
                 BillingBillingEditor.class,
                 "BillingBillingEditor.lblGeschaeftsbuchnummer.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         jPanel3.add(lblGeschaeftsbuchnummer, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geschaeftsbuchnummer}"),
                 txtGeschaeftsbuchnummer,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
         jPanel3.add(txtGeschaeftsbuchnummer, gridBagConstraints);
 
         semiRoundedPanel1.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel1.setLayout(new java.awt.FlowLayout());
 
         jLabel1.setForeground(new java.awt.Color(255, 255, 255));
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel1,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel1.text")); // NOI18N
         semiRoundedPanel1.add(jLabel1);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         jPanel3.add(semiRoundedPanel1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         jPanel1.add(jPanel3, gridBagConstraints);
 
         jPanel2.setOpaque(false);
         jPanel2.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblStornogrund,
             org.openide.util.NbBundle.getMessage(
                 BillingBillingEditor.class,
                 "BillingBillingEditor.lblStornogrund.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
         jPanel2.add(lblStornogrund, gridBagConstraints);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.stornogrund}"),
                 coboStornogrund,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel2.add(coboStornogrund, gridBagConstraints);
 
         if (editable) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 btnStornoBuchung,
                 org.openide.util.NbBundle.getMessage(
                     BillingBillingEditor.class,
                     "BillingBillingEditor.btnStornoBuchung.text")); // NOI18N
             btnStornoBuchung.addActionListener(new java.awt.event.ActionListener() {
 
                     @Override
                     public void actionPerformed(final java.awt.event.ActionEvent evt) {
                         btnStornoBuchungActionPerformed(evt);
                     }
                 });
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 3;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
             gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
             jPanel2.add(btnStornoBuchung, gridBagConstraints);
         }
 
         org.openide.awt.Mnemonics.setLocalizedText(
             cboStorno,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.cboStorno.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.storniert}"),
                 cboStorno,
                 org.jdesktop.beansbinding.BeanProperty.create("selected"));
         binding.setSourceNullValue(false);
         binding.setSourceUnreadableValue(false);
         binding.setConverter(new BooleanConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
         jPanel2.add(cboStorno, gridBagConstraints);
 
         semiRoundedPanel2.setBackground(new java.awt.Color(51, 51, 51));
         semiRoundedPanel2.setLayout(new java.awt.FlowLayout());
 
         jLabel2.setForeground(new java.awt.Color(238, 238, 238));
         org.openide.awt.Mnemonics.setLocalizedText(
             jLabel2,
             org.openide.util.NbBundle.getMessage(BillingBillingEditor.class, "BillingBillingEditor.jLabel2.text")); // NOI18N
         semiRoundedPanel2.add(jLabel2);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         jPanel2.add(semiRoundedPanel2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
         jPanel1.add(jPanel2, gridBagConstraints);
 
         scpMain.setViewportView(jPanel1);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         roundedPanel.add(scpMain, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(roundedPanel, gridBagConstraints);
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtProjektbezeichnungActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtProjektbezeichnungActionPerformed
         // TODO add your handling code here:
     } //GEN-LAST:event_txtProjektbezeichnungActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnStornoBuchungActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnStornoBuchungActionPerformed
         if (
             JOptionPane.showConfirmDialog(
                         this.getTopLevelAncestor(),
                         "Buchung stornieren?",
                         "Soll die Buchung wirklich storniert werden?",
                         JOptionPane.YES_NO_OPTION)
                     == JOptionPane.YES_OPTION) {
             try {
                 cidsBean.setProperty("storniert", Boolean.TRUE);
                 cidsBean.setProperty("stornogrund", coboStornogrund.getSelectedItem());
                 cidsBean.setProperty("storniert_durch", SessionManager.getSession().getUser().toString());
             } catch (Exception ex) {
                 LOG.error("Error while setting 'storniert' of billing", ex);
             }
         }
     }                                                                                    //GEN-LAST:event_btnStornoBuchungActionPerformed
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         bindingGroup.unbind();
         if (cidsBean != null) {
             this.cidsBean = cidsBean;
             DefaultCustomObjectEditor.setMetaClassInformationToMetaClassStoreComponentsInBindingGroup(
                 bindingGroup,
                 this.cidsBean);
             bindingGroup.bind();
             fillTextFields();
 
             originalGeschaeftsbuchnummer = (String)cidsBean.getProperty("geschaeftsbuchnummer");
             originalProjektbezeichnung = (String)cidsBean.getProperty("projektbezeichnung");
         }
     }
 
     @Override
     public void dispose() {
         bindingGroup.unbind();
     }
 
     @Override
     public String getTitle() {
         String title = "Buchung";
         final String desc = String.valueOf(cidsBean);
         if (desc != null) {
             title += ": " + desc;
         }
 
         return title;
     }
 
     @Override
     public void setTitle(final String title) {
     }
 
     @Override
     public void editorClosed(final EditorClosedEvent event) {
     }
 
     @Override
     public boolean prepareForSave() {
         try {
             if (!txtGeschaeftsbuchnummer.getText().equals(originalGeschaeftsbuchnummer)) {
                 cidsBean.setProperty("geaendert_am", new java.sql.Timestamp(System.currentTimeMillis()));
                 cidsBean.setProperty("geaendert_von", SessionManager.getSession().getUser().toString());
                 cidsBean.setProperty("aenderung_attribut", "Geschäftsbuchnummer");
                 cidsBean.setProperty("aenderung_alter_wert", originalGeschaeftsbuchnummer);
                 cidsBean.setProperty("aenderung_neuer_wert", txtGeschaeftsbuchnummer.getText());
             } else if (!txtProjektbezeichnung.getText().equals(originalProjektbezeichnung)) {
                 cidsBean.setProperty("geaendert_am", new java.sql.Timestamp(System.currentTimeMillis()));
                 cidsBean.setProperty("geaendert_von", SessionManager.getSession().getUser().toString());
                 cidsBean.setProperty("aenderung_attribut", "Projektbezeichnung");
                 cidsBean.setProperty("aenderung_alter_wert", originalProjektbezeichnung);
                 cidsBean.setProperty("aenderung_neuer_wert", txtProjektbezeichnung.getText());
             }
             return true;
         } catch (Exception ex) {
             LOG.error(ex.getMessage(), ex);
             return false;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
         DevelopmentTools.createEditorInFrameFromRMIConnectionOnLocalhost(
             "WUNDA_BLAU",
             "Administratoren",
             "admin",
             "kif",
             "billing_billing",
             1080,
             1280,
             1024);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void fillTextFields() {
         Object user = cidsBean.getProperty("angelegt_durch");
         if (user == null) {
             user = cidsBean.getProperty("username");
         }
         txtAngelegt_durch.setText(user.toString());
 
         String text = "";
         final Date geaendert_am = (Date)cidsBean.getProperty("geaendert_am");
         if (geaendert_am != null) {
             final String geaendert_von = (String)cidsBean.getProperty("geaendert_von");
             final String aenderung_attribut = (String)cidsBean.getProperty("aenderung_attribut");
             final String aenderung_alter_wert = (String)cidsBean.getProperty("aenderung_alter_wert");
             final String aenderung_neuer_wert = (String)cidsBean.getProperty("aenderung_neuer_wert");
             text = "Änderung von " + geaendert_von + " am " + DATE_FORMAT.format(geaendert_am) + "\n";
             text += "Attribut: " + aenderung_attribut + "\n";
             text += "Alter Wert: " + aenderung_alter_wert + "\n";
             text += "Neuer Wert: " + aenderung_neuer_wert;
         }
         txtaAenderung.setText(text);
 
         txtaBerechnung.setText(cidsBean.getProperty("berechnung").toString());
 
         txtAngelegt_am.setText(DATE_FORMAT.format(cidsBean.getProperty("ts")));
 
         final Boolean storniert = (Boolean)cidsBean.getProperty("storniert");
         text = "Buchung ist storniert";
         if ((storniert != null) && storniert.booleanValue()) {
             final Date storno_am = (Date)cidsBean.getProperty("storno_datum");
             final String storniert_durch = (String)cidsBean.getProperty("storniert_durch");
             text += " (von " + storniert_durch
                         + " am " + DATE_FORMAT.format(storno_am)
                         + ")";
         }
         cboStorno.setText(text);
     }
 }
