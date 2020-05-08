 /*
  * Copyright 2013 The Last Crusade ContactLastCrusade@gmail.com
  * 
  * This file is part of SoundStream.
  * 
  * SoundStream is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SoundStream is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SoundStream.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.lastcrusade.soundstream.components;
 
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Surface;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.lastcrusade.soundstream.CoreActivity;
 import com.lastcrusade.soundstream.R;
 import com.lastcrusade.soundstream.service.ConnectionService;
 import com.lastcrusade.soundstream.service.ConnectionService.ConnectionServiceBinder;
 import com.lastcrusade.soundstream.service.IMessagingService;
 import com.lastcrusade.soundstream.service.MessagingService;
 import com.lastcrusade.soundstream.service.MessagingService.MessagingServiceBinder;
 import com.lastcrusade.soundstream.service.ServiceLocator;
 import com.lastcrusade.soundstream.service.ServiceNotBoundException;
 import com.lastcrusade.soundstream.util.BroadcastRegistrar;
 import com.lastcrusade.soundstream.util.IBroadcastActionHandler;
 import com.lastcrusade.soundstream.util.ITitleable;
 import com.lastcrusade.soundstream.util.Transitions;
 
 /*
  * This fragment should be what is first presented to the user when
  * they enter the app and are not connected to any network
  */
 public class ConnectFragment extends SherlockFragment implements ITitleable{
     
     private static final String TAG = ConnectFragment.class.getSimpleName();
 
     private BroadcastRegistrar broadcastRegistrar;
     private View joinView;
     
     private ServiceLocator<ConnectionService> connectionServiceLocator;
 
     private ServiceLocator<MessagingService> messagingServiceLocator;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         connectionServiceLocator = new ServiceLocator<ConnectionService>(
                 this.getActivity(), ConnectionService.class, ConnectionServiceBinder.class);
         
         messagingServiceLocator = new ServiceLocator<MessagingService>(
                 this.getActivity(), MessagingService.class, MessagingServiceBinder.class);
         
         registerReceivers();
     }
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_connect, container, false);
         ((CoreActivity)getActivity()).hidePlaybar();
         
         int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
         if(rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90){
             ((LinearLayout)v).setOrientation(LinearLayout.HORIZONTAL);
             LinearLayout.LayoutParams clickableParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
             clickableParams.setMargins(5, 10, 10, 10);
             v.findViewById(R.id.join).setLayoutParams(clickableParams);
             clickableParams.setMargins(10, 10, 5, 10);
             v.findViewById(R.id.create).setLayoutParams(clickableParams); 
         }
         else{
             ((LinearLayout)v).setOrientation(LinearLayout.VERTICAL);
             LinearLayout.LayoutParams clickableParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
             clickableParams.setMargins(10, 5, 10, 10);
             v.findViewById(R.id.join).setLayoutParams(clickableParams);
             clickableParams.setMargins(10, 10, 10, 5);
             v.findViewById(R.id.create).setLayoutParams(clickableParams);
         }
         View create = v.findViewById(R.id.create);
         create.setOnClickListener( new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 Transitions.transitionToHome((CoreActivity)getActivity());
                 ((CoreActivity)getActivity()).enableSlidingMenu();
                 //add the playbar fragment onto the active content view
                 ((CoreActivity)getActivity()).showPlaybar();
             }
         });
         
         this.joinView = v.findViewById(R.id.join);
         this.joinView.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                getConnectionService().broadcastSelfAsGuest(getActivity().getApplicationContext());
             }
         });
 
         return v;
     }
 
     @Override
     public void onStart() {
         super.onStart();
     }
 
     @Override
     public void onResume(){
         super.onResume();
         getActivity().setTitle(getTitle());
     }
     
     @Override
     public void onDestroy() {
         unregisterReceivers();
         
         this.connectionServiceLocator.unbind();
         this.messagingServiceLocator.unbind();
         super.onDestroy();
     }
     
     private ConnectionService getConnectionService() {
         ConnectionService connectionService = null;
         try {
             connectionService = this.connectionServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return connectionService;
     }
 
     private IMessagingService getMessagingService() {
         MessagingService messagingService = null;
         try {
             messagingService = this.messagingServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return messagingService;
     }
     
     private void registerReceivers() {
         this.broadcastRegistrar = new BroadcastRegistrar();
         this.broadcastRegistrar
             .addAction(ConnectionService.ACTION_HOST_CONNECTED, new IBroadcastActionHandler() {
 
                     @Override
                     public void onReceiveAction(Context context, Intent intent) {
                         joinView.setEnabled(true);
                         //switch 
                         Transitions.transitionToHome((CoreActivity)getActivity());
                         ((CoreActivity)getActivity()).enableSlidingMenu();
                         
                       //add the playbar fragment onto the active content view
                         ((CoreActivity)getActivity()).showPlaybar();
                     }
                 })
              .addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED, new IBroadcastActionHandler() {
 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     int mode = intent.getIntExtra(
                             BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE);
                     if(joinView != null){
                         switch(mode){
                         case BluetoothAdapter.SCAN_MODE_NONE:
                             joinView.setEnabled(true);
                             joinView.setBackgroundColor(getResources().getColor(R.color.abs__background_holo_light));
                             break;
                         case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                             joinView.setEnabled(true);
                             joinView.setBackgroundColor(getResources().getColor(R.color.abs__background_holo_light));
                             break;
                         case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                             joinView.setEnabled(false);
                             joinView.setBackgroundColor(getResources().getColor(R.color.gray));
                             break;
                         default:
                             Log.wtf(TAG, "Recieved scan mode changed with unknown mode");
                             break;
                         }
                     }
                 }
             })
             .register(this.getActivity());
     }
 
     private void unregisterReceivers() {
         this.broadcastRegistrar.unregister();
     }
 
     @Override
     public int getTitle() {
         return R.string.select;
     }
 }
