 /*
  * Copyright 2012 Alberto Salmerón Moreno
  * 
  * This file is part of SimpleAndroidTest - https://github.com/berti/SimpleAndroidTest
  * 
  * SimpleAndroidTest is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SimpleAndroidTest is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SimpleAndroidTest.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.primoberti.simpleandroidtest;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * Activity for showing the results of a test.
  * 
  * @author berti
  */
 public class ResultsActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.results);
 
 		Bundle extras = getIntent().getExtras();
 		int totalQuestions = extras.getInt(TestActivity.EXTRA_TOTAL_QUESTIONS);
 		int correctAnswers = extras.getInt(TestActivity.EXTRA_CORRECT_ANSWERS);
 		int score = extras.getInt(TestActivity.EXTRA_SCORE);
 		int percentageScore = correctAnswers * 100 / totalQuestions;
 
 		TextView textView = (TextView) findViewById(R.id.finalScore);
 		textView.setText(percentageScore + "%");
 
 		textView = (TextView) findViewById(R.id.resultsOverview);
 		textView.setText(String.format(
 				getResources().getString(R.string.results_overview),
				totalQuestions, correctAnswers));
 
 		Button button = (Button) findViewById(R.id.closeResultsButton);
 		button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(ResultsActivity.this,
 						SimpleAndroidTestActivity.class);
 				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 			}
 		});
 	}
 
 }
