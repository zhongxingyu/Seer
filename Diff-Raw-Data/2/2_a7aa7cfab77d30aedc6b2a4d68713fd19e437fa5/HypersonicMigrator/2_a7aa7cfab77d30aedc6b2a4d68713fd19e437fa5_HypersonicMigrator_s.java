 /*
  * Feb 8, 2006
  */
 package com.thinkparity.migrator.io.hsqldb.util;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.thinkparity.codebase.DateUtil;
 import com.thinkparity.codebase.Mode;
 import com.thinkparity.codebase.DateUtil.DateImage;
 import com.thinkparity.codebase.config.Config;
 import com.thinkparity.codebase.config.ConfigFactory;
 
 import com.thinkparity.migrator.Library;
 import com.thinkparity.migrator.LoggerFactory;
 import com.thinkparity.migrator.Version;
 import com.thinkparity.migrator.io.hsqldb.HypersonicException;
 import com.thinkparity.migrator.io.hsqldb.HypersonicSession;
 import com.thinkparity.migrator.io.hsqldb.HypersonicSessionManager;
 import com.thinkparity.migrator.io.hsqldb.Table;
 import com.thinkparity.migrator.io.md.MetaDataType;
 import com.thinkparity.migrator.util.ChecksumUtil;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 class HypersonicMigrator {
 
 	private static final Config CONFIG;
 
 	private static final String[] CREATE_SCHEMA_SQL;
 
 	private static final String INSERT_SEED_LIBRARY_TYPE;
 
     private static final String INSERT_SEED_META_DATA_TYPE;
 
     private static final String INSERT_SEED_VERSION;
 
     private static final String INSERT_TEST_DATA_LIBRARY;
 
     private static final String INSERT_TEST_DATA_LIBRARY_BYTES;
 
     private static final String INSERT_TEST_DATA_RELEASE;
 
     private static final String INSERT_TEST_DATA_RELEASE_LIBRARY_REL;
 
 	private static final String READ_META_DATA_VERSION;
 
 	static {
 		// sql statements
 		CONFIG = ConfigFactory.newInstance("io/hsqldb/hypersonicMigrator.properties");
 
 		CREATE_SCHEMA_SQL = new String[] {
                 CONFIG.getProperty("CreateMetaDataType"),
                 CONFIG.getProperty("CreateMetaData"),
                 CONFIG.getProperty("CreateLibraryType"),
 				CONFIG.getProperty("CreateLibrary"),
 				CONFIG.getProperty("CreateLibraryBytes"),
                 CONFIG.getProperty("CreateRelease"),
                 CONFIG.getProperty("CreateReleaseLibraryRel")
 		};
 
 		INSERT_SEED_LIBRARY_TYPE = CONFIG.getProperty("InsertSeedLibraryType");
 
         INSERT_SEED_META_DATA_TYPE = CONFIG.getProperty("InsertSeedMetaDataType");
 		
 		INSERT_SEED_VERSION = CONFIG.getProperty("InsertSeedVersion");
 
         INSERT_TEST_DATA_LIBRARY = CONFIG.getProperty("InsertTestDataLibrary");
 
         INSERT_TEST_DATA_LIBRARY_BYTES = CONFIG.getProperty("InsertTestDataLibraryBytes");
 
         INSERT_TEST_DATA_RELEASE = CONFIG.getProperty("InsertTestDataRelease");
 
         INSERT_TEST_DATA_RELEASE_LIBRARY_REL = CONFIG.getProperty("InsertTestDataReleaseLibraryRel");
 
 		READ_META_DATA_VERSION = CONFIG.getProperty("ReadMetaDataVersion");
 	}
 
 	protected final Logger logger;
 
 	/**
 	 * Create a HypersonicMigrator.
 	 * 
 	 */
 	HypersonicMigrator() {
 		super();
 		this.logger = LoggerFactory.getLogger(getClass());
 	}
 
 	void migrate() throws HypersonicException {
 		final String actualVersionId = Version.getBuildId();
 		final String expectedVersionId = getExpectedVersionId();
 		if(null == expectedVersionId) { initializeSchema(); }
 		else if(actualVersionId.equals(expectedVersionId)) {
 			migrateSchema(expectedVersionId, actualVersionId);
 		}
 	}
 
 	private void createSchema(final HypersonicSession session) {
 		for(final String sql : CREATE_SCHEMA_SQL) {
 			session.execute(sql);
 		}
 	}
 
 	private String getExpectedVersionId() {
 		final List<Table> tables = HypersonicSessionManager.listTables();
 		Boolean isTableFound = Boolean.FALSE;
 		for(final Table t : tables) {
 			if(t.getName().equals("META_DATA")) {
 				isTableFound = Boolean.TRUE;
 				break;
 			}
 		}
 		if(!isTableFound) { return null; }
 		else {
 			final HypersonicSession session = HypersonicSessionManager.openSession();
 			try {
 				session.prepareStatement(READ_META_DATA_VERSION);
 				session.setLong(1, 1000L);
 				session.setTypeAsInteger(2, MetaDataType.STRING);
 				session.setString(3, "VERSION");
 				session.executeQuery();
 				session.nextResult();
 				return session.getString("VALUE");
 			}
 			finally { session.close(); }
 		}
 	}
 
 	/**
 	 * Initialize the parity model schema.
 	 * 
 	 */
 	private void initializeSchema() {
 		final HypersonicSession session = HypersonicSessionManager.openSession();
 		try {
 			createSchema(session);
 			insertSeedData(session);
             if(Mode.DEVELOPMENT == Version.getMode()) {
                 insertTestData(session);
             }
 			session.commit();
 		}
 		catch(final HypersonicException hx) {
 			session.rollback();
 			logger.fatal("Could not create parity schema.", hx);
 			throw hx;
 		}
 		finally { session.close(); }
 	}
 
 	private void insertSeedData(final HypersonicSession session) {
 		session.prepareStatement(INSERT_SEED_META_DATA_TYPE);
 		for(final MetaDataType mdt : MetaDataType.values()) {
 			session.setTypeAsInteger(1, mdt);
 			session.setTypeAsString(2, mdt);
 			if(1 != session.executeUpdate())
 				throw new HypersonicException(
 						"[RMIGRATOR] [IO] [UTIL] [HYPERSONIC MIGRATOR] [INSERT SEED DATA] [CANNOT INSERT META DATA SEED DATA]");
 		}
 
 		session.prepareStatement(INSERT_SEED_VERSION);
 		session.setTypeAsInteger(1, MetaDataType.STRING);
 		session.setString(2, "VERSION");
 		session.setString(3, Version.getBuildId());
 		if(1 != session.executeUpdate())
 			throw new HypersonicException(
                     "[RMIGRATOR] [IO] [UTIL] [HYPERSONIC MIGRATOR] [CANNOT INSERT VERSION META DATA]");
 
         session.prepareStatement(INSERT_SEED_LIBRARY_TYPE);
         for(final Library.Type t : Library.Type.values()) {
             session.setTypeAsInteger(1, t);
             session.setTypeAsString(2, t);
             if(1 != session.executeUpdate())
                 throw new HypersonicException(
                         "[RMIGRATOR] [IO] [UTIL] [HYPERSONIC MIGRATOR] [CANNOT INSERT LIBRARY TYPE SEED DATA]");
         }
 	}
 
     /**
      * Insert test data.  This will insert a single dummy release with
      * dummy libraries.
      *
      * @param session
      *      A database session.
      */
     private void insertTestData(final HypersonicSession session) {
         // v1.0.0
         final List<Long> libraryIds = new ArrayList<Long>();
         Calendar createdOn = null;
         try { createdOn = DateUtil.parse("1970 01 01 00:00", DateImage.YearMonthDayHourMinute); }
         catch(final ParseException px) { throw new HypersonicException(px); }
         libraryIds.add(insertTestDataLibrary(session, Library.Type.JAVA,
                 "com.thinkparity.parity", "tJavaLibrary", "1.0.0",
                 "core/tJavaLibrary-1.0.0.jar", createdOn,
                 "com.thinkparity.parity:tJavaLibrary:1.0.0".getBytes()));
         libraryIds.add(insertTestDataLibrary(session, Library.Type.JAVA,
                 "com.3rdparty", "tJavaLibrary", "1.0.0",
                 "lib/tJavaLibrary-1.0.0.jar", createdOn,
                 "com.3rdparty:tJavaLibrary:1.0.0".getBytes()));
         libraryIds.add(insertTestDataLibrary(session, Library.Type.NATIVE,
                 "com.3rdparty", "tNativeLibrary", "1.0.0",
                 "lib/win32/tNativeLibrary.dll", createdOn,
                 "com.3rdparty:tJavaLibrary:1.0.0".getBytes()));
         insertTestDataRelease(session, "com.thinkparity.parity", "tRelease",
                     "1.0.0", createdOn, libraryIds);
         // v1.0.1
         libraryIds.clear();
         try { createdOn = DateUtil.parse("1970 01 01 00:01", DateImage.YearMonthDayHourMinute); }
         catch(final ParseException px) { throw new HypersonicException(px); }
         libraryIds.add(insertTestDataLibrary(session, Library.Type.JAVA,
                 "com.thinkparity.parity", "tJavaLibrary", "1.0.1",
                 "core/tJavaLibrary-1.0.1.jar", createdOn,
                 "com.thinkparity.parity:tJavaLibrary:1.0.1".getBytes()));
         libraryIds.add(insertTestDataLibrary(session, Library.Type.JAVA,
                 "com.3rdparty", "tJavaLibrary", "1.0.1",
                 "lib/tJavaLibrary-1.0.1.jar", createdOn,
                 "com.3rdparty:tJavaLibrary:1.0.1".getBytes()));
         libraryIds.add(insertTestDataLibrary(session, Library.Type.NATIVE,
                 "com.3rdparty", "tNativeLibrary", "1.0.1",
                 "lib/win32/tNativeLibrary.dll", createdOn,
                 "com.3rdparty:tJavaLibrary:1.0.1".getBytes()));
         insertTestDataRelease(session, "com.thinkparity.parity", "tRelease",
                     "1.0.1", createdOn, libraryIds);
     }
 
     /**
      * Insert a test library into the database.
      *
      * @param type
      *      A library type.
      * @param groupId
      *      A group id.
      * @param artifactId
      *      An artifact id.
      * @param version
      *      A version.
      * @param bytes
      *      A byte array.
      */
     private Long insertTestDataLibrary(final HypersonicSession session,
             final Library.Type type, final String groupId,
             final String artifactId, final String version, final String path,
             final Calendar createdOn, final byte[] bytes) {
         session.prepareStatement(INSERT_TEST_DATA_LIBRARY);
         session.setTypeAsInteger(1, type);
         session.setString(2, groupId);
         session.setString(3, artifactId);
         session.setString(4, version);
         session.setString(5, path);
        session.setCalendar(5, createdOn);
         if(1 != session.executeUpdate())
             throw new HypersonicException("[RMIGRATOR] [IO] [UTIL] [HYPERSONIC MIGRATOR] [CANNOT INSERT LIBRARY TEST DATA]");
         final Long libraryId = session.getIdentity();
         session.prepareStatement(INSERT_TEST_DATA_LIBRARY_BYTES);
         session.setLong(1, libraryId);
         session.setBytes(2, bytes);
         session.setString(3, ChecksumUtil.md5Hex(bytes));
         if(1 != session.executeUpdate())
             throw new HypersonicException("[RMIGRATOR] [IO] [UTIL] [HYPERSONIC MIGRATOR] [CANNOT INSERT LIBRARY BYTES TEST DATA");
 
         return libraryId;
     }
 
     /**
      * Insert a test release into the databse.
      *
      * @param session
      *      A database session.
      * @param groupId
      *      A group id.
      * @param artifactId
      *      An artifact id.
      * @param version
      *      A version.
      * @param libraryIds
      *      Library ids.
      */
     private void insertTestDataRelease(final HypersonicSession session,
             final String groupId, final String artifactId,
             final String version, final Calendar createdOn,
             final List<Long> libraryIds) {
         session.prepareStatement(INSERT_TEST_DATA_RELEASE);
         session.setString(1, groupId);
         session.setString(2, artifactId);
         session.setString(3, version);
         session.setCalendar(4, createdOn);
         if(1 != session.executeUpdate())
             throw new HypersonicException("[RMIGRATOR] [IO] [UTIL] [HYPERSONIC MIGRATOR] [CANNOT INSERT RELEASE TEST DATA]");
         final Long releaseId = session.getIdentity();
 
         session.prepareStatement(INSERT_TEST_DATA_RELEASE_LIBRARY_REL);
         session.setLong(1, releaseId);
         for(final Long libraryId : libraryIds) {
             session.setLong(2, libraryId);
             if(1 != session.executeUpdate())
                 throw new HypersonicException("[RMIGRATOR] [IO] [UTIL] [HYPERSONIC MIGRATOR] [CANNOT INSERT RELEASE LIBRARY REL TEST DATA]");
         }
     }
 
 	private void migrateSchema(final String fromVersionId,
 			final String toVersionId) {}
 }
