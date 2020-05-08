 package mobile.group1;
 
import mobile.group1.DB.ScavengerDB;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         // test code
         ScavengerDB db = new ScavengerDB();
         db.getUser("test");
         
         
         
         
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void createNewHandler(View v){
     	
     	Intent intent = new Intent(this, CreateNewActivity.class);
     	startActivity(intent);
     	
     	
     }
 public void login(View v){
     	
     	Intent intent = new Intent(this, Main_Game.class);
     	startActivity(intent);
     	
     	
     }
 
     
 }
