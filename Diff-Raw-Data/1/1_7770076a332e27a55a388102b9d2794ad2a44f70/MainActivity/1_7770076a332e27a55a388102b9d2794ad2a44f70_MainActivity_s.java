 package com.lostmiracle.lifetrack;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	int counter;
 	Button add1, add5, sub1, sub5, bReset;
 	TextView display;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		counter = 20;
 		add1 = (Button) findViewById(R.id.add_one);
 		add5 = (Button) findViewById(R.id.add_five);
 		sub1 = (Button) findViewById(R.id.minus_one);
 		sub5 = (Button) findViewById(R.id.minus_five);
 		bReset = (Button) findViewById(R.id.bReset);
 		display = (TextView) findViewById(R.id.tvCounter);
 		
 		bReset.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				display.setText("" + 20);
 			}
 		});
 		add1.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				counter++;
 				display.setText("" + counter);
 			}
 		});
 		add5.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				counter += 5;
 				display.setText("" + counter);
 			}
 		});
 		sub1.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				counter--;
 				display.setText("" + counter);
 			}
 		});
 		sub5.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				counter -= 5;
 				display.setText("" + counter);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
