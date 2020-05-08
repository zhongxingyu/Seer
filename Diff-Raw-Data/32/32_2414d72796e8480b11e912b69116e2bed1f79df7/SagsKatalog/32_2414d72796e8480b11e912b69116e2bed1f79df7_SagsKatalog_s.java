 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dk.pop.srs.sag;
 
 import dk.pop.srs.database.SagDAO;
 import dk.pop.srs.betaler.Betaler;
 import dk.pop.srs.person.Person;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 /**
  *
  * @author Oliverbærbar
  */
 public class SagsKatalog {
 
     private SagDAO sagDAO;
     private ArrayList<Sag> sager = new ArrayList<>();
     private ArrayList<Person> personer = new ArrayList<>();
     private ArrayList<Betaler> betalere = new ArrayList<>();
 
     public SagsKatalog(SagDAO sagDAO) {
         this.sagDAO = sagDAO;
         updateArray();
     }
 
     public boolean gemSag(String sagsSted, String paragraf, String foranstalningsnavn, String beskrivelse, int periodeFra, int periodeTil, int aer, int sagsType, String betalingNavn, String betalingCPR, double betalingBelob, Sag sag) {
 
         if (!sag.getBetaler().getBetalingCPR().equals(betalingCPR)) { //Hvis betaleren er blevet ændret
             if (checkBetalerExists(betalingCPR)) { //Hvis den nye betaler findes i forvejen
                 Betaler betaler = getBetaler(betalingCPR); //Hent nye betaler
                 sag.getBetaler().remove(sag); //Fjern sag fra den originale betaler
                 sag.setBetaler(betaler); //Set betaler i sagen til den nye sag
                 betaler.add(sag); //Tilføj sagen til den nye betaler
 
                 /**
                  * GEM SAG *
                  */
                 sag.setAer(aer);
                 sag.setBeskrivelse(beskrivelse);
                 sag.setBetalingBelob(betalingBelob);
                 sag.setForanstaltningsnavn(foranstalningsnavn);
                 sag.setParagraf(paragraf);
                 sag.setPeriodeFra(periodeFra);
                 sag.setPeriodeTil(periodeTil);
                 sag.setSagsSted(sagsSted);
                 sag.setSagstype(sagsType);
                 sagDAO.opdaterSag(sag);
             } else { //Hvis den nye betaler ikke findes i forvejen
                 Betaler betaler = opretBetaler(betalingCPR, betalingNavn);
 
                 sag.getBetaler().remove(sag);
                 sag.setBetaler(betaler);
                 betaler.add(sag);
 
                 /**
                  * GEM SAG *
                  */
                 sag.setAer(aer);
                 sag.setBeskrivelse(beskrivelse);
                 sag.setBetalingBelob(betalingBelob);
                 sag.setForanstaltningsnavn(foranstalningsnavn);
                 sag.setParagraf(paragraf);
                 sag.setPeriodeFra(periodeFra);
                 sag.setPeriodeTil(periodeTil);
                 sag.setSagsSted(sagsSted);
                 sag.setSagstype(sagsType);
                 sagDAO.opdaterSag(sag);
             }
         } else {
             /**
              * GEM SAG *
              */
             sag.setAer(aer);
             sag.setBeskrivelse(beskrivelse);
             sag.setBetalingBelob(betalingBelob);
             sag.setForanstaltningsnavn(foranstalningsnavn);
             sag.setParagraf(paragraf);
             sag.setPeriodeFra(periodeFra);
             sag.setPeriodeTil(periodeTil);
             sag.setSagsSted(sagsSted);
             sag.setSagstype(sagsType);
             sagDAO.opdaterSag(sag);
         }
         return false;
     }
 
     public Betaler opretBetaler(String betalingCPR, String betalingNavn) {
         Betaler betaler = new Betaler(betalingCPR, betalingNavn);
         betalere.add(betaler);
         sagDAO.opretBetaler(betaler);
         return betaler;
     }
 
     private void updateArray() {
         ArrayList<ArrayList> alt = sagDAO.getEverything();
         sager = alt.get(0);
         personer = alt.get(1);
         betalere = alt.get(2);
     }
 
     public void registrerSag(String sagsSted, String fornavn, String mellemnavn, String efternavn, String paragraf, String foranstalningsnavn, String beskrivelse, int periodeFra, int periodeTil, int aer, int sagsType, String betalingNavn, String betalingCPR, double betalingBelob, String CPR) {
         Person person = new Person(CPR, fornavn, mellemnavn, efternavn);
         Betaler betaler = new Betaler(betalingCPR, betalingNavn);
         Sag sag = new Sag(sagsSted, paragraf, foranstalningsnavn, beskrivelse, person, betaler, betalingBelob, periodeFra, periodeTil, aer, sagsType);
         sager.add(sag);
         int sagsID = sagDAO.opretSag(sag);
         sag.setSagsID(sagsID);
         person.add(sag);
         betaler.add(sag);
 
         personer.add(person);
         betalere.add(betaler);
     }
 
     public void sletSag(Sag sag) {
         sagDAO.sletSag(sag);
         sager.remove(sag);
 
         sag.getPerson().remove(sag);
 
         sag.getBetaler().remove(sag);
     }
 
     public boolean sletPerson(Person person) {
         if (person.getArraySize() == 0) {
             sagDAO.sletPerson(person);
             personer.remove(person);
             return true;
         } else {
             return false;
         }
     }
 
     public ArrayList<Person> getPersonV2(String value) {
         ArrayList<Person> fundnePersoner = new ArrayList<>();
         for (Person p : personer) {
             if (p.getCpr().equalsIgnoreCase(value)) {
                 fundnePersoner.add(p);
                 return fundnePersoner;
             } else if (p.getFornavn().equalsIgnoreCase(value)) {
                 fundnePersoner.add(p);
             } else if (p.getMellemnavn().equalsIgnoreCase(value)) {
                 fundnePersoner.add(p);
             } else if (p.getEfternavn().equalsIgnoreCase(value)) {
                 fundnePersoner.add(p);
             } else if ((p.getFornavn() + " " + p.getEfternavn()).equalsIgnoreCase(value)) {
                 fundnePersoner.add(p);
             } else if ((p.getFornavn() + " " + p.getMellemnavn() + " " + p.getEfternavn()).equalsIgnoreCase(value)) {
                 fundnePersoner.add(p);
             } else if ((p.getFornavn() + " " + p.getMellemnavn()).equalsIgnoreCase(value)) {
                 fundnePersoner.add(p);
             } else if ((p.getMellemnavn() + " " + p.getEfternavn()).equalsIgnoreCase(value)) {
                 fundnePersoner.add(p);
             }
 
         }
         return fundnePersoner;
     }
 
     public Sag getSag(int sagsID) {
         for (Sag s : sager) {
             if (s.getSagsID() == sagsID) {
                 return s;
             }
         }
         return null;
     }
 
     public Person getPerson(String CPR) {
         for (Person p : personer) {
             if (p.getCpr().equals(CPR)) {
                 return p;
             }
         }
         return null;
     }
 
     public Betaler getBetaler(String betalingCPR) {
         for (Betaler b : betalere) {
             if (b.getBetalingCPR().equals(betalingCPR)) {
                 return b;
             }
         }
         return null;
     }
 
     public boolean checkBetalerExists(String betalingCPR) {
         for (Betaler b : betalere) {
             if (b.getBetalingCPR().equals(betalingCPR)) {
                 return true;
             }
         }
         return false;
     }
     
     public boolean checkPersonExists(String CPR) {
         for(Person p : personer) {
             if(p.getCpr().equals(CPR)) {
                 return true;
             }
         }
         return false;
     }
 
     
     public void redigerPerson(String CPR, String fornavn, String mellemnavn, String efternavn) {
     Person p = getPerson(CPR);
     p.setFornavn(fornavn);
     p.setMellemnavn(mellemnavn);
     p.setEfternavn(efternavn);
 }
     
     public ArrayList<Sag> sogCPR(String CPR, boolean ui90d) {
         if (ui90d) {
             ArrayList<Sag> sager_p = new ArrayList<>();
             SimpleDateFormat formatter = new SimpleDateFormat("mm");
             int currentMonth = Integer.valueOf(formatter.format(new java.util.Date()));
             for (Sag s : sager) {
                 int periodeTil = s.getPeriodeTil();
                 int tjek = periodeTil - currentMonth;
                 String s_cpr = s.getPerson().getCpr();
                 if (s_cpr.equals(CPR) && tjek <= 3 && tjek >= 0) {
                     sager_p.add(s);
                 }
             }
             return sager_p;
         } else {
             ArrayList<Sag> sager_p = new ArrayList<>();
             for (Sag s : sager) {
                 String s_cpr = s.getPerson().getCpr();
 
                 if (s_cpr.equals(CPR)) {
                     sager_p.add(s);
                 }
             }
             return sager_p;
         }
     }
 
     public ArrayList<Sag> sogParagraf(String paragraf, boolean ui90d) {
         if (ui90d) {
             ArrayList<Sag> sager_p = new ArrayList<>();
             SimpleDateFormat formatter = new SimpleDateFormat("mm");
             int currentMonth = Integer.valueOf(formatter.format(new java.util.Date()));
             for (Sag s : sager) {
                 int periodeTil = s.getPeriodeTil();
                 int tjek = periodeTil - currentMonth;
                 String s_paragraf = s.getParagraf();
                 if (s_paragraf.equalsIgnoreCase(paragraf) && tjek <= 3 && tjek >= 0) {
                     sager_p.add(s);
                 }
             }
             return sager_p;
         } else {
             ArrayList<Sag> sager_p = new ArrayList<>();
             for (Sag s : sager) {
                 String s_paragraf = s.getParagraf();
 
                 if (s_paragraf.equalsIgnoreCase(paragraf)) {
                     sager_p.add(s);
                 }
             }
             return sager_p;
         }
     }
 
     public ArrayList<Sag> sogSagstype(String sagstype, boolean ui90d) {
         if (ui90d) {
             ArrayList<Sag> sager_p = new ArrayList<>();
             SimpleDateFormat formatter = new SimpleDateFormat("mm");
             int currentMonth = Integer.valueOf(formatter.format(new java.util.Date()));
             for (Sag s : sager) {
                 int periodeTil = s.getPeriodeTil();
                 int tjek = periodeTil - currentMonth;
                 int s_INTsagstype = s.getSagstype();
                 String s_sagstype = "";
                 switch (s_INTsagstype) {
                     case 1:
                         s_sagstype = "Socialsag";
                         break;
                     case 2:
                         s_sagstype = "Handicapsag";
                         break;
                     case 3:
                         s_sagstype = "Flytningerefusion";
                         break;
                     case 4:
                         s_sagstype = "Mellemkommunal";
                         break;
                 }
                 if (s_sagstype.equalsIgnoreCase(sagstype) && tjek <= 3 && tjek >= 0) {
                     sager_p.add(s);
                 }
             }
             return sager_p;
         } else {
             ArrayList<Sag> sager_p = new ArrayList<>();
             for (Sag s : sager) {
                 int s_INTsagstype = s.getSagstype();
                 String s_sagstype = "";
                 switch (s_INTsagstype) {
                     case 1:
                         s_sagstype = "Socialsag";
                         break;
                     case 2:
                         s_sagstype = "Handicapsag";
                         break;
                     case 3:
                         s_sagstype = "Flytningerefusion";
                         break;
                     case 4:
                         s_sagstype = "Mellemkommunal";
                         break;
                 }
                 if (s_sagstype.equalsIgnoreCase(sagstype)) {
                     sager_p.add(s);
                 }
             }
             return sager_p;
         }
     }
 
     public ArrayList<Sag> getAlleSager(boolean ui90d) {
         if (ui90d) {
             ArrayList<Sag> sager_ui90d = new ArrayList<>();
             SimpleDateFormat formatter = new SimpleDateFormat("mm");
             int currentMonth = Integer.valueOf(formatter.format(new java.util.Date()));
             for (Sag s : sager) {
                 int periodeTil = s.getPeriodeTil();
                 int tjek = periodeTil - currentMonth;
                 System.out.println(tjek);
                 System.out.println(currentMonth);
                 if (tjek <= 3 && tjek >= 0) {
                     sager_ui90d.add(s);
                 }
             }
             return sager_ui90d;
         } else {
             return sager;
         }
     }
 
     public boolean checkValueExists(String column, String value, String table) {
         return sagDAO.checkValueExists(column, value, table);
     }
     
     public ArrayList<Person> getAllePersoner(){
         return personer;
     }
 }
