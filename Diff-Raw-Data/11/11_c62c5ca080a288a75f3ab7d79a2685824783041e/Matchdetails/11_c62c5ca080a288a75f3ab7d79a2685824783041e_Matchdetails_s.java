 // %3075081895:de.hattrickorganizer.model.matches%
 /*
  * Matchdetails.java
  *
  * Created on 8. Januar 2004, 14:12
  */
 package de.hattrickorganizer.model.matches;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import plugins.IMatchHighlight;
 import plugins.ITeamLineup;
 import de.hattrickorganizer.model.TeamLineup;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.xml.XMLManager;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author thomas.werth
  */
 public class Matchdetails implements plugins.IMatchDetails {
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** TODO Missing Parameter Documentation */
     private String m_sArenaName = "";
 
     /** TODO Missing Parameter Documentation */
     private String m_sGastName = "";
 
     /** TODO Missing Parameter Documentation */
     private String m_sHeimName = "";
 
     /** TODO Missing Parameter Documentation */
     private String m_sMatchreport = "";
 
     /** TODO Missing Parameter Documentation */
     private Timestamp m_clFetchDatum;
 
     /** TODO Missing Parameter Documentation */
     private Timestamp m_clSpielDatum;
 
     /** TODO Missing Parameter Documentation */
     private Vector m_vHighlights = new Vector();
 
     /** TODO Missing Parameter Documentation */
     private int m_iArenaID = -1;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGastId = -1;
 
     /** TODO Missing Parameter Documentation */
 
     //-1pic,0=nor,1=mots
     private int m_iGuestEinstellung;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestGoals;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestLeftAtt;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestLeftDef;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestMidAtt;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestMidDef;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestMidfield;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestRightAtt;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestRightDef;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestTacticSkill;
 
     /** TODO Missing Parameter Documentation */
     private int m_iGuestTacticType;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHeimId = -1;
 
     /** TODO Missing Parameter Documentation */
 
     //-1pic,0=nor,1=mots, -1000 Unbekannt
     private int m_iHomeEinstellung;
 
     //Ratings
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeGoals;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeLeftAtt;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeLeftDef;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeMidAtt;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeMidDef;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeMidfield;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeRightAtt;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeRightDef;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeTacticSkill;
 
     /** TODO Missing Parameter Documentation */
     private int m_iHomeTacticType;
 
     /** TODO Missing Parameter Documentation */
     private int m_iMatchID = -1;
 
     /** TODO Missing Parameter Documentation */
 
     //0=Regen,1=Bewlkt,2=wolkig,3=Sonne
     private int m_iWetterId = -1;
 
     /** TODO Missing Parameter Documentation */
     private int m_iZuschauer;
 
     /** Region ID */
     private int m_iRegionId;
     //~ Constructors -------------------------------------------------------------------------------
 
     ////////////////////////////////////////////////////////////////////////////////
     //Konstruktor
     ////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Creates a new instance of Matchdetails
      */
     public Matchdetails() {
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Gibt den Namen zu einer Bewertungzurck
      *
      * @param einstellung TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public static String getNameForEinstellung(int einstellung) {
         switch (einstellung) {
             case EINSTELLUNG_PIC:
                 return de.hattrickorganizer.model.HOVerwaltung.instance().getResource().getProperty("PIC");
 
             case EINSTELLUNG_NORMAL:
                 return de.hattrickorganizer.model.HOVerwaltung.instance().getResource().getProperty("Normal");
 
             case EINSTELLUNG_MOTS:
                 return de.hattrickorganizer.model.HOVerwaltung.instance().getResource().getProperty("MOTS");
 
             default:
                 return de.hattrickorganizer.model.HOVerwaltung.instance().getResource().getProperty("Unbestimmt");
         }
     }
 
     ////////////////////////////////////////////////////////////////////////////////
     //Static
     ////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Gibt den Namen zu einer Bewertungzurck
      *
      * @param taktikTyp TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public static String getNameForTaktik(int taktikTyp) {
         final Properties properties = de.hattrickorganizer.model.HOVerwaltung.instance()
                                                                              .getResource();
 
         switch (taktikTyp) {
             case TAKTIK_NORMAL:
                 return properties.getProperty("Normal");
 
             case TAKTIK_PRESSING:
                 return properties.getProperty("TT_Pressing");
 
             case TAKTIK_KONTER:
                 return properties.getProperty("TT_Counter");
 
             case TAKTIK_MIDDLE:
                 return properties.getProperty("TT_MIDDLE");
 
             case TAKTIK_WINGS:
                 return properties.getProperty("TT_WINGS");
 
             case TAKTIK_CREATIVE:
                 return properties.getProperty("tt_creative");
 
             default:
                 return properties.getProperty("Unbestimmt");
         }
     }
 
     /**
      * Setter for property m_iArenaID.
      *
      * @param m_iArenaID New value of property m_iArenaID.
      */
     public final void setArenaID(int m_iArenaID) {
         this.m_iArenaID = m_iArenaID;
     }
 
     /**
      * Getter for property m_iArenaID.
      *
      * @return Value of property m_iArenaID.
      */
     public final int getArenaID() {
         return m_iArenaID;
     }
 
     /**
      * Setter for property m_sArenaName.
      *
      * @param m_sArenaName New value of property m_sArenaName.
      */
     public final void setArenaName(java.lang.String m_sArenaName) {
         this.m_sArenaName = m_sArenaName;
     }
 
     /**
      * Getter for property m_sArenaName.
      *
      * @return Value of property m_sArenaName.
      */
     public final java.lang.String getArenaName() {
         return m_sArenaName;
     }
 
     /**
      * Setter for property m_clFetchDatum.
      *
      * @param m_clFetchDatum New value of property m_clFetchDatum.
      */
     public final void setFetchDatum(java.sql.Timestamp m_clFetchDatum) {
         this.m_clFetchDatum = m_clFetchDatum;
     }
 
     /**
      * Getter for property m_clFetchDatum.
      *
      * @return Value of property m_clFetchDatum.
      */
     public final java.sql.Timestamp getFetchDatum() {
         return m_clFetchDatum;
     }
 
     /**
      * Getter for property m_lDatum.
      *
      * @param date TODO Missing Constructuor Parameter Documentation
      *
      * @return Value of property m_lDatum.
      */
     public final boolean setFetchDatumFromString(String date) {
         try {
             //Hattrick
             final java.text.SimpleDateFormat simpleFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                                                                            java.util.Locale.GERMANY);
 
             m_clFetchDatum = new java.sql.Timestamp(simpleFormat.parse(date).getTime());
         } catch (Exception e) {
             try {
                 //Hattrick
                 final java.text.SimpleDateFormat simpleFormat = new java.text.SimpleDateFormat("yyyy-MM-dd",
                                                                                                java.util.Locale.GERMANY);
 
                 m_clFetchDatum = new java.sql.Timestamp(simpleFormat.parse(date).getTime());
             } catch (Exception ex) {
                 HOLogger.instance().log(getClass(),ex);
                 m_clFetchDatum = null;
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Method that extract the formation from the full match comment
      *
      * @param home true for home lineup, false for away
      *
      * @return The formation type 4-4-2 or 5-3-2 etc
      */
     public final String getFormation(boolean home) {
         try {
             final Pattern p = Pattern.compile(".-.-.");
             final Matcher m = p.matcher(m_sMatchreport);
             m.find();
 
             if (!home) {
                 m.find();
             }
 
             return m.group();
         } catch (RuntimeException e) {
             return "";
         }
     }
 
     /**
      * Setter for property m_iGastId.
      *
      * @param m_iGastId New value of property m_iGastId.
      */
     public final void setGastId(int m_iGastId) {
         this.m_iGastId = m_iGastId;
     }
 
     /**
      * Getter for property m_iGastId.
      *
      * @return Value of property m_iGastId.
      */
     public final int getGastId() {
         return m_iGastId;
     }
 
     /**
      * Setter for property m_sGastName.
      *
      * @param m_sGastName New value of property m_sGastName.
      */
     public final void setGastName(java.lang.String m_sGastName) {
         this.m_sGastName = m_sGastName;
     }
 
     /**
      * Getter for property m_sGastName.
      *
      * @return Value of property m_sGastName.
      */
     public final java.lang.String getGastName() {
         return m_sGastName;
     }
 
     /**
      * Setter for property m_iGuestEinstellung.
      *
      * @param m_iGuestEinstellung New value of property m_iGuestEinstellung.
      */
     public final void setGuestEinstellung(int m_iGuestEinstellung) {
         this.m_iGuestEinstellung = m_iGuestEinstellung;
     }
 
     /**
      * Getter for property m_iGuestEinstellung.
      *
      * @return Value of property m_iGuestEinstellung.
      */
     public final int getGuestEinstellung() {
         return m_iGuestEinstellung;
     }
 
     /**
      * Gibt die Gesamtstrke zurck, Absolut oder durch die Anzahl der Summanten geteilt
      *
      * @param absolut TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getGuestGesamtstaerke(boolean absolut) {
         double summe = 0d;
         summe += getGuestLeftDef();
         summe += getGuestMidDef();
         summe += getGuestRightDef();
         summe += (getGuestMidfield() * 3);
         summe += getGuestLeftAtt();
         summe += getGuestMidAtt();
         summe += getGuestRightAtt();
 
         if (!absolut) {
             summe /= 9d;
         }
 
         return (float) summe;
     }
 
     /**
      * Setter for property m_iGuestGoals.
      *
      * @param m_iGuestGoals New value of property m_iGuestGoals.
      */
     public final void setGuestGoals(int m_iGuestGoals) {
         this.m_iGuestGoals = m_iGuestGoals;
     }
 
     /**
      * Getter for property m_iGuestGoals.
      *
      * @return Value of property m_iGuestGoals.
      */
     public final int getGuestGoals() {
         return m_iGuestGoals;
     }
 
     /**
      * Setter for property m_iGuestLeftAtt.
      *
      * @param m_iGuestLeftAtt New value of property m_iGuestLeftAtt.
      */
     public final void setGuestLeftAtt(int m_iGuestLeftAtt) {
         this.m_iGuestLeftAtt = m_iGuestLeftAtt;
     }
 
     /**
      * Getter for property m_iGuestLeftAtt.
      *
      * @return Value of property m_iGuestLeftAtt.
      */
     public final int getGuestLeftAtt() {
         return m_iGuestLeftAtt;
     }
 
     /**
      * Setter for property m_iGuestLeftDef.
      *
      * @param m_iGuestLeftDef New value of property m_iGuestLeftDef.
      */
     public final void setGuestLeftDef(int m_iGuestLeftDef) {
         this.m_iGuestLeftDef = m_iGuestLeftDef;
     }
 
     /**
      * Getter for property m_iGuestLeftDef.
      *
      * @return Value of property m_iGuestLeftDef.
      */
     public final int getGuestLeftDef() {
         return m_iGuestLeftDef;
     }
 
     /**
      * Setter for property m_iGuestMidAtt.
      *
      * @param m_iGuestMidAtt New value of property m_iGuestMidAtt.
      */
     public final void setGuestMidAtt(int m_iGuestMidAtt) {
         this.m_iGuestMidAtt = m_iGuestMidAtt;
     }
 
     /**
      * Getter for property m_iGuestMidAtt.
      *
      * @return Value of property m_iGuestMidAtt.
      */
     public final int getGuestMidAtt() {
         return m_iGuestMidAtt;
     }
 
     /**
      * Setter for property m_iGuestMidDef.
      *
      * @param m_iGuestMidDef New value of property m_iGuestMidDef.
      */
     public final void setGuestMidDef(int m_iGuestMidDef) {
         this.m_iGuestMidDef = m_iGuestMidDef;
     }
 
     /**
      * Getter for property m_iGuestMidDef.
      *
      * @return Value of property m_iGuestMidDef.
      */
     public final int getGuestMidDef() {
         return m_iGuestMidDef;
     }
 
     /**
      * Setter for property m_iGuestMidfield.
      *
      * @param m_iGuestMidfield New value of property m_iGuestMidfield.
      */
     public final void setGuestMidfield(int m_iGuestMidfield) {
         this.m_iGuestMidfield = m_iGuestMidfield;
     }
 
     /**
      * Getter for property m_iGuestMidfield.
      *
      * @return Value of property m_iGuestMidfield.
      */
     public final int getGuestMidfield() {
         return m_iGuestMidfield;
     }
 
     /**
      * Setter for property m_iGuestRightAtt.
      *
      * @param m_iGuestRightAtt New value of property m_iGuestRightAtt.
      */
     public final void setGuestRightAtt(int m_iGuestRightAtt) {
         this.m_iGuestRightAtt = m_iGuestRightAtt;
     }
 
     /**
      * Getter for property m_iGuestRightAtt.
      *
      * @return Value of property m_iGuestRightAtt.
      */
     public final int getGuestRightAtt() {
         return m_iGuestRightAtt;
     }
 
     /**
      * Setter for property m_iGuestRightDef.
      *
      * @param m_iGuestRightDef New value of property m_iGuestRightDef.
      */
     public final void setGuestRightDef(int m_iGuestRightDef) {
         this.m_iGuestRightDef = m_iGuestRightDef;
     }
 
     /**
      * Getter for property m_iGuestRightDef.
      *
      * @return Value of property m_iGuestRightDef.
      */
     public final int getGuestRightDef() {
         return m_iGuestRightDef;
     }
 
     /**
      * Setter for property m_iGuestTacticSkill.
      *
      * @param m_iGuestTacticSkill New value of property m_iGuestTacticSkill.
      */
     public final void setGuestTacticSkill(int m_iGuestTacticSkill) {
         this.m_iGuestTacticSkill = m_iGuestTacticSkill;
     }
 
     /**
      * Getter for property m_iGuestTacticSkill.
      *
      * @return Value of property m_iGuestTacticSkill.
      */
     public final int getGuestTacticSkill() {
         return m_iGuestTacticSkill;
     }
 
     /**
      * Setter for property m_iGuestTacticType.
      *
      * @param m_iGuestTacticType New value of property m_iGuestTacticType.
      */
     public final void setGuestTacticType(int m_iGuestTacticType) {
         this.m_iGuestTacticType = m_iGuestTacticType;
     }
 
     /**
      * Getter for property m_iGuestTacticType.
      *
      * @return Value of property m_iGuestTacticType.
      */
     public final int getGuestTacticType() {
         return m_iGuestTacticType;
     }
 
     /**
      * Setter for property m_iHeimId.
      *
      * @param m_iHeimId New value of property m_iHeimId.
      */
     public final void setHeimId(int m_iHeimId) {
         this.m_iHeimId = m_iHeimId;
     }
 
     ////////////////////////////////////////////////////////////////////////////////
     //Accessor
     ////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Getter for property m_iHeimId.
      *
      * @return Value of property m_iHeimId.
      */
     public final int getHeimId() {
         return m_iHeimId;
     }
 
     /**
      * Setter for property m_sHeimName.
      *
      * @param m_sHeimName New value of property m_sHeimName.
      */
     public final void setHeimName(java.lang.String m_sHeimName) {
         this.m_sHeimName = m_sHeimName;
     }
 
     /**
      * Getter for property m_sHeimName.
      *
      * @return Value of property m_sHeimName.
      */
     public final java.lang.String getHeimName() {
         return m_sHeimName;
     }
 
     /**
      * Setter for property m_vHighlights.
      *
      * @param m_vHighlights New value of property m_vHighlights.
      */
     public final void setHighlights(java.util.Vector m_vHighlights) {
         this.m_vHighlights = m_vHighlights;
     }
 
     /**
      * Getter for property m_vHighlights.
      *
      * @return Value of property m_vHighlights.
      */
     public final java.util.Vector getHighlights() {
         return m_vHighlights;
     }
 
     /**
      * Setter for property m_iHomeEinstellung.
      *
      * @param m_iHomeEinstellung New value of property m_iHomeEinstellung.
      */
     public final void setHomeEinstellung(int m_iHomeEinstellung) {
         this.m_iHomeEinstellung = m_iHomeEinstellung;
     }
 
     /**
      * Getter for property m_iHomeEinstellung.
      *
      * @return Value of property m_iHomeEinstellung.
      */
     public final int getHomeEinstellung() {
         return m_iHomeEinstellung;
     }
 
     /**
      * Gibt die Gesamtstrke zurck, Absolut oder durch die Anzahl der Summanten geteilt
      *
      * @param absolut TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final float getHomeGesamtstaerke(boolean absolut) {
         double summe = 0d;
         summe += getHomeLeftDef();
         summe += getHomeMidDef();
         summe += getHomeRightDef();
         summe += (getHomeMidfield() * 3);
         summe += getHomeLeftAtt();
         summe += getHomeMidAtt();
         summe += getHomeRightAtt();
 
         if (!absolut) {
             summe /= 9d;
         }
 
         return (float) summe;
     }
 
     /**
      * Setter for property m_iHomeGoals.
      *
      * @param m_iHomeGoals New value of property m_iHomeGoals.
      */
     public final void setHomeGoals(int m_iHomeGoals) {
         this.m_iHomeGoals = m_iHomeGoals;
     }
 
     /**
      * Getter for property m_iHomeGoals.
      *
      * @return Value of property m_iHomeGoals.
      */
     public final int getHomeGoals() {
         return m_iHomeGoals;
     }
 
     /**
      * Setter for property m_iHomeLeftAtt.
      *
      * @param m_iHomeLeftAtt New value of property m_iHomeLeftAtt.
      */
     public final void setHomeLeftAtt(int m_iHomeLeftAtt) {
         this.m_iHomeLeftAtt = m_iHomeLeftAtt;
     }
 
     /**
      * Getter for property m_iHomeLeftAtt.
      *
      * @return Value of property m_iHomeLeftAtt.
      */
     public final int getHomeLeftAtt() {
         return m_iHomeLeftAtt;
     }
 
     /**
      * Setter for property m_iHomeLeftDef.
      *
      * @param m_iHomeLeftDef New value of property m_iHomeLeftDef.
      */
     public final void setHomeLeftDef(int m_iHomeLeftDef) {
         this.m_iHomeLeftDef = m_iHomeLeftDef;
     }
 
     /**
      * Getter for property m_iHomeLeftDef.
      *
      * @return Value of property m_iHomeLeftDef.
      */
     public final int getHomeLeftDef() {
         return m_iHomeLeftDef;
     }
 
     /**
      * Setter for property m_iHomeMidAtt.
      *
      * @param m_iHomeMidAtt New value of property m_iHomeMidAtt.
      */
     public final void setHomeMidAtt(int m_iHomeMidAtt) {
         this.m_iHomeMidAtt = m_iHomeMidAtt;
     }
 
     /**
      * Getter for property m_iHomeMidAtt.
      *
      * @return Value of property m_iHomeMidAtt.
      */
     public final int getHomeMidAtt() {
         return m_iHomeMidAtt;
     }
 
     /**
      * Setter for property m_iHomeMidDef.
      *
      * @param m_iHomeMidDef New value of property m_iHomeMidDef.
      */
     public final void setHomeMidDef(int m_iHomeMidDef) {
         this.m_iHomeMidDef = m_iHomeMidDef;
     }
 
     /**
      * Getter for property m_iHomeMidDef.
      *
      * @return Value of property m_iHomeMidDef.
      */
     public final int getHomeMidDef() {
         return m_iHomeMidDef;
     }
 
     /**
      * Setter for property m_iHomeMidfield.
      *
      * @param m_iHomeMidfield New value of property m_iHomeMidfield.
      */
     public final void setHomeMidfield(int m_iHomeMidfield) {
         this.m_iHomeMidfield = m_iHomeMidfield;
     }
 
     /**
      * Getter for property m_iHomeMidfield.
      *
      * @return Value of property m_iHomeMidfield.
      */
     public final int getHomeMidfield() {
         return m_iHomeMidfield;
     }
 
     /**
      * Setter for property m_iHomeRightAtt.
      *
      * @param m_iHomeRightAtt New value of property m_iHomeRightAtt.
      */
     public final void setHomeRightAtt(int m_iHomeRightAtt) {
         this.m_iHomeRightAtt = m_iHomeRightAtt;
     }
 
     /**
      * Getter for property m_iHomeRightAtt.
      *
      * @return Value of property m_iHomeRightAtt.
      */
     public final int getHomeRightAtt() {
         return m_iHomeRightAtt;
     }
 
     /**
      * Setter for property m_iHomeRightDef.
      *
      * @param m_iHomeRightDef New value of property m_iHomeRightDef.
      */
     public final void setHomeRightDef(int m_iHomeRightDef) {
         this.m_iHomeRightDef = m_iHomeRightDef;
     }
 
     /**
      * Getter for property m_iHomeRightDef.
      *
      * @return Value of property m_iHomeRightDef.
      */
     public final int getHomeRightDef() {
         return m_iHomeRightDef;
     }
 
     /**
      * Setter for property m_iHomeTacticSkill.
      *
      * @param m_iHomeTacticSkill New value of property m_iHomeTacticSkill.
      */
     public final void setHomeTacticSkill(int m_iHomeTacticSkill) {
         this.m_iHomeTacticSkill = m_iHomeTacticSkill;
     }
 
     /**
      * Getter for property m_iHomeTacticSkill.
      *
      * @return Value of property m_iHomeTacticSkill.
      */
     public final int getHomeTacticSkill() {
         return m_iHomeTacticSkill;
     }
 
     /**
      * Setter for property m_iHomeTacticType.
      *
      * @param m_iHomeTacticType New value of property m_iHomeTacticType.
      */
     public final void setHomeTacticType(int m_iHomeTacticType) {
         this.m_iHomeTacticType = m_iHomeTacticType;
     }
 
     /**
      * Getter for property m_iHomeTacticType.
      *
      * @return Value of property m_iHomeTacticType.
      */
     public final int getHomeTacticType() {
         return m_iHomeTacticType;
     }
 
     /**
      * Method that extract a lineup from the full match comment
      *
      * @param home true for home lineup, false for away
      *
      * @return The list of last names that started the match
      */
     public final List getLineup(boolean home) {
         try {
             final Pattern p = Pattern.compile(".-.-.");
             final Matcher m = p.matcher(m_sMatchreport);
             m.find();
 
             if (!home) {
                 m.find();
             }
 
             int start = m.end();
             start = m_sMatchreport.indexOf(":", start);
 
             final int end = m_sMatchreport.indexOf(".", start);
             final List lineup = new ArrayList();
 
             final String[] zones = m_sMatchreport.substring(start + 1, end).split(" - ");
 
             for (int i = 0; i < zones.length; i++) {
                 final String[] pNames = zones[i].split(",");
 
                 for (int j = 0; j < pNames.length; j++) {
                     lineup.add(pNames[j]);
                 }
             }
 
             return lineup;
         } catch (RuntimeException e) {
             return new ArrayList();
         }
     }
 
     /**
      * Setter for property m_iMatchID.
      *
      * @param m_iMatchID New value of property m_iMatchID.
      */
     public final void setMatchID(int m_iMatchID) {
         this.m_iMatchID = m_iMatchID;
     }
 
     /**
      * Getter for property m_iMatchID.
      *
      * @return Value of property m_iMatchID.
      */
     public final int getMatchID() {
         return m_iMatchID;
     }
 
     /**
      * get match report from ht server for matchid parses for highlights and creates matchreport
      * String
      *
      * @param matchID TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final String getMatchReport(String matchID) {
         m_vHighlights = getMatchHighlights(matchID);
 
         final StringBuffer report = new StringBuffer();
 
         for (int k = 0; k < m_vHighlights.size(); k++) {
             final MatchHighlight tmp = (MatchHighlight) m_vHighlights.get(k);
             report.append(tmp.getEventText()+" ");
         }
 
         return report.toString();
     }
 
     /**
      * Setter for property m_sMatchreport.
      *
      * @param m_sMatchreport New value of property m_sMatchreport.
      */
     public final void setMatchreport(java.lang.String m_sMatchreport) {
         this.m_sMatchreport = m_sMatchreport;
     }
 
     /**
      * Getter for property m_sMatchreport.
      *
      * @return Value of property m_sMatchreport.
      */
     public final java.lang.String getMatchreport() {
         return m_sMatchreport;
     }
 
     /**
      * Setter for property m_clSpielDatum.
      *
      * @param m_clSpielDatum New value of property m_clSpielDatum.
      */
     public final void setSpielDatum(java.sql.Timestamp m_clSpielDatum) {
         this.m_clSpielDatum = m_clSpielDatum;
     }
 
     /**
      * Getter for property m_clSpielDatum.
      *
      * @return Value of property m_clSpielDatum.
      */
     public final java.sql.Timestamp getSpielDatum() {
         return m_clSpielDatum;
     }
 
     /**
      * Getter for property m_lDatum.
      *
      * @param date TODO Missing Constructuor Parameter Documentation
      *
      * @return Value of property m_lDatum.
      */
     public final boolean setSpielDatumFromString(String date) {
         try {
             //Hattrick
             final java.text.SimpleDateFormat simpleFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                                                                            java.util.Locale.GERMANY);
 
             m_clSpielDatum = new java.sql.Timestamp(simpleFormat.parse(date).getTime());
         } catch (Exception e) {
             try {
                 //Hattrick
                 final java.text.SimpleDateFormat simpleFormat = new java.text.SimpleDateFormat("yyyy-MM-dd",
                                                                                                java.util.Locale.GERMANY);
 
                 m_clSpielDatum = new java.sql.Timestamp(simpleFormat.parse(date).getTime());
             } catch (Exception ex) {
                 HOLogger.instance().log(getClass(),ex);
                 m_clSpielDatum = null;
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Method that extract a lineup from the full match comment
      *
      * @param home true for home lineup, false for away
      *
      * @return The Lineup object
      */
     public final ITeamLineup getTeamLineup(boolean home) {
         final TeamLineup lineup = new TeamLineup();
 
         try {
             final Pattern p = Pattern.compile(".-.-.");
             final Matcher m = p.matcher(m_sMatchreport);
             m.find();
 
             if (!home) {
                 m.find();
             }
 
             int start = m.end();
             start = m_sMatchreport.indexOf(":", start);
 
             final int end = m_sMatchreport.indexOf(".", start);
 
             final String[] zones = m_sMatchreport.substring(start + 1, end).split(" - ");
 
             for (int i = 0; i < zones.length; i++) {
                 final String[] pNames = zones[i].split(",");
 
                 for (int j = 0; j < pNames.length; j++) {
                     lineup.add(pNames[j], i);
                 }
             }
         } catch (RuntimeException e) {
             return new TeamLineup();
         }
 
         return lineup;
     }
 
     /**
      * Setter for property m_iWetterId.
      *
      * @param m_iWetterId New value of property m_iWetterId.
      */
     public final void setWetterId(int m_iWetterId) {
         this.m_iWetterId = m_iWetterId;
     }
 
     /**
      * Getter for property m_iWetterId.
      *
      * @return Value of property m_iWetterId.
      */
     public final int getWetterId() {
         return m_iWetterId;
     }
 
     /**
      * Setter for property m_iZuschauer.
      *
      * @param m_iZuschauer New value of property m_iZuschauer.
      */
     public final void setZuschauer(int m_iZuschauer) {
         this.m_iZuschauer = m_iZuschauer;
     }
 
     /**
      * Getter for property m_iZuschauer.
      *
      * @return Value of property m_iZuschauer.
      */
     public final int getZuschauer() {
         return m_iZuschauer;
     }
 
     /**
      * get match report for matchid
      *
      * @param matchID TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final Vector getMatchHighlights(String matchID) {
         Vector myMatchHighlights = new Vector();
 
         //Check if numeric (use only if unsure)
         if (isNumeric(matchID)) {
             //add match to ht live
             String myMatch = "";
 
             try {
                 myMatch = de.hattrickorganizer.net.MyConnector.instance().getHattrickXMLFile("/chppxml.axd?file=live&actionType=addMatch&matchID="
                                                                                              + matchID);
             } catch (Exception ex) {
                 HOLogger.instance().log(getClass(),ex);
             }
 
             //parse match report
             myMatchHighlights = parseMatchReport(XMLManager.instance().parseString(myMatch), matchID);
 
             //delete match from ht live
             try {
                 myMatch = de.hattrickorganizer.net.MyConnector.instance().getHattrickXMLFile("/chppxml.axd?file=live&actionType=deleteMatch&matchID="
                                                                                              + matchID);
             } catch (Exception ex) {
                 HOLogger.instance().log(getClass(),ex);
             }
         }
 
         return myMatchHighlights;
     }
 
     /**
      * check if numeric
      *
      * @param aText TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final boolean isNumeric(String aText) {
         for (int k = 0; k < aText.length(); k++) {
             if (!((aText.charAt(k) >= '0') && (aText.charAt(k) <= '9'))) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * parse the match report
      *
      * @param doc TODO Missing Constructuor Parameter Documentation
      * @param match TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final Vector parseMatchReport(Document doc, String match) {
         final Vector myHighlights = new Vector();
         final Vector broken = new Vector();
         Element ele = null;
         Element tmpEle = null;
         Element matchList = null;
         Element eventList = null;
 
         try {
             //get Root element
             ele = doc.getDocumentElement();
 
             //get matchlist element
             matchList = (Element) ele.getElementsByTagName("MatchList").item(0);
 
             int k = 0;
 
             while (k < matchList.getElementsByTagName("Match").getLength()) {
                 //find the right match
                 tmpEle = (Element) matchList.getElementsByTagName("Match").item(k);
 
                 final String value = XMLManager.instance().getFirstChildNodeValue((Element) tmpEle.getElementsByTagName("MatchID")
                                                                                                   .item(0));
 
                 if (value.equals(match)) {
                     //get both teams
                     String tmpTeam = "";
                     ele = (Element) tmpEle.getElementsByTagName("HomeTeam").item(0);
 
                     final String homeTeamID = XMLManager.instance().getFirstChildNodeValue((Element) ele.getElementsByTagName("HomeTeamID")
                                                                                                         .item(0));
                     ele = (Element) tmpEle.getElementsByTagName("AwayTeam").item(0);
 
                     final String awayTeamID = XMLManager.instance().getFirstChildNodeValue((Element) ele.getElementsByTagName("AwayTeamID")
                                                                                                         .item(0));
 
                     try {
                         tmpTeam = de.hattrickorganizer.net.MyConnector.instance()
                                                                       .getHattrickXMLFile("/chppxml.axd?file=matchlineup&&matchID="
                                                                                           + match
                                                                                           + "&teamID="
                                                                                           + homeTeamID);
                     } catch (Exception ex) {
                         HOLogger.instance().log(getClass(),ex);
                     }
 
                     //parse players
                     final Vector homeTeamPlayers = parsePlayers(XMLManager.instance().parseString(tmpTeam));
 
                     try {
                         tmpTeam = de.hattrickorganizer.net.MyConnector.instance()
                                                                       .getHattrickXMLFile("/chppxml.axd?file=matchlineup&&matchID="
                                                                                           + match
                                                                                           + "&teamID="
                                                                                           + awayTeamID);
                     } catch (Exception ex) {
                         HOLogger.instance().log(getClass(),ex);
                     }
 
                     //parse players
                     final Vector awayTeamPlayers = parsePlayers(XMLManager.instance().parseString(tmpTeam));
 
                     //now go through the eventlist and add everything together
                     eventList = (Element) tmpEle.getElementsByTagName("EventList").item(0);
 
                     int homeGoals = 0;
                     int awayGoals = 0;
                     int n = 0;
 
                     while (n < eventList.getElementsByTagName("Event").getLength()) {
                         tmpEle = (Element) eventList.getElementsByTagName("Event").item(n);
 
                         //get values from xml
                         final int minute = (Integer.valueOf(XMLManager.instance()
                                                                       .getFirstChildNodeValue((Element) tmpEle.getElementsByTagName("Minute")
                                                                                                               .item(0))))
                                            .intValue();
                         final int subjectplayerid = (Integer.valueOf(XMLManager.instance()
                                                                                .getFirstChildNodeValue((Element) tmpEle.getElementsByTagName("SubjectPlayerID")
                                                                                                                        .item(0))))
                                                     .intValue();
                         final int subjectteamid = (Integer.valueOf(XMLManager.instance()
                                                                              .getFirstChildNodeValue((Element) tmpEle.getElementsByTagName("SubjectTeamID")
                                                                                                                      .item(0))))
                                                   .intValue();
                         final int objectplayerid = (Integer.valueOf(XMLManager.instance()
                                                                               .getFirstChildNodeValue((Element) tmpEle.getElementsByTagName("ObjectPlayerID")
                                                                                                                       .item(0))))
                                                    .intValue();
                         final String eventkey = (XMLManager.instance()
                                                            .getFirstChildNodeValue((Element) tmpEle.getElementsByTagName("EventKey")
                                                                                                    .item(0)))
                                                 .split("_")[0];
                         String eventtext = XMLManager.instance().getFirstChildNodeValue((Element) tmpEle.getElementsByTagName("EventText")
                                                                                                         .item(0));
                         eventtext = eventtext.replaceAll("&lt;", "<");
                         eventtext = eventtext.replaceAll("&gt;", ">");
                         eventtext = eventtext.replaceAll("/>", ">");
                         eventtext = eventtext.replaceAll("&quot;", "\"");
                         eventtext = eventtext.replaceAll("&amp;", "&");
 
                         final int highlighttyp = (Integer.valueOf(eventkey)).intValue() / 100;
                         final int highlightsubtyp = (Integer.valueOf(eventkey)).intValue()
                                                     - (((Integer.valueOf(eventkey)).intValue() / 100) * 100);
 
                         //determine new score
                         if (highlighttyp == IMatchHighlight.HIGHLIGHT_ERFOLGREICH) {
                             if (String.valueOf(subjectteamid).equals(homeTeamID)) {
                                 homeGoals++;
                             } else {
                                 awayGoals++;
                             }
                         }
 
                         //get names for players
                         String subjectplayername = "";
                         String objectplayername = "";
                         boolean subHome = true;
                         boolean objHome = true;
 
                         if (minute > 0) {
                             int i = 0;
 
                             while (i < homeTeamPlayers.size()) {
                                 if ((!subjectplayername.equals(""))
                                     && (!objectplayername.equals(""))) {
                                     break;
                                 }
 
                                 final Vector tmpPlayer = (Vector) homeTeamPlayers.get(i);
 
                                 if (tmpPlayer.get(0).toString().equals(String.valueOf(subjectplayerid))) {
                                     subjectplayername = tmpPlayer.get(1).toString();
                                 }
 
                                 if (tmpPlayer.get(0).toString().equals(String.valueOf(objectplayerid))) {
                                     objectplayername = tmpPlayer.get(1).toString();
                                 }
 
                                 i++;
                             }
 
                             i = 0;
 
                             while (i < awayTeamPlayers.size()) {
                                 if ((!subjectplayername.equals(""))
                                     && (!objectplayername.equals(""))) {
                                     break;
                                 }
 
                                 final Vector tmpPlayer = (Vector) awayTeamPlayers.get(i);
 
                                 if (tmpPlayer.get(0).toString().equals(String.valueOf(subjectplayerid))) {
                                     subjectplayername = tmpPlayer.get(1).toString();
                                     subHome = false;
                                 }
 
                                 if (tmpPlayer.get(0).toString().equals(String.valueOf(objectplayerid))) {
                                     objectplayername = tmpPlayer.get(1).toString();
                                     objHome = false;
                                 }
 
                                 i++;
                             }
                         }
 
                         //add single player
                         if (minute > 0) {
                             switch ((highlighttyp * 100) + highlightsubtyp) {
                                 case 40:
                                 case 45:
                                 case 47:
                                 case 60:
                                 case 61:
                                 case 62:
                                 case 63:
                                 case 64:
                                 case 65:
                                 case 68:
                                 case 70:
                                 case 71:
                                 case 72:
                                 case 331:
                                 case 332:
                                 case 333:
                                 case 334:
                                 case 599:
                                     break;
 
                                 default:
 
                                     if (subjectplayername.equals("") && (subjectplayerid != 0)) {
                                         if (eventtext.indexOf(String.valueOf(subjectplayerid)) >= 0) {
                                             String plname = eventtext.substring(eventtext.indexOf(String
                                                                                                   .valueOf(subjectplayerid)));
                                             plname = plname.substring(plname.indexOf(">") + 1);
                                             plname = plname.substring(0, plname.indexOf("<"));
                                             subjectplayername = plname;
 
                                             final Vector tmpplay = new Vector();
                                             tmpplay.add(String.valueOf(subjectplayerid));
                                             tmpplay.add(plname);
 
                                             if (homeTeamID.equals(String.valueOf(subjectteamid))) {
                                                 homeTeamPlayers.add(tmpplay);
                                             } else {
                                                 awayTeamPlayers.add(tmpplay);
                                                 subHome = false;
                                             }
                                         } else {
                                             subjectplayername = String.valueOf(subjectplayerid);
                                             broken.add(new Integer(myHighlights.size()));
                                         }
                                     }
 
                                     if (objectplayername.equals("") && (objectplayerid != 0)) {
                                         if (eventtext.indexOf(String.valueOf(objectplayerid)) >= 0) {
                                             String plname = eventtext.substring(eventtext.indexOf(String
                                                                                                   .valueOf(objectplayerid)));
                                             plname = plname.substring(plname.indexOf(">") + 1);
                                             plname = plname.substring(0, plname.indexOf("<"));
                                             objectplayername = plname;
 
                                             final Vector tmpplay = new Vector();
                                             tmpplay.add(String.valueOf(objectplayerid));
                                             tmpplay.add(plname);
 
                                             //there is no easy solution to find out for which team this
                                             //players is playing. it's more possible that he's playing
                                             //in home team, so we go like this
                                             homeTeamPlayers.add(tmpplay);
                                         } else {
                                             objectplayername = String.valueOf(objectplayerid);
                                             broken.add(new Integer(myHighlights.size()));
                                         }
                                     }
                             }
                         }
 
                         //modify eventtext
                         if (!subjectplayername.equals("")) {
                             String subplayerColor = "#000000";
 
                             if (subHome) {
                                 subplayerColor = "#000099";
                             } else {
                                 subplayerColor = "#990000";
                             }
 
                             String objplayerColor = "#000000";
 
                             if (objHome) {
                                 objplayerColor = "#000099";
                             } else {
                                 objplayerColor = "#990000";
                             }
 
                             boolean replaceend = false;
 
                             if (eventtext.indexOf(String.valueOf(subjectplayerid)) >= 0) {
                                eventtext = eventtext.replaceAll("(?i)<A HREF=\"/Common/PlayerDetails\\.asp\\?PlayerID="
                                                                  + subjectplayerid + ".*?>",
                                                                  "<FONT COLOR=" + subplayerColor + "#><B>");
                                 replaceend = true;
                             }
 
                             if (eventtext.indexOf(String.valueOf(objectplayerid)) >= 0) {
                                eventtext = eventtext.replaceAll("(?i)<A HREF=\"/Common/PlayerDetails\\.asp\\?PlayerID="
                                                                  + objectplayerid + ".*?>",
                                                                  "<FONT COLOR=" + objplayerColor + "#><B>");
                                 replaceend = true;
                             }
 
                             if (replaceend) {
                                 eventtext = eventtext.replaceAll("(?i)</A>", "</B></FONT>");
                             }
                         }
 
                         //generate MatchHighlight and add to list
                         final MatchHighlight myHighlight = new MatchHighlight();
                         myHighlight.setHighlightTyp(highlighttyp);
                         myHighlight.setHighlightSubTyp(highlightsubtyp);
                         myHighlight.setMinute(minute);
                         myHighlight.setHeimTore(homeGoals);
                         myHighlight.setGastTore(awayGoals);
                         myHighlight.setSpielerID(subjectplayerid);
                         myHighlight.setSpielerName(subjectplayername);
                         myHighlight.setSpielerHeim(subHome);
                         myHighlight.setTeamID(subjectteamid);
                         myHighlight.setGehilfeID(objectplayerid);
                         myHighlight.setGehilfeName(objectplayername);
                         myHighlight.setGehilfeHeim(objHome);
                         myHighlight.setEventText(eventtext);
                         myHighlights.add(myHighlight);
 
                         //break if end of match (due to some corrupt xmls)
                         if ((highlighttyp == IMatchHighlight.HIGHLIGHT_KARTEN)
                             && (highlightsubtyp == IMatchHighlight.HIGHLIGHT_SUB_SPIELENDE)) {
                             break;
                         }
 
                         n++;
                     }
 
                     // check for redcarded highlights
                     for (int i = 0; i < broken.size(); i++) {
                         final int tmpid = ((Integer) broken.get(i)).intValue();
                         final MatchHighlight tmp = (MatchHighlight) myHighlights.get(tmpid);
 
                         String subjectplayername = "";
                         String objectplayername = "";
                         boolean subHome = true;
                         boolean objHome = true;
                         int j = 0;
 
                         while (j < homeTeamPlayers.size()) {
                             if ((!subjectplayername.equals("")) && (!objectplayername.equals(""))) {
                                 break;
                             }
 
                             final Vector tmpPlayer = (Vector) homeTeamPlayers.get(j);
 
                             if (tmpPlayer.get(0).toString().equals(String.valueOf(tmp.getSpielerID()))) {
                                 subjectplayername = tmpPlayer.get(1).toString();
                             }
 
                             if (tmpPlayer.get(0).toString().equals(String.valueOf(tmp.getGehilfeID()))) {
                                 objectplayername = tmpPlayer.get(1).toString();
                             }
 
                             j++;
                         }
 
                         j = 0;
 
                         while (j < awayTeamPlayers.size()) {
                             if ((!subjectplayername.equals("")) && (!objectplayername.equals(""))) {
                                 break;
                             }
 
                             final Vector tmpPlayer = (Vector) awayTeamPlayers.get(j);
 
                             if (tmpPlayer.get(0).toString().equals(String.valueOf(tmp.getSpielerID()))) {
                                 subjectplayername = tmpPlayer.get(1).toString();
                                 subHome = false;
                             }
 
                             if (tmpPlayer.get(0).toString().equals(String.valueOf(tmp.getGehilfeID()))) {
                                 objectplayername = tmpPlayer.get(1).toString();
                                 objHome = false;
                             }
 
                             j++;
                         }
 
                         if (!subjectplayername.equals("")) {
                             String subplayerColor = "#000000";
 
                             if (subHome) {
                                 subplayerColor = "#009900";
                             } else {
                                 subplayerColor = "#990000";
                             }
 
                             String objplayerColor = "#000000";
 
                             if (objHome) {
                                 objplayerColor = "#009900";
                             } else {
                                 objplayerColor = "#990000";
                             }
 
                             String eventtext = tmp.getEventText();
                             boolean replaceend = false;
 
                             if (eventtext.indexOf(String.valueOf(tmp.getSpielerID())) >= 0) {
                                eventtext = eventtext.replaceAll("(?i)<A HREF=\"/Common/PlayerDetails\\.asp\\?PlayerID="
                                                                  + tmp.getSpielerID() + ".*?>",
                                                                  "<FONT COLOR=" + subplayerColor + "#><B>");
                                 replaceend = true;
                             }
 
                             if (eventtext.indexOf(String.valueOf(tmp.getGehilfeID())) >= 0) {
                                eventtext = eventtext.replaceAll("(?i)<A HREF=\"/Common/PlayerDetails\\.asp\\?PlayerID="
                                                                  + tmp.getGehilfeID() + ".*?>",
                                                                  "<FONT COLOR=" + objplayerColor + "#><B>");
                                 replaceend = true;
                             }
 
                             if (replaceend) {
                                 eventtext = eventtext.replaceAll("(?i)</A>", "</B></FONT>");
                             }
 
                             tmp.setSpielerName(subjectplayername);
                             tmp.setSpielerHeim(subHome);
                             tmp.setGehilfeName(objectplayername);
                             tmp.setGehilfeHeim(objHome);
                             tmp.setEventText(eventtext);
                         }
                     }
 
                     break;
                 } else {
                     k++;
                 }
             }
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),e);
         }
 
         return myHighlights;
     }
 
     /**
      * parse player page
      *
      * @param doc TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected final Vector parsePlayers(Document doc) {
         final Vector myPlayers = new Vector();
         Element tmpEle = null;
         Element ele = null;
 
         try {
             tmpEle = doc.getDocumentElement();
             tmpEle = (Element) tmpEle.getElementsByTagName("Team").item(0);
             tmpEle = (Element) tmpEle.getElementsByTagName("Lineup").item(0);
 
             int n = 0;
 
             while (n < tmpEle.getElementsByTagName("Player").getLength()) {
                 ele = (Element) tmpEle.getElementsByTagName("Player").item(n);
 
                 final String playerID = XMLManager.instance().getFirstChildNodeValue((Element) ele.getElementsByTagName("PlayerID")
                                                                                                   .item(0));
 
                 if (Integer.valueOf(playerID).intValue() != 0) {
                     final String playerName = XMLManager.instance().getFirstChildNodeValue((Element) ele.getElementsByTagName("PlayerName")
                                                                                                         .item(0));
                     final Vector tmpPlayer = new Vector();
                     tmpPlayer.add(playerID);
                     tmpPlayer.add(playerName);
                     myPlayers.add(tmpPlayer);
                 }
 
                 n++;
             }
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),e);
         }
 
         return myPlayers;
     }
     
     /**
      * Set the region ID of the match's arena
      * @param regionId
      */
     public void setRegionId (int regionId) {
     	this.m_iRegionId = regionId;
     }
     
     /**
      * Get the region ID of this match's arena
      * @return
      */
     public int getRegionId () {
     	return m_iRegionId;
     }
 }
