 package cc.rainwave.android.api.types;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class Song implements Parcelable, Comparable<Song> {
 	public long song_releasetime;
 	
	public int id, entry_id, elec_isrequest, requestq_id, song_secondslong;
 	public String title;
 	public Artist artists[];
 	public Album albums[];
 	
 	public String song_requestor;
 	public float rating_user, rating;
 	
 	private Song(Parcel in) {
 		id = in.readInt();
		entry_id = in.readInt();
 		elec_isrequest = in.readInt();
 	    title = in.readString();
 	    Parcelable tmpArtists[] = in.readParcelableArray(Artist[].class.getClassLoader());
 	    Parcelable tmpAlbums[] = in.readParcelableArray(Album[].class.getClassLoader());
 	    song_requestor = in.readString();
 	    
 	    artists = new Artist[tmpArtists.length];
 	    for(int i = 0; i < tmpArtists.length; i++) {
 	        artists[i] = (Artist) tmpArtists[i];
 	    }
 	    
 	    albums = new Album[tmpAlbums.length];
 	    for(int i = 0; i < tmpAlbums.length; i++) {
 	    	albums[i] = (Album) tmpAlbums[i];
 	    }
 	}
 	
 	public boolean isRequest() {
 		switch(elec_isrequest) {
 		case ELEC_FULFILLED_REQUEST:
 		case ELEC_RANDOM_REQUEST:
 			return true;
 		}
 		return false;
 	}
 	
 	public String collapseArtists() {
 	    return collapseArtists(", ", " & ");
 	}
 	
 	public String collapseArtists(String comma, String and) {
 		if(artists == null) return "???";
 	    switch(artists.length) {
 	        case 0: return "???";
 	        case 1: return artists[0].name;
 	        case 2: return artists[0].name + " " + and + " " + artists[1].name;
 	        default:
 	            StringBuilder sb = new StringBuilder();
 	            for(int i = 0; i < artists.length; i++) {
 	                sb.append(artists[i].name);
 	                
 	                if(i < artists.length - 2) {
 	                    sb.append(comma);
 	                }
 	                else if(i == artists.length - 2) {
 	                    sb.append(and);
 	                }
 	            }
 	            return sb.toString();
 	    }
 	}
 	
 	public String toString() {
 		return title;
 	}
 	
 	public String getLengthString() {
 		int m = song_secondslong / 60;
 		int s = song_secondslong % 60;
 		return String.format("%d:%02d",m,s);
 	}
 	
 	public boolean isCooling() {
 		long utc = System.currentTimeMillis() / 1000;
 		return song_releasetime > utc;
 	}
 	
 	public long getCooldown() {
 		long utc = System.currentTimeMillis() / 1000;
 		return song_releasetime - utc;
 	}
 	
 	@Override
 	public int compareTo(Song s) {
 		final String album_name = albums[0].name;
 		final String other_album_name = s.albums[0].name;
 		if(album_name != null && other_album_name != null && !album_name.equals(other_album_name)) {
 			return album_name.compareTo(other_album_name);
 		}
 		return title.compareTo(s.title);
 	}
 	
     @Override
     public int describeContents() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
     	dest.writeInt(id);
    	dest.writeInt(entry_id);
     	dest.writeInt(elec_isrequest);
         dest.writeString(title);
         dest.writeParcelableArray(artists, flags);
         dest.writeParcelableArray(albums, flags);
         dest.writeString(song_requestor);
     }
     
     public static final Parcelable.Creator<Song> CREATOR
     = new Parcelable.Creator<Song>() {
         @Override
         public Song createFromParcel(Parcel source) {
             return new Song(source);
         }
 
         @Override
         public Song[] newArray(int size) {
             return new Song[size];
         }
     };
     
     /** LiquidRain's definition for the field 'elec_isrequest' */
     public static final int
     	ELEC_FULFILLED_REQUEST = 4,
     	ELEC_RANDOM_REQUEST = 3,
     	ELEC_NORMAL = 2,
     	ELEC_CONFLICT = 0;
 
 }
