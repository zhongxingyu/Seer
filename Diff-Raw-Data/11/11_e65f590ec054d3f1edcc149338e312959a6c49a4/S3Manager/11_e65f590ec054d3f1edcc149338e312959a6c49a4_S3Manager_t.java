 package models.custom_helper.file_manager;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import com.amazonaws.services.s3.model.CannedAccessControlList;
 import com.amazonaws.services.s3.model.DeleteObjectRequest;
 import com.amazonaws.services.s3.model.GetObjectRequest;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.amazonaws.services.s3.model.S3Object;
 
 import models.custom_helper.S3Plugin;
 import models.data.FileUpload;
 import net.coobird.thumbnailator.ThumbnailParameter;
 import net.coobird.thumbnailator.Thumbnails;
 import net.coobird.thumbnailator.name.Rename;
 import play.Logger;
 import play.Play;
 import play.mvc.Http.MultipartFormData.FilePart;
 
 public class S3Manager implements FileManagerInterface {
 
 	private final String BASE_PATH=Play.application().configuration().getString("upload.base_path"); //path sebenernya
 	private final String BASE_URL_PATH=Play.application().configuration().getString("upload.base_url_path"); //path buat request
 	private final String PROFILE=Play.application().configuration().getString("upload.profile");
 	private final String ADS=Play.application().configuration().getString("upload.ads");
 	private final String TRANSFER=Play.application().configuration().getString("upload.transfer");
 	private final String OTHER =Play.application().configuration().getString("upload.other");
 	
 	private final String THUMBNAIL=Play.application().configuration().getString("upload.s3thumbnail");
 	private final String THUMBNAIL_PREFIX="thumb";
 	private final String BUCKET=Play.application().configuration().getString(S3Plugin.AWS_S3_BUCKET);
 	private final String END_POINT=Play.application().configuration().getString(S3Plugin.AWS_ENDPOINT);
 	private final  String APP_PATH=END_POINT+BUCKET;
 	
 
 	public FileUpload saveNew(FilePart part, SaveToEnum saveTo){
 		String path = getSavePath(saveTo);
 		String fileName = part.getFilename()
 							  .replace(" ", "")
 							  .replace("[", "")
 							  .replace("]", "")
 							  .replace("{", "")
 							  .replace("}", "");
 		FileUpload upload=new FileUpload();
 		upload.setName(fileName);
 		upload.setPath(path);
 		upload.setUrl_path(path);
 		upload.save();
 		
 		String uploadPath=path.substring(1, path.length()); //Fix bug
 		String contentType = part.getContentType(); 
 		File file = part.getFile();
 		
 		String save_name=upload.getId()+fileName; //nama yang disave adalah id+nama file asli
 		Logger.debug("Output untuk file thumbnai " + path);
 
 		
 		if (S3Plugin.amazonS3 == null) {
             Logger.error("Tidak bisa menyimpan karena storage tidak siap");
             throw new RuntimeException("Could not save");
         }else{
         	PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET, uploadPath+save_name, file);
             putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
             S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
             GetObjectRequest request=new GetObjectRequest(BUCKET, uploadPath+save_name);
             S3Object object =S3Plugin.amazonS3.getObject(request);
             if(object==null) throw new RuntimeException(); 
         }
 		return upload;
 	}
 	public boolean delete(int id) {
 		FileUpload upload=FileUpload.find.byId(id);
 		try {
			String deletePath=upload.getPath().substring(1,upload.getPath().length());
             DeleteObjectRequest request=new DeleteObjectRequest(BUCKET, 
            											  deletePath+
             											  upload.getId()+
             											  upload.getName());
             DeleteObjectRequest requestThumbnail=new DeleteObjectRequest(BUCKET,  
            											  deletePath
            											  +THUMBNAIL+
             											  THUMBNAIL_PREFIX+
             											  upload.getId()+
             											  upload.getName());
             S3Plugin.amazonS3.deleteObject(requestThumbnail);
             S3Plugin.amazonS3.deleteObject(request);
            Logger.debug("Delete object "+ request.getKey());
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public String getThumbnailURL(int id) {
 		FileUpload file=FileUpload.find.byId(id);
 		return APP_PATH+file.getPath()+THUMBNAIL+THUMBNAIL_PREFIX+file.getId()+file.getName();
 			   	
 	}	
 	//save thumbnail untuk kontent tipe gambar
 	public boolean saveThumbnail(int id){
 		try {
 			FileUpload file=FileUpload.find.byId(id);
 			String output = file.getPath().substring(1,file.getPath().length())
 							+THUMBNAIL+
 							THUMBNAIL_PREFIX+
 							file.getId()+file.getName();
 			Logger.debug("Output untuk thumbnai " + output);
 			File result=new File("temp");
 			if(result.exists()){
 				result.delete();
 			}
 			URL url=new URL(getFileUrl(file));
 			InputStream input=url.openStream();
 			OutputStream outputStream=new FileOutputStream(result);
 			Logger.debug("Input untuk thumbnai " + url.toString());
 
 			int read = 0;
 			byte[] bytes = new byte[1024];
 			
 			Logger.debug("Downloading file " + url.toString());
 			while ((read = input.read(bytes)) != -1) {
 					outputStream.write(bytes, 0, read);
 				}
 			
 			File outFile = new File("output.jpg"); 
 			Thumbnails.of(result).width(60).height(60).toFile(outFile);
 			if (S3Plugin.amazonS3 == null) {
 	            Logger.error("Tidak bisa menyimpan karena storage tidak siap");
 	            throw new RuntimeException("Could not save");
 	        }else{
 	        	PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET, output, outFile);
 	            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
 	            S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
 	            GetObjectRequest request=new GetObjectRequest(BUCKET, output);
 	            S3Object object =S3Plugin.amazonS3.getObject(request);
 	            if(object==null) throw new RuntimeException(); 
 	            outFile.delete();
 	        }
 			
 		} catch (MalformedURLException e1) {
 			e1.printStackTrace();
 			return false;
 		} catch(IOException io){
 			io.printStackTrace();
 			return false;
 		}catch(Exception e){
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}	
 	public String getFileUrl(int id){
 		FileUpload upload=FileUpload.find.byId(id);
 		return APP_PATH+
 				upload.getPath()
 				+upload.getId()
 				+upload.getName();
 	
 	}
 	public String getFileUrl(FileUpload upload){
 		return APP_PATH+
 				upload.getPath()+
 				upload.getId()+
 				upload.getName();
 	
 	}
 	private String getSavePath(SaveToEnum enume){
 		switch (enume) {
 		case ADS_FILE		   : return BASE_PATH+ADS;
 		case PROFILE_PICTURE   : return BASE_PATH+PROFILE;
 		case OTHER			   : return BASE_PATH+OTHER;
 		case TRANSFER_EVIDENCE : return BASE_PATH+TRANSFER;
 		default:
 			return BASE_PATH+"MISC";
 		}
 	}
 	@Override
 	public String getThumbnailFullPath(int id) {
 		return null;
 	}
 }
