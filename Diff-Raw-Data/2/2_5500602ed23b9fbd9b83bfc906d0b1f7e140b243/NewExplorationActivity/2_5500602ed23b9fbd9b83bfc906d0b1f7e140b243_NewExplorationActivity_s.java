 package jp.knct.di.c6t.ui.exploration;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.DebugSharedPreferencesClient;
 import jp.knct.di.c6t.model.Exploration;
 import jp.knct.di.c6t.model.Route;
 import jp.knct.di.c6t.model.User;
 import jp.knct.di.c6t.util.ActivityUtil;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.DatePicker;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class NewExplorationActivity extends Activity implements OnClickListener {
 	private Route mRoute;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_new_exploration);
 
 		mRoute = getIntent().getParcelableExtra(IntentData.EXTRA_KEY_ROUTE);
 
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.new_exploration_ok,
 		});
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.new_exploration_ok:
 			Exploration exploration = createExplorationFromForms();
 			new DebugSharedPreferencesClient(this).saveExploration(exploration);
 			try {
				Toast.makeText(this, exploration.toJSON().toString(2), 1);
 			}
 			catch (JSONException e) {}
 
 			// TODO
 			// Intent intent = new Intent()
 			// .putExtra(IntentData.EXTRA_KEY_ROUTE, mRoute);
 			// startActivity(intent);
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	private Exploration createExplorationFromForms() {
 		User myself = new DebugSharedPreferencesClient(this).getMyUserData();
 		Date date = getDateTime();
 		String description = ActivityUtil.getText(this, R.id.new_exploration_description);
 		return new Exploration(myself, mRoute, date, description);
 	}
 
 	private Date getDateTime() {
 		DatePicker datePicker = (DatePicker) findViewById(R.id.new_exploration_date);
 		TimePicker timePicker = (TimePicker) findViewById(R.id.new_exploration_time);
 
 		int year = datePicker.getYear();
 		int month = datePicker.getMonth();
 		int day = datePicker.getDayOfMonth();
 		int hour = timePicker.getCurrentHour();
 		int minute = timePicker.getCurrentMinute();
 		return new GregorianCalendar(year, month, day, hour, minute).getTime();
 	}
 
 }
