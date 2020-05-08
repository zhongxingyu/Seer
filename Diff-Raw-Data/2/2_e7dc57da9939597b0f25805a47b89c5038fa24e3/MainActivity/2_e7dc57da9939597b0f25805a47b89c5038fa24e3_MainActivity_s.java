 package cutin.sample;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MenuItem;
 
 import com.garlicg.cutinlib.CutinItem;
import com.garlicg.cutinlib.util.SimpleCutinScreen;
 
 public class MainActivity extends Activity implements SimpleCutinScreen.PickListener{
 	private SimpleCutinScreen mScreen;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		mScreen = new SimpleCutinScreen(this , getIntent());
 		setContentView(mScreen.getView());
 		
 		// Create your CutinService list:
 		// cutinName(SAMPLE) use for showing on the display in your app and CUT-IN Manager. 
 		ArrayList<CutinItem> list = new ArrayList<CutinItem>();
 		list.add(new CutinItem(CutinService1.class, "SAMPLE1"));
 		list.add(new CutinItem(CutinService2.class, "SAMPLE2"));
 		list.add(new CutinItem(CutinService3.class, "SAMPLE3"));
 		mScreen.setCutinList(list);
 		
 		// When called from CUT-IN Manager
 		if(mScreen.getState() == SimpleCutinScreen.STATE_PICK){
 			mScreen.setListener(this);
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getItemId() == android.R.id.home){
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	// OK button which is appear when called from CUT-IN Manager.
 	@Override
 	public void ok(Intent intent) {
 		setResult(Activity.RESULT_OK, intent);
 		finish();
 	}
 
 	// Cancel button which is appear when called from CUT-IN Manager.
 	@Override
 	public void cancel() {
 		finish();
 	}
 
 }
