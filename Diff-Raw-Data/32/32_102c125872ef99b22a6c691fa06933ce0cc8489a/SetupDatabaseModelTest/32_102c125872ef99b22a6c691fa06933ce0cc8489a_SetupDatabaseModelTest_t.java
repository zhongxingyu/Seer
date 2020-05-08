 package net.todd.biblestudy.rcp.models;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.todd.biblestudy.common.BiblestudyException;
 import net.todd.biblestudy.common.ExceptionHandler;
 import net.todd.biblestudy.common.ExceptionHandlerFactory;
 import net.todd.biblestudy.common.SeverityLevel;
 import net.todd.biblestudy.db.ISetupDBDao;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class SetupDatabaseModelTest
 {
 	private MockSetupDBDao dao;
 
 	@Before
 	public void setUp()
 	{
 		dao = new MockSetupDBDao();
 		ExceptionHandlerFactory.setHandler(new ExceptionHandler()
 		{
 			@Override
 			public void handle(String message, Object origin, Throwable t,
 					SeverityLevel severityLevel)
 			{
 			}
 		});
 	}
 
 	@Test
 	public void testModelValidatesCredentials()
 	{
 		dao.setValid(true);
 		SetupDatabaseModel model = new SetupDatabaseModel(dao);
 
 		try
 		{
 			assertFalse(model.validateDatabaseCredentials(null, null, null));
 			assertFalse(model.validateDatabaseCredentials("user", null, null));
 			assertFalse(model.validateDatabaseCredentials(null, "pass", null));
 			assertFalse(model.validateDatabaseCredentials(null, "pass", "url"));
 			assertFalse(model.validateDatabaseCredentials(null, null, "url"));
 			assertFalse(model.validateDatabaseCredentials("user", null, "url"));
 			assertFalse(model.validateDatabaseCredentials(null, "pass", "url"));
 		}
 		catch (BiblestudyException e)
 		{
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 
 		dao.setValid(false);
 
 		try
 		{
 			assertFalse(model.validateDatabaseCredentials("user", "pass", "url"));
 			fail();
 		}
 		catch (BiblestudyException e)
 		{
 			assertEquals("Bad database credentials", e.getMessage());
 		}
 
 		dao.setValid(true);
 		try
 		{
 			assertTrue(model.validateDatabaseCredentials("user", "pass", "url"));
 		}
 		catch (BiblestudyException e)
 		{
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testNotCurrentVersion()
 	{
 		dao.setDatabaseVersion(5);
 
 		SetupDatabaseModel model = new SetupDatabaseModel(dao)
 		{
 			@Override
 			int getCurrentApplicationVersion()
 			{
 				return 4;
 			}
 		};
 
 		try
 		{
 			assertFalse(model.isVersionCurrent());
 		}
 		catch (BiblestudyException e)
 		{
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 
 		dao.setDatabaseVersion(2);
 
 		model = new SetupDatabaseModel(dao)
 		{
 			@Override
 			int getCurrentApplicationVersion()
 			{
 				return 3;
 			}
 		};
 
 		try
 		{
 			assertFalse(model.isVersionCurrent());
 		}
 		catch (BiblestudyException e)
 		{
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testIsCurrentVersion()
 	{
 		dao.setDatabaseVersion(1);
 
 		SetupDatabaseModel model = new SetupDatabaseModel(dao)
 		{
 			@Override
 			int getCurrentApplicationVersion()
 			{
 				return 1;
 			}
 		};
 
 		try
 		{
 			assertTrue(model.isVersionCurrent());
 		}
 		catch (BiblestudyException e)
 		{
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 
 		dao.setDatabaseVersion(2);
 
 		model = new SetupDatabaseModel(dao)
 		{
 			@Override
 			int getCurrentApplicationVersion()
 			{
 				return 2;
 			}
 		};
 
 		try
 		{
 			assertTrue(model.isVersionCurrent());
 		}
 		catch (BiblestudyException e)
 		{
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testGetDatabaseVersionFromFile()
 	{
 		SetupDatabaseModel model = new SetupDatabaseModel(dao);
 
 		int currentDatabaseVersion = model.getCurrentApplicationVersion();
 
 		assertTrue(currentDatabaseVersion != -1);
 	}
 
 	@Test
 	public void testInitializeDb()
 	{
 		SetupDatabaseModel model = new SetupDatabaseModel(dao)
 		{
 			@Override
 			int getCurrentDatabaseVersion() throws BiblestudyException
 			{
				return 0;
 			}
 
 			@Override
 			int getCurrentApplicationVersion()
 			{
				return 2;
 			}
 		};
 
 		assertEquals(0, dao.filesToProcess.size());
 
 		try
 		{
 			model.initializeDatabase();
 		}
 		catch (BiblestudyException e)
 		{
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 
		assertEquals(2, dao.filesToProcess.size());
		assertTrue(dao.filesToProcess.get(0).endsWith("0.biblestudy.sql"));
		assertTrue(dao.filesToProcess.get(1).endsWith("1.biblestudy.sql"));
 	}
 
 	// TODO: test that areDatabaseCredentialsPresent checks preferences
 	// TODO: test that validateDatabaseCredentials saves given creds to
 	// preferences
 
 	private static class MockSetupDBDao implements ISetupDBDao
 	{
 		private List<String> filesToProcess = new ArrayList<String>();
 		private boolean valid;
 
 		public void setValid(boolean valid)
 		{
 			this.valid = valid;
 		}
 
 		public void connectWithCredentials(String user, String pass, String url)
 				throws BiblestudyException
 		{
 			if (!valid)
 			{
 				throw new BiblestudyException("Bad database credentials");
 			}
 		}
 
 		private int databaseVersion;
 
 		public void setDatabaseVersion(int databaseVersion)
 		{
 			this.databaseVersion = databaseVersion;
 		}
 
 		public int getDatabaseVersion() throws BiblestudyException
 		{
 			return databaseVersion;
 		}
 
 		public void processSqlFromFile(File sqlFile)
 		{
 			filesToProcess.add(sqlFile.getAbsolutePath());
 		}
 	}
 }
