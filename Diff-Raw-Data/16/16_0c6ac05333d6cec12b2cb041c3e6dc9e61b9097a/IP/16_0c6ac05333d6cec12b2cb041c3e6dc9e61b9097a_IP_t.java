 package com.thenewboston.jose;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class IP extends Activity implements OnClickListener{
 
 	private Button bShowIP;
 	private TextView tvIP;
 	private Button bResetIP;
 
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ip);
 		init();
 	}
 
 
 
 	private void init()
 	{
 		this.bShowIP = (Button) findViewById(R.id.bShowIP);
 		this.bResetIP = (Button) findViewById(R.id.bResetIP);

		this.tvIP = (TextView) findViewById(R.id.tvDisplayIP);

 		this.bShowIP.setOnClickListener(this);
 		this.bResetIP.setOnClickListener(this);
 	}
 
 	public void onPause()
 	{
 		super.onPause();
 		finish();
 	}
 
 
 
 	public void onClick(View v) {
 
 		switch (v.getId())
 		{
 		case R.id.bShowIP:
			tvIP.setText("IP: LALALA");
			break;
 			
		case R.id.bResetIP:
			tvIP.setText("IP: ");
 			break;
 		}
 
 	}
 
 }
