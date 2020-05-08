 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objectrenderer.utils.billing;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import org.openide.util.Exceptions;
 
 import java.io.IOException;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 
 import de.cismet.cids.client.tools.DevelopmentTools;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class BillingPopup extends javax.swing.JDialog {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String MODE_CONFIG_ATTR = "billing.mode@WUNDA_BLAU";
     public static final String ALLOWED_USAGE_CONFIG_ATTR = "billing.allowed.usage@WUNDA_BLAU";
     private static ObjectMapper mapper = new ObjectMapper();
     private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
             BillingPopup.class);
     private static BillingInfo billingInfo;
     private static HashMap<String, Modus> modi = new HashMap<String, Modus>();
     private static HashMap<String, Product> products = new HashMap<String, Product>();
     private static HashMap<String, Usage> usages = new HashMap<String, Usage>();
     private static HashMap<String, ProductGroup> productGroups = new HashMap<String, ProductGroup>();
 
     static {
         try {
             billingInfo = mapper.readValue(BillingPopup.class.getResourceAsStream(
                         "/de/cismet/cids/custom/billing/billing.json"),
                     BillingInfo.class);
 
             final ArrayList<Modus> lm = billingInfo.getModi();
             for (final Modus m : lm) {
                 modi.put(m.getKey(), m);
             }
             final ArrayList<Product> lp = billingInfo.getProducts();
             for (final Product p : lp) {
                 products.put(p.getId(), p);
             }
             final ArrayList<Usage> lu = billingInfo.getUsages();
             for (final Usage u : lu) {
                 usages.put(u.getKey(), u);
             }
             final ArrayList<ProductGroup> lpg = billingInfo.getProductGroups();
             for (final ProductGroup pg : lpg) {
                 productGroups.put(pg.getKey(), pg);
             }
         } catch (IOException ioException) {
             LOG.error("Error when trying to read the billingInfo.json", ioException);
         }
     }
 
     private static BillingPopup instance = null;
 
     //~ Instance fields --------------------------------------------------------
 
     Product currentProduct = null;
     Modus currentMode = null;
     Usage currentUsage = null;
     String request = null;
     Geometry geom = null;
     CidsBean logEntry = null;
     String berechnungPrefix = "";
 
     private ImageIcon money = new javax.swing.ImageIcon(
             getClass().getResource("/de/cismet/cids/custom/billing/money--exclamation.png"));
     private double rawPrice = 0;
     private double nettoPrice = 0;
     private double bruttoPrice = 0;
     private boolean shouldGoOn = false;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox cboUsage;
     private javax.swing.JButton cmdCancel;
     private javax.swing.JButton cmdOk;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JTextPane jTextPane1;
     private javax.swing.JLabel lblGebTitle;
     private javax.swing.JLabel lblGebuehr;
     private javax.swing.JLabel lblMoneyWarn;
     private javax.swing.JLabel lblMwst;
     private javax.swing.JLabel lblMwstTitle;
     private javax.swing.JPanel panControls;
     private javax.swing.JPanel panLower;
     private javax.swing.JPanel panUpper;
     private javax.swing.JTextPane txtBerechnung;
     private javax.swing.JTextField txtGBuchNr;
     private javax.swing.JTextArea txtProjektbez;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form BillingPopup.
      *
      * @param  parent  DOCUMENT ME!
      * @param  modal   DOCUMENT ME!
      */
     public BillingPopup(final java.awt.Frame parent, final boolean modal) {
         super(parent, modal);
         initComponents();
         final SimpleAttributeSet attribs = new SimpleAttributeSet();
         StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_RIGHT);
         StyleConstants.setFontSize(attribs, 10);
         txtBerechnung.setParagraphAttributes(attribs, true);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   product  DOCUMENT ME!
      * @param   request  DOCUMENT ME!
      * @param   geom     DOCUMENT ME!
      * @param   amounts  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static boolean doBilling(
             final String product,
             final String request,
             final Geometry geom,
             final ProductGroupAmount... amounts) throws Exception {
         if (instance == null) {
             JFrame f = null;
             try {
                 f = ComponentRegistry.getRegistry().getMainWindow();
             } catch (Exception e) {
                 // Developmenttime
             }
             instance = new BillingPopup(f, true);
         }
         final User user = SessionManager.getSession().getUser();
         final String modus = SessionManager.getConnection().getConfigAttr(user, MODE_CONFIG_ATTR);
         if (modus != null) {
             instance.initialize(product, request, geom, amounts);
             return instance.shouldGoOn;
         } else {
             return true;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   product  DOCUMENT ME!
      * @param   request  DOCUMENT ME!
      * @param   gBuchNr  DOCUMENT ME!
      * @param   geom     DOCUMENT ME!
      * @param   amounts  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static boolean doBilling(
             final String product,
             final String request,
             final String gBuchNr,
             final Geometry geom,
             final ProductGroupAmount... amounts) throws Exception {
         if (instance == null) {
             JFrame f = null;
             try {
                 f = ComponentRegistry.getRegistry().getMainWindow();
             } catch (Exception e) {
                 // Developmenttime
             }
             instance = new BillingPopup(f, true);
         }
         instance.txtGBuchNr.setText(gBuchNr);
         final User user = SessionManager.getSession().getUser();
         final String modus = SessionManager.getConnection().getConfigAttr(user, MODE_CONFIG_ATTR);
         if (modus != null) {
             instance.initialize(product, request, geom, amounts);
             return instance.shouldGoOn;
         } else {
             return true;
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         jScrollPane3 = new javax.swing.JScrollPane();
         jTextPane1 = new javax.swing.JTextPane();
         lblMoneyWarn = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         panUpper = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         cboUsage = new javax.swing.JComboBox();
         jScrollPane2 = new javax.swing.JScrollPane();
         txtProjektbez = new javax.swing.JTextArea();
         jLabel2 = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         txtGBuchNr = new javax.swing.JTextField();
         panLower = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         lblGebuehr = new javax.swing.JLabel();
         jPanel2 = new javax.swing.JPanel();
         lblGebTitle = new javax.swing.JLabel();
         lblMwstTitle = new javax.swing.JLabel();
         lblMwst = new javax.swing.JLabel();
         jScrollPane4 = new javax.swing.JScrollPane();
         txtBerechnung = new javax.swing.JTextPane();
         panControls = new javax.swing.JPanel();
         cmdCancel = new javax.swing.JButton();
         cmdOk = new javax.swing.JButton();
         jSeparator2 = new javax.swing.JSeparator();
 
         jScrollPane3.setViewportView(jTextPane1);
 
         lblMoneyWarn.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/billing/money--exclamation.png"))); // NOI18N
         lblMoneyWarn.setText(org.openide.util.NbBundle.getMessage(
                 BillingPopup.class,
                 "BillingPopup.lblMoneyWarn.text"));                                                // NOI18N
 
         setTitle(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.title")); // NOI18N
         setAlwaysOnTop(true);
         getContentPane().setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         getContentPane().add(jSeparator1, gridBagConstraints);
 
         panUpper.setLayout(new java.awt.GridBagLayout());
 
         jLabel3.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.jLabel3.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         panUpper.add(jLabel3, gridBagConstraints);
 
         cboUsage.setModel(new javax.swing.DefaultComboBoxModel(
                 new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         cboUsage.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cboUsageActionPerformed(evt);
                 }
             });
         cboUsage.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
 
                 @Override
                 public void propertyChange(final java.beans.PropertyChangeEvent evt) {
                     cboUsagePropertyChange(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         panUpper.add(cboUsage, gridBagConstraints);
 
         txtProjektbez.setColumns(20);
         txtProjektbez.setRows(5);
         jScrollPane2.setViewportView(txtProjektbez);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
         panUpper.add(jScrollPane2, gridBagConstraints);
 
         jLabel2.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.jLabel2.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 7);
         panUpper.add(jLabel2, gridBagConstraints);
 
         jLabel1.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.jLabel1.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         panUpper.add(jLabel1, gridBagConstraints);
 
         txtGBuchNr.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.txtGBuchNr.text")); // NOI18N
         txtGBuchNr.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     txtGBuchNrActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         panUpper.add(txtGBuchNr, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         getContentPane().add(panUpper, gridBagConstraints);
 
         panLower.setLayout(new java.awt.GridBagLayout());
 
         jLabel5.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.jLabel5.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 5);
         panLower.add(jLabel5, gridBagConstraints);
 
         jPanel1.setMinimumSize(new java.awt.Dimension(101, 35));
         jPanel1.setPreferredSize(new java.awt.Dimension(101, 35));
         jPanel1.setLayout(new java.awt.GridBagLayout());
 
         lblGebuehr.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         lblGebuehr.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.lblGebuehr.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
         gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 5);
         jPanel1.add(lblGebuehr, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         jPanel1.add(jPanel2, gridBagConstraints);
 
         lblGebTitle.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.lblGebTitle.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 5);
         jPanel1.add(lblGebTitle, gridBagConstraints);
 
         lblMwstTitle.setText(org.openide.util.NbBundle.getMessage(
                 BillingPopup.class,
                 "BillingPopup.lblMwstTitle.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
         jPanel1.add(lblMwstTitle, gridBagConstraints);
 
         lblMwst.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         lblMwst.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.lblMwst.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
         jPanel1.add(lblMwst, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         panLower.add(jPanel1, gridBagConstraints);
 
         txtBerechnung.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 10, 2, 10));
         txtBerechnung.setEditable(false);
         txtBerechnung.setMinimumSize(new java.awt.Dimension(0, 200));
         jScrollPane4.setViewportView(txtBerechnung);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         panLower.add(jScrollPane4, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         getContentPane().add(panLower, gridBagConstraints);
 
         panControls.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
 
         cmdCancel.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.cmdCancel.text")); // NOI18N
         cmdCancel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdCancelActionPerformed(evt);
                 }
             });
         panControls.add(cmdCancel);
 
         cmdOk.setText(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.cmdOk.text")); // NOI18N
         cmdOk.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdOkActionPerformed(evt);
                 }
             });
         panControls.add(cmdOk);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         getContentPane().add(panControls, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         getContentPane().add(jSeparator2, gridBagConstraints);
 
         setSize(new java.awt.Dimension(546, 565));
         setLocationRelativeTo(null);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtGBuchNrActionPerformed(final java.awt.event.ActionEvent evt) {
         // TODO add your handling code here:
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdOkActionPerformed(final java.awt.event.ActionEvent evt) {
         if (txtGBuchNr.getText().trim().length() == 0) {
             JOptionPane.showMessageDialog(
                 this,
                 "Sie müssen eine Geschäftsbuchnummer eingeben, damit der Vorgang bearbeitet werden kann.",
                 "Fehlende Eingabe",
                 JOptionPane.WARNING_MESSAGE);
             return;
         }
 
         // save the log entry
         try {
             final CidsBean cb = CidsBean.createNewCidsBeanFromTableName("WUNDA_BLAU", "Billing_Billing");
             cb.setProperty("username", SessionManager.getSession().getUser().toString());
             cb.setProperty("angelegt_durch", getExternalUser());
             cb.setProperty("ts", new java.sql.Timestamp(System.currentTimeMillis()));
             cb.setProperty("angeschaeftsbuch", Boolean.FALSE);
             cb.setProperty("modus", currentMode.getKey());
             cb.setProperty("produktkey", currentProduct.getId());
             cb.setProperty("produktbezeichnung", currentProduct.getName());
             cb.setProperty("netto_summe", nettoPrice);
             cb.setProperty("mwst_satz", currentProduct.getMwst());
             cb.setProperty("brutto_summe", bruttoPrice);
             cb.setProperty("geschaeftsbuchnummer", txtGBuchNr.getText());
 //        cb.setProperty("geometrie", null);
             cb.setProperty("modusbezeichnung", currentMode.getName());
             cb.setProperty("berechnung", txtBerechnung.getText().trim());
             cb.setProperty("verwendungszweck", currentUsage.getName());
             cb.setProperty("projektbezeichnung", txtProjektbez.getText());
             cb.setProperty("request", request);
             cb.setProperty("verwendungskey", currentUsage.getKey());
             cb.persist();
 
             // Nebenläufigkeit my arse
             shouldGoOn = true;
         } catch (Exception e) {
             LOG.error("Error during the persitence of the billing log.", e);
             shouldGoOn = false;
         }
         // the end
         setVisible(false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public CidsBean getExternalUser() {
         final MetaClass MB_MC = ClassCacheMultiple.getMetaClass("WUNDA_BLAU", "billing_kunden_logins");
         if (MB_MC == null) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug(
                     "The metaclass for billing_kunden_logins is null. The current user has probably not the needed rights.");
             }
             return null;
         }
         String query = "SELECT " + MB_MC.getID() + ", " + MB_MC.getPrimaryKey() + " ";
         query += "FROM " + MB_MC.getTableName();
         query += " WHERE name = '" + SessionManager.getSession().getUser().getName() + "'";
 
         CidsBean externalUser = null;
         try {
             final MetaObject[] metaObjects = SessionManager.getProxy().getMetaObjectByQuery(query, 0);
             if ((metaObjects != null) && (metaObjects.length > 0)) {
                 externalUser = metaObjects[0].getBean();
             }
         } catch (ConnectionException ex) {
             LOG.error("Error while retrieving the CidsBean of an external user.", ex);
         }
         return externalUser;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) {
         shouldGoOn = false;
         setVisible(false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboUsageActionPerformed(final java.awt.event.ActionEvent evt) {
         calculateNettoPrice();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboUsagePropertyChange(final java.beans.PropertyChangeEvent evt) {
     }
 
     /**
      * DOCUMENT ME!
      */
     private void calculateNettoPrice() {
         final Object sel = cboUsage.getSelectedItem();
         if (sel instanceof Usage) {
             currentUsage = (Usage)sel;
             String berechungUsageDependent = "";
 
             final double discount = currentProduct.getDiscounts().get(currentUsage.getKey());
             final double absDiscount = (1.0 - discount) * rawPrice;
 
             berechungUsageDependent = "zweckabhängiger Rabatt (" + Math.round((1.0 - discount) * 100) + "%) : -"
                         + NumberFormat.getCurrencyInstance().format(absDiscount) + " \n";
             berechungUsageDependent += "---------\n";
             nettoPrice = rawPrice * discount;
 
             berechungUsageDependent += NumberFormat.getCurrencyInstance().format(nettoPrice) + " \n";
 
             txtBerechnung.setText(berechnungPrefix + "\n" + berechungUsageDependent);
             calculateBruttoPrice();
         } else {
             currentUsage = null;
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void calculateBruttoPrice() {
         final double mwst = nettoPrice * (currentProduct.getMwst() / 100);
         bruttoPrice = nettoPrice + mwst;
        bruttoPrice = Math.round(nettoPrice * 100) / 100;
 
         final DecimalFormat df = new DecimalFormat("0.#");
         lblMwstTitle.setText("zzgl. MwSt. (" + df.format(currentProduct.getMwst()) + "%):");
         lblMwst.setText(NumberFormat.getCurrencyInstance().format(mwst));
         lblGebuehr.setText(NumberFormat.getCurrencyInstance().format(bruttoPrice));
         if (bruttoPrice > 0) {
             lblGebTitle.setIcon(money);
         } else {
             lblGebTitle.setIcon(null);
         }
     }
 
     /**
      * DOCUMENT ME!modus.
      *
      * @param  product  DOCUMENT ME!
      * @param  amounts  DOCUMENT ME!
      */
     private void calculateRawPrice(final String product, final ProductGroupAmount... amounts) {
         rawPrice = 0;
         for (final ProductGroupAmount pga : amounts) {
             rawPrice += ((double)pga.getAmount()) * currentProduct.getPrices().get(pga.group);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean isBillingAllowed() {
         try {
             final User user = SessionManager.getSession().getUser();
             return (SessionManager.getConnection().getConfigAttr(user, MODE_CONFIG_ATTR) == null)
                         || ((SessionManager.getConnection().getConfigAttr(user, MODE_CONFIG_ATTR) != null)
                             && (SessionManager.getConnection().getConfigAttr(user, ALLOWED_USAGE_CONFIG_ATTR) != null));
         } catch (ConnectionException ex) {
             LOG.error("error while checking configAttr", ex);
             return false;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   product  DOCUMENT ME!
      * @param   request  DOCUMENT ME!
      * @param   geom     DOCUMENT ME!
      * @param   amounts  DOCUMENT ME!
      *
      * @throws  Exception                 DOCUMENT ME!
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     private void initialize(
             final String product,
             final String request,
             final Geometry geom,
             final ProductGroupAmount... amounts) throws Exception {
         final User user = SessionManager.getSession().getUser();
 
         // Auslesen des Modus für diesen User
         final String modus = SessionManager.getConnection().getConfigAttr(user, MODE_CONFIG_ATTR);
 
         currentMode = modi.get(modus);
         if (currentMode == null) {
             // Im Moment noch Dialog beenden, später Exception und Druck ablehnen
             LOG.info("mode " + modus + " not found in billing.json. will hide billing popup. reports for free ;-)");
             setVisible(false);
             return;
         }
 
         // Clear den Kram
         txtBerechnung.setText("");
         berechnungPrefix = "";
 
         currentProduct = products.get(product);
 
         berechnungPrefix = "\nProdukt: " + currentProduct.getName() + "\n\n";
 
         final HashMap<String, Double> prices = new HashMap<String, Double>();
 
         // Check ob es die Produktid gibt
         if (currentProduct == null) {
             throw new IllegalArgumentException("Product " + product + " not in the configured productlist.");
         }
 
         // Check ob es jede Produktgruppe gibt
         for (final ProductGroupAmount pga : amounts) {
             if ((currentProduct.getPrices().get(pga.group) == null) || (productGroups.get(pga.group) == null)) {
                 throw new IllegalArgumentException("Productgroup " + pga.group
                             + " not in the configured productgroups.");
             }
             berechnungPrefix += (pga.getAmount() + " " + productGroups.get(pga.group).getDescription() + " (a "
                             + NumberFormat.getCurrencyInstance().format(currentProduct.getPrices().get(pga.group))
                             + ")\n");
         }
 
         berechnungPrefix += "---------\n";
         calculateRawPrice(product, amounts);
         berechnungPrefix += NumberFormat.getCurrencyInstance().format(rawPrice) + " \n";
         berechnungPrefix += "\n";
 
         // Auslesen der gültigen Verwendungszwecke
         final String rawAllowedUsage = SessionManager.getConnection().getConfigAttr(user, ALLOWED_USAGE_CONFIG_ATTR);
         final String[] validUsages = rawAllowedUsage.split(",");
 
         final Usage[] comboUsages = new Usage[validUsages.length];
         int i = 0;
         // Check ob dazu überhaupt ein Discount vorliegt
         for (final String usage : validUsages) {
             if ((currentProduct.getDiscounts().get(usage) == null) || (usages.get(usage) == null)) {
                 throw new IllegalArgumentException("Usage " + usage + " not in the configured discounts for product "
                             + currentProduct.id);
             }
             comboUsages[i++] = usages.get(usage);
         }
 
         cboUsage.setModel(new DefaultComboBoxModel(comboUsages));
 
         setTitle(org.openide.util.NbBundle.getMessage(BillingPopup.class, "BillingPopup.title") + " (" + user + ")");
 
         calculateNettoPrice();
 
         this.request = request;
 
         pack();
         setVisible(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  the command line arguments
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
         DevelopmentTools.initSessionManagerFromRMIConnectionOnLocalhost(
             "WUNDA_BLAU",
             "Administratoren",
             "admin",
             "kif");
         final boolean t = doBilling(
                 "fsnw",
                 "request",
                 null,
                 new ProductGroupAmount("ea", 2),
                 new ProductGroupAmount("ea", 1),
                 new ProductGroupAmount("ea", 1),
                 new ProductGroupAmount("ea", 1));
 
         System.out.println("schluss " + t);
 
         System.exit(0);
     }
 }
