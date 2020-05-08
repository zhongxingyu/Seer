 package userinterface;
 import search.*;
 import storage.FormulaDatabase;
 import storage.FormulaSaver;
 import internalformatting.*;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 /**
 	CLI.java
 	User interface for the sake of interfacing with users.
 	@author Jonathan Tan
 	@version 0.0.1 2/18/12
 */
 
 public class CLI
 {
 	
 	
 	//Valid search terms go here.
 	//May end up making a new class for this thing,
 	//Need a smarter way of checking if the term is in the array.
 	//Maybe ArrayList?
 	//Need Array "find" method!
 	
 	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 	static String[] comsearch = new String[]{"s","search"};
 	static String[] comadd = new String[]{"a","add","new"};
 	static String[] comprint = new String[]{"p","print"};
 	static String[] comprintall = new String[]{"print all","p all", "p a"};
 	static String[] comquit = new String[]{"exit","q","quit",};
 	static String[] comhelp = new String[]{"help"};
 	
 	static ArrayList<Formula> defaultFormulas = (ArrayList<Formula>)FormulaSaver.loadForms();	
 	
 	public static boolean inArray(String prompt, String[] comArray){
 		//I know, I know, I should use a binary search algorithm here.
 		//Will improve efficiency in a little bit, okay?
 		int truthvalue = Arrays.binarySearch(comArray, prompt);
 		if(truthvalue < 0)
 			return false;
 		else
 			return true;
 	}
 	
 	public static void main(String[] args) throws IOException{
 		
 		String uinput = "";
 		while(!uinput.equals("exit") && !uinput.equals("quit")){
 			//System.out.println("Command?");
 	
 			System.out.print(">");
 			uinput = in.readLine().toLowerCase();
 			
 			//Search case
 			if(inArray(uinput,comsearch)){
 				CLISearch.searchOption(defaultFormulas);
 				}
 				
 			
 			//Add case
 			if(inArray(uinput,comadd)){
				Formula newFormula = CLIAdd.AddFormula();
 				newFormula.printFormula();
 				System.out.println(newFormula.getAllTags());
 				
 				//CLAYVEN
 				//SAVE THINGS HERE
 				//YOOOOOOOOO
 				//YOOOOOOOOOO
 				//BOKU WAS HIS MOTHERFUCKING NAME
 			}
 			
 			//Print case
 			if(inArray(uinput,comprint)){
 				//STUB
 				break;
 			}
 			
 			if(inArray(uinput,comprintall)){
 				//STUB
 				break;
 			}
 			
 			//Quit case
 			if(inArray(uinput,comquit)){
 				break;
 			}
 			
 			if(inArray(uinput,comhelp)){
 				CLIHelp.help();
 			}
 		}
 
 	}
 
 }
