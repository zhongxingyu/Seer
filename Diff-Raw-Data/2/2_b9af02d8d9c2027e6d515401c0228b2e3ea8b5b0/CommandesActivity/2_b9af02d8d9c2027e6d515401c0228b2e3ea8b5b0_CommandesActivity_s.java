 package com.horeca;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.widget.CursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 
 public class CommandesActivity extends MyActivity implements ViewBinder, AdapterView.OnItemClickListener {
 	
 	private CommandeListCursorAdapter adapter = null;
 	//private ArrayAdapter<String> adapter2 = null;
 	//private ArrayList<String> list_commande = new ArrayList<String>();
 	
 	private ListView commandes_list = null;
 	
 	private Plat plat = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Get the id of the plat to display provided by PlatListActivity
 		Bundle b = getIntent().getExtras();
 		long id = b.getLong("_id");
 		
 		// Open the db
 		MySqliteHelper sqliteHelper = new MySqliteHelper(this);
 		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
 		
 		// Get the plat from the db
 		plat = new Plat(id, db);
 		
 		
 		// Set the title of the activity
 		setTitle(plat.getHoreca().getName() + " - " + plat.getName() + " - Commandes");
 		
 		// Set TextView's content
 		setContentView(R.layout.activity_commandes);
 		Cursor cursor;
 		cursor = Commande.getAllCommandeForPlat(db, plat);
 		commandes_list = (ListView) findViewById(R.id.commandes_list);
 /*
 		adapter = new SimpleCursorAdapter(this, //this context
 				android.R.layout.simple_list_item_1, //id of the item layout used by default for the individual rows (this id is pre-defined by Android)
 				//android.R.id.list,
 				//R.id.plats_list,
 				Commande.getAllCommandeForPlat(db, plat),
 				new String[] { HorecaContract.Commande.NOMBRE },
 				new int[] { android.R.id.text1 }); // the list of objects to be adapted //android.R.id.text1
 		// to remove deprecation warning, I need to add ", 0" but it is only in API 11 and we need 2.3.3 which is API 10
 		
 		adapter.setViewBinder(this);
 		*/
 		//list_commande = Commande.getAllCommandsTime(db,plat);
 		//adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list_commande);
 		
 		//commandes_list.setAdapter(adapter2);
 		adapter = new CommandeListCursorAdapter(this, cursor);
 		commandes_list.setAdapter(adapter);
 
 		db.close();
 		
 		commandes_list.setOnItemClickListener(this); 
 	}
 	
 	private void refreshCommandesList() {
 		// Open the db
 		MySqliteHelper sqliteHelper = new MySqliteHelper(this);
 		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
 		
 		adapter.changeCursor(Commande.getAllCommandeForPlat(db, plat));
 				
 		db.close();
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 	{
 		// Open the db
 		MySqliteHelper sqliteHelper = new MySqliteHelper(this);
 		SQLiteDatabase db = sqliteHelper.getWritableDatabase();
 		Commande commande = new Commande(db, id);
 		commande.destroy(db);
 		db.close();
 		refreshCommandesList();
 	}
 	
     @Override
     public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
     	Date temps = new Date(cursor.getLong(HorecaContract.Commande.TEMPS_INDEX));
     	((TextView) view).setText(temps.toString());
         return true;
     }
     
     
     
     
     public class CommandeListCursorAdapter extends CursorAdapter {
 		
     	public CommandeListCursorAdapter(Context context, Cursor c) {
 			super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
 		}
 	 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			Commande commande = new Commande(cursor);
 			TextView name = (TextView) view.findViewById(R.id.commande_item_time);
 			name.setText(Utils.dateToString(commande.getTemps()));
 			TextView nombre = (TextView) view.findViewById(R.id.commande_item_nombre);
			nombre.setText((int)commande.getNombre());
 		}
 	 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			LayoutInflater inflater = LayoutInflater.from(context);
 			View v = inflater.inflate(R.layout.commande_item, parent, false);
 			bindView(v, context, cursor);
 			return v;
 		}
     }
 }
     
