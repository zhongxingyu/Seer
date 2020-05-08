 package Exo05_If_Elself_Switch;
 
 import java.util.Random;
 
 /**
  * Class Description: ?
  * <p/>
  * 2013-04-30 - david
  */
 public class SansCervelle {
 	public static void main(String[] args) {
 		Random rdmGenerateur = new Random();
 		int nombreSecret = 1 + rdmGenerateur.nextInt(10000 - 1 +1);
 		int aa = 0;

		System.out.println("Laissez moi chercher ...");
 		while (true) {
			int nombreAleatoire = 1 + rdmGenerateur.nextInt(10000 - 1 +1);
			aa++;

 			if (nombreAleatoire == nombreSecret) {
 				System.out.printf("Nombre secret trouvé = %s\n", nombreSecret);
 				System.out.printf("Nombre d'essai = %s\n", aa);
 				break;
 			}
 
 		}
 
 
 	}
 }
