 package team.nine;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class Home extends Activity {
     ImageView home, search;
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home);
 
         home = (ImageView)findViewById(R.home.home_icon);
         search = (ImageView)findViewById(R.home.search_icon);
     }
 }
