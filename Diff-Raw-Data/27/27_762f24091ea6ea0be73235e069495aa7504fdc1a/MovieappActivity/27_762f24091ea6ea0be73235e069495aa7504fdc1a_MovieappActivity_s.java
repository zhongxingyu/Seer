 package androidnfc.movieapp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
 
 public class MovieappActivity extends Activity {
     /** Called when the activity is first created. */
	private Button nfcButton;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        
        //nfcButton = (Button)findViewById(R.id.nfcTagButton);
     }
 }
