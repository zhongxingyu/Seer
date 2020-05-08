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
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 
 public interface RaceDiscardsData {
 	public Map<Pilot, ? extends List<Integer>> getRaceDiscards();
 
 	public List<Integer> getRaceDiscards(Pilot pilot);
 
 	public int getRaceDiscard(Pilot pilot, int discard);
 
 	public Map<Pilot, ? extends Set<Race>> getDiscardedRaces();
 
 	public Set<Race> getDiscardedRaces(Pilot pilot);
 }
