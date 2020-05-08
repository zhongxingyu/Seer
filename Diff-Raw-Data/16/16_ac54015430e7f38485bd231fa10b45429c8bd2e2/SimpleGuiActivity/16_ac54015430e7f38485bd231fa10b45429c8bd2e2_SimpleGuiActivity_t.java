 package dozedoff.AndroidSandbox;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 public class SimpleGuiActivity extends Activity {
 	private TextView text;
 	private int counter = 0;
 	private Button button;
 	private Button btnToast;
 	private EditText txtInput;
	private TableLayout table;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         text = (TextView)this.findViewById(R.id.text);
         button = (Button)this.findViewById(R.id.button);
         btnToast = (Button)this.findViewById(R.id.btnToast);
         txtInput = (EditText)this.findViewById(R.id.txtInput);
        table = (TableLayout)this.findViewById(R.id.table);
         
         updateText();
         button.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				counter++;
 				updateText();
 			}
 		});
         
         btnToast.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 			Toast toast = Toast.makeText(getApplicationContext(), "A Toast to you!", 1);
 			toast.show();
 			}
 		});
         
         txtInput.setOnEditorActionListener(new OnEditorActionListener() {
 			
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
					if(! txtInput.equals("")){
 					Toast toast = Toast.makeText(getApplicationContext(), "You entered the text: "+txtInput.getText(), 1);
 					toast.show();
					TextView newtext = new TextView(getApplicationContext());
					newtext.setText(txtInput.getText());
					table.addView(newtext);
					txtInput.setText("");
 					return true;
					}
 				}
 				return false;
 			}
 		});
     }
     
     
     
     private void updateText(){
     	text.setText("Button pressed "+counter+" times.");
     }
 }
