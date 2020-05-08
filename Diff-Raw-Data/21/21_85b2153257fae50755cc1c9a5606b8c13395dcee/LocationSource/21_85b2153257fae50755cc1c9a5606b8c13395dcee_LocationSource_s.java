 package myhomeaudio.server.source;
 
 import java.util.ArrayList;
 
 import myhomeaudio.server.media.descriptor.MediaDescriptor;
 
 
public class LocationSource extends SourceBase {
 	
	public String getMedia(MediaDescriptor descriptor) {
		return "";
 	}
 	
 	public ArrayList<MediaDescriptor> searchMedia(String search) {
 		return new ArrayList<MediaDescriptor>();
 	}
 	
 	
 }
