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
		
		if(ex.getString("FROM").equals("From_StartActivity")) {
			ButtonName = "プレイ画面へ";
		} else {
			ButtonName = "戻る";
		}
 		
 		ScrollView sc = new ScrollView(this);		
 		LinearLayout ll = new LinearLayout(this);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		ll.setBackgroundResource(R.drawable.androidmenu);
//		Button btn = new Button(this);
//		btn.setText(ButtonName);
//		btn.setOnClickListener(new click());
//		ll.addView(btn);
 		sc.addView(ll);		
 		setContentView(sc);
 		
 	}
 }
