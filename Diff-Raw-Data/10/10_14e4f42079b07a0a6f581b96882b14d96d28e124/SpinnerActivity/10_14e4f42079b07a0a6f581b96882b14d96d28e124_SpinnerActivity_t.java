 package info.llanox.mobile;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Spinner;
 import android.widget.ArrayAdapter;
 
 public class SpinnerActivity extends Activity{
 	
 	String[] items = { "Uno", "Dos", "Tres", "Cuatro", 
 			"Cinco", "Seis",
 			"Siete", "Ocho", "Nueve", "Diez" };
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.simple_spinner);
         //Traemos el objeto java Spinner que esta definido en el XML simple_spinner
         Spinner spSimple = (Spinner) this.findViewById(R.id.spSimple);
         
         //El constructor de este adapter recibe el contexto, un xml que representa 
         //un item simple para el spinner y los datos a ser mostrados en cada item
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,items);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);        
         spSimple.setAdapter(adapter);
         
         
     }
 
 }
