 package com.gaitianxia.cache_bad_egg;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /*
  * 首页面 activity
  * 让用户选择 "发起新游戏" 或者 "加入到游戏"
  */
 public class MainActivity extends Activity {
 
 	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
 
 	private View.OnClickListener button_on_click = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Intent intent;
 			switch (v.getId()) {
 			case R.id.new_game_btn:
 				intent = new Intent(MainActivity.this, NewGameActivity.class);
 				startActivity(intent);
 				break;
 			case R.id.join_game_btn:
 				intent = new Intent(MainActivity.this, JoinGameActivity.class);
 				startActivity(intent);
 				break;
				
 			case R.id.setting_btn:
 				intent = new Intent(MainActivity.this, SettingActivity.class);
 				startActivity(intent);
 				break;
 			
 
 			default:
 				break;
 			}
 			
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		final Button new_game_btn = (Button) findViewById(R.id.new_game_btn);
 		final Button join_game_btn = (Button) findViewById(R.id.join_game_btn);
 		final Button setting_btn = (Button) findViewById(R.id.setting_btn);
 
 		// 调用 打电话的系统程序
 		// Intent intent2 = new Intent(Intent.ACTION_DIAL,
 		// Uri.parse("tel://10010"));
 
 		new_game_btn.setOnClickListener(button_on_click);
 		join_game_btn.setOnClickListener(button_on_click);
 		setting_btn.setOnClickListener(button_on_click);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 
 }
