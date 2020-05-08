 package controllers;
 
 import models.*;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 import play.Logger;
 import play.libs.Json;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 
 import javax.xml.bind.DatatypeConverter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import static java.net.URLConnection.guessContentTypeFromStream;
 
 public class PhotosREST extends Controller {
 
     //{"id":"5175a1253cdbf70ca1164d10","date_created":123,"owner_id":"5151c9c03609e0d8112d9a5a","title":"this title","description":"ciao","is_useful_count":0,"is_beautiful_count":0,"post_content_location":"/rest/photos/5175a1253cdbf70ca1164d10/content","get_content_location":"/rest/photos/5175a1253cdbf70ca1164d10/content"}
 
     //json mapping for Photo
     public static final String DATE_CREATED = "date_created";
     public static final String OWNER_ID = "owner_id";
     public static final String ID = "id";
     public static final String LATITUDE = "latitude";
     public static final String LONGITUDE = "longitude";
     public static final String DESCRIPTION = "description";
     public static final String TITLE = "title";
     public static final String CONTENT = "content";
     public static final String IS_USEFUL_COUNT = "is_useful_count";
     public static final String IS_BEAUTIFUL_COUNT = "is_beautiful_count";
     //generated in output, are not inherent properties of the object
     public static final String GET_PHOTO_CONTENT_LOCATION = "get_content_location";
     public static final String POST_PHOTO_CONTENT_LOCATION = "post_content_location";
 
     //json mapping for PhotoUserLike
     public static final String USER_ID = "user_id";
     public static final String PHOTO_IS_BEAUTIFUL = "is_beautiful";
     public static final String PHOTO_IS_USEFUL = "is_useful";
 
     //max size of a photo uploaded in json = 2MB (base64 encoded, so it's like 6/8 x 2MB)
     public static final int MAX_BASE64_UPLOAD_SIZE = 2 * 1024 * 1024;
     //max size of a photo uploaded via multipart form = 6MB, max size is 16Mb due to Mongo. Use GridFS if need more.
     public static final int MAX_MULTIPART_UPLOAD_SIZE = 6 * 1024 * 1024;
     //json attribute that specify the pagination params
     public static final String RESULTS_RETURNED = "results";
     public static final String RESULTS_OFFSET = "offset";
     public static final String RESULTS_LIMIT = "limit";
     private static final String RESULTS_HAS_NEXT = "has_next";
     private static final String PHOTO_IS_SEARCHABLE = "is_searchable";
     private static final String ALTERNATE_CONTENTS = "alternate_contents";
 
     @BodyParser.Of(value = BodyParser.MultipartFormData.class,
             maxLength = MAX_MULTIPART_UPLOAD_SIZE)
     public static Result uploadMultipartContent(String id){
 
 //        if (request.headers.get("type").value().equals("multipart/form-data")){
 //            return ok("file received from user: "+ request().username() + "; " + CONTENT_TYPE + "");
 //        }
 
         MultipartFormData body = request().body().asMultipartFormData();
 
 
         if(body == null) {
             return badRequest("Expecting multipart/form-data request body");
         }
 
         Logger.info("multipart file received");
 
 
         ObjectId objId = null;
         try {
             objId = stringToObjectId(id);
         } catch (IllegalArgumentException e) {
             badRequest(e.getMessage());
         }
 
         Photo photo = Photo.findById(objId);
 
         if(photo == null){
             return notFound("error while uploading: photo with id '" + id + "' was not found");
         }
 
         //verify the user is the ower of the photo
         User user = Auth.getRequestingUser();
         if(user == null){
             return badRequest("user not recognized");
         }
         if(!photo.getOwnerId().equals(user.id)){
             return unauthorized("not allowed to update the photo");
         }
 
 
 
         FilePart picture;
 
         //let's take just one file, the first, ignoring any other potentially there
         picture = body.getFiles().get(0);
 
         File f = picture.getFile();
 
         if(f == null) {
             return badRequest("no file found in request");
         }
 
         Logger.info("file uploading: " + f + picture.getContentType());
         String mimeType = picture.getContentType();
         if(mimeType == null || !mimeType.substring(0,6).toLowerCase().equals("image/")){
             return badRequest("sorry, the file was not recognized as an image, it looks: " + mimeType);
         }
         Logger.info("filetype was declared and stored as : " + mimeType);
         try {
             photo.addUpdateContent(f, picture.getContentType());
             photo.save();
             //once the photo is saved, we can launch the independent creation of thumbnails and various sizes
             photo.createAndSaveMultipleResizedContents_async();
         } catch (IOException e) {
             return badRequest("error in file content");
         }
 
 
         return created("file uploaded for picture '" + id + "' type: " + picture.getContentType());
 
     }
 
     private static ObjectId stringToObjectId(String id) {
         return id != null ? new ObjectId(id) : null;
     }
 
     public static Result deletePhoto(String id){
 
         User user = Auth.getRequestingUser();
 
         if(user == null){
             return badRequest("user not recognized or not logged in");
         }
 
         ObjectId obj = stringToObjectId(id);
         Photo photo = Photo.findById(obj);
 
         if(photo == null){
             return notFound("photo with id '" + id + "' was not found");
         }
 
         if(!photo.getOwnerId().equals(user.id)){
             return unauthorized("not allowed to delete the photo");
         }
 
         //delete any associated photoUserLike
         Photo.deleteUserLikesForPhoto(photo);
 
         photo.delete();
 
         return noContent();
 
     }
 
 
     @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_BASE64_UPLOAD_SIZE)
     public static Result newPhoto(){
 
         Photo photo;
         JsonNode json;
         try {
             json = request().body().asJson();
             photo =  jsonToPhoto(json);
         } catch (IllegalArgumentException e) {
             return badRequest(e.getMessage());
         } catch (Exception e) {
             Logger.error("error parsing jsonToPhoto, error message: " + e.getMessage());
             return badRequest("error parsing json to Photo or request total size exceeding " + MAX_BASE64_UPLOAD_SIZE + " bytes");
         }
 
         User user = Auth.getRequestingUser();
 
         if(user == null){
             return badRequest("user not recognized");
         }
 
 
         //requesting user is owner
         photo.setOwnerId(user.id);
         //blank out the photo id if any, it is a new photo, not an update
         photo.setId(null);
 
 
         if(json.has(CONTENT)){
             try{
                 String contentBase64String = json.findPath(CONTENT).getTextValue();
                 byte[] byteContent = extractBase64Content(contentBase64String);
                 String contentType = extractMimeImageContentType(byteContent);
                 photo.addUpdateContent(byteContent, contentType);
             } catch (Exception e){
                 return badRequest("error in " + CONTENT + ": " + e.getMessage());
             }
         }
 
         //get the newly created id
         String id = photo.save().toString();
         
         //if we uploaded content, and once the photo is saved,
         // we can launch the independent creation of thumbnails and various sizes
         if(json.has(CONTENT)){
             photo.createAndSaveMultipleResizedContents_async();
         }
 
         response().setHeader(
                 CONTENT_LOCATION,
                 routes.PhotosREST.getPhoto(id).toString());
         return created(photoToJson(photo));
 
     }
 
 
     private static Photo updatePhotoFromJson(Photo photo, JsonNode json) {
         if(json.has(LATITUDE)) photo.setLatitude(new Double(json.findPath(LATITUDE).getDoubleValue()));
         if(json.has(LONGITUDE)) photo.setLongitude(new Double(json.findPath(LONGITUDE).getDoubleValue()));
         if(json.has(DATE_CREATED)) photo.setCreated(new Date(json.findPath(DATE_CREATED).getLongValue()));
         if(json.has(OWNER_ID)) photo.setOwnerId(stringToObjectId(json.findPath(OWNER_ID).getTextValue()));
         if(json.has(TITLE)) photo.setTitle(json.findPath(TITLE).getTextValue());
         if(json.has(DESCRIPTION)) photo.setDescription(json.findPath(DESCRIPTION).getTextValue());
         if(json.has(ID)) photo.setId(stringToObjectId(json.findPath(ID).getTextValue()));
         if(json.has(PHOTO_IS_SEARCHABLE)) photo.setSearchable(json.findPath(PHOTO_IS_SEARCHABLE).getBooleanValue());
         return photo;
     }
 
 
     private static Photo jsonToPhoto(JsonNode json){
 
         Photo photo = new Photo();
         return updatePhotoFromJson(photo, json);
     }
 
 
     private static JsonNode photoToJson(Photo photo){
         ObjectNode json = Json.newObject();
         if(photo.getId() != null) json.put(ID, photo.getId().toString());
         if(photo.getCreated() != null) json.put(DATE_CREATED, photo.getCreated().getTime());
         if(photo.getOwnerId() != null) json.put(OWNER_ID, photo.getOwnerId().toString());
         if(photo.getLatitude() != null) json.put(LATITUDE, photo.getLatitude());
         if(photo.getLongitude() != null) json.put(LONGITUDE, photo.getLongitude());
         if(photo.getTitle() != null) json.put(TITLE, photo.getTitle());
         if(photo.getDescription() != null) json.put(DESCRIPTION, photo.getDescription());
 
         if(photo.getUsefulCount() != 0) json.put(IS_USEFUL_COUNT, photo.getUsefulCount());
         if(photo.getBeautifulCount() != 0) json.put(IS_BEAUTIFUL_COUNT, photo.getBeautifulCount());
 
         json.put(PHOTO_IS_SEARCHABLE, photo.isSearchable());
 
 
         //if the photo is already persisted (as it normally would)
         //generate the location url that points to its content
         ObjectId objectId = photo.getId();
         if(objectId != null){
             json.put(POST_PHOTO_CONTENT_LOCATION, routes.PhotosREST.uploadMultipartContent(objectId.toString()).toString());
             if(photo.getPhotoContents().size() > 0){
                 json.put(GET_PHOTO_CONTENT_LOCATION, routes.PhotosREST.getPhotoContent(objectId.toString(), 0).toString());
             }
         }
 
         //are there alternative contents?
         int noOfContents = photo.getPhotoContents().size();
         if(noOfContents > 1){
             ObjectNode alternateContentsJson = Json.newObject();
             for(int i = 0; i < noOfContents - 1; i++){
                 ObjectNode altJson = Json.newObject();
                 altJson.put("index", i + 1);
                 altJson.put("max_side", Photo.Content.RESIZE_SIZES.get(i));
                 altJson.put(
                         "content",
                                 routes.PhotosREST.getPhotoContent(objectId.toString(), i + 1).toString()
                         );
                 //creates a base64 representation
 //                altJson.put("bytes",
 //                        "data:" + photo.getPhotoContents().get(1).getMimeType() + ";base64," +
 //                        DatatypeConverter.printBase64Binary(photo.getPhotoContents().get(1).getFileBytes()));
 
                 alternateContentsJson.put(Photo.Content.RESIZE_NAMES.get(i), altJson);
             }
 
             json.put(ALTERNATE_CONTENTS, alternateContentsJson);
         }
 
         return json;
     }
 
 
     /**
      * returns the content of the photo
      * @param id id of the photo
      * @param alternate alternate version (size-wise) of the photo. By default is 0, that means
      *                  'original size'. 1 is the smallest resized version, and numbers up mean a larger size
      *                  according to the pixels listed in {@code Photo.Content.RESIZE_SIZES}. Note that at a given
      *                  point the requested size might not exist, only 0 is guaranteed to be present if the content
      *                  was uploaded.
      *
      * @return the corresponding version of the photo if it exists
      */
     public static Result getPhotoContent(String id, Integer alternate){
 
         ObjectId obj;
         try {
             obj = stringToObjectId(id);;
         } catch (IllegalArgumentException e) {
             return badRequest(e.getMessage());
         }
         Photo photo = Photo.findById(obj);
 
         if(photo == null){
             return notFound("photo with id '" + id + "' was not found");
         }
 
         ByteArrayInputStream inStream = null;
         //returns the first one
         //TODO: it will return the proper Content when handling multiple photo sizes (quality)
 
         int alternativeSizes = photo.getPhotoContents().size() - 1;
         if(alternate > alternativeSizes){
             return notFound("photo content for alternate image '"+ alternate + "' is missing. "
                     + alternativeSizes + " alternate vesions only were found.");
         }
 
         Photo.Content c = photo.getPhotoContents().get(alternate);
         response().setContentType(c.getMimeType());
         //response().setHeader("Content-Disposition", "attachment; filename=FILENAME");
         //response().setHeader(ETAG, "xxx");
         //ByteArrayInputStream bais = new ByteArrayInputStream(c.getFileBytes());
         Logger.info("found photo " + c.getId().toString());
         inStream = new ByteArrayInputStream(c.getFileBytes());
 
 
         if(inStream == null){
             return notFound("photo content is missing");
         }
         return ok(inStream);
     }
 
 
     public static Result getPhoto(String id){
 
         ObjectId obj;
         try {
             obj = stringToObjectId(id);
         } catch (IllegalArgumentException e) {
             return badRequest(e.getMessage());
         }
         Photo photo = Photo.findById(obj);
         if(photo == null){
             return notFound("photo with id '" + id + "' was not found");
         }
         return ok(photoToJson(Photo.findById(obj)));
     }
 
     @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_BASE64_UPLOAD_SIZE)
     public static Result updatePhoto(String id){
         User user = Auth.getRequestingUser();
 
         if(user == null){
             return badRequest("user not recognized");
         }
 
         ObjectId objId = null;
         try {
             objId = stringToObjectId(id);
         } catch (IllegalArgumentException e) {
             badRequest(e.getMessage());
         }
 
         Photo photo = Photo.findById(objId);
 
         if(photo == null) {
             return notFound("error while uploading: photo with id '" + id + "' was not found");
         }
 
         if(!photo.getOwnerId().equals(user.id)){
             return unauthorized("not allowed to update the photo");
         }
 
         JsonNode json;
         try {
             json = request().body().asJson();
             photo = updatePhotoFromJson(photo, json);
         } catch (IllegalArgumentException e) {
             return badRequest(e.getMessage());
         } catch (Exception e) {
             Logger.error("error parsing jsonToPhoto, error message: " + e.getMessage());
             return badRequest("error parsing json to Photo or request total size exceeding " + MAX_BASE64_UPLOAD_SIZE + " bytes");
         }
 
         if(json.has(CONTENT)){
             try{
                 String contentBase64String = json.findPath(CONTENT).getTextValue();
                 byte[] byteContent = extractBase64Content(contentBase64String);
                 String contentType = extractMimeImageContentType(byteContent);
                 photo.addUpdateContent(byteContent, contentType);
             } catch (Exception e){
                 return badRequest("error in " + CONTENT + ": " + e.getMessage());
             }
         }
 
         photo.save();
 
         //if we uploaded content, and once the photo is saved,
         // we can launch the independent creation of thumbnails and various sizes
         if(json.has(CONTENT)){
             photo.createAndSaveMultipleResizedContents_async();
         }
 
         //return the updated representation
         return ok(photoToJson(photo));
     }
 
     public static Result getPhotosByRectangle(Double x1, Double x2, Double y1, Double y2, Integer offset, Integer limit){
 
         //rect;x=39.001409,-84.578201;y=39.001409,-84.578201;
 
         if(limit.intValue() > Photo.MAX_RESULTS_RETURNED){
             return badRequest("can't request more than " + Photo.MAX_RESULTS_RETURNED + " results");
         }
 
         if(x1 == null ||
             x2 == null ||
             y1 == null ||
             y2 == null)
         {
             return badRequest("all four point of the rectangle must be specified");
         }
 
         if(offset < 0){
             offset = 0;
         }
 
         /*
         //adjust coordinate overflow
         if(x2 > 180){
             x2 -= 180;
         }
         if(x1 > 180){
             x1 -= 180;
         }
         if(y2 > 90){
             y2 -= 90;
         }
         if(y1 > 90){
             y1 -= 90;
         }
         */
 
         Double[][][] rect = {{
                 {x1, y1},
                 {x1, y2},
                 {x2, y2},
                 {x2, y1}
         }};
 
         //tag search is not fully implemented, so we are passing them as null
         List<Photo> photos = Photo.findByPoligonAndTags(rect, limit.intValue() + 1, offset, null);
 
         Logger.info("found " + photos.size() + " photo(s) in rect");
         ObjectNode json = Json.newObject();
 
         ArrayNode arrayNode = json.putArray(RESULTS_RETURNED);
 
         for(int i = 0; i < limit && i < photos.size(); i++){
             Photo p = photos.get(i);
             arrayNode.add(photoToJson(p));
         }
         json.put(RESULTS_OFFSET, offset);
         json.put(RESULTS_LIMIT, limit.intValue());
         json.put(RESULTS_HAS_NEXT, photos.size() > limit.intValue() ? true : false);
 
         return ok(json);
 
 
     }
     private static String extractMimeImageContentType(byte[] bytes) {
 
         String type;
 
         try {
             ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
             type = guessContentTypeFromStream(stream);
         } catch (IOException e) {
             Logger.error("extractMimeImageContentType: unable to convert byte[] to stream");
             return "";
         }
 
         Logger.info("recognized filetype: " + type);
 
         if(type == null || !type.substring(0,6).toLowerCase().equals("image/")){
             throw new IllegalArgumentException("sorry, the " + CONTENT + " was not recognized as an image, it looks: " + type);
         }
 
         return type;
     }
 
     private static byte[] extractBase64Content(String contentBase64String) {
 
         if(contentBase64String == null || contentBase64String.equals("")){
             throw new IllegalArgumentException(CONTENT + " is empty");
         }
 
 
         //the first characters will look like this; strip them off the content
         //data:image/png;base64,
         int contentIndex = contentBase64String.indexOf("base64") + 7;
 
         if(contentIndex < 7){
             throw new IllegalArgumentException("error parsing " + CONTENT + ", no 'base64' string found");
         }
 
         byte[] contentBytes = DatatypeConverter.parseBase64Binary(
                 contentBase64String.substring(contentIndex)
         );
         if(contentBytes == null || contentBytes.length==0){
             throw new IllegalArgumentException(CONTENT + "payload seems empty");
         }
 
         return contentBytes;
 
     }
 
 
 
 
 
     /**
      * Returns a list of 'likes' for the photo, voted by the specified user.
      * The results are limited in number by
      *
      * @param photoId identifier of the photo
      * @param userId indentifier of the user;
      *               optional, if is null the query returns the results irrespective of the user
      * @param offset offset in the resultset
      * @param limit limit of number of results. Cannot be higher than PhotoUserLike.MAX_RESULTS_RETURNED
      * @return list of photoUserLikes in json
      */
 
     public static Result getPhotoUserLikes(String photoId, String userId, Integer offset, Integer limit){
 
 
         if(limit.intValue() > PhotoUserLike.MAX_RESULTS_RETURNED){
             return badRequest("can't request more than " + PhotoUserLike.MAX_RESULTS_RETURNED + " results");
         }
         if(offset < 0){
             offset = 0;
         }
 
         ObjectId photoObjId;
         try {
             photoObjId = stringToObjectId(photoId);;
         } catch (IllegalArgumentException e) {
             return badRequest(e.getMessage());
         }
 
 
         ObjectId userObjId = null;
         if(userId!=null){
             try {
                 userObjId = stringToObjectId(userId);;
             } catch (IllegalArgumentException e) {
                 return badRequest(e.getMessage());
             }
         }
 
         PhotoUserLike photoUserLike = null;
         {
             //request one result more than limit, to see if there are more results
             List<PhotoUserLike> userLikesList = PhotoUserLike
                     .getFromPhotoAndUser(
                             photoObjId, userObjId, offset, limit.intValue() + 1);
 
             //results found and user was not specified: return a list
             if(userObjId == null  && userLikesList.size() > 0){
 
                 //ObjectNode json = Json.newObject();
                 Logger.info("found " + userLikesList.size() + " likes");
                 ObjectNode json = Json.newObject();
 
                 ArrayNode arrayNode = json.putArray(RESULTS_RETURNED);
                 for(int i = 0; i < limit.intValue() && i < userLikesList.size(); i++){
                     PhotoUserLike ul = userLikesList.get(i);
                     Logger.info("user like currently is " + ul.getId().toString());
                     Logger.info("user in like currently is " + ul.getUserId().toString());
 
                     arrayNode.add(userLikeToJson(ul));
                 }
                 json.put(RESULTS_OFFSET, offset);
                 json.put(RESULTS_LIMIT, limit.intValue());
                 json.put(RESULTS_HAS_NEXT, userLikesList.size() > limit.intValue() ? true : false);
                 return ok(json);
 
             }
             //a user was specified and a result was found, so return the (single) result
             if(userObjId != null && userLikesList.size() > 0 ){
                 return ok(userLikeToJson(userLikesList.get(0)));
             }
 
 
         }
         return notFound("no 'photo like' found");
 
     }
 
     @BodyParser.Of(value = BodyParser.Json.class)
     public static Result setPhotoUserLike(String photoId){
 
         ObjectId photoObjId;
         try {
             photoObjId = stringToObjectId(photoId);;
         } catch (IllegalArgumentException e) {
             return badRequest(e.getMessage());
         }
 
         PhotoUserLike userLike;
         JsonNode json;
         try {
             json = request().body().asJson();
             userLike =  jsonToPhotoUserLike(json);
         } catch (IllegalArgumentException e) {
             return badRequest(e.getMessage());
         } catch (Exception e) {
             Logger.error("error parsing jsonToPhotoUserLike, error message: " + e.getMessage());
             return badRequest("error parsing json");
         }
 
         User user = Auth.getRequestingUser();
         if(user == null){
             return badRequest("user not recognized");
         }
 
 
         //complete the details of the photoLike object
 
         //if the user id is not declared in the json, set it from the authenticated
         if(userLike.getUserId() == null){
             userLike.setUserId(user.id);
         }else{
             //if it is declared instead, make sure it is equal to the one currently authenticated
             if(!userLike.getUserId().equals(user.id)){
                 return unauthorized("not allowed to set like/unlike with that " + USER_ID);
             }
         }
 
         //check that the photo actually exists
         Photo photo;
         photo = Photo.findById(photoObjId);
         if(photo == null){
             return notFound("photo '" + userLike.getPhotoId().toString() + "' not found");
         }
         userLike.setPhotoId(photoObjId);
 
         if(photo.getOwnerId().equals(userLike.getUserId())){
             return badRequest("don't cheat! you can't vote for your own photo :) ");
         }
 
         //retrieve any vote that the user has already given
         PhotoUserLike oldUserLike = null;
         {
             List<PhotoUserLike> userLikesList = PhotoUserLike.getFromPhotoAndUser(
                     userLike.getPhotoId(),
                     userLike.getUserId(), 1, 1); //pagination params: get first with limit 1
             if(userLikesList!=null && userLikesList.size() > 0){
                 oldUserLike = userLikesList.get(0);
             }
         }
 
         boolean photoBeautiful = userLike.getPhotoIsBeautiful();
         boolean photoUseful = userLike.getPhotoIsUseful();
 
         //these are the values already set by the user in the past, if any
         boolean oldPhotoBeautiful = false;
         boolean oldPhotoUseful = false;
 
         Logger.info("received 'is beautiful' with value: " + photoBeautiful);
         Logger.info("received 'is useful' with value: " + photoUseful);
 
         //if there is an older photo 'like', and is different from the current one, update it, otherwise create a new one
         if (oldUserLike != null){
             oldPhotoBeautiful = oldUserLike.getPhotoIsBeautiful();
             oldPhotoUseful = oldUserLike.getPhotoIsUseful();
             //'old' is different from 'new'? update the old then!
             if((oldPhotoBeautiful != photoBeautiful || oldPhotoUseful != photoUseful)){
                 oldUserLike.setPhotoIsBeautiful(photoBeautiful);
                 oldUserLike.setPhotoIsUseful(photoUseful);
                 oldUserLike.save();
                 Logger.info("previously was 'useful/beautiful': " + oldPhotoUseful + oldPhotoBeautiful);
                 Logger.info("updated with 'useful/beautiful': " + photoUseful + photoBeautiful);
             }
         }else{
             //just create from scratch
             userLike.save();
         }
 
 
         {
             //increment the counters in the photo, but just if there is something different
 
             int incrBeautiful = 0;
             int incrUseful = 0;
 
             if(photoUseful != oldPhotoUseful){
                 incrUseful = (photoUseful ? 1 : -1);
                 Logger.info("incremented 'useful' by: " + (photoUseful ? 1 : -1));
             }
             if(photoBeautiful != oldPhotoBeautiful){
                 incrBeautiful = (photoBeautiful ? 1 : -1);
                 Logger.info("incremented 'beautiful' by: " + (photoBeautiful ? 1 : -1));
             }
 
             if(incrBeautiful != 0 || incrUseful != 0){
                 //increment counter on user photo
                 photo.incrementCountersAndSave(incrUseful, incrBeautiful);
                 //increment statistics for the user
                updateUserStatisticsAndAwards(photo.getOwnerId(), incrUseful + incrBeautiful);
 
             }
         }
 
         response().setHeader(
                 CONTENT_LOCATION,
                 routes.PhotosREST.getPhotoUserLikes(
                         photo.getId().toString(),
                         userLike.getUserId().toString(), 0, 1
                 ).toString()
         );
 
         return ok(userLikeToJson(userLike));
     }
 
 
     private static JsonNode userLikeToJson(PhotoUserLike userLike) {
         ObjectNode json = Json.newObject();
         json.put(USER_ID, userLike.getUserId().toString());
 
         if(userLike.getPhotoIsUseful() != null){
             json.put(PHOTO_IS_USEFUL, userLike.getPhotoIsUseful() ? 1 : 0);
         }
         if(userLike.getPhotoIsBeautiful() != null){
             json.put(PHOTO_IS_BEAUTIFUL, userLike.getPhotoIsBeautiful() ? 1 : 0);
         }
 
         return json;
     }
 
     private static PhotoUserLike jsonToPhotoUserLike(JsonNode json) {
 
         PhotoUserLike pul = new PhotoUserLike();
 
         if(json.has(USER_ID)){
             String userId = json.findPath(USER_ID).getTextValue();
             pul.setUserId(stringToObjectId(userId));
         }
 
         //is not beautiful is value sent is 0, if values is not 0 then is beautiful
         if(json.has(PHOTO_IS_BEAUTIFUL)){
             boolean beautiful = json.findPath(PHOTO_IS_BEAUTIFUL).getDoubleValue() == 0.0 ? false : true;
             pul.setPhotoIsBeautiful(beautiful);
         }
 
         if(json.has(PHOTO_IS_USEFUL)){
             boolean useful = json.findPath(PHOTO_IS_USEFUL).getDoubleValue() == 0.0 ? false : true;
             pul.setPhotoIsUseful(useful);
         }
 
         return pul;
     }
 
 
    private static void updateUserStatisticsAndAwards(ObjectId userId, int photoLikesIncrement) {
         //increment counter on user statistics
        UserStatistics userStatistics = UserStatistics.findByUserId(userId.toString());
         if(userStatistics == null){
            userStatistics = UserStatistics.init(userId.toString());
             //needed for the following update
             userStatistics.save();
         }
 
         //updates the saved value
         userStatistics.updateStatistic(
                 StatisticTypes.PHOTOLIKES.toString(),
                 photoLikesIncrement);
         userStatistics.update();
         Logger.info("incremented statistic '" + StatisticTypes.PHOTOLIKES.toString()
                 + "' by " + photoLikesIncrement);
     }
 
     private static class Auth {
 
         private static final String QUERYSTRING_USERID = "user_email";
 
         private static User getRequestingUser(){
 
             User user = null;
             String userEmail = request().getQueryString(QUERYSTRING_USERID);
 
             Logger.info("user sent querystring with '" + QUERYSTRING_USERID + "' = " + userEmail);
 
             if(userEmail != null && !"".equals(userEmail)){
                 try {
                     return User.findByEmail(userEmail);
                 } catch (Exception e) {
                     Logger.info("unable to find such user");
                     return null;
                 }
             }
             Logger.info("play authenticate...");
             user = Application.getLocalUser(session());
             if(user == null){
                 Logger.info("user not found");
             }
             return user;
 
         }
     }
 
     // Quick REST service to retrieve just all the likes for a photo
     public static Result getPhotoLikeStats(String photoId) {
         JsonNode json = PhotoUserLike.getPhotoStats(photoId);
         if (json == null || json.isNull())
             return badRequest();
         else
             return ok(json);
     }
 
 }
