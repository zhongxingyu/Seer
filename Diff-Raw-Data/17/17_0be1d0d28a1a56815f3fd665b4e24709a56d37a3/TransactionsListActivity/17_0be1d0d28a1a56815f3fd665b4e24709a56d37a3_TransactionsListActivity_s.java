 package ru.spbau.WhereIsMyMoney.gui;
 
 import java.util.Date;
 import java.util.List;
 
 import android.util.Log;
 import ru.spbau.WhereIsMyMoney.R;
 import ru.spbau.WhereIsMyMoney.Transaction;
 import ru.spbau.WhereIsMyMoney.storage.TransactionLogHelper;
 import ru.spbau.WhereIsMyMoney.storage.TransactionLogSource;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * Shows list of transactions from specified card.
  */
 public class TransactionsListActivity extends Activity {
     private static final String TAG = TransactionsListActivity.class.getCanonicalName();
 
     final static String ID_PARAM = "id";
     final static String PLACE = "place";
 
     TransactionLogSource db;
     //todo drop this from class field
     private String cardId;
 
     public void saveTransaction(View view) {
         Intent intent = new Intent(this, AddTransactionActivity.class);
         intent.putExtra(TransactionsListActivity.ID_PARAM, cardId);
         startActivity(intent);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Intent intent = getIntent();
 
         long startTime = this.getIntent().getLongExtra(AbstractCostsReportActivity.START_DATE, -1);
         long endTime = this.getIntent().getLongExtra(AbstractCostsReportActivity.END_DATE, -1);
 
         if (startTime == -1) {
             Log.d(TAG, AbstractCostsReportActivity.START_DATE + "parameter not found");
         }
         if (endTime == -1) {
             Log.d(TAG, AbstractCostsReportActivity.END_DATE + "parameter not found");
         }
 
         setContentView(ru.spbau.WhereIsMyMoney.R.layout.transactions);
         db = new TransactionLogSource(getApplicationContext());
         db.open();
 
         List<Transaction> transactions;
 
         cardId = intent.getStringExtra(ID_PARAM);
 
         if (cardId != null) {
             TextView title = (TextView) findViewById(R.id.header);
             title.setText(getString(R.string.transaction_list_header) + " " + cardId);
 
             if (startTime == -1 || endTime == -1) {
                 transactions = db.getTransactionsPerCard(cardId);
             } else {
                 transactions = db.getTransactionsPerCardForPeriod(cardId, new Date(startTime), new Date(endTime));
             }
 
             Button addTransaction = (Button)findViewById(R.id.add_tr);
             if (cardId.equals(TransactionLogHelper.CASH)) {
                 addTransaction.setVisibility(View.VISIBLE);
             } else {
                 addTransaction.setVisibility(View.GONE);
             }
         } else {
             String place = intent.getStringExtra(PLACE);
 
             if (place == null)
                 return;
 
             TextView title = (TextView) findViewById(R.id.header);
             title.setText(getString(R.string.transaction_list_header) + " place");
 
             if (startTime == -1 || endTime == -1) {
                 transactions = db.getTransactionsPerPlace(place);
             } else {
                 transactions = db.getTransactionsPerPlaceForPeriod(place, new Date(startTime), new Date(endTime));
             }
            
            Button chartButton = (Button) findViewById(R.id.chart_button);
            chartButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    BalanceChartBuilder builder = new BalanceChartBuilder();
                    startActivity(builder.getIntent(TransactionsListActivity.this, db.getTransactionsPerCard(cardId)));
                }
            });
         }
 
         ArrayAdapter<Transaction> adapter = new TransactionListAdapter(getApplicationContext(), transactions);
 
         ListView listView = (ListView) findViewById(ru.spbau.WhereIsMyMoney.R.id.transactions);
         listView.setAdapter(adapter);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         db.close();
     }
 }
