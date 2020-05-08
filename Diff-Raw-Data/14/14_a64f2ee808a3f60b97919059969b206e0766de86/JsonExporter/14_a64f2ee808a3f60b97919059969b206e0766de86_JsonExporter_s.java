 package org.dentleisen.appening2;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
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
			.getCfgStr("appening.export.awsAccessKey");
 	private static final String s3Bucket = Utils
 			.getCfgStr("appening.export.S3Bucket");
 	private static final String s3Key = Utils
 			.getCfgStr("appening.export.s3Key");
 
 	private static final long interval = Utils
 			.getCfgInt("appening.export.intervalSeconds") * 1000;
 
 	public static void main(String[] args) {
 		try {
 			final RestS3Service s3 = new RestS3Service(new AWSCredentials(
 					awsAccessKey, awsSecretKey));
 
 			final AccessControlList bucketAcl = s3.getBucketAcl(s3Bucket);
 			bucketAcl.grantPermission(GroupGrantee.ALL_USERS,
 					Permission.PERMISSION_READ);
 
 			Timer t = new Timer();
 			TimerTask tt = new TimerTask() {
 				@Override
 				public void run() {
 					JSONArray json = createJson();
 
 					try {
 						File tmpFile = File.createTempFile("appening-places",
 								".json");
 						json.writeJSONString(new FileWriter(tmpFile));
 
 						S3Object dataFileObject = new S3Object(tmpFile);
 						dataFileObject.setContentType("application/json");
 						dataFileObject.setKey(s3Key);
 						dataFileObject.setAcl(bucketAcl);
 
 						s3.putObject(s3Bucket, dataFileObject);
 
 						tmpFile.delete();
 
 						log.info("Created JSON & uploaded to S3");
 					} catch (Exception e) {
 						log.warn("Unable to create and upload json file", e);
 					}
 				}
 			};
 			t.scheduleAtFixedRate(tt, 0, interval);
 
 		} catch (Exception e) {
			log.error("Unable to use S3, exiting");
 			System.exit(-1);
 		}
 
 	}
 
 	public static JSONArray createJson() {
 		return createJson(Calendar.getInstance(TimeZone.getTimeZone("UTC"))
 				.getTime());
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public static JSONArray createJson(Date cDate) {
 		JSONArray placesArr = new JSONArray();
 
 		try {
 			List<Place> places = Place.loadPopularPlaces(minMentions,
 					minMentionsDays);
 			for (Place p : places) {
 				JSONObject placeObj = new JSONObject();
 
 				placeObj.put("id", p.id);
 				placeObj.put("name", p.name);
 				placeObj.put("lat", p.lat);
 				placeObj.put("lng", p.lng);
 
 				Map<Integer, Integer> m = p.loadPlaceMentions(cDate);
 
 				placeObj.put("mentions", m);
 
 				placesArr.add(placeObj);
 			}
 		} catch (Exception e) {
 			log.warn("Unable to generate JSON", e);
 		}
 		return placesArr;
 	}
 }
