 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 
 public class LunchRoulette{
 	public static void main(String [] args){
 
 		ArrayList<String> members  = new ArrayList<String>(72);
 		String strline;
 		int size_of_groups = 0;
 		String input;
 
 
 		//Retrieve the size of the groups from the user
 		System.out.println("Welcome to Lunch Roulette!");
 		System.out.println("Please enter the size of each group:");
 		
 		//  open up standard input
       	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
           input = br.readLine();
           size_of_groups = Integer.parseInt(input);
       	} 
 
       	catch (IOException ioe) {
 	        System.out.println("IO error trying to read line!");
 	        System.exit(1);
       	}
 
 
 		//load all the names from the text file
 		try{
 			File file = new File("./team.txt");
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 			
 			//read in each line from file and store in the arraylist
 			while ((strline = reader.readLine()) != null ){
 				members.add(strline);
 			}
 
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 
 		//Let's try this shuffle thing on a collection
 		 Collections.shuffle(members);
 
 		//print all the names from the text file and split into groups
 		int groupNum;
 		for (int i=0; i < members.size(); i++){
 			//split into groups
 			if((i % size_of_groups) == 0){
 				groupNum = (i/size_of_groups) + 1;
 				System.out.println("\nGROUP #" + groupNum);
 			}
 			System.out.println(members.get(i));
 		}
 	}
 
 
 	//get a random number from the array
 	// public static int getRandomNum(ArrayList<String> members){
 	// 	int randNumber;
 	// 	Random rand = new Random();
 	// 	randNumber = rand.nextInt(members.size());
 
 	// 	return randNumber;
 	// }
 
}
