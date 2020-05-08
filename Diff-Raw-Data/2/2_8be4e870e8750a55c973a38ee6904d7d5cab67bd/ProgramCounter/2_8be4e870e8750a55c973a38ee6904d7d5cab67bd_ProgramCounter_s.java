 
 public class ProgramCounter implements Clockable{
 	Pin pcIn = new Pin();
 	Pin pcOut = new Pin();
 	Pin control = new Pin();
 	
 	public ProgramCounter(){}
 
 	public void clockEdge() {
		if(new BinaryNum("0").equals(control)){
 			pcOut.setValue(pcIn.getValue());
 		}
 	}
 }
