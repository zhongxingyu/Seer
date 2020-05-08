 package ru.spbau.qrmenu;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * @author Osipov Stanislav
  */
 public class QRMenuStartActivity extends Activity {
 
     private static final int SCAN_QR_CODE_REQUEST_CODE = 0;
     public final static String EXTRA_MESSAGE = "ru.spbau.qrmenu.QRMenuStartActivity.MESSAGE";
 
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.start);
         Button b = (Button) findViewById(R.id.button);
         b.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                 intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                 startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
             }
         });
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (SCAN_QR_CODE_REQUEST_CODE == requestCode) {
            if (resultCode == RESULT_OK) {
                String jsonTable = data.getStringExtra("SCAN_RESULT");
                if(EntitiesSerializationHelper.parseTable(jsonTable) != null) {
                    startMainActivity(jsonTable);
                    return;
                }
             }
            showRepeatScanMessage();
         }
     }
 
    private void showRepeatScanMessage() {
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText("Repeat scan, please");
    }

     private void startMainActivity(String result) {
         Intent intent = new Intent(this, QRMenuMainActivity.class);
         intent.putExtra(EXTRA_MESSAGE, result);
         startActivity(intent);
     }
 }
