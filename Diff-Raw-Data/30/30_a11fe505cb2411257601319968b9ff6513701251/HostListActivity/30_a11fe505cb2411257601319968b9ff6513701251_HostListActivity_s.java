 /*
  * ConnectBot: simple, powerful, open-source SSH client for Android
  * Copyright 2007 Kenny Root, Jeffrey Sharkey
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.connectbot;
 
 import java.util.List;
 
 import android.app.FragmentTransaction;
 import android.text.AndroidCharacter;
 import android.util.DisplayMetrics;
 import org.connectbot.bean.HostBean;
 import org.connectbot.service.TerminalBridge;
 import org.connectbot.service.TerminalManager;
 import org.connectbot.transport.TransportFactory;
 import org.connectbot.util.HostDatabase;
 import org.connectbot.util.PreferenceConstants;
 import org.connectbot.util.UpdateHelper;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.Intent.ShortcutIconResource;
 import android.content.SharedPreferences.Editor;
 import android.content.res.ColorStateList;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.nullwire.trace.ExceptionHandler;
 
 public class HostListActivity extends Activity implements HostListFragment.HostListFragmentContainer {
 	protected TerminalManager bound = null;
 
     HostListFragment fragmentHostList;
 
     HostDatabase hostdb;
 
 	private ServiceConnection connection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			bound = ((TerminalManager.TerminalBinder) service).getService();
 
 			// update our listview binder to find the service
 			fragmentHostList.updateList();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			bound = null;
 			fragmentHostList.updateList();
 		}
 	};
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		// start the terminal manager service
 		this.bindService(new Intent(this, TerminalManager.class), connection, Context.BIND_AUTO_CREATE);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		this.unbindService(connection);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		ExceptionHandler.checkForTraces(this);
 	}
 
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 
         // If we are on tablet, skip this activity and move straight to console activity
         // TODO: Need a new workaround when new OS are released
         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
             Intent i = new Intent(this, ConsoleActivity.class);
             startActivity(i);
             finish();
             return;
         }
 
 		setContentView(R.layout.act_hostlist);
 
 		this.setTitle(String.format("%s: %s",
 				getResources().getText(R.string.app_name),
 				getResources().getText(R.string.title_hosts_list)));
 
 		ExceptionHandler.register(this);
 
 		// start thread to check for new version
 		new UpdateHelper(this);
 
         fragmentHostList = HostListFragment.newInstance();
 
         FragmentTransaction ft = getFragmentManager().beginTransaction();
         ft.replace(R.id.listFrame, fragmentHostList);
         //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
         ft.commit();
 	}
 
     public boolean startConsoleActivity(Uri uri) {
        if(this.hostdb == null)
			this.hostdb = new HostDatabase(this);

		HostBean host = TransportFactory.findHost(hostdb, uri);
		if (host == null) {
			host = TransportFactory.getTransport(uri.getScheme()).createHost(uri);
			host.setColor(HostDatabase.COLOR_GRAY);
			host.setPubkeyId(HostDatabase.PUBKEYID_ANY);
			hostdb.saveHost(host);
		}

         Intent intent = new Intent(this, ConsoleActivity.class);
        intent.setData(host.getUri());
         startActivity(intent);
 
 		return true;
 	}
 
     public TerminalManager getTerminalManager() {
         return bound;
     }
 }
