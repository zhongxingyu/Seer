 package app.managedBean;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.event.TransferEvent;
 import org.primefaces.model.DualListModel;
 import org.primefaces.push.PushContext;
 import org.primefaces.push.PushContextFactory;
 
 import app.util.Constants;
 import app.util.FileUtils;
 import app.util.ThreadMonitorWinamp;
 import app.util.WinampUtils;
 
 import com.qotsa.exception.InvalidHandle;
 import com.qotsa.jni.controller.WinampController;
 
 @ManagedBean
 @SessionScoped
 public class DjController implements ServletContextListener{
 	private static final Logger log = Logger.getLogger(DjController.class);
 
 	HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
 	ServletContext context = request.getSession().getServletContext();
 	PushContext pushContext = PushContextFactory.getDefault().getPushContext();	
 	
 	private String playingImage;
 	private String playingMusic;
 	private String promptTextHost;
 	private String hostAddress;
 	private boolean playing;
 	private boolean stopping;
 	private ThreadMonitorWinamp threadMonitorWinamp;
 
 	// Modifier this class (DualListModel) ;
 	private DualListModel<String> songs;
 
 	public DjController() {
 		
 		threadMonitorWinamp = new ThreadMonitorWinamp();
 		threadMonitorWinamp.start();
 		
 		String remoteIP = request.getRemoteAddr();
 		if( !Constants.IP_LOCALHOST.equals(remoteIP) ) {
 			FacesContext fc = FacesContext.getCurrentInstance();			
 			ExternalContext ec = fc.getExternalContext();
 			try {
 				ec.redirect(Constants.PAGE_LANDING);
 			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
 		}
 		
 //		WinampUtils.clearWinampPlayList();
 		
 		//preparing DualListModel
 		List<String> sourceSongs = new ArrayList<String>();
 		List<String> targetSongs = FileUtils.getInstance().getMusicListFromDirectory();
 		
 		songs = new DualListModel<String>(sourceSongs, targetSongs);
 
 		context.setAttribute(Constants.ATTRIBUTE_DUAL_LIST_MODEL_SONGS, (DualListModel<String>) songs);
 		
 		promptTextHost = "Share this to users : ";
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
 //    		
 //    		List<String> sourceSongs = songs.getSource();
 //    		
 //    		sourceSongs.remove(playingMusic);
 //    		
 //    		context.setAttribute(Constants.ATTRIBUTE_DUAL_LIST_MODEL_SONGS, songs);
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
 		try {
 			String serverIP = InetAddress.getLocalHost().getHostAddress();
 			hostAddress = "http://"+serverIP+":7810/TheGreanGangRadio/";
 			
 		} catch (UnknownHostException e) {
 			log.error("Error in getHostAddress",e);
 		}
 		return hostAddress;
 	}
 	public void playOrPause() {
 		if(playing) {
 			WinampUtils.playerControl(WinampUtils.PLAYER_ACTION_PLAY);
 		} else {
 			WinampUtils.playerControl(WinampUtils.PLAYER_ACTION_PAUSE);
 		}
 	}
 
 	public String getPlayingImage() {
 		try {
 			if(WinampController.getStatus() == WinampController.PLAYING) {
 				playingImage = Constants.IMAGESOURCE_BABY_DANCE_GIF;
 			} else {
 				playingImage = Constants.IMAGESOURCE_BABY_DANCE_JPG;
 			}
 			
 			//push to client(user)
 			pushContext.push(Constants.CHANNEL_PLAYING_IMAGE, "/TheGreanGangRadio"+playingImage);
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
 	
 	public void playerControl(String clickedButton) {
 		WinampUtils.playerControl(clickedButton);
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
     
 	public boolean isPlaying() {
 		if(stopping) playing = false;
 		return playing;
 	}
 
 	public void setPlaying(boolean playing) {
 		this.playing = playing;
 	}
 
 	public boolean isStopping() {
 		if(playing) stopping = false;
 		return stopping;
 	}
 
 	public void setStopping(boolean stopping) {
 		this.stopping = stopping;
 	}
 	
 	public void refreshDirectory() {
 		List<String> musics = FileUtils.getInstance().getMusicListFromDirectory();
 		songs.setTarget(musics);
 		
 		context.setAttribute(Constants.ATTRIBUTE_DUAL_LIST_MODEL_SONGS, songs);
 		pushContext.push(Constants.CHANNEL_REFRESH_PICKLIST, Constants.STRING_VALUE_1);
 	}
 
 	public void setPlayListRemoved() {
 		List<String> sourceSongs = songs.getSource();
 		
		sourceSongs.remove(playingMusic);
 		
 		context.setAttribute(Constants.ATTRIBUTE_DUAL_LIST_MODEL_SONGS, songs);
 		pushContext.push(Constants.CHANNEL_REFRESH_PICKLIST, Constants.STRING_VALUE_1);
 	}
 	
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0) {
 		threadMonitorWinamp.interrupt();
 	}
 
 	@Override
 	public void contextInitialized(ServletContextEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 		
 }
