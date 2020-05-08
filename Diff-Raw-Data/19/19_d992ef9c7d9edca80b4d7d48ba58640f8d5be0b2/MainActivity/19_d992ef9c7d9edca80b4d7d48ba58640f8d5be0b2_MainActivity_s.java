 /**
  * @author Parnit Sainion
  * @description MainActiviy for Homework 2 for cs185c
  * @dueDate 2-13-2013
  */
 package edu.sjsu.cs185c.hw02;
 
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * 
  * @author Parnit Sainion
  *@description Main activity create a quiz, which list questions fro the user to select and answer
  */
 public class MainActivity extends Activity {
 
 	//Variable Declared
 	//private final String urlLink = "http://horstmann.com/sjsu/spring2013/cs185c/hw02/quiz.xml";
 	private final String url = "http://www.sainion.net/parnit/Output.XML";
 	private ListView myListView;
     private static ArrayList<Question> myQuizList;
     private ListAdapter listAdapter;
     private MainActivity thisActivity = this;
     private Question selectedQuestion;
     private int selectedPosition;
     private ProgressDialog progressDialog;
     
     //final variables for check status of question
     private final int UNANSWERED = 0;
     private final int CORRECT = 1;
     private final int INCORRECT =2;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		//gets the quiz from the url and updates the gui
 		new QuestionGetterTask().execute(url);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	//modified from http://mobileorchard.com/android-app-development-using-intents-to-pass-data-and-return-results-between-activities/
 	//Gets the response data from finished activity
 	@Override
     public void onActivityResult(int requestCode,int resultCode,Intent data)
     {
      super.onActivityResult(requestCode, resultCode, data);
      
      
      	int answer = data.getIntExtra("chosenAnswer", 3);   
      	int position =data.getIntExtra("position", 101);
      	
      	//gets the question that was answered from list of questions
      	Question question = myQuizList.get(position);
      	
      	//changes the status of question to correct or incorrect
      	question.setAnswered(answer);
      	
      	//updates GUI to reflect changes
      	listAdapter.notifyDataSetChanged();     	
 		
     }
 	
 	/**
 	 * My list adapter class to diaply my list of questions
 	 * @author Parnit Sainion
 	 *
 	 */
     private class ListAdapter extends ArrayAdapter<Question>
     {
     	private ArrayList<Question> questions;
     	
     	public ListAdapter(Context context, int textViewResourceId, ArrayList<Question> qList)
     	{
     		super(context,textViewResourceId, qList);
    		questions = qList;
     	}
     	
     	public View getView(int position, View convertView, ViewGroup parent)
     	{
     		View v = convertView;
     		
     		if(v == null)
     		{
     			//inflate new question row
     			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     			v = vi.inflate(R.layout.list_row, null, false);
    		}
    		
     		//get Question object
     		Question question = questions.get(position);
     		
     		if(question!= null)
     		{
     			//sets the question text
     			TextView text = (TextView) v.findViewById(R.id.listRowText);
     			text.setText(question.getQuestion());
     			
     			//gets the question's answered status
     			int answered = question.getAnswered();
     			
     			//sets rows background based on question's status
     			if(answered == UNANSWERED)
     			{
     				v.setBackgroundColor(Color.WHITE);
     			}else if(answered == CORRECT)
     			{
     				v.setBackgroundColor(Color.GREEN);
     			}
     			else if(answered == INCORRECT)
     			{
     				v.setBackgroundColor(Color.RED);
     			}
     		}
     		
     		return v;
     	}
     }
     
     /**
      * My background Asynctask to get the quiz from the web
      * @author parnit
      *
      */
     private class QuestionGetterTask extends AsyncTask<String,Void, ArrayList<Question>>
 	{	
 		@Override
     	protected void onPreExecute()
     	{
 			//set up and show my progress Dialog
     		progressDialog = new ProgressDialog(thisActivity);
     		progressDialog.setIndeterminate(true);
     		progressDialog.setCancelable(false);
     		progressDialog.setMessage("Getting Quiz. Please Wait.");
     		progressDialog.show();    		
     	}
     	
     	
     	protected ArrayList<Question> doInBackground(String... params) {				        
         try {
         	//Create the parser
             SAXParserFactory factory = SAXParserFactory.newInstance();
             SAXParser parser = factory.newSAXParser(); 
             XMLReader reader = parser.getXMLReader();
             
             //set the parser to my handler
             MyParser myParser = new MyParser();
             reader.setContentHandler(myParser);
             
             //open the url link and get xml data
             URL url = new URL(params[0]);
             URLConnection connection = url.openConnection();
             connection.connect();
             reader.parse(new InputSource(url.openStream()));
            
             //returns quiz in the form of  an arraylist 
             return myParser.getQuiz();
         } catch (Exception ex) {
             Log.e("msg", ex.toString());
         }       
           return new ArrayList<Question>();
 		}
 
 		//takes the Quiz take and initializes the ListView
 		protected void onPostExecute(ArrayList<Question> quiz) 
 		{		       
 	       
	       MainActivity.myQuizList = quiz;
 	       	       
 	       //sets my listAdapter to the list
 	       	listAdapter = new ListAdapter(thisActivity,R.layout.list_row,myQuizList);
 			  myListView = (ListView) findViewById(R.id.question_list);
 			  myListView.setAdapter(listAdapter);
 			  
 	        //Creates the click listener
 	        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {           
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View view, int position,
 						long id) {
 					
 					//gets the question at the clicked position
 					selectedQuestion = (Question) listAdapter.getItem(position);
 					selectedPosition = position;
 					
 					//checks if the question has not been answered
 					if(selectedQuestion.getAnswered() == UNANSWERED)
 					{
 						Intent intent = new Intent(MainActivity.this, AnswerActivity.class);
 						intent.putExtra("question", selectedQuestion);
 						intent.putExtra("selectedPosition", selectedPosition);
 						final int intentMarker = 0;
 						startActivityForResult(intent, intentMarker);
 					}
 				}
 	        });
	        
 	        //dismiss the progress dialog
 	        if(progressDialog != null)
	        	progressDialog.dismiss();
 		}
 	}
 
 }
