 import java.io.Console;
 
 
 public class test {
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 		// --------------------------------------------------------------------
 		// Additionner des matrices
 		// --------------------------------------------------------------------
 		afficherTitre("Additionner des matrices");
 		double[][] add1 = {{5,0,2},{6,-4,4},{1,4,1}};
 		double[][] add2 = {{1,2,3},{4,5,6}};
 		double[][] add3 = {{4,1,0},{4,-4,3},{2,-8, 1}};
 		Matrice matAdd1 = new Matrice(add1);
 		Matrice matAdd2 = new Matrice(add2);
 		Matrice matAdd3 = new Matrice(add3);
 
 		// 1ere operation
 		System.out.println("1ere operation\n");
 		matAdd1.afficherMatrice();
 		matAdd2.afficherMatrice();
 		System.out.println("L'addition de ces deux matrices est �gal �: \n");
 		Matrice matAdd4 = matAdd1.additionner(matAdd2); // Err: pas m�me format
 		afficherEspace();
 		
 		// 2eme operation
 		System.out.println("2ere operation\n");
 		matAdd1.afficherMatrice();
 		matAdd3.afficherMatrice();
 		System.out.println("L'addition de ces deux matrices est �gal �: \n");
 		Matrice matAdd5 = matAdd1.additionner(matAdd3); // Succ�s
 		matAdd5.afficherMatrice(); // Affiche le contenu de la matrice
 		afficherEspace();
 		
 		// --------------------------------------------------------------------
 		// Faire produit scalaire
 		// --------------------------------------------------------------------
 		afficherTitre("Faire produit scalaire");
 		double[][] prodScal = {{1,2,3},{1,2,3}};
 		double scalaire = 2;
 		Matrice matScal1 = new Matrice(prodScal);
 		Matrice matScal2 = matScal1.faireProduitScalaire(scalaire);
 
 		matScal1.afficherMatrice();
 		System.out.println("Scalaire: " + scalaire + "\n");
 		System.out.println("La multiplication de la matrice ci-haute avec le scalaire donne ceci: \n");
 		matScal2.afficherMatrice();
 		afficherEspace();
 	
 		// --------------------------------------------------------------------
 		// Faire produit matricielle
 		// --------------------------------------------------------------------
 		afficherTitre("Faire le produit matricielle");
 		double[][] prodMatC = {{7,2,3},{-1,0,-4}};
 		double[][] prodMatD = {{2,4},{-5,1},{-1, 0}};
 		double[][] prodMatE = {{8,6},{-6,-7}};
 		Matrice matProdC  = new Matrice(prodMatC);
 		Matrice matProdD  = new Matrice(prodMatD);
 		Matrice matProdE  = new Matrice(prodMatE);
 
 		// 1ere operation
 		System.out.println("1ere operation");
 		matProdC.afficherMatrice();
 		matProdD.afficherMatrice();
 		System.out.println("Le produit de ces deux matrices est �gal � :\n");
 		Matrice matProdCD = matProdC.faireProduitMatriciel(matProdD);
 		matProdCD.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2ere operation
 		System.out.println("2eme operation");
 		matProdD.afficherMatrice();
 		matProdE.afficherMatrice();
 		System.out.println("Le produit de ces deux matrices est �gal � :\n");
 		Matrice matProdDE = matProdD.faireProduitMatriciel(matProdE);
 		matProdDE.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// 3ere operation
 		System.out.println("3eme operation");
 		matProdE.afficherMatrice();
 		matProdD.afficherMatrice();
 		System.out.println("Le produit de ces deux matrices est �gal � :\n");
 		Matrice matProdED = matProdE.faireProduitMatriciel(matProdD);
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// Faire la trace
 		// --------------------------------------------------------------------
 		afficherTitre("Faire la trace");
 		double[][] traceA = {{1,2,3},{4,5,6}};
 		double[][] traceB = {{1,2,3},{4,5,6},{7,8,9}};
 		
 		Matrice matTraceA = new Matrice(traceA);
 		Matrice matTraceB = new Matrice(traceB);
 		
 		// 1ere operation
 		System.out.println("1ere operation");
 		matTraceA.afficherMatrice();
 		System.out.println("Le trace de cette matrice est :\n");
 		matTraceA.getTrace();
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2eme opeation
 		System.out.println("2eme operation");
 		matTraceB.afficherMatrice();
 		System.out.println("Le trace de cette matrice est : \n");
 		System.out.println(matTraceB.getTrace());
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// Calculer d�terminant
 		// --------------------------------------------------------------------
 		afficherTitre("Calculer d�terminant");
 		double[][] detA = {{9}};
 		double[][] detB = {{5,3,4},{8,1,5},{3,5,6}};
 		double[][] detC = {{4,2,8,3},{5,1,7,5},{8,0,8,5},{3,2,3,8}};
 		
 		Matrice matDetA = new Matrice(detA); 
 		Matrice matDetB = new Matrice(detB);
 		Matrice matDetC = new Matrice(detC);
 		
 		// 1ere operation
 		matDetA.afficherMatrice();
 		System.out.println("Le determinant de cette matrice est : \n");
 		System.out.println(matDetA.getDeterminant());
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2eme operation
 		matDetB.afficherMatrice();
 		System.out.println("Le determinant de cette matrice est : \n");
 		System.out.println(matDetB.getDeterminant());
 		System.out.println("-----------------------------------------------\n");
 		
 		// 3eme operation
 		matDetC.afficherMatrice();
 		System.out.println("Le determinant de cette matrice est : \n");
 		System.out.println(matDetC.getDeterminant());
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// Faire la transpos�e
 		// --------------------------------------------------------------------
 		afficherTitre("Faire la transpos�e");
 		double[][] transA = {{1,2,4}, {2,3,2}};
 		double[][] transB = {{2,3,5},{7,8,9},{1,3,5}};
 		double[][] transC = {{24,-11,2},{16,-10,-8},{22,-23,13}};
 		
 		Matrice matTransA = new Matrice(transA); 
 		Matrice matTransB = new Matrice(transB);
 		Matrice matTransC = new Matrice(transC);
 		
 		// 1ere operation
 		System.out.println("1ere operation");
 		matTransA.afficherMatrice();
 		System.out.println("Le transpos�e de cette matrice est :\n");
 		matTransA.getTransposee();
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2eme operation
 		System.out.println("2eme operation");
 		matTransB.afficherMatrice();
 		System.out.println("Le transpos�e de cette matrice est :\n");
 		Matrice matTransD = matTransB.getTransposee();
 		matTransD.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// 3eme operation 
 		System.out.println("3eme operation");
 		matTransC.afficherMatrice();
 		System.out.println("Le transpos�e de cette matrice est :\n");
 		Matrice matTransE = matTransC.getTransposee();
 		matTransE.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		
 		// --------------------------------------------------------------------
 		// Faire la CoMatrice
 		// --------------------------------------------------------------------
 		afficherTitre("Faire la CoMatrice");
 		double[][] comA = {{1,2,-1},{-2,1,1},{0,3,-3}};
 		double[][] comB = {{1,2,3},{0,1,2},{-1,-4,-1}};
 		
 		Matrice matCom = null;
 		Matrice matComA = new Matrice(comA);
 		Matrice matComB = new Matrice(comB);
 		
 		// 1ere operation
 		System.out.println("1ere operation");
 		matComA.afficherMatrice();
 		System.out.println("La CoMatrice de cette matrice est :\n");
 		matCom = matComA.getCoMatrice();
 		matCom.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2eme operation
 		System.out.println("2eme operation");
 		matComB.afficherMatrice();
 		System.out.println("La CoMatrice de cette matrice est :\n");
 		matCom = matComB.getCoMatrice();
 		matCom.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		
 		// --------------------------------------------------------------------
 		// Faire la matrice inverse
 		// --------------------------------------------------------------------
 		afficherTitre("Faire la matrice inverse");
 		double[][] invB = {{4,5},{1,2}};
 		double[][] invC = {{1,2,3},{0,1,2},{-1,-4,-1}};
 		double[][] invD = {{-3,0,0},{0,2,0},{0,0,-4}};
 		
 		Matrice matInv = null;
 		
 		Matrice matInvB = new Matrice(invB);
 		Matrice matInvC = new Matrice(invC);
 		Matrice matInvD = new Matrice(invD);
 		
 		// 1ere operation
 		System.out.println("1ere operation");
 		matInvB.afficherMatrice();
 		System.out.println("La matrice inverse de cette matrice est :\n");
 		matInv = matInvB.getMatriceInverse();
 		matInv.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2eme operation
 		System.out.println("2eme operation");
 		matInvC.afficherMatrice();
 		System.out.println("La matrice inverse de cette matrice est :\n");
 		matInv = matInvC.getMatriceInverse();
 		matInv.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// 3eme operation (avec calcul triangulaire)
 		System.out.println("3eme operation");
 		matInvD.afficherMatrice();
 		System.out.println("La matrice inverse de cette matrice est :\n");
 		matInv = matInvD.getMatriceInverse();
 		matInv.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// V�rifier si la matrice carre
 		// --------------------------------------------------------------------
 		afficherTitre("V�rifier matrice carr�e");
 		double[][] carA = {{1,2,3},{4,5,6}};
 		double[][] carB = {{2,3},{4,5}};
 		
 		boolean estCar;
 		Matrice matCarA = new Matrice(carA);
 		Matrice matCarB = new Matrice(carB);
 		
 		// 1ere operation
 		System.out.println("1ere operation");
 		matCarA.afficherMatrice();
 		estCar = matCarA.estCarre();
 		System.out.println("Cette matrice est carr�e?: " + estCar);
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2ere operation
 		System.out.println("2eme operation");
 		matCarB.afficherMatrice();
 		estCar = matCarB.estCarre();
 		System.out.println("Cette matrice est carr�e?: " + estCar);
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// V�rifier si la matrice est triangulaire
 		// --------------------------------------------------------------------
 		afficherTitre("Matrice triangulaire");
 		double[][] triS = {{3,6,9},{0,5,4},{0,0,1}}; // Triangulaire sup�rieur
 		double[][] triI = {{8,0,0},{4,6,0},{1,2,4}}; // Triangulaire inf�rieure
 		double[][] triSS = {{0,6,9},{0,0,4},{0,0,0}}; // Triangulaire sup�rieur stricte
 		double[][] triIS = {{0,0,0},{4,0,0},{1,2,0}}; // Triangulaire inf�rieure stricte
 		
 		boolean valeur   = false;
 		Matrice matTriS  = new Matrice(triS);
 		Matrice matTriI  = new Matrice(triI);
 		Matrice matTriSS = new Matrice(triSS);
 		Matrice matTriIS = new Matrice(triIS);
 	
 		//*** L�gende: N = NIMPORTE  S = SUPERIEUR  I = INFERIEUR
 		
 		// 1ere operation
 		System.out.println("1ere operation");
 		matTriS.afficherMatrice();
 		System.out.println("Cette matrice est: \n");
 		
 		// Triangulaire sup�rieur
 		valeur = matTriS.estTriangulaire(Matrice.TRIANGULAIRE_SUPERIEUR, false); 
 		System.out.println("Triangulaire sup�rieur? : "+valeur);
 		// ----- I
 		valeur = matTriS.estTriangulaire(Matrice.TRIANGULAIRE_INFERIEUR, false); 
 		System.out.println("Triangulaire inf�rieur? : " + valeur);
 		// ----- N
 		valeur = matTriS.estTriangulaire(Matrice.TRIANGULAIRE_NIMPORTE, false); 
 		System.out.println("Triangulaire? : " + valeur);
 		// ----- SS
 		valeur = matTriS.estTriangulaire(Matrice.TRIANGULAIRE_SUPERIEUR, true); 
 		System.out.println("Triangulaire sup�rieur stricte? : " + valeur);
 		// ----- IS
 		valeur = matTriS.estTriangulaire(Matrice.TRIANGULAIRE_INFERIEUR, true); 
 		System.out.println("Triangulaire inf�rieur stricte? : " + valeur);
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2ere operation
 		System.out.println("2eme operation");
 		matTriI.afficherMatrice();
 		System.out.println("Cette matrice est: \n");
 				
 		// Triangulaire inf�rieur
 		valeur = matTriI.estTriangulaire(Matrice.TRIANGULAIRE_SUPERIEUR, false); 
 		System.out.println("Triangulaire sup�rieur? : "+valeur);
 		// ----- I
 		valeur = matTriI.estTriangulaire(Matrice.TRIANGULAIRE_INFERIEUR, false); 
 		System.out.println("Triangulaire inf�rieur? : " + valeur);
 		// ----- N
 		valeur = matTriI.estTriangulaire(Matrice.TRIANGULAIRE_NIMPORTE, false); 
 		System.out.println("Triangulaire? : " + valeur);
 		// ----- SS
 		valeur = matTriI.estTriangulaire(Matrice.TRIANGULAIRE_SUPERIEUR, true); 
 		System.out.println("Triangulaire sup�rieur stricte? : " + valeur);
 		// ----- IS
 		valeur = matTriI.estTriangulaire(Matrice.TRIANGULAIRE_INFERIEUR, true); 
 		System.out.println("Triangulaire inf�rieur stricte? : " + valeur);
 		System.out.println("-----------------------------------------------\n");
 		
 		// 3ere operation
 		System.out.println("3eme operation");
 		matTriSS.afficherMatrice();
 		System.out.println("Cette matrice est: \n");
 				
 		// Triangulaire sup�rieur stricte
 		valeur = matTriSS.estTriangulaire(Matrice.TRIANGULAIRE_SUPERIEUR, false); 
 		System.out.println("Triangulaire sup�rieur? : "+valeur);
 		// ----- I
 		valeur = matTriSS.estTriangulaire(Matrice.TRIANGULAIRE_INFERIEUR, false); 
 		System.out.println("Triangulaire inf�rieur? : " + valeur);
 		// ----- N
 		valeur = matTriSS.estTriangulaire(Matrice.TRIANGULAIRE_NIMPORTE, false); 
 		System.out.println("Triangulaire? : " + valeur);
 		// ----- SS
 		valeur = matTriSS.estTriangulaire(Matrice.TRIANGULAIRE_SUPERIEUR, true); 
 		System.out.println("Triangulaire sup�rieur stricte? : " + valeur);
 		// ----- IS
 		valeur = matTriSS.estTriangulaire(Matrice.TRIANGULAIRE_INFERIEUR, true); 
 		System.out.println("Triangulaire inf�rieur stricte? : " + valeur);
 		System.out.println("-----------------------------------------------\n");
 		
 		// 4ere operation
 		System.out.println("4eme operation");
 		matTriIS.afficherMatrice();
 		System.out.println("Cette matrice est: \n");
 				
 		// Triangulaire inf�rieure stricte
 		valeur = matTriIS.estTriangulaire(Matrice.TRIANGULAIRE_SUPERIEUR, false); 
 		System.out.println("Triangulaire sup�rieur? : "+valeur);
 		// ----- I
 		valeur = matTriIS.estTriangulaire(Matrice.TRIANGULAIRE_INFERIEUR, false); 
 		System.out.println("Triangulaire inf�rieur? : " + valeur);
 		// ----- N
 		valeur = matTriIS.estTriangulaire(Matrice.TRIANGULAIRE_NIMPORTE, false); 
 		System.out.println("Triangulaire? : " + valeur);
 		// ----- SS
 		valeur = matTriIS.estTriangulaire(Matrice.TRIANGULAIRE_SUPERIEUR, true); 
 		System.out.println("Triangulaire sup�rieur stricte? : " + valeur);
 		// ----- IS
 		valeur = matTriIS.estTriangulaire(Matrice.TRIANGULAIRE_INFERIEUR, true); 
 		System.out.println("Triangulaire inf�rieur stricte? : " + valeur);
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// V�rifier si la matrice est r�guli�re
 		// --------------------------------------------------------------------
 		afficherTitre("Matrice r�guli�re");
 		double[][] regA = {{1,3,0},{4,-1,2},{-2,-6,0}};
 		double[][] regB = {{1,2},{3,4}};
 		
 		boolean estReguliere;
 		Matrice matRegA = new Matrice(regA);
 		Matrice matRegB = new Matrice(regB);
 		
 		// 1ere operation
 		System.out.println("1ere operation");
 		matRegA.afficherMatrice();
 		estReguliere = matRegA.estReguliere();
 		System.out.println("Cette matrice est r�guli�re? : " + estReguliere);
 		System.out.println("-----------------------------------------------\n");
 		
 		// 2eme operation
 		System.out.println("2eme operation");
 		matRegB.afficherMatrice();
 		estReguliere = matRegB.estReguliere();
 		System.out.println("Cette matrice est r�guli�re? : " + estReguliere);
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// ************* PARTIE 2 *********************************************
 		// --------------------------------------------------------------------
 		double[][] sysA = {{2,1,3},{1,-2,1},{1,1,-2}};
 		double[][] sysB = {{6},{2},{1}};
 		double[][] sysC = {{4,-1,0},{-1,4,-1},{0,-1,4}};
 		double[][] sysD = {{100},{100},{100}};
 		
 		Matrice mat = null;
 		Matrice matA = new Matrice(sysA);
 		Matrice matB = new Matrice(sysB);
 		Matrice matC = new Matrice(sysC);
 		Matrice matD = new Matrice(sysD);
 		
 		Systeme sys = new Systeme(matA, matB);
 		Systeme sys2 = new Systeme(matC, matD);
 		
 		// --------------------------------------------------------------------
 		// Cramer
 		// --------------------------------------------------------------------
 		
 		afficherTitre("Cramer");
 		mat = sys.trouverXParCramer();
 		System.out.println("Syst�me d'�quation sous forme de matrice: \n");
 		matA.afficherMatrice();
 		System.out.println("Matrice d'�galit� : \n");
 		matB.afficherMatrice();
 		System.out.println("R�sultat: \n");
 		mat.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// Inversion matricielle
 		// --------------------------------------------------------------------
 		afficherTitre("Inversion matricielle");
 		mat = sys.trouverXParInversionMatricielle();
 		System.out.println("Syst�me d'�quation sous forme de matrice: \n");
 		matA.afficherMatrice();
 		System.out.println("Matrice d'�galit� : \n");
 		matB.afficherMatrice();
 		System.out.println("R�sultat: \n");
 		mat.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		// --------------------------------------------------------------------
 		// Jacobi
 		// --------------------------------------------------------------------
 		afficherTitre("Jacobi");
 		System.out.println("Syst�me d'�quation sous forme de matrice: \n");
 		matA.afficherMatrice();
 		System.out.println("Matrice d'�galit� : \n");
 		matB.afficherMatrice();
 		System.out.println("R�sultat: \n");
		mat = sys2.trouverXParJacobi(0.1);
		mat.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 		
 		mat = sys2.trouverXParJacobi(1);
 		System.out.println("Syst�me d'�quation sous forme de matrice: \n");
 		matC.afficherMatrice();
 		System.out.println("Matrice d'�galit� : \n");
 		matD.afficherMatrice();
 		System.out.println("R�sultat: \n");
 		mat.afficherMatrice();
 		System.out.println("-----------------------------------------------\n");
 	}
 	
 	public static void afficherTitre(String _str) {
 		System.out.println(_str);
 		System.out.println("-----------------------------------------------------------------------");
 	}
 	
 	public static void afficherEspace() {
 		System.out.println("\n");
 	}
 
 }
