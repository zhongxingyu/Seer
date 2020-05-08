 package de.dailyFH;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class dailyFHKalender extends Activity {
 
 	private final int TERMINEINFUEGEN = 0;
 	private final int TERMINLOESCHEN = 1;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	} // onCreate
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, TERMINEINFUEGEN, 0, "Termin eintragen");
		menu.add(0, TERMINLOESCHEN, 0, "Termin lschen");
 		return super.onCreateOptionsMenu(menu);
 	} // onCreateOptionsMenu
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO - Menuepunkte mit Funktion belegen
 		return super.onOptionsItemSelected(item);
 	}
 }
