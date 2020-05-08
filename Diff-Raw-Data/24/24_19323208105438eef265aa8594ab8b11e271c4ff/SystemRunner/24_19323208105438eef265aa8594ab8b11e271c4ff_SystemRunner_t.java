 package gui;
 
 import adapter.BankAdapter;
 import adapter.PersistenzAdapter;
 import adapter.TransaktionsAdapter;
 import adapter.TransportdienstleisterAdapter;
 import hibernate.HibernateUtil;
 import interfaces.*;
 import komponentenFassaden.*;
 import komponentenInterfaces.intern.IPersitenz;
 import komponentenInterfaces.intern.ITransaktionsAdapter;
 import org.hibernate.Session;
 import org.joda.time.DateTime;
 import typClasses.AngebotTyp;
 import typClasses.AuftragTyp;
 import typClasses.KundeTyp;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
 * TODO UDP-Heartbeat
  */
 public class SystemRunner {
     public static void main(String[] args) {
         Session session = HibernateUtil.getSessionFactory().openSession();
         // adapter
         IPersitenz persitenz = new PersistenzAdapter(session);
         ITransaktionsAdapter transaktionsAdapter = new TransaktionsAdapter(session);
         BankAdapter bankAdapter = new BankAdapter();
         TransportdienstleisterAdapter transportdienstleisterAdapter = new TransportdienstleisterAdapter();
 
         // Fassaden
         LieferantenverwaltungFassade lieferantenverwaltung = new LieferantenverwaltungFassade(persitenz);
         ProduktverwaltungFassade produktverwaltung = new ProduktverwaltungFassade(persitenz);
         KundenverwaltungFassade kundenverwaltung = new KundenverwaltungFassade(persitenz);
 
         AngebotsverwaltungFassade angebotsverwaltung = new AngebotsverwaltungFassade(produktverwaltung, kundenverwaltung, persitenz);
         EinkaufsverwaltungFassade einkaufsverwaltung = new EinkaufsverwaltungFassade(persitenz, produktverwaltung, lieferantenverwaltung);
         LagerverwaltungFassade lagerverwaltung = new LagerverwaltungFassade(produktverwaltung, einkaufsverwaltung, persitenz);
         AuftragsverwaltungFassade auftragsverwaltung = new AuftragsverwaltungFassade(angebotsverwaltung, lagerverwaltung, transportdienstleisterAdapter, persitenz);
         BuchhaltungverwaltungFassade buchhaltungverwaltung = new BuchhaltungverwaltungFassade(bankAdapter, auftragsverwaltung, persitenz);
 
         // --- SIMULATING SOME STUFF ---
 
         // TODO
         /*
         Kunde erstellen
         Angebot erstellen
         Anegebot -> Auftrag
         Produkte erstellen -> Auftrag
         -> erzeuge Waremausgangsmeldung
         -> Lieferung und Transportauftrag
         -> Rechnung
 
         -> simulate -> Lieferung erfolgt
         -> Zahlungseingang
         -> abschließen
          */
 
         // -----------------------------
         Date sampleDate = new DateTime().toDate();
         IProdukt produkt = produktverwaltung.createProdukt("USB-Stick", 10);
         IKunde kunde = kundenverwaltung.createKunde("Hans Dieter", "Musterstr. 123, 27232 Musterhausen");
         IAngebot angebot = angebotsverwaltung.erstelleAngebot(sampleDate, sampleDate, 10f, kunde);
         IAuftrag auftrag = auftragsverwaltung.erstelleAuftrag(sampleDate, angebot);
 
         lagerverwaltung.createWarenausgangsmeldung(5, sampleDate, produkt);
         lagerverwaltung.createWarenausgangsmeldung(25, sampleDate, produkt);
 
 
         buchhaltungverwaltung.erstelleZahlungseingang(sampleDate, 100f);
         buchhaltungverwaltung.erstelleRechnung(sampleDate, null);
 
         ILieferung lieferung = auftragsverwaltung.erstelleLieferung();
         lieferung.setTransportauftrag(auftragsverwaltung.erstelleTransportauftrag(sampleDate, "Super Heftig Transports"));
         auftrag.setLieferung(lieferung);
 
         persitenz.updateObject(auftrag);
 
         auftragsverwaltung.markiereAuftragAlsVerschickt(auftrag.getAuftragsNummer());
         auftragsverwaltung.auftragAbschliessen(auftrag.getAuftragsNummer());
 
         session.close();
     }
 }
 
 
 
