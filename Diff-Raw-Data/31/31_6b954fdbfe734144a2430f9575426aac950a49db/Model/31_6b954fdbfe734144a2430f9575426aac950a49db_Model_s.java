 package ru.terra.jbrss.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Singleton;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import ru.terra.jbrss.db.controllers.FeedpostsJpaController;
 import ru.terra.jbrss.db.controllers.FeedsJpaController;
 import ru.terra.jbrss.db.controllers.UserJpaController;
 import ru.terra.jbrss.db.controllers.exceptions.NonexistentEntityException;
 import ru.terra.jbrss.db.entity.Feedposts;
 import ru.terra.jbrss.db.entity.Feeds;
 import ru.terra.jbrss.db.entity.User;
 import ru.terra.jbrss.rss.Downloader;
 import ru.terra.jbrss.web.security.SessionHelper;
 import ru.terra.twochsaver.twoch.SingleThreadDownloader;
 import ru.terra.twochsaver.twoch.db.TMessage;
 import ru.terra.twochsaver.twoch.db.TThread;
 import ru.terra.twochsaver.twoch.db.controller.TMessageJpaController;
 import ru.terra.twochsaver.twoch.db.controller.TThreadJpaController;
 
 @Singleton
 @Component
 public class Model {
 	private FeedsJpaController feedsJpaController;
 	private UserJpaController usersJpaController;
 	private FeedpostsJpaController feedpostsJpaController;
 	private Downloader downloader = new Downloader();
 	Logger log = LoggerFactory.getLogger(Model.class);
 
 	public Model() {
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jbrss-dbPU");
 		feedsJpaController = new FeedsJpaController(emf);
 		usersJpaController = new UserJpaController(emf);
 		feedpostsJpaController = new FeedpostsJpaController(emf);
 	}
 
 	public User getUser(Integer id) {
 		return usersJpaController.findUser(id);
 	}
 
 	public User getUser(String user) {
 		return usersJpaController.findUser(user);
 	}
 
 	public Boolean addFeed(final User user, final String url) throws IllegalAccessException {
 		if (feedsJpaController.findFeedByUserAndByURL(user.getId(), url) == null) {
 			if (url.contains("2ch")) {
 				new Thread(new Runnable() {
 					public void run() {
 						try {
 							Feeds f = new Feeds(0, user.getId(), new SingleThreadDownloader().loadName(url), url, new Date());
 							feedsJpaController.create(f);
 						} catch (Exception e) {
 							log.info("unable to load 2ch thread", e);
 							e.printStackTrace();
 						}
 					}
 				}).start();
 			} else {
 				new Thread(new Runnable() {
 					public void run() {
 						Feeds f = new Feeds(0, user.getId(), downloader.getFeedTitle(url), url, new Date());
 						feedsJpaController.create(f);
 					}
 				}).start();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public List<Feeds> getFeeds(Integer uid) throws IllegalAccessException {
 		if (SessionHelper.getCurrentIUserId() == uid) {
 			return feedsJpaController.findFeedsByUser(uid);
 		} else {
 			throw new IllegalAccessException();
 		}
 	}
 
 	public Integer updateFeed(Feeds feed) throws IllegalAccessException {
 		if (SessionHelper.getCurrentIUserId() == feed.getUserid()) {
 			if (feed.getFeedurl().contains("2ch")) {
 				try {
 					Integer downloaded = new SingleThreadDownloader().download(feed.getFeedurl());
 					return downloaded;
 				} catch (Exception e) {
 					log.info("error while load thread ", e);
 					return 0;
 				}
 			} else {
 				List<Feedposts> posts = downloader.loadFeeds(feed.getFeedurl());
 				Date d = feedpostsJpaController.getLastPostDate(feed.getId());
 				List<Feedposts> newPosts;
 				if (d != null) {
 					newPosts = new ArrayList<Feedposts>();
 					for (Feedposts fp : posts) {
 						if (fp.getPostdate().getTime() > d.getTime())
 							newPosts.add(fp);
 					}
 				} else {
 					newPosts = new ArrayList<Feedposts>(posts);
 				}
 				for (Feedposts fp : newPosts) {
 					fp.setFeedId(feed.getId());
 					feedpostsJpaController.create(fp);
 				}
 				return newPosts.size();
 			}
 		} else {
 			throw new IllegalAccessException();
 		}
 	}
 
 	public List<Feedposts> getNewUserPosts(Integer uid, Feeds feed, Date d) throws IllegalAccessException {
 		if (SessionHelper.getCurrentIUserId() == uid) {
 			if (feed.getFeedurl().contains("2ch")) {
 				List<Feedposts> ret = new ArrayList<>();
 				EntityManagerFactory emf = Persistence.createEntityManagerFactory("2chPU");
 				TMessageJpaController mjc = new TMessageJpaController(emf);
 				TThreadJpaController tjc = new TThreadJpaController(emf);
				TThread tt = tjc.findByBoard(feed.getFeedurl());
 				List<TMessage> msgs = new ArrayList<>();
 				if (tt != null) {
 					msgs.add(mjc.findTMessage(tt.getStartMessage()));
 					msgs.addAll(mjc.findByParent(tt.getStartMessage()));
 				}
 				if (msgs.size() > 0)
 					for (TMessage msg : msgs)
 						ret.add(process(feed, msg));
 				return ret;
 			} else
 				return feedpostsJpaController.findFeedpostsByFeedFromDate(feedsJpaController.findFeedByUserAndById(uid, feed.getId()).getId(), d);
 		} else {
 			throw new IllegalAccessException();
 		}
 	}
 
 	public List<Feedposts> getFeedPosts(Integer feedId, Integer page, Integer perpage) {
 		return feedpostsJpaController.findFeedpostsByFeed(feedId, page, perpage);
 	}
 
 	public Boolean isUsersExists(String user) {
 		return usersJpaController.findUser(user) != null;
 	}
 
 	public Feeds getUserFeed(Integer uid, Integer fid) throws IllegalAccessException {
 		if (SessionHelper.getCurrentIUserId() == uid) {
 			return feedsJpaController.findFeedByUserAndById(uid, fid);
 		} else {
 			throw new IllegalAccessException();
 		}
 	}
 
 	public void editFeed(Feeds f) {
 		try {
 			feedsJpaController.edit(f);
 		} catch (NonexistentEntityException e) {
 			log.info("editFeed", e);
 			e.printStackTrace();
 		} catch (Exception e) {
 			log.info("editFeed", e);
 			e.printStackTrace();
 		}
 	}
 
 	public void setFeedRead(Integer feed, Boolean read) throws IllegalAccessException {
 		feedpostsJpaController.setPostsForFeedRead(feed, read);
 	}
 
 	public void setPostRead(Integer fp, Boolean read) throws IllegalAccessException {
 		Feedposts feedpost = feedpostsJpaController.findFeedposts(fp);
 		if (feedpost != null) {
 			feedpost.setRead(read);
 			try {
 				feedpostsJpaController.edit(feedpost);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void setAllRead(Integer uid) throws IllegalAccessException {
 	}
 
 	private Feedposts process(Feeds feed, TMessage msg) {
 		Feedposts fp = new Feedposts();
 		fp.setFeedId(feed.getId());
 		fp.setId(msg.getNum().intValue());
 		fp.setPostdate(new Date(msg.getMsgtimestamp()));
 		fp.setPostlink(feed.getFeedurl());
 		fp.setPosttext(msg.getComment());
		fp.setPosttitle(msg.getName());
 		fp.setRead(false);
 		fp.setUpdated(new Date());
 		return fp;
 	}
 }
