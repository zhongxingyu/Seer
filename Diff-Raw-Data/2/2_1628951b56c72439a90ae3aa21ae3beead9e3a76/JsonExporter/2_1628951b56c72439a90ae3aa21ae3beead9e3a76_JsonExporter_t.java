 package org.dentleisen.appening2;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
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
 
 public class JsonExporter {
 
 	private static Logger log = Logger.getLogger(JsonExporter.class);
 
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
 
 	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
 	static {
 		df.setTimeZone(TimeZone.getTimeZone("UTC"));
 	}
 
 	public static void main(String[] args) {
 		try {
 
 			Timer t = new Timer();
 			TimerTask tt = new TimerTask() {
 				@SuppressWarnings("unchecked")
 				@Override
 				public void run() {
 					JSONArray json = new JSONArray();
 
 					try {
 						List<Place> places = Place.loadPopularPlaces(
 								minMentions, minMentionsDays);
 						for (Place p : places) {
 							JSONObject placeObj = new JSONObject();
 
 							placeObj.put("id", p.id);
 							placeObj.put("name", p.name);
 							placeObj.put("lat", p.lat);
 							placeObj.put("lng", p.lng);
 
 							// recent mentions
 							Map<Integer, Integer> m = p
 									.loadPlaceMentions(Calendar.getInstance(
 											TimeZone.getTimeZone("UTC"))
 											.getTime());
 
 							// recent messages
 							List<Message> recentMessagesForPlace = p
 									.loadRecentMessages(20, 13);
 
 							placeObj.put("mentions", m);
 
 							JSONArray messages = new JSONArray();
 							for (Message msg : recentMessagesForPlace) {
 								JSONObject msgObj = new JSONObject();
 								msgObj.put("id", msg.getId());
 								msgObj.put("created",
 										df.format(msg.getCreated()));
 								msgObj.put("user", msg.getUser());
 								msgObj.put("text", msg.getText());
 								messages.add(msgObj);
 							}
 
 							String messagesJsonUrl = jsonArrToS3(messages,
 									s3Prefix + p.id + "-messages.json");
 
 							placeObj.put("messagesUrl", messagesJsonUrl);
 							json.add(placeObj);
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
 
 	private static String jsonArrToS3(JSONArray json, String s3Key) {
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
 			File tmpFile = File.createTempFile("appening-upload", ".json");
 			FileWriter writer = new FileWriter(tmpFile);
 			json.writeJSONString(writer);
 			writer.close();
 
 			S3Object dataFileObject = new S3Object(tmpFile);
 			dataFileObject.setKey(s3Key);
 			dataFileObject.setAcl(bucketAcl);
 			dataFileObject.setContentType("application/json");
 			dataFileObject.setContentEncoding("UTF-8");
 
 			s3.putObject(s3Bucket, dataFileObject);
 			tmpFile.delete();
 
 			return s3.createUnsignedObjectUrl(s3Bucket, s3Key, true, false,
 					false);
 		} catch (Exception e) {
 			log.warn("Unable to upload JSON to S3", e);
 		}
 
 		return "";
 	}
 }
