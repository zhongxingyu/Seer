 
 import java.io.ByteArrayInputStream;
 import java.text.DecimalFormat;
 import junit.framework.TestCase;
 
 /**
  *
  * @author Loïc Cimon
  */
 public class testGeolocalisation extends TestCase {
 
     /*
      * Test de la méthode distance de Coordonnee
      */
     public void testDistance() {
         double distance;
         DecimalFormat f;
 
         Coordonnee c1 = new Coordonnee(1., 1.);
         distance = c1.distance(c1);
         assertEquals(0., distance);
 
         Coordonnee c2 = new Coordonnee(2., 2.);
         distance = c1.distance(c2);
         f = new DecimalFormat();
         f.setMaximumFractionDigits(4);
         assertEquals("1,4142", f.format(distance));
 
         distance = c2.distance(c1);
         f = new DecimalFormat();
         f.setMaximumFractionDigits(4);
         assertEquals("1,4142", f.format(distance));
     }
 
     /*
      * Test de la méthode geolocalisation de Main
      */
     public void testGeolocalisation() {
         //On crée le plan initial
         Plan p = new Plan();
         Station sauge = new Station("Sauge", new Coordonnee(48.91, 2.30));
         Station capucine = new Station("Capucine", new Coordonnee(48.89, 2.35));
         p.addStation(sauge);
         p.addStation(capucine);
 
         /*
          * Scénario 1
          */
 
         String data = "A\r\nO\r\np\r\n-1\r\n2\r\n1\r\n";
         System.setIn(new ByteArrayInputStream(data.getBytes()));
        Geolocalisation.geolocalisation(p);
         assertEquals(capucine, p.getStationUtil());
 
         /*
          * Scénario 2
          */
         data = "N\r\np\r\n-5\r\n48.89\r\n2.35\r\n";
         System.setIn(new ByteArrayInputStream(data.getBytes()));
        Geolocalisation.geolocalisation(p);
         assertEquals(capucine, p.getStationUtil());
     }
 }
