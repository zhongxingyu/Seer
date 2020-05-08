 package de.eahapp.gui;
 
 import java.util.Calendar;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.MenuItem;
 
 import de.eahapp.R;
 import de.eahapp.RegExHelp;
 import de.eahapp.SQLiteHelper;
 import de.eahapp.Utils;
 
 public class TerminDetailScreen extends ANotifyingScreen {
 	private SQLiteHelper db;
 	private TextView textViewName;
 	private TextView textViewDatum;
 	private TextView textViewDozent;
 	private TextView textViewOrt;
 	private Context con;
 	private Calendar cal = Calendar.getInstance();
 	private String name;
 	private String beschreibung;
 	private String person;
 	private String ort;
 	private String date1;
 	private String date2;
 	private String id;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		con = this.getApplicationContext();
 		Bundle b = getIntent().getExtras();
 		ActionBar navigation_bar = getSupportActionBar();
 		navigation_bar.setTitle(null);
 		navigation_bar.setSubtitle(null);
 		name = b.getString("name");
 		beschreibung = b.getString("beschreibung");
 		person = b.getString("person");
 		ort = b.getString("ort");
 		date1 = b.getString("date1");
 		date2 = b.getString("date2");
 		id = b.getString("id");
 		setContentView(R.layout.termin_detail_screen);
 		this.textViewName = (TextView) this.findViewById(R.id.textViewName);
 		this.textViewDatum = (TextView) this.findViewById(R.id.textViewDatum);
 		this.textViewDozent = (TextView) this.findViewById(R.id.textViewDozent);
 		this.textViewOrt = (TextView) this.findViewById(R.id.textViewOrt);
 		LinearLayout linLay = (LinearLayout) this.findViewById(R.id.layoutTermin);
 		linLay.setBackgroundColor(getColor(name));
 		textViewName.setText(name);
		textViewDatum.setText(date1 + "-" + date2);
 		textViewDozent.setText(person);
 		textViewOrt.setText(ort);
 
 	}
 
 	private int getColor(String name) {
 		// TextView btn = new TextView(con);
 		int color = this.con.getResources().getColor(R.color.other);
 		if (RegExHelp.match(name, "/P/")) {
 			color = this.con.getResources().getColor(R.color.praktikum);
 		} else if (RegExHelp.match(name, "/V/")) {
 			color = this.con.getResources().getColor(R.color.vorlesung);
 		} else if (RegExHelp.match(name, "/�/") || RegExHelp.match(name, "/S/")) {
 			color = this.con.getResources().getColor(R.color.uebung);
 		}
 		return color;
 	}
 
 	protected void back() {
 		this.startActivity(new Intent(this, Termin_Day.class));
 	}
 
 	@Override
 	public void refreshGUI(String arg) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean doInbackground() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean reloadGUI() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean setProgress() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Move back if tap on home button in actionbar
 		switch (item.getItemId()) {
 
 		case 12:
 			Intent myIntent = new Intent(this, AddTermin.class);
 			myIntent.putExtra("name", name);
 			myIntent.putExtra("beschreibung", beschreibung);
 			myIntent.putExtra("ort", ort);
 			myIntent.putExtra("id", id);
 			myIntent.putExtra("person", person);
 			myIntent.putExtra("date1", date1);
 			myIntent.putExtra("date2", date2);
 			this.startActivity(myIntent);
 			break;
 		case 13:
 			db = new SQLiteHelper(con, Utils.getAppFilespath(con));
 			db.deleteTermine(Integer.valueOf(id));
 			Toast.makeText(TerminDetailScreen.this, (this.getResources().getString(R.string.terminplaner_loesche)), Toast.LENGTH_SHORT).show();
 			db.close();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
