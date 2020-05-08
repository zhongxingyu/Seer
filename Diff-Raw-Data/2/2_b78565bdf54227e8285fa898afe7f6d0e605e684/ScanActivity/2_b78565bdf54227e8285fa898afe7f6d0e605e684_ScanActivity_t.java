 package org.csie.mpp.buku;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class ScanActivity extends Activity implements OnClickListener {
 	public static final int REQUEST_CODE = 1436;
 	public static final String ISBN = "isbn";
 	
 	protected EditText isbn;
 	protected Button barcode;
 	protected Button lookup;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.scan);
 
         isbn = (EditText)findViewById(R.id.isbn);
         barcode = (Button)findViewById(R.id.barcode);
         lookup = (Button)findViewById(R.id.look_up);
 
         barcode.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
		        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 		        intent.putExtra("SCAN_MODE", "ONE_D_MODE");
 		        startActivityForResult(intent, 0);
 			}
         });
         
         lookup.setOnClickListener(this);
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         if (requestCode == 0) {
             if (resultCode == RESULT_OK) {
                 String contents = intent.getStringExtra("SCAN_RESULT");
                 isbn.setText(contents);
             }
             else if (resultCode == RESULT_CANCELED) {
                 //TODO(ianchou): show error message
             }
         }
 
     }
 
 	@Override
 	public void onClick(View v) {
 		String input = isbn.getText().toString();
 		
 		Intent data = new Intent();
 		data.putExtra(ScanActivity.ISBN, input);
 		setResult(RESULT_OK, data);
 		finish();
 	}
 }
