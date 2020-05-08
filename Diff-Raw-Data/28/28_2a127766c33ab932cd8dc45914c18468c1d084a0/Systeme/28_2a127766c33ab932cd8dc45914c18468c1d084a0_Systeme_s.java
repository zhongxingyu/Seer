 
 public class Systeme {
 	private Matrice matriceA;
 	private Matrice matriceB;
 	
 	public Systeme(Matrice a, Matrice b)
 	{
 		matriceA = a;
 		matriceB = b;
 	}
 	
 	public Matrice getMatriceA()
 	{
 		return matriceA;
 	}
 	
 	public Matrice getMatriceB()
 	{
 		return matriceB;
 	}
 	
 	public Matrice trouverXParCramer()
 	{
 		double detA = matriceA.getDeterminant();
 		
 		Matrice result = new Matrice(new double[matriceB.getLignes()][matriceB.getColonnes()]);
 		
 		if (detA == 0) {
 			System.err.println("La matrice a un d�terminant �gal � 0.");
 			return result;
 		}
 		
 		for (int currentCol = 0; currentCol < matriceA.getColonnes();currentCol++) {
 			Matrice matriceAn = new Matrice(new double[matriceA.getLignes()][matriceA.getColonnes()]);
 			
 			for (int ligne = 0; ligne < matriceA.getLignes(); ligne++) {
 				for (int colonne = 0; colonne < matriceA.getColonnes(); colonne++) {
 					if (colonne == currentCol){
 						matriceAn.setElement(ligne, colonne, matriceB.getElement(ligne, 0));
 					}
 					else {
 						matriceAn.setElement(ligne, colonne, matriceA.getElement(ligne, colonne));
 					}
 				}
 			}
 			
 			double detAn = matriceAn.getDeterminant();
 			
 			result.setElement(currentCol, 0, detAn / detA);
 		}
 		
 		return result;
 	}
 	
 	public Matrice trouverXParInversionMatricielle()
 	{
 		 boolean matReguliere = matriceA.estReguliere();
 		 
 		// S'assurer d'un d�terminant non null
 		 if (!matReguliere) {
 			 System.err.println("La matrice n'est pas r�guli�re (det �gale 0)");
 			 return matriceA;
 		 }
 		 
 		 Matrice matInverse   = matriceA.getMatriceInverse(); 
 		 return matInverse.faireProduitMatriciel(matriceB);
 	}
 	
 	public Matrice trouverXParJacobi(double epsilon)
 	{
 		//Vérification de la dominance diagonale stricte
 		if (!matriceA.verifierDominanceDiagonaleStricte()) {
 			System.err.println("La matrice n'est pas strictement dominante diagonalement.");
 			return null;
 		}
 	
 		Matrice d = matriceA.getDiagonale(); //Matrice diagonale
 		Matrice l = matriceA.getTriangulaireInferieure(); //Matrice triangulaire inférieure
 		Matrice u = matriceA.getTriangulaireSuperieure(); //Matrice triangulaire supérieure
 		
 		Matrice lu = l.additionner(u); //Addition des matrices triangulaires inférieure et supérieure
 		
 		Matrice results = new Matrice(new double[matriceA.getLignes()][1]); //Matrice contenant les résultats de l'itération en cours
 		Matrice lastResults = new Matrice(new double[matriceA.getLignes()][1]); //Matrice contenant les résultats de la dernière itération
 		
 		boolean stop;
 		
 		do {
 			stop = true;
 			
 			for (int ligne = 0;ligne < matriceA.getLignes();ligne++) {
 				double result = 0;
 				
 				//Addition des dernières valeurs de x par les valeurs dans l'addition des matrices triangulaires l et u
 				for (int colonne = 0; colonne < matriceA.getColonnes();colonne++) {
 					result += lastResults.getElement(colonne, 0) * lu.getElement(ligne, colonne);
 				}
 				
 				//On soustrait b au résultat de lu * x et on divise par la diagonale
 				result = (matriceB.getElement(ligne, 0) - result) / d.getElement(ligne, ligne); 
 				
 				results.setElement(ligne, 0, result);
 				
 				//Vérifier si le critère de terminaison est rencontré
				if (epsilon * -1 > result - lastResults.getElement(ligne, 0) && result - lastResults.getElement(ligne, 0) > epsilon) {
 					stop = false;
 				}
 			}
 			
 			//Copie des résultats dans la matrice des derniers résultats
 			lastResults = results.copy();
 			
 		} while (!stop); //Critère de terminaison rencontré 
 		
 		return results;
 	}
 }
