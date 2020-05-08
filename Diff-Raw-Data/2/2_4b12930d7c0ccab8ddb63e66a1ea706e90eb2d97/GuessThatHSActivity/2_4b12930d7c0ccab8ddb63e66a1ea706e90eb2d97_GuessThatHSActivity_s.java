 package knaps.hacker.school;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.app.NavUtils;
 import android.support.v4.content.Loader;
 import android.support.v4.util.LruCache;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import knaps.hacker.school.data.HSData;
 import knaps.hacker.school.data.HSRandomCursorWrapper;
 import knaps.hacker.school.data.SQLiteCursorLoader;
 import knaps.hacker.school.models.Student;
 import knaps.hacker.school.networking.Constants;
 import knaps.hacker.school.networking.ImageDownloads;
 
 public class GuessThatHSActivity extends FragmentActivity implements View.OnClickListener,
         LoaderManager.LoaderCallbacks<Cursor>, TextView.OnEditorActionListener {
 
     private static final String GUESS_COUNT = "guess_count";
     private static final String CORRECT_COUNT = "correct_count";
     private static final String SUCCESS_MESSAGE_COUNT = "success_count";
     private static final String HINT_MESSAGE_COUNT = "hint_count";
     private static final String GAME_OVER = "game_over";
 
     private ImageView mHsPicture;
     private EditText mEditGuess;
     private Button mGuess;
     private Button mRestartButton;
     private String mBatchName;
     private TextView mGuessCounter;
     private TextView mBatchText;
 
     private Student mCurrentStudent;
     private static Cursor mStudentCursor;
     private int mCurrentScore;
     private int mCurrentGuesses;
     private int mHintCount = 0;
     private int mGameMax = Integer.MAX_VALUE;
     private int mSuccessMessageCount = 0;
     private String[] mSuccessMessages = { "Yup.", "Correct.", "Yes." };
     private String[] mHintMessages = new String[] { "Give it a try.", "Not a guess?", "Hint: Starts with %s" };
 
     private LruCache<String, Bitmap> mMemoryCache;
     private ImageDownloads.RetainFragment mRetainedFragment;
 
     private boolean mIsRestart = false;
     private boolean mGameOver = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_guess);
 
         mHsPicture = (ImageView) findViewById(R.id.imageStudent);
         mEditGuess = (EditText) findViewById(R.id.editGuess);
         mEditGuess.setOnEditorActionListener(this);
         mGuess = (Button) findViewById(R.id.buttonGuess);
         mGuess.setOnClickListener(this);
         mGuess.setEnabled(false);
         mRestartButton = (Button) findViewById(R.id.buttonRestart);
         mRestartButton.setOnClickListener(this);
         mGuessCounter = (TextView) findViewById(R.id.textGuessCount);
         mBatchText = (TextView) findViewById(R.id.textBatchName);
 
         // TODO: save high score to preferences (so you can beat yourself!)
         // TODO: Settings -- save your email and password
 
         mRetainedFragment = ImageDownloads.RetainFragment.findOrCreateRetainFragment(getSupportFragmentManager());
         mMemoryCache = mRetainedFragment.mRetainedCache;
         if (mMemoryCache == null) {
             mMemoryCache = ImageDownloads.getBitmapMemoryCache();
             mRetainedFragment.mRetainedCache = mMemoryCache;
         }
 
         if (getIntent() != null) {
             mGameMax = getIntent().getIntExtra(Constants.GAME_MAX, Constants.INVALID_MIN);
             mBatchName = getIntent().getStringExtra(Constants.BATCH_NAME);
 
         }
         if (savedInstanceState != null) {
             mIsRestart = true;
             mCurrentGuesses = savedInstanceState.getInt(GUESS_COUNT);
             mCurrentScore = savedInstanceState.getInt(CORRECT_COUNT);
             mSuccessMessageCount = savedInstanceState.getInt(SUCCESS_MESSAGE_COUNT);
             mHintCount = savedInstanceState.getInt(HINT_MESSAGE_COUNT);
             mGameMax = savedInstanceState.getInt(Constants.GAME_MAX);
             mGameOver = savedInstanceState.getBoolean(GAME_OVER);
             mBatchName= savedInstanceState.getString(Constants.BATCH_NAME);
         }
 
         if (mBatchName != null && !Constants.BATCH_NAME.equals(mBatchName)) {
             mBatchText.setText(mBatchName);
         }
 
         getSupportLoaderManager().initLoader(0, null, this);
         setupActionBar();
 
         if (!mIsRestart) {
             showGameSettingsDialog();
         }
     }
 
     private void showGameSettingsDialog() {
         FragmentManager fm = getSupportFragmentManager();
         ChooseGameFragment fragment = new ChooseGameFragment();
         fragment.setOnChooseGameListener(new ChooseGameFragment.OnChooseGameListener() {
             @Override
             public void onChooseGame(String batch, int gameMax) {
                 mBatchName = batch;
                 mGameMax = gameMax;
                 restartGame();
             }
         });
         fragment.show(fm, "fragment_choose_dialog");
     }
 
     /**
      * Set up the {@link android.app.ActionBar}, if the API is available.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupActionBar() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
 
 //    @Override
 //    public boolean onCreateOptionsMenu(Menu menu) {
 //        // Inflate the menu; this adds items to the action bar if it is present.
 //        getMenuInflater().inflate(R.menu.guess_that_h, menu);
 //        return true;
 //    }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle icicle) {
         icicle.putInt(GUESS_COUNT, mCurrentGuesses);
         icicle.putInt(CORRECT_COUNT, mCurrentScore);
         icicle.putInt(SUCCESS_MESSAGE_COUNT, mSuccessMessageCount);
         icicle.putInt(HINT_MESSAGE_COUNT, mHintCount);
         icicle.putInt(Constants.GAME_MAX, mGameMax);
         icicle.putBoolean(GAME_OVER, mGameOver);
         icicle.putString(Constants.BATCH_NAME, mBatchName);
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.buttonGuess:
                 mGuess.setClickable(false);
                 final String guess = mEditGuess.getText().toString().toLowerCase().trim();
                 if ("".equals(guess)) {
                     Toast.makeText(this, String.format(mHintMessages[mHintCount], mCurrentStudent.mName.charAt(0)), Toast.LENGTH_SHORT).show();
                     incrementHint();
                     mGuess.setClickable(true);
                 }
                 else if (runGuess(guess)) {
                     mEditGuess.setEnabled(false);
                     showSuccess();
                     mCurrentScore++;
                     mCurrentGuesses++;
                 }
                 else {
                     mEditGuess.setEnabled(false);
                     showFail();
                     mCurrentGuesses++;
                 }
                 displayScore();
                 break;
             case R.id.buttonRestart:
                 showGameSettingsDialog();
             default:
                 //no default
         }
     }
 
     private void incrementHint() {
         mHintCount = Math.min(mHintCount + 1, mHintMessages.length - 1);
     }
 
     private void displayScore() {
 //        if (mCurrentGuesses != 0) {
 //            final float score = (float) (mCurrentScore / (mCurrentGuesses * 1.0) * 100);
 //        }
         if (mStudentCursor != null) {
             int count = mGameMax - mCurrentGuesses;
            if (count > 0) mGuessCounter.setText(String.valueOf(count));
         }
     }
 
     private void showNextStudent(boolean isFirst) {
         if (mStudentCursor.isLast() || mGameMax <= mStudentCursor.getPosition()) {
             showEndGame();
         }
         else if (mStudentCursor.getCount() > 0) {
             if (isFirst) {
                 mStudentCursor.moveToFirst();
             }
             else {
                 mStudentCursor.moveToNext();
             }
             showStudent();
             mGuess.setClickable(true);
             mEditGuess.setEnabled(true);
         }
         else {
             Toast.makeText(this, "No valid students found. Try connecting to the internet.", Toast.LENGTH_SHORT).show();
             this.finish();
         }
     }
 
     private void showStudent() {
         mCurrentStudent = new knaps.hacker.school.models.Student(mStudentCursor);
         mEditGuess.setText("");
         mHintCount = 0;
         new ImageDownloads.HSGetImageTask(mCurrentStudent.mImageUrl, mHsPicture, this).execute();
     }
 
     private void restartGame() {
         mCurrentScore = 0;
         mCurrentGuesses = 0;
         mSuccessMessageCount = 0;
         mHintCount = 0;
         mGameOver = false;
         mIsRestart = false;
 
         mRestartButton.setVisibility(View.GONE);
         mGuess.setVisibility(View.VISIBLE);
         mEditGuess.setVisibility(View.VISIBLE);
         getSupportLoaderManager().initLoader(0, null, this);
     }
 
     private void showEndGame() {
         mGameOver = true;
         mEditGuess.setVisibility(View.GONE);
         mGuess.setVisibility(View.GONE);
         mRestartButton.setVisibility(View.VISIBLE);
         mHsPicture.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
         double finalScore = mCurrentScore / (mCurrentGuesses * 1.0) * 100;
         mGuessCounter.setText(String.format("Final Score: %.0f%%", finalScore));
         String message;
         if (finalScore >= 90) {
             message = "Aces!";
         }
         else if (finalScore >= 70) {
             message = "The source is with you!";
         }
         else if (finalScore >= 30) {
             message = "Dr's orders: an alumni happy hour";
         }
         else {
             message = "Have you asked Govind about the memory palace?";
         }
         Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
     }
 
     private void showFail() {
         Toast.makeText(this, "This is " + mCurrentStudent.mName, Toast.LENGTH_SHORT).show();
         mGuessCounter.postDelayed(new Runnable() {
             @Override
             public void run() {
                 showNextStudent(false);
             }
         }, 1700);
     }
 
     private void showSuccess() {
         if (!"".equals(mCurrentStudent.mSkills) && mCurrentStudent.mSkills != null && mSuccessMessageCount % 3 == 2) {
             String[] skills = mCurrentStudent.mSkills.split(",");
             String skill = mCurrentStudent.mSkills;
             if (skills.length > 0) {
                 skill = skills[0];
             }
             Toast.makeText(this, "You got it. " + mCurrentStudent.mName + " is a " + skill + " ninja.",
                     Toast.LENGTH_SHORT).show();
         }
         else if (!"".equals(mCurrentStudent.mJob) && mCurrentStudent.mJob != null && mSuccessMessageCount % 3 == 1) {
             Toast.makeText(this, "Right. " + mCurrentStudent.mName + " works at " + mCurrentStudent.mJob,
                     Toast.LENGTH_SHORT).show();
         }
         else {
             Toast.makeText(this, mSuccessMessages[mSuccessMessageCount] + " You know " + mCurrentStudent.mName, Toast.LENGTH_SHORT).show();
         }
         incrementSuccess();
         mGuessCounter.postDelayed(new Runnable() {
             @Override
             public void run() {
                 showNextStudent(false);
             }
         }, 1700);
     }
 
     private void incrementSuccess() {
         mSuccessMessageCount++;
         mSuccessMessageCount = mSuccessMessageCount % 3;
     }
 
     private boolean runGuess(final String guess) {
         final String first = mCurrentStudent.mName.split(" ")[0];
         if (guess.equals(mCurrentStudent.mName.toLowerCase()) || guess.equals(first.toLowerCase())) {
             return true;
         }
         return false;
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         String limit = null;
         String selection = null;
         String[] selectionArgs = null;
         if (!ImageDownloads.isOnline(this)) {
             selection = HSData.HSer.COLUMN_NAME_IMAGE_FILENAME + HSData.STMT_IS_NOT_NULL;
         }
         if (mGameMax > 0) {
            limit = mGameMax + "";
         }
         if (!TextUtils.isEmpty(mBatchName) && !Constants.BATCH_STRING.equals(mBatchName)) {
             if (selection != null) selection += HSData.STMT_AND + HSData.HSer.COLUMN_NAME_BATCH + HSData.STMT_EQUALS_Q;
             else selection = HSData.HSer.COLUMN_NAME_BATCH + HSData.STMT_EQUALS_Q;
             selectionArgs = new String[] {mBatchName};
         }
         return new SQLiteCursorLoader.SQLiteCursorBuilder(this, HSData.HSer.TABLE_NAME)
                 .columns(HSData.HSer.PROJECTION_ALL)
                 .selection(selection)
                 .selectionArgs(selectionArgs)
                 .limit(limit).build();
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> objectLoader, Cursor o) {
         if (mGameMax == Constants.INVALID_MIN) {
             mGameMax = o.getCount();
         }
 
         if (mGameOver) {
             showEndGame();
         } else {
             mStudentCursor = new HSRandomCursorWrapper(o);
             if (!mIsRestart) {
                 showNextStudent(true);
             } else {
                 showStudent();
             }
             mGuess.setEnabled(true);
             displayScore();
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> objectLoader) {
         mStudentCursor = null;
     }
 
     @Override
     public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         if ((event != null && event.getKeyCode() == KeyEvent.FLAG_EDITOR_ACTION) ||
                 (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO)) {
             mGuess.performClick();
             final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(mEditGuess.getWindowToken(), 0);
             return true;
         }
         return false;
     }
 
 
 }
