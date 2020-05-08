 /*
 	cursus - Race series management program
 	Copyright 2013  Simon Arlott
 
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
 package org.spka.cursus.test.cc_2013;
 
 import org.fisly.cursus.scoring.FISLYConstants;
 import org.spka.cursus.test.AbstractSeries;
 
 import eu.lp0.cursus.db.DatabaseSession;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Gender;
 import eu.lp0.cursus.db.data.Penalty;
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.db.data.RaceAttendee;
 import eu.lp0.cursus.db.data.RaceNumber;
 import eu.lp0.cursus.db.data.RaceTally;
 import eu.lp0.cursus.db.data.Series;
 import eu.lp0.cursus.scoring.scorer.Scorer;
 import eu.lp0.cursus.scoring.scorer.ScorerFactory;
 
 public abstract class AbstractSeries2013 extends AbstractSeries {
 	protected final String SERIES_NAME = "Celtic Challenge 2013"; //$NON-NLS-1$
 	protected final int SERIES_FLEET = 15;
 	protected final int SERIES_FLEET_AT_EVENT1 = 15;
	protected final int SERIES_FLEET_AT_EVENT2 = 21;
 
 	protected final String EVENT1_NAME = "Race Event 1"; //$NON-NLS-1$
 	protected final String EVENT1_DESC = "Benone (25/05/2013 and 26/05/2013)"; //$NON-NLS-1$
 	protected final int EVENT1_FLEET = 15;
 	protected final String RACE1_NAME = "Race 1"; //$NON-NLS-1$
 	protected final String RACE1_DESC = "Benone (25/05/2013)"; //$NON-NLS-1$
 	protected final int RACE1_PILOTS = 15;
 	protected final String RACE2_NAME = "Race 2"; //$NON-NLS-1$
 	protected final String RACE2_DESC = "Benone (25/05/2013)"; //$NON-NLS-1$
 	protected final int RACE2_PILOTS = 15;
 	protected final String RACE3_NAME = "Race 3"; //$NON-NLS-1$
 	protected final String RACE3_DESC = "Benone (25/05/2013)"; //$NON-NLS-1$
 	protected final int RACE3_PILOTS = 15;
 	protected final String RACE4_NAME = "Race 4"; //$NON-NLS-1$
 	protected final String RACE4_DESC = "Benone (25/05/2013)"; //$NON-NLS-1$
 	protected final int RACE4_PILOTS = 15;
 	protected final String RACE5_NAME = "Race 5"; //$NON-NLS-1$
 	protected final String RACE5_DESC = "Benone (26/05/2013)"; //$NON-NLS-1$
 	protected final int RACE5_PILOTS = 15;
 	protected final String RACE6_NAME = "Race 6"; //$NON-NLS-1$
 	protected final String RACE6_DESC = "Benone (26/05/2013)"; //$NON-NLS-1$
 	protected final int RACE6_PILOTS = 15;
 
 	protected final String EVENT2_NAME = "Race Event 2"; //$NON-NLS-1$
	protected final String EVENT2_DESC = "Luce Bay (15/06/2013 and 16/06/2013)"; //$NON-NLS-1$
 	protected final int EVENT2_FLEET = 19;
 	protected final String RACE7_NAME = "Race 7"; //$NON-NLS-1$
 	protected final String RACE7_DESC = "Luce Bay (15/06/2013)"; //$NON-NLS-1$
 	protected final int RACE7_PILOTS = 19;
 	protected final String RACE8_NAME = "Race 8"; //$NON-NLS-1$
 	protected final String RACE8_DESC = "Luce Bay (16/06/2013)"; //$NON-NLS-1$
 	protected final int RACE8_PILOTS = 18;
 	protected final String RACE9_NAME = "Race 9"; //$NON-NLS-1$
 	protected final String RACE9_DESC = "Luce Bay (16/06/2013)"; //$NON-NLS-1$
 	protected final int RACE9_PILOTS = 18;
 	protected final String RACE10_NAME = "Race 10"; //$NON-NLS-1$
 	protected final String RACE10_DESC = "Luce Bay (16/06/2013)"; //$NON-NLS-1$
 	protected final int RACE10_PILOTS = 18;
 	protected final String RACE11_NAME = "Race 11"; //$NON-NLS-1$
 	protected final String RACE11_DESC = "Luce Bay (16/06/2013)"; //$NON-NLS-1$
 	protected final int RACE11_PILOTS = 18;
 
 	protected Scorer scorer = ScorerFactory.newScorer(FISLYConstants.UUID_2010);
 
 	protected Pilot sco018;
 	protected Pilot sco060;
 	protected Pilot sco068;
 	protected Pilot sco087;
 	protected Pilot sco116;
 	protected Pilot sco153;
 	protected Pilot sco156;
 	protected Pilot sco159;
 	protected Pilot sco179;
 	protected Pilot sco200;
 	protected Pilot sco528;
 	protected Pilot sco666;
 	protected Pilot sco808;
 	protected Pilot ir001;
 	protected Pilot ir014;
 	protected Pilot ir016;
 	protected Pilot ir025;
 	protected Pilot ir053;
 	protected Pilot ir077;
 	protected Pilot ir085;
 	protected Pilot ir181;
 
 	private Series _series;
 	private Event _event1;
 	private Race _race1;
 	private Race _race2;
 	private Race _race3;
 	private Race _race4;
 	private Race _race5;
 	private Race _race6;
 	private Event _event2;
 	private Race _race7;
 	private Race _race8;
 	private Race _race9;
 	private Race _race10;
 	private Race _race11;
 
 	protected void createSeriesData() throws Exception {
 		if (_series != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			// Create the 2012/13 series
 			Series series = new Series(SERIES_NAME);
 
 			// Add all the pilots
 			sco018 = new Pilot(series, "SCO018", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco018);
 
 			sco060 = new Pilot(series, "SCO060", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco060);
 
 			sco068 = new Pilot(series, "SCO068", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco068);
 
 			sco087 = new Pilot(series, "SCO087", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco087);
 
 			sco116 = new Pilot(series, "SCO116", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco116);
 
 			sco153 = new Pilot(series, "SCO153", Gender.FEMALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco153);
 
 			sco156 = new Pilot(series, "SCO156", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco156);
 
 			sco159 = new Pilot(series, "SCO159", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco159);
 
 			sco179 = new Pilot(series, "SCO179", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco179);
 
 			sco200 = new Pilot(series, "SCO200", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco200);
 
 			sco528 = new Pilot(series, "SCO528", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco528);
 
 			sco666 = new Pilot(series, "SCO666", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco666);
 
 			sco808 = new Pilot(series, "SCO808", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco808);
 
 			ir001 = new Pilot(series, "IR001", Gender.MALE, "Ireland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(ir001);
 
 			ir014 = new Pilot(series, "IR014", Gender.MALE, "Ireland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(ir014);
 
 			ir016 = new Pilot(series, "IR016", Gender.MALE, "Ireland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(ir016);
 
 			ir025 = new Pilot(series, "IR025", Gender.MALE, "Ireland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(ir025);
 
 			ir053 = new Pilot(series, "IR053", Gender.MALE, "Ireland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(ir053);
 
 			ir077 = new Pilot(series, "IR077", Gender.MALE, "Ireland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(ir077);
 
 			ir085 = new Pilot(series, "IR085", Gender.MALE, "Ireland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(ir085);
 
 			ir181 = new Pilot(series, "IR181", Gender.MALE, "Ireland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(ir181);
 
 			// Create race numbers
 			for (Pilot pilot : series.getPilots()) {
 				pilot.setRaceNumber(RaceNumber.valueOfFor(pilot.getName(), pilot));
 			}
 
 			// Save
 			seriesDAO.persist(series);
 
 			DatabaseSession.commit();
 
 			_series = series;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createEvent1Data() throws Exception {
 		createSeriesData();
 
 		if (_event1 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 
 			Event event1 = new Event(series, EVENT1_NAME, EVENT1_DESC);
 			series.getEvents().add(event1);
 			eventDAO.persist(event1);
 
 			DatabaseSession.commit();
 
 			_event1 = event1;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createEvent1Races() throws Exception {
 		createRace1Data();
 		createRace2Data();
 		createRace3Data();
 		createRace4Data();
 		createRace5Data();
 		createRace6Data();
 	}
 
 	protected void createRace1Data() throws Exception {
 		createEvent1Data();
 
 		if (_race1 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race1 = new Race(event1, RACE1_NAME, RACE1_DESC);
 			event1.getRaces().add(race1);
 			race1.getAttendees().put(sco060, new RaceAttendee(race1, sco060, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco116, new RaceAttendee(race1, sco116, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco153, new RaceAttendee(race1, sco153, RaceAttendee.Type.PILOT));
 			RaceAttendee att159 = new RaceAttendee(race1, sco159, RaceAttendee.Type.PILOT);
 			att159.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Hit a mark")); //$NON-NLS-1$
 			race1.getAttendees().put(sco159, att159);
 			race1.getAttendees().put(sco528, new RaceAttendee(race1, sco528, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco666, new RaceAttendee(race1, sco666, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco808, new RaceAttendee(race1, sco808, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(ir001, new RaceAttendee(race1, ir001, RaceAttendee.Type.PILOT));
 			RaceAttendee att014 = new RaceAttendee(race1, ir014, RaceAttendee.Type.PILOT);
 			att014.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Hit a mark")); //$NON-NLS-1$
 			race1.getAttendees().put(ir014, att014);
 			race1.getAttendees().put(ir016, new RaceAttendee(race1, ir016, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(ir025, new RaceAttendee(race1, ir025, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(ir053, new RaceAttendee(race1, ir053, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(ir077, new RaceAttendee(race1, ir077, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(ir085, new RaceAttendee(race1, ir085, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(ir181, new RaceAttendee(race1, ir181, RaceAttendee.Type.PILOT));
 			race1.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			// 2
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco153)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			// 3
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			// 4
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco153)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			// 5
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco060)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			// 6
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco153)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			// 7
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race1.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco153)); //$NON-NLS-1$
 			raceDAO.persist(race1);
 
 			DatabaseSession.commit();
 
 			_race1 = race1;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace2Data() throws Exception {
 		createEvent1Data();
 
 		if (_race2 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race2 = new Race(event1, RACE2_NAME, RACE2_DESC);
 			event1.getRaces().add(race2);
 			race2.getAttendees().put(sco060, new RaceAttendee(race2, sco060, RaceAttendee.Type.V_SCORER));
 			race2.getAttendees().put(sco116, new RaceAttendee(race2, sco116, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco153, new RaceAttendee(race2, sco153, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco159, new RaceAttendee(race2, sco159, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco528, new RaceAttendee(race2, sco528, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco666, new RaceAttendee(race2, sco666, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco808, new RaceAttendee(race2, sco808, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(ir001, new RaceAttendee(race2, ir001, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(ir014, new RaceAttendee(race2, ir014, RaceAttendee.Type.PILOT));
 			RaceAttendee att016 = new RaceAttendee(race2, ir016, RaceAttendee.Type.PILOT);
 			att016.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Hit a mark")); //$NON-NLS-1$
 			race2.getAttendees().put(ir016, att016);
 			race2.getAttendees().put(ir025, new RaceAttendee(race2, ir025, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(ir053, new RaceAttendee(race2, ir053, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(ir077, new RaceAttendee(race2, ir077, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(ir085, new RaceAttendee(race2, ir085, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(ir181, new RaceAttendee(race2, ir181, RaceAttendee.Type.PILOT));
 			race2.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			// 2
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			// 3
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco153)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			// 4
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			// 5
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race2.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco153)); //$NON-NLS-1$
 			raceDAO.persist(race2);
 
 			DatabaseSession.commit();
 
 			_race2 = race2;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace3Data() throws Exception {
 		createEvent1Data();
 
 		if (_race3 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race3 = new Race(event1, RACE3_NAME, RACE3_DESC);
 			event1.getRaces().add(race3);
 			race3.getAttendees().put(sco060, new RaceAttendee(race3, sco060, RaceAttendee.Type.V_SCORER));
 			race3.getAttendees().put(sco116, new RaceAttendee(race3, sco116, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco153, new RaceAttendee(race3, sco153, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco159, new RaceAttendee(race3, sco159, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco528, new RaceAttendee(race3, sco528, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco666, new RaceAttendee(race3, sco666, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco808, new RaceAttendee(race3, sco808, RaceAttendee.Type.PILOT));
 			RaceAttendee att001 = new RaceAttendee(race3, ir001, RaceAttendee.Type.PILOT);
 			att001.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Hit a mark")); //$NON-NLS-1$
 			att001.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Hit a mark")); //$NON-NLS-1$
 			race3.getAttendees().put(ir001, att001);
 			race3.getAttendees().put(ir014, new RaceAttendee(race3, ir014, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(ir016, new RaceAttendee(race3, ir016, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(ir025, new RaceAttendee(race3, ir025, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(ir053, new RaceAttendee(race3, ir053, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(ir077, new RaceAttendee(race3, ir077, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(ir085, new RaceAttendee(race3, ir085, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(ir181, new RaceAttendee(race3, ir181, RaceAttendee.Type.PILOT));
 			race3.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			// 2
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			// 3
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			// 4
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			// 5
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco153)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race3.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			raceDAO.persist(race3);
 
 			DatabaseSession.commit();
 
 			_race3 = race3;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace4Data() throws Exception {
 		createEvent1Data();
 
 		if (_race4 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race4 = new Race(event1, RACE4_NAME, RACE4_DESC);
 			event1.getRaces().add(race4);
 			race4.getAttendees().put(sco060, new RaceAttendee(race4, sco060, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(sco116, new RaceAttendee(race4, sco116, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(sco153, new RaceAttendee(race4, sco153, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(sco159, new RaceAttendee(race4, sco159, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(sco528, new RaceAttendee(race4, sco528, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(sco666, new RaceAttendee(race4, sco666, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(sco808, new RaceAttendee(race4, sco808, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(ir001, new RaceAttendee(race4, ir001, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(ir014, new RaceAttendee(race4, ir014, RaceAttendee.Type.PILOT));
 			RaceAttendee att016 = new RaceAttendee(race4, ir016, RaceAttendee.Type.PILOT);
 			att016.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Hit a mark")); //$NON-NLS-1$
 			race4.getAttendees().put(ir016, att016);
 			race4.getAttendees().put(ir025, new RaceAttendee(race4, ir025, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(ir053, new RaceAttendee(race4, ir053, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(ir077, new RaceAttendee(race4, ir077, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(ir085, new RaceAttendee(race4, ir085, RaceAttendee.Type.PILOT));
 			race4.getAttendees().put(ir181, new RaceAttendee(race4, ir181, RaceAttendee.Type.PILOT));
 			race4.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			// 2
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			// 3
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			// 4
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race4.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			raceDAO.persist(race4);
 
 			DatabaseSession.commit();
 
 			_race4 = race4;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace5Data() throws Exception {
 		createEvent1Data();
 
 		if (_race5 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race5 = new Race(event1, RACE5_NAME, RACE5_DESC);
 			event1.getRaces().add(race5);
 			race5.getAttendees().put(sco060, new RaceAttendee(race5, sco060, RaceAttendee.Type.V_SCORER));
 			race5.getAttendees().put(sco116, new RaceAttendee(race5, sco116, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(sco153, new RaceAttendee(race5, sco153, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(sco159, new RaceAttendee(race5, sco159, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(sco528, new RaceAttendee(race5, sco528, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(sco666, new RaceAttendee(race5, sco666, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(sco808, new RaceAttendee(race5, sco808, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(ir001, new RaceAttendee(race5, ir001, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(ir014, new RaceAttendee(race5, ir014, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(ir016, new RaceAttendee(race5, ir016, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(ir025, new RaceAttendee(race5, ir025, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(ir053, new RaceAttendee(race5, ir053, RaceAttendee.Type.PILOT));
 			race5.getAttendees().put(ir077, new RaceAttendee(race5, ir077, RaceAttendee.Type.PILOT));
 			RaceAttendee att085 = new RaceAttendee(race5, ir085, RaceAttendee.Type.PILOT);
 			att085.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Hit a mark")); //$NON-NLS-1$
 			race5.getAttendees().put(ir085, att085);
 			race5.getAttendees().put(ir181, new RaceAttendee(race5, ir181, RaceAttendee.Type.PILOT));
 			race5.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			// 2
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			// 3
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			// 4
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			// 5
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			// 6
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			// 7
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race5.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			raceDAO.persist(race5);
 
 			DatabaseSession.commit();
 
 			_race5 = race5;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace6Data() throws Exception {
 		createEvent1Data();
 
 		if (_race6 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race6 = new Race(event1, RACE6_NAME, RACE6_DESC);
 			event1.getRaces().add(race6);
 			race6.getAttendees().put(sco060, new RaceAttendee(race6, sco060, RaceAttendee.Type.V_SCORER));
 			race6.getAttendees().put(sco116, new RaceAttendee(race6, sco116, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(sco153, new RaceAttendee(race6, sco153, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(sco159, new RaceAttendee(race6, sco159, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(sco528, new RaceAttendee(race6, sco528, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(sco666, new RaceAttendee(race6, sco666, RaceAttendee.Type.PILOT));
 			RaceAttendee att808 = new RaceAttendee(race6, sco808, RaceAttendee.Type.PILOT);
 			att808.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Went over IR53's lines")); //$NON-NLS-1$
 			race6.getAttendees().put(sco808, att808);
 			race6.getAttendees().put(ir001, new RaceAttendee(race6, ir001, RaceAttendee.Type.PILOT));
 			RaceAttendee att014 = new RaceAttendee(race6, ir014, RaceAttendee.Type.PILOT);
 			att014.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Wrong side of red line")); //$NON-NLS-1$
 			race6.getAttendees().put(ir014, att014);
 			race6.getAttendees().put(ir016, new RaceAttendee(race6, ir016, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(ir025, new RaceAttendee(race6, ir025, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(ir053, new RaceAttendee(race6, ir053, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(ir077, new RaceAttendee(race6, ir077, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(ir085, new RaceAttendee(race6, ir085, RaceAttendee.Type.PILOT));
 			race6.getAttendees().put(ir181, new RaceAttendee(race6, ir181, RaceAttendee.Type.PILOT));
 			race6.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			// 2
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			// 3
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			// 4
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			// 5
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco153)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			// 6
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir014)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			// 7
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race6.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			raceDAO.persist(race6);
 
 			DatabaseSession.commit();
 
 			_race6 = race6;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createEvent2Data() throws Exception {
 		createSeriesData();
 
 		if (_event2 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 
 			Event event2 = new Event(series, EVENT2_NAME, EVENT2_DESC);
 			series.getEvents().add(event2);
 			eventDAO.persist(event2);
 
 			DatabaseSession.commit();
 
 			_event2 = event2;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createEvent2Races() throws Exception {
 		createRace7Data();
 		createRace8Data();
 		createRace9Data();
 		createRace10Data();
 		createRace11Data();
 	}
 
 	protected void createRace7Data() throws Exception {
 		createEvent2Data();
 
 		if (_race7 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event2 = eventDAO.find(series, EVENT2_NAME);
 
 			Race race7 = new Race(event2, RACE7_NAME, RACE7_DESC);
 			event2.getRaces().add(race7);
 			race7.getAttendees().put(sco018, new RaceAttendee(race7, sco018, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(sco068, new RaceAttendee(race7, sco068, RaceAttendee.Type.PILOT));
 			RaceAttendee att087 = new RaceAttendee(race7, sco087, RaceAttendee.Type.PILOT);
 			att087.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Hit a mark")); //$NON-NLS-1$
 			race7.getAttendees().put(sco087, att087);
 			race7.getAttendees().put(sco116, new RaceAttendee(race7, sco116, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(sco156, new RaceAttendee(race7, sco156, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(sco159, new RaceAttendee(race7, sco159, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(sco179, new RaceAttendee(race7, sco179, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(sco200, new RaceAttendee(race7, sco200, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(sco528, new RaceAttendee(race7, sco528, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(sco666, new RaceAttendee(race7, sco666, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(sco808, new RaceAttendee(race7, sco808, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(ir001, new RaceAttendee(race7, ir001, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(ir016, new RaceAttendee(race7, ir016, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(ir025, new RaceAttendee(race7, ir025, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(ir053, new RaceAttendee(race7, ir053, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(ir077, new RaceAttendee(race7, ir077, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(ir085, new RaceAttendee(race7, ir085, RaceAttendee.Type.PILOT));
 			race7.getAttendees().put(ir181, new RaceAttendee(race7, ir181, RaceAttendee.Type.PILOT));
 			race7.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco018)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			// 2
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco018)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			// 3
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco018)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			// 4
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco018)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			// 5
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco018)); //$NON-NLS-1$
 			// 6
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race7.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			raceDAO.persist(race7);
 
 			DatabaseSession.commit();
 
 			_race7 = race7;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace8Data() throws Exception {
 		createEvent2Data();
 
 		if (_race8 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event2 = eventDAO.find(series, EVENT2_NAME);
 
 			Race race8 = new Race(event2, RACE8_NAME, RACE8_DESC);
 			event2.getRaces().add(race8);
 			race8.getAttendees().put(sco068, new RaceAttendee(race8, sco068, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco087, new RaceAttendee(race8, sco087, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco116, new RaceAttendee(race8, sco116, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco156, new RaceAttendee(race8, sco156, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco159, new RaceAttendee(race8, sco159, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco179, new RaceAttendee(race8, sco179, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco200, new RaceAttendee(race8, sco200, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco528, new RaceAttendee(race8, sco528, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco666, new RaceAttendee(race8, sco666, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(sco808, new RaceAttendee(race8, sco808, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(ir001, new RaceAttendee(race8, ir001, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(ir016, new RaceAttendee(race8, ir016, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(ir025, new RaceAttendee(race8, ir025, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(ir053, new RaceAttendee(race8, ir053, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(ir077, new RaceAttendee(race8, ir077, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(ir085, new RaceAttendee(race8, ir085, RaceAttendee.Type.PILOT));
 			race8.getAttendees().put(ir181, new RaceAttendee(race8, ir181, RaceAttendee.Type.PILOT));
 			race8.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			// 2
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			// 3
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			// 4
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			// 5
			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			// 6
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			// 7
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race8.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			raceDAO.persist(race8);
 
 			DatabaseSession.commit();
 
 			_race8 = race8;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace9Data() throws Exception {
 		createEvent2Data();
 
 		if (_race9 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event2 = eventDAO.find(series, EVENT2_NAME);
 
 			Race race9 = new Race(event2, RACE9_NAME, RACE9_DESC);
 			event2.getRaces().add(race9);
 			race9.getAttendees().put(sco068, new RaceAttendee(race9, sco068, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(sco087, new RaceAttendee(race9, sco087, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(sco116, new RaceAttendee(race9, sco116, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(sco156, new RaceAttendee(race9, sco156, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(sco159, new RaceAttendee(race9, sco159, RaceAttendee.Type.PILOT));
			RaceAttendee att179 = new RaceAttendee(race9, sco179, RaceAttendee.Type.PILOT);
			att179.getPenalties().add(new Penalty(Penalty.Type.AUTOMATIC, "Mounted IR1")); //$NON-NLS-1$
			race9.getAttendees().put(sco179, att179);
 			race9.getAttendees().put(sco200, new RaceAttendee(race9, sco200, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(sco528, new RaceAttendee(race9, sco528, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(sco666, new RaceAttendee(race9, sco666, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(sco808, new RaceAttendee(race9, sco808, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(ir001, new RaceAttendee(race9, ir001, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(ir016, new RaceAttendee(race9, ir016, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(ir025, new RaceAttendee(race9, ir025, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(ir053, new RaceAttendee(race9, ir053, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(ir077, new RaceAttendee(race9, ir077, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(ir085, new RaceAttendee(race9, ir085, RaceAttendee.Type.PILOT));
 			race9.getAttendees().put(ir181, new RaceAttendee(race9, ir181, RaceAttendee.Type.PILOT));
 			race9.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			// 2
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			// 3
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			// 4
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			// 5
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			// 6
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			// 7
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race9.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			raceDAO.persist(race9);
 
 			DatabaseSession.commit();
 
 			_race9 = race9;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace10Data() throws Exception {
 		createEvent2Data();
 
 		if (_race10 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event2 = eventDAO.find(series, EVENT2_NAME);
 
 			Race race10 = new Race(event2, RACE10_NAME, RACE10_DESC);
 			event2.getRaces().add(race10);
 			race10.getAttendees().put(sco068, new RaceAttendee(race10, sco068, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco087, new RaceAttendee(race10, sco087, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco116, new RaceAttendee(race10, sco116, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco156, new RaceAttendee(race10, sco156, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco159, new RaceAttendee(race10, sco159, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco179, new RaceAttendee(race10, sco179, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco200, new RaceAttendee(race10, sco200, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco528, new RaceAttendee(race10, sco528, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco666, new RaceAttendee(race10, sco666, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(sco808, new RaceAttendee(race10, sco808, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(ir001, new RaceAttendee(race10, ir001, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(ir016, new RaceAttendee(race10, ir016, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(ir025, new RaceAttendee(race10, ir025, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(ir053, new RaceAttendee(race10, ir053, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(ir077, new RaceAttendee(race10, ir077, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(ir085, new RaceAttendee(race10, ir085, RaceAttendee.Type.PILOT));
 			race10.getAttendees().put(ir181, new RaceAttendee(race10, ir181, RaceAttendee.Type.PILOT));
 			race10.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			// 2
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir077)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			// 3
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			// 4
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			// 5
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			// 6
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			// 7
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco068)); //$NON-NLS-1$
 			race10.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			raceDAO.persist(race10);
 
 			DatabaseSession.commit();
 
 			_race10 = race10;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace11Data() throws Exception {
 		createEvent2Data();
 
 		if (_race11 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event2 = eventDAO.find(series, EVENT2_NAME);
 
 			Race race11 = new Race(event2, RACE11_NAME, RACE11_DESC);
 			event2.getRaces().add(race11);
 			race11.getAttendees().put(sco068, new RaceAttendee(race11, sco068, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco087, new RaceAttendee(race11, sco087, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco116, new RaceAttendee(race11, sco116, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco156, new RaceAttendee(race11, sco156, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco159, new RaceAttendee(race11, sco159, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco179, new RaceAttendee(race11, sco179, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco200, new RaceAttendee(race11, sco200, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco528, new RaceAttendee(race11, sco528, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco666, new RaceAttendee(race11, sco666, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(sco808, new RaceAttendee(race11, sco808, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(ir001, new RaceAttendee(race11, ir001, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(ir016, new RaceAttendee(race11, ir016, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(ir025, new RaceAttendee(race11, ir025, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(ir053, new RaceAttendee(race11, ir053, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(ir077, new RaceAttendee(race11, ir077, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(ir085, new RaceAttendee(race11, ir085, RaceAttendee.Type.PILOT));
 			race11.getAttendees().put(ir181, new RaceAttendee(race11, ir181, RaceAttendee.Type.PILOT));
 			race11.getTallies().add(new RaceTally(RaceTally.Type.START));
 			// 1
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			// 2
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco087)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			// 3
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			// 4
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			// 5
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			// 6
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco156)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco666)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			// 7
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir181)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco200)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco808)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir001)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir025)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco528)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco179)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir085)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir053)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", ir016)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco159)); //$NON-NLS-1$
 			race11.getTallies().add(new RaceTally(RaceTally.Type.LAP, "", sco116)); //$NON-NLS-1$
 			raceDAO.persist(race11);
 
 			DatabaseSession.commit();
 
 			_race11 = race11;
 		} finally {
 			db.endSession();
 		}
 	}
 }
