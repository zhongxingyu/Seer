 package testenough;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import testenough.weaver.BCWeaver;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.instrument.Instrumentation;
 import java.util.jar.JarFile;
 
 import static junit.framework.Assert.fail;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 
 public class InstrumentingAgentTest {
 
 
     private File config;
     private Instrumentation instrumentation;
 
     @Before
     public void setUp() throws Exception {
         config = new File("config.properties");
         FileUtils.write(config, configFileContents());
         FileUtils.forceDeleteOnExit(config);
         instrumentation = mock(Instrumentation.class);
     }
 
     @After
     public void tearDown() throws Exception {
         verifyNoMoreInteractions(instrumentation);
         FileUtils.forceDelete(config);
     }
 
     @Test
     public void shouldAddANewClassTransformer() throws IOException {
         InstrumentingAgent.premain(String.format("%s:resource/config.properties=%s:.", InstrumentingAgent.CONFIG_FILE_PATH, InstrumentingAgent.LIB_DIR), instrumentation);
 
         Configuration configuration = new Configuration(configFileContents());
         verify(instrumentation).addTransformer(new BCWeaver(configuration));
         verify(instrumentation, atLeastOnce()).appendToBootstrapClassLoaderSearch(any(JarFile.class));
     }
 
     private String configFileContents() {
         return String.format("%s=tlb.foo", Configuration.POPULATE_INCLUDE_PACKAGES);
     }
 
     @Test
     public void shouldDefaultToConfigFileIfNotSpecified() throws IOException {
         InstrumentingAgent.premain(String.format("%s:.", InstrumentingAgent.LIB_DIR), instrumentation);
 
         Configuration configuration = new Configuration(configFileContents());
         verify(instrumentation).addTransformer(new BCWeaver(configuration));
         verify(instrumentation, atLeastOnce()).appendToBootstrapClassLoaderSearch(any(JarFile.class));
     }
 
     @Test
     public void shouldUseAnEmptyConfigurationWhenTheConfigFileIsNotFound() throws IOException {
         InstrumentingAgent.premain(String.format("%s:foo_bar=%s:.", InstrumentingAgent.CONFIG_FILE_PATH, InstrumentingAgent.LIB_DIR), instrumentation);
 
         Configuration configuration = new Configuration("");
         verify(instrumentation).addTransformer(new BCWeaver(configuration));
         verify(instrumentation, atLeastOnce()).appendToBootstrapClassLoaderSearch(any(JarFile.class));
     }
 
     @Test
     public void testShouldAddTheJarsInLibDirToBootstrapperClasspath() throws Exception {
         InstrumentingAgent.premain(String.format("%s:test_folder", InstrumentingAgent.LIB_DIR), instrumentation);
         Configuration configuration = new Configuration(configFileContents());
         verify(instrumentation).addTransformer(new BCWeaver(configuration));
         verify(instrumentation, atLeastOnce()).appendToBootstrapClassLoaderSearch(any(JarFile.class));
     }
 
     @Test
     public void shouldDefaultTheLibFolder() throws IOException {
         InstrumentingAgent.premain("", instrumentation);
         Configuration configuration = new Configuration(configFileContents());
         verify(instrumentation).addTransformer(new BCWeaver(configuration));
         verify(instrumentation, atLeastOnce()).appendToBootstrapClassLoaderSearch(any(JarFile.class));
     }
 
     @Test
     public void shouldBombIfTheLibFolderIsNotFound() throws IOException {
         try {
             InstrumentingAgent.premain(String.format("%s:not_found", InstrumentingAgent.LIB_DIR), instrumentation);
             fail("Should have failed since the lib directory is not found");
         } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(String.format("Lib directory '%s' not found. Please make sure you provide the right path to the library directory.", new File("not_found").getPath())));
         }
     }
 
     @Test
     public void shouldBombIfTheLibDirectoryIsNotADirectory() throws IOException {
         try {
             InstrumentingAgent.premain(String.format("%s:resource/config.properties", InstrumentingAgent.LIB_DIR), instrumentation);
             fail("Should have failed since the lib directory is not a directory");
         } catch (IllegalArgumentException e) {
             assertThat(e.getMessage(), is(String.format("Lib directory '%s' is not a directory. Please make sure you provide the right path to the library directory.", new File("resource/config.properties").getPath())));
         }
     }
 
     @Test
     public void shouldBehaveFineWhenThereAreNoArgumentsPassed() throws IOException {
         InstrumentingAgent.premain(null, instrumentation);
         Configuration configuration = new Configuration(configFileContents());
         verify(instrumentation).addTransformer(new BCWeaver(configuration));
         verify(instrumentation, atLeastOnce()).appendToBootstrapClassLoaderSearch(any(JarFile.class));
     }
 
     @Test
     public void shouldGiveNiceErrorMessageWhenCannotParse() throws IOException {
         try {
             InstrumentingAgent.premain("FooBarBaaz", instrumentation);
         } catch (IllegalArgumentException e) {
             assertThat(e.getMessage(), is("Arguments are name value pairs. Name and value are separated using ':'. Multiple arguments are separated using '='"));
         }
     }
 }
