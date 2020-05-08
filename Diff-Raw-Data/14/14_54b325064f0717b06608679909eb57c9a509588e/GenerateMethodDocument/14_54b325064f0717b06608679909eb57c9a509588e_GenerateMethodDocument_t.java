 package com.firetruckbowl.tgirest.processor;
 
 import com.firetruckbowl.tgirest.TGIRestWatcher;
 import com.firetruckbowl.tgirest.annotation.MethodError;
 import com.firetruckbowl.tgirest.annotation.ParamNote;
 import com.firetruckbowl.tgirest.annotation.ResourceMethod;
 import com.firetruckbowl.tgirest.model.MethodDocument;
 import com.firetruckbowl.tgirest.model.ParamDocument;
 import com.firetruckbowl.tgirest.resource.Documented;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TestRule;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.mockito.Mock;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.hasItems;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 /**
  * @author <a href="mailto:lochnguyen@gmail.com">Loc Nguyen</a>
  */
 @RunWith(JUnit4.class)
 public class GenerateMethodDocument {
   private final String BASE_URI = "http://winterfel.com/api/";
 
   @Rule
   public TestRule watcher = new TGIRestWatcher();
 
   @Mock
   private UriInfo uriInfo;
 
   private Documenter systemUnderTest;
 
   @Before
   public void setUp() throws Exception {
     initMocks(this);
     URI baseUri = new URI(BASE_URI);
     given(uriInfo.getBaseUri()).willReturn(baseUri);
 
     systemUnderTest = new TGIRestDocumenter();
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetPath() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document =  systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
    assertThat(document.getPath(), is(BASE_URI + "foo/bar/{id}"));
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetHttpMethod() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document = systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
     assertThat(document.getHttpMethod(), is("GET"));
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetSuccessStatus() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document = systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
     assertThat(document.getStatus(), is(Response.Status.OK.getStatusCode()));
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetDescription() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document = systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
     assertThat(document.getDescription(), is("bar method"));
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetErrors() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document = systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
     assertThat(document.getResponseErrors().size(), is(1));
     assertThat(document.getResponseErrors().get(0).getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetMediaProduced() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document = systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
     List<String> types = Arrays.asList(document.getMediaTypesProduced());
     assertThat(types, hasItems(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetMediaConsumed() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document = systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
     List<String> types = Arrays.asList(document.getMediaTypesConsumed());
     assertThat(types, hasItems(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetQueryParamDocs() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document = systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
     List<ParamDocument> queryParams = Arrays.asList(document.getQueryParams());
 
     ParamDocument qpt = new ParamDocument("t", "The time");
     ParamDocument qpl = new ParamDocument("l", "The location");
 
     assertThat(queryParams, hasItems(qpt, qpl));
   }
 
   @Test
   @SuppressWarnings("")
   public void shouldGetPathParamDocs() throws Exception {
     // given
     FooResource resource = new FooResource();
     Method m = resource.getClass().getMethod("getBar", String.class, String.class, String.class);
 
     // when
     MethodDocument document = systemUnderTest.generateMethodDocument(uriInfo, m);
 
     // then
     List<ParamDocument> pathParams = Arrays.asList(document.getPathParams());
     assertThat(pathParams, hasItems(new ParamDocument("id", "The ID of the bar")));
   }
 
   /**
    * A class for use with testing only since we don't want to rely on the
    * library's actual resource classes staying the same.
    */
   @Path("foo")
   class FooResource implements Documented {
 
     @GET
     @Path("bar/{id}")
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @ResourceMethod(
       status = Response.Status.OK,
       description = "bar method",
       errors = {
         @MethodError(status = Response.Status.NOT_FOUND, cause = "The bar could not be found")
       }
     )
     public Response getBar(@PathParam("id")
                            @ParamNote("The ID of the bar") String id,
                            @QueryParam("t")
                            @ParamNote("The time") String time,
                            @QueryParam("l")
                            @ParamNote("The location") String location) {
       return Response.status(Response.Status.OK).build();
     }
 
     @Override
     public Response getDocumentation(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) {
       /* no-op */
       return null;
     }
   }
 }
