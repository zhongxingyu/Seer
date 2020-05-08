 package com.gpap.euroconvert;
 
 import com.google.ads.AdRequest;
 
 import com.google.ads.AdView;
 import com.gpap.euroconvert.R;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Main extends Activity {
 
 	TextView apot;
 	TextView poun;
 	EditText money;
 	Calcula result;
 	AdView adView;
     double aa;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Button koump = (Button) findViewById(R.id.koum);
 
 		apot = (TextView) findViewById(R.id.apotelesmaeurokilo);
 		poun = (EditText) findViewById(R.id.poundsLabel);
 		money = (EditText) findViewById(R.id.money);
 		result = new Calcula();
 
 		koump.setOnClickListener(ok);

 		// Look up the AdView as a resource and load a request.
 		adView = (AdView) this.findViewById(R.id.adView);
 		// layout = (RelativeLayout)findViewById(R.id.ad);
 		adView.loadAd(new AdRequest());
 	}
 
 	private View.OnClickListener ok = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			aa = result.toEuroKg(
 					Double.parseDouble(money.getText().toString()),
 					Double.parseDouble(poun.getText().toString()));
 			apot.setText(String.valueOf(aa));
 
 			// TODO Auto-generated method stub
 
 		}
 	};
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
