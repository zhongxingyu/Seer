 package earth.xor.rest.routes;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.xorrr.util.EnvironmentVars;
 import org.xorrr.util.LinkObjects;
 
 import spark.AbstractRoute;
 import spark.Request;
 import spark.Response;
 import earth.xor.db.DatastoreFacade;
 import earth.xor.db.Link;
 import earth.xor.rest.transformation.Transformator;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({ AbstractRoute.class })
 public class TestPostLinkRoute {
     @Mock
     Request req;
     @Mock
     Response resp;
     @Mock
     DatastoreFacade facade;
     @Mock
     Transformator transformator;
     @Mock
     Link testLink;
 
     private PostLinkRoute route;
     private String jsonExample = "{ \"url\":\"http://www.foo.org\", "
             + "\"title\":\"foo\", " + "\"user\":\"user\"}";
 
     @Before
     public void setUp() {
         PowerMockito.mockStatic(AbstractRoute.class);
        route = new PostLinkRoute(Routes.LINKS_ROUTE, facade, transformator);
 
         testLink = LinkObjects.testLink1;
     }
 
     @Test
     public void linkCanBePosted() {
         when(req.queryParams("pw")).thenReturn(System.getenv(EnvironmentVars.PW));
         when(req.body()).thenReturn(jsonExample);
         when(transformator.toLink(jsonExample)).thenReturn(testLink);
 
         Object returned = route.handle(req, resp);
 
         verify(req, times(1)).queryParams("pw");
         verify(facade, times(1)).addLink(testLink);
         verify(req, times(2)).body();
         assertEquals(jsonExample, returned.toString());
     }
 
     @Test
     public void dontAuthWithWrongPassword() throws Exception {
         when(req.queryParams("pw")).thenReturn("wrong");
 
         route.handle(req, resp);
 
         PowerMockito.verifyPrivate(AbstractRoute.class).invoke(401,
                 "Authentication failed");
     }
 }
