 package pro.trousev.cleer.android.userInterface;
 
 import java.util.List;
 
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Messaging.Message;
 import pro.trousev.cleer.Player.Reason;
 import pro.trousev.cleer.Queue;
 import pro.trousev.cleer.Queue.EnqueueMode;
 import pro.trousev.cleer.Playlist;
 import pro.trousev.cleer.android.Constants;
 import pro.trousev.cleer.android.service.AndroidCleerService;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class MainActivity extends FragmentActivity implements OnClickListener {
 	private ServiceConnection serviceConnection;
 	public static AndroidCleerService service;
 	private boolean serviceIsBound;
 	private Intent serviceIntent;
 	SwipePageAdapter main_pager_adapter;
 	ViewPager main_pager;
 	Menu _main_menu;
 	public static MainActivity singletone = null;
 	
 	private Messaging.Event _menu_updater = new Messaging.Event() {
 		@Override
 		public void messageReceived(Message message) {
 			MenuItem m = (MenuItem)  _main_menu.findItem(R.id.menu_scan_system);
 			m.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 				@Override
 				public boolean onMenuItemClick(MenuItem item) {
 					on_scanSystem_click(item);
 					return true;
 				}
 			});
			
			((MenuItem) _main_menu.findItem(R.id.menu_exit_program)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					exit();
					return true;
				}
			});
 		}
 	};
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		singletone = this;
 		setContentView(R.layout.main);
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
 		
 		serviceIntent = new Intent(
 				"pro.trousev.cleer.android.service.AndroidCleerService");
 
 		serviceConnection = new ServiceConnection() {
 			@Override
 			public void onServiceConnected(ComponentName name, IBinder binder) {
 				service = ((AndroidCleerService.CleerBinder) binder)
 						.getService();
 				serviceIsBound = true;
 				
 				postInit();
 			}
 
 			@Override
 			public void onServiceDisconnected(ComponentName name) {
 				serviceIsBound = false;
 			}
 		};
 		startService(serviceIntent);
 		bindService(serviceIntent, serviceConnection, 0);
 	}
 
 
 	/// This is initialized when Library is initialized
 	public void postInit()
 	{
 		main_pager_adapter = new SwipePageAdapter(getSupportFragmentManager());
 		
 		main_pager_adapter.addPage(new QueueView(main_pager_adapter));
 		main_pager_adapter.addPage(new PlaylistsView(main_pager_adapter));
         
 		// Set up the ViewPager with the sections adapter.
 		main_pager = (ViewPager) findViewById(R.id.pager);
 		System.out.println("[cleer] Pager: "+main_pager);
 		System.out.println("[cleer] Adapter: "+main_pager_adapter);
 		main_pager.setAdapter(main_pager_adapter);
 		main_pager_adapter.setPager(main_pager);
 	}
 	public void onClick(View view) {
 		int id = view.getId();
 	}
 
 	public void exit() {
 		if (serviceIsBound)
 			unbindService(serviceConnection);
		
 		stopService(new Intent(this, AndroidCleerService.class));
 		serviceIsBound = false;
 		this.finish();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		//
 		//System.out.println("[cleer] Menu Created.");
 		//this.getMenuInflater().inflate(R.menu.main_option_menu, menu);
 		
 		boolean ans = super.onCreateOptionsMenu(menu);
 		menu.clear();
 		this.getMenuInflater().inflate(R.menu.main_option_menu, menu);
 		_main_menu = menu;
 		main_pager_adapter.setMenu(_main_menu);
 		Messaging.subscribe(SwipePageAdapter.SwipePageAdapterMenuChanged.class, _menu_updater);
 		Messaging.fire(new SwipePageAdapter.SwipePageAdapterMenuChanged());
 		return ans;
 	}
 	public boolean on_scanSystem_click(MenuItem item)
 	{
 		System.out.println("[cleer] onScanClick");
 		LibraryUpdatePopup p = new LibraryUpdatePopup(R.layout.update_library_dialog, main_pager);
 		p.show();
 		p.update();
 		return true;
 	}
 	
 	@Override
 	public void onDestroy() {
 		if (serviceIsBound)
 			unbindService(serviceConnection);
 		Messaging.unSubscribe(SwipePageAdapter.SwipePageAdapterMenuChanged.class, _menu_updater);
 		main_pager_adapter.onDestroy();
 		Log.d(Constants.LOG_TAG, "MainActivity.onDestoy()");
 		System.runFinalization();
 		super.onDestroy();
 	}
 }
