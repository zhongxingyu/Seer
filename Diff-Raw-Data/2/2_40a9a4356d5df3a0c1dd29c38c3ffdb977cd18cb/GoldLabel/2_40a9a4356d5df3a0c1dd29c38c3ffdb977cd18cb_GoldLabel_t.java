 package test.java.com.apitests.helpers;
 
 import java.util.Hashtable;
 
 public class GoldLabel {
 
 	private final Hashtable<String, String> labelData;
 
 	public GoldLabel(Hashtable<String, String> labelData) {
 		this.labelData = labelData;
 	}
 	
 	public GoldLabel(String objectName, String correctCategory){
		this.labelData = new Hashtable<String, String>();
 		this.labelData.put("objectName", objectName);
 		this.labelData.put("correctCategory", correctCategory);
 	}
 
 	@Override
 	public String toString() {
 		return this.labelData.toString();
 	}
 	
 
 
 	
 	
 
 }
