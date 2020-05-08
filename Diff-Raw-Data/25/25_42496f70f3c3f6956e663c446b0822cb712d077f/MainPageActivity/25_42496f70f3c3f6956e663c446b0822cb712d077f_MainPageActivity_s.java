 package edu.stanford.mdocent;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 
 public class MainPageActivity extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		Button takeButton = (Button) findViewById(R.id.Button00);
 		takeButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 					startSearchTour();
 				
 			}
 
 		});
 		
 		Button createButton = (Button) findViewById(R.id.Button01);
 		createButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				createTour();
 				
 			}
 
 		});
 		
 		Button accountButton = (Button) findViewById(R.id.Button02);
 		accountButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				myTours();
 				
 			}
 
 		});
 		
 		Button signOutButton = (Button) findViewById(R.id.Button03);
 		signOutButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				signIn();
 				
 			}
 
 		});
 
 	}
 
 	public void startSearchTour (){
 		Intent intent = new Intent(this, TourSearchActivity.class );
 		startActivity(intent);
 	}
 	
 	public void createTour (){
		Intent intent = new Intent(this, CreateTourActivity.class );
 		startActivity(intent);
 	}
 	
 	public void myTours (){
 		Intent intent = new Intent(this, MyToursActivity.class );
 		startActivity(intent);
 	}
 	
 	public void signIn (){
 		Intent intent = new Intent(this, SignInActivity.class );
 		startActivity(intent);
 	}
 
 
 }
