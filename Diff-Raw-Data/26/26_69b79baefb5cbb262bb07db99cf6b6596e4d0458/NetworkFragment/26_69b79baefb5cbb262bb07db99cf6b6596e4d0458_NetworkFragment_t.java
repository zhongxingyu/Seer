 package com.lastcrusade.fanclub.components;
 
 import java.util.List;
 
 import android.bluetooth.BluetoothDevice;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.lastcrusade.fanclub.CustomApp;
 import com.lastcrusade.fanclub.R;
 import com.lastcrusade.fanclub.model.UserList;
 import com.lastcrusade.fanclub.net.BluetoothDeviceDialogFormatter;
 import com.lastcrusade.fanclub.service.ConnectionService;
 import com.lastcrusade.fanclub.service.MessagingService;
 import com.lastcrusade.fanclub.util.BroadcastRegistrar;
 import com.lastcrusade.fanclub.util.IBroadcastActionHandler;
 import com.lastcrusade.fanclub.util.ITitleable;
 import com.lastcrusade.fanclub.util.UserListAdapter;
 /*
  * This fragment handles the ability for members to add new members to 
  * the network and to view the currently connected members
  */
 public class NetworkFragment extends SherlockFragment implements ITitleable{
     private BroadcastRegistrar broadcastRegistrar;
     private Button addMembersButton;
     private UserListAdapter adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         registerReceivers();
     }
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_network, container,false);
         
         this.addMembersButton = (Button)v.findViewById(R.id.add_members);
         this.addMembersButton.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 addMembersButton.setEnabled(false);
                 //TODO: add some kind of visual indicator while discovering...seconds until discovery is finished, number of clients found, etc
                 getConnectionService().findNewFans();
             }
         });
         
         ListView users = (ListView)v.findViewById(R.id.connected_users);
         
         this.adapter = new UserListAdapter(getActivity(), ((CustomApp)getActivity().getApplication()).getUserList(), false );
         users.setAdapter(this.adapter);
         return v;
     }
     
     @Override
     public void onDestroy() {
         unregisterReceivers();
         super.onDestroy();
     }
 
     private ConnectionService getConnectionService() {
         CustomApp app = (CustomApp) getActivity().getApplication();
         return app.getConnectionService();
     }
     
     private void registerReceivers() {
         this.broadcastRegistrar = new BroadcastRegistrar();
         this.broadcastRegistrar
             .addAction(ConnectionService.ACTION_FIND_FINISHED, new IBroadcastActionHandler() {
 
                     @Override
                     public void onReceiveAction(Context context, Intent intent) {
                         onDiscoveredDevices(intent);
                         addMembersButton.setEnabled(true);
                     }
                 })
             .addAction(UserList.ACTION_USER_LIST_UPDATE, new IBroadcastActionHandler() {
                 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     adapter.notifyDataSetChanged();
                 }
             })
             .register(this.getActivity());
     }
 
     private void unregisterReceivers() {
         this.broadcastRegistrar.unregister();
     }
 
     private void onDiscoveredDevices(Intent intent) {
 
         
         //locally initiated device discovery...pop up a dialog for the user
         //TODO: this should probably use FoundFan, to decouple this code from the bluetooth code
         List<BluetoothDevice> devices = intent.getParcelableArrayListExtra(ConnectionService.EXTRA_DEVICES);
         
         new MultiSelectListDialog<BluetoothDevice>(this.getActivity(),
                 R.string.select_fans, R.string.connect)
                 .setItems(devices)
                 .setOnClickListener(
                         new IOnDialogItemClickListener<BluetoothDevice>() {
 
                             @Override
                             public void onItemClick(
                                     BluetoothDevice device) {
                                 getConnectionService().connectToFan(device);
                             }
                         })
                 .setFormatter(new BluetoothDeviceDialogFormatter())
                 .show();
     }
 
     @Override
     public int getTitle() {
         return R.string.network;
     }
 }
