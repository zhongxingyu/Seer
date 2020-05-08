 package de.uni_hamburg.informatik.sep.zuul.spiel;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import de.uni_hamburg.informatik.sep.zuul.ISchreiber;
 
 public class SpielKontextTest
 {
 	SpielKontext testKontext = new SpielKontext(new ISchreiber()
 	{
 
 		@Override
 		public void schreibeNL(String nachricht)
 		{
 			//Dummy for TestCase
 		}
 
 		@Override
 		public void schreibe(String nachricht)
 		{
 			//Dummy for TestCase
 		}
 	});
 
 	@Test
 	public void testSpielKontext()
 	{
 
 	}
 
 	@Test
 	public void testSchreibeNL()
 	{
 
 	}
 
 	@Test
 	public void testSchreibe()
 	{
 
 	}
 
 	@Test
 	public void testGetAktuellerRaum()
 	{
		Raum testraum = new Raum("tr", "testRaum");
 		testKontext.setAktuellerRaum(testraum);
 
 		assertEquals(testraum, testKontext.getAktuellerRaum());
 	}
 
 	@Test
 	public void testSetAktuellerRaum()
 	{
		Raum testraum = new Raum("tr", "testRaum");
 		testKontext.setAktuellerRaum(testraum);
 
 		assertEquals(testraum, testKontext.getAktuellerRaum());
 
 	}
 
 	@Test
 	public void testIsSpielZuende()
 	{
 		assertFalse(testKontext.isSpielZuende());
 		testKontext.beendeSpiel("vorbei!!!");
 		assertTrue(testKontext.isSpielZuende());
 	}
 
 	@Test
 	public void testBeendeSpiel()
 	{
 		assertFalse(testKontext.isSpielZuende());
 		testKontext.beendeSpiel("vorbei!!!");
 		assertTrue(testKontext.isSpielZuende());
 	}
 
 	@Test
 	public void testZeigeRaumbeschreibung()
 	{
 		//Nicht testbar bei dieser SpielKontext Implementation.
 	}
 
 	@Test
 	public void testZeigeAusgaenge()
 	{
 		//Nicht testbar bei dieser SpielKontext Implementation.
 	}
 
 	@Test
 	public void testGetInventar()
 	{
 		//Nicht testbar bei dieser SpielKontext Implementation.
 	}
 
 	@Test
 	public void testGetLebensEnergie()
 	{
 		//DefaultBelegung
 		assertEquals(8, testKontext.getLebensEnergie());
 
 		testKontext.setLebensEnergie(0);
 		assertEquals(0, testKontext.getLebensEnergie());
 
 		//GrenzwertTests
 		testKontext.setLebensEnergie(Integer.MAX_VALUE);
 		assertEquals(Integer.MAX_VALUE, testKontext.getLebensEnergie());
 
 		testKontext.setLebensEnergie(Integer.MIN_VALUE);
 		assertEquals(Integer.MIN_VALUE, testKontext.getLebensEnergie());
 	}
 
 	@Test
 	public void testSetLebensEnergie()
 	{
 		//Fehlerfreies Überschreiben?
 		testKontext.setLebensEnergie(2);
 		testKontext.setLebensEnergie(1);
 		testKontext.setLebensEnergie(0);
 		assertEquals(0, testKontext.getLebensEnergie());
 
 		//GrenzwertTests
 		testKontext.setLebensEnergie(Integer.MAX_VALUE);
 		assertEquals(Integer.MAX_VALUE, testKontext.getLebensEnergie());
 
 		testKontext.setLebensEnergie(Integer.MIN_VALUE);
 		assertEquals(Integer.MIN_VALUE, testKontext.getLebensEnergie());
 	}
 
 }
