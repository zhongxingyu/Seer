 /*
  *Copyright 2013 Peter Keomanvianh
  *[This program is licensed under the "GNU License"]
  *Please see the file LICENSE in the source
  *distribution of this software for license terms.
 */
 package com.example.paperandpens;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 import android.view.View;
 import android.widget.*;
 //import android.opengl.*;
 public class NewGame extends Activity {
 
 	Button enter, next;
 	TextView txt;String name;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.newgame);
 		enter = (Button)findViewById(R.id.enter);
	//	next = (Button)findViewById(R.id.next1);
 		txt = (TextView)findViewById(R.id.textView1);
 		
 		enter.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				EditText input = (EditText) findViewById(R.id.editText);
 				name = input.getText().toString();
 				txt.setText("Welcome "+ name);
 				try{
 				
 					Thread.sleep(2000);
 					gotoJob();
 					//txt.setText(name + ", on next screen what is class are you?");
 					
 				}catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 				
 				
 			}
 		});
 		
 	/*	next.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				gotoJob();
 			}
 		});*/
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	public void gotoJob()
 	{
 			
 				Intent jb = new Intent(NewGame.this,JobSelection.class);
 				Bundle b = new Bundle();
 				b.putString("NAME", name);
 				jb.putExtras(b);
 				startActivity(jb);
 		
 	}
 	
 	
 
 	
 }
