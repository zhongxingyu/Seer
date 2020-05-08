 package yoloswagswag.swe_app;
 
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import java.lang.String;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     /* Called when the user clicks the Send button */
     public void okMessage(View view) {
         Intent intent = new Intent(this, timeSelector.class);
        EditText codeEdit =(EditText) findViewById(R.id.codeEdit);
        TextView error =(TextView) findViewById(R.id.errorMessage);
        String code = codeEdit.getText().toString();
        if(code.length()==5){
            startActivity(intent);
            error.setText("");
        }
        else error.setText("Ihr Code muss aus 5 Zeichen bestehen!");
 
     }
     
 }
