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
     public void lisaaminenYlivuoto() {
 
         // lisää yli
         varasto.lisaaVarastoon(12);
         assertEquals(10, varasto.getSaldo(), vertailuTarkkuus);
     }
 
     @Test
     public void lisaaminenAlivuoto() {
 
         // lisää ali
         varasto.lisaaVarastoon(-2);
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
 
     @Test
     public void ottaminenYlivuoto() {
         varasto.lisaaVarastoon(2);
 
         // ota yli
         varasto.otaVarastosta(-2);
        assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
 
     @Test
     public void ottaminenAlivuoto() {
 
         // ota ali
         varasto.otaVarastosta(2);
         assertEquals(0, varasto.getSaldo(), vertailuTarkkuus);
     }
 
     @Test
     public void konstr() {
         varasto = new Varasto(-1);
         varasto = new Varasto(0);
         varasto = new Varasto(1, 1);
         varasto = new Varasto(1, 2);
         varasto = new Varasto(-1, 2);
         varasto = new Varasto(-1, -1);
         varasto.toString();
     }
 }
