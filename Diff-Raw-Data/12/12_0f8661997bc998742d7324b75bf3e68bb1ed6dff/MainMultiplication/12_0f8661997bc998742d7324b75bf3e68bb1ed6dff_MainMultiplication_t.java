 package ac.multiplication;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 
 public class MainMultiplication {
 
 	/**
 	 * tests : 275 * 332 , 475 * 32 , 97 * 32 , 99 * 999
 	 */
 	public static void main(String[] args) {
 		
 		ArrayList<Integer> chiffreUn = new ArrayList<Integer>() ;
 		ArrayList<Integer> chiffreDeux = new ArrayList<Integer>() ;
 		
 		Scanner sc = new Scanner(System.in) ;
 		System.out.println("Saisissez le premier chiffre à multiplier : ") ;
 		String premierChiffre = sc.nextLine() ;
 		// Création de chiffreUn puis inversion :
 				for(int i = 0 ; i < premierChiffre.length() ; ++i){
 					int e = (int) (premierChiffre.charAt(i) - '0') ;
 					chiffreUn.add(e) ;
 				}
 				Collections.reverse(chiffreUn) ;
 				
 		System.out.println("Saisissez le deuxième chiffre à multiplier : ") ;
 		String deuxiemeChiffre = sc.nextLine() ;
 		// Création de chiffreDeux puis inversion :
 				for(int i = 0 ; i < deuxiemeChiffre.length() ; ++i){
 					int e = (int) (deuxiemeChiffre.charAt(i) - '0') ;
 					chiffreDeux.add(e) ;
 				}
 				Collections.reverse(chiffreDeux) ;
 				
 		System.out.println("Saisissez la base : ") ;
 		int BASE = sc.nextInt() ;
 		
 		int retenue = 0 ;
 		int multiplication ;
 		
 		
 		// Création de l'array contenant le résultat final -------------------
 		int tailleResult = chiffreUn.size() + chiffreDeux.size() ;
 		ArrayList<Integer> result = new ArrayList<Integer>(tailleResult) ;
 		for(int i = 0 ; i < tailleResult ; ++i){
 			result.add(0);
 		}
 		//--------------------------------------------------------------------
 		int addition ; // Le résultat des additions intermédiaires
 		int index = 0; // pour se promener dans result
 		
 		for(int i = 0 ; i < chiffreDeux.size() ; ++i){
 			if(retenue != 0) {
 				result.set(index, retenue) ;
 				retenue = 0 ;
 			}
 			index = i ;
 			for(int j = 0 ; j < chiffreUn.size() ; ++j){
 				
 				multiplication = (chiffreDeux.get(i) * chiffreUn.get(j)) + retenue ;
 				retenue = 0 ; // réinitialisation de la retenue
 				
 				if(multiplication < BASE) {
 					addition = multiplication + result.get(index) ;
 					if(addition >= BASE) {
 						retenue = addition / BASE ;
 						addition %= BASE ;
 					}
 					result.set(index, addition) ;
 				}
 				
 				else if(multiplication == BASE) {
 					result.set(index, (0 + result.get(index))) ;
 					retenue = 1 ;
 				}
 				
 				else{
 					addition = (multiplication % BASE) + result.get(index) ;
 					if(addition >= BASE) {
 						retenue = addition / BASE ;
 						addition %= BASE ;
 					}
 					result.set(index, addition ) ;
 					while(multiplication % BASE != 0 && multiplication > 1 ){
 						retenue += multiplication / BASE ;
 						multiplication /= BASE ;
 					} // fin while
 					if(multiplication > BASE) {
 						retenue += multiplication / BASE ;
 					}
 				}
 				index++ ;
 			} // fin for j 
 			
 		} // fin for i
 		
 		if(retenue != 0) { // test si il reste une retenue non ajoutée à result
 			result.set(result.size()-1, retenue) ;
 			retenue = 0 ;
 		}
 		
		// NORMALISATION :
		Collections.reverse(result);
		for(int i = 0 ; i < result.size() ; ++i){
			if(result.get(i) > 0) break ;
			
			if(result.get(i) == 0) {
				result.remove(i) ;
			}
		}
		
 		
 		
 		// AFFICHAGE DU RESULTAT :
 		String reponse = "" ;
 		for(int elem : result){
 			reponse += String.valueOf(elem);
 		}
 		System.out.println(reponse) ;
 		
 		
 
 	} // fin méthode main
 
 } // fin classe MainMultiplication
