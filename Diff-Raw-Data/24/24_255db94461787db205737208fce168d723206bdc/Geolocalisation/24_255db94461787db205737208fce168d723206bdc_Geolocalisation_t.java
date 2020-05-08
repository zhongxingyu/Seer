 import java.util.Iterator;
 import java.util.Scanner;
 
 public class Geolocalisation {
         
    public static void geolocalisation(Plan plan) {
         Scanner sc = new Scanner(System.in);
         Station util = null;
         boolean saisieOk = false, choixOk = false;
         do {
             System.out.print("Vous trouvez-vous dans une station (O : oui/N : non) ? ");
             String rep = sc.next();
 
             if (rep.toUpperCase().compareTo("O") == 0) {
                 choixOk = true;
                 //On affiche les stations dispo
                 Iterator<Station> is = plan.getStations().iterator();
                 int i = 1;
                 while (is.hasNext()) {
                     System.out.println(i + " - " + is.next());
                     i++;
                 }
 
                 int nStation = 0;
                 do {
                     System.out.print("Quel est son nom (tapez le numero correspondant à votre station) ? ");
                     try {
                         nStation = Integer.parseInt(sc.next());
 
                         if (nStation < 0 || nStation > plan.getStations().size()) {
                             throw new NumberFormatException();
                         }
                         saisieOk = true;
                     } catch (NumberFormatException e) {
                         System.out.println("\nChoix incorrect.");
                     }
                 } while (!saisieOk);
 
                 is = plan.getStations().iterator();
                 i = 1;
                 while (is.hasNext() && i <= nStation) {
                     if (i == nStation) {
                         util = is.next();
                     } else {
                         i++;
                         is.next();
                     }
                 }
 
                 System.out.println("Vous vous trouvez à " + util + ".");
                 plan.setStationUtil(util);
             } else if (rep.toUpperCase().compareTo("N") == 0) {
                 choixOk = true;
                 double lat = 0, lon = 0;
 
                 do {
                     System.out.println("A quelle latitude vous trouvez-vous ?");
                     try {
                         lat = Double.parseDouble(sc.next());
                         saisieOk = true;
                     } catch (NumberFormatException e) {
                         System.out.println("\nChoix incorrect.");
                     }
                 } while (!saisieOk);
 
                 do {
                     System.out.println("A quelle longitude vous trouvez-vous ?");
                     try {
                         lon = Double.parseDouble(sc.next());
                         saisieOk = true;
                     } catch (NumberFormatException e) {
                         System.out.println("\nChoix incorrect.");
                     }
                 } while (!saisieOk);
 
                 util = plan.getStationProche(new Coordonnee(lat, lon));
                 plan.setStationUtil(util);
                 System.out.println("La station la plus proche est " + util + ".");
             } else {
                 System.out.println("Merci de respecter le format d'ecriture.");
             }
         } while (!choixOk);
     }
     
 }
