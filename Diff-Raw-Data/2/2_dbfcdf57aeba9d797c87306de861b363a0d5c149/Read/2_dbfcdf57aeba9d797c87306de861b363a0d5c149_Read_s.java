 package it.example.storygame;
 
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View.OnClickListener;
 import android.widget.RatingBar.OnRatingBarChangeListener;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class Read extends Activity {
 	Button Indietro;
 	Button story1;
 	Button guerra;
 	ImageView star;
 	private RatingBar ratingBar;
 	private RatingBar ratingBar2;
 	private TextView txtRatingValue;
 	private TextView txtRatingValue2;
 	private Button btnSubmit;
 	private Button btnSubmit2;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.read);
         addListenerOnRatingBar();
         addListenerOnButton();
         //addListenerOnRatingBar2();
         //addListenerOnButton2();
         //La prima gamestory del cavaliere oscuro
         story1=(Button)findViewById(R.id.story1);
         story1.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent storyintent = new Intent(Read.this, story1.class);
 				startActivity(storyintent);
 				Vibrator vb = (Vibrator)   getSystemService(Context.VIBRATOR_SERVICE);
 	            vb.vibrate(100);
 				
 			}
 		});
         //la seconda game story: il continuo del cavaliere oscuro
         guerra=(Button)findViewById(R.id.guerra);
         guerra.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent guerraintent = new Intent(Read.this, Guerra.class);
 				startActivity(guerraintent);
 				Vibrator vb = (Vibrator)   getSystemService(Context.VIBRATOR_SERVICE);
 	            vb.vibrate(100);
 			}
 		});
         
         /* star=(ImageView)findViewById(R.id.star1);
         star.setImageResource(R.drawable.button_focused);
         star.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				star=(ImageView)findViewById(R.id.star1);
 				star.setImageResource(R.drawable.button_normal);
 				
 			}
 		});*/
         Indietro= (Button)findViewById(R.id.Indietroread);
         Indietro.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				Intent indietro = new Intent(Read.this, MainActivity.class);
 				startActivity(indietro);
 				Vibrator vb = (Vibrator)   getSystemService(Context.VIBRATOR_SERVICE);
 	            vb.vibrate(100);
 			}
 		});
     }
 
 
    
 
     
     
     public void addListenerOnRatingBar() {
     	 
     	ratingBar = (RatingBar) findViewById(R.id.ratingstory1);
     	txtRatingValue = (TextView) findViewById(R.id.txtRatingValue);
      
     	//if rating value is changed,
     	//display the current rating value in the result (textview) automatically
     	ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
     		public void onRatingChanged(RatingBar ratingBar, float rating,
     			boolean fromUser) {
      
    			txtRatingValue.setText(String.valueOf(rating));
      
     		}
     	});
       }
      
       public void addListenerOnButton() {
      
     	ratingBar = (RatingBar) findViewById(R.id.ratingstory1);
     	btnSubmit = (Button) findViewById(R.id.btnSubmit);
      
     	//if click on me, then display the current rating value.
     	btnSubmit.setOnClickListener(new OnClickListener() {
      
     		@Override
     		public void onClick(View v) {
      
     			Toast.makeText(Read.this,
     				String.valueOf(ratingBar.getRating()),
     					Toast.LENGTH_SHORT).show();
      
     		}
      
     	});
      
       }
     
     
       
       
 
       
   	public void addListenerOnRatingBar2() {
   		// TODO Auto-generated method stub
       	ratingBar2 = (RatingBar) findViewById(R.id.ratingstory2);
       	
       	ratingBar2.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
       		public void onRatingChanged(RatingBar ratingBar2, float rating,
       			boolean fromUser) {
        
       			
   				txtRatingValue.setText(String.valueOf(rating));
        
       		}
       	});
   		
   	}
   	
   	
   	 private void addListenerOnButton2() {
   			// TODO Auto-generated method stub
   	    	ratingBar2 = (RatingBar) findViewById(R.id.ratingstory2);
   	    	
   	     
   	    	//if click on me, then display the current rating value.
   	    	btnSubmit2.setOnClickListener(new OnClickListener() {
   	     
   	    		@Override
   	    		public void onClick(View v) {
   	     
   	    			Toast.makeText(Read.this,
   	    				String.valueOf(ratingBar2.getRating()),
   	    					Toast.LENGTH_SHORT).show();
   	     
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
