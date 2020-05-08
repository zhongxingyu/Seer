 /**
  * Copyright (C) 2012 Emil Edholm, Emil Johansson, Johan Andersson, Johan Gustafsson
  *
  * This file is part of dat255-bearded-octo-lama
  *
  *  dat255-bearded-octo-lama is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  dat255-bearded-octo-lama is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with dat255-bearded-octo-lama.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package it.chalmers.dat255_bearded_octo_lama.games;
 
 import it.chalmers.dat255_bearded_octo_lama.R;
 import it.chalmers.dat255_bearded_octo_lama.games.anno.Game;
 
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint.Align;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.view.Gravity;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 
 @Game(name = "Calculus")
 public class CalculusGame extends AbstractGameView {
 	
 	//Set all background color attributes.
 	private static final int ALPHA = 255;
 	private static final int RED   = 51;
 	private static final int GREEN = 204;
 	private static final int BLUE  = 255;
 	
 	private String exerciseText;
 	private int var1, var2;
 	private int textSize;
 	private EditText answerTextField;
 	
 	public CalculusGame(Context context) {
 		super(context);
 		
 		initUI();
 		initGame();
 	}
 	
 	private void initGame() {
 		Random randomGenerator = new Random();
 		
 		//Decide 2 random number between 1 and 100 to be added to each other.
 		int maxNumberValue = 100;
 		var1 = randomGenerator.nextInt(maxNumberValue) + 1;
 		var2 = randomGenerator.nextInt(maxNumberValue) + 1;
 		
 		exerciseText = "What is: " + var1 + " + " + var2;
 	}
 	
 	private void initUI() {
 		//Initiate the UI components.
 		RelativeLayout uiHolder = new RelativeLayout(getContext());
 		uiHolder.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
 		uiHolder.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		
 		//Add the text field that will take the users answer.
 		answerTextField = new EditText(getContext());
 		answerTextField.setBackgroundColor(getResources().getColor(R.color.black));
 		answerTextField.setTextColor(getResources().getColor(R.color.white));
 		answerTextField.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
 		answerTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
 		
 		//Adding a custom TextWatcher to handle the input as we want it to.
 		answerTextField.addTextChangedListener(new TextWatcher() {
 			
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int givenAnswer = Integer.parseInt(s.toString());
				
				//End game if the given answer is correct, else do nothing.
				if(givenAnswer == var1 + var2) {
					endGame();
 				}
 			}
 			
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				//Do nothing.
 			}
 			
 			public void afterTextChanged(Editable s) {
 				//Do nothing.
 			}
 		});
 		
 		textSize = 40;
 		getPainter().setTextSize(textSize);
 		getPainter().setTextAlign(Align.CENTER);
 		
 		uiHolder.addView(answerTextField);
 		getUiList().add(uiHolder);
 	}
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 		
 		//Scale the width and height of the text field if the user view size is changed.
 		int xScaling = 2;
 		int yScaling = 5;
 		answerTextField.setWidth(w/xScaling);
 		answerTextField.setHeight(h/yScaling);
 	}
 
 	@Override
 	protected void updateGame() {
 		//This game has no logic that needs to be continuously updated.
 	}
 
 	@Override
 	protected void updateGraphics(Canvas c) {
 		
 		//Paint background color
 		c.drawARGB(ALPHA, RED, GREEN, BLUE);
 		
 		//Draw text.
 		c.drawText(exerciseText, getWidth()/2, textSize*2, getPainter());
 	}
 
 }
