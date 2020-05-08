 package be.norio.twunch.android.data;
 
 import android.content.Context;
 import android.text.format.DateUtils;
 
 import com.squareup.otto.Produce;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import be.norio.twunch.android.data.model.Twunch;
 import be.norio.twunch.android.data.model.Twunches;
 import be.norio.twunch.android.otto.BusProvider;
 import be.norio.twunch.android.otto.TwunchesAvailableEvent;
 import be.norio.twunch.android.util.PrefsUtils;
 import retrofit.Callback;
 import retrofit.RestAdapter;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 import retrofit.converter.SimpleXMLConverter;
 import retrofit.http.GET;
 
 public class DataManager {
 
     private static DataManager instance = null;
     private final Context mContext;
     private final TwunchServer mServer;
     private final TwunchData mTwunchData;
 
     public static DataManager getInstance() {
         if(instance == null) {
             instance = new DataManager();
         }
         return instance;
     }
     protected DataManager() {
         mContext = PrefsUtils.getContext();
         mServer = new RestAdapter.Builder().setServer("http://twunch.be/").setConverter(new SimpleXMLConverter()).build().create(TwunchServer.class);
         if (PrefsUtils.isDataAvailable()) {
             mTwunchData = PrefsUtils.getData();
         } else {
             mTwunchData = new TwunchData();
             loadTwunches(true);
         }
     }
 
     public Twunch getTwunch(String id) {
         final List<Twunch> twunches = mTwunchData.getTwunches();
         for (int i = 0; i < twunches.size(); i++) {
             final Twunch twunch =  twunches.get(i);
             if(id.equals(twunch.getId())) {
                 return twunch;
             }
         }
         return null;
     }
 
     public List<Twunch> getTwunches() {
         return new ArrayList<Twunch>(mTwunchData.getTwunches());
     }
 
     public void loadTwunches(boolean force) {
         long lastSync = mTwunchData.getTimestamp();
         long now = (new Date()).getTime();
         if (!force && lastSync != 0 && (now - lastSync < DateUtils.HOUR_IN_MILLIS)) {
             return;
         }
         mServer.loadTwunches(new Callback<Twunches>() {
             @Override
             public void success(Twunches twunches, Response response) {
                 mTwunchData.setTwunches(twunches.twunches);
                 PrefsUtils.setData(mTwunchData);
                 BusProvider.getInstance().post(new TwunchesAvailableEvent(mTwunchData.getTwunches()));
             }
 
             @Override
             public void failure(RetrofitError retrofitError) {
                 // FIXME: Show error in the UI
                 retrofitError.printStackTrace();
             }
         });
     }
 
     @Produce
     public TwunchesAvailableEvent produceTwunches() {
         return new TwunchesAvailableEvent(mTwunchData.getTwunches());
     }
 
     interface TwunchServer {
         @GET("/events.xml?when=future")
         void loadTwunches(Callback<Twunches> callback);
     }
 
 }
