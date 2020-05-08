 package luke.crisford.skyispy;
 
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 	 
 	public class Level1 extends Activity {
 		MediaPlayer correct;
 		MediaPlayer incorrect;
 		String CorrectAnswer = null;
 		String SpellAnswer = null;
 		
 	    @Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.level1);
 	        
 	        //*************Deals with Answers****************\\
 	        ImageView image = (ImageView)findViewById(R.id.full_image_view); 
 	        
 	        final EditText answer = (EditText)findViewById(R.id.Answer);
 	        answer.setSingleLine(); //Handles return/enter key
 	        // setOnKeyListener detects when "done" or "enter" is pressed
 	        answer.setOnKeyListener(new View.OnKeyListener() {
 	            public boolean onKey(View v, int keyCode, KeyEvent event) {
 	                // If the event is a key-down event on the "enter" button
 	                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
 	                  // Checking Answer
 	                	if (answer.getText().toString().trim().equalsIgnoreCase(CorrectAnswer)) {	
 							// If correct makes small pop up at bottom of screen
 							Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_SHORT).show();
 							correct = MediaPlayer.create(Level1.this, R.raw.correct); //Play sound effect for correct
 							correct.start();
 						} 
	                	if (answer.getText().toString().trim().equalsIgnoreCase(CorrectAnswer)) {	
 							// If there is a spelling mistake player is informed
							Toast.makeText(getApplicationContext(), "Check your spelling, your almost there", Toast.LENGTH_SHORT).show();
							}
	                	else {
 							// If incorrect makes small pop up at bottom of screen
 							Toast.makeText(getApplicationContext(), "Incorrect", Toast.LENGTH_SHORT).show();
 							incorrect = MediaPlayer.create(Level1.this, R.raw.incorrect); //Play sound effect for correct
 							incorrect.start();
 						}
 	                }
 					return false;
 	            }
 	        });
 	        
 	        
 	        Integer number = getIntent().getExtras().getInt("ButtonNo");
 	        
 	        switch (number) {
 		        case 1:
 					image.setImageResource(R.drawable.angel_of_north); 	// Sets the Picture
 					CorrectAnswer="Angel Of The North";					// Sets the Answers each word is auto-capitalised
 					SpellAnswer="Angle Of The North";                 // Incorrect but similar spellings
 					break;
 		        case 2:
 					image.setImageResource(R.drawable.burj); 	
 					CorrectAnswer="Burj Kahlifa";
 					SpellAnswer="Burge Kalifa";
 					SpellAnswer="Burj Kalifa";
 					SpellAnswer="Burge Kahlifa";
 					break;
 		        case 3:
 					image.setImageResource(R.drawable.cichen);
					CorrectAnswer="Cichen Itza";
 					break;
 		        case 4:
 					image.setImageResource(R.drawable.cristo);
 					CorrectAnswer="Christ The Redeemer";
 					CorrectAnswer="Cristo Redentor";
 					break;
 		        case 5:
 					image.setImageResource(R.drawable.forbidden_city);
 					CorrectAnswer="The Forbidden City";
 					break;
 		        case 6:
 					image.setImageResource(R.drawable.fukushima);
 					CorrectAnswer="Fukushima Power Plant";
 					CorrectAnswer="Fukushima";
 					break;
 		        case 7:
 					image.setImageResource(R.drawable.kremlin);
 					CorrectAnswer="Kremlin";
 					CorrectAnswer="The Kremlin";
 					break;
 		        case 8:
 					image.setImageResource(R.drawable.louvre);
 					CorrectAnswer="Louvre";
 					CorrectAnswer="The Louvre";
 					break;
 		        case 9:
 					image.setImageResource(R.drawable.machu_pichu);
 					CorrectAnswer="Machu Picchu";
 					break;
 		        case 10:
 					image.setImageResource(R.drawable.pripyat);
 					CorrectAnswer="Pripyat";
 					CorrectAnswer="Chernobyl";
 					break;
 		        case 11:
 					image.setImageResource(R.drawable.pyramids);
 					CorrectAnswer="Pyramids";
 					CorrectAnswer="The Pyramids";
 					CorrectAnswer="Pyramids Of Giza";
 					CorrectAnswer="The Pyramids Of Giza";
 					break;
 		        case 12:
 					image.setImageResource(R.drawable.rushmore);
 					CorrectAnswer="Mt Rushmore";
 					CorrectAnswer="Mount Rushmore";
 					break;
 		        case 13:
 					image.setImageResource(R.drawable.st_pauls);
 					CorrectAnswer="St Pauls";
 					CorrectAnswer="St Pauls Cathedral";
 					break;
 		        case 14:
 					image.setImageResource(R.drawable.white_house);
 					CorrectAnswer="White House";
 					CorrectAnswer="The White House";
 					break;
 		        case 15:
 					image.setImageResource(R.drawable.washington_monument);
 					CorrectAnswer="Washington Monument";
 					CorrectAnswer="The Washington Monument";
 					break;
 				default:
 					break;
 			}
 	    }
 	 
 	}
