 package indeece;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.TreeSet;
 import java.io.*;
 
 public class Doc implements Comparable<Doc>,Serializable{
 	private int ID;
 	private String title;
 	private String body;
 	private int length;
 	private float vectorNorm;
 	
 	public Doc(int iD, String title, String body) {
 		super();
 		ID = iD;
 		this.title = title;
 		this.body = body;
 		this.vectorNorm = 0;
 		// set this.length (TODO)
 	}
 	
 	public void setID(int ID){
 		this.ID = ID;
 	}
 	public int getID(){
 		return this.ID;
 	}
 	public void setTitle(String title){
 		this.title = title;
 	}
 	public String getTitle(){
 		return this.title;
 	}
 	public void setBody(String body){
 		this.body = body;
 	}
 	public String getBody(){
 		return this.body;
 	}
 	
 	public int getLength() {
 		return this.length;
 	}
 	
 	public void addToNorm(float weight) {
 		this.vectorNorm+=(float)Math.pow((double)weight, 2.0);
 	}
 	
 	public void finalizeVectorNorm() {
 		
 		this.vectorNorm= (float) Math.sqrt((double)vectorNorm);
 	}
 	
 	public String toString() {
 		return this.title;
 	}
 	
 	// Returns the intersection of set1 and set2	
 	public static Set<Doc> and(Set<Doc> set1, Set<Doc> set2) {
 		
 		if((set1 == null) || (set2 == null)) {
			System.err.println("Doc.and() returning null!");
 			return new TreeSet<Doc>();
 		}
 		//set1.retainAll(set2);
 		//return set1;
 		
 		TreeSet<Doc> result = new TreeSet<Doc>();
 		Iterator<Doc> op1 = set1.iterator();
 		Iterator<Doc> op2 = set2.iterator();
 		
 		Doc current1 = getNext(op1);
 		Doc current2 = getNext(op2);
 
 		while(current1!=null && current2!=null) {
 			//If is the same document
 			if(current1.getID() == current2.getID()) {
 				result.add(current1);
 				current1 = getNext(op1);
 				current2 = getNext(op2);
 			}
 			//If op2 is ahead of op1
 			else if(current1.getID() < current2.getID()) 
 				current1 = getNext(op1);
 			//If op1 is ahead of op2
 			else
 				current2 = getNext(op2);
 		}
 		
 		return result;
 	}
 	
 	private static Doc getNext(Iterator<Doc> it) {
 		Doc nextDoc;
 		try {
 			nextDoc = it.next();
 		}
 		catch(NoSuchElementException e) {
 			nextDoc = null;
 		}
 		return nextDoc;
 	}
 	
 	// Returns the union of this and the other set
 	public static Set<Doc> or(Set<Doc> set1, Set<Doc> set2) {
 		if(set1 == null)
 			set1 = new TreeSet<Doc>();
 		if(set2 == null)
 			set2 = new TreeSet<Doc>();
 		
 		TreeSet<Doc> result = new TreeSet<Doc>(set1);
 		result.addAll(set2);
 		return result;
 	}
 	
 	//Returns the complement of the corpus	
 	public static Set<Doc> not(Set<Doc> set1,Set<Doc> corpus) {
 		if(set1 == null)
 			set1 = new TreeSet<Doc>();
 		TreeSet<Doc> result = new TreeSet<Doc>(corpus);
 		result.removeAll(set1);
 		return result;
 	}
 	
 	@Override
 	public int compareTo(Doc arg0) {
 		// TODO Auto-generated method stub
 		return this.getID()-arg0.getID();
 	}
 
 	public float getVectorNorm() {
 		return vectorNorm;
 	}
 }
