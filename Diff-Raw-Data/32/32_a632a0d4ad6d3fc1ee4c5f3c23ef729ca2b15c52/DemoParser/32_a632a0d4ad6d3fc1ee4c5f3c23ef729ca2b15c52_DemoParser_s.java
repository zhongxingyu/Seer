 package org.racenet.racesow;
 
 import java.io.DataInputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.util.Map.Entry;
 
 import org.racenet.framework.FileIO;
 import org.racenet.racesow.models.DemoKeyFrame;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentSkipListMap;
 
 /**
  * Class to parse r2d demo files
  * 
  * @author so#zolex
  *
  */
 public class DemoParser {
 
 	String map;
 	ConcurrentSkipListMap demoParts = new ConcurrentSkipListMap();
 	
 	/**
 	 * Parse the given demo string
 	 * 
 	 * @param String demo
 	 */
 	public boolean parse(String demo) {
 		
 		try {
 
 			DataInputStream dis = new DataInputStream(FileIO.getInstance().readFile(demo));
 			this.map = dis.readUTF();
 			while (dis.available() > 0) {
 			
 				DemoKeyFrame frame = new DemoKeyFrame();
 				frame.frameTime = dis.readFloat();
 				frame.playerPosition.x = dis.readFloat();
 				frame.playerPosition.y = dis.readFloat();
 				frame.playerAnimation = dis.readInt();
 				frame.playerSpeed = dis.readInt();
 				frame.mapTime = dis.readFloat();
 				frame.playerSound = dis.readInt();
 				frame.decalType = dis.readUTF();
 				frame.decalX = dis.readFloat();
 				frame.decalY = dis.readFloat();
 				
 				this.demoParts.put(frame.frameTime, frame);
 			}
 			
 			dis.close();
 			return true;
 			
 		} catch (IOException e) {
 			
 			return false;
 		}
 	}
 	
 	/**
 	 * Get the best matching keyframe for the given frameTime
 	 * 
 	 * @param float time
 	 * @return DemoKeyFrame
 	 */
 	public DemoKeyFrame getKeyFrame(float time) {
 		
 		@SuppressWarnings("unchecked")
 		Entry<Float, DemoKeyFrame> frame = this.demoParts.lowerEntry(time);
 		if (frame != null) {
 			
 			return frame.getValue();
 		}
 		
 		return null;
 	}
 }
