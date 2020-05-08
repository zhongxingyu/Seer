 package fr.fcamblor.demos.sbjd.test.rules;
 
 import com.jayway.restassured.RestAssured;
 import com.jayway.restassured.config.RestAssuredConfig;
 import com.jayway.restassured.filter.Filter;
 import com.jayway.restassured.filter.FilterContext;
 import com.jayway.restassured.response.Response;
 import com.jayway.restassured.specification.FilterableRequestSpecification;
 import com.jayway.restassured.specification.FilterableResponseSpecification;
 import org.junit.rules.ExternalResource;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.List;
 
 import static com.jayway.restassured.config.DecoderConfig.decoderConfig;
 import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
 import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
 
 /**
  * @author fcamblor
  */
 public class RequiresDefaultRestAssuredConfiguration extends ExternalResource {
 
     RestAssuredConfig oldConfig = null;
     String currentSessionId = null;
 
     public class KeepingSessionIdAmongRequestsFilter implements Filter {
         public Response filter(FilterableRequestSpecification filterableRequestSpecification,
                                FilterableResponseSpecification filterableResponseSpecification,
                                FilterContext filterContext) {
 
             // Ensuring every tests will work in the same http session
             if(/* can't do this for the moment ... filterableRequestSpecification.sessionId() == null && */ currentSessionId != null){
                 filterableRequestSpecification.sessionId(currentSessionId);
             }
 
             Response res = filterContext.next(filterableRequestSpecification, filterableResponseSpecification);
 
             if(currentSessionId == null){
                 currentSessionId = res.sessionId();
             }
 
             return res;
         }
     }
     List<Filter> additionnalFilters = Arrays.<Filter>asList(new KeepingSessionIdAmongRequestsFilter());
 
     @Override
     protected void before() throws Throwable {
         super.before();
 
         oldConfig = RestAssured.config;
         RestAssured.config = newConfig().
                 encoderConfig(encoderConfig().defaultContentCharset("UTF-8")).
                 decoderConfig(decoderConfig().defaultContentCharset("UTF-8"));
 
         // No session id for the moment (see KeepingSessionIdAmongRequestsFilter)
         currentSessionId = null;
 
         RestAssured.filters(additionnalFilters);
     }
 
     @Override
     protected void after() {
         // That's ugly but heh.. can't do better for the moment
         // see https://groups.google.com/forum/?fromgroups#!topic/rest-assured/KlR3UFvc_Qk
         try {
             Field filtersField = RestAssured.class.getDeclaredField("filters");
             filtersField.setAccessible(true);
             List<Filter> filters = (List<Filter>) filtersField.get(null);
             filters.removeAll(additionnalFilters);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         // Resetting session id after each test
         currentSessionId = null;
 
         RestAssured.config = oldConfig;
         oldConfig = null;
 
         super.after();
     }
 }
