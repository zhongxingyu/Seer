 package com.tedebold.buylist;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.tedebold.provider.helpers.ProductHelper.Products;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 
 public class Save extends Activity implements OnClickListener {
     static final int DIALOG_ID = 0;
     static final String SUCCESS_MESSAGE = "Information saved successfully ! Add Another Product";
     static final String KEY = "AIzaSyAdPullFIgDyP7wG1uZH-qd69SV7fBBPzo";
     static final String BARCODE_SCANNER_PACKAGE= "com.google.zxing.client.android";
     private AdView adView;
     private static final String MY_AD_UNIT_ID = "a14f6b426ebd4fc";
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.form);
         addAds();
         View add = findViewById(R.id.add);
         add.setOnClickListener(this);
         View scan = findViewById(R.id.scan);
         scan.setOnClickListener(this);
         View cancel = findViewById(R.id.cancel);
         cancel.setOnClickListener(this);
 
         Cursor productCursor = getContentResolver().query(Products.CONTENT_URI,
                 new String[]{Products.NAME, Products.BAR_CODE},
                 null,
                 null,
                 Products.NAME + " ASC");
 
         String[] productList = new String[productCursor.getCount()];
         if (productCursor.moveToFirst()) {
             for (int i = 0; i < productCursor.getCount(); i++) {
                 productList[i] = productCursor.getString(0) + "-" + productCursor.getString(1);
                 productCursor.moveToNext();
             }
         }
         productCursor.close();
 
         ArrayAdapter<String> products = new ArrayAdapter<String>(this,
                 android.R.layout.simple_dropdown_item_1line, productList);
 
         AutoCompleteTextView find = (AutoCompleteTextView) findViewById(R.id.form_product_name);
         find.setAdapter(products);
         find.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                 String[] productData = arg0.getItemAtPosition(arg2).toString().split("-");
                 addToList(productData[1]);
                 Save.this.finish();
             }
         });
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         if (requestCode == 0) {
             if (resultCode == RESULT_OK) {
                 String barCode = intent.getStringExtra("SCAN_RESULT");
                 if (addToList(barCode)) {
                     Save.this.finish();
                 } else {
                     //requestProduct(barCode);
                     TextView code = (TextView) findViewById(R.id.code);
                     code.setText(barCode);
                 }
             } else if (resultCode == RESULT_CANCELED) {
                 Save.this.finish();
             }
         }
     }
 
     private boolean addToList(String barCode) {
         String where = Products.BAR_CODE + "=" + barCode;
         ContentValues values = new ContentValues();
         values.put(Products.IN_LIST.toString(), "true");
         return getContentResolver().update(Products.CONTENT_URI, values, where, null) == 1;
     }
 
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.cancel:
                 finish();
                 break;
             case R.id.add:
                 EditText nameInput = (EditText) findViewById(R.id.form_product_name);
                 EditText barCodeInput = (EditText) findViewById(R.id.code);
 
                 String name = nameInput.getText().toString();
                 String barCode = barCodeInput.getText().toString();
                if ( name != null && !name.equals("") && barCode != null && !barCode.equals("")) {
                   ContentValues values = new ContentValues();
                   values.put(Products.NAME, name);
                   values.put(Products.BAR_CODE, barCode);
                   values.put(Products.IN_LIST.toString(), "true");
                   getContentResolver().insert(Products.CONTENT_URI, values);
                   showDialog(DIALOG_ID);
                 } else {
                   Toast.makeText(getApplicationContext(), getString(R.string.validation), Toast.LENGTH_SHORT).show();
                 }
                 break;
             case R.id.scan:
               try{
                 Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                 intent.setPackage(BARCODE_SCANNER_PACKAGE);
                 intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
                 startActivityForResult(intent, 0);
                 break;
               } catch (ActivityNotFoundException e) {
                 Toast.makeText(getApplicationContext(),"you don't have the required barcode scanner app", Toast.LENGTH_SHORT).show();
                 Uri marketUri = Uri.parse("market://details?id=" + BARCODE_SCANNER_PACKAGE);
                 Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                 startActivity(marketIntent);
               }
             default:
                 break;
         }
     }
 
     protected final Dialog onCreateDialog(final int id) {
         Dialog dialog = null;
         switch (id) {
             case DIALOG_ID:
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(SUCCESS_MESSAGE)
                         .setCancelable(false)
                         .setPositiveButton("No", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 Save.this.finish();
                             }
                         }).setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
                         clearForm();
                     }
                 });
                 AlertDialog alert = builder.create();
                 dialog = alert;
                 break;
             default:
         }
         return dialog;
     }
 
     private void clearForm() {
         TextView code = (TextView) findViewById(R.id.code);
         code.setText("");
         TextView name = (TextView) findViewById(R.id.form_product_name);
         name.setText("");
         name.requestFocus();
 
     }
     private void addAds(){
         // Create the adView
         adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);
         LinearLayout layout = (LinearLayout)findViewById(R.id.adsform);
         // Add the adView to it
         layout.addView(adView);
         // Initiate a generic request to load it with an ad
         adView.loadAd(new AdRequest());
     }
     private void requestProduct(String barCode) {
         BufferedReader in = null;
         try {
             HttpClient client = new DefaultHttpClient();
             HttpGet request = new HttpGet();
             request.setURI(new URI("https://www.googleapis.com/shopping/search/v1/public/products?key=" + KEY + "&country=AR&q=" + barCode));
             HttpResponse response = client.execute(request);
             in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
             StringBuffer sb = new StringBuffer("");
             String line = "";
             String NL = System.getProperty("line.separator");
             while ((line = in.readLine()) != null) {
                 sb.append(line + NL);
             }
             in.close();
             String page = sb.toString();
             System.out.println(page);
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 }
