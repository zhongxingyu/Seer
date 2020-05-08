 package edu.upenn.cis350.voice;
 
 import java.util.ArrayList;
 
 import edu.upenn.cis350.voice.db.DBManager;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteException;
 import android.os.Bundle;
 import android.util.Log;
 
 public class QuestionActivity extends Activity {
 
 	public static final int ACTIVITY_NewQuestion = 1;
 	public static final int ACTIVITY_Confirmation = 2;
 	public static final int ACTIVITY_End = 3;
 	private DBManager _dataManager;
 
 	private int _totalScore;
 
 	private ArrayList<Question> _questionList;
 	private Question _currentQuestion;
 	private int _numQuestion;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		_totalScore = 0; 
 		_dataManager = new DBManager(this);
 		
 		try{
 			_dataManager.open();
 			_questionList = _dataManager.getAllQuestions();
 			_dataManager.close();
 		} catch(SQLiteException e){
 			e.printStackTrace();
 		}
		
 		_numQuestion = -1;
 		switchQuestion(true);
 	}	


 	public void switchQuestion(boolean next){
 		if(next){
 			++_numQuestion;
 			if(_numQuestion >= _questionList.size()){
 				Intent i = new Intent(this, ConfirmationActivity.class);
 				startActivityForResult(i, QuestionActivity.ACTIVITY_Confirmation);
 				return;
 			}
 		}
 		else{
 			--_numQuestion;
 			if(_numQuestion < 0)
 				_numQuestion = 0;
 		}
 		_currentQuestion = _questionList.get(_numQuestion);
 		Intent i = new Intent(this, DisplayQuestion.class);
 		i.putExtra("Type", _currentQuestion.getType());
 		i.putExtra("Text", _currentQuestion.getText());
 		i.putExtra("Answer", new Integer(_currentQuestion.getAnswer()));
 		startActivityForResult(i, QuestionActivity.ACTIVITY_NewQuestion);
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent intent){
 		super.onActivityResult(requestCode, resultCode, intent);
 
 		switch(requestCode){
 		case ACTIVITY_NewQuestion:
 			Integer answer = (Integer)intent.getExtras().get("Answer");
 			_currentQuestion.setAnswer(answer);
 			switchQuestion(intent.getExtras().getBoolean("NextQuestion"));
 			break;
 		case ACTIVITY_Confirmation:
 			boolean submit = (Boolean)intent.getExtras().get("Submit");
 			if(submit)
 				terminate();
 			else
 				switchQuestion(false);
 		}
 	}
 	
 	/**
 	 * Store answer information in the database and pass control to
 	 * the thank you activity
 	 */
 	public void terminate(){
 		StringBuffer ansbuff = new StringBuffer();
 		//Store answers to the questions as a string for caching
 		for(Question q : _questionList)
 			ansbuff.append(q.getAnswer() + ",");
 		//Eliminate the ending comma
 		ansbuff.substring(0, ansbuff.length()-1);
 		try{
 			_dataManager.open();
 			
 			int cachesize = _dataManager.getAnswerCacheSize()+1;
 			_dataManager.insertAnswer(cachesize, ansbuff.toString());
 			
 			ArrayList<String> ans = _dataManager.getAllAnswers();
 		    _dataManager.close();
 		}catch(SQLiteException e){
 			//Catching exception so the app doesn't crash and just goes to the thank you screen
 		}
 		Intent i = new Intent(this, ThankYouActivity.class);
 		startActivity(i);
 	}
 }
