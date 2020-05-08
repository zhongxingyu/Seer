 package world;
 
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import simulateur.Simulation;
 import view.View;
 
 public class Main {
 
 	/**
 	 * main
 	 */
 	public static void main(String args[]) {//ne sert a rien pour le moment, pas d'arguments a donner
		World world = new World (5,5);
 		View view = new View();
 		Simulation sim = new Simulation();
 		//etats avancer et stop
 		State init = new State("init") ;
 		State allerA = new State("goto") ;
 		State ramasser = new State("ramasser") ;
 		State back = new State("back") ;
 		
 		//transition d'un état
 		LinkedList<String> guard = new LinkedList<String>() ; 
 		guard.add("find") ;
 		Transition init_allerA = new Transition(guard,allerA) ;
 		init.addTransition(init_allerA) ;
 		
 		LinkedList<String> guard1 = new LinkedList<String>() ; 
 		guard1.add("check") ;
 		Transition allerA_ramasser = new Transition(guard1,ramasser) ;
 		allerA.addTransition(allerA_ramasser) ;
 		
 		LinkedList<String> guard2 = new LinkedList<String>() ; 
 		guard2.add("ras") ;
 		Transition ramasser_back = new Transition(guard2,back) ;
 		ramasser.addTransition(ramasser_back) ;
 		
 		//creation de l'automate
 		LinkedList<State> states = new LinkedList<State>() ;
 		states.add(init) ;
 		states.add(allerA) ;
 		states.add(ramasser) ;
 		states.add(back) ;
 		Automate a = new Automate(states);
 		
 		/* test de guard!
 		HashMap<String, Elements> H = new HashMap<String, Elements>() ;
 		H.put("find", new Balle(new Point(1,1))) ;
 		System.out.println("etat courant : "+a.getCurrent().getName()) ;
 		a.nextState(H) ;
 		System.out.println("etat final : "+a.getCurrent().getName()) ;
 		System.out.println("Objectif : "+a.getCurrent().getObjectif()) ;
 		*/
 		
 		
 		//creation d1 balle et ajout sur la map
		Balle baballe = new Balle(new Point(4,4)) ;
 		//Balle baballe2 = new Balle(new Point(3,2)) ;
 		
 		
 		
 		//ajout d'un robot et de sont équipe
		Simulation.ajouter(a,new Point(2,2)) ;
		Robot rob = (Robot) World.getMap()[2][2] ;
 		
 		
 		//creation d1 base et ajout sur la map
		Base babase = new Base(new Point(1,1)) ;
 		
 		LinkedList<Base> mesBabases = new LinkedList<Base>() ;
 		mesBabases.add(babase) ;
 		rob.setteam(new Equipe(1,mesBabases)) ;
 		//world.toStringScan(rob) ;
 		//world.toStringMap() ;
 		//et c'est parti!!
 		Simulation.activate(sim);
 		
 	}
 }
