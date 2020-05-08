 package de.tangibleit.crawler.twitterUser;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.Proxy;
 import java.net.URL;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import twitter4j.MediaEntity;
 import twitter4j.Paging;
 import twitter4j.Status;
 import twitter4j.TwitterException;
 import twitter4j.URLEntity;
 import twitter4j.User;
 import de.tangibleit.crawler.twitterUser.Messages.CrawlUser;
 import de.tangibleit.crawler.twitterUser.db.Tables;
 import de.tangibleit.crawler.twitterUser.db.tables.pojos.BlacklistUrl;
 import de.tangibleit.crawler.twitterUser.db.tables.pojos.Tweet;
 import de.tangibleit.crawler.twitterUser.db.tables.pojos.TweetUrl;
 import de.tangibleit.crawler.twitterUser.db.tables.records.TweetRecord;
 import de.tangibleit.crawler.twitterUser.db.tables.records.TweetUrlRecord;
 import de.tangibleit.crawler.twitterUser.db.tables.records.UserRecord;
 
 public class UserWorker extends Worker<Messages.CrawlUser> {
 	private final String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
 	private final Pattern pattern = Pattern.compile(regex);
 
 	private String expandURL(String address) throws IOException {
 		URL url = new URL(address);
 
 		HttpURLConnection connection = (HttpURLConnection) url
 				.openConnection(Proxy.NO_PROXY);
 		connection.setInstanceFollowRedirects(false);
 		connection.connect();
 		String expandedURL = connection.getHeaderField("Location");
 		connection.getInputStream().close();
 		return expandedURL;
 	}
 
 	@Override
 	protected void execute(CrawlUser msg) throws SQLException {
 		log.info("Retrieve: " + msg.userName);
 		try {
 			// transaction.begin();
 			try {
 				preRequest();
 				Paging paging = new Paging();
 				paging.setCount(200);
 
 				long id;
 				try {
 					id = twitter.showUser(msg.userName).getId();
 				} catch (Exception e) {
 					log.info("user: " + msg.userName
 							+ " does not exist. skipping");
 					return;
 				}
 
 				// If we've already crawled this user in the past, don't crawl
 				// his old tweets.
 				TweetRecord rec = create.selectFrom(Tables.TWEET)
 						.where(Tables.TWEET.USER_ID.equal(id))
 						.orderBy(Tables.TWEET.ID.desc()).fetchOne();
 				if (rec != null)
 					paging.setSinceId(rec.getId());
 
 				List<Status> statuses = twitter.getUserTimeline(msg.userName,
 						paging);
 				if (statuses.isEmpty())
 					return;
 
 				processStatuses(statuses);
 				// Get total statuses count.
 				User u = statuses.get(0).getUser();
 				// Synchronise user
 				updateUser(u);
 
 				int statusCount = statuses.size();
 
 				log.info("Status count: " + statuses.size());
 				do {
 
 					preRequest();
 					paging.setMaxId(statuses.get(statuses.size() - 1).getId() - 1);
 					statuses = twitter.getUserTimeline(msg.userName, paging);
 					statusCount += statuses.size();
 
 					processStatuses(statuses);
 
 				} while (statuses.size() != 0);
 				log.info("done!");
 
 			} catch (TwitterException e) {
 				log.info("Twitter failure: " + e.getMessage());
 				// transaction.rollback();
 				getContext().parent().tell(msg);
 			}
 		} catch (SQLException e) {
 			connection.rollback();
 		} finally {
 			connection.commit();
 		}
 	}
 
 	private void processStatuses(List<Status> statuses) throws SQLException {
 
 		for (Status status : statuses) {
 			TweetRecord rec = create.selectFrom(Tables.TWEET)
 					.where(Tables.TWEET.ID.equal(status.getId())).fetchOne();
 			if (rec == null) {
 				rec = new TweetRecord();
 				rec.attach(create);
 				rec.setId(status.getId());
 			} else
 				continue; // If the tweet already exists, skip it this time.
 			rec.setMessage(status.getText());
 			rec.setTime(new Timestamp(status.getCreatedAt().getTime()));
 			rec.setUserId(status.getUser().getId());
 			rec.store();
 
 			Matcher m = pattern.matcher(status.getText());
 			while (m.find()) {
 				String urlStr = m.group();
 				if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
 					urlStr = urlStr.substring(1, urlStr.length() - 1);
 				}
 
 				String url;
 				try {
 					url = expandURL(urlStr);
 
 				} catch (IOException e) {
 					// 404 or similar
 					url = urlStr;
 				}
 
 				// Check for blacklisting
 
 				TweetUrlRecord urec = new TweetUrlRecord();
 				urec.attach(create);
 				urec.setTweetId(status.getId());
 				urec.setUrl(url);
 				urec.store();
 				// Add url to db.
 			}
 		}
 
 	}
 
 	private void updateUser(User user) throws SQLException {
 		UserRecord rec = create.selectFrom(Tables.USER)
 				.where(Tables.USER.ID.equal(user.getId())).fetchOne();
 
 		if (rec == null) {
 			rec = new UserRecord();
 			rec.setId(user.getId());
 			rec.attach(create);
 		}
 
 		rec.setScreenName(user.getScreenName());
 		rec.setImageUrl(user.getProfileImageURL());
 
 		rec.store();
 	}
 
 	public String getPath() {
 		return "/statuses/user_timeline";
 	}
 
 }
