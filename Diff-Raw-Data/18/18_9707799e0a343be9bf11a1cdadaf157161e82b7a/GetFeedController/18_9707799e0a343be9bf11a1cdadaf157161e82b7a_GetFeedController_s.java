 package ar.edu.utn.tacs.group5.controller;
 
 import java.io.PrintWriter;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 
 import ar.edu.utn.tacs.group5.controller.Constants;
 import ar.edu.utn.tacs.group5.model.Feed;
 import ar.edu.utn.tacs.group5.service.FeedService;
 
 import com.github.mustachejava.DefaultMustacheFactory;
 import com.github.mustachejava.Mustache;
 import com.github.mustachejava.MustacheFactory;
 
 public class GetFeedController extends Controller {
 
     private MustacheFactory mustacheFactory = new DefaultMustacheFactory();
 	private FeedService feedService = new FeedService();
 
 	@Override
     public Navigation run() throws Exception {
    	String userId = sessionScope(Constants.USER_ID);
         Mustache mustache = mustacheFactory.compile("feed.mustache");
        Feed feed = feedService.getByUserId(Long.valueOf(userId));
 		mustache.execute(new PrintWriter(response.getWriter()), feed).flush();
     	return null;
     }
 }
