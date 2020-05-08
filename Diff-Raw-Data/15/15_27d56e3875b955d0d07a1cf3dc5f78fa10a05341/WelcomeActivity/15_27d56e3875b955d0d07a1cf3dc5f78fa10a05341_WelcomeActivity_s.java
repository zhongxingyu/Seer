 package edu.upenn.cis350.voice;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.upenn.cis350.voice.db.DBManager;
 import edu.upenn.cis350.voice.db.VoiceCallback;
 
 import android.app.Activity;
 import com.parse.ParseException;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.content.Intent;
 import android.database.sqlite.SQLiteException;
 
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 public class WelcomeActivity extends Activity {
 
 	private DBManager _db;
 
 	private Timer synchTimer = new Timer();
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Parse.initialize(this, "pPpqQgvXPcLhyGUNTD7ktBhXEsMWVybkyq89kamw",
 		"5GMEKejNwtcqJBSO6G4gcvjbD2mC6dgMi3XdgnQY");
 		try{
 			_db = new DBManager(this);
 		} catch(SQLiteException e){}
 		setContentView(R.layout.welcome);
 		
 		//Sync with parse every 5 minutes
		long interval = 60*5*1000;
 		synchTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				AutoSync();
 			}
 		}, 0,interval);
 	}
 	
 	private void AutoSync()
 	{
 		this.runOnUiThread(new Runnable() {
 			public void run() {
 				synchronizeQuestions();
 				synchronizeAnswers();
 			}
 		});
 	}
 
 	
 	public ArrayList<Integer> parseAnswer(String ans){
 		Scanner scan = new Scanner(ans);
 		scan.useDelimiter(","); //For CSV string
 		ArrayList<Integer> answers = new ArrayList<Integer>();
 		while(scan.hasNext())
 			answers.add(Integer.parseInt(scan.next()));
 		return answers;
 	}
 	/**
 	 * Synchronize with the Parse database
 	 */
 	public void synchronizeAnswers(){
 		try{
 			_db.open();
 			ParseObject syncAnswer;
 			ArrayList<String> cachedAnswers = _db.getAllAnswers();
 			for(String ans : cachedAnswers){
 				syncAnswer = new ParseObject("Answers");
 				ArrayList<Integer> ansList = parseAnswer(ans);
 				for(int i = 0; i < ansList.size(); ++i)
 					syncAnswer.put("Answer_" + (i+1), ansList.get(i));
 				syncAnswer.saveInBackground();
 			}
 			_db.deleteAllAnswers();
 			_db.close();
 		}catch(SQLiteException e){}
 	}
 
 	public Question constructQuestion(ParseObject p){
 		String text = p.getString("Text");
 		if(text==null)
 			return null;
 		int num = p.getInt("Number");
 		if(num==0)
 			return null;
 		Type t = null;
 		String type = p.getString("Type");
 		if(type==null)
 			return null;
 		if(type.equalsIgnoreCase("button"))
 			t = Type.BUTTON;
 		else if(type.equalsIgnoreCase("wheel"))
 			t = Type.WHEEL;
 		else if(type.equalsIgnoreCase("drag"))
 			t = Type.DRAG;
 		else if(type.equalsIgnoreCase("slider"))
 			t = Type.SLIDER;
 		else if(type.equalsIgnoreCase("picture"))
 			t = Type.PICTURE;
 		else
 			return null;
 		return new Question(num, text, t);
 	}
 
 	public void synchronizeQuestions(){
 		ParseQuery query = new ParseQuery("Question");
 		VoiceCallback callback = new VoiceCallback(this);
 		query.findInBackground(callback);
 		
 	}
 
 	public void onSynchronizeClick(View view){
 		synchronizeQuestions();
 		synchronizeAnswers();
 	}
 	
 	public void updateQuestions(List<ParseObject> list){
 		ArrayList<Question> qlist = new ArrayList<Question>();
 		Iterator<ParseObject> iter = list.iterator();
 		while(iter.hasNext()){
 			Question q = constructQuestion(iter.next());
 			if(q!=null)
 				qlist.add(q);
 		}
 		try{	
 			_db.open();
 			_db.deleteAllQuestions();
 			for(Question q : qlist)
 				_db.insertQuestion(q);
 			_db.close();
 		}catch(SQLiteException e){
 			e.printStackTrace();
 		}
 	}
 
 	public void onContinueClick(View view){
 		//Create and intent using the new activity and the class to be created (A quiz activity)
 		Intent i = new Intent(WelcomeActivity.this, QuestionActivity.class);
 
 		startActivity(i);
 	}
 
 }
