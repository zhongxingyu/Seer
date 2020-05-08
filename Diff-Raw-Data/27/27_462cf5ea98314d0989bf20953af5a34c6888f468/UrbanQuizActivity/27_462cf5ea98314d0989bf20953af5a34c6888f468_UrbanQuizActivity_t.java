 package gary.cregan.urbanquiz.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.*;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.*;
 import gary.cregan.urbanquiz.R;
 import gary.cregan.urbanquiz.R.drawable;
 import gary.cregan.urbanquiz.db.DataBaseHelper;
 import gary.cregan.urbanquiz.domain.Question;
 import gary.cregan.urbanquiz.domain.Result;
 import gary.cregan.urbanquiz.domain.SearchResponse;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.http.*;
 import org.apache.http.client.methods.*;
 import org.apache.http.impl.client.*;
 
 import com.google.gson.Gson;
 
 public class UrbanQuizActivity extends Activity 
 {
 	String url = "http://www.urbandictionary.com/iphone/search/define?term=";
 	public TextView textView;
 	public TableLayout tableLayout;
 	public DataBaseHelper myDbHelper;
 	public InputStream source;
 	public Reader reader;
 	public int correctCount = 0;
 	public ProgressDialog progressDialog;
 	public AlertDialog alert;
 	public Button button1;
 	public Button button2;
 	public Button button3;
 	public Button button4;
 	public TextView totalSoFar;
 	public List<Question> listOfAnswers;
 	public Question currentQuestion;
 	public boolean initialSetup;
 	public TextView youGotIt;
 	public TextView author;
 	public TextView wordInUse;
 	public TextView correctAnswer;
 	public TextView sourceOfAnswer;
 	
 	public UrbanQuizActivity getUrbanQuizActivityContext()
 	{
 		return this;
 	}
 	
 	@Override 
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		progressDialog = new ProgressDialog(this); //taking object for progress dialog
     	progressDialog.setMessage("Loading...");
     	
     	AlertDialog.Builder builder;
 
     	Context mContext = this;
     	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
     	View layout = inflater.inflate(R.layout.custom_dialog,
     	                               (ViewGroup) findViewById(R.id.layout_root));
 
     	this.youGotIt = (TextView) layout.findViewById(R.id.youGotIt);
     	this.author = (TextView) layout.findViewById(R.id.author);
     	this.wordInUse = (TextView) layout.findViewById(R.id.wordInUse);
     	this.correctAnswer = (TextView) layout.findViewById(R.id.correctAnswer);
     	this.sourceOfAnswer = (TextView) layout.findViewById(R.id.source);
     	
     	builder = new AlertDialog.Builder(mContext);
     	builder.setView(layout);
     	builder.setCancelable(false)
 	           .setPositiveButton("Next Question", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   setAllButtons();
 		        	   dialog.cancel();
 		           }
 	           });
     	alert = builder.create();
     	
     	this.alert = builder.create();
 		
 		this.textView = (TextView)this.findViewById(R.id.textView);
 	    
 		this.myDbHelper = new DataBaseHelper(this);
 	    
 	    this.button1 = (Button)this.findViewById(R.id.button1);
 	    this.button1.setOnClickListener(new OnClickListener() 
 	    {
 	          public void onClick(View v) 
 	          {
 	        	  performButtonClickAnswerCheck(v);
 	          }
 	    });
 	    this.button2 = (Button)this.findViewById(R.id.button2);
 	    this.button2.setOnClickListener(new OnClickListener() 
 	    {
 	          public void onClick(View v) 
 	          {
 	        	  performButtonClickAnswerCheck(v);
 	          }
 	    });
 	    this.button3 = (Button)this.findViewById(R.id.button3);
 	    this.button3.setOnClickListener(new OnClickListener() 
 	    {
 	          public void onClick(View v) 
 	          {
 	        	  performButtonClickAnswerCheck(v);
 	          }
 	    });
 	    this.button4 = (Button)this.findViewById(R.id.button4);
 	    this.button4.setOnClickListener(new OnClickListener() 
 	    {
 	          public void onClick(View v) 
 	          {
 	        	  performButtonClickAnswerCheck(v);
 	          }
 	    });
 	    
 	    this.totalSoFar = (TextView)this.findViewById(R.id.totalSoFar);
 	    this.totalSoFar.setText("Score : 0");
 	
 	    this.listOfAnswers = new ArrayList<Question>();
 	    
 	    initialSetup = true;
 	    
 		this.addQuestion();
 	}
 	
 	public void showLoadingDialog()
 	{
 		if(progressDialog != null)
     	{
     		progressDialog.show();
     	}
 	}
 	
 	public void hideLoadingDialog()
 	{
 		if(progressDialog.isShowing())
     	{
     		progressDialog.dismiss();
     	}
 	}
 	
 	public void showAnsweredQuestionDialog(String correctOrIncorrect)
 	{
 		if(alert != null)
     	{
 			this.youGotIt.setText(correctOrIncorrect);
 			if(correctOrIncorrect.equals("Correct"))
 			{
 				this.youGotIt.setCompoundDrawablesWithIntrinsicBounds(drawable.tick, 0, 0, 0);
 			}
 			else
 			{
 				this.youGotIt.setCompoundDrawablesWithIntrinsicBounds(drawable.redx, 0, 0, 0);
 			}
 			
 			this.correctAnswer.setText(Html.fromHtml("<b>The correct answer was: </b> " + currentQuestion.getRightAnswer()));
 			this.wordInUse.setText(Html.fromHtml("<b>As in: </b> " + currentQuestion.getWordInUse()));
 			this.author.setText(Html.fromHtml("<b>Author: </b> " + currentQuestion.getAuthor()));
 			this.sourceOfAnswer.setText(Html.fromHtml("<b>Source: </b> Urban Dictionary"));
     		alert.show();
     	}
 	}
 	
 	public void hideAnsweredQuestionDialog()
 	{
 		if(alert.isShowing())
     	{
     		alert.dismiss();
     	}
 	}
 	
 	private void performButtonClickAnswerCheck(View v)
 	{
 		Button button = (Button)v;
     	if(button.getText().equals(currentQuestion.getRightAnswer()))
     	{
     		correctCount++;
     		this.showAnsweredQuestionDialog("Correct");
     	}
     	else
     	{
     		correctCount = 0;
     		this.showAnsweredQuestionDialog("Incorrect");
     	}
     	
     	this.addQuestion();
     	this.totalSoFar.setText("Score: " + correctCount);
 	}
 	private void addQuestion()
 	{
 		new DownloadNextQuestionTask().execute(url);
 	}
 	private InputStream retrieveStream(String url)
 	{
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpGet getRequest = new HttpGet(url);
 		try
 		{
 			HttpResponse getResponse = client.execute(getRequest);
 			final int statusCode = getResponse.getStatusLine().getStatusCode();
 			if (statusCode != HttpStatus.SC_OK)
 			{
 				Log.w(getClass().getSimpleName(), "Error " + statusCode + " for URL " + url);
 				return null;
 			}
 			HttpEntity getResponseEntity = getResponse.getEntity();
 			return getResponseEntity.getContent();
 		}
 		catch (IOException e)
 		{
 			getRequest.abort();
 			Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
 		}
 		return null;
 	}
 	
 	@Override 
 	public void onStart()
 	{
 		if(!myDbHelper.isOpen())
 		{
 			myDbHelper.openDataBase();			
 		}
 		
 		super.onStart();
 	}
 	
 	@Override 
 	public void onStop()
 	{
 		try 
 		{
 			reader.close();
 			source.close();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		myDbHelper.close();
 		super.onStop();
 	}
 	
 	@Override
 	protected void onDestroy() {
 	    super.onDestroy();
 	    if (myDbHelper != null) {
 	        myDbHelper.close();
 	    }
 	}
 
 	
 	public List<String> getRandomOtherAnswers(String argWord)
 	{
 		List<String> allPossibleWrongAnswers = myDbHelper.getWrongAnswers(argWord);
 		List<String> answersToBeReturned = new ArrayList<String>();
 		//We have a list of loads of possible wrong answers, just pick three
 		//randomly
 		if(allPossibleWrongAnswers.size() > 3)
 		{
 			for(int i = 0; i < 3; i++)
 			{
 				Random randomGenerator = new Random();
 				int randomInt = randomGenerator.nextInt(allPossibleWrongAnswers.size());
 				
 				if(argWord.split(" ").length > 1)
 				{
 					answersToBeReturned.add(allPossibleWrongAnswers.get(randomInt) + argWord.substring(argWord.lastIndexOf(" ")));				
 				}
 				else 
 				{
 					answersToBeReturned.add(allPossibleWrongAnswers.get(randomInt));
 				}
 				
 			}			
 		}
 		//In this scenario, the database failed to find words that start similaryly
 		//to the correct answer. So we get exactly three answers and return them.
 		//So in that case we just want to add the three (no need to randomly pick)
 		else if(allPossibleWrongAnswers.size() == 3)
 		{
 			answersToBeReturned.addAll(allPossibleWrongAnswers);
 		}
 		//I have made a terrible mistake.
 		else
 		{
 			System.out.println("ERROR: 0 Wrong Answers");
 		}
 		
 		convertToLowerCase(answersToBeReturned);
 		return answersToBeReturned;
 	}
 	
 	public void convertToLowerCase(List<String> listOfStrings)
 	{
 		ListIterator<String> iterator = listOfStrings.listIterator();
 	    while (iterator.hasNext())
 	    {
 	        iterator.set(iterator.next().toLowerCase());
 	    }
 
 	}
 	
 	public void setAllButtons()
 	{
 		//get last question on list
 		this.currentQuestion = listOfAnswers.get(listOfAnswers.size() - 1);
 		
 		//remove answer from the end
 		listOfAnswers.remove(listOfAnswers.size() - 1);
 		
 		this.textView.setText(this.currentQuestion.getDescription());
 		
 		List<String> allAnswers = new ArrayList<String>();
 		allAnswers.add(this.currentQuestion.getRightAnswer());
 		allAnswers.addAll(this.currentQuestion.getWrongAnswers());
 		
 		Collections.shuffle(allAnswers);
 		
 		button1.setText(allAnswers.get(0));
 		button2.setText(allAnswers.get(1));
 		button3.setText(allAnswers.get(2));
 		button4.setText(allAnswers.get(3));
 	}
 	
 	private class DownloadNextQuestionTask extends AsyncTask<String, Void, List<Result>> 
 	{	
 		protected void onPreExecute()
 		{
 			//if the list is empty, show the loading screen
 			if(listOfAnswers.isEmpty())
 			{
 				showLoadingDialog();
 			}
 		}
 		
 	    protected void onPostExecute(List<Result> result) 
 	    {
 	    	Question question = new Question();
 	    	question.setDescription(result.get(0).getDefinition());
 	    	question.setRightAnswer(result.get(0).getWord().toLowerCase().trim());
 	    	question.setAuthor(result.get(0).getAuthor());
 	    	question.setWordInUse(result.get(0).getExample());
 	    	
 	    	List<String> returnedAnswers = getRandomOtherAnswers(result.get(0).getWord());
 	    	question.addToWrongAnswers(returnedAnswers.get(0));
 	    	question.addToWrongAnswers(returnedAnswers.get(1));
 	    	question.addToWrongAnswers(returnedAnswers.get(2));
 	    	
 	    	listOfAnswers.add(question);
 	    	
 	    	//when we have two answers or mode, hide the dialog
 	    	if(listOfAnswers.size() >= 2)
 	    	{
 	    		hideLoadingDialog();
 	    		
 	    		if(initialSetup)
 	    		{
 	    			initialSetup = false;
 	    			setAllButtons();
 	    		}
 	    	}
 	    	else
 	    	{
 	    		addQuestion();
 	    	}
 	    }
 
 		@Override
 		protected List<Result> doInBackground(String... params) 
 		{	
 			SearchResponse response = null;
 			while(response == null || response.getResult_type().equals("no_results"))
 			{
 				Random randomGenerator = new Random();
 				int randomInt = randomGenerator.nextInt(40184);
 				source = retrieveStream(params[0] + myDbHelper.getWord(randomInt));
 				Gson gson = new Gson();
 				reader = new InputStreamReader(source);
 				response = gson.fromJson(reader, SearchResponse.class);				
 			}
 			return response.getList();
 		}
 	 }
 }
