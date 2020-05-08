 package net.redlinesoft.app.barcodescanner;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 public class MainActivity extends Activity {
 
 	 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		Button bntScan = (Button) findViewById(R.id.button1);
 		bntScan.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
 				integrator.initiateScan();
 			}
 		});
 		
 	}	
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
 		  if (scanResult != null) {
 			  TextView txtBarcode = (TextView) findViewById(R.id.textView1);
 			  txtBarcode.setText(scanResult.getContents().toString());
 			  
 		  } else {
 			  Toast.makeText(this, "No data!", Toast.LENGTH_SHORT).show();
 		  }
 		
 	}
 
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
