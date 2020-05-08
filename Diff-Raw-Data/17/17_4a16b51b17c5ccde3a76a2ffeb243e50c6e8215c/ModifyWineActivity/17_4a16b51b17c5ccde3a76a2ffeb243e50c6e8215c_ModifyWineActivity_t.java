 package ws.wiklund.vinguiden.activities;
 
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import ws.wiklund.guides.R;
 import ws.wiklund.guides.activities.FullAdActivity;
 import ws.wiklund.guides.activities.ModifyBeverageActivity;
 import ws.wiklund.guides.db.BeverageDatabaseHelper;
 import ws.wiklund.vinguiden.db.WineDatabaseHelper;
 
 public class ModifyWineActivity extends ModifyBeverageActivity {
 
 	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView view = (TextView) findViewById(R.id.Text_category);
		Spinner spinner = (Spinner) findViewById(R.id.Spinner_category);
		
		view.setVisibility(View.GONE);
		spinner.setVisibility(View.GONE);
	}

	@Override
 	protected BeverageDatabaseHelper getDatabaseHelper() {
 		return new WineDatabaseHelper(this);
 	}
 	
 	@Override
 	protected Class<?> getIntentClass() {
 		return FullAdActivity.class;
 	}
 	
 }
