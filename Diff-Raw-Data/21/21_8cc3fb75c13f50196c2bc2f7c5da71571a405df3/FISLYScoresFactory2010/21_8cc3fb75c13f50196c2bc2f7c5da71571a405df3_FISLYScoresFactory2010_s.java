 /*
 	cursus - Race series management program
 	Copyright 2012  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fisly.cursus.scoring;
 
 import eu.lp0.cursus.scoring.data.OverallPenaltiesData;
 import eu.lp0.cursus.scoring.data.OverallPointsData;
 import eu.lp0.cursus.scoring.data.OverallPositionData;
 import eu.lp0.cursus.scoring.data.RaceDiscardsData;
 import eu.lp0.cursus.scoring.data.RaceLapsData;
 import eu.lp0.cursus.scoring.data.RacePenaltiesData;
 import eu.lp0.cursus.scoring.data.RacePointsData;
 import eu.lp0.cursus.scoring.data.RacePositionsData;
 import eu.lp0.cursus.scoring.data.Scores;
 import eu.lp0.cursus.scoring.scores.base.AbstractScoresFactory;
import eu.lp0.cursus.scoring.scores.impl.AveragingRacePointsData;
 import eu.lp0.cursus.scoring.scores.impl.GenericDiscardCalculator;
 import eu.lp0.cursus.scoring.scores.impl.GenericOverallPenaltiesData;
 import eu.lp0.cursus.scoring.scores.impl.GenericOverallPointsData;
 import eu.lp0.cursus.scoring.scores.impl.GenericOverallPositionData;
 import eu.lp0.cursus.scoring.scores.impl.GenericRaceDiscardsData;
 import eu.lp0.cursus.scoring.scores.impl.GenericRaceLapsData;
 import eu.lp0.cursus.scoring.scores.impl.GenericRacePenaltiesData;
 import eu.lp0.cursus.scoring.scores.impl.GenericRacePointsData;
 import eu.lp0.cursus.scoring.scores.impl.GenericRacePositionsData;
 import eu.lp0.cursus.scoring.scores.impl.NoDiscards;
 import eu.lp0.cursus.scoring.scores.impl.PilotRacePlacingComparator;
 
 public class FISLYScoresFactory2010 extends AbstractScoresFactory {
 	protected static final int RACES_PER_DISCARD = 4;
 
 	@Override
 	public RaceLapsData newRaceLapsData(Scores scores) {
 		return new GenericRaceLapsData<Scores>(scores, false, true);
 	}
 
 	@Override
 	public RacePenaltiesData newRacePenaltiesData(Scores scores) {
 		return new GenericRacePenaltiesData<Scores>(scores, GenericRacePenaltiesData.CumulativeMethod.EVENT);
 	}
 
 	@Override
 	public RacePointsData newRacePointsData(Scores scores) {
		return new AveragingRacePointsData<Scores>(scores, GenericRacePointsData.FleetMethod.RACE, AveragingRacePointsData.AveragingMethod.AFTER_DISCARDS,
				AveragingRacePointsData.Rounding.ROUND_HALF_UP);
 	}
 
 	@Override
 	public RacePositionsData newRacePositionsData(Scores scores) {
 		return new GenericRacePositionsData<Scores>(scores, GenericRacePositionsData.EqualPositioning.WITHOUT_LAPS);
 	}
 
 	@Override
 	public RaceDiscardsData newRaceDiscardsData(Scores scores) {
 		return new GenericRaceDiscardsData<Scores>(scores, new GenericDiscardCalculator(RACES_PER_DISCARD));
 	}
 
 	@Override
 	public OverallPenaltiesData newOverallPenaltiesData(Scores scores) {
 		return new GenericOverallPenaltiesData<Scores>(scores, 0, new NoDiscards());
 	}
 
 	@Override
 	public OverallPointsData newOverallPointsData(Scores scores) {
 		return new GenericOverallPointsData<Scores>(scores);
 	}
 
 	@Override
 	public OverallPositionData newOverallPositionData(Scores scores) {
 		return new GenericOverallPositionData<Scores>(scores, GenericOverallPositionData.EqualPositioning.IF_REQUIRED,
 				PilotRacePlacingComparator.PlacingMethod.EXCLUDING_DISCARDS);
 	}
 }
