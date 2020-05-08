 import java.util.Arrays;
 import java.util.ArrayList;
 
 public class fuzzySetsAPI{
     public static void main(String[] args){
 		// element a = new element(-1, 0);
 		// element b = new element(-0.99, 0.01);
 		// element c = new element(0, 1);
 		// element d = new element(1.99, 0.005);
 		// element e = new element(2, 0);
 
 		element a = new element(1, 0);
 		element b = new element(1.01, 0.01);
 		element c = new element(2, 1);
 		element d = new element(2.99, 0.01);
 		element e = new element(3, 0);
 		System.out.println("1. Initialisation des points");
 		System.out.println(a);
 		System.out.println(b);
 		System.out.println(c);
 		System.out.println("***\n");
 
 		System.out.println("2. Création d'un ensemble");
 		ArrayList<element> elementsA = new ArrayList<element>(Arrays.asList(a,b,c,d,e));
 		double min=-1.5, max=3;
 		set SetA = new set(elementsA, min, max, "SetA");
 		//System.out.println("Taille: "+ SetA.length());
 
 		System.out.println(SetA);
 		//System.out.println("Nom: "+ SetA.name);
 		System.out.println("***\n");
 
 		System.out.println("3. Valeur aux points :");
 		System.out.println("0   :"+SetA.valueAt(0));
		System.out.println("1 :"+SetA.valueAt(0.5));
		System.out.println("2   :"+SetA.valueAt(1));
 		System.out.println("***\n");
 
 		System.out.println("4. Complémentaire :");
		//System.out.println(set.complementaire(SetA));
 		System.out.println("***\n");
 
 		System.out.println("4. Fonctions - carré");
 		//IMapping f = new function_identity();
 		IMapping f = new function_square();
 		System.out.println(set.apply(SetA, f, 0.1));
 		System.out.println("***\n");
 
 		//data d = new data(); 
 		/** Use case
 		    data = new data
 		    ensemble A;
 		    ensemble B = A.complementaire()
 		*/
 
     }
 }
