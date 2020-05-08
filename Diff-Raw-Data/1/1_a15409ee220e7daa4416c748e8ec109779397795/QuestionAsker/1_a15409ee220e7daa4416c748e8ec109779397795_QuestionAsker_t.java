 package org.nonstdout.askme;
 
 import org.nonstdout.askme.Question;
 
 import android.os.Handler;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.content.Context;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Vector;
 import java.util.Random;
 
 public class QuestionAsker
   implements TextToSpeech.OnInitListener,
              TextToSpeech.OnUtteranceCompletedListener
 {
   public static final String TAG = "Askme";
 
   private Random random_ = new Random();
 
   private Handler handler_ = new Handler();
 
   private String speech_text_;
   private String speech_id_;
 
   public static final String QUESTION_ID = "QUESTION";
   public static final String ANSWER_ID = "ANSWSER";
 
   private Vector<Question> questions_;
   private Question current_question_;
 
   private TextToSpeech tts_;
 
   public QuestionAsker(Context context)
   {
     tts_ = new TextToSpeech(context, this);
   }
 
   public void start(Vector<Question> questions)
   {
    questions_ = questions;
     next_question(0);
   }
 
   public void stop()
   {
     tts_.stop();
     handler_.removeCallbacks(speech_task_);
   }
 
   public void shutdown()
   {
     tts_.shutdown();
   }
 
   private Runnable speech_task_ = new Runnable() {
     public void run()
     {
       HashMap<String, String> params = new HashMap<String, String>();
       params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, speech_id_);
       tts_.speak(
           speech_text_,
           TextToSpeech.QUEUE_FLUSH,
           params);
     }
   };
 
   private void speak(String str, long delay, String utterance_id)
   {
     speech_text_ = str;
     speech_id_ = utterance_id;
     handler_.removeCallbacks(speech_task_);
     handler_.postDelayed(speech_task_, delay);
   }
 
   private void next_question(int delay)
   {
     current_question_ = pick_question();
     speak(current_question_.question(), delay, QUESTION_ID);
   }
 
   private Question pick_question()
   {
     int idx = random_.nextInt(questions_.size());
     return questions_.get(idx);
   }
 
   @Override
   public void onUtteranceCompleted(String utterance_id)
   {
     if (utterance_id.equals(ANSWER_ID))
     {
       next_question(3000);
     }
     else // QUESTION_ID
     {
       say_answer();
     }
   }
 
   private void say_answer()
   {
     speak(current_question_.answer(), 5000, ANSWER_ID);
   }
 
   @Override
   public void onInit(int status)
   {
     if (status == TextToSpeech.SUCCESS)
     {
       int result = tts_.setLanguage(Locale.US);
       if (result == TextToSpeech.LANG_MISSING_DATA ||
           result == TextToSpeech.LANG_NOT_SUPPORTED)
       {
         Log.e(TAG, "Language is not available.");
       }
       else
       {
         result = tts_.setOnUtteranceCompletedListener(this);
         if (result != TextToSpeech.SUCCESS)
         {
           Log.e(TAG, "Could not set utterance completed listener");
         }
         // start_button_.setEnabled(true);
       }
     }
     else
     {
       Log.e(TAG, "Could not initialize TextToSpeech.");
     }
   }
 }
 
