 
 public class Main {
 	
 	public static void main(String[] arg){
 		///TP 4
 		//On crée les variable
		Formule x1 = new Formule(0, 1); Formule x2 = new Formule(0, 2); Formule x3 = new Formule(0, 3); Formule x4 = new Formule(0, 4); Formule x5 = new Formule(0, 5); 
 		
 		//Création des formules du TP
 		Formule f1 = new Formule(2, x3,
 									new Formule(2, new Formule(3, x1, new Formule(1, x3)),
 													new Formule(3, x2,
 																	new Formule(3, x3,
 																					new Formule(1, x1)))));
 		
		//
 		Formule f2 = new Formule(2, x3, new Formule(1, x3));
		
		//(x5 ou non x3 ou x1 ou non x2) et (non x5 ou non x1 ou x2 ou non x2)
		Formule f3 = new Formule(2, new Formule(3, x5,
												new Formule(3, new Formule(1, x3)
																,new Formule(3, x1,
																				new Formule(1, x2))))
								  ,new Formule(3, new Formule(1, x5),
										  		new Formule(3, new Formule(1, x1),
										  						new Formule(3, x2, new Formule(1, x2)))));

		
 	}
 	
 	public void ancienTest(){
 		//Formule non non X
 				Formule pasBonne1 = new Formule(1, 
 												new Formule(1, 
 														new Formule(0, 0)));
 				//Formule non(X et Y)
 				Formule pasBonne2 = new Formule(1,
 												new Formule(2,
 														new Formule(0, 0)
 														,new Formule(0, 1)));
 				//Pas Bonne 3
 				Formule pasBonne3 = new Formule(3, 
 													new Formule(2, new Formule(0, 1), new Formule(0, 2)),
 													new Formule(2, new Formule(0, 3), new Formule(0, 4))
 												);
 				//Mise en NNF :
 						Formule trozarb = new Formule(2, new Formule(1, new Formule(0, 1)),
 														new Formule(1, new Formule(3, new Formule(0, 1), new Formule(0,2))));
 						
 				/*Formule x1 = new Formule(0, 1);
 				Formule x2 = new Formule(0, 2);
 				Formule x3 = new Formule(0, 3);
 				
 				Formule form = new Formule(3, new Formule(2, x1, x2), new Formule(1, x3));
 				
 				boolean[] b = Formule.affectation(3);
 				System.out.println("La formule est elle satisfiable ? " + form.isSatisfiable());
 				
 				Formule formule2 = new Formule(2, new Formule (0, 0), new Formule(1,new Formule(0,0)));
 				System.out.println("La formule est : " + formule2.toString() + " Est elle satisfiable ? " + formule2.isSatisfiable() + " Est elle en NFG " + formule2.isNnf());
 				*/
 				
 				
 				//System.out.println("La formule est : " + pasBonne1.toString() + " Est elle en NFG " + pasBonne1.isNnf() + "Appliquons lui une regle : " + pasBonne1.miseEnNnf().toString());
 				
 				
 				//System.out.println("La formule est : " + pasBonne2.toString() + " Est elle en NFG " + pasBonne2.isNnf() + "Appliquons lui une regle : " + pasBonne2.miseEnNnf().toString());
 				
 				
 				//System.out.println("La Formule est : "+trozarb.toString()+"\n Est elle en NNF : " + trozarb.isNnf() +"\n Sa forme en NNF est : "+ trozarb.miseEnNnf().toString());
 				
 				
 				//Mise en CNF :
 				//pasBonne1
 				//System.out.println("La formule est : " + pasBonne1.toString() + " Est elle en CNF " + pasBonne1.isCnf() + "Appliquons lui une regle : " + pasBonne1.miseEnNnf().miseEnCnf().toString());
 				//Pas Bonne 2
 				//System.out.println("La formule est : " + pasBonne2.toString() + " Est elle en CNF " + pasBonne2.isCnf() + "Appliquons lui une regle : " + pasBonne2.miseEnNnf().miseEnCnf().toString());
 
 						//System.out.println("La formule est : " + pasBonne3.toString() + " Est elle en CNF : " + pasBonne3.isCnf() + " Appliquons lui une regle : " + pasBonne3Corrigee.toString());
 						
 				System.out.println(pasBonne3.miseEnNnf().miseEnCnf().toString());
 				Formule pasBonne3Corrigee = pasBonne3.miseEnNnf().miseEnCnf();
 				System.out.println(pasBonne3.miseEnNnf().miseEnCnf().isCnf());
 				
 				System.out.println(pasBonne3Corrigee.toDIMACS());
 				pasBonne3Corrigee.toDimacsTxt();
 	}
 }
