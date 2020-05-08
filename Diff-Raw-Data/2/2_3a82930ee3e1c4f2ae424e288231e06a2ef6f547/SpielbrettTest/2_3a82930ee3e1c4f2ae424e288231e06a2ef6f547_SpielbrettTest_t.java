 package com.dhbw.dvst.unit.model;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import junit.framework.TestCase;
 
 import com.dhbw.dvst.models.Ausrichtung;
 import com.dhbw.dvst.models.Farbe;
 import com.dhbw.dvst.models.Form;
 import com.dhbw.dvst.models.Kreuzung;
 import com.dhbw.dvst.models.Kurve;
 import com.dhbw.dvst.models.Sehenswuerdigkeit;
 import com.dhbw.dvst.models.Spielbrett;
 import com.dhbw.dvst.models.Spieler;
 import com.dhbw.dvst.models.Spielfigur;
 import com.dhbw.dvst.models.Spielplatte;
 
 public class SpielbrettTest extends TestCase{
 
 	private Spielbrett testBrett;
 	private ArrayList<Spieler> testSpieler = new ArrayList<Spieler>();
 
 	public SpielbrettTest(String name) {
 		super(name);
 	}
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		testBrett = new Spielbrett();
 		testSpieler.add(new Spieler("test1", new Spielfigur(new Form("auto", "car"), new Farbe("rot", "red"), "car_red")));
 		testSpieler.add(new Spieler("test2", new Spielfigur(new Form("auto", "car"), new Farbe("blau", "blue"), "car_blue")));
 		testBrett.fuelleLosesSpielplattenArray();
 	}
 	
 	public void testObLosesSpielPlattenArrayKorrekteLaengeHat() {
 		assertEquals(33, testBrett.getAlleSpielplatten().size());
 	}
 	
 	public void testObSpielplattenArrayKorrekteLaengeHat() {
 		testBrett.fuegeStatischePlattenEin();
 		assertEquals(49, this.testBrett.getAlleSpielplatten().size());	
 	}
 	
 	public void testeObVorletztesElementKorrekteAusrichtungHat() {
 		testBrett.fuegeStatischePlattenEin();
 		assertEquals(new Kurve(Ausrichtung.KURVEOBENLINKS).getAusrichtung(),
 				this.testBrett.getAlleSpielplatten().get(48).getAusrichtung());
 	}
 	
 	public void testeObDrittesElementKorrekteAusrichtungHat() {
 		testBrett.fuegeStatischePlattenEin();
 		assertEquals(new Kreuzung(Ausrichtung.KREUZUNGUNTEN).getAusrichtung(),
 				this.testBrett.getAlleSpielplatten().get(2).getAusrichtung());
 	}
 	
 	public void testeSpielfigurPlatziert(){
 		testBrett.fuegeStatischePlattenEin();
 		testBrett.verteileSpielfiguren(testSpieler);
 		assertNotNull(testSpieler.get(0).getSpielfigur().getSpielplatte());
 		assertNotNull(testSpieler.get(1).getSpielfigur().getSpielplatte());
 	}
 	
 	public void testObMischekartenstapelKorrekteLaengeDesKartenStapelsAufbaut() {
 		//TODO
 		testBrett.mischeKartenstapel(new Stack<Sehenswuerdigkeit>());
 	}
 	
 	public void testObFigurAufSpielbrettUmsetzenFigurKorrektSetzt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielfigur figur = new Spielfigur(new Form("auto", "car"), new Farbe("gelb", "yellow"), "car_yellow");
 		this.testBrett.getAktivePlatte().setFigur(figur);
 		this.testBrett.figurUmsetzen(1);
 		assertEquals(figur, this.testBrett.getAlleSpielplatten().get(1).getFigur());
 	}
 	
 	public void testObFigurUmsetzenFigurAufAktiverPlatteLoescht() {
 		testBrett.fuegeStatischePlattenEin();
 		this.testBrett.getAktivePlatte().setFigur(
 				new Spielfigur(new Form("auto", "car"), new Farbe("gelb", "yellow"), "car_yellow"));
 		this.testBrett.figurUmsetzen(1);
 		assertNull(this.testBrett.getAlleSpielplatten().get(48).getFigur());
 	}
 	
 	public void testObSpielplattenAktualisierenSchiebbarKorrektSetztt() {
 		testBrett.fuegeStatischePlattenEin();
 		testBrett.aktualisiereSchiebbarePlatten(43, 8);
 		assertEquals(true, testBrett.getAlleSpielplatten().get(43).isSchiebbar());
 	}
 	
 	public void testObSpielplattenAktualisierenSchiebbarKorrektLoescht() {
 		testBrett.fuegeStatischePlattenEin();
 		testBrett.aktualisiereSchiebbarePlatten(43, 8);
 		assertEquals(false, testBrett.getAlleSpielplatten().get(8).isSchiebbar());
 	}
 	
 	public void testObAktualisiereAktivePlatteEingeschobenePlatteKorrektSetzt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte alteAktivePlatte = testBrett.getAktivePlatte();
 		Spielplatte neueAktivePlatte = testBrett.getAlleSpielplatten().get(45);
 		testBrett.aktualisiereAktivePlatte(3, neueAktivePlatte);
 		assertEquals(alteAktivePlatte, testBrett.getAlleSpielplatten().get(3));
 	}
 	
 	public void testObAktualisiereAktivePlatteKorrektePlatteSetzt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte raus = testBrett.getAlleSpielplatten().get(45);
 		testBrett.aktualisiereAktivePlatte(3, raus);
 		assertEquals(raus, testBrett.getAktivePlatte());
 	}
 	
 	public void testObSpielplatteLinksEinschiebenKorrekteMengeAnSpielplattenBehaelt() {
 		testBrett.fuegeStatischePlattenEin();
 		testBrett.spielplatteLinksEinschieben(35);
 		assertEquals(49, testBrett.getAlleSpielplatten().size());
 	}
 	
 	public void testObSpielplatteLinksEinschiebenKorrektePlatteRausschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte raus = testBrett.getAlleSpielplatten().get(27);
 		testBrett.spielplatteLinksEinschieben(21);
 		assertEquals(raus, testBrett.getAktivePlatte());
 	}
 	
 	public void testObSpielplatteLinksEinschiebenAktivePlatteAnkorrekterPositionEinschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte aktiv = testBrett.getAktivePlatte();
 		testBrett.spielplatteLinksEinschieben(7);
 		assertEquals(aktiv, testBrett.getAlleSpielplatten().get(7));
 	}
 	
 	public void testObSpielplatteRechtsEinschiebenKorrektePlatteRausschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte raus = testBrett.getAlleSpielplatten().get(7);
 		testBrett.spielplatteRechtsEinschieben(13);
 		assertEquals(raus, testBrett.getAktivePlatte());
 	}
 	
 	public void testObSpielplatteRechtsEinschiebenAktivePlatteAnkorrekterPositionEinschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte aktiv = testBrett.getAktivePlatte();
 		testBrett.spielplatteRechtsEinschieben(41);
 		assertEquals(aktiv, testBrett.getAlleSpielplatten().get(41));
 	}
 	
 	public void testObSpielplatteObenEinschiebenKorrektePlatteRausschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte raus = testBrett.getAlleSpielplatten().get(45);
 		testBrett.spielplatteObenEinschieben(3);
 		assertEquals(raus, testBrett.getAktivePlatte());
 	}
 	
 	public void testObSpielplatteObenEinschiebenAktivePlatteAnkorrekterPositionEinschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte aktiv = testBrett.getAktivePlatte();
 		testBrett.spielplatteObenEinschieben(1);
 		assertEquals(aktiv, testBrett.getAlleSpielplatten().get(1));
 	}
 	
 	public void testObSpielplatteObenEinschiebenDieViertePlatteKorrektWeiterschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte altePlattePos24 = testBrett.getAlleSpielplatten().get(24);
 		testBrett.spielplatteObenEinschieben(3);
 		assertEquals(altePlattePos24, testBrett.getAlleSpielplatten().get(31));
 	}
 	
 	public void testObSpielplatteUntenEinschiebenKorrektePlatteRausschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte raus = testBrett.getAlleSpielplatten().get(5);
 		testBrett.spielplatteUntenEinschieben(47);
 		assertEquals(raus, testBrett.getAktivePlatte());
 	}
 	
 	public void testObSpielplatteUntenEinschiebenAktivePlatteAnKorrekterPositionEinschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte aktiv = testBrett.getAktivePlatte();
 		testBrett.spielplatteUntenEinschieben(43);
 		assertEquals(aktiv, testBrett.getAlleSpielplatten().get(43));
 	}
 	
 	public void testObSpielplatteUntenEinschiebenDieDrittePlatteKorrektWeiterschiebt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte altePlattePos31 = testBrett.getAlleSpielplatten().get(31);
 		testBrett.spielplatteUntenEinschieben(45);
 		assertEquals(altePlattePos31, testBrett.getAlleSpielplatten().get(24));
 	}
 	
 	public void testObSpielplatteEinschiebenLinksWaehlt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte altePlattePos24 = testBrett.getAlleSpielplatten().get(24);
 		this.testBrett.spielplatteEinschieben(testBrett.getAlleSpielplatten().get(21));
 		assertEquals(altePlattePos24, testBrett.getAlleSpielplatten().get(25));
 	}
 	
 	public void testObSpielplatteEinschiebenRechtsWaehlt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte altePlattePos24 = testBrett.getAlleSpielplatten().get(24);
 		this.testBrett.spielplatteEinschieben(testBrett.getAlleSpielplatten().get(27));
 		assertEquals(altePlattePos24, testBrett.getAlleSpielplatten().get(23));
 	}
 	
 	public void testObSpielplatteEinschiebenObenWaehlt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte altePlattePos24 = testBrett.getAlleSpielplatten().get(24);
 		this.testBrett.spielplatteEinschieben(testBrett.getAlleSpielplatten().get(3));
 		assertEquals(altePlattePos24, testBrett.getAlleSpielplatten().get(31));
 	}
 	
 	public void testObSpielplatteEinschiebenUntenWaehlt() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte altePlattePos24 = testBrett.getAlleSpielplatten().get(24);
 		this.testBrett.spielplatteEinschieben(testBrett.getAlleSpielplatten().get(45));
 		assertEquals(altePlattePos24, testBrett.getAlleSpielplatten().get(17));
 	}
 	
	public void testObSpielplatteEinschiebenFehlerWirftWennStelleNichtZumEinschiebenIst() {
 		testBrett.fuegeStatischePlattenEin();
 		Spielplatte altePlattePos24 = testBrett.getAlleSpielplatten().get(24);
 		this.testBrett.spielplatteEinschieben(testBrett.getAlleSpielplatten().get(28));
 		assertEquals(altePlattePos24, testBrett.getAlleSpielplatten().get(25));
 	}
 	
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 }
