 package org.mdissjava.mdisscore.view.photo;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 import org.mdissjava.commonutils.photo.status.PhotoStatusManager;
 import org.mdissjava.commonutils.properties.PropertiesFacade;
 import org.mdissjava.mdisscore.controller.bll.impl.PhotoManagerImpl;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.model.pojo.Album;
 import org.mdissjava.mdisscore.model.pojo.Photo;
 import org.mdissjava.mdisscore.view.params.ParamsBean;
 
 import com.google.code.morphia.Datastore;
 
 @RequestScoped
 @ManagedBean
 public class PhotoDetailsBean {
 	
 	private String photoId;
 	private String userNick;
 	
 	private List<String> defaultPhotoSizes;
 	private List<String> thumbnailIds;
 	private String thumbnailBucket;
 	private String thumbnailDatabase;
 	
 	private String detailedPhotoURL;
 	
 	private Photo photo;
 	
 	private final String GLOBAL_PROPS_KEY = "globals";
 	private final String MORPHIA_DATABASE_KEY = "morphia.db";
 	private final String RESOLUTIONS_PROPS_KEY = "resolutions";
 	
 	
 	public PhotoDetailsBean() {
 		ParamsBean pb = getPrettyfacesParams();
 		this.userNick = pb.getUserId();
 		this.photoId = pb.getPhotoId();
 		
 		//TODO: check if isn't detailed to redirect to /user/xxx/upload/details/yyy-yyyyyy-yyyy-yyy
 		
 		try {
 			//get morphia database from properties and load the photo by its id
 			String database;
 			PropertiesFacade propertiesFacade = new PropertiesFacade();
 			database = propertiesFacade.getProperties(GLOBAL_PROPS_KEY).getProperty(MORPHIA_DATABASE_KEY);
 		
 			Datastore datastore = MorphiaDatastoreFactory.getDatastore(database);
 			PhotoManagerImpl photoManager = new PhotoManagerImpl(datastore);
 			
 			this.photo = photoManager.searchPhotoUniqueUtil(photoId);
 			
 			
 			//search the available sizes for this photo
 			//int sizes[] = {100, 240, 320, 500, 640, 800, 1024}; //our different sizes
 			Properties allResolutions = propertiesFacade.getProperties(RESOLUTIONS_PROPS_KEY);
 			
 			this.defaultPhotoSizes = new ArrayList<String>();
 			//set the size
 			int height = this.photo.getMetadata().getResolutionREAL().getHeight();
 			int width = this.photo.getMetadata().getResolutionREAL().getWidth();
 			int photoSize = height > width ? height: width;
 			
 			System.out.println(photoSize);
 			
 			// we get all the available resolutions
 			@SuppressWarnings("rawtypes")
 			Enumeration resolutions = allResolutions.keys();
 			
 			String key;
 			//for each one we check if is scalar one and not square and if is smaller than the photo
 			while(resolutions.hasMoreElements())
 			{
 				key = (String)resolutions.nextElement();
 				
 				//Only needed the scale ones, not the squares
 				if (allResolutions.getProperty(key).contains("scale"))
 				{
 					//if is bigger than our photo size then don't add to the list of available sizes for this photo
 					if (photoSize >= Integer.valueOf(key))
 					{
 						this.defaultPhotoSizes.add(key);
 					}
 				}
 					
 			}
 			
			// we want to know wich is the best photo for the display of the detail. 
 			// Max is 640px but some photos are smaller than 640px so we set the original size
 			//and if the photo is bigger than 640 then set the size to 640
 			//get the database of the photos and create the url with the appropiate image
 			
 			String bucket;
 			String bucketPropertyKey = null;
 			if(photoSize >= 500)//500px size
 			{
 				bucketPropertyKey = "thumbnail.scale.500px.bucket.name";
 				bucket = propertiesFacade.getProperties("thumbnails").getProperty(bucketPropertyKey);
 			}
 			else//original size
 			{
 				bucketPropertyKey = "images.bucket";
 				bucket = propertiesFacade.getProperties("globals").getProperty(bucketPropertyKey);
 			}
 			
 			database = propertiesFacade.getProperties("globals").getProperty("images.db");
 			this.detailedPhotoURL = "/dynamic/image?db="+database+"&amp;bucket="+bucket+"&amp;id="+this.photoId;
 			
 			
 			//set the album thumbnails identifiers and necessary data
 			this.thumbnailIds= new ArrayList<String>();
 			Album album = this.photo.getAlbum();
 			this.thumbnailBucket = "square.75";
 			this.thumbnailDatabase = database;
 			
 			for (Photo i: album.getPhotos())
 			{
 				if(!i.getPhotoId().equals(this.photoId))
 					this.thumbnailIds.add(i.getPhotoId());
 			}
 			
 			
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public String getPhotoId() {
 		return photoId;
 	}
 
 	public void setPhotoId(String photoId) {
 		this.photoId = photoId;
 	}
 
 	public String getUserNick() {
 		return userNick;
 	}
 
 	public void setUserNick(String userNick) {
 		this.userNick = userNick;
 	}
 	
 	public List<String> getDefaultPhotoSizes() {
 		return defaultPhotoSizes;
 	}
 
 	public void setDefaultPhotoSizes(List<String> defaultPhotoSizes) {
 		this.defaultPhotoSizes = defaultPhotoSizes;
 	}
 	
 	public String getThumbnailDatabase() {
 		return thumbnailDatabase;
 	}
 
 	public void setThumbnailDatabase(String thumbnailDatabase) {
 		this.thumbnailDatabase = thumbnailDatabase;
 	}
 
 	public List<String> getThumbnailIds() {
 		return thumbnailIds;
 	}
 
 	public void setThumbnailIds(List<String> thumbnailIds) {
 		this.thumbnailIds = thumbnailIds;
 	}
 
 	public String getThumbnailBucket() {
 		return thumbnailBucket;
 	}
 
 	public void setThumbnailBucket(String thumbnailBucket) {
 		this.thumbnailBucket = thumbnailBucket;
 	}
 
 	public String getDetailedPhotoURL() {
 		return detailedPhotoURL;
 	}
 
 	public void setDetailedPhotoURL(String detailedPhotoURL) {
 		this.detailedPhotoURL = detailedPhotoURL;
 	}
 
 	public Photo getPhoto() {
 		return photo;
 	}
 
 	public void setPhoto(Photo photo) {
 		this.photo = photo;
 	}
 	
 	private ParamsBean getPrettyfacesParams()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ParamsBean pb = (ParamsBean) context.getApplication().evaluateExpressionGet(context, "#{paramsBean}", ParamsBean.class);
 		return pb;
 	}
 	
 	
 
 }
