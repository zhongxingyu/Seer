 package ohtu.ohtuvarasto;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class VarastoTest {
 
     Varasto varasto;
     double vertailuTarkkuus = 0.0001;
 
     @Before
     public void setUp() {
         varasto = new Varasto(10);
     }
 
     @Test
     public void konstruktoriLuoTyhjanVaraston() {
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
 
     @Test
     public void uudellaVarastollaOikeaTilavuus() {
         assertEquals(10, varasto.getTilavuus(), vertailuTarkkuus);
     }
     
      @Test
     public void liianPieniVarasto() {
          varasto = new Varasto(0);
         assertEquals(0, varasto.getTilavuus(), vertailuTarkkuus);
     }
     @Test
     public void liianPieniVarasto2() {
          varasto = new Varasto(-6);
         assertEquals(0, varasto.getTilavuus(), vertailuTarkkuus);
     }
     @Test
     public void luoErikokoisiaVarastoja() {
         varasto = new Varasto(20);
         assertEquals(20, varasto.getTilavuus(), vertailuTarkkuus);
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
 
      @Test
     public void luoErikokoisiaVarastoja2() {
         varasto = new Varasto(87);
         assertEquals(87, varasto.getTilavuus(), vertailuTarkkuus);
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
      
     @Test
     public void luoEpatyhjäVarasto() {
         varasto = new Varasto(10, 5);
         assertEquals(10, varasto.getTilavuus(), vertailuTarkkuus);
         assertEquals(5, varasto.getSaldo(), vertailuTarkkuus);
     }
     @Test
     public void luoNegatiivisellaSaldolla() {
         varasto = new Varasto(10, -5);
         assertEquals(10, varasto.getTilavuus(), vertailuTarkkuus);
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
//       @Test
//    public void luoLiianPieniVarasto() {
//        varasto = new Varasto(-5, 5);
//        assertEquals(0, varasto.getTilavuus(), vertailuTarkkuus);
//        assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
//    }
     @Test
     public void luoLiianTaysiVarasto() {
         varasto = new Varasto(5, 8);
         assertEquals(5, varasto.getTilavuus(), vertailuTarkkuus);
         assertEquals(5, varasto.getSaldo(), vertailuTarkkuus);
     }
     
     
     @Test
     public void lisaysLisaaSaldoa() {
         varasto.lisaaVarastoon(8);
 
         // saldon pitäisi olla sama kun lisätty määrä
         assertEquals(8, varasto.getSaldo(), vertailuTarkkuus);
     }
        @Test
     public void tyhjaLisaysEiMuutaMitaan() {
         varasto.lisaaVarastoon(-4);
 
         // saldon ei pitäisi muuttua
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
 
     @Test
     public void lisaysLisaaPienentaaVapaataTilaa() {
         varasto.lisaaVarastoon(8);
 
         // vapaata tilaa pitäisi vielä olla tilavuus-lisättävä määrä eli 2
         assertEquals(2, varasto.paljonkoMahtuu(), vertailuTarkkuus);
     }
 
     @Test
     public void ottaminenPalauttaaOikeanMaaran() {
         varasto.lisaaVarastoon(8);
 
         double saatuMaara = varasto.otaVarastosta(2);
 
         assertEquals(2, saatuMaara, vertailuTarkkuus);
     }
         @Test
     public void tyhjanOttaminenEiPalautaMitaan() {
         varasto.lisaaVarastoon(8);
 
         double saatuMaara = varasto.otaVarastosta(-3);
 
         assertEquals(0, saatuMaara, vertailuTarkkuus);
     }
 
     @Test
     public void ottaminenLisääTilaa() {
         varasto.lisaaVarastoon(8);
 
         varasto.otaVarastosta(2);
 
         // varastossa pitäisi olla tilaa 10 - 8 + 2 eli 4
         assertEquals(4, varasto.paljonkoMahtuu(), vertailuTarkkuus);
     }
 
     @Test
     public void konstr() {
         varasto = new Varasto(-1);
         varasto = new Varasto(0);
         varasto = new Varasto(1,1);
         varasto = new Varasto(1,2);
         varasto = new Varasto(-1,2);
         varasto = new Varasto(-1,-1);
         varasto.toString();
     }
     
     @Test
     public void laitetaanLiikaa() {
         varasto = new Varasto(6, 6);
         varasto.lisaaVarastoon(4);
         assertTrue(varasto.getSaldo() == 6);
     
     }
      @Test
     public void laitetaanLiikaa2() {
         varasto.lisaaVarastoon(12);
         assertTrue(varasto.getSaldo() == 10);
     
     }
     @Test
     public void jokuTesti() {
         }
     
     @Test
     public void otetaanLiikaaPalauttaaKaiken() {
         varasto.lisaaVarastoon(6);
         double palautus = varasto.otaVarastosta(8);
         assertTrue(palautus == 6);
     
     }
     
         @Test
     public void otetaanLiikaaNollaaSaldon() {
         varasto.lisaaVarastoon(10);
         varasto.otaVarastosta(14);
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     
     }
 }
