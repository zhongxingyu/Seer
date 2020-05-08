 package dal;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class Hashtag {
 	private int id;
 	private String hashtag;
 	
	private Set<PictureData> pictures;
 	
 	public Hashtag(String hashtag){
 		this.hashtag = hashtag;
		pictures = new HashSet<PictureData>();
 	}

	public Set<PictureData> getPictures(){
		return pictures;
 	}
 
	public void setPictures(Set<PictureData> pics){
		this.pictures = pics;
 	}
 	
 	public void addPicToHashtag(PictureData pd){
		pictures.add(pd);
 		pd.getHashtags().add(this);
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getHashtag() {
 		return hashtag;
 	}
 
 	public void setHashtag(String hashtag) {
 		this.hashtag = hashtag;
 	}
 
 	@Override
 	public int hashCode() {
 		return hashtag.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(o instanceof Hashtag){
 			Hashtag h = (Hashtag)o;
 			if(h.hashtag.equals(hashtag)) return true;
 		}
 		return false;
 	}
 	
 }
