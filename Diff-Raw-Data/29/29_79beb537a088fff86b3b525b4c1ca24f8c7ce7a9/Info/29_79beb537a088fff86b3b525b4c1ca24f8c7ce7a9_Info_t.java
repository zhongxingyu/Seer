 package to.i.le;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class Info extends Activity {
 	
 	String ButtonName;
 	Bundle ex;
 	
 	public class click implements android.view.View.OnClickListener {
 		public void onClick(View v) {
 			if(ex.getString("FROM").equals("From_StartActivity")) {
 				Intent i = new Intent(getApplicationContext(),ToileActivity.class);
 				startActivity(i);
 			} else {
 				finish();
 			}
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		ex = getIntent().getExtras();

		//TODO: URL：　[端末のブラウザを立ち上げる] or [URLの文字列を表示]。
		//TODO: スタートボタン
 		
 		ScrollView sc = new ScrollView(this);		
 		LinearLayout ll = new LinearLayout(this);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		ll.setBackgroundResource(R.drawable.androidmenu);
		ll.setOnClickListener(new click());
 		sc.addView(ll);		
 		setContentView(sc);
 		
 	}
 }
