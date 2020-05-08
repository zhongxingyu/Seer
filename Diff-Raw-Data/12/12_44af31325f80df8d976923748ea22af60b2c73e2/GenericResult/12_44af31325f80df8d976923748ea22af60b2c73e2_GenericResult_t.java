 package cc.rainwave.android.api.types;
 
 public class GenericResult {
 	/** Result code returned with many actions */
     public boolean success;
     
     /** Message associated with result for many actions */
     public String text;
     
     /** The election ID voted for. Returned after a successful election vote. */
     public int elec_entry_id;
     
     /** Song/album ID. Returned after a rating is submitted. */
     public int song_id, album_id;
     
     /** Song/album rating. Returned after a rating is submitted. */
    public float rating_user;
 }
