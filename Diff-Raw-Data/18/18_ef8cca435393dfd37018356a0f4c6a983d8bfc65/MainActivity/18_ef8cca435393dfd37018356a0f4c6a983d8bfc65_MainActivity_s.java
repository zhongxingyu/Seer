 package org.personalfinance;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.personalfinance.database.TransactionDAO;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	SQLiteDatabase database;
 	float moneySpent;
 	float moneyEarned;
 	String dateFormat;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		updateDataDisplayed();
 
 		// Create the actions for the buttons
 
 		// Set action handler for allTransactionButton (history)
 		Button allTransactionsButton = (Button) findViewById(R.id.AllTransactionButton);
 		OnClickListener allTransactionListener = new OnClickListener() {
 
 			public void onClick(View v) {
 
 				Intent transactionList = new Intent(getApplicationContext(),
 						TransactionListActivity.class);
 				startActivity(transactionList);
 			}
 		};
 		allTransactionsButton.setOnClickListener(allTransactionListener);
 		
 		//Set the handler for the leader Board  activity
 		Button leaderBoardButton = (Button) findViewById(R.id.leaderBoardButton);
 		OnClickListener leaderBoardListener = new OnClickListener() {
 
 			public void onClick(View v) {
 
 				Intent transactionList = new Intent(getApplicationContext(),
 						leaderBoardActivity.class);
 				startActivity(transactionList);
 			}
 		};
 		leaderBoardButton.setOnClickListener(leaderBoardListener);
 
 
 	}
 
 	public void updateDataDisplayed() {
 		int nTransaccionesMasRecientes = 3;
 		// dateFormat = "yyyy-MM-dd HH:mm:ss";
 		//recentTransactionsArray = new ArrayList<Transaction>();
 
 		// crearDatosDePrueba(database);
 
 		// Fill general information views
 		TextView moneySpentTV = (TextView) findViewById(R.id.spentMoneyTV);
 		TextView moneyLeftTV = (TextView) findViewById(R.id.leftMoneyTV);
 		TextView moneyEarnedTV = (TextView) findViewById(R.id.earnedMoneyTV);
 		ProgressBar moneyProgressBar = (ProgressBar) findViewById(R.id.moneyProgressBar);
 
 		
 		
 		//TODO habria que filtrar transacctiones solo para este mes y no todas las de la base de datos
 		//TODO estas sumas se podrian optimizar y hacer con una query SUM en la base de datos
 		TransactionDAO DAO = new TransactionDAO(getApplicationContext());
 		DAO.open();
 		
 		// Sum all expenses
 		moneySpent = 0;
 		List<Transaction> outcomeTransactions = DAO.getAllTransactions(true);
 		for(Transaction trans: outcomeTransactions){
 			moneySpent +=trans.getCantidadDinero();
 		}
 
 		// Sum all incomes
 		moneyEarned = 0;
 		List<Transaction> incomeTransactions = DAO.getAllTransactions(false);
 		for(Transaction trans: outcomeTransactions){
 			moneyEarned +=trans.getCantidadDinero();
 		}
 
 		DAO.close();
 		
 		// Fill balance information
 		moneySpentTV.setText("" + moneySpent);
 		moneyLeftTV.setText("" + (moneyEarned - moneySpent));
 		moneyEarnedTV.setText("" + moneyEarned);
 		moneyProgressBar.setMax((int) moneyEarned);
 		moneyProgressBar.setProgress((int) (moneyEarned - moneySpent));
 		
 		
 		outcomeTransactions.addAll(incomeTransactions);
 
 		// Fill list of the 3 most recent transactions
 		Collections.sort(outcomeTransactions);
 		
		List<Transaction> recentTransactions = outcomeTransactions.subList(0, nTransaccionesMasRecientes);
 		
 		ListView lastTransactionsLV = (ListView) findViewById(R.id.lastTransactionsLV);
 		lastTransactionsLV.setAdapter(new RecentTransactionsAdapter(this,
 				android.R.layout.simple_list_item_1, R.id.lastTransactionsLV,
 				recentTransactions));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	private class RecentTransactionsAdapter extends ArrayAdapter<Transaction> {
 		
 		private List<Transaction> recentTransactionsArray;
 
 		public RecentTransactionsAdapter(Context context, int resource,
 				int textViewResourceId,
 				List<Transaction> recentTransactionsArray) {
 			super(context, resource, textViewResourceId,
 					recentTransactionsArray);
 			
 			this.recentTransactionsArray = recentTransactionsArray;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View row = inflater.inflate(R.layout.recent_transaction_lv, parent,
 					false);
 
 			if (position >= recentTransactionsArray.size())
 				return row;
 
 			TextView transactionSignTV = (TextView) row
 					.findViewById(R.id.transactionSignTV);
 			TextView transactionNameTV = (TextView) row
 					.findViewById(R.id.transactionTV);
 			TextView transactionAmountTV = (TextView) row
 					.findViewById(R.id.transactionAmountTV);
 
 			String transactionSign = (recentTransactionsArray.get(position)
 					.isOutcome()) ? "-" : "+";
 			transactionSignTV.setText(transactionSign);
 
 			String nota = recentTransactionsArray.get(position).getNota();
 			if (nota == null)
 				nota = "expense";
 			transactionNameTV.setText(nota);
 
 			transactionAmountTV
 					.setText(""
 							+ recentTransactionsArray.get(position)
 									.getCantidadDinero());
 
 			return row;
 		}
 	}
 
	void crearDatosDePrueba(SQLiteDatabase database) {
 
 		database.execSQL("INSERT INTO INCOME (Id, Nota, Fecha, Ganacia) "
 				+ "VALUES (1, 'Sueldo', '2012-12-06 05:40:00',2500)");
 
 		database.execSQL("INSERT INTO OUTCOME (Id, Nota, Fecha, Categoria, LocalizacionValida, Longitud, Latitud, Gasto) "
 				+ "VALUES (2, 'Compras del super', '2012-12-07 05:40:00', 1, 0, 0, 0, 100)");
 
 		database.execSQL("INSERT INTO OUTCOME (Id, Nota, Fecha, Categoria, LocalizacionValida, Longitud, Latitud, Gasto) "
 				+ "VALUES (3, 'Ropa', '2012-12-08 05:40:00', 2, 0, 0, 0, 50)");
 
 		database.execSQL("INSERT INTO OUTCOME (Id, Nota, Fecha, Categoria, LocalizacionValida, Longitud, Latitud, Gasto) "
 				+ "VALUES (4, 'Bocadillo', '2012-12-09 05:40:00', 1, 0, 0, 0, 2.50)");
 
	}
 
 }
