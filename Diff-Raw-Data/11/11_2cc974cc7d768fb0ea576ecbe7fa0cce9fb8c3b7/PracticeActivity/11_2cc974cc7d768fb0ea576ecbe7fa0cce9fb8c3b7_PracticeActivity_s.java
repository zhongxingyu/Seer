 package ro.undef.patois;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.TranslateAnimation;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.util.ArrayList;
 
 public class PracticeActivity extends Activity {
     private final static String TAG = "PracticeActivity";
 
     private final static int STATE_QUESTION = 0;
     private final static int STATE_ANSWER = 1;
 
     private Database mDb;
     private Trainer mTrainer;
 
     private LayoutInflater mInflater;
     private ScoreRenderer mScoreRenderer;
     private TextView mScoreView;
     private ViewGroup mWholeScreen;
     private ViewGroup mWordPanel;
     private ViewGroup mQuestionButtons;
     private ViewGroup mAnswerButtons;
     private Animation mFadeInAnimation;
     private Animation[] mButtonsOutAnimation;
     private Animation[] mButtonsInAnimation;
     private Animation[] mRestartOutAnimation;
     private Animation[] mRestartInAnimation;
 
     // These fields are saved across restarts.
     private Trainer.Direction mDirection;
     private int mState;
     private Word mWord;
     private ArrayList<Word> mTranslations;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         try {
             mDb = new Database(this);
             mTrainer = new Trainer(mDb);
 
             if (savedInstanceState != null) {
                 loadStateFromBundle(savedInstanceState);
             } else {
                 String action = getIntent().getAction();
                 resetState(Trainer.Direction.fromAction(action));
             }
 
             setupViews();
         } catch (Trainer.EmptyException ex) {
             Toast.makeText(this, R.string.no_words_for_practice, Toast.LENGTH_LONG).show();
             finish();
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         mDb.close();
     }
 
     private void resetState(Trainer.Direction direction) throws Trainer.EmptyException {
         mDirection = direction;
         mState = STATE_QUESTION;
         loadWord(mTrainer.selectWord(mDb.getActiveLanguage(), mDirection));
     }
 
     private boolean loadWord(long wordId) {
         mWord = mDb.getWord(wordId);
         if (mWord == null)
             return false;
 
         mTranslations = mDb.getTranslations(mWord);
 
         return true;
     }
 
     @SuppressWarnings("unchecked")
     private void loadStateFromBundle(Bundle savedInstanceState) {
         mDirection = Trainer.Direction.fromValue(savedInstanceState.getInt("direction"));
         mState = savedInstanceState.getInt("state");
         mWord = (Word) savedInstanceState.getSerializable("word");
         mTranslations = (ArrayList<Word>) savedInstanceState.getSerializable("translations");
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putInt("direction", mDirection.getValue());
         outState.putInt("state", mState);
         outState.putSerializable("word", mWord);
         outState.putSerializable("translations", mTranslations);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	MenuInflater inflater = getMenuInflater();
 	inflater.inflate(R.menu.practice_activity_menu, menu);
 	return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.edit_word: {
                 Bundle extras = new Bundle();
                 extras.putLong("word_id", mWord.getId());
 
                 Intent intent = new Intent();
                 intent.setClass(this, EditWordActivity.class);
                 intent.setAction(Intent.ACTION_EDIT);
                 intent.putExtras(extras);
 
                 startActivityForResult(intent, R.id.edit_word);
                 return true;
             }
         }
         return false;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case R.id.edit_word: {
                 // The word might have been deleted during the edit operation,
                 // so it's possible for loadWord() to return false.
                 if (loadWord(mWord.getId()))
                     updateViews();
                 else
                     finish();
                 break;
             }
         }
     }
 
     private void setupViews() {
         setContentView(R.layout.practice_activity);
 
         mInflater = getLayoutInflater();
 
         mScoreRenderer = new ScoreRenderer(this,
                                            R.style.practice_score_good,
                                            R.style.practice_score_average,
                                            R.style.practice_score_bad);
         mScoreView = (TextView) findViewById(R.id.score);
         mWholeScreen = (ViewGroup) findViewById(R.id.whole_screen);
         mWordPanel = (ViewGroup) findViewById(R.id.word_panel);
         mQuestionButtons = (ViewGroup) findViewById(R.id.question_side);
         mAnswerButtons = (ViewGroup) findViewById(R.id.answer_side);
 
         findViewById(R.id.show).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showAnswer();
             }
         });
 
         findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 saveStatsAndRestart(true);
             }
         });
         findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 saveStatsAndRestart(false);
             }
         });
 
         mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.practice_word_fade_in);
 
         mButtonsOutAnimation = new Animation[2];
         mButtonsOutAnimation[Trainer.Direction.FROM_FOREIGN.getValue()] =
             AnimationUtils.loadAnimation(this, R.anim.practice_buttons_from_out);
         mButtonsOutAnimation[Trainer.Direction.TO_FOREIGN.getValue()] =
             AnimationUtils.loadAnimation(this, R.anim.practice_buttons_to_out);
 
         mButtonsInAnimation = new Animation[2];
         mButtonsInAnimation[Trainer.Direction.FROM_FOREIGN.getValue()] =
             AnimationUtils.loadAnimation(this, R.anim.practice_buttons_from_in);
         mButtonsInAnimation[Trainer.Direction.TO_FOREIGN.getValue()] =
             AnimationUtils.loadAnimation(this, R.anim.practice_buttons_to_in);
 
         mRestartOutAnimation = new Animation[2];
         mRestartOutAnimation[0] =
             AnimationUtils.loadAnimation(this, R.anim.practice_restart_no_out);
         mRestartOutAnimation[1] =
             AnimationUtils.loadAnimation(this, R.anim.practice_restart_yes_out);
 
         mRestartInAnimation = new Animation[2];
         mRestartInAnimation[0] =
             AnimationUtils.loadAnimation(this, R.anim.practice_restart_no_in);
         mRestartInAnimation[1] =
             AnimationUtils.loadAnimation(this, R.anim.practice_restart_yes_in);
 
         updateViews();
     }
 
     private void updateViews() {
        mScoreView.setText(mScoreRenderer.renderScore(mDb.getPracticeInfo(mWord, mDirection)));
 
         mWordPanel.removeAllViews();
 
         if (mState == STATE_ANSWER || mDirection == Trainer.Direction.FROM_FOREIGN)
             mWordPanel.addView(buildWordView(mWord, true), 0);
 
         if (mState == STATE_ANSWER || mDirection == Trainer.Direction.TO_FOREIGN)
             mWordPanel.addView(buildTranslationsPanel());
 
         if (mState == STATE_QUESTION) {
             mQuestionButtons.setVisibility(View.VISIBLE);
             mAnswerButtons.setVisibility(View.GONE);
         } else if (mState == STATE_ANSWER) {
             mQuestionButtons.setVisibility(View.GONE);
             mAnswerButtons.setVisibility(View.VISIBLE);
         }
     }
 
     private View buildWordView(Word word, boolean isMainWord) {
         View view = mInflater.inflate(R.layout.practice_word_entry, mWordPanel, false);
 
         TextView nameView = (TextView) view.findViewById(R.id.name);
         nameView.setText(word.getName());
 
         TextView languageView = (TextView) view.findViewById(R.id.language);
         languageView.setText(String.format("(%s)", word.getLanguage().getCode()));
 
         if (isMainWord) {
             nameView.setTextAppearance(this, R.style.practice_main_word_name);
             languageView.setTextAppearance(this, R.style.practice_main_word_language);
         }
 
         return view;
     }
 
     private ViewGroup buildTranslationsPanel() {
         LinearLayout panel = new LinearLayout(this);
         panel.setOrientation(LinearLayout.VERTICAL);
 
         for (Word word : mTranslations)
             panel.addView(buildWordView(word, false));
 
         return panel;
     }
 
     private void showAnswer() {
         mState = STATE_ANSWER;
 
         if (mDirection == Trainer.Direction.FROM_FOREIGN) {
             addToWordPanel(buildTranslationsPanel(), -1);
         } else if (mDirection == Trainer.Direction.TO_FOREIGN) {
             addToWordPanel(buildWordView(mWord, true), 0);
         }
 
         flipButtons();
     }
 
     private void addToWordPanel(final View view, final int index) {
         // First we need to measure the view to be added, since we need to move
         // the existing elements with half the height of the new component.
         view.measure(MeasureSpec.makeMeasureSpec(mWordPanel.getMeasuredWidth(),
                                                  MeasureSpec.AT_MOST),
                      MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
 
         // If we're adding the new view at the top (index == 0), then we need
         // to move the existing elements downwards; otherwise, we move them
         // upwards.
         final float offset = (index == 0 ? 1 : -1) * view.getMeasuredHeight() / 2.0f;
 
         Animation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, offset);
         animation.setInterpolator(new AccelerateInterpolator(1.0f));
         animation.setDuration(150);
         animation.setAnimationListener(new Animation.AnimationListener() {
             public void onAnimationEnd(Animation a) {
                 mWordPanel.addView(view, index);
                 mWordPanel.clearAnimation();
                 view.startAnimation(mFadeInAnimation);
             }
             public void onAnimationStart(Animation animation) {}
             public void onAnimationRepeat(Animation animation) {}
         });
 
         mWordPanel.startAnimation(animation);
     }
 
     private void flipButtons() {
         mQuestionButtons.startAnimation(mButtonsOutAnimation[mDirection.getValue()]);
         mQuestionButtons.setVisibility(View.GONE);
         mAnswerButtons.startAnimation(mButtonsInAnimation[mDirection.getValue()]);
         mAnswerButtons.setVisibility(View.VISIBLE);
     }
 
     private void saveStatsAndRestart(boolean knewAnswer) {
         mTrainer.updatePracticeInfo(mWord, mDirection, knewAnswer);
 
         final Animation animation_out = mRestartOutAnimation[knewAnswer ? 1 : 0];
         final Animation animation_in = mRestartInAnimation[knewAnswer ? 1 : 0];
 
         animation_out.setAnimationListener(new Animation.AnimationListener() {
             public void onAnimationEnd(Animation a) {
                 restart();
                 mWholeScreen.startAnimation(animation_in);
             }
             public void onAnimationStart(Animation animation) {}
             public void onAnimationRepeat(Animation animation) {}
         });
 
         mWholeScreen.startAnimation(animation_out);
     }
 
     private void restart() {
         try {
             resetState(mDirection);
             updateViews();
         } catch (Trainer.EmptyException ex) {
             Toast.makeText(this, R.string.no_words_for_practice, Toast.LENGTH_LONG).show();
             finish();
         }
     }
 }
