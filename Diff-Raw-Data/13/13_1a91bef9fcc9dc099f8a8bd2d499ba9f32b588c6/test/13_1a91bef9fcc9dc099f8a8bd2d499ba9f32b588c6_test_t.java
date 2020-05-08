 /*
 *The purpose of this code is to test StringHolder and RunCode
 * NOTE: Does not test Shell.java
 */
 import java.io.*;
 public class test{
 	public static void main(String[] args)throws IOException{
 		StringHolder comms = new StringHolder();
 		comms.add("import java.io.*;");
 		comms.add("}public static String fisch(){return \"fisch\";};");
		comms.add("int i;");
		comms.add("i=5");
 		comms.add("fisch();");
 	//	comms.add("int a = 5;"); //doesn't work yet
 		RunCode run = new RunCode(comms.toFile());
	//	System.out.println(comms.toFile()); //debug
 		run.comprun();
 	}

//TODO: raptortech97, make this work
		//and update testRes if you could, with what this file should print out
 /*
 	public static void testforGoodInput(){
 		StringHolder unsafe = new StringHolder();
 		StringHolder safe = new StringHolder();
 		RunCode run = new RunCode(unsafe.toFile());
 		String currentInput = "char j = 'k'";
 		unsafe.add(currentInput);
 		try{
 			run = new RunCode(unsafe.toFile());
 			safe.add(currentInput);
 			
 		}
 		catch (IOException compileFail){
 			//Removes the last input if possible
 			if (!unsafe.methods.remove(currentInput)
 				&&!unsafe.orders.remove(currentInput)
 				&&!unsafe.imports.remove(currentInput))
 					System.out.println("You broke it. Please tell the developers how you managed to do this.");
 			else{//This is the code that should be run. If the print statement above gets shown, things are wrong.
 				System.out.println("You tried to add" + currentInput);
 				System.out.println("This threw some sort of error.\n Please try again.");
 				System.out.println(compileFail);
 			}
 		}
 		
 	}
 */
 }
 
