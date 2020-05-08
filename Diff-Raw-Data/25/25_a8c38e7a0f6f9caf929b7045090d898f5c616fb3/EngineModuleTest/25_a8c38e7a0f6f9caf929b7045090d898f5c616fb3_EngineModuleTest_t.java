 package uk.org.sappho.code.change.management.engine;
 
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.google.inject.Injector;
 
 import uk.org.sappho.code.change.management.data.persistence.ConfigurationRawDataPersistence;
 import uk.org.sappho.code.change.management.data.persistence.FilenameRawDataPersistence;
 import uk.org.sappho.code.change.management.data.persistence.ReaderRawDataPersistence;
 import uk.org.sappho.configuration.Configuration;
 import uk.org.sappho.configuration.ConfigurationException;
 
 @RunWith(MockitoJUnitRunner.class)
 public class EngineModuleTest {
 
     @Mock
     private Configuration mockConfiguration;
 
     @Before
     public void setupFakeWiringConfiguration() throws ConfigurationException {
         returnFakePluginFor("scm.plugin", "uk.org.sappho.code.change.management.scm");
         returnFakePluginFor("issues.plugin", "uk.org.sappho.code.change.management.issues");
        returnFakePluginFor("raw.data.processing.plugin", "uk.org.sappho.code.heatmap.engine");
     }
 
     @Test
     public void shouldWireExplicitly() {
         new EngineModule().init(mockConfiguration);
     }
 
     @Test
     // TODO: better to use annotations to distinguish specialisations,
     // if we really need specialisations like this - probably not
    public void shouldWireRawDataPersistenceImplementations() throws ConfigurationException {
        String filename = mockConfiguration.getProperty("raw.data.store.filename");
        when(filename).thenReturn("test.xml");
         EngineModule engineModule = new EngineModule();
         engineModule.init(mockConfiguration);
         Injector injector = engineModule.getInjector();
         injector.getInstance(ReaderRawDataPersistence.class);
         injector.getInstance(FilenameRawDataPersistence.class);
         injector.getInstance(ConfigurationRawDataPersistence.class);
     }
 
     private void returnFakePluginFor(String name, String defaultPackage) throws ConfigurationException {
         Class<FakePlugin> getSCMPluginClass = mockConfiguration.getPlugin(name,
                 defaultPackage);
         when(getSCMPluginClass).thenReturn(FakePlugin.class);
     }
 }
