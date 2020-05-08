 import java.util.Collection;
 
 import de.umass.lastfm.Tag;
 
 public class Lied {
 //zoiets?
 	String naam;
 	String artiest;
 	String album;
 	Collection<String> tag;
 	
 	public Lied(String naam, String artiest){
 		this.naam = naam;
 		this.artiest = artiest;
 		this.album = "";
 		//this.tag = null;
 	}
 	public Lied(String naam, String artiest, String album, Collection<String> tag){
 		this.naam = naam;
 		this.artiest = artiest;
 		this.album = album;
 		this.tag = tag;
 	}
 	public void setNaam(String naam){
 		this.naam = naam;
 	}
 	public String getNaam(){
 		return naam;
 		
 	}
 	public void setArtiest(String artiest){
 		this.artiest = artiest;
 	}
 	public String getArtiest(){
 		return artiest;
 	}
 	public void setAlbum(String album){
 		this.album = album;
 	}
 	public String getAlbum(){
 		return this.album;
 	}
 	public void setTag(Collection<String> tag){
 		this.tag = tag;
 	}
 	public Collection<String> getTag(){
 		return this.tag;
 	}
 	
	public boolean equals(Object obj)
 	{
		if(obj instanceof Lied)
		{
			Lied that = (Lied) obj;
			return that.getArtiest().equals(this.artiest) && that.getNaam().equals(this.naam);
		}
		return false;
		
 	}
 	
 	
 }
