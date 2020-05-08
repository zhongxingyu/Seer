 package com.firetruckbowl.tgirest.processor;
 
 import com.firetruckbowl.tgirest.TGIRestWatcher;
 import com.firetruckbowl.tgirest.annotation.MethodError;
 import com.firetruckbowl.tgirest.annotation.ParamNote;
 import com.firetruckbowl.tgirest.annotation.ResourceDoc;
 import com.firetruckbowl.tgirest.annotation.ResourceMethod;
 import com.firetruckbowl.tgirest.model.MethodDocument;
 import com.firetruckbowl.tgirest.model.ResourceDocument;
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
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 /**
  * @author <a href="mailto:lochnguyen@gmail.com">Loc Nguyen</a>
  */
 @RunWith(JUnit4.class)
 public class GenerateResourceDocument {
   private final String BASE_URI = "http://casterlyrock.com/api/";
 
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
 
   @Test(expected = RuntimeException.class)
   @SuppressWarnings("")
   public void shouldCheckResourceDocAnnotation() {
     // given
     Documented resource = new WidgetResource();
 
     // when
     systemUnderTest.generateResourceDocument(uriInfo, resource);
 
     // then
     /* should throw RuntimeException */
   }
 
   @Test
   public void shouldGetResourcePath() {
     // given
     Documented resource = new FooResource();
 
     // when
     ResourceDocument rd = systemUnderTest.generateResourceDocument(uriInfo, resource);
 
     // then
     assertThat(rd.getPath(), equalTo(BASE_URI + "foo"));
   }
 
   @Test
   public void shouldGetDescription() {
     // given
     Documented resource = new FooResource();
 
     // when
     ResourceDocument rd = systemUnderTest.generateResourceDocument(uriInfo, resource);
 
     // then
     assertThat(rd.getDescription(), equalTo("A test resource"));
   }
 
   @Test
  public void shouldGenerateMethodDocumentsForAnnotatedMethods() {
     // given
     Documented resource = new FooResource();
 
     // when
     ResourceDocument rd = systemUnderTest.generateResourceDocument(uriInfo, resource);
 
     // then
    assertThat(rd.getMethodDocuments().size(), equalTo(1));
   }
 
   /**
    * A class for use with testing only since we don't want to rely on the
    * library's actual resource classes staying the same.
    */
   @Path("foo")
   @ResourceDoc(description = "A test resource")
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
 
   @Path("widget")
   class WidgetResource implements Documented {
     @Override
     public Response getDocumentation(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) {
       /* no-op */
       return null;
     }
   }
 }
