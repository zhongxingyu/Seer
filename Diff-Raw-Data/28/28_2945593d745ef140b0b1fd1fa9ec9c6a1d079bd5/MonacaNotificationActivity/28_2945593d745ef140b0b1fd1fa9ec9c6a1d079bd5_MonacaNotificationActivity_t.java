 package mobi.monaca.framework;
 
 import mobi.monaca.utils.gcm.GCMPushDataset;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class MonacaNotificationActivity extends Activity {
 	public static final String ACTION_RECEIVED_PUSH = "mobi.monaca.framework.receivedpush";
 	public static final String KEY_PUSHED_PROJECT_ID = "pushed_project_id";
 	public static final String KEY_RUNS_PROJECT_AT_ONCE = "run_project_at_once";
 
 	@Override
 	public void onCreate(Bundle savedInstance) {
 		super.onCreate(savedInstance);
 		process(getIntent());
 	}
 
 	@Override
 	protected void onNewIntent(Intent arg) {
 		process(arg);
 	}
 	private void process(Intent arg) {
 		Bundle b = arg.getExtras();
 		GCMPushDataset pushData = (GCMPushDataset)b.getSerializable(GCMPushDataset.KEY);
 		if (pushData == null) {
 			finish();
 			return;
 		}
 		MonacaApplication app = (MonacaApplication)getApplication();
 
 		if (app.getPages().size() == 0) {
 			Intent i = new Intent(this,  MonacaSplashActivity.class);
 			i.putExtra(GCMPushDataset.KEY, pushData);
 			startActivity(i);
 		} else {
 			Intent i = new Intent(ACTION_RECEIVED_PUSH);
 			i.putExtra(GCMPushDataset.KEY, pushData);
 			sendBroadcast(i);
 		}

		this.finish();
 	}
 }
