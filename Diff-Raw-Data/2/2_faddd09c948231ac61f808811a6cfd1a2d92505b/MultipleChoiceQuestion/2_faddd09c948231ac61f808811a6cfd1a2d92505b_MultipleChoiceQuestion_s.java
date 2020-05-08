 package edu.bonn.mobilegaming.geoquest.mission;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.Attribute;
 import org.dom4j.Element;
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.qeevee.gq.history.TextItem;
 import com.qeevee.gq.history.TextType;
 
 import edu.bonn.mobilegaming.geoquest.Globals;
 import edu.bonn.mobilegaming.geoquest.R;
 import edu.bonn.mobilegaming.geoquest.Variables;
 
 /**
  * Simple multiple choice question and answer mission.
  * 
  * @author Holger Muegge
  */
 public class MultipleChoiceQuestion extends InteractiveMission {
     /** layout */
     private LinearLayout mcButtonPanel;
     /** text view for displaying text */
     private TextView mcTextView;
     private Button bottomButton;
 
     private int mode = 0; // UNDEFINED MODE AT START IS USED TO TRIGGER INIT!
     private static final int MODE_QUESTION = 1;
     private static final int MODE_REPLY_TO_CORRECT_ANSWER = 2;
     private static final int MODE_REPLY_TO_WRONG_ANSWER = 3;
 
     private List<Answer> answers = new ArrayList<Answer>();
     private Answer selectedAnswer;
     private String questionText;
     private OnClickListener proceed, restart;
 
     private final static String SHUFFLE_ANSWERS = "true";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	initContentView();
 	initQuestion();
 	setMode(MODE_QUESTION);
     }
 
     /**
      * Clears and (re-) populates the complete view.
      * 
      * @param newMode
      */
     private void setMode(int newMode) {
 	if (mode == newMode)
 	    return;
 	// real change in mode:
 	mode = newMode;
 	mcButtonPanel.removeAllViews();
 	switch (mode) {
 	case MODE_QUESTION:
 	    setUpQuestionView();
 	    break;
 	case MODE_REPLY_TO_CORRECT_ANSWER:
 	    setMCTextViewToReply();
 	    setMCButtonPanel(loopUntilSuccess);
 	    invokeOnSuccessEvents();
 	    break;
 	case MODE_REPLY_TO_WRONG_ANSWER:
 	    setMCTextViewToReply();
 	    if (loopUntilSuccess)
 		setMCButtonPanel(loopUntilSuccess);
 	    else
 		setMCButtonPanel(loopUntilSuccess);
 	    invokeOnFailEvents();
 	    break;
 	}
     }
 
     private void setMCButtonPanel(boolean loop) {
 	mcButtonPanel.addView(bottomButton);
 	// set the button text:
 	if (selectedAnswer.nextbuttontext != null) {
 	    // game specific if specified:
 	    bottomButton.setText(selectedAnswer.nextbuttontext);
 	} else {
 	    // generic if not specified:
	    if (loop) {
 		bottomButton
 			.setText(getString(R.string.question_repeat_button));
 		bottomButton.setOnClickListener(restart);
 	    } else {
 		bottomButton
 			.setText(getString(R.string.question_proceed_button));
 		bottomButton.setOnClickListener(proceed);
 	    }
 	}
     }
 
     private void setMCTextViewToReply() {
 	String answerToShow;
 	if (selectedAnswer.onChoose != null) {
 	    answerToShow = selectedAnswer.onChoose;
 	} else {
 	    answerToShow = selectedAnswer.correct ? getString(R.string.questionandanswer_rightAnswer)
 		    : getString(R.string.questionandanswer_wrongAnswer);
 	}
 	mcTextView.setText(answerToShow);
 	new TextItem(answerToShow, this,
 		selectedAnswer.correct ? TextType.REACTION_ON_CORRECT
 			: TextType.REACTION_ON_WRONG);
     }
 
     private void initContentView() {
 	setContentView(R.layout.multiplechoice);
 	mcTextView = (TextView) findViewById(R.id.mcTextView);
 	mcButtonPanel = (LinearLayout) findViewById(R.id.mcButtonPanel);
 	// prefab neccessary buttons:
 	prepareBottomButton();
     }
 
     private void prepareBottomButton() {
 	bottomButton = new Button(this);
 	bottomButton.setWidth(LayoutParams.FILL_PARENT);
 	proceed = new OnClickListener() {
 
 	    public void onClick(View v) {
 		// TODO rework and test
 		if (selectedAnswer.correct) {
 		    invokeOnSuccessEvents();
 		} else {
 		    invokeOnFailEvents();
 		}
 		if (loopUntilSuccess) {
 		    if (!selectedAnswer.correct) {
 			setMode(MODE_QUESTION);
 		    } else {
 			finish(Globals.STATUS_SUCCESS);
 		    }
 		} else {
 		    finish(selectedAnswer.correct ? Globals.STATUS_SUCCESS
 			    : Globals.STATUS_FAIL);
 		}
 	    }
 	};
 
 	restart = new OnClickListener() {
 
 	    public void onClick(View v) {
 		setMode(MODE_QUESTION);
 	    }
 	};
     }
 
     @SuppressWarnings("unchecked")
     private void initQuestion() {
 	Element xmlQuestion = (Element) mission.xmlMissionNode
 		.selectSingleNode("./question");
 	questionText = xmlQuestion.selectSingleNode("./questiontext").getText()
 		.replaceAll("\\s+",
 			    " ").trim();
 	List<Element> xmlAnswers = xmlQuestion.selectNodes("./answer");
 	for (Iterator<Element> j = xmlAnswers.iterator(); j.hasNext();) {
 	    Element xmlAnswer = j.next();
 	    Attribute correct = (Attribute) xmlAnswer
 		    .selectSingleNode("./@correct");
 	    Answer answer = new Answer();
 	    if ((correct != null) && (correct.getText().equals("1")))
 		answer.correct = true;
 	    answer.answertext = xmlAnswer.getText().replaceAll("\\s+",
 							       " ").trim();
 
 	    Attribute onChoose = ((Attribute) xmlAnswer
 		    .selectSingleNode("./@onChoose"));
 	    if (onChoose != null)
 		answer.onChoose = onChoose.getText().replaceAll("\\s+",
 								" ").trim();
 
 	    Attribute nbt = ((Attribute) xmlAnswer
 		    .selectSingleNode("./@nextbuttontext"));
 	    if (nbt != null)
 		answer.nextbuttontext = nbt.getText().replaceAll("\\s+",
 								 " ").trim();
 
 	    answers.add(answer);
 	}
 	shuffleAnswers();
     }
 
     private void shuffleAnswers() {
 	CharSequence shuffleString = getMissionAttribute("shuffle",
 							 R.string.multipleChoiceQuestion_default_shuffle_mode)
 		.toString();
 
 	if (shuffleString.equals(SHUFFLE_ANSWERS)) {
 	    java.util.Collections.shuffle(answers);
 	}
     }
 
     private void setUpQuestionView() {
 	mcButtonPanel.removeAllViews();
 	// show question:
 	mcTextView.setText(questionText);
 	new TextItem(questionText, this, TextType.QUESTION);
 
 	// list answers:
 	for (Iterator<Answer> i = answers.iterator(); i.hasNext();) {
 	    Answer answer = i.next();
 	    Button answerButton = new Button(MultipleChoiceQuestion.this);
 	    answerButton.setText(answer.answertext);
 	    answerButton.setWidth(LayoutParams.FILL_PARENT);
 	    answerButton.setTag(answer);
 	    answerButton.setOnClickListener(new AnswerClickListener());
 	    mcButtonPanel.addView(answerButton);
 	}
     }
 
     /**
      * called when the player taps on an answer
      */
     private class AnswerClickListener implements View.OnClickListener {
 	public void onClick(View view) {
 	    selectedAnswer = (Answer) view.getTag();
 	    // set chosen answer text as result in mission specific variable:
 	    Variables.registerMissionResult(mission.id,
 					    selectedAnswer.answertext);
 	    if (selectedAnswer.correct) {
 		setMode(MODE_REPLY_TO_CORRECT_ANSWER);
 	    } else {
 		setMode(MODE_REPLY_TO_WRONG_ANSWER);
 	    }
 	}
     }
 
     /**
      * Simple class that encapsulates an answer
      */
     private class Answer {
 	public String answertext;
 	public Boolean correct = false;
 	public String onChoose;
 	public String nextbuttontext = null;
     }
 
     public void onBlockingStateUpdated(boolean blocking) {
 	mcButtonPanel.setEnabled(!blocking);
     }
 
 }
