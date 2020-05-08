 package ch.hsr.sa.radiotour.technicalservices.database;
 
 import java.sql.SQLException;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import ch.hsr.sa.radiotour.R;
 import ch.hsr.sa.radiotour.domain.Group;
 import ch.hsr.sa.radiotour.domain.Judgement;
 import ch.hsr.sa.radiotour.domain.Maillot;
 import ch.hsr.sa.radiotour.domain.PointOfRace;
 import ch.hsr.sa.radiotour.domain.RaceSituation;
 import ch.hsr.sa.radiotour.domain.Rider;
 import ch.hsr.sa.radiotour.domain.RiderStageConnection;
 import ch.hsr.sa.radiotour.domain.SpecialPointHolder;
 import ch.hsr.sa.radiotour.domain.SpecialRanking;
 import ch.hsr.sa.radiotour.domain.Stage;
 import ch.hsr.sa.radiotour.domain.Team;
 
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 /**
  * Database helper class used to manage the creation and upgrading of your
  * database. This class also usually provides the DAOs used by the other
  * classes.
  */
 public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
 
 	// name of the database file for your application -- change to something
 	// appropriate for your app
 	private static final String DATABASE_NAME = "radioTour.db";
 	// any time you make changes to your database objects, you may have to
 	// increase the database version
 	private static final int DATABASE_VERSION = 2;
 
 	private static DatabaseHelper helper;
 
 	// the DAO object we use to access the BicycleRider table
 	private RuntimeExceptionDao<Rider, Integer> riderRuntimeDao = null;
 	private RuntimeExceptionDao<Team, String> teamRuntimeDao = null;
 	private RuntimeExceptionDao<Group, Integer> groupRuntimeDao = null;
 	private RuntimeExceptionDao<Stage, Integer> stageRuntimeDao = null;
 	private RuntimeExceptionDao<PointOfRace, Integer> pointOfRaceRuntimeDao = null;
 	private RuntimeExceptionDao<SpecialRanking, Integer> specialRankingDao = null;
 	private RuntimeExceptionDao<Judgement, Integer> judgementRankingDao = null;
 	private RuntimeExceptionDao<Maillot, Integer> maillotRuntimeDao = null;
 	private RuntimeExceptionDao<RiderStageConnection, Integer> riderStageDao = null;
 	private RuntimeExceptionDao<RaceSituation, Long> raceSituationDao = null;
 	private RuntimeExceptionDao<SpecialPointHolder, Integer> specialPointDao = null;
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION,
 				R.raw.ormlite_config);
 		helper = this;
 	}
 
 	public static synchronized DatabaseHelper getHelper(Context context) {
 		if (helper == null) {
 			helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
 		}
 		return helper;
 	}
 
 	/**
 	 * This is called when the database is first created. Usually you should
 	 * call createTable statements here to create the tables that will store
 	 * your data.
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
 		try {
 			TableUtils.createTable(connectionSource, Rider.class);
 			TableUtils.createTable(connectionSource, Team.class);
 			TableUtils.createTable(connectionSource, Group.class);
 			TableUtils.createTable(connectionSource, Stage.class);
 			TableUtils.createTable(connectionSource, PointOfRace.class);
 			TableUtils.createTable(connectionSource, SpecialRanking.class);
 			TableUtils.createTable(connectionSource, Judgement.class);
 			TableUtils.createTable(connectionSource, Maillot.class);
 			TableUtils.createTable(connectionSource, RaceSituation.class);
 			TableUtils
 					.createTable(connectionSource, RiderStageConnection.class);
 			TableUtils.createTable(connectionSource, SpecialPointHolder.class);
 		} catch (SQLException e) {
 			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	/**
 	 * This is called when your application is upgraded and it has a higher
 	 * version number. This allows you to adjust the various data to match the
 	 * new version number.
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
 			int oldVersion, int newVersion) {
 		try {
 			TableUtils.dropTable(connectionSource, Rider.class, true);
 			TableUtils.dropTable(connectionSource, Team.class, true);
 			TableUtils.dropTable(connectionSource, Group.class, true);
 			TableUtils.dropTable(connectionSource, Stage.class, true);
 			TableUtils.dropTable(connectionSource, PointOfRace.class, true);
 			TableUtils.dropTable(connectionSource, SpecialRanking.class, true);
 			TableUtils.dropTable(connectionSource, Judgement.class, true);
 			TableUtils.dropTable(connectionSource, Maillot.class, true);
 			TableUtils.dropTable(connectionSource, RaceSituation.class, true);
 			TableUtils.dropTable(connectionSource, SpecialPointHolder.class,
 					true);
 			TableUtils.dropTable(connectionSource, RiderStageConnection.class,
 					true);
 			onCreate(db, connectionSource);
 		} catch (SQLException e) {
 			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao
 	 * for our BicycleRider class. It will create it or just give the cached
 	 * value. RuntimeExceptionDao only through RuntimeExceptions.
 	 */
 	public RuntimeExceptionDao<Rider, Integer> getBicycleRiderDao() {
 		if (riderRuntimeDao == null) {
 			riderRuntimeDao = getRuntimeExceptionDao(Rider.class);
 		}
 		return riderRuntimeDao;
 	}
 
 	public RuntimeExceptionDao<Group, Integer> getGroupDao() {
 		if (groupRuntimeDao == null) {
 			groupRuntimeDao = getRuntimeExceptionDao(Group.class);
 		}
 		return groupRuntimeDao;
 	}
 
 	public RuntimeExceptionDao<Team, String> getTeamDao() {
 		if (teamRuntimeDao == null) {
 			teamRuntimeDao = getRuntimeExceptionDao(Team.class);
 		}
 		return teamRuntimeDao;
 	}
 
 	public RuntimeExceptionDao<Stage, Integer> getStageDao() {
 		if (stageRuntimeDao == null) {
 			stageRuntimeDao = getRuntimeExceptionDao(Stage.class);
 		}
 		return stageRuntimeDao;
 	}
 
 	public RuntimeExceptionDao<PointOfRace, Integer> getPointOfRaceDao() {
 		if (pointOfRaceRuntimeDao == null) {
 			pointOfRaceRuntimeDao = getRuntimeExceptionDao(PointOfRace.class);
 		}
 		return pointOfRaceRuntimeDao;
 	}
 
 	public RuntimeExceptionDao<SpecialRanking, Integer> getSpecialRankingDao() {
 		if (specialRankingDao == null) {
 			specialRankingDao = getRuntimeExceptionDao(SpecialRanking.class);
 		}
 		return specialRankingDao;
 	}
 
 	public RuntimeExceptionDao<Judgement, Integer> getJudgementDao() {
 		if (judgementRankingDao == null) {
 			judgementRankingDao = getRuntimeExceptionDao(Judgement.class);
 		}
 		return judgementRankingDao;
 	}
 
 	public RuntimeExceptionDao<Maillot, Integer> getMaillotRuntimeDao() {
 		if (maillotRuntimeDao == null) {
 			maillotRuntimeDao = getRuntimeExceptionDao(Maillot.class);
 		}
 		return maillotRuntimeDao;
 	}
 
 	public RuntimeExceptionDao<RiderStageConnection, Integer> getRiderStageDao() {
 		if (riderStageDao == null) {
 			riderStageDao = getRuntimeExceptionDao(RiderStageConnection.class);
 		}
 		return riderStageDao;
 	}
 
 	public RuntimeExceptionDao<RaceSituation, Long> getRaceSituationDao() {
 		if (raceSituationDao == null) {
 			raceSituationDao = getRuntimeExceptionDao(RaceSituation.class);
 		}
 		return raceSituationDao;
 	}
 
 	public RuntimeExceptionDao<SpecialPointHolder, Integer> getSpecialPointDao() {
 		if (specialPointDao == null) {
 			specialPointDao = getRuntimeExceptionDao(SpecialPointHolder.class);
 		}
 		return specialPointDao;
 	}
 
 	/**
 	 * Close the database connections and clear any cached DAOs.
 	 */
 	@Override
 	public void close() {
 		super.close();
 		riderRuntimeDao = null;
 		teamRuntimeDao = null;
 		groupRuntimeDao = null;
 		stageRuntimeDao = null;
 		pointOfRaceRuntimeDao = null;
 		specialRankingDao = null;
 		judgementRankingDao = null;
 		maillotRuntimeDao = null;
 		riderStageDao = null;
 		raceSituationDao = null;
 		specialPointDao = null;
 	}
 }
