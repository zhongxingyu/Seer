 package client;
 
 /* Author: CS2212 Group 2
  * File Name: BonusScreen.java
  * Date: 25/01/2012
  * Project: UWOSurvivorPool
  * Course: CS2212b
  * Description:
  * */
 
 import java.util.Vector;
 
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.FontFamily;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.EditField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import net.rim.device.api.ui.container.MainScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import data.GameData;
 import data.User;
 import data.bonus.Bonus;
 import data.bonus.BonusQuestion;
 
 public class BonusScreen extends MainScreen implements FieldChangeListener {
 	/* Variables */
 	private LabelField labelTitle; // various labels.
 	private FontFamily ff1; // fonts.
 	private Font font1, font2; // fonts.
 	private ButtonField buttonSend, buttonPrevious, buttonNext;
 	private EditField answerField, questionField; // editable text field
 	private ObjectChoiceField multiChoiceField;
 	
 	private VerticalFieldManager vertFieldManager;
 	
 	private Vector questions = Bonus.getAllQuestions();
 	int questionNum = questions.size() - 1;
 
 	public BonusScreen() {
 		super(NO_VERTICAL_SCROLL);
 		drawQuestionScreen();
 	}
 
 	private void drawQuestionScreen() {
 
 		vertFieldManager = new VerticalFieldManager(
 				VerticalFieldManager.USE_ALL_WIDTH
 						| VerticalFieldManager.USE_ALL_HEIGHT) {
 			// Override the paint method to draw the background image.
 			public void paint(Graphics graphics) {
 				graphics.setColor(Color.BLACK);
 				graphics.fillRect(0, 0, 640, 430);
 				super.paint(graphics);
 			}
 		};
 
 		setToolbar(Common.getToolbar());
 
 		/* Font setup */
 		try { // set up the smaller list font
 			ff1 = FontFamily.forName("Verdana");
 			font2 = ff1.getFont(Font.BOLD, 20);
 		} catch (final ClassNotFoundException cnfe) {
 			System.out.println("font family failed");
 		}
 
 		/* Add the send button */
 		buttonSend = new ButtonField("Send", ButtonField.FIELD_HCENTER|ButtonField.FIELD_BOTTOM);
 		buttonSend.setChangeListener(this);
 
 		buttonSend.setMargin(0, 20, 30, 20);
 		vertFieldManager.add(buttonSend);
 
 		HorizontalFieldManager horFieldManager = new HorizontalFieldManager(
 				HorizontalFieldManager.USE_ALL_WIDTH
 						| HorizontalFieldManager.FIELD_HCENTER);
 
 		/* Top manager bar setup */
 		buttonPrevious = new ButtonField("Prev"); // previous button
 		buttonPrevious.setChangeListener(this);
 		
 		horFieldManager.add(buttonPrevious);
 
 		labelTitle = new LabelField("  Bonus Questions ");
 		labelTitle.setFont(font1); // centre label
 
 		horFieldManager.add(labelTitle);
 
 		buttonNext = new ButtonField("Next"); // next button
 		buttonNext.setChangeListener(this);
 		if (questionNum == questions.size()-1)
 			buttonNext.setEnabled(false);
 		horFieldManager.add(buttonNext);
 		
 		updateQuestionScreen();
 		this.setTitle(horFieldManager);
 		this.add(vertFieldManager);
 		this.setStatus(Common.getToolbar());
 	}
 	
 	private void updateQuestionScreen(){
 		vertFieldManager.deleteAll();
 		BonusQuestion current = (BonusQuestion) questions.elementAt(questionNum);
 		questionField = new EditField(current.getPrompt(), "", 1,
 				EditField.NO_NEWLINE) {
 			public void paint(Graphics graphics) { // keep on same line
 				graphics.setColor(Color.WHITE); // white text
 				super.paint(graphics);
 			}
 		};
 
 		questionField.setEditable(false);
 		questionField.setFont(font2);
 		questionField.setMargin(30, 20, 30, 20);
 		vertFieldManager.add(questionField);
 
 		multiChoiceField = new ObjectChoiceField("", current.getChoices(), 0,
 				ObjectChoiceField.FORCE_SINGLE_LINE
 						| ObjectChoiceField.FIELD_HCENTER);
 		
 		answerField = new EditField("Answer:  ", "", 200, EditField.NO_NEWLINE) {
 			public void paint(Graphics graphics) { // keep on same line
 				graphics.setColor(Color.WHITE); // white text
 				super.paint(graphics);
 			}
 		};
 		
 		User curUser = GameData.getCurrentGame().getCurrentUser();
 		if(current.getBonusType()==0){
 			vertFieldManager.add(answerField);
 			answerField.setMargin(0, 20, 10, 20);
 			if (showAnswer())
 				answerField.setText(current.getAnswer()+"\nYour answer: "
 						+curUser.getUserAnswer(questionNum));
 		}else{
 			vertFieldManager.add(multiChoiceField);
			if (showAnswer())
 				answerField.setText(current.getAnswer()+"\nYour answer: "
 						+curUser.getUserAnswer(questionNum));
 		}
 		
 		buttonPrevious.setEnabled(true);
 		buttonNext.setEnabled(true);
 		
 		if(questionNum==0)
 			buttonPrevious.setEnabled(false);
 		if(questionNum==questions.size()-1)
 			buttonNext.setEnabled(false);
 		
 
 		/* Add the send button */
 		if(!showAnswer()){
 			buttonSend = new ButtonField("Send", ButtonField.FIELD_HCENTER|ButtonField.FIELD_BOTTOM);
 			buttonSend.setChangeListener(this);
 			buttonSend.setMargin(0, 20, 30, 20);
 			vertFieldManager.add(buttonSend);
 		}
 	}
 
 	private boolean showAnswer(){
 		BonusQuestion cur = (BonusQuestion) questions.elementAt(questionNum);
		if(cur.getWeek()<GameData.getCurrentGame().getCurrentWeek())
 			return true;
 		return false;
 	}
 	
 	public boolean onSavePrompt() {
 		return true;
 	}
 
 	public void fieldChanged(Field arg0, int arg1) {
 		if (arg0 == buttonSend) { 
 			User u = GameData.getCurrentGame().getCurrentUser();
 			BonusQuestion b = (BonusQuestion) questions.elementAt(questionNum);
 		} else if (arg0 == buttonNext) {
 			questionNum++;
 			updateQuestionScreen();
 		} else if (arg0 == buttonPrevious) {
 			questionNum--;
 			updateQuestionScreen();
 
 		}
 	}
 
 }
