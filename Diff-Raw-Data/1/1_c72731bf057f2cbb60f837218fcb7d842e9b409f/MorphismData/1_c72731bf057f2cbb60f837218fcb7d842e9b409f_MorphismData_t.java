 package data;
 
 import java.util.HashSet;
 import java.util.Set;
 
 
 import petrinet.Arc;
 import petrinet.Petrinet;
 import petrinet.IRenew;
 import petrinet.PetrinetComponent;
 import petrinet.Place;
 import petrinet.RenewCount;
 import petrinet.RenewId;
 import petrinet.Transition;
 
 
 /**
  * This Class contains the test data for morphism test. 
  * 
  */
 public class MorphismData {
 	
 
 	public static int getIdMatchesInRule2(){
 		return idOfThird;
 	}
 	
 	public static int getIdFromTransitions() {
 		return idFromTransitions;
 	}
 
 	public static int getIdFromPlaces() {
 		return idFromPlaces;
 	}
 
 	public static int getIdMatchedTransition() {
 		return idMatchedTransition;
 	}
 
 	public static Set<Integer> getIdsMatchedPlaces() {
 		return idsMatchedPlaces;
 	}
 	
 	public static Set<Integer> getIdsOfPlaceAndArcsOfThirdPlace() {
 		return idsOfPlaceAndArcsOfThirdPlace;
 	}
 	
 	public static Set<Integer> getIdsOfTransitionPreAndArcsOfThirdPlace(){
 		return idsOfTransitionPreAndArcsOfThirdPlace;
 	}
 	
 	public static int getIdPreTransiotionOfThird(){
 		return idPreTransiotionOfThird;
 	}
 	
 	public static int getIdPostTransiotionOfThird(){
 		return idPostTransiotionOfThird;
 	}
 	
 	public static Set<Integer> getIdsOfTransitionPostAndArcsOfThirdPlace(){
 		return idsOfTransitionPostAndArcsOfThirdPlace;
 	}
 	
 	public static int getIdOfDeleteArc(){
 		return idOfDeleteArc;
 	}
 
 	private static int idFromTransitions;
 
 	private static int idFromPlaces;
 	
 	private static int idMatchedTransition;
 	
 	private static Set<Integer> idsMatchedPlaces = new HashSet<Integer>();
 	
 	private static int idOfThird;
 	
 	private static int idPreTransiotionOfThird;
 	
 	private static int idPostTransiotionOfThird;
 	
 	private static int idOfDeleteArc;
 	
 	private static Set<Integer> idsOfPlaceAndArcsOfThirdPlace = new HashSet<Integer>();
 	
 	private static Set<Integer> idsOfTransitionPreAndArcsOfThirdPlace = new HashSet<Integer>();
 	
 	private static Set<Integer> idsOfTransitionPostAndArcsOfThirdPlace = new HashSet<Integer>();
 	
 	private MorphismData(){}
 	
 	/**
 	 * 
 	 * @return the "from" Petrinet that is specified in... 
  	 */
 	public static Petrinet getPetrinetIsomorphismPlacesFrom(){
 		Petrinet result = PetrinetComponent.getPetrinet().createPetrinet();
 		IRenew renewId = new RenewId();
 		
 		Place p1 = result.createPlace("P1");
 		
 		idFromPlaces = p1.getId();
 		
 		Transition t1 = result.createTransition("A", renewId);
 		Transition t2 = result.createTransition("A", renewId);
 		Transition t3 = result.createTransition("A", renewId);
 		Transition t4 = result.createTransition("A", renewId);
 		Transition t5 = result.createTransition("A", renewId);
 		
 		// pre
 		result.createArc("", t1, p1);
 		result.createArc("", t2, p1);
 		
 		//post
 		result.createArc("", p1, t3);
 		result.createArc("", p1, t4);
 		result.createArc("", p1, t5);
 		
 		//mark
 		p1.setMark(2);
 		
 		return result;
 	}
 	
 	
 	/**
 	 * 
 	 * @return the "to" Petrinet that is specified in... 
  	 */
 	public static Petrinet getPetrinetIsomorphismPlacesTo(){
 		idsOfTransitionPostAndArcsOfThirdPlace = new HashSet<Integer>();
 		idsOfTransitionPreAndArcsOfThirdPlace = new HashSet<Integer>();
		idsMatchedPlaces = new HashSet<Integer>();
 		
 		// The matching subnet P1 and T1...
 		Petrinet result = PetrinetComponent.getPetrinet().createPetrinet();
 		IRenew renewId = new RenewId();
 		
 		Place p1 = result.createPlace("P1");
 		
 		idsMatchedPlaces.add(p1.getId());
 		
 		Transition t11 = result.createTransition("A", renewId);
 		Transition t12 = result.createTransition("A", renewId);
 		Transition t13 = result.createTransition("A", renewId);
 		Transition t14 = result.createTransition("A", renewId);
 		Transition t15 = result.createTransition("A", renewId);
 		
 		// pre
 		result.createArc("", t11, p1);
 		result.createArc("", t12, p1);
 		
 		//post
 		result.createArc("", p1, t13);
 		result.createArc("", p1, t14);
 		result.createArc("", p1, t15);
 		
 		//mark
 		p1.setMark(2);
 		
 		
 		
 		// The not matching subnet mark not enough  
 		// with following int 2
 		Place p2 = result.createPlace("P1");
 				
 		Transition t21 = result.createTransition("A", renewId);
 		Transition t22 = result.createTransition("A", renewId);
 		Transition t23 = result.createTransition("A", renewId);
 		Transition t24 = result.createTransition("A", renewId);
 		Transition t25 = result.createTransition("A", renewId);
 				
 		// pre
 		result.createArc("", t21, p2);
 		result.createArc("", t22, p2);
 				
 		//post
 		result.createArc("", p2, t23);
 		result.createArc("", p2, t24);
 		result.createArc("", p2, t25);
 				
 		//mark
 		p2.setMark(1);
 		
 		
 		// The matching subnet mark is 1 more  
 		// with following int 3
 		Place p3 = result.createPlace("P1");
 		idOfThird = p3.getId();
 		idsMatchedPlaces.add(idOfThird);
 		idsOfPlaceAndArcsOfThirdPlace.add(idOfThird);
 
 				
 		Transition t31 = result.createTransition("A", renewId);
 		idPreTransiotionOfThird = t31.getId();
 		idsOfTransitionPreAndArcsOfThirdPlace.add(idPreTransiotionOfThird);
 		Transition t32 = result.createTransition("A", renewId);
 		Transition t33 = result.createTransition("A", renewId);
 		idPostTransiotionOfThird = t33.getId();
 		idsOfTransitionPostAndArcsOfThirdPlace.add(idPostTransiotionOfThird);
 		Transition t34 = result.createTransition("A", renewId);
 		Transition t35 = result.createTransition("A", renewId);
 		
 		
 		
 				
 		// pre
 		Arc arcPlace31 = result.createArc("", t31, p3);
 		idOfDeleteArc = arcPlace31.getId();
 		idsOfPlaceAndArcsOfThirdPlace.add(idOfDeleteArc);
 		idsOfTransitionPreAndArcsOfThirdPlace.add(idOfDeleteArc);
 		Arc arcPlace32 = result.createArc("", t32, p3);
 		idsOfPlaceAndArcsOfThirdPlace.add(arcPlace32.getId());
 				
 		//post
 		Arc arcPlace33 = result.createArc("", p3, t33);
 		idsOfPlaceAndArcsOfThirdPlace.add(arcPlace33.getId());
 		idsOfTransitionPostAndArcsOfThirdPlace.add(arcPlace33.getId());
 		Arc arcPlace34 = result.createArc("", p3, t34);
 		idsOfPlaceAndArcsOfThirdPlace.add(arcPlace34.getId());
 		Arc arcPlace35 = result.createArc("", p3, t35);
 		idsOfPlaceAndArcsOfThirdPlace.add(arcPlace35.getId());
 				
 		//mark
 		p3.setMark(3);
 		
 		
 		// The not matching subnet pre is not enough  
 		// with following int 4
 		Place p4 = result.createPlace("P1");
 						
 		Transition t41 = result.createTransition("A", renewId);
 		Transition t43 = result.createTransition("A", renewId);
 		Transition t44 = result.createTransition("A", renewId);
 		Transition t45 = result.createTransition("A", renewId);
 						
 		// pre
 		result.createArc("", t41, p4);
 						
 		//post
 		result.createArc("", p4, t43);
 		result.createArc("", p4, t44);
 		result.createArc("", p4, t45);
 						
 		//mark
 		p4.setMark(2);
 		
 		
 		// The not matching subnet post is not enough  
 		// with following int 5
 		Place p5 = result.createPlace("P1");
 				
 		Transition t51 = result.createTransition("A", renewId);
 		Transition t52 = result.createTransition("A", renewId);
 		Transition t54 = result.createTransition("A", renewId);
 		Transition t55 = result.createTransition("A", renewId);
 				
 		// pre
 		result.createArc("", t51, p5);
 		result.createArc("", t52, p5);
 				
 		//post
 		result.createArc("", p5, t54);
 		result.createArc("", p5, t55);
 				
 		//mark
 		p5.setMark(2);
 		
 		
 		// The matching subnet pre is to many  
 		// with following int 6
 		Place p6 = result.createPlace("P1");
 		idsMatchedPlaces.add(p6.getId());
 
 						
 		Transition t61 = result.createTransition("A", renewId);
 		Transition t62 = result.createTransition("A", renewId);
 		Transition t63 = result.createTransition("A", renewId);
 		Transition t64 = result.createTransition("A", renewId);
 		Transition t65 = result.createTransition("A", renewId);
 		Transition t66 = result.createTransition("A", renewId);
 						
 		// pre
 		result.createArc("", t61, p6);
 		result.createArc("", t62, p6);
 		result.createArc("", t63, p6);
 						
 		//post
 		result.createArc("", p6, t64);
 		result.createArc("", p6, t65);
 		result.createArc("", p6, t66);
 						
 		//mark
 		p6.setMark(2);
 		
 		
 		// The matching subnet post is to many  
 		// with following int 7
 		Place p7 = result.createPlace("P1");
 		idsMatchedPlaces.add(p7.getId());
 
 								
 		Transition t71 = result.createTransition("A", renewId);
 		Transition t72 = result.createTransition("A", renewId);
 		Transition t73 = result.createTransition("A", renewId);
 		Transition t74 = result.createTransition("A", renewId);
 		Transition t75 = result.createTransition("A", renewId);
 		Transition t76 = result.createTransition("A", renewId);
 								
 		// pre
 		result.createArc("", t71, p7);
 		result.createArc("", t72, p7);
 								
 		//post
 		result.createArc("", p7, t73);
 		result.createArc("", p7, t74);
 		result.createArc("", p7, t75);
 		result.createArc("", p7, t76);
 								
 		//mark
 		p7.setMark(2);
 		
 		
 		return result;
 	}
 	
 	
 	/**
 	 * Returns the "from" petrinet specified in '../additional/images/Isomorphism_transitions.png'
 	 */
 	public static Petrinet getPetrinetIsomorphismTransitionsFrom(){
 		Petrinet result = PetrinetComponent.getPetrinet().createPetrinet();
 		
 		IRenew rnwId = new RenewId();
 		
 		idFromTransitions = addSubnetToPetrinetLikeInMorphismTransition(result, "P1".split(" "), "P2 P3".split(" "), "A", rnwId, "1");
 		
 		return result;
 	}
 
 	
 	/**
 	 * Returns the "to" petrinet specified in '../additional/images/Isomorphism_transitions.png'
 	 */
 	public static Petrinet getPetrinetIsomorphismTransitionsTo(){
 		Petrinet result = PetrinetComponent.getPetrinet().createPetrinet();
 		
 		IRenew rnwId = new RenewId();
 		IRenew rnwCount = new RenewCount();
 		
 		String[] pre1 = {"P1"};
 		String[] pre2 = {"P1"};
 		String[] pre3 = {"P1"};
 		String[] pre4 = {"P1"};
 		String[] pre5 = {"P1"};
 		String[] pre6 = {"P1", "P2"};
 		String[] pre7 = {"P1"};
 		String[] pre8 = {};
 		
 		
 		String[] post1 = {"P2", "P3"};
 		String[] post2 = {"P2", "P3"};
 		String[] post3 = {"P2", "P3"};
 		String[] post4 = {"P2", "P3"};
 		String[] post5 = {"P2", "P3", "P4"};
 		String[] post6 = {"P2", "P3"};
 		String[] post7 = {"P2"};
 		String[] post8 = {"P2", "P3"};
 		
 		String[] name = {"A", "A", "A", "B", "A", "A", "A", "A"};
 		String[] tlb =  {"1", "1", "2", "1", "1", "1", "1", "1"};
 		
 		idMatchedTransition = addSubnetToPetrinetLikeInMorphismTransition(result,pre1,post1,name[0],rnwId,tlb[0]);
 		addSubnetToPetrinetLikeInMorphismTransition(result,pre2,post2,name[1],rnwCount,tlb[1]);
 		addSubnetToPetrinetLikeInMorphismTransition(result,pre3,post3,name[2],rnwId,tlb[2]);
 		addSubnetToPetrinetLikeInMorphismTransition(result,pre4,post4,name[3],rnwId,tlb[3]);
 		addSubnetToPetrinetLikeInMorphismTransition(result,pre5,post5,name[4],rnwId,tlb[4]);
 		addSubnetToPetrinetLikeInMorphismTransition(result,pre6,post6,name[5],rnwId,tlb[5]);
 		addSubnetToPetrinetLikeInMorphismTransition(result,pre7,post7,name[6],rnwId,tlb[6]);
 		addSubnetToPetrinetLikeInMorphismTransition(result,pre8,post8,name[7],rnwId,tlb[7]);
 		return result;
 	}
 	
 	
 	private static int addSubnetToPetrinetLikeInMorphismTransition(Petrinet petrinet, 
 			String[] pre, 
 			String[] post, 
 			String name, 
 			IRenew renew, 
 			String tlb){
 		Transition transition = petrinet.createTransition(name,renew);
 		transition.setTlb(tlb);
 		for (String string: pre) {
 			Place place = petrinet.createPlace(string);
 			petrinet.createArc("", place, transition);
 		}
 		for (String string : post) {
 			Place place = petrinet.createPlace(string);
 			petrinet.createArc("", transition, place);
 		}
 		return transition.getId();
 	}
 	
 	
 //	public static void main(String[] args){
 //		Petrinet from = MorphismData.getPetrinetIsomorphismPlacesFrom();
 //		System.out.println(from.toString());
 		
 //		Petrinet to = MorphismData.getPetrinetIsomorphismPlacesTo();
 //		System.out.println(to.toString());
 		
 //	}
 	
 
 }
