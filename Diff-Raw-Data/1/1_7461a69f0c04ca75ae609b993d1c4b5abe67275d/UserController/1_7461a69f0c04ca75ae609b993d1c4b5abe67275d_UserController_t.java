 package app.managedBean;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.event.TransferEvent;
 import org.primefaces.model.DualListModel;
 import org.primefaces.push.PushContext;
 import org.primefaces.push.PushContextFactory;
 
 import com.qotsa.exception.InvalidHandle;
 import com.qotsa.jni.controller.WinampController;
 
 import app.util.Constants;
 import app.util.FileUtils;
 import app.util.WinampUtils;
 
 @ManagedBean
 @SessionScoped
 public class UserController {
 	private static final Logger log = Logger.getLogger(UserController.class);
 	
 	HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
 	ServletContext context = request.getSession().getServletContext();
 	PushContext pushContext = PushContextFactory.getDefault().getPushContext();
 	
 	private String playingImage;
 	private String playingMusic;
 	private String promptTextHost;
 	private String hostAddress;
 
 	// Modifier this class (DualListModel) ;
 	private DualListModel<String> songs;
 
 	public UserController() {
 		
 		//preparing DualListModel
 		List<String> sourceSongs = new ArrayList<String>();
 		List<String> targetSongs = FileUtils.getInstance().getMusicListFromDirectory();
 		
 		songs = new DualListModel<String>(sourceSongs, targetSongs);
 
 		promptTextHost = "Connect your music player to : ";
 	}
 
 	public DualListModel<String> getSongs() {
 		songs = (DualListModel<String>) context.getAttribute(Constants.ATTRIBUTE_DUAL_LIST_MODEL_SONGS);
 		return songs;
 	}
 	
 	public void setSongs(DualListModel<String> songs) {
 		this.songs = songs;
 	}
 	
 	/**
 	 * when transferring item(s) in PickList<BR />
 	 * <p>
 	 * Source - Winamp Playlist <BR/>
 	 * Destination - Music Directory
 	 * </p>
 	 * 
 	 * @param event object of transferring
 	 */
     public void onTransfer(TransferEvent event) {  
     	log.debug("Enter onTransfer");
     	
     	if(event.isAdd()) {
     		//add=true is transfer source to destination
     		log.debug("removing Winamp Playlist(transferring source to destination)");
     		
    		pushContext.push(Constants.CHANNEL_REFRESH_PICKLIST, Constants.STRING_VALUE_1);
     	} else {
     		//add=false is transfer destination to source ( can change to event.isRemove() )
     		log.debug("adding music(s) to Winamp Playlist(transferring destination to source)");
         	
             StringBuilder builder = new StringBuilder();
             for(Object item : event.getItems()) {
             	String fileName = (String) item;
                 builder.append(fileName).append("<BR/>");
                 WinampUtils.appendFileToPlaylist(fileName);
             }  
               
             //set to ServletContext for using the same list for all users
     		context.setAttribute(Constants.ATTRIBUTE_DUAL_LIST_MODEL_SONGS, (DualListModel<String>) songs);
             
     		pushContext.push(Constants.CHANNEL_REFRESH_PICKLIST, Constants.STRING_VALUE_1);
     		
             FacesMessage msg = new FacesMessage();  
             msg.setSeverity(FacesMessage.SEVERITY_INFO);  
             msg.setSummary("Music added to playlist");  
             msg.setDetail(builder.toString());  
             
             FacesContext.getCurrentInstance().addMessage(null, msg);
             
     	}
     	
         log.debug("Quit onTransfer");
     } 	
 	
 	public String getPromptTextHost() {
 		return promptTextHost;
 	}
 
 	public String getHostAddress() {
 		String serverIP = request.getLocalAddr();
 		hostAddress = "http://"+serverIP+":8000/";
 		
 		return hostAddress;
 	}
 	
 	/**
 	 * handle upload, copy file to main directory, check duplicate before update pickList 
 	 * <BR/>and send message to uploader
 	 * 
 	 * @param event
 	 */
     public void handleFileUpload(FileUploadEvent event) {
 		log.debug("Enter handleFileUpload");
 		try {
 			// must encoded on xhtml page before
 			String fileName = event.getFile().getFileName();
 			InputStream inputStream = event.getFile().getInputstream();
 			
 			FileUtils.getInstance().copyFile(fileName, inputStream);
 
 			//update list
 			List<String> directory = songs.getTarget();
 			boolean isAlready = false;
 			for(String file : directory){
 				if(file.equals(fileName)) isAlready = true;
 			}
 			FacesMessage msg;
 			if( !isAlready ) {
 				directory.add(fileName);
 				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Uploaded!", fileName);
 			} else {
 				msg = new FacesMessage(FacesMessage.SEVERITY_WARN,"Already Exist!", fileName);
 			}
 			songs.setTarget(directory);
 			
 			//show message dialog to uploader
 			FacesContext.getCurrentInstance().addMessage(null, msg);
 			
 			pushContext.push(Constants.CHANNEL_REFRESH_PICKLIST, Constants.STRING_VALUE_1);
 			
 
 		} catch (IOException e) {
 			log.error("Error in handleFileUpload", e);
 		} finally {
 			log.debug("Quit handleFileUpload");
 		}
 	}
     
 	public String getPlayingImage() {
 		try {
 			if(WinampController.getStatus() == WinampController.PLAYING) {
 				playingImage = Constants.IMAGESOURCE_BABY_DANCE_GIF;
 			} else {
 				playingImage = Constants.IMAGESOURCE_BABY_DANCE_JPG;
 			}
 			
 		} catch (InvalidHandle e) {
 			log.error("Error in getPlayingImage", e);
 		}		
 		return playingImage;
 	}
 
 	public void setPlayingImage(String playingImage) {
 		this.playingImage = playingImage;
 	}
 	
 	public String getPlayingMusic() {
 		playingMusic = WinampUtils.getFileNamePlaying();
 		return playingMusic;
 	}
 
 	public void setPlayingMusic(String playingMusic) {
 		this.playingMusic = playingMusic;
 	}
 }
