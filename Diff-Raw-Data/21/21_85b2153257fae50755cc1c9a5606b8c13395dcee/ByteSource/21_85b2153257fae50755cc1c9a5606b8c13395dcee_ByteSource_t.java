 package myhomeaudio.server.source;
 
 import java.util.ArrayList;
 
 import myhomeaudio.server.media.descriptor.MediaDescriptor;
 
 /**
  * 
  * @author Cameron
  *
  */
 public abstract class ByteSource extends SourceBase {
 	
 	public MediaDescriptor getMedia(MediaDescriptor descriptor) {
 		return new MediaDescriptor(0, "", "", "", "", new byte[0]);
	}
 	
 	public ArrayList<MediaDescriptor> searchMedia(String search) {
 		return new ArrayList<MediaDescriptor>();
 	}
 	
 }
