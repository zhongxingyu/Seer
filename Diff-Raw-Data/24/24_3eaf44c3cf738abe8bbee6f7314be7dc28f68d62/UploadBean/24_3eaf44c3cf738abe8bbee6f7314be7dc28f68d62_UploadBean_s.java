 package org.mdissjava.mdisscore.view.upload;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 import org.mdissjava.commonutils.mongo.gridfs.GridfsDataStorer;
 import org.mdissjava.commonutils.photo.status.PhotoStatusManager;
 import org.mdissjava.commonutils.properties.PropertiesFacade;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.view.params.ParamsBean;
 import org.primefaces.event.FileUploadEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.Datastore;
 
 @RequestScoped
 @ManagedBean
 public class UploadBean {
 	
 	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 	private String imageId;
 	private String userId;
 	private GridfsDataStorer gfs = null;
	private final String DATABASE = "mdissphoto";
 	
 	public UploadBean()
 	{
 		try {
 			ParamsBean pb = getPrettyfacesParams();
 			this.userId = pb.getUserId();
 			//TODO: Load from properties
 			
 			Properties globals;
 			globals = new PropertiesFacade().getProperties("globals");
			String db  = globals.getProperty("morphia.db");
			String bucket  = globals.getProperty("original");
 	
			gfs = new GridfsDataStorer(db, bucket);
 			
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public String getImageId() {
 		return imageId;
 	}
 
 	public void setImageId(String imageId) {
 		this.imageId = imageId;
 	}
 
 	public String getUserId() {
 		return userId;
 	}
 
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 
 	public void handleFileUpload(FileUploadEvent event) throws IOException
 	{
 		
 		String outcome = null;
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		
 		try{
 			//Save in database
 			this.logger.info("Entering the upload");
 			InputStream data =  event.getFile().getInputstream();
 			this.imageId = gfs.saveData(data);
 			this.logger.info("upload and stored: {}", this.imageId);
 			
 			//set the new status to the photo
			Datastore ds = MorphiaDatastoreFactory.getDatastore(this.DATABASE);
 			PhotoStatusManager photoManager = new PhotoStatusManager(ds);
 			photoManager.createPhotoStatus(this.imageId);
 			
 			//set the paramas in the url
 			ParamsBean params = getPrettyfacesParams();
 			params.setPhotoId(this.imageId);
 			params.setUserId(this.userId);
 		
 			outcome = "pretty:user_upload_processing";
 			
 		}catch(Exception e){
 			
 			outcome = "pretty:user_upload_error";
 		
 		}finally{
 			//call
 			facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, outcome);
 		}
 
 	}
 	
 	
 	
 	private ParamsBean getPrettyfacesParams()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ParamsBean pb = (ParamsBean) context.getApplication().evaluateExpressionGet(context, "#{paramsBean}", ParamsBean.class);
 		return pb;
 	}
 
 }
