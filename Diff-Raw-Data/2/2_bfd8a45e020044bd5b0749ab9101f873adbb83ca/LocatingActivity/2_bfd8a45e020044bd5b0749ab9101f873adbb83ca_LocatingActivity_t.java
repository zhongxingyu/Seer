 package edu.aau.utzon.indoor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.aau.utzon.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class LocatingActivity extends Activity {
 	WifiManager _wifi;
 	TextView _textView;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.location);
 
 		_textView = (TextView)findViewById(R.id.textView3);
 
 		String connectivity_context = Context.WIFI_SERVICE;
 		_wifi = (WifiManager)getSystemService(connectivity_context);
 	}
 
 	public void findLocation(View view) throws InterruptedException {
 		_textView.setText("");
 		if (_wifi.startScan() == true)
 		{
 			List<ScanResult> scanResults = _wifi.getScanResults();
 			ArrayList<WifiMeasureCollection> measures = WifiHelper.getWifiMeasures(this, _wifi, 10, 200);
			Point p = RadioMap.FindPosition(measures, 1, 1);
 			String text = "";
 			text += p.getName() + "\n";
 			
 			if (p == null) {
 				text = "You are not close to any points.!";
 			}
 			else {
 				
 				// This is just printet out for debug reasons. You can just delete it if you want... But ask lige Steffan first
 				//for (WifiMeasure m1 : measures) {
 				//	for (WifiMeasure m2 : p.getMeasures()) {
 				//		if (m1.getName().equals(m2.getName())) {
 				//			Double temp = (double)m1.getSignal() - (double)m2.getSignal();
 				//			text += m1.getName() + ": " + temp + "\n";
 				//		}
 				//	}
 				//}
 			}
 			_textView.setText(text);
 		}
 		else
 		{
 			_textView.setText("Could not scan networks");
 		}
 	}
 }
