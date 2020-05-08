 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class AmeiseTest {
 	
 	private static final int WARTEZEIT_AUF_TESTTHREAD = 100;
 	
	private static Ameise ameise;
 	private static TspBerechnungsdaten testTspBerechnungsdaten;
 
 	private static double ergebnis;
 	
 	@BeforeClass
 	public static void initialisiereVektor() {
 		testTspBerechnungsdaten = new TspBerechnungsdaten();
 		testTspBerechnungsdaten.fuegeKoordinatenHinzu(105.0, 65.0);
 		testTspBerechnungsdaten.fuegeKoordinatenHinzu(55.5, 95.0);
 		testTspBerechnungsdaten.fuegeKoordinatenHinzu(240.0, 281.0);
 		testTspBerechnungsdaten.initialisiereBerechnungsdaten(10);
 		
 		double wert1 = testTspBerechnungsdaten.getAbstand(0, 1);
 		double wert2 = testTspBerechnungsdaten.getAbstand(1, 2);
 		double wert3 = testTspBerechnungsdaten.getAbstand(2, 0);
 		ergebnis = wert1 + wert2 + wert3;
 	}
 	
 	@Before
 	public void erzeugeInstanz() {
 		Kolonie kolonieMock = mock(Kolonie.class);
 		ameise = new Ameise(null, 0, TestAlle.GEWICHTUNG_PHEROMON, testTspBerechnungsdaten, kolonieMock);
 		ameise.start();
 	}
 	
 	@Test
 	public void testeBerechneRouteLaenge() {
 		try {
 			Thread.sleep(WARTEZEIT_AUF_TESTTHREAD);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		double ergebnisRouteLaenge = ameise.getRouteLaenge();
 		
 		assertEquals(ergebnis, ergebnisRouteLaenge, 0);
 	}
 	
 
 }
