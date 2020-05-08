 package userinterface;
 import java.io.*;
 import internalformatting.*;
 
 /**
  * CLIAdd.java : Add module for the CLI that lets a user create formulas to save to the database.
  * 	@author Jonathan Tan
  * 	@version -0.0.1
  */
 
 public class CLIAdd extends CLI{
 	
 	public static Formula addFormula() throws IOException{
 		Formula newFormula = new Formula();
 		System.out.println("Enter name of formula:");
 		System.out.print(">");
 		//Need to restructure this exception handling.
 		//Should be handled by CLI.
 		try{
 			newFormula.setName(in.readLine());
 		}
 		catch(IOException ex){
 			System.out.println("Invalid name! Ceasing operation.");
 			newFormula.setName("YOU MESSED UP");
 			return newFormula;
 		}
 		String input = "";
 		//System.out.println("Enter term and/or operator. When finished entering terms, enter 'done' when it says "Enter operator.");
 		while(!input.equals("done")){
 			
 			//System.out.print(">");
 			//input = in.readLine();
 			
 			//if(input.equals("term")){
 				System.out.println("Enter term as coefficient (double) <SPACE> variable (char) <space> exponent (int):");
 				System.out.print(">");
 				input = in.readLine();
 				
 				//if(input.equals("done"))
 				//	break;
 				
 				String[] termTerms = input.split(" ");
 				//Default values for term constructor
 				Double coeff = 0.0;
 				Variable var = null;
 				Integer exp = 0;
 				if(termTerms.length==3){
 					coeff = Double.parseDouble(termTerms[0]);
 					var = new Variable(termTerms[1]);
 					exp = Integer.parseInt(termTerms[2]);
 				}else{
 					System.out.println("Error: parameters not passed correctly. Debug values are used.");
 				}
 				//UNIT IS NULL FOR NOW
 				Term tempTerm = new Term(coeff,var,exp, null);
 				newFormula.add(tempTerm);
 				System.out.println("Term added.");
 			//}
 			
 			//if(input.equals("operator")){
 				System.out.println("Enter operator: ");
 				System.out.print(">");
 				input = in.readLine();
 				
 				if(input.equals("done"))
 					break;
 				
 				Operator tempOperator = new Operator(input);
 				newFormula.add(tempOperator);
 				System.out.println("Operator added.");
 			//}
 		}
 		
 		//Entering info
 		System.out.println("Enter info:");
 		System.out.print(">");
 		input = in.readLine();
 		newFormula.setInfo(input);
 		
 		//Entering tags
 		//This is broken
 		System.out.println("Enter comma-separated tags:");
 		System.out.print(">");
 		input = in.readLine();
 		String[] tagsTemp = input.split(",");
 		
 		for(int i=0;i<tagsTemp.length;i++){
			newFormula.addTag(tagsTemp[i]);
 			}
 		
 		return newFormula;
 	}
 	
 }
