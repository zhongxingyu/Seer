 package com.novell.android.yastroid;
 
 import java.util.ArrayList;
 
 import com.novell.webyast.status.Health;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ServerActivity extends ListActivity {
 
 	private ProgressDialog moduleListProgress;
 	private Server yastServer;
 	private ArrayList<Module> moduleList = null;
 	private ModuleAdapter moduleAdapter;
 	private Runnable moduleView;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.server);
 		yastServer = new Server(getIntent().getExtras());
 		
 		moduleList = new ArrayList<Module>();
 		this.moduleAdapter = new ModuleAdapter(this, R.layout.module_list_row, moduleList);
 		
 		// Set the Header on this Activity
 		View header = getLayoutInflater().inflate(R.layout.server_header, null);
 		TextView serverName = (TextView)header.findViewById(R.id.server_name);
 		TextView serverHost = (TextView)header.findViewById(R.id.server_address);
 		//TextView serverUptime = (TextView)header.findViewById(R.id.server_uptime); // Not available yet
 		serverName.setText(yastServer.getName());
 		serverHost.setText(yastServer.getHostname());
 		//serverUptime.setText("Uptime: " + "8 days, 8 Hours"); //Not available yet
 		
 		ListView lv = getListView();
 		lv.addHeaderView(header);
 		setListAdapter(this.moduleAdapter);
 
 		
 		moduleView = new Runnable() {
 			@Override
 			public void run() {
 				getModules();
 			}
 		};
 		
 		Thread thread = new Thread(null, moduleView, "ModuleListBackground");
 		thread.start();
 		moduleListProgress = ProgressDialog.show(this, "Please wait...", "Building Module list...", true);
 	}
 	
 	@Override 
 	protected void onResume() {
 		super.onResume();
 		//getModules();
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent intent = null;
		Module m = moduleList.get(position);
		String n = m.getName();
		
		if (n == "UPDATE") {
			// Do something with the updates
		} else if (n == "HEALTH") {
			intent = new Intent(ServerActivity.this, SystemStatusActivity.class);
			intent.putExtras(getIntent().getExtras());
			startActivity(intent);
		} else {
			// Do nothing?
 		}
 	}
 	
 	private void getModules() {
 		moduleList = new ArrayList<Module>();
 		String moduleName = "SETME";
 		// Get Updates
 		Module update = null;
 		moduleName = "UPDATE";
 		int availableUpdates = 0;
 		
 		// TODO: Add icons for each module, which change dynamically
 		// (for example, green icon when system is healthy, red
 		// otherwise)
 		
 		try {
 			availableUpdates = yastServer.getUpdateModule().getNumberOfAvailableUpdates();
 			update = new Module(moduleName, availableUpdates + " updates available", R.drawable.yast_system);
 		}
 		catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 		if(update != null)
 			moduleList.add(update);
 		
 		
 		// Get Health
 		Module systemHealth = null;
 		moduleName = "HEALTH";
 		
 		// TODO: Figure out available modules dynamically
 		// TODO: Populate health text dynamically
 
 		try {
 			// TODO: Clicking this message should show you full details of the status
 			switch(yastServer.getStatusModule().getHealthSummary()) {
 				case Health.ERROR:
 					systemHealth = new Module(moduleName, "Cannot read system status", R.drawable.status_red);
 					break;
 				case Health.UNHEALTHY:
 					systemHealth = new Module(moduleName, "System is not healthy", R.drawable.status_red);
 					break;
 				case Health.HEALTHY:
 					systemHealth = new Module(moduleName, "Everything's shiny, Cap'n. Not to fret.", R.drawable.status_green);
 					break;
 			}
 		} 
 		catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 		if (systemHealth != null)
 			moduleList.add(systemHealth);
 		
 		runOnUiThread(returnRes);
 	}
 	
 	private Runnable returnRes = new Runnable() {
 		@Override
 		public void run() {
 			moduleAdapter.clear();
 			if (moduleList != null && moduleList.size() > 0) {
 				moduleAdapter.notifyDataSetChanged();
 				for (int i = 0; i < moduleList.size(); i++) {
 					moduleAdapter.add(moduleList.get(i));
 				}
 			}
 			moduleListProgress.dismiss();
 			moduleAdapter.notifyDataSetChanged();
 		}
 	};
 }
