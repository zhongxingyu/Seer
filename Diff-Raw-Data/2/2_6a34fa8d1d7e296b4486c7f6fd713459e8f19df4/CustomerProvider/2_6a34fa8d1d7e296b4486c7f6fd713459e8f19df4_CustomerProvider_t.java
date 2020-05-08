 package com.goliathonline.android.greenstreetcrm.provider;
 
 import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers;
 import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs;
 import com.goliathonline.android.greenstreetcrm.provider.CustomerDatabase.CustomersJobs;
 import com.goliathonline.android.greenstreetcrm.provider.CustomerDatabase.Tables;
 import com.goliathonline.android.greenstreetcrm.util.SelectionBuilder;
 
 import android.app.Activity;
 import android.content.ContentProvider;
 import android.content.ContentProviderOperation;
 import android.content.ContentProviderResult;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.OperationApplicationException;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.ParcelFileDescriptor;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * Provider that stores {@link ScheduleContract} data. Data is usually inserted
  * by {@link SyncService}, and queried by various {@link Activity} instances.
  */
 public class CustomerProvider extends ContentProvider {
     private static final String TAG = "CustomerProvider";
     private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
 
     private CustomerDatabase mOpenHelper;
 
     private static final UriMatcher sUriMatcher = buildUriMatcher();
 
     private static final int CUSTOMERS = 100;
     private static final int CUSTOMERS_ID = 102;
     private static final int CUSTOMERS_ID_JOBS = 103;
     private static final int CUSTOMERS_STARRED = 104;
     
     private static final int JOBS = 200;
     private static final int JOBS_ID = 201;
     private static final int JOBS_STARRED = 202;
 
     /**
      * Build and return a {@link UriMatcher} that catches all {@link Uri}
      * variations supported by this {@link ContentProvider}.
      */
     private static UriMatcher buildUriMatcher() {
         final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
         final String authority = CustomerContract.CONTENT_AUTHORITY;
 
         matcher.addURI(authority, "customers", CUSTOMERS);
         matcher.addURI(authority, "customers/*", CUSTOMERS_ID);
         matcher.addURI(authority, "customers/*/jobs", CUSTOMERS_ID_JOBS);
         matcher.addURI(authority, "customers/starred", CUSTOMERS_STARRED);
         
         matcher.addURI(authority, "jobs", JOBS);
         matcher.addURI(authority, "jobs/*", JOBS_ID);
         matcher.addURI(authority, "jobs/starred", JOBS_STARRED);
 
         return matcher;
     }
 
     @Override
     public boolean onCreate() {
         final Context context = getContext();
         mOpenHelper = new CustomerDatabase(context);
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getType(Uri uri) {
         final int match = sUriMatcher.match(uri);
         switch (match) {
             case CUSTOMERS:
                 return Customers.CONTENT_TYPE;
             case CUSTOMERS_ID:
                 return Customers.CONTENT_ITEM_TYPE;
             case CUSTOMERS_ID_JOBS:
             	return Jobs.CONTENT_TYPE;
             case CUSTOMERS_STARRED:
             	return Customers.CONTENT_TYPE;
             case JOBS:
             	return Jobs.CONTENT_TYPE;
             case JOBS_ID:
             	return Jobs.CONTENT_ITEM_TYPE;
             case JOBS_STARRED:
             	return Jobs.CONTENT_TYPE;
             default:
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
             String sortOrder) {
         if (LOGV) Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
         final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
 
         final int match = sUriMatcher.match(uri);
         switch (match) {
             default: {
                 // Most cases are handled with simple SelectionBuilder
                 final SelectionBuilder builder = buildExpandedSelection(uri, match);
                 return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         if (LOGV) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         long retId;
         final int match = sUriMatcher.match(uri);
         switch (match) {
             case CUSTOMERS: {
                 retId = db.insertOrThrow(Tables.CUSTOMERS, null, values);
                 getContext().getContentResolver().notifyChange(uri, null);
                 return Customers.buildCustomerUri(String.valueOf(retId));
             }
             case JOBS: {
             	retId = db.insertOrThrow(Tables.JOBS, null, values);
                 getContext().getContentResolver().notifyChange(uri, null);
                 return Jobs.buildJobUri(String.valueOf(retId));
             }
             case CUSTOMERS_ID_JOBS: {
                 db.insertOrThrow(Tables.CUSTOMERS_JOBS, null, values);
                 getContext().getContentResolver().notifyChange(uri, null);
                 return Jobs.buildJobUri(values.getAsString(CustomersJobs.JOB_ID));
             }
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         if (LOGV) Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         final SelectionBuilder builder = buildSimpleSelection(uri);
         int retVal = builder.where(selection, selectionArgs).update(db, values);
         getContext().getContentResolver().notifyChange(uri, null);
         return retVal;
     }
 
     /** {@inheritDoc} */
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         if (LOGV) Log.v(TAG, "delete(uri=" + uri + ")");
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         final SelectionBuilder builder = buildSimpleSelection(uri);
         int retVal = builder.where(selection, selectionArgs).delete(db);
         getContext().getContentResolver().notifyChange(uri, null);
         return retVal;
     }
 
     /**
      * Apply the given set of {@link ContentProviderOperation}, executing inside
      * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
      * any single one fails.
      */
     @Override
     public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
             throws OperationApplicationException {
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         db.beginTransaction();
         try {
             final int numOperations = operations.size();
             final ContentProviderResult[] results = new ContentProviderResult[numOperations];
             for (int i = 0; i < numOperations; i++) {
                 results[i] = operations.get(i).apply(this, results, i);
             }
             db.setTransactionSuccessful();
             return results;
         } finally {
             db.endTransaction();
         }
     }
 
     /**
      * Build a simple {@link SelectionBuilder} to match the requested
      * {@link Uri}. This is usually enough to support {@link #insert},
      * {@link #update}, and {@link #delete} operations.
      */
     private SelectionBuilder buildSimpleSelection(Uri uri) {
         final SelectionBuilder builder = new SelectionBuilder();
         final int match = sUriMatcher.match(uri);
         switch (match) {
             case CUSTOMERS: {
                 return builder.table(Tables.CUSTOMERS);
             }
             case CUSTOMERS_ID: {
                 final String customerId = Customers.getCustomerId(uri);
                 return builder.table(Tables.CUSTOMERS)
                         .where(BaseColumns._ID + "=?", customerId);
             }
             case JOBS: {
                 return builder.table(Tables.JOBS);
             }
             case JOBS_ID: {
                 final String jobId = Jobs.getJobId(uri);
                 return builder.table(Tables.JOBS)
                        .where(Jobs._ID + "=?", jobId);
             }
             case CUSTOMERS_ID_JOBS: {
                 final String customerId = Customers.getCustomerId(uri);
                 return builder.table(Tables.CUSTOMERS_JOBS)
                         .where(Customers.CUSTOMER_ID + "=?", customerId);
             }
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
 
     /**
      * Build an advanced {@link SelectionBuilder} to match the requested
      * {@link Uri}. This is usually only used by {@link #query}, since it
      * performs table joins useful for {@link Cursor} data.
      */
     private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
         final SelectionBuilder builder = new SelectionBuilder();
         switch (match) {
             case CUSTOMERS: {
                 return builder.table(Tables.CUSTOMERS);
             }
             case CUSTOMERS_ID: {
                 final String customerId = Customers.getCustomerId(uri);
                 return builder.table(Tables.CUSTOMERS)
                         .where(BaseColumns._ID + "=?", customerId);
             }
             case CUSTOMERS_ID_JOBS: {
                 final String customerId = Customers.getCustomerId(uri);
                 return builder.table(Tables.CUSTOMERS_JOBS_JOIN_JOBS)
                         .mapToTable(Jobs._ID, Tables.JOBS)
                         .mapToTable(Jobs.JOB_ID, Tables.JOBS)
                         .where(Qualified.CUSTOMERS_JOBS_CUSTOMER_ID + "=?", customerId);
             }
             case JOBS: {
                 return builder.table(Tables.JOBS);
             }
             case JOBS_ID: {
                 final String jobId = Jobs.getJobId(uri);
                 return builder.table(Tables.JOBS)
                         .where(BaseColumns._ID + "=?", jobId);
             }
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
 
     @Override
     public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
         final int match = sUriMatcher.match(uri);
         switch (match) {
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
     
     /**
      * {@link ScheduleContract} fields that are fully qualified with a specific
      * parent {@link Tables}. Used when needed to work around SQL ambiguity.
      */
     private interface Qualified {
         String CUSTOMERS_CUSTOMER_ID = Tables.CUSTOMERS + "." + Customers.CUSTOMER_ID;
 
         String CUSTOMERS_JOBS_CUSTOMER_ID = Tables.CUSTOMERS_JOBS + "."
                 + CustomersJobs.CUSTOMER_ID;
         String CUSTOMERS_JOBS_JOB_ID = Tables.CUSTOMERS_JOBS + "."
                 + CustomersJobs.JOB_ID;
 
         @SuppressWarnings("hiding")
         String JOBS_STARRED = Tables.JOBS + "." + Jobs.JOB_STARRED;
     }
 }
