 package mlproject.models;
 
 import java.util.Date;
 
 public class Issue{
 	public String mImagePath;
 	
	public Double sales, expectedSales;
 	public Long Issue;
 	public Date date;
 	public String heading;
 	public Boolean earthScience, astronomyAndCosmology, physics, technology, neuroSciencePsychology;
 	public Boolean otherBiology, otherTopic, abstractImage, photoImage, typographicImage;
 	public Boolean hasSpecial, isSpecialEdition, isAnniverseryEdition;
 	
 	private void extractImageFeatures(String image){
 		//TODO: Matthew
 	}	
 }
