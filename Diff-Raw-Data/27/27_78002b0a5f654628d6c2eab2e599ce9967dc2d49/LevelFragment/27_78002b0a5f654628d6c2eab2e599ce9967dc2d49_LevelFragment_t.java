 package com.quizz.places.fragments;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Html;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
 import com.actionbarsherlock.internal.nineoldandroids.animation.Animator.AnimatorListener;
 import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
 import com.quizz.core.activities.BaseQuizzActivity;
 import com.quizz.core.fragments.BaseGridLevelsFragment;
 import com.quizz.core.fragments.BaseLevelFragment;
 import com.quizz.core.imageloader.ImageLoader;
 import com.quizz.core.imageloader.ImageLoader.ImageType;
 import com.quizz.core.interfaces.FragmentContainer;
 import com.quizz.core.managers.DataManager;
 import com.quizz.core.models.Badge;
 import com.quizz.core.models.Level;
 import com.quizz.core.models.Section;
 import com.quizz.core.utils.NavigationUtils;
 import com.quizz.core.utils.PreferencesUtils;
 import com.quizz.core.utils.StringUtils;
 import com.quizz.core.widgets.QuizzActionBar;
 import com.quizz.places.R;
 import com.quizz.places.activities.PictureFullscreenActivity;
 import com.quizz.places.application.QuizzPlacesApplication;
 import com.quizz.places.dialogs.HintsDialog;
 import com.quizz.places.dialogs.LevelSuccessDialog;
 
 public class LevelFragment extends BaseLevelFragment {
 	public static final int LEVEL_SUCCESS_REQUEST_CODE = 1;
 	private static final int GREEN_LETTER = 0xff34C924;
 	
 	private static final String STATE_CURRENT_LEVEL = "LevelFragment.STATE_CURRENT_LEVEL";
 	
 	private ImageView mPictureBig;
 	private ImageButton mInfoButton;
 	private ImageButton mHintLettersButton;
 	private TextView mLevelTitle;
 	private Button mCheckButton;
 	private EditText mInputText;
 
 	private TextView mHintsNbView;
 	private TextView mLevelCompletedLabel;
 	
 	private Level mCurrentLevel;
 	private StringBuilder mPartialResponse;
 	private ImageLoader mImageLoader;
 	
 	private ImageButton mPreviousButton;
 	private ImageButton mNextButton;
 	private ImageButton mPictureGridButton;
 	private TextView mCurrentLevelNumber;
 	
 	//private TableLayout mLettersTableLayout;
 	private boolean mBackFromPicturesGrid = false;
 	
 	private MediaPlayer mSuccessPlayer;
 	private Toast mNotifToast;
 	private Toast mInfoToast;
 
 	private int mLettersTotal;
 	private int mLettersFoundNb;
 	private LetterState[] mLetterStateArray;
 	
 	private enum LetterState {
 		FOUND, NOT_FOUND, NOT_ANSWERED, GIVEN, UNLOCKED, NOT_LETTER
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 
 		View view = inflater.inflate(R.layout.fragment_level, container, false);
 		mPictureBig = (ImageView) view.findViewById(R.id.levelPictureBig);
 		mInfoButton = (ImageButton) view.findViewById(R.id.levelInfo);
 		mHintLettersButton = (ImageButton) view.findViewById(R.id.levelHintLetters);
 		mLevelTitle = (TextView) view.findViewById(R.id.levelName);
 		mCheckButton = (Button) view.findViewById(R.id.levelCheckButton);
 		mInputText = (EditText) view.findViewById(R.id.levelInputResponse);
 		mLevelCompletedLabel = (TextView) view.findViewById(R.id.levelPictureFoundLabel);
 		//mLettersTableLayout = (TableLayout) view.findViewById(R.id.tableLetters);
 		mHintsNbView = (TextView) view.findViewById(R.id.levelNbHints);
 		
 		// Init actionBar
 		QuizzActionBar actionBar = ((BaseQuizzActivity) getActivity()).getQuizzActionBar();
 		actionBar.setCustomView(R.layout.ab_view_level);
 
 		View customView = actionBar.getCustomViewContainer();
 		mPreviousButton = (ImageButton) customView.findViewById(R.id.previous_level_button);
 		mNextButton = (ImageButton) customView.findViewById(R.id.next_level_button);
 		mPictureGridButton = (ImageButton) customView.findViewById(R.id.grid_pictures_button);
 		mCurrentLevelNumber = (TextView) customView.findViewById(R.id.current_level_number);
 		
 		mPreviousButton.setOnClickListener(mOnPreviousButtonClickListener);
 		mNextButton.setOnClickListener(mOnNextButtonClickListener);
 		mPictureGridButton.setOnClickListener(mOnPictureGridButtonClickListener);
 		
 		mImageLoader = new ImageLoader(getActivity());
 		mCheckButton.setOnClickListener(mCheckButtonClickListener);
 		mInfoButton.setOnClickListener(mInfoButtonClickListener);
 		mHintLettersButton.setOnClickListener(mHintLettersClickListener);
 		mLevelTitle.setOnClickListener(mLevelTitleClickListener);
 		
 		// Load special font
 		Typeface face = Typeface.createFromAsset(getActivity().getAssets(),
 				"fonts/OpenSans-CondBold.ttf");
 		mLevelTitle.setTypeface(face);
 
 		// get number of hints the user can use
 		mHintsNbView.setText(String.valueOf(PreferencesUtils.getHintsAvailable(getActivity())));
 		mInputText.setText("");
 		
 		Level level;
 		if (mBackFromPicturesGrid) {
 			level = mCurrentLevel;
 			mBackFromPicturesGrid = false;
 		} else {
 			if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_LEVEL)) {
 				level = savedInstanceState.getParcelable(STATE_CURRENT_LEVEL);
 			} else {
 				level = getArguments().getParcelable(ARG_LEVEL);
 			}
 		}
 
 		View notificationLayout = getActivity().getLayoutInflater().inflate(
 				R.layout.toast_notification, null);
 		mInfoToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
 		mNotifToast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);
 		float density = getActivity().getResources().getDisplayMetrics().density;
 		mNotifToast.setGravity(Gravity.BOTTOM, 0, (int) (40 * density));
 		mNotifToast.setView(notificationLayout);
 		
 		initLayout(level);
 		initSounds();
 		
 		int screenHeight = getActivity().getResources().getDisplayMetrics().heightPixels;
 		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 			mPictureBig.setMaxHeight((int) (screenHeight / 2.5f));
 		} else {
 			mPictureBig.setMaxHeight((int) (screenHeight / 1.5f));
 		}
 		mPictureBig.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getActivity(), PictureFullscreenActivity.class);
 				intent.putExtra(PictureFullscreenActivity.EXTRA_LEVEL, mCurrentLevel);
 				startActivity(intent);
 			}
 		});
 		
 		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
 			      Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(mInputText.getWindowToken(), 0);
 		
 		return view;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (mHintsNbView != null) {
 			// Refresh after spending hints inside HintsDialog
 			mHintsNbView.setText(String.valueOf(PreferencesUtils.getHintsAvailable(getActivity())));
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putParcelable(STATE_CURRENT_LEVEL, mCurrentLevel);
 		super.onSaveInstanceState(outState);
 	}
 	
 	/**
 	 * This method is used when fragment is already added in backstack
 	 * but we need to change the level argument from the pictures grid
 	 */
 	public void overrideCurrentLevelArgument(Level level) {
 		mCurrentLevel = level;
 		saveCurrentLevelAsLastPlayerLevel();
 	}
 	
 	private void saveCurrentLevelAsLastPlayerLevel() {
 		// Store this new level as the new last level played
 		PreferencesUtils.setLastPlayedLevel(getActivity(), 
 				mCurrentLevel.sectionId, mCurrentLevel.id);
 	}
 	
 	public void initSounds() {
 		AssetFileDescriptor afd;
 		try {
 			afd = getActivity().getAssets().openFd("sounds/success.wav");
 			mSuccessPlayer = new MediaPlayer();
 			mSuccessPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
 			mSuccessPlayer.prepare();
 		} catch (IOException e) {
 			Log.d("IOException : ", e.getMessage());
 		}
 	}
 	
 	public void playSuccessSound() {
 		if (mSuccessPlayer != null)
 			mSuccessPlayer.start();
 	}
 
 	private boolean isLetter(char c) {
 		// uppercase, lowercase and digits are considered as guessable letters
 		if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122) || (c >= 48 && c <= 57)) {
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean isLowercase(char c) {
 		if (c >= 97 && c <= 122) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Load, rotate picture, fill data
 	 * 
 	 * @param level
 	 */
 	private void initLayout(Level level) {
 		if (level == null) {
 			return;
 		}
 		mCurrentLevel = level;
 		mImageLoader.displayImage(QuizzPlacesApplication.IMAGES_DIR + mCurrentLevel.imageName, 
 				mPictureBig, ImageType.MEDIUM);
 		
 		Section section = DataManager.getSection(mCurrentLevel.sectionId);
		int levelNumber = section.levels.indexOf(mCurrentLevel) + 1;
		mCurrentLevelNumber.setText(""+levelNumber);
 		
 		if (mCurrentLevel.status == Level.STATUS_LEVEL_CLEAR) {
 			mLevelTitle.setText(mCurrentLevel.response);
 			mInputText.setVisibility(View.GONE);
 			mCheckButton.setVisibility(View.GONE);
 			mLevelCompletedLabel.setVisibility(View.VISIBLE);
 			mHintLettersButton.setVisibility(View.GONE);
 		} else {
 			// Init partial response
 			int totalLetters = mCurrentLevel.response.length();
 			mPartialResponse = new StringBuilder(totalLetters);
 			// Init letters state
 			mLetterStateArray = new LetterState[totalLetters];
 			
 			// we offer the first letter
 			if (mCurrentLevel.response != null && mCurrentLevel.response.length() > 0) {
 				mPartialResponse.append(StringUtils.removeDiacritic(mCurrentLevel.response.charAt(0)));
 				mLetterStateArray[0] = LetterState.GIVEN;
 				mLettersFoundNb = 1;
 				mLettersTotal = 1;
 			} else {
 				mLettersFoundNb = 0;
 				mLettersTotal = 0;
 			}
 			
 			// Get previously unlocked letters
 			String unlockedLetters = PreferencesUtils.getUnlockedLetters(getActivity(), mCurrentLevel);
 			
 			char currentChar;
 			for (int i = mLettersTotal; i < totalLetters; i++) {
 				// important, remove accent before checking if a letter is valid
 				currentChar = StringUtils.removeDiacritic(mCurrentLevel.response.charAt(i));
 				
 				// We stored unlocked letters as '1' and locked ones as '0' 
 				if (i < unlockedLetters.length() && unlockedLetters.charAt(i) == '1') {
 					if (isLowercase(currentChar)) {
 						currentChar -= 32;
 					}
 					mPartialResponse.append(currentChar);
 					mLetterStateArray[i] = LetterState.UNLOCKED;
 				} else if (isLetter(currentChar)) {
 					mPartialResponse.append('_');
 					mLetterStateArray[i] = LetterState.NOT_ANSWERED;
 					mLettersTotal++;
 				} else {
 					mPartialResponse.append(currentChar);
 					mLetterStateArray[i] = LetterState.NOT_LETTER;
 				}
 			}
 			
 			// For the unlocked letters
 			displayColoredPartialResponse();
 			
 			mInputText.setVisibility(View.VISIBLE);
 			mCheckButton.setVisibility(View.VISIBLE);
 			mLevelCompletedLabel.setVisibility(View.GONE);
 			mHintLettersButton.setVisibility(View.VISIBLE);
 			
 			/*
 			 * Don't limit response length..
 			 * 
 			InputFilter[] FilterArray = new InputFilter[1];
 			FilterArray[0] = new InputFilter.LengthFilter(mCurrentLevel.response.length());
 			mInputText.setFilters(FilterArray);
 */
 			
 			/* ADD INPUT CELLS */
 			/*
 			Display display = getActivity().getWindowManager().getDefaultDisplay();
 		    DisplayMetrics outMetrics = new DisplayMetrics();
 		    display.getMetrics(outMetrics);
 
 		    float density  = getResources().getDisplayMetrics().density;
 		    float screenDpWidth  = outMetrics.widthPixels / density;
 			
 		    // TODO: Get from dimens.xml
 		    int cellWidth = 32;
 
 		    int currentRowWidth = 0;
 		    
 			mLettersTableLayout.removeAllViews();
 			TableRow currentRow = new TableRow(getActivity());
 			LayoutParams params = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 			currentRow.setLayoutParams(params);
 			currentRow.setGravity(Gravity.CENTER_HORIZONTAL);
 			mLettersTableLayout.addView(currentRow);
 			for (Character letter : level.response.toCharArray()) {
 				
 				// TODO: TAKE WORD IN ACCOUNT
 				if (currentRowWidth + cellWidth > screenDpWidth) {
 					currentRow = new TableRow(getActivity());
 					currentRow.setLayoutParams(params);
 					currentRow.setGravity(Gravity.CENTER_HORIZONTAL);
 					mLettersTableLayout.addView(currentRow);
 					
 					currentRowWidth = 0;
 				}
 				
 				TextView letterTextView = (TextView) getActivity().getLayoutInflater().inflate(R.layout.item_table_letters, null);
 				letterTextView.setText(""+letter);
 				currentRow.addView(letterTextView);
 				
 				currentRowWidth += cellWidth;
 			}*/
 		}
 	}
 	
 	private void displayColoredPartialResponse() {
 		mLettersFoundNb = mLettersTotal;
 		
 		// We now apply the colors
 		SpannableString coloredUserResponse = new SpannableString(mPartialResponse);
 		for (int i = 0; i < coloredUserResponse.length(); i++) {
 			// We color only the '_' characters of the base partial response
 			if (mLetterStateArray[i] == LetterState.FOUND
 					|| mLetterStateArray[i] == LetterState.NOT_FOUND) {
 				// Remove accents
 				char userLetter = coloredUserResponse.charAt(i);
 				char responseLetter = Character.toUpperCase(
 						StringUtils.removeDiacritic(mCurrentLevel.response.charAt(i)));
 				
 				// Check validity of the character
 				boolean isValid = (userLetter == responseLetter);
 				int color = (isValid) ? GREEN_LETTER : Color.RED;
 				mLetterStateArray[i] = (isValid) ? LetterState.FOUND : LetterState.NOT_FOUND;
 				coloredUserResponse.setSpan(new ForegroundColorSpan(color), i, i + 1, 0);
 				
 				if (!isValid) {
 					mLettersFoundNb--;
 				}
 			} else if (mLetterStateArray[i] == LetterState.UNLOCKED) {
 				// UNLOCKED means revealed by a hint
 				int color = Color.YELLOW;
 				coloredUserResponse.setSpan(new ForegroundColorSpan(color), i, i + 1, 0);
 			} else if (mLetterStateArray[i] == LetterState.NOT_ANSWERED) {
 				mLettersFoundNb--;
 			}
 		}
 		
 		mLevelTitle.setText(coloredUserResponse);		
 		
 		Log.e("level", "LETTERS FOUND: "+mLettersFoundNb+", LETTERS TOTAL: "+mLettersTotal);
 		
 		// if we didn't find any error, display success dialog
 		if (mLettersFoundNb == mLettersTotal) {
 			onSuccess();
 		} else {
 			onError(mLettersTotal - mLettersFoundNb);
 		}
 	}
 	
 	// TODO: Move to BaseLevelFragment
 	private void checkResponse(String inputContent) {
 		// Normalize and uppercase: Colise => COLISEE
 		inputContent = StringUtils.removeDiacritic(
 				inputContent.trim().replaceAll(" +", " ")).toUpperCase(Locale.getDefault());
 		
 		// We will replace all letters from partial response to the corresponding
 		// input response characters, except GIVEN and UNLOCKED letters
 		for (int i = 0; i < inputContent.length(); i++) {
 			// Assure the response entered isn't (for any reason) longer than
 			// the current partial response
 			if (i >= mPartialResponse.length()) {
 				break;
 			}
 
 			// We leave alone 'not_letters', 'unlocked' and 'given' characters
 			if (mLetterStateArray[i] == LetterState.FOUND
 					|| mLetterStateArray[i] == LetterState.NOT_FOUND
 					|| mLetterStateArray[i] == LetterState.NOT_ANSWERED) {
 				
 				mPartialResponse.setCharAt(i, inputContent.charAt(i));
 				mLetterStateArray[i] = LetterState.NOT_FOUND;
 			}
 		}
 
 		displayColoredPartialResponse();
 	}
 	
 	private void onSuccess() {
 		int newHintsAvailableNb = PreferencesUtils.getHintsAvailable(getActivity()) + 
 				PreferencesUtils.PREF_DEFAULT_NB_HINTS_ONSUCCESS_VALUE;
 		PreferencesUtils.setHintsAvailable(getActivity(), newHintsAvailableNb);
 		
 		// update actionBar hints number
 		mHintsNbView.setText(String.valueOf(newHintsAvailableNb));
 		
 		mCurrentLevel.status = Level.STATUS_LEVEL_CLEAR;
 		mCurrentLevel.update();
 		
 		mLevelTitle.setText(mCurrentLevel.response);
 		mInputText.setText("");
 		mHintLettersButton.setVisibility(View.GONE);
 		mInputText.setVisibility(View.GONE);
 		mCheckButton.setVisibility(View.GONE);
 		mLevelCompletedLabel.setVisibility(View.VISIBLE);
 
 		// Unlock next section if necessary
 		boolean sectionUnlocked = false;
 		int unlockedSectionNumber = DataManager.unlockNextSectionIfNecessary();
 		if (unlockedSectionNumber != -1) {
 			sectionUnlocked = true;
 			
 			TextView content = (TextView) mNotifToast.getView().findViewById(R.id.toastText);
 			ImageView icon = (ImageView) mNotifToast.getView().findViewById(R.id.toastImage);
 			icon.setImageResource(R.drawable.unlock);
 			String sectionNumber = String.valueOf(unlockedSectionNumber);
 			content.setText(Html.fromHtml(String.format(getString(
 					R.string.level_unlock_section, sectionNumber))));
 			mNotifToast.show();
 		}
 		
 		PreferencesUtils.removeUnlockedLetters(getActivity(), mCurrentLevel);
 
 		int totalCleared = DataManager.getClearedLevelTotalCount();
 		Badge currentBadge = StatsFragment.getCurrentBadge();
 		if (currentBadge.requiredLevelProgression == totalCleared) {
 			Toast toast;
 			// In order to display 2 toasts (section then badge), one after the other
 			if (sectionUnlocked) {
 				View notificationLayout = getActivity().getLayoutInflater().inflate(
 						R.layout.toast_notification, null);
 				toast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);
 				float density = getActivity().getResources().getDisplayMetrics().density;
 				toast.setGravity(Gravity.BOTTOM, 0, (int) (40 * density));
 				toast.setView(notificationLayout);
 			} else {
 				toast = mNotifToast;
 			}
 			
 			TextView content = (TextView) toast.getView().findViewById(R.id.toastText);
 			ImageView icon = (ImageView) toast.getView().findViewById(R.id.toastImage);
 			icon.setImageResource(currentBadge.icon);
 			content.setText(Html.fromHtml(String.format(getString(R.string.level_unlock_badge, 
 					getString(currentBadge.label)))));
 			toast.show();
 		}
 		/*
 		 * Not useful
 		 * 
 		if (DataManager.getSection(mCurrentLevel.sectionId).isComplete()) {
 			PreferencesUtils.removeLastPlayedLevel(getActivity(), mCurrentLevel.sectionId);
 		}*/
 		
 		// Run success vibrations
 		if (PreferencesUtils.isVibrationEnabled(this.getActivity())) {
 			this.runSuccessVibration();
 		}
 		
 		if (PreferencesUtils.isAudioEnabled(this.getActivity())) {
 			this.playSuccessSound();
 		}
 		
 		// Launching LevelSuccessDialog
 		Intent intent = new Intent(getActivity(), LevelSuccessDialog.class);
 		intent.putExtra(LevelSuccessDialog.EXTRA_LEVEL, mCurrentLevel);
 		startActivityForResult(intent, LEVEL_SUCCESS_REQUEST_CODE);
 	}
 	
 	public void runSuccessVibration() {
 		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
 		vibrator.vibrate(300);
 	}
 	
 	private void onError(int errorsCount) {
 		
 	}
 	
 	private void onNextLevel(boolean skipClosedLevels) {
 		// Fade out current level, load next level when animation end
 		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(getView(), "alpha", 1f, 0f);
 		alphaAnimator.setDuration(200);
 		alphaAnimator.addListener((skipClosedLevels) ? mStartLoadNextOpenLevelAnimatorListener :
 			mStartLoadNextLevelAnimatorListener);			
 		alphaAnimator.start();
 	}
 	
 	private void onPreviousLevel() {
 		// Fade out current level, load next level when animation end
 		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(getView(), "alpha", 1f, 0f);
 		alphaAnimator.setDuration(200);
 		alphaAnimator.addListener(mStartLoadPreviousLevelAnimatorListener);			
 		alphaAnimator.start();
 	}
 	
 	private void displayNewLevel(Level newLevel) {
 		if (newLevel != null) {
 			initLayout(newLevel);
 			saveCurrentLevelAsLastPlayerLevel();
 		}
 		
 		// Fade in new level
 		if (getView() != null) {
 			ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(getView(), "alpha", 0f, 1f);
 			alphaAnimator.setDuration(200);
 			alphaAnimator.start();
 		}
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (requestCode == LEVEL_SUCCESS_REQUEST_CODE) {
 			switch (resultCode) {
 			
 				case LevelSuccessDialog.RESULT_CODE_NEXT:
 					onNextLevel(true);
 					break;
 					
 				case LevelSuccessDialog.RESULT_CODE_BACK:
 					getActivity().onBackPressed();
 					break;
 			}
 		}
 	}
 	
 	private void addLetters() {
 		// LettersTotal just includes ... letters
 		int lettersToUnlock = /*mLettersTotal / 3*/ 1;
 		
 		// If we know that using a hint will reveal the answer..
 		if (lettersToUnlock > (mLettersTotal - mLettersFoundNb)) {
 			onSuccess();
 		} else {
 			ArrayList<Integer> notFoundPositions = new ArrayList<Integer>();
 			for (int i = 0; i < mLetterStateArray.length; i++) {
 				if (mLetterStateArray[i] == LetterState.NOT_FOUND
 						|| mLetterStateArray[i] == LetterState.NOT_ANSWERED) {
 					notFoundPositions.add(i);
 				}
 			}
 			
 			// just for safety..
 			if (lettersToUnlock > notFoundPositions.size()
 					|| notFoundPositions.size() == 0) {
 				onSuccess();
 			}
 
 			Random randomGenerator = new Random();
 			for (int i = lettersToUnlock; i > 0; i--) {
 				int randomPosition = randomGenerator.nextInt(notFoundPositions.size());
 				int letterPosition = notFoundPositions.get(randomPosition);
 				
 				mLetterStateArray[letterPosition] = LetterState.UNLOCKED;
 				// set letter in response, in uppercase
 				char unlockedLetter = StringUtils.removeDiacritic(
 						mCurrentLevel.response.charAt(letterPosition));
 				if (isLowercase(unlockedLetter)) {
 					unlockedLetter -= 32;
 				}
 				mPartialResponse.setCharAt(letterPosition, unlockedLetter);
 				mLettersFoundNb++;
 				notFoundPositions.remove(randomPosition);
 			}
 			displayColoredPartialResponse();
 			
 			StringBuilder unlockedLetters = new StringBuilder(mLetterStateArray.length);
 			for (int i = 0; i < unlockedLetters.capacity(); i++) {
 				unlockedLetters.append((mLetterStateArray[i] == LetterState.UNLOCKED) ? '1' : '0');
 			}
 			PreferencesUtils.setUnlockedLetters(getActivity(), mCurrentLevel, unlockedLetters.toString());
 		}
 	}
 	
 	private void useHintLetters() {
 		int hints = PreferencesUtils.getHintsAvailable(getActivity());
 		if (hints > 0) {
 			hints--;
 			// update action bar and shared preferences
 			mHintsNbView.setText(String.valueOf(hints));
 			PreferencesUtils.setHintsAvailable(getActivity(), hints);
 			PreferencesUtils.incrementUsedHintsCount(getActivity(), 1);
 			// Add letters to partial response
 			addLetters();
 		} else {
 			mInfoToast.setText(R.string.level_not_enough_hints);
 			mInfoToast.show();
 		}
 	}
 	
 	// ===========================================================
 	// Listeners
 	// ===========================================================
 
 	OnClickListener mLevelTitleClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// focus response input
 			if (mCurrentLevel != null 
 					&& mCurrentLevel.status == Level.STATUS_LEVEL_UNCLEAR) {
 				mInputText.requestFocus();
 				InputMethodManager imm = (InputMethodManager) getActivity()
 						.getSystemService(Context.INPUT_METHOD_SERVICE);
 				if (imm != null) {
					imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
 				}
 			}
 		}
 	};
 	
 	OnClickListener mCheckButtonClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			checkResponse(mInputText.getText().toString());
 		}
 	};
 
 	OnClickListener mInfoButtonClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent(getActivity(), HintsDialog.class);
 			intent.putExtra(HintsDialog.EXTRA_LEVEL, mCurrentLevel);
 			startActivity(intent);
 		}
 	};
 	
 	OnClickListener mHintLettersClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			useHintLetters();
 		}
 	};
 	
 	AnimatorListener mStartLoadPreviousLevelAnimatorListener = new AnimatorListener() {
 		
 		@Override
 		public void onAnimationStart(Animator animation) {
 		}
 		
 		@Override
 		public void onAnimationRepeat(Animator animation) {
 		}
 		
 		@Override
 		public void onAnimationEnd(Animator animation) {
 			displayNewLevel(DataManager.getPreviousLevel(mCurrentLevel));
 		}
 		
 		@Override
 		public void onAnimationCancel(Animator animation) {
 		}
 	};
 	
 	AnimatorListener mStartLoadNextOpenLevelAnimatorListener = new AnimatorListener() {
 		
 		@Override
 		public void onAnimationStart(Animator animation) {
 		}
 		
 		@Override
 		public void onAnimationRepeat(Animator animation) {
 		}
 		
 		@Override
 		public void onAnimationEnd(Animator animation) {
 			displayNewLevel(DataManager.getNextOpenedLevelInSection(mCurrentLevel));
 		}
 		
 		@Override
 		public void onAnimationCancel(Animator animation) {
 		}
 	};
 	
 	AnimatorListener mStartLoadNextLevelAnimatorListener = new AnimatorListener() {
 		
 		@Override
 		public void onAnimationStart(Animator animation) {
 		}
 		
 		@Override
 		public void onAnimationRepeat(Animator animation) {
 		}
 		
 		@Override
 		public void onAnimationEnd(Animator animation) {
 			displayNewLevel(DataManager.getNextLevel(mCurrentLevel));
 		}
 		
 		@Override
 		public void onAnimationCancel(Animator animation) {
 		}
 	};
 	
 	OnClickListener mOnNextButtonClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			onNextLevel(false);
 		}
 	};
 	
 
 	OnClickListener mOnPreviousButtonClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			onPreviousLevel();
 		}
 	};
 	
 	OnClickListener mOnPictureGridButtonClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			// We set a flag indicating that on the next onCreateView, we'll be back from the 
 			// pictures grid
 			mBackFromPicturesGrid = true;
 			
 			FragmentContainer container = (FragmentContainer) getActivity();
 			FragmentManager fragmentManager = getActivity()
 					.getSupportFragmentManager();
 
 			FragmentTransaction transaction = fragmentManager
 					.beginTransaction();
 			transaction.setCustomAnimations(R.anim.slide_in_right,
 					R.anim.slide_out_left, R.anim.slide_in_left,
 					R.anim.slide_out_right);
 			
 			Bundle args = new Bundle();
 			args.putParcelable(BaseGridLevelsFragment.ARG_SECTION,
 					DataManager.getSection(mCurrentLevel.sectionId));
 			NavigationUtils.directNavigationTo(GridLevelsFragment.class,
 					fragmentManager, container, true, transaction, args);
 		}
 	};
 }
