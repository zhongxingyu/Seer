 package com.novell.android.yastroid;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import com.novell.webyast.status.Log;
 import com.novell.webyast.status.StatusModule;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import com.novell.webyast.status.Health;
 
 public class SystemStatusActivity extends ListActivity {
 	private StatusListAdapter statusListAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.system_status);
 		statusListAdapter = new StatusListAdapter(this, R.layout.system_status_list_item, new ArrayList<SystemStatus>());
 		setListAdapter(statusListAdapter);
 		buildList();
 		statusListAdapter.notifyDataSetChanged();
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Intent statusIntent = null;
 		SystemStatus systemStatus = null;
 		
 		super.onListItemClick(l, v, position, id);
 		systemStatus = (SystemStatus)getListView().getItemAtPosition(position);
         statusIntent = new Intent(SystemStatusActivity.this, DisplayResourceActivity.class);
 		statusIntent.putExtras(getIntent().getExtras());
 		switch (systemStatus.getSystemType()) {
 		case SystemStatus.NETWORK_STATUS:
 	        statusIntent.putExtra("RESOURCE_TYPE", getString(R.string.network_status_text));
 	        break;
 		case SystemStatus.MEMORY_STATUS:
 	        statusIntent.putExtra("RESOURCE_TYPE", getString(R.string.memory_status_text));
 	        break;
 		case SystemStatus.DISK_STATUS:
 	        statusIntent.putExtra("RESOURCE_TYPE", getString(R.string.disk_status_text));
 	        break;
 		case SystemStatus.CPU_STATUS:
 	        statusIntent.putExtra("RESOURCE_TYPE", getString(R.string.cpu_status_text));
 	        break;
	    default:
	    	statusIntent = null;
 		//case SystemStatus.SYSTEM_MSGS_STATUS:
 	        //statusIntent = new Intent(SystemStatusActivity.this, SystemMessagesActivity.class);
 	        //break;
 		}
 		if (statusIntent != null) {
 			startActivity(statusIntent);
 		}
 	}
 	
 	protected void buildList() {
 		Intent statusIntent;
 		SystemStatus status;
 		Server yastServer;
 		StatusModule statusModule;
 		Collection<Health> healthCollection;
 		int statusID;
 		Bundle b;
 		Health health;
 		
 		statusIntent = getIntent();
 		b = statusIntent.getExtras();
 		yastServer = new Server(b);
 		statusModule = yastServer.getStatusModule();
 		healthCollection = statusModule.getFullHealth();
 		health = new Health(0, 0, "Network", "");
 		if(healthCollection.contains(health))
 			statusID = SystemStatus.STATUS_RED;
 		else
 			statusID = SystemStatus.STATUS_GREEN;
 		status = new SystemStatus(this.getApplication(), SystemStatus.NETWORK_STATUS, statusID);
 		statusListAdapter.add(status);
 		health.setHeadline("Memory");
 		if(healthCollection.contains(health))
 			statusID = SystemStatus.STATUS_RED;
 		else
 			statusID = SystemStatus.STATUS_GREEN;
 		status = new SystemStatus(this.getApplication(), SystemStatus.MEMORY_STATUS, statusID);
 		statusListAdapter.add(status);
 		health.setHeadline("Disk");
 		if(healthCollection.contains(health))
 			statusID = SystemStatus.STATUS_RED;
 		else
 			statusID = SystemStatus.STATUS_GREEN;
 		status = new SystemStatus(this.getApplication(), SystemStatus.DISK_STATUS, statusID);
 		statusListAdapter.add(status);
 		health.setHeadline("CPU");
 		if(healthCollection.contains(health))
 			statusID = SystemStatus.STATUS_RED;
 		else
 			statusID = SystemStatus.STATUS_GREEN;
 		status = new SystemStatus(this.getApplication(), SystemStatus.CPU_STATUS, statusID);
 		statusListAdapter.add(status);
 
 		// TODO: Use a ProgressDialog
 		try {
 			Collection<Log> logs = yastServer.getStatusModule ().getLogs ();
 			if (logs == null) {
 				status = new SystemStatus(getApplication(), "Cannot get logs from server");
 				statusListAdapter.add(status);
 			} else {
 				for (Log l : logs) {
 					status = new SystemStatus (getApplication(), l.getDescription());
 					statusListAdapter.add(status);
 				}
 			}
 		} catch (Exception e) {
 			// Unable to get logs
 			status = new SystemStatus(getApplication(), "Cannot read messages");
 			statusListAdapter.add(status);
 			System.out.println(e.getMessage());
 		}
 	}
 }
