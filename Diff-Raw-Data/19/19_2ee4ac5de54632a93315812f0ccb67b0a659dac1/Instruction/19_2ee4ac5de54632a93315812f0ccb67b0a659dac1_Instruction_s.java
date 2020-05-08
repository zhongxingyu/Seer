 package backEnd;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Instruction {
 	
 	List<String> myInstruction;
 	
 	public Instruction(String[] arguments){
 		loadInstruction(arguments);
 	}
 	
 	public Instruction(List<String> inst){
 		myInstruction = inst;
 	}
 	
 	private void loadInstruction(String[] arguments){
 		myInstruction = new ArrayList<String>();
 		for(int i = 0 ; i < arguments.length ; i++){
 			myInstruction.add(arguments[i]);
 		}
 	}
 
 	public Instruction progress(){
 		myInstruction = myInstruction.subList(1, myInstruction.size());
 		return this;
 	}
 	
 	public Instruction block(){
 		if(!myInstruction.get(0).equals("[")){
 			//throw exception
 		}
 		int bracket1Count = 1;
 		int bracket2Count = 0;
 		List<String> block = new ArrayList<String>();
 		for(String ins: myInstruction){
			if(bracket1Count == bracket2Count){
				break;
			}
 			if(ins.equals("]")){
 				bracket2Count++;
 			}
 			if(ins.equals("[")){
 				bracket1Count++;
 			}
 			block.add(ins);
 		}
		myInstruction = myInstruction.subList(block.size()-2, myInstruction.size());
		
		return new Instruction(block.subList(1, block.size()-3));
 		
 		
 	}
 	
 	public Instruction clone(){
 		return new Instruction(myInstruction);
 	}
 	
 	public String get(int index){
 		return myInstruction.get(index);	
 	}
 	
 	public int length(){
 		return myInstruction.size();
 	}
 	
 	public String toString(){
 		String a = "";
 		for(String b: myInstruction){
 			a+=b;
 		}
 		return a;
 	}
 	
 	
 }
