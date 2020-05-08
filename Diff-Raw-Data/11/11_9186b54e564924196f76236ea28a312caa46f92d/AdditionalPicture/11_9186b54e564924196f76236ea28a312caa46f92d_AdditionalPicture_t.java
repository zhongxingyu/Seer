 package de.rallye.model.structures;
 
 public class AdditionalPicture extends AdditionalResource {
 	
 	public static final String PICTURE_ID = "pictureID";
 
 	final public int pictureID;
 	
 	public AdditionalPicture(int pictureID) {
 		this.pictureID = pictureID;
 	}
 	
 	@Override
 	public String toString() {
 		return "picID:"+ pictureID;
 	}
 	
 	public static AdditionalPicture fromString(String s) {
		String pic = s.replaceAll("^picID:(\\d+)$", "$1");
		return new AdditionalPicture(Integer.parseInt(pic));
 	}
 }
