 package se.lth.student.eda040.a1;
 
 import se.lth.student.eda040.a1.data.Input;
 import se.lth.student.eda040.a1.data.Output;
 import se.lth.student.eda040.a1.data.ClientMonitor;
 import se.lth.student.eda040.a1.data.ImageFetcher;
 import se.lth.student.eda040.a1.network.ClientProtocol;
 import se.lth.student.eda040.a1.network.Command;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.MenuInflater;
 import android.view.Menu;
 import android.util.Log;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import java.net.UnknownHostException;
 import java.io.IOException;
 
 
 public class VideoActivity extends Activity {
 	private Input in0;
 	private Input in1;
 	private Output out0;
 	private Output out1;
 	private Handler handler;
 	private ClientMonitor monitor;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.videoview);
 		AwesomeVideoView avv = (AwesomeVideoView) findViewById(R.id.avv);
 		
 		// TODO the socket instantiation is blocking. OK for now but if possible do this in another setup-thread.
 		handler = new Handler();
 		monitor = new ClientMonitor();
 		ImageFetcher fetcher = new ImageFetcher(monitor, avv, handler);
 		ClientProtocol protocol0 = new ClientProtocol((byte) 0);
 		ClientProtocol protocol1 = new ClientProtocol((byte) 1);
 		monitor.addProtocol((byte) 0, protocol0);
 		monitor.addProtocol((byte) 1, protocol1);
 		in0 = new Input(monitor, protocol0);
 		in1 = new Input(monitor, protocol1);
 		out0 = new Output(monitor, protocol0);
 		out1 = new Output(monitor, protocol1);
 
 		in0.start();
 		in1.start();
 		out0.start();
 		out1.start();
 		fetcher.start();
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.videomenu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.connectCam0:
 				Log.d("VideoActivity", "Selected connectCam0 option.");
 				// TODO let user choose camera here.
 				connectCamera((byte) 0, "argus-7.student.lth.se");
 				break;
 		case R.id.connectCam1:
 				Log.d("VideoActivity", "Selected connectCam0 option.");
 				// TODO let user choose camera here.
 				// The actual state will not be changed unit StateFetcher notifies about it.
 				connectCamera((byte) 1, "argus-8.student.lth.se");
 				break;
 		case R.id.disconnectCam0:
 				Log.d("VideoActivity", "Selected disconnectCam0");
        monitor.gracefullDisconnect((byte) 1);	
 				break;
 		case R.id.disconnectCam1:
 				Log.d("VideoActivity", "Selected disconnectCam1");
        monitor.gracefullDisconnect((byte) 1);	
 		case R.id.setIdle:
 				monitor.setIdleMode();
 				break;
 		default:
 				super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
     private void connectCamera(byte cameraId, String host){
         try{
             if(!monitor.connectTo(cameraId, host)){
                 Log.d("videoactivity", "failed to connect camera: " + cameraId);
                 // display information text
             }
         }catch (UnknownHostException e){
             Log.d("videoactivity", "failed to connect camera: " + cameraId +
                     ". unable to conect to host: " + host + ".");
             // display information text
         } catch (IOException e){
             Log.d("videoactivity", "failed to connect camera: " + cameraId);
             // display information text
         } catch (IllegalArgumentException e){
             Log.d("videoactivity", "failed to connect camera: " + cameraId + 
                     "Camera has not been set up!");
             // display information text
         }
     }
 
 	public boolean onPrepareOptionsMenu(Menu menu) {
 				// TODO this overides the icons that otherwide would be used according to the xml. What to do?
 				menu.clear();	 // Clears all items, below: rebuild from scratch.
 				if (monitor.isConnectedCamera((byte) 0)) {
 					menu.add(Menu.NONE, R.id.disconnectCam0, 0, "Disconnect c0");
 				} else {
 					menu.add(Menu.NONE, R.id.connectCam0, 0, "Connect c0");
 				}
 
 				if (monitor.isConnectedCamera((byte) 1)) {
 					menu.add(Menu.NONE, R.id.disconnectCam1, 1, "Disconnect c1");
 				} else {
 					menu.add(Menu.NONE, R.id.connectCam1, 1, "Connect c1");
 				}
 				menu.add(Menu.NONE, R.id.setIdle, 2, "Set Idle");
 		return true;
 	}
 }
