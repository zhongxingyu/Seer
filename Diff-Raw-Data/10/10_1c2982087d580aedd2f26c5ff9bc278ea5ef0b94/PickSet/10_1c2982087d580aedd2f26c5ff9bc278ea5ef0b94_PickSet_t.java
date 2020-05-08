 package edu.upenn.cis350.mosstalkwords;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import org.apache.http.util.ByteArrayBuffer;
 
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class PickSet extends Activity {
 	public final static String currentSetPath = "edu.upenn.cis350.mosstalkwords.currentSetPath";
 	public final static String currentSet = "edu.upenn.cis350.mosstalkwords.currentSet";
 	private ArrayList<String> categories;
 	private ListView lv;
 	private String difficulty;
 	private String category;
 	private Scores scores;
 	private TextView highscore;
 	
 	private AsyncTask<String, Integer, Boolean> downloadCatsWords;
 	private TreeMap<String, ArrayList<String>> catToWords;
 	private TreeMap<String, Integer> catToSizeOfCat = new TreeMap<String, Integer>();
 	private TreeMap<String, Integer> catToNumWordCompleted = new TreeMap<String, Integer>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_pick);	
 		scores = new Scores(getApplicationContext());
 		//initialize category and difficulty
 		category = "livingthings";
 		difficulty = "easy";
 		downloadCatsWords = new LoadCategoriesWords().execute("");
 	}
 	
 	@Override 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
 	  super.onActivityResult(requestCode, resultCode, data); 
 	  updateListViewInfo();
 	}
 	
 	@Override 
 	public void onRestart() {     
 	  super.onRestart(); 
 	  updateListViewInfo();
 	}
 		 
 	 private ArrayList<Set> getSetList(){
 		 String [] cats = getResources().getStringArray(R.array.stimulus_array);
 		 String [] diffs = getResources().getStringArray(R.array.difficulty_array);
 		 ArrayList<Set> setlist = new ArrayList<Set>();
 		 for (String cat : cats){
 			 for (String diff : diffs){
 				 String category = cat+diff;
 				 category = category.replaceAll("\\s","");
 				 category = category.toLowerCase();
 				 String score = getPercentageOfCategoryCompleted(category);
 				 Log.i("info", score);
 				 setlist.add(new Set(score, cat, diff));
 			 }
 		 }
 		 return setlist;
 	 }
 	 public void setListViewInfo(){
 		ArrayList<Set> sets = getSetList();
 		SetAdapter adapter= new SetAdapter(this,R.layout.listview_item, sets); 
 		lv = (ListView)findViewById(R.id.listView1);
 		View header = (View)getLayoutInflater().inflate(R.layout.listview_header, null);
 		lv.addHeaderView(header);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(new SetSelectedListener());      
 	}
 	 
 	 public void updateListViewInfo(){
 		 ArrayList<Set> newSetList = getSetList();
 		 lv = (ListView)findViewById(R.id.listView1);
 		 HeaderViewListAdapter hlva = (HeaderViewListAdapter) lv.getAdapter();
 		 ArrayAdapter<Set> adap = (ArrayAdapter<Set>) hlva.getWrappedAdapter();
 		 if(adap != null){
 			 adap.clear();
 			 for(Set set : newSetList){
 				 adap.add(set);
 			 }
 		 }   
 	 }
 	    
 	
 	//Method that returns the percentage of the category that's been completed (eg. 8/10, 0/10 etc.)
 	//Fiona should use this method to display either this string or a star if the percentage is 10/10 or 9/9
 	//Also, when the user plays a set and the catToNumWordCompleted changes you should call
 	//catToNumWordCompleted.put(category, newValueOfNumCompleted) to update the mapping and then call this
 	//method again to get the updated string for PercentageOfCategoryCompleted
 	public String getPercentageOfCategoryCompleted(String category)
 	{
		int numCompleted = scores.getNumCompleted(category);
		catToNumWordCompleted.put(category, numCompleted);
 		return catToNumWordCompleted.get(category) + "/" + catToSizeOfCat.get(category);
 	}
 
 	public void start(View view){
 		Intent i = new Intent(this, MainActivity.class);
 		i.putExtra(currentSetPath, category+difficulty);
 		i.putStringArrayListExtra(currentSet, getSet(category+difficulty));
 		//Log.i("info", category+difficulty);
 		//Log.i("info", catToWords.keySet().toString());
 		startActivityForResult(i,1);
 	}
 
 	public ArrayList<String> getSet(String key){
 		return catToWords.get(key);
 	}
 	
 	public class SetSelectedListener implements OnItemClickListener {
 
 		@SuppressLint("DefaultLocale")
 		public void onItemClick(AdapterView<?> parent, View view, int pos,long id) {
 			Set chosen = (Set)parent.getItemAtPosition(pos);
 			category = chosen.category;
 			category = category.replaceAll("\\s","");
 			category = category.toLowerCase();
 			difficulty = chosen.difficulty;
 			difficulty = difficulty.toLowerCase();
 			start(view);
 		}
 	}
 	
 	private class LoadCategoriesWords extends AsyncTask<String, Integer, Boolean>{
 		@Override
 		protected Boolean doInBackground(String... set) {
 			categories = new ArrayList<String>();
 			catToWords = new TreeMap<String, ArrayList<String>>();
 			boolean b = false;
 			try {
 					URL ur = new URL("https://s3.amazonaws.com/mosswords/categories.txt");
 					BufferedReader categoryReader = new BufferedReader(new InputStreamReader(ur.openStream()));
 					String lineRead;
 					while ((lineRead = categoryReader.readLine()) != null){
 						b = true;
 						categories.add(lineRead);
 					}
 					categoryReader.close();
 					for (String cat: categories){
 						try{
 						URL urwords = new URL("https://s3.amazonaws.com/mosswords/" + cat + "/words.txt");
 						BufferedReader wordsReader = new BufferedReader(new InputStreamReader(urwords.openStream()));
 						String word;
 						ArrayList<String> wordslist = new ArrayList<String>();
 						int count = 0;
 						while ((word = wordsReader.readLine()) != null){
 							wordslist.add(word);
 							count++;
 						}
 						catToWords.put(cat, wordslist);
 						catToSizeOfCat.put(cat, count);
 						
 						int numCompleted = scores.getNumCompleted(cat);
 						catToNumWordCompleted.put(cat, numCompleted);
 						
 						wordsReader.close();
 						}
 						catch(FileNotFoundException e){
 							e.printStackTrace();
 						}
 					}
 			}catch (MalformedURLException e1) {
 				e1.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		 return b;
 		}
 		
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			setListViewInfo();
 			AsyncTask<String, Integer, Boolean> downloadFirstFiles = new LoadOneFile().execute("");
 		}
 
 	}
 	
 	private class LoadOneFile extends AsyncTask<String, Integer, Boolean>{
 		@Override
 		protected Boolean doInBackground(String... set) {
 			boolean b = false;
 			Log.i("info", "LoadOneFile called");
 			try {
 				for (String cat: categories ){
 					
 					if(getSet(cat) != null){
 					URL ur = new URL("https://s3.amazonaws.com/mosswords/" + cat + 
 							"/" + getSet(cat).get(0) + ".jpg");
 					Log.i("info", ur.toString());
 					File file = new File(getApplicationContext().getCacheDir(),  getSet(cat).get(0) +".jpg");
 					Log.i("info", file.getAbsolutePath());	
 					if (file.exists() == false){
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
 					
 					else{
 						Log.i("info", file.getAbsolutePath() + "  exists!");
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
 	
 	
 	public AsyncTask.Status getDownloadCatsStatus() {
 		return downloadCatsWords.getStatus();
 	}
 	
 	
 	
 }
