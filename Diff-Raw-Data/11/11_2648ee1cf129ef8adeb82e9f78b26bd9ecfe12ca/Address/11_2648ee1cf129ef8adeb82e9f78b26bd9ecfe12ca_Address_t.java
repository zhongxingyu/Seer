 /**
  * 
  */
 package alcaldiadebarranquilla.prohibidoparquear;
 
 import alcaldiadebarranquilla.prohibidoparquear.util.AppGlobal;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * @author Soldier
  *
  */
 public class Address extends Activity {
 	
 	private EditText direccion;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_address);
		this.direccion = (EditText) findViewById(R.id.editText1);		
 	}
 	
 	public void btn_ok(View view){
 		
 		String dir = this.direccion.getText().toString();
 		
		if(dir.length()>4){
 			AppGlobal.getInstance().dispatcher.open(
 					Address.this, "thanks", true);
 		}else{
 			Toast.makeText(getBaseContext(),
 			        getResources().getString(R.string.direccion_layout_error_input),
 			        Toast.LENGTH_LONG).show();
 		}
 		
 	}
 	
 }
