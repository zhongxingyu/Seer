 package net.m_kawato.tabletpos;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.util.Calendar;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class Receipt extends Activity implements OnClickListener {
     private static final String TAG = "Receipt";
     private Globals globals;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "onCreate");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_receipt);
 
         this.globals = (Globals) this.getApplication();
 
         // Insert/update receipt to database
         writeReceiptToDb();
         
         // Build ListView for order list
         ListView orderListView = (ListView) findViewById(R.id.list);
         LayoutInflater inflater = this.getLayoutInflater();
         View header = inflater.inflate(R.layout.receipt_header, null);
         View footer = inflater.inflate(R.layout.receipt_footer, null);
         orderListView.addHeaderView(header);
         orderListView.addFooterView(footer);
 
        String[] from = {"product_name", "unit_price", "unit_price_box", "quantity", "amount"};
        int[] to = {R.id.product_name, R.id.unit_price, R.id.unit_price_box, R.id.quantity, R.id.amount};
         SimpleAdapter orderListAdapter = new SimpleAdapter(this, globals.orderItemList, R.layout.receipt_item, from, to);
         orderListView.setAdapter(orderListAdapter);        
 
         // Loading sheet number
         TextView loadingSheetNumberView = (TextView) findViewById(R.id.loading_sheet_number);
         loadingSheetNumberView.setText(globals.loadingSheetNumber);
         
         // date, route and place
         Log.d(TAG, String.format("onCreate: selectedRoute=%s, routes.size=%s",
                 globals.selectedRoute, globals.routes.size()));
         TextView dateView = (TextView) findViewById(R.id.date);
         String dateText = DateFormat.format("dd-MM-yyyy", Calendar.getInstance()).toString();
         dateView.setText(dateText);
 
         TextView routeView = (TextView) findViewById(R.id.route);
         String routeCode = globals.routes.get(globals.selectedRoute);
         String routeName = globals.routeName.get(routeCode);
         routeView.setText(String.format("%s (%s)", routeName, routeCode));
         
         TextView placeView = (TextView) findViewById(R.id.place);
         String placeCode = globals.places.get(routeCode).get(globals.selectedPlace);
         String placeName = globals.placeName.get(placeCode);
         placeView.setText(String.format("%s (%s)", placeName, placeCode));
 
         // total amount, credit amount and cash amount
         TextView totalAmountView = (TextView) findViewById(R.id.total_amount);
         totalAmountView.setText(globals.totalAmount.toString() + " " + getString(R.string.currency));
 
         TextView creditAmountView = (TextView) findViewById(R.id.credit_amount);
         creditAmountView.setText(globals.creditAmount.toString() + " " + getString(R.string.currency));
 
         BigDecimal cashAmount = globals.totalAmount.subtract(globals.creditAmount);
         TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
         cashAmountView.setText(cashAmount.toString() + " " + getString(R.string.currency));
         
         // "Confirm" button
         Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
         btnConfirm.setOnClickListener(this);
 
         // "Enter New Order" button (aka. go to top)
         Button btnGotoTop = (Button) findViewById(R.id.btn_gototop);
         btnGotoTop.setOnClickListener(this);
         
         // "Export" button
         Button btnExport = (Button) findViewById(R.id.btn_export);
         btnExport.setOnClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.receipt, menu);
         return true;
     }
 
     @Override
     public void onClick(View v) {
         Intent i;
         switch (v.getId()) {
         case R.id.btn_confirm:
             i = new Intent(this, Confirm.class);
             startActivity(i);
             break;
         case R.id.btn_gototop:
             // reset order status and return to Order page
             globals.initialize();
             globals.incrTransactionId();
             i = new Intent(this, Order.class);
             startActivity(i);
             break;
         case R.id.btn_export:
             exportReceipt();
             break;
         }        
     }
 
     // Export receipt data to sdcard
     private void exportReceipt() {
         Log.d(TAG, "exportReceipt");
 
         String sdcardPath = Environment.getExternalStorageDirectory().getPath();
         String dirname = String.format("%s/%s", sdcardPath, Globals.SDCARD_DIRNAME);
         String filename = String.format("%s/%s_%s.%s", dirname,
                 Globals.RECEIPT_PREFIX,
                 DateFormat.format("dd_MM_yyyy", Calendar.getInstance()).toString(),
                 Globals.RECEIPT_SUFFIX);
         String timestamp = DateFormat.format("dd-MM-yyyy kk:mm", Calendar.getInstance()).toString();
         Log.d(TAG, String.format("exportReceipt: filename=%s, timestamp=%s", filename, timestamp));
 
         String routeCode = globals.routes.get(globals.selectedRoute);
         String routeName = globals.routeName.get(routeCode);
         String placeCode = globals.places.get(routeCode).get(globals.selectedPlace);
         String placeName = globals.placeName.get(placeCode);
         
         PrintWriter writer = null;
         try {
             File dir = new File(dirname);
             if (! dir.exists()) {
                 dir.mkdirs();  
             }
             File outfile = new File(filename);
             String header = null;
             if (! outfile.exists()) {
                 header = "Sale Date,Route Name,Route Code,RRP Name,RRP Code,Credit Amount,Product Name,Product Code,Quantity,Loading Sheet Number";
             }
             FileOutputStream fout = new FileOutputStream(filename, true);
             writer = new PrintWriter(fout);
             if (header != null) {
                 writer.println(header);
             }
 
             boolean firstItem = true;
             for (Product item: globals.orderItems) {
                 String creditAmountText = null;
                 if (firstItem) {
                     creditAmountText = globals.creditAmount.toString();
                     firstItem = false;
                 } else {
                     creditAmountText = "0";
                 }
                 writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%d,%d,%s",
                         timestamp, routeName, routeCode, placeName, placeCode,
                         creditAmountText, item.productName, item.productId, item.quantity,
                         globals.loadingSheetNumber));
             }
         
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("The receipt was successfully exported to\n" + filename);
             builder.setPositiveButton(android.R.string.ok, null);
             builder.show();
         } catch (IOException e) {
             Log.e(TAG, "failed to write receipt to sdcard");
             e.printStackTrace();
         } finally {
             writer.close();
         }  
     }
 
     // Insert/update receipt to SQLite DB
     private void writeReceiptToDb() {
         Log.d(TAG, "writeReceiptToDb");
         PosDbHelper dbHelper = new PosDbHelper(this);
         SQLiteDatabase db = dbHelper.getReadableDatabase();
 
         String routeCode = globals.routes.get(globals.selectedRoute);
         String placeCode = globals.places.get(routeCode).get(globals.selectedPlace);
 
         // delete the old order with the current transaction ID
         String deleteOrder = "DELETE FROM orders WHERE transaction_id = ?";
         db.execSQL(deleteOrder, new String[] {Long.toString(globals.transactionId)});
         
         // insert the latest order
         String insertOrder = "INSERT INTO orders " +
                 " (transaction_id, loading_sheet_number, route, place, quantity, credit) " +
                 " VALUES (?, ?, ?, ?, ?, ?)";
 
         boolean firstItem = true;
         for (Product item: globals.orderItems) {
             String creditAmountText = null;
             if (firstItem) {
                 creditAmountText = globals.creditAmount.toString();
                 firstItem = false;
             } else {
                 creditAmountText = "0";
             }
             db.execSQL(insertOrder, new String[] {
                     Long.toString(globals.transactionId),
                     globals.loadingSheetNumber,
                     routeCode,
                     placeCode,
                     Integer.toString(item.quantity),
                     creditAmountText});
         }
         Log.d(TAG, "writeReceiptToDb completed");
     }
 }
