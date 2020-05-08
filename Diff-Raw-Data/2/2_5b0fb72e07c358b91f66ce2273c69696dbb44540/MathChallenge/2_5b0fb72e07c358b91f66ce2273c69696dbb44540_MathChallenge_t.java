 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.challenge.math;
 
 import java.util.Random;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.activity.ChallengeActivity;
 import se.chalmers.dat255.sleepfighter.challenge.Challenge;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeParamsReadWriter;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengePrototypeDefinition;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengePrototypeDefinition.ParameterDefinition;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeConfigSet;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import se.chalmers.dat255.sleepfighter.utils.math.*;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.webkit.WebSettings;
 import android.webkit.WebSettings.RenderPriority;
 import android.webkit.WebView;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 /**
  * A Class for randomly generating simple arithmetic challenges.
  * 
 * @author Laszlo Sall Vesselenyi, Danny Lam, Johan Hasselqvist, Eric Arnebäck
  */
 
 public class MathChallenge implements Challenge {
 	/**
 	 * PrototypeDefinition for MathChallenge.
 	 *
 	 * @version 1.0
 	 * @since Oct 5, 2013
 	 */
 	public static class PrototypeDefinition extends ChallengePrototypeDefinition {{
 		setType( ChallengeType.MATH );
 		
 		add( "hard_problems", PrimitiveValueType.BOOLEAN, false );
 	}}
 
 	//MathProblem problem;
 	private ProblemType problemType;
 	
 	// The problem in string format. Uses jqmath to represent math formulas. 
 	private String problemString;
 	private int problemSolution;
 	
 	private Context context;
 	
 	private ChallengeConfigSet config;
 	
 	private Random rng = new Random();
 	
 	public boolean getHardProblemsSetting() {
 		ChallengeParamsReadWriter readWriter = new ChallengeParamsReadWriter( this.config, ChallengeType.MATH );
 		ChallengePrototypeDefinition protdef = new MathChallenge.PrototypeDefinition();
 		ParameterDefinition paramDef = protdef.get("hard_problems");
 		
 		// TODO: for some reason it always returns false.
 		return readWriter.getBoolean( paramDef.getKey(), (Boolean) paramDef.getDefaultValue() );
 	}
 	
 	private void runChallenge() {
 		
 		
 		// create challenge object
 		
 		MathProblem problem = null;
 		
 		if(problemType == ProblemType.differentiation) {
 			problem = new DifferentiationProblem(context);			
 		} else if(problemType == ProblemType.gcd) {
 			problem = new GCDProblem(context);			
 		} else if(problemType == ProblemType.prime) {
 			problem = new PrimeFactorizationProblem(context);			
 		}else if(problemType == ProblemType.simple) {
 			problem = new SimpleProblem();			
 		}else if(problemType == ProblemType.matrix) {
 			problem = new MatrixProblem(context);			
 		}
 		
 		problem.newProblem();
 		this.problemString = problem.render();
 		this.problemSolution = problem.solution();
 	}
 	
 	@Override
 	public void start(final ChallengeActivity activity,  ChallengeConfigSet config) {
 		this.config = config;
 		
 		this.context = activity;
 		
 		// here we randomize a type.
 		
 		// TODO: for some reason getHardProblemsSetting is always false.
 		boolean hardProblems = true;// getHardProblemsSetting();
 			
 		if(!hardProblems) {
 			this.problemType = ProblemType.simple;
 		} else {
 			// we want a hard challenge
 			do {
 				this.problemType = RandomMath.randomEnum(rng, ProblemType.class);		
 			}while(this.problemType == ProblemType.simple);
 		}
 		
 		// create a new challenge
 		runChallenge();
 		
 		commonStart(activity);
 	}
 	
 	private void commonStart(final ChallengeActivity activity) {
 		activity.setContentView(R.layout.alarm_challenge_math);
 		
 		
 		final EditText editText = (EditText) activity
 				.findViewById(R.id.answerField);
 		
 		
 		editText.setOnEditorActionListener(new OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				boolean handled = false;
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					handleAnswer(editText, activity);
 					handled = true;
 				}
 				return handled;
 			}
 		});
 
 		// make the keyboard appear.
 	/*	InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 	*/
 		setupWebview(activity);
 		renderMathProblem(activity);
 
 	}
 	
 	private final static String PROBLEM_STRING = "problem_string";
 	private final static String PROBLEM_SOLUTION = "problem_solution";
 	private final static String PROBLEM_TYPE = "problem_type";
 	
 	@Override
 	public void start( ChallengeActivity activity, Bundle state ) {
 		
 		this.context = activity;
 		this.problemString = state.getString(PROBLEM_STRING);
 		this.problemSolution = state.getInt(PROBLEM_SOLUTION);
 		this.problemType = (ProblemType) state.getSerializable(PROBLEM_TYPE);
 		
 		commonStart(activity);
 	}
 
 	@Override
 	public Bundle savedState() {
 		Bundle outState = new Bundle();
 
 		outState.putString(PROBLEM_STRING, this.problemString);
 		outState.putInt(PROBLEM_SOLUTION, this.problemSolution);
 		outState.putSerializable(PROBLEM_TYPE, this.problemType);
 		
 		return outState;
 	}
 	
 	
 	@SuppressLint({ "SetJavaScriptEnabled", "NewApi", "InlinedApi" })
 	private void setupWebview(final Activity activity) {
 	
 					
 		
 		final WebView w = (WebView)  activity.findViewById(R.id.math_webview);
 		w.getSettings().setJavaScriptEnabled(true);
 		
 		// make rendering faster.
 		w.getSettings().setRenderPriority(RenderPriority.HIGH);
 		w.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
 		
 		if(Build.VERSION.SDK_INT >= 11)
 			w.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 	}
 	
 	private String getStyleSheet() {
 		return 
 				"<style type=\"text/css\">" +
 		"body {color: white;}" +
 		"div {text-align: center; font-size: 1000%;}" +
 		"</style>";
 	}
 	
 	/*
 	 *We use jqmath to render math formulas. jqmath is a javascript library,
 	 *so therefore we need to use a WebVIew to render the formulas.  
 	 */
 	private void renderMathProblem(final Activity activity) {
 		
 		final String open_html =
 				"<!DOCTYPE html><html lang=\"en\" xmlns:m=\"http://www.w3.org/1998/Math/MathML\"><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" href=\"file:///android_asset/jqmath-0.4.0.css\"><script src=\"file:///android_asset/jquery-1.4.3.min.js\"></script><script src=\"file:///android_asset/jqmath-etc-0.4.0.min.js\"></script></head><html>";
 		final String close_html = "</html>";
 		
 		final WebView w = (WebView)  activity.findViewById(R.id.math_webview);
 		
 		String problem = "<p style=\"text-align: center;\">" + this.problemString + "</p>";
 		
 		String html = new StringBuilder().append(open_html).append(this.getStyleSheet()).append(problem).append(close_html).toString();
 		
 		w.loadDataWithBaseURL("file:///android_asset", html, "text/html", "utf-8", "");	
 		w.setBackgroundColor(0x00000000);
 	}
 		
 	/**
 	 * Handles what will happen when you answer
 	 */
 	private void handleAnswer(final EditText editText,
 			final ChallengeActivity activity) {
 		boolean correctAnswer = false;
 		try {
 			int guess = Integer.parseInt(editText.getText().toString());
 			int solution = this.problemSolution;
 			Debug.d(guess + "");
 			Debug.d(solution + "");
 			
 			if (guess == solution) {
 				activity.complete();
 				correctAnswer = true;
 				Toast.makeText(activity.getBaseContext(), "Alarm deactivated!",
 						Toast.LENGTH_SHORT).show();
 			}
 		} catch (NumberFormatException e) {
 			// Handles exception when the user answer with empty strings
 		}
 		if (!correctAnswer) {
 			// somehow reload here. 
 			Toast.makeText(activity.getBaseContext(), "Wrong answer!",
 					Toast.LENGTH_SHORT).show();
 			runChallenge();
 		
 			renderMathProblem(activity);
 			editText.setText("");
 		}
 	}
 	
 
 
 }
