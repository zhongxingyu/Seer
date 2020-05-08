 package nz.cri.gns.springs.db;
 
 import java.sql.SQLException;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 /**
  * Initiates creation of and connections to the app's database.
  * @author duncanw
  */
 public class SpringsDbHelper extends OrmLiteSqliteOpenHelper  {
 	
     // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 25;
     public static final String DATABASE_NAME = "1000-Springs-DB";
     
     private RuntimeExceptionDao<Feature, Long> featureDao = null;
     private RuntimeExceptionDao<Survey, Long> surveyDao = null;
     private RuntimeExceptionDao<SurveyImage, Long> surveyImageDao = null;
     private RuntimeExceptionDao<BiologicalSample, Long> biologicalSampleDao = null;
     private RuntimeExceptionDao<ChecklistItem, Long> checklistItemDao = null;
     private RuntimeExceptionDao<Configuration, String> configurationDao = null;
     
     public SpringsDbHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
 	@Override
 	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
 		createTables(connectionSource);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,int oldVersion, int newVersion) {
 		dropTables(db);
 		createTables(connectionSource);
 	}
 	
 	
 	private void dropTables(SQLiteDatabase db) {
 		
 		db.execSQL("DROP TABLE Feature");
 		db.execSQL("DROP TABLE BiologicalSample");
 		db.execSQL("DROP TABLE Survey");
 		db.execSQL("DROP TABLE SurveyImage");
 		db.execSQL("DROP TABLE ChecklistItem");
 		db.execSQL("DROP TABLE Configuration");
 	}
 	
 	private void createTables(ConnectionSource connectionSource) {
 		try {
 			TableUtils.createTable(connectionSource, Feature.class);
 			TableUtils.createTable(connectionSource, Survey.class);
 			TableUtils.createTable(connectionSource, SurveyImage.class);
 			TableUtils.createTable(connectionSource, BiologicalSample.class);
 			TableUtils.createTable(connectionSource, ChecklistItem.class);
 			TableUtils.createTable(connectionSource, Configuration.class);
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}		
 	}
 	
 	public RuntimeExceptionDao<Feature, Long> getFeatureDao() {
 		if (featureDao == null) {
 			Dao<Feature, Long> dao;
 			try {
 				dao = DaoManager.createDao(getConnectionSource(), Feature.class);
 				featureDao = new RuntimeExceptionDao<Feature, Long>(dao) {
 					private SpringsUpdater <Feature> springsUpdater = new SpringsUpdater<Feature>();
 					@Override
 					public int update(Feature feature) {
 						springsUpdater.update(feature, this);
 						return super.update(feature);
 					}
 				};
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		return featureDao;
 	}
 	
 	public RuntimeExceptionDao<Survey, Long> getSurveyDao() {
 		if (surveyDao == null) {
 			Dao<Survey, Long> dao;
 			try {
 				dao = DaoManager.createDao(getConnectionSource(), Survey.class);
 				surveyDao = new RuntimeExceptionDao<Survey, Long>(dao) {
 					private SpringsUpdater <Survey> springsUpdater = new SpringsUpdater<Survey>();
 					@Override
 					public int update(Survey survey) {
 						springsUpdater.update(survey, this);
 						return super.update(survey);
 					}
 				};
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		return surveyDao;
 	}
 	
 	public RuntimeExceptionDao<SurveyImage, Long> getSurveyImageDao() {
 		if (surveyImageDao == null) {
 			Dao<SurveyImage, Long> dao;
 			try {
 				dao = DaoManager.createDao(getConnectionSource(), SurveyImage.class);
 				surveyImageDao = new RuntimeExceptionDao<SurveyImage, Long>(dao) {
 					private SpringsUpdater <SurveyImage> springsUpdater = new SpringsUpdater<SurveyImage>();
 					@Override
 					public int update(SurveyImage surveyImage) {
 						springsUpdater.update(surveyImage, this);
 						return super.update(surveyImage);
 					}
 				};
 			} catch (SQLException e) {
 				throw new RuntimeException();
 			}
 		}
 		
 		return surveyImageDao;
 	}
 	
 	public RuntimeExceptionDao<BiologicalSample, Long> getBiologicalSampleDao() {
 		if (biologicalSampleDao == null) {
 			Dao<BiologicalSample, Long> dao;
 			try {
 				dao = DaoManager.createDao(getConnectionSource(), BiologicalSample.class);
 				biologicalSampleDao = new RuntimeExceptionDao<BiologicalSample, Long>(dao) {
 					private SpringsUpdater <BiologicalSample> springsUpdater = new SpringsUpdater<BiologicalSample>();
 					@Override
 					public int update(BiologicalSample biologicalSample) {
 						springsUpdater.update(biologicalSample, this);
 						return super.update(biologicalSample);
 					}
 				};
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		return biologicalSampleDao;
 	}
 	
 	public RuntimeExceptionDao<ChecklistItem, Long> getChecklistItemDao() {
 		if (checklistItemDao == null) {
 			Dao<ChecklistItem, Long> dao;
 			try {
 				dao = DaoManager.createDao(getConnectionSource(), ChecklistItem.class);
 				checklistItemDao = new RuntimeExceptionDao<ChecklistItem, Long>(dao);
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		return checklistItemDao;
 	}
 	
 	public RuntimeExceptionDao<Configuration, String> getConfigurationDao() {
 		if (configurationDao == null) {
 			Dao<Configuration, String> dao;
 			try {
 				dao = DaoManager.createDao(getConnectionSource(), Configuration.class);
 				configurationDao = new RuntimeExceptionDao<Configuration, String>(dao);
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		return configurationDao;
 	}	
 	
 	public static class SpringsUpdater<T extends PersistentObject> {
 		
 		public void update(T object, RuntimeExceptionDao<T, Long> dao) {
 			T current = dao.queryForSameId(object);
 			if (PersistentObject.Status.EXPORTED == current.getStatus()) {
 				object.setStatus(Feature.Status.UPDATED);
 			}
 			object.setUpdatedDate(System.currentTimeMillis());			
 		}
 	}
 
 }
