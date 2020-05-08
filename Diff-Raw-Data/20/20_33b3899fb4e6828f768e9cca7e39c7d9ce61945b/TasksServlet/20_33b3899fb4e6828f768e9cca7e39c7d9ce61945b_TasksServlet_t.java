 /**
  * 
  */
 package com.opensajux.common;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import twitter4j.Status;
 
 import com.google.api.services.blogger.model.Blog;
 import com.google.api.services.blogger.model.Post;
 import com.google.api.services.blogger.model.PostList;
 import com.google.api.services.plus.model.Activity;
 import com.google.appengine.api.datastore.Text;
 import com.opensajux.dao.PMF;
 import com.opensajux.dto.FacebookStatusMessage;
 import com.opensajux.dto.SiteDetails;
 import com.opensajux.entity.BlogPost;
 import com.opensajux.entity.MyBlog;
 import com.opensajux.entity.SiteInfo;
 import com.opensajux.entity.TalkStream;
 import com.opensajux.integration.BloggerClient;
 import com.opensajux.integration.FacebookClient;
 import com.opensajux.integration.PlusClient;
 import com.opensajux.integration.TwitterClient;
 
 /**
  * @author Sheikh Mohammad Sajid
  * 
  */
 public class TasksServlet extends HttpServlet {
 	private static final long serialVersionUID = 2175074959875861703L;
 
 	private static Logger LOGGER = Logger.getLogger(TasksServlet.class.getName());
 
	@SuppressWarnings("unchecked")
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter out = resp.getWriter();
 		BloggerClient bloggerClient;
 		PlusClient plusClient;
 		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
 		SiteInfo siteInfo = ((List<SiteInfo>) pm.newQuery(SiteInfo.class).execute()).get(0);
 		SiteDetails siteDetails = new SiteDetails();
 		siteDetails.setAboutMe(siteInfo.getAboutMe());
 		siteDetails.setTitle(siteInfo.getTitle());
 		siteDetails.setSubTitle(siteInfo.getSubTitle());
 		siteDetails.setGoogleApiKey(siteInfo.getGoogleApiKey());
 		siteDetails.setTwitterAccessToken(siteInfo.getTwitterAccessToken());
 		siteDetails.setTwitterAccessTokenSecret(siteInfo.getTwitterAccessTokenSecret());
 		siteDetails.setTwitterConsumerKey(siteInfo.getTwitterConsumerKey());
 		siteDetails.setTwitterConsumerSecret(siteInfo.getTwitterConsumerSecret());
 		siteDetails.setFacebookAppId(siteInfo.getFacebookAppId());
 		siteDetails.setFacebookAppSecret(siteInfo.getFacebookAppSecret());
 		siteDetails.setFacebookAccessToken(siteInfo.getFacebookAccessToken());
 
 		if (req.getRequestURI().endsWith("blogs")) {
 			try {
 				Query query = pm.newQuery(BlogPost.class);
 				List<BlogPost> blogCache = (List<BlogPost>) query.execute();
 
 				bloggerClient = new BloggerClient(siteDetails);
 				query = pm.newQuery(MyBlog.class);
 				List<MyBlog> myBlogs = (List<MyBlog>) query.execute();
 				for (MyBlog mb : myBlogs) {
 					Blog blog = bloggerClient.getBlog(mb.getUrl().getValue());
 					PostList posts = bloggerClient.getPostList(blog);
 					for (Post p : posts.getItems()) {
 						boolean found = false;
 						for (BlogPost b : blogCache) {
 							if (b.getId().equals(p.getId().toString())) {
 								b.setTitle(new Text(p.getTitle()));
 								b.setContent(new Text(p.getContent()));
 								b.setPublishDate(new Date(p.getPublished().getValue()));
 								b.setUpdatedDate(new Date()); // set to today
 								b.setSourceUrl(new Text(p.getUrl()));
 								found = true;
 
 								pm.makePersistent(b);
 							}
 						}
 						if (!found) { // new post
 							BlogPost b = new BlogPost();
 							b.setId(p.getId().toString());
 							b.setTitle(new Text(p.getTitle()));
 							b.setContent(new Text(p.getContent()));
 							b.setPublishDate(new Date(p.getPublished().getValue()));
 							b.setUpdatedDate(new Date()); // set to today
 							b.setSourceUrl(new Text(p.getUrl()));
 							b.setSource(SocialSource.BLOGGER);
 							b.setBlogKey(mb.getKey());
 							pm.makePersistent(b);
 						}
 					}
 				}
 				out.println("Blogs: Success");
 			} catch (Exception e) {
 				LOGGER.log(Level.SEVERE, e.getMessage(), e);
 				out.println("Blogs: Error");
 			} finally {
 				pm.close();
 			}
 		} else if (req.getRequestURI().endsWith("talkstream")) {
 			Query query = pm.newQuery(TalkStream.class);
 			List<TalkStream> talkCache = (List<TalkStream>) query.execute();
 			try {
 				// check Google Plus
 				plusClient = new PlusClient(siteDetails);
 				for (Activity act : plusClient.getActivities()) {
 					boolean found = false;
 					for (TalkStream talk : talkCache) {
 						if (act.getId().equals(talk.getId())) {
 							found = true;
 							if ("".equals(act.getObject().getContent())) {
 								if (act.getObject().getAttachments() != null
 										&& act.getObject().getAttachments().size() > 0) {
 									talk.setTitle(new Text(act.getObject().getAttachments().get(0).getDisplayName()));
 									talk.setContent(new Text(act.getObject().getAttachments().get(0).getContent()));
 								}
 							}
 							talk.setPublishDate(new Date(act.getPublished().getValue()));
 							talk.setUpdatedDate(new Date(act.getUpdated().getValue()));
 							talk.setSource(SocialSource.GOOGLE_PLUS);
 							talk.setSourceUrl(new Text(act.getUrl()));
 							pm.makePersistent(talk);
 						}
 					}
 					if (!found && act.getVerb().equalsIgnoreCase("post")) {
 						TalkStream talk = new TalkStream();
 						talk.setId(act.getId());
 						if ("".equals(act.getObject().getContent())) {
 							if (act.getObject().getAttachments() != null && act.getObject().getAttachments().size() > 0) {
 								talk.setTitle(new Text(act.getObject().getAttachments().get(0).getDisplayName()));
 								talk.setContent(new Text(act.getObject().getAttachments().get(0).getContent()));
 							}
 						}
 						talk.setPublishDate(new Date(act.getPublished().getValue()));
 						talk.setUpdatedDate(new Date(act.getUpdated().getValue()));
 						talk.setSource(SocialSource.GOOGLE_PLUS);
 						talk.setSourceUrl(new Text(act.getUrl()));
 						pm.makePersistent(talk);
 					}
 				}
 				out.println("Plus: Success");
 			} catch (Exception e) {
 				LOGGER.log(Level.SEVERE, e.getMessage(), e);
 				out.println("Plus: Error");
 			}
 			try {
 				// check Twitter
 				String twitterUser = "sajux";
 				TwitterClient twitterClient = new TwitterClient(siteDetails);
 				List<Status> list = twitterClient.getUserStatuses(twitterUser);
 				for (Status s : list) {
 					boolean found = false;
 					for (TalkStream talk : talkCache) {
 						if (talk.getId().equals("" + s.getId())) {
 							found = true;
 							talk.setTitle(new Text(s.getText()));
							talk.setPublishDate(s.getCreatedAt());
							talk.setUpdatedDate(s.getCreatedAt());
 							talk.setSource(SocialSource.TWITTER);
 							talk.setSourceUrl(new Text("https://twitter.com/" + twitterUser + "/status/" + s.getId()));
 							pm.makePersistent(talk);
 						}
 					}
 					if (!found) {
 						TalkStream talk = new TalkStream();
 						talk.setId(s.getId() + "");
 						talk.setTitle(new Text(s.getText()));
						talk.setPublishDate(s.getCreatedAt());
						talk.setUpdatedDate(s.getCreatedAt());
 						talk.setSource(SocialSource.TWITTER);
 						talk.setSourceUrl(new Text("https://twitter.com/" + twitterUser + "/status/" + s.getId()));
 						pm.makePersistent(talk);
 					}
 				}
 				out.println("Twitter: Success");
 			} catch (Exception e) {
 				LOGGER.log(Level.SEVERE, e.getMessage(), e);
 				out.println("Twitter: Error");
 			}
 			try {
 				// check facebook
 				String facebookUser = "abuabdullah.sajid";
 				FacebookClient facebookClient = new FacebookClient(siteDetails);
 				List<FacebookStatusMessage> statuses = facebookClient.getUserPosts(facebookUser);
 				if (statuses != null) {
 					for (FacebookStatusMessage sm : statuses) {
 						boolean found = false;
 						for (TalkStream talk : talkCache) {
 							if (talk.getId().equals(sm.getId())) {
 								found = true;
 								talk.setTitle(new Text(sm.getMessage()));
 								talk.setPublishDate(sm.getUpdatedTime());
 								talk.setSource(SocialSource.FACEBOOK);
 								talk.setSourceUrl(new Text("https://www.facebook.com/" + facebookUser + "/posts/"
 										+ sm.getUpdatedTime().getTime()));
 								pm.makePersistent(talk);
 							}
 						}
 						if (!found) {
 							TalkStream talk = new TalkStream();
 							talk.setId(sm.getId());
 							talk.setTitle(new Text(sm.getMessage()));
 							talk.setPublishDate(sm.getUpdatedTime());
 							talk.setSource(SocialSource.FACEBOOK);
 							talk.setSourceUrl(new Text("https://www.facebook.com/" + facebookUser + "/posts/"
 									+ sm.getUpdatedTime().getTime()));
 							pm.makePersistent(talk);
 						}
 					}
 				}
 				out.println("Facebook: Success");
 			} catch (Exception e) {
 				LOGGER.log(Level.SEVERE, e.getMessage(), e);
 				out.println("Facebook: Error");
 			} finally {
 				pm.close();
 			}
 		} else if (req.getRequestURI().endsWith("keepalive")) {
 			LOGGER.info("Breathing ...");
 		}
 	}
 }
