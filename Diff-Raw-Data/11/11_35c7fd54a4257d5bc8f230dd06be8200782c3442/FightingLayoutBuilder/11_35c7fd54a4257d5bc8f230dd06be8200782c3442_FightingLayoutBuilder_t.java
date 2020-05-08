 /*
 * Copyright (c) 2013, TeamCMPUT301F13T02
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 
 * Neither the name of the {organization} nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package ca.ualberta.CMPUT301F13T02.chooseyouradventure;
 
 import android.graphics.Color;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * This class generates the GUI asociated with fighting.
  * It displays 6 things:
  * 
  * Current Health
  * Current Treasure
  * Current Enemy Health
  * Recent change in Health
  * Recent change in Treasure
  * Recent Change in Enemy Health
  * 
  *
  */
 public class FightingLayoutBuilder {
 	protected void updateFightView(LinearLayout fightingLayout,ViewPageActivity pageActivity, Story story, Page page) {
 		fightingLayout.removeAllViews();
 		if(pageActivity.isOnEntry() == true){
 			
 			if(story.getFirstpage().getId().equals(page.getId())){
 				Counters counter = new Counters();
 				counter.setBasic("0","100");
 				story.setPlayerStats(counter);
 			}
 			story.getPlayerStats().setEnemyHpStat(page.getEnemyHealth());
 		}
 		
 		TextView fightingUpdate = new TextView(pageActivity);
 		TextView healthView = new TextView(pageActivity);
 		TextView treasureView = new TextView(pageActivity);
 		TextView enemyView = new TextView(pageActivity);
 		Counters stat = story.getPlayerStats();
 		
 		healthView.setTextColor(Color.BLUE);
 		healthView.setText(pageActivity.getString(R.string.currentHealth) + " " + stat.getPlayerHpStat());
 		fightingLayout.addView(healthView);
 		
 		treasureView.setTextColor(Color.YELLOW);
 		treasureView.setText(pageActivity.getString(R.string.currentTreasure) + " " + stat.getTreasureStat());
 		fightingLayout.addView(treasureView);
 		
 		if(page.getFightingState() == true){
			if(stat.getEnemyHpStat() > 0){
				enemyView.setTextColor(Color.RED);
				enemyView.setText(pageActivity.getString(R.string.enemyHealthColon) + " " + stat.getEnemyHpStat());
				fightingLayout.addView(enemyView);
				story.getPlayerStats().setEnemyRange(true);
			}
 		}
 		else {
 			story.getPlayerStats().setEnemyRange(false);
 		}
 		
 		String displayChanges = "\n";
 
 		if(stat.getEnemyHpChange() != 0) {
 			displayChanges += stat.getHitMessage() + "\n";
 			displayChanges += page.getEnemyName();
 
 			if(stat.getEnemyHpChange() <= 0) 
 				displayChanges += " " + pageActivity.getString(R.string.gained) + " ";
 			else 
 				displayChanges += " " + pageActivity.getString(R.string.lost) + " ";
 
 			displayChanges += "" + Math.abs(stat.getEnemyHpChange()) + " " + pageActivity.getString(R.string.hitpoints) + "\n";
 		}
 		if(stat.getPlayerHpChange() != 0){
 			displayChanges += stat.getDamageMessage() + "\n";
 			displayChanges += pageActivity.getString(R.string.you);
 
 			if(stat.getPlayerHpChange() <= 0)
 				displayChanges += " " + pageActivity.getString(R.string.gained) + " ";
 			else
 				displayChanges += " " + pageActivity.getString(R.string.lost) + " ";
 
 			displayChanges += "" + Math.abs(stat.getPlayerHpChange()) +" " +  pageActivity.getString(R.string.hitpoints) + "\n";
 		}
 		if(stat.getTreasureChange() != 0) {
 			displayChanges += stat.getTreasureMessage() + "\n";
 			displayChanges += pageActivity.getString(R.string.you);
 
 			if(stat.getTreasureChange() <= 0)
 				displayChanges += " " + pageActivity.getString(R.string.lost) + " ";
 			else 
 				displayChanges += " " + pageActivity.getString(R.string.gained) + " ";
 
 			displayChanges += "" + Math.abs(stat.getTreasureChange()) + " " +  pageActivity.getString(R.string.coinsWorth);
 		}
 		
 		fightingUpdate.setTextColor(Color.GREEN);
 		fightingUpdate.setText(displayChanges);
 		fightingLayout.addView(fightingUpdate);
 	}
 
 
 }
