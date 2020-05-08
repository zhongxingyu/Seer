 
 import java.util.Scanner;
 
 public class Main {
 
     private static String fichier = "plan.txt";
 
     public static void main(String[] args) {
         Plan plan = new Plan(fichier);
         System.out.println(plan);
         
         boolean ok=true;
 
         System.out.println("BIENVENU que desirez vous faire ?\n");
         
 	while (ok) {
 		System.out.println("        MENU        ");
 		System.out.println("1 - Vous localiser");
 		System.out.println("2 - Connaître les incidents");
 		System.out.println("3 - Signaler un incident");
 		System.out.println("4 - Itineraire le plus rapide");
 		System.out.println("5 - Connaitre l_itineraire avec le moins de changement");
                 System.out.println("6 - Connaitre l_itineraire avec différentes etapes");
 		int reponse;
 		Scanner sc=new Scanner(System.in);
 		reponse=sc.nextInt();
 
 		switch(reponse){
 			case 1 :
                                Geolocalisation.geolocalisation(plan);
 				break;
 			case 2 :
 				
 				break;
 			case 3:
 				plan.ajoutincident();
 				break;
 			case 4:
 				
 				break;
 			case 5:
 				
 				break;
                         case 6 :
                                 
                                 break;
 			default:
				Geolocalisation.geolocalisation(plan);
 				break;
 		}
        
                 System.out.println("\nAvez-vous quelque chose d'autre à faire ? (O : oui/N : non) ? ");
 		String Rep;
 		Rep=sc.next();
 		if (Rep.compareTo("O")==0) {
 			ok=true;
 		}
 		else {
 			ok=false;
 		}
         }
    }  
 }
