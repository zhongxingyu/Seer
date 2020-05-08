 package org.techbridgeworld.bwt.student;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.speech.tts.UtteranceProgressListener;
 import android.util.Log;
 
 public class MyApplication extends Application implements OnInitListener {
 
 	public TextToSpeech myTTS;
 	HashMap<String, String> params; 
 	
 	public MediaPlayer myPlayer; 
 	
 	public Context context; 
 	public String dir;
 	public int currentFile;
 	public ArrayList<String> filenames;
 	
 	public String prompt, help; 
 	
 	@Override
 	public void onCreate () {
 		myTTS = new TextToSpeech(this, this); 
 		params = new HashMap<String, String>();
 		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");
 		
 		myPlayer = new MediaPlayer();
 		
 		currentFile = 0;
 		filenames = new ArrayList<String>();
 
 		try {
 			context = createPackageContext("org.techbridgeworld.bwt.teacher", 0);
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		dir = context.getFilesDir().getPath().toString();
 	}
 	
 	@Override
 	public void onInit(int status) {
 		if (status == TextToSpeech.SUCCESS) {
             myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener()
             {
                 @Override
                 public void onDone(String utteranceId)
                 {
                 	if(utteranceId.equals("utteranceId")) {
                 		myTTS.stop();
     					if(currentFile < filenames.size() - 1) {
     						currentFile++;
     						playAudio(filenames.get(currentFile));
     					}
     					else {
     						filenames.clear();
     						currentFile = 0;
     					}
                 	}
                 }
 
                 @Override
                 public void onError(String utteranceId) {}
 
                 @Override
                 public void onStart(String utteranceId) {}
             });
 			int result = myTTS.setLanguage(Locale.US);
 			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
 				Log.e("TTS", "This language is not supported");
 			
 			if(prompt != null)
 				speakOut(prompt); 
 		}
 		else
 			Log.e("TTS", "Initilization Failed!");
 	}
 	
 	/**
 	 * Use TextToSpeech to speak some text out loud
 	 * @param text (the text)
 	 */
 	public void speakOut(String text) {
 		myTTS.speak(text, TextToSpeech.QUEUE_ADD, params);
 	}
 	
 	/**
 	 * Adds the resourceId to filenames
 	 * @param resourceId (i.e. R.string.___)
 	 */
 	public void queueAudio(int resourceId) {
 		filenames.add(getResources().getString(resourceId));
 		Log.i("neha", "added " + getResources().getString(resourceId) + " to queue.");
 	}
 	
 	/**
 	 * Adds the string to filenames
 	 * @param str (the string)
 	 */
 	public void queueAudio(String str) {
 		filenames.add(str);
 		Log.i("neha", "added " + str + " to queue.");
 	}
 	
 	/**
 	 * Clears everything in the audio queue
 	 */
 	public void clearAudio() {
 		myTTS.stop();
 		if (myPlayer.isPlaying()) {
 			myPlayer.stop();
 		}
 		filenames.clear();
 		currentFile = 0;
 	}
 	
 	/**
 	 * Begin playing the audio files in filenames
 	 */
 	public void playAudio() {
		if(filenames.isEmpty()) return;
 		playAudio(filenames.get(0));
 	}
 	
 	/**
 	 * Play a certain audio file
 	 * @param filename (the audio file)
 	 */
 	public void playAudio(String filename) {
 		FileInputStream fis;
 		Log.i("neha", "playing " + filename + ".");
 		try {
 			filename = filename.replaceAll(" ", "_");
 			fis = new FileInputStream(dir + "/" + filename + ".m4a");
 			Log.i("Audio", "Could find file " + filename + ".m4a.");
 			myPlayer.reset();
 			myPlayer.setDataSource(fis.getFD());
 			fis.close();
 			myPlayer.prepare();
 			myPlayer.start();
 			myPlayer.setOnCompletionListener(new OnCompletionListener() {
 				@Override
 				public void onCompletion(MediaPlayer mp) {
 					myPlayer.stop(); 
 					if(currentFile < filenames.size() - 1) {
 						currentFile++;
 						playAudio(filenames.get(currentFile));
 					}
 					else {
 						filenames.clear();
 						currentFile = 0;
 					}
 				}
 			});
 		} catch (FileNotFoundException e) {
 			Log.w("Audio", "Could not find file " + filename + ".m4a."); 
 			filename = filename.replaceAll("_", " ");
 			speakOut(filename);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
