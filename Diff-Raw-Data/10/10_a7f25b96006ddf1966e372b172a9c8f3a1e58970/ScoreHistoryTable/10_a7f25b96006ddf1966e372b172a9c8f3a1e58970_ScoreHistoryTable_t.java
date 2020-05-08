 /**
  * Copyright (C) 2012 Jesse Wilson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.publicobject.rounds;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.view.Gravity;
 import android.view.ViewGroup;
 import android.widget.HorizontalScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public final class ScoreHistoryTable {
     private static final int MIN_LENGTH = 6;
     private static final int STRIPE_COLOR = Color.parseColor("#161616");
 
     private final Context context;
     private final TableLayout runningScoresTable;
     private final TableLayout currentScoresTable;
     private final HorizontalScrollView runningScoresScroller;
     private final Game model;
 
     private TableRow runningScoresHeader;
     private TableRow[] runningScoresRows;
     private TextView[] playerTotals;
 
     public ScoreHistoryTable(Context context,
                              TableLayout runningScoresTable,
                              TableLayout currentScoresTable,
                              HorizontalScrollView runningScoresScroller,
                              Game model) {
         if (model.playerCount() == 0 || model.roundCount() == 0) {
             throw new IllegalArgumentException();
         }
 
         this.context = context;
         this.runningScoresTable = runningScoresTable;
         this.currentScoresTable = currentScoresTable;
         this.runningScoresScroller = runningScoresScroller;
         this.model = model;
 
         clear();
         roundCountChanged();
     }
 
     /**
      * Clears the rows and columns of the table. These will be reconstructed
      * with fresh data the next time {@link #roundCountChanged} is invoked.
      */
     public void clear() {
         runningScoresTable.removeAllViews();
         currentScoresTable.removeAllViews();
 
         runningScoresHeader = new TableRow(context);
         runningScoresTable.addView(runningScoresHeader);
 
         TableRow currentScoresHeader = new TableRow(context);
         TextView total = newTextView(-1);
         total.setText(pad("Σ"));
         currentScoresHeader.addView(total);
         currentScoresTable.addView(currentScoresHeader);
 
         runningScoresRows = new TableRow[model.playerCount()];
         playerTotals = new TextView[model.playerCount()];
 
         TableLayout.LayoutParams WRAP_PARAMS = new TableLayout.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 
         for (int p = 0; p < model.playerCount(); p++) {
             TableRow playerRunningScores = new TableRow(context);
             runningScoresRows[p] = playerRunningScores;
             runningScoresTable.addView(playerRunningScores);
 
             TableRow currentScoresRow = new TableRow(context);
 
             TextView playerTotal = newTextView(p);
             playerTotal.setText(totalString(p));
             playerTotals[p] = playerTotal;
             playerTotal.setTypeface(null, Typeface.BOLD);
             currentScoresRow.addView(playerTotal);
 
             TextView playerName = newTextView(p);
             playerName.setGravity(Gravity.LEFT);
             playerName.setText(model.playerName(p));
             currentScoresRow.addView(playerName);
 
             currentScoresTable.addView(currentScoresRow, WRAP_PARAMS);
         }
 
         runningScoresTable.setColumnStretchable(0, true);
     }
 
     public void scoreChanged(int player, int round) {
         TextView roundScore = (TextView) runningScoresRows[player].getChildAt(round);
         roundScore.setText(getScore(player, round));
         playerTotals[player].setText(totalString(player));
     }
 
     private String totalString(int player) {
         int total = model.playerTotal(player);
         return Integer.toString(total);
     }
 
     private String getScore(int player, int round) {
         int score = model.playerScore(player, round);
         return score != 0 ? Integer.toString(score) : "";
     }
 
     public void roundCountChanged() {
         for (int r = runningScoresHeader.getChildCount(); r < model.roundCount(); r++) {
             TextView roundNumber = newTextView(-1);
             roundNumber.setText(pad(String.valueOf(r + 1)));
             runningScoresHeader.addView(roundNumber);
 
             for (int p = 0; p < model.playerCount(); p++) {
                 TextView playerScore = newTextView(p);
                 playerScore.setText(getScore(p, r));
                 runningScoresRows[p].addView(playerScore);
             }
         }
         runningScoresTable.post(scroll);
     }
 
     private final Runnable scroll = new Runnable() {
         @Override public void run() {
             runningScoresScroller.smoothScrollTo(runningScoresTable.getWidth(),
                     runningScoresScroller.getScrollY());
 
         }
     };
 
     /**
      * Returns a string ending with {@code s} of at least {@link #MIN_LENGTH}
      * characters.
      */
     private String pad(String s) {
         String spaces = "          ";
         int toAdd = MIN_LENGTH - s.length();
         return toAdd <= 0 ? s : (spaces.substring(0, toAdd) + s);
     }
 
     private TextView newTextView(int player) {
         TextView result = new TextView(context);
        result.setTextAppearance(context, R.style.TextAppearance_Rounds_Medium);
         result.setGravity(Gravity.RIGHT);
         result.setPadding(20, 0, 20, 0); // TODO: vary based on device DPI?
         if (player != -1) {
             result.setTextColor(model.playerColor(player));
             if (player % 2 == 0) {
                 result.setBackgroundColor(STRIPE_COLOR);
             }
         }
         return result;
     }
 }
