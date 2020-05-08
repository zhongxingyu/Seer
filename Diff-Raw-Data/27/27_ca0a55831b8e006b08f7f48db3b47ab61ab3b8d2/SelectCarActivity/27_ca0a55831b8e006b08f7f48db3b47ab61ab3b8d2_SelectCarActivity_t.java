 package at.fhv.audioracer.client.android.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 import at.fhv.audioracer.client.android.R;
 import at.fhv.audioracer.client.android.activity.listener.IFreeCarsListener;
 import at.fhv.audioracer.client.android.activity.listener.ISelectCarListener;
 import at.fhv.audioracer.client.android.controller.ClientManager;
 import at.fhv.audioracer.client.android.info.CarInfo;
 import at.fhv.audioracer.client.android.network.task.FreeCarsAsyncTask;
 import at.fhv.audioracer.client.android.network.task.SelectFreeCarAsyncTask;
 import at.fhv.audioracer.client.android.network.task.SelectFreeCarAsyncTask.SuccessMessage;
 import at.fhv.audioracer.client.android.network.task.params.NetworkParams;
 import at.fhv.audioracer.client.android.network.task.params.SelectCarParams;
 import at.fhv.audioracer.client.player.IPlayerClientListener;
 import at.fhv.audioracer.client.player.PlayerClient;
 
 public class SelectCarActivity extends ListActivity implements IFreeCarsListener, ISelectCarListener {
 	
 	/**
 	 * Available cars.
 	 */
 	private List<HashMap<String, String>> _cars = new ArrayList<HashMap<String, String>>();
 	private SimpleAdapter _carsListAdapter = null;
 	
 	private IPlayerClientListener _playerClientListener;
 	
 	private PlayerClient _playerClient = ClientManager.getInstance().getPlayerClient();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_select_car);
 		
 		_carsListAdapter = new SimpleAdapter(this, // context
 				_cars, // data
 				android.R.layout.two_line_list_item, // resource
 				new String[] { CarInfo.NAME, CarInfo.ID }, // Array of cursor columns to bind to.
 				new int[] { android.R.id.text1, android.R.id.text2 } // Parallel array of which template objects to bind to those columns.
 		);
 		setListAdapter(_carsListAdapter);
 		
 		_playerClientListener = new IPlayerClientListener.Adapter() {
 			@Override
 			public void onUpdateFreeCars() {
				Log.d(ACTIVITY_SERVICE, "called onUpdateFreeCars(); # of free cars:" + _playerClient.getFreeCarIds().length);
 				
 				final FreeCarsAsyncTask task = new FreeCarsAsyncTask(SelectCarActivity.this);
 				task.execute(new NetworkParams());
 			}
 			
 		};
 		_playerClient.getListenerList().add(_playerClientListener);
		addFreeCars(_playerClient.getFreeCarIds());
 		
 		ListView carsListView = (ListView) findViewById(android.R.id.list);
 		carsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Log.d(ACTIVITY_SERVICE, "called onItemClick()");
 				@SuppressWarnings("unchecked")
 				HashMap<String, String> car = (HashMap<String, String>) parent.getItemAtPosition(position);
 				int carId = Integer.parseInt(car.get(CarInfo.ID));
 				SelectCarParams params = new SelectCarParams();
 				params.carId = (byte) carId;
 				
 				final SelectFreeCarAsyncTask task = new SelectFreeCarAsyncTask(SelectCarActivity.this);
 				task.execute(params);
 			}
 		});
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.select_car, menu);
 		return true;
 	}
 	
 	@Override
 	public void addFreeCars(byte[] freeCarIds) {
 		Log.d("addFreeCars", "called addCars()");
		_cars.clear();
 		if (freeCarIds == null) {
 			Log.d("addFreeCars", "freeCars was null");
		} else {
			Log.d("addFreeCars", "now there are " + freeCarIds.length + " cars free");
			for (int id : freeCarIds) {
				HashMap<String, String> carMap = new HashMap<String, String>();
				carMap.put(CarInfo.NAME, "Car " + id);
				carMap.put(CarInfo.ID, "" + id);
				_cars.add(carMap);
				Log.d(ACTIVITY_SERVICE, "Added car with id '" + id + "'");
			}
 		}
		_carsListAdapter.notifyDataSetChanged();
 	}
 	
 	@Override
 	public void notifySuccess(SuccessMessage message) {
 		if (message.success) {
 			// car was free, change to playGameActivity
 			Toast.makeText(getApplicationContext(), "Selected car " + message.carId + " :)", Toast.LENGTH_SHORT).show();
 			_playerClient.getListenerList().remove(_playerClientListener);
 			final Intent playGameIntent = new Intent(this, PlayGameActivity.class);
 			startActivity(playGameIntent);
 		} else {
 			Toast.makeText(getApplicationContext(), "Sorry, but car " + message.carId + " isn't available anymore.", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 }
