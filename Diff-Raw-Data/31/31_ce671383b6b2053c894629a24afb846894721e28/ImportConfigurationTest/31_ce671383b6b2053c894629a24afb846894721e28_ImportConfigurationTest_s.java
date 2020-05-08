 package org.kalibro;
 
 import java.io.File;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.kalibro.client.ClientDaoFactory;
 import org.kalibro.core.concurrent.VoidTask;
import org.kalibro.dao.ConfigurationDao;
 import org.kalibro.tests.UtilityClassTest;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({Configuration.class, ImportConfiguration.class})
 public class ImportConfigurationTest extends UtilityClassTest {
 
 	private static final String FILE_PATH = "ImportConfigurationTest file path";
	private static final String SERVICE_ADDRESS = "ImportConfigurationTest service address";
 
 	@Test
	public void shouldThrowExceptionForInvalidNumberOfArguments() {
		shouldThrowExceptionFor();
		shouldThrowExceptionFor(".");
	}

	private void shouldThrowExceptionFor(final String... arguments) {
 		assertThat(new VoidTask() {
 
 			@Override
 			protected void perform() throws Throwable {
				ImportConfiguration.main(arguments);
 			}
		}).doThrow(IllegalArgumentException.class).withMessage("Expected 2 arguments: file path and service address.");
 	}
 
 	@Test
 	public void shouldSaveConfigurationOnService() throws Exception {
 		File file = mock(File.class);
 		Configuration configuration = mock(Configuration.class);
		ClientDaoFactory daoFactory = mock(ClientDaoFactory.class);
		ConfigurationDao dao = mock(ConfigurationDao.class);
 
 		mockStatic(Configuration.class);
 		whenNew(File.class).withArguments(FILE_PATH).thenReturn(file);
		whenNew(ClientDaoFactory.class).withArguments(SERVICE_ADDRESS).thenReturn(daoFactory);
 		when(Configuration.importFrom(file)).thenReturn(configuration);
		when(daoFactory.createConfigurationDao()).thenReturn(dao);
 
		ImportConfiguration.main(array(FILE_PATH, SERVICE_ADDRESS));
		verify(dao).save(configuration);
 	}
 }
