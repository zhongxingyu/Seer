 package ru.spbau.WhereIsMyMoney.gui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.View;
import android.widget.*;
import ru.spbau.WhereIsMyMoney.R;
 import ru.spbau.WhereIsMyMoney.Transaction;
import ru.spbau.WhereIsMyMoney.storage.TransactionLogHelper;
 import ru.spbau.WhereIsMyMoney.storage.TransactionLogSource;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 /**
  * Shows list of transactions from specified card.
  */
 public class TransactionsListActivity extends Activity {
 	private static final String FORMAT = "yyyy.MM.dd HH:mm:ss";
 	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(FORMAT);
 	
     final static String ID_PARAM = "id";
     final static  String OK = "Ok";
     final static String TRANSACTION = "Transaction description";
 
     TransactionLogSource db;
     private String cardId;
 
     public void saveTransaction(View view) {
         Intent intent = new Intent(this, AddTransactionActivity.class);
         intent.putExtra(TransactionsListActivity.ID_PARAM, cardId);
         startActivity(intent);
     }
 
     private void createTransactionsDialog(String title, String message) {
         AlertDialog.Builder ad = new AlertDialog.Builder(TransactionsListActivity.this);
         ad.setTitle(title);
         ad.setMessage(message);
         ad.setCancelable(true);
         ad.setPositiveButton(OK, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int arg1) {
             }
         });
         ad.show();
     }
     
     private String shortDescription(Transaction trans) {
     	StringBuilder sb = new StringBuilder();
     	sb.append(FORMATTER.format(trans.getDate()));
     	if (trans.getDelta() != null) {
     		sb.append(", delta: ").append(trans.getDelta());
     	} else {
     		sb.append(", balance: ").append(trans.getBalance());
     	}
     	return sb.toString();
     }
 
     private void createListView(final String card) {
         ListView listView = (ListView) findViewById(ru.spbau.WhereIsMyMoney.R.id.transactions);
 
         final List<Transaction> transactions = db.getTransactionsPerCard(card);
         final String[] values = new String[transactions.size()];
         final String[] messages = new String[transactions.size()];
         for (int i = 0; i < transactions.size(); ++i) {
         	Transaction trans = transactions.get(i);
             values[i] = shortDescription(trans);
             messages[i] = trans.toString();
         }
 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, android.R.id.text1, values);
 
         listView.setAdapter(adapter);
 
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
                 createTransactionsDialog(TRANSACTION, messages[position]);
             }
         });
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Intent intent = getIntent();
         cardId = intent.getStringExtra(ID_PARAM);
         setContentView(ru.spbau.WhereIsMyMoney.R.layout.transactions);
         db = new TransactionLogSource(getApplicationContext());
         db.open();
         TextView card = (TextView) findViewById(ru.spbau.WhereIsMyMoney.R.id.card_id);
         card.setText(cardId);
         createListView(cardId);
        Button addTransaction = (Button)findViewById(R.id.add_tr);
        if (cardId.equals(TransactionLogHelper.CASH)) {
            addTransaction.setEnabled(true);
        } else {
            addTransaction.setEnabled(false);
        }
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         db.close();
     }
 }
