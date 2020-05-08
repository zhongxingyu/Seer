 package pl.byd.promand.Team1;
 
 import android.app.Activity;
import android.content.Intent;
 import android.os.Bundle;
import android.view.View;
import android.widget.Button;
 import com.promand.Team1.R;
 
 public class MyActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        Button button4 = (Button) findViewById(R.id.button4);

        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);

            }
        });
     }
}
