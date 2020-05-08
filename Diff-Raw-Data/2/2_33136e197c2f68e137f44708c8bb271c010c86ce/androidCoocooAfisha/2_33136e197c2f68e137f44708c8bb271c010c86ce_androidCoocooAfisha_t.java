 package ua.in.leopard.androidCoocooAfisha;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class androidCoocooAfisha extends Activity implements OnClickListener {
 	private TextView current_city;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         View cinemasButton = findViewById(R.id.cinemas_button);
         cinemasButton.setOnClickListener(this);
         View theatersButton = findViewById(R.id.theaters_button);
         theatersButton.setOnClickListener(this);
         View updateButton = findViewById(R.id.update_button);
         updateButton.setOnClickListener(this);
         
         current_city=(TextView)findViewById(R.id.current_city);
         current_city.setText(Html.fromHtml(getString(R.string.current_city_title) + " <b>" + EditPreferences.getCity(this) + "</b>"));
         
         if (EditPreferences.getTheaterUrl(this) == "" || EditPreferences.getCinemasUrl(this) == ""){
         	startActivity(new Intent(this, EditPreferences.class));
         	Toast.makeText(this, getString(R.string.select_city_dialog), Toast.LENGTH_LONG).show();
         } else {        
 	        if (EditPreferences.getAutoUpdate(this)){
 	        	new DataProgressDialog(this);
 	        	if (Integer.parseInt(EditPreferences.getAutoUpdateTime(this)) != 0){
 	        		startService(new Intent(this, DataUpdateService.class));
 	        	}
 	        }
         }
     }
  
     @Override
     protected void onResume() {
        super.onResume();
        current_city.setText(Html.fromHtml(getString(R.string.current_city_title) + " <b>" + EditPreferences.getCity(this) + "</b>"));
     }
 
     @Override
     protected void onPause() {
        super.onPause();
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	if (EditPreferences.getAutoUpdate(this) && 
     		Integer.parseInt(EditPreferences.getAutoUpdateTime(this)) != 0){
     		stopService(new Intent(this, DataUpdateService.class));
     	}    	
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (EditPreferences.getTheaterUrl(this) == "" || EditPreferences.getCinemasUrl(this) == ""){
         	startActivity(new Intent(this, EditPreferences.class));
         	Toast.makeText(this, getString(R.string.select_city_dialog), Toast.LENGTH_LONG).show();
         } else {
 			switch (v.getId()) {
 			  case R.id.cinemas_button:
 				 startActivity(new Intent(this, Cinemas.class));
 		         break;
 			  case R.id.theaters_button:
 				 startActivity(new Intent(this, Theaters.class));
 		         break;
 			  case R.id.update_button:
 				 new DataProgressDialog(this);
 		         break;         
 		      }
         }
 	}
 	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.menu, menu);
       return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
       	case R.id.settings:
          startActivity(new Intent(this, EditPreferences.class));
          return true;
       	case R.id.about_button:
       	 startActivity(new Intent(this, About.class));
          break;
       }
       return false;
    }
 
 }
