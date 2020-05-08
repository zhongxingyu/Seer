 package uk.ac.ucl.cs.clonedetector;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Index {
 	
 	private HashMap<BigInteger, ArrayList<CDoc>> index = new HashMap<BigInteger, ArrayList<CDoc>>();

 	public void updateIndex(BigInteger fingerprint, String fileName, int lineNumber) {
 		if (fingerprint == BigInteger.ZERO)
 			return;
 
 		if (!index.containsKey(fingerprint)) {
 			index.put(fingerprint, new ArrayList<CDoc>());
 		}
 
 		index.get(fingerprint).add(new CDoc(fileName, lineNumber));
 	}
 
 	public ArrayList<CDoc> linesWithFingerprint(BigInteger fingerprint) {
 		ArrayList<CDoc> matches = index.get(fingerprint);
 		
 		if (matches == null){
 			return new ArrayList<CDoc>();
 		}
 		return matches;
 	}
 
 	class CDoc {
 		private String fileName;
 		private int lineNumber;
 
 		public CDoc(String fileName, int lineNumber) {
 			this.fileName = fileName;
 			this.lineNumber = lineNumber;
 		}
 		
 		public String getFileName(){
 			return fileName;
 		}
 		
 		public int getLineNumber(){
 			return lineNumber;
 		}
 	}
 }
