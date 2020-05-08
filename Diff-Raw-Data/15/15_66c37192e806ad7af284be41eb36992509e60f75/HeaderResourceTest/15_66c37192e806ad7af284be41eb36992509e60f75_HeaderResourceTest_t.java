 package de.twenty11.skysail.server.ext.osgimonitor.resources;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Version;
 import org.restlet.Context;
 import org.restlet.Request;
 
 import de.twenty11.skysail.common.ext.osgimonitor.HeaderDescriptor;
 import de.twenty11.skysail.common.responses.SkysailResponse;
 import de.twenty11.skysail.server.ext.osgimonitor.OsgiMonitorViewerApplication;
 import de.twenty11.skysail.server.ext.osgimonitor.test.BaseTests;
 
 public class HeaderResourceTest extends BaseTests {
 
     private HeaderResource headerResource;
     private Bundle bundle;
 
     @Before
     public void setUp() throws Exception {
 
         OsgiMonitorViewerApplication spy = setUpRestletApplication();
         BundleContext bundleContext = mock(BundleContext.class);
         bundle = mock(Bundle.class);
         when(bundle.getBundleId()).thenReturn(99l);
         when(bundle.getSymbolicName()).thenReturn("symbolic");
         when(bundle.getLastModified()).thenReturn(111l);
         Dictionary<String, String> dictionary = new Hashtable<String, String>();
         dictionary.put("key1", "value1");
         dictionary.put("key2", "value2");
         when(bundle.getHeaders()).thenReturn(dictionary);
         when(bundleContext.getBundle(99l)).thenReturn(bundle);
         when(bundle.getVersion()).thenReturn(new Version("1.0.0"));
         when(spy.getBundleContext()).thenReturn(bundleContext);
 
         Context context = mock(Context.class);
         when(context.getAttributes()).thenReturn(new ConcurrentHashMap<String, Object>());
 
         headerResource = new HeaderResource();
         Request request = mock(Request.class);
         ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
         attributes.putIfAbsent("bundleId", "99");
         when(request.getAttributes()).thenReturn(attributes);
         headerResource.init(context, request, null);
     }
 
     @Test
    @Ignore
     public void getHeader_returns_headerDetails_for_bundleId_from_request() throws Exception {
         SkysailResponse<HeaderDescriptor> headers = headerResource.getHeader();
         HeaderDescriptor details = headers.getData();
         assertThat(details.getContent().size(), is(equalTo(2)));
     }
 
 }
