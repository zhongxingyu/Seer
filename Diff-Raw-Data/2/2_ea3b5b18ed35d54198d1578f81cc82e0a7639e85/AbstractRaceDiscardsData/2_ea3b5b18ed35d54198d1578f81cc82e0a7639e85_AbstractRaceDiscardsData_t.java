 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
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
 package eu.lp0.cursus.scoring;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.TreeMap;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ArrayTable;
 import com.google.common.collect.Table;
 
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.util.IntegerSequence;
 
 public abstract class AbstractRaceDiscardsData<T extends ScoredData & RacePointsData> implements RaceDiscardsData {
 	protected final T scores;
 
 	protected final int discards;
 
 	public AbstractRaceDiscardsData(T scores, int discards) {
 		Preconditions.checkArgument(discards >= 0);
 
 		this.scores = scores;
 		this.discards = discards;
 	}
 
 	@Override
 	public Table<Pilot, Integer, Integer> getRaceDiscards() {
 		Table<Pilot, Integer, Integer> pilotDiscards = ArrayTable.create(scores.getPilots(), new IntegerSequence(1, discards));
 		for (Pilot pilot : scores.getPilots()) {
 			pilotDiscards.row(pilot).putAll(getRaceDiscards(pilot));
 		}
 		return pilotDiscards;
 	}
 
 	@Override
 	public Map<Integer, Integer> getRaceDiscards(final Pilot pilot) {
 		Map<Integer, Integer> pilotDiscards = new TreeMap<Integer, Integer>();
 
 		final Map<Race, Integer> racePoints = scores.getRacePoints(pilot);
 		Map<Integer, Race> raceDiscards = getDiscardedRaces(pilot);
 
 		for (int discard = 1; discard <= discards; discard++) {
 			pilotDiscards.put(discard, racePoints.get(raceDiscards.get(discard)));
 		}
 
 		return Collections.unmodifiableMap(pilotDiscards);
 	}
 
 	@Override
 	public Map<Pilot, Integer> getRaceDiscards(int discard) {
 		return Collections.unmodifiableMap(getRaceDiscards().column(discard));
 	}
 
 	@Override
 	public int getRaceDiscard(Pilot pilot, int discard) {
		if (discard < 1 || discard > discards) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		return getRaceDiscards(pilot).get(discard);
 	}
 
 	@Override
 	public abstract Map<Integer, Race> getDiscardedRaces(final Pilot pilot);
 }
