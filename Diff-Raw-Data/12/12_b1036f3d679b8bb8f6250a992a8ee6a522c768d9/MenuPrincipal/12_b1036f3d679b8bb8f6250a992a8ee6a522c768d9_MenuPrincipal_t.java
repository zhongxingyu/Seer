 package cmdf2011.weff.interfaz.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MenuPrincipal extends Activity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.menuprincipal);
 
 
 		Button boton2 = (Button) findViewById(R.id.boton2);
				
 		boton2.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(),EstadoFuga.class);
 				startActivityForResult(i, 0);
 			}
 		});
 		
 	}
 
 
 	public void reportarFuga(View v) {
 		Intent i = new Intent(v.getContext(), TicketFugaActivity.class);
 		startActivity(i);
 	}
 	
 	public void consultarTicketsIngresados(View v) {
 		Intent i = new Intent(v.getContext(), ConsultaTicketsRegistradosActivity.class);
 		startActivity(i);
 	}
 
 
 }
