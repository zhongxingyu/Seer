 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import com.sk.SearchController;
 import com.sk.util.PersonalDataStorage;
 
 /**
  * Main class. Run this to search for people.
  * 
  * @author Strikeskids
  * 
  */
 public class Driver {
 
 	public static void main(String[] args) throws IllegalStateException, IOException {
 		SearchController searcher = new SearchController();
 		Scanner in = new Scanner(System.in);
 		Gson gson = new Gson();
 		JsonObject output = new JsonObject();
 		while (true) {
 			System.out.print("First name: ");
 			String first = in.nextLine();
 			if (first.length() == 0)
 				break;
 			System.out.print("Last name: ");
 			String last = in.nextLine();
 			long start = System.currentTimeMillis();
 			System.out.println("Searching...");
 			if (searcher.lookForName(first, last)) {
 				PersonalDataStorage pds = searcher.getDataStorage();
 				output.add(first + " " + last, gson.toJsonTree(pds));
 				System.out.printf("Found %d possible results%n", pds.size());
 			} else {
 				System.out.println("None found");
 			}
 			System.out.printf("Query took %.3f seconds%n", (System.currentTimeMillis() - start) / 1000d);
 		}
 		System.out.print("Output file location: ");
 		String file = in.nextLine();
 		BufferedWriter w = new BufferedWriter(new FileWriter(file));
 		w.append(output.toString());
 		w.close();
 		in.close();
		searcher.close();
 	}
 }
