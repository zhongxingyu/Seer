 package de.twenty11.skysail.server.ext.osgimonitor.resources;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Version;
 import org.restlet.Request;
 import org.restlet.representation.Representation;
 
 import de.twenty11.skysail.server.ext.osgimonitor.OsgiMonitorViewerApplication;
 import de.twenty11.skysail.server.ext.osgimonitor.domain.BundleDescriptor;
 import de.twenty11.skysail.server.ext.osgimonitor.test.BaseTests;
 
 public class BundlesResourceTest extends BaseTests {
 
     private BundlesResource resource;
 
     @Before
     public void setUp() throws Exception {
         OsgiMonitorViewerApplication spy = setUpRestletApplication();
         BundleContext context = mock(BundleContext.class);
         Bundle bundle = mock(Bundle.class);
         when(bundle.getBundleId()).thenReturn(99l);
         when(bundle.getSymbolicName()).thenReturn("symbolic");
         when(bundle.getLastModified()).thenReturn(111l);
         when(bundle.getVersion()).thenReturn(new Version("1.2.3.qualifier"));
         when(bundle.getState()).thenReturn(32);
         Bundle[] bundles = new Bundle[1];
         bundles[0] = bundle;
         when(context.getBundles()).thenReturn(bundles);
         when(spy.getBundleContext()).thenReturn(context);
 
         resource = new BundlesResource();
         Request request = mock(Request.class);
         ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
         attributes.putIfAbsent("bundleId", "99");
         when(request.getAttributes()).thenReturn(attributes);
         resource.init(spy.getContext(), request, null);
     }
 
     @Test
    @Ignore
     public void returns_bundles_with_proper_values() throws Exception {
         List<BundleDescriptor> bundles = getBundles();
         assertThat(bundles.size(), is(equalTo(1)));
         assertThat(bundles.get(0).getVersion(), is(equalTo("1.2.3.qualifier")));
         assertThat(bundles.get(0).getState(), is(equalTo("Active")));
 
     }
 
     @Test
     @Ignore
     public void gives_error_message_for_post_when_location_doesnt_start_with_prefix() throws Exception {
         Representation answer = resource.install("wrongLocation");
         assertThat(answer.getText(), is(equalTo("location didn't start with 'prefix'")));
     }
 
 }
