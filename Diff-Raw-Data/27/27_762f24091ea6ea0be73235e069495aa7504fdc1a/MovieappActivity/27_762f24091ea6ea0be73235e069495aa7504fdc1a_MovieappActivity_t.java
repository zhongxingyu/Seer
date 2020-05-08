 package androidnfc.movieapp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
import android.widget.TextView;
 
 public class MovieappActivity extends Activity {
     /** Called when the activity is first created. */
	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        Button nfcButton = (Button)findViewById(R.id.nfcTagButton);
     }
 }
