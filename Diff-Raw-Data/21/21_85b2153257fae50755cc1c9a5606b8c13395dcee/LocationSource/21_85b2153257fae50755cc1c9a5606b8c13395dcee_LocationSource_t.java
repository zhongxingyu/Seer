 package myhomeaudio.server.source;
 
 import java.util.ArrayList;
 
 import myhomeaudio.server.media.descriptor.MediaDescriptor;
 
 
public abstract class LocationSource extends SourceBase {
 	
	public MediaDescriptor getMedia(MediaDescriptor descriptor) {
		return new MediaDescriptor(0, "", "", "", "", "");
 	}
 	
 	public ArrayList<MediaDescriptor> searchMedia(String search) {
 		return new ArrayList<MediaDescriptor>();
 	}
 	
 	
 }
