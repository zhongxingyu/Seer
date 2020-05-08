 package edu.ucla.cs.cs144;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import edu.ucla.cs.cs144.AuctionSearch;
 import edu.ucla.cs.cs144.SearchResult;
 import edu.ucla.cs.cs144.SearchConstraint;
 import edu.ucla.cs.cs144.FieldName;
 
 public class AuctionSearchTest {
 	public static void main(String[] args1)
 	{
 		AuctionSearch as = new AuctionSearch();
 
 		String message = "Test message";
 		String reply = as.echo(message);
 		System.out.println("Reply: " + reply);
 		System.out.println();
 		
 		// Basic limited
 		String query = "superman";
 		SearchResult[] basicResults = as.basicSearch(query, 0, 20);
 		System.out.println("Basic Search Query: " + query);
 		System.out.println("Received " + basicResults.length + " results");
 		for(SearchResult result : basicResults) {
 			System.out.println(result.getItemId() + ": " + result.getName());
 		}
 		System.out.println();
 		
 		// Basic skip beyond number of results
 		query = "superman";
 		basicResults = as.basicSearch(query, 72, 0);
 		System.out.println("Basic Search Query: " + query);
 		System.out.println("Received " + basicResults.length + " results");
 		for(SearchResult result : basicResults) {
 			System.out.println(result.getItemId() + ": " + result.getName());
 		}
 		System.out.println();
 
 		// Basic unlimited
 		query = "superman";
 		basicResults = as.basicSearch(query, 0, 0);
 		System.out.println("Basic Search Query: " + query);
 		System.out.println("Received " + basicResults.length + " results");
 		for(SearchResult result : basicResults) {
 			System.out.println("     " + result.getItemId() + ": " + result.getName());
 		}
 		System.out.println();
 		
 		// basic unlimited
 		query = "kitchenware";
 		basicResults = as.basicSearch(query, 0, 0);
 		System.out.println("Basic Search Query: " + query);
 		System.out.println("Received " + basicResults.length + " results");
 		for(SearchResult result : basicResults) {
 			System.out.println(result.getItemId() + ": " + result.getName());
 		}
 		System.out.println();
 		
 		// basic unlimited
 		query = "star trek";
 		basicResults = as.basicSearch(query, 0, 0);
 		System.out.println("Basic Search Query: " + query);
 		System.out.println("Received " + basicResults.length + " results");
 		for(SearchResult result : basicResults) {
 			System.out.println(result.getItemId() + ": " + result.getName());
 		}
 		System.out.println();
 		
 		// Advanced
 		SearchConstraint constraint =
 		    new SearchConstraint(FieldName.BuyPrice, "5.99"); 
 		SearchConstraint[] constraints = {constraint};
 		SearchResult[] advancedResults = as.advancedSearch(constraints, 0, 20);
 		System.out.println("Advanced Search");
 		System.out.println("Received " + advancedResults.length + " results");
 		for(SearchResult result : advancedResults) {
 			System.out.println(result.getItemId() + ": " + result.getName());
 		}
 		System.out.println();
 		
 		// Advanced
 		SearchConstraint constraint1 = new SearchConstraint(FieldName.ItemName, "pan");
 		SearchConstraint constraint2 = new SearchConstraint(FieldName.Category, "kitchenware");
 		SearchConstraint[] constraints2 = {constraint1, constraint2};
 		advancedResults = as.advancedSearch(constraints2, 0, 20);
 		System.out.println("Advanced Search");
 		System.out.println("Received " + advancedResults.length + " results");
 		for(SearchResult result : advancedResults) {
 			System.out.println(result.getItemId() + ": " + result.getName());
 		}
 		System.out.println();
 		
 		String itemId = "1497595357";
 		String item = as.getXMLDataForItemId(itemId);
 		System.out.println("XML data for ItemId: " + itemId);
 		System.out.println(item);
 		System.out.println();
 		
 		itemId = "1497497054";
 		item = as.getXMLDataForItemId(itemId);
 		System.out.println("XML data for ItemId: " + itemId);
 		System.out.println(item);
 		System.out.println();
 
 		// Add your own test here
 	}
 }
