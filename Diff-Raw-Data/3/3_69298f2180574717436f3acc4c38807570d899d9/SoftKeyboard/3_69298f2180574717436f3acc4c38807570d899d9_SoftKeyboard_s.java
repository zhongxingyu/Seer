 package com.mobileobservinglog.softkeyboard;
 
 import java.nio.CharBuffer;
 
 import com.mobileobservinglog.R;
 
 import android.app.Activity;
 import android.os.Vibrator;
 import android.text.Editable;
 import android.text.InputType;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 
 public class SoftKeyboard{
 	EditText targetTextView;
 	Activity context;
 	
 	Vibrator vibrator;
 	
 	RelativeLayout lowerCase;
     RelativeLayout upperCase;
     RelativeLayout numberLayout;
     RelativeLayout numberLayoutSecondary;
 	
 	Button q;
 	Button w;
 	Button e;
 	Button r;
 	Button t;
 	Button y;
 	Button u;
 	Button i;
 	Button o;
 	Button p;
 	Button a;
 	Button s;
 	Button d;
 	Button f;
 	Button g;
 	Button h;
 	Button j;
 	Button k;
 	Button l;
 	Button z;
 	Button x;
 	Button c;
 	Button v;
 	Button b;
 	Button n;
 	Button m;
 	Button Q;
 	Button W;
 	Button E;
 	Button R;
 	Button T;
 	Button Y;
 	Button U;
 	Button I;
 	Button O;
 	Button P;
 	Button A;
 	Button S;
 	Button D;
 	Button F;
 	Button G;
 	Button H;
 	Button J;
 	Button K;
 	Button L;
 	Button Z;
 	Button X;
 	Button C;
 	Button V;
 	Button B;
 	Button N;
 	Button M;
 	Button one;
 	Button two;
 	Button three;
 	Button four;
 	Button five;
 	Button six;
 	Button seven;
 	Button eight;
 	Button nine;
 	Button zero;
 	Button oneNumTwo;
 	Button twoNumTwo;
 	Button threeNumTwo;
 	Button fourNumTwo;
 	Button fiveNumTwo;
 	Button sixNumTwo;
 	Button sevenNumTwo;
 	Button eightNumTwo;
 	Button nineNumTwo;
 	Button zeroNumTwo;
 	Button comma;
 	Button ucComma;
 	Button period;
 	Button ucPeriod;
 	Button numPeriod;
 	Button numTwoPeriod;
 	Button quote;
 	Button apostrophe;
 	Button colon;
 	Button semicolon;
 	Button plus;
 	Button openParen;
 	Button closeParen;
 	Button at;
 	Button degree;
 	Button minus;
 	Button underscore;
 	Button percent;
 	Button exclamation;
 	Button question;
 	Button multiply;
 	Button pound;
 	Button divide;
 	Button equals;
 	Button dollar;
 	Button carat;
 	Button ampersand;
 	Button shift;
 	Button ucShift;
 	Button numShift;
 	Button numTwoShift;
 	Button backspace;
 	Button ucBackspace;
 	Button numBackspace;
 	Button numTwoBackspace;
 	Button numbers;
 	Button ucNumbers;
 	Button enter;
 	Button ucEnter;
 	Button numEnter;
 	Button numTwoEnter;
 	Button space;
 	Button ucSpace;
 	Button numSpace;
 	Button numTwoSpace;
 	Button letters;
 	Button numTwoLetters;
 	
 	public SoftKeyboard(Activity context, EditText textView, TargetInputType input){
 		this.context = context;
 		targetTextView = textView;
 		vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
 		findLayouts();
 		findButtons();
 		setListeners();
 		
 		if(input == TargetInputType.NUMBER_DECIMAL){
 			lowerCase.setVisibility(View.INVISIBLE);
 			upperCase.setVisibility(View.INVISIBLE);
 			numberLayoutSecondary.setVisibility(View.INVISIBLE);
 			numberLayout.setVisibility(View.VISIBLE);
 		}
 		else{
 			upperCase.setVisibility(View.INVISIBLE);
 			numberLayout.setVisibility(View.INVISIBLE);
 			numberLayoutSecondary.setVisibility(View.INVISIBLE);
 			lowerCase.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	public static enum TargetInputType{
 		NUMBER_DECIMAL,
 		LETTERS
 	}
 	
 	private void findLayouts(){
 		lowerCase = (RelativeLayout)context.findViewById(com.mobileobservinglog.R.id.lower_case_letters);
         upperCase = (RelativeLayout)context.findViewById(com.mobileobservinglog.R.id.upper_case_letters);
        numberLayout = (RelativeLayout)context.findViewById(com.mobileobservinglog.R.id.numbers_and_symbols);
 	}
 
 	private void findButtons(){
 		q = (Button)context.findViewById(com.mobileobservinglog.R.id.q);
 		w = (Button)context.findViewById(com.mobileobservinglog.R.id.w);
 		e = (Button)context.findViewById(com.mobileobservinglog.R.id.e);
 		r = (Button)context.findViewById(com.mobileobservinglog.R.id.r);
 		t = (Button)context.findViewById(com.mobileobservinglog.R.id.t);
 		y = (Button)context.findViewById(com.mobileobservinglog.R.id.y);
 		u = (Button)context.findViewById(com.mobileobservinglog.R.id.u);
 		i = (Button)context.findViewById(com.mobileobservinglog.R.id.i);
 		o = (Button)context.findViewById(com.mobileobservinglog.R.id.o);
 		p = (Button)context.findViewById(com.mobileobservinglog.R.id.p);
 		a = (Button)context.findViewById(com.mobileobservinglog.R.id.a);
 		s = (Button)context.findViewById(com.mobileobservinglog.R.id.s);
 		d = (Button)context.findViewById(com.mobileobservinglog.R.id.d);
 		f = (Button)context.findViewById(com.mobileobservinglog.R.id.f);
 		g = (Button)context.findViewById(com.mobileobservinglog.R.id.g);
 		h = (Button)context.findViewById(com.mobileobservinglog.R.id.h);
 		j = (Button)context.findViewById(com.mobileobservinglog.R.id.j);
 		k = (Button)context.findViewById(com.mobileobservinglog.R.id.k);
 		l = (Button)context.findViewById(com.mobileobservinglog.R.id.l);
 		z = (Button)context.findViewById(com.mobileobservinglog.R.id.z);
 		x = (Button)context.findViewById(com.mobileobservinglog.R.id.x);
 		c = (Button)context.findViewById(com.mobileobservinglog.R.id.c);
 		v = (Button)context.findViewById(com.mobileobservinglog.R.id.v);
 		b = (Button)context.findViewById(com.mobileobservinglog.R.id.b);
 		n = (Button)context.findViewById(com.mobileobservinglog.R.id.n);
 		m = (Button)context.findViewById(com.mobileobservinglog.R.id.m);
 		Q = (Button)context.findViewById(com.mobileobservinglog.R.id.Q);
 		W = (Button)context.findViewById(com.mobileobservinglog.R.id.W);
 		E = (Button)context.findViewById(com.mobileobservinglog.R.id.E);
 		R = (Button)context.findViewById(com.mobileobservinglog.R.id.R);
 		T = (Button)context.findViewById(com.mobileobservinglog.R.id.T);
 		Y = (Button)context.findViewById(com.mobileobservinglog.R.id.Y);
 		U = (Button)context.findViewById(com.mobileobservinglog.R.id.U);
 		I = (Button)context.findViewById(com.mobileobservinglog.R.id.I);
 		O = (Button)context.findViewById(com.mobileobservinglog.R.id.O);
 		P = (Button)context.findViewById(com.mobileobservinglog.R.id.P);
 		A = (Button)context.findViewById(com.mobileobservinglog.R.id.A);
 		S = (Button)context.findViewById(com.mobileobservinglog.R.id.S);
 		D = (Button)context.findViewById(com.mobileobservinglog.R.id.D);
 		F = (Button)context.findViewById(com.mobileobservinglog.R.id.F);
 		G = (Button)context.findViewById(com.mobileobservinglog.R.id.G);
 		H = (Button)context.findViewById(com.mobileobservinglog.R.id.H);
 		J = (Button)context.findViewById(com.mobileobservinglog.R.id.J);
 		K = (Button)context.findViewById(com.mobileobservinglog.R.id.K);
 		L = (Button)context.findViewById(com.mobileobservinglog.R.id.L);
 		Z = (Button)context.findViewById(com.mobileobservinglog.R.id.Z);
 		X = (Button)context.findViewById(com.mobileobservinglog.R.id.X);
 		C = (Button)context.findViewById(com.mobileobservinglog.R.id.C);
 		V = (Button)context.findViewById(com.mobileobservinglog.R.id.V);
 		B = (Button)context.findViewById(com.mobileobservinglog.R.id.B);
 		N = (Button)context.findViewById(com.mobileobservinglog.R.id.N);
 		M = (Button)context.findViewById(com.mobileobservinglog.R.id.M);
 		one = (Button)context.findViewById(com.mobileobservinglog.R.id.one);
 		two = (Button)context.findViewById(com.mobileobservinglog.R.id.two);
 		three = (Button)context.findViewById(com.mobileobservinglog.R.id.three);
 		four = (Button)context.findViewById(com.mobileobservinglog.R.id.four);
 		five = (Button)context.findViewById(com.mobileobservinglog.R.id.five);
 		six = (Button)context.findViewById(com.mobileobservinglog.R.id.six);
 		seven = (Button)context.findViewById(com.mobileobservinglog.R.id.seven);
 		eight = (Button)context.findViewById(com.mobileobservinglog.R.id.eight);
 		nine = (Button)context.findViewById(com.mobileobservinglog.R.id.nine);
 		zero = (Button)context.findViewById(com.mobileobservinglog.R.id.zero);
 		oneNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_one);
 		twoNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_two);
 		threeNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_three);
 		fourNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_four);
 		fiveNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_five);
 		sixNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_six);
 		sevenNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_seven);
 		eightNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_eight);
 		nineNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_nine);
 		zeroNumTwo = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_zero);
 		comma = (Button)context.findViewById(com.mobileobservinglog.R.id.comma);
 		ucComma = (Button)context.findViewById(com.mobileobservinglog.R.id.uc_comma);
 		period = (Button)context.findViewById(com.mobileobservinglog.R.id.period);
 		ucPeriod = (Button)context.findViewById(com.mobileobservinglog.R.id.uc_period);
 		numPeriod = (Button)context.findViewById(com.mobileobservinglog.R.id.num_period);
 		numTwoPeriod = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_period);
 		quote = (Button)context.findViewById(com.mobileobservinglog.R.id.quote);
 		apostrophe = (Button)context.findViewById(com.mobileobservinglog.R.id.apostrophe);
 		colon = (Button)context.findViewById(com.mobileobservinglog.R.id.colon);
 		semicolon = (Button)context.findViewById(com.mobileobservinglog.R.id.semicolon);
 		plus = (Button)context.findViewById(com.mobileobservinglog.R.id.plus);
 		openParen = (Button)context.findViewById(com.mobileobservinglog.R.id.open_paren);
 		closeParen = (Button)context.findViewById(com.mobileobservinglog.R.id.close_paren);
 		at = (Button)context.findViewById(com.mobileobservinglog.R.id.at);
 		degree = (Button)context.findViewById(com.mobileobservinglog.R.id.degree);
 		minus = (Button)context.findViewById(com.mobileobservinglog.R.id.minus);
 		underscore = (Button)context.findViewById(com.mobileobservinglog.R.id.underscore);
 		percent = (Button)context.findViewById(com.mobileobservinglog.R.id.percent);
 		exclamation = (Button)context.findViewById(com.mobileobservinglog.R.id.exclamation);
 		question = (Button)context.findViewById(com.mobileobservinglog.R.id.question);
 		multiply = (Button)context.findViewById(com.mobileobservinglog.R.id.multiply);
 		pound = (Button)context.findViewById(com.mobileobservinglog.R.id.pound);
 		divide = (Button)context.findViewById(com.mobileobservinglog.R.id.divide);
 		equals = (Button)context.findViewById(com.mobileobservinglog.R.id.equals);
 		dollar = (Button)context.findViewById(com.mobileobservinglog.R.id.dollar);
 		carat = (Button)context.findViewById(com.mobileobservinglog.R.id.carat);
 		ampersand = (Button)context.findViewById(com.mobileobservinglog.R.id.ampersand);
 		shift = (Button)context.findViewById(com.mobileobservinglog.R.id.shift);
 		ucShift = (Button)context.findViewById(com.mobileobservinglog.R.id.uc_shift);
 		numShift = (Button)context.findViewById(com.mobileobservinglog.R.id.num_shift);
 		numTwoShift = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_shift);
 		backspace = (Button)context.findViewById(com.mobileobservinglog.R.id.backspace);
 		ucBackspace = (Button)context.findViewById(com.mobileobservinglog.R.id.uc_backspace);
 		numBackspace = (Button)context.findViewById(com.mobileobservinglog.R.id.num_backspace);
 		numTwoBackspace = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_backspace);
 		numbers = (Button)context.findViewById(com.mobileobservinglog.R.id.numbers);
 		ucNumbers = (Button)context.findViewById(com.mobileobservinglog.R.id.uc_numbers);
 		enter = (Button)context.findViewById(com.mobileobservinglog.R.id.enter);
 		ucEnter = (Button)context.findViewById(com.mobileobservinglog.R.id.uc_enter);
 		numEnter = (Button)context.findViewById(com.mobileobservinglog.R.id.num_enter);
 		numTwoEnter = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_enter);
 		space = (Button)context.findViewById(com.mobileobservinglog.R.id.space);
 		ucSpace = (Button)context.findViewById(com.mobileobservinglog.R.id.uc_space);
 		numSpace = (Button)context.findViewById(com.mobileobservinglog.R.id.num_space);
 		numTwoSpace = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_space);
 		letters = (Button)context.findViewById(com.mobileobservinglog.R.id.letters);
 		numTwoLetters = (Button)context.findViewById(com.mobileobservinglog.R.id.num_two_letters);
 	}
 	
 	private void setListeners(){
 		q.setOnClickListener(inputLowerQ);
 		w.setOnClickListener(inputLowerW);
 		e.setOnClickListener(inputLowerE);
 		r.setOnClickListener(inputLowerR);
 		t.setOnClickListener(inputLowerT);
 		y.setOnClickListener(inputLowerY);
 		u.setOnClickListener(inputLowerU);
 		i.setOnClickListener(inputLowerI);
 		o.setOnClickListener(inputLowerO);
 		p.setOnClickListener(inputLowerP);
 		a.setOnClickListener(inputLowerA);
 		s.setOnClickListener(inputLowerS);
 		d.setOnClickListener(inputLowerD);
 		f.setOnClickListener(inputLowerF);
 		g.setOnClickListener(inputLowerG);
 		h.setOnClickListener(inputLowerH);
 		j.setOnClickListener(inputLowerJ);
 		k.setOnClickListener(inputLowerK);
 		l.setOnClickListener(inputLowerL);
 		z.setOnClickListener(inputLowerZ);
 		x.setOnClickListener(inputLowerX);
 		c.setOnClickListener(inputLowerC);
 		v.setOnClickListener(inputLowerV);
 		b.setOnClickListener(inputLowerB);
 		n.setOnClickListener(inputLowerN);
 		m.setOnClickListener(inputLowerM);
 		Q.setOnClickListener(inputUpperQ);
 		W.setOnClickListener(inputUpperW);
 		E.setOnClickListener(inputUpperE);
 		R.setOnClickListener(inputUpperR);
 		T.setOnClickListener(inputUpperT);
 		Y.setOnClickListener(inputUpperY);
 		U.setOnClickListener(inputUpperU);
 		I.setOnClickListener(inputUpperI);
 		O.setOnClickListener(inputUpperO);
 		P.setOnClickListener(inputUpperP);
 		A.setOnClickListener(inputUpperA);
 		S.setOnClickListener(inputUpperS);
 		D.setOnClickListener(inputUpperD);
 		F.setOnClickListener(inputUpperF);
 		G.setOnClickListener(inputUpperG);
 		H.setOnClickListener(inputUpperH);
 		J.setOnClickListener(inputUpperJ);
 		K.setOnClickListener(inputUpperK);
 		L.setOnClickListener(inputUpperL);
 		Z.setOnClickListener(inputUpperZ);
 		X.setOnClickListener(inputUpperX);
 		C.setOnClickListener(inputUpperC);
 		V.setOnClickListener(inputUpperV);
 		B.setOnClickListener(inputUpperB);
 		N.setOnClickListener(inputUpperN);
 		M.setOnClickListener(inputUpperM);
 		one.setOnClickListener(inputOne);
 		two.setOnClickListener(inputTwo);
 		three.setOnClickListener(inputThree);
 		four.setOnClickListener(inputFour);
 		five.setOnClickListener(inputFive);
 		six.setOnClickListener(inputSix);
 		seven.setOnClickListener(inputSeven);
 		eight.setOnClickListener(inputEight);
 		nine.setOnClickListener(inputNine);
 		zero.setOnClickListener(inputZero);
 		oneNumTwo.setOnClickListener(inputOne);
 		twoNumTwo.setOnClickListener(inputTwo);
 		threeNumTwo.setOnClickListener(inputThree);
 		fourNumTwo.setOnClickListener(inputFour);
 		fiveNumTwo.setOnClickListener(inputFive);
 		sixNumTwo.setOnClickListener(inputSix);
 		sevenNumTwo.setOnClickListener(inputSeven);
 		eightNumTwo.setOnClickListener(inputEight);
 		nineNumTwo.setOnClickListener(inputNine);
 		zeroNumTwo.setOnClickListener(inputZero);
 		comma.setOnClickListener(inputComma);
 		ucComma.setOnClickListener(inputComma);
 		period.setOnClickListener(inputPeriod);
 		ucPeriod.setOnClickListener(inputPeriod);
 		numPeriod.setOnClickListener(inputPeriod);
 		numTwoPeriod.setOnClickListener(inputPeriod);
 		quote.setOnClickListener(inputQuote);
 		apostrophe.setOnClickListener(inputApostrophe);
 		colon.setOnClickListener(inputColon);
 		semicolon.setOnClickListener(inputSemicolon);
 		plus.setOnClickListener(inputPlus);
 		openParen.setOnClickListener(inputOpenParen);
 		closeParen.setOnClickListener(inputCloseParen);
 		at.setOnClickListener(inputAt);
 		degree.setOnClickListener(inputDegree);
 		minus.setOnClickListener(inputMinus);
 		underscore.setOnClickListener(inputUnderscore);
 		percent.setOnClickListener(inputPercent);
 		exclamation.setOnClickListener(inputExclamation);
 		question.setOnClickListener(inputQuestion);
 		multiply.setOnClickListener(inputMultiply);
 		pound.setOnClickListener(inputPound);
 		divide.setOnClickListener(inputDivide);
 		equals.setOnClickListener(inputEquals);
 		dollar.setOnClickListener(inputDollar);
 		carat.setOnClickListener(inputCarat);
 		ampersand.setOnClickListener(inputAmpersand);
 		shift.setOnClickListener(showUpperCase);
 		ucShift.setOnClickListener(showLowerCase);
 		numShift.setOnClickListener(showSecondaryNumbers);
 		numTwoShift.setOnClickListener(showNumbers);
 		backspace.setOnClickListener(doBackspace);
 		ucBackspace.setOnClickListener(doBackspace);
 		numBackspace.setOnClickListener(doBackspace);
 		numTwoBackspace.setOnClickListener(doBackspace);
 		numbers.setOnClickListener(showNumbers);
 		ucNumbers.setOnClickListener(showNumbers);
 		enter.setOnClickListener(inputEnter);
 		ucEnter.setOnClickListener(inputEnter);
 		numEnter.setOnClickListener(inputEnter);
 		numTwoEnter.setOnClickListener(inputEnter);
 		space.setOnClickListener(inputSpace);
 		ucSpace.setOnClickListener(inputSpace);
 		numSpace.setOnClickListener(inputSpace);
 		numTwoSpace.setOnClickListener(inputSpace);
 		letters.setOnClickListener(showLowerCase);
 		numTwoLetters.setOnClickListener(showLowerCase);
 	}
 	
 	public void hideAll(){
 		lowerCase.setVisibility(View.INVISIBLE);
 		upperCase.setVisibility(View.INVISIBLE);
 		numberLayout.setVisibility(View.INVISIBLE);
 	}
 	
 	protected void insertText(char character){
 		//Get the position of the cursor
 		int cursorStart = targetTextView.getSelectionStart();
 		int cursorEnd = targetTextView.getSelectionEnd();
 		Editable text = targetTextView.getText();
 		char[] input = new char[]{character};
 		vibrator.vibrate(100);
 		
 		if(cursorStart > cursorEnd){//if the selection was made from right to left
 			int temp = cursorStart;
 			cursorStart = cursorEnd;
 			cursorEnd = temp;
 		}
 		
 		text = text.replace(cursorStart, cursorEnd, CharBuffer.wrap(input));
 
 		//Set the text
 		targetTextView.setText(text);
 		//set the cursor position
 		targetTextView.setSelection(cursorStart + 1);
 	}
 	
 	protected void handleBackspace(){
 		//Get the position of the cursor
 		int cursorStart = targetTextView.getSelectionStart();
 		int cursorEnd = targetTextView.getSelectionEnd();
 		Editable text = targetTextView.getText();
 		vibrator.vibrate(100);
 		
 		if(cursorStart > cursorEnd){//if the selection was made from right to left
 			int temp = cursorStart;
 			cursorStart = cursorEnd;
 			cursorEnd = temp;
 		}
 		
 		//Set the text
 		//set the cursor position
 		if(cursorStart < cursorEnd || cursorStart == 0){
 			text = text.replace(cursorStart, cursorEnd, "");
 			targetTextView.setText(text);
 			targetTextView.setSelection(cursorStart);
 		}
 		else{
 			text = text.replace(cursorStart - 1, cursorEnd, "");
 			targetTextView.setText(text);
 			targetTextView.setSelection(cursorStart - 1);
 		}
 	}
 	
 	protected final Button.OnClickListener inputLowerQ = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('q');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerW = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('w');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerE = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('e');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerR = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('r');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerT = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('t');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerY = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('y');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerU = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('u');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerI = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('i');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerO = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('o');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerP = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('p');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerA = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('a');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerS = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('s');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerD = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('d');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerF = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('f');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerG = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('g');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerH = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('h');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerJ = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('j');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerK = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('k');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerL = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('l');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerZ = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('z');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerX = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('x');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerC = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('c');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerV = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('v');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerB = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('b');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerN = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('n');
     	}
     };
 	
 	protected final Button.OnClickListener inputLowerM = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('m');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperQ = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('Q');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperW = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('W');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperE = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('E');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperR = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('R');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperT = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('T');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperY = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('Y');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperU = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('U');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperI = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('I');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperO = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('O');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperP = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('P');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperA = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('A');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperS = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('S');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperD = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('D');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperF = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('F');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperG = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('G');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperH = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('H');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperJ = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('J');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperK = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('K');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperL = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('L');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperZ = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('Z');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperX = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('X');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperC = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('C');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperV = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('V');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperB = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('B');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperN = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('N');
     	}
     };
 	
 	protected final Button.OnClickListener inputUpperM = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('M');
     	}
     };
 	
 	protected final Button.OnClickListener inputOne = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('1');
     	}
     };
 	
 	protected final Button.OnClickListener inputTwo = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('2');
     	}
     };
 	
 	protected final Button.OnClickListener inputThree = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('3');
     	}
     };
 	
 	protected final Button.OnClickListener inputFour = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('4');
     	}
     };
 	
 	protected final Button.OnClickListener inputFive = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('5');
     	}
     };
 	
 	protected final Button.OnClickListener inputSix = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('6');
     	}
     };
 	
 	protected final Button.OnClickListener inputSeven = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('7');
     	}
     };
 	
 	protected final Button.OnClickListener inputEight = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('8');
     	}
     };
 	
 	protected final Button.OnClickListener inputNine = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('9');
     	}
     };
 	
 	protected final Button.OnClickListener inputZero = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('0');
     	}
     };
 	
 	protected final Button.OnClickListener inputComma = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText(',');
     	}
     };
 	
 	protected final Button.OnClickListener inputPeriod = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('.');
     	}
     };
 	
 	protected final Button.OnClickListener inputQuote = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('"');
     	}
     };
 	
 	protected final Button.OnClickListener inputApostrophe = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('\'');
     	}
     };
 	
 	protected final Button.OnClickListener inputColon = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText(':');
     	}
     };
 	
 	protected final Button.OnClickListener inputSemicolon = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText(';');
     	}
     };
 	
 	protected final Button.OnClickListener inputPlus = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('+');
     	}
     };
 	
 	protected final Button.OnClickListener inputOpenParen = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('(');
     	}
     };
 	
 	protected final Button.OnClickListener inputCloseParen = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText(')');
     	}
     };
 	
 	protected final Button.OnClickListener inputAt = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('@');
     	}
     };
 	
 	protected final Button.OnClickListener inputDegree = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('');
     	}
     };
 	
 	protected final Button.OnClickListener inputMinus = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('-');
     	}
     };
 	
 	protected final Button.OnClickListener inputUnderscore = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('_');
     	}
     };
 	
 	protected final Button.OnClickListener inputPercent = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('%');
     	}
     };
 	
 	protected final Button.OnClickListener inputExclamation = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('!');
     	}
     };
 	
 	protected final Button.OnClickListener inputQuestion = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('?');
     	}
     };
 	
 	protected final Button.OnClickListener inputMultiply = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('*');
     	}
     };
 	
 	protected final Button.OnClickListener inputPound = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('#');
     	}
     };
 	
 	protected final Button.OnClickListener inputDivide = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('/');
     	}
     };
 	
 	protected final Button.OnClickListener inputEquals = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('=');
     	}
     };
 	
 	protected final Button.OnClickListener inputCarat = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('^');
     	}
     };
 	
 	protected final Button.OnClickListener inputDollar = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('$');
     	}
     };
 	
 	protected final Button.OnClickListener inputAmpersand = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('&');
     	}
     };
 	
 	protected final Button.OnClickListener showUpperCase = new Button.OnClickListener(){
     	public void onClick(View view){
 			numberLayout.setVisibility(View.INVISIBLE);
 			numberLayoutSecondary.setVisibility(View.INVISIBLE);
 			lowerCase.setVisibility(View.INVISIBLE);
     		upperCase.setVisibility(View.VISIBLE);
     	}
     };
 	
 	protected final Button.OnClickListener showLowerCase = new Button.OnClickListener(){
     	public void onClick(View view){
     		numberLayout.setVisibility(View.INVISIBLE);
 			numberLayoutSecondary.setVisibility(View.INVISIBLE);
     		upperCase.setVisibility(View.INVISIBLE);
 			lowerCase.setVisibility(View.VISIBLE);
     	}
     };
 	
 	protected final Button.OnClickListener doBackspace = new Button.OnClickListener(){
     	public void onClick(View view){
     		handleBackspace();
     	}
     };
 	
 	protected final Button.OnClickListener showNumbers = new Button.OnClickListener(){
     	public void onClick(View view){
     		upperCase.setVisibility(View.INVISIBLE);
 			lowerCase.setVisibility(View.INVISIBLE);
 			numberLayoutSecondary.setVisibility(View.INVISIBLE);
     		numberLayout.setVisibility(View.VISIBLE);
     	}
     };
 	
 	protected final Button.OnClickListener inputEnter = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText('\n');
     	}
     };
 	
 	protected final Button.OnClickListener inputSpace = new Button.OnClickListener(){
     	public void onClick(View view){
     		insertText(' ');
     	}
     };
 	
 	protected final Button.OnClickListener showSecondaryNumbers = new Button.OnClickListener(){
     	public void onClick(View view){
     		upperCase.setVisibility(View.INVISIBLE);
 			lowerCase.setVisibility(View.INVISIBLE);
     		numberLayout.setVisibility(View.INVISIBLE);
 			numberLayoutSecondary.setVisibility(View.VISIBLE);
     	}
     };
 }
