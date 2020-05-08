 package com.studieux.main;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.studieux.bdd.DaoMaster;
 import com.studieux.bdd.DaoSession;
 import com.studieux.bdd.Matiere;
 import com.studieux.bdd.Periode;
 import com.studieux.bdd.PeriodeDao;
 import com.studieux.bdd.DaoMaster.DevOpenHelper;
 
 import de.greenrobot.dao.QueryBuilder;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.ListFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.pm.ActivityInfo;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Point;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 public class MatiereActivity extends MenuActivity {
 
 	AlertDialog.Builder builder;
 	AlertDialog alertDialog;
 	
 	//La priode slectionne
 	private Periode periode;
 	
 	TextView periodeName;
 	
 	//DB Stuff
 	private Cursor cursor;
 	private SQLiteDatabase db;
 	private DaoMaster daoMaster;
     private DaoSession daoSession;
     private PeriodeDao periodeDao;
 	private Long currentPeriodeId;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_matiere);
 		
 		initMenu();
 		View v1 = findViewById(R.id.viewRed2);
 		v1.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
 		currentButtonIndex = 2;
 		
 		periodeName = (TextView) findViewById(R.id.matiere_periodeName);
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 		//Db init
 		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "studieux-db.db", null);
         db = helper.getWritableDatabase();
         daoMaster = new DaoMaster(db);
         daoSession = daoMaster.newSession();
         periodeDao = daoSession.getPeriodeDao();
 		
 		Bundle donnees = getIntent().getExtras();
 		//Si une priode est dfinie
 		if (periode == null && donnees != null && donnees.containsKey("periodeId"))
 		{
 			periode = periodeDao.load(donnees.getLong("periodeId"));
 		}
 		else if (periode == null)//sinon, on cherche la priode courante si pas de priode
 		{
 			Date d = new Date();
 			QueryBuilder<Periode> qb = periodeDao.queryBuilder();
 			//on rcupre les priodes courante (date courant > date_debut et date courante < date_fin)
 			//normalement une seule priode doit arriver (on n'autorise pas le chevauchement de priodes)
 			qb.where(com.studieux.bdd.PeriodeDao.Properties.Date_debut.le(d), com.studieux.bdd.PeriodeDao.Properties.Date_fin.ge(d));
 			List<Periode> periodes = qb.list();
 			
 			if (periodes.size() >= 1)
 			{
 				periode = periodes.get(0);
 				periodeName.setText(periode.getNom());
 				//Toast.makeText(MatiereActivity.this, "lol:" + periode.getNom(), Toast.LENGTH_SHORT).show();
 			}
 		}
 		
 		
 	}
 	
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_matiere, menu);
 		return true;
 	}
 	
 	public void selectionnerPeriode(View v)
 	{
         periodeDao = daoSession.getPeriodeDao();
         
         //Recupration des periodes en BD
         String ddColumn = PeriodeDao.Properties.Date_debut.columnName;
         String orderBy = ddColumn + " COLLATE LOCALIZED ASC";
         cursor = db.query(periodeDao.getTablename(), periodeDao.getAllColumns(), null, null, null, null, orderBy);
         
         if(cursor.getCount() != 0)
         {
         	
         	//String[] from = { PeriodeDao.Properties.Nom.columnName, ddColumn, PeriodeDao.Properties.Date_fin.columnName };
             String[] from = {"title", "date_debut", "date_fin"};
             int[] to = { R.id.periodeListItemNomPeriode , R.id.periodeListItemDateDebut, R.id.periodeListItemDateFin };
             //SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.periode_list_item, cursor, from, to, 0);
             
             //On parse la liste pour convertir les long en Date, avant affichage
             List<Map<String, String>> data = new ArrayList<Map<String, String>>();
             cursor.moveToFirst();
             do
             {
             	//Contient le dtail d'une priode
             	Map<String, String> datum = new HashMap<String, String>(3);
             	datum.put("id", "" + cursor.getLong(PeriodeDao.Properties.Id.ordinal) );
             	datum.put("title", cursor.getString(PeriodeDao.Properties.Nom.ordinal));
             	SimpleDateFormat dateFormater = new SimpleDateFormat("dd MM yyyy");
             	Date d = new Date(cursor.getLong(PeriodeDao.Properties.Date_debut.ordinal));
             	datum.put("date_debut", "Date de dbut : " + dateFormater.format(d));
             	d = new Date(cursor.getLong(PeriodeDao.Properties.Date_fin.ordinal));
             	datum.put("date_fin", "Date de fin : " + dateFormater.format(d));
             	
             	data.add(datum);
             } while (cursor.moveToNext());
             
             //Toast.makeText(MatiereActivity.this, "lol:" + data.size(), Toast.LENGTH_SHORT).show();	
             
             //Adapter pour notre listView
             SimpleAdapter adapter = new SimpleAdapter(this, 
             		data,
             		R.layout.periodepopup_list_item,
                     from,
                     to);
     		final Integer i = -1;
     		PeriodeSelectionDialog dlg = new PeriodeSelectionDialog(MatiereActivity.this, adapter, i);
     		dlg.setTitle("Liste des priodes");
     		dlg.setDialogListener( new MyDialogListener()
     	    {
     		    public void userCanceled()
     		    {
     		    }
     			@Override
     			public void userSelectedAValue(Long value) {
     				// TODO Auto-generated method stub
     				//Toast.makeText(MatiereActivity.this, "id: " + value, Toast.LENGTH_SHORT).show();
     				periodeHasChanged(value);
     				MatiereActivity.this.currentPeriodeId = value;
     			}
     		});
 
 //    	    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 //    	    lp.copyFrom(dlg.getWindow().getAttributes());
 //    	    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
 //    	    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
 //    	    lp.horizontalMargin = 200;
     	   
     		dlg.show();
     		dlg.setOnDismissListener(new OnDismissListener() {
 				
 				@Override
 				public void onDismiss(DialogInterface dialog) {
 					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 					
 				}
 			});
     		Display display =((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
     	    Point p = new Point();
     	    display.getSize(p);
     		int width = p.x;
     	    int height=p.y;
 
     	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
     	    dlg.getWindow().setLayout((9*width)/10,(9*height)/10);
         }		
 	}
 	
 	public void periodeHasChanged(Long id)
 	{
 		this.periode = periodeDao.load(id);
 		//Toast.makeText(MatiereActivity.this, "id: " + periode.getNom(), Toast.LENGTH_SHORT).show();
 		periodeName.setText(periode.getNom());
 		this.updateList();
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case android.R.id.home:
 	            // This is called when the Home (Up) button is pressed
 	            // in the Action Bar.
 	            finish();
 	            return true;
 	        case R.id.menu_add:
 	        	this.ajouter(findViewById(R.id.menu_add));
 	        	break;
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 	
 	public void ajouter(View v)
 	{
 		Intent intention = new Intent(MatiereActivity.this, MatiereAddActivity.class);
 		intention.putExtra("periodeId", periode.getId());
 		startActivity(intention);
 		this.overridePendingTransition(R.anim.animation_enter_up,
 		        R.anim.animation_leave_up);
 		
 	}
 	
 	public void updateList()
 	{
 		periode.resetMatiereList();
 		
         //Recupration des periodes en BD
         String[] from = {"title", "coef"};
         int[] to = { R.id.matiereListItemNomMatiere , R.id.matiereListItemCoef };
         //SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.periode_list_item, cursor, from, to, 0);
         
         //On parse la liste pour convertir les long en Date, avant affichage
         List<Map<String, String>> data = new ArrayList<Map<String, String>>();
         //cursor.moveToFirst();
         //while (!cursor.isBeforeFirst() && !cursor.isLast())
         for (Matiere m : this.periode.getMatiereList())
         {
         	System.out.println(m.getNom());
         	//Contient le dtail d'une priode
         	Map<String, String> datum = new HashMap<String, String>(3);
         	datum.put("id", "" + m.getId() );
         	datum.put("title", m.getNom());
         	datum.put("coef", "Coef. : " + m.getCoef());
         	data.add(datum);
         }
         Toast.makeText(MatiereActivity.this, "updtLst: " + data.size(), Toast.LENGTH_SHORT).show();
         
         //Adapter pour notre listView
         SimpleAdapter adapter = new SimpleAdapter(this, 
         		data,
         		R.layout.matiere_list_item,
                 from,
                 to);
         
         //on rcupre la liste on lui affecte l'adapter
     	ListView listview = (ListView) findViewById(R.id.matiere_listView);
     	
     	listview.setAdapter(adapter);
     	
     	listview.setOnItemLongClickListener( new OnItemLongClickListener () {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				// On rcupre l'item click = HashMap<String, String>
 				HashMap<String, String> data = (HashMap<String, String>) arg0.getItemAtPosition(arg2);
 				
 				Intent intention = new Intent(MatiereActivity.this, MatiereAddActivity.class);
 				intention.putExtra( "matiereId", Long.parseLong(data.get("matiereId")) );
 				startActivity(intention);
 				
 				//Toast.makeText(MatiereActivity.this, "id: " + data.get("id"), Toast.LENGTH_SHORT).show();
 				
 				return false;
 			}
     		
     	});
     	if(periode.getMatiereList().size()>0)
     	{
     		findViewById(R.id.matiereExplications).setAlpha(0.0f);
     	}
     	else
     	{
     		findViewById(R.id.matiereExplications).setAlpha(1.0f);
     	}
     	
 	}
 	
 	public static interface MyDialogListener
 	{
 	    public void userSelectedAValue(Long value);
 	    public void userCanceled();
 	}
 
 }
