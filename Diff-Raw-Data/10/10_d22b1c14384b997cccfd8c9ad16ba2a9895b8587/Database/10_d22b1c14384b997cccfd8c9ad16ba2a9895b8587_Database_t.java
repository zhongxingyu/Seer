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
 package eu.lp0.cursus.db;
 
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Properties;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.lp0.cursus.db.dao.CursusDAO;
 import eu.lp0.cursus.db.dao.EventDAO;
 import eu.lp0.cursus.db.dao.RaceDAO;
 import eu.lp0.cursus.db.dao.SeriesDAO;
 import eu.lp0.cursus.db.data.Cursus;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.db.data.Series;
 import eu.lp0.cursus.util.Constants;
 import eu.lp0.cursus.util.Messages;
 
 public abstract class Database {
 	public static final String UNTITLED_SERIES = "series.untitled"; //$NON-NLS-1$
 	public static final String UNTITLED_EVENT = "event.untitled"; //$NON-NLS-1$
 	public static final String UNTITLED_RACE = "race.untitled"; //$NON-NLS-1$

 	private final Logger log = LoggerFactory.getLogger(getClass());
 	private final String name;
 	private final EntityManagerFactory emf;
 	private final DatabaseSession session;
 
 	private static final CursusDAO cursusDAO = new CursusDAO();
 	private static final SeriesDAO seriesDAO = new SeriesDAO();
 	private static final EventDAO eventDAO = new EventDAO();
 	private static final RaceDAO raceDAO = new RaceDAO();
 
 	protected Database(String name, String url, String user, String password) throws SQLException, DatabaseVersionException, InvalidDatabaseException {
 		this.name = name;
 		this.session = new DatabaseSession(this);
 
 		log.info("Connecting to database \"" + name + "\" at \"" + url + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 		Properties conn = new Properties();
 		conn.setProperty("javax.persistence.jdbc.url", url); //$NON-NLS-1$
 		conn.setProperty("javax.persistence.jdbc.user", user); //$NON-NLS-1$
 		conn.setProperty("javax.persistence.jdbc.password", password); //$NON-NLS-1$
 		emf = Persistence.createEntityManagerFactory(getClass().getPackage().getName(), conn);
 
 		initDatabase();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	EntityManager createEntityManager() {
 		return emf.createEntityManager();
 	}
 
 	public final void startSession() {
 		session.startSession();
 	}
 
 	public final void endSession() {
 		session.endSession();
 	}
 
 	private void initDatabase() throws InvalidDatabaseException {
 		startSession();
 		try {
 			DatabaseSession.begin();
 
 			Cursus cursus = cursusDAO.findSingleton();
 			if (cursus == null) {
 				log.info("Database \"" + name + "\" has no version record, setting to " + DatabaseVersion.getLatest()); //$NON-NLS-1$ //$NON-NLS-2$
 
 				cursus = new Cursus(DatabaseVersion.getLatest().asLong(), Constants.APP_DESC);
 				cursusDAO.persist(cursus);
 			} else if (cursus.getVersion() != DatabaseVersion.getLatest().asLong()) {
 				// TODO support upgrading
 				log.info("Database \"" + name + "\" has version "); //$NON-NLS-1$ //$NON-NLS-2$
 				throw new DatabaseVersionException(cursus);
 			} else {
 				// TODO need to parse the actual version
 				log.info("Database \"" + name + "\" has version " + DatabaseVersion.getLatest()); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 
 			List<Series> seriesList = seriesDAO.findAll();
 			if (seriesList.isEmpty()) {
 				log.info("Database \"" + name + "\" has no series, creating untitled series"); //$NON-NLS-1$ //$NON-NLS-2$
 
 				Series series = new Series(Messages.getString(UNTITLED_SERIES));
 				seriesDAO.persist(series);
 
 				Event event = new Event(series, Messages.getString(UNTITLED_EVENT));
 				eventDAO.persist(event);
 
 				Race race = new Race(event, Messages.getString(UNTITLED_RACE));
 				raceDAO.persist(race);
 			}
 
 			DatabaseSession.commit();
 		} finally {
 			endSession();
 		}
 	}
 
 	public abstract boolean isSaved();
 
 	public boolean close(boolean force) {
 		// TODO consider database saved if all connections in the pool are closed
 		return force || !isSaved();
 	}
 }
