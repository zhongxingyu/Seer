 package com.tedebold.buylist;
 import android.net.Uri;
 import android.text.Html;
import android.util.Log;
 import android.view.*;
 import com.google.ads.*;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.widget.*;
 import com.tedebold.Model.Product;
 import com.tedebold.data.CustomAdapter;
 import com.tedebold.provider.helpers.ProductHelper;
 
 import java.util.ArrayList;
 
 public class ProductList extends ListActivity {
   private AdView adView;
   private static final String MY_AD_UNIT_ID = "a14f6b426ebd4fc";
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.buylist);
     addAds();
     fillData();
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     fillData();
   }
 
   public void addToList(View v) {
     Intent newIntent = new Intent(v.getContext(), Save.class);
     startActivity(newIntent);
   }
 
   private void fillData() {
     this.setListAdapter(new CustomAdapter(this, R.layout.row, getProductsList()));
   }
 
   private void addAds(){
     // Create the adView
     adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);
     LinearLayout layout = (LinearLayout)findViewById(R.id.adsList);
     // Add the adView to it
     layout.addView(adView);
     // Initiate a generic request to load it with an ad
     adView.loadAd(new AdRequest());
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
       case R.id.message: {
         sendByMessage(this.getListView());
         break;
       }
       case R.id.email: {
         sendByMail(this.getListView());
         break;
       }
     }
     return true;
   }
 
   private ArrayList<Product> getProductsList() {
     String[] displayFields = new String[]{
         ProductHelper.Products.NAME,
         ProductHelper.Products._ID,
         ProductHelper.Products.IN_LIST,
         ProductHelper.Products.BAR_CODE
     };
     ArrayList<Product> products = new ArrayList<Product>();
     String where = ProductHelper.Products.IN_LIST + "='true'";
     Cursor productList =  managedQuery(ProductHelper.Products.CONTENT_URI, displayFields, where, null, null);
 
     if(productList == null || !productList.moveToFirst()) return products;
 
     do {
       products.add(new Product(Integer.parseInt(productList.getString(1)),
           productList.getString(3),
           productList.getString(0)));
     } while (productList.moveToNext());
 
     return products;
   }
 
   private String getProductMessage() {
     String productsMessage = "";
     ArrayList<Product> prods = getProductsList();
     for (int i = 0 ; i < prods.size(); i++ ){
       productsMessage += prods.get(i).getName() + ", ";
     }
    if (productsMessage.equals("")) {
       return productsMessage;
     } else {
       return productsMessage.substring(0,productsMessage.length() - 2);
     }
   }
 
   public void sendByMessage(View v){
     Intent sendIntent = new Intent(Intent.ACTION_VIEW);
     sendIntent.setData(Uri.parse("sms:"));
     sendIntent.putExtra("sms_body", getProductMessage());
     startActivity(sendIntent);
   }
 
   public void sendByMail(View v){
     Intent emailIntent = new Intent(Intent.ACTION_SEND);
     emailIntent.setType("text/html");
     emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Buylist");
     emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getProductMessage()));
     startActivity(Intent.createChooser(emailIntent,  getString(R.string.emailSelector)));
   }

  public void removeItem(View v){
    String barcode = ((TextView)((LinearLayout)v.getParent()).findViewById(R.id.bottomtext)).getText().toString();
    ((CustomAdapter)getListView().getAdapter()).remove(barcode);
  }
 }
