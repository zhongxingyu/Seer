 package epfl.sweng.showquestions;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import epfl.sweng.R;
 import epfl.sweng.authentication.SessionManager;
 import epfl.sweng.entry.MainActivity;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.tasks.IQuizQuestionReceivedCallback;
 import epfl.sweng.tasks.IQuizServerCallback;
 import epfl.sweng.tasks.LoadRandomQuestion;
 import epfl.sweng.tasks.SubmitQuestionVerdict;
 import epfl.sweng.tasks.UpdateQuestionRating;
 
 
 /**
  * Activity showing a question
  */
 public class ShowQuestionsActivity extends Activity {
 	
 	private QuizQuestion mQuestionDisplayed = new QuizQuestion();
 	
 	private final IQuizQuestionReceivedCallback mQuizQuestionReceivedCallback = new IQuizQuestionReceivedCallback() {
 		
 		@Override
 		public void onQuestionSuccess(QuizQuestion question) {
 			displayQuestion(question);
 		}
 		
 		@Override
 		public void onRatingSuccess(QuizQuestion question) {
 			updateQuestionRating(question);
 		}
 		
 		
 		@Override
 		public void onRatingError() {
 			displayUpdateRatingError();
 		}
 		
 		@Override
 		public void onQuestionError() {
 			displayError();
 		}
 	};
 	
 	/**
 	 * Method invoked at the creation of the Activity. 
 	 * Triggers the display of a random question fetched from the server.
 	 * @param savedInstanceState the saved instance
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_questions);
 
         if (!SessionManager.getInstance().isAuthenticated()) {
         	finish();
         	Intent mainActivityIntent = new Intent(this, MainActivity.class);
         	startActivity(mainActivityIntent);
         }
         
         new LoadRandomQuestion(mQuizQuestionReceivedCallback).execute();
 
     }
     
     /**
      * Display an error if the application was unable to fetch a random Question
      */
     public void displayError() {
     	final ListView listView = (ListView) findViewById(R.id.listView);
         final TextView questionTxt = (TextView) findViewById(R.id.question);
     	final Button button = (Button) findViewById(R.id.button);
         listView.setVisibility(View.GONE);
         questionTxt.setText(R.string.error_message);
         button.setEnabled(true);
     }
     
     /**
      * Display a question on the screen
      * @param question The question to be displayed
      */
     public void displayQuestion(final QuizQuestion question) {
 
     	final ListView listView = (ListView) findViewById(R.id.listView);
         final TextView questionTxt = (TextView) findViewById(R.id.question);
         final Button button = (Button) findViewById(R.id.button);
         listView.setVisibility(View.VISIBLE);
         mQuestionDisplayed = question;
         questionTxt.setText(question.getQuestion());
                        
         // Instantiating array adapter to populate the listView
         // Using an ArrayList instead of an Array to populate, for future modifications
         // The layout android.R.layout.simple_list_item_single_choice creates radio button for each listview item
         final ArrayList<String> listAnswers = new ArrayList<String>();
         listAnswers.addAll(Arrays.asList(question.getAnswers()));
         final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
         														android.R.layout.simple_list_item_single_choice,
         														listAnswers);
 
         listView.setAdapter(adapter);
         listView.setEnabled(true);
         
         button.setEnabled(false);
         
         // Instantiating a boolean array to keep track of the clicked items
         // Item #i has been clicked <=> clickedItems[i]==true
         final boolean[] clickedItems = new boolean[question.getAnswers().length];
         
         // Implementing the interaction with the user
         listView.setOnItemClickListener(new OnItemClickListener() {
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         		
         		// User clicked the right answer
         		if (position == question.getSolutionIndex()) {
         			clickedItems[position] = true;
         			listAnswers.set(position, listAnswers.get(position) + " \u2714");
         			listView.setEnabled(false);
         			button.setEnabled(true);
         		// User clicked the wrong answer for first time
         		} else if (!clickedItems[position]) {
         			clickedItems[position] = true;
         			listAnswers.set(position, listAnswers.get(position) + " \u2718");
         		}
         		
         		adapter.notifyDataSetChanged();
         	}
         });
         
         updateQuestionRating(question);
     } 
     
     public void updateQuestionRating(QuizQuestion question) {
     	
     	final Button buttonLike = (Button) findViewById(R.id.button_like);
     	final Button buttonDislike = (Button) findViewById(R.id.button_dislike);
     	final Button buttonIncorrect = (Button) findViewById(R.id.button_incorrect);
 		
     	buttonLike.setText(String.format(getResources().getString(R.string.button_like),
     			question.getLikeCount()));
     	buttonDislike.setText(String.format(getResources().getString(R.string.button_dislike),
     			question.getDislikeCount()));
     	buttonIncorrect.setText(String.format(getResources().getString(R.string.button_incorrect),
     			question.getIncorrectCount()));
     	
     	buttonLike.setVisibility(View.VISIBLE);
     	buttonDislike.setVisibility(View.VISIBLE);
     	buttonIncorrect.setVisibility(View.VISIBLE);
     	
     	
 	}
     
     public void updatePersonalRating(QuizQuestion question) {
     	final TextView textRatingVerdict = (TextView) findViewById(R.id.text_rating_verdict);
     	
     	if (question.getVerdict().equals("like")) {
     		textRatingVerdict.setText(R.string.rating_like);
         } else if (question.getVerdict().equals("dislike")) {
         	textRatingVerdict.setText(R.string.rating_dislike);
     	} else if (question.getVerdict().equals("incorrect")) {
     		textRatingVerdict.setText(R.string.rating_incorrect);
     	} else {
     		textRatingVerdict.setText(R.string.rating_notrated);
     	}
     }
 
 	/**
      * Handle the "Next Question" button. Loads a new random question
      * @param currentView
      */
     public void nextQuestion(View currentView) {
         new LoadRandomQuestion(mQuizQuestionReceivedCallback).execute();
     }
 
     
     /**
      * Handle the "Like" Button
      * @param likeButton the like button
      */
     public void clickedLike(View likeButton) {
     	mQuestionDisplayed.setVerdict("like");
     	submitQuizQuestionVerdict();
     }
     
     
 
 	/**
      * Handle the "Dislike" Button
      * @param dislikeButton the like button
      */
     public void clickedDislike(View dislikeButton) {
     	mQuestionDisplayed.setVerdict("dislike");
     	submitQuizQuestionVerdict();    	
     }
     
     /**
      * Handle the "Incorrect" Button
      * @param incorrectButton the like button
      */
     public void clickedIncorrect(View incorrectButton) {
     	mQuestionDisplayed.setVerdict("incorrect");
     	submitQuizQuestionVerdict();
     }
     
     private void submitQuizQuestionVerdict() {
         new SubmitQuestionVerdict(new IQuizQuestionReceivedCallback() {
 			
 			@Override
 			public void onQuestionSuccess(QuizQuestion question) {
 			}
 			
 			@Override
 			public void onRatingSuccess(QuizQuestion question) {
 				updatePersonalRating(question);
				new UpdateQuestionRating(new IQuizServerCallback() {
					
					@Override
					public void onSuccess(QuizQuestion question) {
						updateQuestionRating(question);
					}
					
					@Override
					public void onError() {
						displayUpdateRatingError();
					}
				}).execute(mQuestionDisplayed);
 			}
 			
 			@Override
 			public void onRatingError() {
 				displaySubmitRatingError();
 			}
 			
 			@Override
 			public void onQuestionError() {
 				displaySubmitRatingError();
 			}
 		}).execute(mQuestionDisplayed);
         
        
 	}
 
     /**
      * Display Error message if the rating refresh wasn't successful
      */
     public void displayUpdateRatingError() {
     	Toast.makeText(this, R.string.load_rating_error_text, Toast.LENGTH_LONG).show();
 
     }
     
 
     /**
      * Display Error message if the rating submission wasn't successful
      */
     public void displaySubmitRatingError() {
     	Toast.makeText(this, R.string.submit_rating_error_text, Toast.LENGTH_LONG).show();
     }
 }
