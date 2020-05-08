 package org.dentleisen.appening2;
 
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.ServiceException;
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.acl.GroupGrantee;
 import org.jets3t.service.acl.Permission;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.security.AWSCredentials;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.io.SyndFeedOutput;
 
 public class DataExporter {
 
 	private static Logger log = Logger.getLogger(DataExporter.class);
 
 	private static final long minMentions = Utils
 			.getCfgInt("appening.export.minMentions");
 	private static final long minMentionsDays = Utils
 			.getCfgInt("appening.export.minMentionDays");
 
 	private static final String awsAccessKey = Utils
 			.getCfgStr("appening.export.awsAccessKey");
 	private static final String awsSecretKey = Utils
 			.getCfgStr("appening.export.awsSecretKey");
 	private static final String s3Bucket = Utils
 			.getCfgStr("appening.export.S3Bucket");
 	private static final String s3Prefix = Utils
 			.getCfgStr("appening.export.s3Prefix");
 
 	private static final long interval = Utils
 			.getCfgInt("appening.export.intervalSeconds") * 1000;
 
 	// i know, wrong prefix... but hey
 	private static final String urlPrefix = Utils
 			.getCfgStr("appening.twitter.urlPrefix");
 
 	private static final int numMessages = 20;
 
 	public static void main(String[] args) {
 		log.info("Data Exporter starting, charset=" + Charset.defaultCharset()
 				+ ", interval=" + interval);
 		try {
 
 			Timer t = new Timer();
 			TimerTask tt = new TimerTask() {
 				@SuppressWarnings("unchecked")
 				@Override
 				public void run() {
 
 					// now the rss feed...
 
 					try {
 						SyndFeed feed = new SyndFeedImpl();
 						feed.setFeedType("rss_2.0");
 						feed.setLink("http://www.appening.at");
 						feed.setTitle("Appening Amsterdam");
 						feed.setDescription("Things happening now in Amsterdam according to Twitter.");
 						List<PopularPlace> places = Place
 								.loadLastMentionedPlaces(50);
 						List<SyndEntry> entries = new ArrayList<SyndEntry>();
 
 						for (PopularPlace p : places) {
 							SyndEntry entry = new SyndEntryImpl();
 							SyndContent description;
 
 							entry.setTitle(p.name);
							String link = urlPrefix + "#"
									+ URLEncoder.encode(p.name, "UTF-8") + "-"
									+ p.id;
 
 							entry.setLink(link);
 
 							entry.setPublishedDate(p.lastMentioned);
 							description = new SyndContentImpl();
 							description.setType("text/html");
 
 							String html = "<ul><li><a href=\""
 									+ link
 									+ "\">Appening page for place</a></li><li><a href=\"https://maps.google.com/maps?t=h&q=loc:"
 									+ p.lat
 									+ ","
 									+ p.lng
 									+ "&z=17\">Google Maps</a></li></ul><br /><ul>";
 							List<Message> recentMessages = p
 									.loadRecentMessages(numMessages,
 											Utils.messageThreshold);
 
 							for (Message m : recentMessages) {
								html += "<li>" + Utils.linkify(m.getText())
										+ "</li>";
 							}
 
 							html += "</ul>";
 
 							description.setValue(html);
 
 							entry.setDescription(description);
 							entries.add(entry);
 						}
 
 						feed.setEntries(entries);
 						SyndFeedOutput output = new SyndFeedOutput();
 						String rss = output.outputString(feed);
 
 						stringToS3(rss, s3Prefix + "feed.rss",
 								"application/rss+xml");
 
 						log.info("RSS feed uploaded");
 					} catch (Exception e) {
 						log.warn("Unable to create and upload RSS feed", e);
 					}
 
 					JSONArray json = new JSONArray();
 
 					try {
 						List<PopularPlace> places = Place.loadPopularPlaces(
 								minMentions, minMentionsDays);
 						for (PopularPlace p : places) { // upload recent twitter
 							// messages so we can display them
 							JSONArray messages = new JSONArray();
 							List<Message> recentMessages = p
 									.loadRecentMessages(numMessages,
 											Utils.messageThreshold);
 							for (Message msg : recentMessages) {
 								messages.add(msg.toJSON());
 							}
 
 							String messagesJsonUrl = jsonArrToS3(messages,
 									s3Prefix + p.id + "-messages.json");
 
 							// add reference to messages JSON to place so we can
 							// load it from GUI
 							JSONObject pj = p.toJSON();
 							pj.put("messagesUrl", messagesJsonUrl);
 							json.add(pj);
 						}
 						jsonArrToS3(json, s3Prefix + "places.json");
 
 						log.info("Created JSON & uploaded to S3");
 					} catch (Exception e) {
 						log.warn("Unable to create and upload json file", e);
 					}
 
 				}
 			};
 			t.scheduleAtFixedRate(tt, 0, interval);
 
 		} catch (Exception e) {
 			log.error("Unable to use S3, exiting", e);
 			System.exit(-1);
 		}
 
 	}
 
 	private static RestS3Service s3 = null;
 	private static AccessControlList bucketAcl = null;
 
 	private static String jsonArrToS3(JSONArray arr, String s3Key) {
 		return stringToS3(arr.toJSONString(), s3Key, "application/json");
 	}
 
 	private static String stringToS3(String data, String s3Key,
 			String contentType) {
 		if (s3 == null) {
 			try {
 				s3 = new RestS3Service(new AWSCredentials(awsAccessKey,
 						awsSecretKey));
 				s3.getHttpClient().getParams()
 						.setParameter("http.protocol.content-charset", "UTF-8");
 			} catch (S3ServiceException e) {
 				log.warn("Unable to initialize S3 client", e);
 			}
 		}
 		if (bucketAcl == null) {
 			try {
 				bucketAcl = s3.getBucketAcl(s3Bucket);
 			} catch (ServiceException e) {
 				log.warn("Unable to update S3 Bucket ACL", e);
 			}
 			bucketAcl.grantPermission(GroupGrantee.ALL_USERS,
 					Permission.PERMISSION_READ);
 		}
 
 		try {
 			S3Object dataFileObject = new S3Object(s3Key, data);
 			dataFileObject.setAcl(bucketAcl);
 			dataFileObject.setContentType(contentType);
 			dataFileObject.setContentEncoding("UTF-8");
 
 			s3.putObject(s3Bucket, dataFileObject);
 			log.info("Uploaded to " + s3Key);
 			return s3.createUnsignedObjectUrl(s3Bucket, s3Key, true, false,
 					false);
 		} catch (Exception e) {
 			log.warn("Unable to upload JSON to S3", e);
 		}
 
 		return "";
 	}
 }
