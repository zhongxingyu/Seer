 package team18.cs.ncl.ac.uk;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Random;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.FacebookError;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 
 
 public class FlashCards extends Activity{
 	public ImageView x;
 	public  AsyncFacebookRunner mAsyncRunner=new AsyncFacebookRunner(FbRelatedStuff.facebook);
 	String rightAnswer; //Will hold the correct string answer
 	
 	//Takes the string of the button and compares it to the correct answer
 	private OnClickListener guessListener = new OnClickListener() {
 	    @Override
 		public void onClick(View v) {
 			Button guessButton = (Button) v;
 			String s = guessButton.getText().toString();
 			correctGuess(s);
 	    }
 	};
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     	//Random number generator, used to pick a button for the correct answer 
         Random r = new Random();
         int ranNum = r.nextInt(3); //Will generate a random number between 0-2
        
        x=(ImageView)findViewById(R.id.flashcardMainImageView);
        DictionaryTools t = new DictionaryTools();
        WordPair p = t.getRandom();
        
       String urlOfWString = FlashCardTools.getImageUrlForWord(p.Word);
       getImage(urlOfWString);
       
       setContentView(R.layout.flashcards);
       
       rightAnswer = p.Word;
       //Sets up 2 more random wordPairs from server
       WordPair p2 =t.getRandom();
       WordPair p3 =t.getRandom();
       
       Button button1=(Button)findViewById(R.id.button1);
       Button button2=(Button)findViewById(R.id.button2);
       Button button3=(Button)findViewById(R.id.button3);
      
       //Assigns the words to a button each depending on the random Num generated
       if(ranNum==0){
     	  button1.setText(p.Word);
     	  button2.setText(p2.Word);
     	  button3.setText(p3.Word);
       }else if(ranNum==1){
     	  button1.setText(p2.Word);
     	  button2.setText(p.Word);
     	  button3.setText(p3.Word);
       }else{
     	  button1.setText(p2.Word);
     	  button2.setText(p3.Word);
     	  button3.setText(p.Word);
       }
       
       //Each button uses the same Listener
       button1.setOnClickListener(guessListener);
       button2.setOnClickListener(guessListener);
       button3.setOnClickListener(guessListener);
     }
     void getImage(final String S)
     {
     	FlashCards.this.runOnUiThread(new Runnable() {
 		public void run() {
 		    new GetFlashCardPicAsyncTask().execute(S);
 		}
 	    });
 
 	
     }
 	 private class GetFlashCardPicAsyncTask extends AsyncTask<Object, Void, Bitmap>  
 	    {
 		String picturePath;
 		
 	        @Override
 		    protected Bitmap doInBackground(Object... params) {
 		    picturePath =  params[0].toString();
 		    return  Utility.getBitmap(picturePath);
 		    
 	    }
 		
 	        @Override
 		    protected void onPostExecute(final Bitmap result) {
 	            
 		    FlashCards.this.runOnUiThread(new Runnable() {
 	        	    public void run() {
 			if(x != null)
 			x.setImageBitmap(result);
 			else
 			{
 				 x=(ImageView)findViewById(R.id.flashcardMainImageView);
 					x.setImageBitmap(result);
 						 
 			}
 	        	    }
 			});
 	        }
 	    }
 	 
 	 /*
 	  * Checks the string of the button you chose against the correct answer,
 	  * updating the server if you were correct or incorrect
 	  */
 	 public void correctGuess(String s){
 		 	if(rightAnswer==s){
 		 		try {
 					DogBoneServer.sendJson(FbRelatedStuff.uid,1,rightAnswer, 1);
 					  Bundle params = new Bundle();
 					    params.putString("method", "post");
 					    params.putString("query", "/me/newcastleproject:play");
 					    mAsyncRunner.request(params, new RequestListener() {
 							
 							@Override
 							public void onMalformedURLException(MalformedURLException e, Object state) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void onIOException(IOException e, Object state) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void onFileNotFoundException(FileNotFoundException e, Object state) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void onFacebookError(FacebookError e, Object state) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void onComplete(String response, Object state) {
 								System.out.print(response);						
 							}
 						});
 					    displayEndGame(getString(R.string.WonTxt));
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		 	}else{
 		 		try {
 					DogBoneServer.sendJson(FbRelatedStuff.uid,1, s, -1);
 					displayEndGame(getString(R.string.LostTxt));
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		 	}
 	 }
 	
 	 /*
 	  * Displays the standard win or lose message then shows a dialogue box asking to direct 
 	  * the user back to the main menu or try again at the flash card game.
 	  */
 	private void displayEndGame(String Text) {
 	        
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    builder.setMessage(Text)
 	        .setCancelable(false)
 	        .setPositiveButton("Main Menu", new DialogInterface.OnClickListener() {
 	    	    public void onClick(DialogInterface dialog, int id)
 	    	    {	 
 	        	FlashCards.this.finish();
 	    			Intent intent =
 	    		    new Intent(team18.cs.ncl.ac.uk.FlashCards.this, team18.cs.ncl.ac.uk.DogboneIslandActivity.class);
 	    			startActivity(intent);			
 	    		    }
 	    		})
 	        .setNegativeButton("Try Again", new DialogInterface.OnClickListener()
	    	{ 4
 	    	    public void onClick(DialogInterface dialog, int id)
 	    	    {
 	    		FlashCards.this.finish();
 	    		Intent intent =
 	    		    new Intent(team18.cs.ncl.ac.uk.FlashCards.this, team18.cs.ncl.ac.uk.FlashCards.class);
 	    		startActivity(intent);				
 	    		    }
 	    		});
 	    	AlertDialog alert = builder.create();
 	    	alert.show();
 	    }
 }
