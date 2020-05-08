 package org.icemobile.samples.spring.mediacast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.web.context.ServletContextAware;
 
 @Service
 public class MediaStore implements ServletContextAware {
 	
	private List<MediaMessage> media = new ArrayList<MediaMessage>();
 	private static final int CAROUSEL_IMG_HEIGHT = 96;
 	private static final int CAROUSEL_IMG_WIDTH = 92;
 	private static final String CAROUSEL_ITEM_MARKUP = 
 			"<div style='overflow:hidden;width:175px;height:98px;'><img height='"+CAROUSEL_IMG_HEIGHT+"' src='%1$s/resources/uploads/%2$s' style='border:none;' title='%3$s' width='"+CAROUSEL_IMG_WIDTH+"'></div><a class='view-play-icon' href='%1$s/media/%4$s' ><img src='%1$s/resources/images/view-icon.png' style='border:none;'></a>";
 	private String contextPath;
 	
 	private static final Log log = LogFactory
 			.getLog(MediaStore.class);
 	
 	public List<MediaMessage> getMedia(){
 		return media;
 	}
 	
 	public void setMedia(List<MediaMessage> media) {
 		this.media = media;
 	}
 	
 	public void setContextPath(String contextPath){
 		this.contextPath = contextPath;
 	}
 	
 	public List<String> getMediaImageMarkup(){
     	List<String> imageMarkup = new ArrayList<String>();
    	if( media != null ){
	    	for( MediaMessage mediaMsg : media ){
	    		imageMarkup.add(String.format(CAROUSEL_ITEM_MARKUP, contextPath, mediaMsg.getPhoto().getFileName(), mediaMsg.getTitle(), mediaMsg.getId()));
	    	}
     	}
     	return imageMarkup;
     }
 
 	public MediaMessage getMediaMessage(String id){
 		MediaMessage result = null;
 		for( MediaMessage msg: media){
 			if( msg.getId().equals(id)){
 				result = msg;
 				break;
 			}
 		}
 		return result;
 	}
 	
 	public void removeMessage(String id){
 		for( MediaMessage msg: media){
 			if( msg.getId().equals(id)){
 				media.remove(msg);
 				break;
 			}
 		}
 	}
 
 	public void setServletContext(ServletContext context) {
 		this.contextPath = context.getContextPath();
 	}
 	
 
 }
