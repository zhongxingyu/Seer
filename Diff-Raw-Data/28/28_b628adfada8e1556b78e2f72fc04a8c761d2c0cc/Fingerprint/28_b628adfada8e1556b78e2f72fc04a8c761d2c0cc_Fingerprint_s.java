 package ca.idrc.tagin.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 @Entity
 public class Fingerprint implements Comparable<Fingerprint> {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id;
 	private Pattern mPattern;
 	private Double mRank;
 	
 	public Fingerprint() {
 		
 	}
 	
	public Long getId() {
		return id;
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
