 package net.m_kawato.tabletpos;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.R.drawable;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class Order extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener, TextView.OnEditorActionListener {
     private static final String TAG = "Order";
     private Globals globals;
     private List<Product> productsInCategory; // products in the selected category
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_order);
 
         globals = (Globals) this.getApplication();
         this.productsInCategory = new ArrayList<Product>();
         Log.d(TAG, "onCreate: products.size() = " + globals.products.size());
         OrderInputHelper orderInputHelper = new OrderInputHelper(this, globals);
         
         // Loading sheet number
         EditText loadingSheetNumberView = (EditText) findViewById(R.id.loading_sheet_number);
         loadingSheetNumberView.setText(globals.loadingSheetNumber);
         loadingSheetNumberView.setOnEditorActionListener(this);
         Button btnEnter = (Button) findViewById(R.id.btn_enter);
         btnEnter.setOnClickListener(this);
 
         // Spinner for route, shop, category selection
         orderInputHelper.buildRouteSelector();
         orderInputHelper.buildPlaceSelector();
         ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
         categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         for(String category: globals.categories) {
             categoryAdapter.add(category);
         }
         Spinner spnCategory = (Spinner) findViewById(R.id.spn_category);
         spnCategory.setAdapter(categoryAdapter);    
         spnCategory.setOnItemSelectedListener(this);
 
         // "Confirm" button
         Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
         btnConfirm.setOnClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.order, menu);
         return true;
     }
 
     // Event handler for EditText (loading sheet number)
     @Override  
     public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         Log.d(TAG, "onEditorAction: actionId=" + actionId);
         if (actionId == EditorInfo.IME_ACTION_GO) {  
             updateLoadingSheetNumber();
         }  
         return true;  
     }
 
     // Event handler for spinner (category selection)
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         Log.d(TAG, String.format("onItemSelected: id=%x, position=%d", parent.getId(), position));
         switch (parent.getId()) {
         case R.id.spn_category:
             changeCategory(globals.categories.get(position));
             break;
         }
     }
     @Override
     public void onNothingSelected(AdapterView<?> parent) {
         Log.d(TAG, "onNothingSelected");
     }
 
     // Event handler for buttons (loading sheet number Enter, Confirm, order check boxes)
     @Override
     public void onClick(View v) {
         Log.d(TAG, "onClick");
         Intent i;
 
         switch (v.getId()) {
         case R.id.btn_enter:
             updateLoadingSheetNumber();
             break;
         case R.id.btn_confirm:
             i = new Intent(this, Confirm.class);
             startActivity(i);
             break;
         case R.id.order_checked:
            int position = (Integer) v.getTag();
            boolean checked = ((CheckBox) v).isChecked();
             Log.d(TAG, String.format("onClick: order_checked position=%d, checked=%b", position, checked));
            Product p = this.productsInCategory.get(position);
             if (checked) {
                 p.orderItem = new OrderItem(this, this.productsInCategory.get(position), 0);
             } else {
                 p.orderItem = null;
             }
             break;
         }        
     }
 
     // Update loading sheet number
     private void updateLoadingSheetNumber() {
         Log.d(TAG, "updateLoadingSheetNumber");
         EditText loadingSheetNumberView = (EditText) findViewById(R.id.loading_sheet_number);
         String loadingSheetNumber = loadingSheetNumberView.getText().toString();
         if (loadingSheetNumber.equals("")) {
             return;
         }
         globals.setLoadingSheetNumber(loadingSheetNumber);
     }
     
     // Change selected category of products
     private void changeCategory(String category) {
         Log.d(TAG, String.format("changeCategory: %s", category));
         this.productsInCategory.clear();
         for(Product product: globals.products) {
             if (product.category.equals(category) && product.loaded) {
                 this.productsInCategory.add(product);
             }
         }
         buildProductTable();
     }
     
     // Build product table
     private void buildProductTable() {
         TableLayout table = (TableLayout) findViewById(R.id.product_table);
         table.removeAllViews();
         LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         int row = 0;
         TableRow tableRow = new TableRow(this);
         for (int position = 0; position < this.productsInCategory.size(); position++) {
             Product p = this.productsInCategory.get(position);
 
             LinearLayout orderItem = (LinearLayout) inflater.inflate(R.layout.order_item, null);
             tableRow.addView(orderItem);
             
             String imgFilePath =
                     String.format("%s/%s/%s-%d.jpg",
                     Globals.SDCARD_DIRNAME,
                     Globals.IMAGE_DIRNAME,
                     Globals.PRODUCT_IMAGE_PREFIX,
                     p.productId);
             File imgFile = new File(Environment.getExternalStorageDirectory(), imgFilePath);
             ImageView imageView = (ImageView) orderItem.findViewById(R.id.image);
             if (imgFile.canRead()) {
                 Log.d(TAG, "image file = " + imgFile.toString());
                 Bitmap bm = BitmapFactory.decodeFile(imgFile.getPath());
                 imageView.setImageBitmap(bm);
             } else {
                 imageView.setImageResource(drawable.ic_menu_gallery);
             }
 
             CheckBox checkBox = (CheckBox) orderItem.findViewById(R.id.order_checked);
             checkBox.setTag(position);
             checkBox.setChecked(p.orderItem == null ? false : true);
             checkBox.setOnClickListener(this);
 
             TextView productNameView = (TextView) orderItem.findViewById(R.id.product_name);
             productNameView.setText(p.productName);
 
             TextView unitPriceView = (TextView) orderItem.findViewById(R.id.unit_price);
             unitPriceView.setText(p.getFormattedUnitPrice());
 
             row++;
             if (row == 4) {
                 table.addView(tableRow);
                 tableRow = new TableRow(this);
                 row = 0;
             }
         }
         if (row != 0) {
             table.addView(tableRow);
         }
     }
 
 }
