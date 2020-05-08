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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.ArrayTable;
import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.Table;
 
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 
 public abstract class AbstractRaceLapsData<T extends ScoredData> implements RaceLapsData {
 	protected final T scores;
 
 	private final Table<Pilot, Race, Integer> raceLaps;
	private final Map<Race, LinkedListMultimap<Integer, Pilot>> lapOrder;
 
 	public AbstractRaceLapsData(T scores) {
 		this.scores = scores;
 
 		raceLaps = ArrayTable.create(scores.getPilots(), scores.getRaces());
		lapOrder = new HashMap<Race, LinkedListMultimap<Integer, Pilot>>();
 
 		Map<Race, Integer> zeroLaps = new HashMap<Race, Integer>();
 		for (Race race : scores.getRaces()) {
			LinkedListMultimap<Integer, Pilot> order = LinkedListMultimap.create();
 			lapOrder.put(race, order);
 			zeroLaps.put(race, 0);
 		}
 
 		for (Pilot pilot : scores.getPilots()) {
 			raceLaps.row(pilot).putAll(zeroLaps);
 		}
 	}
 
 	@Override
 	public int getLaps(Pilot pilot, Race race) {
 		return raceLaps.get(pilot, race);
 	}
 
 	@Override
 	public Map<Race, Integer> getLaps(Pilot pilot) {
 		return Collections.unmodifiableMap(raceLaps.row(pilot));
 	}
 
 	@Override
 	public Map<Pilot, Integer> getLaps(Race race) {
 		return Collections.unmodifiableMap(raceLaps.column(race));
 	}
 
 	@Override
 	public List<Pilot> getLapOrder(Race race) {
		return Collections.unmodifiableList(new ArrayList<Pilot>(lapOrder.get(race).values()));
 	}
 
 	protected void completeRaceLap(Race race, Pilot pilot) {
		LinkedListMultimap<Integer, Pilot> order = lapOrder.get(race);
 		int laps = raceLaps.get(pilot, race);
 
 		order.remove(laps, pilot);
 		raceLaps.put(pilot, race, ++laps);
 		order.put(laps, pilot);
 	}
 }
