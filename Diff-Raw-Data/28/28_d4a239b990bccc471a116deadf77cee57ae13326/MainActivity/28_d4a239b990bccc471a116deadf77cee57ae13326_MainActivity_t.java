 package fr.nf28.vmote;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 
import org.apache.http.conn.util.InetAddressUtils;

 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
 import com.actionbarsherlock.view.SubMenu;
 
 import fr.nf28.vmote.R;
 import fr.nf28.vmote.history.view.HistoryViewPagerFragment;
 import fr.nf28.vmote.interfaces.OnChangePageListener;
 import fr.nf28.vmote.play.view.ViewPagerFragment;
 import fr.nf28.vmote.series.view.SeriesHomeFragment;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.Html;
import android.text.format.Formatter;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends SherlockFragmentActivity implements OnChangePageListener {
 	private boolean useLogo = false;
 	public boolean isConnected = false;
 	private boolean showHomeUp = false;
 	private final static String TAG_FRAGMENT = "TAG_FRAGMENT";
 	private Activity cxt;
 	private PopupWindow popUp;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main_layout);
 		cxt = this;
 		
 		AbstractFragment fragment = new ViewPagerFragment();
 		Bundle arguments = new Bundle();
 		arguments.putString(ViewPagerFragment.ARG_ITEM_ID, "pager_play_fragment");
         fragment.setArguments(arguments);
         cxt.setTitle("Lecture");
 		getSupportFragmentManager().beginTransaction()
 			.replace(R.id.applicationview_detail_container, fragment, TAG_FRAGMENT)
 			.addToBackStack(null)
 			.commit();
 	}
 
 
 	@Override
 	public void selectPage(int page) {
 		// useless..
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu) {
 		//Used to put dark icons on light action bar
 		//boolean isLight = SampleList.THEME == R.style.Theme_Sherlock_Light;
 		
 		menu.add("Lecture")
 		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				AbstractFragment fragment = new ViewPagerFragment();
 				Bundle arguments = new Bundle();
 				arguments.putString(ViewPagerFragment.ARG_ITEM_ID, "pager_play_fragment");
 		        fragment.setArguments(arguments);
 		        cxt.setTitle("Lecture");
 				getSupportFragmentManager().beginTransaction()
 					.replace(R.id.applicationview_detail_container, fragment, TAG_FRAGMENT)
 					.addToBackStack(null)
 					.commit();
 				
 				activateMenuButton(menu, 0);
 				return true;
 			}
 		})
 		.setIcon(R.drawable.vmoteplay)
 		//.setActionView(R.layout.tvseries_menu)
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 
 		menu.add("Historique")
 		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				AbstractFragment fragment = new HistoryViewPagerFragment();
 				Bundle arguments = new Bundle();
 				arguments.putString(HistoryViewPagerFragment.ARG_ITEM_ID, "pager_history_fragment");
 		        fragment.setArguments(arguments);
 		        cxt.setTitle("Historique");
 				getSupportFragmentManager().beginTransaction()
 					.replace(R.id.applicationview_detail_container, fragment, TAG_FRAGMENT)
 					.addToBackStack(null)
 					.commit();
 				
 				activateMenuButton(menu, 1);
 				return true;
 			}
 		})
 		.setIcon(R.drawable.vmotehistory)
 		//.setActionView(R.layout.tvseries_menu)
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW); //SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
 		menu.add("Series")
 		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				AbstractFragment fragment = new SeriesHomeFragment();
 				Bundle arguments = new Bundle();
 				arguments.putString(SeriesHomeFragment.ARG_ITEM_ID, "serie_home_fragment");
 		        fragment.setArguments(arguments);
 		    	cxt.setTitle("Series");
 				getSupportFragmentManager().beginTransaction()
 					.replace(R.id.applicationview_detail_container, fragment, TAG_FRAGMENT)
 					.addToBackStack(null)
 					.commit();
 				
 				activateMenuButton(menu, 2);
 				return true;
 			}
 			
 		})
 		.setIcon(R.drawable.vmotetvshow)
 		//.setActionView(R.layout.tvseries_menu)
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW); //SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		
 		
 		// menu parametres
 		SubMenu param = menu.addSubMenu("Parametres");
 		param.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
 		param.add(R.string.ipCheck);
 		param.add(R.string.helpVLC);
 		param.add(R.string.helpAndroid);
 		param.add(R.string.information);
 
 	    MenuItem subMenuItem = param.getItem();
 		
 	    param.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				showIPView();
 				return false;
 			}
 		});
 	    
 	    // aide VLC
 	    param.getItem(1).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				showHelpView(true);
 				return false;
 			}
 		});
 	    
 	    // aide android
 	    param.getItem(2).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				showHelpView(false);
 				return false;
 			}
 		});
 	    
 	    // info
 	    param.getItem(3).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				showInfoView();
 				return false;
 			}
 		});
 		
 		subMenuItem
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 		
 		activateMenuButton(menu, 0);
 		return true;
 	}
 	
 	private void activateMenuButton(Menu menu, int index){
 		switch(index){
 			case 0:
 				menu.getItem(0).setIcon(R.drawable.vmoteplayon);
 				menu.getItem(1).setIcon(R.drawable.vmotehistory);
 				menu.getItem(2).setIcon(R.drawable.vmotetvshow);
 				break;
 			
 			case 1:
 				menu.getItem(0).setIcon(R.drawable.vmoteplay);
 				menu.getItem(1).setIcon(R.drawable.vmotehistoryon);
 				menu.getItem(2).setIcon(R.drawable.vmotetvshow);
 				break;
 				
 			case 2:
 				menu.getItem(0).setIcon(R.drawable.vmoteplay);
 				menu.getItem(1).setIcon(R.drawable.vmotehistory);
 				menu.getItem(2).setIcon(R.drawable.vmotetvshowon);
 				break;
 			
 			default:
 			break;
 		}
 	
 	}
 	
 	private void showHelpView(boolean isDesktop) {
 		View internalLayout;
         TextView tv;
         Button btnOk;
 		
 		//We need to get the instance of the LayoutInflater, use the context of this activity
 		LayoutInflater inflater = (LayoutInflater) MainActivity.this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		//Inflate the view from a predefined XML layout
 		if(isDesktop) {
 			internalLayout = inflater.inflate(R.layout.settings_help_layout_desktop,
 					(ViewGroup) findViewById(R.id.settings_help_layout_desktop));
 		}
 		else {
 			internalLayout = inflater.inflate(R.layout.settings_help_layout_android,
 					(ViewGroup) findViewById(R.id.settings_help_layout_android));
 		}
 		
         // create a 500px width and 600px height PopupWindow
 		popUp = new PopupWindow(internalLayout, 500, 600, true);
 		
 		// display the popup in the center
         popUp.showAtLocation(internalLayout, Gravity.CENTER, 0, 0);
 		
         if (isDesktop) {
         	tv = (TextView) internalLayout.findViewById(R.id.tvHelpDesk);
         	btnOk = (Button) internalLayout.findViewById(R.id.btnHelpDeskBack);
 			tv.setText(Html.fromHtml(getString(R.string.tvHelpTextDesktop)));
 		}
         else {
 			tv = (TextView) internalLayout.findViewById(R.id.tvHelpAnd);
         	btnOk = (Button) internalLayout.findViewById(R.id.btnHelpAndroidBack);
 
 			tv.setText(Html.fromHtml(getString(R.string.tvHelpTextAndroid)));
         }
         
         btnOk.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				popUp.dismiss();
 			}
 		});
 	}
 	
 	private void showInfoView() {
 		View internalLayout;
         TextView tv;
         Button btnOk;
 		
 		//We need to get the instance of the LayoutInflater, use the context of this activity
 		LayoutInflater inflater = (LayoutInflater) MainActivity.this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		//Inflate the view from a predefined XML layout
 		internalLayout = inflater.inflate(R.layout.settings_information,
 					(ViewGroup) findViewById(R.id.settings_info_layout));
 
 		
         // create a 500px width and 570px height PopupWindow
 		popUp = new PopupWindow(internalLayout, 500, 570, true);
 		
 		// display the popup in the center
         popUp.showAtLocation(internalLayout, Gravity.CENTER, 0, 0);
 
        	tv = (TextView) internalLayout.findViewById(R.id.tvInfoIp);
        	btnOk = (Button) internalLayout.findViewById(R.id.btnInfoBack);
 
        	tv.setText(getLocalIpAddress());
        	btnOk.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				popUp.dismiss();
 			}
 		});
 	}
 	
 	private void showIPView() {
 		Toast.makeText(cxt, "IP", Toast.LENGTH_SHORT).show();
 	}
 	
	private String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
	        		en.hasMoreElements();)
 	        {
 	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
	            		enumIpAddr.hasMoreElements();)
 	            {
 	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()
	                		&& InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) 
 	                {
 	                    return inetAddress.getHostAddress().toString();
 	                }
 	            }
 	        }
	    }
	    catch (SocketException ex) {
 	        Log.e("IP", ex.toString());
 	    }
 	    return "";
 	}
 }
