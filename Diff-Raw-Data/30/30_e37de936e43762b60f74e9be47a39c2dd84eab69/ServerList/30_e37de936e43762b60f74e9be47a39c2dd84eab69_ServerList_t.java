 package net.djmacgyver.bgt.socket;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.DataSetObserver;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.widget.ListAdapter;
 
 public abstract class ServerList implements ListAdapter {
 	private Context context;
 	private JSONArray data;
 	private ArrayList<DataSetObserver> observers = new ArrayList<DataSetObserver>();
 
 	private ServiceConnection conn = new ServiceConnection() {
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 		}
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			SocketService s = ((SocketService.LocalBinder) service).getService();
 			final SocketCommand command = new SocketCommand(getServerCommand());
 			command.addCallback(new Runnable() {
 				@Override
 				public void run() {
 					data = command.getResponseData();
 					fireChanged();
 				}
 			});
 			s.getSharedConnection().sendCommand(command);
 			context.unbindService(this);
 		}
 	};
 	
 	abstract protected String getServerCommand();
 
 	public ServerList(Context context){
 		this.context = context;
		//refresh();
	}
	
	public ServerList(Context context, boolean autoLoad)  {
		this(context);
		if (autoLoad) refresh();
 	}
 	
 	@Override
 	public int getCount() {
 		if (getData() == null) return 0;
 		return getData().length();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		try {
 			return getData().getJSONObject(arg0);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public long getItemId(int arg0) {
 		try {
 			return getData().getJSONObject(arg0).getInt("id");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return getCount() <= 0;
 	}
 
 	@Override
 	public void registerDataSetObserver(DataSetObserver arg0) {
 		observers.add(arg0);
 	}
 
 	@Override
 	public void unregisterDataSetObserver(DataSetObserver arg0) {
 		observers.remove(arg0);
 	}
 
 	@Override
 	public boolean areAllItemsEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isEnabled(int arg0) {
 		return true;
 	}
 
 	protected JSONArray getData() {
 		return data;
 	}
 
 	private void fireChanged() {
 		h.sendEmptyMessage(0);
 	}
 
 	private Handler h = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 			Iterator<DataSetObserver> i = observers.iterator();
 			while (i.hasNext()) i.next().onChanged();
 		}
 	};
 	
 	public Context getContext() {
 		return context;
 	}
 	
 	public void refresh() {
 		context.bindService(new Intent(context, SocketService.class), conn, Context.BIND_AUTO_CREATE);
 	}
 }
