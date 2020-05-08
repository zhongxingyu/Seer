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
 /*
  * Arc_stadtbildEditor.java
  *
  * Created on 26.06.2009, 11:02:39
  */
 package de.cismet.cids.custom.objecteditors.wunda_blau;
 
 import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import java.awt.Cursor;
 import java.awt.event.KeyEvent;
 
 import java.util.Collection;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.custom.objectrenderer.converter.BooleanConverter;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.wunda_blau.res.StaticProperties;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.editors.DefaultCustomObjectEditor;
 import de.cismet.cids.editors.FastBindableReferenceCombo;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cismap.cids.geometryeditor.DefaultCismapGeometryComboBoxEditor;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.gui.RoundedPanel;
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class Arc_stadtbildEditor extends DefaultCustomObjectEditor {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final String SUCHWORT_TABNAME = "ARC_SUCHWORT";
 
     //~ Instance fields --------------------------------------------------------
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private String latestPicture = "";
     private final FastBindableReferenceCombo suchwortModelProvider = new FastBindableReferenceCombo(
             "%1$2s",
             new String[] { "SUCHWORT" });
     private final AbstractAttributeRepresentationFormater strasseFormater =
         new AbstractAttributeRepresentationFormater() {
 
             @Override
             public final String getRepresentation() {
                 final Object stadtteil = getAttribute("stadtteil");
                 if (stadtteil == null) {
                     return String.valueOf(getAttribute("name"));
                 } else {
                     return getAttribute("name") + " (" + stadtteil + ")";
                 }
             }
         };
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox bcbAuftraggeber;
     private javax.swing.JComboBox bcbFilmart;
     private javax.swing.JComboBox bcbFotograf;
     private javax.swing.JComboBox bcbStrasse;
     private javax.swing.JButton btnAdd;
     private javax.swing.JButton btnMenAbort;
     private javax.swing.JButton btnMenOk;
     private javax.swing.JButton btnRemove;
     private javax.swing.JComboBox cbGeometrie;
     private javax.swing.JComboBox cbHauptsuchwort;
     private javax.swing.JComboBox cbSuchworte;
     private javax.swing.JCheckBox chkAuswahl;
     private javax.swing.JCheckBox chkLager;
     private javax.swing.JCheckBox chkPruefen;
     private de.cismet.cids.editors.DefaultBindableDateChooser dcAufnahmeDate;
     private de.cismet.cids.editors.DefaultBindableDateChooser dcEingabeDate;
     private javax.swing.JDialog dlgAddSuchwort;
     private javax.swing.JLabel lblAufnahmeDatum;
     private javax.swing.JLabel lblAuftraggeber;
     private javax.swing.JLabel lblBildnummer;
     private javax.swing.JLabel lblBildnummern;
     private javax.swing.JLabel lblContentStadtteil;
     private javax.swing.JLabel lblEingabeDatum;
     private javax.swing.JLabel lblFilmart;
     private javax.swing.JLabel lblFotograf;
     private javax.swing.JLabel lblGeometrie;
     private javax.swing.JLabel lblHauptsuchwort;
     private javax.swing.JLabel lblHausnummer;
     private javax.swing.JLabel lblInfo;
     private de.cismet.cids.custom.objectrenderer.utils.LoaderLabel lblPicture;
     private javax.swing.JLabel lblStadtteil;
     private javax.swing.JLabel lblStrasse;
     private javax.swing.JLabel lblSuchwortEingeben;
     private javax.swing.JLabel lblSuchworte;
     private javax.swing.JList lstSuchworte;
     private javax.swing.JPanel panButtons;
     private javax.swing.JPanel panContent;
     private javax.swing.JPanel panMenButtons;
     private javax.swing.JPanel panNewSuchwort;
     private javax.swing.JPanel panPicture;
     private javax.swing.JScrollPane scpInfo;
     private javax.swing.JScrollPane scpSuchworte;
     private javax.swing.JTextArea txtAInfo;
     private javax.swing.JTextField txtBildnummer;
     private javax.swing.JTextField txtBildnummern;
     private javax.swing.JTextField txtHausnummer;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form Arc_stadtbildEditor.
      */
     public Arc_stadtbildEditor() {
         initComponents();
 //        final MetaClass swClass = ClassCacheMultiple.getMetaClass(domain, "ARC_S");
         suchwortModelProvider.setSorted(true);
         cbSuchworte.setEditable(true);
        AutoCompleteDecorator.decorate(bcbFotograf);
        AutoCompleteDecorator.decorate(bcbAuftraggeber);
        AutoCompleteDecorator.decorate(bcbStrasse);
        AutoCompleteDecorator.decorate(bcbFilmart);
        AutoCompleteDecorator.decorate(cbSuchworte);
         ObjectRendererUtils.decorateComponentWithMouseOverCursorChange(
             lblPicture,
             Cursor.HAND_CURSOR,
             Cursor.DEFAULT_CURSOR);
 
 //        ComboCompleterFilter filter = ComboCompleterFilter.addCompletionMechanism(bcbFotograf);
 //        filter.setCaseSensitive(true);
 //        filter.setStrict(true);
 ////        filter.setNullRespresentation("null");
 //        filter = ComboCompleterFilter.addCompletionMechanism(bcbAuftraggeber);
 //        filter.setCaseSensitive(true);
 ////        filter.setNullRespresentation("null");
 //        filter.setStrict(true);
 //        filter = ComboCompleterFilter.addCompletionMechanism(bcbStrasse);
 //        filter.setCaseSensitive(true);
 //        filter.setStrict(true);
 ////        filter.setNullRespresentation("null");
 //        filter = ComboCompleterFilter.addCompletionMechanism(bcbFilmart);
 //        filter.setCaseSensitive(true);
 //        filter.setStrict(true);
 ////        filter.setNullRespresentation("null");
 //        filter = ComboCompleterFilter.addCompletionMechanism(cbSuchworte);
 //        filter.setCaseSensitive(true);
 //        filter.setStrict(false);
         dlgAddSuchwort.pack();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void dispose() {
         super.dispose();
         dlgAddSuchwort.dispose();
         ((DefaultCismapGeometryComboBoxEditor)cbGeometrie).dispose();
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         dlgAddSuchwort = new javax.swing.JDialog();
         panNewSuchwort = new javax.swing.JPanel();
         lblSuchwortEingeben = new javax.swing.JLabel();
         cbSuchworte = new javax.swing.JComboBox();
         panMenButtons = new javax.swing.JPanel();
         btnMenAbort = new javax.swing.JButton();
         btnMenOk = new javax.swing.JButton();
         panContent = new javax.swing.JPanel();
         lblBildnummer = new javax.swing.JLabel();
         txtBildnummer = new javax.swing.JTextField();
         lblBildnummern = new javax.swing.JLabel();
         txtBildnummern = new javax.swing.JTextField();
         lblHausnummer = new javax.swing.JLabel();
         txtHausnummer = new javax.swing.JTextField();
         lblInfo = new javax.swing.JLabel();
         lblFotograf = new javax.swing.JLabel();
         lblAuftraggeber = new javax.swing.JLabel();
         lblStadtteil = new javax.swing.JLabel();
         bcbFotograf = new FastBindableReferenceCombo("%1$2s", new String[] { "FOTOGRAF" });
         lblStrasse = new javax.swing.JLabel();
         scpInfo = new javax.swing.JScrollPane();
         txtAInfo = new javax.swing.JTextArea();
         bcbStrasse = new FastBindableReferenceCombo(
                 "select s.id,s.name,o.stadtteil from arc_strasse s left outer join arc_stadtteil o on s.stadtteil = o.id",
                 strasseFormater,
                 new String[] { "NAME", "STADTTEIL" });
         bcbAuftraggeber = new FastBindableReferenceCombo("%1$2s", new String[] { "AUFTRAGGEBER" });
         lblSuchworte = new javax.swing.JLabel();
         lblHauptsuchwort = new javax.swing.JLabel();
         lblContentStadtteil = new javax.swing.JLabel();
         lblFilmart = new javax.swing.JLabel();
         lblEingabeDatum = new javax.swing.JLabel();
         lblAufnahmeDatum = new javax.swing.JLabel();
         chkAuswahl = new javax.swing.JCheckBox();
         chkLager = new javax.swing.JCheckBox();
         chkPruefen = new javax.swing.JCheckBox();
         bcbFilmart = new FastBindableReferenceCombo("%1$2s", new String[] { "FILMART" });
         dcEingabeDate = new de.cismet.cids.editors.DefaultBindableDateChooser();
         dcAufnahmeDate = new de.cismet.cids.editors.DefaultBindableDateChooser();
         lblGeometrie = new javax.swing.JLabel();
         cbGeometrie = new DefaultCismapGeometryComboBoxEditor();
         scpSuchworte = new javax.swing.JScrollPane();
         lstSuchworte = new javax.swing.JList();
         panButtons = new javax.swing.JPanel();
         btnAdd = new javax.swing.JButton();
         btnRemove = new javax.swing.JButton();
         cbHauptsuchwort = new javax.swing.JComboBox();
         panPicture = new RoundedPanel();
         lblPicture = new de.cismet.cids.custom.objectrenderer.utils.LoaderLabel();
 
         dlgAddSuchwort.setModal(true);
 
         panNewSuchwort.setMaximumSize(new java.awt.Dimension(180, 120));
         panNewSuchwort.setMinimumSize(new java.awt.Dimension(180, 120));
         panNewSuchwort.setPreferredSize(new java.awt.Dimension(180, 120));
         panNewSuchwort.setLayout(new java.awt.GridBagLayout());
 
         lblSuchwortEingeben.setText("Bitte Suchwort eingeben:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         panNewSuchwort.add(lblSuchwortEingeben, gridBagConstraints);
 
         cbSuchworte.setMaximumSize(new java.awt.Dimension(100, 20));
         cbSuchworte.setMinimumSize(new java.awt.Dimension(100, 20));
         cbSuchworte.setPreferredSize(new java.awt.Dimension(100, 20));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panNewSuchwort.add(cbSuchworte, gridBagConstraints);
 
         panMenButtons.setLayout(new java.awt.GridBagLayout());
 
         btnMenAbort.setText("Abbrechen");
         btnMenAbort.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnMenAbortActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMenButtons.add(btnMenAbort, gridBagConstraints);
 
         btnMenOk.setText("Ok");
         btnMenOk.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnMenOkActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panMenButtons.add(btnMenOk, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panNewSuchwort.add(panMenButtons, gridBagConstraints);
 
         dlgAddSuchwort.getContentPane().add(panNewSuchwort, java.awt.BorderLayout.CENTER);
 
         setLayout(new java.awt.BorderLayout());
 
         panContent.setOpaque(false);
         panContent.setLayout(new java.awt.GridBagLayout());
 
         lblBildnummer.setText("Bildnummer");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblBildnummer, gridBagConstraints);
 
         txtBildnummer.setMaximumSize(new java.awt.Dimension(300, 20));
         txtBildnummer.setMinimumSize(new java.awt.Dimension(300, 20));
         txtBildnummer.setPreferredSize(new java.awt.Dimension(300, 20));
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.bildnummer}"),
                 txtBildnummer,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 "bndBildnummer");
         binding.setSourceNullValue("-");
         binding.setSourceUnreadableValue("-");
         bindingGroup.addBinding(binding);
 
         txtBildnummer.addFocusListener(new java.awt.event.FocusAdapter() {
 
                 @Override
                 public void focusLost(final java.awt.event.FocusEvent evt) {
                     txtBildnummerFocusLost(evt);
                 }
             });
         txtBildnummer.addKeyListener(new java.awt.event.KeyAdapter() {
 
                 @Override
                 public void keyPressed(final java.awt.event.KeyEvent evt) {
                     txtBildnummerKeyPressed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(txtBildnummer, gridBagConstraints);
 
         lblBildnummern.setText("Bildnummern");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblBildnummern, gridBagConstraints);
 
         txtBildnummern.setMaximumSize(new java.awt.Dimension(300, 20));
         txtBildnummern.setMinimumSize(new java.awt.Dimension(300, 20));
         txtBildnummern.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.bildnummern}"),
                 txtBildnummern,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 "bndBildnummern");
         binding.setSourceNullValue("-");
         binding.setSourceUnreadableValue("-");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(txtBildnummern, gridBagConstraints);
 
         lblHausnummer.setText("Hausnummer");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblHausnummer, gridBagConstraints);
 
         txtHausnummer.setMaximumSize(new java.awt.Dimension(300, 20));
         txtHausnummer.setMinimumSize(new java.awt.Dimension(300, 20));
         txtHausnummer.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.hausnummer}"),
                 txtHausnummer,
                 org.jdesktop.beansbinding.BeanProperty.create("text"),
                 "bndHausnummer");
         binding.setSourceNullValue("-");
         binding.setSourceUnreadableValue("-");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(txtHausnummer, gridBagConstraints);
 
         lblInfo.setText("Info");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 16;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblInfo, gridBagConstraints);
 
         lblFotograf.setText("Fotograf");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblFotograf, gridBagConstraints);
 
         lblAuftraggeber.setText("Auftraggeber");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblAuftraggeber, gridBagConstraints);
 
         lblStadtteil.setText("Stadtteil");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblStadtteil, gridBagConstraints);
 
         ((FastBindableReferenceCombo)bcbFotograf).setSorted(true);
         bcbFotograf.setMaximumSize(new java.awt.Dimension(300, 20));
         bcbFotograf.setMinimumSize(new java.awt.Dimension(300, 20));
         bcbFotograf.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.fotograf}"),
                 bcbFotograf,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(bcbFotograf, gridBagConstraints);
 
         lblStrasse.setText("Strasse");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblStrasse, gridBagConstraints);
 
         txtAInfo.setColumns(7);
         txtAInfo.setFont(new java.awt.Font("Tahoma", 0, 11));
         txtAInfo.setRows(5);
         txtAInfo.setMaximumSize(new java.awt.Dimension(150, 20));
         txtAInfo.setMinimumSize(new java.awt.Dimension(150, 20));
         txtAInfo.setPreferredSize(new java.awt.Dimension(150, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.info}"),
                 txtAInfo,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("-");
         binding.setSourceUnreadableValue("-");
         bindingGroup.addBinding(binding);
 
         scpInfo.setViewportView(txtAInfo);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 16;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(scpInfo, gridBagConstraints);
 
         ((FastBindableReferenceCombo)bcbStrasse).setSorted(true);
         bcbStrasse.setMaximumSize(new java.awt.Dimension(300, 20));
         bcbStrasse.setMinimumSize(new java.awt.Dimension(300, 20));
         bcbStrasse.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.ort}"),
                 bcbStrasse,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(bcbStrasse, gridBagConstraints);
 
         ((FastBindableReferenceCombo)bcbAuftraggeber).setSorted(true);
         bcbAuftraggeber.setMaximumSize(new java.awt.Dimension(300, 20));
         bcbAuftraggeber.setMinimumSize(new java.awt.Dimension(300, 20));
         bcbAuftraggeber.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.auftraggeber}"),
                 bcbAuftraggeber,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(bcbAuftraggeber, gridBagConstraints);
 
         lblSuchworte.setText("Suchworte");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblSuchworte, gridBagConstraints);
 
         lblHauptsuchwort.setText("Hauptsuchwort");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblHauptsuchwort, gridBagConstraints);
 
         lblContentStadtteil.setMaximumSize(new java.awt.Dimension(300, 20));
         lblContentStadtteil.setMinimumSize(new java.awt.Dimension(300, 20));
         lblContentStadtteil.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 bcbStrasse,
                 org.jdesktop.beansbinding.ELProperty.create("${selectedItem.stadtteil.stadtteil}"),
                 lblContentStadtteil,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("-");
         binding.setSourceUnreadableValue("-");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblContentStadtteil, gridBagConstraints);
 
         lblFilmart.setText("Filmart");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblFilmart, gridBagConstraints);
 
         lblEingabeDatum.setText("Eingabedatum");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblEingabeDatum, gridBagConstraints);
 
         lblAufnahmeDatum.setText("Aufnahmedatum");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblAufnahmeDatum, gridBagConstraints);
 
         chkAuswahl.setText("Auswahl");
         chkAuswahl.setOpaque(false);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.auswahl}"),
                 chkAuswahl,
                 org.jdesktop.beansbinding.BeanProperty.create("selected"));
         binding.setSourceNullValue(false);
         binding.setSourceUnreadableValue(false);
         binding.setConverter(new BooleanConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 13;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(chkAuswahl, gridBagConstraints);
 
         chkLager.setText("Lager");
         chkLager.setOpaque(false);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.lager}"),
                 chkLager,
                 org.jdesktop.beansbinding.BeanProperty.create("selected"));
         binding.setSourceNullValue(false);
         binding.setSourceUnreadableValue(false);
         binding.setConverter(new BooleanConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 14;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(chkLager, gridBagConstraints);
 
         chkPruefen.setText("Prüfen");
         chkPruefen.setOpaque(false);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.pruefen}"),
                 chkPruefen,
                 org.jdesktop.beansbinding.BeanProperty.create("selected"));
         binding.setSourceNullValue(false);
         binding.setSourceUnreadableValue(false);
         binding.setConverter(new BooleanConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 15;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(chkPruefen, gridBagConstraints);
 
         ((FastBindableReferenceCombo)bcbFilmart).setSorted(true);
         bcbFilmart.setMaximumSize(new java.awt.Dimension(300, 20));
         bcbFilmart.setMinimumSize(new java.awt.Dimension(300, 20));
         bcbFilmart.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.filmart}"),
                 bcbFilmart,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(bcbFilmart, gridBagConstraints);
 
         dcEingabeDate.setMaximumSize(new java.awt.Dimension(300, 20));
         dcEingabeDate.setMinimumSize(new java.awt.Dimension(300, 20));
         dcEingabeDate.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.eingabedatum}"),
                 dcEingabeDate,
                 org.jdesktop.beansbinding.BeanProperty.create("date"));
         binding.setConverter(dcEingabeDate.getConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 12;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(dcEingabeDate, gridBagConstraints);
 
         dcAufnahmeDate.setMaximumSize(new java.awt.Dimension(300, 20));
         dcAufnahmeDate.setMinimumSize(new java.awt.Dimension(300, 20));
         dcAufnahmeDate.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.aufnahmedatum}"),
                 dcAufnahmeDate,
                 org.jdesktop.beansbinding.BeanProperty.create("date"));
         binding.setConverter(dcAufnahmeDate.getConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 11;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(dcAufnahmeDate, gridBagConstraints);
 
         lblGeometrie.setText("Geometrie");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(lblGeometrie, gridBagConstraints);
 
         cbGeometrie.setMaximumSize(new java.awt.Dimension(300, 20));
         cbGeometrie.setMinimumSize(new java.awt.Dimension(300, 20));
         cbGeometrie.setPreferredSize(new java.awt.Dimension(300, 20));
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.georeferenz}"),
                 cbGeometrie,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         binding.setConverter(((DefaultCismapGeometryComboBoxEditor)cbGeometrie).getConverter());
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(cbGeometrie, gridBagConstraints);
 
         scpSuchworte.setMaximumSize(new java.awt.Dimension(275, 100));
         scpSuchworte.setMinimumSize(new java.awt.Dimension(275, 100));
         scpSuchworte.setPreferredSize(new java.awt.Dimension(275, 100));
 
         lstSuchworte.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create(
                 "${cidsBean.suchworte}");
         final org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJListBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         lstSuchworte);
         bindingGroup.addBinding(jListBinding);
 
         scpSuchworte.setViewportView(lstSuchworte);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(scpSuchworte, gridBagConstraints);
 
         panButtons.setOpaque(false);
         panButtons.setLayout(new java.awt.GridBagLayout());
 
         btnAdd.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_add_mini.png"))); // NOI18N
         btnAdd.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
         panButtons.add(btnAdd, gridBagConstraints);
 
         btnRemove.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/objecteditors/wunda_blau/edit_remove_mini.png"))); // NOI18N
         btnRemove.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(3, 5, 5, 5);
         panButtons.add(btnRemove, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(panButtons, gridBagConstraints);
 
         cbHauptsuchwort.setMaximumSize(new java.awt.Dimension(300, 20));
         cbHauptsuchwort.setMinimumSize(new java.awt.Dimension(300, 20));
         cbHauptsuchwort.setPreferredSize(new java.awt.Dimension(300, 20));
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${cidsBean.suchworte}");
         final org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJComboBoxBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         cbHauptsuchwort);
         bindingGroup.addBinding(jComboBoxBinding);
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${cidsBean.objekt}"),
                 cbHauptsuchwort,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         cbHauptsuchwort.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cbHauptsuchwortActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panContent.add(cbHauptsuchwort, gridBagConstraints);
 
         panPicture.setOpaque(false);
         panPicture.setLayout(new java.awt.GridBagLayout());
 
         lblPicture.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         lblPicture.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     lblPictureMouseClicked(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panPicture.add(lblPicture, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridheight = 17;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 5);
         panContent.add(panPicture, gridBagConstraints);
 
         add(panContent, java.awt.BorderLayout.CENTER);
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  cidsBean  DOCUMENT ME!
      */
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         super.setCidsBean(cidsBean);
         ClassCacheMultiple.addInstance(CidsBeanSupport.DOMAIN_NAME);
         suchwortModelProvider.setMetaClassFromTableName(CidsBeanSupport.DOMAIN_NAME, SUCHWORT_TABNAME);
         setNewPicture();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRemoveActionPerformed
         final Object selection = lstSuchworte.getSelectedValue();
         if (selection != null) {
             final int answer = JOptionPane.showConfirmDialog(
                     StaticSwingTools.getParentFrame(this),
                     "Soll das Suchwort wirklich gelöscht werden?",
                     "Suchwort entfernen",
                     JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 try {
                     final CidsBean sw = (CidsBean)selection;
                     final Object suchworteColl = cidsBean.getProperty("suchworte");
                     if (suchworteColl instanceof Collection) {
                         ((Collection)suchworteColl).remove(sw);
                     }
                 } catch (Exception e) {
                     showExceptionToUser(e);
                 }
             }
         }
     }                                                                             //GEN-LAST:event_btnRemoveActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddActionPerformed
         CismetThreadPool.execute(new ComboBoxWorker());
     }                                                                          //GEN-LAST:event_btnAddActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnMenOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnMenOkActionPerformed
         final String suchwort = lookForNewSuchwort();
         if (suchwort != null) {
             if (suchwort.trim().length() > 0) {
                 try {
                     final CidsBean newSuchwortBean = createNewSuchwortBeanFromString(suchwort);
                     addSuchwortBeanToSuchworte(newSuchwortBean);
                 } catch (Exception ex) {
                     showExceptionToUser(ex);
                 }
             }
         } else {
             final Object selItem = cbSuchworte.getSelectedItem();
             if (selItem instanceof MetaObject) {
                 try {
                     addSuchwortBeanToSuchworte(((MetaObject)selItem).getBean());
                 } catch (Exception ex) {
                     log.error(ex, ex);
                 }
             }
         }
         dlgAddSuchwort.setVisible(false);
     }                                                                            //GEN-LAST:event_btnMenOkActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnMenAbortActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnMenAbortActionPerformed
         dlgAddSuchwort.setVisible(false);
     }                                                                               //GEN-LAST:event_btnMenAbortActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void lblPictureMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblPictureMouseClicked
         if (!evt.isPopupTrigger()) {
             if (lblPicture.getPictureURL() != null) {
                 ObjectRendererUtils.openURL(lblPicture.getPictureURL());
             }
         }
     }                                                                          //GEN-LAST:event_lblPictureMouseClicked
 
     /**
      * DOCUMENT ME!
      */
     private void setNewPicture() {
         if ((latestPicture == null) || !latestPicture.equals(txtBildnummer.getText())) {
             final String pictureURL = StaticProperties.ARCHIVAR_URL_PREFIX + txtBildnummer.getText()
                         + StaticProperties.ARCHIVAR_URL_SUFFIX;
             lblPicture.setPictureURL(pictureURL);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtBildnummerFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtBildnummerFocusLost
         setNewPicture();
     }                                                                          //GEN-LAST:event_txtBildnummerFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtBildnummerKeyPressed(final java.awt.event.KeyEvent evt) { //GEN-FIRST:event_txtBildnummerKeyPressed
         final int key = evt.getKeyCode();
         if (key == KeyEvent.VK_ENTER) {
             setNewPicture();
         }
     }                                                                         //GEN-LAST:event_txtBildnummerKeyPressed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cbHauptsuchwortActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbHauptsuchwortActionPerformed
         // TODO add your handling code here:
     } //GEN-LAST:event_cbHauptsuchwortActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  ex  DOCUMENT ME!
      */
     private void showExceptionToUser(final Exception ex) {
         final ErrorInfo ei = new ErrorInfo(
                 "Fehler",
                 "Beim Vorgang ist ein Fehler aufgetreten",
                 null,
                 null,
                 ex,
                 Level.SEVERE,
                 null);
         JXErrorPane.showDialog(this, ei);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   suchwort  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private CidsBean createNewSuchwortBeanFromString(final String suchwort) throws Exception {
         if ((suchwort != null) && (suchwort.trim().length() > 0)) {
             MetaClass suchwortMC = suchwortModelProvider.getMetaClass();
             if (suchwortMC == null) {
                 suchwortMC = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, SUCHWORT_TABNAME);
             }
             if (suchwortMC != null) {
                 final CidsBean newSuchwortBean = suchwortMC.getEmptyInstance().getBean();
                 newSuchwortBean.setProperty("suchwort", suchwort);
                 return newSuchwortBean;
             } else {
                 log.error("Could not find MetaClass for table " + SUCHWORT_TABNAME);
             }
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  newSuchwortBean  DOCUMENT ME!
      */
     private void addSuchwortBeanToSuchworte(final CidsBean newSuchwortBean) {
         if (newSuchwortBean != null) {
             final Object o = cidsBean.getProperty("suchworte");
             if (o instanceof Collection) {
                 try {
                     final Collection<CidsBean> col = (Collection)o;
                     for (final CidsBean bean : col) {
                         if (newSuchwortBean.getProperty("suchwort").equals(bean.getProperty("suchwort"))) {
                             log.info("Suchwort " + newSuchwortBean.getProperty("suchwort") + " already present!");
                             return;
                         }
                     }
                     col.add(newSuchwortBean);
                 } catch (Exception ex) {
                     log.error(ex, ex);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String lookForNewSuchwort() {
         final Object o = cbSuchworte.getSelectedItem();
         if (o instanceof String) {
             return o.toString();
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private DefaultComboBoxModel initDialogeSuchwortCombobox() throws Exception {
         final MetaObject[] mos = suchwortModelProvider.receiveLightweightMetaObjects();
         final DefaultComboBoxModel model = new DefaultComboBoxModel(mos);
         return model;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class ComboBoxWorker extends SwingWorker<DefaultComboBoxModel, Void> {
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ComboBoxWorker object.
          */
         public ComboBoxWorker() {
             btnAdd.setEnabled(false);
             btnRemove.setEnabled(false);
 //            dlgWait.setLocationRelativeTo(Arc_stadtbildEditor.this);
 //            dlgWait.setVisible(true);
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         @Override
         protected DefaultComboBoxModel doInBackground() throws Exception {
             return initDialogeSuchwortCombobox();
         }
 
         /**
          * DOCUMENT ME!
          */
         @Override
         protected void done() {
             try {
                 cbSuchworte.setModel(get());
                 cbSuchworte.setSelectedIndex(0);
                 StaticSwingTools.showDialog(StaticSwingTools.getParentFrame(Arc_stadtbildEditor.this),
                     dlgAddSuchwort,
                     true);
             } catch (InterruptedException ex) {
                 log.warn(ex, ex);
             } catch (ExecutionException ex) {
                 showExceptionToUser(ex);
             } finally {
                 btnAdd.setEnabled(true);
                 btnRemove.setEnabled(true);
             }
         }
     }
 }
