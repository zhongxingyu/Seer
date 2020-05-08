 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * BaulastWindowSearch.java
  *
  * Created on 09.12.2010, 14:33:10
  */
 package de.cismet.cids.custom.wunda_blau.search;
 
 import Sirius.navigator.actiontag.ActionTagProtected;
 import Sirius.navigator.search.dynamic.SearchControlListener;
 import Sirius.navigator.search.dynamic.SearchControlPanel;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.search.CidsServerSearch;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import java.awt.CardLayout;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.custom.objectrenderer.utils.ObjectRendererUtils;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsAlkisSearchStatement;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cids.tools.search.clientstuff.CidsWindowSearch;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 /**
  * DOCUMENT ME!
  *
  * @author   stefan
  * @version  $Revision$, $Date$
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsWindowSearch.class)
 public class AlkisWindowSearch extends javax.swing.JPanel implements CidsWindowSearch,
     ActionTagProtected,
     SearchControlListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlkisWindowSearch.class);
     private static final String ACTION_TAG = "custom.alkis.windowsearch";
 
     //~ Instance fields --------------------------------------------------------
 
     private final MetaClass mc;
     private final ImageIcon icon;
     private SearchControlPanel pnlSearchCancel;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup bgrNach;
     private javax.swing.ButtonGroup bgrOwner;
     private javax.swing.ButtonGroup bgrUeber;
     private javax.swing.JCheckBox chkGeburtsnameExakt;
     private javax.swing.JCheckBox chkGeomFilter;
     private javax.swing.JCheckBox chkNameExakt;
     private javax.swing.JCheckBox chkVornameExakt;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JRadioButton optEigIstFirma;
     private javax.swing.JRadioButton optEigIstMaennlich;
     private javax.swing.JRadioButton optEigIstUnbekannt;
     private javax.swing.JRadioButton optEigIstWeiblich;
     private javax.swing.JRadioButton optSucheNachFlurstuecke;
     private javax.swing.JRadioButton optSucheNachGrundbuchblaetter;
     private javax.swing.JRadioButton optSucheUeberEigentuemer;
     private javax.swing.JRadioButton optSucheUeberFlurstueck;
     private javax.swing.JRadioButton optSucheUeberGrundbuchblatt;
     private javax.swing.JPanel panCommand;
     private javax.swing.JPanel panEingabe;
     private javax.swing.JPanel panEingabeEigentuemer;
     private javax.swing.JPanel panEingabeFlurstueck;
     private javax.swing.JPanel panEingabeGrundbuchblatt;
     private javax.swing.JPanel panSearch;
     private javax.swing.JPanel panSucheNach;
     private javax.swing.JPanel panSucheUeber;
     private javax.swing.JTextField txtFlurstuecksnummer;
     private javax.swing.JTextField txtGeburtsdatum;
     private javax.swing.JTextField txtGeburtsname;
     private javax.swing.JTextField txtGrundbuchblattnummer;
     private javax.swing.JTextField txtName;
     private javax.swing.JTextField txtVorname;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form BaulastWindowSearch.
      */
     public AlkisWindowSearch() {
         mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, "ALKIS_LANDPARCEL");
         icon = new ImageIcon(mc.getIconData());
         initComponents();
         ((CardLayout)panEingabe.getLayout()).show(panEingabe, "eigentuemer");
         pnlSearchCancel = new SearchControlPanel(this);
         panCommand.add(pnlSearchCancel);
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
 
         bgrUeber = new javax.swing.ButtonGroup();
         bgrNach = new javax.swing.ButtonGroup();
         bgrOwner = new javax.swing.ButtonGroup();
         panSearch = new javax.swing.JPanel();
         panCommand = new javax.swing.JPanel();
         panSucheNach = new javax.swing.JPanel();
         optSucheNachFlurstuecke = new javax.swing.JRadioButton();
         optSucheNachGrundbuchblaetter = new javax.swing.JRadioButton();
         jPanel2 = new javax.swing.JPanel();
         jPanel6 = new javax.swing.JPanel();
         panSucheUeber = new javax.swing.JPanel();
         optSucheUeberEigentuemer = new javax.swing.JRadioButton();
         optSucheUeberFlurstueck = new javax.swing.JRadioButton();
         optSucheUeberGrundbuchblatt = new javax.swing.JRadioButton();
         jPanel3 = new javax.swing.JPanel();
         panEingabe = new javax.swing.JPanel();
         panEingabeGrundbuchblatt = new javax.swing.JPanel();
         txtGrundbuchblattnummer = new javax.swing.JTextField();
         jLabel12 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         panEingabeFlurstueck = new javax.swing.JPanel();
         txtFlurstuecksnummer = new javax.swing.JTextField();
         jLabel9 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         panEingabeEigentuemer = new javax.swing.JPanel();
         txtVorname = new javax.swing.JTextField();
         jLabel7 = new javax.swing.JLabel();
         txtName = new javax.swing.JTextField();
         jLabel8 = new javax.swing.JLabel();
         jPanel4 = new javax.swing.JPanel();
         jPanel5 = new javax.swing.JPanel();
         optEigIstMaennlich = new javax.swing.JRadioButton();
         optEigIstWeiblich = new javax.swing.JRadioButton();
         optEigIstFirma = new javax.swing.JRadioButton();
         optEigIstUnbekannt = new javax.swing.JRadioButton();
         chkNameExakt = new javax.swing.JCheckBox();
         chkGeburtsnameExakt = new javax.swing.JCheckBox();
         chkVornameExakt = new javax.swing.JCheckBox();
         jLabel1 = new javax.swing.JLabel();
         txtGeburtsname = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         txtGeburtsdatum = new javax.swing.JTextField();
         jLabel2 = new javax.swing.JLabel();
         chkGeomFilter = new javax.swing.JCheckBox();
 
         setMaximumSize(new java.awt.Dimension(325, 460));
         setMinimumSize(new java.awt.Dimension(325, 460));
         setPreferredSize(new java.awt.Dimension(325, 460));
         setLayout(new java.awt.BorderLayout());
 
         panSearch.setMaximumSize(new java.awt.Dimension(400, 150));
         panSearch.setMinimumSize(new java.awt.Dimension(400, 150));
         panSearch.setPreferredSize(new java.awt.Dimension(400, 150));
         panSearch.setLayout(new java.awt.GridBagLayout());
 
         panCommand.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panSearch.add(panCommand, gridBagConstraints);
 
         panSucheNach.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createTitledBorder("Suche nach"),
                 javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
         panSucheNach.setLayout(new java.awt.GridBagLayout());
 
         bgrNach.add(optSucheNachFlurstuecke);
         optSucheNachFlurstuecke.setSelected(true);
         optSucheNachFlurstuecke.setText("Flurstücken");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         panSucheNach.add(optSucheNachFlurstuecke, gridBagConstraints);
 
         bgrNach.add(optSucheNachGrundbuchblaetter);
         optSucheNachGrundbuchblaetter.setText("Grundbuchblättern");
         panSucheNach.add(optSucheNachGrundbuchblaetter, new java.awt.GridBagConstraints());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         panSucheNach.add(jPanel2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(15, 15, 15, 15);
         panSearch.add(panSucheNach, gridBagConstraints);
 
         jPanel6.setOpaque(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.weighty = 1.0;
         panSearch.add(jPanel6, gridBagConstraints);
 
         panSucheUeber.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createTitledBorder("Suche über"),
                 javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
         panSucheUeber.setLayout(new java.awt.GridBagLayout());
 
         bgrUeber.add(optSucheUeberEigentuemer);
         optSucheUeberEigentuemer.setSelected(true);
         optSucheUeberEigentuemer.setText("Eigentümer");
         optSucheUeberEigentuemer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     optSucheUeberEigentuemerActionPerformed(evt);
                 }
             });
         panSucheUeber.add(optSucheUeberEigentuemer, new java.awt.GridBagConstraints());
 
         bgrUeber.add(optSucheUeberFlurstueck);
         optSucheUeberFlurstueck.setText("Flurstück");
         optSucheUeberFlurstueck.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     optSucheUeberFlurstueckActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         panSucheUeber.add(optSucheUeberFlurstueck, gridBagConstraints);
 
         bgrUeber.add(optSucheUeberGrundbuchblatt);
         optSucheUeberGrundbuchblatt.setText("Grundbuchblatt");
         optSucheUeberGrundbuchblatt.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     optSucheUeberGrundbuchblattActionPerformed(evt);
                 }
             });
         panSucheUeber.add(optSucheUeberGrundbuchblatt, new java.awt.GridBagConstraints());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         panSucheUeber.add(jPanel3, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 15);
         panSearch.add(panSucheUeber, gridBagConstraints);
 
         panEingabe.setLayout(new java.awt.CardLayout());
 
         panEingabeGrundbuchblatt.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createTitledBorder("Grundbuchblatt Suchmaske"),
                 javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
         panEingabeGrundbuchblatt.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeGrundbuchblatt.add(txtGrundbuchblattnummer, gridBagConstraints);
 
         jLabel12.setText("Grundbuchblattnummer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeGrundbuchblatt.add(jLabel12, gridBagConstraints);
 
         jLabel4.setText(
             "<html> <p>Beispiel: 053001-003117</p><br><p>Platzhaltersymbole:</p><p>&nbsp;<b>%</b>&nbsp;&nbsp;&nbsp;&nbsp;eine beliebige Anzahl von Zeichen</p> <p>&nbsp;<b>_</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ein einzelnes Zeichen</p> </html>");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
         panEingabeGrundbuchblatt.add(jLabel4, gridBagConstraints);
 
         panEingabe.add(panEingabeGrundbuchblatt, "grundbuchblatt");
 
         panEingabeFlurstueck.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createTitledBorder("Flurstück Suchmaske"),
                 javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
         panEingabeFlurstueck.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeFlurstueck.add(txtFlurstuecksnummer, gridBagConstraints);
 
         jLabel9.setText("Flurstücksnummer:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeFlurstueck.add(jLabel9, gridBagConstraints);
 
         jLabel3.setText(
             "<html> <p>Beispiel: 053001-117-00058</p><br><p>Platzhaltersymbole:</p><p>&nbsp;<b>%</b>&nbsp;&nbsp;&nbsp;&nbsp;eine beliebige Anzahl von Zeichen</p> <p>&nbsp;<b>_</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ein einzelnes Zeichen</p> </html>");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
         panEingabeFlurstueck.add(jLabel3, gridBagConstraints);
 
         panEingabe.add(panEingabeFlurstueck, "flurstueck");
 
         panEingabeEigentuemer.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createTitledBorder("Eigentümer Suchmaske"),
                 javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
         panEingabeEigentuemer.setLayout(new java.awt.GridBagLayout());
 
         txtVorname.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     txtVornameActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeEigentuemer.add(txtVorname, gridBagConstraints);
 
         jLabel7.setText("Vorname:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeEigentuemer.add(jLabel7, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeEigentuemer.add(txtName, gridBagConstraints);
 
         jLabel8.setText("Name:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeEigentuemer.add(jLabel8, gridBagConstraints);
 
         jPanel4.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                 javax.swing.BorderFactory.createTitledBorder("Eigentümer ist"),
                 javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));
         jPanel4.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         jPanel4.add(jPanel5, gridBagConstraints);
 
         bgrOwner.add(optEigIstMaennlich);
         optEigIstMaennlich.setText("männlich");
         optEigIstMaennlich.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     optEigIstMaennlichActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         jPanel4.add(optEigIstMaennlich, gridBagConstraints);
 
         bgrOwner.add(optEigIstWeiblich);
         optEigIstWeiblich.setText("weiblich");
         optEigIstWeiblich.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     optEigIstWeiblichActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         jPanel4.add(optEigIstWeiblich, gridBagConstraints);
 
         bgrOwner.add(optEigIstFirma);
         optEigIstFirma.setText("Firma");
         optEigIstFirma.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     optEigIstFirmaActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         jPanel4.add(optEigIstFirma, gridBagConstraints);
 
         bgrOwner.add(optEigIstUnbekannt);
         optEigIstUnbekannt.setSelected(true);
         optEigIstUnbekannt.setText("unbekannt");
         optEigIstUnbekannt.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     optEigIstUnbekanntActionPerformed(evt);
                 }
             });
         jPanel4.add(optEigIstUnbekannt, new java.awt.GridBagConstraints());
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         panEingabeEigentuemer.add(jPanel4, gridBagConstraints);
 
         chkNameExakt.setToolTipText("z.Zt. vom Alkis-Dienst nicht unterstützt");
         chkNameExakt.setEnabled(false);
         chkNameExakt.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     chkNameExaktActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 2;
         panEingabeEigentuemer.add(chkNameExakt, gridBagConstraints);
 
         chkGeburtsnameExakt.setToolTipText("z.Zt. vom Alkis-Dienst nicht unterstützt");
         chkGeburtsnameExakt.setEnabled(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 5;
         panEingabeEigentuemer.add(chkGeburtsnameExakt, gridBagConstraints);
 
         chkVornameExakt.setToolTipText("z.Zt. vom Alkis-Dienst nicht unterstützt");
         chkVornameExakt.setEnabled(false);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 3;
         panEingabeEigentuemer.add(chkVornameExakt, gridBagConstraints);
 
         jLabel1.setText("exakt?");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         panEingabeEigentuemer.add(jLabel1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeEigentuemer.add(txtGeburtsname, gridBagConstraints);
 
         jLabel13.setText("Geburtsname:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeEigentuemer.add(jLabel13, gridBagConstraints);
 
         jLabel14.setText("Geburtsdatum:");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeEigentuemer.add(jLabel14, gridBagConstraints);
 
         txtGeburtsdatum.setToolTipText("Bsp.: 18.01.1974");
         txtGeburtsdatum.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     txtGeburtsdatumActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panEingabeEigentuemer.add(txtGeburtsdatum, gridBagConstraints);
 
         jLabel2.setText(
             "<html><br><p>Platzhaltersymbole:</p><p>&nbsp;<b>%</b>&nbsp;&nbsp;&nbsp;&nbsp;eine beliebige Anzahl von Zeichen</p> <p>&nbsp;<b>_</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ein einzelnes Zeichen</p> </html>");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
         panEingabeEigentuemer.add(jLabel2, gridBagConstraints);
 
         panEingabe.add(panEingabeEigentuemer, "eigentuemer");
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(15, 15, 15, 15);
         panSearch.add(panEingabe, gridBagConstraints);
 
         chkGeomFilter.setText("nur im aktuellen Kartenausschnitt suchen");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 17, 0, 0);
         panSearch.add(chkGeomFilter, gridBagConstraints);
 
         add(panSearch, java.awt.BorderLayout.CENTER);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void optSucheUeberEigentuemerActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_optSucheUeberEigentuemerActionPerformed
         ((CardLayout)panEingabe.getLayout()).show(panEingabe, "eigentuemer");
     }                                                                                            //GEN-LAST:event_optSucheUeberEigentuemerActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void chkNameExaktActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkNameExaktActionPerformed
         // TODO add your handling code here:
     } //GEN-LAST:event_chkNameExaktActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtVornameActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtVornameActionPerformed
         // TODO add your handling code here:
     } //GEN-LAST:event_txtVornameActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtGeburtsdatumActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtGeburtsdatumActionPerformed
         // TODO add your handling code here:
     } //GEN-LAST:event_txtGeburtsdatumActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void optEigIstMaennlichActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_optEigIstMaennlichActionPerformed
         txtVorname.setEnabled(true);
         txtGeburtsname.setEnabled(true);
         txtGeburtsdatum.setEnabled(true);
     }                                                                                      //GEN-LAST:event_optEigIstMaennlichActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void optEigIstUnbekanntActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_optEigIstUnbekanntActionPerformed
         txtVorname.setEnabled(true);
         txtGeburtsname.setEnabled(true);
         txtGeburtsdatum.setEnabled(true);
     }                                                                                      //GEN-LAST:event_optEigIstUnbekanntActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void optEigIstFirmaActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_optEigIstFirmaActionPerformed
         txtVorname.setEnabled(false);
         txtGeburtsname.setEnabled(false);
         txtGeburtsdatum.setEnabled(false);
     }                                                                                  //GEN-LAST:event_optEigIstFirmaActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void optEigIstWeiblichActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_optEigIstWeiblichActionPerformed
         txtVorname.setEnabled(true);
         txtGeburtsname.setEnabled(true);
         txtGeburtsdatum.setEnabled(true);
     }                                                                                     //GEN-LAST:event_optEigIstWeiblichActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void optSucheUeberFlurstueckActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_optSucheUeberFlurstueckActionPerformed
         ((CardLayout)panEingabe.getLayout()).show(panEingabe, "flurstueck");
     }                                                                                           //GEN-LAST:event_optSucheUeberFlurstueckActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void optSucheUeberGrundbuchblattActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_optSucheUeberGrundbuchblattActionPerformed
         ((CardLayout)panEingabe.getLayout()).show(panEingabe, "grundbuchblatt");
     }                                                                                               //GEN-LAST:event_optSucheUeberGrundbuchblattActionPerformed
 
     @Override
     public ImageIcon getIcon() {
         return icon;
     }
 
     @Override
     public String getName() {
        return "Alkis Suche";
     }
 
     @Override
     public JComponent getSearchWindowComponent() {
         return this;
     }
 
     @Override
     public CidsServerSearch getServerSearch() {
         Geometry searchgeom = null;
         if (chkGeomFilter.isSelected()) {
             final Geometry g = ((XBoundingBox)CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBox())
                         .getGeometry();
             final Geometry transformed = CrsTransformer.transformToDefaultCrs(g);
             // Damits auch mit -1 funzt:
             transformed.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
             searchgeom = transformed;
         }
 
         CidsAlkisSearchStatement.Resulttyp resulttype = null;
         if (optSucheNachFlurstuecke.isSelected()) {
             resulttype = CidsAlkisSearchStatement.Resulttyp.FLURSTUECK;
         } else {
             resulttype = CidsAlkisSearchStatement.Resulttyp.BUCHUNGSBLATT;
         }
         if (optSucheUeberEigentuemer.isSelected()) {
             CidsAlkisSearchStatement.Personentyp ptyp = null;
             if (optEigIstFirma.isSelected()) {
                 ptyp = CidsAlkisSearchStatement.Personentyp.FIRMA;
             } else if (optEigIstMaennlich.isSelected()) {
                 ptyp = CidsAlkisSearchStatement.Personentyp.MANN;
             } else if (optEigIstWeiblich.isSelected()) {
                 ptyp = CidsAlkisSearchStatement.Personentyp.FRAU;
             }
 
             String name = txtName.getText().trim();
             String vorname = txtVorname.getText().trim();
             String geburtsname = txtGeburtsname.getText().trim();
             final String geburtsdatum = txtGeburtsdatum.getText().trim();
             if (!chkNameExakt.isSelected() && (name.length() > 0)) {
                 name = CidsAlkisSearchStatement.WILDCARD + name + CidsAlkisSearchStatement.WILDCARD;
             }
             if (!chkVornameExakt.isSelected() && (vorname.length() > 0)) {
                 vorname = CidsAlkisSearchStatement.WILDCARD + vorname + CidsAlkisSearchStatement.WILDCARD;
             }
             if (!chkGeburtsnameExakt.isSelected() && (geburtsname.length() > 0)) {
                 geburtsname = CidsAlkisSearchStatement.WILDCARD + geburtsname + CidsAlkisSearchStatement.WILDCARD;
             }
             return new CidsAlkisSearchStatement(resulttype, name, vorname, geburtsname, geburtsdatum, ptyp, searchgeom);
         } else if (optSucheUeberFlurstueck.isSelected()) {
             return new CidsAlkisSearchStatement(
                     resulttype,
                     CidsAlkisSearchStatement.SucheUeber.FLURSTUECKSNUMMER,
                     txtFlurstuecksnummer.getText(),
                     searchgeom);
         } else {
             return new CidsAlkisSearchStatement(
                     resulttype,
                     CidsAlkisSearchStatement.SucheUeber.BUCHUNGSBLATTNUMMER,
                     txtGrundbuchblattnummer.getText(),
                     searchgeom);
         }
     }
 
     @Override
     public boolean checkActionTag() {
         return ObjectRendererUtils.checkActionTag(ACTION_TAG);
     }
 //    @Override
 //    public boolean checkActionTag() {
 //        try {
 //            return SessionManager.getConnection()
 //                        .getConfigAttr(SessionManager.getSession().getUser(), "navigator.alkis.search") != null;
 //        } catch (ConnectionException ex) {
 //            log.error("Can not validate ActionTag for Alkis Suche!", ex);
 //            return false;
 //        }
 //    }
 
     @Override
     public CidsServerSearch assembleSearch() {
         return getServerSearch();
     }
 
     @Override
     public void searchStarted() {
     }
 
     @Override
     public void searchDone(final int results) {
     }
 
     @Override
     public void searchCanceled() {
     }
 
     @Override
     public boolean suppressEmptyResultMessage() {
         return false;
     }
 }
