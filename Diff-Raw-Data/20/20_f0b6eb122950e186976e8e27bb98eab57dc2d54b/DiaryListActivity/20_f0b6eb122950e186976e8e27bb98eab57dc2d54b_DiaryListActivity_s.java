 /**
 * 第??????(FB???????)
 * 顯示??????????????????????Help? * 
 * @author ???? * 
  */
 
 package com.ntu.iddc.exnote;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.markupartist.android.widget.ActionBar;
 
 public class DiaryListActivity extends Activity {
 	public static final String APP_SP = "applicationSharedPreferences";
 	public static final String USER_ID = "userFacebookId";
 	public static final String USER_NAME = "userFacebookName";
 	public static final int REQUEST_CODE_LOGIN = 11;
 
 	public static SharedPreferences settings;
 	public static String userId;
 	public static String userName;
 
 	private ActionBar mActionBar;
 	private DiaryView dv_diaryView1, dv_diaryView2, dv_diaryView3,
 					  dv_diaryView4, dv_diaryView5, dv_diaryView6;
 	private TextView tv_selectedDiaryName;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.diary_list_layout);
 
 		if (settings == null) {
 			settings = getSharedPreferences(APP_SP, MODE_PRIVATE);
 		}
 		String check = settings.getString(USER_ID, "");// get User pre-used id.
 
 		if (check != "" && check != null) { // get userId succeed.
 			userId = check;
 			userName = settings.getString(USER_NAME, "No Name");
 		} else {
 			Intent intent = new Intent(DiaryListActivity.this,
 									   LoginActivity.class);
 			startActivityForResult(intent, REQUEST_CODE_LOGIN);
 		}
 
 		setViews();
 		setListeners();
 		setDiaries();
 	}
 
 	private void setViews() {
 		mActionBar = (ActionBar) findViewById(R.id.action_bar);
		mActionBar.setTitle(userName + "?????");
 		
 		dv_diaryView1 = (DiaryView) findViewById(R.id.diaryView1);
 		dv_diaryView2 = (DiaryView) findViewById(R.id.diaryView2);
 		dv_diaryView3 = (DiaryView) findViewById(R.id.diaryView3);
 		dv_diaryView4 = (DiaryView) findViewById(R.id.diaryView4);
 		dv_diaryView5 = (DiaryView) findViewById(R.id.diaryView5);
 		dv_diaryView6 = (DiaryView) findViewById(R.id.diaryView6);
 		
 		tv_selectedDiaryName = (TextView) findViewById(R.id.diaryName_text);
 	}
 
 	private void setListeners() {
 		// TODO set Listeners
 	}
 
 	private void setDiaries() {
		dv_diaryView1.setText("?????);
 		dv_diaryView1.setVisibility(View.VISIBLE);
 		
		dv_diaryView2.setText("?????);
 		dv_diaryView2.setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		menu.add(0, Menu.FIRST, 1, "New Diary");
 		menu.add(0, Menu.FIRST + 1, 1, "Logout");
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case Menu.FIRST:
 			// TODO create new diary.
 			break;
 		case Menu.FIRST + 1:
 			settings.edit().putString(USER_ID, "")
 						   .putString(USER_NAME, "")
 						   .commit();
 			userId = "";
 			userName = "";
 			Intent intent = new Intent(DiaryListActivity.this,
 					LoginActivity.class);
 			startActivity(intent);
 			break;
 		default:
 			// Do nothing
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	//onClickListener for DiaryView
 	public void OnDiaryClickListener(View v){
 		DiaryView dv = (DiaryView) v;
 		tv_selectedDiaryName.setText(dv.getText());
 		Intent intent = new Intent();
      	 intent.setClass(DiaryListActivity.this, DiaryContentViewActivity.class);
 		Bundle bundle = new Bundle();
      	 bundle.putString("authorId", "1");
      	 bundle.putString("diaryId", "1");
      	 bundle.putString("authorName", "1");
 
      	 intent.putExtras(bundle);
 		startActivity(intent);
 
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == REQUEST_CODE_LOGIN) {
 			switch (resultCode) {
 			case RESULT_OK:
				Toast.makeText(DiaryListActivity.this, "??????,
 						Toast.LENGTH_LONG).show();
 				break;
 			case RESULT_CANCELED:
				Toast.makeText(DiaryListActivity.this, "??????,
 						Toast.LENGTH_LONG).show();
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 }
