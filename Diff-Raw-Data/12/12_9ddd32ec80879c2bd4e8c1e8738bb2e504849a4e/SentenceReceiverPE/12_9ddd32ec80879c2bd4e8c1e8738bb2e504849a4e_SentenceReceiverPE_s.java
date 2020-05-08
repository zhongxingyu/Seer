 package test.s4;
 
 import io.s4.processor.AbstractPE;
 
 /**
  * This class receives Sentence events and print them on stdout.
  */
 public class SentenceReceiverPE extends AbstractPE {
 	
 	/**
 	 * Print received sentence event.
 	 * @param sentence
 	 */
 	public void processEvent(Sentence sentence){
		System.out.println("Recieved Sentence: " + sentence);
 	}
 
 	@Override
 	public void output() {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	public String getId() {
 		return this.getClass().getName();
 	}
 
 }
