 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugrd, Andreas Roln
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.modules.mathModule.activity;
 
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.chalmers.dat255.group09.Alarmed.R;
 import edu.chalmers.dat255.group09.Alarmed.activity.BaseActivationActivity;
 import edu.chalmers.dat255.group09.Alarmed.modules.mathModule.controller.MathController;
 import edu.chalmers.dat255.group09.Alarmed.modules.mathModule.model.MathProblem;
 import edu.chalmers.dat255.group09.Alarmed.modules.mathModule.model.MathProblemGenerator;
 import edu.chalmers.dat255.group09.Alarmed.modules.mathModule.model.problemTypes.MathProblemType;
 
 /**
  * 
  * @author Joakim Persson
  * 
  */
 public class MathActivity extends BaseActivationActivity {
 
 	private MathController controller;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_math);
 		controller = new MathController(new MathProblemGenerator());
 		generateNewMathProblem();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_math, menu);
 		return true;
 	}
 
 	/**
 	 * A callback for when the submit button has been pressed in the
 	 * activity_math gui.
 	 * 
 	 * @param view
 	 *            The current view
 	 */
 	public void onSubmitButtonPressed(View view) {
 		EditText answerField = (EditText) findViewById(R.id.activity_math_answer_text);
 		String text = answerField.getText().toString().replace(" ", "");
 
 		if (isInputValid(text)) {
 			evaluateAnswer(text);
 		} else {
 			displayErrorMessage("Please Enter Something!");
 		}
 
 		answerField.setText("");
 	}
 
 	private boolean isInputValid(String text) {
 		return text.length() != 0;
 	}
 
 	/**
 	 * Evaluate the answer the user entered. If it is correct then generate a
 	 * new answer or if the user has answered enough questions then stop the
 	 * alarm
 	 * 
 	 * @param text
 	 *            The answer the user entered
 	 */
 	private void evaluateAnswer(String text) {
 		int answer = Integer.parseInt(text);
 
 		if (isWrongAnswer(answer)) {
 			displayErrorMessage("OOOOOOO not good....");
 		}
 
 		if (controller.isComplete()) {
 			super.stopAlarm();
 		}

		generateNewMathProblem();
 	}
 
 	/**
 	 * Evaluate if the user entered right or wrong answer
 	 * 
 	 * @param answer
 	 *            The users answer
 	 * @return If it was correct or not
 	 */
 	private boolean isWrongAnswer(int answer) {
 		return !controller.checkAnswer(answer);
 	}
 
 	/**
 	 * Display an error message to the user
 	 * 
 	 * @param message
 	 *            The message to the user
 	 */
 	private void displayErrorMessage(String message) {
 		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * Generate a new MathProblem and update the view with the problem text from
 	 * the new problem
 	 */
 	private void generateNewMathProblem() {
 		MathProblem problem = controller.generateNewProblem();
 		MathProblemType problemType = problem.getProblemType();
 
 		int[] nbrs = problem.getNumbers();
 
 		setProblemHeader(problemType);
 		setProblemText(nbrs, problemType);
 	}
 
 	private void setProblemText(int[] nbrs, MathProblemType problemType) {
 		TextView textView = (TextView) findViewById(R.id.math_activity_problem_text);
 		textView.setText(problemType.getFormattedProblem(nbrs));
 	}
 
 	private void setProblemHeader(MathProblemType problemType) {
 		TextView textHeader = (TextView) findViewById(R.id.math_activity_problem_header);
 		textHeader.setText(problemType.getProblemHeader());
 	}
 }
