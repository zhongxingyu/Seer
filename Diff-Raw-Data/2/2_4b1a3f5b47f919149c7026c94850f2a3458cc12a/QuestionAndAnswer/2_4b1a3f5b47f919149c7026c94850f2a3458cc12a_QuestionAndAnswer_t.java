 package edu.bonn.mobilegaming.geoquest.mission;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.Attribute;
 import org.dom4j.Element;
 import org.dom4j.Node;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.qeevee.gq.xml.XMLUtilities;
 
 import edu.bonn.mobilegaming.geoquest.Globals;
 import edu.bonn.mobilegaming.geoquest.R;
 
 // TODO: test
 // TODO: fehlendes intro testen
 // TODO: fehlendes outro testen
 // TODO anzahl der fragen
 
 /**
  * A simple quiz mission. The player gets questions and have to select the right
  * answer. Multiple choice.
  * 
  * @deprecated Do use either MultipleChoiceQuestion or TextQuestion instead.
  * 
  * @author Krischan Udelhoven
  * @author Folker Hoffmann
  */
 public class QuestionAndAnswer extends InteractiveMission {
 	/** layout */
 	private LinearLayout ll;
 	/** text view for displaying text */
 	private TextView tv;
 	/** button for proceed to the next question / exit the mission */
 	private Button button;
 	/** list of all questions. Is filled in the onCreate method */
 	private List<Question> questions = new ArrayList<Question>();
 	/** iterator for iterating the questions */
 	private Iterator<Question> questionsIterator;
 	/** only true when there are no questions left */
 	private Boolean exit = false;
 	/** setuped by the onCreate method. Answers needed to get a result_OK code */
 	private int correctAnswersNeeded;
 	/** current correct answers */
 	private int correctAnswers = 0;
 	/** current incorrect answers */
 	private int incorrectAnswers = 0;
 
 	/** the intro text */
 	private String introText;
 	/** the outroSuccess text, on top of page */
 	private String outroSuccessText;
 	/** a short positive greeting at end of page */
 	/** the outroFail text, on top of page */
 	private String outroFailText;
 	/** a short negative greeting at end of page */
 
 	/**
 	 * tokens can be used in <intro> and <outro> tags, they will be replaced by
 	 * there value
 	 * 
 	 * TODO: comment on wiki page!
 	 */
 	final String TOKEN_CORRECT = "%correct";
 	final String TOKEN_INCORRECT = "%incorrect";
 	final String TOKEN_REQUIRED = "%required";
 	final String TOKEN_QUESTIONS = "%questions";
 
 	/**
 	 * The attribute shuffle can be omitted. If it's set the value must be "no",
 	 * "all", "questions" or "answers". Depending on the values, answers and/or
 	 * questions will be shuffled.
 	 */
 	public final static String SHUFFLE_NO = "no";
 	public final static String SHUFFLE_ALL = "all";
 	public final static String SHUFFLE_QUESTIONS = "questions";
 	public final static String SHUFFLE_ANSWERS = "answers";
 
 	String replaceTokens(String str) {
 		str = str.replaceAll(TOKEN_CORRECT, correctAnswers + "");
 		str = str.replaceAll(TOKEN_INCORRECT, incorrectAnswers + "");
 		str = str.replaceAll(TOKEN_REQUIRED, correctAnswersNeeded + "");
 		str = str.replaceAll(TOKEN_QUESTIONS, questions.size() + "");
 		return (str);
 	}
 
 	/**
 	 * called by the android framework when the mission is created. Reads the
 	 * xml file to get the questions and answers, the intro and exit text.
 	 * Starts the mission by showing the intro text and a button the player can
 	 * tap to start with the first question.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.questionandanswer);
 		initQuestions();
 		initCorrectAnswersNeeded();
 		shuffleQuestionAndAnswers();
 		initIntroAndOutro();
 		// Setup Answer List
 		ll = (LinearLayout) findViewById(R.id.answer_panel);
 		MyOnClickListener listener = setupButton();
 		setupTextView(listener);
 	}
 
 	private void initCorrectAnswersNeeded() {
 		CharSequence correctAnswersNeededAsText = getMissionAttribute(
 				"correctAnswersNeeded", XMLUtilities.OPTIONAL_ATTRIBUTE);
 		if (correctAnswersNeededAsText == null) {
 			correctAnswersNeeded = questions.size();
 		} else {
 			correctAnswersNeeded = Integer.valueOf(correctAnswersNeededAsText
 					.toString());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initQuestions() {
 		List<Element> xmlQuestions = mission.xmlMissionNode
 				.selectNodes("./question");
 		for (Iterator<Element> i = xmlQuestions.iterator(); i.hasNext();) {
 			Element xmlQuestion = i.next();
 			Question question = new Question();
 			question.question = xmlQuestion.selectSingleNode("./questiontext")
 					.getText().replaceAll("\\s+", " ");
 			List<Element> xmlAnswers = xmlQuestion.selectNodes("./answer");
 			for (Iterator<Element> j = xmlAnswers.iterator(); j.hasNext();) {
 				Element xmlAnswer = j.next();
 				Attribute correct = (Attribute) xmlAnswer
 						.selectSingleNode("./@correct");
 				Answer answer = new Answer();
 				if ((correct != null) && (correct.getText().equals("1")))
 					answer.correct = true;
 				answer.answertext = xmlAnswer.getText().replaceAll("\\s+", " ");
 
 				Attribute onChoose = ((Attribute) xmlAnswer
 						.selectSingleNode("./@onChoose"));
 				if (onChoose != null)
 					answer.onChoose = onChoose.getText()
 							.replaceAll("\\s+", " ");
 
 				question.answers.add(answer);
 			}
 			questions.add(question);
 		}
 		questionsIterator = questions.iterator();
 	}
 
 	private void setupTextView(MyOnClickListener listener) {
 		tv = (TextView) findViewById(R.id.questionView);
 		// when there is no <intro> tag: skip intro by simulating button click.
 		if (introText == null) {
 			listener.onClick(button);
 		} else {
 			tv.setText(replaceTokens(introText));
 		}
 	}
 
 	private MyOnClickListener setupButton() {
 		button = new Button(this);
		button.setText(getString(R.string.question_proceed_button));
 		button.setWidth(LayoutParams.FILL_PARENT);
 		MyOnClickListener listener = new MyOnClickListener();
 		button.setOnClickListener(listener);
 		ll.addView(button);
 		return listener;
 	}
 
 	private void shuffleQuestionAndAnswers() {
 		Node shuffle = mission.xmlMissionNode.selectSingleNode("./@shuffle");
 		String shuffleString = null;
 		if (shuffle != null) {
 			shuffleString = mission.xmlMissionNode
 					.selectSingleNode("./@shuffle").getText()
 					.replaceAll("\\s+", " ");
 		}
 		if (shuffleString != null && !shuffleString.equals(SHUFFLE_NO)) {
 			if (shuffleString.equals(SHUFFLE_ALL)) {
 				java.util.Collections.shuffle(questions);
 				for (Iterator<Question> it = questions.iterator(); it.hasNext();) {
 					Question question = (Question) it.next();
 					java.util.Collections.shuffle(question.answers);
 				}
 			} else {
 				if (shuffleString.equals(SHUFFLE_ANSWERS)) {
 					for (Iterator<Question> it = questions.iterator(); it
 							.hasNext();) {
 						Question question = (Question) it.next();
 						java.util.Collections.shuffle(question.answers);
 					}
 				} else {
 					if (shuffleString.equals(SHUFFLE_QUESTIONS)) {
 						java.util.Collections.shuffle(questions);
 					} else {
 						Log.w(this.getClass().getSimpleName(),
 								"xml attribute 'shuffle' has an incorrect content: "
 										+ shuffleString);
 					}
 				}
 			}
 		}
 	}
 
 	private void initIntroAndOutro() {
 		Node tempnode;
 		tempnode = mission.xmlMissionNode.selectSingleNode("intro");
 		if (tempnode != null)
 			introText = tempnode.getText().replaceAll("\\s+", " ");
 		tempnode = mission.xmlMissionNode.selectSingleNode("outroSuccess");
 		if (tempnode != null)
 			outroSuccessText = tempnode.getText().replaceAll("\\s+", " ");
 		tempnode = mission.xmlMissionNode.selectSingleNode("outroFail");
 		if (tempnode != null)
 			outroFailText = tempnode.getText().replaceAll("\\s+", " ");
 	}
 
 	/**
 	 * click handler. When the the last question is over the mission ends. When
 	 * there are still questions left the next question have to be shown.
 	 */
 	private class MyOnClickListener implements View.OnClickListener {
 		public void onClick(View view) {
 			if (exit) {
 				missionResultInPercent = (correctAnswers / questions.size()) * 100;
 				if (correctAnswers >= correctAnswersNeeded) {
 					finish(Globals.STATUS_SUCCESS);
 				} else {
 					finishAsFailedOrLoop();
 				}
 			} else {
 				if (questionsIterator.hasNext()) {
 					Question question = questionsIterator.next();
 					setUpQuestionView(question);
 
 				} else {
 					// end of mission, show outro
 
 					// TODO: Bitte nicht mit try-catch arbeiten au�er in
 					// fehlerf�llen. (hm)
 
 					ll.removeAllViews();
 					if (correctAnswers >= correctAnswersNeeded) {
 						if (outroSuccessText == null) {
 							finish(Globals.STATUS_SUCCESS);
 						} else {
 							tv.setText(replaceTokens(outroSuccessText));
 						}
 					} else {
 						if (outroFailText == null) {
 							if (finishAsFailedOrLoop()) return;
 						} else {
 							tv.setText(replaceTokens(outroFailText));
 						}
 					}
 					TextView myTextView = new TextView(QuestionAndAnswer.this);
 					if (correctAnswers >= correctAnswersNeeded) {
 						myTextView
 								.setText(getString(R.string.questionandanswer_successfullyCompletedMission));
 					} else {
 						myTextView
 								.setText(getString(R.string.questionandanswer_notEnoughCorrectAnswers));
 					}
 					myTextView.setTextSize(16);
 					myTextView.setTextColor(Color.BLACK);
 					myTextView.setGravity(Gravity.CENTER_HORIZONTAL);
 					ll.addView(myTextView);
 					ll.addView(button);
 					exit = true;
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return true iff looping, i.e. restarting the mission.
 	 */
 	private boolean finishAsFailedOrLoop() {
 		if (loopUntilSuccess) {
 			setViewsForRestart();
 			return true;
 		} else {
 			finish(Globals.STATUS_FAIL);
 			return false;
 		}
 	}
 
 	/**
 	 * Reinitialize questions and set up view again with first question and its
 	 * answers:
 	 */
 	private void setViewsForRestart() {
 		questionsIterator = questions.iterator();
 		Question question = questionsIterator.next();
 		setUpQuestionView(question);
 	}
 
 	private void setUpQuestionView(Question question) {
 		ll.removeAllViews();
 		tv.setText(question.question);
 		for (Iterator<Answer> i = question.answers.iterator(); i.hasNext();) {
 			Answer answer = i.next();
 			Button answerButton = new Button(QuestionAndAnswer.this);
 			answerButton.setText(answer.answertext);
 			answerButton.setWidth(LayoutParams.FILL_PARENT);
 			answerButton.setTag(answer);
 			answerButton.setOnClickListener(new answerClickListener());
 
 			ll.addView(answerButton);
 		}
 	}
 
 	/**
 	 * called when the player taps on an answer
 	 */
 	private class answerClickListener implements View.OnClickListener {
 		public void onClick(View view) {
 			ll.removeAllViews();
 			Answer answer = (Answer) view.getTag();
 
 			if (answer.correct) {
 				correctAnswers++;
 			} else {
 				incorrectAnswers++;
 			}
 
 			if (answer.onChoose != null) {
 				tv.setText(replaceTokens(answer.onChoose));
 			} else {
 				if (answer.correct) {
 					tv.setText(getString(R.string.questionandanswer_rightAnswer));
 				} else {
 					tv.setText(getString(R.string.questionandanswer_wrongAnswer));
 				}
 			}
 
 			ll.addView(button);
 		}
 	}
 
 	/**
 	 * Simple class that encapsulate a question
 	 */
 	private class Question {
 		public String question;
 		public List<Answer> answers = new ArrayList<Answer>();
 	}
 
 	/**
 	 * Simple class that encapsulates an answer
 	 */
 	private class Answer {
 		public String answertext;
 		public Boolean correct = false;
 		public String onChoose;
 	}
 
 	public void onBlockingStateUpdated(boolean blocking) {
 		// TODO Auto-generated method stub
 		
 	}
 }
