 package com.sound.ampache;
 
 /* Copyright (c) 2008-2009 Kevin James Purdy <purdyk@gmail.com>
  *
  * +------------------------------------------------------------------------+
  * | This program is free software; you can redistribute it and/or          |
  * | modify it under the terms of the GNU General Public License            |
  * | as published by the Free Software Foundation; either version 2         |
  * | of the License, or (at your option) any later version.                 |
  * |                                                                        |
  * | This program is distributed in the hope that it will be useful,        |
  * | but WITHOUT ANY WARRANTY; without even the implied warranty of         |
  * | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
  * | GNU General Public License for more details.                           |
  * |                                                                        |
  * | You should have received a copy of the GNU General Public License      |
  * | along with this program; if not, write to the Free Software            |
  * | Foundation, Inc., 59 Temple Place - Suite 330,                         |
  * | Boston, MA  02111-1307, USA.                                           |
  * +------------------------------------------------------------------------+
  */
 
 import android.app.ListActivity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Debug;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.preference.PreferenceManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.BaseAdapter;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 import android.widget.TextView;
 import android.widget.ImageView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.view.Window;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.*;
 import com.sound.ampache.objects.*;
 import java.lang.Integer;
 
 public final class collectionActivity extends ListActivity implements OnItemLongClickListener 
 {
 
     public dataHandler dataReadyHandler;
     private String title;
     private ArrayList<ampacheObject> list = null;
     private String[] directive;
     private Boolean isFetching = false;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         /* fancy spinner */
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         requestWindowFeature(Window.FEATURE_PROGRESS);
         
         //debugging crap
         //Debug.waitForDebugger();
 
         // Verify a valid session.
         amdroid.comm.ping();
 
         // We've tried to login, and failed, so present the user with the preferences pane
         if (amdroid.comm.authToken.equals("") || amdroid.comm.authToken == null) {
             Toast.makeText(this, "Login Failed: " + amdroid.comm.lastErr, Toast.LENGTH_LONG).show();
             Intent prefsIntent = new Intent().setClass(this, prefsActivity.class);
             startActivity(prefsIntent);
             return;
         }
 
         // Verify a valid session. The if statement should probably not be there.
         if (amdroid.comm.authToken.equals("") || amdroid.comm.authToken == null)
             amdroid.comm.ping();
 
         // We've tried to login, and failed, so present the user with the preferences pane
         if (amdroid.comm.authToken.equals("") || amdroid.comm.authToken == null) {
             Toast.makeText(this, "Login Failed: " + amdroid.comm.lastErr, Toast.LENGTH_LONG).show();
             return;
         }
 
         Intent intent = getIntent();
 
         directive = intent.getStringArrayExtra("directive");
         title = intent.getStringExtra("title");
 
         setTitle(title);
 
         tidbits td = (tidbits) getLastNonConfigurationInstance();
         if (td != null) {
             dataReadyHandler = td.dh;
             list = td.list;
             dataReadyHandler.ca = new collectionAdapter(this, R.layout.browsable_item, list);
             setListAdapter(dataReadyHandler.ca);
             setProgressBarVisibility(true);
             return;
         }
         // and be prepared to handle the response
         dataReadyHandler = new dataHandler();
         
         // Are we being 're-created' ?
         list = savedInstanceState != null ? (ArrayList) savedInstanceState.getParcelableArrayList("list") : null;
 
         // Maybe we have the cache already?
         if (amdroid.cache.containsKey(directive[0])) { 
             list = amdroid.cache.getParcelableArrayList(directive[0]); 
         }
 
         // If not, queue up a data fetch
         if (list == null) {
             isFetching = true;
             //Tell them we're loading
 
             //ampacheRequest req = amdroid.comm.new ampacheRequest(directive[0], directive[1], this);
             Message requestMsg = new Message();
 
             requestMsg.arg1 = 0;
             requestMsg.what = 0x1336;
 
             /* we want incremental pulls for the large ones */
             if (directive[0].equals("artists")) {
                 setProgressBarVisibility(true);
                 list = new ArrayList(amdroid.comm.artists);
                 requestMsg.arg2 = amdroid.comm.artists;
             } else if (directive[0].equals("albums")) {
                 setProgressBarVisibility(true);
                 list = new ArrayList(amdroid.comm.albums);
                 requestMsg.arg2 = amdroid.comm.albums;
             } else if (directive[0].equals("songs")) {
                 setProgressBarVisibility(true);
                 list = new ArrayList(amdroid.comm.songs);
                 requestMsg.arg2 = amdroid.comm.songs;
             } else {
                 list = new ArrayList();
                 requestMsg.what = 0x1337;
                getListView().setTextFilterEnabled(true);
                 setProgressBarIndeterminateVisibility(true);
             }
 
             //tell it what to do
             requestMsg.obj = directive;
 
             //tell it how to handle the stuff
             requestMsg.replyTo = new Messenger (this.dataReadyHandler);
             amdroid.requestHandler.incomingRequestHandler.sendMessage(requestMsg);
             dataReadyHandler.ca = new collectionAdapter(this, R.layout.browsable_item, list);
             setListAdapter(dataReadyHandler.ca);
         } else {
             setListAdapter(new collectionAdapter(this, R.layout.browsable_item, list));
            getListView().setTextFilterEnabled(true);
         }
         getListView().setOnItemLongClickListener(this);
         getListView().setEmptyView(findViewById(R.id.loading));
     }
 
     public Object onRetainNonConfigurationInstance() {
         if (isFetching) {
             tidbits td = new tidbits();
             td.dh = dataReadyHandler;
             td.list = list;
             return td;
         }
         return null;
     }
     
     protected void onSaveInstanceState(Bundle bundle) {
         super.onSaveInstanceState(bundle);
         if (!isFetching) {
             bundle.putParcelableArrayList("list", list);
         }
     }
 
     public void onDestroy() {
         super.onDestroy();
         if (isFetching) {
             dataReadyHandler.removeMessages(0x1336);
             dataReadyHandler.removeMessages(0x1337);
             dataReadyHandler.stop = true;
             amdroid.requestHandler.stop = true;
         }
     }
 
     public boolean onItemLongClick(AdapterView l, View v, int position, long id) {
         ampacheObject cur = (ampacheObject) l.getItemAtPosition(position);
         Toast.makeText(this, "Enqueue " + cur.getType() + ": " + cur.toString(), Toast.LENGTH_LONG).show();
         if (cur.hasChildren()) {
             Message requestMsg = new Message();
             requestMsg.obj = cur.allChildren();
             requestMsg.what = 0x1339;
             //tell it how to handle the stuff
             requestMsg.replyTo = new Messenger(dataReadyHandler);
             amdroid.requestHandler.incomingRequestHandler.sendMessage(requestMsg);
         } else {
             amdroid.playlistCurrent.add((Song) cur);
         }
         return true;
     }
 
     protected void onListItemClick(ListView l, View v, int position, long id) {
         ampacheObject val = (ampacheObject) l.getItemAtPosition(position);
         if (val == null)
             return;
         Intent intent = new Intent().setClass(this, collectionActivity.class);
         if (val.getType().equals("Song")) {
             Toast.makeText(this, "Enqueue " + val.getType() + ": " + val.toString(), Toast.LENGTH_LONG).show();
             amdroid.playlistCurrent.add((Song) val);
             return;
         } else {
             String[] dir = {val.childString(), val.id};
             intent = intent.putExtra("directive", dir).putExtra("title", val.getType() + ": " + val.toString());
         }
         startActivity(intent);
     }
 
     private class tidbits {
         public dataHandler dh;
         public ArrayList list;
         public collectionAdapter ca;
     }
 
     private class dataHandler extends Handler {
 
         public Boolean stop = false;
         public collectionAdapter ca;
 
         public void handleMessage(Message msg) {
             if (stop)
                 return;
             switch (msg.what) {
             case (0x1336):
                 /* Handle incremental updates */
                 list.addAll((ArrayList) msg.obj);
                 
                 /* queue up the next inc */
                 if (msg.arg1 < msg.arg2) {
                     Message requestMsg = new Message();
                     requestMsg.obj = directive;
                     requestMsg.what = 0x1336;
                     requestMsg.arg1 = msg.arg1 + 100;
                     requestMsg.arg2 = msg.arg2;
                     requestMsg.replyTo = new Messenger (this);
                     amdroid.requestHandler.incomingRequestHandler.sendMessage(requestMsg);
                     ca.notifyDataSetChanged();
                     collectionActivity.this.setProgress((10000 * msg.arg1) / msg.arg2);
                 } else {
                     /* we've completed incremental fetch, cache it baby! */
                     ca.notifyDataSetChanged();
                     amdroid.cache.putParcelableArrayList(directive[0], list);
                     collectionActivity.this.setProgress(10000);
                     getListView().setTextFilterEnabled(true);
                     isFetching = false;
                 }
                 break;
             case (0x1337):
                 /* Handle primary updates */
                 list.addAll((ArrayList) msg.obj);
                 setProgressBarIndeterminateVisibility(false);
                 ca.notifyDataSetChanged();
                 isFetching = false;
                 break;
             case (0x1338):
                 /* handle an error */
                 setProgressBarIndeterminateVisibility(false);
                 Toast.makeText(collectionActivity.this, "Communicator error:" + (String) msg.obj, Toast.LENGTH_LONG).show();
                 isFetching = false;
                 break;
             case (0x1339):
                 /* handle playlist enqueues */
                 amdroid.playlistCurrent.addAll((ArrayList) msg.obj);
                 break;
             }
         }
     }
 }
