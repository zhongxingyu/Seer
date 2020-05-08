 package org.kalibro.desktop.configuration;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.JDesktopPane;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.Configuration;
 import org.kalibro.dao.ConfigurationDao;
 import org.kalibro.dao.DaoFactory;
 import org.kalibro.tests.UnitTest;
 import org.mockito.Mockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.powermock.reflect.Whitebox;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({ConfigurationController.class, DaoFactory.class})
 public class ConfigurationControllerTest extends UnitTest {
 
 	private static final String NAME = "ConfigurationControllerTest name";
 	private static final List<String> NAMES = Arrays.asList(NAME);
 
 	private JDesktopPane desktopPane;
 	private Configuration configuration;
 	private ConfigurationDao configurationDao;
 
 	private ConfigurationController controller;
 
 	@Before
 	public void setUp() {
 		desktopPane = mock(JDesktopPane.class);
 		configuration = mock(Configuration.class);
 		mockConfigurationDao();
 		controller = new ConfigurationController(desktopPane);
 	}
 
 	private void mockConfigurationDao() {
 		configurationDao = mock(ConfigurationDao.class);
 		mockStatic(DaoFactory.class);
 		when(DaoFactory.getConfigurationDao()).thenReturn(configurationDao);
 	}
 
 	@Test
 	public void checkDesktopPane() {
 		assertSame(desktopPane, Whitebox.getInternalState(controller, JDesktopPane.class));
 	}
 
 	@Test
 	public void checkEntityName() {
 		assertEquals("Configuration", Whitebox.getInternalState(controller, String.class));
 	}
 
 	@Test
 	public void shouldCreateConfiguration() {
 		configuration = controller.createEntity(NAME);
 		assertEquals(NAME, configuration.getName());
 	}
 
 	@Test
 	public void shouldGetConfigurationNamesFromDao() {
 		when(configurationDao.getConfigurationNames()).thenReturn(NAMES);
 		assertSame(NAMES, controller.getEntityNames());
 	}
 
 	@Test
 	public void shouldGetConfigurationFromDao() {
 		when(configurationDao.getConfiguration(NAME)).thenReturn(configuration);
 		assertSame(configuration, controller.getEntity(NAME));
 	}
 
 	@Test
 	public void shouldCreateFrame() throws Exception {
 		ConfigurationFrame configurationFrame = mock(ConfigurationFrame.class);
 		whenNew(ConfigurationFrame.class).withArguments(configuration).thenReturn(configurationFrame);
 		assertSame(configurationFrame, controller.createFrameFor(configuration));
 	}
 
 	@Test
 	public void shouldRemoveConfiguration() {
 		controller.removeEntity(NAME);
//		TODO Mockito.verify(configurationDao).removeConfiguration(NAME);
 	}
 
 	@Test
 	public void shouldSaveConfiguration() {
 		controller.save(configuration);
 		Mockito.verify(configurationDao).save(configuration);
 	}
 
 	@Test
 	public void shouldSetConfigurationName() {
 		controller.setEntityName(configuration, NAME);
 		Mockito.verify(configuration).setName(NAME);
 	}
 }
