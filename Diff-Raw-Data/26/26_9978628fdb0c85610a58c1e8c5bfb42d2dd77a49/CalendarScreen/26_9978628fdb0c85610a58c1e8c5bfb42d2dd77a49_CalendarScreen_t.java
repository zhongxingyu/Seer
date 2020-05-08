 /**
  * 
  */
 package br.rio.puc.inf.app.activecal.screen;
 
import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
import android.widget.ArrayAdapter;
 
 /**
  * @author victor.pantoja
  *
  */
public class CalendarScreen extends ListActivity
 {
	private static final String[] menu = new String[] {"View","Eventos"};
 	
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         //this.getWindow().setBackgroundDrawableResource(R.drawable.fundo_240x320);
         
		this.setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,menu));
 	}
 
 	/* Creates the menu items */
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    menu.add(0, 0, 0, "New Event");
 	    menu.add(0, 1, 0, "Quit");
 	    return true;
 	}
 
 	/* Handles item selections */
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case 0:
 	        //newGame();
 	        return true;
 	    case 1:
 	        //quit();
 	        return true;
 	    }
 	    return false;
 	}
 }
