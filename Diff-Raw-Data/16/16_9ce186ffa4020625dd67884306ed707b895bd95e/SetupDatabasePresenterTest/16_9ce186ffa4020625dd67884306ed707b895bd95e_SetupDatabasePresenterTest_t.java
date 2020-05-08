 package net.todd.biblestudy.rcp.presenters;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import net.todd.biblestudy.rcp.models.ISetupDatabaseModel;
 import net.todd.biblestudy.rcp.views.ISetupDatabaseView;
 import net.todd.biblestudy.rcp.views.UserCredentials;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class SetupDatabasePresenterTest
 {
 	private MockSetupDatabaseView view;
 	private MockSetupDatabaseModel model;
 
 	@Before
 	public void setup()
 	{
 		view = new MockSetupDatabaseView();
 		model = new MockSetupDatabaseModel();
 	}
 
 	@Test
 	public void testIfModelHasDatabaseCredentialsButNotValidDatabaseVersion() throws Exception
 	{
 		model.setIsVersionCurrent(false);
 		model.setDatabaseCredentialsPresent(true);
 
 		assertFalse(model.hasCalledDatabaseCredentialsPresent());
 		assertFalse(model.hasDatabaseBeenInitialized());
 
 		new SetupDatabasePresenter(view, model).setup();
 
 		assertTrue(model.hasCalledDatabaseCredentialsPresent());
 		assertTrue(model.hasDatabaseBeenInitialized());
 	}
 
 	@Test
 	public void testIfModelHasDatabaseCredentialsAndHasValidDatabaseVersion() throws Exception
 	{
 		model.setIsVersionCurrent(true);
 		model.setDatabaseCredentialsPresent(true);
 
 		assertFalse(model.hasCalledDatabaseCredentialsPresent());
 		assertFalse(model.hasDatabaseBeenInitialized());
 
 		new SetupDatabasePresenter(view, model).setup();
 
 		assertTrue(model.hasCalledDatabaseCredentialsPresent());
 		assertFalse(model.hasDatabaseBeenInitialized());
 	}
 
 	@Test
	public void testSetupReturnTrueIfModelHasDatabaseCredentialsAndHasValidDatabaseVersion()
			throws Exception
	{
		model.setIsVersionCurrent(true);
		model.setDatabaseCredentialsPresent(true);

		assertFalse(model.hasCalledDatabaseCredentialsPresent());
		assertFalse(model.hasDatabaseBeenInitialized());

		assertTrue(new SetupDatabasePresenter(view, model).setup());

		assertTrue(model.hasCalledDatabaseCredentialsPresent());
		assertFalse(model.hasDatabaseBeenInitialized());
	}

	@Test
 	public void testIfModelDoesntHaveCredentialsPresenterPromptsUser() throws Exception
 	{
 		model.setDatabaseCredentialsPresent(false);
 
 		assertFalse(model.hasCalledDatabaseCredentialsPresent());
 		assertFalse(view.hasPromptedUserForDatabaseCredentials());
 
 		new SetupDatabasePresenter(view, model).setup();
 
 		assertTrue(model.hasCalledDatabaseCredentialsPresent());
 		assertTrue(view.hasPromptedUserForDatabaseCredentials());
 		assertFalse(model.hasDatabaseBeenInitialized());
 	}
 
 	@Test
 	public void testModelIsToldToStoreCredentialsAFterGivenCredentialsFromUser() throws Exception
 	{
 		UserCredentials creds = new UserCredentials();
 		creds.setUser("user");
 		creds.setPass("pass");
 
 		model.setValidCredentials(true);
 		view.setUserCreds(creds);
 
 		SetupDatabasePresenter presenter = new SetupDatabasePresenter(view, model);
 
 		assertFalse(model.hasDatabaseCredentialsBeenStored());
 
 		presenter.setup();
 
 		assertTrue(model.hasDatabaseCredentialsBeenStored());
 		assertEquals("user", model.getStoredUsername());
 		assertEquals("pass", model.getStoredPassword());
 	}
 
 	@Test
 	public void testModelIsToldToStoreCredentialsAFterGivenCredentialsFromUser2() throws Exception
 	{
 		UserCredentials creds = new UserCredentials();
 		creds.setUser("user1");
 		creds.setPass("pass1");
 
 		model.setValidCredentials(true);
 		view.setUserCreds(creds);
 
 		SetupDatabasePresenter presenter = new SetupDatabasePresenter(view, model);
 
 		assertFalse(model.hasDatabaseCredentialsBeenStored());
 
 		presenter.setup();
 
 		assertTrue(model.hasDatabaseCredentialsBeenStored());
 		assertEquals("user1", model.getStoredUsername());
 		assertEquals("pass1", model.getStoredPassword());
 	}
 
 	@Test
 	public void testPresenterInitializesDatabaseAfterGivenCredentialsFromUser() throws Exception
 	{
 		model.setValidCredentials(true);
 		view.setUserCreds(new UserCredentials());
 
 		SetupDatabasePresenter presenter = new SetupDatabasePresenter(view, model);
 
 		assertFalse(model.hasDatabaseBeenInitialized());
 
 		presenter.setup();
 
 		assertTrue(model.hasDatabaseBeenInitialized());
 	}
 
 	@Test
 	public void testPresenterDoesNotInitializesDatabaseIfModelDoesNotValidateCreds()
 			throws Exception
 	{
 		model.setValidCredentials(false);
 		view.setUserCreds(new UserCredentials());
 
 		SetupDatabasePresenter presenter = new SetupDatabasePresenter(view, model);
 
 		assertFalse(model.hasDatabaseBeenInitialized());
 
 		presenter.setup();
 
 		assertFalse(model.hasDatabaseBeenInitialized());
 	}
 
 	@Test
 	public void testPresenterDoesNotInitializesDatabaseIfVersionIsCurrent() throws Exception
 	{
 		model.setValidCredentials(true);
 		model.setIsVersionCurrent(true);
 		view.setUserCreds(new UserCredentials());
 
 		SetupDatabasePresenter presenter = new SetupDatabasePresenter(view, model);
 
 		assertFalse(model.hasDatabaseBeenInitialized());
 
 		presenter.setup();
 
 		assertFalse(model.hasDatabaseBeenInitialized());
 	}
 
 	@Test
 	public void testPresenterDoesInitializesDatabaseIfVersionIsNotCurrent() throws Exception
 	{
 		model.setValidCredentials(true);
 		model.setIsVersionCurrent(false);
 		view.setUserCreds(new UserCredentials());
 
 		SetupDatabasePresenter presenter = new SetupDatabasePresenter(view, model);
 
 		assertFalse(model.hasDatabaseBeenInitialized());
 
 		presenter.setup();
 
 		assertTrue(model.hasDatabaseBeenInitialized());
 	}
 
 	private class MockSetupDatabaseView implements ISetupDatabaseView
 	{
 		private boolean promptedUserForDatabaseCredentials;
 
 		public boolean hasPromptedUserForDatabaseCredentials()
 		{
 			return promptedUserForDatabaseCredentials;
 		}
 
 		private UserCredentials creds;
 
 		public void setUserCreds(UserCredentials creds)
 		{
 			this.creds = creds;
 		}
 
 		public UserCredentials promptUserForDatabaseCredentials()
 		{
 			promptedUserForDatabaseCredentials = true;
 			return creds;
 		}
 	}
 
 	private class MockSetupDatabaseModel implements ISetupDatabaseModel
 	{
 		private boolean calledDatabaseCredentialsPresent;
 		private boolean isDatabaseCredentialsPresent;
 
 		public boolean hasCalledDatabaseCredentialsPresent()
 		{
 			return calledDatabaseCredentialsPresent;
 		}
 
 		public boolean areDatabaseCredentialsPresent()
 		{
 			calledDatabaseCredentialsPresent = true;
 			return isDatabaseCredentialsPresent;
 		}
 
 		public void setDatabaseCredentialsPresent(boolean databaseCredentialsPresent)
 		{
 			isDatabaseCredentialsPresent = databaseCredentialsPresent;
 		}
 
 		private boolean databaseHasBeenInitialized;
 
 		public boolean hasDatabaseBeenInitialized()
 		{
 			return databaseHasBeenInitialized;
 		}
 
 		public void initializeDatabase()
 		{
 			databaseHasBeenInitialized = true;
 		}
 
 		private boolean databaseCredentialsHasBeenStored;
 
 		public boolean hasDatabaseCredentialsBeenStored()
 		{
 			return databaseCredentialsHasBeenStored;
 		}
 
 		private String user;
 		private String pass;
 
 		private boolean validCredentials;
 
 		public void setValidCredentials(boolean validCredentials)
 		{
 			this.validCredentials = validCredentials;
 		}
 
 		public boolean validateDatabaseCredentials(String user, String pass, String url)
 		{
 			if (validCredentials)
 			{
 				databaseCredentialsHasBeenStored = true;
 				this.user = user;
 				this.pass = pass;
 
 				return true;
 			}
 
 			return false;
 		}
 
 		public String getStoredPassword()
 		{
 			return pass;
 		}
 
 		public String getStoredUsername()
 		{
 			return user;
 		}
 
 		private boolean versionIsCurrent;
 
 		public void setIsVersionCurrent(boolean versionIsCurrent)
 		{
 			this.versionIsCurrent = versionIsCurrent;
 		}
 
 		public boolean isVersionCurrent()
 		{
 			return versionIsCurrent;
 		}
 	}
 }
