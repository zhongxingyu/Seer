 package org.join.zxing.simple;
 
 import org.join.zxing.CaptureActivity;
 import org.join.zxing.Contents;
 import org.join.zxing.Intents;
 import org.join.zxing.encode.QRCodeEncoder;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.WriterException;
 import com.google.zxing.client.result.ParsedResult;
 import com.google.zxing.client.result.ParsedResultType;
 import com.google.zxing.client.result.TextParsedResult;
 
 public class MainActivity extends Activity implements OnClickListener {
 
     static final String TAG = "MainActivity";
 
     private static final int REQ_CAPTURE = 0x0001;
 
     private Button scanQRcode, generateQRcode;
     private EditText editText;
     private TextView textView;
     private ImageView imageView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         initViews(savedInstanceState);
     }
 
     private void initViews(Bundle state) {
         scanQRcode = (Button) findViewById(R.id.scanQRcode);
         generateQRcode = (Button) findViewById(R.id.generateQRcode);
         scanQRcode.setOnClickListener(this);
         generateQRcode.setOnClickListener(this);
 
         editText = (EditText) findViewById(R.id.editText);
         textView = (TextView) findViewById(R.id.textView);
         imageView = (ImageView) findViewById(R.id.imageView);
     }
 
     @Override
     @SuppressWarnings("deprecation")
     public void onClick(View v) {
         switch (v.getId()) {
         case R.id.scanQRcode:
             toCaptureActivity();
             break;
         case R.id.generateQRcode:
             String text = editText.getText().toString().trim();
             if (text.equals("")) {
                 return;
             }
 
             WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
             Display display = manager.getDefaultDisplay();
             int width = display.getWidth();
             int height = display.getHeight();
             int smallerDimension = width < height ? width : height;
             smallerDimension = smallerDimension * 7 / 8;
 
             generateQRCode(text, smallerDimension);
             break;
         }
     }
 
     private void toCaptureActivity() {
         Intent intent = new Intent(this, CaptureActivity.class);
         intent.setAction(Intents.Scan.ACTION);
         /*intent.putExtra(Intents.Scan.WIDTH, 400);
         intent.putExtra(Intents.Scan.HEIGHT, 300);*/
         startActivityForResult(intent, REQ_CAPTURE);
     }
 
     private void generateQRCode(String text, int dimension) {
         Intent intent = new Intent(Intents.Encode.ACTION);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE);
         /** Contents.Type comments for details */
         intent.putExtra(Intents.Encode.TYPE, parseContentsType(text));
         intent.putExtra(Intents.Encode.DATA, text);
         try {
             QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(this, intent, dimension, false);
             Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
             if (bitmap == null) {
                 Log.w(TAG, "Could not encode barcode");
                 Toast.makeText(this, R.string.msg_encode_contents_failed, Toast.LENGTH_SHORT)
                         .show();
             } else {
                 String title = qrCodeEncoder.getTitle();
                 String contents = qrCodeEncoder.getContents();
                 textView.setText(title + "," + contents);
                 imageView.setImageBitmap(bitmap);
             }
         } catch (WriterException e) {
             e.printStackTrace();
         }
     }
 
     private String parseContentsType(String text) {
         ParsedResult result = new TextParsedResult(text, null);
         switch (result.getType()) {
         case EMAIL_ADDRESS:
             return Contents.Type.EMAIL;
         case TEL:
             return Contents.Type.PHONE;
         case SMS:
             return Contents.Type.SMS;
         case URI:
         case TEXT:
         default:
             return Contents.Type.TEXT;
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQ_CAPTURE) {
             if (resultCode == RESULT_OK) {
                 String result = data.getStringExtra(Intents.Scan.RESULT);
                 ParsedResultType type = ParsedResultType.values()[data.getIntExtra(
                         Intents.Scan.RESULT_TYPE, ParsedResultType.TEXT.ordinal())];
                 String format = data.getStringExtra(Intents.Scan.RESULT_FORMAT);
                 byte[] rawBytes = data.getByteArrayExtra(Intents.Scan.RESULT_BYTES);
 
                 textView.setText(format + "," + type + "," + result);
                 Bitmap bm = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length);
                 imageView.setImageBitmap(bm);
             }
         } else {
             super.onActivityResult(requestCode, resultCode, data);
         }
     }
 
 }
