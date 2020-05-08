 package edu.mit.printAtMIT.view;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Comparator;
 import java.util.Map;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import edu.mit.printAtMIT.R;
 import edu.mit.printAtMIT.list.EntryAdapter;
 import edu.mit.printAtMIT.list.Item;
 import edu.mit.printAtMIT.list.PrinterEntryItem;
 import edu.mit.printAtMIT.list.SectionItem;
 
 /**
  * Lists all the printers from database. Shows name, location, status from each
  * printer List of favorite printers on top, then list of all printers
  * 
  * Menu Item: Settings About Home Refresh
  * 
  * Context Menu Items: Favorite, Info, MapView
  */
 
 public class PrinterListActivity extends ListActivity {
     public static final String TAG = "PrinterListActivity";
 
     private static final int REFRESH_ID = Menu.FIRST;
     private Map<String, PrinterEntryItem> map = new HashMap<String, PrinterEntryItem>();
     private PrintersDbAdapter mDbAdapter;
     private PrinterComparator comparator = new PrinterComparator();
 
     public class PrinterComparator implements Comparator<PrinterEntryItem> {
 
         @Override
         public int compare(PrinterEntryItem item1, PrinterEntryItem item2) {
             return item1.printerName.compareTo(item2.printerName);
         }
 
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Parse.initialize(this, "KIb9mNtPKDtkDk7FJ9W6b7MiAr925a10vNuCPRer",
                 "dSFuQYQXSvslh9UdznzzS9Vb0kDgcKnfzgglLUHT");
         setContentView(R.layout.printer_list);
         Button button01 = (Button) findViewById(R.id.button01);
         mDbAdapter = new PrintersDbAdapter(this);
 
         button01.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
                 Intent intent = new Intent(view.getContext(),
                         PrinterMapActivity.class);
                 intent.putExtra("allPrinterView", true);
                 startActivity(intent);
             }
         });
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         Log.i("PrinterListActivity", "Calling onResume()");
         RefreshHashMapTask task = new RefreshHashMapTask();
         task.execute();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Log.i("PrinterListActivity", "Calling onPause()");
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
             RefreshHashMapTask task = new RefreshHashMapTask();
             task.execute();
             return true;
         }
 
         return super.onMenuItemSelected(featureId, item);
     }
 
 
     /**
      * Sets Views, called in UI thread from AsyncTask
      */
     private void setListViewData(List<ParseObject> objects) {
         // resets map if not nulll
         if (objects != null) {
             map = new HashMap<String, PrinterEntryItem>();
             for (ParseObject o : objects) {
                 PrinterEntryItem item = new PrinterEntryItem(o.getObjectId(),
                         o.getString("printerName"), o.getString("location"),
                         Integer.parseInt(o.getString("status")));
                 map.put(o.getObjectId(), item);
             }
         } else {
             // changes all status to unknown
             Map<String, PrinterEntryItem> tmp = new HashMap<String, PrinterEntryItem>();
             for (String key : map.keySet()) {
                 PrinterEntryItem item = map.get(key);
                 tmp.put(key, new PrinterEntryItem(item.parseId,
                         item.printerName, item.location, item.status));
                 map = tmp;
             }
         }
 
         final List<Item> items = new ArrayList<Item>();
         items.add(new SectionItem("Favorites"));
         mDbAdapter.open();
 
         List<String> ids = mDbAdapter.getFavorites();
         List<PrinterEntryItem> favorites = new ArrayList<PrinterEntryItem>();
         for (String id : ids) {
             if (map.containsKey(id)) {
                 favorites.add(map.get(id));
             }
         }
         Collections.sort(favorites, comparator);
         for (PrinterEntryItem item : favorites) {
             items.add(item);
         }
         items.add(new SectionItem("All Printers"));
 
         List<PrinterEntryItem> printers = new ArrayList<PrinterEntryItem>(
                 map.values());
         Collections.sort(printers, comparator);
 
         for (PrinterEntryItem item : printers) {
             items.add(item);
         }
 
         Log.i(TAG, new Integer(items.size()).toString());
         EntryAdapter adapter = new EntryAdapter(this, (ArrayList<Item>) items);
         setListAdapter(adapter);
         mDbAdapter.close();
 
         ListView lv = getListView();
         lv.setTextFilterEnabled(true);
 
         lv.setOnItemClickListener(new OnItemClickListener() {
 
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 Intent intent = new Intent(view.getContext(),
                         PrinterInfoActivity.class);
 
                 if (!items.get(position).isSection()) {
                     intent.putExtra("id",
                             ((PrinterEntryItem) items.get(position)).parseId);
                 }
 
                 startActivity(intent);
             }
 
         });
         Log.i(TAG, "end of fillListData()");
     }
 
     /**
      * Background task that refreshes the hashmap of printers. Modifies map.
      */
     public class RefreshHashMapTask extends
             AsyncTask<Void, byte[], List<ParseObject>> {
         private ProgressDialog dialog;
 
         @Override
         protected void onPreExecute() {
             Log.i(TAG, "RefreshTask onPreExecute");
             dialog = ProgressDialog.show(PrinterListActivity.this, "",
                     "Refreshing Data", true);
         }
 
         @Override
         protected List<ParseObject> doInBackground(Void... arg0) { // happens in
                                                                    // background
                                                                    // thread
             ParseQuery query = new ParseQuery("PrintersData");
             List<ParseObject> objects = null;
             try {
                 objects = query.find();
             } catch (ParseException e) {
                 // swallow exception
                 // e.printStackTrace();
             }
             return objects;
         }
 
         @Override
         protected void onCancelled() {
             Log.i(TAG, "RefreshTask Cancelled.");
         }
 
         @Override
         protected void onPostExecute(List<ParseObject> objects) { // happens in
                                                                   // UI thread
            dialog.dismiss();

             // Bad practice, but meh, it'd be better if java had tuples
             if (objects == null) {
                 Toast.makeText(getApplicationContext(),
                         "Error getting data, please try again later",
                         Toast.LENGTH_SHORT).show();
                 Log.i(TAG,
                         "RefreshHashMapTask onPostExecute: Completed with an Error.");
            } else {
                Toast.makeText(getApplicationContext(), "Data refreshed",
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG, "RefreshTask onPostExecute: Completed.");
            }
             setListViewData(objects);
         }
     }
 }
