 package com.gradugation;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 
 public class WiresMiniGame extends Activity {
 	//Which wire you will find (1-4).
 	private int gameNumber;
 	
 	boolean win;
 	final Context context = this;
 	final Context context2 = this;
 	final Context context3 = this;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		gameNumber = 1 + (int)(Math.random()*4);
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_wires_mini_game);
 		
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 		
 		// set title and message
 		alertDialogBuilder.setTitle("Press the button that goes to Wire " + gameNumber + ".");
 		alertDialogBuilder.setMessage("Press Continue to play.");
 		alertDialogBuilder.setCancelable(false);
 		
 		// create continue button
 		alertDialogBuilder.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// if this button is clicked, close
 				// dialog box
 				dialog.cancel();
 			}
 		  });
 		
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_wires_mini_game, menu);
 		return true;
 	}
 
 	public void buttonOneClick(View view){
 		if(gameNumber == 1){
 			AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context2);
 			
 			// set title and message
 			alertDialogBuilder2.setTitle("Congradugation! You win!");
 			alertDialogBuilder2.setMessage("Press Continue.");
 			alertDialogBuilder2.setCancelable(false);
 			
 			// create continue button
 			alertDialogBuilder2.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					WiresMiniGame.this.finish();
 				}
 			  });
 			
 			// create alert dialog
 			AlertDialog alertDialog2 = alertDialogBuilder2.create();
 
 			// show it
 			alertDialog2.show();
 		}
 		else { 
 			AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(context3);
 
 			// set title and message
 			alertDialogBuilder3.setTitle("Sorry, you lose.");
 			alertDialogBuilder3.setMessage("Press Continue.");
 			alertDialogBuilder3.setCancelable(false);
 
 			// create continue button
 			alertDialogBuilder3.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					WiresMiniGame.this.finish();
 				}
 			});
 
 			// create alert dialog
 			AlertDialog alertDialog3 = alertDialogBuilder3.create();
 
 			// show it
 			alertDialog3.show();
 		}
 	}	
 	public void buttonTwoClick(View view){
 		if(gameNumber == 4){
 			AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context2);
 			
 			// set title and message
			alertDialogBuilder2.setTitle("Congradugation! You win!");
 			alertDialogBuilder2.setMessage("Press Continue.");
 			alertDialogBuilder2.setCancelable(false);
 			
 			// create continue button
 			alertDialogBuilder2.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					WiresMiniGame.this.finish();
 				}
 			  });
 			
 			// create alert dialog
 			AlertDialog alertDialog2 = alertDialogBuilder2.create();
 
 			// show it
 			alertDialog2.show();
 		}
 		else { 
 			AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(context3);
 
 			// set title and message
 			alertDialogBuilder3.setTitle("Sorry, you lose.");
 			alertDialogBuilder3.setMessage("Press Continue.");
 			alertDialogBuilder3.setCancelable(false);
 
 			// create continue button
 			alertDialogBuilder3.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					WiresMiniGame.this.finish();
 				}
 			});
 
 			// create alert dialog
 			AlertDialog alertDialog3 = alertDialogBuilder3.create();
 
 			// show it
 			alertDialog3.show();
 		}
 	}
 	public void buttonThreeClick(View view){
 		if(gameNumber == 2){
 			AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context2);
 			
 			// set title and message
 			alertDialogBuilder2.setTitle("Congradugation! You win!");
 			alertDialogBuilder2.setMessage("Press Continue.");
 			alertDialogBuilder2.setCancelable(false);
 			
 			// create continue button
 			alertDialogBuilder2.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					WiresMiniGame.this.finish();
 				}
 			  });
 			
 			// create alert dialog
 			AlertDialog alertDialog2 = alertDialogBuilder2.create();
 
 			// show it
 			alertDialog2.show();
 		}
 		else{ 
 			AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(context3);
 
 			// set title and message
 			alertDialogBuilder3.setTitle("Sorry, you lose.");
 			alertDialogBuilder3.setMessage("Press Continue.");
 			alertDialogBuilder3.setCancelable(false);
 
 			// create continue button
 			alertDialogBuilder3.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					WiresMiniGame.this.finish();
 				}
 			});
 
 			// create alert dialog
 			AlertDialog alertDialog3 = alertDialogBuilder3.create();
 
 			// show it
 			alertDialog3.show();
 		}
 	}
 	public void buttonFourClick(View view){
 		if(gameNumber == 3){
 			AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context2);
 			
 			// set title and message
 			alertDialogBuilder2.setTitle("Congradugation! You win!");
 			alertDialogBuilder2.setMessage("Press Continue.");
 			alertDialogBuilder2.setCancelable(false);
 			
 			// create continue button
 			alertDialogBuilder2.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					WiresMiniGame.this.finish();
 				}
 			  });
 			
 			// create alert dialog
 			AlertDialog alertDialog2 = alertDialogBuilder2.create();
 
 			// show it
 			alertDialog2.show();
 		}
 		else{ 
 			AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(context3);
 
 			// set title and message
 			alertDialogBuilder3.setTitle("Sorry, you lose.");
 			alertDialogBuilder3.setMessage("Press Continue.");
 			alertDialogBuilder3.setCancelable(false);
 
 			// create continue button
 			alertDialogBuilder3.setNeutralButton("Continue",new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,int id) {
 					WiresMiniGame.this.finish();
 				}
 			});
 
 			// create alert dialog
 			AlertDialog alertDialog3 = alertDialogBuilder3.create();
 
 			// show it
 			alertDialog3.show();
 		}	
 	}
 }
