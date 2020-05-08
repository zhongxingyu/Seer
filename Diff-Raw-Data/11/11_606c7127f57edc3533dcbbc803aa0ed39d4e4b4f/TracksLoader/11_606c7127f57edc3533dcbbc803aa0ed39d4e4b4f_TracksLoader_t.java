 package ru.noisefm.orders.loader;
 
 import android.content.Context;
 import android.support.v4.content.AsyncTaskLoader;
 import ru.noisefm.orders.order.OrdersTable;
 import ru.noisefm.orders.order.Track;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class TracksLoader extends AsyncTaskLoader<ArrayList<Track>> {
     public static final int LOADER_ID = 0;
     private String query;
     private int page;
 
     public TracksLoader(Context context) {
         super(context);
     }
 
     @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
     public ArrayList<Track> loadInBackground() {
         try {
             return OrdersTable.getTrackList(query, page);
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     public void setQuery(String query) {
         this.query = query;
     }
 
     public void setPage(int page) {
         this.page = page;
     }
 }
