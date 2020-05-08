 package pt.traincompany.main;
 
 import pt.traincompany.account.AccountManager;
 import pt.traincompany.account.CardManagement;
 import pt.traincompany.map.Map;
 import pt.traincompany.search.Search;
 import pt.traincompany.searchHistory.MyHistory;
 import pt.traincompany.tickets.MyTickets;
 import pt.traincompany.utility.Configurations;
 import pt.traincompany.utility.Utility;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class Home extends Activity {
 
 	ProgressDialog dialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_home);
 		
		this.deleteDatabase(Configurations.THE_TRAIN_PROJECT_DB);
 		DatabaseHelper helper = new DatabaseHelper(Home.this);
 		Configurations.databaseHelper = helper;
 		
 		Configurations.userId = PreferenceManager.getDefaultSharedPreferences(Home.this).getInt("userId", 0);
 		Configurations.username = PreferenceManager.getDefaultSharedPreferences(Home.this).getString("username", "");
 		Configurations.name = PreferenceManager.getDefaultSharedPreferences(Home.this).getString("name", "");
 
 		final ImageView search = (ImageView) findViewById(R.id.btnSearch);
 		search.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent myIntent = new Intent(Home.this, Search.class);
 				Home.this.startActivity(myIntent);
 			}
 		});
 
 		final ImageView historic = (ImageView) findViewById(R.id.btnHistoric);
 		historic.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				
 				if (Configurations.userId > 0) {
 					DatabaseHelper helper = new DatabaseHelper(Home.this);
 					SQLiteDatabase db = helper.getWritableDatabase();
 					Cursor cursor = db.query("SearchHistory",
 							new String[] { "departure, arrival, hours, date" }, "departure IS NOT NULL AND arrival IS NOT NULL AND userId = ?", new String[]{Configurations.userId+""}, null,
 							null, "date DESC");
 					
 					if(!cursor.moveToFirst())
 						Toast.makeText(Home.this, "Não tem histórico...", Toast.LENGTH_SHORT).show();
 					else {
 						Intent myIntent = new Intent(Home.this, MyHistory.class);
 						Home.this.startActivity(myIntent);
 					}
 					
 					db.close();
 				}
 				else
 					Toast.makeText(Home.this, "Tem que efetuar primeiro login...", Toast.LENGTH_SHORT).show();
 		
 			}
 		});
 
 		final ImageView myTickets = (ImageView) findViewById(R.id.btnMyTickets);
 		myTickets.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				
 				if (Configurations.userId > 0) {
 					Intent myIntent = new Intent(Home.this, MyTickets.class);
 					Home.this.startActivity(myIntent);	
 				}
 				else
 					Toast.makeText(Home.this, "Tem que efetuar primeiro login...", Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		final ImageView login = (ImageView) findViewById(R.id.btnLogin);
 		login.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 					
 				Intent myIntent;
 				
 				
 				if (Configurations.userId > 0)  
 					myIntent = new Intent(Home.this, CardManagement.class);
 				else
 					myIntent = new Intent(Home.this, AccountManager.class);
 				
 				Home.this.startActivity(myIntent);
 			}
 		});
 		
 		final ImageView about = (ImageView) findViewById(R.id.btnAbout);
 		about.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				String ms1 = "Esta aplicação foi desenvolvida por Hélder Moreira e Tiago Babo na unidade curricular de Computação Móvel, na Faculdade de Engenharia da Universidade do Porto.";
 				new AlertDialog.Builder(Home.this)
 						.setTitle("About")
 						.setMessage(ms1)
 						.setPositiveButton("Fechar",
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog,
 											int which) {
 									}
 								}).show();
 			}
 		});
 		
 		final ImageView map = (ImageView) findViewById(R.id.btnMap);
 		map.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent myIntent = new Intent(Home.this, Map.class);
 				Home.this.startActivity(myIntent);
 			}
 		});
 	}
 	
 
 }
