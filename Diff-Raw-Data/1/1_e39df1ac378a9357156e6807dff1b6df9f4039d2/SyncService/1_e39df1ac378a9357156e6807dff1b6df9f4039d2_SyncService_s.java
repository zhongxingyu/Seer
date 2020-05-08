 package com.glutamatt.velibgo.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 import com.glutamatt.velibgo.io.Network;
 import com.glutamatt.velibgo.models.Station;
 import com.glutamatt.velibgo.providers.StationsProvider;
 import com.glutamatt.velibgo.storage.DaoStation;
 import com.glutamatt.velibgo.storage.DatabaseOpenHelper;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.IBinder;
 
 public class SyncService extends Service{
 
 	private IBinder mBinder = new SyncBinder();
 	public class SyncBinder extends Binder
 	{
 		public SyncService getService()
 		{
 			return SyncService.this;
 		}
 	}
 	
 	List<ISyncServerListener> listeners = new ArrayList<SyncService.ISyncServerListener>();
 	public interface ISyncServerListener
 	{
 		public void onUpdateStart();
 		public void onStationsUpdated(List<Station> stations);
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 	
 	public void addListener(ISyncServerListener listener)
 	{
 		listeners.add(listener);
 	}
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 	}
 
 	public void pullFreshData() {
 		class Refresh extends AsyncTask<Void, Void, List<Station>>
 		{
 			@Override
 			protected List<Station> doInBackground(Void... params) {
 				Network network = new Network(getApplicationContext());
 				if(network.checkNetwork())
 				{
 					StationsProvider provider = new StationsProvider(network);
 					List<Station> stations = provider.getAllStations();
 					return stations;
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(List<Station> stations) {
 				for (ISyncServerListener listener : listeners) {
 					listener.onStationsUpdated(stations);
 				}
 				persistStations(stations);
 				super.onPostExecute(stations);
 			}
 		}
 		for (ISyncServerListener listener : listeners) {
 			listener.onUpdateStart();
 		}
 		new Refresh().execute();
 	}
 	
 	private void persistStations(final List<Station> stations) {
 		class PersistStations extends AsyncTask<Void, Void, Void>
 		{
 			@Override
 			protected Void doInBackground(Void... params) {
 				DaoStation dao = DaoStation.getInstance(getApplicationContext());
 				for(Station station : stations)
 				{
 					if(null == dao.find(station.getId()))
 							dao.save(station);
 				}
 				return null;
 			}
 		}
 		new PersistStations().execute();
 	}
 	
 	@Override
 	public void onDestroy() {
 		DatabaseOpenHelper.getInstance(getApplicationContext()).close();
 		super.onDestroy();
 	}
 
 }
