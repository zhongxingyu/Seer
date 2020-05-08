 package cc.rainwave.android.api.types;
 
 import java.lang.reflect.Field;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 public class RainwaveResponse implements Parcelable {
 	private static final String TAG = "RainwaveResponse";
 	
     private Event sched_current;
     
     private Event sched_history[], sched_next[];
     
     private Song requests_all[], requests_user[];
     
     public Album[] playlist_all_albums;
     
     public Artist[] artist_list;
     
     public Artist artist_detail;
     
     public Album playlist_album;
     
     /** API returns these members paired to an error object if there is a problem */
     public GenericResult
     	error,
     	request_result,
     	request_delete_return,
     	request_reorder_return,
     	vote_result,
     	rate_result;
     
     private User user;
     
     private Station stations[];
     
     private RainwaveResponse(Parcel source) {
         sched_current = source.readParcelable(Event.class.getClassLoader());
         
         Parcelable history[] = source.readParcelableArray(Event.class.getClassLoader());
         Parcelable next[] = source.readParcelableArray(Event.class.getClassLoader());
         Parcelable tmpStations[] = source.readParcelableArray(Station.class.getClassLoader());
         
         sched_history = new Event[history.length];
         sched_next = new Event[next.length];
         stations = new Station[tmpStations.length];
         
         for(int i = 0; i < history.length; i++) { sched_history[i] = (Event) history[i]; }
         for(int i = 0; i < next.length; i++) { sched_next[i] = (Event) next[i]; }
         for(int i = 0; i < tmpStations.length; i++) { stations[i] = (Station) tmpStations[i]; }
     }
     
     public RainwaveResponse() { }
 
     public Song getCurrentSong() {
         return sched_current.songs[0];
     }
     
     public Event getCurrentEvent() {
     	return sched_current;
     }
     
     public Song[] getElection() {
     	return sched_next[0].songs;
     }
     
     public int getPastVote() {
     	return vote_result.elec_entry_id;
     }
     
     public boolean hasVoteResult() {
     	return vote_result != null;
     }
     
     public boolean hasError() {
         return error != null;
     }
     
     public boolean isTunedIn() {
     	return user != null && user.tuned_in;
     }
     
     public GenericResult getError() {
         return error;
     }
     
     public GenericResult getVoteResult() {
     	return vote_result;
     }
     
     public GenericResult getRateResult() {
         return rate_result;
     }
     
     public Song[] getRequests() {
     	return requests_user;
     }
     
     public Station getStation(int stationId) {
     	if(stations == null) {
     		return null;
     	}
     	for(int i = 0; i < stations.length; i++) {
     		if(stationId == stations[i].id) return stations[i];
     	}
     	return null;
     }
     
     public Station[] getStations() {
     	return stations;
     }
     
     public String getStationName(int id) {
     	Station s = getStation(id);
     	return (s == null) ? null : s.name;
     }
     
     public void setStations(Station[] newStations) {
     	stations = newStations;
     }
     
     public void receiveUpdates(RainwaveResponse other) {
     	for(Field f : this.getClass().getDeclaredFields()) {
     		try {
 				Object mine = f.get(this);
 				Object theirs = f.get(other);
 				f.set(this, update(theirs,mine));
 			} catch (IllegalArgumentException e) {
 				Log.w(TAG, "Class mismatch while updating: " + f.getName());
 			} catch (IllegalAccessException e) {
 				Log.w(TAG, "Couldn't access field: " + f.getName());
 			}
     	}
     }
     
     private Object update(Object newGuy, Object current) {
     	if(newGuy == null) return current;
     	return newGuy;
     }
     
     public void updateSongRatings(GenericResult result) {
         Song s = getCurrentSong();
         final Album album = s.albums[0];
        s.rating_user = result.song_rating;
        album.rating_user = result.album_rating;
     }
     
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeParcelable(sched_current, flags);
         dest.writeParcelableArray(sched_history, flags);
         dest.writeParcelableArray(sched_next, flags);
         dest.writeParcelableArray(stations, flags);
     }
     
     public static final Parcelable.Creator<RainwaveResponse> CREATOR
     = new Parcelable.Creator<RainwaveResponse>() {
         @Override
         public RainwaveResponse createFromParcel(Parcel source) {
             return new RainwaveResponse(source);
         }
 
         @Override
         public RainwaveResponse[] newArray(int size) {
             return new RainwaveResponse[size];
         }
     };
 }
