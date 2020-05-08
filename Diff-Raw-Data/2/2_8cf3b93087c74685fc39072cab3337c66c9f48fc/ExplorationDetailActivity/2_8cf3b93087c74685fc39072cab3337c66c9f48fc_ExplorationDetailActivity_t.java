 package jp.knct.di.c6t.ui.exploration;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.model.Exploration;
 import jp.knct.di.c6t.util.ActivityUtil;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class ExplorationDetailActivity extends Activity implements OnClickListener {
 	private Exploration mExploration;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_exploration_detail);
 
 		mExploration = getIntent().getParcelableExtra(IntentData.EXTRA_KEY_EXPLORATION);
 		putExplorationDataIntoComponents(mExploration);
 
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.exploration_detail_join,
 				R.id.exploration_detail_calender,
 				R.id.exploration_detail_share,
 		});
 
 	}
 
 	private void putExplorationDataIntoComponents(Exploration exploration) {
 		new ActivityUtil(this)
 				.setText(R.id.exploration_detail_name, exploration.getRoute().getName())
 				.setText(R.id.exploration_detail_host, exploration.getHost().getName())
 				.setText(R.id.exploration_detail_start_location, exploration.getRoute().getStartLocation().toString())
 				.setText(R.id.exploration_detail_description, exploration.getDescription());
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent intent;
 		switch (v.getId()) {
 		case R.id.exploration_detail_join:
 			intent = new Intent(this, ExplorationStartActivity.class)
 					.putExtra(IntentData.EXTRA_KEY_EXPLORATION, mExploration);
 			startActivity(intent);
 			break;
 
 		case R.id.exploration_detail_calender:
 			// TODO
 			break;
 
 		case R.id.exploration_detail_share:
 			intent = new Intent(Intent.ACTION_SEND)
 					.setType("text/plain")
 					.putExtra(Intent.EXTRA_EMAIL, "")
 					.putExtra(Intent.EXTRA_SUBJECT, "T:" + mExploration.getRoute().getName())
 					.putExtra(Intent.EXTRA_TEXT, mExploration.getDescription());
 
			startActivity(Intent.createChooser(intent, "L"));
 			break;
 
 		default:
 			break;
 		}
 	}
 }
