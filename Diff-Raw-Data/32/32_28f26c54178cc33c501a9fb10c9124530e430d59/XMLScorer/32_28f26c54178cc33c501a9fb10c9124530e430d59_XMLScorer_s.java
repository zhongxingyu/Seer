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
 package eu.lp0.cursus.xml.scores;
 
 import java.util.List;
 import java.util.Set;
 
 import com.google.common.base.Predicate;
 
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.scoring.data.Scores;
 import eu.lp0.cursus.scoring.scorer.AbstractScorer;
 import eu.lp0.cursus.scoring.scorer.ScoringSystem;
 
@ScoringSystem(uuid = "4063fecb-3dbd-5c0a-baeb-1e41769b8091", name = "XML Import", hide = true)
 public class XMLScorer extends AbstractScorer {
 	@Override
 	public Scores scoreRaces(List<Race> races, Set<Pilot> pilots, Set<Event> events, Predicate<Pilot> fleetFilter) {
 		throw new UnsupportedOperationException();
 	}
 }
