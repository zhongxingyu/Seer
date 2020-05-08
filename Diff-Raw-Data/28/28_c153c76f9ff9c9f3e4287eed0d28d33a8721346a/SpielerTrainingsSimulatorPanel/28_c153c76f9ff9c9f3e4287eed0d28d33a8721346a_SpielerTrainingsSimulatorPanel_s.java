 // %857396099:de.hattrickorganizer.gui.playeroverview%
 package de.hattrickorganizer.gui.playeroverview;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import plugins.ISpieler;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.gui.model.CBItem;
 import de.hattrickorganizer.gui.templates.ColorLabelEntry;
 import de.hattrickorganizer.gui.templates.DoppelLabelEntry;
 import de.hattrickorganizer.gui.templates.ImagePanel;
 import de.hattrickorganizer.model.EPVData;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.Spieler;
 import de.hattrickorganizer.model.SpielerPosition;
 import de.hattrickorganizer.tools.Helper;
 
 
 /**
  * Hier kann ein Spieler erstellt werden, bzw die Parameter verändert werden, um sich die
  * Änderungen anzeigen zu lassen
  */
 final class SpielerTrainingsSimulatorPanel extends ImagePanel
     implements de.hattrickorganizer.gui.Refreshable, ItemListener, ActionListener, FocusListener
 {
 
 	private static final long serialVersionUID = 7657564758631332932L;
 	
 	//~ Static fields/initializers -----------------------------------------------------------------
 
     private static Dimension CBSIZE = new Dimension(Helper.calcCellWidth(120),
                                                     Helper.calcCellWidth(25));
     private static Dimension PFEILSIZE = new Dimension(20, 20);
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     private final ColorLabelEntry m_jpBestPos = new ColorLabelEntry("", ColorLabelEntry.FG_STANDARD,
                                                               ColorLabelEntry.BG_STANDARD,
                                                              SwingConstants.LEFT);
     private final DoppelLabelEntry m_jpWertAussenVert = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertAussenVertDef = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertAussenVertIn = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertAussenVertOff = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertFluegel = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertFluegelDef = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertFluegelIn = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertFluegelOff = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertInnenVert = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertInnenVertAus = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertInnenVertOff = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertMittelfeld = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertMittelfeldAus = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertMittelfeldDef = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertMittelfeldOff = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertSturm = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertSturmAus = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertSturmDef = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERSUBPOSITONSWERTE);
     private final DoppelLabelEntry m_jpWertTor = new DoppelLabelEntry(ColorLabelEntry.BG_SPIELERPOSITONSWERTE);
     private final ColorLabelEntry m_jpEPV = new ColorLabelEntry("",
 									            ColorLabelEntry.FG_STANDARD,
 									            ColorLabelEntry.BG_STANDARD,
 									            SwingConstants.RIGHT);
     private final JButton m_jbAddTempSpieler = new JButton(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("AddTempspieler"));
     private final  JButton m_jbRemoveTempSpieler = new JButton(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("RemoveTempspieler"));
     private final JComboBox m_jcbErfahrung = new JComboBox(Helper.EINSTUFUNG);
     private final JComboBox m_jcbFluegel = new JComboBox(Helper.EINSTUFUNG);
     private final JComboBox m_jcbForm = new JComboBox(Helper.EINSTUFUNG_FORM);
     private final JComboBox m_jcbKondition = new JComboBox(Helper.EINSTUFUNG_KONDITION);
     private final JComboBox m_jcbPasspiel = new JComboBox(Helper.EINSTUFUNG);
     private final JComboBox m_jcbSpielaufbau = new JComboBox(Helper.EINSTUFUNG);
     private final JComboBox m_jcbStandard = new JComboBox(Helper.EINSTUFUNG);
     private final JComboBox m_jcbTorschuss = new JComboBox(Helper.EINSTUFUNG);
     private final JComboBox m_jcbTorwart = new JComboBox(Helper.EINSTUFUNG);
     private final JComboBox m_jcbVerteidigung = new JComboBox(Helper.EINSTUFUNG);
 	private final JComboBox m_jcbSpeciality = new JComboBox(Helper.EINSTUFUNG_SPECIALITY);    
 	private JTextField jtfAge = new JTextField("17.0");	    
     private final JLabel m_jlErfahrung = new JLabel();
     private final JLabel m_jlFluegel = new JLabel();
     private final JLabel m_jlForm = new JLabel();
     private final JLabel m_jlKondition = new JLabel();
     private final JLabel m_jlName = new JLabel();
     private final JLabel m_jlPasspiel = new JLabel();
     private final JLabel m_jlSpielaufbau = new JLabel();
     private final JLabel m_jlStandard = new JLabel();
     private final JLabel m_jlTorschuss = new JLabel();
     private final JLabel m_jlTorwart = new JLabel();
     private final JLabel m_jlVerteidigung = new JLabel();
     private Spieler m_clSpieler;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new SpielerTrainingsSimulatorPanel object.
      */
     protected SpielerTrainingsSimulatorPanel() {
         initComponents();
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @param spieler TODO Missing Method Parameter Documentation
      */
     public final void setSpieler(Spieler spieler) {
         m_clSpieler = spieler;
 
         if (spieler != null) {
             setLabels();
             setCBs();
 
             //Remove für Tempspieler
             if (spieler.getSpielerID() < 0) {
                 m_jbRemoveTempSpieler.setEnabled(true);
             } else {
                 m_jbRemoveTempSpieler.setEnabled(false);
             }
         } else {
             resetLabels();
             resetCBs();
             m_jbRemoveTempSpieler.setEnabled(false);
         }
 
         invalidate();
         validate();
         repaint();
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param e TODO Missing Method Parameter Documentation
      */
     public final void actionPerformed(ActionEvent e) {
         if (e.getSource().equals(m_jbAddTempSpieler)) {
             final Spieler tempSpieler = new Spieler();
             tempSpieler.setNationalitaet(HOVerwaltung.instance().getModel().getBasics().getLand());
             tempSpieler.setSpielerID(de.hattrickorganizer.gui.transferscout.TransferEingabePanel
                                      .getNextTempSpielerID());
            tempSpieler.setName("Temp " + Math.abs(tempSpieler.getSpielerID()));
 			tempSpieler.setAlter(getAge());
 			tempSpieler.setAgeDays(getAgeDays());
             tempSpieler.setErfahrung(((CBItem) m_jcbErfahrung.getSelectedItem()).getId());
             tempSpieler.setForm(((CBItem) m_jcbForm.getSelectedItem()).getId());
             tempSpieler.setKondition(((CBItem) m_jcbKondition.getSelectedItem()).getId());
             tempSpieler.setVerteidigung(((CBItem) m_jcbVerteidigung.getSelectedItem()).getId());
             tempSpieler.setSpezialitaet(((CBItem) m_jcbSpeciality.getSelectedItem()).getId());
             tempSpieler.setTorschuss(((CBItem) m_jcbTorschuss.getSelectedItem()).getId());
             tempSpieler.setTorwart(((CBItem) m_jcbTorwart.getSelectedItem()).getId());
             tempSpieler.setFluegelspiel(((CBItem) m_jcbFluegel.getSelectedItem()).getId());
             tempSpieler.setPasspiel(((CBItem) m_jcbPasspiel.getSelectedItem()).getId());
             tempSpieler.setStandards(((CBItem) m_jcbStandard.getSelectedItem()).getId());
             tempSpieler.setSpielaufbau(((CBItem) m_jcbSpielaufbau.getSelectedItem()).getId());
             
 
             HOVerwaltung.instance().getModel().addSpieler(tempSpieler);
             de.hattrickorganizer.gui.RefreshManager.instance().doReInit();
             de.hattrickorganizer.gui.HOMainFrame.instance().showTab(de.hattrickorganizer.gui.HOMainFrame.SPIELERUEBERSICHT);
         } else if (e.getSource().equals(m_jbRemoveTempSpieler)) {
             HOVerwaltung.instance().getModel().removeSpieler(m_clSpieler);
             de.hattrickorganizer.gui.RefreshManager.instance().doReInit();
             de.hattrickorganizer.gui.HOMainFrame.instance().showTab(de.hattrickorganizer.gui.HOMainFrame.SPIELERUEBERSICHT);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param itemEvent TODO Missing Method Parameter Documentation
      */
     public final void itemStateChanged(java.awt.event.ItemEvent itemEvent) {
         if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
             if (m_clSpieler != null) {
                 setLabels();
             } else {
                 resetLabels();
             }
         }
 
         //        invalidate();
         //        validate();            
         //        repaint();
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public final void reInit() {
         setSpieler(null);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public final void refresh() {
         setSpieler(null);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void setCBs() {
         m_jlName.setText(m_clSpieler.getName());
 		jtfAge.setText(m_clSpieler.getAlter()+"."+m_clSpieler.getAgeDays());
         Helper.markierenComboBox(m_jcbForm, m_clSpieler.getForm());
         Helper.markierenComboBox(m_jcbErfahrung,
                                                             m_clSpieler.getErfahrung());
         Helper.markierenComboBox(m_jcbKondition,
                                                             m_clSpieler.getKondition());
         Helper.markierenComboBox(m_jcbSpielaufbau,
                                                             m_clSpieler.getSpielaufbau());
         Helper.markierenComboBox(m_jcbFluegel,
                                                             m_clSpieler.getFluegelspiel());
         Helper.markierenComboBox(m_jcbTorschuss,
                                                             m_clSpieler.getTorschuss());
         Helper.markierenComboBox(m_jcbTorwart, m_clSpieler.getTorwart());
         Helper.markierenComboBox(m_jcbPasspiel, m_clSpieler.getPasspiel());
         Helper.markierenComboBox(m_jcbVerteidigung,
                                                             m_clSpieler.getVerteidigung());
 		Helper.markierenComboBox(m_jcbSpeciality,
 															m_clSpieler.getSpezialitaet());
                                                             
         Helper.markierenComboBox(m_jcbStandard,
                                                             m_clSpieler.getStandards());
 
         m_jlForm.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlKondition.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlErfahrung.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlSpielaufbau.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlFluegel.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlTorschuss.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlTorwart.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlPasspiel.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlVerteidigung.setIcon(Helper.getImageIcon4Veraenderung(0));        
         m_jlStandard.setIcon(Helper.getImageIcon4Veraenderung(0));
 
         m_jcbForm.setEnabled(true);
         m_jcbKondition.setEnabled(true);
         m_jcbSpielaufbau.setEnabled(true);
         m_jcbFluegel.setEnabled(true);
         m_jcbTorschuss.setEnabled(true);
         m_jcbTorwart.setEnabled(true);
         m_jcbPasspiel.setEnabled(true);
         m_jcbVerteidigung.setEnabled(true);
 		m_jcbSpeciality.setEnabled(true);
         m_jcbStandard.setEnabled(true);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void setLabels() {
         final Spieler tempSpieler = new Spieler();
         tempSpieler.setForm(((CBItem) m_jcbForm.getSelectedItem()).getId());
         tempSpieler.setErfahrung(((CBItem) m_jcbErfahrung.getSelectedItem()).getId());
         tempSpieler.setKondition(((CBItem) m_jcbKondition.getSelectedItem()).getId());
         tempSpieler.setVerteidigung(((CBItem) m_jcbVerteidigung.getSelectedItem()).getId());
 		tempSpieler.setSpezialitaet(((CBItem) m_jcbSpeciality.getSelectedItem()).getId());
         tempSpieler.setTorschuss(((CBItem) m_jcbTorschuss.getSelectedItem()).getId());
         tempSpieler.setTorwart(((CBItem) m_jcbTorwart.getSelectedItem()).getId());
         tempSpieler.setFluegelspiel(((CBItem) m_jcbFluegel.getSelectedItem()).getId());
         tempSpieler.setPasspiel(((CBItem) m_jcbPasspiel.getSelectedItem()).getId());
         tempSpieler.setStandards(((CBItem) m_jcbStandard.getSelectedItem()).getId());
         tempSpieler.setSpielaufbau(((CBItem) m_jcbSpielaufbau.getSelectedItem()).getId());
 
         m_jlForm.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getForm()- m_clSpieler.getForm()));
         m_jlKondition.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getKondition()- m_clSpieler.getKondition()));
         m_jlErfahrung.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getErfahrung()- m_clSpieler.getErfahrung()));
         m_jlSpielaufbau.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getSpielaufbau()- m_clSpieler.getSpielaufbau()));
         m_jlFluegel.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getFluegelspiel()- m_clSpieler.getFluegelspiel()));
         m_jlTorschuss.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getTorschuss()- m_clSpieler.getTorschuss()));
         m_jlTorwart.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getTorwart()- m_clSpieler.getTorwart()));
         m_jlPasspiel.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getPasspiel()- m_clSpieler.getPasspiel()));
         m_jlVerteidigung.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getVerteidigung()- m_clSpieler.getVerteidigung()));
         m_jlStandard.setIcon(Helper.getImageIcon4Veraenderung(tempSpieler.getStandards()- m_clSpieler.getStandards()));
 
         m_jpBestPos.setText(SpielerPosition.getNameForPosition(tempSpieler.getIdealPosition())
                             + " (" + tempSpieler.calcPosValue(tempSpieler.getIdealPosition(), true)
                             + ")");
         m_jpWertTor.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.KEEPER,true),
                                                                                gui.UserParameter
                                                                                .instance().anzahlNachkommastellen)+ "");
         m_jpWertTor.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.KEEPER,true)
                                                  - m_clSpieler.calcPosValue(ISpielerPosition.KEEPER,true), false);
         m_jpWertInnenVert.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER,true),
                                                                                      gui.UserParameter
                                                                                      .instance().anzahlNachkommastellen)+ "");
         m_jpWertInnenVert.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER,true)
                                                        - m_clSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER,true), false);
         m_jpWertInnenVertAus.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_TOWING,
                                                                                                       true),
                                                                                         gui.UserParameter
                                                                                         .instance().anzahlNachkommastellen)+ "");
         m_jpWertInnenVertAus.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_TOWING,
                                                                                    true)
                                                           - m_clSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_TOWING,
                                                                                      true), false);
         m_jpWertInnenVertOff.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_OFF,
                                                                                                       true),
                                                                                         gui.UserParameter
                                                                                         .instance().anzahlNachkommastellen)
                                                 + "");
         m_jpWertInnenVertOff.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_OFF,
                                                                                    true)
                                                           - m_clSpieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_OFF,
                                                                                      true), false);
         m_jpWertAussenVert.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                       .calcPosValue(ISpielerPosition.BACK,
                                                                                                     true),
                                                                                       gui.UserParameter
                                                                                       .instance().anzahlNachkommastellen)
                                               + "");
         m_jpWertAussenVert.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.BACK,
                                                                                  true)
                                                         - m_clSpieler.calcPosValue(ISpielerPosition.BACK,
                                                                                    true), false);
         m_jpWertAussenVertIn.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.BACK_TOMID,
                                                                                                       true),
                                                                                         gui.UserParameter
                                                                                         .instance().anzahlNachkommastellen)
                                                 + "");
         m_jpWertAussenVertIn.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.BACK_TOMID,
                                                                                    true)
                                                           - m_clSpieler.calcPosValue(ISpielerPosition.BACK_TOMID,
                                                                                      true), false);
         m_jpWertAussenVertOff.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.BACK_OFF,
                                                                                                        true),
                                                                                          gui.UserParameter
                                                                                          .instance().anzahlNachkommastellen)
                                                  + "");
         m_jpWertAussenVertOff.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.BACK_OFF,
                                                                                     true)
                                                            - m_clSpieler.calcPosValue(ISpielerPosition.BACK_OFF,
                                                                                       true), false);
         m_jpWertAussenVertDef.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.BACK_DEF,
                                                                                                        true),
                                                                                          gui.UserParameter
                                                                                          .instance().anzahlNachkommastellen)
                                                  + "");
         m_jpWertAussenVertDef.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.BACK_DEF,
                                                                                     true)
                                                            - m_clSpieler.calcPosValue(ISpielerPosition.BACK_DEF,
                                                                                       true), false);
         m_jpWertMittelfeld.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                       .calcPosValue(ISpielerPosition.MIDFIELDER,
                                                                                                     true),
                                                                                       gui.UserParameter
                                                                                       .instance().anzahlNachkommastellen)
                                               + "");
         m_jpWertMittelfeld.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.MIDFIELDER,
                                                                                  true)
                                                         - m_clSpieler.calcPosValue(ISpielerPosition.MIDFIELDER,
                                                                                    true), false);
         m_jpWertMittelfeldAus.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.MIDFIELDER_TOWING,
                                                                                                        true),
                                                                                          gui.UserParameter
                                                                                          .instance().anzahlNachkommastellen)
                                                  + "");
         m_jpWertMittelfeldAus.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.MIDFIELDER_TOWING,
                                                                                     true)
                                                            - m_clSpieler.calcPosValue(ISpielerPosition.MIDFIELDER_TOWING,
                                                                                       true), false);
         m_jpWertMittelfeldOff.getLinks().setText(de.hattrickorganizer.tools.Helper.round(tempSpieler
                                                                                          .calcPosValue(ISpielerPosition.MIDFIELDER_OFF,
                                                                                                        true),
                                                                                          gui.UserParameter
                                                                                          .instance().anzahlNachkommastellen)
                                                  + "");
         m_jpWertMittelfeldOff.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.MIDFIELDER_OFF,
                                                                                     true)
                                                            - m_clSpieler.calcPosValue(ISpielerPosition.MIDFIELDER_OFF,
                                                                                       true), false);
         m_jpWertMittelfeldDef.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.MIDFIELDER_DEF,
                                                                                                        true),
                                                                                          gui.UserParameter
                                                                                          .instance().anzahlNachkommastellen)
                                                  + "");
         m_jpWertMittelfeldDef.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.MIDFIELDER_DEF,
                                                                                     true)
                                                            - m_clSpieler.calcPosValue(ISpielerPosition.MIDFIELDER_DEF,
                                                                                       true), false);
         m_jpWertFluegel.getLinks().setText(Helper.round(tempSpieler
                                                                                    .calcPosValue(ISpielerPosition.WINGER,
                                                                                                  true),
                                                                                    gui.UserParameter
                                                                                    .instance().anzahlNachkommastellen)
                                            + "");
         m_jpWertFluegel.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.WINGER,
                                                                               true)
                                                      - m_clSpieler.calcPosValue(ISpielerPosition.WINGER,
                                                                                 true), false);
         m_jpWertFluegelIn.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.WINGER_TOMID,
                                                                                                    true),
                                                                                      gui.UserParameter
                                                                                      .instance().anzahlNachkommastellen)
                                              + "");
         m_jpWertFluegelIn.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.WINGER_TOMID,
                                                                                 true)
                                                        - m_clSpieler.calcPosValue(ISpielerPosition.WINGER_TOMID,
                                                                                   true), false);
         m_jpWertFluegelOff.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.WINGER_OFF,
                                                                                                     true),
                                                                                       gui.UserParameter
                                                                                       .instance().anzahlNachkommastellen)
                                               + "");
         m_jpWertFluegelOff.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.WINGER_OFF,
                                                                                  true)
                                                         - m_clSpieler.calcPosValue(ISpielerPosition.WINGER_OFF,
                                                                                    true), false);
         m_jpWertFluegelDef.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.WINGER_DEF,
                                                                                                     true),
                                                                                       gui.UserParameter
                                                                                       .instance().anzahlNachkommastellen)
                                               + "");
         m_jpWertFluegelDef.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.WINGER_DEF,
                                                                                  true)
                                                         - m_clSpieler.calcPosValue(ISpielerPosition.WINGER_DEF,
                                                                                    true), false);
         m_jpWertSturm.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.FORWARD,
                                                                                                true),
                                                                                  gui.UserParameter
                                                                                  .instance().anzahlNachkommastellen)
                                          + "");
         m_jpWertSturm.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.FORWARD,
                                                                             true)
                                                    - m_clSpieler.calcPosValue(ISpielerPosition.FORWARD,
                                                                               true), false);
         
         m_jpWertSturmAus.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.FORWARD_TOWING,
 														        			true),
 														  gui.UserParameter
 														  .instance().anzahlNachkommastellen)
 														+ "");
         m_jpWertSturmAus.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.FORWARD_TOWING,
                 																				true)
                 																				- m_clSpieler.calcPosValue(ISpielerPosition.FORWARD_TOWING,
                 																						true), false);
         m_jpWertSturmDef.getLinks().setText(Helper.round(tempSpieler.calcPosValue(ISpielerPosition.FORWARD_DEF,
                                                                                                   true),
                                                                                     gui.UserParameter
                                                                                     .instance().anzahlNachkommastellen)
                                             + "");
         m_jpWertSturmDef.getRechts().setSpezialNumber(tempSpieler.calcPosValue(ISpielerPosition.FORWARD_DEF,
                                                                                true)
                                                       - m_clSpieler.calcPosValue(ISpielerPosition.FORWARD_DEF,
                                                                                  true), false);
 		tempSpieler.setAlter(getAge());
 		tempSpieler.setAgeDays(getAgeDays());
 		tempSpieler.setFuehrung(m_clSpieler.getFuehrung());
 		tempSpieler.setSpezialitaet(m_clSpieler.getSpezialitaet());		        
         EPVData data = new EPVData(tempSpieler);
         double price = HOVerwaltung.instance().getModel().getEPV().getPrice(data);        
         final String epvtext = java.text.NumberFormat.getCurrencyInstance().format(price);
         m_jpEPV.setText( epvtext );        
     }
 
 	private int getAge() {
 		int age = 17;
 		if (m_clSpieler != null) {
 			age = m_clSpieler.getAlter();
 		}
 		try {
 			age = Integer.parseInt(jtfAge.getText().replaceFirst("\\..*", ""));
 		} catch (NumberFormatException e) {
 		}
 		return age;
 	}
 
 	private int getAgeDays() {
 		int age = 0;
 		if (m_clSpieler != null) {
 			age = m_clSpieler.getAgeDays();
 		}
 		try {
 			age = Integer.parseInt(jtfAge.getText().replaceFirst(".*\\.", ""));
 		} catch (NumberFormatException e) {
 		}
 		return age;
 	}
 
     /**
      * TODO Missing Method Documentation
      */
     private void initComponents() {
         final GridBagLayout layout = new GridBagLayout();
         final GridBagConstraints constraints = new GridBagConstraints();
         constraints.anchor = GridBagConstraints.CENTER;
         constraints.fill = GridBagConstraints.NONE;
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.insets = new Insets(2, 2, 2, 2);
 
         JPanel panel;
         JLabel label;
 
         setLayout(layout);
 
         //Eingaben-------
         final GridBagLayout eingabenLayout = new GridBagLayout();
         final GridBagConstraints eingabenconstraints = new GridBagConstraints();
         eingabenconstraints.anchor = GridBagConstraints.WEST;
         eingabenconstraints.fill = GridBagConstraints.NONE;
         eingabenconstraints.weightx = 0.0;
         eingabenconstraints.weighty = 0.0;
         eingabenconstraints.insets = new Insets(4, 4, 4, 4);
 
         panel = new ImagePanel();
         panel.setLayout(eingabenLayout);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Name"));
         eingabenconstraints.gridx = 0;
         eingabenconstraints.gridy = 0;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         eingabenconstraints.gridx = 1;
         eingabenconstraints.gridy = 0;
         eingabenconstraints.gridwidth = 2;
         eingabenLayout.setConstraints(m_jlName, eingabenconstraints);
         panel.add(m_jlName);
 
 		label = new JLabel(HOVerwaltung.instance().getLanguageString("Spezialitaet"));
 		eingabenconstraints.gridx = 0;
 		eingabenconstraints.gridy = 1;
 		eingabenconstraints.gridwidth = 1;
 		eingabenLayout.setConstraints(label, eingabenconstraints);
 		panel.add(label);
 		m_jcbSpeciality.setPreferredSize(CBSIZE);
 		m_jcbSpeciality.addItemListener(this);
 		eingabenconstraints.gridx = 1;
 		eingabenconstraints.gridy = 1;
 		eingabenLayout.setConstraints(m_jcbSpeciality, eingabenconstraints);
 		panel.add(m_jcbSpeciality);
 		
 		label = new JLabel(HOVerwaltung.instance().getLanguageString("Alter"));
 		eingabenconstraints.gridx = 3;
 		eingabenconstraints.gridy = 1;
 		eingabenLayout.setConstraints(label, eingabenconstraints);
 		panel.add(label);
 		jtfAge.setPreferredSize(CBSIZE);
 		jtfAge.addFocusListener(this);
 		eingabenconstraints.gridx = 4;
 		eingabenconstraints.gridy = 1;
 		eingabenLayout.setConstraints(jtfAge, eingabenconstraints);
 		panel.add(jtfAge);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Erfahrung"));
         eingabenconstraints.gridx = 0;
         eingabenconstraints.gridy = 2;
         eingabenconstraints.gridwidth = 1;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbErfahrung.setPreferredSize(CBSIZE);
         m_jcbErfahrung.addItemListener(this);
         eingabenconstraints.gridx = 1;
         eingabenconstraints.gridy = 2;
         eingabenLayout.setConstraints(m_jcbErfahrung, eingabenconstraints);
         panel.add(m_jcbErfahrung);
         eingabenconstraints.gridx = 2;
         eingabenconstraints.gridy = 2;
         m_jlErfahrung.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlErfahrung, eingabenconstraints);
         panel.add(m_jlErfahrung);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Form"));
         eingabenconstraints.gridx = 3;
         eingabenconstraints.gridy = 2;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbForm.setPreferredSize(CBSIZE);
         m_jcbForm.addItemListener(this);
         eingabenconstraints.gridx = 4;
         eingabenconstraints.gridy = 2;
         eingabenLayout.setConstraints(m_jcbForm, eingabenconstraints);
         panel.add(m_jcbForm);
         eingabenconstraints.gridx = 5;
         eingabenconstraints.gridy = 2;
         m_jlForm.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlForm, eingabenconstraints);
         panel.add(m_jlForm);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.stamina"));
         eingabenconstraints.gridx = 0;
         eingabenconstraints.gridy = 3;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbKondition.setPreferredSize(CBSIZE);
         m_jcbKondition.addItemListener(this);
         eingabenconstraints.gridx = 1;
         eingabenconstraints.gridy = 3;
         eingabenLayout.setConstraints(m_jcbKondition, eingabenconstraints);
         panel.add(m_jcbKondition);
         eingabenconstraints.gridx = 2;
         eingabenconstraints.gridy = 3;
         m_jlKondition.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlKondition, eingabenconstraints);
         panel.add(m_jlKondition);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.keeper"));
         eingabenconstraints.gridx = 3;
         eingabenconstraints.gridy = 3;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbTorwart.setPreferredSize(CBSIZE);
         m_jcbTorwart.addItemListener(this);
         eingabenconstraints.gridx = 4;
         eingabenconstraints.gridy = 3;
         eingabenLayout.setConstraints(m_jcbTorwart, eingabenconstraints);
         panel.add(m_jcbTorwart);
         eingabenconstraints.gridx = 5;
         eingabenconstraints.gridy = 3;
         m_jlTorwart.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlTorwart, eingabenconstraints);
         panel.add(m_jlTorwart);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.playmaking"));
         eingabenconstraints.gridx = 0;
         eingabenconstraints.gridy = 4;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbSpielaufbau.setPreferredSize(CBSIZE);
         m_jcbSpielaufbau.addItemListener(this);
         eingabenconstraints.gridx = 1;
         eingabenconstraints.gridy = 4;
         eingabenLayout.setConstraints(m_jcbSpielaufbau, eingabenconstraints);
         panel.add(m_jcbSpielaufbau);
         eingabenconstraints.gridx = 2;
         eingabenconstraints.gridy = 4;
         m_jlSpielaufbau.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlSpielaufbau, eingabenconstraints);
         panel.add(m_jlSpielaufbau);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.passing"));
         eingabenconstraints.gridx = 3;
         eingabenconstraints.gridy = 4;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbPasspiel.setPreferredSize(CBSIZE);
         m_jcbPasspiel.addItemListener(this);
         eingabenconstraints.gridx = 4;
         eingabenconstraints.gridy = 4;
         eingabenLayout.setConstraints(m_jcbPasspiel, eingabenconstraints);
         panel.add(m_jcbPasspiel);
         eingabenconstraints.gridx = 5;
         eingabenconstraints.gridy = 4;
         m_jlPasspiel.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlPasspiel, eingabenconstraints);
         panel.add(m_jlPasspiel);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.winger"));
         eingabenconstraints.gridx = 0;
         eingabenconstraints.gridy = 5;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbFluegel.setPreferredSize(CBSIZE);
         m_jcbFluegel.addItemListener(this);
         eingabenconstraints.gridx = 1;
         eingabenconstraints.gridy = 5;
         eingabenLayout.setConstraints(m_jcbFluegel, eingabenconstraints);
         panel.add(m_jcbFluegel);
         eingabenconstraints.gridx = 2;
         eingabenconstraints.gridy = 5;
         m_jlFluegel.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlFluegel, eingabenconstraints);
         panel.add(m_jlFluegel);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.defending"));
         eingabenconstraints.gridx = 3;
         eingabenconstraints.gridy = 5;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbVerteidigung.setPreferredSize(CBSIZE);
         m_jcbVerteidigung.addItemListener(this);
         eingabenconstraints.gridx = 4;
         eingabenconstraints.gridy = 5;
         eingabenLayout.setConstraints(m_jcbVerteidigung, eingabenconstraints);
         panel.add(m_jcbVerteidigung);
         eingabenconstraints.gridx = 5;
         eingabenconstraints.gridy = 5;
         m_jlVerteidigung.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlVerteidigung, eingabenconstraints);
         panel.add(m_jlVerteidigung);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.scoring"));
         eingabenconstraints.gridx = 0;
         eingabenconstraints.gridy = 6;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbTorschuss.setPreferredSize(CBSIZE);
         m_jcbTorschuss.addItemListener(this);
         eingabenconstraints.gridx = 1;
         eingabenconstraints.gridy = 6;
         eingabenLayout.setConstraints(m_jcbTorschuss, eingabenconstraints);
         panel.add(m_jcbTorschuss);
         eingabenconstraints.gridx = 2;
         eingabenconstraints.gridy = 6;
         m_jlTorschuss.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlTorschuss, eingabenconstraints);
         panel.add(m_jlTorschuss);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("skill.set_pieces"));
         eingabenconstraints.gridx = 3;
         eingabenconstraints.gridy = 6;
         eingabenLayout.setConstraints(label, eingabenconstraints);
         panel.add(label);
         m_jcbStandard.setPreferredSize(CBSIZE);
         m_jcbStandard.addItemListener(this);
         eingabenconstraints.gridx = 4;
         eingabenconstraints.gridy = 6;
         eingabenLayout.setConstraints(m_jcbStandard, eingabenconstraints);
         panel.add(m_jcbStandard);
         eingabenconstraints.gridx = 5;
         eingabenconstraints.gridy = 6;
         m_jlStandard.setPreferredSize(PFEILSIZE);
         eingabenLayout.setConstraints(m_jlStandard, eingabenconstraints);
         panel.add(m_jlStandard);
 
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.anchor = GridBagConstraints.NORTH;
         layout.setConstraints(panel, constraints);
         add(panel);
 
         //Button--------
         panel = new JPanel();
         m_jbAddTempSpieler.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_add_tempspieler"));
         m_jbAddTempSpieler.addActionListener(this);
         panel.add(m_jbAddTempSpieler);
         m_jbRemoveTempSpieler.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_remove_tempspieler"));
         m_jbRemoveTempSpieler.addActionListener(this);
         m_jbRemoveTempSpieler.setEnabled(false);
         panel.add(m_jbRemoveTempSpieler);
 
         constraints.gridx = 0;
         constraints.gridy = 1;
         constraints.anchor = GridBagConstraints.NORTH;
         layout.setConstraints(panel, constraints);
         add(panel);
 
         //Werte---------
         panel = new ImagePanel();
         panel.setLayout(new GridLayout(21, 2, 2, 2));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("BestePosition"));
         panel.add(label);
         panel.add(m_jpBestPos.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Torwart"));
         panel.add(label);
         panel.add(m_jpWertTor.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Innenverteidiger"));
         panel.add(label);
         panel.add(m_jpWertInnenVert.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Innenverteidiger_Aus"));
         panel.add(label);
         panel.add(m_jpWertInnenVertAus.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Innenverteidiger_Off"));
         panel.add(label);
         panel.add(m_jpWertInnenVertOff.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Aussenverteidiger"));
         panel.add(label);
         panel.add(m_jpWertAussenVert.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Aussenverteidiger_In"));
         panel.add(label);
         panel.add(m_jpWertAussenVertIn.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Aussenverteidiger_Off"));
         panel.add(label);
         panel.add(m_jpWertAussenVertOff.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Aussenverteidiger_Def"));
         panel.add(label);
         panel.add(m_jpWertAussenVertDef.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Mittelfeld"));
         panel.add(label);
         panel.add(m_jpWertMittelfeld.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Mittelfeld_Aus"));
         panel.add(label);
         panel.add(m_jpWertMittelfeldAus.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Mittelfeld_Off"));
         panel.add(label);
         panel.add(m_jpWertMittelfeldOff.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Mittelfeld_Def"));
         panel.add(label);
         panel.add(m_jpWertMittelfeldDef.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Fluegel"));
         panel.add(label);
         panel.add(m_jpWertFluegel.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Fluegelspiel_In"));
         panel.add(label);
         panel.add(m_jpWertFluegelIn.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Fluegelspiel_Off"));
         panel.add(label);
         panel.add(m_jpWertFluegelOff.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Fluegelspiel_Def"));
         panel.add(label);
         panel.add(m_jpWertFluegelDef.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Sturm"));
         panel.add(label);
         panel.add(m_jpWertSturm.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Sturm_Aus"));
         panel.add(label);
         panel.add(m_jpWertSturmAus.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Sturm_Def"));
         panel.add(label);
         panel.add(m_jpWertSturmDef.getComponent(false));
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Marktwert"));
         panel.add(label);
         panel.add(m_jpEPV.getComponent(false));
         
         constraints.gridx = 1;
         constraints.gridy = 0;
         constraints.gridheight = 2;
         layout.setConstraints(panel, constraints);
         add(panel);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void resetCBs() {
         m_jlName.setText("");
 		jtfAge.setText("17.0");
         m_jlForm.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlKondition.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlErfahrung.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlSpielaufbau.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlFluegel.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlTorschuss.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlTorwart.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlPasspiel.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlVerteidigung.setIcon(Helper.getImageIcon4Veraenderung(0));
         m_jlStandard.setIcon(Helper.getImageIcon4Veraenderung(0));
 
         resetCB(m_jcbForm);
         resetCB(m_jcbErfahrung);
         resetCB(m_jcbKondition);
         resetCB(m_jcbSpielaufbau);
         resetCB(m_jcbFluegel);
         resetCB(m_jcbTorschuss);
         resetCB(m_jcbTorwart);
         resetCB(m_jcbPasspiel);
         resetCB(m_jcbVerteidigung);
         resetCB(m_jcbSpeciality);
         resetCB(m_jcbStandard);
     }
 
     
     private void resetCB(JComboBox cb){
     	Helper.markierenComboBox(cb, ISpieler.katastrophal);
     	cb.setEnabled(false);
     }
     /**
      * TODO Missing Method Documentation
      */
     private void resetLabels() {
         m_jpBestPos.clear();
         m_jpWertTor.clear();
         m_jpWertInnenVert.clear();
         m_jpWertInnenVertAus.clear();
         m_jpWertInnenVertOff.clear();
         m_jpWertAussenVert.clear();
         m_jpWertAussenVertIn.clear();
         m_jpWertAussenVertOff.clear();
         m_jpWertAussenVertDef.clear();
         m_jpWertMittelfeld.clear();
         m_jpWertMittelfeldAus.clear();
         m_jpWertMittelfeldDef.clear();
         m_jpWertMittelfeldOff.clear();
         m_jpWertFluegel.clear();
         m_jpWertFluegelIn.clear();
         m_jpWertFluegelDef.clear();
         m_jpWertFluegelOff.clear();
         m_jpWertSturm.clear();
         m_jpWertSturmAus.clear();
         m_jpWertSturmDef.clear();
         m_jpEPV.clear();
     }
 
 	public void focusGained(FocusEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	public void focusLost(FocusEvent e) {
 		if (e.getSource().equals(jtfAge)) {
 			if (m_clSpieler != null) {
 				setLabels();
 			} else {
 				resetLabels();
 			}
 
 		}		
 	}
 }
