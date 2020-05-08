 // %1128099595984:de.hattrickorganizer.gui.transferscout%
 package de.hattrickorganizer.gui.transferscout;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 //import java.awt.datatransfer.Clipboard;
 //import java.awt.datatransfer.DataFlavor;
 //import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyListener;
 import java.text.NumberFormat;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SwingConstants;
 
 import plugins.IEPVData;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.gui.model.CBItem;
 import de.hattrickorganizer.gui.templates.ColorLabelEntry;
 import de.hattrickorganizer.gui.templates.DoppelLabelEntry;
 import de.hattrickorganizer.gui.templates.ImagePanel;
 import de.hattrickorganizer.model.EPVData;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.ScoutEintrag;
 import de.hattrickorganizer.model.Spieler;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.Helper;
 
 
 /**
  * A player can be created and modified here.
  *
  * @author Marco Senn
  */
 public class TransferEingabePanel extends ImagePanel implements ItemListener, ActionListener,
                                                                 FocusListener, KeyListener
 {
     //~ Static fields/initializers -----------------------------------------------------------------
 
 	private static final long serialVersionUID = -3287232092187457640L;
	private static int iTempSpielerID = -1;
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     private ColorLabelEntry jpBestPos = new ColorLabelEntry("", ColorLabelEntry.FG_STANDARD,
                                                             ColorLabelEntry.BG_STANDARD, SwingConstants.LEFT);
     private DoppelLabelEntry jpWertAussenVert = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private DoppelLabelEntry jpWertAussenVertDef = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertAussenVertIn = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertAussenVertOff = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertFluegel = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private DoppelLabelEntry jpWertFluegelDef = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertFluegelIn = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertFluegelOff = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertInnenVert = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertInnenVertAus = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertInnenVertOff = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertMittelfeld = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private DoppelLabelEntry jpWertMittelfeldAus = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertMittelfeldDef = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertMittelfeldOff = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertSturm = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private DoppelLabelEntry jpWertSturmAus = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertSturmDef = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private DoppelLabelEntry jpWertTor = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private JButton jbAddTempSpieler = new JButton(HOVerwaltung.instance().getLanguageString("AddTempspieler"));
     private JButton jbDrucken = new JButton(new ImageIcon(de.hattrickorganizer.tools.Helper
                                                           .loadImage("gui/bilder/Drucken.png")));
     private JButton jbEntfernen = new JButton(HOVerwaltung.instance().getLanguageString("ScoutEntfernen"));
     private JButton jbHinzufuegen = new JButton(HOVerwaltung.instance().getLanguageString("ScoutHinzu"));
     private JButton jbMiniScout = new JButton(HOVerwaltung.instance().getLanguageString("ScoutMini"));
     private JButton jbUebernehmen = new JButton(HOVerwaltung.instance().getLanguageString("Uebernehmen"));
     private JButton jbRemoveAll = new JButton(HOVerwaltung.instance().getLanguageString("Scout.RemoveAll"));
     //private JButton jbClipBoard = new JButton("From Clipboard");
     private JComboBox jcbErfahrung = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG);
     private JComboBox jcbFluegel = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG);
     private JComboBox jcbForm = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG_FORM);
     private JComboBox jcbKondition = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG_KONDITION);
     private JComboBox jcbPasspiel = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG);
     private JComboBox jcbSpeciality = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG_SPECIALITY);
     private JComboBox jcbSpielaufbau = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG);
     private JComboBox jcbStandard = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG);
     private JComboBox jcbTorschuss = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG);
     private JComboBox jcbTorwart = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG);
     private JComboBox jcbVerteidigung = new JComboBox(de.hattrickorganizer.tools.Helper.EINSTUFUNG);
     private JLabel jlStatus = new JLabel(HOVerwaltung.instance().getLanguageString("scout_status")
                                          + ": ");
     private JTextArea jtaCopyPaste = new JTextArea(5, 20);
     private JTextArea jtaNotizen = new JTextArea();
     private JTextField jtfAlter = new JTextField("17.0");
     private JTextField jtfTSI = new JTextField("1000");
     private JTextField jtfPrice = new JTextField("0");
 	private JLabel jtfEPV = new JLabel("",SwingConstants.RIGHT);
     private ScoutEintrag clScoutEintrag;
     private SpinnerDateModel clSpinnerModel = new SpinnerDateModel();
     private JSpinner jsSpinner = new JSpinner(clSpinnerModel);
     private JTextField jtfName = new JTextField();
     private JTextField jtfPlayerID = new JTextField("0");
     private TransferScoutPanel clOwner;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new TransferEingabePanel object.
      *
      * @param owner the parent control holding this panel
      */
     public TransferEingabePanel(TransferScoutPanel owner) {
         clOwner = owner;
         initComponents();
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Return next temporary playerID. Starting from -1.
      *
      * @return Returns next temporary playerID.
      */
     public static int getNextTempSpielerID() {
         return iTempSpielerID--;
     }
 
     /**
      * Set new scout entry or modify old
      *
      * @param scoutEintrag The new scout entry object
      */
     public final void setScoutEintrag(ScoutEintrag scoutEintrag) {
         if (scoutEintrag != null) {
             clScoutEintrag = scoutEintrag;
 
             // If scout entry already exists
             if (clOwner.getTransferTable().getTransferTableModel().getScoutEintrag(clScoutEintrag
                                                                                    .getPlayerID()) != null) {
                 jbHinzufuegen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Transferscout_ersetzen"));
                 jbHinzufuegen.setText(HOVerwaltung.instance().getLanguageString("ScoutErsetzen"));
                 jbEntfernen.setEnabled(true);
             } else {
                 jbHinzufuegen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Transferscout_hinzufuegen"));
                 jbHinzufuegen.setText(HOVerwaltung.instance().getLanguageString("ScoutHinzu"));
                 jbEntfernen.setEnabled(false);
             }
 
             jbHinzufuegen.setEnabled(true);
         } else {
             clScoutEintrag = new ScoutEintrag();
             jbHinzufuegen.setText(HOVerwaltung.instance().getLanguageString("ScoutHinzu"));
             jbEntfernen.setEnabled(false);
             jbHinzufuegen.setEnabled(false);
         }
 
         setCBs();
         setLabels();
         checkFields();
 
         invalidate();
         validate();
         repaint();
     }
 
     /**
      * Fired when any button is pressed
      *
      * @param actionEvent Event fired when button is pressed.
      */
     public final void actionPerformed(java.awt.event.ActionEvent actionEvent) {
         if (actionEvent.getSource().equals(jbUebernehmen)) {
             copyPaste();
         //} else if (actionEvent.getSource().equals(jbClipBoard)) {
         //	copyFromClipBoard();
     	} else if (actionEvent.getSource().equals(jbAddTempSpieler)) {
             final Spieler tempSpieler = new Spieler();
             tempSpieler.setNationalitaet(HOVerwaltung.instance().getModel().getBasics().getLand());
             tempSpieler.setSpielerID(getNextTempSpielerID());
 
             if (jtfName.getText().trim().equals("")) {
                tempSpieler.setName("Temp " + Math.abs(tempSpieler.getSpielerID()));
             } else {
                 tempSpieler.setName(jtfName.getText());
             }
 
             tempSpieler.setTSI(Integer.parseInt(jtfTSI.getText()));
             tempSpieler.setSpezialitaet(((CBItem) jcbSpeciality.getSelectedItem()).getId());
             tempSpieler.setAlter(Integer.parseInt(jtfAlter.getText().replaceFirst("\\..*", "")));
             tempSpieler.setAgeDays(Integer.parseInt(jtfAlter.getText().replaceFirst(".*\\.", "")));
             tempSpieler.setErfahrung(((CBItem) jcbErfahrung.getSelectedItem()).getId());
             tempSpieler.setForm(((CBItem) jcbForm.getSelectedItem()).getId());
             tempSpieler.setKondition(((CBItem) jcbKondition.getSelectedItem()).getId());
             tempSpieler.setVerteidigung(((CBItem) jcbVerteidigung.getSelectedItem()).getId());
             tempSpieler.setTorschuss(((CBItem) jcbTorschuss.getSelectedItem()).getId());
             tempSpieler.setTorwart(((CBItem) jcbTorwart.getSelectedItem()).getId());
             tempSpieler.setFluegelspiel(((CBItem) jcbFluegel.getSelectedItem()).getId());
             tempSpieler.setPasspiel(((CBItem) jcbPasspiel.getSelectedItem()).getId());
             tempSpieler.setStandards(((CBItem) jcbStandard.getSelectedItem()).getId());
             tempSpieler.setSpielaufbau(((CBItem) jcbSpielaufbau.getSelectedItem()).getId());
 
             HOVerwaltung.instance().getModel().addSpieler(tempSpieler);
             de.hattrickorganizer.gui.RefreshManager.instance().doReInit();
             de.hattrickorganizer.gui.HOMainFrame.instance().showTab(de.hattrickorganizer.gui.HOMainFrame.SPIELERUEBERSICHT);
         }
 
 		else if (actionEvent.getSource().equals(jbRemoveAll)) {
 			clOwner.removeScoutEntries();
 		}
     	else {
             clScoutEintrag.setPlayerID(Integer.parseInt(jtfPlayerID.getText()));
             clScoutEintrag.setPrice(Integer.parseInt(jtfPrice.getText()));
             clScoutEintrag.setAlter(Integer.parseInt(jtfAlter.getText().replaceFirst("\\..*", "")));
             clScoutEintrag.setAgeDays(Integer.parseInt(jtfAlter.getText().replaceFirst(".*\\.", "")));
             clScoutEintrag.setTSI(Integer.parseInt(jtfTSI.getText()));
             clScoutEintrag.setName(jtfName.getText());
             clScoutEintrag.setInfo(jtaNotizen.getText());
             clScoutEintrag.setDeadline(new java.sql.Timestamp(clSpinnerModel.getDate().getTime()));
 
             // clScoutEintrag.setDeadline(new java.sql.Timestamp( zeitlong ) );
             if (actionEvent.getSource().equals(jbHinzufuegen)) {
                 clOwner.addScoutEintrag(clScoutEintrag);
             } else if (actionEvent.getSource().equals(jbEntfernen)) {
                 clOwner.removeScoutEintrag(clScoutEintrag);
             } else if (actionEvent.getSource().equals(jbMiniScout)) {
                 new MiniScoutDialog(this);
             } else if (actionEvent.getSource().equals(jbDrucken)) {
                 clOwner.drucken();
             }
         }
 
         checkFields();
     }
 
     /**
      * Fired when panel receives focus
      *
      * @param focusEvent Event fired when panel receives focus
      */
     public void focusGained(java.awt.event.FocusEvent focusEvent) {
     }
 
     /**
      * Fired when panel losts focus
      *
      * @param focusEvent Event fired when panel losts focus
      */
     public final void focusLost(java.awt.event.FocusEvent focusEvent) {
         if (!de.hattrickorganizer.tools.Helper.parseInt(de.hattrickorganizer.gui.HOMainFrame
                                                            .instance(), jtfTSI, false)
             || !de.hattrickorganizer.tools.Helper.parseInt(de.hattrickorganizer.gui.HOMainFrame
                                                            .instance(), jtfPlayerID, false)
             || !de.hattrickorganizer.tools.Helper.parseInt(de.hattrickorganizer.gui.HOMainFrame
                                                            .instance(), jtfPrice, false)) {
             return;
         }
 
         checkFields();
         if (focusEvent.getSource().equals(jtfAlter)) {
 			setLabels();
         }
 
     }
 
     /**
      * Fired when an item changes
      *
      * @param itemEvent Indicates which item has changed
      */
     public final void itemStateChanged(java.awt.event.ItemEvent itemEvent) {
         if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
             setLabels();
         }
     }
 
     /**
      * Fired when a key is pressed
      *
      * @param keyEvent Event fired when a key is pressed
      */
     public void keyPressed(java.awt.event.KeyEvent keyEvent) {
     }
 
     /**
      * Fired when a key is released
      *
      * @param keyEvent Event fired when a key is released
      */
     public final void keyReleased(java.awt.event.KeyEvent keyEvent) {
         checkFields();
     }
 
     /**
      * Fired when a key is typed
      *
      * @param keyEvent Fired when a key is typed
      */
     public void keyTyped(java.awt.event.KeyEvent keyEvent) {
     }
 
     /**
      * Set checkboxes to their corresponding value
      */
     private void setCBs() {
         clSpinnerModel.setValue(clScoutEintrag.getDeadline());
 
         //m_jtfDatum.setText ( java.text.DateFormat.getDateTimeInstance ().format ( m_clScoutEintrag.getDeadline () ) );
         jtfPlayerID.setText(clScoutEintrag.getPlayerID() + "");
         jtfName.setText(clScoutEintrag.getName());
         jtfPrice.setText(clScoutEintrag.getPrice() + "");
         jtfAlter.setText(clScoutEintrag.getAlter() + "." + clScoutEintrag.getAgeDays());
         jtfTSI.setText(clScoutEintrag.getTSI() + "");
         jtaNotizen.setText(clScoutEintrag.getInfo());
         jcbSpeciality.removeItemListener(this);
         jcbErfahrung.removeItemListener(this);
         jcbForm.removeItemListener(this);
         jcbKondition.removeItemListener(this);
         jcbSpielaufbau.removeItemListener(this);
         jcbFluegel.removeItemListener(this);
         jcbTorschuss.removeItemListener(this);
         jcbTorwart.removeItemListener(this);
         jcbPasspiel.removeItemListener(this);
         jcbVerteidigung.removeItemListener(this);
         jcbStandard.removeItemListener(this);
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbSpeciality,
                                                             clScoutEintrag.getSpeciality());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbErfahrung,
                                                             clScoutEintrag.getErfahrung());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbForm, clScoutEintrag.getForm());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbKondition,
                                                             clScoutEintrag.getKondition());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbSpielaufbau,
                                                             clScoutEintrag.getSpielaufbau());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbFluegel,
                                                             clScoutEintrag.getFluegelspiel());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbTorschuss,
                                                             clScoutEintrag.getTorschuss());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbTorwart, clScoutEintrag.getTorwart());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbPasspiel,
                                                             clScoutEintrag.getPasspiel());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbVerteidigung,
                                                             clScoutEintrag.getVerteidigung());
         de.hattrickorganizer.tools.Helper.markierenComboBox(jcbStandard,
                                                             clScoutEintrag.getStandards());
         jcbSpeciality.addItemListener(this);
         jcbErfahrung.addItemListener(this);
         jcbForm.addItemListener(this);
         jcbKondition.addItemListener(this);
         jcbSpielaufbau.addItemListener(this);
         jcbFluegel.addItemListener(this);
         jcbTorschuss.addItemListener(this);
         jcbTorwart.addItemListener(this);
         jcbPasspiel.addItemListener(this);
         jcbVerteidigung.addItemListener(this);
         jcbStandard.addItemListener(this);
     }
 
     /**
      * Set labels to the new values
      */
     private void setLabels() {
         final Spieler tempSpieler = new Spieler();
         tempSpieler.setSpezialitaet(((CBItem) jcbSpeciality.getSelectedItem()).getId());
         tempSpieler.setErfahrung(((CBItem) jcbErfahrung.getSelectedItem()).getId());
 		tempSpieler.setFuehrung(3);
         tempSpieler.setForm(((CBItem) jcbForm.getSelectedItem()).getId());
         tempSpieler.setKondition(((CBItem) jcbKondition.getSelectedItem()).getId());
         tempSpieler.setVerteidigung(((CBItem) jcbVerteidigung.getSelectedItem()).getId());
         tempSpieler.setTorschuss(((CBItem) jcbTorschuss.getSelectedItem()).getId());
         tempSpieler.setTorwart(((CBItem) jcbTorwart.getSelectedItem()).getId());
         tempSpieler.setFluegelspiel(((CBItem) jcbFluegel.getSelectedItem()).getId());
         tempSpieler.setPasspiel(((CBItem) jcbPasspiel.getSelectedItem()).getId());
         tempSpieler.setStandards(((CBItem) jcbStandard.getSelectedItem()).getId());
         tempSpieler.setSpielaufbau(((CBItem) jcbSpielaufbau.getSelectedItem()).getId());
         tempSpieler.setAlter(Integer.parseInt(jtfAlter.getText().replaceFirst("\\..*", "")));
         tempSpieler.setAgeDays(Integer.parseInt(jtfAlter.getText().replaceFirst(".*\\.", "")));
 		IEPVData data = new EPVData(tempSpieler);
 		double price = HOVerwaltung.instance().getModel().getEPV().getPrice(data);
 		jtfEPV.setText(NumberFormat.getCurrencyInstance().format(price));
 
         jpBestPos.setText(de.hattrickorganizer.model.SpielerPosition.getNameForPosition(tempSpieler
                                                                                         .getIdealPosition())
                           + " (" + tempSpieler.calcPosValue(tempSpieler.getIdealPosition(), true)
                           + ")");
         jpWertTor.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                              .calcPosValue(ISpielerPosition.KEEPER,
                                                                                            true),
                                                                              gui.UserParameter
                                                                              .instance().anzahlNachkommastellen)
                                      + "");
         jpWertInnenVert.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                    .calcPosValue(ISpielerPosition.CENTRAL_DEFENDER,
                                                                                                  true),
                                                                                    gui.UserParameter
                                                                                    .instance().anzahlNachkommastellen)
                                            + "");
         jpWertInnenVertAus.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                       .calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_TOWING,
                                                                                                     true),
                                                                                       gui.UserParameter
                                                                                       .instance().anzahlNachkommastellen)
                                               + "");
         jpWertInnenVertOff.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                       .calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_OFF,
                                                                                                     true),
                                                                                       gui.UserParameter
                                                                                       .instance().anzahlNachkommastellen)
                                               + "");
         jpWertAussenVert.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                     .calcPosValue(ISpielerPosition.BACK,
                                                                                                   true),
                                                                                     gui.UserParameter
                                                                                     .instance().anzahlNachkommastellen)
                                             + "");
         jpWertAussenVertIn.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                       .calcPosValue(ISpielerPosition.BACK_TOMID,
                                                                                                     true),
                                                                                       gui.UserParameter
                                                                                       .instance().anzahlNachkommastellen)
                                               + "");
         jpWertAussenVertOff.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                        .calcPosValue(ISpielerPosition.BACK_OFF,
                                                                                                      true),
                                                                                        gui.UserParameter
                                                                                        .instance().anzahlNachkommastellen)
                                                + "");
         jpWertAussenVertDef.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                        .calcPosValue(ISpielerPosition.BACK_DEF,
                                                                                                      true),
                                                                                        gui.UserParameter
                                                                                        .instance().anzahlNachkommastellen)
                                                + "");
         jpWertMittelfeld.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                     .calcPosValue(ISpielerPosition.MIDFIELDER,
                                                                                                   true),
                                                                                     gui.UserParameter
                                                                                     .instance().anzahlNachkommastellen)
                                             + "");
         jpWertMittelfeldAus.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                        .calcPosValue(ISpielerPosition.MIDFIELDER_TOWING,
                                                                                                      true),
                                                                                        gui.UserParameter
                                                                                        .instance().anzahlNachkommastellen)
                                                + "");
         jpWertMittelfeldOff.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                        .calcPosValue(ISpielerPosition.MIDFIELDER_OFF,
                                                                                                      true),
                                                                                        gui.UserParameter
                                                                                        .instance().anzahlNachkommastellen)
                                                + "");
         jpWertMittelfeldDef.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                        .calcPosValue(ISpielerPosition.MIDFIELDER_DEF,
                                                                                                      true),
                                                                                        gui.UserParameter
                                                                                        .instance().anzahlNachkommastellen)
                                                + "");
         jpWertFluegel.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                  .calcPosValue(ISpielerPosition.WINGER,
                                                                                                true),
                                                                                  gui.UserParameter
                                                                                  .instance().anzahlNachkommastellen)
                                          + "");
         jpWertFluegelIn.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                    .calcPosValue(ISpielerPosition.WINGER_TOMID,
                                                                                                  true),
                                                                                    gui.UserParameter
                                                                                    .instance().anzahlNachkommastellen)
                                            + "");
         jpWertFluegelOff.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                     .calcPosValue(ISpielerPosition.WINGER_OFF,
                                                                                                   true),
                                                                                     gui.UserParameter
                                                                                     .instance().anzahlNachkommastellen)
                                             + "");
         jpWertFluegelDef.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                     .calcPosValue(ISpielerPosition.WINGER_DEF,
                                                                                                   true),
                                                                                     gui.UserParameter
                                                                                     .instance().anzahlNachkommastellen)
                                             + "");
         jpWertSturm.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                .calcPosValue(ISpielerPosition.FORWARD,
                                                                                              true),
                                                                                gui.UserParameter
                                                                                .instance().anzahlNachkommastellen)
                                        + "");
 
         jpWertSturmAus.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                 																.calcPosValue(ISpielerPosition.FORWARD_TOWING,
 															                              true),
 															                    gui.UserParameter
 															                    .instance().anzahlNachkommastellen) + "");
         jpWertSturmDef.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                   .calcPosValue(ISpielerPosition.FORWARD_DEF,
                                                                                                 true),
                                                                                   gui.UserParameter
                                                                                   .instance().anzahlNachkommastellen)
                                           + "");
 
         clScoutEintrag.setSpeciality(((CBItem) jcbSpeciality.getSelectedItem()).getId());
         clScoutEintrag.setErfahrung(((CBItem) jcbErfahrung.getSelectedItem()).getId());
         clScoutEintrag.setForm(((CBItem) jcbForm.getSelectedItem()).getId());
         clScoutEintrag.setKondition(((CBItem) jcbKondition.getSelectedItem()).getId());
         clScoutEintrag.setVerteidigung(((CBItem) jcbVerteidigung.getSelectedItem()).getId());
         clScoutEintrag.setTorschuss(((CBItem) jcbTorschuss.getSelectedItem()).getId());
         clScoutEintrag.setTorwart(((CBItem) jcbTorwart.getSelectedItem()).getId());
         clScoutEintrag.setFluegelspiel(((CBItem) jcbFluegel.getSelectedItem()).getId());
         clScoutEintrag.setPasspiel(((CBItem) jcbPasspiel.getSelectedItem()).getId());
         clScoutEintrag.setStandards(((CBItem) jcbStandard.getSelectedItem()).getId());
         clScoutEintrag.setSpielaufbau(((CBItem) jcbSpielaufbau.getSelectedItem()).getId());
 
     }
 
     /**
      * Check inputfields of valid values
      */
     private void checkFields() {
         // When playerid already exists
         int id = 0;
         boolean valid = true;
 
         try {
             id = Integer.parseInt(jtfPlayerID.getText());
         } catch (NumberFormatException e) {
             valid = false;
         }
 
         if (valid
             && (clOwner.getTransferTable().getTransferTableModel().getScoutEintrag(id) != null)) {
             jbHinzufuegen.setEnabled(true);
             jbHinzufuegen.setText(HOVerwaltung.instance().getLanguageString("ScoutErsetzen"));
             jbEntfernen.setEnabled(true);
         } else {
             jbHinzufuegen.setEnabled(true);
             jbHinzufuegen.setText(HOVerwaltung.instance().getLanguageString("ScoutHinzu"));
             jbEntfernen.setEnabled(false);
         }
     }
 
     /* Not used anymore
     private void copyFromClipBoard() {
     	try {
 			Clipboard clipboard = getToolkit().getSystemClipboard ();
 			Transferable trans = clipboard.getContents(this);
 			DataFlavor[] dfs = trans.getTransferDataFlavors();
 			for (int m=0; m<dfs.length; m++) {
 				System.out.println(dfs[m].getHumanPresentableName() + " / " + dfs[m].getMimeType() + " - " + dfs[m].getPrimaryType() + " - " + dfs[m].getSubType());
 				if ((trans != null) && (trans.isDataFlavorSupported (dfs[m]))) {
 					String tempString = "" + trans.getTransferData(dfs[m]); //DataFlavor.stringFlavor);
 					System.out.println(tempString);
 					System.out.println("---------------------------------------------------------------------------");
 				}
 			}
 //			if ((trans != null) && (trans.isDataFlavorSupported (DataFlavor.stringFlavor))) {
 //				String tempString;
 //				tempString = (String) trans.getTransferData(DataFlavor.stringFlavor);
 //				System.out.println(tempString);
 //			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     */
 
     /**
      * Calls playerconverter and fills boxes to the corresponding values
      */
     private void copyPaste() {
         String message = "";
         final PlayerConverter pc = new PlayerConverter();
 
         try {
             final Player player = pc.build(jtaCopyPaste.getText());
 
             if (player != null) {
                 jtfPlayerID.setText(player.getPlayerID() + "");
                 jtfName.setText(player.getPlayerName());
                 jtfAlter.setText(player.getAge() + "." + player.getAgeDays());
                 jtfPrice.setText(player.getPrice() + "");
                 jtfTSI.setText(player.getTSI() + "");
                 jtaNotizen.setText(player.getInfo());
 
                 jcbSpeciality.removeItemListener(this);
                 Helper.markierenComboBox(jcbSpeciality,player.getSpeciality());
                 jcbSpeciality.addItemListener(this);
                 jcbErfahrung.removeItemListener(this);
                 Helper.markierenComboBox(jcbErfahrung,player.getExperience());
                 jcbErfahrung.addItemListener(this);
                 jcbForm.removeItemListener(this);
                 Helper.markierenComboBox(jcbForm, player.getForm());
                 jcbForm.addItemListener(this);
                 jcbKondition.removeItemListener(this);
                 Helper.markierenComboBox(jcbKondition,player.getStamina());
                 jcbKondition.addItemListener(this);
                 jcbVerteidigung.removeItemListener(this);
                 Helper.markierenComboBox(jcbVerteidigung,player.getDefense());
                 jcbVerteidigung.addItemListener(this);
                 jcbTorschuss.removeItemListener(this);
                 Helper.markierenComboBox(jcbTorschuss, player.getAttack());
                 jcbTorschuss.addItemListener(this);
                 jcbTorwart.removeItemListener(this);
                 Helper.markierenComboBox(jcbTorwart,player.getGoalKeeping());
                 jcbTorwart.addItemListener(this);
                 jcbFluegel.removeItemListener(this);
                 Helper.markierenComboBox(jcbFluegel, player.getWing());
                 jcbFluegel.addItemListener(this);
                 jcbPasspiel.removeItemListener(this);
                 Helper.markierenComboBox(jcbPasspiel, player.getPassing());
                 jcbPasspiel.addItemListener(this);
                 jcbStandard.removeItemListener(this);
                 Helper.markierenComboBox(jcbStandard,player.getSetPieces());
                 jcbStandard.addItemListener(this);
 
                 // Listener stays here for recalculation of rating
                 Helper.markierenComboBox(jcbSpielaufbau,player.getPlayMaking());
 
                 // Normally not working. Thus last positioned
                 final java.text.SimpleDateFormat simpleFormat = new java.text.SimpleDateFormat("dd.MM.yy HH:mm",
                                                                                                java.util.Locale.GERMANY);
                 final java.util.Date date = simpleFormat.parse(player.getExpiryDate() + " "
                                                                + player.getExpiryTime());
                 jsSpinner.setValue(date);
 
                 setLabels();
             }
         } catch (Exception e) {
         	HOLogger.instance().debug(getClass(), e);
             message = HOVerwaltung.instance().getLanguageString("scout_error");
         }
 
         jtaCopyPaste.setText("");
 
         if (message.equals("")) {
             switch (pc.getError()) {
                 case 1:
                     message = HOVerwaltung.instance().getLanguageString("scout_warning");
                     break;
 
                 case 2:
                     message = HOVerwaltung.instance().getLanguageString("scout_error");
                     break;
 
                 default:
                     message = HOVerwaltung.instance().getLanguageString("scout_success");
             }
         }
 
         jlStatus.setText(HOVerwaltung.instance().getLanguageString("scout_status") + ": "
                          + message);
     }
 
     /**
      * Create components on the panel with default values
      */
     private void initComponents() {
         final GridBagLayout layout = new GridBagLayout();
         final GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.NONE;
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.insets = new Insets(4, 4, 4, 4);
 
         JPanel panel;
         JLabel label;
 
         setLayout(layout);
 
         // Entries
         panel = new ImagePanel();
         panel.setLayout(new GridLayout(9, 4, 4, 4));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("ID"));
         panel.add(label);
         jtfPlayerID.setHorizontalAlignment(JLabel.RIGHT);
         jtfPlayerID.addFocusListener(this);
         jtfPlayerID.addKeyListener(this);
         panel.add(jtfPlayerID);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Name"));
         panel.add(label);
         jtfName.addFocusListener(this);
         panel.add(jtfName);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Alter"));
         panel.add(label);
         jtfAlter.setHorizontalAlignment(JLabel.RIGHT);
         jtfAlter.addFocusListener(this);
         panel.add(jtfAlter);
 
         label = new JLabel("TSI");
         panel.add(label);
         jtfTSI.setHorizontalAlignment(JLabel.RIGHT);
         jtfTSI.addFocusListener(this);
         panel.add(jtfTSI);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("scout_price"));
         panel.add(label);
         jtfPrice.setHorizontalAlignment(JLabel.RIGHT);
         jtfPrice.addFocusListener(this);
         panel.add(jtfPrice);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Ablaufdatum"));
         panel.add(label);
         jsSpinner.addFocusListener(this);
         panel.add(jsSpinner);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Spezialitaet"));
         panel.add(label);
         jcbSpeciality.addItemListener(this);
         panel.add(jcbSpeciality);
 
 		label = new JLabel("EPV");
 		panel.add(label);
 		panel.add(jtfEPV);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.experience"));
         panel.add(label);
         jcbErfahrung.addItemListener(this);
         panel.add(jcbErfahrung);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Form"));
         panel.add(label);
         jcbForm.addItemListener(this);
         panel.add(jcbForm);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.stamina"));
         panel.add(label);
         jcbKondition.addItemListener(this);
         panel.add(jcbKondition);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.keeper"));
         panel.add(label);
         jcbTorwart.addItemListener(this);
         panel.add(jcbTorwart);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.playmaking"));
         panel.add(label);
         jcbSpielaufbau.addItemListener(this);
         panel.add(jcbSpielaufbau);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.passing"));
         panel.add(label);
         jcbPasspiel.addItemListener(this);
         panel.add(jcbPasspiel);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.winger"));
         panel.add(label);
         jcbFluegel.addItemListener(this);
         panel.add(jcbFluegel);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.defending"));
         panel.add(label);
         jcbVerteidigung.addItemListener(this);
         panel.add(jcbVerteidigung);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.scoring"));
         panel.add(label);
         jcbTorschuss.addItemListener(this);
         panel.add(jcbTorschuss);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.set_pieces"));
         panel.add(label);
         jcbStandard.addItemListener(this);
         panel.add(jcbStandard);
 
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.anchor = GridBagConstraints.NORTH;
         layout.setConstraints(panel, constraints);
         add(panel);
 
         // Notes
         panel = new ImagePanel();
         panel.setLayout(new BorderLayout());
         panel.setBorder(javax.swing.BorderFactory.createTitledBorder(HOVerwaltung.instance().getLanguageString("Notizen")));
         jtaNotizen.addFocusListener(this);
         panel.add(new JScrollPane(jtaNotizen), BorderLayout.CENTER);
 
         constraints.fill = GridBagConstraints.BOTH;
         constraints.gridx = 0;
         constraints.gridy = 1;
         constraints.anchor = GridBagConstraints.NORTH;
         layout.setConstraints(panel, constraints);
         add(panel);
 
         // Copy & Paste
         panel = new ImagePanel();
         panel.setLayout(new BorderLayout());
         panel.setBorder(javax.swing.BorderFactory.createTitledBorder(HOVerwaltung.instance().getLanguageString("CopyPaste")));
         jtaCopyPaste.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Transferscout_CopyPaste"));
         panel.add(new JScrollPane(jtaCopyPaste), BorderLayout.NORTH);
         jbUebernehmen.addActionListener(this);
         //jbClipBoard.addActionListener(this);
         layout.setConstraints(jbUebernehmen, constraints);
         panel.add(jbUebernehmen, BorderLayout.CENTER);
         //panel.add(jbClipBoard, BorderLayout.CENTER);
         panel.add(jlStatus, BorderLayout.SOUTH);
 
         constraints.fill = GridBagConstraints.BOTH;
         constraints.gridx = 0;
         constraints.gridy = 2;
         constraints.anchor = GridBagConstraints.NORTH;
         layout.setConstraints(panel, constraints);
 
         add(panel);
 
         // Player values
         panel = new ImagePanel();
         panel.setLayout(new GridLayout(20, 2, 4, 4));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("BestePosition"));
         panel.add(label);
         panel.add(jpBestPos.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Torwart"));
         panel.add(label);
         panel.add(jpWertTor.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Innenverteidiger"));
         panel.add(label);
         panel.add(jpWertInnenVert.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Innenverteidiger_Aus"));
         panel.add(label);
         panel.add(jpWertInnenVertAus.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Innenverteidiger_Off"));
         panel.add(label);
         panel.add(jpWertInnenVertOff.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Aussenverteidiger"));
         panel.add(label);
         panel.add(jpWertAussenVert.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Aussenverteidiger_In"));
         panel.add(label);
         panel.add(jpWertAussenVertIn.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Aussenverteidiger_Off"));
         panel.add(label);
         panel.add(jpWertAussenVertOff.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Aussenverteidiger_Def"));
         panel.add(label);
         panel.add(jpWertAussenVertDef.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Mittelfeld"));
         panel.add(label);
         panel.add(jpWertMittelfeld.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Mittelfeld_Aus"));
         panel.add(label);
         panel.add(jpWertMittelfeldAus.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Mittelfeld_Off"));
         panel.add(label);
         panel.add(jpWertMittelfeldOff.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Mittelfeld_Def"));
         panel.add(label);
         panel.add(jpWertMittelfeldDef.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Fluegelspiel"));
         panel.add(label);
         panel.add(jpWertFluegel.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Fluegelspiel_In"));
         panel.add(label);
         panel.add(jpWertFluegelIn.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Fluegelspiel_Off"));
         panel.add(label);
         panel.add(jpWertFluegelOff.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Fluegelspiel_Def"));
         panel.add(label);
         panel.add(jpWertFluegelDef.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Sturm"));
         panel.add(label);
         panel.add(jpWertSturm.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Sturm_Aus"));
         panel.add(label);
         panel.add(jpWertSturmAus.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Sturm_Def"));
         panel.add(label);
         panel.add(jpWertSturmDef.getComponent(false));
 
         constraints.fill = GridBagConstraints.NONE;
         constraints.gridx = 1;
         constraints.gridy = 0;
         constraints.gridheight = 2;
         layout.setConstraints(panel, constraints);
         add(panel);
 
         // Buttons
         panel = new ImagePanel();
         panel.setLayout(new GridLayout(6, 1, 4, 4));
 
         jbHinzufuegen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Transferscout_hinzufuegen"));
         jbHinzufuegen.addActionListener(this);
         panel.add(jbHinzufuegen);
         jbEntfernen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Transferscout_entfernen"));
         jbEntfernen.addActionListener(this);
         jbEntfernen.setEnabled(false);
         panel.add(jbEntfernen);
 		jbRemoveAll.addActionListener(this);
 		jbRemoveAll.setToolTipText(HOVerwaltung.instance().getLanguageString("Scout.tt_RemoveAll"));
 		panel.add(jbRemoveAll);
         jbMiniScout.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Transferscout_Miniscout"));
         jbMiniScout.addActionListener(this);
         panel.add(jbMiniScout);
         jbDrucken.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Transferscout_drucken"));
         jbDrucken.addActionListener(this);
         panel.add(jbDrucken);
         jbAddTempSpieler.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_add_tempspieler"));
         jbAddTempSpieler.addActionListener(this);
         panel.add(jbAddTempSpieler);
 
         constraints.fill = GridBagConstraints.NONE;
         constraints.gridx = 2;
         constraints.gridy = 0;
         constraints.gridheight = 1;
         constraints.anchor = GridBagConstraints.NORTH;
         layout.setConstraints(panel, constraints);
         add(panel);
 
         setScoutEintrag(null);
     }
 }
