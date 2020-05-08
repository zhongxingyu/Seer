 package android.ping;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.TextView;
 
 import edu.uw.cs.cse461.sp12.OS.DDNSException;
import edu.uw.cs.cse461.sp12.OS.DDNSFullName;
 import edu.uw.cs.cse461.sp12.OS.DDNSRRecord;
 import edu.uw.cs.cse461.sp12.OS.DDNSResolverService;
 import edu.uw.cs.cse461.sp12.OS.OS;
 import edu.uw.cs.cse461.sp12.OS.RPCCallerSocket;
 import edu.uw.cs.cse461.sp12.OS.RPCService;
 import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSNoAddressException;
 import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSNoSuchNameException;
 import edu.uw.cs.cse461.sp12.util.Log;
 
 
 
 public class AndroidPingActivity extends Activity {
 	
 	public static final String TAG = "AndroidPingActivity";
     public static final String PREFS_NAME = "CSE461";
 
     private String mServerHost;
     private String mServerPort;
 	
 	/** Called when the activity is first created.  Establishes the UI.  Reads state information saved by previous runs. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
         bootOS();
         
    }
    private void bootOS(){
 	   Properties config = new Properties();
        config.put("host.name", "foo.bar.");
        config.put("rpc.timeout", "10000");
        config.put("ddns.cachettl", "60");
        config.put("ddns.rootserver", "cse461.cs.washington.edu");
        config.put("ddns.rootport", "46130");
        config.put("ddns.password", "champ");
        config.put("rpc.serverport", "46120");
        config.put("host.name", "galaxy.hallshep.cse461");
 		// boot the OS and load RPC services
 		try {
 			OS.boot(config);
 		} catch (RuntimeException r){
 		} catch (Exception e) {
 			throw new IllegalStateException("OS Hasn't booted!");
 		}
 		OS.startServices(OS.androidServiceClasses);
		DDNSFullName ddnsName = new DDNSFullName(OS.hostname());
		int port = ((RPCService) OS.getService("rpc")).localPort();
		try {
			((DDNSResolverService) OS.getService("ddnsresolver")).register(ddnsName, port);
		} catch (DDNSException e) {
			Log.e("DDNS Register", "Couldnt Register this device!: " + e.getMessage());
		}
    }
 
     /**
      * Called when activity is stopped.  Save user preferences for next execution.
      */
     @Override
     protected void onStop() {
     	super.onStop();
		DDNSFullName ddnsName = new DDNSFullName(OS.hostname());
    	try {
			((DDNSResolverService) OS.getService("ddnsresolver")).unregister(ddnsName);
		} catch (DDNSException e) {
			Log.e("Shutdown", "Couldn't unregister this device!");
		}
    	OS.shutdown();
     	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putString("serverhost", mServerHost);
     	editor.putString("serverport", mServerPort);
     	editor.commit();
     }
     
     /**
      * Fetch the data entered through the UI by the user.
      * @return
      */
     private boolean readUserInputs() {
         mServerHost = ((TextView)findViewById(R.id.host_ip)).getText().toString();
         mServerPort = ((TextView)findViewById(R.id.app_port)).getText().toString();
     	return true;
     }
     
     /**
      * Start/stop toggle button click handler.  (The association of a button click with the 
      * invocation of this routine is made in the layout object.) This runs the Ping protocol
      * @throws JSONException 
      * @throws IOException 
      */
     public void onStart(View v) throws IOException, JSONException {
     	TextView output = (TextView) findViewById(R.id.output);
     	output.setText("");
     	Thread pingThread = new Thread(){
     		public void run(){
     			readUserInputs();
     	    	int runs = 5;
     			long time;
     			long newTime;
     			long overall = 0;
     			RPCCallerSocket socket;
     			socket = getSocket();
     			if(socket == null) return;
     			String host = socket.remotehost();
 				String ip = socket.getInetAddress().getHostAddress();
 				int port = socket.getPort();
 				runOnUiThread(new outputUpdater("Pinging " + host + " @ " + ip + ":" + port + "\n\n"));
     			try {
     				for(int i = 0; i < runs; i++){
     					time = System.currentTimeMillis();
     					socket.invoke("echo", "echo", new JSONObject().put("msg", ""));
     					newTime = System.currentTimeMillis();
     					long diff = newTime - time;
     					overall += diff;
     					runOnUiThread(new outputUpdater("Run #" + i + " (msec): " + diff + "\n"));
     					if(!socket.isPersistent() && i < runs){
     						socket.close();
     						socket = new RPCCallerSocket(host, ip, port);
     					}
     				}
     				runOnUiThread(new outputUpdater("Average (msec): " + ((double)overall) / runs + "\n"));
     				if (!socket.isClosed()) socket.close();
     				runOnUiThread(new outputUpdater("Socket Closed."));
     			} catch (Exception e) {
     				runOnUiThread(new outputUpdater(e.getMessage()));
     				return;
     			}
     	    }
     	};
     	pingThread.start();
     }
     private RPCCallerSocket getSocket(){
     	RPCCallerSocket socket;
     	try {
 			socket = new RPCCallerSocket(mServerHost, mServerHost, mServerPort);
 		} catch(UnknownHostException e){
 			socket = resolve();
 		} catch(SocketException se){
 			socket = resolve();
 		} catch (Exception e2) {
 			runOnUiThread(new outputUpdater(e2.getMessage()));
 			return null;
 		}
 		return socket;
     }
     private RPCCallerSocket resolve(){
     	RPCCallerSocket socket;
     	DDNSRRecord record = null;
 		try {
 			record = ((DDNSResolverService) OS.getService("ddnsresolver")).resolve(mServerHost);
 		} catch (DDNSNoAddressException nae) {
 			runOnUiThread(new outputUpdater("No address is currently assoicated with the name: " + mServerHost + "\n"));
 			return null;
 		} catch (DDNSNoSuchNameException nsne) {
 			runOnUiThread(new outputUpdater("No such name: " + mServerHost  + "\n"));
 			return null;
 		} catch (Exception genE) {
 			runOnUiThread(new outputUpdater("Exception: " + genE.getMessage() + "\n"));
 			return null;
 		}
 		try {
 			socket = new RPCCallerSocket(mServerHost, record.host, record.port);
 		} catch (Exception e1) {
 			runOnUiThread(new outputUpdater("Exception: " + e1.getMessage() + "\n"));
 			return null;
 		} 
 		return socket;
     }
     public void whoami(View v) throws IOException, JSONException {
     	TextView output = (TextView) findViewById(R.id.output);
     	output.setText("");
 		StringBuilder IFCONFIG=new StringBuilder();
 		RPCService rpcService = (RPCService)OS.getService("rpc");
 		IFCONFIG.append(rpcService.localIP());
 	    try {	
 			IFCONFIG.append("  Port: " + rpcService.localPort());
 		} catch (Exception e) {}
 		output.setText(IFCONFIG);
     }
     private class outputUpdater implements Runnable {
     	String output;
     	TextView v;
     	public outputUpdater(String output){
     		this.output = output;
     		v = (TextView)findViewById(R.id.output);
     	}
 		@Override
 		public void run() {
 			if(output != null) v.append(output);
 		}
     	
     }
 }
