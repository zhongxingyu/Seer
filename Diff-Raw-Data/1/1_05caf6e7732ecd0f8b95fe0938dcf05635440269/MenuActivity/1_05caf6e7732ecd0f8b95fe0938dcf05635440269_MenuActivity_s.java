 package com.reindeermobile.pexeso.view;
 
 import com.reindeermobile.pexeso.R;
 import com.reindeermobile.pexeso.controller.DatabaseController;
 import com.reindeermobile.pexeso.entity.Record;
import com.reindeermobile.reindeerutils.mvp.MessageObject;
 import com.reindeermobile.reindeerutils.mvp.Presenter;
 import com.reindeermobile.reindeerutils.mvp.ViewHandler;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MenuActivity extends Activity implements OnClickListener {
 	public static final String TAG = "MenuActivity";
 
 	private ViewHandler viewHandler;
 
 	private Button playButton;
 	private Button toplistButton;
 	private Button aboutButton;
 
 	@Override
 	public void onClick(View v) {
 		if (v != null) {
 			switch (v.getId()) {
 			case R.id.buttonPlay:
 				this.startNewGame();
 				break;
 			case R.id.buttonToplist:
 				this.startToplist();
 				break;
 			case R.id.buttonAbout:
 				Record record = new Record();
 				record.setName("sdlkfé");
 				record.setClicks(324);
 				record.setTime(23f);
 				record.setLevel(0);
 				Bundle messageBundle = new Bundle();
 				messageBundle.putParcelable(DatabaseController.SAVE_RECORD,
 						record);
 				Presenter.getInst().sendModelMessage(
 						DatabaseController.SAVE_RECORD, messageBundle);
 				break;
 			default:
 				Log.d(TAG, "onClick - unknown view");
 				break;
 			}
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.layout_main);
 
 		this.initLayout();
 
 		this.viewHandler = new ViewHandler(TAG);
 
 		Presenter.getInst().subscribeToServices(this.viewHandler,
 				DatabaseController.SEND_RECORD_LIST);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Log.d(TAG, "onActivityResult - receive data");
 	}
 
 	private void initLayout() {
 		this.playButton = (Button) findViewById(R.id.buttonPlay);
 		this.toplistButton = (Button) findViewById(R.id.buttonToplist);
 		this.aboutButton = (Button) findViewById(R.id.buttonAbout);
 
 		this.playButton.setOnClickListener(this);
 		this.toplistButton.setOnClickListener(this);
 		this.aboutButton.setOnClickListener(this);
 	}
 
 	private void startToplist() {
 		Intent playIntent = new Intent(this, ToplistActivity.class);
 		this.startActivity(playIntent);
 	}
 
 	private void startNewGame() {
 		Intent playIntent = new Intent(this, NewGameActivity.class);
 		this.startActivity(playIntent);
 	}
 }
