 package assignment7;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import assignment7.EpidemicSystem;
 import assignment7.EpidemicSystem.Node;
 
 
 public class Main {
 	final static List<Patient> list = new ArrayList<Patient>();
 	static String temp;
 	static Scanner in = new Scanner(System.in);
 	static Node root;
 	
 	public static void main( String[] args ) {
 		printStartUpMessage();
 		list.add(new Patient(160, 72.7));
 		list.add(new Patient(178, 82.7));
 		list.add(new Patient(167, 76.9));
 		list.add(new Patient(177, 82.0));
 		list.add(new Patient(156, 79.3));
 		list.add(new Patient(180, 76.4));
 		list.add(new Patient(176, 78.9));
 		list.add(new Patient(187, 73.1));
 		list.add(new Patient(155, 76.7));
 		list.add(new Patient(190, 78.8));
 		
 		
 		EpidemicSystem es = new EpidemicSystem();
 		mainMenu(sendListToTree(list, es), es);
 	}
 	
 	private static void printStartUpMessage() {
 		System.out.printf("Doctor we need your help!!\nThe situation is very grave!!\nA mass number of patients have been admitted to the hospital;\n");
 		System.out.printf("however, all the patients have amnesia!!\nSo we have no medical data or history to go off of besides their height and weight.\n");
 		System.out.printf("We would like you to make the executive desicsion on how we should organize the patients.\n");
 		
 	}
 	
 	private static void mainMenu(String sortType, EpidemicSystem es) {
 		while(true){
 			System.out.printf("\nNow what do you want to do?:\n(1): search for patient\n(2): admit patient\n(3): discharge patient\n(4): resort tree\n(5): traverse tree in order\n(6): leave hospital\n>>>");
 			int option;
 			while ( true ){
 				temp = in.nextLine();
 				if(!isNumber(temp)) continue;
 				option = Integer.parseInt(temp);
 				if(option != 1 && option != 2 && option != 3 && option != 4 && option != 5) continue;
 				break;
 			}
 			
 			switch(option) {
 			case 1: 
 				searchForPatient(sortType, es);
 				break;
 			case 2: 
 				admitPatient(sortType, es);
 				break;
 			case 3: 
 				dischargePatient(sortType, es);
 				break;
 			case 4: 
 				sendListToTree(list, es);
 				break;
 			case 5:
 				es.printInOrder(root, sortType);
 				break;
 			case 6: return;
 			}
 		}
 	}
 	
 	public static String sendListToTree( List<Patient> list, EpidemicSystem es ) {
 		int option;
 
 		while ( true ){
 			System.out.println("To sort by weight, enter 0");
 			System.out.printf("To sort by Height, enter 1\n>>>");
 			temp = in.nextLine();
 			if(!isNumber(temp)) continue;
 			option = Integer.parseInt(temp);
 			if(option != 0 && option != 1) continue;
 			break;
 		}
 		
 		if ( option == 0 ) {
 			Main.root = new Node(list.get(0).getWeight());
 			es.root = root;
 			for( Patient current : list ) {
 				es.insert(root, current.getWeight());
 			}
 			System.out.println("You have sorted the patients by weight.");
 			return "weight";
 		}
 		
 		else {
 			Main.root = new Node(list.get(0).getHeight());
 			es.root = root;
 			for( Patient current : list ) {
 				es.insert(root, current.getHeight());
 			}
 			System.out.println("You have sorted the patients by height.");
 			return "height";
 		}
 		
 	}
 	
 	public static boolean isNumber(String temp) {
 		try{
 			@SuppressWarnings("unused")
 			double d = Double.parseDouble(temp);
 		} catch(NumberFormatException nfe){
 			return false;
 		}
 		return true;
 	}
 	
 	private static void searchForPatient(String sortType, EpidemicSystem es) {
		System.out.printf("Search for patient by %s: ", sortType);
 		while(true){
 			try {
 				double search = readval(300);
 				if(es.inTree(root, search)) {
 					Patient patient = findPatient(search, sortType);
					System.out.printf("Patient's known information\nWeight: %.2f kg\nHeight: %.2f cm", patient.getWeight(), patient.getHeight());
 				}
 				else
 					System.out.println("That patient does not exsist.");
 				break;
 			} catch (IOException e) {
 			
 			}
 		}	
 	}
 	
 	private static Patient findPatient(double search, String sortType) {
 		for( Patient current : list ){
 			if(sortType == "weight") {
 				if( current.getWeight() == search) {	
 					return current;
 				}
 			}
 			else 
 				if( current.getHeight() == search)
 					return current;
 		}
 		return null;
 	}
 	
 	private static void admitPatient(String sortType, EpidemicSystem es) {
 		while(true){
 			try {
 				System.out.print("Enter in weight:");
 				double weight = readval(300);
 				System.out.print("Enter in height:");
 				double height = readval(300);
 				if( sortType == "weight") es.insert(root, weight);
 				else es.insert(root, height);
 				list.add(new Patient(height, weight));
 				break;
 			} catch (IOException e) {
 				
 			}
 		}
 	}
 	
 	private static void dischargePatient(String sortType, EpidemicSystem es) {
 		while(true){
 			try {
 				System.out.print("Enter in weight:");
 				double weight = readval(300);
 				System.out.print("Enter in height:");
 				double height = readval(300);
 				if( sortType == "weight") es.delete(weight);
 				else es.delete(height);
 				list.remove(findPatient(height, sortType));
 				break;
 			} catch (IOException e) {
 				
 			}
 		}
 	}
 	
 	public static double readval(double check2) throws IOException {
         BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
         double check1 = 0;
         boolean uix = true;
         double status = -1;
         while (uix){
         	try{				// Reads the user input. If input is not between the values, then it re-prompts the user for input
         		status = Double.parseDouble(input.readLine());
                 uix = false;
                 if (status < check1 || status > check2){
                     uix = true;
                     System.out.println("_Incorrect value: please input a double (between " + check1 + " and " + check2 + ")");
                     System.out.println("");
                     continue;
                 }
         	}catch (NumberFormatException nos){
                     System.out.println("_Incorrect value: please input an integer");
                     uix = true;
                     System.out.println("");
                 }
                 
         }
         if (status == -1){
         	throw new Error("##########ERROR IN READVAL1 METHOD, QUITTING PROGRAM##########");
         }
         return status;
 	}
 }
