 package com.sandwich;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.sandwich.client.PeerSet;
 import com.sandwich.client.PeerSet.Peer;
 import com.sandwich.ui.DetailsDialog;
 import com.sandwich.ui.Dialog;
 
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.app.Activity;
 import android.content.Intent;
 
 public class PeerList extends Activity implements OnItemClickListener {
 	private static ClientUiBinding client;
 	private ListView peerList;
 	private static ArrayAdapter<Peer> adapter;
 	private static Activity thisActivity;
 	private static boolean suspendUpdates, queuedUpdate;
 	
 	public static void addClient(ClientUiBinding t) {
 		client = t;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_peer_list);
 		
 		// This is the current peer list activity
 		thisActivity = this;
 		
 		// Setup the list view
 		peerList = (ListView)findViewById(R.id.peersListView);
 		adapter = new ArrayAdapter<Peer>(this, R.layout.simplerow);
         peerList.setAdapter(adapter);
         peerList.setOnItemClickListener(this); 
         registerForContextMenu(peerList);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
         // Update the list view
         updateListView();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		// Deregister our activity
 		thisActivity = null;
 		super.onDestroy();
 	}
 	
 	@Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         // Disable updates to the peerlist while displaying the context menu
 		suspendUpdates = true;
 		
 		// Call superclass
 		super.onCreateContextMenu(menu, v, menuInfo);
                 
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
         PeerSet.Peer peer = (PeerSet.Peer) adapter.getItem(info.position);
         if (peer == null)
         	return;
         
         // Inflate the context menu
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.peer_menu, menu);
         
         // Offer to unblacklist if the peer is already blacklisted
         if (client.blacklist.isBlacklisted(peer.getIpAddress()))
         {
         	// FIXME: Magic number
         	menu.getItem(1).setTitle("Unblacklist Peer");
         }
     }
 	
 	@Override
 	public void onContextMenuClosed(Menu menu) {
 		// Restart updating
 		suspendUpdates = false;
 		
 		// Update if requested
 		if (queuedUpdate) {
 			queuedUpdate = false;
 			updateListView();
 		}
 	}
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         PeerSet.Peer peer = (PeerSet.Peer) adapter.getItem(info.position);
         switch (item.getItemId())
         {
         case R.id.blacklist:
         	if (client.blacklist.isBlacklisted(peer.getIpAddress())) {
         		client.blacklist.removeFromBlacklist(peer.getIpAddress());
        		peer.setState(Peer.STATE_UNKNOWN);
         	} else {
         		client.blacklist.addToBlacklist(peer.getIpAddress());
        		peer.setState(Peer.STATE_BLACKLISTED);
         	}
         	
        	// Update the list view
        	updateListView();
        	
         	// Bootstrap so the changes take effect
         	client.bootstrap();
         	return true;
         	
         case R.id.details:
         	try {
         		new DetailsDialog(this, peer).createDetailsDialog();
         	} catch (Exception e) {
         		Dialog.displayDialog(this, "Error", e.getMessage(), false);
         	}
         	return true;
         	
         case R.id.viewfiles:
             Intent filesPage = new Intent(this, PeerFiles.class);
             filesPage.putExtra(PeerFiles.PEER, peer.getIpAddress());
             startActivity(filesPage);
             return true;
             
         default:
           return super.onContextItemSelected(item);
         }
     }
 
 	public static void updateListView()
 	{
 		// Queue an async update if updates are suspended
 		if (suspendUpdates) {
 			queuedUpdate = true;
 			return;
 		}
 		
 		if (thisActivity != null) {
 			thisActivity.runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					List<Peer> peerList = new ArrayList<Peer>(client.getPeerSet());
 					System.out.println("Refreshing peer list");
 					
 					// Sort the list alphabetically
 					Collections.sort(peerList);
 					
 					if (adapter != null) {
 						adapter.clear();
 						for (Peer p : peerList)
 						{
 							adapter.add(p);
 						}
 					}
 				}
 			});
 		}
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
         PeerSet.Peer peer = adapter.getItem(pos);
         Intent filesPage = new Intent(this, PeerFiles.class);
         filesPage.putExtra(PeerFiles.PEER, peer.getIpAddress());
         startActivity(filesPage);
 	}
 }
