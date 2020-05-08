 package app.managedBean;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import org.apache.log4j.Logger;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.event.TransferEvent;
 import org.primefaces.model.DualListModel;
 
 import com.qotsa.exception.InvalidHandle;
 import com.qotsa.exception.InvalidParameter;
 import com.qotsa.jni.controller.WinampController;
 
 import app.util.FileUtils;
 import app.util.WinampUtils;
 
 @ManagedBean(name = "djController")
 @SessionScoped
 public class DjController {
 	private static final Logger log = Logger.getLogger(DjController.class);
 	
 	private String textStatus;
 	private String prompt;
 	private String playingMusic;
 	
 	// Modifier this class (DualListModel) ;
 	private DualListModel<String> songs;
 
 	public DjController() {
 		//start winamp
 		WinampUtils.playerControl("run");
 		
 		//preparing DualListModel
 		List<String> sourceSong = new ArrayList<String>();
 		List<String> targetSong = FileUtils.getInstance().getMusicListFromDirectory();
 		
 		songs = new DualListModel<String>(sourceSong, targetSong);
 		
 		this.prompt = "Winamp : ";
 	}
 
 	public DualListModel<String> getSongs() {
 		return songs;
 	}
 	
 	public void setSongs(DualListModel<String> songs) {
 		this.songs = songs;
 	}
 	
     public void onTransfer(TransferEvent event) {  
         StringBuilder builder = new StringBuilder();  
         for(Object item : event.getItems()) {  
         	String name = (String) item;
            builder.append(name).append("<br />");
             try {
				WinampController.appendToPlayList(FileUtils.ABSOLUTEPATH_THE_GREAN_GANG_RADIO+name);
				WinampController.refreshPlayListCache();
			} catch (InvalidHandle e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidParameter e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
         }  
        
        
          
         FacesMessage msg = new FacesMessage();  
         msg.setSeverity(FacesMessage.SEVERITY_INFO);  
         msg.setSummary("Items Transferred");  
         msg.setDetail(builder.toString());  
           
        FacesContext.getCurrentInstance().addMessage(null, msg);  
     } 	
 	
 	public String getTextStatus() {
 		return textStatus;
 	}
 
 	public void setTextStatus(String textStatus) {
 		this.textStatus = textStatus;
 	}
 
 	public String getPrompt() {
 		return prompt;
 	}
 
 	public void setPrompt(String prompt) {
 		this.prompt = prompt;
 	}
 
 	public String getPlayingMusic() {
 		try {
 			playingMusic = WinampController.getFileNamePlaying();
 		} catch (InvalidHandle e) {
 			log.error("Error in getPlayingMusic", e);
 		}
 		return playingMusic;
 	}
 
 	public void setPlayingMusic(String playingMusic) {
 		this.playingMusic = playingMusic;
 	}	
 	
 	public void playerControl(String clickedButton) {
 		textStatus = WinampUtils.playerControl(clickedButton);
 	}
 	
     public void handleFileUpload(FileUploadEvent event) {
 		log.debug("Enter handleFileUpload");
 		try {
 			// must encoded on xhtml page before
 			String fileName = event.getFile().getFileName();
 			InputStream inputStream = event.getFile().getInputstream();
 			
 			FileUtils.getInstance().copyFile(fileName, inputStream);
 
 			//show message dialog to uploader
 			FacesMessage msg = new FacesMessage("Success! ", fileName + " is uploaded.");
 			FacesContext.getCurrentInstance().addMessage(null, msg);
 
 		} catch (IOException e) {
 			log.error("Error in handleFileUpload", e);
 		} finally {
 			log.debug("Quit handleFileUpload");
 		}
 	}
 }
