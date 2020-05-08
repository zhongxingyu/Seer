 package controllers;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 import java.util.Formatter;
 import java.util.List;
 
 import models.Coupon;
 import models.Look;
 import models.UserLook;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import play.Logger;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Http.RequestBody;
 import play.mvc.Result;
 import so.tree.imageQueue.ImageSender;
 
 public class RestApp extends Controller {
 	
 	private static String LOCAL_IMAGE_PATH = System.getProperty("user.dir") + "/public/lookImages/";
 	private static String AMAZON_S3_PATH = "https://s3-ap-northeast-1.amazonaws.com/swmaestro/";
 	
 	static JSONObject jsonObject;
 
 	public static Result getLooks(String year, String season, String lookType) throws JSONException {
 		List<Look> looks;
 		if(year == null && season == null && lookType == null){
 			looks = Look.find.all();
 		}else if(year != null && season == null && lookType == null){
 			looks = Look.find.where().ilike("year", year).findList();
 		}else if(year == null && season != null && lookType == null){
 			looks = Look.find.where().ilike("season", season).findList();
 		}else if(year == null && season == null && lookType != null){
 			looks = Look.find.where().ilike("lookType", lookType).findList();
 		}else if(year != null && season != null && lookType == null){
 			looks = Look.find.where().ilike("year", year).ilike("season", season).findList();
 		}else if(year != null && season == null && lookType != null){
 			looks = Look.find.where().ilike("year", year).ilike("lookType", lookType).findList();
 		}else if(year == null && season != null && lookType != null){
 			looks = Look.find.where().ilike("season", season).ilike("lookType", lookType).findList();
 		}else{
 			looks = Look.find.where().ilike("year", year).ilike("season", season).ilike("lookType", lookType).findList();
 		}
 		if (looks.size() == 0) {
 			jsonObject = new JSONObject();
 			jsonObject.put("code", 2);
 			jsonObject.put("msg", "Looks are empty.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 		
 		
 		return ok(Json.toJson(looks)).as("application/json");
 	}
 
 	public static Result getLookById(Long id) throws JSONException {
 
 		Look look = Look.find.byId(id);
 		if (look == null) {
 			Logger.error("[code: -3] Can't find look.");
 			jsonObject = new JSONObject();
 			jsonObject.put("code", -3);
 			jsonObject.put("msg", "Can't find look.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 		return ok(Json.toJson(look)).as("application/json");
 	}
 	
 	public static Result getLookByBarcode(String barcode) throws JSONException {
 
 		Look look = Look.find.where().ilike("barcode", barcode).findUnique();
 		if (look == null) {
 			Logger.error("[code: -3] Can't find look.");
 			jsonObject = new JSONObject();
 			jsonObject.put("code", -3);
 			jsonObject.put("msg", "Can't find look.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 		return ok(Json.toJson(look)).as("application/json");
 	}
 
 	public static Result getAllUserLooksByLookId(Long id) throws JSONException {
 
 		Look look = Look.find.byId(id);
 		if (look == null) {
 			Logger.error("[code: -3] Can't find look.");
 			jsonObject = new JSONObject();
 			jsonObject.put("code", -3);
 			jsonObject.put("msg", "Can't find look.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 		if (look.getUserLooks().size() == 0) {
 			jsonObject = new JSONObject();
 			jsonObject.put("code", 2);
 			jsonObject.put("msg", "UserLooks are empty.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 		
 //		for(UserLook userLook : look.getUserLooks()){
 //			if(userLook.isImageToS3()){
 //				userLook.setImageFileName(AMAZON_S3_PATH + userLook.getImageFileName());
 //			}else{
 //				userLook.setImageFileName(LOCAL_IMAGE_PATH + userLook.getImageFileName());
 //			}
 //		}
 		
 		return ok(Json.toJson(look.getUserLooks())).as("application/json");
 	}
 
 	public static Result getUserLookById(Long id) throws JSONException {
 
 		UserLook userLook = UserLook.find.byId(id);
 		if (userLook == null) {
 			Logger.error("[code: -4] Can't find UserLook.");
 			jsonObject = new JSONObject();
 			jsonObject.put("code", -4);
 			jsonObject.put("msg", "Can't find UserLook.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 
 //		if(userLook.isImageToS3()){
 //			userLook.setImageFileName(AMAZON_S3_PATH + userLook.getImageFileName());
 //		}else{
 //			userLook.setImageFileName(LOCAL_IMAGE_PATH + userLook.getImageFileName());
 //		}
 		
 		return ok(Json.toJson(userLook));
 	}
 	
 	public static Result getMatchUserLookByLookId(String id) throws JSONException {
 
 		List<UserLook> userLooks = UserLook.find.where().ilike("matchUserLookId", id).findList();
 		if (userLooks.size() == 0) {
 			Logger.error("[code: -4] Can't find UserLook.");
 			jsonObject = new JSONObject();
 			jsonObject.put("code", -4);
 			jsonObject.put("msg", "Can't find UserLook.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 		
 		return ok(Json.toJson(userLooks)).as("application/json");
 	}
 	
 	public static Result saveUserLook() throws JSONException {
 
 		Form<UserLook> form = new Form<UserLook>(UserLook.class)
 				.bindFromRequest();
 
 		jsonObject = new JSONObject();
 		
 		try{
 			Long lookId = Long.parseLong(request().body().asMultipartFormData().asFormUrlEncoded().get("lookId")[0]);
 			Look look = Look.find.byId(lookId);
 			if (look == null) {
 				Logger.error("[code: -3] Can't find look.");
 				jsonObject = new JSONObject();
 				jsonObject.put("code", -3);
 				jsonObject.put("msg", "Can't find look.");	
 				return ok(jsonObject.toString()).as("application/json");
 				
 			}
 			
 			UserLook userLook = form.get();
 			userLook.setLook(look);
 			userLook.setDate(Calendar.getInstance().getTime());
			look.setShotCount(look.getShotCount()+1);
			look.save();
 			
 			RequestBody request = request().body();
 			File file = request.asMultipartFormData().getFile("front").getFile();
 			File file_noFace = request.asMultipartFormData().getFile("noface").getFile();
 			File file_back = request.asMultipartFormData().getFile("back").getFile();
 			
 			//Hashing image.
 			try {
 				userLook.setImageFileName(calculateHash(MessageDigest.getInstance("MD5"), file));
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			
 			jsonObject = new JSONObject();
 			
 			fileOut(file, userLook.getImageFileName());
 			fileOut(file_noFace, userLook.getImageFileName()+"_noface");
 			fileOut(file_back, userLook.getImageFileName()+"_back");
 			
 			Coupon coupon = new Coupon();
 			coupon.setPrice(3000);
 			coupon.setUsed(false);
 			coupon.setUserlookHash(userLook.getImageFileName());
 			coupon.save();
 
 			userLook.setImageFileName(userLook.getImageFileName());
 			userLook.setImageToS3(false);
 			userLook.save();
 			
 			
 			
 
 			ImageSender imageSender = new ImageSender("localhost", userLook.getImageFileName(), "none");
 			imageSender.send();
 			imageSender.setImageFileName(userLook.getImageFileName()+"_noface");
 			imageSender.send();
 			imageSender.setImageFileName(userLook.getImageFileName()+"_back");
 			imageSender.setLookType("userLook");
 			imageSender.send();
 			
 			jsonObject.put("code", 0);
 			jsonObject.put("id", userLook.getId());
 			jsonObject.put("hash", userLook.getImageFileName());
 			jsonObject.put("msg", "ok");
 		
 		}catch(NullPointerException e){
 			e.printStackTrace();
 			Logger.error("[code: -1] Parameter error.");
 			jsonObject.put("code", -1);
 			jsonObject.put("msg", "Parameter error.");
 		
 		}catch(IllegalStateException e){
 			e.printStackTrace();
 			Logger.error("[code: -2] Parameter error.");
 			jsonObject.put("code", -1);
 			jsonObject.put("msg", "Parameter error.");
 		
 		}catch (IOException e) {
 			e.printStackTrace();
 			Logger.error("[code: -3] File upload error.");
 			jsonObject.put("code", -4);
 			jsonObject.put("msg", "File upload error");
 		
 		}catch (IndexOutOfBoundsException e) {
 			e.printStackTrace();
 			Logger.error("[code: -4] File upload error.");
 			jsonObject.put("code", -4);
 			jsonObject.put("msg", "File upload error");
 		
 		}catch (NumberFormatException e){
 			e.printStackTrace();
 			Logger.error("[code: -5] Wrong input.");
 			jsonObject.put("code", -1);
 			jsonObject.put("msg", "Wrong input.");
 		
 		}
 		
 		
 		return ok(jsonObject.toString()).as("application/json");
 	}
 
 	public static Result deleteUserLookById(Long id) throws JSONException {
 		
 		try{
 			UserLook.find.byId(id).delete();
 		}catch(NullPointerException e){
 			Logger.error("[code: -4] Can't find UserLook.");
 			jsonObject = new JSONObject();
 			jsonObject.put("code", -4);
 			jsonObject.put("msg", "Can't find UserLook.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 
 		jsonObject = new JSONObject();
 		jsonObject.put("code", 0);
 		jsonObject.put("msg", "ok");
 		return ok(jsonObject.toString()).as("application/json");
 	}
 	
 	public static Result imageToS3(String fileName){
 		UserLook userLook = UserLook.find.where().ilike("imageFileName", fileName).findUnique();
 		userLook.setImageToS3(true);
 		userLook.save();
 
 		File file = new File(LOCAL_IMAGE_PATH + userLook.getImageFileName());
 		file.delete();
 		File file_noFace = new File(LOCAL_IMAGE_PATH + userLook.getImageFileName()+"_noface");
 		file_noFace.delete();
 		File file_back = new File(LOCAL_IMAGE_PATH + userLook.getImageFileName()+"_back");
 		file_back.delete();
 		
 		
 		return ok();
 	}
 	
 	
 	
 	public static Result useCoupon() throws JSONException{
 		
 		String hash = request().body().asFormUrlEncoded().get("hash")[0];
 		Coupon coupon = Coupon.find.where().ilike("userLookHash", hash).findUnique();
 		
 		coupon.setUsed(true);
 		coupon.save();
 		
 		jsonObject.put("code", 0);
 		jsonObject.put("msg", "ok");
 		return ok(jsonObject.toString()).as("application/json");
 	}
 	
 	public static Result getCoupon(String hash){
 		
 		Coupon coupon = Coupon.find.where().ilike("userLookHash", hash).findUnique();
 		
 		return ok(Json.toJson(coupon));
 	}
 	
 	public static Result likeByLookId(Long id) throws JSONException{
 		
 		Look look = Look.find.byId(id);
 		if (look == null) {
 			Logger.error("[code: -3] Can't find look.");
 			jsonObject = new JSONObject();
 			jsonObject.put("code", -3);
 			jsonObject.put("msg", "Can't find look.");
 			return ok(jsonObject.toString()).as("application/json");
 		}
 		look.setId(look.getId()+1);
 		look.save();
 		
 		jsonObject.put("code", 0);
 		jsonObject.put("msg", "ok");
 		return ok(jsonObject.toString()).as("application/json");
 	}
 	
 	public static String calculateHash(MessageDigest algorithm, File file) throws Exception
     {
         FileInputStream fis = new FileInputStream(file);
         BufferedInputStream bis = new BufferedInputStream(fis);
         DigestInputStream dis = new DigestInputStream(bis, algorithm);
  
         // read the file and update the hash calculation
         while(dis.read() != -1) 
             ;
  
         // get the hash value as byte array
         byte[] hash = algorithm.digest();
  
         return byteArray2Hex(hash);
     }
  
     private static String byteArray2Hex(byte[] hash)
     {
         Formatter formatter = new Formatter();
         for(byte b : hash)
         {
             formatter.format("%02x", b);
         }
         return formatter.toString();
     }
     
     private static void fileOut(File file, String fileName) throws IOException{
 
 		FileChannel inChannel = new FileInputStream(file).getChannel();
 		FileChannel outChannel = new FileOutputStream(new File(LOCAL_IMAGE_PATH + "/" + fileName)).getChannel();
 		
 		ByteBuffer buf = ByteBuffer.allocate(1024);
 		
 		while(true){
 			if(inChannel.read(buf) == -1){
 				break;
 			}else{
 				buf.flip();
 				outChannel.write(buf);
 				buf.clear();
 			}
 		}
     }
 }
