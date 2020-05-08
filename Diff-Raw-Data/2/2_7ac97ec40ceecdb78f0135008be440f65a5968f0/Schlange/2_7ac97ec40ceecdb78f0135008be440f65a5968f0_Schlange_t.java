 import java.awt.Point;
 import java.util.ArrayList;
 
 /**
  * Repraesentiert eine Schlange
  * 
  */
 public class Schlange {
   private ArrayList<Point> schlange;
 
   /**
    * Konstruiert eine Schlangen mit Startposition
    * bei den angegebenen Koordinaten. 
    * 
    * @param x x-Koordinate der Schlangenposition
    * @param y y-Koordinate der Schlangenposition
    */
   public Schlange(int x, int y) {
     schlange = new ArrayList<Point>();
     schlange.add(new Point(x, y));
   }
 
   /**
    * Prueft, ob die Schlange am bezeichneten Standort ist
    * 
    * @param standort
    * @return wahr, falls die Schlange am bezeichneten Standort ist
    */
   public boolean istAufPunkt(Point standort) {
     return schlange.contains(standort);
   }
 
   /**
    * Prueft, ob der Schlangenkopf am bezeichneten Standort ist
    * 
    * @param standort
    * @return wahr, falls der Schlangenkopf am bezeichneten Standort ist
    */
   public boolean istKopfAufPunkt(Point standort) {
     return kopf().equals(standort);
   }
 
   /**
    * Bewege die Schlange in die angegebene Richtung.<br />
    * Gueltige Richtungen sind:
    * <ul>
    * <li>w  (oben)</li>
    * <li>s  (unten)</li>
    * <li>a  (links)</li>
    * <li>d  (rechts)</li>
    * </ul>
    * <br />
    * 
    * @param richtung Richtung, in die sich die Schlange bewegen soll
    */
   public void bewege(char richtung) {
     Point kopf = kopf();
     switch (richtung) {
     case 'w':
       schlange.add( new Point(kopf.x, kopf.y - 1) );
       schlange.remove(0);
       break;
     case 's':
       schlange.add( new Point(kopf.x, kopf.y + 1) );
       schlange.remove(0);
       break;
     case 'a':
       schlange.add( new Point(kopf.x - 1, kopf.y) );
       schlange.remove(0);
       break;
     case 'd':
       schlange.add( new Point(kopf.x + 1, kopf.y) );
       schlange.remove(0);
       break;
     }
   }
 
   /**
    * Testet, ob der Kopf der Schlange auf dem Koerper liegt.
    * 
    * @return true, falls der Kopf auf dem Koerper liegt
    */
   public boolean istKopfAufKoerper() {
     boolean istKopfAufKoerper = false;
 
     for(int i=0; i<schlange.size()-2; i++) {
       istKopfAufKoerper |= istKopfAufPunkt(schlange.get(i));
     }
     return istKopfAufKoerper;
   }
 
   /**
    * Laesst die Schlange um eine Einheit laenger werden.
    */
   public void wachsen(int veraenderungGroesse) {
 	for (int i = 0; i < veraenderungGroesse; i++)
 	{
 		Point schwanz = schlange.get(0);
    	schlange.add(0, new Point(schwanz.x+1, schwanz.y));
 	}
   }
 
   /**
    * Liefert die Position des Kopfs der Schlange zurueck.
    * 
    * @return die aktuelle Position (Kopf) der Schlange
    */
   public Point gibPosition() {
     return kopf();
   }
 
   private Point kopf() {
     return schlange.get(schlange.size()-1);
   }
 }
