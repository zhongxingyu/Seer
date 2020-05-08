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
 package de.cismet.cids.custom.nas;
 
import Sirius.navigator.exception.ConnectionException;

 import com.vividsolutions.jts.geom.Geometry;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.Exceptions;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import java.text.DecimalFormat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.custom.objectrenderer.utils.billing.ProductGroupAmount;
 import de.cismet.cids.custom.utils.nas.NasProductTemplate;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class NasFeePreviewPanel extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger log = Logger.getLogger(NasFeePreviewPanel.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean isPointType;
     private NasProductTemplate template;
     private Geometry geom;
     private int pointAmount = 0;
     private int gebaeudeAmount = 0;
     private int flurstueckAmount = 0;
     private DecimalFormat formatter = new DecimalFormat("#,###,##0.00 \u00A4\u00A4");
     private double discount;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel jPanel1;
     private javax.swing.JLabel lblAnzahlTitle;
     private org.jdesktop.swingx.JXBusyLabel lblBusy;
     private javax.swing.JLabel lblDatensaetze;
     private javax.swing.JLabel lblEigentuemer;
     private javax.swing.JLabel lblEigentuemerAnzahl;
     private javax.swing.JLabel lblEigentuemerGesamt;
     private javax.swing.JLabel lblError;
     private javax.swing.JLabel lblFiller;
     private javax.swing.JLabel lblFlurstuecke;
     private javax.swing.JLabel lblFlurstueckeAnzahl;
     private javax.swing.JLabel lblFlurstueckeGesamt;
     private javax.swing.JLabel lblGebaeude;
     private javax.swing.JLabel lblGebaeudeAnzahl;
     private javax.swing.JLabel lblGebeaudeGesamt;
     private javax.swing.JLabel lblGesamt;
     private javax.swing.JLabel lblGesamtTitle;
     private javax.swing.JLabel lblGesamtValue;
     private javax.swing.JLabel lblPunkte;
     private javax.swing.JLabel lblPunkteAnzahl;
     private javax.swing.JLabel lblPunkteGesamt;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JPanel pnlFee;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new NasFeePreviewPanel object.
      */
     public NasFeePreviewPanel() {
         this(NasProductTemplate.OHNE_EIGENTUEMER);
     }
 
     /**
      * Creates new form NasFeePreviewPanel.
      *
      * @param  template  isPointType DOCUMENT ME!
      */
     public NasFeePreviewPanel(final NasProductTemplate template) {
         this.template = template;
         this.discount = 1;
         initComponents();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  amount  DOCUMENT ME!
      * @param  value   DOCUMENT ME!
      */
     public void setFlurstueckLabels(final String amount, final String value) {
         lblFlurstueckeAnzahl.setText(amount);
         lblFlurstueckeGesamt.setText(value);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  amount  DOCUMENT ME!
      * @param  value   DOCUMENT ME!
      */
     public void setPointLabels(final String amount, final String value) {
         lblPunkteAnzahl.setText(amount);
         lblPunkteGesamt.setText(value);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  discount  DOCUMENT ME!
      */
     public void setDiscount(final double discount) {
         this.discount = discount;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  amount  DOCUMENT ME!
      * @param  value   DOCUMENT ME!
      */
     public void setGebaeudeLabels(final String amount, final String value) {
         lblGebaeudeAnzahl.setText(amount);
         lblGebeaudeGesamt.setText(value);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  amount  DOCUMENT ME!
      * @param  value   DOCUMENT ME!
      */
     public void setEigentuemerLabels(final String amount, final String value) {
         lblEigentuemerAnzahl.setText(amount);
         lblEigentuemerGesamt.setText(value);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  value  DOCUMENT ME!
      */
     public void setTotalLabel(final String value) {
         lblGesamtValue.setText(value);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  wait  DOCUMENT ME!
      */
     private void showWait(final boolean wait) {
         if (wait) {
             if (!lblBusy.isBusy()) {
                 this.removeAll();
                 this.add(lblBusy);
                 lblBusy.setBusy(true);
                 lblBusy.setVisible(true);
             }
         } else {
             lblBusy.setBusy(false);
             lblBusy.setVisible(wait);
             this.removeAll();
             this.add(pnlFee);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void showError() {
         this.remove(lblBusy);
         this.removeAll();
         this.add(lblError, BorderLayout.CENTER);
         lblError.setVisible(true);
         this.invalidate();
         this.revalidate();
         this.repaint();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void calculateFee() {
         final SwingWorker<HashMap<String, ArrayList<String>>, Void> feeCalculator =
             new SwingWorker<HashMap<String, ArrayList<String>>, Void>() {
 
                 @Override
                 protected HashMap<String, ArrayList<String>> doInBackground() throws Exception {
                     SwingUtilities.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 showWait(true);
                             }
                         });
                     // clear the old amount fields
                     pointAmount = 0;
                     flurstueckAmount = 0;
                     gebaeudeAmount = 0;
                     final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
                     if (geom == null) {
 //                    showError();
                         NasFeePreviewPanel.this.revalidate();
                         NasFeePreviewPanel.this.repaint();
                         return null;
                     }
                     // do the search
 // final NasProductTemplate type = (NasProductTemplate)cbType.getSelectedItem();
                     double totalFee = 0;
                     if (template == NasProductTemplate.POINTS) {
                         final ArrayList<String> values = new ArrayList<String>();
                         pointAmount = NasFeeCalculator.getPointAmount(geom);
                         values.add("" + pointAmount);
                         final double pointFee = NasFeeCalculator.getFeeForPoints(pointAmount) * discount;
                         totalFee += pointFee;
                         values.add(formatter.format(pointFee));
                         result.put("points", values);
                     } else {
                         final ArrayList<String> flurstueckValues = new ArrayList<String>();
                         final ArrayList<String> gebaeudeValues = new ArrayList<String>();
                         flurstueckAmount = NasFeeCalculator.getFlurstueckAmount(geom);
                         flurstueckValues.add("" + flurstueckAmount);
                        final double flurstueckFee = NasFeeCalculator.getFeeForFlurstuecke(flurstueckAmount)
                                    * discount;
                         totalFee += flurstueckFee;
                         // ToDo this is a quick and dirty way to calculate the fee for type KOMPLETT
                         if (template == NasProductTemplate.KOMPLETT) {
                             final double eigentuemerFee = NasFeeCalculator.getFeeForEigentuemer(flurstueckAmount)
                                         * discount;
                             totalFee += eigentuemerFee;
                             final ArrayList<String> eigentuemerValues = new ArrayList<String>();
                             eigentuemerValues.add("" + flurstueckAmount);
                             eigentuemerValues.add(formatter.format(eigentuemerFee));
                             result.put("eigentuemer", eigentuemerValues);
                         }
                         flurstueckValues.add(formatter.format(flurstueckFee));
                         result.put("flurstuecke", flurstueckValues);
                         gebaeudeAmount = NasFeeCalculator.getGebaeudeAmount(geom);
                         gebaeudeValues.add("" + gebaeudeAmount);
                         final double gebaeudeFee = NasFeeCalculator.getFeeForGebaeude(gebaeudeAmount) * discount;
                         totalFee += gebaeudeFee;
                         gebaeudeValues.add(formatter.format(gebaeudeFee));
                         result.put("gebaeude", gebaeudeValues);
                     }
                     final ArrayList<String> totalList = new ArrayList<String>();
                     totalList.add(formatter.format(totalFee));
                     result.put("total", totalList);
                     return result;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         final HashMap<String, ArrayList<String>> result = get();
 //                    final NasProductTemplate selectedTemplate = (NasProductTemplate) cbType.getSelectedItem();
 //                    pnlFee = new NasFeePreviewPanel(selectedTemplate);
                         if (result == null) {
 //                            showWait(false);
                             showError();
                             NasFeePreviewPanel.this.revalidate();
                             NasFeePreviewPanel.this.repaint();
                             return;
                         }
                         for (final String key : result.keySet()) {
                             final ArrayList<String> values = result.get(key);
                             if (key.equals("total")) {
                                 setTotalLabel(values.get(0));
                             }
                             if (template == NasProductTemplate.POINTS) {
                                 if (key.equals("points")) {
                                     setPointLabels(values.get(0), values.get(1));
                                     break;
                                 }
                             } else {
                                 if (template == NasProductTemplate.KOMPLETT) {
                                     if (key.equals("eigentuemer")) {
                                         setEigentuemerLabels(values.get(0), values.get(1));
                                     }
                                 }
                                 if (key.equals("gebaeude")) {
                                     setGebaeudeLabels(values.get(0), values.get(1));
                                 } else if (key.equals("flurstuecke")) {
                                     setFlurstueckLabels(values.get(0), values.get(1));
                                 }
                             }
                         }
                         repaint();
                     } catch (InterruptedException ex) {
                         showError();
                         log.error("nas fee calculation was interrupted. showing error state", ex);
                         return;
                     } catch (ExecutionException ex) {
                         showError();
                         log.error("an error occured during nas fee calculation. showing error state", ex);
                         return;
                     }
 
                     showWait(false);
                     NasFeePreviewPanel.this.revalidate();
                     NasFeePreviewPanel.this.repaint();
                 }
             };
 
         feeCalculator.execute();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  geom  DOCUMENT ME!
      */
     public void setGeom(final Geometry geom) {
         this.geom = geom;
         calculateFee();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public NasProductTemplate getTemplate() {
         return template;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ArrayList<ProductGroupAmount> getProductGroupAmounts() {
         final ArrayList<ProductGroupAmount> result = new ArrayList<ProductGroupAmount>();
         if (template == NasProductTemplate.POINTS) {
             result.addAll(getProductGroupAmountForObject("eapkt", pointAmount));
         } else if (template == NasProductTemplate.OHNE_EIGENTUEMER) {
             result.addAll(getProductGroupAmountForObject("eageb", gebaeudeAmount));
             result.addAll(getProductGroupAmountForObject("eaflst", flurstueckAmount));
         } else if (template == NasProductTemplate.KOMPLETT) {
             result.addAll(getProductGroupAmountForObject("eageb", gebaeudeAmount));
             result.addAll(getProductGroupAmountForObject("eaflst", flurstueckAmount));
             result.addAll(getProductGroupAmountForObject("eaeig", flurstueckAmount));
         }
         return result;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void refresh() {
         calculateFee();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   objectBaseKey  DOCUMENT ME!
      * @param   amount         DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private ArrayList<ProductGroupAmount> getProductGroupAmountForObject(final String objectBaseKey, int amount) {
         final ArrayList<ProductGroupAmount> result = new ArrayList<ProductGroupAmount>();
         if (amount > 1000000) {
             final int tmpPoints = amount - 1000000;
             result.add(new ProductGroupAmount(objectBaseKey + "_1000001", tmpPoints));
             amount = 1000000;
         }
         if (amount > 100000) {
             final int tmpPoints = amount - 100000;
             result.add(new ProductGroupAmount(objectBaseKey + "_100001-1000000", tmpPoints));
             amount = 100000;
         }
         if (amount > 10000) {
             final int tmpPoints = amount - 10000;
             result.add(new ProductGroupAmount(objectBaseKey + "_10001-100000", tmpPoints));
             amount = 10000;
         }
         if (amount > 1000) {
             final int tmpPoints = amount - 1000;
             result.add(new ProductGroupAmount(objectBaseKey + "_1001-10000", tmpPoints));
             amount = 1000;
         }
         result.add(new ProductGroupAmount(objectBaseKey + "_1000", amount));
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  DOCUMENT ME!
      */
     public static void main(final String[] args) {
         final JFrame f = new JFrame("foo");
         f.setSize(500, 500);
         f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         final NasFeePreviewPanel pan = new NasFeePreviewPanel();
         f.getContentPane().add(pan);
         f.setVisible(true);
         try {
             Thread.sleep(2000);
         } catch (InterruptedException ex) {
             Exceptions.printStackTrace(ex);
         }
         pan.setGeom(null);
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         pnlFee = new javax.swing.JPanel();
         lblTitle = new javax.swing.JLabel();
         lblAnzahlTitle = new javax.swing.JLabel();
         lblGesamtTitle = new javax.swing.JLabel();
         if (template != NasProductTemplate.POINTS) {
             lblFlurstuecke = new javax.swing.JLabel();
         }
         if (template != NasProductTemplate.POINTS) {
             lblGebaeude = new javax.swing.JLabel();
         }
         if (template == NasProductTemplate.POINTS) {
             lblPunkte = new javax.swing.JLabel();
         }
         if (template != NasProductTemplate.POINTS) {
             lblFlurstueckeAnzahl = new javax.swing.JLabel();
         }
         if (template != NasProductTemplate.POINTS) {
             lblFlurstueckeGesamt = new javax.swing.JLabel();
         }
         if (template != NasProductTemplate.POINTS) {
             lblGebaeudeAnzahl = new javax.swing.JLabel();
         }
         if (template != NasProductTemplate.POINTS) {
             lblGebeaudeGesamt = new javax.swing.JLabel();
         }
         if (template == NasProductTemplate.POINTS) {
             lblPunkteAnzahl = new javax.swing.JLabel();
         }
         if (template == NasProductTemplate.POINTS) {
             lblPunkteGesamt = new javax.swing.JLabel();
         }
         jPanel1 = new javax.swing.JPanel();
         lblGesamtValue = new javax.swing.JLabel();
         lblGesamt = new javax.swing.JLabel();
         if (template == NasProductTemplate.KOMPLETT) {
             lblEigentuemer = new javax.swing.JLabel();
         }
         if (template == NasProductTemplate.KOMPLETT) {
             lblEigentuemerAnzahl = new javax.swing.JLabel();
         }
         if (template == NasProductTemplate.KOMPLETT) {
             lblEigentuemerGesamt = new javax.swing.JLabel();
         }
         lblFiller = new javax.swing.JLabel();
         lblDatensaetze = new javax.swing.JLabel();
         lblError = new javax.swing.JLabel();
         lblBusy = new org.jdesktop.swingx.JXBusyLabel(new Dimension(75, 75));
 
         pnlFee.setOpaque(false);
         pnlFee.setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 14));                                               // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             lblTitle,
             org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblTitle.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 20, 10);
         pnlFee.add(lblTitle, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblAnzahlTitle,
             org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblAnzahlTitle.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.weightx = 0.7;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
         pnlFee.add(lblAnzahlTitle, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblGesamtTitle,
             org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblGesamtTitle.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.weightx = 0.3;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
         pnlFee.add(lblGesamtTitle, gridBagConstraints);
 
         if (template != NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblFlurstuecke,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblFlurstuecke.text")); // NOI18N
         }
         if (template != NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 0;
             gridBagConstraints.gridy = 3;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
             gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
             pnlFee.add(lblFlurstuecke, gridBagConstraints);
         }
 
         if (template != NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblGebaeude,
                 org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblGebaeude.text")); // NOI18N
         }
         if (template != NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 0;
             gridBagConstraints.gridy = 2;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
             gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
             pnlFee.add(lblGebaeude, gridBagConstraints);
         }
 
         if (template == NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblPunkte,
                 org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblPunkte.text")); // NOI18N
         }
         if (template == NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 0;
             gridBagConstraints.gridy = 6;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
             gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
             pnlFee.add(lblPunkte, gridBagConstraints);
         }
 
         if (template != NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblFlurstueckeAnzahl,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblFlurstueckeAnzahl.text")); // NOI18N
         }
         if (template != NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 3;
             gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
             pnlFee.add(lblFlurstueckeAnzahl, gridBagConstraints);
         }
 
         if (template != NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblFlurstueckeGesamt,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblFlurstueckeGesamt.text")); // NOI18N
         }
         if (template != NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 2;
             gridBagConstraints.gridy = 3;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
             gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
             pnlFee.add(lblFlurstueckeGesamt, gridBagConstraints);
         }
 
         if (template != NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblGebaeudeAnzahl,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblGebaeudeAnzahl.text")); // NOI18N
         }
         if (template != NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 2;
             gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
             pnlFee.add(lblGebaeudeAnzahl, gridBagConstraints);
         }
 
         if (template != NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblGebeaudeGesamt,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblGebeaudeGesamt.text")); // NOI18N
         }
         if (template != NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 2;
             gridBagConstraints.gridy = 2;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
             gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
             pnlFee.add(lblGebeaudeGesamt, gridBagConstraints);
         }
 
         if (template == NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblPunkteAnzahl,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblPunkteAnzahl.text")); // NOI18N
         }
         if (template == NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 6;
             gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
             pnlFee.add(lblPunkteAnzahl, gridBagConstraints);
         }
 
         if (template == NasProductTemplate.POINTS) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblPunkteGesamt,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblPunkteGesamt.text")); // NOI18N
         }
         if (template == NasProductTemplate.POINTS) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 2;
             gridBagConstraints.gridy = 6;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
             gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
             pnlFee.add(lblPunkteGesamt, gridBagConstraints);
         }
 
         jPanel1.setOpaque(false);
         jPanel1.setLayout(new java.awt.GridBagLayout());
 
         lblGesamtValue.setFont(new java.awt.Font("DejaVu Sans", 1, 14));                                               // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             lblGesamtValue,
             org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblGesamtValue.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(20, 10, 10, 0);
         jPanel1.add(lblGesamtValue, gridBagConstraints);
 
         lblGesamt.setFont(new java.awt.Font("DejaVu Sans", 1, 14));                                               // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(
             lblGesamt,
             org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblGesamt.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
         jPanel1.add(lblGesamt, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
         pnlFee.add(jPanel1, gridBagConstraints);
 
         if (template == NasProductTemplate.KOMPLETT) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblEigentuemer,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblEigentuemer.text")); // NOI18N
         }
         if (template == NasProductTemplate.KOMPLETT) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 0;
             gridBagConstraints.gridy = 5;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
             gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
             pnlFee.add(lblEigentuemer, gridBagConstraints);
         }
 
         if (template == NasProductTemplate.KOMPLETT) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblEigentuemerAnzahl,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblEigentuemerAnzahl.text")); // NOI18N
         }
         if (template == NasProductTemplate.KOMPLETT) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 5;
             gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
             pnlFee.add(lblEigentuemerAnzahl, gridBagConstraints);
         }
 
         if (template == NasProductTemplate.KOMPLETT) {
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblEigentuemerGesamt,
                 org.openide.util.NbBundle.getMessage(
                     NasFeePreviewPanel.class,
                     "NasFeePreviewPanel.lblEigentuemerGesamt.text")); // NOI18N
         }
         if (template == NasProductTemplate.KOMPLETT) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 2;
             gridBagConstraints.gridy = 5;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
             gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
             pnlFee.add(lblEigentuemerGesamt, gridBagConstraints);
         }
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblFiller,
             org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblFiller.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         pnlFee.add(lblFiller, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblDatensaetze,
             org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblDatensaetze.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
         pnlFee.add(lblDatensaetze, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblError,
             org.openide.util.NbBundle.getMessage(NasFeePreviewPanel.class, "NasFeePreviewPanel.lblError.text")); // NOI18N
 
         setOpaque(false);
         setLayout(new java.awt.BorderLayout());
 
         lblBusy.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblBusy.setMaximumSize(new java.awt.Dimension(140, 40));
         lblBusy.setMinimumSize(new java.awt.Dimension(140, 60));
         lblBusy.setPreferredSize(new java.awt.Dimension(140, 60));
         add(lblBusy, java.awt.BorderLayout.CENTER);
     } // </editor-fold>//GEN-END:initComponents
 }
