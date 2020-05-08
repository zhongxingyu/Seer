 package ar.edu.utn.tacs.group5.controller;
 
 import static ar.edu.utn.tacs.group5.controller.GetFeedController.FEED_TEMPLATE;
 import static org.junit.Assert.assertEquals;
 
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.junit.Test;
 
 import ar.edu.utn.tacs.group5.model.Feed;
 import ar.edu.utn.tacs.group5.service.FeedService;
 
 import com.github.mustachejava.DefaultMustacheFactory;
 import com.github.mustachejava.Mustache;
 import com.github.mustachejava.MustacheFactory;
 import com.google.api.server.spi.config.ApiMethod.HttpMethod;
 import com.google.appengine.api.datastore.KeyFactory;
 
 public class GetFeedControllerTest extends AbstractAuthorizedControllerTest<GetFeedController> {
 
     private FeedService feedService = new FeedService();
     private MustacheFactory mustacheFactory = new DefaultMustacheFactory();
 
     @Test
     public void runOk() throws Exception {
         doLogin();
         Feed feed = new Feed();
         feed.setTitle("My Feed");
         feed.setDescription("My Feed description");
         feedService.insert(feed);
        tester.request.addParameter(Constants.FEED, KeyFactory.keyToString(feed.getKey()));
         tester.start(resource());
         assertController(HttpStatus.SC_OK);
         Mustache feedTemplate = mustacheFactory.compile(FEED_TEMPLATE);
         Writer writer = new StringWriter();
         assertEquals(feedTemplate.execute(writer, feed).toString(), tester.response.getOutputAsString());
     }
 
     @Test
     public void testRunIsNotAllowed() throws Exception {
         doLogin();
         tester.request.setMethod(HttpMethod.POST);
         tester.start(resource());
         assertController(HttpStatus.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     protected String resource() {
         return "/GetFeed";
     }
 
 }
