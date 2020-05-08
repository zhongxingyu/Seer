 package com.cse454.nel;
 
 import java.sql.SQLException;
 import java.util.Scanner;
 
 import com.cse454.nel.search.BasicSearcher;
 
 public class Test {
 
 	private static Scanner scanner;
 
 	/**
 	 * @param args
 	 * @throws SQLException
 	 */
 	public static void main(String[] args) throws SQLException {
 		scanner = new Scanner(System.in);
 		WikiConnect wiki = new WikiConnect();
 		SentenceConnect docs = new SentenceConnect();
 		BasicSearcher searcher = new BasicSearcher(wiki);
 		
 		while (true) {
 			System.out.print("Enter a query: ");
 			String query = scanner.nextLine();
 
 			try {
			//	System.out.println(wiki.GetWikiText(query));
				searcher.GetCandidateEntities(new EntityMention(0, query, 0, 0));
 				//wiki.Search(query);
 				//docs.getDocument(Integer.valueOf(query));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
     }
 
 }
