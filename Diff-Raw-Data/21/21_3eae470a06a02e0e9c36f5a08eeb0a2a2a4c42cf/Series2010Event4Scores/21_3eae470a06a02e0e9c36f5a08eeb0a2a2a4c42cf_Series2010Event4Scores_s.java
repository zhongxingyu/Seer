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
 package org.spka.cursus.test.series_2010;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import eu.lp0.cursus.db.DatabaseSession;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.db.data.Series;
 import eu.lp0.cursus.scoring.Scores;
 import eu.lp0.cursus.test.util.OverallAssertUtil;
 import eu.lp0.cursus.test.util.RaceAssertUtil;
 
 /**
  * Scores at the end of event 4 (18/06/2011 to 19/06/2011)
  */
 public class Series2010Event4Scores extends Series2010Event3Scores {
 	@Override
 	@Before
 	public void createData() throws Exception {
 		super.createData();
 		createEvent4Races();
 	}
 
 	@Override
 	@Test
 	public void checkSeries() throws Exception {
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 
 			Scores scores = scorer.scoreSeries(series);
 			Assert.assertEquals(SERIES_FLEET_AT_EVENT4, scores.getOverallFleetSize());
 
 			OverallAssertUtil overallAssertUtil = new OverallAssertUtil(scores);
 			overallAssertUtil.assertPilot(sco200, 0, 2, 1);
 			overallAssertUtil.assertPilot(sco179, 0, 11, 2);
 			overallAssertUtil.assertPilot(sco068, 0, 16, 3);
 			overallAssertUtil.assertPilot(sco808, 0, 17, 3);
 			overallAssertUtil.assertPilot(sco159, 0, 19, 5);
 			overallAssertUtil.assertPilot(sco081, 0, 25, 6);
			overallAssertUtil.assertPilot(sco248, 0, 36, 6);
			overallAssertUtil.assertPilot(sco019, 0, 40, 6);
 			overallAssertUtil.assertPilot(sco116, 0, 42, 9);
			overallAssertUtil.assertPilot(sco087, 0, 43, 9);
 			overallAssertUtil.assertPilot(sco249, 0, 53, 11);
 			overallAssertUtil.assertPilot(sco018, 0, 55, 12);
 			overallAssertUtil.assertPilot(sco060, 0, 61, 13);
 			overallAssertUtil.assertPilot(sco042, 0, 77, 14);
 			overallAssertUtil.assertPilot(sco136, 0, 79, 15);
			overallAssertUtil.assertPilot(sco153, 0, 79, 15);
 			overallAssertUtil.assertPilot(sco158, 0, 87, 17);
 			overallAssertUtil.assertPilot(sco197, 0, 89, 18);
 			overallAssertUtil.assertPilot(sco156, 0, 95, 19);
 			overallAssertUtil.assertOrder();
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 
 	@Test
 	public void checkEvent4() throws Exception {
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event4 = eventDAO.find(series, EVENT4_NAME);
 
 			Scores scores = scorer.scoreEvent(event4);
 			Assert.assertEquals(EVENT4_FLEET, scores.getOverallFleetSize());
 
 			OverallAssertUtil overallAssertUtil = new OverallAssertUtil(scores);
 			overallAssertUtil.assertPilot(sco808, 0, 9, 1);
 			overallAssertUtil.assertPilot(sco068, 0, 13, 2);
 			overallAssertUtil.assertPilot(sco179, 0, 14, 3);
			overallAssertUtil.assertPilot(sco159, 0, 14, 3);
 			overallAssertUtil.assertPilot(sco081, 0, 17, 5);
 			overallAssertUtil.assertPilot(sco200, 0, 21, 6);
			overallAssertUtil.assertPilot(sco248, 0, 21, 6);
			overallAssertUtil.assertPilot(sco087, 0, 21, 6);
 			overallAssertUtil.assertPilot(sco249, 0, 27, 9);
			overallAssertUtil.assertPilot(sco116, 0, 27, 9);
 			overallAssertUtil.assertPilot(sco136, 0, 31, 11);
 			overallAssertUtil.assertPilot(sco018, 0, 33, 12);
 			overallAssertUtil.assertPilot(sco153, 0, 39, 13);
 			overallAssertUtil.assertPilot(sco019, 0, 41, 14);
 			overallAssertUtil.assertPilot(sco156, 0, 47, 15);
 			overallAssertUtil.assertOrder();
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 
 	@Test
 	public void checkRace6() throws Exception {
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event4 = eventDAO.find(series, EVENT4_NAME);
 			Race race6 = raceDAO.find(event4, RACE6_NAME);
 
 			Scores scores = scorer.scoreRace(race6);
 			Assert.assertEquals(EVENT4_FLEET, scores.getOverallFleetSize());
 			Assert.assertEquals(EVENT4_FLEET, scores.getFleetSize(race6));
 
 			RaceAssertUtil raceAssertUtil = new RaceAssertUtil(scores, race6);
 			raceAssertUtil.assertPilot(sco068, 6, 0, 0, 1);
 			raceAssertUtil.assertPilot(sco179, 6, 0, 2, 2);
 			raceAssertUtil.assertPilot(sco808, 6, 0, 3, 3);
 			raceAssertUtil.assertPilot(sco159, 6, 0, 4, 4);
 			raceAssertUtil.assertPilot(sco200, 6, 0, 5, 5);
 			raceAssertUtil.assertPilot(sco081, 6, 0, 6, 6);
 			raceAssertUtil.assertPilot(sco116, 6, 0, 7, 7);
 			raceAssertUtil.assertPilot(sco087, 6, 0, 8, 8);
 			raceAssertUtil.assertPilot(sco136, 6, 0, 9, 9);
 			raceAssertUtil.assertPilot(sco248, 6, 0, 10, 10);
 			raceAssertUtil.assertPilot(sco249, 5, 0, 11, 11);
 			raceAssertUtil.assertPilot(sco019, 4, 0, 12, 12);
 			raceAssertUtil.assertPilot(sco018, 2, 0, 13, 13);
 			raceAssertUtil.assertPilot(sco153, 0, 0, 16, 14);
 			raceAssertUtil.assertPilot(sco156, 0, 0, 16, 14);
 			raceAssertUtil.assertOrder();
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 
 	@Test
 	public void checkRace7() throws Exception {
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event4 = eventDAO.find(series, EVENT4_NAME);
 			Race race7 = raceDAO.find(event4, RACE7_NAME);
 
 			Scores scores = scorer.scoreRace(race7);
 			Assert.assertEquals(EVENT4_FLEET, scores.getOverallFleetSize());
 			Assert.assertEquals(EVENT4_FLEET, scores.getFleetSize(race7));
 
 			RaceAssertUtil raceAssertUtil = new RaceAssertUtil(scores, race7);
 			raceAssertUtil.assertPilot(sco200, 4, 0, 0, 1);
 			raceAssertUtil.assertPilot(sco808, 4, 0, 2, 2);
 			raceAssertUtil.assertPilot(sco159, 4, 0, 3, 3);
 			raceAssertUtil.assertPilot(sco018, 4, 0, 4, 4);
 			raceAssertUtil.assertPilot(sco087, 4, 0, 5, 5);
 			raceAssertUtil.assertPilot(sco136, 4, 0, 6, 6);
 			raceAssertUtil.assertPilot(sco068, 4, 0, 7, 7);
 			raceAssertUtil.assertPilot(sco248, 4, 0, 8, 8);
 			raceAssertUtil.assertPilot(sco081, 4, 0, 9, 9);
 			raceAssertUtil.assertPilot(sco116, 4, 0, 10, 10);
 			raceAssertUtil.assertPilot(sco249, 4, 0, 11, 11);
 			raceAssertUtil.assertPilot(sco179, 3, 0, 12, 12);
 			raceAssertUtil.assertPilot(sco019, 3, 0, 13, 13);
 			raceAssertUtil.assertPilot(sco153, 2, 0, 14, 14);
 			raceAssertUtil.assertPilot(sco156, 1, 0, 15, 15);
 			raceAssertUtil.assertOrder();
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 
 	@Test
 	public void checkRace8() throws Exception {
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event4 = eventDAO.find(series, EVENT4_NAME);
 			Race race8 = raceDAO.find(event4, RACE8_NAME);
 
 			Scores scores = scorer.scoreRace(race8);
 			Assert.assertEquals(EVENT4_FLEET, scores.getOverallFleetSize());
 			Assert.assertEquals(EVENT4_FLEET, scores.getFleetSize(race8));
 
 			RaceAssertUtil raceAssertUtil = new RaceAssertUtil(scores, race8);
 			raceAssertUtil.assertPilot(sco179, 8, 0, 0, 1);
 			raceAssertUtil.assertPilot(sco081, 8, 0, 2, 2);
 			raceAssertUtil.assertPilot(sco248, 8, 0, 3, 3);
 			raceAssertUtil.assertPilot(sco808, 7, 0, 4, 4);
 			raceAssertUtil.assertPilot(sco249, 6, 0, 5, 5);
 			raceAssertUtil.assertPilot(sco068, 5, 0, 6, 6);
 			raceAssertUtil.assertPilot(sco159, 4, 0, 7, 7);
 			raceAssertUtil.assertPilot(sco087, 4, 0, 8, 8);
 			raceAssertUtil.assertPilot(sco153, 3, 0, 9, 9);
 			raceAssertUtil.assertPilot(sco116, 2, 0, 10, 10);
 			raceAssertUtil.assertPilot(sco018, 0, 0, 16, 11);
 			raceAssertUtil.assertPilot(sco019, 0, 0, 16, 11);
 			raceAssertUtil.assertPilot(sco136, 0, 0, 16, 11);
 			raceAssertUtil.assertPilot(sco156, 0, 0, 16, 11);
 			raceAssertUtil.assertPilot(sco200, 0, 0, 16, 11);
 			raceAssertUtil.assertOrder();
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 }
