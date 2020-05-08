 package data;
 
 import petrinet.Petrinet;
 import petrinet.Place;
 import petrinet.Transition;
 import transformation.Rule;
 import transformation.TransformationComponent;
 
 public class Rule2Data {
 	
 	public static int getIdOfMatchedPlace(){
 		return MorphismData.getIdMatchesInRule2();
 	}
 	
 	public static Petrinet getnPetrinet() {
 		return nPetrinet;
 	}
 
 	public static Rule getRule() {
 		return rule;
 	}
 	
 	private static Petrinet nPetrinet = MorphismData.getPetrinetIsomorphismPlacesTo();
 
 	private static Rule rule = TransformationComponent.getTransformation().createRule();
 	
 	static {
 		addSubnetToPetrinet(rule.getL());
		TransformationComponent.getTransformation().setMark(rule, rule.getL().getAllPlaces().iterator().next().getId(), 4);
 	}
 	
 	private static void addSubnetToPetrinet(Petrinet petrinet){
 		Place place = petrinet.createPlace("P1");
 		for (int i = 0; i < 3; i++) {
 			Transition transition = petrinet.createTransition("A");
 			petrinet.createArc("", place, transition);
 		}
 		for (int i = 0; i < 2; i++) {
 			Transition transition = petrinet.createTransition("A");
 			petrinet.createArc("", transition, place);
 		}
 	}
 
 }
