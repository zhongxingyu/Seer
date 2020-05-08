 package gui;
 
 import adapter.BankAdapter;
 import adapter.PersistenzAdapter;
 import adapter.TransaktionsAdapter;
 import adapter.TransportdienstleisterAdapter;
 import hibernate.HibernateUtil;
 import komponentenFassaden.*;
 import komponentenInterfaces.intern.IPersitenz;
 import komponentenInterfaces.intern.ITransaktionsAdapter;
 import org.hibernate.Session;
 import serverFassaden.CallCenterServerFassade;
 import typClasses.*;
 
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Date;

 
 public class SystemRunner {
     public static void main(String[] args) throws RemoteException, MalformedURLException {
 
         try {
             LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
         } catch (RemoteException e) { /* ... */ }
 
 
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
 
         // Systeme  A3
         CallCenterServerFassade system = new CallCenterServerFassade(angebotsverwaltung, auftragsverwaltung);
         Naming.rebind("//localhost:" + Registry.REGISTRY_PORT + "/" + args[0], system);
 
 
         //        Heartbeat hbeat = new Heartbeat(args[0], Integer.parseInt(args[1]));
 //        hbeat.start();
 //        try {
 //            hbeat.join();
 //        } catch (InterruptedException e) {
 //            e.printStackTrace();
 //        }
 
         Date d1 = new Date(System.currentTimeMillis());
         Date d2 = new Date(System.currentTimeMillis() + 86400000 * 5);
         Date d3 = new Date(System.currentTimeMillis() + 86400000 * 3);
 
         ProduktTyp p1 = angebotsverwaltung.erstelleProdukt("USB-Stick", 10);
         ProduktTyp p2 = angebotsverwaltung.erstelleProdukt("DVDs", 20);
         ProduktTyp p3 = angebotsverwaltung.erstelleProdukt("Buch: AI ist das tollste Fach auf der Welt (limited Edition)", 1);
 
         KundeTyp k1 = angebotsverwaltung.erstelleKunde("Klaus Dieter", "Hamburger Str. 1");
         KundeTyp k2 = angebotsverwaltung.erstelleKunde("Hans Peter", "Hamburger Str. 2");
 
         AngebotTyp ang1 = angebotsverwaltung.erstelleAngebot(d1, d2, 10.99f, k1);
         AngebotTyp ang2 = angebotsverwaltung.erstelleAngebot(d1, d2, 7.99f, k2);
 
         AuftragTyp auf1 = auftragsverwaltung.erstelleAuftrag(d3, ang1);
         AuftragTyp auf2 = auftragsverwaltung.erstelleAuftrag(d3, ang2);
 
         RechnungTyp r1 = buchhaltungverwaltung.erstelleRechnung(d3);
         RechnungTyp r2 = buchhaltungverwaltung.erstelleRechnung(d3);
         RechnungTyp r3 = buchhaltungverwaltung.erstelleRechnung(d3);
         RechnungTyp r4 = buchhaltungverwaltung.erstelleRechnung(d3);
         RechnungTyp r5 = buchhaltungverwaltung.erstelleRechnung(d3);
 
         buchhaltungverwaltung.addAuftragZuRechnung(r1.getRechnungsNummer(), auf1.getAuftragsNummer());
         buchhaltungverwaltung.addAuftragZuRechnung(r2.getRechnungsNummer(), auf2.getAuftragsNummer());
         buchhaltungverwaltung.addAuftragZuRechnung(r3.getRechnungsNummer(), auf1.getAuftragsNummer());
         buchhaltungverwaltung.addAuftragZuRechnung(r4.getRechnungsNummer(), auf2.getAuftragsNummer());
         buchhaltungverwaltung.addAuftragZuRechnung(r5.getRechnungsNummer(), auf1.getAuftragsNummer());
 
 
         // -----------------------------
 //        Date sampleDate = new DateTime().toDate();
 //        IProdukt produkt = produktverwaltung.createProdukt("USB-Stick", 10);
 //        IProdukt p2 = produktverwaltung.getProdukt("USB-Stick");
 //        System.out.println(p2);
 //        IKunde kunde = kundenverwaltung.createKunde("Hans Dieter", "Musterstr. 123, 27232 Musterhausen");
 //        IAngebot angebot = angebotsverwaltung.erstelleAngebot(sampleDate, sampleDate, 10f, kunde);
 //        IAuftrag auftrag = auftragsverwaltung.erstelleAuftrag(sampleDate, angebot);
 //
 //        lagerverwaltung.createWarenausgangsmeldung(5, sampleDate, produkt);
 //        lagerverwaltung.createWarenausgangsmeldung(25, sampleDate, produkt);
 //
 //
 //        buchhaltungverwaltung.erstelleZahlungseingang(sampleDate, 100f);
 //        buchhaltungverwaltung.erstelleRechnung(sampleDate, null);
 //
 //        ILieferung lieferung = auftragsverwaltung.erstelleLieferung();
 //        lieferung.setTransportauftrag(auftragsverwaltung.erstelleTransportauftrag(sampleDate, "Super Heftig Transports"));
 //        auftrag.setLieferung(lieferung);
 //
 //        persitenz.updateObject(auftrag);
 //
 //        auftragsverwaltung.markiereAuftragAlsVerschickt(auftrag.getAuftragsNummer());
 //        auftragsverwaltung.auftragAbschliessen(auftrag.getAuftragsNummer());
 
         session.close();
     }
 }
 
 
 
