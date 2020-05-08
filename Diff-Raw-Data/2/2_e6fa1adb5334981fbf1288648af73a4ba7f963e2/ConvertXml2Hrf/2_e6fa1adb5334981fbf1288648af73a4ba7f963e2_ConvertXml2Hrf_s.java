 // %929884203:de.hattrickorganizer.net%
 /*
  * ConvertXml2Hrf.java
  *
  * Created on 12. Januar 2004, 09:44
  */
 package de.hattrickorganizer.net;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import plugins.ISpielerPosition;
 
 import de.hattrickorganizer.gui.login.LoginWaitDialog;
 import de.hattrickorganizer.logik.xml.XMLArenaParser;
 import de.hattrickorganizer.logik.xml.XMLClubParser;
 import de.hattrickorganizer.logik.xml.XMLMatchLineupParser;
 import de.hattrickorganizer.logik.xml.XMLMatchesParser;
 import de.hattrickorganizer.logik.xml.XMLTrainingParser;
 import de.hattrickorganizer.logik.xml.xmlEconomyParser;
 import de.hattrickorganizer.logik.xml.xmlLeagueDetailsParser;
 import de.hattrickorganizer.logik.xml.xmlMatchOrderParser;
 import de.hattrickorganizer.logik.xml.xmlMatchdetailsParser;
 import de.hattrickorganizer.logik.xml.xmlPlayersParser;
 import de.hattrickorganizer.logik.xml.xmlTeamDetailsParser;
 import de.hattrickorganizer.logik.xml.xmlWorldDetailsParser;
 import de.hattrickorganizer.model.Team;
 import de.hattrickorganizer.model.matches.MatchKurzInfo;
 import de.hattrickorganizer.model.matches.MatchLineup;
 import de.hattrickorganizer.model.matches.MatchLineupTeam;
 import de.hattrickorganizer.model.matches.Matchdetails;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.PlayerHelper;
 
 
 /**
  * Convert the necessary xml data into a HRF file.
  *
  * @author thomas.werth
  */
 public class ConvertXml2Hrf {
     //~ Instance fields ----------------------------------------------------------------------------
 
     protected Hashtable<?, ?> m_htArena;
     protected Hashtable<?, ?> m_htClub;
     protected Hashtable<?, ?> m_htEconomy;
     protected Hashtable<?, ?> m_htLiga;
     protected Hashtable<?, ?> m_htNextLineup;
     protected Hashtable<?, ?> m_htTeamdetails;
     protected Hashtable<?, ?> m_htTraining;
     protected Hashtable<?, ?> m_htWorld;
     protected MatchLineup m_clLineUp;
     protected MatchLineupTeam m_clTeam;
 
     protected StringBuffer m_sHRFBuffer;
 
     //enthält eine Liste an Hashtable die je einen Spieler beschreiben
     protected Vector<?> m_vSpieler;
 
     //MatchOrder
     protected MatchKurzInfo[] m_aMatches;
     int m_iLastAttitude;
     int m_iLastTactic;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new instance of ConvertXml2Hrf
      */
     public ConvertXml2Hrf() {
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Create the HRF data and return it in one string. 
      */
     public final String createHrf(LoginWaitDialog waitDialog) throws Exception {
         //init
         m_sHRFBuffer = new StringBuffer();
 
         try {
             //Hashtable's füllen
         	final MyConnector mc = MyConnector.instance();
             waitDialog.setValue(5);
            m_htTeamdeatils = new xmlTeamDetailsParser().parseTeamdetailsFromString(mc.getTeamdetails(-1));
             waitDialog.setValue(10);
             m_htClub = new XMLClubParser().parseClubFromString(mc.getVerein());
             waitDialog.setValue(15);
             m_htLiga = new xmlLeagueDetailsParser().parseLeagueDetailsFromString(mc.getLeagueDetails(),m_htTeamdetails.get("TeamID").toString());
             waitDialog.setValue(20);
             m_htWorld = new xmlWorldDetailsParser().parseWorldDetailsFromString(mc.getWorldDetails(),m_htTeamdetails.get("LeagueID").toString());
             waitDialog.setValue(25);
             m_clLineUp = new XMLMatchLineupParser().parseMatchLineupFromString(mc.getMatchLineup(-1,-1).toString());
             waitDialog.setValue(30);
             m_vSpieler = new xmlPlayersParser().parsePlayersFromString(mc.getPlayersAsp());
             waitDialog.setValue(35);
             m_htEconomy = new xmlEconomyParser().parseEconomyFromString(mc.getEconomy());
             waitDialog.setValue(40);
             m_htTraining = new XMLTrainingParser().parseTrainingFromString(mc.getTraining());
             waitDialog.setValue(45);
             m_htArena = new XMLArenaParser().parseArenaFromString(mc.getArena());
 
             //MatchOrder
             waitDialog.setValue(50);
             m_aMatches = new XMLMatchesParser().parseMatchesFromString(mc.getMatchesASP(Integer.parseInt(m_htTeamdetails.get("TeamID").toString()), false));
             waitDialog.setValue(52);
 
             // Automatisch alle MatchLineups runterladen
 			for (int i = 0; (m_aMatches != null) && (i < m_aMatches.length); i++) {
 				if (m_aMatches[i].getMatchStatus() == MatchKurzInfo.UPCOMING) {
 					waitDialog.setValue(54);
 					m_htNextLineup = new xmlMatchOrderParser().parseMatchOrderFromString(mc.getMatchOrder(m_aMatches[i].getMatchID()));
 					break;
 				}
 			}
 
             waitDialog.setValue(55);
 
             // Team ermitteln, für Ratings der Player wichtig
             if (m_clLineUp != null) {
                 final Matchdetails md = new xmlMatchdetailsParser().parseMachtdetailsFromString(mc.getMatchdetails(m_clLineUp.getMatchID()));
 
                 if (m_clLineUp.getHeimId() == Integer.parseInt(m_htTeamdetails.get("TeamID").toString())) {
                     m_clTeam = (de.hattrickorganizer.model.matches.MatchLineupTeam) m_clLineUp.getHeim();
 
                     if (md != null) {
                         m_iLastAttitude = md.getHomeEinstellung();
                         m_iLastTactic = md.getHomeTacticType();
                     }
                 } else {
                     m_clTeam = (de.hattrickorganizer.model.matches.MatchLineupTeam) m_clLineUp.getGast();
 
                     if (md != null) {
                         m_iLastAttitude = md.getGuestEinstellung();
                         m_iLastTactic = md.getGuestTacticType();
                     }
                 }
                 m_clTeam.getTeamID();
             }
 
             //Abschnitte erstellen   
             waitDialog.setValue(60);
 
             //basics
             createBasics();
             waitDialog.setValue(65);
 
             //Liga
             createLeague();
             waitDialog.setValue(70);
 
             //Club
             createClub();
             waitDialog.setValue(75);
 
             //team
             createTeam();
             waitDialog.setValue(80);
 
             //lineup
             createLineUp();
             waitDialog.setValue(85);
 
             //economy  
             createEconemy();
             waitDialog.setValue(90);
 
             //Arena
             createArena();
             waitDialog.setValue(93);
 
             //players
             createPlayers();
             waitDialog.setValue(96);
 
             //xtra Data
             createWorld();
             waitDialog.setValue(99);
 
             //lineup from the last match
             createLastLineUp();
             waitDialog.setValue(100);
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),"convertxml2hrf: Exception: " + e);
             HOLogger.instance().log(getClass(),e);
             throw new Exception(e);
         }
 
         //dialog zum Saven anzeigen
         //speichern
         //writeHRF( dateiname );
         return m_sHRFBuffer.toString();
     }
 
     /**
      * Create the arena data.
      */
     protected final void createArena() throws Exception {
         m_sHRFBuffer.append("[arena]" + "\n");
         m_sHRFBuffer.append("arenaname=" + m_htArena.get("ArenaName") + "\n");
         m_sHRFBuffer.append("arenaid=" + m_htArena.get("ArenaID") + "\n");
         m_sHRFBuffer.append("antalStaplats=" + m_htArena.get("Terraces") + "\n");
         m_sHRFBuffer.append("antalSitt=" + m_htArena.get("Basic") + "\n");
         m_sHRFBuffer.append("antalTak=" + m_htArena.get("Roof") + "\n");
         m_sHRFBuffer.append("antalVIP=" + m_htArena.get("VIP") + "\n");
         m_sHRFBuffer.append("seatTotal=" + m_htArena.get("Total") + "\n");
         m_sHRFBuffer.append("expandingStaplats=" + m_htArena.get("ExTerraces") + "\n");
         m_sHRFBuffer.append("expandingSitt=" + m_htArena.get("ExBasic") + "\n");
         m_sHRFBuffer.append("expandingTak=" + m_htArena.get("ExRoof") + "\n");
         m_sHRFBuffer.append("expandingVIP=" + m_htArena.get("ExVIP") + "\n");
         m_sHRFBuffer.append("expandingSseatTotal=" + m_htArena.get("ExTotal") + "\n");
         m_sHRFBuffer.append("isExpanding=" + m_htArena.get("isExpanding") + "\n");
 
         //Achtung bei keiner Erweiterung = 0!
         m_sHRFBuffer.append("ExpansionDate=" + m_htArena.get("ExpansionDate") + "\n");
     }
 
     ////////////////////////////////////////////////////////////////////////////////
     //Helper
     ////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Create the basic data.
      */
     protected final void createBasics() throws Exception {
         m_sHRFBuffer.append("[basics]\n");
         m_sHRFBuffer.append("application=HO\n");
         m_sHRFBuffer.append("appversion=" + de.hattrickorganizer.gui.HOMainFrame.VERSION + "\n");
         m_sHRFBuffer.append("date=" + m_htTeamdetails.get("FetchedDate") + "\n");
         m_sHRFBuffer.append("season=" + m_htWorld.get("Season") + "\n");
         m_sHRFBuffer.append("matchround=" + m_htWorld.get("MatchRound") + "\n");
         m_sHRFBuffer.append("teamID=" + m_htTeamdetails.get("TeamID") + "\n");
         m_sHRFBuffer.append("teamName=" + m_htTeamdetails.get("TeamName") + "\n");
         m_sHRFBuffer.append("owner=" + m_htTeamdetails.get("Loginname") + "\n");
         m_sHRFBuffer.append("ownerEmail=" + m_htTeamdetails.get("Email") + "\n");
         m_sHRFBuffer.append("ownerICQ=" + m_htTeamdetails.get("ICQ") + "\n");
         m_sHRFBuffer.append("ownerHomepage=" + m_htTeamdetails.get("HomePage") + "\n");
         m_sHRFBuffer.append("countryID=" + m_htWorld.get("CountryID") + "\n");
         m_sHRFBuffer.append("leagueID=" + m_htTeamdetails.get("LeagueID") + "\n");
 //        m_sHRFBuffer.append("arenaID=" + m_htArena.get("ArenaID") + "\n");
         m_sHRFBuffer.append("regionID=" + m_htTeamdetails.get("RegionID") + "\n");
         m_sHRFBuffer.append("hasSupporter=" + m_htTeamdetails.get("HasSupporter") + "\n");
     }
 
     /**
      * Create the club data.
      */
     protected final void createClub() throws Exception {
         m_sHRFBuffer.append("[club]\n");
         m_sHRFBuffer.append("mvTranare=" + m_htClub.get("KeeperTrainers") + "\n");
         m_sHRFBuffer.append("hjTranare=" + m_htClub.get("AssistantTrainers") + "\n");
         m_sHRFBuffer.append("psykolog=" + m_htClub.get("Psychologists") + "\n");
         m_sHRFBuffer.append("presstalesman=" + m_htClub.get("PressSpokesmen") + "\n");
         m_sHRFBuffer.append("ekonom=" + m_htClub.get("Economists") + "\n");
         m_sHRFBuffer.append("massor=" + m_htClub.get("Physiotherapists") + "\n");
         m_sHRFBuffer.append("lakare=" + m_htClub.get("Doctors") + "\n");
         m_sHRFBuffer.append("juniorverksamhet=" + m_htClub.get("YouthLevel") + "\n");
         m_sHRFBuffer.append("undefeated=" + m_htTeamdetails.get("NumberOfUndefeated") + "\n");
         m_sHRFBuffer.append("victories=" + m_htTeamdetails.get("NumberOfVictories") + "\n");
         m_sHRFBuffer.append("fanclub=" + m_htEconomy.get("FanClubSize") + "\n");
     }
 
     /**
      * Create the economy data.
      */
     protected final void createEconemy() throws Exception {
         //wahrscheinlich in Training.asp fehlt noch
         m_sHRFBuffer.append("[economy]" + "\n");
 
         if (m_htEconomy.get("SponsorsPopularity") != null) {
             m_sHRFBuffer.append("supporters=" + m_htEconomy.get("SupportersPopularity") + "\n");
             m_sHRFBuffer.append("sponsors=" + m_htEconomy.get("SponsorsPopularity") + "\n");
 
             //es wird grad gespielt flag setzen
         } else {
             m_sHRFBuffer.append("playingMatch=true");
         }
 
         m_sHRFBuffer.append("cash=" + m_htEconomy.get("Cash") + "\n");
         m_sHRFBuffer.append("IncomeSponsorer=" + m_htEconomy.get("IncomeSponsors") + "\n");
         m_sHRFBuffer.append("incomePublik=" + m_htEconomy.get("IncomeSpectators") + "\n");
         m_sHRFBuffer.append("incomeFinansiella=" + m_htEconomy.get("IncomeFinancial") + "\n");
         m_sHRFBuffer.append("incomeTillfalliga=" + m_htEconomy.get("IncomeTemporary") + "\n");
         m_sHRFBuffer.append("incomeSumma=" + m_htEconomy.get("IncomeSum") + "\n");
         m_sHRFBuffer.append("costsSpelare=" + m_htEconomy.get("CostsPlayers") + "\n");
         m_sHRFBuffer.append("costsPersonal=" + m_htEconomy.get("CostsStaff") + "\n");
         m_sHRFBuffer.append("costsArena=" + m_htEconomy.get("CostsArena") + "\n");
         m_sHRFBuffer.append("costsJuniorverksamhet=" + m_htEconomy.get("CostsYouth") + "\n");
         m_sHRFBuffer.append("costsRantor=" + m_htEconomy.get("CostsFinancial") + "\n");
         m_sHRFBuffer.append("costsTillfalliga=" + m_htEconomy.get("CostsTemporary") + "\n");
         m_sHRFBuffer.append("costsSumma=" + m_htEconomy.get("CostsSum") + "\n");
         m_sHRFBuffer.append("total=" + m_htEconomy.get("ExpectedWeeksTotal") + "\n");
         m_sHRFBuffer.append("lastIncomeSponsorer=" + m_htEconomy.get("LastIncomeSponsors") + "\n");
         m_sHRFBuffer.append("lastIncomePublik=" + m_htEconomy.get("LastIncomeSpectators") + "\n");
         m_sHRFBuffer.append("lastIncomeFinansiella=" + m_htEconomy.get("LastIncomeFinancial") + "\n");
         m_sHRFBuffer.append("lastIncomeTillfalliga=" + m_htEconomy.get("LastIncomeTemporary") + "\n");
         m_sHRFBuffer.append("lastIncomeSumma=" + m_htEconomy.get("LastIncomeSum") + "\n");
         m_sHRFBuffer.append("lastCostsSpelare=" + m_htEconomy.get("LastCostsPlayers") + "\n");
         m_sHRFBuffer.append("lastCostsPersonal=" + m_htEconomy.get("LastCostsStaff") + "\n");
         m_sHRFBuffer.append("lastCostsArena=" + m_htEconomy.get("LastCostsArena") + "\n");
         m_sHRFBuffer.append("lastCostsJuniorverksamhet=" + m_htEconomy.get("LastCostsYouth") + "\n");
         m_sHRFBuffer.append("lastCostsRantor=" + m_htEconomy.get("LastCostsFinancial") + "\n");
         m_sHRFBuffer.append("lastCostsTillfalliga=" + m_htEconomy.get("LastCostsTemporary") + "\n");
         m_sHRFBuffer.append("lastCostsSumma=" + m_htEconomy.get("LastCostsSum") + "\n");
         m_sHRFBuffer.append("lastTotal=" + m_htEconomy.get("LastWeeksTotal") + "\n");
     }
 
     /**
      * Create last lineup section.
      */
     protected final void createLastLineUp() {
         m_sHRFBuffer.append("[lastlineup]" + "\n");
         m_sHRFBuffer.append("trainer=" + m_htTeamdetails.get("TrainerID") + "\n");
 
            
         try {
 			m_sHRFBuffer.append("installning=" + m_iLastAttitude + "\n");
 			m_sHRFBuffer.append("tactictype=" + m_iLastTactic + "\n");
 			m_sHRFBuffer.append("keeper=" + m_clTeam.getPlayerByPosition(ISpielerPosition.keeper).getSpielerId() + "\n");
 			m_sHRFBuffer.append("rightBack=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightBack).getSpielerId() + "\n");
 			m_sHRFBuffer.append("insideBack1=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightCentralDefender).getSpielerId() + "\n");
 			m_sHRFBuffer.append("insideBack2=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftCentralDefender).getSpielerId() + "\n");
 			m_sHRFBuffer.append("insideBack3=" + m_clTeam.getPlayerByPosition(ISpielerPosition.middleCentralDefender).getSpielerId() + "\n");
 			m_sHRFBuffer.append("leftBack=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftBack).getSpielerId() + "\n");
 			m_sHRFBuffer.append("rightWinger=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightWinger).getSpielerId() + "\n");
 			m_sHRFBuffer.append("insideMid1=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightInnerMidfield).getSpielerId() + "\n");
 			m_sHRFBuffer.append("insideMid2=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftInnerMidfield).getSpielerId() + "\n");
 			m_sHRFBuffer.append("insideMid3=" + m_clTeam.getPlayerByPosition(ISpielerPosition.centralInnerMidfield).getSpielerId() + "\n");
 			m_sHRFBuffer.append("leftWinger=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftWinger).getSpielerId() + "\n");
 			m_sHRFBuffer.append("forward1=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightForward).getSpielerId() + "\n");
 			m_sHRFBuffer.append("forward2=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftForward).getSpielerId() + "\n");
 			m_sHRFBuffer.append("forward3=" + m_clTeam.getPlayerByPosition(ISpielerPosition.centralForward).getSpielerId() + "\n");
 			m_sHRFBuffer.append("substBack=" + m_clTeam.getPlayerByPosition(ISpielerPosition.substDefender).getSpielerId() + "\n");
 			m_sHRFBuffer.append("substInsideMid=" + m_clTeam.getPlayerByPosition(ISpielerPosition.substInnerMidfield).getSpielerId() + "\n");
 			m_sHRFBuffer.append("substWinger=" + m_clTeam.getPlayerByPosition(ISpielerPosition.substWinger).getSpielerId() + "\n");
 			m_sHRFBuffer.append("substKeeper=" + m_clTeam.getPlayerByPosition(ISpielerPosition.substKeeper).getSpielerId() + "\n");
 			m_sHRFBuffer.append("substForward=" + m_clTeam.getPlayerByPosition(ISpielerPosition.substForward).getSpielerId() + "\n");
 			m_sHRFBuffer.append("captain=" + m_clTeam.getPlayerByPosition(ISpielerPosition.captain).getSpielerId() + "\n");
 			m_sHRFBuffer.append("kicker1=" + m_clTeam.getPlayerByPosition(ISpielerPosition.setPieces).getSpielerId() + "\n");
 
 			m_sHRFBuffer.append("behrightBack=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightBack).getTaktik() + "\n");
 			m_sHRFBuffer.append("behinsideBack1=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightCentralDefender).getTaktik() + "\n");
 			m_sHRFBuffer.append("behinsideBack2=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftCentralDefender).getTaktik() + "\n");
 			m_sHRFBuffer.append("behinsideBack3=" + m_clTeam.getPlayerByPosition(ISpielerPosition.middleCentralDefender).getTaktik() + "\n");
 			m_sHRFBuffer.append("behleftBack=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftWinger).getTaktik() + "\n");
 			m_sHRFBuffer.append("behrightWinger=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightWinger).getTaktik() + "\n");
 			m_sHRFBuffer.append("behinsideMid1=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightInnerMidfield).getTaktik() + "\n");
 			m_sHRFBuffer.append("behinsideMid2=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftInnerMidfield).getTaktik() + "\n");
 			m_sHRFBuffer.append("behinsideMid3=" + m_clTeam.getPlayerByPosition(ISpielerPosition.centralInnerMidfield).getTaktik() + "\n");
 			m_sHRFBuffer.append("behleftWinger=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftWinger).getTaktik() + "\n");
 			m_sHRFBuffer.append("behforward1=" + m_clTeam.getPlayerByPosition(ISpielerPosition.rightForward).getTaktik() + "\n");
 			m_sHRFBuffer.append("behforward2=" + m_clTeam.getPlayerByPosition(ISpielerPosition.leftForward).getTaktik() + "\n");
 			m_sHRFBuffer.append("behforward3=" + m_clTeam.getPlayerByPosition(ISpielerPosition.centralForward).getTaktik() + "\n");
         } catch (Exception e) {
         	HOLogger.instance().debug(getClass(), "Error(last lineup): " + e);
         }
     }
 
     /**
      * Create the league data.
      */
     protected final void createLeague() throws Exception {
         m_sHRFBuffer.append("[league]\n");
         m_sHRFBuffer.append("serie=" + m_htLiga.get("LeagueLevelUnitName") + "\n");
         m_sHRFBuffer.append("spelade=" + m_htLiga.get("Matches") + "\n");
         m_sHRFBuffer.append("gjorda=" + m_htLiga.get("GoalsFor") + "\n");
         m_sHRFBuffer.append("inslappta=" + m_htLiga.get("GoalsAgainst") + "\n");
         m_sHRFBuffer.append("poang=" + m_htLiga.get("Points") + "\n");
         m_sHRFBuffer.append("placering=" + m_htLiga.get("Position") + "\n");
     }
 
     private String getPlayerForNextLineup(String position) {
     	if (m_htNextLineup != null) {
     		final Object ret = m_htNextLineup.get(position);
     		if (ret != null) {
     			return ret.toString();
     		}
     	}
     	return "0";
     }
     
     private String getPlayerOrderForNextLineup(String position) {
     	if (m_htNextLineup != null) {
     		String ret = (String)m_htNextLineup.get(position);
     		
     		if (ret != null) {
     			ret = ret.trim();
     			if (!"null".equals(ret) && !"".equals(ret)) {
     				return ret.trim();
     			}
     		}
     	}
     	return "0";
     }
     
     /**
      * Erstellt die LineUp Daten
      */
     protected final void createLineUp() throws Exception {
         m_sHRFBuffer.append("[lineup]" + "\n");
 
         try {
             m_sHRFBuffer.append("trainer=" + m_htTeamdetails.get("TrainerID") + "\n");
             m_sHRFBuffer.append("installning=" + m_htNextLineup.get("Attitude") + "\n");
 			m_sHRFBuffer.append("tactictype="+ (m_htNextLineup.get("TacticType").toString().trim().equals("") ? "0" : m_htNextLineup.get("TacticType").toString().trim()) + "\n");
            
 			m_sHRFBuffer.append("keeper=" + getPlayerForNextLineup("KeeperID") + "\n");
             m_sHRFBuffer.append("rightBack=" + getPlayerForNextLineup("RightBackID") + "\n");
             m_sHRFBuffer.append("insideBack1=" + getPlayerForNextLineup("RightCentralDefenderID") + "\n");
             m_sHRFBuffer.append("insideBack2=" + getPlayerForNextLineup("LeftCentralDefenderID") + "\n");
             m_sHRFBuffer.append("insideBack3=" + getPlayerForNextLineup("MiddleCentralDefenderID") + "\n");
             m_sHRFBuffer.append("leftBack=" + getPlayerForNextLineup("LeftBackID") + "\n");
             m_sHRFBuffer.append("rightWinger=" + getPlayerForNextLineup("RightWingerID") + "\n");
             m_sHRFBuffer.append("insideMid1=" + getPlayerForNextLineup("RightInnerMidfieldID") + "\n");
             m_sHRFBuffer.append("insideMid2=" + getPlayerForNextLineup("LeftInnerMidfieldID") + "\n");
             m_sHRFBuffer.append("insideMid3=" + getPlayerForNextLineup("CentralInnerMidfieldID") + "\n");
             m_sHRFBuffer.append("leftWinger=" + getPlayerForNextLineup("LeftWingerID") + "\n");
             m_sHRFBuffer.append("forward1=" + getPlayerForNextLineup("RightForwardID") + "\n");
             m_sHRFBuffer.append("forward2=" + getPlayerForNextLineup("LeftForwardID") + "\n");
             m_sHRFBuffer.append("forward3=" + getPlayerForNextLineup("CentralForwardID") + "\n");
             m_sHRFBuffer.append("substBack=" + getPlayerForNextLineup("SubstBackID") + "\n");
             m_sHRFBuffer.append("substInsideMid=" + getPlayerForNextLineup("SubstInsideMidID") + "\n");
             m_sHRFBuffer.append("substWinger=" + getPlayerForNextLineup("SubstWingerID") + "\n");
             m_sHRFBuffer.append("substKeeper=" + getPlayerForNextLineup("SubstKeeperID") + "\n");
             m_sHRFBuffer.append("substForward=" + getPlayerForNextLineup("SubstForwardID") + "\n");
             m_sHRFBuffer.append("captain=" + getPlayerForNextLineup("CaptainID") + "\n");
             m_sHRFBuffer.append("kicker1=" + getPlayerForNextLineup("KickerID") + "\n");
             
 			m_sHRFBuffer.append("behrightBack=" + getPlayerOrderForNextLineup("RightBackOrder") + "\n");
 			m_sHRFBuffer.append("behinsideBack1=" + getPlayerOrderForNextLineup("RightCentralDefenderOrder") + "\n");
 			m_sHRFBuffer.append("behinsideBack2=" + getPlayerOrderForNextLineup("LeftCentralDefenderOrder") + "\n");
 			m_sHRFBuffer.append("behinsideBack3=" + getPlayerOrderForNextLineup("MiddleCentralDefenderOrder") + "\n");
 			m_sHRFBuffer.append("behleftBack=" + getPlayerOrderForNextLineup("LeftBackOrder") + "\n");
 			m_sHRFBuffer.append("behrightWinger=" + getPlayerOrderForNextLineup("RightWingerOrder") + "\n");
 			m_sHRFBuffer.append("behinsideMid1=" + getPlayerOrderForNextLineup("RightInnerMidfieldOrder") + "\n");
 			m_sHRFBuffer.append("behinsideMid2=" + getPlayerOrderForNextLineup("LeftInnerMidfieldOrder") + "\n");
 			m_sHRFBuffer.append("behinsideMid3=" + getPlayerOrderForNextLineup("CentralInnerMidfieldOrder") + "\n");
 			m_sHRFBuffer.append("behleftWinger=" + getPlayerOrderForNextLineup("LeftWingerOrder") + "\n");
 			m_sHRFBuffer.append("behforward1=" + getPlayerOrderForNextLineup("RightForwardOrder") + "\n");
 			m_sHRFBuffer.append("behforward2=" + getPlayerOrderForNextLineup("LeftForwardOrder") + "\n");
 			m_sHRFBuffer.append("behforward3=" + getPlayerOrderForNextLineup("CentralForwardOrder") + "\n");
         } catch (Exception e) {
         	HOLogger.instance().debug(getClass(), "Error(lineup): " + e);
         }
     }
 
     /**
      * Create the player data.
      */
     protected final void createPlayers() throws Exception {
         Hashtable<?, ?> ht = null;
 
         for (int i = 0; (m_vSpieler != null) && (i < m_vSpieler.size()); i++) {
             ht = (Hashtable<?, ?>) m_vSpieler.elementAt(i);
 
             m_sHRFBuffer.append("[player" + ht.get("PlayerID").toString() + "]" + "\n");
             m_sHRFBuffer.append("name=" + ht.get("PlayerName").toString() + "\n");
             m_sHRFBuffer.append("ald=" + ht.get("Age").toString() + "\n");
             m_sHRFBuffer.append("agedays=" + ht.get("AgeDays").toString() + "\n");
             m_sHRFBuffer.append("ska=" + ht.get("InjuryLevel").toString() + "\n");
             m_sHRFBuffer.append("for=" + ht.get("PlayerForm").toString() + "\n");
             m_sHRFBuffer.append("uth=" + ht.get("StaminaSkill").toString() + "\n");
             m_sHRFBuffer.append("spe=" + ht.get("PlaymakerSkill").toString() + "\n");
             m_sHRFBuffer.append("mal=" + ht.get("ScorerSkill").toString() + "\n");
             m_sHRFBuffer.append("fra=" + ht.get("PassingSkill").toString() + "\n");
             m_sHRFBuffer.append("ytt=" + ht.get("WingerSkill").toString() + "\n");
             m_sHRFBuffer.append("fas=" + ht.get("SetPiecesSkill").toString() + "\n");
             m_sHRFBuffer.append("bac=" + ht.get("DefenderSkill").toString() + "\n");
             m_sHRFBuffer.append("mlv=" + ht.get("KeeperSkill").toString() + "\n");
             m_sHRFBuffer.append("rut=" + ht.get("Experience").toString() + "\n");
             m_sHRFBuffer.append("led=" + ht.get("Leadership").toString() + "\n");
             m_sHRFBuffer.append("sal=" + ht.get("Salary").toString() + "\n");
             m_sHRFBuffer.append("mkt=" + ht.get("MarketValue").toString() + "\n");
             m_sHRFBuffer.append("gev=" + ht.get("CareerGoals").toString() + "\n");
             m_sHRFBuffer.append("gtl=" + ht.get("LeagueGoals").toString() + "\n");
             m_sHRFBuffer.append("gtc=" + ht.get("CupGoals").toString() + "\n");
             m_sHRFBuffer.append("gtt=" + ht.get("FriendliesGoals").toString() + "\n");
             m_sHRFBuffer.append("hat=" + ht.get("CareerHattricks").toString() + "\n");
             m_sHRFBuffer.append("CountryID=" + ht.get("CountryID").toString() + "\n");
             m_sHRFBuffer.append("warnings=" + ht.get("Cards").toString() + "\n");
             m_sHRFBuffer.append("speciality=" + ht.get("Specialty").toString() + "\n");
             m_sHRFBuffer.append("specialityLabel="
                                 + PlayerHelper.getNameForSpeciality(Integer.parseInt(ht.get("Specialty")
                                                                                        .toString()))
                                 + "\n");
             m_sHRFBuffer.append("gentleness=" + ht.get("Agreeability").toString() + "\n");
             m_sHRFBuffer.append("gentlenessLabel="
                                 + PlayerHelper.getNameForGentleness(Integer.parseInt(ht.get("Agreeability")
                                                                                        .toString()))
                                 + "\n");
             m_sHRFBuffer.append("honesty=" + ht.get("Honesty").toString() + "\n");
             m_sHRFBuffer.append("honestyLabel="
                                 + PlayerHelper.getNameForCharacter(Integer.parseInt(ht.get("Honesty")
                                                                                       .toString()))
                                 + "\n");
             m_sHRFBuffer.append("Aggressiveness=" + ht.get("Aggressiveness").toString() + "\n");
             m_sHRFBuffer.append("AggressivenessLabel="
                                 + PlayerHelper.getNameForAggressivness(Integer.parseInt(ht.get("Aggressiveness")
                                                                                           .toString()))
                                 + "\n");
 
             if (ht.get("TrainerSkill") != null) {
                 m_sHRFBuffer.append("TrainerType=" + ht.get("TrainerType").toString() + "\n");
                 m_sHRFBuffer.append("TrainerSkill=" + ht.get("TrainerSkill").toString() + "\n");
             } else {
                 m_sHRFBuffer.append("TrainerType=" + "\n");
                 m_sHRFBuffer.append("TrainerSkill=" + "\n");
             }
 
             if ((m_clTeam != null)
                 && (m_clTeam.getPlayerByID(Integer.parseInt(ht.get("PlayerID").toString())) != null)
                 && (m_clTeam.getPlayerByID(Integer.parseInt(ht.get("PlayerID").toString()))
                             .getRating() >= 0)) {
                 m_sHRFBuffer.append("rating="
                                     + (int) (m_clTeam.getPlayerByID(Integer.parseInt(ht.get("PlayerID")
                                                                                        .toString()))
                                                      .getRating() * 2) + "\n");
             } else {
                 m_sHRFBuffer.append("rating=0" + "\n");
             }
 
             //Bonus
             if ((ht.get("PlayerNumber") != null) || (!ht.get("PlayerNumber").equals(""))) {
                 m_sHRFBuffer.append("PlayerNumber=" + ht.get("PlayerNumber") + "\n");
             }
 
             m_sHRFBuffer.append("TransferListed=" + ht.get("TransferListed") + "\n");
             m_sHRFBuffer.append("NationalTeamID=" + ht.get("NationalTeamID") + "\n");
             m_sHRFBuffer.append("Caps=" + ht.get("Caps") + "\n");
             m_sHRFBuffer.append("CapsU20=" + ht.get("CapsU20") + "\n");
         }
     }
 
     /**
      * Create team related data (training, confidence, formation experience, etc.).
      */
     protected final void createTeam() throws Exception {
         m_sHRFBuffer.append("[team]" + "\n");
         m_sHRFBuffer.append("trLevel=" + m_htTraining.get("TrainingLevel") + "\n");
         m_sHRFBuffer.append("staminaTrainingPart=" + m_htTraining.get("StaminaTrainingPart") + "\n");
         m_sHRFBuffer.append("trTypeValue=" + m_htTraining.get("TrainingType") + "\n");
 		m_sHRFBuffer.append("trType=" + Team.getNameForTraining(Integer.parseInt(m_htTraining.get("TrainingType").toString())) + "\n");
 
 		if ((m_htTraining.get("Morale") != null) && (m_htTraining.get("SelfConfidence") != null)) {
 			m_sHRFBuffer.append("stamningValue=" + m_htTraining.get("Morale") + "\n");
 			m_sHRFBuffer.append("stamning=" + Team.getNameForStimmung(Integer.parseInt(m_htTraining.get("Morale").toString())) + "\n");
 			m_sHRFBuffer.append("sjalvfortroendeValue=" + m_htTraining.get("SelfConfidence") + "\n");
 			m_sHRFBuffer.append("sjalvfortroende=" + Team.getNameForSelbstvertrauen(Integer.parseInt(m_htTraining.get("SelfConfidence").toString()))+ "\n");
         } else {
             m_sHRFBuffer.append("playingMatch=true");
         }
 
         m_sHRFBuffer.append("exper433=" + m_htTraining.get("Experience433") + "\n");
         m_sHRFBuffer.append("exper451=" + m_htTraining.get("Experience451") + "\n");
         m_sHRFBuffer.append("exper352=" + m_htTraining.get("Experience352") + "\n");
         m_sHRFBuffer.append("exper532=" + m_htTraining.get("Experience532") + "\n");
         m_sHRFBuffer.append("exper343=" + m_htTraining.get("Experience343") + "\n");
         m_sHRFBuffer.append("exper541=" + m_htTraining.get("Experience541") + "\n");
         if (m_htTraining.get("Experience442") != null) {
         	m_sHRFBuffer.append("exper442=" + m_htTraining.get("Experience442") + "\n");
         }
         if (m_htTraining.get("Experience523") != null) {
         	m_sHRFBuffer.append("exper523=" + m_htTraining.get("Experience523") + "\n");
         }
         if (m_htTraining.get("Experience550") != null) {
         	m_sHRFBuffer.append("exper550=" + m_htTraining.get("Experience550") + "\n");
         }
         if (m_htTraining.get("Experience253") != null) {
         	m_sHRFBuffer.append("exper253=" + m_htTraining.get("Experience253") + "\n");
         }
     }
 
     /**
      * Create the world data.
      */
     protected final void createWorld() throws Exception {
         m_sHRFBuffer.append("[xtra]\n");
 
         //schon in basics
         //m_sHRFBuffer.append ( "LeagueID=" + m_htWorld.get ( "LeagueID" ) + "\n" );
         //m_sHRFBuffer.append ( "season=" + m_htWorld.get ( "Season" ) + "\n" );
         //m_sHRFBuffer.append ( "matchround=" + m_htWorld.get ( "MatchRound" ) + "\n" );
         //m_sHRFBuffer.append ( "CountryID=" + m_htWorld.get ( "CountryID" ) + "\n" );
         //m_sHRFBuffer.append ( "ownerICQ=" + m_htWorld.get ( "ICQ" ) + "\n" );
         m_sHRFBuffer.append("TrainingDate=" + m_htWorld.get("TrainingDate") + "\n");
         m_sHRFBuffer.append("EconomyDate=" + m_htWorld.get("EconomyDate") + "\n");
         m_sHRFBuffer.append("SeriesMatchDate=" + m_htWorld.get("SeriesMatchDate") + "\n");
         m_sHRFBuffer.append("CurrencyName=" + m_htWorld.get("CurrencyName") + "\n");
         m_sHRFBuffer.append("CurrencyRate="
                             + m_htWorld.get("CurrencyRate").toString().replace(',', '.') + "\n");
         m_sHRFBuffer.append("LogoURL=" + m_htTeamdetails.get("LogoURL") + "\n");
         m_sHRFBuffer.append("HasPromoted=" + m_htClub.get("HasPromoted") + "\n");
 
         m_sHRFBuffer.append("TrainerID=" + m_htTraining.get("TrainerID") + "\n");
         m_sHRFBuffer.append("TrainerName=" + m_htTraining.get("TrainerName") + "\n");
         m_sHRFBuffer.append("ArrivalDate=" + m_htTraining.get("ArrivalDate") + "\n");
         m_sHRFBuffer.append("LeagueLevelUnitID=" + m_htTeamdetails.get("LeagueLevelUnitID") + "\n");
     }
 
     /**
      * Save the HRF file.
      */
     protected final void writeHRF(String dateiname) {
         BufferedWriter out = null;
         final String text = m_sHRFBuffer.toString();
 
         //utf-8
         OutputStreamWriter outWrit = null;
 
         try {
         	File f = new File(dateiname);
 
             if (f.exists()) {
                 f.delete();
             }
 
             f.createNewFile();
 
             //write utf 8
             outWrit = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
             out = new BufferedWriter(outWrit);
 
             //write ansi
             //out = new BufferedWriter( new FileWriter( f ) );
             if (text != null) {
                 out.write(text);
             }
         } catch (Exception except) {
             HOLogger.instance().log(getClass(),except);
         } finally {
         	if (out != null) {
         		try {
 					out.close();
 				} catch (Exception e) {
 				}        	
         	}
         }
     }
 }
