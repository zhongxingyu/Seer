 package ejemploSergio.ejemploActividades;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 // Clase principal que se ejecuta al inicio de la aplicacin.
 public class ActividadPrincipal extends Activity {
 	/** Constructores y mtodos heredados a partir de aqu */
 
 	// Mtodo ejecutado cuando al inicio.
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// La GUI de esta actividad est definida en main.xml
 		setContentView(R.layout.main);
 
 		// Obtengo el elemento btnUno definido en main.xml
 		Button btnUno = (Button) this.findViewById(R.id.btnUno);
 
 		// Asigno al elemento btnUno un evento click de clase BtnUnoClick.
 		btnUno.setOnClickListener(new BtnUnoClick(this));
 
 		// Obtengo el elemento btnDatosUno definido en main.xml
 		Button btnDatosUno = (Button) this.findViewById(R.id.btnDatosUno);
 
 		// Asigno el elemento btnDatosUno un evento click de clase
 		// BtnDatosUnoClick.
 		btnDatosUno.setOnClickListener(new BtnDatosUnoClick(this));
 	}
 
 	// Mtodo ejecutado cuando termina la ejecucin de Actividad2.
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		// Ejecuto el mtodo de la clase madre.
 		super.onActivityResult(requestCode, resultCode, intent);
 
 		// Obtengo los datos recibidos de Actividad2.
 		Bundle extras = intent.getExtras();
 
 		// Si se han recibido datos.
 		if (extras != null) {
 			// Segn el cdigo de respuesta (en este caso slo hay una).
 			switch (requestCode) {
 			case 0:
 				// Obtengo el valor del dato con nombre datoDos.
 				String datoDos = extras.getString("datoDos");
 
				// Obtengo el elemento tvDatosUno definido en main.xml
				TextView tvDatosUno = (TextView) this
						.findViewById(R.id.tvDatosUno);

 				// Asigno el dato al elemento tvDatosUno.
 				tvDatosUno.setText(datoDos);
 
 				break;
 			}
 		}
 	}
 }
 
 // Clase que gestiona el evento click del elemento btnUno.
 class BtnUnoClick implements OnClickListener {
 	/** Propiedades a partir de aqu */
 
 	// Referencia a la actividad para acceder a sus datos.
 	private ActividadPrincipal actividadPrincipal;
 
 	/** Constructores y mtodos heredados a partir de aqu */
 
 	// El constructor se encarga de pedir la referencia a la actividad.
 	BtnUnoClick(ActividadPrincipal actividadPrincipal) {
 		this.actividadPrincipal = actividadPrincipal;
 	}
 
 	// Mtodo ejecutado en el evento click del elemento btnUno.
 	@Override
 	public void onClick(View v) {
 		// Para llamar a otra actividad se necesita un Intent.
 		Intent i = new Intent(this.actividadPrincipal, Actividad2.class);
 
 		// Se utiliza una llamada con respuesta.
 		this.actividadPrincipal.startActivityForResult(i, 0);
 	}
 }
 
 // Clase que gestiona el evento click del elemento btnDatosUno.
 class BtnDatosUnoClick implements OnClickListener {
 	/** Propiedades a partir de aqu */
 
 	// Referencia a la actividad para acceder a sus datos.
 	private ActividadPrincipal actividadPrincipal;
 
 	/** Constructores y mtodos heredados a partir de aqu */
 
 	// El constructor se encarga de pedir la referencia a la actividad.
 	BtnDatosUnoClick(ActividadPrincipal actividadPrincipal) {
 		this.actividadPrincipal = actividadPrincipal;
 	}
 
 	// Mtodo ejecutado en el evento click del elemento btnUno.
 	@Override
 	public void onClick(View v) {
 		// Para llamar a otra actividad se necesita un Intent.
 		Intent i = new Intent(this.actividadPrincipal, Actividad2.class);
 
 		// Se manda una cadena de texto (hola) bajo el nombre datoUno.
 		i.putExtra("datoUno", "hola");
 
 		// Se utiliza una llamada con respuesta.
 		this.actividadPrincipal.startActivityForResult(i, 0);
 	}
 }
