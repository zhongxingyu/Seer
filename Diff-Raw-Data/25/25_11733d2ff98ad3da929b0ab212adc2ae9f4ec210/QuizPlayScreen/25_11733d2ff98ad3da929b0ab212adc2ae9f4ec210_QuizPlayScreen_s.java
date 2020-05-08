 package com.sarath.gkquiz;
 
 import java.util.List;
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This class is used for quiz board.
  *
  * @author sarath prakash.
  */
 public class QuizPlayScreen extends Activity {
 
   private TextView[] textIndicator;
   private TextView question;
   private TextView option1;
   private TextView option2;
   private TextView option3;
   private TextView option4;
   private TextView countDown;
   private int quizLimit;
   private int quizId;
   private List<QuizEntry> quizEntries;
   private CountDownTimer timer;
   private int numQuestionAnsweredCorrectly;
   private static final int DELAY_MILLIS = 4000;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.play_quiz);
     QuestionGenerator generator = new QuestionGenerator();
     quizEntries = generator.getQuizEntries();
     quizLimit = quizEntries.size();
     quizId = 0;
     numQuestionAnsweredCorrectly = 0;
     textIndicator = new TextView[15];
     question = (TextView)findViewById(R.id.tv_question);
     option1 = (TextView)findViewById(R.id.tv_choice_1);
     option2 = (TextView)findViewById(R.id.tv_choice_2);
     option3 = (TextView)findViewById(R.id.tv_choice_3);
     option4 = (TextView)findViewById(R.id.tv_choice_4);
     textIndicator[0] = (TextView)findViewById(R.id.tv_rs100);
     textIndicator[1] = (TextView)findViewById(R.id.tv_rs200);
     textIndicator[2] = (TextView)findViewById(R.id.tv_rs300);
     textIndicator[3] = (TextView)findViewById(R.id.tv_rs500);
     textIndicator[4] = (TextView)findViewById(R.id.tv_rs1000);
     textIndicator[5] = (TextView)findViewById(R.id.tv_rs2000);
     textIndicator[6] = (TextView)findViewById(R.id.tv_rs4000);
     textIndicator[7] = (TextView)findViewById(R.id.tv_rs8000);
     textIndicator[8] = (TextView)findViewById(R.id.tv_rs16000);
     textIndicator[9] = (TextView)findViewById(R.id.tv_rs32000);
     textIndicator[10] = (TextView)findViewById(R.id.tv_rs64000);
     textIndicator[11] = (TextView)findViewById(R.id.tv_rs125000);
     textIndicator[12] = (TextView)findViewById(R.id.tv_rs250000);
     textIndicator[13] = (TextView)findViewById(R.id.tv_rs500000);
     textIndicator[14] = (TextView)findViewById(R.id.tv_1million);
     countDown = (TextView)findViewById(R.id.tv_countdown);
     setOnClickListener(option1);
     setOnClickListener(option2);
     setOnClickListener(option3);
     setOnClickListener(option4);
     updateQuiz();
     updatePrizeIndicator();
   }
 
   private void setOnClickListener(final TextView selectedOption) {
     selectedOption.setOnClickListener(new OnClickListener() {
       @Override public void onClick(View v) {
        if(checkCurrentQuestion(selectedOption)) {
           numQuestionAnsweredCorrectly++;
           updatePrizeIndicator();
           if(timer != null) {
             timer.cancel();
             timer = null;
           }
           selectedOption.setBackgroundColor(Color.GREEN);
           ++quizId;
           delay();
           displayAmountWon();
         } else {
           selectedOption.setBackgroundColor(Color.RED);
           if(option1.getText().equals(quizEntries.get(quizId).answer)){
             option1.setBackgroundColor(Color.GREEN);
           }
           if(option2.getText().equals(quizEntries.get(quizId).answer)){
             option2.setBackgroundColor(Color.GREEN);
           }
           if(option3.getText().equals(quizEntries.get(quizId).answer)){
             option3.setBackgroundColor(Color.GREEN);
           }
           if(option4.getText().equals(quizEntries.get(quizId).answer)){
             option4.setBackgroundColor(Color.GREEN);
           }
           new Handler().postDelayed(new Runnable() {
             @Override public void run() {
               showToast(Color.RED, "GAME OVER");
               new Handler().postDelayed(new Runnable() {
                 @Override public void run() {
                   displayAmountWon();
                   new Handler().postDelayed(new Runnable() {
                     @Override public void run() {
                       finish();
                     }
                   }, DELAY_MILLIS);
                 }
               }, DELAY_MILLIS);
             }
           }, DELAY_MILLIS);
         }
       }
     });
   }
 
   private void showToast(int color, String post) {
     Toast toast = Toast.makeText(this, post, Toast.LENGTH_SHORT);
     toast.getView().setBackgroundColor(color);
     toast.show();
   }
 
   private void updateQuiz() {
     if (quizId < quizLimit) {
       countdownTimer();
       int blueColor = getResources().getColor(R.color.blue);
       option1.setBackgroundColor(blueColor);
       option2.setBackgroundColor(blueColor);
       option3.setBackgroundColor(blueColor);
       option4.setBackgroundColor(blueColor);
       QuizEntry entry = quizEntries.get(quizId);
       question.setText(entry.question);
       option1.setText(entry.options.get(0));
       option2.setText(entry.options.get(1));
       option3.setText(entry.options.get(2));
       option4.setText(entry.options.get(3));
     }
   }
 
   private void countdownTimer() {
     timer = new CountDownTimer(60000, 1000) {
       public void onTick(long millisUntilFinished) {
 	    countDown.setText("" + millisUntilFinished / 1000);
 	  }
 	  public void onFinish() {
 	    countDown.setText("TIMED OUT");
 	    countDown.setTextColor(Color.RED);
 	    new Handler().postDelayed(new Runnable() {
           @Override public void run() {
             displayAmountWon();
             new Handler().postDelayed(new Runnable() {
               @Override public void run() {
                 finish();
               }
             }, DELAY_MILLIS);
           }
         }, DELAY_MILLIS);
 	  }
 	}.start();
   }
 
   private void displayAmountWon() {
     switch(numQuestionAnsweredCorrectly) {
     case 0:
	  showToast(Color.GREEN, "WON Rs 0");
	  break;
     case 1:
       showToast(Color.GREEN, "WON Rs 100");
       break;
     case 2:
       showToast(Color.GREEN, "WON Rs 200");
       break;
     case 3:
       showToast(Color.GREEN, "WON Rs 300");
       break;
     case 4:
       showToast(Color.GREEN, "WON Rs 500");
       break;
     case 5:
       showToast(Color.GREEN, "WON Rs 1,000");
       break;
     case 6:
       showToast(Color.GREEN, "WON Rs 2,000");
       break;
     case 7:
       showToast(Color.GREEN, "WON Rs 4,000");
       break;
     case 8:
       showToast(Color.GREEN, "WON Rs 8,000");
       break;
     case 9:
       showToast(Color.GREEN, "WON Rs 16,000");
       break;
     case 10:
       showToast(Color.GREEN, "WON Rs 32,000");
       break;
     case 11:
       showToast(Color.GREEN, "WON Rs 64,000");
       break;
     case 12:
	  showToast(Color.GREEN, "WON Rs 125,000");
 	  break;
 	case 13:
      showToast(Color.GREEN, "WON Rs 250,000");
       break;
     case 14:
      showToast(Color.GREEN, "WON Rs 500,000");
       break;
 	case 15:
       showToast(Color.GREEN, "WON Rs 1 Million");
       break;
     }
   }
 
   public void delay() {
     new Handler().postDelayed(new Runnable() {
 	  @Override public void run() {
 	    updateQuiz();
 	  }
 	}, 1000);
   }
 
   public void updatePrizeIndicator() {
     switch (numQuestionAnsweredCorrectly) {
     case 0:
       textIndicator[0].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 1:
       textIndicator[0].setTextColor(Color.GREEN);
       textIndicator[1].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 2:
       textIndicator[1].setTextColor(Color.GREEN);
       textIndicator[2].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 3:
       textIndicator[2].setTextColor(Color.GREEN);
       textIndicator[3].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 4:
       textIndicator[3].setTextColor(Color.GREEN);
       textIndicator[4].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 5:
       textIndicator[4].setTextColor(Color.GREEN);
       textIndicator[5].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 6:
       textIndicator[5].setTextColor(Color.GREEN);
       textIndicator[6].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 7:
       textIndicator[6].setTextColor(Color.GREEN);
       textIndicator[7].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 8:
       textIndicator[7].setTextColor(Color.GREEN);
       textIndicator[8].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 9:
       textIndicator[8].setTextColor(Color.GREEN);
       textIndicator[9].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 10:
       textIndicator[9].setTextColor(Color.GREEN);
       textIndicator[10].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 11:
       textIndicator[10].setTextColor(Color.GREEN);
       textIndicator[11].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 12:
       textIndicator[11].setTextColor(Color.GREEN);
       textIndicator[12].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 13:
       textIndicator[12].setTextColor(Color.GREEN);
       textIndicator[13].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 14:
       textIndicator[13].setTextColor(Color.GREEN);
       textIndicator[14].setTextColor(getResources().getColor(R.color.orange));
       break;
     case 15:
       textIndicator[14].setTextColor(Color.GREEN);
       textIndicator[15].setTextColor(getResources().getColor(R.color.orange));
       break;
     default:
       break;
     }
   }

  private boolean checkCurrentQuestion(TextView selectedOption) {
    if(selectedOption.getText().equals(quizEntries.get(quizId).answer)) {
      return true;
    }else {
      return false;
    }
  }
 }
