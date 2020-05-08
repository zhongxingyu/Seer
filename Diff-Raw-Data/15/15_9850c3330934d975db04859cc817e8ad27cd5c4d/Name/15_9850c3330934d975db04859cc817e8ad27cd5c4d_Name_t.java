 package uk.ac.aber.dcs.cs221.monstermash.util;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Scanner;
 
 /**
  * Utility class for generating random names by gender.
  * @author Jacob Smith, jas32
  *
  * 
  */
 public class Name {
 
 	private ArrayList<String> maleNameList;
 	private ArrayList<String> femaleNameList;
 	
 	
 	public Name() {
 		this.init();
 	}
 	
 	/**
 	 * Initialises the Name generator with a list of names. The data file expected is a public
 	 * record of first names given to newborns in Scotland in 2007.
 	 * 
 	 * Note! In order for this class to work properly the resource data/names.csv must be in the class path.
 	 * 
 	 * @return This name object.
 	 */
 	public Name init() {
 		maleNameList = new ArrayList<String>();
 		femaleNameList = new ArrayList<String>();
 			
		Scanner in = new Scanner(this.getClass().getClassLoader().getResourceAsStream("C:/Users/Ivan/Desktop/glassfish/data/names.csv") );
 		in.useDelimiter("[,\\s*]");
 		while (in.hasNext() ) {
 			try {
 				String s = in.next();
 				if (!s.equals("") ) {
 					maleNameList.add(s);
 				}
 				in.next();
 				in.next();
 				
 				s = in.next();
 				if (!s.equals("") ) {
 					femaleNameList.add(s);
 				}
 				//in.next();
 				in.next();
 				in.next();
 				
 			} catch (NoSuchElementException e) {}
 			
 		}
 		in.close();
 
 		return this;
 	}
 	
 	/**
 	 *  
 	 * @param male True if a male name is needed, else false.
 	 * @return A random name.
 	 */
 	protected String random (boolean male, java.util.Random rand) {
 		ArrayList<String> list = (male) ? maleNameList: femaleNameList;
 		
 		return list.get(rand.nextInt(list.size() ) );
 	}
 	
 	/**
 	 * Wrapper for @see #random(boolean).
 	 * @return A male name.
 	 */
 	public String male(java.util.Random rand) { return random(true, rand); }
 	/**
 	 * Wrapper for @see #random(boolean).
 	 * @return A female name.
 	 */
 	public String female(java.util.Random rand) { return random(false, rand); }
 
 	public static void main (String[] args) {
 		Name name = new Name();
 		name.init();
 		
 		
 	}
 }
 
