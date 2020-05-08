package thesaurus.parsingTemp;
 
 
 
 import teamo.thesaurus.parsing.Parser;
 
 /*
  * getAll, getRange return a list of vertices
  * getTableData returns a dictionary of strings of the vertices
  * currently in use
  */
 
 
 public class Driver 
 {
 	public static void main(String args[]){
 		
 		Parser driver = new Parser("C:/Users/Craig/Desktop/data.xml");
 		driver.getSynmsFor("Happy"); 
 		System.out.println(driver.getCurrentlyInUse());
 		System.out.println();
 		System.out.println(driver.getTableData());
 			
 			
 		}
 }
