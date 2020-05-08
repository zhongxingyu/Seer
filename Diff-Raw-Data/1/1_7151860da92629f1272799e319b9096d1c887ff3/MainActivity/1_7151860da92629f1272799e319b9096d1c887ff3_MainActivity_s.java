 package test.btt;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private Bluetooth mBt;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Toast.makeText(this, "hoge...", Toast.LENGTH_LONG).show();
 		mBt = new Bluetooth();
 		try {
 			mBt.setBluetoothDevice(getString(R.string.BTDEVICE_NAME_BLUETOOTHMATE), getString(R.string.BTDEVICE_ADDRESS_BLUETOOTHMATE));
 			mBt.connectSocket();
 		} catch (Exception e) {
 			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onDestroy(){
 		try {
 			mBt.closeSocket();
 		} catch (IOException e) {
 			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
 		}
 	}
 	
     public void onClickButton1(View view){
     	byte[] buffer = {0x01};
     	try {
 			mBt.write(buffer);
 		} catch (IOException e) {
 			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
 		}
     }
 }
