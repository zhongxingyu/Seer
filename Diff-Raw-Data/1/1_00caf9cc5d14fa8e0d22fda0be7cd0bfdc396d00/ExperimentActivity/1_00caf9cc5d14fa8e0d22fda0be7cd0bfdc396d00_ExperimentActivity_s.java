 package nz.ac.otago.linguistics.sgre;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Random;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.JsonWriter;
 import android.view.View;
 
 /**
  * Conduct an experiment for a single subject.
  * 
  * @author Tonic Artos
  */
 public class ExperimentActivity extends Activity {
 	private static final int BREAK_PERIOD = 12;
 	public static final int MODE_PRACTICE = 0;
 	public static final int MODE_EXPERIMENT1 = 1;
 	public static final int MODE_EXPERIMENT2 = 2;
 	protected static final String KEY_MODE = "key_mode";
 
 	/**
 	 * Indices match up with sentences. A true value means the matching sentence
 	 * has been used.
 	 */
 	private boolean[] usedSentences;
 
 	/**
 	 * The Number of used sentences.
 	 */
 	private int numUsedSentences;
 
 	protected Random rand;
 
 	private Vector<SentenceResult> results;
 	private int sentencesSinceBreak;
 	private int totalNumSentences;
 
 	protected long sessionId;
 
 	private int practice_count;
 	private int mode;
 	private ProfileResult profile;
 
 	@Override
 	@SuppressWarnings("deprecation")
 	protected void onResume() {
 		super.onResume();
 		final View mainView = findViewById(android.R.id.content);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			mainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
 		} else {
 			mainView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
 		}
 	}
 
 	/**
 	 * Called when the activity is first created. Uses intent to figure out
 	 * which mode to run in.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// setContentView(R.layout.landing_page);
 
 		mode = getIntent().getIntExtra(KEY_MODE, MODE_EXPERIMENT1);
 
 		results = new Vector<SentenceResult>();
 
 		rand = new Random();
 		numUsedSentences = 0;
 		if (mode == MODE_EXPERIMENT1) {
 			totalNumSentences = getResources().getStringArray(R.array.list1_sentences).length;
 		} else {
 			totalNumSentences = getResources().getStringArray(R.array.list2_sentences).length;
 		}
 		usedSentences = new boolean[totalNumSentences]; // All elements
 														// initialise to false
 
 		// Work out this session ID;
 		DatabaseHelper db = new DatabaseHelper(getApplicationContext());
 		Cursor c = db.getReadableDatabase().query(ExperimentData.TABLE, new String[] { ExperimentData.KEY_ROWID }, null, null, null, null, null);
 		if (!c.moveToLast()) {
 			sessionId = 1;
 		} else {
 			sessionId = c.getLong(c.getColumnIndex(ExperimentData.KEY_ROWID)) + 1;
 		}
 		c.close();
 		db.close();
 
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		ft.replace(android.R.id.content, ProfileFragment.newInstance(this, mode));
 		ft.setTransition(FragmentTransaction.TRANSIT_NONE);
 		ft.commit();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO: There may be a need to do stuff here to protect against the
 		// activity life-cycle.
 		super.onSaveInstanceState(outState);
 	}
 
 	/**
 	 * Add a result to the total collection for this session.
 	 * 
 	 * @param result
 	 */
 	public void addSentenceResult(SentenceResult result) {
 		results.add(result);
 	}
 	
 	public void addQuestionResult(QuestionResult result) {
 		results.lastElement().addQuestionResult(result);
 	}
 
 	/**
 	 * Add the gathered profile of the experimentee.
 	 * @param result
 	 */
 	public void addProfileResult(ProfileResult result) {
 		profile = result;
 	}
 
 	/**
 	 * Show another sentence to the user. Every 12 sentences a break page will
 	 * be shown instead. After showing all sentences an exit page will be shown
 	 * and the experiment will end.
 	 */
 	public void showNextSentence() {
 		// Finish when we have shown all sentences.
 		if (numUsedSentences - totalNumSentences == 0) {
 			storeResults();
 			FragmentTransaction ft = getFragmentManager().beginTransaction();
 			ft.replace(android.R.id.content, PauseFragment.newInstance(this, PauseFragment.MODE_EXIT, 0));
 			ft.setTransition(FragmentTransaction.TRANSIT_NONE);
 			ft.commit();
 		}
 
 		// Give the user a break every 12 (BREAK_PERIOD) sentences.
 		if (sentencesSinceBreak >= BREAK_PERIOD) {
 			sentencesSinceBreak = 0;
 			// Work out number of blocks remaining.
 			int numBlocksRemaining = (totalNumSentences - numUsedSentences) / BREAK_PERIOD;
 
 			// Create break page.
 			FragmentTransaction ft = getFragmentManager().beginTransaction();
 			ft.replace(android.R.id.content, PauseFragment.newInstance(this, PauseFragment.MODE_BREAK, numBlocksRemaining));
 			ft.setTransition(FragmentTransaction.TRANSIT_NONE);
 			ft.commit();
 
 			return;
 		}
 
 		// Select a valid random sentence index.
 		int selected = rand.nextInt(totalNumSentences - numUsedSentences);
 		// Jump past used sentences.
 		for (int i = 0; i <= selected; i++) {
 			if (usedSentences[i]) {
 				selected += 1;
 			}
 		}
 
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		ft.replace(android.R.id.content, SentenceFragment.newInstance(this, selected, mode));
 		ft.setTransition(FragmentTransaction.TRANSIT_NONE);
 		ft.commit();
 
 		sentencesSinceBreak += 1;
 		usedSentences[selected] = true;
 		numUsedSentences += 1;
 	}
 
 	/**
 	 * Save the experiment results into the database.
 	 */
 	private void storeResults() {
 		DatabaseHelper db = new DatabaseHelper(this);
 		ContentValues values = new ContentValues();
 
 		StringWriter jsonData = new StringWriter();
 		JsonWriter w = new JsonWriter(jsonData);
 		try {
 			w.beginObject();
 			profile.toJSON(w);
 			w.name("rows").beginArray();
 			for (JSONData r : results) {
 				r.toJSON(w);
 			}
 			w.endArray();
 			w.endObject();
 			w.flush();
 			values.put(ExperimentData.KEY_DATA, jsonData.toString());
 			w.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		db.getWritableDatabase().insert(ExperimentData.TABLE, null, values);
 		db.close();
 	}
 
 	public void showNextPracticeSentence() {
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		if (practice_count < getResources().getStringArray(R.array.practice_sentences).length) {
 			// Find next sentence in practice sentence order.
 			int[] ps = getResources().getIntArray(R.array.practice_indices);
 			for (int i = 0; i < ps.length; i++) {
 				if (ps[i] == practice_count) {
 					ft.replace(android.R.id.content, SentenceFragment.newInstance(this, i, MODE_PRACTICE));
 					break;
 				}
 			}
 			practice_count += 1;
 		} else {
 			ft.replace(android.R.id.content, TutorialFragment.newInstance(this, TutorialFragment.MODE_PART_2));
 		}
 		ft.setTransition(FragmentTransaction.TRANSIT_NONE);
 		ft.commit();
 	}
 }
