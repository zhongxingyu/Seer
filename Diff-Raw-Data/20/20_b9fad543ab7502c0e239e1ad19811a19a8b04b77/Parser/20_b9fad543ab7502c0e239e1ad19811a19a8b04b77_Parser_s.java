 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Scanner;
 public class Parser {
 	static ArrayList<Terminals> classes;
 	public static void main(String[] args){
 		fileParser("input_spec.txt");
 		System.out.println("Input Regex: ");
 		Scanner scan = new Scanner(System.in);
 		if(scan.hasNext()){
 			RegExpFunc regex = new RegExpFunc(scan.next().trim());
 		}
 	}
 
 	public static void fileParser(String filename){
 		try{
 			Scanner scan = new Scanner(new File(filename));
 			Terminals currClass = null;
 			classes = new ArrayList<Terminals>();
 			while(scan.hasNextLine()){
 				String line = scan.nextLine();
 				Scanner lineScan = new Scanner(line);
 				int count = 0;
 				while(lineScan.hasNext()){
 					String token = lineScan.next();
 					if(count == 0 && token.charAt(0) == '$'){
 						Terminals newClass = new Terminals();
 						currClass = newClass;
 						classes.add(currClass);
 						currClass.setName(token.substring(1,token.length()));
 						System.out.println(currClass.getName());
 					}
 					else {
 						if(token.charAt(0) == '['){
 							int endIndex = token.indexOf(']');
 							String inside = token.substring(1,endIndex);
 							System.out.println(inside);
 							if(inside.charAt(0) == '^'){
 								ArrayList<Character> list = getIntervalOfChars(inside.substring(1));
 								String s = lineScan.next();
 								if(s.equals("IN")){
 									String newClass = lineScan.next();
 									String className = newClass.substring(1);
 									System.out.println("exclude from "+className);
 									for(Terminals eachClass : classes){
 										if(eachClass.getName().equals(className)){
 											Object[] chars = eachClass.getChars().toArray();
 											for(int i=0; i<chars.length; i++){
 												currClass.addChar((Character)(chars[i]));
 											}
 											for(char c : list){
 												currClass.removeChar(c);
 											}
 										}
 									}
 								}
 							} else {
 								ArrayList<Character> list = getIntervalOfChars(inside);
 								for(char c : list){
 									currClass.addChar(c);
 									//System.out.println("added "+c+" to "+currClass.getName());
 								}
 							}
 						}
					}
 					count++;
 				}
 			}
 		} catch(Exception e){}
 
		for(Terminals eachClass : classes){
 			//commented portion is for when we start using the toDFA() method
 
 			/*State startState = new State();
 			startState.setLabel("start");
 			startState.setAccepts(false);
 
 			State acceptState = new State();
 			acceptState.setLabel("accept");
 			acceptState.setAccepts(true);
 
 			HashSet<Character> chars = eachClass.getChars();
 			for(Character c : chars){
 					startState.addTransition(new Transition(startState, c, acceptState));
 			}
 			NFA nfa = new NFA(startState);
 			eachClass.setNFA(nfa);
 			DFA dfa = nfa.toDFA();
 			eachClass.setDFA(dfa);*/
 
			State startState = new State();
 			startState.setLabel("start");
 			startState.setAccepts(false);
 
 			State acceptState = new State();
 			acceptState.setLabel("accept");
 			acceptState.setAccepts(true);
 
 			State deadState = new State();
 			deadState.setLabel("dead");
 			deadState.setAccepts(false);
 			
 			DFA dfa = new DFA(startState);
 			dfa.addState(acceptState);
 			dfa.addState(deadState);
 
 			HashSet<Character> chars = eachClass.getChars();
 			System.out.println(eachClass.getName());
 			for(int num=0; num<=127; num++){
 				char c = (char)num;
 				if(chars.contains(c)){
 					dfa.addTransition(startState, c, acceptState);
 					System.out.print(c+ " ");
 				} else {
 					dfa.addTransition(startState, c, deadState);
 				}
 			}
 			System.out.println();
 			
 			eachClass.setDFA(dfa);
			eachClass.setNFA(dfa);
 		}
	}
 	 /**
 	 * @return the classes
 	 */
 	public static ArrayList<Terminals> getClasses() {
 		return classes;
 	}
 
 	public static ArrayList<Character> getIntervalOfChars(String inside){
 		ArrayList<Character> list = new ArrayList<Character>();
 		int index=0;
 		char previousChar = inside.charAt(0);
 		while(index<inside.length()){
 			char c = inside.charAt(index);
 			if(c == '-'){
 				previousChar++;
 				char currentChar = previousChar;
 				char endChar = inside.charAt(index+1);
 				while (currentChar < endChar){
 					list.add(currentChar);
 					//System.out.println("added "+currentChar+" to "+currClass.getName());
 					currentChar++;
 				}
 			}
 			else{
 				list.add(c);
 				//System.out.println("added "+c+" to "+currClass.getName());
 			}
 			previousChar = c;
 			index++;
 		}
 		return list;
 	}
 }
