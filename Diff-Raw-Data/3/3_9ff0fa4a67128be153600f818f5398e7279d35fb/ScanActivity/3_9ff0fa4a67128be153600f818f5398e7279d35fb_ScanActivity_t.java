 package org.csie.mpp.buku;
 
 import android.app.Activity;
 import android.app.TabActivity;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 
 public class ScanActivity extends TabActivity implements OnTabChangeListener {
 	public static final int REQUEST_CODE = 1436;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.scan);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         Resources res = getResources();
         TabHost tabhost = getTabHost();
 
         // [Yi] Notes: ISBN Input should always has smaller index than Barcode Scanner
         // an unknown bug that cause soft-keyboard can't be set visible
         
         // tab: ISBN Input
         Intent intent = new Intent(this, IsbnInputActivity.class);
         String title = getString(R.string.tab_isbn);
         TabHost.TabSpec spec = tabhost.newTabSpec(title).setIndicator(title, res.getDrawable(R.drawable.ic_menu_text)).setContent(intent);
         tabhost.addTab(spec);
         
         // tab: Barcode Scanner
         intent = new Intent("com.google.zxing.client.android.BUKU_SCAN");
         intent.putExtra("SCAN_MODE", "ONE_D_MODE");
         title = getString(R.string.tab_barcode);
         spec = tabhost.newTabSpec(title).setIndicator(title, res.getDrawable(R.drawable.ic_menu_barcode)).setContent(intent);
         tabhost.addTab(spec);
         
         tabhost.setCurrentTab(1);
         tabhost.setOnTabChangedListener(this);
     }
 
 	@Override
 	public void onTabChanged(String tabId) {
 		// [Yi] Notes: prevent soft-keyboard to show on other view (such as barcode scanner) 
 		if(!tabId.equals(getString(R.string.tab_isbn))) {
 			InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 			if(imm != null && imm.isActive())
 				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
 		}
 	}
     
     public static abstract class AbstractTabContentActivity extends Activity {
     	// [Yi] Notes: a work-around for TabActivity
     	// a problems that cause resultCode being RESULT_CANCEL
     	protected void setResultForTabActivity(int resultCode, Intent data) {
     		Activity parent = getParent();
         	if(parent == null)
         		setResult(resultCode, data);
         	else
         		parent.setResult(resultCode, data);
     	}
     }
     
     public static final class IsbnInputActivity extends AbstractTabContentActivity implements OnClickListener {
     	private EditText input;
     	
     	@Override
     	public void onCreate(Bundle savedInstanceState) {
     		super.onCreate(savedInstanceState);
     		setContentView(R.layout.isbn);
     		
     		((Button)findViewById(R.id.ok)).setOnClickListener(this);
     		
     		input = (EditText)findViewById(R.id.isbn);
     	}
 
 		@Override
 		public void onClick(View v) {
         	Intent data = new Intent();
         	data.putExtra(App.ISBN, input.getText().toString());
         	setResultForTabActivity(RESULT_OK, data);
             finish();
 		}
     }
 }
