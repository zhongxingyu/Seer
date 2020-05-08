 package org.fourdnest.androidclient.test.services;
 
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.Tag;
 import org.fourdnest.androidclient.comm.Protocol;
 import org.fourdnest.androidclient.comm.ProtocolResult;
 
 public class SendQueueTestProtocol implements Protocol {
 	
 	public int getProtocolId() {
 		return 1024;
 	}
 	public ProtocolResult sendEgg(Egg egg) {
 		return new ProtocolResult(null, 200);
 	}
 	public void setNest(Nest nest) {
 	}
 	public List<Tag> topTags(int count) {
 		return null;
 	}
 	public Egg getEgg(String uid) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	public List<Egg> getStream(int size) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	public boolean getMediaFile(String uri, String localPath) {
 		return false;
 	}
 	
 	public ProtocolResult overwriteEgg(Egg egg) {
 		return new ProtocolResult(null, 200);
 	}
 	@Override
 	public boolean getThumbnail(Egg egg, String size) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	@Override
 	public boolean getMedia(Egg egg) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	@Override
 	public String getHelpURL() {
 		// TODO Auto-generated method stub
 		return null;
 	}
	@Override
	public boolean getRelativeMediaFile(String uri, String localPath) {
		// TODO Auto-generated method stub
		return false;
	}
 
 }
 
