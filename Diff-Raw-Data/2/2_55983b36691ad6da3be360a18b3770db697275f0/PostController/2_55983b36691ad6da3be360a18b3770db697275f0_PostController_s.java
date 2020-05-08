  package sage.web;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import sage.domain.service.BlogPostService;
 import sage.domain.service.TweetPostService;
 import sage.entity.Blog;
 import sage.entity.Tweet;
 import sage.web.auth.AuthUtil;
 
 @Controller
 @RequestMapping(value="/post", method=RequestMethod.POST)
 public class PostController {
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	@Autowired
 	private TweetPostService tweetPostService;
 	@Autowired
 	private BlogPostService blogService;
 	
 	@RequestMapping("/tweet")
 	@ResponseBody
 	public boolean tweet(
 			@RequestParam("content") String content, 
 			@RequestParam(value="tagIds[]", required=false) Collection<Long> tagIds) {
 		Long uid = AuthUtil.currentUid();
 		if (uid == null) {return false;}
 		if (content.isEmpty()) {return false;}
 		if (content.length() > 2000) {return false;}
 		if (tagIds == null) {tagIds = new ArrayList<>(0);}
 		
 		Tweet tweet = tweetPostService.newTweet(uid, content, tagIds);
 		logger.info("post tweet {} success", tweet.getId());
 		return true;
 	}
 	
 	@RequestMapping("/forward")
 	@ResponseBody
 	public boolean forward(
 	        @RequestParam("content") String content,
 	        @RequestParam("originId") Long originId) {
 	    Long uid = AuthUtil.currentUid();
 	    if (uid == null) {return false;}
 
 	    Tweet tweet = tweetPostService.forward(uid, content, originId);
         logger.info("forward tweet {} success", tweet.getId());
 	    return true;
 	}
 	
 	@RequestMapping("/blog")
 	@ResponseBody
 	public boolean blog(
 			@RequestParam("title") String title,
 			@RequestParam("content") String content,
 			@RequestParam(value="tagIds[]", required=false) Collection<Long> tagIds) {
 		Long uid = AuthUtil.currentUid();
 		if (uid == null) {return false;}
 		if (title.isEmpty() || content.isEmpty()) {return false;}
 		if (tagIds == null) {tagIds = new ArrayList<>(0);}
 
 		Blog blog = blogService.newBlog(uid, title, content, tagIds);
 		tweetPostService.share(uid, blog);
 		if (blog != null) {
 			logger.info("post blog {} success", blog.getId());
 			return true;
 		}
 		else {
 			logger.info("post blog failure");
 			return false;
 		}
 	}
 	
 	@RequestMapping("/comment")
 	@ResponseBody
	public boolean comment(@RequestParam("content") String content, @RequestParam("source") Long sourceId) {
 	    Long uid = AuthUtil.currentUid();
 	    if (uid == null) {return false;}
 	    if (content.isEmpty()) {return false;}
 	    
 	    tweetPostService.comment(uid, content, sourceId);
 	    return true;
 	}
 }
