 package com.tapad.sample;
 
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.List;
 
import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTabHost;
 import android.view.Menu;
 import android.view.MenuItem;
import android.widget.Toast;
 
 import com.tapad.tapestry.Logging;
 import com.tapad.tapestry.R;
import com.tapad.tapestry.TapestryClient;
 import com.tapad.tapestry.TapestryService;
 import com.tapad.tapestry.deviceidentification.TypedIdentifier;
 
 public class MainActivity extends FragmentActivity {
 	private MenuItem bridgeEmailItem;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.menu_tabs);
 
 		FragmentTabHost host = (FragmentTabHost) findViewById(android.R.id.tabhost);
 		host.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
 		host.addTab(host.newTabSpec("demo").setIndicator("Demo"), CarExampleFragment.class, null);
 		host.addTab(host.newTabSpec("debug").setIndicator("Debug"), DebugFragment.class, null);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		bridgeEmailItem = menu.add("Compose Bridge Email");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item == bridgeEmailItem) {
 			composeBridgeEmail();
 			return true;
 		} else
 			return super.onOptionsItemSelected(item);
 	}
 
 	private void composeBridgeEmail() {
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setType("text/html");
 		intent.putExtra(Intent.EXTRA_SUBJECT, "TapAd Bridge: " + Build.MODEL);
		
 		intent.putExtra(Intent.EXTRA_TEXT, "Pleaes open this URL in your desktop's browser to bridge " + Build.MODEL + ":\n\n" + buildURL());
 		startActivity(Intent.createChooser(intent, "Select Email App"));
 	}
 
 	private String buildURL() {
 		JSONObject bridge = new JSONObject();
 		List<TypedIdentifier> deviceIDs = TapestryService.client().getDeviceIDs();
 		try {
 			for (TypedIdentifier id : deviceIDs) {
 				bridge.put(id.getType(), id.getValue());
 			}
 			Logging.d("Added device ids:\n" + bridge.toString());
 
 			StringBuilder stringBuilder = new StringBuilder(TapestryService.client().getURL());
 			stringBuilder.append("?ta_partner_id=");
 			stringBuilder.append(TapestryService.client().getPartnerID());
 			stringBuilder.append("&ta_bridge=");
 			stringBuilder.append(URLEncoder.encode(bridge.toString(), "UTF-8"));
 			stringBuilder.append("&ta_redirect=");
 			stringBuilder.append(URLEncoder.encode("http://tapestry-demo-test.dev.tapad.com/content_optimization", "UTF-8"));
 			return stringBuilder.toString();
 		} catch (Exception e) {
 			Logging.e("Some error occured while making URL for bridging", e);
 			return "error!";
 		}
 	}
 }
