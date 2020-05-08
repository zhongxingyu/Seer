 package aip2.m.AngebotAuftragModul;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestAngebotAuftragModul {
 
 	// private static IPersistenzSessionIntern persistenzSession;
	// private static ITransaktionIntern transaktion;
 
 	// private static AngebotAuftragModulLogik angebotAuftragModulLogik;
 	// private static AngebotVerwalter angebotVerwalter;
 	// private static AuftragVerwalter auftragVerwalter;
 	//
 	// @SuppressWarnings("unused")
 	// private static AngebotAuftragModulFassade angebotAuftragModulFassade;
 	//
 	// private static IProduktModulIntern iProduktIntern;
 	// private static IKundenModulIntern iKundeIntern;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		// persistenzSession = Persistenz.getPersistenzSessionIntern();
 		// transaktion = Transaktion.getTransaktion(persistenzSession);
 		// angebotVerwalter = new AngebotVerwalter(persistenzSession);
 		// auftragVerwalter = new AuftragVerwalter(persistenzSession);
 		// angebotAuftragModulLogik = new AngebotAuftragModulLogik(
 		// angebotVerwalter, auftragVerwalter);
 		//
 		// iKundeIntern = KundenModul.getIKundeIntern(persistenzSession,
 		// transaktion);
 		//
 		// angebotAuftragModulFassade = new AngebotAuftragModulFassade(
 		// transaktion, angebotAuftragModulLogik, angebotVerwalter,
 		// auftragVerwalter, iProduktIntern, iKundeIntern);
 	}
 
 	@Before
 	public void setUp() throws Exception {
		// transaktion.startTransaction();
 	}
 
 	@After
 	public void tearDown() throws Exception {
		// transaktion.rollbackTransaction();
 	}
 
 	@Test
 	public void test() {
 
 		// IProduktModulExtern ipme = ProduktModul.getProduktFassade(
 		// persistenzSession, transaktion);
 		// ProduktTyp pt = ipme.sucheProdukt(4);
 		// System.out.println(pt);
 		//
 		// IKunde kunde = iKundeIntern.getKunde(114);
 		// KundenTyp kundeT =
 		// KundenModul.getIKundenModulExtern(persistenzSession,
 		// transaktion).sucheKunden(kunde.getKundenNr());
 		//
 		// Map<ProduktTyp, Integer> anzahlProdukte = new HashMap<ProduktTyp,
 		// Integer>();
 		// anzahlProdukte.put(pt, 100);
 		//
 		// AngebotTyp angebot =
 		// angebotAuftragModulFassade.erstelleAngebot(kundeT,
 		// new Date(), anzahlProdukte, 100);
 		// System.out.println(angebot);
 		//
 		// Angebot a = angebotVerwalter.getAngebot(angebot.getAngebotsNr());
 		// System.out.println(a);
 		//
 		// AuftragTyp auftrag = angebotAuftragModulFassade
 		// .erstelleAuftrag(angebot);
 		// angebotAuftragModulFassade.schliesseAbAuftrag(auftrag);
 		assert (true);
 
 	}
 
 }
