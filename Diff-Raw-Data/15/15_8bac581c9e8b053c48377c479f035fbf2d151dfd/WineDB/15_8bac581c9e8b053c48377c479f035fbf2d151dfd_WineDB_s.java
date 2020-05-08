 package com.selesse.android.winedb.activity;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.drawable.ColorDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import com.selesse.android.winedb.R;
 import com.selesse.android.winedb.contentprovider.WineContentProvider;
 import com.selesse.android.winedb.database.FileDatabaseBackup;
 import com.selesse.android.winedb.database.Wine;
 import com.selesse.android.winedb.database.WineDatabaseHandler;
 import com.selesse.android.winedb.model.RequestCode;
 import com.selesse.android.winedb.model.WineContextMenu;
 import com.selesse.android.winedb.winescraper.WineScrapers;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class WineDB extends SherlockFragmentActivity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         if (findViewById(R.id.fragment_container) != null) {
             if (savedInstanceState != null) {
                 return;
             }
 
             WineListFragment firstFragment = new WineListFragment();
 
             firstFragment.setArguments(getIntent().getExtras());
 
             // Add the fragment to the 'fragment_container' FrameLayout
             getSupportFragmentManager().beginTransaction()
                     .add(R.id.fragment_container, firstFragment, WineListFragment.TAG).commit();
         }
 
         // remove the icon from the ActionBar
         getSupportActionBar().setIcon(new ColorDrawable(android.R.color.transparent));
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
 
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 
         for (WineContextMenu contextMenuItem : WineContextMenu.values()) {
             menu.add(0, info.position, contextMenuItem.ordinal(), contextMenuItem.toString());
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.main_activity, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
         switch (item.getItemId()) {
             case R.id.scan:
                 IntentIntegrator integrator = new IntentIntegrator(this);
                 integrator.initiateScan();
                 return true;
             case R.id.add_wine:
                 startEditWineIntent(new Wine());
                 return true;
             case R.id.export_database:
                 FileDatabaseBackup fileDatabaseBackup = new FileDatabaseBackup(this);
                 fileDatabaseBackup.startExportDatabase();
                 return true;
             case R.id.import_database:
                 fileDatabaseBackup = new FileDatabaseBackup(this);
                 fileDatabaseBackup.startImportDatabase();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         super.onContextItemSelected(item);
 
         WineContextMenu selectedItem = WineContextMenu.values()[item.getOrder()];
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 
         switch (selectedItem) {
             case DELETE:
                 Uri uri = Uri.parse(WineContentProvider.CONTENT_URI + "/" + info.id);
                 getContentResolver().delete(uri, null, null);
                 break;
             case EDIT:
                 Intent intent = new Intent(getBaseContext(), CreateOrEditWineActivity.class);
                 intent.putExtra("id", info.id);
                 startActivity(intent);
                 break;
         }
 
         return true;
     }
 
     @Override
     public void onActivityResult(int requestCodeNumber, int resultCode, Intent intent) {
         if (resultCode == Activity.RESULT_CANCELED) {
             return;
         }
 
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCodeNumber, resultCode,
                intent);

        if (scanResult.getContents() == null) {
            return;
        }

         // case where zxing was called and successfully scanned something
         if (requestCodeNumber == IntentIntegrator.REQUEST_CODE) {
             // extract the wine object from barcode (presumably by going through a bunch of sources)
             String barcode = scanResult.getContents();
 
             // we'll only use the scrapers if it looks like it'll match approximately match UPC
             if (Pattern.matches("[0-9]{1,13}", barcode)) {
                 // check to see if we already have this barcode in our database...
                 // if we do, we edit that wine; otherwise we scrape and create
                 Wine wine = WineDatabaseHandler.getInstance(this).getWineByBarcode(barcode);
                 if (wine == null) {
                     scrapeWinesAndEditWine(barcode);
                 } else {
                     startEditWineIntent(wine);
                 }
             } else {
                 Wine wine = new Wine();
                 wine.setBarcode(barcode);
                 startEditWineIntent(wine);
             }
         }
     }
 
     public void startEditWineIntent(Wine wine) {
         Intent editIntent = new Intent(this, CreateOrEditWineActivity.class);
         Bundle extras = new Bundle();
         extras.putLong("id", wine.getId());
         extras.putSerializable("wine", wine);
         editIntent.putExtras(extras);
         startActivityForResult(editIntent, RequestCode.EDIT_WINE.ordinal());
     }
 
     /**
      * Create an AsyncTask to go scrape wines for us. The real magic happens in
      * {@link WineScraperThread}.
      *
      * @param barcode The barcode of the wine we'll be scraping.
      */
     private void scrapeWinesAndEditWine(String barcode) {
         AsyncTask<String, Void, List<Wine>> task = new WineScraperThread();
         task.execute(barcode);
     }
 
     private class WineScraperThread extends AsyncTask<String, Void, List<Wine>> {
         private ProgressDialog progress;
         private String barcode;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progress = new ProgressDialog(WineDB.this);
             progress.setMessage(getString(R.string.scraping));
             progress.setIndeterminate(true);
             progress.show();
         }
 
         @Override
         protected List<Wine> doInBackground(String... params) {
             barcode = params[0];
             WineScrapers scrapers = new WineScrapers(barcode);
             return scrapers.scrape();
         }
 
         @Override
         protected void onPostExecute(List<Wine> result) {
             super.onPostExecute(result);
             try {
                 progress.dismiss();
                 progress = null;
             }
             catch (Exception e) {
                 // do nothing
             }
 
             Wine wine;
             if (result.size() == 0) {
                 wine = new Wine();
             }
             // for now we'll just grab the first result, we can do something more sophisticated later
             else {
                 wine = result.get(0);
             }
             wine.setBarcode(barcode);
             startEditWineIntent(wine);
         }
     }
 }
