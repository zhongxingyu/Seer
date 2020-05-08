 package no.kantega.android.afp;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 import no.kantega.android.afp.adapters.TransactionsAdapter;
 import no.kantega.android.afp.controllers.Transactions;
 import no.kantega.android.afp.models.Transaction;
 import no.kantega.android.afp.utils.GsonUtil;
 import no.kantega.android.afp.utils.HttpUtil;
 import no.kantega.android.afp.utils.Prefs;
 import no.kantega.android.afp.utils.Register;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 public class NotificationsActivity extends ListActivity {
 
     private static final String TAG = NotificationsActivity.class.
             getSimpleName();
     private static final int PROGRESS_DIALOG = 0;
     private ProgressDialog progressDialog;
     private AlertDialog alertDialog;
     private SharedPreferences preferences;
     private Cursor cursor;
     private Transactions db;
     private TransactionsAdapter adapter;
     private long latestTimestamp;
     private String url;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.transactions);
         this.db = new Transactions(getApplicationContext());
         this.adapter = new TransactionsAdapter(this, cursor);
         setListAdapter(adapter);
         showDialog(PROGRESS_DIALOG);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         Object o = l.getItemAtPosition(position);
         if (o instanceof Cursor) {
             Cursor cursor = (Cursor) o;
             int transaction_id = cursor.getInt(cursor.getColumnIndex("_id"));
             Transaction t = db.getById(transaction_id);
             Intent intent;
             intent = new Intent(getApplicationContext(), EditTransactionActivity.class);
             intent.putExtra("transaction", t);
             startActivity(intent);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         new Thread(new Runnable() {
             @Override
             public void run() {
                if (latestTimestamp == 0) {
                    latestTimestamp = getLatestExternalTimestamp();
                }
                 cursor = db.getCursorAfterTimestamp(latestTimestamp);
                 runOnUiThread(adapterHandler);
             }
         }).start();
     }
 
     private final Runnable adapterHandler = new Runnable() {
         @Override
         public void run() {
             // Change to a fresh cursor, the old one will be automatically closed
             adapter.changeCursor(cursor);
             Log.d(TAG, "Changed to a new cursor");
         }
     };
 
     private AlertDialog createAlertDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.server_unavailable)
                 .setCancelable(false)
                 .setPositiveButton(R.string.confirm,
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog,
                                                 int id) {
                                 dialog.dismiss();
                             }
                         });
         return builder.create();
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case PROGRESS_DIALOG: {
                 progressDialog = new ProgressDialog(this);
                 progressDialog.setMessage(getResources().getString(
                         R.string.wait));
                 progressDialog.setCancelable(false);
                 progressDialog.setProgressStyle(ProgressDialog.
                         STYLE_HORIZONTAL);
                 return progressDialog;
             }
             default: {
                 return null;
             }
         }
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch (id) {
             case PROGRESS_DIALOG: {
                 progressDialog.setProgress(0);
                 if (url == null) {
                     url = Prefs.getProperties(getApplicationContext()).
                             getProperty("newTransactions").toString();
                 }
                 new TransactionsTask().execute(url);
             }
         }
     }
 
     private final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             progressDialog.setProgress(msg.arg1);
         }
     };
 
     private class TransactionsTask
             extends AsyncTask<String, Integer, Boolean> {
 
         @Override
         protected void onPreExecute() {
             progressDialog.show();
         }
 
         @Override
         protected Boolean doInBackground(String... urls) {
             return getTransactions(urls[0]);
         }
 
         /**
          * Retrieve transactions from server
          *
          * @param url URL for new transactions
          * @return True if successful
          */
         private boolean getTransactions(final String url) {
            if (latestTimestamp == 0) {
                latestTimestamp = getLatestExternalTimestamp();
            }
             final InputStream in = post(String.format(url, latestTimestamp),
                     new ArrayList<NameValuePair>());
             if (in == null) {
                 return false;
             }
             final List<Transaction> transactions = GsonUtil.
                     parseTransactions(in);
             if (transactions == null) {
                 return false;
             }
             progressDialog.setMax(transactions.size());
             int i = 0;
             for (Transaction t : transactions) {
                 db.add(t);
                 publishProgress(++i);
             }
             return true;
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             final Message msg = handler.obtainMessage();
             msg.arg1 = values[0];
             handler.sendMessage(msg);
         }
 
         @Override
         protected void onPostExecute(Boolean success) {
             progressDialog.setProgress(0);
             progressDialog.setMax(100);
             progressDialog.dismiss();
             if (!success) {
                 if (alertDialog == null) {
                     alertDialog = createAlertDialog();
                 }
                 alertDialog.show();
             }
             onResume();
         }
     }
 
     private InputStream post(String url, List<NameValuePair> values) {
         if (preferences == null) {
             preferences = Prefs.get(getApplicationContext());
         }
         values.add(new BasicNameValuePair("registrationId",
                 preferences.getString(Register.REGISTRATION_ID_KEY, null)));
         return HttpUtil.post(url, values);
     }
 
     private long getLatestExternalTimestamp() {
         final Transaction transaction = db.getLatestExternal();
         return transaction != null ? transaction.getTimestamp() : 0;
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         db.close();
     }
 }
