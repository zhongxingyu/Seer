 /*
  * Magic Annotator - The only thing you need to write down whatever you want.
  * Copyright (C) 2013 Nahuel Barrios <barrios.nahuel@gmail.com>.
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * TrucoAnnotatorActivity.java Created by: Nahuel Barrios: 01/03/2012, 05:55:58.
  */
 package com.nbempire.android.magicannotator.component.activity.annotator;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.animation.AnimationUtils;
 import android.widget.TextSwitcher;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 import com.nbempire.android.magicannotator.GameKeys;
 import com.nbempire.android.magicannotator.R;
 import com.nbempire.android.magicannotator.listener.TrucoScoreListener;
 import com.nbempire.android.magicannotator.util.android.analytics.GoogleAnalyticsUtil;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * {@link Activity} To annotate the score of a Truco game.
  *
  * @author Nahuel Barrios.
  * @since 1
  */
 public class TrucoAnnotatorActivity extends Activity {
 
     /**
      * Tag for class' log.
      */
     private static final String LOG_TAG = "TrucoAnnotatorActivity";
 
     /**
      * Key para el score del equipo "Nosotros", para controlar el giro del telefono.
      */
     private static final String SCORE_TEAM_1 = "scoreTeam1";
 
     /**
      * Key para el score del equipo "Ellos", para controlar el giro del telefono.
      */
     private static final String SCORE_TEAM_2 = "scoreTeam2";
 
     private static final String TEAM_1_STATUS = "team1Status";
 
     private static final String TEAM_2_STATUS = "team2Status";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         GoogleAnalyticsTracker.getInstance().trackPageView(GoogleAnalyticsUtil.generatePageName(LOG_TAG));
         setContentView(R.layout.trucoannotator);
 
         // Setteo las acciones para los elementos del equipo "nosotros"
         List<Integer> viewsToDisable = new ArrayList<Integer>();
         viewsToDisable.add(R.id.trucoAnnotator_labelTeam2);
         viewsToDisable.add(R.id.trucoAnnotator_scoreTeam2);
         viewsToDisable.add(R.id.trucoAnnotator_substractButtonTeam2);
         setActions(R.id.trucoAnnotator_scoreTeam1, R.string.trucoAnnotator_youWin,
                    R.id.trucoAnnotator_labelTeam1, viewsToDisable);
 
         // Setteo las acciones para los elementos del equipo "ellos"
         viewsToDisable.clear();
         viewsToDisable.add(R.id.trucoAnnotator_labelTeam1);
         viewsToDisable.add(R.id.trucoAnnotator_scoreTeam1);
         viewsToDisable.add(R.id.trucoAnnotator_substractButtonTeam1);
         setActions(R.id.trucoAnnotator_scoreTeam2, R.string.trucoAnnotator_theyWin,
                    R.id.trucoAnnotator_labelTeam2, viewsToDisable);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putCharSequence(SCORE_TEAM_1, ((TextView) ((TextSwitcher) findViewById(R.id.trucoAnnotator_scoreTeam1)).getCurrentView()).getText());
         outState.putCharSequence(SCORE_TEAM_2, ((TextView) ((TextSwitcher) findViewById(R.id.trucoAnnotator_scoreTeam2)).getCurrentView()).getText());
 
         outState.putBoolean(TEAM_1_STATUS, findViewById(R.id.trucoAnnotator_scoreTeam1).isEnabled());
         outState.putBoolean(TEAM_2_STATUS, findViewById(R.id.trucoAnnotator_scoreTeam2).isEnabled());
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         ((TextSwitcher) findViewById(R.id.trucoAnnotator_scoreTeam1)).setText(savedInstanceState.getCharSequence(SCORE_TEAM_1));
         ((TextSwitcher) findViewById(R.id.trucoAnnotator_scoreTeam2)).setText(savedInstanceState.getCharSequence(SCORE_TEAM_2));
 
         boolean isTeamEnabled = savedInstanceState.getBoolean(TEAM_1_STATUS);
         if (!isTeamEnabled) {
             List<Integer> controlsId = new ArrayList<Integer>();
             controlsId.add(R.id.trucoAnnotator_labelTeam1);
             controlsId.add(R.id.trucoAnnotator_scoreTeam1);
             controlsId.add(R.id.trucoAnnotator_substractButtonTeam1);
             disableControls(controlsId);
         }
 
         isTeamEnabled = savedInstanceState.getBoolean(TEAM_2_STATUS);
         if (!isTeamEnabled) {
             List<Integer> controlsId = new ArrayList<Integer>();
             controlsId.add(R.id.trucoAnnotator_labelTeam2);
             controlsId.add(R.id.trucoAnnotator_scoreTeam2);
             controlsId.add(R.id.trucoAnnotator_substractButtonTeam2);
             disableControls(controlsId);
         }
     }
 
     /**
      * Setteo las acciones para un equipo, ya sea nosotros o ellos, dependiendo de los ID de los resources.
      *
      * @param teamScoreId
      *         El ID del {@link TextView} donde se anotar� el score.
      * @param labelForWinnerTeamId
      *         ID del recurso string que se utilizar� cuando gane el equipo.
      * @param teamLabelId
      *         El ID del {@link TextView} con el label del equipo en el cu�l se debe hacer tap para sumar un punto.
      * @param viewsIdToDisable
      *         List of Integer containing one element for each View to disable when one team wins.
      *
      * @since 1
      */
     private void setActions(int teamScoreId, int labelForWinnerTeamId, int teamLabelId, List<Integer> viewsIdToDisable) {
 
         List<View> viewsToDisable = new ArrayList<View>();
         for (int eachViewId : viewsIdToDisable) {
             viewsToDisable.add(findViewById(eachViewId));
         }
 
         TextSwitcher teamScore = (TextSwitcher) findViewById(teamScoreId);
         TrucoScoreListener listener = new TrucoScoreListener(teamScore, getText(labelForWinnerTeamId), viewsToDisable);
         initializeTextSwitcher(teamScore, listener);
 
         TextView teamLabel = (TextView) findViewById(teamLabelId);
         teamLabel.setOnTouchListener(listener);
     }
 
     /**
      * Reset the entire activity by setting scores to zero and enabling all controls.
      *
      * @since 1
      */
     public void resetGame(View view) {
         Log.d(LOG_TAG, "--> resetGame() from view: " + view.getId());
         resetScoreFor(R.id.trucoAnnotator_scoreTeam1);
         resetScoreFor(R.id.trucoAnnotator_scoreTeam2);
 
         enableAllControls();
     }
 
     /**
      * Substracts one point to the corresponding team score depending on the {@code callerView}.
      *
      * @param callerView
      *         The view that has called this method.
      *
      * @since 6
      */
     public void substractScore(View callerView) {
         int teamScoreToSubstractId = -1;
 
         switch (callerView.getId()) {
             case R.id.trucoAnnotator_substractButtonTeam1:
                 teamScoreToSubstractId = R.id.trucoAnnotator_scoreTeam1;
                 break;
             case R.id.trucoAnnotator_substractButtonTeam2:
                 teamScoreToSubstractId = R.id.trucoAnnotator_scoreTeam2;
                 break;
         }
 
         if (teamScoreToSubstractId != -1) {
             TextSwitcher scoreToUpdate = (TextSwitcher) findViewById(teamScoreToSubstractId);
             String currentValueString = ((TextView) scoreToUpdate.getCurrentView()).getText().toString();
 
             int currentScore = 0;
             if (!currentValueString.equals("")) {
                 currentScore = Integer.valueOf(currentValueString);
             }
 
             if (currentScore != Integer.parseInt(getText(R.string.defaultInitialGameScore).toString())) {
                 int updatedScore = currentScore - GameKeys.TRUCO_INCREMENT;
 
                 Log.i(LOG_TAG, "Updating score to: " + updatedScore);
                 scoreToUpdate.setText(String.valueOf(updatedScore));
 
                 if (updatedScore == GameKeys.TRUCO_MAX_SCORE_WITHOUT_WIN) {
                     enableAllControls();
                 }
             }
         }
 
     }
 
     /**
      * Set enabled=true to each control from both teams even if they are already enabled.
      *
      * @since 6
      */
     private void enableAllControls() {
         List<Integer> viewsIdToEnable = new ArrayList<Integer>();
         viewsIdToEnable.add(R.id.trucoAnnotator_labelTeam1);
         viewsIdToEnable.add(R.id.trucoAnnotator_labelTeam2);
         viewsIdToEnable.add(R.id.trucoAnnotator_substractButtonTeam1);
         viewsIdToEnable.add(R.id.trucoAnnotator_scoreTeam1);
         viewsIdToEnable.add(R.id.trucoAnnotator_scoreTeam2);
         viewsIdToEnable.add(R.id.trucoAnnotator_substractButtonTeam2);
         setControlsStatus(viewsIdToEnable, true);
     }
 
     /**
      * Set enabled=false to each control from the specified List.
      *
      * @param controlsId
      *         List of int with Views IDs.
      *
      * @since 6
      */
     private void disableControls(List<Integer> controlsId) {
         setControlsStatus(controlsId, false);
     }
 
     /**
      * Set the enabled attribute of the corresponding Views from the {@code controlsId} List based on the {@code enabled} parameter.
      *
      * @param controlsId
      *         List of int with Views IDs.
      * @param enabled
      *         {@code true} to enable controls. {@code false} to disable them.
      *
      * @since 6
      */
     private void setControlsStatus(List<Integer> controlsId, boolean enabled) {
         for (int eachViewId : controlsId) {
             findViewById(eachViewId).setEnabled(enabled);
         }
     }
 
     /**
      * Reset the score for the specified {@code teamScoreId}.
      *
      * @param teamScoreId
      *         The id of the team score to reset.
      *
      * @since 1
      */
     private void resetScoreFor(int teamScoreId) {
        ((TextSwitcher) findViewById(teamScoreId)).setText(this.getText(R.string.defaultInitialGameScore));
     }
 
     /**
      * Initializes the score TextSwitcher for the specified {@code textSwitcher}. It also sets the {@code listener}.
      *
      * @param listener
      *         The listener which will update the text.
      */
     private void initializeTextSwitcher(final TextSwitcher textSwitcher, View.OnTouchListener listener) {
         textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
             @Override
             public View makeView() {
                 TextView textView = new TextView(textSwitcher.getContext());
                 textView.setTextSize(36);
                 return textView;
             }
         });
 
         textSwitcher.setOnTouchListener(listener);
         textSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
         textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
 
         textSwitcher.setText(getText(R.string.defaultInitialGameScore));
     }
 
 }
