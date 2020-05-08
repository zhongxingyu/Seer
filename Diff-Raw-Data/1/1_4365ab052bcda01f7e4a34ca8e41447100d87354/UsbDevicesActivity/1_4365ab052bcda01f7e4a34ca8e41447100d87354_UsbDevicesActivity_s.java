 package pro.kornev.kcar.cop;
 
 import android.content.Context;
 import android.hardware.usb.UsbDevice;
 import android.hardware.usb.UsbManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class UsbDevicesActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.usb_devices_activity);
         refresh();
     }
 
     public void refreshButtonClick(View v) {
         refresh();
     }
 
     public void devicesListClick(View v) {
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.usb_devices, menu);
         return true;
     }
 
     private void refresh() {
         UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
         Map<String, UsbDevice> usbList = manager.getDeviceList();
         if (usbList==null || usbList.isEmpty()) return;
 
         ListView devicesList = (ListView)findViewById(R.id.uaDevicesList);
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.abc_action_menu_item_layout, (String[])usbList.keySet().toArray());
     }
 }
