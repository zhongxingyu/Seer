 package assignment1;
 
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 public class Main {
 
 	Main(){
 		
 	}
 	
 	private char nextChar(Scanner in){
 		return in.next().charAt(0);
 	}
 	
 	private boolean nextCharIsDigit (Scanner in) {
 		return in.hasNext("[0-9]");
 	} 
 	
 	private boolean nextCharIsLetter (Scanner in) {
 		return in.hasNext("[a-zA-Z]");
 	}
 	
 	private boolean nextCharIs(Scanner in, char c) {
 		return in.hasNext(Pattern.quote(c+""));
 	}
 	
 	private boolean is1Correct(Verzameling v){				
 		boolean valid = false;		
 		while(valid == false){
 			Scanner in = new Scanner(System.in);
 			in.useDelimiter("");
 			v.init();
 			System.out.print("Geef eerste verzameling: ");
 			
 			/*if (nextCharIs == EOF)
 				do something cool here;
 			*/
 			
 			if(nextCharIs(in, '{')){
 				in.next(); //Get rid of '{'
 				
 				/*
 				First you need a way to keep reading until you encounter a '}' character, with an error handling if such a character is non-existent.
 				Second read until a space or said '}', and add everything you've read in an Identifier.
 				If something you've read doesn't qualify as valid input, abort and start the entire loop over again.
 				If your Identifier is valid, add it to v.
 				If you've read everthing and every Identifier is valid and v is also valid, set boolean 'valid' to true, so the while loop ends and the program continues.
 				Make sure there are correct error handling messages for each specific error.
 				You also need to find an exception in case of an EOF character (ctrl+z windows), in which false is returned, essentially stopping the program.
 				
 				I tried to find solutions with both while(in.hasNext()) as well as while(!in.nextCharIs(in, '}')) as loop within the outer loop,
 				but couldn't find a working one for either.
 				
 				My best attempt looked something like the code below:
 				*/
 				
 				start:
 				while(in.hasNext()){
 					
					if(nextCharIs(in, '}'){
 						if(v.getSize() < 11){
 							valid = true;
 							break;
 						}else {
 							System.out.println("Verzameling heeft meer dan 10 elementen");
 							break;
 						}
 					}
 					
					if(nextCharIsLetter(in){
 						Identifier id = new Identifier();
						id.init(in.next());
						while(!(nextCharIs(in, ' ' || nextCharIs(in, '}')){
							if(nextCharIsLetter || nextCharIsDigit){
								id.addChar(in.next);
 							}else{
 								System.out.println("Identifier mag alleen bestaan uit letters en cijfers");
 								break start;
 							}
 						}  
 						v.addElement(id);
 					}else{
 						System.out.println("Identifier moet beginnen met een letter.");
 						break;
 					}
 				}
 			}else System.out.println("Verzamling moet beginnen met een '{'");
 		}
		
 		return true;
 	}
 	
 	private boolean is2Correct(Verzameling v){				
 		boolean valid = false;
 		while(valid == false){
 			Scanner in = new Scanner(System.in);
 			in.useDelimiter("");
 			v.init();
 			System.out.print("Geef tweede verzameling: ");
 			
 			if(nextCharIs(in, '{')){
 				valid = true;
 				
 			}else System.out.println("Verzamling moet beginnen met een '{'");
 		}
 		
 		return true;
 	}
 	
 	private boolean readyToGo(Verzameling v1, Verzameling v2){
 		return is1Correct(v1) && is2Correct(v2);
 	}
 	
 	private void start(){
 		Verzameling v1 = new Verzameling(),
 					v2 = new Verzameling();
 		while(readyToGo(v1, v2)){
 			System.out.println("done\n");
 		}
 	}
 	
 	public static void main(String[] args) {
 		new Main().start();
 	}
 
 }
