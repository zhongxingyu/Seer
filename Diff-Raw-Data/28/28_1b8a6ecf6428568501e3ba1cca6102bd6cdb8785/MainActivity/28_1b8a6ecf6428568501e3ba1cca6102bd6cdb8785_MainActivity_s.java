 package edu.upenn.cis350.mosstalkwords;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutionException;
 import android.speech.tts.TextToSpeech;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.ByteArrayBuffer;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
     /** Called when the activity is first created. */
 	public final static String currentSavedScore = "edu.upenn.cis350.mosstalkwords.currentSavedScore";
 	private ImageView _imgView;
 	private String _currentPath;
 	private int _currentIndex;
 	private Button _hintPhraseButton;
 	private Button _hintRhymeButton;
 	private Button _hintPronounceButton;
 	private Button _micButton;
 	private Button _skipButton;
 	private MediaPlayer _mediaPlayer;
 
 
 	private boolean _listenerIsReady = false;
 	private TextToSpeech soundGenerator;
 	private TreeMap<String, String[]> hints; 
 	private int _rhymeUsed; 
 	public int _totalScore = 0;
 
 
 	public Scores _scores;
 	public int _setScore = 0;
 	public int _streak = 0;
 	public int _numHintsUsed = 0;
 	private int _numTries = 0;
 	private String _feedbackResult = "";
 	private ArrayList<String> _currentSet;
 
 	public AlertDialog ad;
 
 	public int _numCorrect = 0;
 
 	private String buildUrl(String extension) {
 		_currentSet = getIntent().getStringArrayListExtra("edu.upenn.cis350.mosstalkwords.currentSet");
 		return "https://s3.amazonaws.com/mosstalkdata/" + _currentPath + 
 				"/" + _currentSet.get(_currentIndex) + extension;
 
 	}
 	private String buildCachePath(String extension){
 		if(getApplicationContext() != null){
 			if(getApplicationContext().getCacheDir() != null){
 				if(getApplicationContext().getCacheDir().getPath() != null){
 					if(_currentSet == null){
 						return "curr set null!";
 					}
 						else
 						{
 							return getApplicationContext().getCacheDir().getPath()+"/"+_currentSet.get(_currentIndex)+extension; 
 						}
 
 				}
 				else{
 					return "path";
 				}
 			}
 			else{
 				return "get Cache Dir";
 			}
 		}
 		else{
 			return "App context";
 		}
 	}
 	
 	private class LoadFilesTask extends AsyncTask<String, Integer, Boolean>{
 		@Override
 		protected Boolean doInBackground(String... set) {
 			boolean b = false;
 			String [] extensions = {".jpg"};//, "_phrase.wav", "_rhyme.wav", ".wav"};
 			try {
 			 for(int j = 0; j< extensions.length; j++){
 				String extension = extensions[j];
 				if(_currentSet != null){
 				for (String word: _currentSet){
 					URL ur = new URL("https://s3.amazonaws.com/mosstalkdata/" + _currentPath + 
 				"/" +word + extension);
 					File file = new File(getApplicationContext().getCacheDir(),word+extension);
 					URLConnection ucon = ur.openConnection();
 					InputStream is = ucon.getInputStream();
 					BufferedInputStream bis = new BufferedInputStream(is);
 					ByteArrayBuffer baf = new ByteArrayBuffer(50);
 					int current = 0;
 					while ((current = bis.read()) != -1)
 						baf.append((byte) current);
 					FileOutputStream fos = new FileOutputStream(file);
 					fos.write(baf.toByteArray());
 					fos.close();
 					b = true;
 				}
 				}
 			} 
 			}catch (MalformedURLException e1) {
 				e1.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		 return b;
 		}
 
 	}
 
 	private class LoadHintsTask extends AsyncTask<String, Integer, Boolean>{
 
 		@Override
 		protected Boolean doInBackground(String... set) {
 			hints = new TreeMap<String, String[]>();
 			boolean b = false;
 			try {
 					URL ur = new URL("https://s3.amazonaws.com/mosstalkdata/" + _currentPath + 
 				"/" + "hints.txt");
 					//File file = new File(getApplicationContext().getCacheDir(),_currentSet[i]+extension);
 					BufferedReader hintReader = new BufferedReader(new InputStreamReader(ur.openStream()));
 					String lineRead;
 					int linenumber = 0;
 					String word = null;
 					String sentence = null;
 					String Rhyme1 = null;
 					String Rhyme2 = null;
 					while ((lineRead = hintReader.readLine()) != null){
 						switch(linenumber) {
 						case 0: word = lineRead; break;
 						case 1: sentence = lineRead; break;
 						case 2: Rhyme1 = lineRead; break;
 						case 3: Rhyme2 = lineRead;  break;
 						}
 						linenumber++;
 						if(lineRead.length()==0){ 
 							linenumber = 0;
 						}
 						if(linenumber == 4){
 							if(word != null && sentence != null && Rhyme1 != null && Rhyme2 != null){
 								String [] hts = {sentence, Rhyme1, Rhyme2};
 								hints.put(word, hts);
 							}
 						}	
 
 					} 
 			}catch (MalformedURLException e1) {
 				e1.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		 return b;
 		}
 
 	}
 	
 	private void loadImage() throws ClientProtocolException, IOException, InterruptedException, ExecutionException {	
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		 options.inSampleSize = 2;
 		 options.outHeight = (_imgView.getHeight())/2;
 		 options.outWidth= _imgView.getWidth();
 		Bitmap myBitmap= null;
 		 try{
 			 myBitmap = BitmapFactory.decodeFile(buildCachePath(".jpg"),options);
 		 }
 		 catch(Exception e){
 			 e.printStackTrace();
 		 }
 			
 		 if (myBitmap != null){
 		_imgView.setScaleType(ScaleType.FIT_XY);
 		 _imgView.setImageBitmap(myBitmap);
 		 }
 	}
 
 
 
 	private void playSound(String hint) {
 		try {
 			if (_mediaPlayer.isPlaying()) {
 				_mediaPlayer.stop();
 			}
 			
 			_mediaPlayer.reset();
 			//_mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 			_mediaPlayer.setDataSource("https://s3.amazonaws.com/mosstalkdata/" + _currentPath + 
 					"/" + _currentSet.get(_currentIndex) + hint + ".wav");
 			//_mediaPlayer.setDataSource("https://s3.amazonaws.com/mosstalkdata/nonlivingthingseasy/banana.wav");
 			_mediaPlayer.prepare();
 			_mediaPlayer.start();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void playSoundText(String hint){
 		if (_listenerIsReady == false){
 			Toast.makeText(this, "Hold on! I'm not ready yet! Try again in a second!", Toast.LENGTH_LONG).show();
 		}
 		else {
 			String text = "";
 			Log.i("info", _currentSet.get(_currentIndex));
 			Log.i("info", Arrays.toString(hints.get(_currentSet.get(_currentIndex))));
 			String[] hintarray = hints.get(_currentSet.get(_currentIndex));
 			if (hintarray != null && hintarray.length > 1){
 				if (hint.equals("word")){
 					text = _currentSet.get(_currentIndex);
 				}
 				if (hint.equals("phrase")){
 					text = hintarray[0];
 				}
 				if (hint.equals("rhyme")){
 					text = hintarray[_rhymeUsed+1];
 					if(_rhymeUsed == (hintarray.length-1)){
 						_rhymeUsed = 0;
 					}
 					else{
 						_rhymeUsed++;
 					}
 				}
 			}
 			Toast.makeText(this, text, Toast.LENGTH_LONG);
 			soundGenerator.speak(text, TextToSpeech.QUEUE_ADD, null);
 		}
 	}
 
 	private class TextToSpeechListener implements TextToSpeech.OnInitListener{
 
 		@Override
 		public void onInit(int arg0) {
 			_listenerIsReady = true;
 
 		}
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         _imgView = (ImageView) findViewById(R.id.image);
         _currentIndex = 0;
         _currentPath = getIntent().getStringExtra("edu.upenn.cis350.mosstalkwords.currentSetPath");
         _currentSet = getIntent().getStringArrayListExtra("edu.upenn.cis350.mosstalkwords.currentSet");
         
         _scores = new Scores(this.getApplicationContext());
         
         _totalScore = _scores.getTotalScore();
         
         _setScore = 0;
         _streak = 0;
         
         TextView st = (TextView) findViewById(R.id.score);
     	st.setText(Integer.toString(_totalScore));
     	
         AsyncTask<String, Integer, Boolean> downloadFiles = new LoadFilesTask().execute("");
         AsyncTask<String, Integer, Boolean> downloadHints = new LoadHintsTask().execute("");
         soundGenerator = new TextToSpeech(this, new TextToSpeechListener());
     	try {
 			loadImage();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
        
         _hintPhraseButton = (Button) findViewById(R.id.hintbuttona);
         _hintRhymeButton = (Button) findViewById(R.id.hintbuttonb);
         _hintPronounceButton = (Button) findViewById(R.id.hintbuttonc);
         _micButton = (Button) findViewById(R.id.micbutton);
         _skipButton = (Button) findViewById(R.id.skipbutton);
         
         _mediaPlayer = new MediaPlayer();
         _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
         
         _hintPhraseButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				playSoundText("phrase");
 				if(_numHintsUsed < 3)
 					_numHintsUsed++;	
 			}
 		});
         
         _hintRhymeButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				playSoundText("rhyme");
 				if(_numHintsUsed < 3)
 					_numHintsUsed++;
 			}
 		});
         
         _hintPronounceButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				playSoundText("word");
 				if(_numHintsUsed < 3)
 					_numHintsUsed++;
 			}
 		});
         
         _micButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
 		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
 
 		        try {
 		            startActivityForResult(intent, 1);
 		        } catch (ActivityNotFoundException a) {
 		        	a.printStackTrace();
 		            Toast.makeText(getApplicationContext(),
 				                   "Oops! Your device doesn't support Speech to Text",
 				                   Toast.LENGTH_SHORT).show();
 		        }
 			}
 		});
         
        
         
         _skipButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if(_scores.getHighestStreak() < _streak)
 					_scores.setHighestStreak(_streak);
 				
 				_streak = 0;
 				nextImage();
 			}
 		});
         
     }
     
     
     public void nextImage(){
     	_currentIndex++;
 		_rhymeUsed = 0;
 		if(checkEndOfSet() == true){
 			return;
 		}
 		else{
 		try {
 			loadImage();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		_numHintsUsed = 0;
 		_numTries = 0;
 		}
     }
     
     private boolean checkEndOfSet(){
     	boolean end = false;
     	if(_currentIndex >= _currentSet.size()){
     		end = true;
     		
    		//check if current set score is > high score, if so update
//    		if(_setScore > _scores.getHighScore(_currentPath)) {
//    			_scores.setHighScore(_currentPath, _setScore);
//    		}
//    		
//    		//check highest streak compared to current streak
//    		if(_scores.getHighestStreak() < _streak)
//				_scores.setHighestStreak(_streak);
//
////    		_scores.incTotalScore(_setScore); //increment total score by this set's score
//  		  	
//    		int prevNumOfCorrectAnswers = _scores.getNumCompleted(_currentPath);
//    		if(_numCorrect > prevNumOfCorrectAnswers)
//    		{
//    			_scores.setNumCompleted(_currentPath, _numCorrect);
//    		}
//    		
//  		    _scores.setTotalScore(_totalScore);
//  		    _scores.closeDb();
   		    finish();
     	}
     	return end;
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
         case 1: {
             if (resultCode == RESULT_OK && data != null) {
                 ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                 String correctAnswer = _currentSet.get(_currentIndex);
                 
                 for(String str: result)
                 {
                 	if(str.equals(correctAnswer) || str.contains(correctAnswer))
                 	{
                 		giveFeedback(true, str);
                 		return;
                 	}
                 }
                 giveFeedback(false, result.get(0));
             }
             break;
         }
         }
     }
     
     /**
 	 * Method that displays a dialog showing the user whether they said the right 
 	 * answer or not, and giving them the option of continuing or trying again.
 	 * @param isSuccess  whether or not the user said the correct word
 	 * @param word_said  the word that the user said
 	 */
 	private void giveFeedback(boolean isSuccess, String word_said) {
 
 		//build the dialog
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 
 		b.setCancelable(false);
 
 
 		if(isSuccess) {  //only give them continue button if they got it right
 			
 			_numCorrect++;
 			
 			b.setTitle("Correct!");
 			b.setIcon(R.drawable.checkmark);
 			b.setMessage("You said: " + word_said);
 
 			b.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					_feedbackResult="continue";
 					_setScore += 3-_numHintsUsed;
 					_totalScore += 3-_numHintsUsed;
 
 					_streak++;
 		        	TextView st = (TextView) findViewById(R.id.score);
 
 		        	st.setText(Integer.toString(_totalScore));
 		        	nextImage();
 				}
 			});
 
 		}
 		else if(isSuccess == false && _numTries >= 2) {  //got it wrong, but time to move on
 			b.setTitle("Try the next picture!");
 			b.setIcon(R.drawable.wrong);
 			b.setMessage("The correct answer was: " + _currentSet.get(_currentIndex));
 			
 			b.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					_feedbackResult="continue";
 
 					//check if streak that just ended was the highest
 					if(_scores.getHighestStreak() < _streak)
 						_scores.setHighestStreak(_streak);
 					
 					//check if streak that just ended was the highest
 					if(_scores.getHighestStreak() < _streak)
 						_scores.setHighestStreak(_streak);
 					
 					_streak = 0;
 					nextImage();
 				}
 			});
 		}
 		else {
 			b.setTitle("Not quite!");
 			b.setIcon(R.drawable.wrong);
 			b.setMessage("You said: " + word_said);
 
 			b.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					_feedbackResult="again";
 					_numTries++;
 
 					//check if streak that just ended was the highest
 					if(_scores.getHighestStreak() < _streak)
 						_scores.setHighestStreak(_streak);
 					_streak = 0;
 				}
 			});
 		}
 
 		ad = b.create();
 		ad.show();  //show the dialog
 
 		
 		//play the audio feedback
 		if(isSuccess) {
 			MediaPlayer mp = MediaPlayer.create(this, R.raw.correct);
 			mp.start();
 		}
 
 	}
 	
 	 public static void trimCache(Context context) {
 	      try {
 	         File dir = context.getCacheDir();
 	         if (dir != null && dir.isDirectory()) {
 	            deleteDir(dir);
 	         }
 	      } catch (Exception e) {
 	         // TODO: handle exception
 	      }
 	   }
 
 	   public static boolean deleteDir(File dir) {
 	      if (dir != null && dir.isDirectory()) {
 	         String[] children = dir.list();
 	         for (int i = 0; i < children.length; i++) {
 	            boolean success = deleteDir(new File(dir, children[i]));
 	            if (!success) {
 	               return false;
 	            }
 	         }
 	      }
 
 	      // The directory is now empty so delete it
 	      return dir.delete();
 	   }
 
 	   @Override
 	   protected void onDestroy() {
 		  _currentIndex = 0;
 		  if(soundGenerator != null){
 			  _listenerIsReady = false;
 			  soundGenerator.stop();
 			  soundGenerator.shutdown();
 			  soundGenerator = null;
 		  }
 		  
   		  if(_setScore > _scores.getHighScore(_currentPath)) {
 			_scores.setHighScore(_currentPath, _setScore);
 		  }
 		
 		  //check highest streak compared to current streak
 		  if(_scores.getHighestStreak() < _streak)
 			_scores.setHighestStreak(_streak);
 		  	
 		  int prevNumOfCorrectAnswers = _scores.getNumCompleted(_currentPath);
 		  if(_numCorrect > prevNumOfCorrectAnswers)
 		  {
 			_scores.setNumCompleted(_currentPath, _numCorrect);
 		  }
 		  
 		  _scores.setTotalScore(_totalScore);
 
 		  _scores.closeDb();
 	      super.onDestroy();
 	      
 	      try {
 	         trimCache(this);
 	      } catch (Exception e) {
 	         // TODO Auto-generated catch block
 	         e.printStackTrace();
 	      }
 	   }
 
 	   @Override
 	    protected void onPause() {
 	        super.onPause();
 	        if(soundGenerator != null){
 	        soundGenerator.stop();
 	        soundGenerator.shutdown();
 	        _listenerIsReady = false;
 	        soundGenerator = null;
 	        }
 	    }
 
 	    @Override
 	    protected void onResume() {
 	        super.onResume();
 	       // soundGenerator = new TextToSpeech(this, new TextToSpeechListener());
 	    }
 
 }
