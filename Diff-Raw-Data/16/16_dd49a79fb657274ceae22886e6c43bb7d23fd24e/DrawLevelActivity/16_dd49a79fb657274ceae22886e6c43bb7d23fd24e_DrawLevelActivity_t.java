 /*
  * Fling! Solver
  * Copyright (C) 2013 - Jos Expsito <jose.exposito89@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  */
 package com.eggsoftware.flingsolver.gui;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 import com.eggsoftware.flingsolver.R;
 import com.eggsoftware.flingsolver.solver.SolutionStep;
 import com.eggsoftware.flingsolver.solver.SolveBoardAsyncTask;
 import com.eggsoftware.flingsolver.solver.SolveBoardAsyncTaskDelegate;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 
 /**
  * Activity where the user can draw the level to solve.
  */
 public class DrawLevelActivity extends Activity implements SolveBoardAsyncTaskDelegate {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.activity_draw_level);		
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		// Make the board editable
 		BoardCanvas boardCanvas = (BoardCanvas)this.findViewById(R.id.boardCanvas);
 		boardCanvas.setAcceptEditFlings(true);
 		
 		// Initialize AdMob
	    AdView adView = (AdView) findViewById(R.id.adView);
 	    adView.loadAd(new AdRequest());
 	}
 	
 	/**
 	 * Called when the solve button is pressed.
 	 * Solves the level and shows the steps.
 	 */
 	public void onSolveLevelClicked(View solveLevelButton) {
 		BoardCanvas boardCanvas = (BoardCanvas)this.findViewById(R.id.boardCanvas);
 		SolveBoardAsyncTask task = new SolveBoardAsyncTask(this, boardCanvas.getBoardRepresentation());
 		
 		try {
 			task.execute();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Called when the level is solved or not have solution.
 	 */
 	@Override
 	public void boardSolved(SolveBoardAsyncTask task, ArrayList<SolutionStep> solution) {
 		if (solution == null) {
 			Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.can_not_solve_level), Toast.LENGTH_SHORT).show();
 		} else {
 			Intent intent = new Intent(this, DrawSolutionActivity.class);
 			intent.putParcelableArrayListExtra(DrawSolutionActivity.SOLUTION_KEY, solution);
 			this.startActivity(intent);	
 		}
 	}
 	
 }
