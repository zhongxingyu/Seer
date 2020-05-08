 package fr.piratekado.aqua;
 import java.util.Vector;
 
 public class AlgoRecherche2 {
 // Principe : recherche itrative et non pas rcursive 
 // Classement de chaque niveau en fonction 
 // piste  creuser : recherche par rapport aux densits de bulles 
 // 
 	
 	
 	Grille etape0;
 	int action [] = new int[2];
 	Vector<Action> recherche ;
 	
 	public AlgoRecherche2(Grille info) {
 		// TODO Auto-generated constructor stub
 		this.etape0 = info;
 	}
 	
 	public Vector<Action> recherche (int coup) {
 		// calcul pour toutes les cases le rsultat 
 		
 		Action actionTop = null;
 		Vector<Action> lstActionTop = null;
 		
 		
 		for (int i=0;i<6;i++) {
 			for (int j =0; j<6; j++) {
 				Grille test = (Grille)etape0.clone();
 				test.action(i, j);
 				Action action = new Action();
 				action.x = i;
 				action.y = j;
 				action.nbBulle = test.getNbBulle();
 				if (action.nbBulle == 0) {
 					// on a trouv la bonne solution
 					Vector<Action> lstAction = new Vector<Action>();
 					lstAction.add(action);
 					return lstAction;
 					
 				}
 				if (coup == 0) {
 					// System.out.println("algo" + i + " " + j + " " + test.getNbBulle());
 					// Recherche de la meilheure solution
 					if ( actionTop == null ||
 						 action.nbBulle <= actionTop.nbBulle) {
 						try {
 							actionTop = (Action)action.clone();
 						} catch (CloneNotSupportedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					
 				} else {
 					// Recherche rcurssive
 					AlgoRecherche recussif = new AlgoRecherche(test);
 					Vector<Action> lstActionR = new Vector<Action>();
 					lstActionR.add(action);
 					lstActionR.addAll(recussif.recherche(coup - 1 ));
 					/* Recherche si la solution est la meilheure */
 					if (/* lstActionTop n'est pas initialis */
 						lstActionTop == null ||
 						/* solution avec moins de coups qui dtruit toutes les bulles */
 						lstActionR.lastElement().nbBulle == 0  &&
 						lstActionTop.lastElement().nbBulle == 0 && 
 						lstActionTop.size() >= lstActionR.size() ||
 						/* solution qui dtruit le maximum de bulles */
 						lstActionTop.lastElement().nbBulle >= lstActionR.lastElement().nbBulle)
 					 {
 							lstActionTop = lstActionR;
 					 }
 				}
 				
 			}
 		}
 		
 		if (coup == 0) {
 			Vector<Action> lstAction = new Vector<Action>();
 			lstAction.add(actionTop);
 			return lstAction;
 		} else {
 			return lstActionTop;
 		}
 		 
 	}
 	public int[] getAction() {
 		return action;
 	}
 }
