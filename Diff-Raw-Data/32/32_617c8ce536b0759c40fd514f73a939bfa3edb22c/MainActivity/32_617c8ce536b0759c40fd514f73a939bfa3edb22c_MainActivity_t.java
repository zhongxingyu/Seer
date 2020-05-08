 package ca.ualberta.cs.oneclick_cookbook;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.content.Intent;
 import android.view.Menu;
import android.view.View;
import android.widget.Button;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        
        //Button Listener Setup
        Button loginButton = (Button) findViewById(R.id.bHomeLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), MainActivity.class /*TODO change activity name*/);
				startActivity(intent);
			}
		});
        Button createNewRecipe = (Button) findViewById(R.id.bHomeCreate);
        createNewRecipe.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), MainActivity.class /*TODO change activity name*/);
				startActivity(intent);
			}
		});
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
