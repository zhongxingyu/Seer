 package no.kantega.android;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import no.kantega.android.models.Transaction;
 import no.kantega.android.utils.DatabaseHelper;
 import no.kantega.android.utils.DatabaseOpenHelper;
 import no.kantega.android.utils.GsonUtil;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Properties;
 
 public class SynchronizeActivity extends Activity {
 
     private static final String TAG = SynchronizeActivity.class.getSimpleName();
     private static final int PROGRESS_DIALOG = 0;
     private DatabaseHelper db;
     private ProgressDialog progressDialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.synchronize);
         Button syncButton = (Button) findViewById(R.id.syncButton);
         syncButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDialog(PROGRESS_DIALOG);
             }
         });
         db = new DatabaseHelper(new DatabaseOpenHelper(
                 getApplicationContext()).getWritableDatabase());
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
                progressDialog.setMax(0);
                 populateDatabase();
             }
         }
     }
 
     private void populateDatabase() {
         try {
             InputStream inputStream = getAssets().open("url.properties");
             Properties properties = new Properties();
             properties.load(inputStream);
             new TransactionsTask().execute(
                     properties.get("allTransactions").toString());
         } catch (IOException e) {
             Log.e(TAG, "Could not read properties file", e);
         }
     }
 
     private final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             progressDialog.setProgress(msg.arg1);
         }
     };
 
     private class TransactionsTask
             extends AsyncTask<String, Integer, List<Transaction>> {
 
         @Override
         protected void onPreExecute() {
             progressDialog.show();
         }
 
         @Override
         protected List<Transaction> doInBackground(String... urls) {
             List<Transaction> transactions = GsonUtil.parseTransactions(
                     GsonUtil.getJSON(urls[0]));
             if (transactions != null && !transactions.isEmpty()) {
                 progressDialog.setMax(transactions.size());
                 db.emptyTables();
                 int i = 0;
                 for (Transaction t : transactions) {
                     db.insert(t);
                     publishProgress(++i);
                 }
             }
             return null;
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             final Message msg = handler.obtainMessage();
             msg.arg1 = values[0];
             handler.sendMessage(msg);
         }
 
         @Override
         protected void onPostExecute(List<Transaction> transactions) {
             progressDialog.dismiss();
         }
     }
 }
