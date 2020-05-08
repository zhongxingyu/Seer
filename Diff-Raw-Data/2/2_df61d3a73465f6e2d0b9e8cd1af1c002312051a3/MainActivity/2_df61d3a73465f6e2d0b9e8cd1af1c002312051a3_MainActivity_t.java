 package com.hack.regionunlocked;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements GameStatusCompleteListener {
 	
 	GameStatus scanStatus;
 	
 	@Override
 	public void setString(String s){
 		TextView textView1 = (TextView) findViewById(R.id.textView1);
 		textView1.setText(s);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		ImageButton scanButton = (ImageButton) findViewById(R.id.scanButton);
		onActivityResult(1, RESULT_OK, "885370429671");
 	    /*
 		scanButton.setOnClickListener(new View.OnClickListener() {
 	    	public void onClick(View v) {
 	    		Intent clickIntent = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
 	    		startActivityForResult(clickIntent, 1);
 			 }
 		 });
 		 */
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onGameStatusComplete(){
 		setString("WOOOOOOOOOOO!");
 		/*
 		if (scanStatus.wasSuccessful()){
 			setString(scanStatus.getSupportAsText());
 		}else{
 			setString("Not found in databases.");
 		}
 		*/
 	}
 	
 	@Override
 	public void onGameStatusError(Exception ex){
 		System.out.println("Because fuck you, that's why.");
 		setString(ex.toString());
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, String barcode){
 			//int requestCode, int resultCode, Intent data) {
 
 		if (requestCode == 1) {
 
 			if(resultCode == RESULT_OK){
 				String result = "Looking up " + barcode + ".";
 				try{
 					scanStatus = new GameStatus(barcode, this);
 					scanStatus.execute();
 				}catch (Exception e){
 					result = e.toString();
 				}
 				setString(result);
 			}
 			if (resultCode == RESULT_CANCELED) {    
 				//Do nothing! Wait for the user to initiate another go.
 		    }
 		}
 	}
 
 }
