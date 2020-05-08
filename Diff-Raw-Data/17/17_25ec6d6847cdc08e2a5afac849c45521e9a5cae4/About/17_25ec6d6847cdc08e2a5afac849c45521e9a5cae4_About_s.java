 package com.img.jk.beethelion;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class About extends Activity implements OnClickListener{
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.about);
 		
 		TextView aboutContent = (TextView)findViewById(R.id.about_content);
 		aboutContent.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		finish();
 	}
 }
