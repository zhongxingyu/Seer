 package org.analogweb.util;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.analogweb.Application;
 import org.analogweb.ApplicationProperties;
 import org.analogweb.exception.ApplicationConfigurationException;
 import org.analogweb.exception.MissingRequiredParameterException;
 import org.analogweb.util.ApplicationPropertiesHolder.Creator;
 import org.analogweb.util.ApplicationPropertiesHolder.DefaultCreator;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.rules.TemporaryFolder;
 
 public class ApplicationPropertiesHolderTest {
 
     private Application app;
 
     @Rule
     public ExpectedException thrown = ExpectedException.none();
     @Rule
     public TemporaryFolder folder = new TemporaryFolder();
 
     @Before
     public void setUp() {
         app = mock(Application.class);
     }
 
     @After
     public void tearDown() {
         ApplicationPropertiesHolder.dispose(app);
     }
 
     @Test
     public void testConfigure() {
         Creator config = mock(Creator.class);
         ApplicationProperties expected = mock(ApplicationProperties.class);
         when(config.create()).thenReturn(expected);
         ApplicationProperties actual = ApplicationPropertiesHolder.configure(app, config);
         assertThat(actual, is(expected));
         assertThat(ApplicationPropertiesHolder.current(), is(expected));
     }
 
     @Test
     public void testNoConfigure() {
         thrown.expect(ApplicationConfigurationException.class);
         ApplicationPropertiesHolder.current();
     }
 
     @Test
     public void testReConfigure() {
         thrown.expect(ApplicationConfigurationException.class);
         Creator config = mock(Creator.class);
         ApplicationProperties expected = mock(ApplicationProperties.class);
         when(config.create()).thenReturn(expected);
         ApplicationProperties actual = ApplicationPropertiesHolder.configure(app, config);
         assertThat(actual, is(expected));
         ApplicationPropertiesHolder.configure(app, config);
     }
 
     @Test
     public void testDisposeAndGet() {
         thrown.expect(ApplicationConfigurationException.class);
         Creator config = mock(Creator.class);
         ApplicationProperties expected = mock(ApplicationProperties.class);
         when(config.create()).thenReturn(expected);
         ApplicationProperties actual = ApplicationPropertiesHolder.configure(app, config);
         assertThat(actual, is(expected));
         ApplicationPropertiesHolder.dispose(app);
         ApplicationPropertiesHolder.current();
     }
 
     @Test
     public void testUsingDefaultCreator() throws IOException {
         DefaultCreator creator = new DefaultCreator();
         ApplicationProperties actual = ApplicationPropertiesHolder.configure(app, creator);
         assertThat(actual.getApplicationSpecifier(), is(StringUtils.EMPTY));
         Collection<String> actualPackageNames = actual.getComponentPackageNames();
         assertThat(actualPackageNames.size(), is(1));
         assertThat(actualPackageNames.contains(Application.class.getPackage().getName()), is(true));
        assertThat(actual.getTempDir().getPath(), is(System.getProperty("java.io.tmpdir") + "/"
                 + Application.class.getCanonicalName()));
     }
 
     @Test
     public void testUsingConfiguredDefaultCreator() throws IOException {
         File dir = folder.newFolder();
         String packageNames = "foo.baa,baz.boo";
         String applicationSpecifier = ".do";
         String tempDirectoryPath = dir.getPath();
         DefaultCreator creator = new DefaultCreator(packageNames, applicationSpecifier,
                 tempDirectoryPath);
         ApplicationProperties actual = ApplicationPropertiesHolder.configure(app, creator);
         assertThat(actual.getApplicationSpecifier(), is(applicationSpecifier));
         Collection<String> actualPackageNames = actual.getComponentPackageNames();
         assertThat(actualPackageNames.size(), is(2));
         assertThat(actualPackageNames.containsAll(Arrays.asList("foo.baa", "baz.boo")), is(true));
         assertThat(actual.getTempDir().getPath(),
                is(dir.getPath() + "/" + Application.class.getCanonicalName()));
     }
 
     @Test
     public void testUsingConfiguredDefaultCreatorWithEmptyPackageNames() throws IOException {
         thrown.expect(MissingRequiredParameterException.class);
         File dir = folder.newFolder();
         String packageNames = "";
         String applicationSpecifier = ".do";
         String tempDirectoryPath = dir.getPath();
         DefaultCreator creator = new DefaultCreator(packageNames, applicationSpecifier,
                 tempDirectoryPath);
         ApplicationPropertiesHolder.configure(app, creator);
     }
 
 }
