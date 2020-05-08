 /**
  *     Copyright (C) 2011 Julien SMADJA <julien dot smadja at gmail dot com>
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package com.anzymus.neogeo.hiscores.success;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.anzymus.neogeo.hiscores.common.IntegerToRank;
 import com.anzymus.neogeo.hiscores.domain.Achievement;
 import com.anzymus.neogeo.hiscores.domain.Player;
 import com.anzymus.neogeo.hiscores.domain.Scores;
 
 public abstract class AbstractGenreTitleStrategy extends AbstractTitleStrategy {
 
 	private static final int THRESHOLD = 3;
 
 	@Override
 	public boolean isUnlockable(Player player) {
 		List<Scores> scoresByGame = titleService.getScoresByGameGenre(getGenre());
 		if (scoresByGame.isEmpty()) {
 			return false;
 		}
 		int countNok = 0;
 		int unplayedGame = 0;
 		int countOk = 0;
 		for (Scores scores : scoresByGame) {
 			int rank = scores.getRank(player);
 			if (rank == Integer.MAX_VALUE) {
 				unplayedGame++;
 			} else if (rank > 3) {
 				countNok++;
 			} else {
 				countOk++;
 			}
 		}
 		if (unplayedGame == scoresByGame.size()) {
 			return false;
 		}
 		return countOk >= THRESHOLD && countNok <= countOk;
 	}
 
 	@Override
 	public Achievement getAchievementFor(Player player) {
 		List<Scores> scoresByGame = titleService.getScoresByGameGenre(getGenre());
 		if (scoresByGame.isEmpty()) {
 			Achievement achievement = new Achievement(getTitle(), 0);
 			Step step = new Step("Have a rank Between 1st and 3rd place in " + getGenre() + " games", false);
 			achievement.addStep(step);
 			return achievement;
 		}
 		int countNok = 0;
 		int countOk = 0;
 		List<Step> steps = new ArrayList<Step>();
 		for (Scores scores : scoresByGame) {
 			boolean complete = false;
 			int rank = scores.getRank(player);
 			if (rank == Integer.MAX_VALUE) {
 			} else if (rank > 3) {
 				countNok++;
 			} else {
 				complete = true;
 				countOk++;
 			}
 			if (rank != Integer.MAX_VALUE) {
 				String gameName = scores.asSortedList().get(0).getGame().getName();
 				steps.add(new Step(gameName, IntegerToRank.getOrdinalFor(rank), complete));
 			}
 		}
 
 		boolean allIsComplete = countOk >= THRESHOLD && countNok <= countOk;
 		int percent;
 		if (allIsComplete) {
 			percent = countOk > 0 ? 100 : 0;
 		} else {
			int ceil = (int) Math.ceil((double) scoresByGame.size() / 2);
 			if (ceil < 3) {
 				ceil = 3;
 			}
 			percent = percent(countOk, ceil);
 		}
 		Achievement achievement = new Achievement(title, percent);
 		for (Step step : steps) {
 			if (allIsComplete) {
 				if (step.isComplete()) {
 					achievement.addStep(step);
 				}
 			} else {
 				achievement.addStep(step);
 			}
 		}
 		return achievement;
 	}
 
 	protected abstract String getGenre();
 
 }
