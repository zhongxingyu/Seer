 package de.soenkedomroese.haushaltsbuch;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class EintraegeAnzeigen extends Activity {
 
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
 		setContentView(R.layout.eintraegeanzeigen);
 		
 		//try {
 		
 			HaushaltsbuchDatabase db = new HaushaltsbuchDatabase(getBaseContext());
 			//status.setText("Database getbaseContext");
 			SQLiteDatabase dbconn = db.getReadableDatabase();
 			//status.setText("db.getReadabledataBase");
 			Cursor eintraege = dbconn.query(
 					"haushaltsbuch",
 					new String[] {"_id", "category", "direction", "itemname", "value" },
 					"",
 					new String[]{},
 					"",
 					"",
 					"date"
 				);
 			
 			startManagingCursor(eintraege);
 			
 			int i=0;
 			
 			//while (eintraege.moveToNext()) {
 				/*	
 				TextView category;
 				TextView direction;
 				TextView value;
 					
 				//TODO: jeweils eine neue TabellenZeile erstellen
 				if (i==1){
 					//Zeile 1 fllen
 					TableRow zeile = (TableRow) findViewById(R.id.tableRow1);
 					category = (TextView) zeile.findViewById(R.id.textView1);
 					category.setText(eintraege.getString(1));
 					direction = (TextView) zeile.findViewById(R.id.textView2);
 					direction.setText(eintraege.getString(2));
 					value = (TextView) zeile.findViewById(R.id.textView3);
 					value.setText(eintraege.getString(4));
 										
 					
 				}else if (i==2){
 					//Zeile 2 fllen
 					TableRow zeile = (TableRow) findViewById(R.id.tableRow2);
 					category = (TextView) zeile.findViewById(R.id.textView4);
 					category.setText(eintraege.getString(1));
 					direction = (TextView) zeile.findViewById(R.id.textView5);
 					direction.setText(eintraege.getString(2));
 					value = (TextView) zeile.findViewById(R.id.textView6);
 					value.setText(eintraege.getString(4));
 										
 					
 					
 				}else if (i==3){
 					//Zeile 3 fllen
 					TableRow zeile = (TableRow) findViewById(R.id.tableRow3);
 					category = (TextView) zeile.findViewById(R.id.textView7);
 					category.setText(eintraege.getString(1));
 					direction = (TextView) zeile.findViewById(R.id.textView8);
 					direction.setText(eintraege.getString(2));
 					value = (TextView) zeile.findViewById(R.id.textView9);
 					value.setText(eintraege.getString(4));
 										
 					
 				}
 				*/
 			//}
 			TextView status = (TextView) findViewById(R.id.status);
 			status.setText(R.string.msgSuccess);
 
 			//eintraege.close();
 			dbconn.close();
 		//} catch (Exception e) {
 			// TODO: handle exception
 			//TextView status = (TextView) findViewById(R.id.status);			
 			//status.setText(R.string.msgError);
 			
 		//}
 			
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.optExit:
 			finish();
 			return true;
 		case R.id.optShowAll:
 			final Intent intentShowAll = new Intent(this, EintraegeAnzeigen.class);
 			startActivity(intentShowAll);
 			return true;
 		case R.id.optInfo:
 			final Intent intentAbout = new Intent(this, About.class);
 			startActivity(intentAbout);
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return false;
 
 	}
 }
