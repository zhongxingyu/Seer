 package net.m_kawato.tabletpos;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.R.drawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Loading extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
     private static final String TAG = "Loading";
     private static final int REQCODE_FILEPICKER = 1001;
     private Globals globals;
     private List<Product> productsInCategory; // products in the selected category
     // private LoadingProductListAdapter productListAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_loading);
 
         globals = (Globals) this.getApplication();
         this.productsInCategory = new ArrayList<Product>();
 
         // Spinner for category selection
         ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
         categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         for(String category: globals.categories) {
             categoryAdapter.add(category);
         }
         Spinner spnCategory = (Spinner) findViewById(R.id.spn_category);
         spnCategory.setAdapter(categoryAdapter);    
         spnCategory.setOnItemSelectedListener(this);
 
         // Event handlers of buttons (Clear All, Top Menu, Import, Stock)
         Button btnClearAll = (Button) findViewById(R.id.btn_clear_all);
         btnClearAll.setOnClickListener(this);
 
         Button btnTopMenu = (Button) findViewById(R.id.btn_topmenu);
         btnTopMenu.setOnClickListener(this);
 
         Button btnImport = (Button) findViewById(R.id.btn_import_loading);
         btnImport.setOnClickListener(this);
 
         Button btnStock = (Button) findViewById(R.id.btn_stock);
         btnStock.setOnClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.loading, menu);
         return true;
     }
     
     @Override
     protected void onDestroy() {
         Log.d(TAG, "onDestroy");
         globals.saveLoading();
         super.onDestroy();
     }
 
     // Event handler for check boxes and navigation buttons
     @Override
     public void onClick(View v) {
         Intent i;
         int position;
         boolean checked;
         Product p;
 
         switch (v.getId()) {
         case R.id.btn_clear_all:
             Log.d(TAG, "Clear All button is clicked");
             for (Product product: globals.products) {
                 product.loaded = false;
                 product.stock = 0;
             }
             updateProductTable();
             break;
         case R.id.btn_topmenu:
             Log.d(TAG, "Top Menu button is clicked");
             globals.saveLoading();
             i = new Intent(this, TopMenu.class);
             startActivity(i);
             break;
         case R.id.btn_import_loading:
             i = new Intent(this, LoadingFilePicker.class);
             startActivityForResult(i, REQCODE_FILEPICKER);
             break;
         case R.id.btn_stock:
             Log.d(TAG, "Stock button is clicked");
             globals.saveLoading();
             i = new Intent(this, Stock.class);
             startActivity(i);
             break;
         case R.id.image:
             position = (Integer) v.getTag();
             Log.d(TAG, String.format("onClick: image position=%d", position));
             View orderItemView = (View) v.getParent();
             CheckBox checkBoxView = (CheckBox) orderItemView.findViewById(R.id.loading_checked);
             checked = ! checkBoxView.isChecked();
             checkBoxView.setChecked(checked);
             p = this.productsInCategory.get(position);
             p.loaded = checked;
             break;
         case R.id.loading_checked:
             position = (Integer) v.getTag();
             checked = ((CheckBox) v).isChecked();
             Log.d(TAG, String.format("onClick: loading_checked position=%d, checked=%b", position, checked));
             p = this.productsInCategory.get(position);
             p.loaded = checked;
             break;
         }        
     }
 
     // Event handler for category selector
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
 
     @Override
     protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQCODE_FILEPICKER) {
            if (resultCode == RESULT_OK) {
                String filename = data.getStringExtra("filename");
                Log.d(TAG, "onActivityResult: filename = " + filename);
                loadStockData(filename);
               updateProductTable();
            }
        }
     }
            
     // Change selected category of products
     private void changeCategory(String category) {
         Log.d(TAG, String.format("changeCategory: %s", category));
         this.productsInCategory.clear();
         for (Product product: globals.products) {
             if (! product.category.equals(category)) {
                 continue;
             }
             this.productsInCategory.add(product);
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
 
             LinearLayout loadingItem = (LinearLayout) inflater.inflate(R.layout.loading_item, null);
             tableRow.addView(loadingItem);
             
             String imgFilePath =
                     String.format("%s/%s/%s-%d.jpg",
                     Globals.SDCARD_DIRNAME,
                     Globals.IMAGE_DIRNAME,
                     Globals.PRODUCT_IMAGE_PREFIX,
                     p.productId);
             File imgFile = new File(Environment.getExternalStorageDirectory(), imgFilePath);
             ImageView imageView = (ImageView) loadingItem.findViewById(R.id.image);
             if (imgFile.canRead()) {
                 Log.d(TAG, "image file = " + imgFile.toString());
                 Bitmap bm = BitmapFactory.decodeFile(imgFile.getPath());
                 imageView.setImageBitmap(bm);
             } else {
                 imageView.setImageResource(drawable.ic_menu_gallery);
             }
             imageView.setTag(position);
             imageView.setOnClickListener(this);
 
             CheckBox checkBox = (CheckBox) loadingItem.findViewById(R.id.loading_checked);
             checkBox.setTag(position);
             checkBox.setChecked(p.loaded);
             checkBox.setOnClickListener(this);
 
             TextView productNameView = (TextView) loadingItem.findViewById(R.id.product_name);
             productNameView.setText(String.format("%d, %s", p.productId, p.productName));
             
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
 
     // Update check state of product table
     private void updateProductTable() {
         buildProductTable();
         // TODO update only check boxes instead of the whole table
     }
 
     // load stock data from selected file
     private void loadStockData(String basename) {
         Log.d(TAG, "loadStockData: " + basename);
         String sdcardPath = Environment.getExternalStorageDirectory().getPath();
         String filename = String.format("%s/%s/%s", sdcardPath, Globals.SDCARD_DIRNAME, basename);
         List<String> lines = new ArrayList<String>();
         try {
             File stockFile = new File(filename);
             FileInputStream fin = new FileInputStream(stockFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
             String line;
             while((line = reader.readLine()) != null){     
                 lines.add(line);    
             } 
             fin.close();
         } catch (IOException e) {
             Log.e(TAG, "Failed to load stock data");
             AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
             alertDialogBuilder.setCancelable(false);
             alertDialogBuilder.setTitle("Error");
             alertDialogBuilder.setMessage("Failed to load stock data: \n" + filename);
             alertDialogBuilder.setPositiveButton("OK",
                     new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.cancel();
                         }
                     });
             alertDialogBuilder.create().show();
             return;
         }
 
         for(int i = 0; i < lines.size(); i++) {
             String[] lineSplit = lines.get(i).split("\\s*?,\\s*?");
             int productId = -1;
             int stock = -1;
             try {
                 productId = Integer.parseInt(lineSplit[1]);
                 stock = Integer.parseInt(lineSplit[2]);
             } catch (NumberFormatException e) {
                 continue;
             }
             Product p = globals.getProduct(productId);
             if (p == null) {
                 Log.w(TAG, "loadStockData: failed to find productId: " + productId);
                 continue;
             }
             p.loaded = true;
             p.stock = stock;
             Log.d(TAG, String.format("loadStockData: productName=%s (%d), stock=%d", p.productName, productId, stock));
         }
         Toast.makeText(this, "Loading data has been imported.", Toast.LENGTH_LONG).show();
     }
 }
