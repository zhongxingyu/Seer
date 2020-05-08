 package edu.wisc.cs707.src;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.EOFException;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.xmlrpc.android.XMLRPCClient;
 import org.xmlrpc.android.XMLRPCException;
 
 import android.content.Context;
 import android.widget.Toast;
 import edu.wisc.cs707.util.IUserActivityListener;
 import edu.wisc.cs707.util.LocationController;
 import edu.wisc.cs707.util.LocationController.Activity;
 import edu.wisc.cs707.util.LocationController.Snapshot;
 
 public class XmlRpcClientHandler implements IUserActivityListener {
 	
 	private static final String SERVER_NAME = "http://10.0.2.2:7500";
 	private static final String UUID_FILENAME = "uuid.dat";
 	private static final String TIMESTAMP_FMT = "yyyyMMdd'T'HH:mm:ss";
 	
 	private static final String FX_ADD_POI = "add_location";
 	private static final String FX_UPD_POI = "upd_location";
 	private static final String FX_REQUEST_UUID = "request_uid";
 	private static final String FX_ENTERED_LOCATION = "enter_location";
 	private static final String FX_EXITED_LOCATION = "exit_location";
 	
 	private static XmlRpcClientHandler instance = null;
 	private int uid = -1;
 	
	private Context mContext;
	private XmlRpcClientHandler(Context context) { mContext = context;
 		
 		/* do auto login/registration here. */
		if (this.login(context)) {
			this.uid = this.register(context);
			LocationController.getInstance(context).requestActivityUpdates(this);
		} else {
 			this.uid = this.register(context);
 			
 			// register for updates if logged in.
 			if (this.uid != -1) {
 				LocationController.getInstance(context).requestActivityUpdates(this);
 			}
 		}
 	}
 	
 	public static XmlRpcClientHandler getInstance(Context context) {
 		if (instance == null) {
 			instance = new XmlRpcClientHandler(context);
 		}
 		
 		return instance;
 	}
 	
	private SimpleDateFormat df = new SimpleDateFormat(TIMESTAMP_FMT);
 	@Override
 	public void onUserActivityUpdate(Activity activity, Snapshot snapshot) {
 		
 		if (this.uid == -1) {
 			return;
 		}
 		
		Toast.makeText(mContext, "Location: " + snapshot.getPoi().getName() + ", Activity: " + activity, Toast.LENGTH_LONG).show();
		
 		XMLRPCClient client = new XMLRPCClient(URI.create(SERVER_NAME));
 		try {
 			switch (activity) {
 			case Entered:
 				client.call(FX_ENTERED_LOCATION, this.uid, snapshot.getPoi().getId(),
 						df.format(new Date(snapshot.getTimeEntered())));
 				break;
 			case Exited:
 				client.call(FX_EXITED_LOCATION, this.uid, snapshot.getPoi().getId(),
 						df.format(new Date(snapshot.getTimeExited())));
 				break;
 			}
 		} catch (XMLRPCException e) {
 		
 		}
 	}
 	
 	public void addPoi(PointOfInterest poi) {
 		
 		// ensure user is logged in or registed.
 		if (this.uid == -1) {
 			return;
 		}
 		
 		try {
 			XMLRPCClient client = new XMLRPCClient(URI.create(SERVER_NAME));
 			client.call(FX_ADD_POI, this.uid, poi.getId(), poi.getName(), null);
 		} catch (XMLRPCException e) {
 		}
 	}
 	
 	public void updatePoi(PointOfInterest poi) {
 		
 		// ensure user is logged in or registed.
 		if (this.uid == -1) {
 			return;
 		}
 		
 		try {
 			XMLRPCClient client = new XMLRPCClient(URI.create(SERVER_NAME));
 			client.call(FX_UPD_POI, this.uid, poi.getId(), poi.getName(), null);
 		} catch (XMLRPCException e) {
 		}
 	}
 	
 	private boolean login(Context context) {
 		FileInputStream fs = null;
 		InputStream in = null;
 		try {
 			fs = context.openFileInput(UUID_FILENAME);
 			in = new BufferedInputStream(fs);
 			
 			byte [] data = new byte[1024];
 			in.read(data);
 
 			String s = new String(data);
 			
 			this.uid = Integer.parseInt(s.substring(s.indexOf('=') + 1, s.indexOf(';')));
 			Toast.makeText(context, "Login: " + String.valueOf(this.uid), Toast.LENGTH_LONG).show();
 			return true;	
 		} catch (EOFException e) {
 			return false;
 		} catch (FileNotFoundException e) {
 			return false;
 		} catch (IOException e) {
 			
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private int register(Context c) {
 		int uid = -1;
 		XMLRPCClient client = new XMLRPCClient(URI.create(SERVER_NAME));
 		
 		try {
 			uid = (Integer)client.call(FX_REQUEST_UUID);
 			BufferedOutputStream bufferedOutput = null;
 			try {
 				DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
 				FileOutputStream fileStream = c.openFileOutput(UUID_FILENAME, Context.MODE_PRIVATE);
 				bufferedOutput = new BufferedOutputStream(fileStream);
 				
 				String text = "uuid=" + String.valueOf(uid) + ";registed-on=" + df.format(new Date());
 				bufferedOutput.write(text.getBytes());
 				
 				Toast.makeText(c, "Register: " + String.valueOf(uid), Toast.LENGTH_LONG).show();
 				
 			} catch (IOException e) {
 				uid = -1;
 			} finally {
 				if (bufferedOutput != null) {
 					try {
 						bufferedOutput.close();
 					} catch (IOException e) {
 
 					}	
 				}
 			}
 		} catch (XMLRPCException e) {
 		}
 		
 		return uid;
 	}
 
 }
