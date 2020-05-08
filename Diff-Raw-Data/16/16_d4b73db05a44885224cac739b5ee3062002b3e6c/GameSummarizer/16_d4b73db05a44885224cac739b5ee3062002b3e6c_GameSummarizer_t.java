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
 
 import android.text.SpannableStringBuilder;
 import android.text.TextPaint;
 import android.text.style.ClickableSpan;
 import android.text.style.UnderlineSpan;
 import android.view.View;
 import java.util.ArrayList;
 import java.util.List;
 
 final class GameSummarizer {
     private static final String[] DEFEAT_WORDS = {
             "defeated",
             "beat",
             "conquered",
             "destroyed",
             "pwned",
             "got lucky vs.",
             "narrowly defeated",
     };
 
     private ClickableSpan defeatWordSpan;
     private UnderlineSpan underlineSpan;
     private String defeatWord = DEFEAT_WORDS[0];
 
     public GameSummarizer(final ShareActivity activity) {
         defeatWordSpan = new ClickableSpan() {
             int index = 0;
             @Override public void onClick(View view) {
                 defeatWord = DEFEAT_WORDS[++index % DEFEAT_WORDS.length];
                 activity.updateMessagePreview();
             }
             @Override public void updateDrawState(TextPaint ds) {
             }
         };
         underlineSpan = new UnderlineSpan();
     }
 
     public CharSequence summarize(Game game, String url) {
         List<String> winners = new ArrayList<String>();
         List<String> losers = new ArrayList<String>();
 
         if (game.getWinCondition() == WinCondition.NONE) {
             for (int p = 0; p < game.playerCount(); p++) {
                 winners.add(game.playerName(p));
             }
         } else {
             int winningTotal = game.winningTotal();
             for (int p = 0; p < game.playerCount(); p++) {
                 if (winningTotal == game.playerTotal(p)) {
                     winners.add(game.playerName(p));
                 } else {
                     losers.add(game.playerName(p));
                 }
             }
         }
 
         SpannableStringBuilder ssb = new SpannableStringBuilder();
         appendNames(ssb, winners);
 
         if (game.getWinCondition() == WinCondition.NONE) {
             ssb.append(" played ");
         } else if (!losers.isEmpty()) {
             ssb.append(" ");
             ssb.append(defeatWord);
             if (defeatWordSpan != null && underlineSpan != null) {
                 ssb.setSpan(defeatWordSpan,
                         ssb.length() - defeatWord.length(), ssb.length(), 0);
                 ssb.setSpan(underlineSpan,
                         ssb.length() - defeatWord.length(), ssb.length(), 0);
             }
             ssb.append(" ");
             appendNames(ssb, losers);
             ssb.append(" at ");
         } else {
             ssb.append(" tied at ");
         }
 
         String gameName = game.getName();
        if (gameName == null || gameName.length() == 0) {
             gameName = "a game";
         }
         ssb.append(gameName).append(". ");
         ssb.append(url);
         return ssb;
     }
 
     private static void appendNames(SpannableStringBuilder message, List<String> players) {
         for (int i = 0; i < players.size(); i++) {
             String player = players.get(i);
             if (i == 0) {
                 message.append(player);
             } else if (i < players.size() - 1) {
                 message.append(", ").append(player);
             } else {
                 message.append(" and ").append(player);
             }
         }
     }
 }
