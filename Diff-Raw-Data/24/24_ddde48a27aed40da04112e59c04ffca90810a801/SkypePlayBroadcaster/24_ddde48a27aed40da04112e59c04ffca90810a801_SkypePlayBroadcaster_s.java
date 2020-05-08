 package skype;
 
 import com.skype.SkypeException;
 import com.skype.Stream;
 import com.thoughtworks.xstream.XStream;
 
 import sliceWars.RemotePlay;
 import sliceWars.RemotePlayListener;
 
 public class SkypePlayBroadcaster implements RemotePlayListener{
 
 	private RemotePlayListener local;
 	private Stream stream;
 
 	public void setLocalPlayer(RemotePlayListener local){
 		this.local = local;
 		
 	}
 	
 	public void setSkypePlayerStream(Stream stream){
 		this.stream = stream;
		
 	}
 	
 	@Override
 	public void play(RemotePlay play) {
		if(local != null)
			local.play(play);
 		if(stream != null){
 			XStream xstream = new XStream();
 			String xml = xstream.toXML(play);
 			try {
 				stream.write(xml);
 			} catch (SkypeException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 }
