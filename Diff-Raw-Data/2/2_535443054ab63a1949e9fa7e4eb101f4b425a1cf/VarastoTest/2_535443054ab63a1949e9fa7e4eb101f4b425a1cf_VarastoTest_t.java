 
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
        assertEquals(0, varasto.getSaldo(), vertailuTarkkuus); // 0 -> 1
     }
  
     @Test
     public void uudellaVarastollaOikeaTilavuus() {
         assertEquals(10, varasto.getTilavuus(), vertailuTarkkuus);
     }
  
     @Test
     public void lisaysLisaaSaldoa() {
         varasto.lisaaVarastoon(8);
  
         // saldon pitäisi olla sama kun lisätty määrä
         assertEquals(8, varasto.getSaldo(), vertailuTarkkuus);
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
     public void ottaminenLisääTilaa() {
         varasto.lisaaVarastoon(8);
  
         varasto.otaVarastosta(2);
  
         // varastossa pitäisi olla tilaa 10 - 8 + 2 eli 4
         assertEquals(4, varasto.paljonkoMahtuu(), vertailuTarkkuus);
     }
     @Test
     public void kunOttaaLiikaaNiinEiNegatiiviseksi() {
         varasto.lisaaVarastoon(4);
         
         varasto.otaVarastosta(6);
         assertEquals(0, varasto.getSaldo(),  vertailuTarkkuus);
     }
     @Test
     public void kunOtetaanLiikaaNiinPalautetaanSeMitaVoidaan() {
         varasto.lisaaVarastoon(4);
         
         assertEquals(4, varasto.otaVarastosta(6), vertailuTarkkuus);
     }
     @Test
     public void kunLisataanLiikaaNiinLisataanVainTayteen() {
         varasto.lisaaVarastoon(8);
         varasto.lisaaVarastoon(10);
         
         assertEquals(10, varasto.getSaldo(), vertailuTarkkuus);
     }
     @Test
     public void ottaminenPienentaaSaldoa() {
         varasto.lisaaVarastoon(3);
         
         varasto.otaVarastosta(2);
         
         assertEquals(1, varasto.getSaldo(), vertailuTarkkuus);
     }
     @Test
     public void negatiivinenTilavuusNollataan() {
         varasto = new Varasto(-2);
         assertEquals(0, varasto.getTilavuus(), vertailuTarkkuus);
     }
     @Test
     public void josSaldoAluksiNegatiivinenNiinNollataan() {
         varasto = new Varasto(2, -10);
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
     @Test
     public void aluksiSaldoPienempiTaiYhtasuuriKuinTilavuus() {
         varasto = new Varasto(2, 3);
         assertEquals(2, varasto.getSaldo(), vertailuTarkkuus);
     }
     @Test
     public void lisattaessaNegatiivistaMaaraaEiTehdaMitaan() {
         varasto.lisaaVarastoon(-3);
         
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
     @Test
     public void toStringToimiiOIkein() {
         assertEquals("saldo = 0.0, vielä tilaa 10.0", varasto.toString());
     }
     @Test
     public void kuormitetullaKonstruktorillaTilavuusEiNegatiivinen() {
         varasto = new Varasto(-2, 0);
         assertEquals(0, varasto.getTilavuus(), vertailuTarkkuus);
     }
     @Test
     public void kuormitetullaKonstruktorillaSaldoKorkeintaanTilavuus() {
         varasto = new Varasto(2, 2);
         
         assertEquals(2, varasto.getSaldo(), vertailuTarkkuus);
     }
     @Test
     public void varastostaEiVoiOttaaNegatiivistaMaaraa() {
         varasto.otaVarastosta(-22);
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
 }
