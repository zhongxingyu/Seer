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
			if(tvIP.getText().equals("IP: "))
				tvIP.setText("IP: LALALA");
 			
			else
				tvIP.setText("IP: ");
 			break;
 		}
 
 	}
 
 }
