 package school.trungi.tpac;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        Toast.makeText(this, "Ahoj, moje nová super androidí aplikace!", Toast.LENGTH_LONG).show();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
