 package com.andresornelas.whichcontainer.svc;
 
 import android.annotation.SuppressLint;
 import android.app.IntentService;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.util.Log;
 
 import com.andresornelas.whichcontainer.ResultsActivity;
 import com.andresornelas.whichcontainer.WCContract;
 import com.andresornelas.whichcontainer.entities.Brand;
 import com.andresornelas.whichcontainer.entities.Pan;
 import com.andresornelas.whichcontainer.entities.Unit;
 import com.andresornelas.whichcontainer.entities.Volume;
 
 public class WCService extends IntentService {
   private static final String LOG_TAG = "WCService";
 
   public static final String PARAM_OP = "WhichContainter.OP";
   public static final String PARAM_ESTIMATE = "WhichContainer.ESTIMATE";
 
   public static final int OP_ADD_PAN = -1;
   public static final int OP_FIND_CONTAINER = -2;
 
   public WCService() {
     super(LOG_TAG);
   }
 
   @Override
   protected void onHandleIntent(Intent i) {
     int op = i.getIntExtra(PARAM_OP, 0);
     switch (op) {
       case OP_ADD_PAN:
         addPan(i);
         break;
       case OP_FIND_CONTAINER:
         findBestContainer(i);
         break;
       default:
         throw new UnsupportedOperationException("Unrecognized operation: " + op);
     }
   }
 
   @SuppressLint("DefaultLocale")
   private void findBestContainer(Intent i) {
     Log.d(LOG_TAG, "Finding best container...");
 
     // TODO: Get this off the UI thread
     Cursor cursor = null;
     try {
       cursor = getContentResolver().query(WCContract.Pans.URI, new String[] {
           WCContract.Pans.Columns.CAPACITY,
           WCContract.Pans.Columns.UNIT,
           WCContract.Pans.Columns.BRAND
       }, "IS_CONTAINER = 1 AND AMOUNT > 0", null, null);
 
       if (cursor.getCount() == 0) {
        // TODO: Show Toast that no containers are available and open SelectionActivity.
         return;
       }
 
       double amount = i.getDoubleExtra(WCContract.Pans.Columns.CAPACITY, 0);
       Unit unit = Unit.valueOf(i.getStringExtra(WCContract.Pans.Columns.UNIT).toUpperCase());
       int percentFull = i.getIntExtra(PARAM_ESTIMATE, 0);
 
       int estimatedMLs = new Volume(amount, unit).getEstimation(percentFull);
       cursor.moveToFirst();
       Pan bestContainer = getPan(cursor);
       do {
         cursor.moveToNext();
         Pan currentContainer = getPan(cursor);
         if (currentContainer.percentFull(estimatedMLs) > bestContainer.percentFull(estimatedMLs)) {
           bestContainer = currentContainer;
         }
       } while (!cursor.isLast());
 
       i = new Intent(this, ResultsActivity.class);
       i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       // TODO: Make Pan Parceable so we can add it to the Intent directly
       i.putExtra(WCContract.Pans.Columns.CAPACITY, bestContainer.getCapacity());
       i.putExtra(WCContract.Pans.Columns.UNIT, bestContainer.getUnit().toString());
       i.putExtra(WCContract.Pans.Columns.BRAND, bestContainer.getBrand().toString());
       startActivity(i);
     } finally {
       if (cursor != null) cursor.close();
     }
   }
 
   @SuppressLint("DefaultLocale")
   private Pan getPan(Cursor c) {
     double capacity = c.getDouble(c.getColumnIndex(WCContract.Pans.Columns.CAPACITY));
     String unit = c.getString(c.getColumnIndex(WCContract.Pans.Columns.UNIT)).toUpperCase();
     String brand = c.getString(c.getColumnIndex(WCContract.Pans.Columns.BRAND)).toUpperCase();
     Volume v = new Volume(capacity, Unit.valueOf(unit));
     return new Pan(v, Brand.valueOf(brand), true);
   }
 
   private void addPan(Intent i) {
     Log.d(LOG_TAG, "Adding pan");
     double capacity = i.getDoubleExtra(WCContract.Pans.Columns.CAPACITY, 0);
     String unit = i.getStringExtra(WCContract.Pans.Columns.UNIT);
     String brand = i.getStringExtra(WCContract.Pans.Columns.BRAND);
 
     Cursor cursor = null;
     try {
       cursor = getContentResolver().query(WCContract.Pans.URI,
               new String[] { WCContract.Pans.Columns.ID },
               "CAPACITY = " + capacity + " AND UNIT = '" + unit + "' AND BRAND = '" + brand + "'",
               null, null);
 
       if (cursor.getCount() != 0) {
         Log.d(LOG_TAG, "Pan already in the database!");
         return;
       }
     } finally {
       if (cursor != null) cursor.close();
     }
 
     ContentValues pan = new ContentValues();
     pan.put(WCContract.Pans.Columns.BRAND, brand);
     pan.put(WCContract.Pans.Columns.CAPACITY, capacity);
     pan.put(WCContract.Pans.Columns.UNIT, unit);
     pan.put(WCContract.Pans.Columns.AMOUNT, i.getIntExtra(WCContract.Pans.Columns.AMOUNT, 0));
     pan.put(WCContract.Pans.Columns.IS_CONTAINTER,
             i.getIntExtra(WCContract.Pans.Columns.IS_CONTAINTER, 0));
     getContentResolver().insert(WCContract.Pans.URI, pan);
   }
 }
