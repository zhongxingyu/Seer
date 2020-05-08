 import java.util.*;
 
 public class LineNumber {	
 	
 	private ArrayList<Integer> current;
 	
 	boolean iAmDebugging = true;
 	
 	public LineNumber() {
 		current = new ArrayList<Integer>();
 		current.add(1);
 	}
 	
 	public void setLineNumber(String s) throws IllegalLineException {
 		if (s.charAt(s.length()-1)=='.') {
			throw new IllegalLineException("Extra period.");				
 		}
 		current = new ArrayList<Integer>();
 		//Makes string into a array of integers.
 			String[] temp= s.split("\\.");
 			try {	//Checks for correct format. 
 				for (int i=0; i<temp.length;i++) {
 					if (temp[i].equals("")) {
 						throw new IllegalLineException("Extra period.");				
 					}
 					current.add(Integer.parseInt(temp[i]));
 				}
 			 } catch (NumberFormatException i) { //Not integers.
 					throw new IllegalLineException("Line reference can only be numbers and periods.");	
 				 }
 	}
 
 	//Use these functions?
 	public void endSubproof() {
 		current.remove(current.size()-1); //Exit subproof.		
 		current.set(current.size()-1, current.get(current.size()-1)+1); //Next line outside of subproof.
 	}
 	
 	public void startSubproof() {
 		if (current.get(0)==1) { //Special case for step 2.
 			current.set(0, 2);
 		} else {
 			current.add(1);
 		}
 	}
 	
 	public void nextLine() { //Not starting or ending subproof.
 		current.set(current.size()-1, current.get(current.size()-1)+1);
 		}
	
	//Delete this?
	public void findNextLine(String s) {
		
		//Needs another case for valid but unnecessary contradictions.
		//Find nextline if completes subproof/proof.
		
		
		if (s.equals("show") && current.get(0)!=1) {
			current.add(1); //Start subproof.
		} else {
			if (s.equals("ic")) {
					if (current.size()!=1) { //Inside a subproof.
						current.remove(current.size()-1); //Exit subproof.
					}
				}
			//Next line outside of subproof.
			current.set(current.size()-1, current.get(current.size()-1)+1);
			}
		}
 		 
 	//Check if the line array 
 	public void isAccessibleLine(LineNumber n) throws IllegalInferenceException {
 		int sizeOfN = n.current.size();
 		if (current.size()<sizeOfN) { //Line cannot be longer than current.
 			throw new IllegalInferenceException("Inaccessible line."); 
 		}
 		//Checking if integer except last is equal.
 		for (int i=0; i<sizeOfN-1;i++) {
 			/*if (iAmDebugging) {
 				System.out.println(s[i] + "compare" + current.get(i));
 			}*/ if (n.current.get(i)!=current.get(i)) {
 				throw new IllegalInferenceException("Inaccessible line.");
 			}
 		}
 		//Last integer must be less than last integer of current line.
 		if (n.current.get(sizeOfN-1) >= current.get(sizeOfN-1)) {
 			throw new IllegalInferenceException("Inaccessible line."); 		
 		}
 	}
 	
 	public String toString() {
 		String temp = "";
 		//Turns ArrayList into a string with periods.
 		for (int i = 0; i<current.size();i++) {
 			temp += Integer.toString(current.get(i)) +".";
 		}
 		return temp.substring(0,temp.length()-1); //Exclude last period.
 	}
 }
 
