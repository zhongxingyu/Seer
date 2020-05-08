 /*
  * wePoker: Play poker with your friends, wherever you are!
  * Copyright (C) 2012, The AmbientTalk team.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2 of the License, or (at your
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package edu.vub.at.nfcpoker.comm;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.MulticastLock;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.commlib.CommLibConnectionInfo;
 import edu.vub.at.nfcpoker.ui.Splash;
 
 public class DiscoveryAsyncTask extends AsyncTask<Void, Void, CommLibConnectionInfo> {
 	public interface DiscoveryCompletionListener {
 		void onDiscovered(CommLibConnectionInfo result);
 	}
 
 	private final Activity act;
 	public DiscoveryCompletionListener dcl;
 
 	public DiscoveryAsyncTask(Activity act, DiscoveryAsyncTask.DiscoveryCompletionListener dcl) {
 		this.act = act;
 		this.dcl = dcl;
 	}
 	
 	@Override
 	protected CommLibConnectionInfo doInBackground(Void... arg0) {
 		if (act == null) return null;
 		WifiManager wm = (WifiManager) act.getSystemService(Splash.WIFI_SERVICE);
 		MulticastLock ml = wm.createMulticastLock("edu.vub.at.nfcpoker");
 		ml.acquire();
 		try {
 			while (!isCancelled()) {
 				try {
 					String broadcastAddress = CommLib.getBroadcastAddress(wm);
 					CommLibConnectionInfo c = CommLib.discover(PokerServer.class, broadcastAddress);
 					return c;
 				} catch (IOException e) {
 					Log.d("wePoker - Discovery", "Could not start discovery", e);
 				}
 				try { Thread.sleep(1000);
 				} catch (InterruptedException e) { }
 			}
 		} finally {
 			ml.release();
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(CommLibConnectionInfo result) {
 		super.onPostExecute(result);
 		if (isCancelled())
 			return;
 		
 		if (result != null) {
 			dcl.onDiscovered(result);
 		} else {
 			Toast.makeText(this.act, "Could not discover hosts", Toast.LENGTH_SHORT).show();
 		}
 	}
}
