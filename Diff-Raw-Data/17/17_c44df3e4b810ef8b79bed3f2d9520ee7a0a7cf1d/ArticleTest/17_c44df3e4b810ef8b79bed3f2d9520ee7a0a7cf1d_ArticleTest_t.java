 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package viitteenhallinta;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author atteaalt
  */
 public class ArticleTest {
     private Parser parseri;
     private Article artikkeli;
     private String oikeaRivi;
     
     public ArticleTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
         parseri = new Parser();
        artikkeli = new Article("Tagi", "Jussi Paananen", "Suomen MM -juhlimisen herattamasta pahennuksesta vuonna 2011 ja miten se peilaa nyky-yhteiskuntaamme", "Taysin turhia tieteellisia artikkeleita", 2012, parseri);
         oikeaRivi = "Author: Jussi Paananen"
                + "\nTitle: Suomen MM -juhlimisen herattamasta pahennuksesta vuonna 2011 ja miten se peilaa nyky-yhteiskuntaamme"
                + "\nJournal: Taysin turhia tieteellisia artikkeleita"
                 + "\nYear: 2012";
     }
         //volume, number, pages, month, note, key
     @After
     public void tearDown() {
     }
     
     @Test
     public void constructorWorks(){
         assertEquals(oikeaRivi, artikkeli.toReadableString());
     }
     
     @Test
     public void testSetVolume(){
         artikkeli.setVolume(1);
         oikeaRivi += "\nVolume: 1";
         assertEquals(oikeaRivi, artikkeli.toReadableString());
     }
     
     @Test
     public void testSetVolumeAndPages(){
         artikkeli.setPages("158--170");
         artikkeli.setVolume(1);
         oikeaRivi += "\nVolume: 1\nPages: 158--170";
         assertEquals(oikeaRivi, artikkeli.toReadableString());                
     }
     
     @Test
     public void testWithAllSetters(){
         artikkeli.setPages("158--170");
         artikkeli.setNumber(1);
         artikkeli.setMonth("jan");
         artikkeli.setNote("This is a note");
         artikkeli.setKey("This is a key");
         artikkeli.setVolume(1);
         oikeaRivi += "\nVolume: 1"
                 + "\nNumber: 1"
                 + "\nPages: 158--170"
                 + "\nMonth: jan"
                 + "\nNote: This is a note"
                 + "\nKey: This is a key";
         assertEquals(oikeaRivi, artikkeli.toReadableString());
     }
     @Test
     public void testToLaTexStringWithCompulsoryVariables(){
           String oikeaLaTexRivi = "@Article(Tagi,"
                 +"\nAUTHOR = {Jussi Paananen},"
                + "\nTITLE = {Suomen MM -juhlimisen herattamasta pahennuksesta vuonna 2011 ja miten se peilaa nyky-yhteiskuntaamme},"
                + "\nJOURNAL = {Taysin turhia tieteellisia artikkeleita},"
                 + "\nYEAR = {2012},"
                 + "\n)";
           assertEquals(oikeaLaTexRivi, artikkeli.toLaTexString());
     }
     @Test
     public void testToLaTexStringWithAllVariables(){
         artikkeli.setPages("158--170");
         artikkeli.setNumber(1);
         artikkeli.setMonth("jan");
         artikkeli.setNote("This is a note");
         artikkeli.setKey("This is a key");
         artikkeli.setVolume(1);
         String oikeaLaTexRivi = "@Article(Tagi,"
                 +"\nAUTHOR = {Jussi Paananen},"
                + "\nTITLE = {Suomen MM -juhlimisen herattamasta pahennuksesta vuonna 2011 ja miten se peilaa nyky-yhteiskuntaamme},"
                + "\nJOURNAL = {Taysin turhia tieteellisia artikkeleita},"
                 + "\nYEAR = {2012},"
                 + "\nVOLUME = {1},"
                 + "\nNUMBER = {1},"
                 + "\nPAGES = {158--170},"
                 + "\nMONTH = {jan},"
                 + "\nNOTE = {This is a note},"
                 + "\nKEY = {This is a key},"
                 + "\n)";
         assertEquals(oikeaLaTexRivi, artikkeli.toLaTexString());
     }
     
 
 }
