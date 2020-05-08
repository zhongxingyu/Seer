 package org.analogweb.core;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collection;
 
 import javax.servlet.FilterConfig;
 
 import org.analogweb.ApplicationProperties;
 import org.analogweb.InvocationMetadata;
 import org.analogweb.Modules;
 import org.analogweb.RequestPathMapping;
 import org.analogweb.RequestPathMetadata;
 import org.analogweb.core.WebApplication;
 import org.analogweb.exception.MissingRequiredParameterException;
 import org.analogweb.junit.NoDescribeMatcher;
 import org.analogweb.util.ApplicationPropertiesHolder;
 import org.analogweb.util.logging.Log;
 import org.analogweb.util.logging.Logs;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.rules.TemporaryFolder;
 
 /**
  * @author snowgoose
  */
 public class WebApplicationTest {
 
     private static final Log log = Logs.getLog(WebApplicationTest.class);
 
     @Rule
     public TemporaryFolder folder = new TemporaryFolder();
 
     private FilterConfig filterConfig;
     private WebApplication application;
     private ClassLoader classLoader;
 
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     @Before
     public void setUp() {
         filterConfig = mock(FilterConfig.class);
         classLoader = Thread.currentThread().getContextClassLoader();
         ApplicationPropertiesHolder.dispose(application);
     }
 
     @After
     public void tearDown() {
         ApplicationPropertiesHolder.dispose(application);
     }
 
     @Test
     public void testInitApplication() {
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_APPLICATION_SPECIFIER))
                 .thenReturn(".rn");
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_ROOT_COMPONENT_PACKAGES))
                 .thenReturn("jp.acme.test.actionsonly");
 
         File tempFolder = folder.newFolder("test");
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_APPLICATION_TEMPORARY_DIR))
                 .thenReturn(tempFolder.getPath());
 
         application = new WebApplication(filterConfig, classLoader);
 
         RequestPathMapping mapping = application.getRequestPathMapping();
         RequestPathMetadata pathAnyThing = mock(RequestPathMetadata.class);
         when(pathAnyThing.getActualPath()).thenReturn("/baa/anything");
         when(pathAnyThing.getRequestMethods()).thenReturn(Arrays.asList("POST"));
         InvocationMetadata metadataAnyThing = mapping.getActionMethodMetadata(pathAnyThing);
         log.debug(metadataAnyThing.toString());
 
         ApplicationProperties props = ApplicationPropertiesHolder.current();
 
        assertThat(props.geApplicationSpecifier(), is(".rn"));
         Collection<String> packageNames = props.getComponentPackageNames();
 
         assertThat(packageNames.size(), is(1));
         assertThat(packageNames.iterator().next(), is("jp.acme.test.actionsonly"));
 
         assertThat(props.getTempDir().getPath(), is(new File(tempFolder.getPath() + "/"
                 + WebApplication.class.getCanonicalName()).getPath()));
     }
 
     @Test
     public void testInitApplicationWithoutMetadata() {
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_APPLICATION_SPECIFIER))
                 .thenReturn(null);
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_ROOT_COMPONENT_PACKAGES))
                 .thenReturn("jp.acme.test.actionsonly");
 
         application = new WebApplication(filterConfig, classLoader);
 
         Modules modules = application.getModules();
         assertThat(application.getApplicationSpecifier(), is(""));
         log.debug(modules.getInvocationProcessors().toString());
         ApplicationProperties props = ApplicationPropertiesHolder.current();
 
        assertThat(props.geApplicationSpecifier(), is(""));
         Collection<String> packageNames = props.getComponentPackageNames();
 
         assertThat(packageNames.size(), is(1));
         assertThat(packageNames.iterator().next(), is("jp.acme.test.actionsonly"));
 
         System.out.println(props.getTempDir());
         assertThat(props.getTempDir().getPath(), is(new File(System.getProperty("java.io.tmpdir")
                 + "/" + WebApplication.class.getCanonicalName()).getPath()));
     }
 
     @Test
     public void testInitApplicationWithoutRootComponentPackages() {
 
         thrown.expect(new NoDescribeMatcher<MissingRequiredParameterException>() {
             @Override
             public boolean matches(Object arg0) {
                 if (arg0 instanceof MissingRequiredParameterException) {
                     MissingRequiredParameterException mrq = (MissingRequiredParameterException) arg0;
                     assertThat(mrq.getMissedParameterName(),
                             is(WebApplication.INIT_PARAMETER_ROOT_COMPONENT_PACKAGES));
                     return true;
                 }
                 return false;
             }
         });
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_APPLICATION_SPECIFIER))
                 .thenReturn(".do");
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_ROOT_COMPONENT_PACKAGES))
                 .thenReturn(null);
 
         new WebApplication(filterConfig, classLoader);
     }
 
     @Test
     public void testInitApplicationWithAdditionalComponents() {
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_APPLICATION_SPECIFIER))
                 .thenReturn(".rn");
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_ROOT_COMPONENT_PACKAGES))
                 .thenReturn("jp.acme.test.additionalcomponents");
 
         application = new WebApplication(filterConfig, classLoader);
 
         Modules modules = application.getModules();
         log.debug(modules.getInvocationProcessors().toString());
     }
 
     @Test
     public void testDispose() {
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_APPLICATION_SPECIFIER))
                 .thenReturn(".rn");
         when(filterConfig.getInitParameter(WebApplication.INIT_PARAMETER_ROOT_COMPONENT_PACKAGES))
                 .thenReturn("jp.acme.test.actionsonly");
 
         application = new WebApplication(filterConfig, classLoader);
         application.dispose();
 
         assertNull(application.getApplicationSpecifier());
         assertNull(application.getModules());
         assertNull(application.getRequestPathMapping());
     }
 
 }
