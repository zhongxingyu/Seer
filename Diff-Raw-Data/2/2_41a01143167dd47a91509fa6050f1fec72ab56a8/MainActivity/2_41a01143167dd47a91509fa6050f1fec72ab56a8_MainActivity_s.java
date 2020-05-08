 
 package name.roupsky.geno.project;
 
import name.roupsky.geno.library.LibraryActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class MainActivity extends Activity implements OnClickListener {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         findViewById(android.R.id.content).setOnClickListener(this);
     }
 
     @Override
     public void onClick(View v) {
         startActivity(new Intent(this, LibraryActivity.class));
     }
 
 }
