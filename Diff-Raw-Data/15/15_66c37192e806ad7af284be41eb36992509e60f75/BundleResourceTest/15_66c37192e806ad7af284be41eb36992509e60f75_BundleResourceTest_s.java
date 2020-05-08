 package de.twenty11.skysail.server.ext.osgimonitor.resources;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.codehaus.jackson.type.TypeReference;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Version;
 import org.restlet.Context;
 import org.restlet.Request;
 import org.restlet.representation.Representation;
 
 import de.twenty11.skysail.common.responses.SkysailResponse;
 import de.twenty11.skysail.server.ext.osgimonitor.OsgiMonitorViewerApplication;
 import de.twenty11.skysail.server.ext.osgimonitor.domain.BundleDescriptor;
 import de.twenty11.skysail.server.ext.osgimonitor.domain.BundleDetails;
 import de.twenty11.skysail.server.ext.osgimonitor.test.BaseTests;
 
 public class BundleResourceTest extends BaseTests {
 
     private BundleResource bundleResource;
     private Bundle bundle;
 
     @Before
     public void setUp() throws Exception {
 
         OsgiMonitorViewerApplication spy = setUpRestletApplication();
         BundleContext bundleContext = mock(BundleContext.class);
         bundle = mock(Bundle.class);
         when(bundle.getBundleId()).thenReturn(99l);
         when(bundle.getSymbolicName()).thenReturn("symbolic");
         when(bundle.getLastModified()).thenReturn(111l);
         when(bundleContext.getBundle(99l)).thenReturn(bundle);
         when(bundle.getVersion()).thenReturn(new Version("1.0.0"));
         when(spy.getBundleContext()).thenReturn(bundleContext);
 
         Context context = mock(Context.class);
         when(context.getAttributes()).thenReturn(new ConcurrentHashMap<String, Object>());
 
         bundleResource = new BundleResource();
         Request request = mock(Request.class);
         ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
         attributes.putIfAbsent("bundleId", "99");
         when(request.getAttributes()).thenReturn(attributes);
         bundleResource.init(context, request, null);
     }
 
     @Test
     public void getBundle_returns_bundleDetails_for_bundleId_from_request() throws Exception {
         SkysailResponse<BundleDetails> bundle = bundleResource.getBundle();
         BundleDescriptor details = bundle.getData();
         assertThat(details.getBundleId(), is(equalTo(99l)));
     }
 
     @Test
     @Ignore
     // TODO
     public void canIssue_GET_request() throws Exception {
         org.restlet.Response response = get("bundles/details/99");
         assertDefaults(response);
         Representation entity = response.getEntity();
         System.out.println(entity.getText());
         SkysailResponse<BundleDetails> skysailResponse = mapper.readValue(entity.getText(),
                 new TypeReference<SkysailResponse<BundleDetails>>() {
                 });
         // assertThat(skysailResponse.getMessage(), skysailResponse.getSuccess(), is(true));
 
     }
 
     @Test
     @Ignore
     public void canIssue_PUT_request_to_start_bundle() throws Exception {
         org.restlet.Response response = put("bundles/details/99/start");
         assertDefaults(response);
         Representation entity = response.getEntity();
         assertThat(entity.getText(), is(equalTo("Success!")));
         verify(bundle).start();
     }
 
     @Test
     @Ignore
     public void canIssue_PUT_request_to_stop_bundle() throws Exception {
         org.restlet.Response response = put("bundles/details/99/stop");
         assertDefaults(response);
         Representation entity = response.getEntity();
         assertThat(entity.getText(), is(equalTo("Success!")));
         verify(bundle).stop();
     }
 
     @Test
     @Ignore
     public void canIssue_PUT_request_to_update_bundle() throws Exception {
         org.restlet.Response response = put("bundles/details/99/update");
         assertDefaults(response);
         Representation entity = response.getEntity();
         assertThat(entity.getText(), is(equalTo("Success!")));
         verify(bundle).update();
     }
 
 }
