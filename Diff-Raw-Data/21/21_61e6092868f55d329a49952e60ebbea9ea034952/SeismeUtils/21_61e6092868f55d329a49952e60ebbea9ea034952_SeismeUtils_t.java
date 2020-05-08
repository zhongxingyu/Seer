 package seismes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 
 /**
  * Classe utilitaire pour le tri des entrees
  * @author Eric Thivierge, Vincent Foley
  *
  */
 public class SeismeUtils {
     /**
      * Trie les séismes par date
      * @param arr le tableau de séisme
      */
     public static void sortByDate(Seisme[] arr) {
         Arrays.sort(arr, new Comparator<Seisme>() {
            public int compare(Seisme s1, Seisme s2) {
         	   if (s1 != null && s2 != null)
         		   return s1.getDatetime().compareTo(s2.getDatetime());
         	   else
         		   return 0;
            }
         });
     }
     
     /**
      * Trie les séismes par distance
      * @param arr le tableau de séisme
      */
     public static void sortByDistance(Seisme[] arr, final Coord point) {
         Arrays.sort(arr, new Comparator<Seisme>() {
            public int compare(Seisme s1, Seisme s2) {
         	   if (s1 != null && s2 != null)
         		   return (int)((s1.distance(point) - s2.distance(point)) * 10);
         	   else
         		   return 0;
            }
         });
     }
     
     /**
      * Trie les séismes par distance
      * @param arr le tableau de séisme
      */
     public static void sortByMagnitude(Seisme[] arr) {
         Arrays.sort(arr, new Comparator<Seisme>() {
            public int compare(Seisme s1, Seisme s2) {
         	   if (s1 != null && s2 != null)
         		   return (int)((s1.getMagnitude() - s2.getMagnitude()) * 10);
         	   else
         		   return 0;
            }
         });
     }
     
     public static boolean isAccepted(Date afterDate, double latitude,
     							     double longitude, double distance,
     								 double magnitude, Seisme seisme) {
     	
     	return (seisme.getDatetime().after(afterDate)) &&
     		    (new Coord(latitude, longitude).distance(seisme.getCoord()) <= distance) &&
     		    (seisme.getMagnitude() >= magnitude);
     }
     
     /**
      * Filtre les seismes selon les params donnés
      * @param afterDate	ne pas afficher les seismes avant cette date
      * @param latitude	latitude de référence
      * @param longitude	longitude de référence
      * @param distance	distance max par rapport à (latitude, longitude)
      * @param magnitude	magnitude minimale
      * @param filename	le fichier contenant les seismes enregistrés
      */
     public static Seisme[] filterSeismes(Date afterDate, double latitude,
 		                               double longitude, double distance,
 			                           double magnitude, String filename) {
     	
         Parser p = new Parser(filename);
     	Seisme[] all;
         ArrayList<Seisme> accepted = new ArrayList<Seisme>();
 		
 		p.parse();
 		all = p.getSeismes();
 		for (Seisme s: all) {
 			if (isAccepted(afterDate, latitude, longitude, distance, magnitude, s)) {
 				accepted.add(s);
 			}
 		}
    	return accepted.toArray(new Seisme[0]);
     }
 }
