 
 
 /**
  * Search is a class to search through terms
  * of formula class with the search terms entered by the user
  * @author May Camp
  * @version 02/18/2012 for CS 48 Project, W12
  */
 import java.util.ArrayList;
 
public static class search {
 	// for a one word search term
     //assuming we store all formulas in ArrayList<Formula> 
     //and enter that as the first argument
     //and there are tags in ArrayList<Tag> under each formula
 	
 
 	private ArrayList<Formula> formulas = new ArrayList<Formula>();
 	/**
 	 * 
 	 * @param searchTerm The term you are searching for
 	 * @return formulas An array list of formulas
 	 */
 	ArrayList<Formula> foundFormulas(String searchTerm) {
 		int fsize = formulas.size(); //size of formula arraylist
 		String currentTag;
 		for(int i=0; i<fsize; i++) { // loop through formulas
			int tsize = formulas.get(i).getTagSize(); //size of tag arraylist
 			for(int j=0; j<tsize; j++) { // loop through tags
				currentTag = formulas.get(i).getTag(j);    	
 				if(searchTerm == currentTag) {
					this.formulas.add(formulas.get(i));
 				}
 			}
 		}
 		return formulas;
 		
 	}
 
 }
