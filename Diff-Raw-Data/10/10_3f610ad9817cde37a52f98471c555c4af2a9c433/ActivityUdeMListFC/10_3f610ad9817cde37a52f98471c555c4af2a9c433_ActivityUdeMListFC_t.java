 package com.seboid.udem;
 
 import java.util.HashMap;
 import java.util.Set;
 
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.MergeCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.AsyncTaskLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // list feed et categories ensembles...
 
 public class ActivityUdeMListFC extends FragmentActivity implements
 LoaderManager.LoaderCallbacks<Cursor> {
 
 	// The loader's unique id. Loader ids are specific to the Activity or
 	// Fragment in which they reside.
 	private static final int LOADER_ID = 1;
 
 	// on garde une trace pour pouvoir invalider le tout avec onContentChanged()
	// PEUT ETRE NULL!!! (comme lorsqu'on change l'orientation de l'ecran...)
 	myASyncLoader asl;
 
 	// pour activer/desactiver un bout de menu
 	MenuItem menuRefresh=null;
 
 	// busy affichage
 	IntentFilter busyFilter;
 	BusyReceiver busyR;
 
 	SharedPreferences preferences;
 	SharedPreferences.Editor prefeditor;
 
 	public static final HashMap<String,String> feedName;
 	static {
 		feedName = new HashMap<String,String>();
 		feedName.put("recherche","La Recherche");
 		feedName.put("enseignement","Enseignement");
 		feedName.put("campus","Campus");
 		feedName.put("international","International");
 		feedName.put("culture","Culture");
 		feedName.put("sports","Sports");
 		feedName.put("multimedia","Multimédia");
 		feedName.put("revue-de-presse","Revue de presse");
 	}
 
 	ListView lv;
 	MySimpleCursorAdapter adapter; // pour afficher les lignes
 
 
 	TextView nouvellesTotal;
 
 
 	//	DBHelper dbH;
 	//	SQLiteDatabase db;
 	//	Cursor cursor;
 
 
 
 	// Mapping pour l'affiche. from contient les id des elements d'une rangees dans le mapping
 	// to contient les id des elements d'interface
 	static final String[] from = new String[] { "feed","category","count(*)"};
 	static final int[] to = new int[] {R.id.feedinfo,R.id.rowcat,R.id.rowcatcount };
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// pour le progress bar rotatif
 		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
 		setContentView(R.layout.rss_head);
 
 		// marche pas...
 		preferences=PreferenceManager.getDefaultSharedPreferences(this);
 		prefeditor=preferences.edit();
 		//preferences=getPreferences(MODE_PRIVATE);
 
 		lv=(ListView)findViewById(R.id.list);
 		nouvellesTotal=(TextView)findViewById(R.id.headcount);
 
 
 		// adapter (pour loader)
 		adapter = new MySimpleCursorAdapter(this, R.layout.rowcat, /* extendedCursor */ from, to); 
 		lv.setAdapter(adapter);
 
 		// notre activite va fournir les callbacks de ce loader.
 		getSupportLoaderManager().initLoader(LOADER_ID, null,
 				(android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>) this);
 
 
 		// access a la database
 		//		dbH=new DBHelper(this);
 		//		db=dbH.getReadableDatabase();
 
 		// le busy receiver
 		busyR=new BusyReceiver();
 		busyFilter=new IntentFilter("com.seboid.udem.BUSY");
 
 	}
 
 	public void actionCheck(View v) {
 		String feed=(String)v.getTag();
 		Boolean use=((CheckBox)v).isChecked();
 		//Toast.makeText(this, "actionCheck! feed is "+feed+" val="+use, Toast.LENGTH_LONG).show();
 		// pour l'instant on garde ca simple... on doit faire "reload" pour voir le resultat.
 		// c'est le mem editor qui doit faire le put et le apply... donc pas de pref.edit().put
 		prefeditor.putBoolean(feed, use);
 		prefeditor.apply();
 
 		// mise a jour de la bd...
 		// ne pas mettre a jour pour l'instant... attendre la prochaine mise a jour manuelle
 //		if( !use ) {
 //			Uri u = UdeMContentProvider.CONTENT_URI;
 //			getContentResolver().delete(u, DBHelper.C_FEED+" = '"+feed+"'" , null);
 //			this.asl.onContentChanged();
 //		}
 		
 		//		 SharedPreferences settings = getSharedPreferences(GAME_PREFERENCES, MODE_PRIVATE);
 		//	        SharedPreferences.Editor prefEditor = settings.edit();
 		//	        prefeditor.putString("UserName", "John Doe");
 		//	        prefEditor.putInt("UserAge", 22);
 		//	        prefEditor.commit();
 	}
 
 	public void actionFeed(View v) {
 		String tag=(String)v.getTag();
 		//Toast.makeText(this, "actionFeed! tage is "+tag, Toast.LENGTH_LONG).show();
 		Intent in = new Intent(this, ActivityUdeMNouvelles.class);
 		in.putExtra("type","feed");
 		in.putExtra("selection",tag);
 		in.putExtra("title",feedName.get(tag));
 		startActivity(in);
 	}
 
 	public void actionCat(View v) {
 		String tag=(String)v.getTag();
 		//Toast.makeText(this, "actionCat! tag is "+tag, Toast.LENGTH_LONG).show();
 		Intent in = new Intent(this, ActivityUdeMNouvelles.class);
 		in.putExtra("type","category");
 		in.putExtra("selection",tag);
 		in.putExtra("title",tag);
 		startActivity(in);
 	}
 
 	public void actionNouvelles(View v) {
 		String tag=(String)v.getTag();
 		//Toast.makeText(this, "actionFeed! tage is "+tag, Toast.LENGTH_LONG).show();
 		Intent in = new Intent(this, ActivityUdeMNouvelles.class);
 		//in.putExtra("type","feed");
 		//in.putExtra("selection",tag);
 		//in.putExtra("title",feedName.get(tag));
 		startActivity(in);
 	}
 
 
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		//db.close();		// ferme le database
 	}
 
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d("rss","resume!");
 
 
 		//		cursor=db.rawQuery("select _id,feed,category,count(*) from timeline group by category order by feed asc, category asc", null);
 		//
 		//		// on veut ajouter deux rangees au cursor
 		//		MatrixCursor extras = new MatrixCursor(new String[] { "_id", "feed","category","count(*)" });
 		//		// ajoute les feed qui on 0 elements
 		//		int j=1;
 		//		for(String i : feedCount.keySet()) {
 		//			if( feedCount.get(i)==0 ) {
 		//				extras.addRow(new String[] { ""+j, i,"cat","0" });
 		//			}
 		//			j++;
 		//		}
 		//		Cursor[] cursors = { cursor, extras };
 		//		extendedCursor = new MergeCursor(cursors);
 		//
 		//		startManagingCursor(extendedCursor);
 		//
 
 
 		// enregistre le receiver pour l'etat busy
 		registerReceiver(busyR,busyFilter);
 	}
 
 	@Override
 	protected void onPause() {
 		// on se rappellera ou on est...
 		// comme ca quand on revient...
 		//lastPos=lv.getFirstVisiblePosition();
 		super.onPause();
 		unregisterReceiver(busyR);
 	}
 
 
 
 	//	ViewBinder VIEW_BINDER = new ViewBinder() {
 	//		public boolean setViewValue(View view, Cursor c, int index) {
 	//			if( view.getId()==R.id.feedinfo) {
 	//				TextView tv=(TextView)view.findViewById(R.id.rowfeed);
 	//				TextView tvc=(TextView)view.findViewById(R.id.rowfeedcount);
 	//				String feed=c.getString(index);
 	//			
 	//				if( tv!=null ) tv.setText(feed);
 	//				if( tvc!=null ) tvc.setText(ActivityUdeMListFC.this.feedCount.get(feed));
 	//				return true;
 	//			}
 	//			return false;
 	//		}
 	//	};
 
 	//
 	// On va se definir notre propre adapter... ca va simplfier la chose...
 	//
 	class MySimpleCursorAdapter extends SimpleCursorAdapter {
 		Context context;
 		int layout;
 		LayoutInflater inflater;
 
 		public MySimpleCursorAdapter(Context context, int layout,
 				String[] from, int[] to) {
 			// on passe null comme cursor alors il sera manage par un loader
 			super(context, layout, null, from, to,0);
 			this.context=context;
 			this.layout=layout;
 			inflater = (LayoutInflater)   getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			Log.d("async","created MySimpleCursorAdapter");
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			//super.bindView(view, context, cursor);
 
 			CheckBox cb=(CheckBox)view.findViewById(R.id.rowfeedcheck);
 			TextView vf=(TextView)view.findViewById(R.id.rowfeed);
 			TextView vfc=(TextView)view.findViewById(R.id.rowfeedcount);
 			TextView vc=(TextView)view.findViewById(R.id.rowcat);
 			TextView vcc=(TextView)view.findViewById(R.id.rowcatcount);
 
 			Log.d("cursor","position="+cursor.getPosition());
 
 			// 1 = feed, 2=category, 3=count
 			String feed=cursor.getString(1);
 			String cat=cursor.getString(2);
 			String nbCat=cursor.getString(3);
 
 			// preferences du feed
 			boolean use = preferences.getBoolean(feed, false);
 
 			// on voit la categorie seulement si >0 elements
 			int n=Integer.parseInt(nbCat);
 			vc.setVisibility(n>0?View.VISIBLE:View.GONE);
 			vcc.setVisibility(n>0?View.VISIBLE:View.GONE);
 
 
 			// ... comment
 			// pas sur que c'est genial... mais bon... on devrait passer cette info par le callback du loader onfinishedload
			int nbItem;
			if( ActivityUdeMListFC.this.asl!=null ) nbItem=ActivityUdeMListFC.this.asl.feedCount.get(feed);
			else nbItem=0;
			
 			cb.setChecked(use);
 			vf.setText(ActivityUdeMListFC.this.feedName.get(feed)/*+"<"+cursor.getPosition()+">"*/);
 			vfc.setText(""+nbItem);
 
 			cb.setVisibility(View.VISIBLE);
 			vf.setVisibility(View.VISIBLE);
 			vfc.setVisibility(nbItem>0?View.VISIBLE:View.INVISIBLE);
 
 			// verifier si le precedent item a le meme feed
 			if( cursor.getPosition()>0 ) {
 				cursor.moveToPrevious();
 				if( cursor.getString(1).equals(feed) ) {
 					// on est pareil comme le precedent 
 					cb.setVisibility(View.GONE);
 					vf.setVisibility(View.GONE);
 					vfc.setVisibility(View.GONE);
 				}
 				cursor.moveToNext();
 			}
 
 			vc.setText(cat);
 			vcc.setText(nbCat);
 
 			// ajuste les tags au cas ou on clic
 			cb.setTag(feed);
 			view.findViewById(R.id.feedinfo).setTag(feed);
 			view.findViewById(R.id.catinfo).setTag(cat);
 		}
 	}
 
 	//
 	// menu
 	//
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.nouvelles, menu);
 		// trouve l'item mise-a-jour pour pouvoir le desactiver au besoin...
 		// ok pour android>=3.0, mais pas appelle tant qu' on ne presse pas menu dans android 2.33
 		menuRefresh=menu.findItem(R.id.menurefresh);
 		return true;
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.menurefresh:			
 			startService(new Intent(this,ServiceRss.class));
 			break;
 //		case R.id.menusource:
 //			startActivity(new Intent(this, ActivityUdeMListFC.class));
 //			break;
 		case R.id.menuprefs:
 			// Launch Preference activity
 			startActivity(new Intent(this, ActivityPreferences.class));
 			break;
 			//		case R.id.menufeed:
 			//			startActivity(new Intent(this, ActivityUdeMListFeed.class));
 			//			break;
 			//		case R.id.menucat:
 			//			startActivity(new Intent(this, ActivityUdeMListCat.class));
 			//			break;
 		}
 		return true;
 	}
 
 
 	//
 	// un broadcast receiver pour affiche le status "busy"...
 	// on veut afficher busy quand le service travaille...
 	//
 	class BusyReceiver extends BroadcastReceiver {
 		ProgressDialog mDialog=null;
 
 		@Override
 		public void onReceive(Context ctx, Intent in) {
 			//boolean busy = in.getExtras().getBoolean("busy");
 			//setProgressBarIndeterminateVisibility(busy);
 
 			String msg=in.getExtras().getString("msg");
 			int progress=in.getExtras().getInt("progress",-1);
 
 			if( progress<100 ) {
 				setProgressBarIndeterminateVisibility(true);
 				// pour android >3.0
 				if( menuRefresh!=null ) {
 					menuRefresh.setVisible(false);
 					menuRefresh.setEnabled(false);
 				}
 			}else{
 				setProgressBarIndeterminateVisibility(false);
 				if( menuRefresh!=null ) {
 					menuRefresh.setEnabled(true);
 					menuRefresh.setVisible(true);
 				}
 				if( msg!=null ) Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
 			}
 			
 			// on va reloader l'info et mettre a jour l' affichage...
 			if( asl!=null ) asl.onContentChanged();
 		}		
 	}
 
 	//
 	// callbacks du loaderManager
 	//
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int loaderId, Bundle b) {
 //		return new myASyncLoader(this.getApplicationContext());
 		asl=new myASyncLoader(this.getApplicationContext());
 		return asl;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		adapter.swapCursor(data);
 		// le total devrait etre passe dans data, mais on va aller le chercher directement
 		// ajuste l'affichage du total
		if( nouvellesTotal!=null && this.asl!=null ) nouvellesTotal.setText(Integer.toString(this.asl.total)); // total
 
 		// check isResumed() ...
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		adapter.swapCursor(null);
 	}
 
 	//
 	// loader asynchrone
 	//
 
 	public static class myASyncLoader extends AsyncTaskLoader<Cursor> {
 		final ForceLoadContentObserver mObserver; // en cas de forceload
 
 		Cursor extendedCursor; // le data qui est retourne
 		//Cursor cursor; // le resultat du query
 
 		// une petite hasmap contenant le nombre d'item dans chaque feed...
 		HashMap<String,Integer> feedCount;
 		int total;
 
 		DBHelper dbh;
 		SQLiteDatabase db;
 
 		public myASyncLoader(Context context) {
 			super(context);
 			mObserver = new ForceLoadContentObserver();			
 			dbh=new DBHelper(context);
 			db=dbh.getReadableDatabase();
 		}
 
 		@Override
 		public void deliverResult(Cursor data) {
 			Log.d("asyncloader","deliver results");
 
 			if (isReset()) {
 				// An async query came in while the loader is stopped.  We
 				// don't need the result.
 				if (extendedCursor != null) { releaseResources(extendedCursor); }
 			}
 			Cursor oldC=extendedCursor;
 			extendedCursor=data;
 
 			// If the Loader is currently started, we can immediately
 			// deliver its results.
 			if (isStarted()) { super.deliverResult(data); }
 
 			// At this point we can release the resources associated with
 			// 'oldApps' if needed; now that the new result is delivered we
 			// know that it is no longer in use.
 			if (oldC != null && oldC!=extendedCursor && !oldC.isClosed()) { releaseResources(oldC); }
 		}
 
 		// si on a des ressources a fermer
 		protected void releaseResources(Cursor c) {
 			Log.d("async","should releasing cursor");
 			c.close();
 		}
 
 
 		@Override
 		public Cursor loadInBackground() {
 			//			 synchronized (this) {
 			//		            if (isLoadInBackgroundCanceled()) {
 			//		                throw new OperationCanceledException();
 			//		            }
 			//		            mCancellationSignal = new CancellationSignal();
 			//		        }
 			//		        try {
 
 			// count total
 			Log.d("asyncloader","load in background");
 
 			//
 			// on compte le nombre d'articles pour chaque feed
 			//
 			feedCount=new HashMap<String,Integer>();
 
 			// on va definir un count de 0 pour les feed absents
 			Set<String> keys=feedName.keySet();
 			for(String i : keys) {
 				Log.d("cursor","reset feed "+i);
 				feedCount.put(i,0);
 			}
 
 			Cursor c=db.rawQuery("select _id,feed,count(*) from timeline group by feed", null);
 			total=0;
 			c.moveToFirst();
 			while( !c.isAfterLast() ) {
 				int nb=c.getInt(2);
 				Log.d("cursor","feed "+c.getString(1)+" = "+nb);
 				feedCount.put(c.getString(1),nb);
 				c.moveToNext();
 				total+=nb;
 			}
 			c.close();
 			c=null;
 
 			//extendedCursor=db.rawQuery("select _id,feed,category,count(*) from timeline group by category order by feed asc, category asc", null);
 
 			Cursor cursor=db.rawQuery("select _id,feed,category,count(*) from timeline group by category order by feed asc, category asc", null);
 
 			// on veut ajouter deux rangees au cursor
 			MatrixCursor extras = new MatrixCursor(new String[] { "_id", "feed","category","count(*)" });
 			// ajoute les feed qui on 0 elements
 			int j=1;
 			for(String i : feedCount.keySet()) {
 				if( feedCount.get(i)==0 ) {
 					extras.addRow(new String[] { ""+j, i,"cat","0" });
 				}
 				j++;
 			}
 			Cursor[] cursors = { cursor, extras };
 			extendedCursor = new MergeCursor(cursors);
 
 			Log.d("loader","got extended="+extendedCursor+", cursor="+cursor+", extra="+extras);
 			
 			if (extendedCursor != null) {
 				// Ensure the cursor window is filled
 				extendedCursor.getCount();
 				registerContentObserver(extendedCursor, mObserver);
 			}
 			return extendedCursor;
 			//		        } finally {
 			//		            synchronized (this) {
 			//		                mCancellationSignal = null;
 			//		            }
 			//		        }
 		}
 
 		/**
 		 * Registers an observer to get notifications from the content provider
 		 * when the cursor needs to be refreshed.
 		 */
 		void registerContentObserver(Cursor cursor, ContentObserver observer) {
 			cursor.registerContentObserver(mObserver);
 		}
 
 		@Override
 		protected void onStartLoading() {
 			// TODO Auto-generated method stub
 			super.onStartLoading();
 			Log.d("async","on start loading");
 			if (extendedCursor!=null) {
 				deliverResult(extendedCursor); // already available!
 			}
 			if( takeContentChanged() || extendedCursor==null ) forceLoad();
 		}
 
 		@Override
 		protected void onStopLoading() {
 			Log.d("async","on stop loading");
 			//cancelLoad();
 		}
 
 		@Override
 	    public void onCanceled(Cursor cursor) {
 	        if (cursor != null && !cursor.isClosed()) {
 	            cursor.close();
 	        }
 	    }
 
 	    @Override
 	    protected void onReset() {
 	        super.onReset();
 
 	        // Ensure the loader is stopped
 	        onStopLoading();
 
 	        if (extendedCursor != null && !extendedCursor.isClosed()) {
 	            extendedCursor.close();
 	        }
 	        extendedCursor = null;
 	    }
 
 		
 	}
 
 
 }
 
 
 
 
 
