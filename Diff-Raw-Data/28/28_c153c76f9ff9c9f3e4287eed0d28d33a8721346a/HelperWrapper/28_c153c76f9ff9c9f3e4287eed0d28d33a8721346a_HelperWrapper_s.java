 // %2519751489:de.hattrickorganizer.tools%
 /*
  * HelperWrapper.java
  *
  * Created on 29. März 2004, 08:10
  */
 package de.hattrickorganizer.tools;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 import plugins.IHOMiniModel;
 import plugins.IHTCalendar;
 import plugins.IMP3Player;
 import plugins.IMatchHelper;
 import plugins.IMatchKurzInfo;
 import plugins.IPlugin;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.database.DBZugriff;
 import de.hattrickorganizer.gui.HOMainFrame;
 import de.hattrickorganizer.gui.menu.HRFImport;
 import de.hattrickorganizer.gui.transferscout.Player;
 import de.hattrickorganizer.gui.transferscout.PlayerConverter;
 import de.hattrickorganizer.gui.transferscout.TransferEingabePanel;
 import de.hattrickorganizer.logik.MatchUpdater;
 import de.hattrickorganizer.model.HOMiniModel;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.Spieler;
 import de.hattrickorganizer.model.SpielerPosition;
 import de.hattrickorganizer.model.Team;
 import de.hattrickorganizer.model.matches.MatchHelper;
 import de.hattrickorganizer.model.matches.MatchKurzInfo;
 import de.hattrickorganizer.model.matches.MatchLineup;
 import de.hattrickorganizer.model.matches.Matchdetails;
 import de.hattrickorganizer.net.MyConnector;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author thomas.werth
  */
 public class HelperWrapper implements plugins.IHelper {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     private static HelperWrapper m_clInstance;
     final static long WEEK = 24 * 7 * 3600 * 1000L;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new instance of HelperWrapper
      */
     private HelperWrapper() {
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public static HelperWrapper instance() {
         if (m_clInstance == null) {
             m_clInstance = new HelperWrapper();
         }
 
         return m_clInstance;
     }
 
 	/**
 	 * Returns a match helper instance
 	 * @return	an instance of the match helper class
 	 */
 	public IMatchHelper getMatchHelper() {
 		return MatchHelper.instance();
 	}
 
     /**
      * TODO Missing Method Documentation
      *
      * @param typ TODO Missing Method Parameter Documentation
      * @param subtyp TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public java.awt.Color getColor4SpielHighlight(int typ, int subtyp) {
         return Helper.getColor4SpielHighlight(typ, subtyp);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param string TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      *
      * @throws ParseException TODO Missing Method Exception Documentation
      */
     public Date getHattrickDate(String string) throws ParseException {
         final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.UK);
         final Date d = sdf.parse(string);
         final String hh = string.substring(11, 13);
 
         if (hh.equalsIgnoreCase("12")) {
             final Calendar c = Calendar.getInstance(Locale.UK);
             c.setFirstDayOfWeek(Calendar.SUNDAY);
             c.setTime(d);
             c.add(Calendar.HOUR_OF_DAY, 12);
             return c.getTime();
         }
 
         return d;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param posid TODO Missing Method Parameter Documentation
      * @param taktik TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public ImageIcon getImage4Position(int posid, byte taktik) {
         return Helper.getImage4Position(posid, taktik, 0);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param country TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public ImageIcon getImageIcon4Country(int country) {
         return Helper.getImageIcon4Country(country);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param wert TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public ImageIcon getImageIcon4Spezialitaet(int wert) {
         return Helper.getImageIcon4Spezialitaet(wert);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param typ TODO Missing Method Parameter Documentation
      * @param subtyp TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public ImageIcon getImageIcon4SpielHighlight(int typ, int subtyp) {
         return Helper.getImageIcon4SpielHighlight(typ, subtyp);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param spieltyp TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public ImageIcon getImageIcon4Spieltyp(int spieltyp) {
         return Helper.getImageIcon4Spieltyp(spieltyp);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param wert TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public ImageIcon getImageIcon4Veraenderung(int wert) {
         return Helper.getImageIcon4Veraenderung(wert);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param wert TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public ImageIcon getImageIcon4Wetter(int wert) {
         return Helper.getImageIcon4Wetter(wert);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param wert TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public ImageIcon getImageIcon4WetterEffekt(int wert) {
         return Helper.getImageIcon4WetterEffekt(wert);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public int getLanguageID() {
         final String id = HOVerwaltung.instance().getLanguageString("LanguageID");
 
         try {
             return Integer.parseInt(id);
         } catch (Exception e) {
             return -1;
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getLanguageName() {
         final java.io.File sprachdatei = new java.io.File("sprache/languages.properties");
 
         if (sprachdatei.exists()) {
             try {
                 final java.util.Properties temp = new java.util.Properties();
                 temp.load(new java.io.FileInputStream(sprachdatei));
                 return temp.getProperty(getLanguageID() + "");
             } catch (Exception e) {
             }
         }
 
         return "Unknown";
     }
 
     /**
      * Calculate the last valid training date for a certain date (skillupDate)
      *
      * @param skillupDate the skillupdate or HRF date
      * @param refTrainingDate a reference containing a valid training time and day of week
      *
      * @return the last valid training date for the given 'skillupDate'
      */
     public Calendar getLastTrainingDate(Date skillupDate, Date refTrainingDate) {
         // Calendar for TrainingDate
         final Calendar trDate = Calendar.getInstance(Locale.US);
 
         // IF refTrainingDate is null first run of HO
         if (refTrainingDate == null) {
             return trDate;
         }
 
         trDate.setTime(refTrainingDate);
         trDate.setFirstDayOfWeek(Calendar.SUNDAY);
         trDate.setLenient(true);
         trDate.setMinimalDaysInFirstWeek(1);
 
         // Calendar for Skillup Date
         final Calendar suDate = Calendar.getInstance(Locale.US);
         suDate.setFirstDayOfWeek(Calendar.SUNDAY);
         suDate.setMinimalDaysInFirstWeek(1);
         suDate.setLenient(true);
         suDate.setTime(skillupDate);
 
         // Move TrainingDate back to skillup week
         trDate.set(Calendar.YEAR, suDate.get(Calendar.YEAR));
         trDate.set(Calendar.WEEK_OF_YEAR, suDate.get(Calendar.WEEK_OF_YEAR));
 
         // Check that is fine
         long diff = suDate.getTimeInMillis() - trDate.getTimeInMillis();
 
         // training date must be within one week (handle end of year)
         while (diff > WEEK) {
         	trDate.add(Calendar.WEEK_OF_YEAR, +1);
         	diff = suDate.getTimeInMillis() - trDate.getTimeInMillis();
         }
 
         // training date must not be after skillup date
         while (diff != 0 && trDate.after(suDate)) {
         	trDate.add(Calendar.WEEK_OF_YEAR, -1);
         }
 
         final Calendar c = Calendar.getInstance(Locale.UK);
         c.setFirstDayOfWeek(Calendar.SUNDAY);
         c.setMinimalDaysInFirstWeek(1);
         c.setTime(trDate.getTime());
         return c;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForAggressivness(int value) {
         return PlayerHelper.getNameForAggressivness(value);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      * @param showNumber TODO Missing Method Parameter Documentation
      * @param isMatch TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForBewertung(int value, boolean showNumber, boolean isMatch) {
         return PlayerHelper.getNameForSkill(value, showNumber, isMatch);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForCharacter(int value) {
         return PlayerHelper.getNameForCharacter(value);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForConfidence(int value) {
         return Team.getNameForSelbstvertrauen(value);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForGentleness(int value) {
         return PlayerHelper.getNameForGentleness(value);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param typ TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForMatchTyp(int typ) {
         return MatchLineup.getName4MatchTyp(typ);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForPosition(byte value) {
         return SpielerPosition.getNameForPosition(value);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      * @param showNumber TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForSkill(int value, boolean showNumber) {
         return PlayerHelper.getNameForSkill(value, showNumber);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForSpeciality(int value) {
         return PlayerHelper.getNameForSpeciality(value);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param typ TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForTaktik(int typ) {
         return Matchdetails.getNameForTaktik(typ);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param typ TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForTeamorder(int typ) {
         return Matchdetails.getNameForEinstellung(typ);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForTeamspirit(int value) {
         return Team.getNameForStimmung(value);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param value TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getNameForTraining(int value) {
         return Team.getNameForTraining(value);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public Vector<IPlugin> getPlugins() {
         return HOMainFrame.getPlugins();
     }
 
     /**
      * Utility Method that returns the field position from the HO Position Code (hoposcode) It is
      * impossible to make difference between left and right so always the left position is
      * returned
      *
      * @param hoposcode
      *
      * @return the field position
      */
     public int getPosition(int hoposcode) {
         if (hoposcode == 0) {
             return ISpielerPosition.keeper;
         }
 
         if (hoposcode < 4) {
             return ISpielerPosition.rightCentralDefender;
         }
 
         if ((hoposcode < 8)) {
             return ISpielerPosition.leftBack;
         }
 
         if ((hoposcode < 12)) {
             return ISpielerPosition.rightInnerMidfield;
         }
 
         if ((hoposcode < 16)) {
             return ISpielerPosition.leftWinger;
         }
 
         if ((hoposcode < 18) || (hoposcode == 21)) {
             return ISpielerPosition.rightForward;
         }
 
         if (hoposcode < 0) {
             return 0;
         }
 
         return hoposcode;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param typ TODO Missing Method Parameter Documentation
      * @param subtyp TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String getTooltiptext4SpielHighlight(int typ, int subtyp) {
         return Helper.getTooltiptext4SpielHighlight(typ, subtyp);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param matchID TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public boolean isUserMatch(String matchID) {
         return Helper.isUserMatch(matchID);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param spieler TODO Missing Method Parameter Documentation
      */
     public void addTempSpieler(plugins.ISpieler spieler) {
         final Spieler tempSpieler = new Spieler();
 
         tempSpieler.setNationalitaet(spieler.getNationalitaet());
         tempSpieler.setSpielerID(TransferEingabePanel
                                  .getNextTempSpielerID());
         tempSpieler.setName(spieler.getName());
         tempSpieler.setAlter(spieler.getAlter());
         tempSpieler.setAgeDays(spieler.getAgeDays());
         tempSpieler.setErfahrung(spieler.getErfahrung());
         tempSpieler.setForm(spieler.getForm());
         tempSpieler.setKondition(spieler.getKondition());
         tempSpieler.setVerteidigung(spieler.getVerteidigung());
         tempSpieler.setTorschuss(spieler.getTorschuss());
         tempSpieler.setTorwart(spieler.getTorwart());
         tempSpieler.setFluegelspiel(spieler.getFluegelspiel());
         tempSpieler.setPasspiel(spieler.getPasspiel());
         tempSpieler.setStandards(spieler.getStandards());
         tempSpieler.setSpielaufbau(spieler.getSpielaufbau());
 
         HOVerwaltung.instance().getModel().addSpieler(tempSpieler);
         de.hattrickorganizer.gui.RefreshManager.instance().doReInit();
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param timewerte TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String[] convertTimeMillisToFormatString(double[] timewerte) {
         return Helper.convertTimeMillisToFormatString(timewerte);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param src TODO Missing Method Parameter Documentation
      * @param dest TODO Missing Method Parameter Documentation
      */
     public void copyArray2Vector(Object[] src, Vector dest) {
         MyHelper.copyArray2Vector(src, dest);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param src TODO Missing Method Parameter Documentation
      * @param dest TODO Missing Method Parameter Documentation
      */
     public void copyVector2Array(Vector src, Object[] dest) {
         MyHelper.copyVector2Array(src, dest);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param vector TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public DefaultComboBoxModel createListModel(Vector<Object> vector) {
         return Helper.createListModel(vector);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param text TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      *
      * @throws Exception TODO Missing Method Exception Documentation
      */
     public plugins.ISpieler createSpielerFromText(String text) throws Exception {
         final Player player = new PlayerConverter().build(text);
 
         final Spieler tempSpieler = new Spieler();
         tempSpieler.setNationalitaet(HOVerwaltung.instance().getModel().getBasics().getLand());
         tempSpieler.setSpielerID(TransferEingabePanel.getNextTempSpielerID());
 
         if (player.getPlayerName().trim().equals("")) {
            tempSpieler.setName("Temp " + Math.abs(tempSpieler.getSpielerID()));
         } else {
             tempSpieler.setName(player.getPlayerName());
         }
 
         tempSpieler.setAlter(player.getAge());
         tempSpieler.setAgeDays(player.getAgeDays());
         tempSpieler.setErfahrung(player.getExperience());
         tempSpieler.setForm(player.getForm());
         tempSpieler.setKondition(player.getStamina());
         tempSpieler.setVerteidigung(player.getDefense());
         tempSpieler.setTorschuss(player.getAttack());
         tempSpieler.setTorwart(player.getGoalKeeping());
         tempSpieler.setFluegelspiel(player.getWing());
         tempSpieler.setPasspiel(player.getPassing());
         tempSpieler.setStandards(player.getSetPieces());
         tempSpieler.setSpielaufbau(player.getPlayMaking());
 
         return tempSpieler;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param text TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String decodeStringFromDatabase(String text) {
         return DBZugriff.deleteEscapeSequences(text);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param text TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String deleteEscapeSequences(String text) {
         return DBZugriff.deleteEscapeSequences(text);
     }
 
     /**
      * downloads all match related data and stores it in Database
      *
      * @param matchID TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public boolean downloadMatchData(int matchID) {
         //Spiel nicht vorhanden, dann erst runterladen!
         if (!DBZugriff.instance().isMatchVorhanden(matchID)) {
             try {
                 //details
                 if (HOMainFrame.instance().getOnlineWorker()
                                                         .getMatchDetails(matchID)) {
                     Matchdetails details = DBZugriff.instance()
                                                                                         .getMatchDetails(matchID);
 
                     //Lineups
                     HOMainFrame.instance().getOnlineWorker()
                                                         .getMatchlineup(matchID,
                                                                         details.getHeimId(),
                                                                         details.getGastId());
 
                     //Workaround
                     // Lineups *must* be available before the match highlights / report
                     // can be parsed in getMatchDetails()
                     // If these informations are still missing, we re-fetch them now
                     if (details == null || details.getMatchreport() == null || details.getMatchreport().trim().length() == 0) {
                     	// Fetch matchDetails again
                     	HOLogger.instance().debug(getClass(), "Fetching missing match highlights / report");
                     	HOMainFrame.instance().getOnlineWorker().getMatchDetails(matchID);
                     	details = DBZugriff.instance().getMatchDetails(matchID);
                     }
 
                     //MatchKurzInfo muss hier erstellt werden!!
                     final MatchLineup lineup = DBZugriff.instance()
                                                                                       .getMatchLineup(matchID);
 
                     if (lineup != null) {
                         final MatchKurzInfo info = new MatchKurzInfo();
 
                         //?
                         info.setAufstellung(true);
                         info.setGastID(lineup.getGastId());
                         info.setGastName(lineup.getGastName());
                         info.setGastTore(details.getGuestGoals());
                         info.setHeimID(lineup.getHeimId());
                         info.setHeimName(lineup.getHeimName());
                         info.setHeimTore(details.getHomeGoals());
                         info.setMatchDate(lineup.getStringSpielDate());
                         info.setMatchID(matchID);
                         info.setMatchStatus(IMatchKurzInfo.FINISHED);
                         info.setMatchTyp(lineup.getMatchTyp());
 
                         final MatchKurzInfo[] infos = {info};
                         DBZugriff.instance().storeMatchKurzInfos(infos);
                     }
 
                     MatchUpdater.updateMatch(HOMiniModel.instance(), matchID);
                 } else {
                     return false;
                 }
             } catch (Exception ex) {
                 //Fehler!
                 HOLogger.instance().log(getClass(),"SpieltagPanel.actionPerformed:  Fehler beim Download eines Spieles : "
                                    + ex);
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param text TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String encodeString4Database(String text) {
         return DBZugriff.insertEscapeSequences(text);
     }
 
     /**
      * checkes wheater matchdata is already stored in db or not
      *
      * @param matchID TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public boolean existsMatchInDB(int matchID) {
         return DBZugriff.instance().isMatchVorhanden(matchID);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param text TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public int[] generateIntArray(String text) {
         return Helper.generateIntArray(text);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param werte TODO Missing Method Parameter Documentation
      * @param trenner TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String[] generateStringArray(String werte, char trenner) {
         return MyHelper.generateStringArray(werte, trenner);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public void importHRF() {
         new HRFImport(HOMainFrame.instance());
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param text TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public String insertEscapeSequences(String text) {
         return DBZugriff.insertEscapeSequences(text);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param datei TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public java.awt.image.BufferedImage loadImage(String datei) {
         return Helper.loadImage(datei);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param im TODO Missing Method Parameter Documentation
      * @param color TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public java.awt.Image makeColorTransparent(java.awt.Image im, java.awt.Color color) {
         return Helper.makeColorTransparent(im, color);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param im TODO Missing Method Parameter Documentation
      * @param minr TODO Missing Method Parameter Documentation
      * @param ming TODO Missing Method Parameter Documentation
      * @param minb TODO Missing Method Parameter Documentation
      * @param maxr TODO Missing Method Parameter Documentation
      * @param maxg TODO Missing Method Parameter Documentation
      * @param maxb TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public java.awt.Image makeColorTransparent(java.awt.Image im, int minr, int ming, int minb,
                                                int maxr, int maxg, int maxb) {
         return Helper.makeColorTransparent(im, minr, ming, minb, maxr, maxg, maxb);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param url TODO Missing Method Parameter Documentation
      */
     public void openUrlInUserBRowser(String url) {
         try {
             de.hattrickorganizer.tools.BrowserLauncher.openURL(url);
         } catch (java.io.IOException ioex) {
 			JPanel panel = new JPanel();
 			panel.add(new JLabel("Open this manually: "));
 			JTextField urlField = new JTextField();
 			urlField.setAlignmentX(Component.CENTER_ALIGNMENT);
 			urlField.setText(" " + MyConnector.getHOSite());
 			urlField.addKeyListener(new KeyListener() {
 
 				public void keyTyped(KeyEvent event) {
 					event.consume();
 				}
 
 				public void keyPressed(KeyEvent event) {
 					if (!(event.getModifiers() == InputEvent.CTRL_MASK)) {
 						event.consume();
 					}
 				}
 
 				public void keyReleased(KeyEvent event) {
 					event.consume();
 				}
 
 			});
 			urlField.setSelectionColor(Color.GRAY);
 			urlField.setBackground(UIManager.getColor("Label.background"));
 			urlField.setBorder(null);
 			panel.add(urlField);
 
 			JOptionPane.showMessageDialog(
 					HOMainFrame.instance().getOwner(),
 					panel,
 					"Browser not found",
 					JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param date TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public java.sql.Timestamp parseDate(String date) {
         return MyHelper.parseDate(date);
     }
 
     /**
      * Round a double value
      *
      * @param wert value to round
      * @param nachkommastellen number of fraction digits
      *
      * @return rounded value
      */
     public double round(double wert, int nachkommastellen) {
         return Helper.round(wert, nachkommastellen);
     }
 
     /**
      * Round a float value
      *
      * @param wert value to round
      * @param nachkommastellen number of fraction digits
      *
      * @return rounded value
      */
     public float round(float wert, int nachkommastellen) {
         return Helper.round(wert, nachkommastellen);
     }
     
     /**
      * TODO Missing Method Documentation
      *
      * @param parent TODO Missing Method Parameter Documentation
      * @param message TODO Missing Method Parameter Documentation
      * @param titel TODO Missing Method Parameter Documentation
      * @param typ TODO Missing Method Parameter Documentation
      */
     public void showMessage(java.awt.Component parent, String message, String titel, int typ) {
         Helper.showMessage(parent, message, titel, typ);
     }
 
     public IMP3Player getMP3Player()
     {
     	return new de.hattrickorganizer.gui.utils.MP3PlayerWrapper();
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param toSort TODO Missing Method Parameter Documentation
      * @param spaltenindex TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public int[][] sortintArray(int[][] toSort, int spaltenindex) {
         return Helper.sortintArray(toSort, spaltenindex);
     }
 
 	public boolean isDevVersion() {
 		File file = new File(HOVerwaltung.instance().getModel().getBasics().getManager() + ".ext");
 		if (file.exists()) {
 			return true;
 		}
 		return false;
 	}
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the economy date to flip
      * over to the next week.
      *
      * @see return a HTCalendar.
      */
     public IHTCalendar createEconomyCalendar() {
     	return HTCalendarFactory.createEconomyCalendar(HOMiniModel.instance());
     }
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the economy date to flip
      * over to the next week.
      *
      * @param model HO data model
      *
      * @see return a HTCalendar.
      */
     public IHTCalendar createEconomyCalendar(IHOMiniModel model) {
     	return HTCalendarFactory.createEconomyCalendar(model);
     }
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the economy date to flip
      * over to the next week.
      *
      * @param date Date to set the calendar
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createEconomyCalendar(Date date) {
     	return HTCalendarFactory.createEconomyCalendar(HOMiniModel.instance(), date);
 
     }
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the economy date to flip
      * over to the next week.
      *
      * @param timestamp Date to set the calendar
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createEconomyCalendar(Timestamp timestamp) {
     	return createEconomyCalendar(new Date(timestamp.getTime()));
 
     }
 /**
      * Creates a HTCalendar to calculate local values for a league,  using the economy date to flip
      * over to the next week.
      *
      * @param model HO data model
      * @param date Date to set the calendar
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createEconomyCalendar(IHOMiniModel model, Date date) {
     	return HTCalendarFactory.createEconomyCalendar(model, date);
 
     }
 
     /**
      * Creates a HTCalendar to calculate global (Swedish) values.
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createGlobalCalendar() {
     	return HTCalendarFactory.createGlobalCalendar();
     }
 
     /**
      * Creates a HTCalendar to calculate global (Swedish) values  and presets it with he specified
      * date a date.
      *
      * @param date Date to set the calendar
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createGlobalCalendar(Date date) {
     	return HTCalendarFactory.createGlobalCalendar(date);
     }
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the training date to
      * flip over to the next week.
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createTrainingCalendar() {
     	return HTCalendarFactory.createTrainingCalendar(HOMiniModel.instance());
     }
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the training date to
      * flip over to the next week.
      *
      * @param model HO data model
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createTrainingCalendar(IHOMiniModel model) {
     	return HTCalendarFactory.createTrainingCalendar(model);
     }
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the training date to
      * flip over to the next week.
      *
      * @param date Date to set the calendar
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createTrainingCalendar(Date date) {
     	return HTCalendarFactory.createTrainingCalendar(HOMiniModel.instance(), date);
     }
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the training date to
      * flip over to the next week.
      *
      * @param timestamp Date to set the calendar
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createTrainingCalendar(Timestamp timestamp) {
     	return createTrainingCalendar(new Date(timestamp.getTime()));
     }
 
     /**
      * Creates a HTCalendar to calculate local values for a league,  using the training date to
      * flip over to the next week.
      *
      * @param model HO data model
      * @param date Date to set the calendar
      *
      * @return a HTCalendar.
      */
     public IHTCalendar createTrainingCalendar(IHOMiniModel model, Date date) {
     	return HTCalendarFactory.createTrainingCalendar(model, date);
     }
 
 	/**
 	 * Get HT-Season of a given date (using the economy calendar)
 	 *
 	 * @param date					the date to convert
 	 * @return	HT-Season
 	 */
 	public int getHTSeason (Date date) {
 		return getHTSeason(date, false);
 	}
 
 	/**
 	 * Get HT-Season of a given date
 	 *
 	 * @param date					the date to convert
 	 * @param useTrainingCalendar	use training calendar if true, else use economy calendar
 	 * @return	HT-Season
 	 */
 	public int getHTSeason (Date date, boolean useTrainingCalendar) {
 		IHTCalendar cal;
 		if (useTrainingCalendar)
 			cal = createTrainingCalendar(date);
 		else
 			cal = createEconomyCalendar(date);
 		if (cal != null)
 			return cal.getHTSeason();
 		else
 			return -1;
 	}
 
 	/**
 	 * Get HT-Week of a given date (using the economy calendar)
 	 *
 	 * @param date					the date to convert
 	 * @return	HT-Week
 	 */
 	public int getHTWeek (Date date) {
 		return getHTWeek(date, false);
 	}
 
 	/**
 	 * Get HT-Week of a given date
 	 *
 	 * @param date					the date to convert
 	 * @param useTrainingCalendar	use training calendar if true, else use economy calendar
 	 * @return	HT-Week
 	 */
 	public int getHTWeek (Date date, boolean useTrainingCalendar) {
 		IHTCalendar cal;
 		if (useTrainingCalendar)
 			cal = createTrainingCalendar(date);
 		else
 			cal = createEconomyCalendar(date);
 		if (cal != null)
 			return cal.getHTWeek();
 		else
 			return -1;
 	}
 
 	/**
 	 * Get HT-Season of a given date (using the economy calendar)
 	 *
 	 * @param timestamp				the date to convert
 	 * @return	HT-Season
 	 */
 	public int getHTSeason (Timestamp timestamp) {
 		return getHTSeason(new Date(timestamp.getTime()));
 	}
 
 	/**
 	 * Get HT-Season of a given date
 	 *
 	 * @param timestamp				the date to convert
 	 * @param useTrainingCalendar	use training calendar if true, else use economy calendar
 	 * @return	HT-Season
 	 */
 	public int getHTSeason (Timestamp timestamp, boolean useTrainingCalendar) {
 		return getHTSeason(new Date(timestamp.getTime()), useTrainingCalendar);
 	}
 
 	/**
 	 * Get HT-Week of a given date (using the economy calendar)
 	 *
 	 * @param timestamp				the date to convert
 	 * @return	HT-Week
 	 */
 	public int getHTWeek (Timestamp timestamp) {
 		return getHTWeek(new Date(timestamp.getTime()));
 	}
 
 	/**
 	 * Get HT-Week of a given date
 	 *
 	 * @param timestamp				the date to convert
 	 * @param useTrainingCalendar	use training calendar if true, else use economy calendar
 	 * @return	HT-Week
 	 */
 	public int getHTWeek (Timestamp timestamp, boolean useTrainingCalendar) {
 		return getHTWeek(new Date(timestamp.getTime()), useTrainingCalendar);
 	}
 
 }
