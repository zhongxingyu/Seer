 package net.todd.biblestudy.rcp.models;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 
 import net.todd.biblestudy.common.BiblestudyException;
 import net.todd.biblestudy.common.ExceptionHandlerFactory;
 import net.todd.biblestudy.common.SeverityLevel;
 import net.todd.biblestudy.db.ISetupDBDao;
 import net.todd.biblestudy.rcp.Activator;
 import net.todd.biblestudy.rcp.PreferenceInitializer;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.preferences.ConfigurationScope;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.osgi.framework.Bundle;
 
 public class SetupDatabaseModel implements ISetupDatabaseModel
 {
 	private final ISetupDBDao setupDBDao;
 
 	public SetupDatabaseModel(ISetupDBDao setupDBDao)
 	{
 		this.setupDBDao = setupDBDao;
 	}
 
 	public boolean areDatabaseCredentialsPresent()
 	{
 		IPreferenceStore preferences = new ScopedPreferenceStore(new ConfigurationScope(),
 				Activator.PLUGIN_ID);
 
 		String username = preferences.getString(PreferenceInitializer.DB_USER);
 		String password = preferences.getString(PreferenceInitializer.DB_PASS);
 		String url = preferences.getString(PreferenceInitializer.DB_URL);
 
 		return !(StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils
 				.isEmpty(url));
 	}
 
 	public void initializeDatabase() throws BiblestudyException
 	{
		for (int i = getCurrentDatabaseVersion(); i < getCurrentApplicationVersion(); i++)
 		{
 			try
 			{
 				String sqlFileName = "resources/db/sql_files/" + i + ".biblestudy.sql";
 				URL resource = Platform.getBundle(Activator.PLUGIN_ID).getResource(sqlFileName);
 				String filename = FileLocator.resolve(resource).getFile();
 				File file = new File(filename);
 				setupDBDao.processSqlFromFile(file);
 			}
 			catch (Exception e)
 			{
 				throw new BiblestudyException(e);
 			}
 		}
 	}
 
 	public boolean validateDatabaseCredentials(String user, String pass, String url)
 			throws BiblestudyException
 	{
 		boolean valid = false;
 		if (user != null && pass != null && url != null)
 		{
 			store(user, pass, url);
 			setupDBDao.connectWithCredentials(user, pass, url);
 			valid = true;
 		}
 		return valid;
 	}
 
 	private void store(String user, String pass, String url) throws BiblestudyException
 	{
 		ScopedPreferenceStore preferences = new ScopedPreferenceStore(new ConfigurationScope(),
 				Activator.PLUGIN_ID);
 
 		preferences.setValue(PreferenceInitializer.DB_USER, user);
 		preferences.setValue(PreferenceInitializer.DB_PASS, pass);
 		preferences.setValue(PreferenceInitializer.DB_URL, url);
 
 		try
 		{
 			preferences.save();
 		}
 		catch (IOException e)
 		{
 			throw new BiblestudyException(e);
 		}
 	}
 
 	public boolean isVersionCurrent() throws BiblestudyException
 	{
 		boolean current = getCurrentDatabaseVersion() == getCurrentApplicationVersion();
 
 		return current;
 	}
 
 	int getCurrentDatabaseVersion() throws BiblestudyException
 	{
 		return setupDBDao.getDatabaseVersion();
 	}
 
 	int getCurrentApplicationVersion()
 	{
 		int version = -1;
 		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
 		if (bundle != null)
 		{
 			URL resource = bundle.getResource("resources/db/currentApplicationVersion.txt");
 			BufferedReader reader = null;
 
 			try
 			{
 				URL resolvedResource = FileLocator.resolve(resource);
 				File file = new File(resolvedResource.getFile());
 				reader = new BufferedReader(new FileReader(file));
 				String versionString = reader.readLine().trim();
 
 				version = Integer.parseInt(versionString);
 			}
 			catch (IOException e)
 			{
 				ExceptionHandlerFactory.getHandler().handle(
 						"Could not find the version configuration file.", this, e,
 						SeverityLevel.ERROR);
 			}
 			catch (NumberFormatException e)
 			{
 				ExceptionHandlerFactory.getHandler().handle(
 						"No version number found in the version configuration file.", this, e,
 						SeverityLevel.ERROR);
 			}
 			finally
 			{
 				if (reader != null)
 				{
 					try
 					{
 						reader.close();
 					}
 					catch (IOException e)
 					{
 					}
 				}
 			}
 		}
 
 		return version;
 	}
 }
