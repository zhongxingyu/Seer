 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dk.pop.srs.database;
 
 import dk.pop.srs.sag.Sag;
 import dk.pop.srs.betaler.Betaler;
 import dk.pop.srs.person.Person;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Patrick
  */
 public class SagDAO {
 
     private Connection dbConn;
 
     /**
      * Constructor
      * @param dbConn 
      */
     public SagDAO(Connection dbConn) {
         this.dbConn = dbConn;
     }
 
     /**
      * Creates a Sag in database from a Sag object
      * @param sag
      * @return 
      */
     public int opretSag(Sag sag) {
         try {
             Statement st = dbConn.createStatement();
 
             //Opret person
             if (!checkValueExists("CPR", sag.getPerson().getCpr(), "person")) {
                 st.execute("INSERT INTO person (CPR, Fornavn, Mellemnavn, efternavn) VALUES('" + sag.getPerson().getCpr() + "', '" + sag.getPerson().getFornavn() + "', '" + sag.getPerson().getMellemnavn() + "', '" + sag.getPerson().getEfternavn() + "');");
             }
 
             //Opret betaler
             if (!checkValueExists("BetalingCPR", sag.getBetaler().getBetalingCPR(), "betaler")) {
                 st.execute("INSERT INTO betaler (betalingCPR, betalingNavn) VALUES('" + sag.getBetaler().getBetalingCPR() + "', '" + sag.getBetaler().getBetalingNavn() + "')");
             }
 
             //Opret sag
             st.execute("INSERT INTO sag (CPR, sagsSted, paragraf, foranstaltningsnavn, beskrivelse, periodeFra, periodeTil, aer, sagsType, betalingCPR, betalingBelob) VALUES('" + sag.getPerson().getCpr() + "', '" + sag.getSagsSted() + "', '" + sag.getParagraf() + "', '" + sag.getForanstaltningsnavn() + "', '" + sag.getBeskrivelse() + "', '" + sag.getPeriodeFra() + "', '" + sag.getPeriodeTil() + "', '" + sag.getAer() + "', '" + sag.getSagstype() + "', '" + sag.getBetaler().getBetalingCPR() + "', '" + sag.getBetalingBelob() + "')");
 
             ResultSet rs = st.executeQuery("select sagsID from sag order by sagsID desc limit 1");
             rs.next();
             return rs.getInt(1);
         } catch (SQLException ex) {
             Logger.getLogger(SagDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return -1;
     }
 
     /**
      * Deletes a Sag from database from a Sag object
      * @param sag 
      */
     public void sletSag(Sag sag) {
         try {
             Statement st = dbConn.createStatement();
             System.out.println("WE GOT: " + sag.getSagsID());
 
             st.execute("DELETE FROM sag WHERE SagsID='" + sag.getSagsID() + "'");
         } catch (SQLException ex) {
             Logger.getLogger(SagDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Deletes a Person from database from a Person object
      * @param person 
      */
     public void sletPerson(Person person) {
         try {
             Statement st = dbConn.createStatement();
             st.execute("DELETE FROM person WHERE CPR='" + person.getCpr() + "'");
         } catch (SQLException ex) {
             Logger.getLogger(SagDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Updates a Sag in database from a Sag object
      * @param sag 
      */
     public void opdaterSag(Sag sag) {
         try {
             Statement st = dbConn.createStatement();
             st.execute("UPDATE sag SET SagsSted='" + sag.getSagsSted() + "', Paragraf='" + sag.getParagraf() + "', foranstaltningsnavn='" + sag.getForanstaltningsnavn() + "', Beskrivelse='" + sag.getBeskrivelse() + "', PeriodeFra='" + sag.getPeriodeFra() + "', PeriodeTil='" + sag.getPeriodeTil() + "', AER='" + sag.getAer() + "', SagsType='" + sag.getSagstype() + "', BetalingCPR='" + sag.getBetaler().getBetalingCPR() + "', BetalingBelob='" + sag.getBetalingBelob() + "' WHERE SagsID=" + sag.getSagsID());
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * Creates a Betaler in database form a Betaler object
      * @param betaler 
      */
     public void opretBetaler(Betaler betaler) {
         try {
             Statement st = dbConn.createStatement();
 
             st.execute("INSERT INTO betaler (betalingCPR, betalingNavn) VALUES('" + betaler.getBetalingCPR() + "', '" + betaler.getBetalingNavn() + "')");
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
     
     /**
      * Checks if a value in a table exists, and return true or false
      * @param column
      * @param value
      * @param table
      * @return 
      */
     public boolean checkValueExists(String column, String value, String table) {
         try {
             Statement st = dbConn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table);
             while (rs.next()) {
                 if (rs.getString(column).equals(value)) {
                     return true;
                 }
             }
         } catch (SQLException ex) {
             Logger.getLogger(SagDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return false;
     }
     
     /**
      * Returns an ArrayList with Sager, Personer, and Betalere, taken from the database and assembled
      * @return 
      */
     public ArrayList<ArrayList> getEverything() {
         try {
             Statement st = dbConn.createStatement();
 
             ArrayList<Sag> sager = new ArrayList<>();
             ArrayList<Person> personer = new ArrayList<>();
             ArrayList<Betaler> betalere = new ArrayList<>();
             ArrayList<ArrayList> alt = new ArrayList<>();
 
             alt.add(sager);
             alt.add(personer);
             alt.add(betalere);
 
 
             ResultSet rsPersoner = st.executeQuery("SELECT * FROM person");
             while (rsPersoner.next()) {
                 Person p = new Person(rsPersoner.getString(1), rsPersoner.getString(2), rsPersoner.getString(3), rsPersoner.getString(4));
                 personer.add(p);
             }
 
             ResultSet rsBetalere = st.executeQuery("SELECT * FROM betaler");
             while (rsBetalere.next()) {
                 Betaler b = new Betaler(rsBetalere.getString(1), rsBetalere.getString(2));
                 betalere.add(b);
             }
 
             ResultSet rsSager = st.executeQuery("SELECT * FROM sag");
             while (rsSager.next()) {
                 String cpr = rsSager.getString("CPR");
                 String betalerCPR = rsSager.getString("BETALINGCPR");
                 String sagsSted = rsSager.getString("SAGSSTED");
                 String paragraf = rsSager.getString("PARAGRAF");
                 String foranstaltningsnavn = rsSager.getString("FORANSTALTNINGSNAVN");
                 String beskrivelse = rsSager.getString("BESKRIVELSE");
 
                 Person person = null;
                 Betaler betaler = null;
 
                 double betalingBelob = rsSager.getDouble("BETALINGBELOB");
 
                 int sagsID = rsSager.getInt("SAGSID");
                 int periodeFra = rsSager.getInt("PERIODEFRA");
                 int periodeTil = rsSager.getInt("PERIODETIL");
                 int aer = rsSager.getInt("AER");
                 int sagstype = rsSager.getInt("SAGSTYPE");
 
                 Sag sag;
 
                 for (Person p : personer) {
                     if (p.getCpr().equals(cpr)) {
                         person = p;
                         break;
                     }
                 }
 
                 for (Betaler b : betalere) {
                     if (b.getBetalingCPR().equals(betalerCPR)) {
                         betaler = b;
                         break;
                     }
                 }
 
                 sag = new Sag(sagsSted, paragraf, foranstaltningsnavn, beskrivelse, person, betaler, betalingBelob, periodeFra, periodeTil, aer, sagstype);
                 sag.setSagsID(sagsID);
                 sager.add(sag);
 
             }
 
             for (Person p : personer) {
                 for (Sag s : sager) {
                     if (s.getPerson().getCpr().equals(p.getCpr())) {
                         p.add(s);
                         break;
                     }
                 }
             }
 
             for (Betaler b : betalere) {
                 for (Sag s : sager) {
                     if (s.getBetaler().getBetalingCPR().equals(b.getBetalingCPR())) {
                         b.add(s);
                         break;
                     }
                 }
             }
 
 
             return alt;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
 
         return null;
     }
 }
