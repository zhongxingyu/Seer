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
 package eu.lp0.cursus.test.db;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.lp0.cursus.db.dao.ClassDAO;
 import eu.lp0.cursus.db.dao.CursusDAO;
 import eu.lp0.cursus.db.dao.EventDAO;
 import eu.lp0.cursus.db.dao.PilotDAO;
 import eu.lp0.cursus.db.dao.RaceAttendeeDAO;
 import eu.lp0.cursus.db.dao.RaceDAO;
 import eu.lp0.cursus.db.dao.RaceNumberDAO;
 import eu.lp0.cursus.db.dao.SeriesDAO;
 
 public class AbstractDataTest {
 	protected final Logger log = LoggerFactory.getLogger(getClass());
 
 	protected ClassDAO classDAO = new ClassDAO();
 	protected CursusDAO cursusDAO = new CursusDAO();
 	protected EventDAO eventDAO = new EventDAO();
 	protected PilotDAO pilotDAO = new PilotDAO();
 	protected RaceDAO raceDAO = new RaceDAO();
 	protected RaceAttendeeDAO raceAttendeeDAO = new RaceAttendeeDAO();
 	protected RaceNumberDAO raceNumberDAO = new RaceNumberDAO();
 	protected SeriesDAO seriesDAO = new SeriesDAO();
}
