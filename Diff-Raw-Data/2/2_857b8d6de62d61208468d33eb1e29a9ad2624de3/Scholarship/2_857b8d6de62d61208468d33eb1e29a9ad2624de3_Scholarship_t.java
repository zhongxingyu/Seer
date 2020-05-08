 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class Scholarship implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6640981900226594101L;
 	
 	//ArrayList of Scholar objects
 	private ArrayList<Scholar> scholarList = new ArrayList<Scholar>();
 	//Maps Serial title with Serial object
 	private HashMap<String, Serial> serialMap;
 	//Maps Publication title to  Publication object
 	private HashMap<String, Publication> pubMap;
 	
 	/**
	 * No argument constructor which creates an empty scholarship
 	 */
 	public Scholarship(){
 	}
 	
 	/**
 	 * Getter for values of the List of Scholars
 	 * @return a copy of scholarList
 	 */
 	public ArrayList<Scholar> getScholarList() {
 		return new ArrayList<Scholar>(scholarList);
 	}
 	
 	/**
 	 * Getter for values of the HashMap of Serials
 	 * @return a copy of serialMap
 	 */
 	public HashMap<String, Serial> getSerialMap() {
 		return new HashMap<String, Serial>(serialMap);
 	}
 	
 	/**
 	 * Getter for values of the HashMap of Publications
 	 * @return a copy of pubMap
 	 */
 	public HashMap<String, Publication> getPubMap() {
 		return new HashMap<String, Publication>(pubMap);
 	}
 
 	/**
 	 * Method to add an entire scholarship
 	 * @param scholarship    the scholarship bieng loaded
 	 */
 	public void addScholarship(Scholarship scholarship) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Method to add a scholar
 	 * @param scholar    the scholar object being added
 	 */
 	public void addScholar(Scholar scholar) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Method to delete a scholar
 	 * @param scholar    the scholar object being deleted
 	 */
 	public void deleteSelectedScholar(Scholar scholar) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Method to delete all scholars
 	 */
 	public void deleteAllScholars() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Method to add a serial
 	 * @param serial	serial object being added
 	 */
 	public void addSerial(Serial serial) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Method to delete a serial
 	 * @param serial	serial object being deleted
 	 */
 	public void deleteSelectedSerial(Serial serial) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Method to delete all serials
 	 */
 	public void deleteAllSerials() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Method to add a publication
 	 * @param pub	the publication being added
 	 */
 	public void addPaper(Publication pub) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Method to delete a publication
 	 * @param pub	the publication being deleted
 	 */
 	public void deleteSelectedPaper(Publication pub) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Method to delete all publications
 	 */
 	public void deleteAllPapers() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 	
 	
 	
 }
