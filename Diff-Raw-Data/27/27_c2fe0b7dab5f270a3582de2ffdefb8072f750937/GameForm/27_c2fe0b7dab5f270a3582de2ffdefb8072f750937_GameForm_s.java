 package org.fruct.oss.russianriddles;
 
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Item;
 import javax.microedition.lcdui.ItemStateListener;
 import javax.microedition.lcdui.StringItem;
 import javax.microedition.lcdui.TextField;
 
 public class GameForm extends Form implements ItemStateListener {
 
 	public static int MAX_TIME = 90;
 	
 	protected TextField fld = null;
 	protected String solution = null;
 	protected String solutionTransl = null;
 	protected String currentTip = null;
 	
 	protected StringItem textMsg = null;
 	protected StringItem errorMsg = null;
 	public StringItem timeMsg = null;
 	protected Timer gameTimer = null;
 	protected TimerTask gameTask = null;
 	public int elapsedTime = 0;
 	
 	public static GameForm game;
 	
 	private Vector questions = null;
 	private int questionsLength = 0;
 	Random rnd = new Random();
 	private Menu menu;
 	
 	int numSuccessQuestions = 0;
 	
 	public GameForm(Menu curMenu) {
 		super(null);
 
 		game = this;
 		menu = curMenu;
 		
 		loadQuestions();
 		
 		timeMsg = new StringItem(null, " XX .");
 		this.append(timeMsg);
 		
 		gameTimer = new Timer();
 		
 		textMsg = new StringItem(null, "");
 		textMsg.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
 		this.append(textMsg);
 		
 		fld = new TextField(null, null, 20, TextField.ANY);
 		this.append(fld);
 		fld.setInitialInputMode("UCB_CYRILLIC");
 		//fld.setItemCommandListener(this);
 		this.setItemStateListener(this);
 		
 		errorMsg = new StringItem(null, null);
 		this.append(errorMsg);
 }
 	
 	private void nextQuestion() {
 		int num = Math.abs(rnd.nextInt()) % questionsLength;
 		textMsg.setText(((String[])questions.elementAt(num))[0]);
 		solution = ((String[])questions.elementAt(num))[1];
 		solutionTransl = ((String[])questions.elementAt(num)).length > 2 ? ((String[])questions.elementAt(num))[2]: "";
 		elapsedTime = MAX_TIME;
 		timeMsg.setText(" " + elapsedTime + " .");
 		fld.setString("");
 
 		if (gameTask != null)
 			try {
 				gameTask.cancel();
 			} catch (Exception ex) {};
 			
 		gameTask = new TimerTask() {
 			public void run() {
 				System.err.println(" " + game.elapsedTime + " .");
 				game.elapsedTime--;
 				game.timeMsg.setText(" " + game.elapsedTime + " .");
 				if (game.elapsedTime <= 0) {
 					this.cancel();
 					game.menu.manager.back();
 					game.stopGame();
 				}
 				if (game.elapsedTime == Math.abs(MAX_TIME/2))
 					game.showTip();
 			}
 		};
 		gameTimer.schedule(gameTask, 1000, 1000);
 		currentTip = null;
 		fld.setConstraints((fld.getConstraints() & TextField.CONSTRAINT_MASK));
 	}
 
 	protected void checkSolution()
 	{
 		if (fld.getString().equalsIgnoreCase(this.solution))
 		{
 			System.err.println("good");
 			errorMsg.setText("!");
 			fld.setConstraints((fld.getConstraints() & TextField.CONSTRAINT_MASK)|TextField.UNEDITABLE);
 			this.numSuccessQuestions++;
 			gameTimer.schedule(new TimerTask(){public void run() {game.nextQuestion();}}, 50);
 		}
 		else
 			if (fld.getString().equalsIgnoreCase(this.solutionTransl) && this.solutionTransl.length() > 0)
 			{
 				System.err.println("good");
 				errorMsg.setText("!");
 				fld.setConstraints((fld.getConstraints() & TextField.CONSTRAINT_MASK)|TextField.UNEDITABLE);
 				this.numSuccessQuestions++;
 				gameTimer.schedule(new TimerTask(){public void run() {game.nextQuestion();}}, 50);
 			} else {
 				System.err.println("bad");
				errorMsg.setText("!" + (this.currentTip != null ? " (" + this.currentTip : ")"));
 		}
 	}
 
 	public void itemStateChanged(Item item) {
 		if (item == fld) {
			if (fld.getString().endsWith("\n")) {
				fld.setString(fld.getString().substring(0, fld.getString().length() - 1));
 				checkSolution();
 			} else {
 				System.err.println("empty string");
 				errorMsg.setText((this.currentTip != null ? this.currentTip + " " : "") + "   ");
 			}
 		}
 	}
 	
 	public void stopGame() {
 		System.err.println("stop");
 		fld.setConstraints((fld.getConstraints() & TextField.CONSTRAINT_MASK)|TextField.UNEDITABLE);
 		gameTask.cancel();
		if (elapsedTime <= 0)
 			menu.showResult();
 		else
 			menu.hideResult();
 	}
 	
 	public void startGame() {
 		this.numSuccessQuestions = 0;
 		this.nextQuestion();
 		errorMsg.setText("   ");
 	}
 	
 	public void showTip() {
 		char[] tip = solution.toCharArray();
 		for(int i = 0; i < tip.length; i++) {
 			if (Math.abs(rnd.nextInt() % 2) > 0) {
 				tip[i] = '*';
 			}
 		}
 		currentTip = new String(tip);
 		
 		errorMsg.setText(": \"" + currentTip + "\".");
 	}
 	
 	private void loadQuestions() {
 		questions = new Vector();
 		questions.addElement(new String[]{" ,  ,   .", "", "Nozhnicy"});
 		questions.addElement(new String[]{"     .    ?", "", "Ovca"});
 		questions.addElement(new String[]{" ,  ,   .", "", "Ovca"});
 		questions.addElement(new String[]{" ,      .", "", "Ovca"});
 		questions.addElement(new String[]{"  ,  ,      ,   .", "", "Baran"});
 		questions.addElement(new String[]{"   :-,  !", "", "Cyplyonok"});
 		questions.addElement(new String[]{"    ,    - .", "", "Nasedka"});
 		questions.addElement(new String[]{"    ?", "", "Petushinym"});
 		questions.addElement(new String[]{"-   .", "", "Zamok"});
 		questions.addElement(new String[]{"     .", "", "Porog"});
 		questions.addElement(new String[]{"  ,   ,     .", "", "Um"});
 		questions.addElement(new String[]{",    ; ,    ;  ,  .", "", "Ten'"});
 		questions.addElement(new String[]{"    .", "","Noch'"});
 		questions.addElement(new String[]{" ,    .", "", "Dozhd'"});
 		questions.addElement(new String[]{"     .", "", "Luch"});
 		questions.addElement(new String[]{" ,  ,  ,   ,  ,  ,   .", "", "Rosa"});
 		questions.addElement(new String[]{"  :   .       ;   ,   ", "", "Doroga"});
 		questions.addElement(new String[]{"  ,   .", "", "Stolb"});
 		questions.addElement(new String[]{"  ,      ,   .", "", "Prorub'"});
 		questions.addElement(new String[]{"  ,   .", "", "Dym"});
 		questions.addElement(new String[]{" ,  ,  .", "", "Svecha"});
 		questions.addElement(new String[]{" ,   .", "", "Molot"});
 		questions.addElement(new String[]{"   ?", "", "Uxvat"});
 		questions.addElement(new String[]{"    .", "", "Kocherga"});
 		questions.addElement(new String[]{" ,  ,   ,   .", "", "Pila"});
 		questions.addElement(new String[]{", ,    .", "", "Topor"});
 		questions.addElement(new String[]{" ,  .", "", "Podushka"});
 		questions.addElement(new String[]{" ,  ,   .", "", "Podushka"});
 		questions.addElement(new String[]{"     ,   .", "", "Stol"});
 		questions.addElement(new String[]{", ,     .", "", "Chasy"});
 		questions.addElement(new String[]{", ,   ,   .        .", "", "Venik"});
 		questions.addElement(new String[]{" ,  ,   -,   -    .", "", "Venik"});
 		questions.addElement(new String[]{" ,  ,   ,    .", "", "Samovar"});
 		questions.addElement(new String[]{" ,  ,  ,   .", "", "Karandash"});
 		questions.addElement(new String[]{" ,    .   ,    .", "", "Ochki"});
 		questions.addElement(new String[]{"    .", "", "Gvozd'"});
 		questions.addElement(new String[]{"  ,   ,   ,   ,    .", "", "Chajnik"});
 		questions.addElement(new String[]{", ,     .", "", "Klubok"});
 		questions.addElement(new String[]{"  ,   .", "", "Klubok"});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		//questions.addElement(new String[]{"", ""});
 		
 		questionsLength = questions.size();
 		System.err.println("questions:" + questionsLength);
 	}
 }
