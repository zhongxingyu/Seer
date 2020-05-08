 package com.movisens.xs.android.cognition.emo_stroop;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.PowerManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.movisens.xs.android.cognition.CognitiveActivity;
 import com.movisens.xs.android.cognitive.library.R;
 
 /**
  * This class implements the "Emotional Stroop Test". See:
  * http://www.onlinestrooptest.com/emotional_stroop_test.php
  * 
  * @author Stephan Grund
  * 
  */
 public class EmoStroop extends CognitiveActivity {
 	private final int buttonsPerLine = 6;
 	private final Handler mHandler = new Handler(Looper.getMainLooper());
 	private TestRun actualRun = null;
 	private PowerManager.WakeLock wakelock = null;
 
 	Set<String> emotionalWords = new HashSet<String>();
 	Set<String> neutralWords = new HashSet<String>();
 
 	/**
 	 * Font size of the shown key words.
 	 */
 	protected float wordFontSize = 50;
 
 	/**
 	 * The states of the test. See doc for a diagram of the state machine.
 	 */
 	public enum State {
 		INSTRUCTIONS, SHOW_RESULT, FINISHING, SHOW_WORD, undef
 	}
 
 	/**
 	 * The actual state of the test.
 	 */
 	protected State state = State.undef;
 
 	/**
 	 * The estimated next state of the test.
 	 */
 	protected State transitionTo = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "STROOP");
 
 		setContentView(R.layout.emo_stroop_intro);
 		((Button) findViewById(R.id.stroop_finishTest))
 				.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View arg0) {
 						actualRun.startButtonClick();
 					}
 
 				});
 
 		TextView link = ((TextView) findViewById(R.id.stroop_linkTextView));
 		link.setClickable(true);
 		link.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				String url = "http://www.onlinestrooptest.com/emotional_stroop_test.php";
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse(url));
 				startActivity(i);
 			}
 
 		});
 
 		fillParameters(getIntent(), this);
 
 		actualRun = new TestRun();
 		transitionTo = State.INSTRUCTIONS;
 		mHandler.post(actualRun);
 	}
 
 	private void fillParameters(Intent intent, Context context) {
 		try {
 			String[] emoWords = null;
 			fillStringArray(emoWords, "emotionalWords");
 			emotionalWords = new HashSet<String>(Arrays.asList(emoWords));
 
 			String[] neutraWords = null;
 			fillStringArray(neutraWords, "neutralWords");
 			neutralWords = new HashSet<String>(Arrays.asList(neutraWords));
 		} catch (Exception e) {
 			Toast toast = Toast.makeText(context,
					"Invalid Parameters: " + e.getMessage(), Toast.LENGTH_LONG);
 			toast.show();
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		wakelock.acquire();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		wakelock.release();
 	}
 
 	private void setMainLayout() {
 		setContentView(R.layout.emo_stroop);
 
 		String[] color_names = new String[] { "Black", "Blue", "Yellow",
 				"Green", "Red" };
 		int[] colors = new int[] { Color.BLACK, Color.BLUE, Color.YELLOW,
 				Color.GREEN, Color.RED };
 
 		for (int i = 0; i < color_names.length; i++)
 			addColorButton(color_names[i], colors[i]);
 	}
 
 	private void setResultLayout() {
 		setContentView(R.layout.emo_stroop_results);
 		Button buttonFinish = (Button) this
 				.findViewById(R.id.stroop_finishTest);
 		buttonFinish.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				actualRun.finishButtonClick();
 			}
 
 		});
 
 		Button buttonDetails = (Button) this
 				.findViewById(R.id.stroop_detailsButton);
 		buttonDetails.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				showDetailsPage();
 			}
 
 		});
 	}
 
 	// Can be called after the test results are shown.
 	private void showDetailsPage() {
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ObjectOutputStream oos = new ObjectOutputStream(baos);
 			oos.writeObject(actualRun.trials);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Adds a color button that can be chosen to assess a word.
 	 * 
 	 * @param colorName
 	 * @param buttonColor
 	 */
 	protected void addColorButton(final String colorName, int buttonColor) {
 		final LinearLayout layout = (LinearLayout) this
 				.findViewById(R.id.stroop_buttonsLinearLayout);
 		LinearLayout lastLine = (LinearLayout) layout.getChildAt(layout
 				.getChildCount() - 1);
 		if (lastLine == null || lastLine.getChildCount() >= buttonsPerLine) {
 			final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
 					LinearLayout.LayoutParams.WRAP_CONTENT,
 					LinearLayout.LayoutParams.WRAP_CONTENT);
 
 			lastLine = new LinearLayout(this);
 			lastLine.setOrientation(LinearLayout.HORIZONTAL);
 			layout.addView(lastLine, lp);
 		}
 
 		final Button colorButton = new Button(this);
 		colorButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				actualRun.choiceButtonClick(colorName);
 			}
 
 		});
 		colorButton.setText(colorName);
 		colorButton.setTextColor(buttonColor);
 		lastLine.addView(colorButton);
 	}
 
 	/**
 	 * Permutes the entries of the list at random.
 	 * 
 	 * @param list
 	 *            The list to be permuted.
 	 */
 	protected static <T> void generateRandomList(List<T> list) {
 		List<T> setTemp = new ArrayList<T>(list);
 		Random r = new Random(System.currentTimeMillis());
 		list.clear();
 
 		while (setTemp.size() > 0)
 			list.add(setTemp.remove(r.nextInt(setTemp.size())));
 	}
 
 	// Implements the test process.
 	// Has to be posted to the main-Thread because of GUI-manipulations.
 	private class TestRun implements Runnable {
 		Iterator<String> testWords = null;
 
 		List<Result> trials = new ArrayList<Result>();
 
 		String actualWord = null;
 		long beginShowWord = -1;
 		boolean lockChoice = false;
 
 		// Invoked when a color button has been pressed.
 		public void choiceButtonClick(String choice) {
 			if (!lockChoice) {
 				lockChoice = true;
 
 				switch (state) {
 				case SHOW_WORD:
 					final long period = System.currentTimeMillis()
 							- beginShowWord;
 					final Result result = new Result(period, actualWord, choice);
 					trials.add(result);
 					transitionTo = State.SHOW_WORD;
 					mHandler.post(this);
 					break;
 				default:
 					assert (false);
 				}
 			}
 		}
 
 		// Invoked when the start button at the introduction screen has been
 		// pressed.
 		public void startButtonClick() {
 			switch (state) {
 			case INSTRUCTIONS:
 				setMainLayout();
 				transitionTo = State.SHOW_WORD;
 				mHandler.post(this);
 				break;
 			default:
 				assert (false);
 			}
 		}
 
 		// Invoked when the finish button at the results screen has been
 		// pressed.
 		public void finishButtonClick() {
 			switch (state) {
 			case SHOW_RESULT:
 				transitionTo = State.FINISHING;
 				mHandler.post(this);
 				break;
 			default:
 				assert (false);
 			}
 		}
 
 		@Override
 		public void run() {
 			assert (transitionTo != null);
 			state = transitionTo;
 			transitionTo = null;
 
 			switch (state) {
 			case INSTRUCTIONS:
 				List<String> testWordsTemp = new ArrayList<String>();
 				testWordsTemp.addAll(emotionalWords);
 				testWordsTemp.addAll(neutralWords);
 				generateRandomList(testWordsTemp);
 				testWords = testWordsTemp.iterator();
 				break;
 			case SHOW_WORD:
 				lockChoice = false;
 
 				if (!testWords.hasNext())
 					state = State.SHOW_RESULT; // drop through to SHOW_RESULT
 				else {
 					actualWord = testWords.next();
 
 					TextView nextWord = (TextView) EmoStroop.this
 							.findViewById(R.id.stroop_wordTextView);
 					nextWord.setText(actualWord);
 					nextWord.setTextSize(wordFontSize);
 					beginShowWord = System.currentTimeMillis();
 					break;
 				}
 			case SHOW_RESULT:
 				setResultLayout();
 				float meanEmo = 0,
 				meanNeutral = 0;
 
 				for (Result r : trials) {
 					// mean += r.reaction;
 					if (emotionalWords.contains(r.testWord))
 						meanEmo += r.reaction;
 					else {
 						assert (neutralWords.contains(r.testWord));
 						meanNeutral += r.reaction;
 					}
 				}
 				// mean /= trials.size();
 				meanEmo /= emotionalWords.size();
 				meanNeutral /= neutralWords.size();
 
 				((TextView) findViewById(R.id.stroop_meanEmo_TextView))
 						.setText(Integer.toString((int) meanEmo));
 				((TextView) findViewById(R.id.stroop_meanNeutral_TextView))
 						.setText(Integer.toString((int) meanNeutral));
 
 				break;
 			case FINISHING:
 				Intent intent = new Intent();
 				StringBuilder sb = new StringBuilder();
 				for (int i = 0; i < trials.size(); i++) {
 					if (i != 0) {
 						sb.append("#");
 					}
 					sb.append(trials.get(i));
 				}
 				intent.putExtra("value", sb.toString());
 				setResult(RESULT_OK, intent);
 				finish();
 				break;
 			default:
 				assert (false);
 			}
 		}
 	}
 }
