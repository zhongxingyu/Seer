 package com.dhbw.dvst.unit.model;
 
 import com.dhbw.dvst.models.Ausrichtung;
 import com.dhbw.dvst.models.Kurve;
 
 import junit.framework.TestCase;
 
 public class KurvenTest extends TestCase {
 
 	private Kurve kurve;
 	private Kurve randomKurve;
 	public KurvenTest(String name) {
 		super(name);
 	}
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.kurve = new Kurve(Ausrichtung.KURVEUNTENRECHTS);
 		this.randomKurve = new Kurve();
 	}
 	
 	public void testObRandomKonstruktorEineAusrichtungSetzt() {
 		assertNotNull(this.randomKurve.getAusrichtung());
 	}
 	
 	public void testObrandomKonstruktorAusrichtungAufGueltigenWertSetzt() {
 		assertTrue(this.randomKurve.getAusrichtung() == Ausrichtung.KURVEUNTENRECHTS
 				|| this.randomKurve.getAusrichtung() == Ausrichtung.KURVEUNTENLINKS
 				|| this.randomKurve.getAusrichtung() == Ausrichtung.KURVEOBENLINKS
 				|| this.randomKurve.getAusrichtung() == Ausrichtung.KURVEOBENRECHTS);
 	}
 	
 	public void testObRandomArrayVierElementeHat() {
 		assertEquals(this.kurve.getAusrichtungsArray().length, 4);
 	}
 	
 	public void testObEinmalRechtsDrehenFunktioniert() {
 		this.kurve.dreheSpielplatteNachRechts();
 		assertEquals(this.kurve.getAusrichtung(), Ausrichtung.KURVEUNTENLINKS);
 	}
 	
 	public void testObSechsmalRechtsDrehenFunktioniert() {
 		this.kurve.dreheSpielplatteNachRechts();
 		this.kurve.dreheSpielplatteNachRechts();
 		this.kurve.dreheSpielplatteNachRechts();
 		this.kurve.dreheSpielplatteNachRechts();
 		this.kurve.dreheSpielplatteNachRechts();
 		this.kurve.dreheSpielplatteNachRechts();
 		assertEquals(this.kurve.getAusrichtung(), Ausrichtung.KURVEOBENLINKS);
 	}
 	
 	public void testObZweimalLinksDrehenFunktioniert() {
 		this.kurve.dreheSpielplatteNachLinks();
 		this.kurve.dreheSpielplatteNachLinks();
 		assertEquals(this.kurve.getAusrichtung(), Ausrichtung.KURVEOBENLINKS);
 	}
 	
 	public void testObViermalLinksDrehenFunktioniert() {
 		this.kurve.dreheSpielplatteNachLinks();
 		this.kurve.dreheSpielplatteNachLinks();
 		this.kurve.dreheSpielplatteNachLinks();
 		this.kurve.dreheSpielplatteNachLinks();
 		assertEquals(this.kurve.getAusrichtung(), Ausrichtung.KURVEUNTENRECHTS);
 	}
 	
 	public void testObgetMotivUrlKorrekteUrlZurueckgibt() {
 		String actual = this.kurve.getMotivURL();
 		assertEquals("kurve", actual);
 	}
 	
 	public void testObSetzeOffeneSeitebeiKurveUntenRechtsAusrichtungUntenOffenSetzt() {
 		assertEquals(true, kurve.isUntenOffen());
 	}
 	
 	public void testObSetzeOffeneSeitebeiKurveUntenRechtsAusrichtungObenGeschlossenenSetzt() {
 		assertEquals(false, kurve.isObenOffen());
 	}
 	
 	public void testObdDreheKurveNachLinksSpielplatteNachRechtsGeschlossenSetzt() {
 		kurve.dreheSpielplatteNachRechts();
 		assertEquals(false, kurve.isRechtsOffen());
 	}
 	
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 }
