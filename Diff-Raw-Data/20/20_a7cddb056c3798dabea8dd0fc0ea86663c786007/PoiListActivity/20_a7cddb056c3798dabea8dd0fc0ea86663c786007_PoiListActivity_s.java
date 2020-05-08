 package edu.aau.utzon;
 
import java.util.ArrayList;
 import java.util.List;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
import edu.aau.utzon.outdoor.OutdoorActivity;
 import edu.aau.utzon.webservice.PointModel;
 import edu.aau.utzon.webservice.ProviderContract;
 
import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
 import android.view.View;
 import android.view.Window;
 
 public class PoiListActivity extends SherlockListActivity {
 	
 	//private static int THEME = R.style.Theme_Sherlock_Light;
 	
 	String[] mGuiText;
 	List<PointModel> mPois;
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.layout.menu_poilist, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.actionbar_search:
 			onSearchRequested();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Remove title bar
 		if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB ) {
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 		}
 
 		
 		mPois = PointModel.asPointModel(
 				getContentResolver().query(	ProviderContract.Points.CONTENT_URI, 
 				ProviderContract.Points.PROJECTIONSTRING_ALL, 
 				null, 
 				null, 
 				null));
 		
 		mGuiText = new String[mPois.size()];
 		for (int i = 0; i < mPois.size(); i++) {
 			mGuiText[i] = mPois.get(i).mDesc; // TODO: Use SharedPreferences for proximity instead of "50"
 		}
 		
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.poi_list, mGuiText));
 
 		  ListView lv = getListView();
 		  lv.setTextFilterEnabled(true);
 
 		  lv.setOnItemClickListener(new OnItemClickListener() {
 		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		    	Intent i = new Intent(getBaseContext(), PoiContentActivity.class);
 		    	i.putExtra("poi", mPois.get(position));
 		    	startActivity(i);
 		    }
 		  });
 	}
 }
