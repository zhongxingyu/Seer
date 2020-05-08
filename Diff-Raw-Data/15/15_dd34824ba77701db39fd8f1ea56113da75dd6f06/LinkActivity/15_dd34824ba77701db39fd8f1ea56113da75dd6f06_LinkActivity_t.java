 package nl.q42.hue2.activities;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nl.q42.hue2.R;
 import nl.q42.hue2.Util;
 import nl.q42.hue2.adapters.BridgeAdapter;
 import nl.q42.hue2.models.Bridge;
 import nl.q42.javahueapi.HueService;
 import nl.q42.javahueapi.HueService.ApiException;
 import nl.q42.javahueapi.Networker;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 
 public class LinkActivity extends Activity {
 	private static final int SEARCH_TIMEOUT = 5000;
 	private static final int LINK_INTERVAL = 1000;
 	
 	private BridgeAdapter bridgesAdapter;
 	private ListView bridgesList;
 	
 	private ImageButton refreshButton;
 	private ProgressBar loadingSpinner;
 	
 	private BridgeSearchTask bridgeSearchTask;
 	private Timer linkChecker;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_link);
 		
 		// Set up layout
 		bridgesList = (ListView) findViewById(R.id.link_bridges);
 		bridgesAdapter = new BridgeAdapter(this);
 		bridgesList.setAdapter(bridgesAdapter);
 		bridgesList.setEmptyView(findViewById(R.id.link_empty));
 		
 		// Set up loading UI elements		
 		ActionBar ab = getActionBar();
 		ab.setCustomView(R.layout.link_loader);
 		ab.setDisplayShowCustomEnabled(true);
 		
 		RelativeLayout loadingLayout = (RelativeLayout) ab.getCustomView();
 
 		loadingSpinner = (ProgressBar) loadingLayout.findViewById(R.id.link_loading_spinner);
 		
 		refreshButton = (ImageButton) loadingLayout.findViewById(R.id.link_refresh_button);
 		refreshButton.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				startSearching();
 			}
 		});
 		
 		// Add connect event
 		bridgesList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
 				Bridge b = bridgesAdapter.getItem(pos);
 				
 				if (b.hasAccess()) {
 					// TODO: Instant connection
 				} else {
 					showLinkDialog(b);					
 				}
 			}
 		});
 		
 		// Start searching for bridges and add them to the results
 		startSearching();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		
 		// Stop any search or link operations
 		stopSearching();
 		linkChecker.cancel();
 	}
 	
 	private void stopSearching() {		
 		if (bridgeSearchTask != null) {
 			bridgeSearchTask.cancel(true);
 			bridgeSearchTask = null;
 			
 			bridgesList.post(new Runnable() {
 				@Override
 				public void run() {
 					setSearchIndicator(false);
 				}
 			});
 		}
 	}
 	
 	private void startSearching() {		
 		stopSearching();
 		
 		bridgesAdapter.clear();
 		
 		bridgeSearchTask = new BridgeSearchTask();
 		bridgeSearchTask.execute();
 		
 		setSearchIndicator(true);
 	}
 	
 	private void setSearchIndicator(boolean searching) {
 		if (searching) {
 			refreshButton.setVisibility(View.GONE);
 			loadingSpinner.setVisibility(View.VISIBLE);
 		} else {
 			refreshButton.setVisibility(View.VISIBLE);
 			loadingSpinner.setVisibility(View.GONE);
 		}
 	}
 	
 	private class BridgeSearchTask extends AsyncTask<Void, Void, Boolean> {
 		@Override
 		protected void onPostExecute(Boolean result) {
 			bridgeSearchTask = null;
 			setSearchIndicator(false);
 			
 			if (!result) {
 				new AlertDialog.Builder(LinkActivity.this)
 					.setTitle(R.string.dialog_bridge_search_title)
 					.setMessage(R.string.dialog_bridge_search)
 					.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					}).create().show();
 			}
 		}
 		
 		@Override
 		protected Boolean doInBackground(Void... params) {			
 			// Search bridges on local network using UPnP
 			try {				
 				String upnpRequest = "M-SEARCH * HTTP/1.1\nHOST: 239.255.255.250:1900\nMAN: ssdp:discover\nMX: 8\nST:SsdpSearch:all";
 				DatagramSocket upnpSock = new DatagramSocket();
 				upnpSock.setSoTimeout(SEARCH_TIMEOUT);
 				upnpSock.send(new DatagramPacket(upnpRequest.getBytes(), upnpRequest.length(), new InetSocketAddress("239.255.255.250", 1900)));
 				
 				HashMap<String, Boolean> ipsDiscovered = new HashMap<String, Boolean>();
 				
 				while (true) {
 					byte[] responseBuffer = new byte[1024];
 					
 					DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
 					upnpSock.receive(responsePacket);
 					
 					final String ip = responsePacket.getAddress().getHostAddress();
 					final String response = new String(responsePacket.getData());
 					
 					if (!ipsDiscovered.containsKey(ip)) {
 						Matcher m = Pattern.compile("LOCATION: (.*)", Pattern.CASE_INSENSITIVE).matcher(response);
 						
 						if (m.find()) {
 							final String description = Networker.get(m.group(1)).getBody();
 							
 							// Parsing with RegEx allowed here because the output format is fairly strict
 							final String modelName = Util.quickMatch("<modelName>(.*?)</modelName>", description);
 															
 							// Check from description if we're dealing with a hue bridge or some other device
 							if (modelName.toLowerCase(Locale.getDefault()).contains("philips hue bridge")) {
 								try {
 									final String bridgeName = HueService.getSimpleConfig(ip).name;
 									final boolean access = HueService.userExists(ip, Util.getDeviceIdentifier(LinkActivity.this));
 									
 									bridgesList.post(new Runnable() {
 										@Override
 										public void run() {
 											bridgesAdapter.add(new Bridge(ip, bridgeName, access));
 										}
 									});
 								} catch (ApiException e) {
 									// Do nothing, this basically serves as an extra check to see if it's really a hue bridge
 								}
 							}
 						}
 						
 						// Ignore subsequent packets
 						ipsDiscovered.put(ip, true);
 					}
 				}
 			} catch (SocketTimeoutException e) {
 				// Gracefully stop
 			} catch (SocketException e) {
 				return false;
 			} catch (IOException e) {
 				// Not sure what would cause this to happen
 				e.printStackTrace();
 			}
 			
 			return true;
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	private void showLinkDialog(final Bridge b) {
 		stopSearching();
 		
 		// Tell user to press link button
 		final ProgressDialog pd = new ProgressDialog(LinkActivity.this);
 		pd.setTitle(b.getName());
 		pd.setMessage(getString(R.string.dialog_link));
 		pd.setCancelable(true);
 		pd.setIndeterminate(true);
 		pd.setButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 		pd.setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				linkChecker.cancel();
 				linkChecker = null;
 			}
 		});
 		pd.show();
 		
 		// Periodically check if the button has been pressed yet
 		final String username = Util.getDeviceIdentifier(LinkActivity.this);
 		
 		linkChecker = new Timer();
 		linkChecker.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				try {
 					boolean pressed = HueService.createUser(b.getIp(), "hue2", username);
 					
					if (pressed) {						
 						// User created!
 						// TODO: Connect and view light activity
						pd.dismiss();
 						linkChecker.cancel();
 					}
 				} catch (ApiException e) {
 					// Ignore, it's because link button hasn't been pressed yet
 				} catch (IOException e) {
 					// TODO: Handle network errors
 				}
 			}
 		}, LINK_INTERVAL, LINK_INTERVAL);
 	}
 }
