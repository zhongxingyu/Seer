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
 package org.spka.cursus.test;
 
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import eu.lp0.cursus.db.DatabaseSession;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.db.data.RaceAttendee;
 import eu.lp0.cursus.db.data.Series;
 
 public class Series2011DataTests extends AbstractSeries2011 {
 	@Test
 	public void checkPilots() throws Exception {
 		createSeriesData();
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 
 			Assert.assertEquals(22, series.getPilots().size());
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 
 	@Test
 	public void checkEvent1() throws Exception {
 		createEvent1Data();
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Assert.assertEquals(1, event1.getRaces().size());
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 
 	@Test
 	public void checkRace1() throws Exception {
 		createRace1Data();
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 			Race race1 = raceDAO.find(event1, RACE1_NAME);
 
 			Assert.assertEquals(22, race1.getAttendees().size());
			for (Map.Entry<Pilot, RaceAttendee> entry : race1.getAttendees().entrySet()) {
 				Assert.assertEquals(Pilot.class, entry.getKey().getClass());
 				Assert.assertEquals(RaceAttendee.class, entry.getValue().getClass());
 			}
 			Assert.assertEquals(RaceAttendee.Type.V_SCORER, race1.getAttendees().get(sco060).getType());
 			Assert.assertEquals(RaceAttendee.Type.PILOT, race1.getAttendees().get(sco197).getType());
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 }
