 package com.quizmania.fragments;
 
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.quizmania.activities.ItemStore;
 import com.quizmania.entities.NameHints;
 import com.quizmania.entities.QuizElement;
 import com.quizmania.fruits.R;
 import com.quizmania.utils.AnswerService;
 import com.quizmania.utils.HintStyle;
 import com.quizmania.utils.QuizElementUtil;
 import com.quizmania.utils.StaticGlobalVariables;
 import com.quizmania.utils.UserConfig;
 import com.quizmania.utils.ViewUtils;
 
 public class QuizMainFragment extends Fragment implements OnKeyListener, OnClickListener, OnTouchListener {
 
 	QuizElement element;
 	@Override
 	public String toString() {
 		return "QuizMainFragment [element=" + element + ", thisView="
 				+ thisView + ", hintButton=" + hintButton + "]";
 	}
 
 
 
 	View thisView;
 	Button hintButton;
 	public static final int NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN = 8;
 	public QuizElement getElement() {
 		return element;
 	}
 
 
 
 	public void setElement(QuizElement element) {
 		this.element = element;
 	}
 
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState){
 		
 		View quizElementView = constructViewFromQuizElement(inflater, container);
 		hintButton = (Button) thisView.findViewById(R.id.hintButton);
 		hintButton.setVisibility(View.VISIBLE);
 		setEventListeners(quizElementView);
 		drawAnswerIconIfAnswered();
         return quizElementView;
 
 	}
 
 
 
 	private void drawAnswerIconIfAnswered() {
 		boolean isCorrectAnswered = AnswerService.getAnswerService().isAwnsered(element, UserConfig.getInstance().getLanguage());
 		if(isCorrectAnswered){
 			showCorrectAnsweredIcon();
 		}else{
 			hideAnswerIcon();
 		}
 		
 	}
 
 
 	@Override
 	public void onResume(){
 		super.onResume();
 		if(!AnswerService.getAnswerService().isAwnsered(element, UserConfig.getInstance().getLanguage())){
 			hideAnswerIcon();
 		}
 	}
 
 
 	private void hideAnswerIcon() {
 		ImageView answerIcon = (ImageView) thisView.findViewById(R.id.answerIcon);
 		answerIcon.setVisibility(View.GONE);  
 
 		
 	}
 
 
 
 	private void setEventListeners(View quizElementView) {
 		addAnswerTextboxEvents();
 		addAnswerButtonEvents();
 		addElementImageEvents();
 		addHintButtonEvents();
 		
 	}
 
 
 
 	private void addHintButtonEvents() {
 		hintButton.setOnClickListener(null);
 		hintButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Log.d("*******", "hintCLICK!!!!!!!!!!!!" + this.hashCode());
 				if(UserConfig.getInstance().getHintsLeft() > 0){
 					Log.d("*******", "element: " + element.getLanguageToNamesMap().get(UserConfig.getInstance().getLanguage()).getNames().get(0));
 					Log.d("*******", "hintsLeft = "  + UserConfig.getInstance().getHintsLeft());
 					AnswerService.getAnswerService().revealRandomLetter(element, getActivity());					
 					drawHintLetters();				
 				}else{
 					Intent intent = new Intent(getActivity(),ItemStore.class);
 					getActivity().startActivity(intent);
 				}
 				
 			}
 			
 		});
 		
 	}
 
 
 
 	private void addElementImageEvents() {
 		ImageView imageView = getElementImageView();
 		imageView.setOnTouchListener(this);
 	}
 
 
 
 	private void addAnswerButtonEvents() {
 		Button answerButton =  (Button) thisView.findViewById(R.id.answerButton);
 		answerButton.setOnClickListener(this);
 	}
 
 
 
 	private void addAnswerTextboxEvents() {
 		TextView answerTextbox = (TextView) thisView.findViewById(R.id.answerTextbox);
 		answerTextbox.setOnKeyListener(this);
 	}
 
 
 
 	private ImageView getElementImageView() {
 		ImageView imageView =  (ImageView) thisView.findViewById(R.id.elementImage);
 		return imageView;
 	}
 
 
 	private View constructViewFromQuizElement(LayoutInflater inflater,
 			ViewGroup container) {
 		View quizElementView = inflater.inflate(R.layout.fragment_quiz_main, container, false);
 		thisView = quizElementView;
 		drawElementImage(quizElementView);
 		drawHintLetters();
 		
 		return quizElementView;
 	}
 
 
 
 	private void drawHintLetters() {
 				
 		
 		char[] elementNameLetters = element.getLanguageToNamesMap().get(UserConfig.getInstance().getLanguage()).getNames().get(0).toCharArray();		 		
 		int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
 		int hintLetterWidth = screenWidth/NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN;
 		int hintLetterHeight = hintLetterWidth;		
 		HintStyle hintStlye = new HintStyle(getActivity(), hintLetterWidth,hintLetterHeight);
 		
 		LinearLayout hintHolder = (LinearLayout) thisView.findViewById(R.id.centralizedHintHolder);
 		NameHints elementsRevealedHints = AnswerService.getAnswerService().getRevealedHintsForQuizElement(element);
 		Log.d("********", "hintParaDibujar: " + elementsRevealedHints);
 		drawLineOfHints(hintHolder,elementNameLetters,elementsRevealedHints,0,hintStlye);
 		if(elementNameLetters.length  > NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN){
 			hintHolder = (LinearLayout) thisView.findViewById(R.id.secondLineHintHolder);
 			drawLineOfHints(hintHolder,elementNameLetters,elementsRevealedHints,NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN,hintStlye);
 		}
 			
 		
 		//addLettersInOtherLines(elementNameLetters,numberOfLettersThatFitInTheFirstLine,hintStlye);
 		
 		
 	}
 
 
 
 	private void drawLineOfHints(LinearLayout hintContainer, char[] elementNameLetters,NameHints elementsRevealedHints, int offset, HintStyle hintStlye) {
 		hintContainer.removeAllViews();
 		LayoutParams layoutParams = new LayoutParams(hintStlye.getWidth(),hintStlye.getHeight());
 		
 		for(int i=0 + offset; i < elementNameLetters.length; i++){
 			if(i >= offset + NUMBER_OF_HINTS_THAT_FIT_IN_SCREEN)
 				break;
 			char letter = elementNameLetters[i];			
 			TextView hintLetter = ViewUtils.createHintLetter(getActivity());
 			
 			hintLetter.setLayoutParams(layoutParams);
 			if(elementsRevealedHints != null && elementsRevealedHints.hasLetterRevealed(i)){
 				String letterAsString = "" + letter;								
 				hintLetter.setText(letterAsString.toUpperCase());
 			}
 			if(letter == StaticGlobalVariables.BLANK_SPACE){
 				hintLetter.setBackgroundResource(0);
 			}
 			
 			hintContainer.addView(hintLetter);
 		}
 	}
 
 
 
 	/*private void addLettersInOtherLines(char[] elementNameLetters,HintStyle hintStlye) {
 		LinearLayout multipleLineHintHolder = (LinearLayout) thisView.findViewById(R.id.multipleLineHintHolder);
 		HintLetterAdapter adapter = new HintLetterAdapter(getActivity(),elementNameLetters,numberOfLettersThatFitInTheFirstLine,hintStlye);
 		multipleLineHintHolder.setAdapter(adapter);
 		
 		
 	}*/
 
 
 
 	private void drawElementImage(View quizElementView) {
 		
 		int resID = QuizElementUtil.getResourceIdFromQuizElement(this, element);
 		ImageView quizElementImage =  getElementImageView();
 		quizElementImage.setImageResource(resID);
 	}
 
 
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if(keyCode != KeyEvent.KEYCODE_ENTER)
 			return false;
 				
 		tryToAnswer();
 		return false;
 	}
 
 
 
 	public void tryToAnswer() {
 		TextView textView = (TextView) thisView.findViewById(R.id.answerTextbox);
 		
 		boolean correctAnswer = AnswerService.getAnswerService().tryToAnswer(element, textView.getText().toString());
 		
 		if(correctAnswer){
 			placeCorrectIcon();			
 			AnswerService.getAnswerService().saveToSDCard(getActivity());
 			
 		}else{
 			placeIncorrectIcon();
 		}
 		Log.d("QuizMa/inFragment", "Fue correcta la respuesta: " + correctAnswer);
 	}
 
 
 
 	private void placeIncorrectIcon() {
 
 		showIncorrectImage();
 		playIncorrectSound();
 		vibrate();
 		
 	}
 	
 
 
 
 	private void vibrate() {
 		if(UserConfig.getInstance().isVibrationActivated()){
 			Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);		 		
 			v.vibrate(500);
 		}
 		
 	}
 
 
 
 	private void playIncorrectSound() {
 		if(UserConfig.getInstance().isSoundActivated()){
 			MediaPlayer playerMedia = MediaPlayer.create(getActivity(), R.raw.fail);
 			playerMedia.start();
 		}
 	
 		
 	}
 
 
 
 	private void showIncorrectImage() {
 		ImageView answerIcon =  (ImageView) thisView.findViewById(R.id.answerIcon);
 		answerIcon.setImageResource(R.drawable.incorrect);
 		answerIcon.setVisibility(View.VISIBLE);
 		
 	}
 
 
 
 	private void placeCorrectIcon() {
 		showCorrectAnsweredIcon();
 		playCorrectSound();
 		
 	}
 
 
 
 	private void playCorrectSound() {
 		if(UserConfig.getInstance().isSoundActivated()){
 			MediaPlayer playerMedia = MediaPlayer.create(getActivity(), R.raw.correct);
 			playerMedia.start();	
 		}
 		
 		
 	}
 
 
 
 	private void showCorrectAnsweredIcon() {
 		
 		ImageView answerIcon = (ImageView) thisView.findViewById(R.id.answerIcon);
 		answerIcon.setImageResource(R.drawable.correct);
 		answerIcon.setVisibility(View.VISIBLE);
 		
 	}
 
 	
	public void navigateBack(View view) {
		getActivity().onBackPressed();
	}
 
 
 	@Override
 	public void onClick(View button) {		
 		tryToAnswer();
 	}
 
 
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent arg1) {
 		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
 			      Context.INPUT_METHOD_SERVICE);
 		EditText answerTextBox = (EditText) thisView.findViewById(R.id.answerTextbox);
 			imm.hideSoftInputFromWindow(answerTextBox.getWindowToken(), 0);
 		return true;
 		
 	}
 
 
 
 	
 }
