 package fib.pec.hovione;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.UUID;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
 	
 	//////// Constants De La Classe ////////////////////////////////////////////////////////////////////////////////////////////	
 	private static final int REQUEST_ENABLE_BT = 1; //constant per identificar peticio de engegar bluetooth
 	
 	/////// Atributs De La Classe /////////////////////////////////////////////////////////////////////////////////	
 	private boolean bluetoothEnabled;
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	
 	private ViewPager mViewPager;//El ViewPager contindr el contingut de les seccions. ViewPager es un tipus de layout que permet
                          // desplasarnos entre pantalles(tambe anomenades pagines o seccions) lliscant el dit horitzontalment. Al viewpager
 	                     // se li ha de configurar un pagerAdapter que s'encarrega de crear les pagines que es mostrarn.
 	                     // Normalment les pagines del viewpager son fragments i es fa servir un adapter que tracta
 	                     // les pagines com a fragments anomenat FragmentPageAdapter.
 	
 	private SectionsPagerAdapter mSectionsPagerAdapter; //FragmentPageAdapter que gestiona la creacio de fragments(pagines) per al viewpager.
 
 	private BluetoothAdapter mBluetoothAdapter;//Adapter del Bluetooth
 	
 	private ArrayList<String> foundDevicesList;//TODO Implementar la llista de dispositius BT i la UI
 	
 	private int mStackLevel = 0;
 	
 	private ActionBar actionBar;
 	
 	private BluetoothManager bluetoothManager;
 	
 	
 	/////// Metodes /////////////////////////////////////////////////////////////////////////////////	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		//inicialitzacions de variables
 		actionBar = getActionBar();
 		mViewPager = (ViewPager) findViewById(R.id.pager); //obtenim el viewpager del arxiu de layout xml de aquesta activity
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
 					
 		configureActionBar();
 		configureViewPager();
 		
 		bluetoothManager = new BluetoothManager(this);
 		
 		foundDevicesList = new ArrayList<String>();
 		
 		if (bluetoothManager.isBluetoothSupported()) {
 			if (bluetoothManager.isBluetoothEnabled()) {
 				Set<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
 				if (pairedDevices.size() > 0) {
 			        for (BluetoothDevice device : pairedDevices) {
 			            // Add the name and address to an array adapter to show in a ListView
 			            foundDevicesList.add(device.getName() + "\n" + device.getAddress());
 			        }
 			    }
 				showBTDialogFragment();
 			}
 			else {
 				AlertDialogFragment dialogBtNoEnabled = AlertDialogFragment.newInstance(R.string.title_bluetooth_enable_petition,
                                                                                         "El bluetooth est desconnectat,\n" +
                                                                                         "vols que l'aplicaci l'engegui per a tu?");
                 dialogBtNoEnabled.show(getSupportFragmentManager(), "BtNoEnabled");
                 
 			}
 		}
 		else {
 			AlertDialogFragment dialogNoBtSupport = AlertDialogFragment.newInstance(R.string.title_warning, "Bluetooth No Soportat");
 			dialogNoBtSupport.show(getSupportFragmentManager(), "BtNoSupported");			
 		}
 		
 	}
 	
 	@Override
     protected void onDestroy() {
         super.onDestroy();
         //this.unregisterReceiver(mReceiver);
        if (bluetoothManager != null) bluetoothManager.endDiscovery(); 
     }
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    super.onActivityResult(requestCode, resultCode, data);
 	    
 	    if (requestCode == REQUEST_ENABLE_BT) {
 	    	if (resultCode == RESULT_OK) {
 				Set<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
 				if (pairedDevices.size() > 0) {
 			        for (BluetoothDevice device : pairedDevices) {
 			            // Add the name and address to an array adapter to show in a ListView
 			            foundDevicesList.add(device.getName() + "\n" + device.getAddress());
 			        }
 			    }
 				showBTDialogFragment();
 	    	}
 	    	else {
 				AlertDialogFragment dialogNoBtSupport = AlertDialogFragment.newInstance(R.string.title_warning, "No s'ha pogut engegar el bluetooth");
 				dialogNoBtSupport.show(getSupportFragmentManager(), "BtCouldntEnable");	
 	    	}
 	    }
 	   
 	}
 		
 	/**
 	 * ActionBar - Menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Infla el menu. Aix afegeix items a l'action bar.
 		getMenuInflater().inflate(R.menu.activity_main, menu); //R.menu.activity_main --> /res/menu/activity_main.xml
 		return true;
 	}
 	
 	
 	
 	private void configureActionBar() {
 		
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);//Volem que al action bar es mostrin les tabs
 	   
 		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
 	        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
 	    		// Quan la tab es seleccionada, canviem de secci al ViewPager.
 	    		mViewPager.setCurrentItem(tab.getPosition());	        		        	
 	        }
 
 	        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
 	        	
 	        }
 
 	        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
 	        	
 	        }
 	    };		
 	    
 		// Per a cada una de les seccions de l'aplicaci, s'afegeix un tab a l'action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Crea un tab amb el text corresponent al titol definit per l'adapter.
 			// Tamb especifica el TabListener per quan el Tab es seleccionat.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					//.setTabListener(this));
 					.setTabListener(tabListener));
 		}
 	}
 	
 	private void configureViewPager() {
 		
 		// Posar el ViewPager amb les seccions de l'adapter.
 		mViewPager.setAdapter(mSectionsPagerAdapter); //li configurem el adapter al viewpager
 
 		// Quan fem swipe entre diferents seccions, seleccionem la corresponent 
 		// tab.
 		mViewPager.setOnPageChangeListener(
 				new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});		
 	}
 	
 	public void close_application() {
 		finish();
 	}
 
 	//*****************************BLUETOOTH**************************************************
 	
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			//Quan el discovery troba un dispositiu
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				// Agafa l'objecte BluetoothDevice del Intent
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
 					// Agafa el nom i l'addrea i el posa en un array adapter per mostrar-lo en un ListView
 					foundDevicesList.add(device.getName() + "\n" + device.getAddress());
 					android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("bluetooth_list_fragment");
 					((BTDialogFragment) prev).notifyListHasToUpdate();
 				}
 			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                 //Aturar la progress bar
                 
             }			
 		}
 	};
 	
 	public void beginDiscovery() {
 		bluetoothManager.performDiscovery();
 	}
 	
 	public BroadcastReceiver getDiscoveryReceiver() {
 		return mReceiver;
 	}
 	
 	public ArrayList<String> getFoundDevices() {
 		return foundDevicesList;
 	}
 	public void pressed_Ok_EnableBluetooth() {
 		bluetoothManager.enableBluetooth();	
 	}
 	
 	public void showBTDialogFragment() {
 		++mStackLevel;
 		android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		Fragment prev = getSupportFragmentManager().findFragmentByTag("bluetooth_list_fragment");
 		if (prev != null) {
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 		
 		BTDialogFragment deviceListFragment = BTDialogFragment.newInstance(mStackLevel);
 		deviceListFragment.show(ft, "bluetooth_list_fragment");
 	}
 }
