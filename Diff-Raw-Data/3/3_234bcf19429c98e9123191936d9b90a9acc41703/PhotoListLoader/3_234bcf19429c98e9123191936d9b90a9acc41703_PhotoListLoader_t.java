 package au.com.dius.resilience.loader;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
import android.util.Log;
 import au.com.dius.resilience.intent.Intents;
 import au.com.dius.resilience.model.Photo;
 import au.com.dius.resilience.observer.IntentBasedLoaderNotifierBroadcastReceiver;
 import au.com.dius.resilience.persistence.repository.Repository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PhotoListLoader extends AbstractAsyncListLoader<Photo> {
 
 private static final String LOG_TAG = PhotoListLoader.class.getName();
 
   private String incidentId;
   public static int PHOTO_LIST_LOADER = 1;
 
   public PhotoListLoader(Context context, Repository repository, String incidentId) {
     super(context, repository);
     this.incidentId = incidentId;
   }
 
   @Override
   protected BroadcastReceiver createBroadcastReceiver() {
     return new IntentBasedLoaderNotifierBroadcastReceiver(this, new IntentFilter(Intents.RESILIENCE_PHOTO_LOADED));
   }
 
   @Override
   public List<Photo> loadInBackground() {
     List<Photo> photos = new ArrayList<Photo>(1);
 
     final Photo photo = repository.findPhotoByIncident(incidentId);
     if (photo != null) {
       photos.add(photo);
     }
 
     Log.d(LOG_TAG, "Load in background complete.  sending back " + photos.size() + " photos");
 
     return photos;
   }
 }
