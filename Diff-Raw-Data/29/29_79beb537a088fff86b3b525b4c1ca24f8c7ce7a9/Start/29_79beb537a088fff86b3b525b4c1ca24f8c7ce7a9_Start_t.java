 package to.i.le;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Gravity;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Start extends Activity {
 
 	final static String FILE_NAME = "data";
 	Handler handler = new Handler();
 	SharedPreferences sp;
 	SharedPreferences.Editor editor;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
 
        // json取得
     	new UntikuTask(ToileActivity.untiku_array).execute();
     	
     	sp = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
     	editor = sp.edit();
     	
     	Timer timer = new Timer(true);
     	timer.schedule(new TimerTask() {
     		@Override
     		public void run() {
     	        handler.post( new Runnable() {
     	            public void run() {
    	            	if(!sp.getString("start","1st").equals("1st")) {
     	            		//NOTE: 一回目の起動
     	            		editor.putString("start","done").commit();
     	            		Intent i = new Intent(getApplicationContext(),Info.class);
     	            		i.putExtra("FROM", "From_StartActivity");
         	            	startActivityForResult(i,0);
     	            	} else {
     	            		//NOTE: 二回目以降の起動
         	            	Intent i = new Intent(getApplicationContext(),ToileActivity.class);
         	            	startActivityForResult(i,0);
     	            	}
     	            }
     	        });
     		}
 	    }, 3000);//NOTE: ミリ秒で待機時間を指定
     	
     	
     	//NOTE: 表示テスト　　あとでxmlに書き直すかもです。
     	LinearLayout ll = new LinearLayout(this);
     	ll.setOrientation(LinearLayout.VERTICAL);
     	ll.setGravity(Gravity.CENTER);
     	ImageView img = new ImageView(this);
     	img.setImageResource(R.drawable.ic_launcher);
     	TextView text = new TextView(this);
     	text.setText("スプラッシュ画面 (3秒間)");
     	text.setGravity(Gravity.CENTER);
     	ll.addView(img);
     	ll.addView(text);
     	setContentView(ll);
     	
     }
 	
 	//NOTE: 実機の「戻る」ボタンでこの画面に戻ってきたとき、正常終了。
 	protected void onActivityResult(int reqcode, int rescode, Intent data) {
         setResult(RESULT_OK);
         finish();
 	}
 	
 }
