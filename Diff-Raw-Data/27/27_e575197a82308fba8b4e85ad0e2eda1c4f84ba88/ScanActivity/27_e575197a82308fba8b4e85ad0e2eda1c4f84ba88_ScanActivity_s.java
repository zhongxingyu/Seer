 package org.csie.mpp.buku;
 
 import android.app.Activity;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TabHost;
 
 public class ScanActivity extends TabActivity {
 	public static final int REQUEST_CODE = 1436;
 	public static final String ISBN = "ISBN";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.scan);
 
         Resources res = getResources();
         TabHost tabhost = getTabHost();
         
        // tab: Barcode Scanner
        Intent intent = new Intent("com.google.zxing.client.android.BUKU_SCAN");
        intent.putExtra("SCAN_MODE", "ONE_D_MODE");
        String title = getString(R.string.tab_barcode);
        TabHost.TabSpec spec = tabhost.newTabSpec(title).setIndicator(title, res.getDrawable(R.drawable.ic_menu_barcode)).setContent(intent);
         tabhost.addTab(spec);
         
        intent = new Intent(this, IsbnInputActivity.class);
        title = getString(R.string.tab_isbn);
        spec = tabhost.newTabSpec(title).setIndicator(title, res.getDrawable(R.drawable.ic_menu_text)).setContent(intent);
         tabhost.addTab(spec);
         
        tabhost.setCurrentTab(0);
     }
     
     public static abstract class AbstractTabContentActivity extends Activity {
     	// [Yi] Notes: a work-around for TabActivity
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
         	data.putExtra(ISBN, input.getText().toString());
         	setResultForTabActivity(RESULT_OK, data);
             finish();
 		}
     }
 }
