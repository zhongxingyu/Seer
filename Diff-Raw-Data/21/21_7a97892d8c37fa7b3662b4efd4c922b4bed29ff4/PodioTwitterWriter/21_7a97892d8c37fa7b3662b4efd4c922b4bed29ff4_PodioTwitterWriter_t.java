 package com.podio.sample.twitter;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 
 import twitter4j.HashtagEntity;
 import twitter4j.Status;
 import twitter4j.URLEntity;
 import twitter4j.UserMentionEntity;
 
 import com.podio.APIFactory;
 import com.podio.ResourceFactory;
 import com.podio.comment.Comment;
 import com.podio.comment.CommentAPI;
 import com.podio.comment.CommentCreate;
 import com.podio.common.Reference;
 import com.podio.common.ReferenceType;
import com.podio.file.FileAPI;
 import com.podio.item.FieldValuesUpdate;
import com.podio.item.ItemAPI;
 import com.podio.item.ItemCreate;
 import com.podio.item.ItemsResponse;
 import com.podio.oauth.OAuthClientCredentials;
 import com.podio.oauth.OAuthUsernameCredentials;
 
 public class PodioTwitterWriter implements TwitterWriter {
 
 	/**
 	 * The id of the app in Podio
 	 */
 	private static final int APP_ID = 29350;
 
 	/**
 	 * The external ids of the fields in Podio
 	 */
 	private static final String TEXT = "title";
 	private static final String TWEET = "tweet";
 	private static final String FROM = "user";
 	private static final String AVATAR = "avatar";
 	private static final String LOCATION = "location";
 	private static final String SOURCE = "source";
 	private static final String LINK = "link";
 	private static final String REPLY_TO = "reply-to";
 
 	/**
 	 * The interface to Podio
 	 */
 	private final APIFactory apiFactory;
 
 	public PodioTwitterWriter(Properties properties) {
 		ResourceFactory resourceFactory = new ResourceFactory(
 				new OAuthClientCredentials(
 						properties.getProperty("podio.client.mail"),
 						properties.getProperty("podio.client.secret")),
 				new OAuthUsernameCredentials(properties
 						.getProperty("podio.user.mail"), properties
 						.getProperty("podio.user.password")));
 		this.apiFactory = new APIFactory(resourceFactory);
 	}
 
 	/**
 	 * Returns the full text for for a Tweet
 	 * 
 	 * @param tweet
 	 *            The tweet to get the full text for
 	 * @param status
 	 *            The status of the tweet
 	 * @return The full text of the tweet in HTML code
 	 */
 	private String getFullText(Status status, TweetPrinter printer) {
 		String text = status.getText();
 
 		HashtagEntity[] hashtags = status.getHashtagEntities();
 		if (hashtags != null) {
 			for (HashtagEntity hashtag : hashtags) {
 				try {
 					String url = "http://twitter.com/#search?q="
 							+ URLEncoder.encode(hashtag.getText(), "UTF-8");
 					String tag = "#" + hashtag.getText();
 
 					text = text.replace(tag, printer.getLink(tag, url));
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		UserMentionEntity[] users = status.getUserMentionEntities();
 		if (users != null) {
 			for (UserMentionEntity user : users) {
 				String url = "http://twitter.com/" + user.getScreenName();
 				String tag = "@" + user.getScreenName();
 
 				text = text.replace(tag, printer.getLink(tag, url));
 			}
 		}
 
 		String url = "http://twitter.com/" + status.getUser().getScreenName();
 		String tag = "@" + status.getUser().getScreenName();
 
 		text = text.replace(tag, printer.getLink(tag, url));
 
 		return text;
 	}
 
 	private boolean include(Status status) {
 		UserMentionEntity[] users = status.getUserMentionEntities();
 		if (users != null) {
 			for (UserMentionEntity user : users) {
 				if (user.getScreenName().equals("podio")
 						|| user.getScreenName().equals("podiosupport")) {
 					return true;
 				}
 			}
 		}
 
		if (status.getText().contains("pódio")
				|| status.getText().contains("pòdio")) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.podio.sample.twitter.TwitterWriter#tweet(twitter4j.Status)
 	 */
 	@Override
 	public boolean write(Status status) throws Exception {
 		if (!include(status)) {
 			System.out.println("Skipping");
 			return false;
 		}
 
 		if (getItemId(status.getId()) != null) {
 			System.out.println("Already added");
 			return false;
 		}
 
 		if (!status.isRetweet()) {
 			Integer imageId = uploadProfile(status);
 
 			TweetPrinter printer = new HTMLTweetPrinter();
 
 			List<FieldValuesUpdate> fields = new ArrayList<FieldValuesUpdate>();
 			fields.add(new FieldValuesUpdate(TEXT, "value", status.getText()));
 			fields.add(new FieldValuesUpdate(TWEET, "value", getFullText(
 					status, printer)));
 			fields.add(new FieldValuesUpdate(FROM, "value", getAuthorLink(
 					status, printer)));
 			if (imageId != null) {
 				fields.add(new FieldValuesUpdate(AVATAR, "value", imageId));
 			}
 			if (status.getPlace() != null) {
 				fields.add(new FieldValuesUpdate(LOCATION, "value", status
 						.getPlace().getName()));
 			}
 			if (status.getSource() != null) {
 				fields.add(new FieldValuesUpdate(SOURCE, "value", status
 						.getSource()));
 			}
 			fields.add(new FieldValuesUpdate(LINK, "value",
 					getTweetLink(status)));
 			if (status.getInReplyToStatusId() > 0) {
 				Integer replyToItemId = getItemId(status.getInReplyToStatusId());
 				if (replyToItemId != null) {
 					fields.add(new FieldValuesUpdate(REPLY_TO, "value",
 							replyToItemId));
 				}
 			}
 
 			List<String> tags = new ArrayList<String>();
 			if (status.getHashtagEntities() != null) {
 				for (HashtagEntity tag : status.getHashtagEntities()) {
 					tags.add(tag.getText());
 				}
 			}
 
 			List<Integer> fileIds = uploadURLs(status);
 
			apiFactory.getAPI(ItemAPI.class).addItem(
 					APP_ID,
 					new ItemCreate(Long.toString(status.getId()), fields,
 							fileIds, tags), false);
 
 			System.out.println("Added tweet " + status.getText());
 
 			return true;
 		} else {
 			Integer retweetItemId = getItemId(status.getRetweetedStatus()
 					.getId());
 			if (retweetItemId == null) {
 				System.out.println("Unable to find original tweet");
 				return false;
 			}
 
			CommentAPI commentAPI = apiFactory.getAPI(CommentAPI.class);
 			List<Comment> comments = commentAPI.getComments(new Reference(
 					ReferenceType.ITEM, retweetItemId));
 			for (Comment comment : comments) {
 				if (comment.getExternalId() != null
 						&& comment.getExternalId().equals(
 								Long.toString(status.getId()))) {
 					System.out.println("Already added as comment");
 					return false;
 				}
 			}
 
 			Integer profileImageId = uploadProfile(status);
 			List<Integer> fileIds = new ArrayList<Integer>();
 			if (profileImageId != null) {
 				fileIds.add(profileImageId);
 			}
 
 			fileIds.addAll(uploadURLs(status));
 
 			TweetPrinter printer = new MarkdownTweetPrinter();
 
 			String text = getFullText(status, printer);
 			text += "\n";
 			text += "\n";
 			text += getAuthorLink(status, printer);
 			text += "\n";
 			text += "\n";
 			text += printer.getLink("twitter.com", getTweetLink(status));
 
 			commentAPI.addComment(new Reference(ReferenceType.ITEM,
 					retweetItemId),
 					new CommentCreate(text, Long.toString(status.getId()),
 							Collections.<Integer> emptyList(), fileIds), false);
 
 			System.out.println("Added retweet " + status.getText());
 
 			return true;
 		}
 	}
 
 	/**
 	 * Returns the link to the tweet
 	 * 
 	 * @param tweet
 	 *            The tweet to link to
 	 * @return The link
 	 */
 	private String getTweetLink(Status status) {
 		return "http://twitter.com/" + status.getUser().getScreenName()
 				+ "/status/" + status.getId();
 	}
 
 	/**
 	 * Returns the link to the author of the tweet
 	 * 
 	 * @param tweet
 	 *            The tweet in question
 	 * @param status
 	 *            The status of the tweet
 	 * @return The link
 	 */
 	private String getAuthorLink(Status status, TweetPrinter printer) {
 		String url = "http://twitter.com/" + status.getUser().getScreenName();
 		return printer.getLink(status.getUser().getName(), url);
 	}
 
 	/**
 	 * Uploads the profile picture to Podio
 	 * 
 	 * @param status
 	 *            The status to upload the photo from
 	 * @return The file id of the uploaded file
 	 */
 	private Integer uploadProfile(Status status) {
 		return uploadURL(status.getUser().getProfileImageURL());
 	}
 
 	private List<Integer> uploadURLs(Status status) {
 		List<Integer> fileIds = new ArrayList<Integer>();
 
 		URLEntity[] urls = status.getURLEntities();
 		if (urls != null) {
 			for (URLEntity url : urls) {
 				URL resolvedUrl = url.getExpandedURL() != null ? url
 						.getExpandedURL() : url.getURL();
 				if (resolvedUrl == null) {
 					continue;
 				}
 
 				resolvedUrl = resolveURL(resolvedUrl);
 				if (resolvedUrl == null) {
 					continue;
 				}
 
 				Integer fileId = uploadURL(resolvedUrl);
 				if (fileId == null) {
 					continue;
 				}
 
 				fileIds.add(fileId);
 			}
 		}
 
 		return fileIds;
 	}
 
 	/**
 	 * Uploads a single URL to the given reference if the URL is a link to an
 	 * image. Currently only images from twitpic and yfrog is supported.
 	 * 
 	 * @param url
 	 *            The URL to the image
 	 * @param reference
 	 *            The reference to attach the image to
 	 */
 	private Integer uploadURL(URL url) {
 		try {
 			String contentType = url.openConnection().getContentType();
 			if (contentType == null || !contentType.startsWith("image/")) {
 				return null;
 			}
 
 			String name = url.getPath().substring(1);
 			if (contentType.contains("png")) {
 				name += ".png";
 			} else if (contentType.contains("jpg")
 					|| contentType.contains("jpeg")) {
 				name += ".jpg";
 			} else if (contentType.contains("gif")) {
 				name += ".gif";
 			}
 
			return apiFactory.getAPI(FileAPI.class).uploadImage(url, name);
 		} catch (IOException e) {
 			e.printStackTrace();
 
 			return null;
 		}
 	}
 
 	private URL resolveURL(URL url) {
 		try {
 			if (url.getHost().equals("twitpic.com")) {
 				return new URL("http://twitpic.com/show/large" + url.getPath());
 			} else if (url.getHost().equals("yfrog.com")) {
 				return new URL(url.toString() + ":medium");
 			} else {
 				return null;
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the item id for a given tweet id.
 	 * 
 	 * @param id
 	 *            The id of the tweet
 	 * @return The item id if the tweet exists in Podio already,
 	 *         <code>null</code> otherwise
 	 */
 	private Integer getItemId(long id) {
		ItemsResponse response = apiFactory.getAPI(ItemAPI.class)
				.getItemsByExternalId(APP_ID, Long.toString(id));
 
 		if (response.getFiltered() == 0) {
 			return null;
 		}
 
 		return response.getItems().get(0).getId();
 	}
 }
