 package edu.mit.printAtMIT.view;
 
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import edu.mit.printAtMIT.R;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Shows printer info; name, location, status, picture, relevant stuff
  * 
  * Menu: Refresh View on Map Home Settings About
  */
 public class PrinterInfoActivity extends Activity {
     public static final String TAG = "PrinterInfoActivity";
     public static final String REFRESH_ERROR = "Error, please try again";
 
     private static final int REFRESH_ID = Menu.FIRST;
 
     private PrintersDbAdapter mDbAdapter;
     private boolean favorite;
     private Button button02;
     private String id;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i("PrinterInfoActivity", "onCreate");
         Parse.initialize(this, "KIb9mNtPKDtkDk7FJ9W6b7MiAr925a10vNuCPRer",
                 "dSFuQYQXSvslh9UdznzzS9Vb0kDgcKnfzgglLUHT");
         setContentView(R.layout.printer_info);
 
         Bundle extras = getIntent().getExtras();
         id = extras.getString("id");
 
         // set favorite state of printer
         mDbAdapter = new PrintersDbAdapter(this);
         mDbAdapter.open();
         favorite = mDbAdapter.isFavorite(id);
 
         Button button01 = (Button) findViewById(R.id.button01);
         button02 = (Button) findViewById(R.id.button02);
 
         button01.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
                 Intent intent = new Intent(view.getContext(),
                         PrinterMapActivity.class);
                 intent.putExtra("id", id);
                 startActivity(intent);
             }
         });
         button02.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Log.i(TAG, "clicking favorite button");
                 if (favorite) {
                     mDbAdapter.removeFavorite(id);
                     button02.setText("Add to Favorites");
                     favorite = false;
                 } else {
                     mDbAdapter.addToFavorites(id);
                     button02.setText("Remove from Favorites");
                     favorite = true;
                 }
             }
 
         });
 
         RefreshTask task = new RefreshTask();
         task.execute();
         mDbAdapter.close();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Log.i("PrinterInfoActivity", "onPause");
         mDbAdapter.close();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Log.i("PrinterInfoActivity", "onResume");
         if (favorite) {
             button02.setText("Remove from favorites");
         } else {
             button02.setText("Add to favorites");
         }
         mDbAdapter.open();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(0, REFRESH_ID, 0, "Refresh");
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch (item.getItemId()) {
         case REFRESH_ID:
             RefreshTask task = new RefreshTask();
             task.execute();
             return true;
         }
 
         return super.onMenuItemSelected(featureId, item);
     }
 
     /**
      * Refreshes printer data Sets Textview.
      * 
      * @throws ParseException
      */
     private String refresh() throws ParseException {
 
         // Parse makes call to cloud to retrieve printer information
         Log.i(TAG, "refresh()");
         ParseQuery query = new ParseQuery("PrintersData");
         ParseObject printer;
         printer = query.get(id);
         return this.constructInfo(printer);
     }
 
     private String getStatus(int code) {
         switch (code) {
         case 0:
             return "Ready";
         case 1:
             return "Busy";
         case 2:
             return "Error";
         default:
             Log.e("PrinterInfoActivity", "shouldn't get here, yo");
             break;
         }
         return null;
     }
 
     private String constructInfo(ParseObject printer) {
         StringBuilder name = new StringBuilder("Printer Name: ");
         StringBuilder info = new StringBuilder("Front Panel Message: " + "\n");
         StringBuilder status = new StringBuilder("Status: ");
         StringBuilder paperJam = new StringBuilder("Paper Jam: ");
         StringBuilder paperStatus = new StringBuilder("Paper Status: ");
         StringBuilder tonerStatus = new StringBuilder("Toner Status: ");
         name.append(printer.getString("printerName"));
         info.append(printer.getString("FrontPanelMessage") + "\n");
         if (printer.getString("status") != null) {
             status.append(this.getStatus(Integer.parseInt(printer
                     .getString("status"))));
         }
         if (printer.getString("line2") != null) {
             info.append(printer.getString("line2") + "\n");
         }
         if (printer.getString("line3") != null) {
             info.append(printer.getString("line3") + "\n");
         }
         if (printer.getString("line4") != null) {
             info.append(printer.getString("line4") + "\n");
         }
         if (printer.getString("line5") != null) {
             info.append(printer.getString("line5"));
         }
         if (printer.getString("PaperJamStatus") != null) {
             paperJam.append(printer.getString("PaperJamStatus"));
         }
         if (printer.getString("PaperStatus") != null) {
             paperStatus.append(printer.getString("PaperStatus"));
         }
         if (printer.getString("TonerStatus") != null) {
             tonerStatus.append(printer.getString("TonerStatus"));
         }
 
         String result = name.toString() + "\n\n" + info.toString() + "\n\n"
                 + status.toString() + "\n\n" + paperJam.toString() + "\n\n"
                 + paperStatus.toString() + "\n\n" + tonerStatus.toString();
         return result;
     }
 
     public class RefreshTask extends AsyncTask<Void, byte[], String> {
         private ProgressDialog dialog;
 
         @Override
         protected void onPreExecute() {
             Log.i(TAG, "RefreshTask onPreExecute");
             dialog = ProgressDialog.show(PrinterInfoActivity.this, "",
                     "Refreshing Printer Data", true);
         }
 
         @Override
         protected String doInBackground(Void... params) { // This runs on a
                                                           // different thread
             String result = "";
             try {
                 result = refresh();
             } catch (ParseException e) {
                 // e.printStackTrace();
                 Log.e(TAG, "RefreshTask Parse NUBFAIL");
                 result = PrinterInfoActivity.REFRESH_ERROR;
             }
             return result;
         }
 
         @Override
         protected void onCancelled() {
             Log.i(TAG, "RefreshTask Cancelled.");
         }
 
         @Override
         protected void onPostExecute(String result) {
            dialog.dismiss();
 
             if (result.equals(PrinterInfoActivity.REFRESH_ERROR)) {
                 Toast.makeText(getApplicationContext(),
                         "Error getting data, please try again later",
                         Toast.LENGTH_SHORT).show();
                 Log.i(TAG,
                         "RefreshTask onPostExecute: Completed with an Error.");
            } else {
                Toast.makeText(getApplicationContext(),
                        "Printer data refreshed", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "RefreshTask onPostExecute: Completed.");
            }
             TextView tv = (TextView) findViewById(R.id.printer_info_text);
             tv.setText(result);
         }
     }
 
 }
