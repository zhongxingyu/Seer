 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package JUnit;
 
import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
import java.util.Arrays;
 import ohtu.viitearto.Tag;
 import ohtu.viitearto.Viite;
 import ohtu.viitearto.Rekisteri;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Keni
  */
 public class RekisteriTest {
     
     public RekisteriTest() {
     }
 
     private Rekisteri rekisteri;
     
     @Before
     public void setUp() {
         rekisteri = Rekisteri.getInstance();
     }
     
     @Test
     public void rekisteristaLuodaanSingleton() {
         Rekisteri toinen = Rekisteri.getInstance();
         
         assertEquals(true, toinen.equals(rekisteri));
     }
     
     @Test
     public void entityManagerLuodaanKutsusta() {
         assertNotNull(rekisteri.getEntityManager());
     }
     
     @Test
     public void viitteenPystyyLisaamaanJaHakemaan() {
         Viite uusi = new Viite();
         uusi.setId(1);
         rekisteri.lisaaViite(uusi);
         
         Viite haettava = rekisteri.haeViite(1);
         assertEquals(haettava, uusi);
     }
     
     @Test
     public void taginPystyyLisaamaanJaHakemaan() {
         Tag uusi = new Tag("uusi");
         rekisteri.lisaaTagi(uusi);
         
         Tag haettava = rekisteri.haeTag("uusi");
         assertEquals("uusi", haettava.getNimi());
     }
     
     @Test
     public void viitteenVoiPoistaa() {
         Viite uusi = new Viite();
         uusi.setId(1);
         rekisteri.lisaaViite(uusi);
         
         Viite haettava = rekisteri.haeViite(1);
         assertEquals(haettava, uusi);
         
         rekisteri.poistaViite(1);
         
         haettava = rekisteri.haeViite(1);
         assertEquals(null, haettava);
     }
     
     @Test
     public void viitteenJollaOnTagejaVoiPoistaa() {
         Viite uusi = new Viite();
         uusi.setId(1);
         
         ArrayList<Tag> tagiLista = new ArrayList<Tag>();
         tagiLista.add(new Tag("eka"));
         tagiLista.add(new Tag("toka"));
         uusi.setTagit(tagiLista);
         
         rekisteri.lisaaViite(uusi);  
         rekisteri.poistaViite(1);
         
         Viite haettava = rekisteri.haeViite(1);
         assertEquals(null, haettava);
 }
     
     @Test
     public void haetaanListaViitteita() {
         Viite uusi = new Viite("tarina", "onni");
         Viite toka = new Viite("satu", "kielo");
         rekisteri.lisaaViite(uusi);
         rekisteri.lisaaViite(toka);
         
         List<Viite> lista = rekisteri.getViitteet();
        ArrayList<String> titleTiedot = new ArrayList<String>();
        titleTiedot.add(lista.get(0).getTitle());
        titleTiedot.add(lista.get(1).getTitle());
        
        Collections.sort(titleTiedot);
        
        assertEquals("satu", titleTiedot.get(0));
        assertEquals("tarina", titleTiedot.get(1));
     }
     
     @Test
     public void viitteenHakuYhdellaSanalla() {
         Viite uusi = new Viite("tarina", "onni");
         Viite toka = new Viite("satu", "kielo");
         rekisteri.lisaaViite(uusi);
         rekisteri.lisaaViite(toka);
         
         List<Viite> lista = rekisteri.haeViiteYhdellaHakuSanalla("kielo", null, "author");
         assertEquals("satu", lista.get(0).getTitle());
     }
     
     @Test
     public void viitteenHakuViiteTyypinPerusteella() {
         Viite uusi = new Viite("tarina", "onni");
         Viite toka = new Viite("satu", "kielo");
         uusi.setAddress("USA");
         uusi.setType("Book");
         rekisteri.lisaaViite(uusi);
         rekisteri.lisaaViite(toka);
         
         List<Viite> lista = rekisteri.haeViiteYhdellaHakuSanalla("USA", "Book", "address");
         assertEquals("tarina", lista.get(0).getTitle());
     }
     
 //    @Test
 //    public void viitteenHakuKahdellaSanalla() {
 //        Viite uusi = new Viite("tarina", "onni");
 //        Viite toka = new Viite("satu", "kielo");
 //        rekisteri.lisaaViite(uusi);
 //        rekisteri.lisaaViite(toka);
 //        
 //        List<Viite> lista = rekisteri.haeViiteKahdellaHakuSanalla("tarina", "satu", null, "title", "title", "or");
 //        assertEquals("tarina", lista.get(0).getTitle());
 //        assertEquals("satu", lista.get(1).getTitle());
 //    }
 }
