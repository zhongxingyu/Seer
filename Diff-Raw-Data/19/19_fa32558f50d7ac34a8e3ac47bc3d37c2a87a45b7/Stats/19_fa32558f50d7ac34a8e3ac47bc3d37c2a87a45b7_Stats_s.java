 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Stats {
 	
 	/**
 	 * Helper function to get number of Publications in ArrayList
 	 * @param pubs publications to get number from
 	 * 
 	 * @return number of Publications in ArrayList
 	 */
 	public static int NumCPs(ArrayList<Publication> pubs) {
 		int out=0;
 		
 		for(Publication pub : pubs)
 		{
 			if(!(pub instanceof JournalArticle))
 			{
 				out++;
 			}
 		}
 		return out;
 	}
 	
 	/**
 	 * Helper function to get number of Journal Articles in ArrayList
 	 * @param pubs journal articles to get number from
 	 * 
 	 * @return number of Journal Articles in ArrayList
 	 */
 	public static int NumJournals(ArrayList<Publication> pubs) {
 		int out=0;
 		
 		for(Publication pub : pubs)
 		{
 			if(pub instanceof JournalArticle)
 			{
 				out++;
 			}
 		}
 		return out;
 	}
 	
 	/**
 	 * Helper function to get number of different years in ArrayList
 	 * @param pubs publications to get number from
 	 * 
 	 * @return number of different years in ArrayList
 	 */
 	public static HashMap<String, Integer> NumOfYears(ArrayList<Publication> pubs) {
 		HashMap<String, Integer> numYears = new HashMap<String, Integer>();
 		
 		for(Publication pub : pubs)
 		{
 			 if(!numYears.containsKey(pub.getDate()))
 			 {
 				 numYears.put(pub.getDate(), 1);
 			 }
 			 else
 			 {
 				 numYears.put(pub.getDate(),numYears.get(pub.getDate()) + 1);
 			 }
 		}
 		
 		return numYears;
 	}
 	
 	/**
 	 * Helper function to get number of different years in ArrayList that are Publications
 	 * @param pubs publications to get number from
 	 * 
 	 * @return number of different years in ArrayList that are Publications
 	 */
 	public static HashMap<String, Integer> NumOfCPYears(ArrayList<Publication> pubs) {
 		HashMap<String, Integer> numYears = new HashMap<String, Integer>();
 		
 		for(Publication pub : pubs)
 		{
 			if(!(pub instanceof JournalArticle))
 			{
 				if(!numYears.containsKey(pub.getDate()))
 				{
 					numYears.put(pub.getDate(), 1);
 				}
 				else
 				{
 					numYears.put(pub.getDate(),numYears.get(pub.getDate()) + 1);
 				}
 			}
 		}
 		
 		return numYears;
 	}
 	
 	/**
 	 * Helper function to get number of different years in ArrayList that are Journal Articles
 	 * @param pubs publications to get number from
 	 * 
 	 * @return number of different years in ArrayList that are Journal Articles
 	 */
 	public static HashMap<String, Integer> NumOfJounalYears(ArrayList<Publication> pubs) {
 		HashMap<String, Integer> numYears = new HashMap<String, Integer>();
 		
 		for(Publication pub : pubs)
 		{
 			if(pub instanceof JournalArticle)
 			{
 				if(!numYears.containsKey(pub.getDate()))
 				{
 					numYears.put(pub.getDate(), 1);
 				}
 				else
 				{
 					numYears.put(pub.getDate(),numYears.get(pub.getDate()) + 1);
 				}
 			}
 		}
 		
 		return numYears;
 	}
 	
 	/**
 	 * Helper function to get number of coauthors in ArrayList
 	 * @param pub publications to get number from
 	 * 
	 * @return number of coauthors as an Array of integers
 	 */
	public static int[] NumCoAuthors(ArrayList<Publication> pubs) {
		int[] coAuthorNumArray = new int[pubs.size()];
 		
		for(int i=0; i<pubs.size(); i++){
			coAuthorNumArray[i]= pubs.get(i).getAuthors().size();
 		}
 		
		return coAuthorNumArray;
 	}
 	
 	/**
 	 * Helper function get only conference papers from all publications
 	 * @param pubs publications to get papers from
 	 * 
 	 * @return ArrayList of conference papers found in pubs
 	 */
 	public static ArrayList<Publication> ListOfConferencePapers(ArrayList<Publication> pubs) {
 		ArrayList<Publication> papers = new ArrayList<Publication>();
 		
 		for(Publication p: pubs){
 			if(!p.getClass().isInstance(JournalArticle.class))
 			{
 				papers.add(p);
 			}
 		}
 		
 		return papers;
 	}
 	
 	/**
 	 * Helper function get only journal articles from all publications
 	 * @param pubs publications to get papers from
 	 * 
 	 * @return ArrayList of journal articles found in pubs
 	 */
 	public static ArrayList<Publication> ListOfJournalArticles(ArrayList<Publication> pubs) {
 		ArrayList<Publication> papers = new ArrayList<Publication>();
 		
 		for(Publication p: pubs){
 			if(p.getClass().isInstance(JournalArticle.class))
 			{
 				papers.add(p);
 			}
 		}
 		
 		return papers;
 	}
 	
 }
