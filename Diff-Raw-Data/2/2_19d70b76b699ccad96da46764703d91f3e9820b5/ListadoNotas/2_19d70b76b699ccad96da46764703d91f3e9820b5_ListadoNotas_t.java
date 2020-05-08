 package com.xoninja.benbox;
 
 
 import com.xoninja.benbox.R;
 
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.view.Menu;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 
 public class ListadoNotas extends ListActivity {
 
 	private NotesDbAdapter mDbHelper;
 	ListView listaNotas;
 	
     @SuppressWarnings("deprecation")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.listadonotas);
        //listaNotas = (ListView) findViewById(R.id.listView1);
        
         
         mDbHelper = new NotesDbAdapter(this);
         mDbHelper.open();
         
         // Get all of the notes from the database and create the item list
         Cursor c = mDbHelper.fetchAllNotes();
         startManagingCursor(c);
 
         String[] from = new String[] { NotesDbAdapter.KEY_TITLE };
         int[] to = new int[] { R.id.text1 };
         
         // Now create an array adapter and set it to display using our row
         SimpleCursorAdapter notes =
             new SimpleCursorAdapter(this, R.layout.fila, c, from, to);
         setListAdapter(notes);
         
         
        /*listaNotas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
 					long id) {
 				Intent myIntent = new Intent(ListadoNotas.this, Nota.class);				
 		        Bundle bundle = new Bundle();
 		        bundle.putLong("position", position);
 		        myIntent.putExtras(bundle);
 				startActivityForResult(myIntent, 0);
 			}       	
 		});    */
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.listadonotas, menu);
         return true;
     }
 }
