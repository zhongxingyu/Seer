 package cmdf2011.weff.interfaz.activities;
 
 import java.util.List;
 
 import cmdf2011.weff.rest.PrioridadRest;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public class TicketFugaActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket_fuga_agua);		
 		
 		Spinner s = (Spinner) findViewById(R.id.prioridadSpinner);
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, PrioridadRest.findPrioridadAll(10).toArray());
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s.setAdapter(adapter);
 		
 		
 	}
 
 	public void cancelar(View v) {
 		finish();
 	}
 }
