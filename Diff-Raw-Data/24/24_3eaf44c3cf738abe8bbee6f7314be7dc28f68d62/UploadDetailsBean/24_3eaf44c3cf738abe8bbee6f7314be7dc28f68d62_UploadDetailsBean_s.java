 package org.mdissjava.mdisscore.view.upload;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AjaxBehaviorEvent;
 
 import org.mdissjava.commonutils.photo.status.PhotoStatusManager;
 import org.mdissjava.commonutils.properties.PropertiesFacade;
 import org.mdissjava.mdisscore.controller.bll.AlbumManager;
 import org.mdissjava.mdisscore.controller.bll.impl.AlbumManagerImpl;
 import org.mdissjava.mdisscore.controller.bll.impl.PhotoManagerImpl;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.model.pojo.Album;
 import org.mdissjava.mdisscore.view.params.ParamsBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.google.code.morphia.Datastore;
 
 @ViewScoped
 @ManagedBean
 public class UploadDetailsBean {
 
 	
 	private Datastore datastore;	
 	private AlbumManager albumManager;
 	private PhotoManagerImpl photoManager;
 	private PhotoStatusManager photoStatusManager;
 	
 	private String userNick;
 	private String imageID;
 	private String title;
 	private String titleResult;
 	private String tags; //need to tokenize
 	private HashMap<String, Boolean> publicPhotoList;
 	private boolean publicPhotoScope;
 	private boolean plus18;
 	private String album;
 	private String license;
 	private String newAlbumTitle;
 	private boolean formButtonDisabled;
 	private HashMap<String, String> albums;
 	private HashMap<String, String> licenses;
 	private String imageURL;
 	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
 
 	public UploadDetailsBean() {
 		this.datastore = MorphiaDatastoreFactory.getDatastore("mdissphoto");
 		
 		logger.info("Creating Upload details instance");
 		
 		//load the needed data (photo ID and set the button form to enabled)
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		ParamsBean pb = (ParamsBean) facesContext.getApplication().evaluateExpressionGet(facesContext, "#{paramsBean}", ParamsBean.class);	
 		
 		this.imageID = pb.getPhotoId();
 		this.formButtonDisabled = false;
 	
 		//Get the user from session
 		this.retrieveSessionUserNick();
 		
 		
 		this.photoManager = new PhotoManagerImpl(datastore);
 		this.photoStatusManager= new PhotoStatusManager(datastore);
 		String outcome = null;
 				
 		//TODO: Is detailed the photo already? if yes then redirect to the final photo 
 		//web page, if no the prepare the form
 		try {
 			if(photoStatusManager.needsToBeDetailed(this.imageID))
 			{
 				//show the form
 				this.prepareForm();
 			}else
 			{
 				//redirect to the final photo
 				ParamsBean params = getPrettyfacesParams();
 				params.setPhotoId(this.imageID);
 				params.setUserId(this.userNick);
 				
 				outcome = "pretty:photo_detail";
 				facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, outcome);
 				
 			}
 		} catch (IOException e) {
 			//to error!
 			ParamsBean params = getPrettyfacesParams();
 			params.setPhotoId(this.imageID);
 			params.setUserId(this.userNick);
 			
 			outcome = "pretty:user_upload_error";
 			facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, outcome);
 			
 		}
 	}
 
 	private void retrieveSessionUserNick() {
 		//Get the current logged user's username
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		this.userNick = auth.getName();
 		
 	}
 
 	private void prepareForm()
 	{
 		try {
 			System.out.println("New photo :)");
 			//TODO: load metadata (Maite)
 			
 			
 			//TODO:Load the best fitting image (320 -> 240 -> 150 -> 100 -> 75 -> 30)
 			
 			//Load from properties
 			Properties globals = new PropertiesFacade().getProperties("globals");
 			Properties scales = new PropertiesFacade().getProperties("thumbnails");
			String db  = globals.getProperty("morphia.db");
 			String bucket  = scales.getProperty("thumbnail.scale.320px.bucket.name");
 			
 			//TODO: Load the image url
 			this.imageURL = "/dynamic/image?db="+db+"&amp;bucket="+bucket+"&amp;id="+this.imageID;
 			
 			//set the radiobuttons for public and private
 			this.publicPhotoList = new HashMap<String, Boolean>();
 			this.publicPhotoList.put("Public", true);
 			this.publicPhotoList.put("Private", false);
 			
 			//Load the albums
 			this.loadAlbums();
 			
 			//TODO: Load the licenses
 			this.licenses = new HashMap<String, String>();
 			this.licenses.put("CC 2.0", "CC 2.0");
 			this.licenses.put("GPL", "GPL");
 			this.licenses.put("AGPL", "AGPL");
 			this.licenses.put("C", "C");
 			this.licenses.put("Apache", "Apache");
 			
 			//default public
 			this.publicPhotoScope = true;
 		
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void saveDetails()
 	{
 		//TODO: Check again public +18... the user could disable javascript in the browser... ¬¬
 		try {
 			this.photoManager.insertPhoto(this.imageID, this.userNick, this.title, 
 											this.album, this.publicPhotoScope, this.plus18, 
 											this.license, this.tags);
 			
 			//Set status to detailed
 			this.photoStatusManager.markAsDetailed(this.imageID);
 			
 			//redirect to the final photo
 			ParamsBean params = getPrettyfacesParams();
 			params.setPhotoId(this.imageID);
 			params.setUserId(this.userNick);
 			
 			String outcome = "pretty:photo_detail";
 			FacesContext facesContext =  FacesContext.getCurrentInstance();
 			facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, outcome);
 			
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	public void newAlbum()
 	{
 		//add the album to database
 		try {
 			this.albumManager.insertAlbum(this.newAlbumTitle, userNick);
 			//load the album directly in the map (we don't want to load all the other albums again) 
 			this.albums.put(this.newAlbumTitle, this.newAlbumTitle);
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"OK", "Album " + this.newAlbumTitle + " has been created"));
 		} catch (IOException e) {
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error", "Error creating " + this.newAlbumTitle + " album"));
 		}
 	}
 	
 	/**
 	 * unchecks public checkbox if +18 is active and viceversa
 	 * 
 	 * @param event
 	 */
 	public void plus18Validator(AjaxBehaviorEvent event)
 	{
 		String component = event.getComponent().getId();
 		
 		//if plus18 component has triggered the event then plublic needs to be updated
 		//otherwise public has triggered the event and plus18 needs to be updated
 		if (component.equals("plus18"))
 		{	
 			if (this.plus18 && this.publicPhotoScope)
 			{
 				this.publicPhotoScope= false;
 				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Public is marked", "Check private scope if you want a +18 photo"));
 			}
 			
 		}else
 		{
 			if (this.plus18 && this.publicPhotoScope)
 			{
 				this.plus18 = false;
 				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"+18 is marked", "Uncheck +18 if you want public scope for the photo"));
 			}
 		}
 	}
 
 	public void titleValidator(AjaxBehaviorEvent event)
 	{  
 		System.out.println("Checking name: "+this.title);
 		//TODO: Check if the image name exists already 
 		if (this.title.contains("info"))
 		{
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"OK", null));  
 		}
 		if (this.title.contains("error"))
 		{
 			this.formButtonDisabled = true;
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error", "Duplicated name"));
 		}else
 		{
 			this.formButtonDisabled = false;
 		}
 	}
 	
 	//utilities
 	private void loadAlbums()
 	{
 		this.albumManager = new AlbumManagerImpl();
 		List<Album> albums = this.albumManager.findUserAlbums(userNick);
 		this.albums = new HashMap<String, String>();
 		
 		for (Album i: albums)
 		{
 			this.albums.put(i.getTitle(), i.getTitle());
 		}
 	}
 
 	public HashMap<String, Boolean> getPublicPhotoList() {
 		return publicPhotoList;
 	}
 
 	public void setPublicPhotoList(HashMap<String, Boolean> publicPhotoList) {
 		this.publicPhotoList = publicPhotoList;
 	}
 
 	public boolean isPublicPhotoScope() {
 		return publicPhotoScope;
 	}
 
 	public void setPublicPhotoScope(boolean publicPhotoScope) {
 		this.publicPhotoScope = publicPhotoScope;
 	}
 
 	public String getUserNick() {
 		return userNick;
 	}
 
 	public void setUserNick(String userNick) {
 		this.userNick = userNick;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 
 	public String getTitleResult() {
 		return titleResult;
 	}
 
 
 	public void setTitleResult(String titleResult) {
 		this.titleResult = titleResult;
 	}
 
 
 	public String getTags() {
 		return tags;
 	}
 
 
 	public void setTags(String tags) {
 		this.tags = tags;
 	}
 
 
 	public boolean isPlus18() {
 		return plus18;
 	}
 
 
 	public void setPlus18(boolean plus18) {
 		this.plus18 = plus18;
 	}
 
 
 	public String getAlbum() {
 		return album;
 	}
 
 
 	public void setAlbum(String album) {
 		this.album = album;
 	}
 
 
 	public String getLicense() {
 		return license;
 	}
 
 
 	public void setLicense(String license) {
 		this.license = license;
 	}
 
 
 	public HashMap<String, String> getAlbums() {
 		return albums;
 	}
 
 
 	public void setAlbums(HashMap<String, String> albums) {
 		this.albums = albums;
 	}
 
 
 	public HashMap<String, String> getLicenses() {
 		return licenses;
 	}
 
 
 	public void setLicenses(HashMap<String, String> licenses) {
 		this.licenses = licenses;
 	}
 
 
 	public String getImageURL() {
 		return imageURL;
 	}
 
 
 	public void setImageURL(String imageURL) {
 		this.imageURL = imageURL;
 	}
 
 	public String getNewAlbumTitle() {
 		return newAlbumTitle;
 	}
 
 	public void setNewAlbumTitle(String newAlbumTitle) {
 		this.newAlbumTitle = newAlbumTitle;
 	}
 
 	public boolean isFormButtonDisabled() {
 		return formButtonDisabled;
 	}
 
 	public void setFormButtonDisabled(boolean formButtonDisabled) {
 		this.formButtonDisabled = formButtonDisabled;
 	}
 	
 	
 	private ParamsBean getPrettyfacesParams()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ParamsBean pb = (ParamsBean) context.getApplication().evaluateExpressionGet(context, "#{paramsBean}", ParamsBean.class);
 		return pb;
 	}
 	
 }
