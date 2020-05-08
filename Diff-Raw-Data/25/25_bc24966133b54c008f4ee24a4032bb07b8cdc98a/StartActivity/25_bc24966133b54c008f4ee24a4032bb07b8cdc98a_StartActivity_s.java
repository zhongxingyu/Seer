 package de.atp.activity;
 
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import de.atp.controller.DataController;
 import de.atp.requester.R;
 
 public class StartActivity extends Activity {
 
     private Button button;
     private EditText codeField;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
        this.button = (Button) findViewById(R.id.button1);
        this.button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handleCodeInput();

            }
        });

        codeField = (EditText) findViewById(R.id.editTextCode);

         if (DataController.isProbandFileExisting()) {
             startActivity(new Intent(this, InfoActivity.class));
             finish();
         } else {
             setContentView(R.layout.activity_start);
         }
     }
 
     private void handleCodeInput() {
 
         String code = codeField.getText().toString();
         if (codeIsValid(code)) {
             DataController.instance(code);
             startActivity(new Intent(this, TimetableActivity.class));
             finish();
         } else {
             Toast.makeText(this, "Falsche Eingabe!", Toast.LENGTH_SHORT).show();
         }
     }
 
     private static final Pattern INPUT_PATTERN = Pattern.compile("\\p{Alpha}{5}");
 
     private boolean codeIsValid(String c) {
         return INPUT_PATTERN.matcher(c).matches();
     }
 }
