 package com.Ichif1205.anrakutei;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.Ichif1205.anrakutei.FieldSurfaceView.GameEventLiestener;
 
 public class MainActivity extends Activity implements GameEventLiestener {
 	private static String TAG = MainActivity.class.getSimpleName();
 	private FieldSurfaceView mFieldSurfaceView;
 	private TextView mScoreView = null;
 	private boolean mPauseFlg = false;
 	private boolean mGameEndFlg = false;
 	private int mStageId = 0;
 	private Status mStatus;
 	private ArrayList<StageInfo> mStageInfoLists = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d(TAG, "onCreate");
 		setContentView(R.layout.activity_main);
 		
 		// ステージIDやスコアのViewを設定
 		setView();
 		mFieldSurfaceView = (FieldSurfaceView) findViewById(R.id.FieldSurfaceView_id);
 		mFieldSurfaceView.setEventListener(this);
 		
 		final StageXmlParser xmlParser = new StageXmlParser(getApplicationContext());
 		
 		// TODO XMLの読み込みが終わるまでActivityを止めておく必要がある
 		Thread thread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				mStageInfoLists = xmlParser.parseStageXml();
 				mFieldSurfaceView.setStageInfo(mStageInfoLists.get(mStageId));
 			}
 		});
 		thread.start();
 
 		Log.d(TAG, "Start FindView");
		
 		Log.d(TAG, "End FindView");
 	}
 	
 	/**
 	 * FieldSurfaceView以外のViewの設定
 	 */
 	private void setView() {
 		Typeface face = Utils.getFonts(getApplicationContext());
 		TextView stageView = (TextView) findViewById(R.id.stageView_id);
 		mScoreView = (TextView) findViewById(R.id.scoreView_id);
 		mScoreView.setText("0");
 		stageView.setTypeface(face);
 		mScoreView.setTypeface(face);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d(TAG, "onResume");
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 	}
 
 	@Override
 	public void onBackPressed() {
 		// mPauseFlg が trueの時はダイアログが表示されている
 		if (!mPauseFlg) {
 			// ダイアログ表示
 			mPauseFlg = true;
 			mFieldSurfaceView.endLoop();
 			mStatus = mFieldSurfaceView.saveStatus();
 			showDialog();
 		} else {
 			// ダイアログで終了を選択された時
 			mPauseFlg = false;
 			mFieldSurfaceView.surfaceDestroyed(null);
 			super.onBackPressed();
 		}
 	}
 
 	@Override
 	protected void onUserLeaveHint() {
 		// ゲーム終了時はActivityを終了する
 		if (mGameEndFlg) { 
 			finish();
 			return;
 		}
 		// mPauseFlg が trueの時はダイアログが表示されている
 		if (!mPauseFlg) {
 			// ダイアログ表示
 			mPauseFlg = true;
 			mFieldSurfaceView.endLoop();
 			mStatus = mFieldSurfaceView.saveStatus();
 			showDialog();
 		} else {
 			// ダイアログで終了を選択された時
 			mPauseFlg = false;
 			mFieldSurfaceView.surfaceDestroyed(null);
 			super.onBackPressed();
 		}
 	}
 
 	/**
 	 * ダイアログ表示
 	 */
 	private void showDialog() {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		// アラートダイアログのタイトルを設定します
 		alertDialogBuilder.setTitle("終了");
 		// アラートダイアログのメッセージを設定します
 		alertDialogBuilder.setMessage("ゲームを終了しますか？");
 		// アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
 		alertDialogBuilder.setPositiveButton("はい",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						onBackPressed();
 					}
 				});
 		// アラートダイアログの否定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
 		alertDialogBuilder.setNegativeButton("いいえ",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mFieldSurfaceView.setInstance(mStatus);
 						mPauseFlg = false;
 						mFieldSurfaceView.restartLoop();
 					}
 				});
 		// アラートダイアログのキャンセルが可能かどうかを設定します
 		alertDialogBuilder.setCancelable(true);
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		// アラートダイアログを表示します
 		alertDialog.show();
 	}
 
 	@Override
 	public void endGame(int score) {
 		// ゲーム終了
 		mGameEndFlg = true;
 		// Result画面へ遷移
 		Intent intent = new Intent(this, ResultActivity.class);
 		intent.putExtra("score", score);
 		startActivity(intent);
 	}
 
 	@Override
 	public void addScore(int score) {
 		mScoreView.setText(Integer.toString(score));
 	}
 
 	@Override
 	public void nextStage(int stageId) {
 		Log.d(TAG, "Game Clear");
 	}
 }
