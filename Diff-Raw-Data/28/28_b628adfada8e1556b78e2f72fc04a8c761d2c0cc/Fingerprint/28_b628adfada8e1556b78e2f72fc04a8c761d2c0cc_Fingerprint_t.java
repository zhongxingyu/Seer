 package ca.idrc.tagin.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
import com.google.appengine.api.datastore.Key;

 @Entity
 public class Fingerprint implements Comparable<Fingerprint> {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Key key;
 	private Pattern mPattern;
 	private Double mRank;
 	
 	public Fingerprint() {
 		
 	}
 	
	public Key getKey() {
		return key;
 	}
 
 	public Pattern getPattern() {
 		return mPattern;
 	}
 
 	public void setPattern(Pattern mPattern) {
 		this.mPattern = mPattern;
 	}
 
 	public Double getRank() {
 		return mRank;
 	}
 
 	public void setRank(Double mRank) {
 		this.mRank = mRank;
 	}
 	
 	@Override
 	public int compareTo(Fingerprint f) {
 		return 0;
 	}
 
 }
