 package com.dhbw.dvst.unit.model;
 
 import com.dhbw.dvst.models.Ausrichtung;
 import com.dhbw.dvst.models.Kreuzung;
 
 import junit.framework.TestCase;
 
 public class KreuzungsTest extends TestCase {
 
 	private Kreuzung kreuzungL;
 	private Kreuzung kreuzungO;
 	private Kreuzung randomKreuzung;
 	public KreuzungsTest(String name) {
 		super(name);
 	}
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.kreuzungL = new Kreuzung(Ausrichtung.KREUZUNGLINKS);
 		this.kreuzungO = new Kreuzung(Ausrichtung.KREUZUNGOBEN);
 		this.randomKreuzung = new Kreuzung();
 	}
 	
 	public void testObRandomKonstruktorEineAusrichtungSetzt() {
 		assertNotNull(this.randomKreuzung.getAusrichtung());
 	}
 	
 	public void testObrandomKonstruktorAusrichtungAufGueltigenWertSetzt() {
 		assertTrue(this.randomKreuzung.getAusrichtung() == Ausrichtung.KREUZUNGLINKS
 				|| this.randomKreuzung.getAusrichtung() == Ausrichtung.KREUZUNGOBEN
 				|| this.randomKreuzung.getAusrichtung() == Ausrichtung.KREUZUNGRECHTS
 				|| this.randomKreuzung.getAusrichtung() == Ausrichtung.KREUZUNGUNTEN);
 	}
 	
 	public void testObRandomArrayVierElementeHat() {
 		assertEquals(this.kreuzungL.getAusrichtungsArray().length, 4);
 	}
 	
 	public void testObEinmalRechtsDrehenBeiKreuzungLinksFunktioniert() {
 		this.kreuzungL.dreheSpielplatteNachRechts();
 		assertEquals(this.kreuzungL.getAusrichtung(), Ausrichtung.KREUZUNGOBEN);
 	}
 	
 	public void testObFuenfmalRechtsDrehenBeiKreuzungObenFunktioniert() {
 		this.kreuzungO.dreheSpielplatteNachRechts();
 		this.kreuzungO.dreheSpielplatteNachRechts();
 		this.kreuzungO.dreheSpielplatteNachRechts();
 		this.kreuzungO.dreheSpielplatteNachRechts();
 		this.kreuzungO.dreheSpielplatteNachRechts();
 		assertEquals(this.kreuzungO.getAusrichtung(), Ausrichtung.KREUZUNGRECHTS);
 	}
 	
 	public void testObFuenfmalLinksDrehenFunktioniert() {
 		this.kreuzungL.dreheSpielplatteNachLinks();
 		this.kreuzungL.dreheSpielplatteNachLinks();
 		this.kreuzungL.dreheSpielplatteNachLinks();
 		this.kreuzungL.dreheSpielplatteNachLinks();
 		this.kreuzungL.dreheSpielplatteNachLinks();
 		assertEquals(this.kreuzungL.getAusrichtung(), Ausrichtung.KREUZUNGUNTEN);
 	}
 	
 	public void testObgetMotivUrlKorrekteUrlZurueckgibt() {
 		String actual = this.kreuzungL.getMotivURL();
 		assertEquals("kreuzung", actual);
 	}
 	
 	public void testObSetzeOffeneSeitebeiKreuzungLinksAusrichtungLinksOffenSetzt() {
		kreuzungL.setzeOffeneSeiten();
 		assertEquals(true, kreuzungL.isLinksOffen());
 	}
 	
 	public void testObSetzeOffeneSeitebeiKreuzungObenAusrichtungObenOffenSetzt() {
		kreuzungO.setzeOffeneSeiten();
 		assertEquals(true, kreuzungO.isObenOffen());
 	}
 	
 	public void testObdDreheKreuzungLinksNachRechtsSpielplatteNachUntenGeschlossenSetzt() {
 		kreuzungL.dreheSpielplatteNachRechts();
 		assertEquals(false, kreuzungL.isUntenOffen());
 	}
 	
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 }
