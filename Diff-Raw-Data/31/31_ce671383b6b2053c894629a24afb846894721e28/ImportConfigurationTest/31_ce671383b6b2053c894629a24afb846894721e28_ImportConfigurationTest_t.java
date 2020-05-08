 package org.kalibro;
 
 import java.io.File;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.core.concurrent.VoidTask;
 import org.kalibro.tests.UtilityClassTest;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({Configuration.class, ImportConfiguration.class})
 public class ImportConfigurationTest extends UtilityClassTest {
 
 	private static final String FILE_PATH = "ImportConfigurationTest file path";
 
 	@Test
	public void shouldThrowExceptionForNoArguments() {
 		assertThat(new VoidTask() {
 
 			@Override
 			protected void perform() throws Throwable {
				ImportConfiguration.main(new String[0]);
 			}
		}).doThrow(IllegalArgumentException.class).withMessage("Expected configuration file path as argument.");
 	}
 
 	@Test
 	public void shouldSaveConfigurationOnService() throws Exception {
 		File file = mock(File.class);
 		Configuration configuration = mock(Configuration.class);
 
 		mockStatic(Configuration.class);
 		whenNew(File.class).withArguments(FILE_PATH).thenReturn(file);
 		when(Configuration.importFrom(file)).thenReturn(configuration);
 
		ImportConfiguration.main(array(FILE_PATH));
		verify(configuration).save();
 	}
 }
