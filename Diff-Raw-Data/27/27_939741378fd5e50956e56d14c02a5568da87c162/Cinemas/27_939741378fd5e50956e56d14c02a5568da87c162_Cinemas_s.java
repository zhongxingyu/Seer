 package ua.in.leopard.androidCoocooAfisha;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class Cinemas extends Activity implements OnItemClickListener {
 	private CinemaAdapter adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.cinemas);
         
         setTitle(getString(R.string.cinemas_title) + " - " + EditPreferences.getCity(this));
         
         ListView CinemasList = (ListView)findViewById(R.id.cinemas_list);
         DatabaseHelper DatabaseHelperObject = new DatabaseHelper(this);
         List<CinemaDB> cinemas = DatabaseHelperObject.getTodayCinemas();
         adapter = new CinemaAdapter(this, cinemas);
         CinemasList.setAdapter(adapter);
         CinemasList.setOnItemClickListener(this);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 		CinemaDB cinema_object = (CinemaDB)adapter.getItem(position);
 		Intent intent = new Intent(this, Cinema.class);
 		Bundle bundle = new Bundle();
 		bundle.putInt("cinema_id", cinema_object.getId());
 		intent.putExtras(bundle);
 		startActivity(intent);
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);
       MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.second_menu, menu);
       return true;
     }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
      case R.id.select_city:
          startActivity(new Intent(this, EditPreferences.class));
         return true;
       }
       return false;
    }
 
 }
