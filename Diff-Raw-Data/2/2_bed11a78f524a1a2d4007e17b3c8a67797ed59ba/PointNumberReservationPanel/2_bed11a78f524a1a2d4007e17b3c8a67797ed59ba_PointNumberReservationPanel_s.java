 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.nas;
 
 import Sirius.navigator.connection.SessionManager;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import org.apache.log4j.Logger;
 
 import java.awt.EventQueue;
 
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 
 import de.cismet.cids.custom.utils.BusyLoggingTextPane;
 import de.cismet.cids.custom.utils.BusyLoggingTextPane.Styles;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import de.cismet.cids.custom.utils.pointnumberreservation.PointNumberReservation;
 import de.cismet.cids.custom.utils.pointnumberreservation.PointNumberReservationRequest;
 import de.cismet.cids.custom.wunda_blau.search.actions.PointNumberReserverationServerAction;
 
 import de.cismet.cids.server.actions.ServerActionParameter;
 
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.XBoundingBox;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.interaction.StatusListener;
 import de.cismet.cismap.commons.interaction.events.StatusEvent;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class PointNumberReservationPanel extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(PointNumberReservationPanel.class);
     private static final String SEVER_ACTION = "pointNumberReservation";
     private static final String WUPP_ZONEN_KENNZIFFER = "32";
 
     //~ Instance fields --------------------------------------------------------
 
     boolean showErrorLbl = false;
     private final PointNumberDialog pnrDialog;
     private BusyLoggingTextPane protokollPane;
     private ArrayList<String> nbz = new ArrayList<String>();
     private int maxNbz = 4;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnErstellen;
     private javax.swing.JButton btnRefreshNbz;
     private javax.swing.JComboBox cbNbz;
     private javax.swing.Box.Filler filler1;
     private javax.swing.Box.Filler filler2;
     private javax.swing.Box.Filler filler3;
     private javax.swing.JSpinner jspAnzahl;
     private javax.swing.JLabel lblAnzahl;
     private javax.swing.JLabel lblNbz;
     private javax.swing.JLabel lblNbzAnzahl;
     private javax.swing.JLabel lblNbzError;
     private javax.swing.JLabel lblNbzINfo;
     private javax.swing.JLabel lblStartwert;
     private javax.swing.JPanel pnlNbz;
     private javax.swing.JPanel pnlNbzInfo;
     private javax.swing.JTextField tfStartWert;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new PointNumberReservationPanel object.
      */
     public PointNumberReservationPanel() {
         this(null);
     }
 
     /**
      * Creates new form PointNumberReservationPanel.
      *
      * @param  pnrDialog  DOCUMENT ME!
      */
     public PointNumberReservationPanel(final PointNumberDialog pnrDialog) {
         this.pnrDialog = pnrDialog;
         final Properties props = new Properties();
         try {
             props.load(PointNumberReservationPanel.class.getResourceAsStream("pointNumberSettings.properties"));
             maxNbz = Integer.parseInt(props.getProperty("maxNbz")); // NOI18N
             if (!loadNummerierungsbezirke()) {
                 showErrorLbl = true;
             }
         } catch (Exception e) {
             LOG.error("Error reading pointNUmberSetting.properties", e);
             showErrorLbl = true;
         }
         initComponents();
         if (!showErrorLbl) {
             cbNbz.setModel(new javax.swing.DefaultComboBoxModel(nbz.toArray(new String[nbz.size()])));
             lblNbzAnzahl.setText("" + nbz.size());
         }
         CismapBroker.getInstance().addStatusListener(new StatusListener() {
 
                 @Override
                 public void statusValueChanged(final StatusEvent e) {
                     final Runnable modifyControls = new Runnable() {
 
                             @Override
                             public void run() {
                                 if (e.getName().equals(StatusEvent.RETRIEVAL_STARTED)) {
                                     btnRefreshNbz.setVisible(true);
                                 }
                             }
                         };
                     if (EventQueue.isDispatchThread()) {
                         modifyControls.run();
                     } else {
                         EventQueue.invokeLater(modifyControls);
                     }
                 }
             });
 
         final JTextComponent textComp = (JTextComponent)cbNbz.getEditor().getEditorComponent();
         textComp.getDocument().addDocumentListener(new DocumentListener() {
 
                 @Override
                 public void insertUpdate(final DocumentEvent e) {
                     checkButtonState();
                 }
 
                 @Override
                 public void removeUpdate(final DocumentEvent e) {
                     checkButtonState();
                 }
 
                 @Override
                 public void changedUpdate(final DocumentEvent e) {
                     checkButtonState();
                 }
             });
         btnRefreshNbz.setVisible(false);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void showError() {
         final Timer t = new Timer();
         t.schedule(new TimerTask() {
 
                 @Override
                 public void run() {
                     if (protokollPane != null) {
                         protokollPane.addMessage(
                             "Während der Bearbeitung des Auftrags trat ein Fehler auf!",
                             Styles.ERROR);
                         protokollPane.setBusy(false);
                     }
                 }
             }, 50);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean loadNummerierungsbezirke() {
         nbz = new ArrayList<String>();
         final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();
         Geometry g = ((XBoundingBox)mapC.getCurrentBoundingBoxFromCamera()).getGeometry();
         if (!CrsTransformer.createCrsFromSrid(g.getSRID()).equals(AlkisConstants.COMMONS.SRS_SERVICE)) {
             g = CrsTransformer.transformToGivenCrs(g, AlkisConstants.COMMONS.SRS_SERVICE);
         }
         final XBoundingBox bb = new XBoundingBox(g);
         final int lowerX = ((Double)Math.floor(bb.getX1())).intValue() / 1000;
         final int upperX = ((Double)Math.floor(bb.getX2())).intValue() / 1000;
         final int lowerY = ((Double)Math.floor(bb.getY1())).intValue() / 1000;
         final int upperY = ((Double)Math.floor(bb.getY2())).intValue() / 1000;
         final int diffX = (((upperX - lowerX) + 1) == 0) ? 1 : ((upperX - lowerX) + 1);
         final int diffY = (((upperY - lowerY) + 1) == 0) ? 1 : ((upperY - lowerY) + 1);
 
         if ((diffX * diffY) > maxNbz) {
             return false;
         }
         for (int i = 0; i < diffX; i++) {
             final int x = lowerX + i;
             for (int j = 0; j < diffY; j++) {
                 final int y = lowerY + j;
                 nbz.add(WUPP_ZONEN_KENNZIFFER + x + y);
             }
         }
         return true;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         lblNbz = new javax.swing.JLabel();
         lblAnzahl = new javax.swing.JLabel();
         jspAnzahl = new javax.swing.JSpinner();
         lblStartwert = new javax.swing.JLabel();
         tfStartWert = new javax.swing.JTextField();
         btnErstellen = new javax.swing.JButton();
         filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 32767));
         if (showErrorLbl) {
             lblNbzError = new javax.swing.JLabel();
         }
         filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 48),
                 new java.awt.Dimension(0, 32767));
         pnlNbz = new javax.swing.JPanel();
         cbNbz = new javax.swing.JComboBox();
         btnRefreshNbz = new javax.swing.JButton();
         pnlNbzInfo = new javax.swing.JPanel();
         lblNbzAnzahl = new javax.swing.JLabel();
         lblNbzINfo = new javax.swing.JLabel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(40, 0),
                 new java.awt.Dimension(40, 0),
                 new java.awt.Dimension(32767, 0));
 
         setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblNbz,
             org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.lblNbz.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblNbz, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblAnzahl,
             org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.lblAnzahl.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblAnzahl, gridBagConstraints);
 
         jspAnzahl.setModel(new javax.swing.SpinnerNumberModel(
                 Integer.valueOf(0),
                 Integer.valueOf(0),
                 null,
                 Integer.valueOf(1)));
         jspAnzahl.setMinimumSize(new java.awt.Dimension(100, 28));
         jspAnzahl.setPreferredSize(new java.awt.Dimension(100, 28));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(jspAnzahl, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblStartwert,
             org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.lblStartwert.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblStartwert, gridBagConstraints);
 
         tfStartWert.setText(org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.tfStartWert.text")); // NOI18N
         tfStartWert.setMinimumSize(new java.awt.Dimension(100, 27));
         tfStartWert.setPreferredSize(new java.awt.Dimension(100, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(tfStartWert, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             btnErstellen,
             org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.btnErstellen.text")); // NOI18N
         btnErstellen.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnErstellenActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(btnErstellen, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 1.0;
         add(filler2, gridBagConstraints);
 
         if (showErrorLbl) {
             lblNbzError.setForeground(new java.awt.Color(255, 0, 0));
             org.openide.awt.Mnemonics.setLocalizedText(
                 lblNbzError,
                 org.openide.util.NbBundle.getMessage(
                     PointNumberReservationPanel.class,
                     "PointNumberReservationPanel.lblNbzError.text")); // NOI18N
         }
         if (showErrorLbl) {
             gridBagConstraints = new java.awt.GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 0;
             gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
             add(lblNbzError, gridBagConstraints);
         }
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         add(filler3, gridBagConstraints);
 
         pnlNbz.setLayout(new java.awt.GridBagLayout());
 
         cbNbz.setEditable(true);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
         pnlNbz.add(cbNbz, gridBagConstraints);
 
         btnRefreshNbz.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/custom/nas/icon-refresh.png"))); // NOI18N
         btnRefreshNbz.setToolTipText(org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.btnRefreshNbz.toolTipText"));               // NOI18N
         btnRefreshNbz.setLabel(org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.btnRefreshNbz.label"));                     // NOI18N
         btnRefreshNbz.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRefreshNbzActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 10);
         pnlNbz.add(btnRefreshNbz, gridBagConstraints);
 
         pnlNbzInfo.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblNbzAnzahl,
             org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.lblNbzAnzahl.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         pnlNbzInfo.add(lblNbzAnzahl, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblNbzINfo,
             org.openide.util.NbBundle.getMessage(
                 PointNumberReservationPanel.class,
                 "PointNumberReservationPanel.lblNbzINfo.text")); // NOI18N
         pnlNbzInfo.add(lblNbzINfo, new java.awt.GridBagConstraints());
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
         pnlNbz.add(pnlNbzInfo, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         pnlNbz.add(filler1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         add(pnlNbz, gridBagConstraints);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnErstellenActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnErstellenActionPerformed
         // check anr
         final String anr = pnrDialog.getAnr();
         if ((anr == null) || anr.isEmpty()) {
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 org.openide.util.NbBundle.getMessage(
                     PointNumberReservationPanel.class,
                     "PointNumberReservationPanel.AnrExistsJOptionPane.message"),
                 org.openide.util.NbBundle.getMessage(
                     PointNumberReservationPanel.class,
                     "PointNumberReservationPanel.AnrExistsJOptionPane.title"),
                 JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         protokollPane = pnrDialog.getProtokollPane();
         try {
             protokollPane.getDocument().remove(0, protokollPane.getDocument().getLength());
         } catch (BadLocationException ex) {
             LOG.error("Could not clear Protokoll Pane", ex);
         }
 
         final String nummerierungsbezirk = (String)cbNbz.getEditor().getItem();
         if (!anr.matches("[a-zA-Z0-9_-]*") || !nummerierungsbezirk.matches("[0-9]*")) {
             JOptionPane.showMessageDialog(
                 StaticSwingTools.getParentFrame(this),
                 org.openide.util.NbBundle.getMessage(
                     PointNumberReservationPanel.class,
                     "PointNumberReservationPanel.ValueCheckJOptionPane.message"),
                 org.openide.util.NbBundle.getMessage(
                     PointNumberReservationPanel.class,
                     "PointNumberReservationPanel.ValueCheckJOptionPane.title"),
                 JOptionPane.ERROR_MESSAGE);
             return;
         }
         protokollPane.setBusy(true);
         final String anrPrefix = pnrDialog.getAnrPrefix();
 
        protokollPane.addMessage("Prüfe ob Antragsnummer " + anrPrefix + "-" + anr + " schon exisitiert.", Styles.INFO);
 
         final SwingWorker<PointNumberReservationRequest, Void> reservationWorker =
             new SwingWorker<PointNumberReservationRequest, Void>() {
 
                 @Override
                 protected PointNumberReservationRequest doInBackground() throws Exception {
                     final Integer anzahl = (Integer)jspAnzahl.getValue();
                     final Integer startwert;
                     final String swText = tfStartWert.getText();
                     if ((swText != null) && !swText.equals("") && swText.matches("[0-9]*")) {
                         startwert = Integer.parseInt(swText);
                     } else {
                         startwert = 0;
                     }
 
                     final ServerActionParameter action;
                     if (pnrDialog.isErgaenzenMode()) {
                         action = new ServerActionParameter(
                                 PointNumberReserverationServerAction.PARAMETER_TYPE.ACTION.toString(),
                                 PointNumberReserverationServerAction.ACTION_TYPE.EXTEND_RESERVATION);
                     } else {
                         action = new ServerActionParameter(
                                 PointNumberReserverationServerAction.PARAMETER_TYPE.ACTION.toString(),
                                 PointNumberReserverationServerAction.ACTION_TYPE.DO_RESERVATION);
                     }
                     final ServerActionParameter prefix = new ServerActionParameter(
                             PointNumberReserverationServerAction.PARAMETER_TYPE.PREFIX.toString(),
                             anrPrefix);
                     final ServerActionParameter aNummer = new ServerActionParameter(
                             PointNumberReserverationServerAction.PARAMETER_TYPE.AUFTRAG_NUMMER.toString(),
                             anr);
                     final ServerActionParameter nbz = new ServerActionParameter(
                             PointNumberReserverationServerAction.PARAMETER_TYPE.NBZ.toString(),
                             nummerierungsbezirk);
                     final ServerActionParameter amount = new ServerActionParameter(
                             PointNumberReserverationServerAction.PARAMETER_TYPE.ANZAHL.toString(),
                             anzahl);
                     final ServerActionParameter startVal = new ServerActionParameter(
                             PointNumberReserverationServerAction.PARAMETER_TYPE.STARTWERT.toString(),
                             startwert);
 
                     final PointNumberReservationRequest result = (PointNumberReservationRequest)SessionManager
                                 .getProxy()
                                 .executeTask(
                                         SEVER_ACTION,
                                         "WUNDA_BLAU",
                                         null,
                                         action,
                                         prefix,
                                         aNummer,
                                         nbz,
                                         amount,
                                         startVal);
 
                     return result;
                 }
 
                 @Override
                 protected void done() {
                     final Timer t = new Timer();
                     t.schedule(new TimerTask() {
 
                             @Override
                             public void run() {
                                 try {
                                     final PointNumberReservationRequest result = get();
                                     pnrDialog.setResult(result);
                                     if ((result == null) || !result.isSuccessfull()) {
                                         protokollPane.addMessage("Fehler beim Senden des Auftrags.", Styles.ERROR);
                                         protokollPane.addMessage("", Styles.INFO);
                                         for (final String s : result.getErrorMessages()) {
                                             protokollPane.addMessage(
                                                 s,
                                                 BusyLoggingTextPane.Styles.ERROR);
                                             protokollPane.addMessage("", Styles.INFO);
                                         }
                                         protokollPane.addMessage("", Styles.INFO);
                                         protokollPane.addMessage(
                                             "Die Protokolldatei mit Fehlerinformationen steht zum Download bereit.",
                                             Styles.ERROR);
                                         protokollPane.setBusy(false);
                                         return;
                                     }
                                     pnrDialog.setResult(result);
                                     protokollPane.addMessage("Ok.", Styles.SUCCESS);
                                     protokollPane.setBusy(false);
                                     protokollPane.addMessage(
                                         "Reservierung für Antragsnummer: "
                                                 + result.getAntragsnummer()
                                                 + ". Folgende Punktnummern wurden reserviert:",
                                         Styles.SUCCESS);
                                     protokollPane.addMessage("", Styles.INFO);
                                     for (final PointNumberReservation pnr : result.getPointNumbers()) {
                                         protokollPane.addMessage("" + pnr.getPunktnummern(), Styles.INFO);
                                     }
                                     if (!pnrDialog.isErgaenzenMode()) {
                                         pnrDialog.addAnr(result.getAntragsnummer().substring(5));
                                     }
                                 } catch (InterruptedException ex) {
                                     LOG.error("Swing worker that executes the reservation was interrupted", ex);
                                     showError();
                                 } catch (ExecutionException ex) {
                                     LOG.error("Error in execution of Swing Worker that executes the reservation", ex);
                                     showError();
                                 }
                             }
                         }, 50);
                 }
             };
 
         final SwingWorker<Boolean, Void> isAntragExistingWorker = new SwingWorker<Boolean, Void>() {
 
                 @Override
                 protected Boolean doInBackground() throws Exception {
                     final ServerActionParameter action = new ServerActionParameter(
                             PointNumberReserverationServerAction.PARAMETER_TYPE.ACTION.toString(),
                             PointNumberReserverationServerAction.ACTION_TYPE.IS_ANTRAG_EXISTING);
                     final ServerActionParameter prefix = new ServerActionParameter(
                             PointNumberReserverationServerAction.PARAMETER_TYPE.PREFIX.toString(),
                             anrPrefix);
                     final ServerActionParameter aNummer = new ServerActionParameter(
                             PointNumberReserverationServerAction.PARAMETER_TYPE.AUFTRAG_NUMMER.toString(),
                             anr);
                     final boolean isAntragExisting = (Boolean)SessionManager.getProxy()
                                 .executeTask(
                                         SEVER_ACTION,
                                         "WUNDA_BLAU",
                                         null,
                                         action,
                                         prefix,
                                         aNummer);
 
                     return isAntragExisting;
                 }
 
                 @Override
                 protected void done() {
                     final Timer t = new Timer();
                     t.schedule(new TimerTask() {
 
                             @Override
                             public void run() {
                                 final boolean startReservationWorker = false;
                                 try {
                                     final Boolean anrExists = get();
                                     if ((anrExists && pnrDialog.isErgaenzenMode())
                                                 || (!anrExists && !pnrDialog.isErgaenzenMode())) {
                                         protokollPane.addMessage("Ok.", Styles.SUCCESS);
                                         protokollPane.addMessage("Sende Reservierungsauftrag.", Styles.INFO);
 //                                startReservationWorker = true;
                                         reservationWorker.run();
                                     } else {
                                         if (pnrDialog.isErgaenzenMode()) {
                                             protokollPane.addMessage(
                                                 "Auftragsnummer existiert noch nicht!",
                                                 Styles.ERROR);
                                             protokollPane.setBusy(false);
                                         } else {
                                             protokollPane.addMessage("Auftragsnummer existiert bereits", Styles.ERROR);
                                             protokollPane.setBusy(false);
                                         }
                                     }
                                 } catch (InterruptedException ex) {
                                     LOG.error(
                                         "Swing worker that checks if antragsnummer is existing was interrupted",
                                         ex);
                                     showError();
                                 } catch (ExecutionException ex) {
                                     LOG.error(
                                         "Error in execution of Swing Worker that checks if antragsnummer is existing",
                                         ex);
                                     showError();
                                 }
 //                                pnrDialog.invalidate();
 //                                pnrDialog.revalidate();
 //                                pnrDialog.repaint();
                                 if (startReservationWorker) {
                                     reservationWorker.run();
                                 }
                             }
                         }, 50);
                 }
             };
 
         isAntragExistingWorker.execute();
     } //GEN-LAST:event_btnErstellenActionPerformed
 
     /**
      * DOCUMENT ME!
      */
     public void checkButtonState() {
         if ((pnrDialog.getAnr() == null) || pnrDialog.getAnr().isEmpty()) {
             btnErstellen.setEnabled(false);
             return;
         }
         if ((cbNbz.getEditor().getItem() == null) || ((String)cbNbz.getEditor().getItem()).isEmpty()) {
             btnErstellen.setEnabled(false);
             return;
         }
         btnErstellen.setEnabled(true);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void checkNummerierungsbezirke() {
         if (loadNummerierungsbezirke()) {
             if (showErrorLbl) {
                 this.remove(lblNbzError);
                 lblNbzError = null;
                 showErrorLbl = false;
             }
         } else {
             showErrorLbl = true;
             if (lblNbzError == null) {
                 lblNbzError = new javax.swing.JLabel();
                 lblNbzError.setForeground(new java.awt.Color(255, 0, 0));
                 org.openide.awt.Mnemonics.setLocalizedText(
                     lblNbzError,
                     org.openide.util.NbBundle.getMessage(
                         PointNumberReservationPanel.class,
                         "PointNumberReservationPanel.lblNbzError.text")); // NOI18N
                 final java.awt.GridBagConstraints gridBagConstraints;
                 gridBagConstraints = new java.awt.GridBagConstraints();
                 gridBagConstraints.gridx = 1;
                 gridBagConstraints.gridy = 0;
                 gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                 gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
                 add(lblNbzError, gridBagConstraints);
             }
         }
         cbNbz.setModel(new javax.swing.DefaultComboBoxModel(nbz.toArray(new String[nbz.size()])));
         lblNbzAnzahl.setText("" + nbz.size());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRefreshNbzActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnRefreshNbzActionPerformed
         checkNummerierungsbezirke();
         btnRefreshNbz.setVisible(false);
         this.invalidate();
         this.validate();
         this.repaint();
     }                                                                                 //GEN-LAST:event_btnRefreshNbzActionPerformed
 }
