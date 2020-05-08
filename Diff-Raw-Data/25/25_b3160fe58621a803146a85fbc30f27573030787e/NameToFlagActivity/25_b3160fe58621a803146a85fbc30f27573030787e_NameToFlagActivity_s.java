 package com.quiz.flag;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 /**
 *
 * Name to flag game mode
 * @author Muammil
 */
 
 public class NameToFlagActivity extends Activity {
   private TextView questionText;
   private int questionId = 0;
   private CustomImageView[] flagOptions;
   private List<QuestionEntry> questions;
   private int questionLimit;
   private TextView countDownText;
   private ProgressBar progress;
   private final long ONE_SECOND = 1000;
   private int count;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.nametoflag_screen);
     QuestionGenerator generator = new QuestionGenerator();
     questions = generator.getQuestions();
     questionLimit = questions.size();
     questionText = (TextView) findViewById(R.id.tv_n2f_nation);
     flagOptions = new CustomImageView[4];
     flagOptions[0] = (CustomImageView) findViewById(R.id.iv_n2f_flag1);
     flagOptions[1] = (CustomImageView) findViewById(R.id.iv_n2f_flag2);
     flagOptions[2] = (CustomImageView) findViewById(R.id.iv_n2f_flag3);
     flagOptions[3] = (CustomImageView) findViewById(R.id.iv_n2f_flag4);
     countDownText = (TextView) findViewById(R.id.tv_n2f_count);
     progress = (ProgressBar) findViewById(R.id.pb_n2f);
     count = 30;
     progress.setMax(count);
     showNextQuestion();
     new Timer().scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
         runOnUiThread(new Runnable() {
          @Override
          public void run() {
             if(count > 0) {
               countDownText.setText(String.valueOf(count));
               progress.setProgress(count);
               count--;
             } else if(count == 0) {
               finish();
               cancel();
             }
           }
         });
       }
     }, 0, ONE_SECOND);
   }
 
   private void showNextQuestion() {
     if (questionId == questionLimit) {
       finish();
     } else {
       Countries answer = questions.get(questionId).getAnswer();
       questionText.setText(answer.getName());
       List<Countries> options = questions.get(questionId).getOptions();
       int optionCount = 0;
       for(Countries option : options) {
         flagOptions[optionCount].setImageResource(option.getDrawableId());
         flagOptions[optionCount].setTag(option);
         addClickListener(flagOptions[optionCount], answer);
         ++optionCount;
       }
     }
   }
 
   private void addClickListener(final CustomImageView option, final Countries answer) {
     final Countries selectedOption = (Countries)option.getTag();
     option.setOnClickListener(new View.OnClickListener() {
       @Override public void onClick(View v) {
         if(selectedOption == answer) {
           option.onClickImage(true, option.getWidth(), option.getHeight());
           new Handler().postDelayed(new Runnable() {
             @Override public void run() {
               ++questionId;
               clearAllSlection();
               showNextQuestion();
             }
          }, 250);
         } else {
           option.onClickImage(false, option.getWidth(), option.getHeight());
         }
         option.setClickable(false);
       }
     });
   }
 
   private void clearAllSlection() {
     for(int i = 0; i < 4; ++i) {
       flagOptions[i].clearSelection();
     }
   }
 }
