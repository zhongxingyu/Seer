 package net.todd.biblestudy.db;
 
 import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
 
 import java.sql.Connection;
 
 import net.todd.biblestudy.rcp.Activator;
 import net.todd.biblestudy.rcp.PreferenceInitializer;
 
 import org.eclipse.core.runtime.preferences.ConfigurationScope;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class BaseDaoTest
 {
 	@Before
 	public void setUp()
 	{
 		ScopedPreferenceStore preference = new ScopedPreferenceStore(new ConfigurationScope(),
 				Activator.PLUGIN_ID);
 		preference.setValue(PreferenceInitializer.DB_USER, "root");
 		preference.setValue(PreferenceInitializer.DB_PASS, "root");
 	}
 
 	@After
 	public void tearDown()
 	{
 		ScopedPreferenceStore preference = new ScopedPreferenceStore(new ConfigurationScope(),
 				Activator.PLUGIN_ID);
 		preference.setValue(PreferenceInitializer.DB_USER, preference
 				.getDefaultString(PreferenceInitializer.DB_USER));
 		preference.setValue(PreferenceInitializer.DB_PASS, preference
 				.getDefaultString(PreferenceInitializer.DB_PASS));
 	}
 
 	@Test
 	public void testDBConnection() throws Exception
 	{
 		Connection connection = new BaseDao()
 		{
 		}.getSqlMapConfig().getDataSource().getConnection();
		assertTrue(connection.isValid(100));
 		assertFalse(connection.isClosed());
 		assertFalse(connection.isReadOnly());
 	}
 }
