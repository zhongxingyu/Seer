 package org.techbridgeworld.bwt.student;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.AsyncTask;
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
 	
 	public ArrayList<String> hangmanWords;
 	
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
 		
 		new HTTPAsyncTask().execute();		
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
 	}
 	
 	/**
 	 * Adds the string to filenames
 	 * @param str (the string)
 	 */
 	public void queueAudio(String str) {
 		filenames.add(str);
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
 	
 	public class HTTPAsyncTask extends AsyncTask<Void, Integer, Void> {
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Creating HTTP client
 			HttpClient httpClient = new DefaultHttpClient();
 			// Creating HTTP Post
			HttpPost httpPost = new HttpPost("http://128.237.204.207:3000/login");
 
 			// Building post parameters
 			// key and value pair
 			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
 			nameValuePair.add(new BasicNameValuePair("username", "admin"));
 			nameValuePair.add(new BasicNameValuePair("password", "admin"));
 
 			// Url Encoding the POST parameters
 			try {
 				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
 			} catch (UnsupportedEncodingException e) {
 				// writing error to Log
 				e.printStackTrace();
 			}
 
 			// Making HTTP Request
 			try {
 				HttpResponse response = httpClient.execute(httpPost);
 				// writing response to log
 				Log.d("HTTP", response.toString());
 			} catch (ClientProtocolException e) {
 				// writing exception to log
 				e.printStackTrace();
 				Log.d("HTTP", "client protocol exception");
 			} catch (IOException e) {
 				// writing exception to log
 				e.printStackTrace();
 				Log.d("HTTP", "io exception");
 			}
 			
 			HttpResponse response = null;
 			hangmanWords = new ArrayList<String>();
 			try {        
 		        HttpClient client = new DefaultHttpClient();
 		        HttpGet request = new HttpGet();
		        request.setURI(new URI("http://128.237.204.207:3000/words"));
 		        response = client.execute(request);
 		        Log.d("HTTP",  response.toString());
 		        responseStreamToJSON(response.getEntity().getContent());
 		        
 				Log.i("HTTP", "Hangman words arraylist: " + hangmanWords);
 		    } catch (URISyntaxException e) {
 		        e.printStackTrace();
 		        Log.d("HTTP", "urisyntax");
 		    } catch (ClientProtocolException e) {
 		        // TODO Auto-generated catch block
 		        e.printStackTrace();
 		        Log.d("HTTP", "client protocol");
 		    } catch (IOException e) {
 		        // TODO Auto-generated catch block
 		        e.printStackTrace();
 		        Log.d("HTTP", "io exception");
 		    }
 			return null;
 		}
 	}
 	
 	private void responseStreamToJSON(InputStream responseStream) {
 		String str = "";
 		if(responseStream != null) {
 			Writer writer = new StringWriter();
 			char[] buffer = new char[1024];
 			try {
 				Reader reader = new BufferedReader(new InputStreamReader(
 						responseStream, "UTF-8"), 1024);
 				int n;
 				while( (n = reader.read(buffer)) != -1) {
 					writer.write(buffer, 0, n);
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				Log.e("HTTP", "responseStream for hangman - Reader IO Exception");
 			} finally {
 				try {
 					responseStream.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					Log.e("HTTP", "responseStream for hangman couldn't close");
 					e.printStackTrace();
 				}
 			}
 			str = writer.toString();
 			Log.i("HTTP", "responseStream as a string: " + str);
 		}
 
 		//Create JSONArray out of what came from the inputStream in response
 		JSONArray hangmanJSON = null;
 		try {
 			hangmanJSON = new JSONArray(str);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			Log.e("HTTP", "responseStream - JSONExceptions");
 			e.printStackTrace();
 		}
 		
 		//Convert JSONArray into an ArrayList of Strings (hangmanWords)
 		if(hangmanJSON != null) {
 			for(int i = 0; i < hangmanJSON.length(); i++) {
 				JSONObject row;
 				try {
 					row = hangmanJSON.getJSONObject(i);
 					hangmanWords.add(row.getString("word"));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					Log.e("HTTP", "responseStream (converting to array) - JSONExceptions");
 					e.printStackTrace();
 				}
 				
 			}
 		}
 	}
 
 }
