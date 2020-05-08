 package model;
 
 import junit.framework.TestCase;
 
 public class TestDetermineFinalDecisionOrder extends TestCase {
 
 	public void testDetermineFinalOrder() {
 		DecisionLinesEvent dle = new DecisionLinesEvent();
 		Line newChoice;
 		dle.setQuestion("Why is there air?");
 		dle.setNumChoices(3);
 		dle.setRounds(0);
 		dle.setMode("asynch");
 		dle.setType("open");
 	
 		// Valid new position and choice
 		newChoice = new Line("Apple", 0);
 		dle.setChoice(newChoice);
 		
 		// Valid new position  and valid choice (Orange should not have been added above)
 		newChoice = new Line("Orange", 1);
         dle.setChoice(newChoice); 
 
         // Valid new position  and Invalid choice (should have already been added above)
 		newChoice = new Line("Pineapple", 2);
 		dle.setChoice(newChoice); 
 		
 		Line choice = dle.getChoice(0);
 		choice = dle.getChoice(1); 
 		choice = dle.getChoice(2);
 		
 		Edge e = new Edge(0, 1, 8);
 		dle.getEdges().add(e);
		e = new Edge(1, 2, 20);
 		dle.getEdges().add(e);
 		
 		dle.determineFinalOrder();
 		for(int i = 0; i < dle.getNumChoices(); i++) {
 	    	System.out.println(dle.getChoiceOrderPosition(i));
 	    }
 	}
 }
